#!/usr/bin/env node
import { App, Fn, StackProps } from 'aws-cdk-lib';
import 'source-map-support/register';
import { ApiStack, ApiStackProps } from '../lib/api-stack';
import { CertificateStack, CertificateStackProps } from '../lib/certificate-stack';
import { DatabaseStack, DatabaseStackProps } from '../lib/database-stack';
import { EcrStack, EcrStackProps } from '../lib/ecr-stack';
import { isLittilEnvironment, LittilEnvironment } from '../lib/littil-environment';
import { LittilEnvironmentSettings } from '../lib/littil-environment-settings';
import { MaintenanceEcrStack } from '../lib/maintenance-ecr-stack';
import { MaintenanceStack, MaintenanceStackProps } from '../lib/maintenance-stack';
import { VpcStack } from '../lib/vpc-stack';

const app = new App();

const awsAccountId = app.node.tryGetContext('account');
if (!awsAccountId) {
    throw new Error('Please supply the ID of the AWS account this stack needs to be build for');
}

const littilEnvironment = app.node.tryGetContext('environment');
if (!isLittilEnvironment(littilEnvironment)) {
    throw new Error('environment needs to be of type LittilEnvironment');
}

const env = {
    region: 'eu-west-1',
    account: awsAccountId,
};

const littilDomain = littilEnvironment !== LittilEnvironment.production
    ? littilEnvironment + '.littil.org'
    : 'littil.org';

const littilEnvironmentSettings: LittilEnvironmentSettings = {
    environment: littilEnvironment,
    backendDomainName: 'api.' + littilDomain,
    httpCorsOrigin: 'https://' + littilDomain,
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

const certificateStackProps: CertificateStackProps = {
    env,
    littil: littilEnvironmentSettings,
    apiCertificateArnExportName: crossStackReferenceExportNames.apiCertificateArn,
};
new CertificateStack(app, 'ApiCertificatesStack', certificateStackProps);

// TODO: Deploy ECR stack to shared account (don't create a separate ECR repository for staging and production)
const apiEcrProps: EcrStackProps = {
    env,
    littil: littilEnvironmentSettings,
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

// TODO: Lookup
//  Lookup is already performed in the stacks. Perhaps we can look up by name instead of ID so we can use the same identifier for staging and production?
const vpcId = littilEnvironment === LittilEnvironment.staging
    ? 'vpc-0587b532bb62f5ccc'
    : '';

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
    littil: littilEnvironmentSettings,
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

const enableMaintenanceContainer = app.node.tryGetContext('maintenance');
if (enableMaintenanceContainer === 'true') {
    const maintenanceProps: MaintenanceStackProps = {
        env,
        littil: littilEnvironmentSettings,
        apiVpc: {
            id: vpcId,
        },
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
}
