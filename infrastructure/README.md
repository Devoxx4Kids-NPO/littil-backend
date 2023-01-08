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
winpty aws --profile littil-staging-maintenance-ecs-exec ecs execute-command --cluster Some-Cluster-abc123 --task 123abc456def --container web --command "sh" --interactive
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
npm run cdk:synth --  --context environment=<env> --context account=<account>  --profile=<profile>
```

Where `<env>` is the environment, like staging or production, `<account>` is the numerical AWS account ID and <profile> is the
name of the credentials profile as configured in `~/.aws/credentials`. These credentials need to be for
a `LITTIL-<country>-<env>-littil-frontend-Cdk-User` user.

To deploy:

```bash
npm run cdk:staging:deploy  --context environment=<env> --context account=<account>  --profile=<profile>
```
