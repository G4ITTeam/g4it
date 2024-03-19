ARG CI_REGISTRY

FROM $CI_REGISTRY/dep/library/project-oci-images/runtime-angular-nginx-v1:update1

COPY ./nginx/nginx.conf /etc/nginx/nginx.conf

# js resources to inject env variables
ENV JSFOLDER=/usr/share/nginx/html
# script to replace env variable before start
COPY ./nginx/25-inject-env-var.sh /docker-entrypoint.d/25-inject-env-var.sh
COPY dist/ /usr/share/nginx/html

USER root

RUN chmod a+rwx -R /usr/share/nginx/html
RUN chmod a+x /docker-entrypoint.d/25-inject-env-var.sh

CMD ["nginx", "-g", "daemon off;"]

USER 1001
EXPOSE 4200
HEALTHCHECK --interval=30s --timeout=5s --start-period=30s --retries=3 CMD curl -f -s localhost:4200 || exit 1
