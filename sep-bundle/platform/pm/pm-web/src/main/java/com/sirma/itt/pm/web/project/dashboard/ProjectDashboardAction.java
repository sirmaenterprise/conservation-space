package com.sirma.itt.pm.web.project.dashboard;

import java.io.Serializable;

import javax.enterprise.event.Observes;
import javax.inject.Named;

import org.apache.myfaces.extensions.cdi.core.api.scope.conversation.ViewAccessScoped;

import com.sirma.cmf.web.EntityAction;
import com.sirma.cmf.web.menu.NavigationMenu;
import com.sirma.cmf.web.menu.NavigationMenuEvent;
import com.sirma.cmf.web.navigation.history.event.NavigationHistoryEvent;
import com.sirma.cmf.web.navigation.history.event.NavigationHistoryType;
import com.sirma.itt.emf.forum.model.TopicInstance;
import com.sirma.itt.emf.instance.InstanceUtil;
import com.sirma.itt.emf.instance.model.Instance;
import com.sirma.itt.pm.domain.definitions.ProjectDefinition;
import com.sirma.itt.pm.domain.model.ProjectInstance;
import com.sirma.itt.pm.event.ProjectOpenEvent;
import com.sirma.itt.pm.web.constants.PmNavigationConstants;
import com.sirma.itt.pm.web.menu.PMMenuConstants;

/**
 * The Class ProjectDashboardAction.
 * 
 * @author svelikov
 */
@Named
@ViewAccessScoped
public class ProjectDashboardAction extends EntityAction implements Serializable {

	private static final long serialVersionUID = 5869564531710802179L;

	/**
	 * Open project dashboard.
	 * 
	 * @param navigationEvent
	 *            the navigation event
	 */
	public void openProjectDashboard(
			@Observes @NavigationMenu(PMMenuConstants.PROJECT_DASHBOARD) NavigationMenuEvent navigationEvent) {
		log.debug("PMWeb: Executing ProjectDashboardAction.openProjectDashboard observer");
		clearContext();
	}

	/**
	 * Open project dashboard from link.
	 * 
	 * @param event
	 *            the event
	 */
	public void openProjectDashboardFromLink(@Observes ProjectOpenEvent event) {
		log.debug("PMWeb: Executing ProjectDashboardAction.openProjectDashboardFromLink observer");
		if (InstanceUtil.isPersisted(event.getInstance())) {
			clearContext();
		}
	}

	/**
	 * Open project dashboard from navigation history.
	 * 
	 * @param event
	 *            the event
	 */
	public void openProjectDashboardFromNavigationHistory(
			@Observes @NavigationHistoryType(PmNavigationConstants.PROJECT_DASHBOARD) NavigationHistoryEvent event) {
		log.debug("PMWeb: Executing ProjectDashboardAction.openProjectDashboardFromNavigationHistory observer");
		clearContext();
	}

	/**
	 * Remove all data from context except project instance and definition.
	 */
	private void clearContext() {
		log.debug("PMWeb: ProjectDashboardAction.clearContext");
		ProjectInstance instance = getDocumentContext().getInstance(ProjectInstance.class);
		ProjectDefinition definition = getDocumentContext().getDefinition(ProjectDefinition.class);
		if (definition == null) {
			definition = (ProjectDefinition) dictionaryService.getInstanceDefinition(instance);
		}
		TopicInstance topicInstance = getDocumentContext().getTopicInstance();
		Instance rootInstance = getDocumentContext().getRootInstance();
		getDocumentContext().clear();
		getDocumentContext().populateContext(instance, ProjectDefinition.class, definition);
		getDocumentContext().setTopicInstance(topicInstance);
		getDocumentContext().setRootInstance(rootInstance);
		getDocumentContext().addContextInstance(instance);
	}

}
