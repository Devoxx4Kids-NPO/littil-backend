import * as cdk from 'aws-cdk-lib';
import { CfnOutput } from 'aws-cdk-lib';
import { Certificate } from 'aws-cdk-lib/aws-certificatemanager';
import { InstanceClass, InstanceSize, InstanceType, Port, Vpc } from 'aws-cdk-lib/aws-ec2';
import { Repository } from 'aws-cdk-lib/aws-ecr';
import { Compatibility, ContainerImage, FargateService, Secret, TaskDefinition } from 'aws-cdk-lib/aws-ecs';
import { ApplicationLoadBalancedFargateService } from 'aws-cdk-lib/aws-ecs-patterns';
import { Effect, Policy, PolicyStatement, User } from 'aws-cdk-lib/aws-iam';
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

export interface ApiStackProps extends cdk.StackProps {
    apiEcrRepository: Repository;
    apiCertificate: Certificate;
    mysqlSupportContainer: {
        enable: boolean;
        ecrRepositoryArn: string;
        ecrRepositoryName: string;
        imageTag: string;
    };
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
        new CfnOutput(this, 'dbEndpoint', {value: database.instanceEndpoint.hostname});

        /* Fargate. */
        const littilBackendSecret = SecretsManagerSecret.fromSecretCompleteArn(this, 'LittilBackendSecret', 'arn:aws:secretsmanager:eu-west-1:680278545709:secret:littil/backend/staging-Muw9Id');

        const fargateService = new ApplicationLoadBalancedFargateService(this, 'LittilApi', {
            vpc,
            memoryLimitMiB: 512,
            desiredCount: 1,
            cpu: 256,
            enableExecuteCommand: true,
            taskImageOptions: {
                image: ContainerImage.fromEcrRepository(props.apiEcrRepository, '0.0.1-SNAPSHOT-4'),
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
            certificate: props.apiCertificate,
        });

        /* ECS Exec. */
        const ecsExecStatement = new PolicyStatement({
            effect: Effect.ALLOW,
            actions: [
                'ecs:ExecuteCommand',
            ],
            resources: [
                fargateService.cluster.clusterArn,
                'arn:aws:ecs:' + this.region + ':' + this.account + ':task/*/*',
            ],
        });

        const ecsDescribeTasksStatement = new PolicyStatement({
            effect: Effect.ALLOW,
            actions: [
                'ecs:DescribeTasks',
            ],
            resources: [
                'arn:aws:ecs:' + this.region + ':' + this.account + ':task/*/*',
            ],
        });

        const ecsExecPolicy = new Policy(this, 'LITTIL-NL-staging-littil-api-ECSExec-Policy');
        ecsExecPolicy.addStatements(ecsExecStatement, ecsDescribeTasksStatement);

        /* ECS Exec users. */
        const ecsExecUserName = 'LITTIL-NL-staging-littil-api-ECSExec-User';
        const ecsExecUser = new User(this, ecsExecUserName, {userName: ecsExecUserName});
        ecsExecPolicy.attachToUser(ecsExecUser);

        /* Database access. */
        const databaseSecurityGroup = database.connections.securityGroups[0];

        const fargateSecurityGroup = fargateService.service.connections.securityGroups[0];
        databaseSecurityGroup.connections.allowFrom(fargateSecurityGroup, Port.allTcp());

        /* Database access container. */
        if (props.mysqlSupportContainer.enable) {
            const mysqlEcrRepository = Repository.fromRepositoryAttributes(this, 'MySQLContainerRepository', {
                repositoryName: props.mysqlSupportContainer.ecrRepositoryName,
                repositoryArn: props.mysqlSupportContainer.ecrRepositoryArn,
            });
            const mysqlContainerImage = ContainerImage.fromEcrRepository(mysqlEcrRepository, props.mysqlSupportContainer.imageTag);

            const mysqlTaskDefinition = new TaskDefinition(this, 'LittilMysqlClientTask', {
                compatibility: Compatibility.FARGATE,
                cpu: '256',
                memoryMiB: '512',
            });
            mysqlTaskDefinition.addContainer('MySqlContainer', {
                image: mysqlContainerImage,
                cpu: 256,
                memoryLimitMiB: 512,
                environment: {
                    DATASOURCE_HOST: database.instanceEndpoint.hostname,
                    DATASOURCE_PORT: String(database.instanceEndpoint.port),
                    DATASOURCE_DATABASE: databaseName,
                    MYSQL_ROOT_PASSWORD: 'mysqlstagingroot',
                }
            });

            const mysqlFargateService = new FargateService(this, 'BackendMySqlClient', {
                cluster: fargateService.cluster,
                taskDefinition: mysqlTaskDefinition,
                assignPublicIp: false,
                enableExecuteCommand: true,
            });

            const fargateMySQLSecurityGroup = mysqlFargateService.connections.securityGroups[0];
            databaseSecurityGroup.connections.allowFrom(fargateMySQLSecurityGroup, Port.allTcp());
        }
    }
}
