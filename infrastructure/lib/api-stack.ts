import * as cdk from 'aws-cdk-lib';
import { Certificate } from 'aws-cdk-lib/aws-certificatemanager';
import { InstanceClass, InstanceSize, InstanceType, Vpc } from 'aws-cdk-lib/aws-ec2';
import { Repository } from 'aws-cdk-lib/aws-ecr';
import { ContainerImage } from 'aws-cdk-lib/aws-ecs';
import { ApplicationLoadBalancedFargateService } from 'aws-cdk-lib/aws-ecs-patterns';
import { DatabaseInstanceEngine, MariaDbEngineVersion } from 'aws-cdk-lib/aws-rds';
import { DatabaseInstanceProps } from 'aws-cdk-lib/aws-rds/lib/instance';
import { Construct } from 'constructs';

export interface ApiStackProps extends cdk.StackProps {
    ecrRepository: Repository;
    certificate: Certificate;
}

export class ApiStack extends cdk.Stack {
    constructor(scope: Construct, id: string, props: ApiStackProps) {
        super(scope, id, props);

        const vpc = new Vpc(this, 'LittilBackendVpc', {
            maxAzs: 2,
            natGateways: 1,
        });

        const databaseProperties: DatabaseInstanceProps = {
            databaseName: 'LittilDatabase',
            publiclyAccessible: false,
            vpc,
            engine: DatabaseInstanceEngine.mariaDb({
                version: MariaDbEngineVersion.VER_10_6_8,
            }),
            instanceType: InstanceType.of(
                InstanceClass.T4G,
                InstanceSize.MICRO,
            ),
        };
        // const database = new DatabaseInstance(this, 'LittilApiDatabase', databaseProperties);

        // new CfnOutput(this, 'dbEndpoint', {value: database.instanceEndpoint.hostname});

        new ApplicationLoadBalancedFargateService(this, 'LittilApi', {
            vpc,
            memoryLimitMiB: 512,
            desiredCount: 1,
            cpu: 256,
            taskImageOptions: {
                image: ContainerImage.fromRegistry('amazon/amazon-ecs-sample'),
                // image: ContainerImage.fromEcrRepository(props.ecrRepository, '0.0.1-SNAPSHOT-1'),
            },
            certificate: props.certificate,
        });
    }
}
