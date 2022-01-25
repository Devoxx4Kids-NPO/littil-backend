# Setting up your local develop environment
When you want to contribute to the LITTIL API you need some tools in order to run the API locally. 
The API is written in JAVA, based on [Quarkus](https://www.quarkus.io). For data persistence we use a [Mysql database](https://www.mysql.com). 

Don't worry about installing these extra dependencies, when running in `dev` mode those will be automatically installed 
in a docker container for you. 

## Prerequisites
Before you are able to run the API of LITTIL you need to make sure you have the following software installed on 
your machine.
- Java 11
- Maven
- Docker
- Quarkus cli

### SDKMAN!
`SDKMAN!` is an util which you can use on all unix based systems. This includes all Linux distributions, but also MacOS. 
`SDKMAN!` makes it really easy to not only install Java on your machine but most utilities which are commonly used within 
the Java eco-environment. You can use the following guide to install `SDKMAN!` on your machine:

You can install `SDKMAN!` by running the following command on your machine:

```shell
$ curl -s "https://get.sdkman.io" | bash
```

To use `SDKMAN!` you can either run the command below or restart your terminal.
```shell
$ source "$HOME/.sdkman/bin/sdkman-init.sh"
```

Run the following code snippet to ensure that installation succeeded:
```shell
$ sdk version
```

### Java
#### Unix
To see which versions are available for java run:
```shell
$ sdk list java 
```

Then install the latest java 11 version, i.e:
```shell
$ sdk install java 11.0.14-zulu    
```                            
For more information regarding `SDKMAN!` please visit their website at: [https://sdkman.io/](https://sdkman.io/)

_if you which to install java directly you can also download it directly from the
[Oracle website.](https://www.oracle.com/java/technologies/javase/jdk11-archive-downloads.html)_
                       
#### Windows
_Please contribute_
         
### Maven
This projects ships a Maven wrapper. With this wrapper it's not necessary to install maven locally on your machine. 
To run maven on unix use `$ ./mvnw <parameters>` on a Windows machine your can use ` $ ./mvnw.cmd <parameters>`.

### Quarkus CLI
Quarkus ships an CLI tool for better developing experience. This tool is not mandatory in order to work locally on the 
LITTIL API project. But in case you would like to use it you should follow the instructions below:

#### Unix
`SDKMAN!` ships the quarkus cli tool as well. The easiest way to install this tool is by using `SDKMAN!`. Run the following 
command in your terminal to see which versions are available

```shell
$ sdk list quarkus
```

Then install the desired version, in example:
```shell
$ sdk install quarkus 2.6.3.Final
```

To verify the installation is successful, run: 
```shell
$ quarkus --version
```
                        
#### Windows
The recommended way to install on Windows is via `JBang` using the powershell. You can simply run:
```powershell
iex "& { $(iwr https://ps.jbang.dev) } app install --fresh --force quarkus@quarkusio"
```

If you already have `JBang` installed on your windows machine you can run:
```powershell
jbang app install --fresh --force quarkus@quarkusio
```

_For more information, please read: [Quarkus cli-tooling](https://quarkus.io/guides/cli-tooling)_

### Docker
Quarkus uses Docker to spin up containers of middleware which the LITTIL API depends on. For example the mysql 
database for persisting data. In order to run these containers `Docker` must be installed on your machine. 

#### MacOS & Windows
The easiest way to install docker on your Windows or MacOS machine is by installing docker-desktop. You can find the
installation files on [docker download page](https://www.docker.com/get-started). 

#### Linux
Please see the following documentation on how to get Docker installed on your preferred Linux distribution: 
[Official Docker Documentation](https://docs.docker.com/engine/install/#server)

## Run the LITTIL API locally
Now, with all dependencies installed you can start the API for local development. You use either Maven via:

```shell
$ ./mvnw quarkus:dev
```

Or via the quarkus-cli tool
```shell
$ ./quarkus dev
```

_If you run into issues regarding the fact the docker daemon can not be found. Please see: [Docker daemon not running](Docker-daemon-not-running)_ 
                                                                                                                                       
To verify that the environment is running, open your web-browser en go to [the health-check](http://localhost:8080/q/health). 
If the response includes a `status:UP` you are good to go!

## Common used commands
The following commands are useful when contributing to the LITTIL API. You should run these within the project directory.
- `$ ./quakus dev` / `$ ./mvnw quarkus:dev` for starting your development environment.
- `$ ./quakus build [--native] --[no-]tests` / `$ ./mvnw quarkus:build [-D[no-]tests] [-Dnative]` to build an artifact.
the native and (no-)test parameters are optional.
- `$ ./quarkus test` / `$ ./mvnw quarkus:test` to run all tests
                 
For more commands you can run `$ ./quarkus --help` / `$ ./mvnw quarkus:help`

## Known issues

### Docker daemon not running
If you are developing on a Linux Ubuntu (distro) you might run into issues with Docker for example when running tests. 
The tests will fail, and you will see logging about the docker daemon. This is due to the fact docker-daemon requires root
permissions.

```
ERROR [org.tes.doc.DockerClientProviderStrategy] (build-4) Could not find a valid Docker environment. Please check configuration. Attempted configurations were:
ERROR [org.tes.doc.DockerClientProviderStrategy] (build-4) UnixSocketClientProviderStrategy: failed with exception TimeoutException (Timeout waiting for result with exception). Root cause LastErrorException ([13] Permission denied)
ERROR [org.tes.doc.DockerClientProviderStrategy] (build-4) As no valid configuration was found, execution cannot continue

---

WARN  [io.qua.dat.dep.dev.DevServicesDatasourceProcessor] (build-29) Please configure the datasource URL for the default datasource or ensure the Docker daemon is up and running.
Recoverable I/O exception (java.io.IOException) caught when processing request to {}->unix://localhost:2375
```

If you see this you are probably running Quarkus within your IDE or use the terminal utility of your IDE. 

To solve this issue you can either:
- configure Docker for rootless mode. For more information see: [Docker rootless](https://docs.docker.com/engine/security/rootless/)
(unfortunately, this does not always solve the issue);
- or run Quarkus from your OS terminal.