package com.sirma.cmf.web.caseinstance.dashboard;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.inject.Named;

import org.apache.myfaces.extensions.cdi.core.api.scope.conversation.ViewAccessScoped;

import com.sirma.cmf.web.userdashboard.DashboardPanelActionBase;
import com.sirma.itt.cmf.beans.model.DocumentInstance;
import com.sirma.itt.cmf.constants.allowed_action.ActionTypeConstants;
import com.sirma.itt.emf.domain.Context;
import com.sirma.itt.emf.instance.dao.InstanceType;
import com.sirma.itt.emf.instance.model.Instance;
import com.sirma.itt.emf.search.model.SearchArguments;
import com.sirma.itt.emf.search.model.SearchFilter;
import com.sirma.itt.emf.web.dashboard.panel.DashboardPanelController;

/**
 * <b>CaseDocumentsPanel</b> manage functionality for panel, located in case dashboard. The content
 * is represented as document records, actions and filters.
 * 
 * @author svelikov
 */
@Named
@InstanceType(type = "CaseDashboard")
@ViewAccessScoped
public class CaseDocumentsPanel extends DashboardPanelActionBase<DocumentInstance> implements
		Serializable, DashboardPanelController {

	private static final long serialVersionUID = 5328697527361900449L;

	private static final String CASEDASHBOARD_DASHLET_DOCUMENTS = "casedashboard_dashlet_documents";

	/** The document panel actions located in the toolbar. */
	private final Set<String> dashletActions = new HashSet<String>(Arrays.asList(
			ActionTypeConstants.CREATE_DOCUMENTS_SECTION, ActionTypeConstants.UPLOAD));

	@Override
	public void initData() {
		onOpen();
	}

	@Override
	public void executeDefaultFilter() {
		searchCriteriaChanged();
	}

	/**
	 * Filters {@link DocumentInstance} list and leaves only those with attachments.
	 * 
	 * @param documentInstances
	 *            List with {@link DocumentInstance} objects.
	 * @return List with {@link DocumentInstance} objects that has attachments.
	 */
	private List<DocumentInstance> getAllWithAttachments(List<Instance> documentInstances) {

		List<DocumentInstance> result = new ArrayList<DocumentInstance>(documentInstances.size());

		for (Instance documentInstance : documentInstances) {
			if ((documentInstance instanceof DocumentInstance)
					&& ((DocumentInstance) documentInstance).hasDocument()) {
				result.add((DocumentInstance) documentInstance);
			}
		}

		return result;
	}

	/**
	 * Generates a css row classes string used in rich:list in order to hide rows for document
	 * instances that has no attachments.
	 * 
	 * @param documentInstances
	 *            DocumentInstances list.
	 * @return String with row classes.
	 */
	public String documentRowClasses(List<DocumentInstance> documentInstances) {
		List<String> classes = new ArrayList<String>();
		if (documentInstances != null) {
			for (DocumentInstance documentInstance : documentInstances) {
				if (documentInstance.hasDocument()) {
					classes.add("");
				} else {
					classes.add("hidden-row");
				}
			}
		}
		String csv = "";
		if (!classes.isEmpty()) {
			csv = classes.toString();
			csv = csv.substring(1, csv.length() - 1);
		}
		return csv;
	}

	@Override
	public Set<String> dashletActionIds() {
		return dashletActions;
	}

	@Override
	public String targetDashletName() {
		return CASEDASHBOARD_DASHLET_DOCUMENTS;
	}

	@Override
	public Instance dashletActionsTarget() {
		return getDocumentContext().getCurrentInstance();
	}

	@Override
	public void updateSearchArguments(SearchArguments<DocumentInstance> searchArguments,
			SearchFilter selectedSearchFilter) {
		// TODO Auto-generated method stub

	}

	@Override
	public void updateSearchContext(Context<String, Object> context) {
		// TODO Auto-generated method stub

	}

}
