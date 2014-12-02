package com.sirma.cmf.web.caseinstance;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.event.Observes;

import com.sirma.cmf.web.ActionsPlaceholders;
import com.sirma.itt.cmf.constants.allowed_action.ActionTypeConstants;
import com.sirma.itt.cmf.domain.ObjectTypesCmf;
import com.sirma.itt.emf.security.event.ActionEvaluatedEvent;
import com.sirma.itt.emf.security.event.FilterAction;
import com.sirma.itt.emf.security.model.Action;

/**
 * DocumentActionFilters implementation for action filtering.
 * 
 * @author svelikov
 */
@ApplicationScoped
public class DocumentActionFilters implements ActionsPlaceholders {

	private static final List<String> notAllowedActionsForProjectDocumentsDashlet = new ArrayList<String>(
			Arrays.asList(ActionTypeConstants.MOVE_SAME_CASE));

	private static final List<String> notAllowedActionsForProjectMediaDashlet = new ArrayList<String>(
			Arrays.asList(ActionTypeConstants.MOVE_SAME_CASE));

	private static final List<String> notAllowedActionsForMyDocumentsDashlet = new ArrayList<String>(
			Arrays.asList(ActionTypeConstants.MOVE_SAME_CASE));

	private static final List<String> notAllowedActionsForMyMediaDashlet = new ArrayList<String>(
			Arrays.asList(ActionTypeConstants.MOVE_SAME_CASE));

	/**
	 * Filter project document dashlet actions actions.
	 * 
	 * @param event
	 *            the event
	 */
	public void filterProjectDocumentDashletActions(
			@Observes @FilterAction(value = ObjectTypesCmf.DOCUMENT, placeholder = PROJECT_DOCUMENTS_DASHLET_PLACEHOLDER) ActionEvaluatedEvent event) {
		filterActions(event.getActions(), notAllowedActionsForProjectDocumentsDashlet, true);
	}

	/**
	 * Filter project media dashlet actions.
	 * 
	 * @param event
	 *            the event
	 */
	public void filterProjectMediaDashletActions(
			@Observes @FilterAction(value = ObjectTypesCmf.DOCUMENT, placeholder = PROJECTDASHBOARD_DASHLET_MEDIA) ActionEvaluatedEvent event) {
		filterActions(event.getActions(), notAllowedActionsForProjectMediaDashlet, true);
	}

	/**
	 * Filter my document dashlet actions.
	 * 
	 * @param event
	 *            the event
	 */
	public void filterMyDocumentDashletActions(
			@Observes @FilterAction(value = ObjectTypesCmf.DOCUMENT, placeholder = USERDASHBOARD_DASHLET_DOCUMENTS) ActionEvaluatedEvent event) {
		filterActions(event.getActions(), notAllowedActionsForMyDocumentsDashlet, true);
	}

	/**
	 * Filter my media dashlet actions.
	 * 
	 * @param event
	 *            the event
	 */
	public void filterMyMediaDashletActions(
			@Observes @FilterAction(value = ObjectTypesCmf.DOCUMENT, placeholder = USERDASHBOARD_DASHLET_MEDIA) ActionEvaluatedEvent event) {
		filterActions(event.getActions(), notAllowedActionsForMyMediaDashlet, true);
	}

	/**
	 * Filter actions.
	 * 
	 * @param actions
	 *            the actions
	 * @param filters
	 *            the filters
	 * @param retain
	 *            the retain
	 */
	private void filterActions(Set<Action> actions, List<String> filters, boolean retain) {
		for (Iterator<Action> iterator = actions.iterator(); iterator.hasNext();) {
			Action action = iterator.next();
			if (filters.contains(action.getActionId()) == retain) {
				iterator.remove();
			}
		}
	}
}
