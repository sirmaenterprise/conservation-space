#!/usr/bin/env bash

set -eu

function genport() {
  echo "$(shuf -i 1-5 -n 1)$(date +%M)$(date +%S)"
}

port=$(genport)

retries=0
while netstat -atn | grep -q :$port; do
  retries=$(expr $retries + 1)

  if [ $retries -eq 10 ]; then
    echo "unable to find a free port"
    exit 1;
  fi

  port=$(genport)
done

echo $port
