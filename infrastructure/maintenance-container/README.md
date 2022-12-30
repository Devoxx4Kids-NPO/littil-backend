# LITTIL Back-end service Maintenance image

This image contains a MySQL client. The image is run in ECS, with access to the LITTIL database. A container run from
this image can therefore be used as a stepping stone for database access. See [the infrastructure README](../README.md)
for more info

## Creating a new image

```bash
TAG=1.0.0
docker build . -t 680278545709.dkr.ecr.eu-west-1.amazonaws.com/littil-backend-maintenance:$TAG
```

## Pushing a new image

Log in to ECR using Docker:

```bash
aws --profile littil-staging-mysql-ecr-pushpull --region eu-west-1 ecr get-login-password | docker login --username AWS --password-stdin 680278545709.dkr.ecr.eu-west-1.amazonaws.com/littil-backend-maintenance
```

where `littil-staging-mysql-ecr-pushpull` is a profile configed in `~/.aws/credentials`
and `680278545709.dkr.ecr.eu-west-1.amazonaws.com/littil-backend-maintenance` is the repository you want to push an
image too.

Push the newly built image

```bash
docker push 680278545709.dkr.ecr.eu-west-1.amazonaws.com/littil-backend-maintenance:$TAG
```

Note: using $TAG variable set when building the image so that you can use these lines without needing to change them.
