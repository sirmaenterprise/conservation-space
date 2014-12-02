package com.sirma.itt.pm.schedule.service;

import java.io.Serializable;
import java.util.List;

import com.sirma.itt.emf.instance.model.Instance;
import com.sirma.itt.emf.resources.model.Resource;
import com.sirma.itt.pm.schedule.model.ScheduleAssignment;
import com.sirma.itt.pm.schedule.model.ScheduleDependency;
import com.sirma.itt.pm.schedule.model.ScheduleEntry;

/**
 * Service for managing the {@link ScheduleAssignment}s, {@link ScheduleDependency}s and resources
 * in the context of a schedule.
 *
 * @author BBonev
 */
public interface ScheduleResourceService {

	/**
	 * Adds the given schedule assignment.
	 *
	 * @param assignment
	 *            the assignment
	 * @return the schedule assignment
	 */
	public ScheduleAssignment add(ScheduleAssignment assignment);

	/**
	 * Save saves and updates the given assignment.
	 *
	 * @param assignment
	 *            the assignment
	 * @return the schedule assignment
	 */
	public ScheduleAssignment save(ScheduleAssignment assignment);

	/**
	 * Deletes the given schedule assignment.
	 *
	 * @param assignment
	 *            the assignment
	 */
	public void delete(ScheduleAssignment assignment);

	/**
	 * Adds the given schedule dependency.
	 *
	 * @param dependency
	 *            the dependency
	 * @return the schedule dependency
	 */
	public ScheduleDependency add(ScheduleDependency dependency);

	/**
	 * Saves the give schedule dependency.
	 *
	 * @param dependency
	 *            the dependency
	 * @return the schedule dependency
	 */
	public ScheduleDependency save(ScheduleDependency dependency);

	/**
	 * Delete deletes the given schedule dependency.
	 *
	 * @param dependency
	 *            the dependency to delete
	 */
	public void delete(ScheduleDependency dependency);

	/**
	 * Gets the schedule resources.
	 *
	 * @param owningInstance
	 *            The instance that owns the schedule represented by the given schedule ID
	 * @param scheduleId
	 *            the schedule id
	 * @return the resources
	 */
	List<Resource> getResources(Instance owningInstance, Serializable scheduleId);

	/**
	 * Gets the schedule assignments.
	 *
	 * @param scheduleId
	 *            the schedule id
	 * @return the assignments
	 */
	List<ScheduleAssignment> getAssignments(Serializable scheduleId);

	/**
	 * Loads the assignments from the table of assigments using the correct dao
	 *
	 * @param assignmentsIds
	 *            are the assignments to load as db ids
	 * @return list of loaded model objects
	 */
	List<ScheduleAssignment> getAssignments(List<Long> assignmentsIds);

	/**
	 * Gets the schedule entry assignments.
	 *
	 * @param entry
	 *            the entry
	 * @return the assignments for the given schedule entry
	 */
	List<ScheduleAssignment> getAssignments(ScheduleEntry entry);

	/**
	 * Gets the dependencies for the given {@link ScheduleEntry}. The second argument defines if the
	 * dependencies are starting from the given entry or ending to the entry. If <code>null</code>
	 * all will be returned.
	 *
	 * @param entry
	 *            the base entry to look for
	 * @param from
	 *            the search direction. If <code>true</code> will fetch dependencies that start with
	 *            the given entry. If <code>false</code> will fetch dependencies that end with the
	 *            given entry. If <code>null</code> will fetch both.
	 * @return the dependencies or empty list.
	 */
	List<ScheduleDependency> getDependencies(ScheduleEntry entry, Boolean from);

	/**
	 * Gets all dependencies for the given schedule.
	 *
	 * @param scheduleId
	 *            the schedule id
	 * @return the dependencies
	 */
	List<ScheduleDependency> getDependencies(Serializable scheduleId);

	/**
	 * Loads the dependencies from the table of dependencies using the correct dao
	 *
	 * @param dependenciesIds
	 *            are the dependencies to load as db ids
	 * @return list of loaded model objects
	 */
	List<ScheduleDependency> getDependencies(List<Long> dependenciesIds);
}
