package com.sirma.itt.emf.db;

/**
 * Predefined JPA queries used in the DbDao(s).
 *
 * @author BBonev
 */
public interface EmfQueries {

	/*
	 * LABEL PROVIDER QUERIES
	 */

	/** The Constant QUERY_LABEL_BY_ID_KEY. */
	String QUERY_LABEL_BY_ID_KEY = "QUERY_LABEL_BY_ID";

	/** The Constant QUERY_LABEL_BY_ID. */
	String QUERY_LABEL_BY_ID = "select l from LabelImpl l inner join fetch l.value v where l.identifier=:labelId";

	/** The Constant QUERY_LABELS_BY_ID_KEY. */
	String QUERY_LABELS_BY_ID_KEY = "QUERY_LABELS_BY_ID";

	/** The Constant QUERY_LABELS_BY_ID. */
	String QUERY_LABELS_BY_ID = "select l from LabelImpl l inner join fetch l.value v where l.identifier in (:labelId)";

	/*
	 * Filter queries
	 */

	/** The Constant QUERY_LABEL_BY_ID_KEY. */
	String QUERY_FILTER_BY_ID_KEY = "QUERY_FILTER_BY_ID";

	/** The Constant QUERY_LABEL_BY_ID. */
	String QUERY_FILTER_BY_ID = "select l from FilterDefinitionImpl l where l.identifier=:filterId";

	/** The Constant QUERY_LABELS_BY_ID_KEY. */
	String QUERY_FILTERS_BY_ID_KEY = "QUERY_FILTERS_BY_ID";

	/** The Constant QUERY_LABELS_BY_ID. */
	String QUERY_FILTERS_BY_ID = "select l from FilterDefinitionImpl l where l.identifier in (:filterId)";

	/*
	 * Link queries
	 */

	/** The Constant QUERY_LINK_BY_SRC_AND_IDS_KEY. */
	String QUERY_LINK_BY_SRC_AND_IDS_KEY = "QUERY_LINK_BY_SRC_AND_IDS";

	/** The Constant QUERY_LINK_BY_SRC_AND_IDS. */
	String QUERY_LINK_BY_SRC_AND_IDS = "select l from LinkEntity l inner join fetch l.to.sourceType inner join fetch l.from.sourceType where l.identifier in (:identifier) AND l.from.sourceId = :fromId AND l.from.sourceType.id=:fromType order by l.id";

	/** The Constant QUERY_LINK_BY_TARGET_AND_IDS_KEY. */
	String QUERY_LINK_BY_TARGET_AND_IDS_KEY = "QUERY_LINK_BY_TARGET_AND_IDS";

	/** The Constant QUERY_LINK_BY_TARGET_AND_IDS. */
	String QUERY_LINK_BY_TARGET_AND_IDS = "select l from LinkEntity l inner join fetch l.to.sourceType inner join fetch l.from.sourceType where l.identifier in (:identifier) AND l.to.sourceId = :toId AND l.to.sourceType.id=:toType order by l.id";

	/** The Constant QUERY_LINK_BY_TARGET_AND_SOURCE_AND_IDS_KEY. */
	String QUERY_LINK_BY_TARGET_AND_SOURCE_AND_IDS_KEY = "QUERY_LINK_BY_TARGET_AND_SOURCE_AND_IDS";

	/** The Constant QUERY_LINK_BY_TARGET_AND_SOURCE_AND_IDS. */
	String QUERY_LINK_BY_TARGET_AND_SOURCE_AND_IDS = "select l from LinkEntity l inner join fetch l.to.sourceType inner join fetch l.from.sourceType where l.identifier in (:identifier) AND l.to.sourceId = :toId AND l.to.sourceType.id=:toType and l.from.sourceId = :fromId AND l.from.sourceType.id=:fromType order by l.id";

	/** The Constant QUERY_LINK_BY_SRC_AND_IDS_KEY. */
	String QUERY_LINK_BY_SRC_KEY = "QUERY_LINK_BY_SRC";

	/** The Constant QUERY_LINK_BY_SRC_AND_IDS. */
	String QUERY_LINK_BY_SRC = "select l from LinkEntity l inner join fetch l.to.sourceType inner join fetch l.from.sourceType where l.from.sourceId = :fromId AND l.from.sourceType.id=:fromType order by l.id";

