package com.sirma.cmf.web.userdashboard.panel;

import java.io.Serializable;
import java.util.Iterator;
import java.util.Set;

import javax.inject.Inject;
import javax.inject.Named;

import org.apache.myfaces.extensions.cdi.core.api.scope.conversation.ViewAccessScoped;

import com.sirma.cmf.web.userdashboard.DashboardPanelActionBase;
import com.sirma.itt.cmf.beans.model.CaseInstance;
import com.sirma.itt.emf.domain.Context;
import com.sirma.itt.emf.info.VersionInfo;
import com.sirma.itt.emf.instance.dao.InstanceType;
import com.sirma.itt.emf.instance.model.Instance;
import com.sirma.itt.emf.plugin.ExtensionPoint;
import com.sirma.itt.emf.search.model.SearchArguments;
import com.sirma.itt.emf.search.model.SearchFilter;
import com.sirma.itt.emf.web.dashboard.panel.DashboardPanelController;

/**
 * <b>MyCasesPanel</b> manage functionality for dashlet, located in personal/user dashboard. The
 * content is represented as case records, actions and filters.
 * 
 * @author svelikov
 */
@Named
@InstanceType(type = "UserDashboard")
@ViewAccessScoped
public class MyCasesPanel extends DashboardPanelActionBase<CaseInstance> implements Serializable,
		DashboardPanelController {

	private static final long serialVersionUID = -7542862392405840580L;

	private static final String USERDASHBOARD_DASHLET_CASES = "userdashboard_dashlet_cases";

	/** The module restriction value. */
	private boolean moduleRestriction;

	/** The versions info. */
	@Inject
	@ExtensionPoint(value = VersionInfo.TARGET_NAME)
	private Iterable<VersionInfo> versionInfo;

	@Override
	public void initData() {
		onOpen();
	}

	@Override
	public void executeDefaultFilter() {
		searchCriteriaChanged();
	}

	/**
	 * Getter method that restrict CMF elements based on the loaded modules. TODO: Need to be
	 * discussed.
	 * 
	 * @return boolean value
	 */
	// REVIEW: this is used to decide if create case instance should be allowed. Add extension point
	// and provide the button from PM module instead!
	public boolean getModuleRestriction() {
		Iterator<VersionInfo> iteratorInfo = versionInfo.iterator();
		moduleRestriction = true;
		while (iteratorInfo.hasNext()) {
			VersionInfo versionInfo = iteratorInfo.next();
			if (versionInfo.getModuleDescription().equalsIgnoreCase("pm module")) {
				moduleRestriction = false;
				break;
			}
		}
		return moduleRestriction;
	}

	@Override
	public Set<String> dashletActionIds() {
		return null;
	}

	@Override
	public String targetDashletName() {
		return USERDASHBOARD_DASHLET_CASES;
	}

	@Override
	public Instance dashletActionsTarget() {
		return null;
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
