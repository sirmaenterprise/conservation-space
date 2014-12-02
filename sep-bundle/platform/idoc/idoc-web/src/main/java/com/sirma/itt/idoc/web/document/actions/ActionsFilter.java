package com.sirma.itt.idoc.web.document.actions;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.event.Observes;
import javax.enterprise.inject.Any;
import javax.inject.Inject;

import com.sirma.cmf.web.ActionsPlaceholders;
import com.sirma.itt.cmf.beans.model.DocumentInstance;
import com.sirma.itt.cmf.constants.allowed_action.ActionTypeConstants;
import com.sirma.itt.cmf.domain.ObjectTypesCmf;
import com.sirma.itt.emf.instance.model.Instance;
import com.sirma.itt.emf.label.LabelProvider;
import com.sirma.itt.emf.security.event.ActionEvaluatedEvent;
import com.sirma.itt.emf.security.event.FilterAction;
import com.sirma.itt.emf.security.model.Action;
import com.sirma.itt.idoc.web.document.IntelligentDocumentEditor;

/**
 * The Class ActionsFilter.
 * 
 * @author yasko
 */
@ApplicationScoped
public class ActionsFilter {

	/** The label provider. */
	@Inject
	private LabelProvider labelProvider;

	/** The editor. */
	@Any
	@Inject
	private IntelligentDocumentEditor editor;

	/** The Constant INAPPLICABLE_CMF_ACTION_IDS. */
	private static final Set<String> INAPPLICABLE_CMF_ACTION_IDS;

	static {
		INAPPLICABLE_CMF_ACTION_IDS = new HashSet<>();
		INAPPLICABLE_CMF_ACTION_IDS.add(ActionTypeConstants.EDIT_DETAILS);
		INAPPLICABLE_CMF_ACTION_IDS.add(ActionTypeConstants.EDIT_OFFLINE);
		INAPPLICABLE_CMF_ACTION_IDS.add(ActionTypeConstants.UPLOAD_NEW_VERSION);
	}

	/**
	 * Filter document details actions.
	 * 
	 * @param event
	 *            Actions filter event object.
	 */
	public void filterDocumentDetailsActions(
			@Observes @FilterAction(value = ObjectTypesCmf.DOCUMENT, placeholder = ActionsPlaceholders.DOCUMENT_DETAILS) final ActionEvaluatedEvent event) {
		Instance instance = event.getInstance();
		Set<Action> actions = event.getActions();
		if (editor.canHandle((DocumentInstance) instance)) {
			actions.clear();
			for (IdocActionDefinition id : IdocActionDefinition.values()) {
				actions.add(new IdocAction(id, labelProvider.getValue("idoc.btn." + id.getId())));
			}
		}
	}

	/**
	 * Filter document case actions.
	 * 
	 * @param event
	 *            Actions filter event object.
	 */
	public void filterDocumentCaseActions(
			@Observes @FilterAction(value = ObjectTypesCmf.DOCUMENT, placeholder = ActionsPlaceholders.CASE_DOCUMENTS_TAB) final ActionEvaluatedEvent event) {
		filterDocumentActions(event);
	}

	/**
	 * Filter document actions.
	 * 
	 * @param event
	 *            the event
	 */
	private void filterDocumentActions(final ActionEvaluatedEvent event) {
		Instance instance = event.getInstance();
		Set<Action> actions = event.getActions();
		if (editor.canHandle((DocumentInstance) instance)) {
			Iterator<Action> iterator = actions.iterator();
			while (iterator.hasNext()) {
				Action action = iterator.next();
				if (INAPPLICABLE_CMF_ACTION_IDS.contains(action.getActionId())) {
					iterator.remove();
				}
			}
		}
	}

	/**
	 * Filter document actions for case dashboard.
	 * 
	 * @param event
	 *            the event
	 */
	public void filterDocumentActionsForCaseDashboard(
			@Observes @FilterAction(value = ObjectTypesCmf.DOCUMENT, placeholder = ActionsPlaceholders.CASE_DASHBOARD_DOCUMENTS) final ActionEvaluatedEvent event) {
		filterDocumentActions(event);
	}

	/**
	 * Filter document actions for user documents dashboard.
	 * 
	 * @param event
	 *            the event
	 */
	public void filterDocumentActionsForUserDocumentsDashboard(
			@Observes @FilterAction(value = ObjectTypesCmf.DOCUMENT, placeholder = ActionsPlaceholders.USER_DASHBOARD_DOCUMENTS) final ActionEvaluatedEvent event) {
		filterDocumentActions(event);
	}

	/**
	 * Filter document actions for project documents dashboard.<br>
	 * It's not for here but there is no good place to add the method. So the placeholder is
	 * hard-coded.
	 * 
	 * @param event
	 *            the event
	 */
	public void filterDocumentActionsForProjectDocumentsDashboard(
			@Observes @FilterAction(value = ObjectTypesCmf.DOCUMENT, placeholder = "project-dashboard-document") final ActionEvaluatedEvent event) {
		filterDocumentActions(event);
	}


}
