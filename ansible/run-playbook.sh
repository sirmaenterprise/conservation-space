#!/usr/bin/env bash

set -eu

script_dir="$( cd "$( dirname "${BASH_SOURCE[0]}" )" >/dev/null && pwd )"
inventory_base="${INVENTORY_BASE_PATH:-$script_dir/inventories}"
inventory="$2"

function cleanup() {
	rm -f "$inventory_base/filter_plugins"
}

trap cleanup EXIT

# needed for client migration playbook so that it can use the 'semver_match' filter
ln -s "$script_dir/filter_plugins" "$inventory_base/filter_plugins"

# joins the elements of $@ using a comma e.g. "a,b,c"
function join_elements() {
	local IFS=","
	echo "$*"
}

function run_migration {
	local migration_tag="$1"
	run_playbook "$script_dir/migration.yml" "$migration_tag"

	local client_migration_play="${inventory_base}/migration.yml"
	if [ -f "${client_migration_play}" ]; then
		run_playbook "${client_migration_play}" "$migration_tag"
	fi
}

function run_playbook() {
	local book="$1"
	shift

	local tags_flag=""
	if [ "$#" -gt 0 ]; then
		tags_flag="--tags $(join_elements $@)"
	fi

	local hosts_file="$inventory_base/$inventory/hosts"
	local extra_vars_flag=""
	if [ -f "$inventory_base/meta.yml" ]; then
		extra_vars_flag="--extra-vars @$inventory_base/meta.yml"
	elif [ -f "$inventory_base/meta.json" ]; then
		extra_vars_flag="--extra-vars @$inventory_base/meta.json"
	fi

	local vault_password_flag=""
	local vault_pass_src="${VAULT_PASS:-script}"
	case "${vault_pass_src}" in
	script)
		vault_password_flag="--vault-password-file $script_dir/ansible-valult-pass.sh"
		;;
	ask)
		vault_password_flag="--ask-vault-pass"
		;;
	none)
		vault_password_flag=""
		;;
	esac

	local extra_args=""
	if [ -n "${USE_PARAMIKO:-}" ]; then
		extra_args="-c paramiko"
	fi

	ansible-playbook \
		--ssh-extra-args='-o StrictHostKeyChecking=no' \
		-i $hosts_file \
		${extra_args:-} \
		$extra_vars_flag \
		$tags_flag \
		$vault_password_flag \
		$book
}

case "$1" in
provision)
	run_migration before_provision

	run_playbook "$script_dir/setup.yml"
	run_playbook "$script_dir/swarm.yml"

	run_migration after_provision
	;;
deploy)
	run_migration before_deploy
	run_playbook "$script_dir/deploy.yml"
	run_migration after_deploy
	;;
conntest)
	run_playbook "$script_dir/connection-test.yml"
	;;
*)
	echo "unknown play name: $1"
	exit 1
	;;
esac
