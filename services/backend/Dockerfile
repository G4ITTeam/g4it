ARG CI_REGISTRY

FROM $CI_REGISTRY/dep/library/project-oci-images/runtime-java-jre21:update1
WORKDIR /app
COPY target/*.jar application.jar

USER 1001
EXPOSE 8080
ENTRYPOINT ["java", "-Xmx1g", "-Xms1g", "-jar", "application.jar"]
HEALTHCHECK --interval=30s --timeout=5s --start-period=30s --retries=3 CMD curl -f -s localhost:8080/actuator/health | grep UP || exit 1
