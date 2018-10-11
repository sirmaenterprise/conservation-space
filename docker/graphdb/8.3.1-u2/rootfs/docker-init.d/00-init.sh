#!/bin/sh

export GDB_JAVA_OPTS="$GDB_JAVA_OPTS -Dgraphdb.connector.port=8080 -Dgraphdb.home.data=$VOLUME_REPOS -Dgraphdb.engine.entity-pool-implementation=transactional"
# enable JMX Remote connection
export GDB_JAVA_OPTS="$GDB_JAVA_OPTS -Dcom.sun.management.jmxremote=true -Dcom.sun.management.jmxremote.rmi.port=8091 -Dcom.sun.management.jmxremote.port=8091 -Dcom.sun.management.jmxremote.authenticate=false -Dcom.sun.management.jmxremote.ssl=false -Djava.rmi.server.hostname=$HOST_NAME"
export GDB_JAVA_OPTS="$GDB_JAVA_OPTS -Dgraphdb.global.page.cache=true -Dgraphdb.page.cache.size=256m"
