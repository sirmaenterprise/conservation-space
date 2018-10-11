#!/bin/sh
set -e

# TODO: Add completion ?
# TODO: Add blocking wait ?

# Defaults
: ${tries=1}
: ${timeout=2}
: ${max_tries=20}


# Reading command line arguments
while [ $# -gt 1 ]; do
	case $1 in
		-h|--host)
			host="$2"
			shift
		;;
		-p|--port)
			port="$2"
			shift
		;;
		-t|--timeout)
			timeout="$2"
			shift
		;;
		-m|--max_tries)
			max_tries="$2"
			shift
		;;
		*)
			echo "Argument $1 is not supported";
		;;
	esac
	shift
done

if [ -z ${host} ]; then
	echo "Provide the service host!";
	exit 1;
fi

if [ -z ${port} ]; then
	echo "Provide the service port!";
	exit 1;
fi

service="${host}:${port}"

echo "Going to wait for ${service} to be reachable...";
until $(nc -z -w 1 ${host} ${port} > /dev/null); do
	echo "${service} is not visible yet... retrying every $timeout seconds... ($tries/$max_tries)";
	sleep $timeout

	if [ $tries -eq $max_tries ]; then
		echo "ERROR: After [${tries}] retries ${service} couldn't be reached!";
		exit 1
	fi
	tries=$((tries+1))
done
echo "${service} is successfully reached after [${tries}] tries";
