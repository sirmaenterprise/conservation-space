package com.sirma.cmf.web.caseinstance.section;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.event.Observes;

import com.sirma.itt.cmf.constants.allowed_action.ActionTypeConstants;
import com.sirma.itt.cmf.domain.ObjectTypesCmf;
import com.sirma.itt.emf.security.event.ActionEvaluatedEvent;
import com.sirma.itt.emf.security.event.FilterAction;
import com.sirma.itt.emf.security.model.Action;

/**
 * CaseActionFilters implementation for action filtering.
 * 
 * @author svelikov
 */
@ApplicationScoped
public class CaseActionFilters {

	// TODO: move these to objects module
	/** The Constant allowedActionsForObjectsTab. */
	private static final List<String> allowedActionsForObjectsTab = new ArrayList<String>(
			Arrays.asList(ActionTypeConstants.CREATE_OBJECTS_SECTION));

	/** The Constant allowedActionsForDocumentsTab. */
	private static final List<String> allowedActionsForDocumentsTab = new ArrayList<String>(
			Arrays.asList(ActionTypeConstants.CREATE_DOCUMENTS_SECTION));

	/** The Constant notAllowedActionsForDetailsTab. */
	private static final List<String> notAllowedActionsForDetailsTab = new ArrayList<String>(
			Arrays.asList(ActionTypeConstants.CREATE_OBJECTS_SECTION,
					ActionTypeConstants.CREATE_DOCUMENTS_SECTION));

	/**
	 * Filter case object tab toolbar actions.
	 * 
	 * @param event
	 *            the event
	 */
	public void filterCaseObjectTabToolbarActions(
			@Observes @FilterAction(value = ObjectTypesCmf.CASE, placeholder = "case-objects-tab") ActionEvaluatedEvent event) {
		filterActions(event.getActions(), allowedActionsForObjectsTab, false);
	}

	/**
	 * Filter case documents tab toolbar actions.
	 * 
	 * @param event
	 *            the event
	 */
	public void filterCaseDocumentsTabToolbarActions(
			@Observes @FilterAction(value = ObjectTypesCmf.CASE, placeholder = "case-documents-tab") ActionEvaluatedEvent event) {
		filterActions(event.getActions(), allowedActionsForDocumentsTab, false);
	}

	/**
	 * Filter case details tab toolbar actions.
	 * 
	 * @param event
	 *            the event
	 */
	public void filterCaseDetailsTabToolbarActions(
			@Observes @FilterAction(value = ObjectTypesCmf.CASE, placeholder = "case-details-tab") ActionEvaluatedEvent event) {
		filterActions(event.getActions(), notAllowedActionsForDetailsTab, true);
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
