package com.sirma.itt.sch.web.action;

import java.io.Serializable;

import javax.enterprise.event.Observes;
import javax.inject.Inject;
import javax.inject.Named;

import org.apache.myfaces.extensions.cdi.core.api.scope.conversation.ViewAccessScoped;

import com.sirma.cmf.web.EntityAction;
import com.sirma.cmf.web.menu.NavigationMenu;
import com.sirma.cmf.web.menu.NavigationMenuEvent;
import com.sirma.itt.emf.db.SequenceEntityGenerator;
import com.sirma.itt.pm.domain.model.ProjectInstance;
import com.sirma.itt.pm.schedule.model.ScheduleInstance;
import com.sirma.itt.pm.schedule.service.ScheduleService;
import com.sirma.itt.sch.web.menu.ScheduleMenuConstants;

/**
 * The Class ScheduleAction.
 * 
 * @author svelikov
 */
@Named
@ViewAccessScoped
public class ScheduleAction extends EntityAction implements Serializable {

	/** The Constant serialVersionUID. */
	private static final long serialVersionUID = 5488891583677705305L;

	/** The schedule service. */
	@Inject
	private ScheduleService scheduleService;

	/**
	 * Open project schedule.
	 * 
	 * @param navigationEvent
	 *            the navigation event
	 */
	public void openProjectSchedule(
			@Observes @NavigationMenu(ScheduleMenuConstants.PROJECT_SCHEDULE) NavigationMenuEvent navigationEvent) {

		log.debug("PMWeb: Executing ScheduleAction.openProjectSchedule observer");

		ProjectInstance projectInstance = getDocumentContext().getInstance(ProjectInstance.class);

		if (!SequenceEntityGenerator.isPersisted(projectInstance)) {
			return;
		}

		ScheduleInstance scheduleInstance = scheduleService.getOrCreateSchedule(projectInstance);

		getDocumentContext().addInstance(scheduleInstance);
	}
}
