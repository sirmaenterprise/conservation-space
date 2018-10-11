#!/usr/bin/env bash

# Checks the build log for failing e2e tests and creates jira blockers for each spec.
# The assignee is the last person to work on the spec.
#
# $1 jenkins build url
# $2 jira credentials

set -eu
set -o pipefail

jenkins_build_url="$1"
jira_credentials="$2"

script_dir="$( cd "$( dirname "${BASH_SOURCE[0]}" )" && pwd )"
repo_root="$(cd $script_dir/../../ && pwd)"
branch_name="$(git rev-parse --abbrev-ref HEAD)"
build_log="$script_dir/build-$(date --utc +%Y-%m-%d_%H-%M-%S).log"
jira_rest_api_url="http://jira.vpn.ittbg.com:8080/jira/rest/api/2"

if [ "$branch_name" != "master" ] && [[ ! "$branch_name" =~ ^release/ ]]; then
	# we don't care for development branches
	echo 'not on master/release'
	exit 0
fi

# if the greps here don't match anything the script with exit with a non-zero code, which is what we want
failed_keys="$(curl -s --fail "$jenkins_build_url/consoleText" | tee "$build_log" | grep -E 'chrome #[[:digit:]]+-[[:digit:]]+ failed' | grep -oE '#[[:digit:]]+-[[:digit:]]+')"

function exctract_failing_specs() {
	grep_pattern=""
	for key in $1; do
		if [ -n "$grep_pattern" ]; then
			grep_pattern="$grep_pattern|$key"
		else
			grep_pattern="$key"
		fi
	done

	grep_pattern="\[chrome ($grep_pattern)\] Specs: "
	grep -E "$grep_pattern" "$build_log" | sed "s|^.*Specs: $repo_root/||"
}

# spec assignee version slave
function create_jira() {
	local spec="$1"
	local assignee="$2"
	local version="$3"
	local slave="$4"

	local key="$(curl -s -u $jira_credentials "$jira_rest_api_url/search?jql=labels%20in%20(\"$spec\")%20and%20resolution%20%3D%20Unresolved&fields=key" | jq -r '.issues[0].key')"
	if [ -n "$key" -a "$key" != "null" ]; then
		echo "a bug report for $spec already exists: $key"
		return 0
	fi

	export SPEC="$spec"
	export ASSIGNEE="$assignee"
	export UI_VERSION="$version"
	export SLAVE="$slave"

	local payload="$(envsubst < $script_dir/jira-create-bug-req.json)"
	local created_key="$(curl -s -u $jira_credentials -H "Content-Type: application/json" -d "$payload" "$jira_rest_api_url/issue" | jq -r '.key')"
	echo "created issue $created_key for spec $spec"

	curl -s -u $jira_credentials -H "X-Atlassian-Token: nocheck" -F "file=@$build_log" "$jira_rest_api_url/issue/$created_key/attachments"
}

slave="$(grep 'Running on' $build_log | cut -d' ' -f3)"
ui_version="$(jq '.version' -r "$repo_root/package.json" | sed 's/.\d-SNAPSHOT$//')"

for spec in $(exctract_failing_specs "$failed_keys"); do
	! assignee="$(git log --pretty=format:%ae%n%ce "$spec" | grep 'sirma.bg' | head -1)"
	create_jira "$spec" "$assignee" "$ui_version" "$slave"
done
