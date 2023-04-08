import { CfnOutput, Stack, StackProps } from 'aws-cdk-lib';
import { Certificate, CertificateValidation } from 'aws-cdk-lib/aws-certificatemanager';
import { Construct } from 'constructs';
import { LittilEnvironmentSettings } from './littil-environment-settings';

export interface CertificateStackProps extends StackProps {
    littil: LittilEnvironmentSettings;
    apiCertificateArnExportName: string;
}

export class CertificateStack extends Stack {
    constructor(scope: Construct, id: string, props: CertificateStackProps) {
        super(scope, id, props);

        const certificateProps = {
            domainName: props.littil.backendDomainName,
            validation: CertificateValidation.fromDns(),
        };
        const certificate = new Certificate(this, 'ApiCertificate', certificateProps);

        new CfnOutput(this, 'CertificateArn', {
            exportName: props.apiCertificateArnExportName,
            value: certificate.certificateArn
        });

        new CfnOutput(this, 'ApiCertificatesStack:ExportsOutputRefApiCertificate1D5B2B3BC42B0A73', {
            value: certificate.certificateArn,
        });
    }
}
