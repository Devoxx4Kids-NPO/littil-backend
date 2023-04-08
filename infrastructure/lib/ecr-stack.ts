import { CfnOutput, Stack, StackProps } from 'aws-cdk-lib';
import { Repository, TagMutability } from 'aws-cdk-lib/aws-ecr';
import { Effect, Policy, PolicyStatement, Role, User, WebIdentityPrincipal } from 'aws-cdk-lib/aws-iam';
import { Construct } from 'constructs';
import { LittilEnvironmentSettings } from './littil-environment-settings';

export interface EcrStackProps extends StackProps {
    littil: LittilEnvironmentSettings;
    apiRepositoryNameExportName: string;
    apiRepositoryArnExportName: string;
}

export class EcrStack extends Stack {
    constructor(scope: Construct, id: string, props: EcrStackProps) {
        super(scope, id, props);
        const ecrRepository = new Repository(this, 'LittilBackendRepository', {
            repositoryName: 'littil-backend',
            imageTagMutability: TagMutability.MUTABLE,
        });

        new CfnOutput(this, 'ApiRepositoryNameOutput', {
            exportName: props.apiRepositoryNameExportName,
            value: ecrRepository.repositoryName
        });
        new CfnOutput(this, 'ApiRepositoryArnOutput', {
            exportName: props.apiRepositoryArnExportName,
            value: ecrRepository.repositoryArn,
        });

        /* Push-pull permissions. */
        const pushPullPolicy = new Policy(this, 'EcrPushPullPolicy', {
            policyName: 'BackendEcrPushPullPolicy',
            statements: [
                new PolicyStatement({
                    effect: Effect.ALLOW,
                    actions: [
                        'ecr:BatchGetImage',
                        'ecr:BatchCheckLayerAvailability',
                        'ecr:CompleteLayerUpload',
                        'ecr:GetDownloadUrlForLayer',
                        'ecr:InitiateLayerUpload',
                        'ecr:PutImage',
                        'ecr:UploadLayerPart',
                    ],
                    resources: [
                        ecrRepository.repositoryArn,
                    ],
                }),
            ],
        });

        const loginToEcrPolicy = new Policy(this, 'EcrAuthPolicy', {
            policyName: 'EcrAuthPolicy',
            statements: [
                new PolicyStatement({
                    effect: Effect.ALLOW,
                    actions: [
                        'ecr:GetAuthorizationToken',
                    ],
                    resources: [
                        '*',
                    ],
                }),
            ],
        });

        /* Push pull user for manual pushing of images. */
        // TODO: Remove when automated from pipeline
        const pushPullUser = new User(this, 'ManualPushPullUser', {userName: 'LITTIL-NL-' + props.littil.environment + '-Ecr-Manual-PushPull'});
        pushPullPolicy.attachToUser(pushPullUser);
        loginToEcrPolicy.attachToUser(pushPullUser);

        /* Push-pull permissions for Github repository. */
        const issuer = 'token.actions.githubusercontent.com';
        const gitHubOrg = 'Devoxx4Kids-NPO';
        const githubRepoName = 'littil-backend';
        const accountId = this.account;
        const openIdConnectProviderArn = `arn:aws:iam::${accountId}:oidc-provider/${issuer}`;

        const ciPushRole = new Role(this, 'EcrCiPushRole', {
            roleName: 'LITTIL-NL-' + props.littil.environment + '-api-ecr-push',
            assumedBy: new WebIdentityPrincipal(openIdConnectProviderArn, {
                StringLike: {
                    [`${issuer}:sub`]: `repo:${gitHubOrg}/${githubRepoName}:*`,
                },
                StringEquals: {
                    [`${issuer}:aud`]: 'sts.amazonaws.com',
                },
            }),
        });
        pushPullPolicy.attachToRole(ciPushRole);
        loginToEcrPolicy.attachToRole(ciPushRole);

        const updateServicePolicy = new Policy(this, 'EcrUpdateServicePolicy', {
            policyName: 'EcrUpdateServicePolicy',
            statements: [
                new PolicyStatement({
                    effect: Effect.ALLOW,
                    actions: [
                        'ecs:UpdateService',
                    ],
                    resources: [
                        '*',
                    ],
                }),
            ],
        });
        updateServicePolicy.attachToRole(ciPushRole);
    }
}
