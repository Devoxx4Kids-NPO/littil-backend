import * as cdk from 'aws-cdk-lib';
import { CfnOutput } from 'aws-cdk-lib';
import { Repository, TagMutability } from 'aws-cdk-lib/aws-ecr';
import { Effect, Policy, PolicyStatement, User } from 'aws-cdk-lib/aws-iam';
import { Construct } from 'constructs';

export class SupportEcrStack extends cdk.Stack {
    constructor(scope: Construct, id: string, props: cdk.StackProps) {
        super(scope, id, props);
        const ecrRepository = new Repository(this, 'MySQLRepository', {
            repositoryName: 'mysql',
            imageTagMutability: TagMutability.IMMUTABLE,
        });

        new CfnOutput(this, 'MysqlRepositoryArnOutput', {
            exportName: 'mysqlRepositoryArnOutput',
            value: ecrRepository.repositoryArn,
        });
        new CfnOutput(this, 'MysqlRepositoryNameOutput', {
            exportName: 'mysqlRepositoryNameOutput',
            value: ecrRepository.repositoryName
        });

        const pushPullPolicy = new Policy(this, 'MySQLEcrPushPullPolicy', {
            policyName: 'MySQLEcrPushPullPolicy',
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

        const loginToEcrPolicy = new Policy(this, 'MySQLEcrAuthPolicy', {
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
        const pushPullUser = new User(this, 'MySQLPushPullUser', {userName: 'MySQL-Ecr-PushPull'});
        pushPullPolicy.attachToUser(pushPullUser);
        loginToEcrPolicy.attachToUser(pushPullUser);
    }
}
