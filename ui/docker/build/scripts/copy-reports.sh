#!/bin/bash
set -e

REPORTS_DIR=$2
echo "Copy reports from $1/reports to $REPORTS_DIR"
rm -rf ${REPORTS_DIR}/*

cp -rp $1/reports/* ${REPORTS_DIR}