import { Effect, PolicyStatement } from 'aws-cdk-lib/aws-iam';

export const allowEcsDescribeTaskStatement = (region: string,
                                              account: string) => {
    return new PolicyStatement({
        effect: Effect.ALLOW,
        actions: [
            'ecs:DescribeTasks',
        ],
        resources: [
            'arn:aws:ecs:' + region + ':' + account + ':task/*/*',
        ],
    });
};
