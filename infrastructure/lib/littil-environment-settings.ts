import { LittilEnvironment } from './littil-environment';

export interface LittilEnvironmentSettings {
    environment: LittilEnvironment;
    httpCorsOrigin: string;
    backendDomainName: string;
}
