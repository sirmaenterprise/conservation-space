package com.sirma.cmf.web.userdashboard.panel;

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

/**
 * <b>MyDocumentsPanel</b> manage functionality for dashlet, located in personal/user dashboard. The
 * content is represented as document records, actions and filters.
 * 
 * @author svelikov
 */
@Named
@InstanceType(type = "UserDashboard")
@ViewAccessScoped
public class MyDocumentsPanel extends DashboardPanelActionBase<DocumentInstance> implements
		Serializable, DashboardPanelController {

	/** The Constant serialVersionUID. */
	private static final long serialVersionUID = 6798947303154434519L;

	@Override
	public void initData() {
		onOpen();
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
		// auto-generated method stub
		return null;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public String targetDashletName() {
		return USERDASHBOARD_DASHLET_DOCUMENTS;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Instance dashletActionsTarget() {
		// auto-generated method stub
		return null;
	}

	@Override
	public void updateSearchArguments(SearchArguments<DocumentInstance> searchArguments,
			SearchFilter selectedSearchFilter) {
		// auto-generated method stub

	}

	@Override
	public void updateSearchContext(Context<String, Object> context) {
		// auto-generated method stub

	}

}
