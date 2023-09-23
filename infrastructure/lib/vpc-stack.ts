import { Stack, StackProps } from 'aws-cdk-lib';
import { Vpc } from 'aws-cdk-lib/aws-ec2';
import { Construct } from 'constructs';

export class VpcStack extends Stack {
    /* Expose resource as advised in https://github.com/aws/aws-cdk/issues/3600#issuecomment-520440293. */
    public readonly vpc: Vpc;

    constructor(scope: Construct,
                id: string,
                props: StackProps) {
        super(scope, id, props);

        this.vpc = new Vpc(this, 'LittilBackendVpc', {
            maxAzs: 2,
            natGateways: 1,
        });
    }
}
