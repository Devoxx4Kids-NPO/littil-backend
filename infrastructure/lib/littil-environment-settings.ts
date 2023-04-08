export interface LittilEnvironmentSettings {
    environment: 'staging' | 'production';
    httpCorsOrigin: string;
    backendDomainName: string;
}
