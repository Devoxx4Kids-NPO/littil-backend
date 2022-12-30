import { Stack, StackProps } from 'aws-cdk-lib';
import { IVpc, Port, SecurityGroup } from 'aws-cdk-lib/aws-ec2';
import { Repository } from 'aws-cdk-lib/aws-ecr';
import { Cluster, Compatibility, ContainerImage, FargateService, TaskDefinition } from 'aws-cdk-lib/aws-ecs';
import { Policy, User } from 'aws-cdk-lib/aws-iam';
import { Construct } from 'constructs';
import { allowEcsDescribeTaskStatement } from './iam/allowEcsDescribeTaskStatement';
import { allowEcsExecuteCommandStatement } from './iam/allowEcsExecuteCommandStatement';

export interface MaintenanceStackProps extends StackProps {
    maintenanceContainer: {
        enable: boolean;
        ecrRepository: {
            name: string;
            arn: string;
        };
        imageTag: string;
    };
    database: {
        host: string;
        port: string;
        name: string;
        vpc: IVpc;
        securityGroup: {
            id: string;
        };
    };
}

export class MaintenanceStack extends Stack {
    constructor(scope: Construct,
                id: string,
                props: MaintenanceStackProps) {
        super(scope, id, props);

        const ecsExecUserName = 'LITTIL-NL-staging-maintenance-ECSExec-User';
        const ecsExecUser = new User(this, ecsExecUserName, {userName: ecsExecUserName});

        /* Database access container. */
        if (props.maintenanceContainer.enable) {
            this.createMaintenanceContainer(props, ecsExecUser);
        }
    }

    private createMaintenanceContainer(props: MaintenanceStackProps,
                                       ecsExecUser: User) {
        const maintenanceEcrRepository = Repository.fromRepositoryAttributes(this, 'MaintenanceContainerRepository', {
            repositoryName: props.maintenanceContainer.ecrRepository.name,
            repositoryArn: props.maintenanceContainer.ecrRepository.arn,
        });
        const maintenanceContainerImage = ContainerImage.fromEcrRepository(maintenanceEcrRepository, props.maintenanceContainer.imageTag);

        const maintenanceTaskDefinition = new TaskDefinition(this, 'LittilMaintenanceClientTask', {
            compatibility: Compatibility.FARGATE,
            cpu: '256',
            memoryMiB: '512',
        });
        maintenanceTaskDefinition
            .addContainer('MaintenanceContainer', {
                image: maintenanceContainerImage,
                cpu: 256,
                memoryLimitMiB: 512,
                environment: {
                    DATASOURCE_HOST: props.database.host,
                    DATASOURCE_PORT: props.database.port,
                    DATASOURCE_DATABASE: props.database.name,
                }
            });

        const ecsCluster = new Cluster(this, 'MaintenanceCluster', {
            vpc: props.database.vpc,
        });
        const maintenanceFargateService = new FargateService(this, 'BackendMaintenanceService', {
            cluster: ecsCluster,
            taskDefinition: maintenanceTaskDefinition,
            assignPublicIp: false,
            enableExecuteCommand: true,
        });

        const fargateMySQLSecurityGroup = maintenanceFargateService.connections.securityGroups[0];
        const databaseSecurityGroup = SecurityGroup.fromSecurityGroupId(this, 'DatabaseSecurityGroup', props.database.securityGroup.id);
        databaseSecurityGroup.connections.allowFrom(fargateMySQLSecurityGroup, Port.allTcp());

        /* ECS Exec. */
        const ecsExecPolicy = new Policy(this, 'LITTIL-NL-staging-maintenance-ECSExec-Policy');
        ecsExecPolicy.addStatements(
            allowEcsExecuteCommandStatement(maintenanceFargateService.cluster.clusterArn, this.region, this.account),
            allowEcsDescribeTaskStatement(this.region, this.account),
        );
        ecsExecPolicy.attachToUser(ecsExecUser);
    }
}
