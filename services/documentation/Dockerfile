ARG CI_REGISTRY

FROM $CI_REGISTRY/dep/library/project-oci-images/runtime-angular-nginx-v1:update1

COPY nginx/default.conf /etc/nginx/conf.d

COPY public/ /usr/share/nginx/html

CMD ["nginx", "-g", "daemon off;"]

USER 1001
EXPOSE 8080
HEALTHCHECK --interval=30s --timeout=5s --start-period=30s --retries=3 CMD curl -f -s localhost:8080 || exit 1
