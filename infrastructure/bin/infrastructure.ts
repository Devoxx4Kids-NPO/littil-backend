#!/usr/bin/env node
import { App, Fn, StackProps } from 'aws-cdk-lib';
import 'source-map-support/register';
import { ApiEc2Stack, ApiEc2StackProps } from '../lib/api-ec2-stack';
import { ApiStack, ApiStackProps } from '../lib/api-stack';
import { CertificateStack, CertificateStackProps } from '../lib/certificate-stack';
import { DatabaseStack, DatabaseStackProps } from '../lib/database-stack';
import { EcrStack, EcrStackProps } from '../lib/ecr-stack';
import { LittilEnvironment } from '../lib/littil-environment';
import { LittilEnvironmentSettings } from '../lib/littil-environment-settings';
import {
    isLittilAwsAccountConfiguration,
    LittilAccountType,
    LittilAwsAccountConfiguration
} from '../lib/LittilAwsAccountConfiguration';
import { MaintenanceEcrStack, MaintenanceEcrStackProps } from '../lib/maintenance-ecr-stack';
import { MaintenanceStack, MaintenanceStackProps } from '../lib/maintenance-stack';
import { VpcStack } from '../lib/vpc-stack';

const fs = require('fs');

const app = new App();

const ecrApiRepositoryName = 'littil-backend';
const ecrMaintenanceRepositoryName = 'littil-backend-maintenance';

const awsAccountName = app.node.tryGetContext('accountName');
const accountConfiguration: LittilAwsAccountConfiguration = JSON.parse(fs.readFileSync('accounts.json', 'utf8'));
if (!isLittilAwsAccountConfiguration(accountConfiguration)) {
    throw new Error('Invalid account configuration');
}
const awsAccount = accountConfiguration.accounts.find(account => account.name === awsAccountName);
if (!awsAccount) {
    throw new Error('Could not find account in accounts.json. Please provide an account name (--context accountName=<accountName>) that matches an entry in accounts.json');
}
const awsAccountId = awsAccount.id;

const sharedAccount = accountConfiguration.accounts.find(account => account.type === LittilAccountType.SHARED);
if (!sharedAccount) {
    throw new Error('Could not find shared account in accounts.json');
}
const sharedAccountId = sharedAccount.id;

const workloadAccounts = accountConfiguration.accounts
    .filter(account => account.type === LittilAccountType.WORKLOAD)
    .map(account => account.id);

const env = {
    region: 'eu-west-1',
    account: awsAccountId,
};

const crossStackReferenceExportNames = {
    apiCertificateArn: 'apiCertificateArn',
    databaseHost: 'databaseHost',
    databasePort: 'databasePort',
    databaseName: 'databaseName',
    databaseSecurityGroup: 'databaseSecurityGroup',
};

const littilEnvironment = app.node.tryGetContext('environment');
if (!littilEnvironment) {
    const apiEcrProps: EcrStackProps = {
        env,
        workloadAccounts,
        ecrApiRepositoryName: ecrApiRepositoryName,
    };
    new EcrStack(app, 'ApiEcrStack', apiEcrProps);

    const maintenanceEcrProps: MaintenanceEcrStackProps = {
        env,
        workloadAccounts,
        ecrMaintenanceRepositoryName: ecrMaintenanceRepositoryName,
    };
    new MaintenanceEcrStack(app, 'MaintenanceEcrStack', maintenanceEcrProps);
} else {
    const littilDomain = littilEnvironment !== LittilEnvironment.production
        ? littilEnvironment + '.littil.org'
        : 'littil.org';

    const littilEnvironmentSettings: LittilEnvironmentSettings = {
        environment: littilEnvironment,
        backendDomainName: 'api.' + littilDomain,
        httpCorsOrigin: 'https://' + littilDomain,
    };

    const certificateStackProps: CertificateStackProps = {
        env,
        littil: littilEnvironmentSettings,
        apiCertificateArnExportName: crossStackReferenceExportNames.apiCertificateArn,
    };
    new CertificateStack(app, 'ApiCertificatesStack', certificateStackProps);

    const vpcStackProps: StackProps = {
        env,
    };
    const vpcStack = new VpcStack(app, 'ApiVpcStack', vpcStackProps);

    const databaseStackProps: DatabaseStackProps = {
        littil: littilEnvironmentSettings,
        apiVpc: vpcStack.vpc,
        env,
        databaseHostExportName: crossStackReferenceExportNames.databaseHost,
        databasePortExportName: crossStackReferenceExportNames.databasePort,
        databaseNameExportName: crossStackReferenceExportNames.databaseName,
        databaseSecurityGroupIdExportName: crossStackReferenceExportNames.databaseSecurityGroup,
    };
    new DatabaseStack(app, 'ApiDatabaseStack', databaseStackProps);

    const apiEc2StackProps: ApiEc2StackProps = {
        littil: littilEnvironmentSettings,
        apiVpc: vpcStack.vpc,
        env,
        database: {
            port: Fn.importValue(crossStackReferenceExportNames.databasePort),
            securityGroup: {
                id: Fn.importValue(crossStackReferenceExportNames.databaseSecurityGroup),
            },
        },
    };
    new ApiEc2Stack(app, 'ApiEc2Stack', apiEc2StackProps);

    const apiStackProps: ApiStackProps = {
        littil: littilEnvironmentSettings,
        apiVpc: vpcStack.vpc,
        env,
        ecrRepository: {
            awsAccount: sharedAccountId,
            name: ecrApiRepositoryName,
        },
        apiCertificateArn: Fn.importValue(crossStackReferenceExportNames.apiCertificateArn),
        database: {
            host: Fn.importValue(crossStackReferenceExportNames.databaseHost),
            port: Fn.importValue(crossStackReferenceExportNames.databasePort),
            name: Fn.importValue(crossStackReferenceExportNames.databaseName),
            securityGroup: {
                id: Fn.importValue(crossStackReferenceExportNames.databaseSecurityGroup),
            },
        },
    };
    new ApiStack(app, 'ApiStack', apiStackProps);

    const maintenanceProps: MaintenanceStackProps = {
        env,
        littil: littilEnvironmentSettings,
        apiVpc: vpcStack.vpc,
        maintenanceContainer: {
            enable: false,
            imageTag: '1.0.2',
            ecrRepository: {
                awsAccount: sharedAccountId,
                name: ecrMaintenanceRepositoryName,
            },
        },
        database: {
            host: Fn.importValue(crossStackReferenceExportNames.databaseHost),
            port: Fn.importValue(crossStackReferenceExportNames.databasePort),
            name: Fn.importValue(crossStackReferenceExportNames.databaseName),
            securityGroup: {
                id: Fn.importValue(crossStackReferenceExportNames.databaseSecurityGroup),
            },
        },
    };
    new MaintenanceStack(app, 'MaintenanceServiceStack', maintenanceProps);
}
