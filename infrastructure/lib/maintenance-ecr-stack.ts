import { CfnOutput, Stack, StackProps } from 'aws-cdk-lib';
import { Repository, TagMutability } from 'aws-cdk-lib/aws-ecr';
import { AccountPrincipal, Effect, Policy, PolicyStatement, User } from 'aws-cdk-lib/aws-iam';
import { Construct } from 'constructs';
import { allowEcrPullFor } from './permissions/ecr.allow-pull';

export interface MaintenanceEcrStackProps extends StackProps {
    workloadAccounts: string[];
    ecrMaintenanceRepositoryName: string;
}

export class MaintenanceEcrStack extends Stack {
    constructor(scope: Construct,
                id: string,
                props: MaintenanceEcrStackProps) {
        super(scope, id, props);
        const ecrRepository = new Repository(this, 'MaintenanceEcrRepository', {
            repositoryName: props.ecrMaintenanceRepositoryName,
            imageTagMutability: TagMutability.IMMUTABLE,
        });

        props.workloadAccounts
            .forEach((workloadAccount) => {
                ecrRepository.addToResourcePolicy(allowEcrPullFor({
                    principals: [
                        new AccountPrincipal(workloadAccount),
                    ]
                }));
            });

        const pushPullPolicy = new Policy(this, 'MaintenanceEcrPushPullPolicy', {
            policyName: 'MaintenanceEcrPushPullPolicy',
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

        const loginToEcrPolicy = new Policy(this, 'MaintenanceEcrAuthPolicy', {
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
        const pushPullUser = new User(this, 'ManualMaintenancePushPullUser', {userName: 'LITTIL-NL-Backend-Maintenance-Ecr-Manual-PushPull'});
        pushPullPolicy.attachToUser(pushPullUser);
        loginToEcrPolicy.attachToUser(pushPullUser);
    }
}
