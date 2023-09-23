import { Stack, StackProps } from 'aws-cdk-lib';
import {
    AmazonLinuxCpuType,
    AmazonLinuxGeneration,
    AmazonLinuxImage,
    CfnEIP, CfnEIPAssociation, CfnKeyPair,
    Instance,
    InstanceClass,
    InstanceSize,
    InstanceType, Peer, Port,
    SecurityGroup,
    SubnetType, UserData,
    Vpc
} from 'aws-cdk-lib/aws-ec2';
import { CfnAccessKey, Effect, Policy, PolicyStatement, Role, User } from 'aws-cdk-lib/aws-iam';
import { LogGroup } from 'aws-cdk-lib/aws-logs';
import { Construct } from 'constructs';
import { LittilEnvironmentSettings } from './littil-environment-settings';
import { LoggingStack } from './logging-stack';

const fs = require('fs');

export interface ApiEc2StackProps extends StackProps {
    littil: LittilEnvironmentSettings;

    apiVpc: {
        id: string;
    };
    database: {
        port: string;
        securityGroup: {
            id: string;
        };
    };
}

export class ApiEc2Stack extends Stack {
    constructor(scope: Construct,
                id: string,
                props: ApiEc2StackProps) {
        super(scope, id, props);

        const vpc = Vpc.fromLookup(this, 'ApiVpc', {
            vpcId: props.apiVpc.id,
        });

        const ec2SecurityGroup = new SecurityGroup(this, 'ApiInstanceSecurityGroup', {
            vpc,
        });
        ec2SecurityGroup.addIngressRule(Peer.anyIpv4(), Port.tcp(443), 'HTTPS over IPv4');
        ec2SecurityGroup.addIngressRule(Peer.anyIpv6(), Port.tcp(443), 'HTTPS over IPv6');

        const elasticIp = new CfnEIP(this, 'ApiIP', {
            tags: [
                {
                    key: 'Name',
                    value: 'API EC2',
                }
            ]
        });

        const keypair = new CfnKeyPair(this, 'ApiEc2Keypair', {
            keyName: 'EC2 Keypair',
            publicKeyMaterial: '---- BEGIN SSH2 PUBLIC KEY ----\n' +
                'Comment: "rsa-key-20230917"\n' +
                'AAAAB3NzaC1yc2EAAAADAQABAAABAQCbNh2l09RvvYyDtIXjtqnJG/nFYtV44Gwx\n' +
                'TjYteFvwyK3wSFlgA0qFIjoUxrh5KLGsVYzoz7JmcD3thg7YdbcCy3agZ8EIK8ds\n' +
                '8ly37dGn5D1u7re5AU+7Y+LPsw31lxjusZCFPJEElKexryuhIP043EAe/pWXDfM6\n' +
                '6urIhgXGKRhxu3prw43xX8STTGGUaropESaEnudAxMlgHu/nNI8DauQhf5LZWboT\n' +
                'YjnB2X8lpDY9Vsab6e0OINUcXgHvH9A9r/twBPt1Hx8MXWjmDTEiU5+vuDOcws+g\n' +
                '4/TsVUxuJ/MqhBkJj/ou2cVkCcBuzbreQdd9zlc5J5CJSfReg4dl\n' +
                '---- END SSH2 PUBLIC KEY ----\n',
        });

        const littilServerConf = fs.readFileSync('lib/nginx/serverconfiguration')
            .toString('utf-8')
            .replaceAll('%ENVIRONMENT%', props.littil.environment);

        const userData = UserData.forLinux();
        userData.addCommands(
            'sudo su',
            'yum update',

            'yum install docker -y',
            'systemctl start docker.service',
            'docker run -p 8081:80 -P -d nginxdemos/hello',
            'aws ecr get-login-password --region ' + this.region + ' | docker login --username AWS --password-stdin ' + this.account + '.dkr.ecr.' + this.region + '.amazonaws.com',
            'docker pull ' + this.account + '.dkr.ecr.eu-west-1.amazonaws.com/littil-backend:latest',

            /* Install Nginx. */
            'amazon-linux-extras install nginx1',
            'systemctl stop nginx',

            /* Nginx configuration. */
            'echo test > /etc/nginx/conf.d/test',
            'echo ' + Buffer.from(littilServerConf).toString('base64') + ' > /etc/nginx/conf.d/test2',
            'echo ' + Buffer.from(littilServerConf).toString('base64') + ' | base64 -d > /etc/nginx/conf.d/api.' + props.littil.environment + '.littil.org.conf',

            /* Certbot. */
            'amazon-linux-extras install epel',
            'yum update',
            'yum install certbot -y',
            'letsencrypt certonly --standalone -d api.' + props.littil.environment + '.littil.org -m lennert.gijsen@littil.org --agree-tos --no-eff-email',

            'systemctl enable docker',
            'systemctl enable nginx',
        );

        const pullPolicyStatement = new PolicyStatement({
            effect: Effect.ALLOW,
            actions: [
                'ecr:BatchGetImage',
                'ecr:BatchCheckLayerAvailability',
                'ecr:GetDownloadUrlForLayer',
                'ecr:GetRepositoryPolicy',
                'ecr:DescribeRepositories',
                'ecr:ListImages',
                'ecr:DescribeImages',
                'ecr:GetAuthorizationToken',
            ],
            resources: [
                '*',
            ]
        });
        const pullPolicy = new Policy(this, 'PullPolicy');
        pullPolicy.addStatements(
            pullPolicyStatement,
        );

        /**/
        const ec2Instance = new Instance(this, 'ApiInstance', {
            vpc,
            instanceType: InstanceType.of(InstanceClass.T4G, InstanceSize.NANO),
            machineImage: new AmazonLinuxImage({
                generation: AmazonLinuxGeneration.AMAZON_LINUX_2,
                cpuType: AmazonLinuxCpuType.ARM_64,
            }),
            vpcSubnets: {subnetType: SubnetType.PUBLIC},
            securityGroup: ec2SecurityGroup,
            keyName: keypair.keyName,
            userData,
        });

        pullPolicy.attachToRole(ec2Instance.role);

        new CfnEIPAssociation(this, 'Ec2EipAssociation', {
            eip: elasticIp.ref,
            instanceId: ec2Instance.instanceId,
        });
        /**/

        /* Logging. */
        const apiEc2LoggingStack = new LoggingStack(this, 'ApiEc2LoggingStack', {
            littil: props.littil,
            logGroupName: 'BackendApiEc2Logs',
        });

        /* Database access. */
        const databaseSecurityGroup = SecurityGroup.fromSecurityGroupId(this, 'DatabaseSecurityGroup', props.database.securityGroup.id);
        databaseSecurityGroup.connections.allowFrom(ec2SecurityGroup, Port.tcp(parseInt(props.database.port)));
    }
}
