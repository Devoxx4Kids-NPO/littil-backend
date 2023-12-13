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
import { Construct } from 'constructs';
import { LittilEnvironmentSettings } from './littil-environment-settings';

export interface DatabaseStackProps extends StackProps {
    littil: LittilEnvironmentSettings;

    apiVpc: Vpc;

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

        const databaseName = 'LittilDatabase';

        const rdsEngine = DatabaseInstanceEngine.mariaDb({
            version: MariaDbEngineVersion.VER_10_6_8,
        });

        const rdsParameterGroup = new ParameterGroup(this, 'littil-rds-parametergroup', {
            engine: rdsEngine,
            parameters: {
                log_bin_trust_function_creators: '1',
            }
        });

        const credentials = Credentials.fromGeneratedSecret(
            'littil_' + props.littil.environment.substring(0, 7),
            {
                secretName: 'littil/backend/' + props.littil.environment + '/database',
            }
        );

        const databaseProperties: DatabaseInstanceProps = {
            databaseName,
            credentials,
            publiclyAccessible: false,
            vpc: props.apiVpc,
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
