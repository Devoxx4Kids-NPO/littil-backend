#!/usr/bin/env node
import { App, Fn } from 'aws-cdk-lib';
import 'source-map-support/register';
import { ApiStack, ApiStackProps } from '../lib/api-stack';
import { CertificateStack } from '../lib/certificate-stack';
import { EcrStack, EcrStackProps } from '../lib/ecr-stack';
import { SupportEcrStack } from '../lib/support-ecr-stack';

const app = new App();

const crossStackReferenceExportNames = {
    apiEcrRepositoryArn: 'apiEcrRepositoryArn',
    apiEcrRepositoryName: 'apiEcrRepositoryName',
    apiCertificateArn: 'apiCertificateArn',
    mysqlRepositoryArn: 'mysqlRepositoryArn',
    mysqlRepositoryName: 'mysqlRepositoryName',
};

const certificateStackProps = {
    env: {
        region: 'eu-west-1',
    },
    apiCertificateArnExportName: crossStackReferenceExportNames.apiCertificateArn,
};
new CertificateStack(app, 'ApiCertificatesStack', certificateStackProps);

const apiEcrProps: EcrStackProps = {
    env: {
        region: 'eu-west-1',
    },
    apiRepositoryNameExportName: crossStackReferenceExportNames.apiEcrRepositoryName,
    apiRepositoryArnExportName: crossStackReferenceExportNames.apiEcrRepositoryArn,
};
new EcrStack(app, 'ApiEcrStack', apiEcrProps);

const supportEcrProps = {
    env: {
        region: 'eu-west-1',
    },
    mysqlRepositoryNameExportName: crossStackReferenceExportNames.mysqlRepositoryName,
    mysqlRepositoryArnExportName: crossStackReferenceExportNames.mysqlRepositoryArn,
};
new SupportEcrStack(app, 'SupportEcrStack', supportEcrProps);

const apiStackProps: ApiStackProps = {
    env: {
        region: 'eu-west-1',
    },
    ecrRepositoryName: Fn.importValue(crossStackReferenceExportNames.apiEcrRepositoryName),
    ecrRepositoryArn: Fn.importValue(crossStackReferenceExportNames.apiEcrRepositoryArn),
    apiCertificateArn: Fn.importValue(crossStackReferenceExportNames.apiCertificateArn),
    mysqlSupportContainer: {
        enable: false,
        ecrRepositoryName: Fn.importValue(crossStackReferenceExportNames.mysqlRepositoryName),
        ecrRepositoryArn: Fn.importValue(crossStackReferenceExportNames.mysqlRepositoryArn),
        imageTag: '8.0.31-oracle',
    },
};
new ApiStack(app, 'ApiStack', apiStackProps);
