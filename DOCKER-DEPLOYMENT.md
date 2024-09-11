# G4IT INSTALLATION WITH DOCKER-COMPOSE

## Prerequisites

- We use Podman Desktop with podman-compose command to build and run components (in commands, podman <-> docker)
- Clone repository locally.
- Open a command prompt or bash inside the g4it folder (repository)

## Build docker images locally

### BACKEND

Prerequisites:
- Maven command

```
cd services/backend
mvn clean package -P SKIP-ALL-TEST
podman build -f Dockerfile-opensource -t g4it-backend
```

It should build a docker image locally named localhost/g4it-backend:latest


### FRONTEND

Prerequisites:
- Node 20

```
cd services/frontend
npm install
npm run build:nohref
podman build -f Dockerfile-opensource -t g4it-frontend
```

It should build a docker image locally named localhost/g4it-frontend:latest

### Hosts .


In file `C:\Windows\System32\drivers\etc\hosts` or `/etc/hosts`, add line : 127.0.0.1 keycloak

### Run components

```
cd workspace/numecoeval
podman-compose up -d -f docker-compose-all.yml
```

### Verify

Go to http://localhost:4200, it will redirect to keycloak login.
A built-in user already exists: 
- username: admin@dev.com
- password: password


### Grant Subscriber admin role

The admin@dev.com user must automatically have subscriber admin role on SOPRA-STERIA-GROUP subscriber.
Then, refresh the page localhost:4200.

### Note 

- I had an issue with Keycloak "iss" validation as it forces to have the same URL for issuer.
- As Keycloak is inside docker:
  - the user is calling from outside docker to get Keycloak connection
  - the backend is calling from inside docker to validate issuer 