	/** The Constant QUERY_LINK_BY_TARGET_AND_IDS_KEY. */
	String QUERY_LINK_BY_TARGET_KEY = "QUERY_LINK_BY_TARGET";

	/** The Constant QUERY_LINK_BY_TARGET_AND_IDS. */
	String QUERY_LINK_BY_TARGET = "select l from LinkEntity l inner join fetch l.to.sourceType inner join fetch l.from.sourceType where l.to.sourceId = :toId AND l.to.sourceType.id=:toType order by l.id";

	/** The Constant QUERY_LINK_BY_TARGET_AND_SOURCE_AND_IDS_KEY. */
	String QUERY_LINK_BY_TARGET_AND_SOURCE_KEY = "QUERY_LINK_BY_TARGET_AND_SOURCE";

	/** The Constant QUERY_LINK_BY_TARGET_AND_SOURCE_AND_IDS. */
	String QUERY_LINK_BY_TARGET_AND_SOURCE = "select l from LinkEntity l inner join fetch l.to.sourceType inner join fetch l.from.sourceType where l.to.sourceId = :toId AND l.to.sourceType.id=:toType and l.from.sourceId = :fromId AND l.from.sourceType.id=:fromType order by l.id";

	/** The Constant DELETE_ALL_LINKS_FOR_INSTANCE_KEY. */
	String DELETE_ALL_LINKS_FOR_INSTANCE_KEY = "DELETE_ALL_LINKS_FOR_INSTANCE";

	/** The Constant DELETE_ALL_LINKS_FOR_INSTANCE. */
	String DELETE_ALL_LINKS_FOR_INSTANCE = "delete from LinkEntity l where (l.to.sourceType.id=:toType AND l.to.sourceId=:toId) or (l.from.sourceType.id=:fromType AND l.from.sourceId=:fromId)";

	/** The Constant DELETE_LINKS_FOR_INSTANCE_AND_TYPE_KEY. */
	String DELETE_LINK_FOR_INSTANCE_AND_TYPE_KEY = "DELETE_LINK_FOR_INSTANCE_AND_TYPE";

	/** The Constant DELETE_LINKS_FOR_INSTANCE_AND_TYPE. */
	String DELETE_LINK_FOR_INSTANCE_AND_TYPE = "delete from LinkEntity l where ((l.to.sourceType.id=:toType AND l.to.sourceId=:toId) or (l.from.sourceType.id=:fromType AND l.from.sourceId=:fromId)) AND l.identifier=:linkId";

	/** The Constant DELETE_ALL_LINKS_FOR_INSTANCE_KEY. */
	String DELETE_LINKS_FOR_INSTANCE_KEY = "DELETE_LINKS_FOR_INSTANCE";

	/** The Constant DELETE_ALL_LINKS_FOR_INSTANCE. */
	String DELETE_LINKS_FOR_INSTANCE = "delete from LinkEntity l where (l.to.sourceType.id=:toType AND l.to.sourceId=:toId) AND (l.from.sourceType.id=:fromType AND l.from.sourceId=:fromId)";

	/** The Constant DELETE_ALL_LINKS_FOR_INSTANCE_KEY. */
	String DELETE_LINK_FOR_INSTANCE_KEY = "DELETE_LINK_FOR_INSTANCE";

	/** The Constant DELETE_ALL_LINKS_FOR_INSTANCE. */
	String DELETE_LINK_FOR_INSTANCE = "delete from LinkEntity l where (l.to.sourceType.id=:toType AND l.to.sourceId=:toId) AND (l.from.sourceType.id=:fromType AND l.from.sourceId=:fromId) AND l.identifier=:linkId";

	/*
	 * Forum queries
	 */

	/** The Constant QUERY_TOPIC_BY_ID_KEY. */
	String QUERY_TOPIC_BY_ID_KEY = "QUERY_TOPIC_BY_ID";

	/** The Constant QUERY_TOPIC_BY_ID. */
	String QUERY_TOPIC_BY_ID = "select t from TopicEntity t where t.identifier=:identifier order by t.id desc";

