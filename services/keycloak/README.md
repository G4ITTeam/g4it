# Keycloak

A keycloak instance to manage user authentications for G4IT

## Local vs Deployed

The __local dev environment__ uses podman-compose to start the keycloak instance (in `workspace/numecoeval/docker-compose.yml`)
- Docker image used: bitnami/keycloak
- URL : http://localhost:8180/auth
- It uses this [extension](https://github.com/sventorben/keycloak-home-idp-discovery) located in `extensions` folder

The __deployed environment__ uses a built docker image with the `Dockerfile` in the current folder.
- It also uses the same extension

## Local dev users

The global admin user has these credentials :
- username: admin
- password: password

The password is __password__ for all users in local dev env

The _local dev environment__ automatically imports the `g4it` realm config name `dev-realm-export.json`. This realm has :
- 4 users:
  - admin@dev.com    : used for Subscriber Admin Role
  - adminorg@dev.com : used for Organization Admin Role
  - ro@dev.com       : used for Read Only Role
  - rw@dev.com       : used for Read + Write Role

Roles are not configured inside Keycloak, but in the G4IT backend.

## Export dev config

Open git bash in the `g4it` repository.

```shell
podman exec -it keycloak bash
/opt/bitnami/keycloak/bin/kc.sh export --file /tmp/dev-int-g4it-realm-export.json --realm g4it
exit
podman cp keycloak:/tmp/dev-int-g4it-realm-export.json services/keycloak/imports/dev-int-g4it-realm-export.json
```

## Extension configuration for home.idp.discovery.domains

On the extension used, it is possible to add a custom configuration for domains authorized on an identity provider.

```shell
# Go into the keycloak container

# Example for an identity provider named 'microsoft'

# Get the config
/opt/bitnami/keycloak/bin/kcadm.sh get identity-provider/instances/microsoft --server http://localhost:8080/auth -r g4it --user admin --realm master
# > Enter admin password

# Set the config
/opt/bitnami/keycloak/bin/kcadm.sh update identity-provider/instances/microsoft --server http://localhost:8080/auth -r g4it --user admin --realm master -s 'config."home.idp.discovery.domains"="example.com##test.com"'
# > Enter admin password
```

## Upgrade Keycloak version

For upgrading the Keycloak version: 
- Update the image tag in :
  - services/keycloak/Dockerfile
  - services/keycloak/.gitlab-ci.yml
  - workspace/numecoeval/docker-compose.yml
- Commit and push -> it will generate the new docker image

The new docker image can then be used in the deployment phase.
