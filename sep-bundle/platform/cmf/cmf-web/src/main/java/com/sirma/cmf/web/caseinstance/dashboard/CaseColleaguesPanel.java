package com.sirma.cmf.web.caseinstance.dashboard;

import java.io.Serializable;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import javax.inject.Inject;
import javax.inject.Named;

import org.apache.myfaces.extensions.cdi.core.api.scope.conversation.ViewAccessScoped;

import com.sirma.cmf.web.userdashboard.DashboardPanelActionBase;
import com.sirma.itt.cmf.beans.model.CaseInstance;
import com.sirma.itt.cmf.beans.model.TaskState;
import com.sirma.itt.cmf.constants.CaseProperties;
import com.sirma.itt.cmf.services.TaskService;
import com.sirma.itt.emf.domain.Context;
import com.sirma.itt.emf.instance.dao.InstanceType;
import com.sirma.itt.emf.instance.model.Instance;
import com.sirma.itt.emf.resources.ResourceProperties;
import com.sirma.itt.emf.resources.ResourceService;
import com.sirma.itt.emf.resources.ResourceType;
import com.sirma.itt.emf.resources.model.Resource;
import com.sirma.itt.emf.resources.model.ResourceRole;
import com.sirma.itt.emf.search.model.SearchArguments;
import com.sirma.itt.emf.search.model.SearchFilter;
import com.sirma.itt.emf.security.AuthorityService;
import com.sirma.itt.emf.security.model.Role;
import com.sirma.itt.emf.web.dashboard.panel.DashboardPanelController;

/**
 * Action bean backing the colleagues dashlet.
 * 
 * @author cdimitrov
 */
@Named
@InstanceType(type = "CaseDashboard")
@ViewAccessScoped
public class CaseColleaguesPanel extends DashboardPanelActionBase<CaseInstance> implements
		Serializable, DashboardPanelController {

	private static final long serialVersionUID = 2865159017672853487L;

	private static final String CASEDASHBOARD_DASHLET_COLLEAGUES = "casedashboard_dashlet_colleagues";

	/** The resource role list. */
	private List<Colleague> colleagues;

	/** The resource service. */
	@Inject
	private ResourceService resourceService;

	@Inject
	private TaskService taskService;

	@Inject
	private AuthorityService authorityService;

	@Override
	public void initData() {
		onOpen();
	}

	@Override
	protected boolean isAsynchronousLoadingSupported() {
		return false;
	}

	/**
	 * Method that will generate resource roles based on the case instance. This method will be used
	 * for dash-let initialization and will be invoked for refreshing.
	 */
	public void generateRoleList() {
		CaseInstance caseInstance = getDocumentContext().getInstance(CaseInstance.class);
		colleagues = new LinkedList<>();
		extractCaseColleagues(caseInstance);
		Collections.sort(colleagues);
	}

	/**
	 * Extract resources from case instance. Populates the {@link #colleagues} collection
	 * 
	 * @param caseInstance
	 *            current case instance
	 */
	private void extractCaseColleagues(CaseInstance caseInstance) {
		Set<String> finalSet = new TreeSet<>();
		Set<String> activeUsers = taskService.getUsersWithTasksForInstance(caseInstance,
				TaskState.IN_PROGRESS);
		finalSet.addAll(activeUsers);
		Set<String> inactiveUsers = taskService.getUsersWithTasksForInstance(caseInstance,
				TaskState.COMPLETED);
		finalSet.addAll(inactiveUsers);
		// cast it instead of toString
		finalSet.add((String) caseInstance.getProperties().get(CaseProperties.CREATED_BY));
		Set<String> processed = new TreeSet<>();
		for (String nextUser : finalSet) {
			if (!processed.contains(nextUser)) {
				ResourceRole resourceRole = new ResourceRole();
				Resource nextResource = resourceService.getResource(nextUser, ResourceType.USER);
				Role userRole = authorityService.getUserRole(caseInstance, nextResource);
				resourceRole.setRole(userRole.getRoleId());
				resourceRole.setResource(nextResource);
				colleagues.add(createColleague(resourceRole));
				processed.add(nextUser);
			}
		}
		processed = null;
	}

	@Override
	public Set<String> dashletActionIds() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String targetDashletName() {
		return CASEDASHBOARD_DASHLET_COLLEAGUES;
	}

	@Override
	public Instance dashletActionsTarget() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void executeDefaultFilter() {
		generateRoleList();
	}

	/**
	 * Creates the colleague object and populates the properties.
	 * 
	 * @param resourceRole
	 *            the role with relevant data
	 * @return the created object colleague
	 */
	private Colleague createColleague(ResourceRole resourceRole) {
		Resource resource = resourceRole.getResource();
		Colleague colleague = new Colleague(resource);
		colleague.setDisplayName(resource.getDisplayName());
		colleague.setRole(resourceRole.getRole().getIdentifier());
		colleague.setJobtitle((String) resource.getProperties().get(ResourceProperties.JOB_TITLE));
		colleague.setAvatarPath((String) resource.getProperties().get(ResourceProperties.AVATAR));
		return colleague;
	}

	/**
	 * Getter method for retrieving all case colleagues.
	 * 
	 * @return list with colleagues
	 */
	public List<Colleague> getColleagues() {
		return colleagues;
	}

	/**
	 * Setter method for storing case colleague in list.
	 * 
	 * @param colleagues
	 *            colleague list
	 */
	public void setColleagues(List<Colleague> colleagues) {
		this.colleagues = colleagues;
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