	/** The Constant QUERY_TOPIC_BY_ABOUT_KEY. */
	String QUERY_TOPIC_BY_ABOUT_KEY = "QUERY_TOPIC_BY_ABOUT";

	/** The Constant QUERY_TOPIC_BY_ABOUT. */
	String QUERY_TOPIC_BY_ABOUT = "select t from TopicEntity t where t.identifier=:identifier AND t.topicAbout.sourceId=:aboutId AND t.topicAbout.sourceType.id=:aboutType order by t.id desc";

	/** The Constant QUERY_COMMENTS_KEY. */
	String QUERY_COMMENTS_KEY = "QUERY_COMMENTS";

	/** The Constant QUERY_COMMENTS. */
	String QUERY_COMMENTS = "select c from CommentEntity c where c.topicId=:topicId AND c.postedDate >= :start AND c.postedDate < :end order by c.postedDate asc";

	/*
	 * PROPERTIES QUERIES
	 */

	/** The Constant QUERY_PROPERTIES_BY_ENTITY_ID_KEY. */
	String QUERY_PROPERTIES_BY_ENTITY_ID_KEY = "QUERY_PROPERTIES_BY_ENTITY_ID";

	/** The Constant QUERY_PROPERTIES_BY_ENTITY_ID. */
	String QUERY_PROPERTIES_BY_ENTITY_ID = "Select p from PropertyEntity p inner join fetch p.value v where p.entityId.beanId=:beanId and p.entityId.beanType=:beanType order By p.id asc";

	/** The Constant DELETE_PROPERTIES_KEY. */
	String DELETE_PROPERTIES_KEY = "DELETE_PROPERTIES";

	/** The Constant DELETE_PROPERTIES. */
	String DELETE_PROPERTIES = "delete from PropertyEntity p where p.key.propertyId in (:id) and p.entityId.beanId=:beanId and p.entityId.beanType=:beanType";

	/** The Constant DELETE_PROPERTIES_KEY. */
	String DELETE_ALL_PROPERTIES_FOR_BEAN_KEY = "DELETE_ALL_PROPERTIES_FOR_BEAN";

	/** The Constant DELETE_PROPERTIES. */
	String DELETE_ALL_PROPERTIES_FOR_BEAN = "delete from PropertyEntity p where p.entityId.beanId=:beanId and p.entityId.beanType=:beanType";

	/** The Constant QUERY_PROPERTIES_KEY. */
	String QUERY_PROPERTIES_KEY = "QUERY_PROPERTIES";

	/** The Constant QUERY_PROPERTIES. */
	String QUERY_PROPERTIES = "Select p from PropertyEntity p inner join fetch p.value v left outer join fetch v.serializableValue as sv where p.entityId.beanId in (:beanId) and p.entityId.beanType in (:beanType) order By p.id asc";

	/** The Constant QUERY_PROTO_TYPE_DEFINITION_KEY. */
	String QUERY_PROTO_TYPE_DEFINITION_KEY = "QUERY_PROTO_TYPE_DEFINITION";

	/** The Constant QUERY_PROTO_TYPE_DEFINITION. */
	String QUERY_PROTO_TYPE_DEFINITION = "select p from PrototypeDefinitionImpl p where p.identifier=:name and p.container=:container and p.multiValued=:multiValued and p.dataType.id=:type";

	/*
	 * TYPE DEFINITION QUERIES
	 */

	/** The Constant QUERY_TYPE_DEFINITION_KEY. */
	String QUERY_TYPE_DEFINITION_KEY = "QUERY_TYPE_DEFINITION";

	/** The Constant QUERY_TYPE_DEFINITION. */
	String QUERY_TYPE_DEFINITION = "select d from DataType d where d.name=:name";

	/** The Constant QUERY_TYPE_DEFINITION_KEY. */
	String QUERY_TYPE_DEFINITION_BY_URI_KEY = "QUERY_TYPE_DEFINITION_BY_URI";

	/** The Constant QUERY_TYPE_DEFINITION. */
	String QUERY_TYPE_DEFINITION_BY_URI = "select d from DataType d where d.uri like :uri";
	/** The Constant QUERY_TYPE_DEFINITION_KEY. */
	String QUERY_TYPE_DEFINITION_BY_CLASS_KEY = "QUERY_TYPE_DEFINITION_BY_CLASS";

