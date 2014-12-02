package com.sirma.itt.pm.schedule.db;

/**
 * The following queries are defined under the PM-Schedule identified by {@link ScheduleDbSource}
 * qualifier.
 *
 * @author BBonev
 */
public interface DbQueryTemplatesSchedule {

	/** The Constant QUERY_SCHEDULE_ENTRY_ENTITY_BY_IDS_KEY. */
	String QUERY_SCHEDULE_ENTRY_ENTITY_BY_IDS_KEY = "QUERY_SCHEDULE_ENTRY_ENTITY_BY_IDS";
	/** The Constant QUERY_SCHEDULE_ENTRY_ENTITY_BY_IDS. */
	String QUERY_SCHEDULE_ENTRY_ENTITY_BY_IDS = "select p from ScheduleEntryEntity p where p.id in (:ids) order by p.id asc";

	/** The Constant QUERY_SCHEDULE_ENTRY_ENTITY_BY_ACT_IDS_KEY. */
	String QUERY_SCHEDULE_ENTRY_ENTITY_BY_ACT_IDS_KEY = "QUERY_SCHEDULE_ENTRY_ENTITY_BY_ACT_IDS";
	/** The Constant QUERY_SCHEDULE_ENTRY_ENTITY_BY_ACT_IDS. */
	String QUERY_SCHEDULE_ENTRY_ENTITY_BY_ACT_IDS = "select p from ScheduleEntryEntity p where p.contentManagementId in (:contentManagementIds) order by p.id asc";

	/** The Constant QUERY_SCHEDULE_ENTRY_ENTITY_BY_ACT_ID_KEY. */
	String QUERY_SCHEDULE_ENTRY_ENTITY_BY_ACT_ID_KEY = "QUERY_SCHEDULE_ENTRY_ENTITY_BY_ACT_ID";
	/** The Constant QUERY_SCHEDULE_ENTRY_ENTITY_BY_ACT_ID. */
	String QUERY_SCHEDULE_ENTRY_ENTITY_BY_ACT_ID = "select p from ScheduleEntryEntity p where p.contentManagementId=:contentManagementId";

	/** The Constant QUERY_SCHEDULE_ENTITY_BY_IDS_KEY. */
	String QUERY_SCHEDULE_ENTITY_BY_IDS_KEY = "QUERY_SCHEDULE_ENTITY_BY_IDS";
	/** The Constant QUERY_SCHEDULE_ENTITY_BY_IDS. */
	String QUERY_SCHEDULE_ENTITY_BY_IDS = "select p from ScheduleEntity p where p.id in (:ids) order by p.id asc";

	String QUERY_SCHEDULE_ENTITY_BY_REFERENCE_KEY = "QUERY_SCHEDULE_ENTITY_BY_REFERENCE";
	/** The Constant QUERY_SCHEDULE_ENTITY_BY_IDS. */
	String QUERY_SCHEDULE_ENTITY_BY_REFERENCE = "select p.id from ScheduleEntity p where p.owningReference.sourceId=:sourceId and p.owningReference.sourceType.id=:sourceTypeId";

	String QUERY_SCHEDULE_ENTRY_BY_REFERENCE_KEY = "QUERY_SCHEDULE_ENTRY_BY_REFERENCE";
	/** The Constant QUERY_SCHEDULE_ENTITY_BY_IDS. */
	String QUERY_SCHEDULE_ENTRY_BY_REFERENCE = "select p.id from ScheduleEntryEntity p where p.actualInstance.sourceId=:sourceId and p.actualInstance.sourceType.id=:sourceTypeId";

	String QUERY_SCHEDULE_ENTRY_ENTITY_BY_SCHEDULE_KEY = "QUERY_SCHEDULE_ENTRY_ENTITY_BY_SCHEDULE";

	// TODO: enable layer/partial loading of schedule tasks
	// String QUERY_SCHEDULE_ENTRY_ENTITY_BY_SCHEDULE =
	// "select p.id from ScheduleEntryEntity p where p.scheduleId=:scheduleId and (p.parentId is null OR p.parentId in (select id from ScheduleEntryEntity where scheduleId=:scheduleId and parentId is null)) order by p.id asc";
	String QUERY_SCHEDULE_ENTRY_ENTITY_BY_SCHEDULE = "select p.id from ScheduleEntryEntity p where p.scheduleId=:scheduleId order by p.id asc";

