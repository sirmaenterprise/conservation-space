package com.sirma.itt.bam.pm.integration.project.dashboard;

import java.io.Serializable;
import java.util.ArrayList;
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
import com.sirma.itt.emf.time.DateRange;
import com.sirma.itt.emf.web.dashboard.panel.DashboardPanelController;
import com.sirma.itt.pm.domain.model.ProjectInstance;

/**
 * This class will manage recent activity panel for project dashboard.
 * 
 * @author cdimitrov
 */
@Named
@InstanceType(type = "ProjectDashboard")
@ViewAccessScoped
public class ProjectActivitiesPanel extends DashboardPanelActionBase<Activities> implements
		Serializable, DashboardPanelController {

	private static final long serialVersionUID = 7035355877293538294L;

	/** Placeholder that will be used for retrieving panel definition. */
	private static final String PROJECTDASHBOARD_DASHLET_ACTIVITIES = "projectdashboard_dashlet_activities";

	/** The current project instance from the context. */
	private ProjectInstance projectInstance;

	/** Service that will retrieve activities based on specific criteria. */
	@Inject
	private AuditActivityRetriever bamRetriever;

	/** Service that will construct activities based on search results. */
	@Inject
	private ActivityUtil activityUtil;

	private List<Activities> result;

	@Override
	protected void initializeForAsynchronousInvocation() {
		projectInstance = getDocumentContext().getInstance(ProjectInstance.class);
	}

	@Override
	public void executeDefaultFilter() {
		searchCriteriaChanged();
	}

	@Override
	public String targetDashletName() {
		return PROJECTDASHBOARD_DASHLET_ACTIVITIES;
	}

	@Override
	public Instance dashletActionsTarget() {
		return getDocumentContext().getCurrentInstance();
	}

	@Override
	public Set<String> dashletActionIds() {
		return null;
	}

	@Override
	public void updateSearchArguments(SearchArguments<Activities> searchArguments,
			SearchFilter selectedSearchFilter) {
		getProjectActivities();
	}

	@Override
	public void updateSearchContext(Context<String, Object> context) {
		getProjectActivities();
	}

	/**
	 * Getter for project instance - retrieved from the context.
	 * 
	 * @return current project instance
	 */
	public ProjectInstance getProjectInstance() {
		return projectInstance;
	}

	/**
	 * Setter for project instance
	 * 
	 * @param projectInstance
	 *            specific project instance
	 */
	public void setProjectInstance(ProjectInstance projectInstance) {
		this.projectInstance = projectInstance;
	}

	@Override
	public void initData() {
		onOpen();
	}

	@Override
	public void searchCriteriaChanged() {
		getProjectActivities();
	}

	/**
	 * This method retrieve activities based on specific filter and sorter applyed.
	 */
	private void getProjectActivities() {
		AuditActivityCriteria criteria = new AuditActivityCriteria();
		List<String> projectIds = new ArrayList<String>();
		List<AuditActivity> bamActivities = new ArrayList<AuditActivity>();
		projectIds.add((String) projectInstance.getId());
		DateRange dateRange = activityUtil.getDateRange(getSelectedSorter());
		String userIdentifier = userId;
		String selectedFilter = getSelectedFilter();
		if (ActivitiesFilterConstants.FILTER_MY_ACTIVITY.equals(selectedFilter)) {
			bamActivities = bamRetriever.getActivities(criteria.setIncludedUsername(userIdentifier)
					.setDateRange(dateRange).setCriteriaType(CriteriaType.PROJECT)
					.setIds(projectIds));
			setResult(activityUtil.constructActivities(bamActivities));
		} else if (ActivitiesFilterConstants.FILTER_OTHER_ACTIVITY.equals(selectedFilter)) {
			bamActivities = bamRetriever.getActivities(criteria.setExcludedUsername(userIdentifier)
					.setDateRange(dateRange).setCriteriaType(CriteriaType.PROJECT)
					.setIds(projectIds));
			setResult(activityUtil.constructActivities(bamActivities));
		} else if (ActivitiesFilterConstants.FILTER_ALL_ACTIVITY.equals(selectedFilter)) {
			bamActivities = bamRetriever.getActivities(criteria.setDateRange(dateRange)
					.setCriteriaType(CriteriaType.PROJECT).setIds(projectIds));
			setResult(activityUtil.constructActivities(bamActivities));
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
