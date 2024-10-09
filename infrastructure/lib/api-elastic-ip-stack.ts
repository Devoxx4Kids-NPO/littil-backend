import { Stack, StackProps } from 'aws-cdk-lib';
import { CfnEIP } from 'aws-cdk-lib/aws-ec2';
import { Construct } from 'constructs';

export class ApiElasticIpStack extends Stack {
    public readonly elasticIp: CfnEIP;

    constructor(scope: Construct,
                id: string,
                props: StackProps) {
        super(scope, id, props);

        this.elasticIp = new CfnEIP(this, 'ApiIP', {
            tags: [
                {
                    key: 'Name',
                    value: 'API EC2',
                }
            ]
        });
    }
}
