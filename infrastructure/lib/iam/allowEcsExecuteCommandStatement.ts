import { Effect, PolicyStatement } from 'aws-cdk-lib/aws-iam';

export const allowEcsExecuteCommandStatement = (ecsClusterArn: string,
                                                region: string,
                                                account: string) => {
    return new PolicyStatement({
        effect: Effect.ALLOW,
        actions: [
            'ecs:ExecuteCommand',
        ],
        resources: [
            ecsClusterArn,
            'arn:aws:ecs:' + region + ':' + account + ':task/*/*',
        ],
    });
};
