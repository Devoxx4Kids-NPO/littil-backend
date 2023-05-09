export enum LittilEnvironment {
    staging = 'staging',
    production = 'production',
}

export const isLittilEnvironment = (input: unknown): input is LittilEnvironment =>
    Object.values(LittilEnvironment).some(value => input === value);
