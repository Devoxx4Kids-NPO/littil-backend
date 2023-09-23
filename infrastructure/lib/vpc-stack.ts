import { CfnOutput, Stack, StackProps } from 'aws-cdk-lib';
import { Vpc } from 'aws-cdk-lib/aws-ec2';
import { Construct } from 'constructs';

export interface VpcStackProps extends StackProps {
    vpcIdExportName: string;
}

export class VpcStack extends Stack {
    constructor(scope: Construct,
                id: string,
                props: VpcStackProps) {
        super(scope, id, props);

        const vpc = new Vpc(this, 'LittilBackendVpc', {
            maxAzs: 2,
            natGateways: 1,
        });

        new CfnOutput(this, 'databasename', {
            value: vpc.vpcId,
            exportName: props.vpcIdExportName,
        });
    }
}
