[![Security Rating](https://sonarcloud.io/api/project_badges/measure?project=littil-backend&metric=security_rating)](https://sonarcloud.io/summary/new_code?id=littil-backend)
![Build image workflow status](https://github.com/Devoxx4Kids-NPO/littil-backend/actions/workflows/publish-build-container.yml/badge.svg)
![Known Vulnerabilities](https://snyk.io/test/github/Devoxx4Kids-NPO/littil-backend/badge.svg)

# LITTIL initiative

The LITTIL project is an initiative to develop and maintain an Open-Source platform to connect schools with volunteer
guestTeachers for programming workshops. It was born to facilitate the central aim of the Devoxx4Kids foundation, which
is to
acquaint children with programming through fun workshops at their school.
The platform provides a web-based portal for schools and volunteer guestTeachers to register themselves and create a
public
profile. The platform then facilitates both parties to find possible matches.

## LITTIL backend API

This repository contains the API backend for the LITTIL platform based on Quarkus.

### Setup local environment

Please
read [the following guide](https://devoxx4kids-npo.github.io/littil-documentation/platform/local-development/set-up-backend-environment/)
about how to set up your local development environment.


### dev users

When running the backend as a developer you can add previously created users at startup.
Create a `dev-users.csv` file in the project root with the users you would like to add. when the backend is started.
Additional information can be found in the file `dev-users.example`.
