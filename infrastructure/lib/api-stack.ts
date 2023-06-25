import { Stack, StackProps } from 'aws-cdk-lib';
import { Certificate } from 'aws-cdk-lib/aws-certificatemanager';
import { Port, SecurityGroup, Vpc } from 'aws-cdk-lib/aws-ec2';
import { Repository } from 'aws-cdk-lib/aws-ecr';
import { ContainerImage, Secret } from 'aws-cdk-lib/aws-ecs';
import { ApplicationLoadBalancedFargateService } from 'aws-cdk-lib/aws-ecs-patterns';
import { CfnAccessKey, Effect, Policy, PolicyStatement, User } from 'aws-cdk-lib/aws-iam';
import { LogGroup } from 'aws-cdk-lib/aws-logs';
import { Secret as SecretsManagerSecret } from 'aws-cdk-lib/aws-secretsmanager';
import { Construct } from 'constructs';
import { allowEcsDescribeTaskStatement } from './iam/allowEcsDescribeTaskStatement';
import { allowEcsExecuteCommandStatement } from './iam/allowEcsExecuteCommandStatement';

export interface ApiStackProps extends StackProps {
    apiVpc: {
        id: string;
    };

    ecrRepository: {
        name: string;
        arn: string;
    };
    apiCertificateArn: string;

    database: {
        host: string;
        port: string;
        name: string;
        vpcId: string;
        securityGroup: {
            id: string;
        };
    };
}

export class ApiStack extends Stack {
    constructor(scope: Construct,
                id: string,
                props: ApiStackProps) {
        super(scope, id, props);

        const vpc = Vpc.fromLookup(this, 'ApiVpc', {
            vpcId: props.apiVpc.id,
        });

        /* Fargate. */
        const littilBackendSecret = SecretsManagerSecret.fromSecretNameV2(this, 'LittilBackendSecret', 'littil/backend/staging');

        const apiEcrRepository = Repository.fromRepositoryAttributes(this, 'ApiEcrContainerRepository', {
            repositoryName: props.ecrRepository.name,
            repositoryArn: props.ecrRepository.arn,
        });

        const certificate = Certificate.fromCertificateArn(this, 'ApiCertificate', props.apiCertificateArn);

        // TODO: Deduplicate
        const littilDatabaseSecretName = 'littil/backend/databaseCredentials';
        const littilBackendDatabaseSecret = SecretsManagerSecret.fromSecretNameV2(this, 'LittilBackendDatabaseSecret', littilDatabaseSecretName);

        const littilBackendCloudwatchLoggingUser = new User(this, 'CloudwatchLoggingUser', {
            userName: 'LITTIL-NL-staging-backend-Cloudwatch'
        });
        const loggingAccessKey = new CfnAccessKey(this, 'CloudwatchLoggingAccessKey', {
            userName: littilBackendCloudwatchLoggingUser.userName,
        });
        const cloudwatchLogGroup = new LogGroup(this, 'BackendLogGroup', {
            logGroupName: 'BackendQuarkusLogs',
        });
        const cloudwatchLoggingStatement = new PolicyStatement({
            effect: Effect.ALLOW,
            actions: [
                'logs:DescribeLogStreams',
                'logs:CreateLogStream',
                'logs:PutLogEvents',
            ],
            resources: [
                cloudwatchLogGroup.logGroupArn,
            ],
        });
        const loggingPolicy = new Policy(this, 'QuarkusCloudwatchLoggingPolicy');
        loggingPolicy.addStatements(cloudwatchLoggingStatement);
        loggingPolicy.attachToUser(littilBackendCloudwatchLoggingUser);

        const fargateService = new ApplicationLoadBalancedFargateService(this, 'LittilApi', {
            vpc,
            memoryLimitMiB: 512,
            desiredCount: 1,
            cpu: 256,
            enableExecuteCommand: true,
            taskImageOptions: {
                image: ContainerImage.fromEcrRepository(apiEcrRepository, 'latest'),
                containerPort: 8080,
                containerName: 'api',
                environment: {
                    DATASOURCE_HOST: props.database.host,
                    DATASOURCE_PORT: props.database.port,
                    DATASOURCE_DATABASE: props.database.name,
                    QUARKUS_HTTP_CORS_ORIGINS: 'https://staging.littil.org',
                    QUARKUS_LOG_CLOUDWATCH_ACCESS_KEY_ID: loggingAccessKey.ref,
                    QUARKUS_LOG_CLOUDWATCH_ACCESS_KEY_SECRET: loggingAccessKey.attrSecretAccessKey,
                    QUARKUS_LOG_CLOUDWATCH_LOG_GROUP: cloudwatchLogGroup.logGroupName,
                    QUARKUS_LOG_CLOUDWATCH_REGION: this.region,
                    QUARKUS_LOG_CLOUDWATCH_LOG_STREAM_NAME: 'quarkus-logs',
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
        fargateService.targetGroup
            .configureHealthCheck({
                path: '/q/health',
                healthyHttpCodes: '200',
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
        const databaseSecurityGroup = SecurityGroup.fromSecurityGroupId(this, 'DatabaseSecurityGroup', props.database.securityGroup.id);

        const fargateSecurityGroup = fargateService.service.connections.securityGroups[0];
        databaseSecurityGroup.connections.allowFrom(fargateSecurityGroup, Port.allTcp());
    }
}
