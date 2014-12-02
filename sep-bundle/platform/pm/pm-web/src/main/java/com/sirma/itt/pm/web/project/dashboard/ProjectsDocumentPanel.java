package com.sirma.itt.pm.web.project.dashboard;

import java.io.Serializable;
import java.util.Set;

import javax.inject.Named;

import org.apache.myfaces.extensions.cdi.core.api.scope.conversation.ViewAccessScoped;

import com.sirma.cmf.web.userdashboard.DashboardPanelActionBase;
import com.sirma.itt.cmf.beans.model.DocumentInstance;
import com.sirma.itt.emf.domain.Context;
import com.sirma.itt.emf.instance.dao.InstanceType;
import com.sirma.itt.emf.instance.model.Instance;
import com.sirma.itt.emf.search.model.SearchArguments;
import com.sirma.itt.emf.search.model.SearchFilter;
import com.sirma.itt.emf.web.dashboard.panel.DashboardPanelController;
import com.sirma.itt.pm.domain.model.ProjectInstance;

/**
 * <b>ProjectsDocumentPanel</b> manage functionality for dashlet, located in project dashboard. The
 * content is represented as document records, actions and filters.
 * 
 * @author cdimitrov
 */
@Named
@InstanceType(type = "ProjectDashboard")
@ViewAccessScoped
public class ProjectsDocumentPanel extends DashboardPanelActionBase<DocumentInstance> implements
		Serializable, DashboardPanelController {

	/** The Constant serial version identifier. */
	private static final long serialVersionUID = 8421197115069895908L;

	/** The project instance extracted from the context. */
	private ProjectInstance context;

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void initData() {
		onOpen();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected void initializeForAsynchronousInvocation() {
		context = getDocumentContext().getInstance(ProjectInstance.class);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void executeDefaultFilter() {
		searchCriteriaChanged();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Set<String> dashletActionIds() {
		return null;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public String targetDashletName() {
		return PROJECT_DOCUMENTS_DASHLET_PLACEHOLDER;
	}

	/**
	 * {@inheritDoc}
	 */
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
