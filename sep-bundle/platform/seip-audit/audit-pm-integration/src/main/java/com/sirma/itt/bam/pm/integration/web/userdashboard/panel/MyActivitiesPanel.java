package com.sirma.itt.bam.pm.integration.web.userdashboard.panel;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import javax.inject.Inject;
import javax.inject.Named;

import org.apache.myfaces.extensions.cdi.core.api.scope.conversation.ViewAccessScoped;

import com.sirma.bam.cmf.integration.caseinstance.dashboard.Activities;
import com.sirma.bam.cmf.integration.caseinstance.dashboard.ActivityUtil;
import com.sirma.bam.cmf.integration.userdashboard.filter.ActivitiesFilterConstants;
import com.sirma.cmf.web.userdashboard.DashboardPanelActionBase;
import com.sirma.itt.emf.audit.activity.AuditActivity;
import com.sirma.itt.emf.audit.activity.AuditActivityCriteria;
import com.sirma.itt.emf.audit.activity.AuditActivityRetriever;
import com.sirma.itt.emf.audit.activity.CriteriaType;
import com.sirma.itt.emf.domain.Context;
import com.sirma.itt.emf.instance.dao.InstanceType;
import com.sirma.itt.emf.instance.model.Instance;
import com.sirma.itt.emf.search.model.SearchArguments;
import com.sirma.itt.emf.search.model.SearchFilter;
import com.sirma.itt.emf.search.model.SearchFilterConfig;
import com.sirma.itt.emf.search.model.SearchInstance;
import com.sirma.itt.emf.time.DateRange;
import com.sirma.itt.emf.web.dashboard.panel.DashboardPanelController;

/**
 * This class will manage recent activity panel for personal dashboard.
 * 
 * @author cdimitrov
 */
@Named
@InstanceType(type = "UserDashboard")
@ViewAccessScoped
public class MyActivitiesPanel extends DashboardPanelActionBase<Activities> implements
		Serializable, DashboardPanelController {

	private static final long serialVersionUID = 8788735034206079795L;

	/** Placeholder that will be used for retrieving panel definition. */
	private static final String USERDASHBOARD_DASHLET_ACTIVITY = "userdashboard_dashlet_activity";

	/** Service that will retrieve activities based on specific criteria. */
	@Inject
	private AuditActivityRetriever bamRetriever;

	/** Service that will construct activities based on search results. */
	@Inject
	private ActivityUtil activityUtil;

	private List<Activities> result;

	@Override
	public void initData() {
		onOpen();
	}

	@Override
	public void updateSearchArguments(SearchArguments<Activities> searchArguments,
			SearchFilter selectedSearchFilter) {
		getProjectActivities();
	}

	@Override
	public void updateSearchContext(Context<String, Object> context) {
		// TODO Auto-generated method stub
	}

	@Override
	public Set<String> dashletActionIds() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String targetDashletName() {
		return USERDASHBOARD_DASHLET_ACTIVITY;
	}

	@Override
	public Instance dashletActionsTarget() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void executeDefaultFilter() {
		searchCriteriaChanged();
	}

	@Override
	public void searchCriteriaChanged() {
		getProjectActivities();
	}

	/**
	 * Retrieve all project by specific search arguments and extract their identifiers.
	 * 
	 * @return list with project identifiers
	 */
	private List<String> getProjectIds() {
		SearchFilter searchFilter = null;
		SearchFilterConfig config = searchService.getFilterConfiguration(
				USERDASHBOARD_DASHLET_PROJECTS, SearchInstance.class);
		for (SearchFilter filter : config.getFilters()) {
			if (filter.getValue().equals("iAmAssignedTo")) {
				searchFilter = filter;
				break;
			}
		}
		if (searchFilter == null) {
			return Collections.emptyList();
		}

		Context<String, Object> searchContext = getSearchContext();
		searchContext.remove("sorter");
		SearchArguments<Instance> arguments = searchService.buildSearchArguments(searchFilter,
				SearchInstance.class, searchContext);

		searchService.search(SearchInstance.class, arguments);

		List<String> projectListIds = new ArrayList<String>(arguments.getResult().size());
		for (Instance instance : arguments.getResult()) {
			projectListIds.add((String) instance.getId());
		}
		return projectListIds;
	}

	/**
	 * This method will retrieve and store activities based on the projects.
	 */
	private void getProjectActivities() {
		List<String> projectIdentifiers = getProjectIds();
		AuditActivityCriteria criteria = new AuditActivityCriteria();
		List<AuditActivity> activityRetriever = new ArrayList<AuditActivity>();
		DateRange dateRange = activityUtil.getDateRange(getSelectedSorter());
		String userIdentifier = userId;

		String selectedFilter = getSelectedFilter();
		if (ActivitiesFilterConstants.FILTER_MY_ACTIVITY.equals(selectedFilter)) {
			activityRetriever = bamRetriever.getActivities(criteria
					.setIncludedUsername(userIdentifier).setDateRange(dateRange)
					.setCriteriaType(CriteriaType.PROJECT).setIds(projectIdentifiers));
			setResult(activityUtil.constructActivities(activityRetriever));
		} else if (ActivitiesFilterConstants.FILTER_OTHER_ACTIVITY.equals(selectedFilter)) {
			activityRetriever = bamRetriever.getActivities(criteria
					.setExcludedUsername(userIdentifier).setDateRange(dateRange)
					.setCriteriaType(CriteriaType.PROJECT).setIds(projectIdentifiers));
			setResult(activityUtil.constructActivities(activityRetriever));
		} else if (ActivitiesFilterConstants.FILTER_ALL_ACTIVITY.equals(selectedFilter)) {
			activityRetriever = bamRetriever.getActivities(criteria.setDateRange(dateRange)
					.setCriteriaType(CriteriaType.PROJECT).setIds(projectIdentifiers));
			setResult(activityUtil.constructActivities(activityRetriever));
		}

		notifyForLoadedData();
	}

	/**
	 * Getter method for result.
	 * 
	 * @return the result
	 */
	@Override
	public List<Activities> getResult() {
		waitForDataToLoad();
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
