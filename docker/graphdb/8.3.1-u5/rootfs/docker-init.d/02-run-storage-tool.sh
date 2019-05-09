#!/usr/bin/env bash

set -eu

if [ -n "${RUN_STORAGE_TOOL:-}" ]; then
    sed -i "s/call_java com.ontotext.trree.util.convert.storage.StorageTool/call_java -Xmx${GDB_HEAP_SIZE:-2g} com.ontotext.trree.util.convert.storage.StorageTool/" $SERVICE_DIR_GRAPHDB_HOME/bin/storage-tool
    for repair in $RUN_STORAGE_TOOL; do
        IFS='=' read -r -a pair <<< "$repair"
        echo "Command:${pair[0]} on GDB repository:${pair[1]} is runned"
        $SERVICE_DIR_GRAPHDB_HOME/bin/storage-tool -command="${pair[0]}" -storage="${VOLUME_REPOS}/repositories/${pair[1]}"/storage
        chown -R $DOCKER_USER:$DOCKER_USER "${VOLUME_REPOS}/repositories/${pair[1]}"
        sync
    done
    chown -R $DOCKER_USER:$DOCKER_USER "$SERVICE_DIR_GRAPHDB_HOME/work"
fi