import * as cdk from 'aws-cdk-lib';
import { CfnOutput } from 'aws-cdk-lib';
import { Certificate, CertificateValidation } from 'aws-cdk-lib/aws-certificatemanager';
import { Construct } from 'constructs';

export class CertificateStack extends cdk.Stack {
    // TODO: Replace with CfnOutputs
    public readonly certificate: Certificate;

    constructor(scope: Construct, id: string, props: cdk.StackProps) {
        super(scope, id, props);

        const certificateProps = {
            domainName: 'api.staging.littil.org',
            validation: CertificateValidation.fromDns(),
        };
        this.certificate = new Certificate(this, 'ApiCertificate', certificateProps);

        new CfnOutput(this, 'CertificateArn', {value: this.certificate.certificateArn});
    }
}
