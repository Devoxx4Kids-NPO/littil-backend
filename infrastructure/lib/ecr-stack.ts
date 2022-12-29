import { CfnOutput, Stack, StackProps } from 'aws-cdk-lib';
import { Repository, TagMutability } from 'aws-cdk-lib/aws-ecr';
import { Effect, Policy, PolicyStatement, User } from 'aws-cdk-lib/aws-iam';
import { Construct } from 'constructs';

export interface EcrStackProps extends StackProps {
    apiRepositoryNameExportName: string;
    apiRepositoryArnExportName: string;
}

export class EcrStack extends Stack {
    constructor(scope: Construct, id: string, props: EcrStackProps) {
        super(scope, id, props);
        const ecrRepository = new Repository(this, 'LittilBackendRepository', {
            repositoryName: 'littil-backend',
            imageTagMutability: TagMutability.IMMUTABLE,
        });

        new CfnOutput(this, 'ApiRepositoryNameOutput', {
            exportName: props.apiRepositoryNameExportName,
            value: ecrRepository.repositoryName
        });
        new CfnOutput(this, 'ApiRepositoryArnOutput', {
            exportName: props.apiRepositoryArnExportName,
            value: ecrRepository.repositoryArn,
        });

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
        const pushPullUser = new User(this, 'PushPullUser', {userName: 'Backend-Ecr-PushPull'});
        pushPullPolicy.attachToUser(pushPullUser);
        loginToEcrPolicy.attachToUser(pushPullUser);
    }
}
