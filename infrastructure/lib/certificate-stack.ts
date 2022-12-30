import { CfnOutput, Stack, StackProps } from 'aws-cdk-lib';
import { Certificate, CertificateValidation } from 'aws-cdk-lib/aws-certificatemanager';
import { Construct } from 'constructs';

export interface CertificateStackProps extends StackProps {
    apiCertificateArnExportName: string;
}

export class CertificateStack extends Stack {
    constructor(scope: Construct, id: string, props: CertificateStackProps) {
        super(scope, id, props);

        const certificateProps = {
            domainName: 'api.staging.littil.org',
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
