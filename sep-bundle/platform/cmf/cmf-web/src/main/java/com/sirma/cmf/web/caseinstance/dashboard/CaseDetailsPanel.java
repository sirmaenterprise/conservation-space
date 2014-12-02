package com.sirma.cmf.web.caseinstance.dashboard;

import java.io.Serializable;
import java.util.Set;

import javax.faces.component.UIComponent;
import javax.inject.Named;

import org.apache.myfaces.extensions.cdi.core.api.scope.conversation.ViewAccessScoped;

import com.sirma.cmf.web.constants.CMFConstants;
import com.sirma.cmf.web.form.FormViewMode;
import com.sirma.cmf.web.userdashboard.DashboardPanelActionBase;
import com.sirma.itt.cmf.beans.definitions.CaseDefinition;
import com.sirma.itt.cmf.beans.model.CaseInstance;
import com.sirma.itt.emf.domain.Context;
import com.sirma.itt.emf.instance.dao.InstanceType;
import com.sirma.itt.emf.instance.model.Instance;
import com.sirma.itt.emf.search.model.SearchArguments;
import com.sirma.itt.emf.search.model.SearchFilter;
import com.sirma.itt.emf.web.dashboard.panel.DashboardPanelController;

/**
 * CaseDetailsPanel backing bean.
 * 
 * @author svelikov
 */
@Named
@InstanceType(type = "CaseDashboard")
@ViewAccessScoped
public class CaseDetailsPanel extends DashboardPanelActionBase<CaseInstance> implements
		Serializable, DashboardPanelController {

	private static final long serialVersionUID = 6205869710050734957L;

	private static final String CASEDASHBOARD_DASHLET_CASE_DETAILS = "casedashboard_dashlet_case_details";

	@Override
	public void initData() {
		onOpen();
		renderCaseFields();
	}

	@Override
	public boolean getFilterActions() {
		return false;
	}

	/**
	 * Builds a case properties fields for preview.
	 */
	public void renderCaseFields() {
		UIComponent panel = getPanel(CMFConstants.CASE_DATA_PANEL);
		if (panel != null) {
			panel.getChildren().clear();
		}

		reloadCaseInstance();
		CaseInstance caseInstance = getDocumentContext().getInstance(CaseInstance.class);
		CaseDefinition caseDefinition = (CaseDefinition) dictionaryService
				.getInstanceDefinition(caseInstance);
		getDocumentContext().populateContext(caseInstance, CaseDefinition.class, caseDefinition);

		invokeReader(caseDefinition, caseInstance, panel, FormViewMode.PREVIEW, null);
	}

	@Override
	public void executeDefaultFilter() {
		// Auto-generated method stub
	}

	@Override
	public Set<String> dashletActionIds() {
		return getActionsForCurrentInstance();
	}

	@Override
	public String targetDashletName() {
		return CASEDASHBOARD_DASHLET_CASE_DETAILS;
	}

	@Override
	public Instance dashletActionsTarget() {
		return getDocumentContext().getCurrentInstance();
	}

	@Override
	public void updateSearchArguments(SearchArguments<CaseInstance> searchArguments,
			SearchFilter selectedSearchFilter) {
		// TODO Auto-generated method stub

	}

	@Override
	public void updateSearchContext(Context<String, Object> context) {
		// TODO Auto-generated method stub

	}

}
