import { Stack, StackProps } from 'aws-cdk-lib';
import { ISubnet, Subnet, SubnetType, Vpc } from 'aws-cdk-lib/aws-ec2';
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
            subnetConfiguration: [
                {
                    name: 'LITTIL-Public-Subnet',
                    subnetType: SubnetType.PUBLIC,
                },
                {
                    name: 'LITTIL-Private-Subnet',
                    subnetType: SubnetType.PRIVATE_ISOLATED,
                },
                {
                    name: 'subnet-06016bf8ed12004e3',
                    subnetType: SubnetType.PRIVATE_WITH_NAT,
                },
                {
                    name: 'subnet-05acd2dc6a262b318',
                    subnetType: SubnetType.PRIVATE_WITH_NAT,
                }
            ]
        });
    }
}
