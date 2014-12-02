package com.sirma.itt.pm.schedule.db;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.Table;

/**
 * Holder class for all prepared statements.
 */
@Entity
@Table(name = "emf_dataTypeDefinition")
@NamedQueries(value = {
		@NamedQuery(name = DbQueryTemplatesSchedule.QUERY_DEPENDENCIES_FOR_ENTRY_KEY, query = DbQueryTemplatesSchedule.QUERY_DEPENDENCIES_FOR_ENTRY),
		@NamedQuery(name = DbQueryTemplatesSchedule.QUERY_SCHEDULE_ENTRY_BY_REFERENCE_KEY, query = DbQueryTemplatesSchedule.QUERY_SCHEDULE_ENTRY_BY_REFERENCE),
		@NamedQuery(name = DbQueryTemplatesSchedule.QUERY_ASSIGNMENT_ID_FOR_SCHEDULE_ENTRY_KEY, query = DbQueryTemplatesSchedule.QUERY_ASSIGNMENT_ID_FOR_SCHEDULE_ENTRY),
		@NamedQuery(name = DbQueryTemplatesSchedule.QUERY_SCHEDULE_ENTRY_BY_PARENT_KEY, query = DbQueryTemplatesSchedule.QUERY_SCHEDULE_ENTRY_BY_PARENT),
		@NamedQuery(name = DbQueryTemplatesSchedule.QUERY_RESOURCES_KEY, query = DbQueryTemplatesSchedule.QUERY_RESOURCES),
		@NamedQuery(name = DbQueryTemplatesSchedule.QUERY_ASSIGNMENTS_BY_IDS_KEY, query = DbQueryTemplatesSchedule.QUERY_ASSIGNMENTS_BY_IDS),
		@NamedQuery(name = DbQueryTemplatesSchedule.QUERY_DEPENDENCY_BY_IDS_KEY, query = DbQueryTemplatesSchedule.QUERY_DEPENDENCY_BY_IDS),
		@NamedQuery(name = DbQueryTemplatesSchedule.QUERY_DEPENDENCY_BY_PAIR_KEY, query = DbQueryTemplatesSchedule.QUERY_DEPENDENCY_BY_PAIR),
		@NamedQuery(name = DbQueryTemplatesSchedule.QUERY_ASSIGNMENTS_BY_PAIR_KEY, query = DbQueryTemplatesSchedule.QUERY_ASSIGNMENTS_BY_PAIR),
		@NamedQuery(name = DbQueryTemplatesSchedule.QUERY_DEPENDENCY_ID_FOR_SCHEDULE_KEY, query = DbQueryTemplatesSchedule.QUERY_DEPENDENCY_ID_FOR_SCHEDULE),
		@NamedQuery(name = DbQueryTemplatesSchedule.QUERY_ASSIGNMENT_ID_FOR_SCHEDULE_KEY, query = DbQueryTemplatesSchedule.QUERY_ASSIGNMENT_ID_FOR_SCHEDULE),
		@NamedQuery(name = DbQueryTemplatesSchedule.QUERY_SCHEDULE_ENTITY_BY_REFERENCE_KEY, query = DbQueryTemplatesSchedule.QUERY_SCHEDULE_ENTITY_BY_REFERENCE),
		@NamedQuery(name = DbQueryTemplatesSchedule.QUERY_SCHEDULE_ENTRY_ENTITY_BY_SCHEDULE_KEY, query = DbQueryTemplatesSchedule.QUERY_SCHEDULE_ENTRY_ENTITY_BY_SCHEDULE),
		@NamedQuery(name = DbQueryTemplatesSchedule.QUERY_SCHEDULE_ENTRY_ENTITY_BY_ACT_IDS_KEY, query = DbQueryTemplatesSchedule.QUERY_SCHEDULE_ENTRY_ENTITY_BY_ACT_IDS),
		@NamedQuery(name = DbQueryTemplatesSchedule.QUERY_SCHEDULE_ENTRY_ENTITY_BY_ACT_ID_KEY, query = DbQueryTemplatesSchedule.QUERY_SCHEDULE_ENTRY_ENTITY_BY_ACT_ID),
		@NamedQuery(name = DbQueryTemplatesSchedule.QUERY_SCHEDULE_ENTRY_ENTITY_BY_IDS_KEY, query = DbQueryTemplatesSchedule.QUERY_SCHEDULE_ENTRY_ENTITY_BY_IDS),
		@NamedQuery(name = DbQueryTemplatesSchedule.QUERY_SCHEDULE_ENTITY_BY_IDS_KEY, query = DbQueryTemplatesSchedule.QUERY_SCHEDULE_ENTITY_BY_IDS),
		@NamedQuery(name = DbQueryTemplatesSchedule.QUERY_RESOURCES_ALLOCATIONS_KEY, query = DbQueryTemplatesSchedule.QUERY_RESOURCES_ALLOCATIONS)
})
public class ScheduleQueryDao {

	/**
	 * Sets the id.
	 *
	 * @param id
	 *            the id to set
	 */
	public void setId(Long id) {
		// empty method. Nothing to set
	}

	/**
	 * Gets the id.
	 *
	 * @return the id
	 */
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "id")
	public Long getId() {
		return 0L;
	}
}