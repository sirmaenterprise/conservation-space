package com.sirma.itt.sch.web.mock.service;

import java.io.Serializable;
import java.util.List;

import javax.enterprise.inject.Alternative;

import com.sirma.itt.emf.instance.model.Instance;
import com.sirma.itt.emf.resources.model.Resource;
import com.sirma.itt.pm.schedule.model.ScheduleAssignment;
import com.sirma.itt.pm.schedule.model.ScheduleDependency;
import com.sirma.itt.pm.schedule.model.ScheduleEntry;
import com.sirma.itt.pm.schedule.service.ScheduleResourceService;

/**
 * The Class ScheduleResourceServiceMock.
 *
 * @author svelikov
 */
@Alternative
public class ScheduleResourceServiceMock implements ScheduleResourceService {

	/**
	 * {@inheritDoc}
	 */
	@Override
	public ScheduleAssignment add(ScheduleAssignment assignment) {
		return null;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public ScheduleAssignment save(ScheduleAssignment assignment) {
		return null;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void delete(ScheduleAssignment assignment) {

	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public ScheduleDependency add(ScheduleDependency assignment) {
		return null;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public ScheduleDependency save(ScheduleDependency assignment) {
		return null;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void delete(ScheduleDependency dependency) {

	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public List<Resource> getResources(Instance owningInstance, Serializable scheduleId) {
		return null;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public List<ScheduleAssignment> getAssignments(Serializable scheduleId) {
		return null;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public List<ScheduleAssignment> getAssignments(ScheduleEntry entry) {
		return null;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public List<ScheduleDependency> getDependencies(Serializable scheduleId) {
		return null;
	}

	@Override
	public List<ScheduleDependency> getDependencies(ScheduleEntry entry, Boolean from) {
		return null;
	}

	@Override
	public List<ScheduleAssignment> getAssignments(List<Long> assignmentsIds) {
		return null;
	}

	@Override
	public List<ScheduleDependency> getDependencies(List<Long> dependenciesIds) {
		return null;
	}

}
