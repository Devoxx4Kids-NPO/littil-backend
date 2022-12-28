# LITTIL back-end

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

### MySQL ECS Container

Another ECS service is set up conditionally. To access the database for debugging or maintenance, developers and
operational engineers can exec into this container, which contains a MySQL client. The container too has access to the
database, so a connection can be made manually to query or manipulate the database as required.

```bash
TAG=8.0.31-oracle
docker pull mysql:$TAG
docker tag mysql:$TAG 680278545709.dkr.ecr.eu-west-1.amazonaws.com/mysql:8.0.31-oracle
docker push 680278545709.dkr.ecr.eu-west-1.amazonaws.com/mysql:8.0.31-oracle
```

## Container image registries

To log in to an ECR registry using the Docker CLI, use the following command, where `littil-staging-mysql-ecr-pushpull` is a profile with push & pull privileges as configured in `~/.aws/credentials`.
```bash
aws --profile littil-staging-mysql-ecr-pushpull --region eu-west-1 ecr get-login-password | docker login --username AWS --password-stdin 680278545709.dkr.ecr.eu-west-1.amazonaws.com/mysql
```
The command retrieves a password from AWS and pipes it into the Docker CLI tool.

## SES:

https://docs.aws.amazon.com/ses/latest/dg/smtp-credentials.html
