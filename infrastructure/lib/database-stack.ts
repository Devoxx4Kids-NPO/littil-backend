import { CfnOutput, Stack, StackProps } from 'aws-cdk-lib';
import { InstanceClass, InstanceSize, InstanceType, Vpc } from 'aws-cdk-lib/aws-ec2';
import {
    Credentials,
    DatabaseInstance,
    DatabaseInstanceEngine,
    MariaDbEngineVersion,
    ParameterGroup
} from 'aws-cdk-lib/aws-rds';
import { DatabaseInstanceProps } from 'aws-cdk-lib/aws-rds/lib/instance';
import { Secret as SecretsManagerSecret } from 'aws-cdk-lib/aws-secretsmanager';
import { Construct } from 'constructs';

export interface DatabaseStackProps extends StackProps {
    apiVpc: {
        id: string;
    };

    databaseHostExportName: string;
    databasePortExportName: string;
    databaseNameExportName: string;
    databaseSecurityGroupIdExportName: string;
}

export class DatabaseStack extends Stack {
    constructor(scope: Construct,
                id: string,
                props: DatabaseStackProps) {
        super(scope, id, props);

        const vpc = Vpc.fromLookup(this, 'ApiVpc', {
            vpcId: props.apiVpc.id,
        });

        const databaseName = 'LittilDatabase';

        const littilDatabaseSecretName = 'littil/backend/databaseCredentials';
        const littilBackendDatabaseSecret = SecretsManagerSecret.fromSecretNameV2(this, 'LittilBackendDatabaseSecret', littilDatabaseSecretName);

        const rdsEngine = DatabaseInstanceEngine.mariaDb({
            version: MariaDbEngineVersion.VER_10_6_8,
        });

        const rdsParameterGroup = new ParameterGroup(this, 'littil-rds-parametergroup', {
            engine: rdsEngine,
            parameters: {
                log_bin_trust_function_creators: '1',
            }
        });

        const databaseProperties: DatabaseInstanceProps = {
            databaseName,
            credentials: Credentials.fromSecret(littilBackendDatabaseSecret),
            publiclyAccessible: false,
            vpc,
            engine: rdsEngine,
            parameterGroup: rdsParameterGroup,
            instanceType: InstanceType.of(
                InstanceClass.T4G,
                InstanceSize.MICRO,
            ),
        };

        const database = new DatabaseInstance(this, 'LittilApiDatabase', databaseProperties);
        new CfnOutput(this, 'databaseHost', {
            value: database.instanceEndpoint.hostname,
            exportName: props.databaseHostExportName,
        });
        new CfnOutput(this, 'databasePort', {
            value: String(database.instanceEndpoint.port),
            exportName: props.databasePortExportName,
        });
        new CfnOutput(this, 'databasename', {
            value: databaseName,
            exportName: props.databaseNameExportName,
        });

        const databaseSecurityGroup = database.connections.securityGroups[0];
        new CfnOutput(this, 'DatabaseSecurityGroupId', {
            value: databaseSecurityGroup.securityGroupId,
            exportName: props.databaseSecurityGroupIdExportName,
        });
    }
}
