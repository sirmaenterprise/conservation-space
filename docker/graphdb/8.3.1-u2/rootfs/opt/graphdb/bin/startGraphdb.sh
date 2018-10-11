#!/usr/bin/env bash

set -eu

BACKUP_FOLDER="$VOLUME_REPOS/backup"

if [ -d "$VOLUME_REPOS/repositories" ] && [ ! -f "$VOLUME_REPOS/migrated" ]; then
	if [ ! -d $BACKUP_FOLDER ]; then
		mkdir $BACKUP_FOLDER
	fi

	for repo_dir in `find $VOLUME_REPOS/repositories -mindepth 1 -maxdepth 1 -type d -not -name SYSTEM`
	do
		REPOSITORY_ID=$(basename $repo_dir)
		REPOSITORY_EXPORT_FILE=$BACKUP_FOLDER/"${REPOSITORY_ID}_export.trig"
		REPO_CONFIG_FILE="$BACKUP_FOLDER/${REPOSITORY_ID}.ttl"

		echo "Processing Repository ID $REPOSITORY_ID"
		REPOSITORY_STORAGE="$repo_dir"

		if [ -d "$REPOSITORY_STORAGE/storage" ]; then
			REPOSITORY_STORAGE="$REPOSITORY_STORAGE/storage"
		fi

		echo "Repository storage folder: $REPOSITORY_STORAGE"
		echo "Exporting file: $REPOSITORY_EXPORT_FILE"
    	$SERVICE_DIR_GRAPHDB_HOME/bin/storage-tool -command=export -storage=$REPOSITORY_STORAGE -destFile=$REPOSITORY_EXPORT_FILE -srcIndex=pos
    	# backup owlim.properties
    	echo "Backup owlim proeprties: $BACKUP_FOLDER/${REPOSITORY_ID}.owlim.properties"
    	cp $REPOSITORY_STORAGE/owlim.properties "$BACKUP_FOLDER/${REPOSITORY_ID}.owlim.properties"

    	# backup solr connector folder
    	if [ -d "$REPOSITORY_STORAGE/solr-connector" ]; then
			echo "Backup solr connector: $BACKUP_FOLDER/${REPOSITORY_ID}.solr-connector"
			cp -r $REPOSITORY_STORAGE/solr-connector "$BACKUP_FOLDER/${REPOSITORY_ID}.solr-connector"
    	fi
		sed -i 's/ >/>/g' "$REPOSITORY_EXPORT_FILE"

		# prepare repository config file
		cp $SERVICE_DIR_GRAPHDB_HOME/bin/worker.ttl $REPO_CONFIG_FILE
		sed -i "s/REPOSITORY_ID/$REPOSITORY_ID/g" $REPO_CONFIG_FILE
		echo "Using Repository configuration file: $REPO_CONFIG_FILE"
		echo "Importing exported file: $REPOSITORY_EXPORT_FILE"

		export LOAD_RDF_OPTS="-Dgraphdb.license.file=$SERVICE_DIR_GRAPHDB_HOME/graphdb.license -Dgraphdb.home.data=$VOLUME_REPOS"
		$SERVICE_DIR_GRAPHDB_HOME/bin/loadrdf -f -m serial -c $REPO_CONFIG_FILE $REPOSITORY_EXPORT_FILE

		# update namespaces
		echo "Restoring owlim proeprties from: $BACKUP_FOLDER/$REPOSITORY_ID.owlim.properties"
		REPOSITORY_OUTPUT_FOLDER="$VOLUME_REPOS/repositories/$REPOSITORY_ID/storage"

		grep -v "Namespace" $REPOSITORY_OUTPUT_FOLDER/owlim.properties > "$REPOSITORY_OUTPUT_FOLDER/owlim_bak.properties"
		grep "Namespace" $BACKUP_FOLDER/$REPOSITORY_ID.owlim.properties | grep -v "Namespace  :" >> "$REPOSITORY_OUTPUT_FOLDER/owlim_bak.properties"
		mv $REPOSITORY_OUTPUT_FOLDER/owlim_bak.properties "$REPOSITORY_OUTPUT_FOLDER/owlim.properties"

		if [ -d "$BACKUP_FOLDER/${REPOSITORY_ID}.solr-connector" ]; then
			echo "Restoring solr connector from: $BACKUP_FOLDER/$REPOSITORY_ID.solr-connector"
			cp -r $BACKUP_FOLDER/${REPOSITORY_ID}.solr-connector/* $REPOSITORY_OUTPUT_FOLDER/solr-connector
		fi
	done

	date +%F\ %T\ %Z > $VOLUME_REPOS/migrated

fi

/$SERVICE_DIR_GRAPHDB_HOME/bin/graphdb
