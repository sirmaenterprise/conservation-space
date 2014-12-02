package com.sirma.cmf.web.userdashboard;

import java.util.Set;

import com.sirma.itt.emf.domain.Context;
import com.sirma.itt.emf.instance.model.Instance;
import com.sirma.itt.emf.search.model.SearchArguments;
import com.sirma.itt.emf.search.model.SearchFilter;

/**
 * DashboardPanelActionBaseMock.
 * 
 * @author svelikov
 */
public class DashboardPanelActionBaseMock extends DashboardPanelActionBase {

	@Override
	public void updateSearchArguments(SearchArguments searchArguments,
			SearchFilter selectedSearchFilter) {
		// TODO Auto-generated method stub

	}

	@Override
	public void updateSearchContext(Context context) {
		// TODO Auto-generated method stub

	}

	@Override
	public Set dashletActionIds() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String targetDashletName() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Instance dashletActionsTarget() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void executeDefaultFilter() {
		// TODO Auto-generated method stub

	}

}
