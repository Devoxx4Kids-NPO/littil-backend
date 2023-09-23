import { NestedStack } from 'aws-cdk-lib';
import { CfnAccessKey, Effect, Policy, PolicyStatement, User } from 'aws-cdk-lib/aws-iam';
import { LogGroup } from 'aws-cdk-lib/aws-logs';
import { NestedStackProps } from 'aws-cdk-lib/core/lib/nested-stack';
import { Construct } from 'constructs';
import { LittilEnvironmentSettings } from './littil-environment-settings';

export interface LoggingStackProps extends NestedStackProps {
    littil: LittilEnvironmentSettings;
    logGroupName: string;
}

export class LoggingStack extends NestedStack {
    public readonly cloudwatchLogGroup: LogGroup;
    public readonly loggingAccessKey: CfnAccessKey;

    private readonly loggingPolicy: Policy;

    constructor(scope: Construct,
                id: string,
                private readonly props: LoggingStackProps) {
        super(scope, id, props);

        this.cloudwatchLogGroup = new LogGroup(this, 'BackendLogGroup', {
            logGroupName: props.logGroupName,
        });

        const cloudwatchLoggingStatement = new PolicyStatement({
            effect: Effect.ALLOW,
            actions: [
                'logs:DescribeLogStreams',
                'logs:CreateLogStream',
                'logs:PutLogEvents',
            ],
            resources: [
                this.cloudwatchLogGroup.logGroupArn,
            ],
        });
        this.loggingPolicy = new Policy(this, 'QuarkusCloudwatchLoggingPolicy');
        this.loggingPolicy.addStatements(cloudwatchLoggingStatement);

        /* The Quarkus Cloudwatch logging library used needs to have an access key to authenticate, so create a user. */
        const littilBackendCloudwatchLoggingUser = new User(this, 'CloudwatchLoggingUser', {
            userName: 'LITTIL-NL-' + this.props.littil.environment + '-' + props.logGroupName
        });
        this.loggingPolicy.attachToUser(littilBackendCloudwatchLoggingUser);

        this.loggingAccessKey = new CfnAccessKey(this, 'CloudwatchLoggingAccessKey', {
            userName: littilBackendCloudwatchLoggingUser.userName,
        });
    }
}