	/** The Constant QUERY_TYPE_DEFINITION. */
	String QUERY_TYPE_DEFINITION_BY_CLASS = "select d from DataType d where d.javaClassName=:javaClassName";

	/** The Constant QUERY_TYPE_DEFINITION_BY_NAME_AND_CLASS_KEY. */
	String QUERY_TYPE_DEFINITION_BY_NAME_AND_CLASS_KEY = "QUERY_TYPE_DEFINITION_BY_NAME_AND_CLASS";

	/** The Constant QUERY_TYPE_DEFINITION_BY_NAME_AND_CLASS. */
	String QUERY_TYPE_DEFINITION_BY_NAME_AND_CLASS = "select d from DataType d where d.name=:name AND d.javaClassName=:javaClassName";

	/*
	 * DICTIONARY/DEFINITION QUERIES
	 */
	/**
	 * The Constant QUERY_ALL_DEFINITIONS_FILTERED_KEY. Fetches max revision definitions by
	 * Abstract, container and type
	 */
	String QUERY_ALL_DEFINITIONS_FILTERED_KEY = "QUERY_ALL_DEFINITIONS_FILTERED";

	/** The Constant QUERY_ALL_DEFINITIONS_FILTERED. */
	String QUERY_ALL_DEFINITIONS_FILTERED = "select c FROM DefinitionEntry c inner join fetch c.targetDefinition inner join fetch c.targetType where c.Abstract=:Abstract AND c.container=:container AND c.targetType.id=:type AND c.revision=(select max(c2.revision) from DefinitionEntry c2 where c2.identifier=c.identifier and c2.container=:container AND c2.targetType.id=:type)";

	/** The Constant QUERY_NON_INSTANTIATED_DEFINITIONS_KEY. */
	String QUERY_NON_INSTANTIATED_DEFINITIONS_KEY = "QUERY_NON_INSTANTIATED_DEFINITIONS";

	/** The Constant QUERY_NON_INSTANTIATED_DEFINITIONS. */
	String QUERY_NON_INSTANTIATED_DEFINITIONS = "select d from DefinitionEntry d where d.identifier not in (:definitions)";

	/**
	 * The Constant QUERY_DEFINITION_BY_ID_AND_CONTAINER_KEY. Fetches a definition entry by id,
	 * container and type
	 */
	String QUERY_DEFINITION_BY_ID_AND_CONTAINER_KEY = "QUERY_DEFINITION_BY_ID_AND_CONTAINER";

	/** The Constant QUERY_DEFINITION_BY_ID_AND_CONTAINER. */
	String QUERY_DEFINITION_BY_ID_AND_CONTAINER = "select d from DefinitionEntry d inner join fetch d.targetDefinition inner join fetch d.targetType where d.identifier = :identifier and d.container=:container AND d.targetType.id=:type order by d.id desc";

	/**
	 * The Constant QUERY_MAX_DEFINITION_BY_ID_KEY. Fetches max definition by id, container and type
	 */
	String QUERY_MAX_DEFINITION_BY_ID_KEY = "QUERY_MAX_DEFINITION_BY_ID";

	/** The Constant QUERY_MAX_DEFINITION_BY_ID. */
	String QUERY_MAX_DEFINITION_BY_ID = "select c FROM DefinitionEntry c inner join fetch c.targetDefinition inner join fetch c.targetType where c.identifier = :identifier and c.container=:container AND c.targetType.id=:type AND c.revision=(select max(revision) from DefinitionEntry where identifier=:identifier and container=:container AND targetType.id=:type)";

	/**
	 * The Constant QUERY_DEFINITION_BY_ID_AND_CONTAINER_KEY. Fetches a definition entry by id,
	 * container and type
	 */
	String QUERY_DEFINITION_BY_ID_CONTAINER_REVISION_KEY = "QUERY_DEFINITION_BY_ID_CONTAINER_REVISION";

