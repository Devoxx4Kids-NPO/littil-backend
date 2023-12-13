import { Effect, PolicyStatement } from 'aws-cdk-lib/aws-iam';
import { PolicyStatementProps } from 'aws-cdk-lib/aws-iam/lib/policy-statement';

export const allowEcrPullFor = (policyStatementProps: PolicyStatementProps) => {
    return new PolicyStatement({
        effect: Effect.ALLOW,
        actions: [
            'ecr:GetDownloadUrlForLayer',
            'ecr:BatchGetImage',
            'ecr:BatchCheckLayerAvailability',
        ],
        ...policyStatementProps,
    });
};
