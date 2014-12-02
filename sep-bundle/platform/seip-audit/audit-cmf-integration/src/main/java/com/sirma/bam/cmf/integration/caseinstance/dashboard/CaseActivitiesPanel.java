package com.sirma.bam.cmf.integration.caseinstance.dashboard;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import javax.inject.Inject;
import javax.inject.Named;

import org.apache.myfaces.extensions.cdi.core.api.scope.conversation.ViewAccessScoped;

import com.sirma.bam.cmf.integration.userdashboard.filter.ActivitiesFilterConstants;
import com.sirma.cmf.web.userdashboard.DashboardPanelActionBase;
import com.sirma.itt.cmf.beans.model.CaseInstance;
import com.sirma.itt.emf.audit.activity.AuditActivity;
import com.sirma.itt.emf.audit.activity.AuditActivityCriteria;
import com.sirma.itt.emf.audit.activity.AuditActivityRetriever;
import com.sirma.itt.emf.audit.activity.CriteriaType;
import com.sirma.itt.emf.domain.Context;
import com.sirma.itt.emf.instance.dao.InstanceType;
import com.sirma.itt.emf.instance.model.Instance;
import com.sirma.itt.emf.search.model.SearchArguments;
import com.sirma.itt.emf.search.model.SearchFilter;
import com.sirma.itt.emf.time.DateRange;
import com.sirma.itt.emf.web.dashboard.panel.DashboardPanelController;

/**
 * This class manages activity panel for case dashboard.
 * 
 * @author cdimitrov
 */
@Named
@InstanceType(type = "CaseDashboard")
@ViewAccessScoped
public class CaseActivitiesPanel extends DashboardPanelActionBase<Activities> implements
		Serializable, DashboardPanelController {

	private static final long serialVersionUID = 4844576681324639017L;

	/** Placeholder that will be used for retrieving panel definition. */
	private static final String CASEDASHBOARD_DASHLET_ACTIVITIES = "casedashboard_dashlet_activities";

	private CaseInstance caseInstance;

	/** The results of activities for current panel. */
	private List<Activities> result;

	/** Service that will retrieve activities based on specific criteria. */
	@Inject
	private AuditActivityRetriever bamRetriever;

	/** Service that will construct activities based on search results. */
	@Inject
	private ActivityUtil activityUtil;

	@Override
	protected void initializeForAsynchronousInvocation() {
		caseInstance = getDocumentContext().getInstance(CaseInstance.class);
	}

	@Override
	public void executeDefaultFilter() {
		searchCriteriaChanged();
	}

	@Override
	public String targetDashletName() {
		return CASEDASHBOARD_DASHLET_ACTIVITIES;
	}

	@Override
	public Instance dashletActionsTarget() {
		return null;
	}

	@Override
	public Set<String> dashletActionIds() {
		return null;
	}

	@Override
	public void updateSearchArguments(SearchArguments<Activities> searchArguments,
			SearchFilter selectedSearchFilter) {
		getCaseActivities();
	}

	@Override
	public void updateSearchContext(Context<String, Object> context) {
	}

	@Override
	public void initData() {
		onOpen();
	}

	/**
	 * Method that search for activities based on specific criteria, construct and assign founded
	 * activities.
	 */
	private void getCaseActivities() {
		AuditActivityCriteria criteria = new AuditActivityCriteria();
		List<AuditActivity> bamActivities = new ArrayList<AuditActivity>();
		List<String> caseListIds = new ArrayList<String>();
		DateRange dateRange = activityUtil.getDateRange(getSelectedSorter());
		String userIdentifier = userId;
		caseListIds.add(caseInstance.getId().toString());

		String selectedFilter = getSelectedFilter();
		if (ActivitiesFilterConstants.FILTER_MY_ACTIVITY.equals(selectedFilter)) {
			bamActivities = bamRetriever
					.getActivities(criteria.setIncludedUsername(userIdentifier)
							.setDateRange(dateRange).setCriteriaType(CriteriaType.CASE)
							.setIds(caseListIds));
			setResult(activityUtil.constructActivities(bamActivities));
		} else if (ActivitiesFilterConstants.FILTER_OTHER_ACTIVITY.equals(selectedFilter)) {
			bamActivities = bamRetriever
					.getActivities(criteria.setExcludedUsername(userIdentifier)
							.setDateRange(dateRange).setCriteriaType(CriteriaType.CASE)
							.setIds(caseListIds));
			setResult(activityUtil.constructActivities(bamActivities));
		} else if (ActivitiesFilterConstants.FILTER_ALL_ACTIVITY.equals(selectedFilter)) {
			bamActivities = bamRetriever.getActivities(criteria.setDateRange(dateRange)
					.setCriteriaType(CriteriaType.CASE).setIds(caseListIds));
			setResult(activityUtil.constructActivities(bamActivities));
		}
		notifyForLoadedData();
	}

	@Override
	public void searchCriteriaChanged() {
		getCaseActivities();
	}

	/**
	 * Getter method for result.
	 * 
	 * @return the result
	 */
	@Override
	public List<Activities> getResult() {
		return result;
	}

	/**
	 * Setter method for result.
	 * 
	 * @param result
	 *            the result to set
	 */
	@Override
	public void setResult(List<Activities> result) {
		this.result = result;
	}

}
