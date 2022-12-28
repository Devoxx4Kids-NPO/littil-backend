#!/usr/bin/env node
import * as cdk from 'aws-cdk-lib';
import { Fn } from 'aws-cdk-lib';
import 'source-map-support/register';
import { ApiStack, ApiStackProps } from '../lib/api-stack';
import { CertificateStack } from '../lib/certificate-stack';
import { EcrStack } from '../lib/ecr-stack';
import { SupportEcrStack } from '../lib/support-ecr-stack';

const app = new cdk.App();

const certificateStackProps = {
    env: {
        region: 'eu-west-1',
    },
};
const certificateStack = new CertificateStack(app, 'ApiCertificatesStack', certificateStackProps);

const apiEcrProps = {
    env: {
        region: 'eu-west-1',
    },
};
const apiEcrStack = new EcrStack(app, 'ApiEcrStack', apiEcrProps);

const supportEcrProps = {
    env: {
        region: 'eu-west-1',
    },
};
const supportEcrStack = new SupportEcrStack(app, 'SupportEcrStack', supportEcrProps);

const apiStackProps: ApiStackProps = {
    env: {
        region: 'eu-west-1',
    },
    apiEcrRepository: apiEcrStack.ecrRepository,
    apiCertificate: certificateStack.certificate,
    mysqlSupportContainer: {
        enable: false,
        ecrRepositoryArn: Fn.importValue('mysqlRepositoryArnOutput'),
        ecrRepositoryName: Fn.importValue('mysqlRepositoryNameOutput'),
        imageTag: '8.0.31-oracle',
    }
};
new ApiStack(app, 'ApiStack', apiStackProps);
