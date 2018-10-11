ARG sep_base_image_version=

FROM docker-reg.sirmaplatform.com/sep-base-wildfly:$sep_base_image_version

COPY cs-war-deployment/target/cs-war-deployment.war /opt/wildfly/standalone/deployments/
