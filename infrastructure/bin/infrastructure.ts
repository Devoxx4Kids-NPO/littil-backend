#!/usr/bin/env node
import { App, Fn, StackProps } from 'aws-cdk-lib';
import 'source-map-support/register';
import { ApiStack, ApiStackProps } from '../lib/api-stack';
import { CertificateStack } from '../lib/certificate-stack';
import { DatabaseStack, DatabaseStackProps } from '../lib/database-stack';
import { EcrStack, EcrStackProps } from '../lib/ecr-stack';
import { MaintenanceEcrStack } from '../lib/maintenance-ecr-stack';
import { VpcStack } from '../lib/vpc-stack';

const app = new App();

const env = {
    region: 'eu-west-1',
    account: app.node.tryGetContext('account'),
};

const crossStackReferenceExportNames = {
    apiEcrRepositoryArn: 'apiEcrRepositoryArn',
    apiEcrRepositoryName: 'apiEcrRepositoryName',
    apiCertificateArn: 'apiCertificateArn',
    maintenanceEcrRepositoryArn: 'maintenanceEcrRepositoryArn',
    maintenanceEcrRepositoryName: 'maintenanceEcrRepositoryName',
    databaseHost: 'databaseHost',
    databasePort: 'databasePort',
    databaseName: 'databaseName',
    databaseSecurityGroup: 'databaseSecurityGroup',
};

const certificateStackProps = {
    env,
    apiCertificateArnExportName: crossStackReferenceExportNames.apiCertificateArn,
};
new CertificateStack(app, 'ApiCertificatesStack', certificateStackProps);

const apiEcrProps: EcrStackProps = {
    env,
    apiRepositoryNameExportName: crossStackReferenceExportNames.apiEcrRepositoryName,
    apiRepositoryArnExportName: crossStackReferenceExportNames.apiEcrRepositoryArn,
};
new EcrStack(app, 'ApiEcrStack', apiEcrProps);

const maintenanceEcrProps = {
    env,
    maintenanceEcrRepositoryNameExportName: crossStackReferenceExportNames.maintenanceEcrRepositoryName,
    maintenanceEcrRepositoryArnExportName: crossStackReferenceExportNames.maintenanceEcrRepositoryArn,
};
new MaintenanceEcrStack(app, 'MaintenanceEcrStack', maintenanceEcrProps);

const vpcStackProps: StackProps = {
    env,
};
new VpcStack(app, 'ApiVpcStack', vpcStackProps);

const vpcId = 'vpc-0587b532bb62f5ccc';

const databaseStackProps: DatabaseStackProps = {
    apiVpc: {
        id: vpcId,
    },
    env,
    databaseHostExportName: crossStackReferenceExportNames.databaseHost,
    databasePortExportName: crossStackReferenceExportNames.databasePort,
    databaseNameExportName: crossStackReferenceExportNames.databaseName,
    databaseSecurityGroupIdExportName: crossStackReferenceExportNames.databaseSecurityGroup,
};
new DatabaseStack(app, 'ApiDatabaseStack', databaseStackProps);

const apiStackProps: ApiStackProps = {
    apiVpc: {
        id: vpcId,
    },
    env,
    ecrRepository: {
        name: Fn.importValue(crossStackReferenceExportNames.apiEcrRepositoryName),
        arn: Fn.importValue(crossStackReferenceExportNames.apiEcrRepositoryArn),
    },
    apiCertificateArn: Fn.importValue(crossStackReferenceExportNames.apiCertificateArn),
    database: {
        host: Fn.importValue(crossStackReferenceExportNames.databaseHost),
        port: Fn.importValue(crossStackReferenceExportNames.databasePort),
        name: Fn.importValue(crossStackReferenceExportNames.databaseName),
        vpcId,
        securityGroup: {
            id: Fn.importValue(crossStackReferenceExportNames.databaseSecurityGroup),
        },
    },
};
new ApiStack(app, 'ApiStack', apiStackProps);
/*
const maintenanceProps: MaintenanceStackProps = {
    env,
    maintenanceContainer: {
        enable: false,
        imageTag: '1.0.2',
        ecrRepository: {
            name: Fn.importValue(crossStackReferenceExportNames.maintenanceEcrRepositoryName),
            arn: Fn.importValue(crossStackReferenceExportNames.maintenanceEcrRepositoryArn),
        },
    },
    database: {
        host: Fn.importValue(crossStackReferenceExportNames.databaseHost),
        port: Fn.importValue(crossStackReferenceExportNames.databasePort),
        name: Fn.importValue(crossStackReferenceExportNames.databaseName),
        vpcId,
        securityGroup: {
            id: Fn.importValue(crossStackReferenceExportNames.databaseSecurityGroup),
        },
    },
};
new MaintenanceStack(app, 'MaintenanceServiceStack', maintenanceProps);
*/