	/** The Constant QUERY_DEFINITION_BY_ID_AND_CONTAINER. */
	String QUERY_DEFINITION_BY_ID_CONTAINER_REVISION = "select d from DefinitionEntry d inner join fetch d.targetDefinition inner join fetch d.targetType where d.identifier = :identifier and d.container=:container AND d.targetType.id=:type and d.revision=:revision order by d.id desc";
	/**
	 * The Constant QUERY_DEFINITION_BY_ID_AND_CONTAINER_KEY. Fetches a definition entry by id,
	 * container and type
	 */
	String DELETE_DEFINITION_BY_ID_CONTAINER_REVISION_KEY = "DELETE_DEFINITION_BY_ID_CONTAINER_REVISION";

	/** The Constant QUERY_DEFINITION_BY_ID_AND_CONTAINER. */
	String DELETE_DEFINITION_BY_ID_CONTAINER_REVISION = "delete from DefinitionEntry d where d.identifier = :identifier and d.container=:container AND d.targetType.id=:type and d.revision=:revision";
	/**
	 * The Constant QUERY_DEFINITION_BY_ID_AND_CONTAINER_KEY. Fetches a definition entry by id,
	 * container and type
	 */
	String QUERY_DEFINITION_BY_ID_TYPE_KEY = "QUERY_DEFINITION_BY_ID_TYPE";

	/** The Constant QUERY_DEFINITION_BY_ID_AND_CONTAINER. */
	String QUERY_DEFINITION_BY_ID_TYPE = "select d from DefinitionEntry d inner join fetch d.targetDefinition inner join fetch d.targetType where d.container=:container AND d.targetType.id=:type order by d.id desc";

	String QUERY_MAX_REVISION_OF_DEFINITIONS_FOR_MIGRATION_KEY = "QUERY_MAX_REVISION_OF_DEFINITIONS_FOR_MIGRATION";
	String QUERY_MAX_REVISION_OF_DEFINITIONS_FOR_MIGRATION = "select distinct d.identifier, d.revision from DefinitionEntry d where d.revision = (select max(t.revision) from DefinitionEntry t where t.identifier=d.identifier and t.container = d.container and t.targetType.id=d.targetType.id) AND d.identifier in (:definitions) and d.container = :container and d.targetType.id=:type";

	/*
	 * SEQUENCE QUERIES
	 */
	/** The Constant QUERY_SEQUENCES_KEY. */
	String QUERY_SEQUENCES_KEY = "QUERY_SEQUENCES";

	/** The Constant QUERY_SEQUENCES. */
	String QUERY_SEQUENCES = "from SequenceEntity";

	/** The Constant UPDATE_SEQUENCES_ENTRY_KEY. */
	String UPDATE_SEQUENCES_ENTRY_KEY = "UPDATE_SEQUENCES_ENTRY";

	/** The Constant UPDATE_SEQUENCES_ENTRY. */
	String UPDATE_SEQUENCES_ENTRY = "update SequenceEntity set sequence=:sequence where sequenceId=:sequenceId";

	/*
	 * COMMON ENTITY QUERIES
	 */
	/** The Constant QUERY_COMMON_ENTITIES_BY_ID_KEY. */
	String QUERY_COMMON_ENTITIES_BY_ID_KEY = "QUERY_COMMON_ENTITIES_BY_ID";

	/** The Constant QUERY_WORKFLOW_ENTITIES_BY_INSTANCE_ID. */
	String QUERY_COMMON_ENTITIES_BY_ID = "select c from CommonEntity c where c.id in (:id)";

	/*
	 * RESOURCE ENTITY QUERIES
	 */
	/** The Constant QUERY_PROJECT_RESOURCE_BY_IDS_KEY. */
	String QUERY_ALL_RESOURCES_KEY = "QUERY_ALL_RESOURCES_KEY";
	/** The Constant QUERY_PROJECT_RESOURCE_BY_IDS. */
	String QUERY_ALL_RESOURCES = "from ResourceEntity";

	/** The Constant QUERY_PROJECT_RESOURCE_BY_NAME_KEY. */
	String QUERY_PROJECT_RESOURCE_BY_NAME_KEY = "QUERY_PROJECT_RESOURCE_BY_NAME";
	/** The Constant QUERY_PROJECT_RESOURCE_BY_NAME. */
	String QUERY_PROJECT_RESOURCE_BY_NAME = "select p from ResourceEntity p where p.identifier in (:identifier) and p.type=:type";

