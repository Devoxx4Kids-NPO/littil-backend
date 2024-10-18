import { CfnOutput, Stack, StackProps } from 'aws-cdk-lib';
import { InstanceClass, InstanceSize, InstanceType, SubnetType, Vpc } from 'aws-cdk-lib/aws-ec2';
import {
    DatabaseInstanceEngine,
    DatabaseInstanceFromSnapshot,
    MariaDbEngineVersion,
    ParameterGroup,
    SnapshotCredentials
} from 'aws-cdk-lib/aws-rds';
import { DatabaseInstanceFromSnapshotProps } from 'aws-cdk-lib/aws-rds/lib/instance';
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
            version: MariaDbEngineVersion.VER_10_6_17,
        });

        const rdsParameterGroup = new ParameterGroup(this, 'littil-rds-parametergroup', {
            engine: rdsEngine,
            parameters: {
                log_bin_trust_function_creators: '1',
            }
        });

        const dbUserName = 'littil_' + props.littil.environment.substring(0, 7);
        // TODO: As soon as supported, use 'secretName' property. Will make looking this secret up easier.
        const snapshotCredentials = SnapshotCredentials.fromGeneratedSecret(dbUserName);

        const databaseProperties: DatabaseInstanceFromSnapshotProps = {
            credentials: snapshotCredentials,
            publiclyAccessible: false,
            vpc: props.apiVpc,
            vpcSubnets: {
                subnetType: SubnetType.PRIVATE_ISOLATED,
            },
            engine: rdsEngine,
            parameterGroup: rdsParameterGroup,
            instanceType: InstanceType.of(
                InstanceClass.T4G,
                InstanceSize.MICRO,
            ),
            snapshotIdentifier: 'apidatabasestack-snapshot-littilapidatabase74782804-gy2ioxtzgpgy',
        };

        const database = new DatabaseInstanceFromSnapshot(this, 'LittilApiDatabase', databaseProperties);

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
