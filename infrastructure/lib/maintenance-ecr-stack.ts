import { CfnOutput, Stack, StackProps } from 'aws-cdk-lib';
import { Repository, TagMutability } from 'aws-cdk-lib/aws-ecr';
import { Effect, Policy, PolicyStatement, User } from 'aws-cdk-lib/aws-iam';
import { Construct } from 'constructs';

export interface SupportEcrStackProps extends StackProps {
    maintenanceEcrRepositoryNameExportName: string;
    maintenanceEcrRepositoryArnExportName: string;
}

export class MaintenanceEcrStack extends Stack {
    constructor(scope: Construct,
                id: string,
                props: SupportEcrStackProps) {
        super(scope, id, props);
        const ecrRepository = new Repository(this, 'MaintenanceEcrRepository', {
            repositoryName: 'littil-backend-maintenance',
            imageTagMutability: TagMutability.IMMUTABLE,
        });

        new CfnOutput(this, 'MaintenanceEcrRepositoryNameOutput', {
            exportName: props.maintenanceEcrRepositoryNameExportName,
            value: ecrRepository.repositoryName
        });
        new CfnOutput(this, 'MaintenanceEcrRepositoryArnOutput', {
            exportName: props.maintenanceEcrRepositoryArnExportName,
            value: ecrRepository.repositoryArn,
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
        const pushPullUser = new User(this, 'MaintenancePushPullUser', {userName: 'Littil-Backend-Maintenance-Ecr-PushPull'});
        pushPullPolicy.attachToUser(pushPullUser);
        loginToEcrPolicy.attachToUser(pushPullUser);
    }
}
