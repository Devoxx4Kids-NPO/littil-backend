# LITTIL back-end infrastructure

## ECR, Certificate & API stacks

The AWS infrastructure for the LITTIL back-end uses ECR (Elastic Container Registry) to store the backend service's
container images. The ECR repository is in the EcrStack.

These container images are then used by ECS (Elastic Container Service) which runs a container from the image. The ECS
service is located in the ApiStack

To facilitate ingress (inbound traffic) using HTTPS, a certificate is created in ACM (AWS Certificate Manager). This is
done in the CertificateStack

## ApiStack

To allow for data persistence, the ApiStack contains an RDS (Relational Database Service) setup. By allowing traffic
from one security group (container's) to another (RDS's) the running container (and the application within) can access
the database.

### Maintenance & debugging

Furthermore, the container supports ECS Exec, which allows logging into the container in the same fashion
as `docker exec` would with a local container. An IAM (Identity & Access Management) user is created which has rights to
exec into the container.

To exec into a container, use the following command:

```bash
aws --profile littil-staging-maintenance-ecs-exec ecs execute-command --cluster Some-Cluster-abc123 --task 123abc456def --container web --command "sh" --interactive
```

Where `littil-staging-maintenance-ecs-exec` is a profile configured in `~/.aws/credentials` (e.g.
through `aws configure`), `ApiStack-abc123` is the ECS cluster in which the container we're trying to exec into is
located, `123abc456def` is the task which the service is running, and "web" is the container name

### Maintenance ECS Container

Another ECS service is set up conditionally. To access the database for debugging or maintenance, developers and
operational engineers can exec into this container, which contains a MySQL client. This container has access to the
database too, so a connection can be made manually to query or manipulate the database as required. For more info,
see [the maintenance container README](maintenance-container/README.md)

## Creating a new back-end container and uploading it (manually)

To log in to an ECR registry using the Docker CLI, use the following command, where `littil-staging-mysql-ecr-pushpull`
is a profile with push & pull privileges as configured in `~/.aws/credentials`.

```bash
aws --profile littil-staging-mysql-ecr-pushpull --region eu-west-1 ecr get-login-password | docker login --username AWS --password-stdin 123456789.dkr.ecr.eu-west-1.amazonaws.com/littil-backend
```

The command retrieves a password from AWS and pipes it into the Docker CLI tool. Images can then be built and pushed as
follows:

```bash
TAG=1.0.0
docker build . -t 680278545709.dkr.ecr.eu-west-1.amazonaws.com/littil-backend:$TAG
docker push 680278545709.dkr.ecr.eu-west-1.amazonaws.com/littil-backend:$TAG
```

## SES:

https://docs.aws.amazon.com/ses/latest/dg/smtp-credentials.html

## Deployment

To manually synthesize Cloudformation templates from the CDK Typescript code:

```bash
npm run build
npm run cdk:synth -- --context environment=<env> --context account=<account>  --profile=<profile>
```

Where `<env>` is the environment, like staging or production, `<account>` is the numerical AWS account ID and <profile> is the
name of the credentials profile as configured in `~/.aws/credentials`. These credentials need to be for
a `LITTIL-<country>-<env>-littil-backend-Cdk-User` user.

To deploy:

```bash
npm run cdk:deploy -- --context environment=<env> --context account=<account>  --profile=<profile>
npm run cdk:deploy:maintenance -- --context environment=<env> --context account=<account>  --profile=<profile>
```

To remove the maintenance service stack:
```bash
npm run cdk:destroy:maintenance -- --context environment=<env> --context account=<account>  --profile=<profile>
```

# AWS CLI

The commands used in this README partially use the NodeJS AWS libs (e.g. `npm run cdk` / `npx cdk`), but some actions (such as the ECS Exec) might require use of the AWS CLI.
The CLI can be downloaded at https://aws.amazon.com/cli/
Specifically for the ECS Exec command, the session manager is required: https://docs.aws.amazon.com/systems-manager/latest/userguide/session-manager-working-with-install-plugin.html

To authenticate with AWS, the AWS CLI can use named profiles (as used in this README): https://docs.aws.amazon.com/cli/latest/userguide/cli-configure-profiles.html

# EC2 change

To reduce costs, the infrastructure has been changed from ECS to EC2. To log into the EC2 instance, look up the public IP in the EC2 console and:
```bash
ssh -i openssh.pem ec2-user@<public-ip>
```

The SSH CLI utility will ask for a password of the openssh.pem interactively.

## Lost private key

If the file or passwords are ever lost, generate a new one locally and use the public key material to create a new EC2 key that can be used in a new EC2 instance (see `new KeyPair` in `api-ec2-stack.ts`).

Alternatively, follow the guide at https://docs.aws.amazon.com/AWSEC2/latest/UserGuide/create-key-pairs.html#how-to-generate-your-own-key-and-import-it-to-aws to import a keypair and reference it in `api-ec2-stack.ts`.

## Updating the certificate

To renew the certificate, the same command needs to be run:
```bash
sudo su
letsencrypt certonly --standalone -d api.littil.org -m <email> --agree-tos --no-eff-email
```

To use port 80 for verification, stop the nginx webserver (way faster than shutting down and starting the back-end itself) for a moment:
```bash
systemctl stop nginx
```

Restart again using:
```bash
systemctl start nginx
```