	/** The Constant QUERY_PROJECT_RESOURCE_BY_IDS_KEY. */
	String QUERY_PROJECT_RESOURCE_BY_IDS_KEY = "QUERY_PROJECT_RESOURCE_BY_IDS";
	/** The Constant QUERY_PROJECT_RESOURCE_BY_IDS. */
	String QUERY_PROJECT_RESOURCE_BY_IDS = "select p from ResourceEntity p where p.id in (:ids)";

	/** The Constant QUERY_PROJECT_RESOURCE_BY_IDS_KEY. */
	String QUERY_RESOURCE_ROLES_BY_IDS_KEY = "QUERY_RESOURCE_ROLES_BY_IDS";
	/** The Constant QUERY_PROJECT_RESOURCE_BY_IDS. */
	String QUERY_RESOURCE_ROLES_BY_IDS = "select p from ResourceRoleEntity p where p.id in (:ids)";

	/** The Constant QUERY_PROJECT_RESOURCE_ROLE_BY_PROJECT_KEY. */
	String QUERY_PROJECT_RESOURCE_ROLE_BY_PROJECT_KEY = "QUERY_PROJECT_RESOURCE_ROLE_BY_PROJECT";
	/** The Constant QUERY_PROJECT_RESOURCE_ROLE_BY_PROJECT. */
	String QUERY_PROJECT_RESOURCE_ROLE_BY_PROJECT = "select p from ResourceRoleEntity p inner join fetch p.targetRoleReference.sourceType where p.targetRoleReference.sourceId=:sourceId AND p.targetRoleReference.sourceType.id=:sourceType AND p.resourceId=:resourceId";

	/** The Constant QUERY_RESOURCE_ROLE_BY_TARGET_ID_KEY. */
	String QUERY_RESOURCE_ROLE_BY_TARGET_ID_KEY = "QUERY_RESOURCE_ROLE_BY_TARGET_ID";
	/** The Constant QUERY_RESOURCE_ROLE_BY_TARGET_ID. */
	String QUERY_RESOURCE_ROLE_BY_TARGET_ID = "select resourceId from ResourceRoleEntity where targetRoleReference.sourceId=:sourceId AND targetRoleReference.sourceType.id=:sourceType";

	/** The Constant QUERY_RESOURCE_ROLE_BY_TARGET_ID_KEY. */
	String QUERY_RESOURCE_ROLE_ID_BY_TARGET_ID_KEY = "QUERY_RESOURCE_ROLE_ID_BY_TARGET_ID";
	/** The Constant QUERY_RESOURCE_ROLE_BY_TARGET_ID. */
	String QUERY_RESOURCE_ROLE_ID_BY_TARGET_ID = "select resourceId from ResourceRoleEntity where targetRoleReference.sourceId=:sourceId AND targetRoleReference.sourceType.id=:sourceType AND role=:role";

	/** The Constant QUERY_RESOURCE_ROLES_BY_TARGET_ID_KEY. */
	String QUERY_RESOURCE_ROLES_BY_TARGET_ID_KEY = "QUERY_RESOURCE_ROLES_BY_TARGET_ID";
	/** The Constant QUERY_RESOURCE_ROLES_BY_TARGET_ID. */
	String QUERY_RESOURCE_ROLES_BY_TARGET_ID = "select id from ResourceRoleEntity where targetRoleReference.sourceId=:sourceId AND targetRoleReference.sourceType.id=:sourceType";

	/** The Constant QUERY_ALL_RESOURCES_BY_TYPE_KEY. */
	String QUERY_ALL_RESOURCES_BY_TYPE_KEY = "QUERY_ALL_RESOURCES_BY_TYPE";
	/** The Constant QUERY_ALL_RESOURCES_BY_TYPE. */
	String QUERY_ALL_RESOURCES_BY_TYPE = "select id from ResourceEntity where type = :type";

	/*
	 * SCHEDULER ENTITY QUERIES
	 */
	/** The Constant QUERY_ALL_SCHEDULER_ENTRIES_BY_IDS_KEY. */
	String QUERY_ALL_SCHEDULER_ENTRIES_BY_IDS_KEY = "QUERY_ALL_SCHEDULER_ENTRIES_BY_IDS";
	/** The Constant QUERY_ALL_SCHEDULER_ENTRIES_BY_IDS. */
	String QUERY_ALL_SCHEDULER_ENTRIES_BY_IDS = "select e from SchedulerEntity e where e.id in (:ids)";

