package com.sirma.itt.pm.schedule.service;

import java.io.Serializable;
import java.util.List;
import java.util.Map;

import com.sirma.itt.emf.domain.model.DefinitionModel;
import com.sirma.itt.emf.instance.model.Instance;
import com.sirma.itt.emf.time.DateRange;
import com.sirma.itt.pm.schedule.model.ScheduleEntry;
import com.sirma.itt.pm.schedule.model.ScheduleInstance;

/**
 * Service providing methods for modification of schedule tasks/entries.
 *
 * @author BBonev
 */
public interface ScheduleService {

	/**
	 * Gets the or create new schedule instance for the given source instance. If method is called
	 * on a project instance for a first time and project has some tasks/cases created as part of it
	 * this method should build the proper schedule instances so a call to the
	 *
	 * @param instance
	 *            the instance for which we want to create or fetch a {@link ScheduleInstance}.
	 * @return existing {@link ScheduleInstance} or creates new one
	 *         {@link #getEntries(ScheduleInstance)} should return the proper result.
	 */
	ScheduleInstance getOrCreateSchedule(Instance instance);

	/**
	 * Gets the schedule for the given source instance.
	 *
	 * @param instance
	 *            the instance
	 * @return the schedule
	 */
	ScheduleInstance getSchedule(Instance instance);

	/**
	 * Gets the entries.
	 *
	 * @param instance
	 *            the instance
	 * @return the entries
	 */
	List<ScheduleEntry> getEntries(ScheduleInstance instance);

	/**
	 * Gets the entry for actual instance. Creates new entry instance automatically if missing, that
	 * is not saved.
	 *
	 * @param instance
	 *            the instance
	 * @return the entry for actual instance
	 */
	ScheduleEntry getEntryForActualInstance(Instance instance);

	/**
	 * Gets the entry for actual instance. If the second argument is <code>true</code> then if the
	 * entry exists new instance will be created and returned. The returned instance will not be
	 * persisted upon return.
	 *
	 * @param instance
	 *            the instance
	 * @param autoCreate
	 *            the auto create instance if non existent
	 * @return the entry for actual instance
	 */
	ScheduleEntry getEntryForActualInstance(Instance instance, boolean autoCreate);

	/**
	 * Method that adds an entry to specific schedule. The method returns any other created entries
	 * that are not present into the model and are created by other user.
	 *
	 * @param entry
	 *            the entry to add to the schedule
	 * @return the list of added tasks
	 */
	List<ScheduleEntry> addEntry(ScheduleEntry entry);

	/**
	 * Update schedule entry and returns the updated/affected entries.
	 *
	 * @param entry
	 *            the entry
	 * @return the list of entries updated/added
	 */
	List<ScheduleEntry> updateEntry(ScheduleEntry entry);

	/**
	 * Delete entry from the schedule.
	 *
	 * @param entry
	 *            the entry to delete.
	 */
	void deleteEntry(ScheduleEntry entry);

	/**
	 * Cancel schedule entry's underlying instance.
	 * 
	 * @param entry
	 *            the entry to delete.
	 * @return updated entry
	 */
	ScheduleEntry cancelEntry(ScheduleEntry entry);

	/**
	 * Gets the immediate children of the given node.
	 * 
	 * @param nodeId
	 *            the node id
	 * @return the children
	 */
	List<ScheduleEntry> getChildren(Serializable nodeId);

	/**
	 * Gets the allowed children for node.
	 *
	 * @param targetNode
	 *            the target node
	 * @param currentChildren
	 *            the current children
	 * @return the allowed children for node
	 */
	Map<String, List<DefinitionModel>> getAllowedChildrenForNode(ScheduleEntry targetNode,
			List<ScheduleEntry> currentChildren);

	/**
	 * Stars the work on the given list of entries. The method creates new instances for each entry
	 * if one does not exists and starts the work on it if not started. If some of the entries have
	 * missing data that will be ignored and will not be returned at the end of the method.
	 *
	 * @param scheduleInstance
	 *            the owning schedule instance
	 * @param entries
	 *            the entries to start
	 * @return the list of started entries.
	 */
	List<ScheduleEntry> startEntries(ScheduleInstance scheduleInstance, List<ScheduleEntry> entries);

	/**
	 * Search assignments by the arguments provided. If only instance is provided - unassigned task
	 * for this instance are searched. If resources are provided task for this resource are searched
	 * and grouped as a result.
	 *
	 * @param instance
	 *            the instance to search assignment for specific project or for any
	 * @param resources
	 *            the resources to search for specific person or any resource in the system
	 * @param timeFrame
	 *            the time frame to use for searched schedule entries
	 * @return the map of entries grouped by resource id, or if searching for unassigned -1 as key.
	 */
	Map<String, List<ScheduleEntry>> searchAssignments(ScheduleInstance instance,
			List<Serializable> resources, DateRange timeFrame);

	/**
	 * Moves entry to the new parent set in the entry. Fires an event for moving and persists the
	 * changes of the given entry to DB.
	 * 
	 * @param entry
	 *            the entry that is being moved
	 * @return the list of entries updated/added
	 */
	List<ScheduleEntry> moveEntry(ScheduleEntry entry);
}
