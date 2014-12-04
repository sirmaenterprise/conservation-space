#!/bin/bash
work_dir=$(readlink -f $(dirname "$0"))
home_dir=$(getent passwd $SUDO_USER | cut -d: -f6)


cd ${work_dir}/alfresco/alfresco-emf-integration/alfresco-integration-impl
mvn install
