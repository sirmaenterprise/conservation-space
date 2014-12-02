package com.sirma.itt.pm.web.project.dashboard;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.inject.Inject;
import javax.inject.Named;

import org.apache.myfaces.extensions.cdi.core.api.scope.conversation.ViewAccessScoped;

import com.sirma.cmf.web.caseinstance.dashboard.Colleague;
import com.sirma.cmf.web.userdashboard.DashboardPanelActionBase;
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
import com.sirma.itt.emf.web.dashboard.panel.DashboardPanelController;
import com.sirma.itt.pm.domain.model.ProjectInstance;

/**
 * This class will manage colleagues that work under project. Helps for dash-let initialization into
 * project dash-board.
 * 
 * @author cdimitrov
 */
@Named
@InstanceType(type = "ProjectDashboard")
@ViewAccessScoped
public class ProjectColleaguesPanel extends DashboardPanelActionBase<ProjectInstance> implements
		Serializable, DashboardPanelController {

	private static final long serialVersionUID = 1868272916593409658L;

	private static final String PROJECTDASHBOARD_DASHLET_COLLEAGUES = "projectdashboard_dashlet_colleagues";

	/** The resource role list. */
	private Set<Colleague> colleagues;

	@Inject
	private ResourceService resourceService;

	@Override
	public void initData() {
		onOpen();
	}

	/**
	 * Method that will generate resource roles based on the project instance. This method will be
	 * used for dash-let initialization and will be invoked for refreshing.
	 */
	public void generateRoleList() {
		log.debug("PMWeb: Executing ProjectColleaguesPanel.generateRoleList");
		ProjectInstance project = getDocumentContext().getInstance(ProjectInstance.class);

		List<ResourceRole> res = extractProjectColleagues(project);
		Iterator<ResourceRole> i = res.iterator();
		ResourceRole resourceRole = null;
		colleagues = new LinkedHashSet<Colleague>();
		Map<Resource, ResourceRole> calculated = new HashMap<>();
		while (i.hasNext()) {
			resourceRole = i.next();
			if (isResourceGroup(resourceRole.getResource())) {
				Resource resource = resourceRole.getResource();
				List<Resource> containedResources = resourceService.getContainedResources(resource);
				for (Resource current : containedResources) {
					ResourceRole highestRole = getHeighestRole(project, current, calculated);
					if ((highestRole != null) && (highestRole.getRole() != null)) {
						colleagues.add(createColleague(highestRole.getRole().getIdentifier(),
								current));
					}
				}
			} else {
				ResourceRole highestRole = getHeighestRole(project, resourceRole.getResource(),
						calculated);
				if ((highestRole != null) && (highestRole.getRole() != null)) {
					colleagues.add(createColleague(highestRole.getRole().getIdentifier(),
							resourceRole.getResource()));
				}
			}
		}
	}

	/**
	 * Gets the highest role for user.
	 * 
	 * @param project
	 *            the project
	 * @param current
	 *            the current
	 * @param calculated
	 *            the calculated
	 * @return the highest role
	 */
	private ResourceRole getHeighestRole(ProjectInstance project, Resource current,
			Map<Resource, ResourceRole> calculated) {
		ResourceRole highestRole;
		if (calculated.containsKey(current)) {
			highestRole = calculated.get(current);
		} else {
			highestRole = resourceService.getResourceRole(project, current);
			calculated.put(current, highestRole);
		}
		return highestRole;
	}

	/**
	 * Extract resources from project instance.
	 * 
	 * @param projectInstance
	 *            current project instance
	 * @return project resources
	 */
	private List<ResourceRole> extractProjectColleagues(ProjectInstance projectInstance) {
		return resourceService.getResourceRoles(projectInstance);
	}

	/**
	 * Check resource for specific group type.
	 * 
	 * @param resource
	 *            current resource
	 * @return boolean
	 */
	private boolean isResourceGroup(Resource resource) {
		return resource.getType() == ResourceType.GROUP;
	}

	/**
	 * Creates the colleague object.
	 * 
	 * @param role
	 *            the role
	 * @param current
	 *            the properties
	 * @return the colleague
	 */
	private Colleague createColleague(String role, Resource current) {
		Map<String, Serializable> props = current.getProperties();
		Colleague colleague = new Colleague(current);
		colleague.setDisplayName(current.getDisplayName());
		colleague.setRole(role);
		colleague.setJobtitle((String) props.get(ResourceProperties.JOB_TITLE));
		colleague.setAvatarPath((String) props.get(ResourceProperties.AVATAR));
		return colleague;
	}

	@Override
	public Set<String> dashletActionIds() {
		return Collections.emptySet();
	}

	@Override
	public String targetDashletName() {
		return PROJECTDASHBOARD_DASHLET_COLLEAGUES;
	}

	@Override
	protected boolean isAsynchronousLoadingSupported() {
		return false;
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
	 * Getter method for colleagues.
	 * 
	 * @return the colleagues
	 */
	public List<Colleague> getColleagues() {
		if (colleagues == null) {
			return new ArrayList<>(1);
		}
		return new ArrayList<Colleague>(colleagues);
	}

	@Override
	public void updateSearchArguments(SearchArguments<ProjectInstance> searchArguments,
			SearchFilter selectedSearchFilter) {
		// TODO Auto-generated method stub

	}

	@Override
	public void updateSearchContext(Context<String, Object> context) {
		// TODO Auto-generated method stub

	}

}
