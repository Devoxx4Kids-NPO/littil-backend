import { Stack, StackProps } from 'aws-cdk-lib';
import {
    AmazonLinuxCpuType,
    AmazonLinuxGeneration,
    AmazonLinuxImage,
    CfnEIP,
    CfnEIPAssociation,
    CfnKeyPair,
    Instance,
    InstanceClass,
    InstanceSize,
    InstanceType,
    Peer,
    Port,
    SecurityGroup,
    SubnetType,
    UserData,
    Vpc
} from 'aws-cdk-lib/aws-ec2';
import { Effect, Policy, PolicyStatement } from 'aws-cdk-lib/aws-iam';
import { Construct } from 'constructs';
import { LittilEnvironmentSettings } from './littil-environment-settings';
import { LoggingStack } from './logging-stack';

const fs = require('fs');

export interface ApiEc2StackProps extends StackProps {
    littil: LittilEnvironmentSettings;

    apiVpc: Vpc;
    ecrRepository: {
        awsAccount: string;
        name: string;
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

export class ApiEc2Stack extends Stack {
    constructor(scope: Construct,
                id: string,
                props: ApiEc2StackProps) {
        super(scope, id, props);

        const ec2SecurityGroup = new SecurityGroup(this, 'ApiInstanceSecurityGroup', {
            vpc: props.apiVpc,
        });
        ec2SecurityGroup.addIngressRule(Peer.anyIpv4(), Port.tcp(443), 'HTTPS over IPv4');
        ec2SecurityGroup.addIngressRule(Peer.anyIpv6(), Port.tcp(443), 'HTTPS over IPv6');
        ec2SecurityGroup.addIngressRule(Peer.anyIpv4(), Port.tcp(80), 'HTTP over IPv4');
        ec2SecurityGroup.addIngressRule(Peer.anyIpv6(), Port.tcp(80), 'HTTP over IPv6');
        ec2SecurityGroup.addIngressRule(Peer.anyIpv4(), Port.tcp(22), 'SSH access');
        ec2SecurityGroup.addIngressRule(Peer.anyIpv6(), Port.tcp(22), 'SSH access');

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
            publicKeyMaterial: 'ssh-ed25519 AAAAC3NzaC1lZDI1NTE5AAAAIN5OhXLM34h3omM+5AoaYgUktcBMUBrC5awrPoItmf3S prod@littil.org',
        });

        const littilServerConf = fs.readFileSync('lib/nginx/serverconfiguration')
            .toString('utf-8')
            .replaceAll('%ENVIRONMENT%', props.littil.environment);

        /* Logging. */
        const logGroupName = 'BackendApiEc2Logs';
        const apiEc2LoggingStack = new LoggingStack(this, 'ApiEc2LoggingStack', {
            littil: props.littil,
            logGroupName,
        });

        const dockerTag = '1.3.1';
        const dockerImage = props.ecrRepository.awsAccount + '.dkr.ecr.eu-west-1.amazonaws.com/' + props.ecrRepository.name + ':' + dockerTag;

        const littilOidcSecretName = 'littil/backend/' + props.littil.environment + '/oidc';
        const littilSmtpSecretName = 'littil/backend/' + props.littil.environment + '/smtp';
        const littilBackendDatabaseSecretName = 'littil/backend/' + props.littil.environment + '/database';

        const userData = UserData.forLinux();
        userData.addCommands(
            'sudo su',
            'yum update -y',

            'yum install docker -y',
            'systemctl start docker.service',

            'aws ecr get-login-password --region ' + this.region + ' | docker login --username AWS --password-stdin ' + props.ecrRepository.awsAccount + '.dkr.ecr.' + this.region + '.amazonaws.com',
            'docker pull ' + dockerImage,

            'echo DATASOURCE_HOST="' + props.database.host + '" >> littil.env',
            'echo DATASOURCE_PORT=' + props.database.port + ' >> littil.env',
            'echo DATASOURCE_DATABASE=' + props.database.name + ' >> littil.env',
            'echo QUARKUS_HTTP_CORS_ORIGINS="' + props.littil.httpCorsOrigin + '" >> littil.env',
            'echo QUARKUS_LOG_CLOUDWATCH_ACCESS_KEY_ID="' + apiEc2LoggingStack.loggingAccessKey.ref + '" >> littil.env',
            'echo QUARKUS_LOG_CLOUDWATCH_ACCESS_KEY_SECRET="' + apiEc2LoggingStack.loggingAccessKey.attrSecretAccessKey + '" >> littil.env',
            'echo QUARKUS_LOG_CLOUDWATCH_LOG_GROUP="' + apiEc2LoggingStack.cloudwatchLogGroup.logGroupName + '" >> littil.env',
            'echo QUARKUS_LOG_CLOUDWATCH_REGION=' + this.region + ' >> littil.env',
            'echo QUARKUS_LOG_CLOUDWATCH_LOG_STREAM_NAME=' + logGroupName + ' >> littil.env',
            'echo QUARKUS_MAILER_FROM=no-reply@mail.littil.org >> littil.env',

            'yum install jq -y',
            'echo DATASOURCE_USERNAME=$(aws secretsmanager get-secret-value --region ' + this.region + ' --secret-id ' + littilBackendDatabaseSecretName + ' | jq --raw-output \'.SecretString\' | jq -r .username) >> littil.env',
            'echo DATASOURCE_PASSWORD=$(aws secretsmanager get-secret-value --region ' + this.region + ' --secret-id ' + littilBackendDatabaseSecretName + ' | jq --raw-output \'.SecretString\' | jq -r .password) >> littil.env',
            'echo OIDC_CLIENT_ID=$(aws secretsmanager get-secret-value --region ' + this.region + ' --secret-id ' + littilOidcSecretName + ' | jq --raw-output \'.SecretString\' | jq -r .oidcClientId) >> littil.env',
            'echo OIDC_CLIENT_SECRET=$(aws secretsmanager get-secret-value --region ' + this.region + ' --secret-id ' + littilOidcSecretName + ' | jq --raw-output \'.SecretString\' | jq -r .oidcClientSecret) >> littil.env',
            'echo OIDC_TENANT=$(aws secretsmanager get-secret-value --region ' + this.region + ' --secret-id ' + littilOidcSecretName + ' | jq --raw-output \'.SecretString\' | jq -r .oidcTenant) >> littil.env',
            'echo M2M_CLIENT_ID=$(aws secretsmanager get-secret-value --region ' + this.region + ' --secret-id ' + littilOidcSecretName + ' | jq --raw-output \'.SecretString\' | jq -r .m2mClientId) >> littil.env',
            'echo M2M_CLIENT_SECRET=$(aws secretsmanager get-secret-value --region ' + this.region + ' --secret-id ' + littilOidcSecretName + ' | jq --raw-output \'.SecretString\' | jq -r .m2mClientSecret) >> littil.env',
            'echo SMTP_HOST=$(aws secretsmanager get-secret-value --region ' + this.region + ' --secret-id ' + littilSmtpSecretName + ' | jq --raw-output \'.SecretString\' | jq -r .smtpHost) >> littil.env',
            'echo SMTP_USERNAME=$(aws secretsmanager get-secret-value --region ' + this.region + ' --secret-id ' + littilSmtpSecretName + ' | jq --raw-output \'.SecretString\' | jq -r .smtpUsername) >> littil.env',
            'echo SMTP_PASSWORD=$(aws secretsmanager get-secret-value --region ' + this.region + ' --secret-id ' + littilSmtpSecretName + ' | jq --raw-output \'.SecretString\' | jq -r .smtpPassword) >> littil.env',

            'docker run -p 8080:80 --env-file littil.env -d ' + dockerImage,

            /* Install Nginx. */
            'amazon-linux-extras install nginx1',
            'systemctl stop nginx',

            /* Nginx configuration. */
            'echo ' + Buffer.from(littilServerConf).toString('base64') + ' | base64 -d > /etc/nginx/conf.d/api.' + props.littil.environment + '.littil.org.conf',

            /* Certbot. */
            'amazon-linux-extras install epel',
            'yum update -y',
            'yum install certbot -y',
            'letsencrypt certonly --standalone -d api.littil.org -m lennert.gijsen@littil.org --agree-tos --no-eff-email',

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
        const readSecretsPolicyStatment = new PolicyStatement({
            effect: Effect.ALLOW,
            actions: [
                'secretsmanager:GetSecretValue',
            ],
            resources: [
                '*',
            ]
        });
        const littilApiPolicy = new Policy(this, 'PullPolicy');
        littilApiPolicy.addStatements(
            pullPolicyStatement,
            readSecretsPolicyStatment,
        );

        /**/
        const ec2Instance = new Instance(this, 'ApiInstance', {
            vpc: props.apiVpc,
            instanceType: InstanceType.of(InstanceClass.T3A, InstanceSize.MICRO),
            machineImage: new AmazonLinuxImage({
                generation: AmazonLinuxGeneration.AMAZON_LINUX_2,
                cpuType: AmazonLinuxCpuType.X86_64,
            }),
            vpcSubnets: {subnetType: SubnetType.PUBLIC},
            securityGroup: ec2SecurityGroup,
            keyName: keypair.keyName,
            userData,
        });

        littilApiPolicy.attachToRole(ec2Instance.role);

        new CfnEIPAssociation(this, 'Ec2EipAssociation', {
            eip: elasticIp.ref,
            instanceId: ec2Instance.instanceId,
        });
        /**/

        /* Database access. */
        const databaseSecurityGroup = SecurityGroup.fromSecurityGroupId(this, 'DatabaseSecurityGroup', props.database.securityGroup.id);
        // databaseSecurityGroup.connections.allowFrom(ec2SecurityGroup, Port.tcp(parseInt(props.database.port)));
        databaseSecurityGroup.connections.allowFrom(ec2SecurityGroup, Port.allTcp());
    }
}