	String QUERY_SCHEDULE_ENTRY_BY_PARENT_KEY = "QUERY_SCHEDULE_ENTRY_BY_PARENT";
	String QUERY_SCHEDULE_ENTRY_BY_PARENT = "select p.id from ScheduleEntryEntity p where p.parentId=:parentId order by p.id asc";

	/*
	 * Resource queries
	 */
	/** The Constant QUERY_DEPENDENCY_KEY. */
	String QUERY_DEPENDENCY_ID_FOR_SCHEDULE_KEY = "QUERY_DEPENDENCY_ID_FOR_SCHEDULE";
	/** The Constant QUERY_DEPENDENCY. */
	String QUERY_DEPENDENCY_ID_FOR_SCHEDULE = "select d.id from ScheduleDependency d where d.scheduleId = :scheduleId";

	String QUERY_DEPENDENCY_BY_IDS_KEY = "QUERY_DEPENDENCY_BY_IDS";
	String QUERY_DEPENDENCY_BY_IDS = "select d from ScheduleDependency d where d.id in (:ids)";

	String QUERY_DEPENDENCY_BY_PAIR_KEY = "QUERY_DEPENDENCY_BY_PAIR";
	String QUERY_DEPENDENCY_BY_PAIR = "select d from ScheduleDependency d where d.from=:from and d.to=:to";

	/** The Constant QUERY_ASSIGNMENTS_KEY. */
	String QUERY_ASSIGNMENT_ID_FOR_SCHEDULE_KEY = "QUERY_ASSIGNMENT_ID_FOR_SCHEDULE";
	/** The Constant QUERY_ASSIGNMENTS. */
	String QUERY_ASSIGNMENT_ID_FOR_SCHEDULE = "select a.id from ScheduleAssignment a where a.scheduleId = :scheduleId";

	/** The Constant QUERY_ASSIGNMENT_ID_FOR_SCHEDULE_ENTRY_KEY. */
	String QUERY_ASSIGNMENT_ID_FOR_SCHEDULE_ENTRY_KEY = "QUERY_ASSIGNMENT_ID_FOR_SCHEDULE_ENTRY";
	/** The Constant QUERY_ASSIGNMENT_ID_FOR_SCHEDULE_ENTRY. */
	String QUERY_ASSIGNMENT_ID_FOR_SCHEDULE_ENTRY = "select a.id from ScheduleAssignment a where a.taskId = :taskId";

	String QUERY_ASSIGNMENTS_BY_IDS_KEY = "QUERY_ASSIGNMENTS_BY_IDS";
	String QUERY_ASSIGNMENTS_BY_IDS = "select a from ScheduleAssignment a where a.id in (:ids)";

	String QUERY_ASSIGNMENTS_BY_PAIR_KEY = "QUERY_ASSIGNMENTS_BY_PAIR";
	String QUERY_ASSIGNMENTS_BY_PAIR = "select a from ScheduleAssignment a where a.taskId=:taskId and a.resourceId=:resourceId";

	/** The Constant QUERY_RESOURCES_KEY. */
	String QUERY_RESOURCES_KEY = "QUERY_RESOURCES";
	/** The Constant QUERY_RESOURCES. */
	String QUERY_RESOURCES = "select r.id from ResourceEntity r where r.id in (select s.resourceId from ScheduleAssignment s where s.scheduleId = :scheduleId) or r.id in (select p.resourceId from ResourceRoleEntity p where p.targetRoleReference.sourceId=:sourceId AND p.targetRoleReference.sourceType.id=:sourceType) order by r.id";

	/** The Constant QUERY_DEPENDENCY_KEY. */
	String QUERY_DEPENDENCIES_FOR_ENTRY_KEY = "QUERY_DEPENDENCIES_FOR_ENTRY";
	/** The Constant QUERY_DEPENDENCY. */
	String QUERY_DEPENDENCIES_FOR_ENTRY = "select d.id from ScheduleDependency d where d.to in (:to) OR d.from in (:from)";

	public String QUERY_RESOURCES_ALLOCATIONS_KEY = "QUERY_RESOURCES_ALLOCATIONS";
	/** Allocated entries for schedule. */
	public String QUERY_RESOURCES_ALLOCATIONS = "SELECT DISTINCT sa.id FROM ScheduleEntryEntity se,ScheduleAssignment sa WHERE (se.id =  sa.taskId AND se.scheduleId in (:scheduleIds) AND sa.resourceId in (:resourceIds) AND se.startDate>:startDate AND se.endDate<:endDate AND se.actualInstance.sourceType.id in (:instanceTypes) )";

}
