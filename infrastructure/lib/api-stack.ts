import { Stack, StackProps } from 'aws-cdk-lib';
import { Certificate } from 'aws-cdk-lib/aws-certificatemanager';
import { Port, SecurityGroup, Vpc } from 'aws-cdk-lib/aws-ec2';
import { Repository } from 'aws-cdk-lib/aws-ecr';
import { ContainerImage, Secret } from 'aws-cdk-lib/aws-ecs';
import { ApplicationLoadBalancedFargateService } from 'aws-cdk-lib/aws-ecs-patterns';
import { CfnAccessKey, Effect, Policy, PolicyStatement, Role, User, WebIdentityPrincipal } from 'aws-cdk-lib/aws-iam';
import { LogGroup } from 'aws-cdk-lib/aws-logs';
import { Secret as SecretsManagerSecret } from 'aws-cdk-lib/aws-secretsmanager';
import { Construct } from 'constructs';
import { allowEcsDescribeTaskStatement } from './iam/allowEcsDescribeTaskStatement';
import { allowEcsExecuteCommandStatement } from './iam/allowEcsExecuteCommandStatement';
import { LittilEnvironmentSettings } from './littil-environment-settings';
import { LoggingStack } from './logging-stack';

export interface ApiStackProps extends StackProps {
    littil: LittilEnvironmentSettings;

    apiVpc: {
        id: string;
    };

    ecrRepository: {
        awsAccount: string;
        name: string;
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
        const apiEcrRepository = Repository.fromRepositoryAttributes(this, 'ApiEcrContainerRepository', {
            repositoryName: props.ecrRepository.name,
            repositoryArn: 'arn:aws:ecr:eu-west-1:' + props.ecrRepository.awsAccount + ':repository/' + props.ecrRepository.name,
        });

        const certificate = Certificate.fromCertificateArn(this, 'ApiCertificate', props.apiCertificateArn);

        const littilOidcSecret = SecretsManagerSecret.fromSecretNameV2(this, 'LittilOidcSecret', 'littil/backend/' + props.littil.environment + '/oidc');
        const littilSmtpSecret = SecretsManagerSecret.fromSecretNameV2(this, 'LittilSmtpSecret', 'littil/backend/' + props.littil.environment + '/smtp');

        const littilDatabaseSecretName = 'littil/backend/' + props.littil.environment + '/databaseCredentials';
        const littilBackendDatabaseSecret = SecretsManagerSecret.fromSecretNameV2(this, 'LittilBackendDatabaseSecret', littilDatabaseSecretName);

        const apiEcsLoggingStack = new LoggingStack(this, 'ApiEcsLoggingStack', {
            littil: props.littil,
            logGroupName: 'BackendApiEcsLogs',
        });

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
                    QUARKUS_HTTP_CORS_ORIGINS: props.littil.httpCorsOrigin,
                    QUARKUS_LOG_CLOUDWATCH_ACCESS_KEY_ID: apiEcsLoggingStack.loggingAccessKey.ref,
                    QUARKUS_LOG_CLOUDWATCH_ACCESS_KEY_SECRET: apiEcsLoggingStack.loggingAccessKey.attrSecretAccessKey,
                    QUARKUS_LOG_CLOUDWATCH_LOG_GROUP: apiEcsLoggingStack.cloudwatchLogGroup.logGroupName,
                    QUARKUS_LOG_CLOUDWATCH_REGION: this.region,
                    QUARKUS_LOG_CLOUDWATCH_LOG_STREAM_NAME: 'quarkus-logs',
                },
                secrets: {
                    DATASOURCE_USERNAME: Secret.fromSecretsManager(littilBackendDatabaseSecret, 'username'),
                    DATASOURCE_PASSWORD: Secret.fromSecretsManager(littilBackendDatabaseSecret, 'password'),
                    OIDC_CLIENT_ID: Secret.fromSecretsManager(littilOidcSecret, 'oidcClientId'),
                    OIDC_CLIENT_SECRET: Secret.fromSecretsManager(littilOidcSecret, 'oidcClientSecret'),
                    OIDC_TENANT: Secret.fromSecretsManager(littilOidcSecret, 'oidcTenant'),
                    M2M_CLIENT_ID: Secret.fromSecretsManager(littilOidcSecret, 'm2mClientId'),
                    M2M_CLIENT_SECRET: Secret.fromSecretsManager(littilOidcSecret, 'm2mClientSecret'),
                    SMTP_HOST: Secret.fromSecretsManager(littilSmtpSecret, 'smtpHost'),
                    SMTP_USERNAME: Secret.fromSecretsManager(littilSmtpSecret, 'smtpUsername'),
                    SMTP_PASSWORD: Secret.fromSecretsManager(littilSmtpSecret, 'smtpPassword'),
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
        const ecsExecPolicy = new Policy(this, 'LITTIL-NL-' + props.littil.environment + '-littil-api-ECSExec-Policy');
        ecsExecPolicy.addStatements(
            allowEcsExecuteCommandStatement(fargateService.cluster.clusterArn, this.region, this.account),
            allowEcsDescribeTaskStatement(this.region, this.account),
        );

        /* ECS Exec users. */
        const ecsExecUserName = 'LITTIL-NL-' + props.littil.environment + '-littil-api-ECSExec-User';
        const ecsExecUser = new User(this, ecsExecUserName, {userName: ecsExecUserName});
        ecsExecPolicy.attachToUser(ecsExecUser);

        /* Database access. */
        const databaseSecurityGroup = SecurityGroup.fromSecurityGroupId(this, 'DatabaseSecurityGroup', props.database.securityGroup.id);

        const fargateSecurityGroup = fargateService.service.connections.securityGroups[0];
        databaseSecurityGroup.connections.allowFrom(fargateSecurityGroup, Port.allTcp());

        /* Push-pull permissions for Github repository. */
        const issuer = 'token.actions.githubusercontent.com';
        const gitHubOrg = 'Devoxx4Kids-NPO';
        const githubRepoName = 'littil-backend';
        const accountId = this.account;
        const openIdConnectProviderArn = `arn:aws:iam::${accountId}:oidc-provider/${issuer}`;
        const ciDeployRole = new Role(this, 'EcsCiDeployRole', {
            roleName: 'LITTIL-NL-api-ecs-redeploy',
            assumedBy: new WebIdentityPrincipal(openIdConnectProviderArn, {
                StringLike: {
                    [`${issuer}:sub`]: `repo:${gitHubOrg}/${githubRepoName}:*`,
                },
                StringEquals: {
                    [`${issuer}:aud`]: 'sts.amazonaws.com',
                },
            }),
        });
        const updateServicePolicy = new Policy(this, 'EcsUpdateServicePolicy', {
            policyName: 'EcsUpdateServicePolicy',
            statements: [
                new PolicyStatement({
                    effect: Effect.ALLOW,
                    actions: [
                        'ecs:UpdateService',
                    ],
                    resources: [
                        fargateService.service.serviceArn,
                    ],
                }),
            ],
        });
        updateServicePolicy.attachToRole(ciDeployRole);
    }
}
