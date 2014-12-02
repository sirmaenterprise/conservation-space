package com.sirma.itt.sch.web.mock.service;

import java.io.Serializable;
import java.util.List;
import java.util.Map;

import javax.enterprise.inject.Alternative;

import com.sirma.itt.emf.domain.model.DefinitionModel;
import com.sirma.itt.emf.instance.model.Instance;
import com.sirma.itt.emf.time.DateRange;
import com.sirma.itt.pm.schedule.model.ScheduleEntry;
import com.sirma.itt.pm.schedule.model.ScheduleInstance;
import com.sirma.itt.pm.schedule.service.ScheduleService;

/**
 * The Class ScheduleServiceMock.
 *
 * @author svelikov
 */
@Alternative
public class ScheduleServiceMock implements ScheduleService {

	@Override
	public ScheduleInstance getOrCreateSchedule(Instance instance) {
		ScheduleInstance scheduleInstance = new ScheduleInstance();
		scheduleInstance.setId(1l);
		return scheduleInstance;
	}

	@Override
	public ScheduleInstance getSchedule(Instance instance) {
		return null;
	}

	@Override
	public List<ScheduleEntry> getEntries(ScheduleInstance instance) {
		return null;
	}

	@Override
	public ScheduleEntry getEntryForActualInstance(Instance instance) {
		return null;
	}

	@Override
	public List<ScheduleEntry> addEntry(ScheduleEntry entry) {
		return null;
	}

	@Override
	public List<ScheduleEntry> updateEntry(ScheduleEntry entry) {
		return null;
	}

	@Override
	public void deleteEntry(ScheduleEntry entry) {

	}

	@Override
	public List<ScheduleEntry> getChildren(Serializable nodeId) {
		return null;
	}

	@Override
	public Map<String, List<DefinitionModel>> getAllowedChildrenForNode(ScheduleEntry targetNode,
			List<ScheduleEntry> currentChildren) {
		return null;
	}

	@Override
	public List<ScheduleEntry> startEntries(ScheduleInstance scheduleInstance,
			List<ScheduleEntry> entries) {
		return null;
	}

	@Override
	public ScheduleEntry getEntryForActualInstance(Instance instance, boolean autoCreate) {

		return null;
	}

	@Override
	public Map<String, List<ScheduleEntry>> searchAssignments(ScheduleInstance instance,
			List<Serializable> resources, DateRange timeFrame) {

		return null;
	}

	@Override
	public ScheduleEntry cancelEntry(ScheduleEntry entry) {

		return null;
	}

	@Override
	public List<ScheduleEntry> moveEntry(ScheduleEntry entry) {
		// TODO Auto-generated method stub
		return null;
	}

}
