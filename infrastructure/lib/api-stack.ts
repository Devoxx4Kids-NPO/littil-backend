import * as cdk from 'aws-cdk-lib';
import { CfnOutput } from 'aws-cdk-lib';
import { Certificate } from 'aws-cdk-lib/aws-certificatemanager';
import { InstanceClass, InstanceSize, InstanceType, Peer, Port, SecurityGroup, Vpc } from 'aws-cdk-lib/aws-ec2';
import { Repository } from 'aws-cdk-lib/aws-ecr';
import { ContainerImage, Secret } from 'aws-cdk-lib/aws-ecs';
import { ApplicationLoadBalancedFargateService } from 'aws-cdk-lib/aws-ecs-patterns';
import { Credentials, DatabaseInstance, DatabaseInstanceEngine, MariaDbEngineVersion, } from 'aws-cdk-lib/aws-rds';
import { DatabaseInstanceProps } from 'aws-cdk-lib/aws-rds/lib/instance';
import { Secret as SecretsManagerSecret } from 'aws-cdk-lib/aws-secretsmanager';
import { Construct } from 'constructs';
import { data } from "aws-cdk/lib/logging";

export interface ApiStackProps extends cdk.StackProps {
    ecrRepository: Repository;
    certificate: Certificate;
}

export class ApiStack extends cdk.Stack {
    constructor(scope: Construct, id: string, props: ApiStackProps) {
        super(scope, id, props);

        const vpc = new Vpc(this, 'LittilBackendVpc', {
            maxAzs: 2,
            natGateways: 1,
        });

        /* Database. */
        const databaseName = 'LittilDatabase';

        const littilDatabaseSecretName = 'littil/backend/databaseCredentials';
        const littilBackendDatabaseSecret = SecretsManagerSecret.fromSecretNameV2(this, 'LittilBackendDatabaseSecret', littilDatabaseSecretName);

        const databaseProperties: DatabaseInstanceProps = {
            databaseName,
            credentials: Credentials.fromSecret(littilBackendDatabaseSecret),
            publiclyAccessible: false,
            vpc,
            engine: DatabaseInstanceEngine.mariaDb({
                version: MariaDbEngineVersion.VER_10_6_8,
            }),
            instanceType: InstanceType.of(
                InstanceClass.T4G,
                InstanceSize.MICRO,
            ),
        };

        const database = new DatabaseInstance(this, 'LittilApiDatabase', databaseProperties);
        new CfnOutput(this, 'dbEndpoint', {value: database.instanceEndpoint.hostname});

        /* Fargate. */
        const littilBackendSecret = SecretsManagerSecret.fromSecretCompleteArn(this, 'LittilBackendSecret', 'arn:aws:secretsmanager:eu-west-1:680278545709:secret:littil/backend/staging-Muw9Id');

        const fargateService = new ApplicationLoadBalancedFargateService(this, 'LittilApi', {
            vpc,
            memoryLimitMiB: 512,
            desiredCount: 1,
            cpu: 256,
            taskImageOptions: {
                image: ContainerImage.fromEcrRepository(props.ecrRepository, '0.0.1-SNAPSHOT-1'),
                containerPort: 8080,
                environment: {
                    DATASOURCE_HOST: database.instanceEndpoint.hostname,
                    DATASOURCE_PORT: String(database.instanceEndpoint.port),
                    DATASOURCE_DATABASE: databaseName,
                },
                secrets: {
                    DATASOURCE_USERNAME: Secret.fromSecretsManager(littilBackendDatabaseSecret, 'username'),
                    DATASOURCE_PASSWORD: Secret.fromSecretsManager(littilBackendDatabaseSecret, 'password'),
                    OIDC_CLIENT_ID: Secret.fromSecretsManager(littilBackendSecret, 'oidcClientId'),
                    OIDC_CLIENT_SECRET: Secret.fromSecretsManager(littilBackendSecret, 'oidcClientSecret'),
                    OIDC_TENANT: Secret.fromSecretsManager(littilBackendSecret, 'oidcTenant'),
                    M2M_CLIENT_ID: Secret.fromSecretsManager(littilBackendSecret, 'm2mClientId'),
                    M2M_CLIENT_SECRET: Secret.fromSecretsManager(littilBackendSecret, 'm2mClientSecret'),
                    SMTP_HOST: Secret.fromSecretsManager(littilBackendSecret, 'smtpHost'),
                    SMTP_USERNAME: Secret.fromSecretsManager(littilBackendSecret, 'smtpUsername'),
                    SMTP_PASSWORD: Secret.fromSecretsManager(littilBackendSecret, 'smtpPassword'),
                },
            },
            certificate: props.certificate,
        });

        const fargateSecurityGroup = fargateService.service.connections.securityGroups[0];

        /* Database access. */
        const databaseSecurityGroup = database.connections.securityGroups[0];
        databaseSecurityGroup.connections.allowFrom(fargateSecurityGroup, Port.allTcp());
    }
}
