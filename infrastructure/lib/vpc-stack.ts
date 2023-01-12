import { Stack, StackProps } from 'aws-cdk-lib';
import { Vpc } from 'aws-cdk-lib/aws-ec2';
import { Construct } from 'constructs';

export class VpcStack extends Stack {
    constructor(scope: Construct,
                id: string,
                props: StackProps) {
        super(scope, id, props);

        new Vpc(this, 'LittilBackendVpc', {
            maxAzs: 2,
            natGateways: 1,
        });
    }
}
