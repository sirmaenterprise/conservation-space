#!/bin/sh

# We use an array so spaces will be preserved and passed on correctly
JAVA_OPTS_ARRAY=()

# If GDB_HEAP_SIZE is provided it will override GDB_MIN_MEM and GDB_MAX_MEM
if [ "x$GDB_HEAP_SIZE" != "x" ]; then
    GDB_MIN_MEM=$GDB_HEAP_SIZE
    GDB_MAX_MEM=$GDB_HEAP_SIZE
fi

# Use GDB_MIN_MEM and GDB_MAX_MEM to set -Xms and -Xmx if they have values
if [ "x$GDB_MIN_MEM" != "x" ]; then
    JAVA_OPTS_ARRAY+=("-Xms${GDB_MIN_MEM}")
else
    # an absolute default for minimum heap size, this helps with 32-bit "client" java
    JAVA_OPTS_ARRAY+=("-Xms1g")
fi
if [ "x$GDB_MAX_MEM" != "x" ]; then
    JAVA_OPTS_ARRAY+=("-Xmx${GDB_MAX_MEM}")
fi

# Use GDB_HEAP_NEWSIZE for -Xmn if it has values
if [ "x$GDB_HEAP_NEWSIZE" != "x" ]; then
    JAVA_OPTS_ARRAY+=("-Xmn${GDB_HEAP_NEWSIZE}")
fi

# Set to headless, just in case
JAVA_OPTS_ARRAY+=("-Djava.awt.headless=true")

# Ensure UTF-8 encoding by default (e.g. filenames)
JAVA_OPTS_ARRAY+=("-Dfile.encoding=UTF-8")

# Prefer IPv4 stack, helps on broken IPv6 configs
JAVA_OPTS_ARRAY+=("-Djava.net.preferIPv4Stack=true")

# Default garbage collector
JAVA_OPTS_ARRAY+=("${GDB_GC_COLLECTOR}")

# Alternative garbage collector (comment the above and uncomment this)
#JAVA_OPTS_ARRAY+=("-XX:+UseConcMarkSweepGC")

# Don't omit stack traces when the JVM recompiles on the fly and swaps with precompiled exceptions
JAVA_OPTS_ARRAY+=("-XX:-OmitStackTraceInFastThrow")

# Causes the JVM to dump its heap on OutOfMemory.
JAVA_OPTS_ARRAY+=("-XX:+HeapDumpOnOutOfMemoryError")

# The path to the heap dump location, note directory must exists and have enough
# space for a full heap dump.

if [ "x$GDB_HEAP_DUMP_FILE" = "x" ]; then
        GDB_HEAP_DUMP_FILE="$GDB_DIST/logs"
    fi
JAVA_OPTS_ARRAY+=("-XX:HeapDumpPath=$GDB_HEAP_DUMP_FILE")

# Exit immediately on out of memory error (but a heap dump will still be done if configured)
JAVA_OPTS_ARRAY+=("-XX:OnOutOfMemoryError=kill -9 %p")

# Garbage collect logs, set GDB_GC_LOG to true to enable
if [ "$GDB_GC_LOG" = "true" ]; then
    if [ "x$GDB_GC_LOG_FILE" = "x" ]; then
        GDB_GC_LOG_FILE="$GDB_DIST/logs/gc-%p.log"
    fi

    # Print current heap distributions - before and after GC
    JAVA_OPTS_ARRAY+=("-XX:+PrintGCDetails")
    # Don't use timestamps but dates instead
    JAVA_OPTS_ARRAY+=("-XX:+PrintGCDateStamps")
    # Print Tunering distribution so we can spot resizing
    JAVA_OPTS_ARRAY+=("-XX:+PrintTenuringDistribution")
    # Logs rotation options
    JAVA_OPTS_ARRAY+=("-XX:+UseGCLogFileRotation")
    JAVA_OPTS_ARRAY+=("-XX:GCLogFileSize=2M")
    JAVA_OPTS_ARRAY+=("-XX:NumberOfGCLogFiles=5")
    JAVA_OPTS_ARRAY+=("-Xloggc:$GDB_GC_LOG_FILE")
fi
