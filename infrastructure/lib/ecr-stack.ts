import * as cdk from 'aws-cdk-lib';
import { Repository, TagMutability } from 'aws-cdk-lib/aws-ecr';
import { Effect, Policy, PolicyStatement, User } from 'aws-cdk-lib/aws-iam';
import { Construct } from 'constructs';

export class EcrStack extends cdk.Stack {
    public readonly ecrRepository: Repository;

    constructor(scope: Construct, id: string, props: cdk.StackProps) {
        super(scope, id, props);
        this.ecrRepository = new Repository(this, 'LittilBackendRepository', {
            repositoryName: 'littil-backend',
            imageTagMutability: TagMutability.IMMUTABLE,
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
                        this.ecrRepository.repositoryArn,
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
