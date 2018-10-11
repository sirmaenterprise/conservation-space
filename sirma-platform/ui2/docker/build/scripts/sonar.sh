#!/bin/bash
set -eu

cd $1

echo "Run Sonar analysis for branch ${SONAR_BRANCH_NAME} on ${SONAR_HOST}"

gulp sonar --sonar_host=${SONAR_HOST} \
  --sonar_branch_name=${SONAR_BRANCH_NAME} \
  --sonar_project_version=$2