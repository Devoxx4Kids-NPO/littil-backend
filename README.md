# littil-backend

## Setup local environment

### Needed tools
Their are several ways to setup the tools needed for development, you can download/install the tools manually or choose to use tools like [SDKMAN](https://sdkman.io/) or [HomeBrew](https://brew.sh/)
Mandatory:
* Java
* Maven
* GraalVM

### Setup 
* Install Maven dependencies
```
./mvnw clean install
```

## Run locally
* Start the application locally
```
./mvnw quarkus:dev 
```
* It will startup a server at [http://localhost:8080](http://localhost:8080)

## Handy resources 
[Quarkus Maven Tooling](https://quarkus.io/guides/maven-tooling) \
[SDKMAN](https://sdkman.io/) \
[HomeBrew](https://brew.sh/) 