	/** The Constant QUERY_SCHEDULER_ENTRY_BY_UID_KEY. */
	String QUERY_SCHEDULER_ENTRY_BY_UID_KEY = "QUERY_SCHEDULER_ENTRY_BY_UID";
	/** The Constant QUERY_SCHEDULER_ENTRY_BY_UID. */
	String QUERY_SCHEDULER_ENTRY_BY_UID = "select e from SchedulerEntity e where e.identifier=:identifier";

	/** The Constant QUERY_SCHEDULER_ENTRY_ID_BY_EVENT_TRIGGER_KEY. */
	String QUERY_SCHEDULER_ENTRY_ID_BY_EVENT_TRIGGER_KEY = "QUERY_SCHEDULER_ENTRY_ID_BY_EVENT_TRIGGER";
	/** The Constant QUERY_SCHEDULER_ENTRY_ID_BY_EVENT_TRIGGER. */
	String QUERY_SCHEDULER_ENTRY_ID_BY_EVENT_TRIGGER = "select e.id from SchedulerEntity e where e.eventTrigger.eventClassId=:eventClassId AND e.eventTrigger.targetClassId=:targetClassId AND e.eventTrigger.targetId=:targetId";

	/** The Constant QUERY_SCHEDULER_ENTRY_ID_BY_EVENT_TRIGGER_OP_KEY. */
	String QUERY_SCHEDULER_ENTRY_ID_BY_EVENT_TRIGGER_OP_KEY = "QUERY_SCHEDULER_ENTRY_ID_BY_EVENT_TRIGGER_OP";
	/** The Constant QUERY_SCHEDULER_ENTRY_ID_BY_EVENT_TRIGGER_OP. */
	String QUERY_SCHEDULER_ENTRY_ID_BY_EVENT_TRIGGER_OP = "select e.id from SchedulerEntity e where e.eventTrigger.eventClassId=:eventClassId AND e.eventTrigger.targetClassId=:targetClassId AND e.eventTrigger.targetId=:targetId AND e.eventTrigger.operation=:operation";

	/** The Constant QUERY_SCHEDULER_TASKS_FOR_TIMED_EXECUTION_KEY. */
	String QUERY_SCHEDULER_TASKS_FOR_TIMED_EXECUTION_KEY = "QUERY_SCHEDULER_TASKS_FOR_TIMED_EXECUTION";
	/** The Constant QUERY_SCHEDULER_TASKS_FOR_TIMED_EXECUTION. */
	String QUERY_SCHEDULER_TASKS_FOR_TIMED_EXECUTION = "select e.id from SchedulerEntity e where e.nextScheduleTime is not null AND e.nextScheduleTime <= :next AND e.status in (:status) AND e.type in (:type) order by e.nextScheduleTime asc";

	/** The Constant QUERY_SCHEDULER_TASKS_FOR_TIMED_EXECUTION_KEY. */
	String QUERY_NEXT_EXECUTION_TIME_KEY = "QUERY_NEXT_EXECUTION_TIME";
	/** The Constant QUERY_SCHEDULER_TASKS_FOR_TIMED_EXECUTION. */
	String QUERY_NEXT_EXECUTION_TIME = "select e.nextScheduleTime from SchedulerEntity e where e.nextScheduleTime is not null AND e.nextScheduleTime > :next AND e.status in (:status) AND e.type in (:type) order by e.nextScheduleTime asc";

	/*
	 * Thumbnail queries
	 */

	String QUERY_THUMBNAILS_BY_IDS_KEY = "QUERY_THUMBNAILS_BY_IDS";
	String QUERY_THUMBNAILS_BY_IDS = "select tm.instanceId, t.thumbnail from ThumbnailMappingEntity tm, ThumbnailEntity t where tm.instanceId in (:ids) AND tm.purpose = :purpose AND tm.thumbnailId = t.id and t.thumbnail is not null and t.retries is null";

