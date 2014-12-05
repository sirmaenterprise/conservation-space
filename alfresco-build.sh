#!/bin/bash
work_dir=$(readlink -f $(dirname "$0"))
home_dir=$(getent passwd $SUDO_USER | cut -d: -f6)

cd ${work_dir}/alfresco/alfresco-parent
mvn install -N
cd ${work_dir}/alfresco/activiti-engine
mvn install
cd ${work_dir}/alfresco/java-opensaml2-main/
mvn install -DskipTests=true
cd ${work_dir}/alfresco/alfresco-emf-integration/
mvn install -N
cd ${work_dir}/alfresco/alfresco-emf-integration/alfresco-integration-api
mvn install
cd ${work_dir}/alfresco/alfresco-emf-integration/alfresco-integration-impl
mvn install
cd ${work_dir}/alfresco/alfresco-emf-integration/
mvn install