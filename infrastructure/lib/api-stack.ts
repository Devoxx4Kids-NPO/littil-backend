import { CfnOutput, Stack, StackProps } from 'aws-cdk-lib';
import { Certificate } from 'aws-cdk-lib/aws-certificatemanager';
import { InstanceClass, InstanceSize, InstanceType, Port, Vpc } from 'aws-cdk-lib/aws-ec2';
import { Repository } from 'aws-cdk-lib/aws-ecr';
import { ContainerImage, Secret } from 'aws-cdk-lib/aws-ecs';
import { ApplicationLoadBalancedFargateService } from 'aws-cdk-lib/aws-ecs-patterns';
import { Policy, User } from 'aws-cdk-lib/aws-iam';
import {
    Credentials,
    DatabaseInstance,
    DatabaseInstanceEngine,
    MariaDbEngineVersion,
    ParameterGroup,
} from 'aws-cdk-lib/aws-rds';
import { DatabaseInstanceProps } from 'aws-cdk-lib/aws-rds/lib/instance';
import { Secret as SecretsManagerSecret } from 'aws-cdk-lib/aws-secretsmanager';
import { Construct } from 'constructs';
import { allowEcsDescribeTaskStatement } from './iam/allowEcsDescribeTaskStatement';
import { allowEcsExecuteCommandStatement } from './iam/allowEcsExecuteCommandStatement';

export interface ApiStackProps extends StackProps {
    ecrRepository: {
        name: string;
        arn: string;
    };
    apiCertificateArn: string;

    databaseHostExportName: string;
    databasePortExportName: string;
    databaseNameExportName: string;
    databaseSecurityGroupIdExportName: string;
}

export class ApiStack extends Stack {
    constructor(scope: Construct,
                id: string,
                props: ApiStackProps) {
        super(scope, id, props);

        const vpc = new Vpc(this, 'LittilBackendVpc', {
            maxAzs: 2,
            natGateways: 1,
        });

        /* Database. */
        const databaseName = 'LittilDatabase';

        const littilDatabaseSecretName = 'littil/backend/databaseCredentials';
        const littilBackendDatabaseSecret = SecretsManagerSecret.fromSecretNameV2(this, 'LittilBackendDatabaseSecret', littilDatabaseSecretName);

        const rdsEngine = DatabaseInstanceEngine.mariaDb({
            version: MariaDbEngineVersion.VER_10_6_8,
        });

        const rdsParameterGroup = new ParameterGroup(this, 'littil-rds-parametergroup', {
            engine: rdsEngine,
            parameters: {
                log_bin_trust_function_creators: '1',
            }
        });

        const databaseProperties: DatabaseInstanceProps = {
            databaseName,
            credentials: Credentials.fromSecret(littilBackendDatabaseSecret),
            publiclyAccessible: false,
            vpc,
            engine: rdsEngine,
            parameterGroup: rdsParameterGroup,
            instanceType: InstanceType.of(
                InstanceClass.T4G,
                InstanceSize.MICRO,
            ),
        };
        const database = new DatabaseInstance(this, 'LittilApiDatabase', databaseProperties);
        new CfnOutput(this, 'databaseHost', {
            value: database.instanceEndpoint.hostname,
            exportName: props.databaseHostExportName,
        });
        new CfnOutput(this, 'databasePort', {
            value: String(database.instanceEndpoint.port),
            exportName: props.databasePortExportName,
        });
        new CfnOutput(this, 'databasename', {
            value: databaseName,
            exportName: props.databaseNameExportName,
        });

        /* Fargate. */
        const littilBackendSecret = SecretsManagerSecret.fromSecretNameV2(this, 'LittilBackendSecret', 'littil/backend/staging');

        const apiEcrRepository = Repository.fromRepositoryAttributes(this, 'ApiEcrContainerRepository', {
            repositoryName: props.ecrRepository.name,
            repositoryArn: props.ecrRepository.arn,
        });

        const certificate = Certificate.fromCertificateArn(this, 'ApiCertificate', props.apiCertificateArn);

        const fargateService = new ApplicationLoadBalancedFargateService(this, 'LittilApi', {
            vpc,
            memoryLimitMiB: 512,
            desiredCount: 1,
            cpu: 256,
            enableExecuteCommand: true,
            taskImageOptions: {
                image: ContainerImage.fromEcrRepository(apiEcrRepository, '0.0.1-SNAPSHOT-4'),
                containerPort: 8080,
                containerName: 'api',
                environment: {
                    DATASOURCE_HOST: database.instanceEndpoint.hostname,
                    DATASOURCE_PORT: String(database.instanceEndpoint.port),
                    DATASOURCE_DATABASE: databaseName,
                    QUARKUS_HTTP_CORS_ORIGINS: 'https://staging.littil.org',
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
            certificate,
        });

        /* ECS Exec. */
        const ecsExecPolicy = new Policy(this, 'LITTIL-NL-staging-littil-api-ECSExec-Policy');
        ecsExecPolicy.addStatements(
            allowEcsExecuteCommandStatement(fargateService.cluster.clusterArn, this.region, this.account),
            allowEcsDescribeTaskStatement(this.region, this.account),
        );

        /* ECS Exec users. */
        const ecsExecUserName = 'LITTIL-NL-staging-littil-api-ECSExec-User';
        const ecsExecUser = new User(this, ecsExecUserName, {userName: ecsExecUserName});
        ecsExecPolicy.attachToUser(ecsExecUser);

        /* Database access. */
        const databaseSecurityGroup = database.connections.securityGroups[0];
        new CfnOutput(this, 'DatabaseSecurityGroupId', {
            value: databaseSecurityGroup.securityGroupId,
            exportName: props.databaseSecurityGroupIdExportName,
        });

        const fargateSecurityGroup = fargateService.service.connections.securityGroups[0];
        databaseSecurityGroup.connections.allowFrom(fargateSecurityGroup, Port.allTcp());
    }
}
