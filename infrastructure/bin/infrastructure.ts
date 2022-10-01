#!/usr/bin/env node
import * as cdk from 'aws-cdk-lib';
import 'source-map-support/register';
import { ApiStackProps, ApiStack } from '../lib/api-stack';
import { CertificateStack } from '../lib/certificate-stack';
import { EcrStack } from '../lib/ecr-stack';

const app = new cdk.App();

const certificateStackProps = {
    env: {
        region: 'eu-west-1',
    },
};
const certificateStack = new CertificateStack(app, 'ApiCertificatesStack', certificateStackProps);

const ecrProps = {
    env: {
        region: 'eu-west-1',
    },
};
const ecrStack = new EcrStack(app, 'ApiEcrStack', ecrProps)

const apiStackProps: ApiStackProps = {
    env: {
        region: 'eu-west-1',
    },
    ecrRepository: ecrStack.ecrRepository,
    certificate: certificateStack.certificate,
};
new ApiStack(app, 'ApiStack', apiStackProps);
