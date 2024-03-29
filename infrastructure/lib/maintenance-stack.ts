import { Stack, StackProps } from 'aws-cdk-lib';
import { Port, SecurityGroup, Vpc } from 'aws-cdk-lib/aws-ec2';
import { Repository } from 'aws-cdk-lib/aws-ecr';
import { Cluster, Compatibility, ContainerImage, FargateService, TaskDefinition } from 'aws-cdk-lib/aws-ecs';
import { Policy, User } from 'aws-cdk-lib/aws-iam';
import { Construct } from 'constructs';
import { allowEcsDescribeTaskStatement } from './iam/allowEcsDescribeTaskStatement';
import { allowEcsExecuteCommandStatement } from './iam/allowEcsExecuteCommandStatement';
import { LittilEnvironmentSettings } from './littil-environment-settings';

export interface MaintenanceStackProps extends StackProps {
    littil: LittilEnvironmentSettings;
    apiVpc: Vpc;
    maintenanceContainer: {
        enable: boolean;
        ecrRepository: {
            awsAccount: string;
            name: string;
        };
        imageTag: string;
    };
    database: {
        host: string;
        port: string;
        name: string;
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

        const ecsExecUserName = 'LITTIL-NL-' + props.littil.environment + '-maintenance-ECSExec-User';
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
            repositoryArn: 'arn:aws:ecr:eu-west-1:' + props.maintenanceContainer.ecrRepository.awsAccount + ':repository/' + props.maintenanceContainer.ecrRepository.name,
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
            vpc: props.apiVpc,
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
        const ecsExecPolicy = new Policy(this, 'LITTIL-NL-' + props.littil.environment + '-maintenance-ECSExec-Policy');
        ecsExecPolicy.addStatements(
            allowEcsExecuteCommandStatement(maintenanceFargateService.cluster.clusterArn, this.region, this.account),
            allowEcsDescribeTaskStatement(this.region, this.account),
        );
        ecsExecPolicy.attachToUser(ecsExecUser);
    }
}
