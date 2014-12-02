package com.sirma.itt.emf.db;

import javax.persistence.Column;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.QueryHint;
import javax.persistence.Table;

import org.hibernate.annotations.QueryHints;

import com.sirma.itt.emf.domain.model.Entity;

/**
 * Holder class for all prepared statements.
 * 
 * @author BBonev
 */
@javax.persistence.Entity
@Table(name = "emf_dataTypeDefinition")
@NamedQueries(value = {
		@NamedQuery(name = EmfQueries.QUERY_THUMBNAILS_BY_IDS_KEY, query = EmfQueries.QUERY_THUMBNAILS_BY_IDS),
		@NamedQuery(name = EmfQueries.QUERY_THUMBNAIL_BY_INSTANCE_ID_AND_PURPOSE_KEY, query = EmfQueries.QUERY_THUMBNAIL_BY_INSTANCE_ID_AND_PURPOSE),
		@NamedQuery(name = EmfQueries.QUERY_THUMBNAIL_MAPPING_BY_ID_AND_PURPOSE_KEY, query = EmfQueries.QUERY_THUMBNAIL_MAPPING_BY_ID_AND_PURPOSE),
		@NamedQuery(name = EmfQueries.QUERY_THUMBNAILS_FOR_SYNC_KEY, query = EmfQueries.QUERY_THUMBNAILS_FOR_SYNC),
		@NamedQuery(name = EmfQueries.UPDATE_THUMBNAIL_DATA_KEY, query = EmfQueries.UPDATE_THUMBNAIL_DATA),
		@NamedQuery(name = EmfQueries.QUERY_THUMBNAIL_ENTITY_BY_ID_KEY, query = EmfQueries.QUERY_THUMBNAIL_ENTITY_BY_ID),
		@NamedQuery(name = EmfQueries.DELETE_THUMBNAIL_BY_SOURCE_ID_KEY, query = EmfQueries.DELETE_THUMBNAIL_BY_SOURCE_ID),
		@NamedQuery(name = EmfQueries.DELETE_THUMBNAIL_MAPPINGS_BY_SOURCE_ID_KEY, query = EmfQueries.DELETE_THUMBNAIL_MAPPINGS_BY_SOURCE_ID),
		@NamedQuery(name = EmfQueries.DELETE_THUMBNAIL_MAPPINGS_BY_INSTANCE_ID_AND_PURPOSE_KEY, query = EmfQueries.DELETE_THUMBNAIL_MAPPINGS_BY_INSTANCE_ID_AND_PURPOSE),
		@NamedQuery(name = EmfQueries.DELETE_THUMBNAIL_MAPPINGS_BY_INSTANCE_ID_KEY, query = EmfQueries.DELETE_THUMBNAIL_MAPPINGS_BY_INSTANCE_ID),
		@NamedQuery(name = EmfQueries.UPDATE_RESCHEDULE_THUMBNAIL_CHECK_FOR_INSTANCES_KEY, query = EmfQueries.UPDATE_RESCHEDULE_THUMBNAIL_CHECK_FOR_INSTANCES),
		@NamedQuery(name = EmfQueries.QEURY_THUMBNAIL_ID_FOR_INSTANCE_KEY, query = EmfQueries.QEURY_THUMBNAIL_ID_FOR_INSTANCE),

		@NamedQuery(name = EmfQueries.QUERY_SCHEDULER_ENTRY_ID_BY_EVENT_TRIGGER_KEY, query = EmfQueries.QUERY_SCHEDULER_ENTRY_ID_BY_EVENT_TRIGGER),
		@NamedQuery(name = EmfQueries.QUERY_SCHEDULER_ENTRY_ID_BY_EVENT_TRIGGER_OP_KEY, query = EmfQueries.QUERY_SCHEDULER_ENTRY_ID_BY_EVENT_TRIGGER_OP),
		@NamedQuery(name = EmfQueries.QUERY_SCHEDULER_ENTRY_BY_UID_KEY, query = EmfQueries.QUERY_SCHEDULER_ENTRY_BY_UID),
		@NamedQuery(name = EmfQueries.QUERY_NEXT_EXECUTION_TIME_KEY, query = EmfQueries.QUERY_NEXT_EXECUTION_TIME),
		@NamedQuery(name = EmfQueries.QUERY_SCHEDULER_TASKS_FOR_TIMED_EXECUTION_KEY, query = EmfQueries.QUERY_SCHEDULER_TASKS_FOR_TIMED_EXECUTION),
		@NamedQuery(name = EmfQueries.QUERY_ALL_SCHEDULER_ENTRIES_BY_IDS_KEY, query = EmfQueries.QUERY_ALL_SCHEDULER_ENTRIES_BY_IDS),
		@NamedQuery(name = EmfQueries.QUERY_ALL_RESOURCES_BY_TYPE_KEY, query = EmfQueries.QUERY_ALL_RESOURCES_BY_TYPE),
		@NamedQuery(name = EmfQueries.QUERY_ALL_RESOURCES_KEY, query = EmfQueries.QUERY_ALL_RESOURCES),
		@NamedQuery(name = EmfQueries.QUERY_RESOURCE_ROLES_BY_IDS_KEY, query = EmfQueries.QUERY_RESOURCE_ROLES_BY_IDS),
		@NamedQuery(name = EmfQueries.QUERY_RESOURCE_ROLES_BY_TARGET_ID_KEY, query = EmfQueries.QUERY_RESOURCE_ROLES_BY_TARGET_ID),
		@NamedQuery(name = EmfQueries.QUERY_RESOURCE_ROLE_ID_BY_TARGET_ID_KEY, query = EmfQueries.QUERY_RESOURCE_ROLE_ID_BY_TARGET_ID),
		@NamedQuery(name = EmfQueries.QUERY_RESOURCE_ROLE_BY_TARGET_ID_KEY, query = EmfQueries.QUERY_RESOURCE_ROLE_BY_TARGET_ID),
		@NamedQuery(name = EmfQueries.QUERY_PROJECT_RESOURCE_ROLE_BY_PROJECT_KEY, query = EmfQueries.QUERY_PROJECT_RESOURCE_ROLE_BY_PROJECT),
		@NamedQuery(name = EmfQueries.QUERY_PROJECT_RESOURCE_BY_NAME_KEY, query = EmfQueries.QUERY_PROJECT_RESOURCE_BY_NAME),
		@NamedQuery(name = EmfQueries.QUERY_PROJECT_RESOURCE_BY_IDS_KEY, query = EmfQueries.QUERY_PROJECT_RESOURCE_BY_IDS),
		@NamedQuery(name = EmfQueries.QUERY_COMMON_ENTITIES_BY_ID_KEY, query = EmfQueries.QUERY_COMMON_ENTITIES_BY_ID),
		@NamedQuery(name = EmfQueries.QUERY_SEQUENCES_KEY, query = EmfQueries.QUERY_SEQUENCES),
		@NamedQuery(name = EmfQueries.UPDATE_SEQUENCES_ENTRY_KEY, query = EmfQueries.UPDATE_SEQUENCES_ENTRY),
		@NamedQuery(name = EmfQueries.QUERY_ALL_DEFINITIONS_FILTERED_KEY, query = EmfQueries.QUERY_ALL_DEFINITIONS_FILTERED),
		@NamedQuery(name = EmfQueries.QUERY_DEFINITION_BY_ID_AND_CONTAINER_KEY, query = EmfQueries.QUERY_DEFINITION_BY_ID_AND_CONTAINER),
		@NamedQuery(name = EmfQueries.QUERY_MAX_DEFINITION_BY_ID_KEY, query = EmfQueries.QUERY_MAX_DEFINITION_BY_ID),
		@NamedQuery(name = EmfQueries.QUERY_DEFINITION_BY_ID_CONTAINER_REVISION_KEY, query = EmfQueries.QUERY_DEFINITION_BY_ID_CONTAINER_REVISION),
		@NamedQuery(name = EmfQueries.DELETE_DEFINITION_BY_ID_CONTAINER_REVISION_KEY, query = EmfQueries.DELETE_DEFINITION_BY_ID_CONTAINER_REVISION),
		@NamedQuery(name = EmfQueries.QUERY_DEFINITION_BY_ID_TYPE_KEY, query = EmfQueries.QUERY_DEFINITION_BY_ID_TYPE),
		@NamedQuery(name = EmfQueries.QUERY_MAX_REVISION_OF_DEFINITIONS_FOR_MIGRATION_KEY, query = EmfQueries.QUERY_MAX_REVISION_OF_DEFINITIONS_FOR_MIGRATION),
		@NamedQuery(name = EmfQueries.QUERY_NON_INSTANTIATED_DEFINITIONS_KEY, query = EmfQueries.QUERY_NON_INSTANTIATED_DEFINITIONS),
		@NamedQuery(name = EmfQueries.QUERY_TYPE_DEFINITION_KEY, query = EmfQueries.QUERY_TYPE_DEFINITION, hints = { @QueryHint(name = QueryHints.CACHEABLE, value = "true") }),
		@NamedQuery(name = EmfQueries.QUERY_TYPE_DEFINITION_BY_URI_KEY, query = EmfQueries.QUERY_TYPE_DEFINITION_BY_URI, hints = { @QueryHint(name = QueryHints.CACHEABLE, value = "true") }),
		@NamedQuery(name = EmfQueries.QUERY_TYPE_DEFINITION_BY_CLASS_KEY, query = EmfQueries.QUERY_TYPE_DEFINITION_BY_CLASS, hints = { @QueryHint(name = QueryHints.CACHEABLE, value = "true") }),
		@NamedQuery(name = EmfQueries.QUERY_TYPE_DEFINITION_BY_NAME_AND_CLASS_KEY, query = EmfQueries.QUERY_TYPE_DEFINITION_BY_NAME_AND_CLASS, hints = { @QueryHint(name = QueryHints.CACHEABLE, value = "true") }),
		@NamedQuery(name = EmfQueries.QUERY_PROTO_TYPE_DEFINITION_KEY, query = EmfQueries.QUERY_PROTO_TYPE_DEFINITION),
		@NamedQuery(name = EmfQueries.QUERY_PROPERTIES_BY_ENTITY_ID_KEY, query = EmfQueries.QUERY_PROPERTIES_BY_ENTITY_ID),
		@NamedQuery(name = EmfQueries.QUERY_PROPERTIES_KEY, query = EmfQueries.QUERY_PROPERTIES),
		@NamedQuery(name = EmfQueries.DELETE_PROPERTIES_KEY, query = EmfQueries.DELETE_PROPERTIES),
		@NamedQuery(name = EmfQueries.DELETE_ALL_PROPERTIES_FOR_BEAN_KEY, query = EmfQueries.DELETE_ALL_PROPERTIES_FOR_BEAN),
		@NamedQuery(name = EmfQueries.QUERY_FILTER_BY_ID_KEY, query = EmfQueries.QUERY_FILTER_BY_ID),
		@NamedQuery(name = EmfQueries.QUERY_FILTERS_BY_ID_KEY, query = EmfQueries.QUERY_FILTERS_BY_ID),
		@NamedQuery(name = EmfQueries.QUERY_LINK_BY_SRC_AND_IDS_KEY, query = EmfQueries.QUERY_LINK_BY_SRC_AND_IDS),
		@NamedQuery(name = EmfQueries.QUERY_LINK_BY_SRC_KEY, query = EmfQueries.QUERY_LINK_BY_SRC),
		@NamedQuery(name = EmfQueries.QUERY_LINK_BY_TARGET_AND_IDS_KEY, query = EmfQueries.QUERY_LINK_BY_TARGET_AND_IDS),
		@NamedQuery(name = EmfQueries.QUERY_LINK_BY_TARGET_KEY, query = EmfQueries.QUERY_LINK_BY_TARGET),
		@NamedQuery(name = EmfQueries.QUERY_LINK_BY_TARGET_AND_SOURCE_AND_IDS_KEY, query = EmfQueries.QUERY_LINK_BY_TARGET_AND_SOURCE_AND_IDS),
		@NamedQuery(name = EmfQueries.QUERY_LINK_BY_TARGET_AND_SOURCE_KEY, query = EmfQueries.QUERY_LINK_BY_TARGET_AND_SOURCE),
		@NamedQuery(name = EmfQueries.DELETE_ALL_LINKS_FOR_INSTANCE_KEY, query = EmfQueries.DELETE_ALL_LINKS_FOR_INSTANCE),
		@NamedQuery(name = EmfQueries.DELETE_LINKS_FOR_INSTANCE_KEY, query = EmfQueries.DELETE_LINKS_FOR_INSTANCE),
		@NamedQuery(name = EmfQueries.DELETE_LINK_FOR_INSTANCE_KEY, query = EmfQueries.DELETE_LINK_FOR_INSTANCE),
		@NamedQuery(name = EmfQueries.DELETE_LINK_FOR_INSTANCE_AND_TYPE_KEY, query = EmfQueries.DELETE_LINK_FOR_INSTANCE_AND_TYPE),
		@NamedQuery(name = EmfQueries.QUERY_COMMENTS_KEY, query = EmfQueries.QUERY_COMMENTS),
		@NamedQuery(name = EmfQueries.QUERY_TOPIC_BY_ID_KEY, query = EmfQueries.QUERY_TOPIC_BY_ID),
		@NamedQuery(name = EmfQueries.QUERY_TOPIC_BY_ABOUT_KEY, query = EmfQueries.QUERY_TOPIC_BY_ABOUT),
		@NamedQuery(name = EmfQueries.QUERY_LABELS_BY_ID_KEY, query = EmfQueries.QUERY_LABELS_BY_ID),
		@NamedQuery(name = EmfQueries.QUERY_LABEL_BY_ID_KEY, query = EmfQueries.QUERY_LABEL_BY_ID) })
public class EmfQueryDao implements Entity<Long> {

	/**
	 * Gets the id.
	 *
	 * @return the id
	 */
	@Override
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "id")
	public Long getId() {
		return 0L;
	}

	@Override
	public void setId(Long id) {
		// nothing to do here
	}

}