	String QUERY_THUMBNAIL_MAPPING_BY_ID_AND_PURPOSE_KEY = "QUERY_THUMBNAIL_MAPPING_BY_ID_AND_PURPOSE";
	String QUERY_THUMBNAIL_MAPPING_BY_ID_AND_PURPOSE = "select tm from ThumbnailMappingEntity tm left join fetch tm.instanceType where tm.instanceId = :id AND tm.purpose = :purpose";

	String QUERY_THUMBNAIL_BY_INSTANCE_ID_AND_PURPOSE_KEY = "QUERY_THUMBNAIL_BY_INSTANCE_ID_AND_PURPOSE";
	String QUERY_THUMBNAIL_BY_INSTANCE_ID_AND_PURPOSE = "select t.thumbnail from ThumbnailMappingEntity tm, ThumbnailEntity t where tm.instanceId = :id AND tm.purpose = :purpose AND tm.thumbnailId = t.id";

	String QUERY_THUMBNAILS_FOR_SYNC_KEY = "QUERY_THUMBNAILS_FOR_SYNC";
	String QUERY_THUMBNAILS_FOR_SYNC = "select t.id, t.endPoint, t.providerName, t.retries from ThumbnailEntity t where t.thumbnail is null and (t.retries is null or t.retries < :retries)";

	String UPDATE_THUMBNAIL_DATA_KEY = "UPDATE_THUMBNAIL_DATA";
	String UPDATE_THUMBNAIL_DATA = "update ThumbnailEntity set thumbnail=:thumbnail, retries = :retries where id = :id";

	String QEURY_THUMBNAIL_ID_FOR_INSTANCE_KEY = "QEURY_THUMBNAIL_ID_FOR_INSTANCE";
	String QEURY_THUMBNAIL_ID_FOR_INSTANCE = "select tm.thumbnailId from ThumbnailMappingEntity tm where tm.instanceId in (:instanceId) AND purpose = :purpose";

	String UPDATE_RESCHEDULE_THUMBNAIL_CHECK_FOR_INSTANCES_KEY = "UPDATE_RESCHEDULE_THUMBNAIL_CHECK_FOR_INSTANCES";
	String UPDATE_RESCHEDULE_THUMBNAIL_CHECK_FOR_INSTANCES = "update ThumbnailEntity set thumbnail= null, retries = null where id in (select tm.thumbnailId from ThumbnailMappingEntity tm where tm.instanceId in (:instanceId) AND purpose = :purpose) AND (thumbnail is null or thumbnail = :thumbnail)";

	String QUERY_THUMBNAIL_ENTITY_BY_ID_KEY = "QUERY_THUMBNAIL_ENTITY_BY_ID";
	String QUERY_THUMBNAIL_ENTITY_BY_ID = "select t from ThumbnailEntity t where t.id=:id";

	String DELETE_THUMBNAIL_BY_SOURCE_ID_KEY = "DELETE_THUMBNAIL_BY_SOURCE_ID";
	String DELETE_THUMBNAIL_BY_SOURCE_ID = "delete from ThumbnailEntity where id=:id";

	String DELETE_THUMBNAIL_MAPPINGS_BY_SOURCE_ID_KEY = "DELETE_THUMBNAIL_MAPPINGS_BY_SOURCE_ID";
	String DELETE_THUMBNAIL_MAPPINGS_BY_SOURCE_ID = "delete from ThumbnailMappingEntity where thumbnailId=:id";

	String DELETE_THUMBNAIL_MAPPINGS_BY_INSTANCE_ID_AND_PURPOSE_KEY = "DELETE_THUMBNAIL_MAPPINGS_BY_INSTANCE_ID_AND_PURPOSE";
	String DELETE_THUMBNAIL_MAPPINGS_BY_INSTANCE_ID_AND_PURPOSE = "delete from ThumbnailMappingEntity where instanceId=:id AND purpose=:purpose";

	String DELETE_THUMBNAIL_MAPPINGS_BY_INSTANCE_ID_KEY = "DELETE_THUMBNAIL_MAPPINGS_BY_INSTANCE_ID";
	String DELETE_THUMBNAIL_MAPPINGS_BY_INSTANCE_ID = "delete from ThumbnailMappingEntity where instanceId=:id";

}
