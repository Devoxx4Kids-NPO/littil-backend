import { Stack, StackProps } from 'aws-cdk-lib';
import {
    AmazonLinuxCpuType,
    AmazonLinuxGeneration,
    AmazonLinuxImage,
    CfnEIP,
    CfnEIPAssociation,
    Instance,
    InstanceClass,
    InstanceSize,
    InstanceType,
    KeyPair,
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
    elasticIp: CfnEIP;
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


        const keypair = new KeyPair(this, 'ApiEc2Keypair', {
            keyPairName: 'EC2 Keypair',
            publicKeyMaterial: 'ssh-ed25519 AAAAC3NzaC1lZDI1NTE5AAAAIN5OhXLM34h3omM+5AoaYgUktcBMUBrC5awrPoItmf3S prod@littil.org',
        });

        const apiDomain = 'api.' + (props.littil.environment === 'production' ? '' : props.littil.environment + '.') + 'littil.org';

        const littilServerConf = fs.readFileSync('lib/nginx/serverconfiguration')
            .toString('utf-8')
            .replaceAll('%API_DOMAIN%', apiDomain);

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
        // TODO: Find by tags
        const littilBackendDatabaseSecretName = 'ApiDatabaseStackLittilApiDa-ia57olJcscCP';

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
            // TODO: Pass these secret values to the docker process in a more secure way than storing them in a file
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

            'docker run -p 8080:8080 --env-file littil.env -d ' + dockerImage,
            // TODO: Test whether we can immediately delete the file
            // 'rm littil.env',

            /* Install Nginx. */
            'amazon-linux-extras install nginx1',
            'systemctl stop nginx',

            /* Nginx configuration. */
            'echo ' + Buffer.from(littilServerConf).toString('base64') + ' | base64 -d > /etc/nginx/conf.d/' + apiDomain + '.conf',

            /* Certbot. */
            'amazon-linux-extras install epel',
            'yum update -y',
            'yum install certbot -y',
            // Will fail when there's no DNS A record pointing to the above created Elastic IP. Should be interactively waiting or perhaps put the Elastic IP in a separate stack that needs to be created first.
            'letsencrypt certonly --standalone -d ' + apiDomain + ' -m lennert.gijsen@littil.org --agree-tos --no-eff-email',

            'systemctl enable docker',
            'systemctl enable nginx',
            'systemctl start nginx',
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
            keyPair: keypair,
            userData,
        });

        littilApiPolicy.attachToRole(ec2Instance.role);

        new CfnEIPAssociation(this, 'Ec2EipAssociation', {
            eip: props.elasticIp.ref,
            instanceId: ec2Instance.instanceId,
        });
        /**/

        /* Database access. */
        const databaseSecurityGroup = SecurityGroup.fromSecurityGroupId(this, 'DatabaseSecurityGroup', props.database.securityGroup.id);
        // TODO: Uncomment, test and remove the allTcp rule
        // databaseSecurityGroup.connections.allowFrom(ec2SecurityGroup, Port.tcp(parseInt(props.database.port)));
        databaseSecurityGroup.connections.allowFrom(ec2SecurityGroup, Port.allTcp());
    }
}
