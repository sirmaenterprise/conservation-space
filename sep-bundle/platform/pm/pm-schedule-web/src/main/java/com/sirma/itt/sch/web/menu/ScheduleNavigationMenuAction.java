package com.sirma.itt.sch.web.menu;

import java.io.Serializable;
import java.util.Map;

import javax.inject.Inject;
import javax.inject.Named;

import org.apache.myfaces.extensions.cdi.core.api.scope.conversation.ViewAccessScoped;

import com.sirma.cmf.web.Action;
import com.sirma.itt.cmf.states.PrimaryStates;
import com.sirma.itt.emf.db.SequenceEntityGenerator;
import com.sirma.itt.emf.state.StateService;
import com.sirma.itt.pm.constants.ProjectProperties;
import com.sirma.itt.pm.domain.model.ProjectInstance;

/**
 * The Class ScheduleNavigationMenuAction.
 * 
 * @author svelikov
 */
@Named
@ViewAccessScoped
public class ScheduleNavigationMenuAction extends Action implements Serializable {

	/**
	 * Comment for serialVersionUID.
	 */
	private static final long serialVersionUID = -6994235482245291203L;
	@Inject
	private StateService stateService;

    /**
	 * Check if current user can open schedule according the project status and user role.
	 * 
	 * @param projectInstance
	 *            the project instance
	 * @return true, if successful
	 */
    public boolean canOpenSchedule(ProjectInstance projectInstance) {
		if (!SequenceEntityGenerator.isPersisted(projectInstance)) {
            return false;
        }

        Map<String, Serializable> properties = projectInstance.getProperties();
        // FIXME: remove hardcodded value after codelist is created
        if ("INIT".equals(properties.get(ProjectProperties.STATUS))) {
            return false;
        }

        // TODO: check for user role

        return true;
    }

    /**
	 * Check if current user can open resource allocation view according the project status and user
	 * role.
	 * 
	 * @param projectInstance
	 *            the project instance
	 * @return true, if successful
	 */
    public boolean canOpenResourceAllocation(ProjectInstance projectInstance) {
		if (!SequenceEntityGenerator.isPersisted(projectInstance)) {
            return false;
        }

		if (stateService.isInState(PrimaryStates.INITIAL, projectInstance)) {
            return false;
        }

        // TODO: check for user role

        return true;
    }
}
