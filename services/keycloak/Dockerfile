# A example build step that downloads a JAR file from a URL and adds it to the providers directory
FROM bitnami/keycloak:24.0.5

# Add the provider JAR file to the providers directory
COPY --chown=keycloak:keycloak extensions/ /opt/bitnami/keycloak/providers/
COPY --chown=keycloak:keycloak themes/ /opt/bitnami/keycloak/themes/

USER keycloak
