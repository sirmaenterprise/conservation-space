package com.sirma.itt.pm.web.project;

import java.io.Serializable;

import javax.inject.Inject;
import javax.inject.Named;

import org.apache.myfaces.extensions.cdi.core.api.scope.conversation.ViewAccessScoped;

import com.sirma.cmf.web.constants.NavigationConstants;
import com.sirma.cmf.web.form.FormViewMode;
import com.sirma.cmf.web.instance.landingpage.InstanceLandingPage;
import com.sirma.itt.emf.db.SequenceEntityGenerator;
import com.sirma.itt.emf.instance.dao.InstanceService;
import com.sirma.itt.emf.instance.dao.InstanceType;
import com.sirma.itt.emf.instance.model.Instance;
import com.sirma.itt.emf.instance.model.InstanceReference;
import com.sirma.itt.pm.domain.ObjectTypesPm;
import com.sirma.itt.pm.domain.definitions.ProjectDefinition;
import com.sirma.itt.pm.domain.model.ProjectInstance;
import com.sirma.itt.pm.event.ProjectOpenEvent;
import com.sirma.itt.pm.services.ProjectService;
import com.sirma.itt.pm.web.constants.PmNavigationConstants;

/**
 * Backing bean for project form. handles project definitions loading and selecting.
 * 
 * @author svelikov
 */
@Named
@ViewAccessScoped
@InstanceType(type = ObjectTypesPm.PROJECT)
public class ProjectLandingPage extends InstanceLandingPage<ProjectInstance, ProjectDefinition>
		implements Serializable {

	private static final long serialVersionUID = 8214447805783119712L;

	@Inject
	private ProjectService projectService;

	@Override
	public Class<ProjectDefinition> getInstanceDefinitionClass() {
		return ProjectDefinition.class;
	}

	@Override
	public ProjectInstance getNewInstance(ProjectDefinition selectedDefinition, Instance context) {
		return projectService.createInstance(selectedDefinition, null);
	}

	@Override
	public String open(ProjectInstance instance) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Class<ProjectInstance> getInstanceClass() {
		return ProjectInstance.class;
	}

	@Override
	public InstanceReference getParentReference() {
		return null;
	}

	@Override
	public String saveInstance(ProjectInstance instance) {
		if (instance != null) {
			projectService.save(instance, createOperation());
			ProjectDefinition definition = (ProjectDefinition) dictionaryService
					.getInstanceDefinition(instance);
			getDocumentContext()
					.populateContext(instance, getInstanceDefinitionClass(), definition);
			getDocumentContext().addContextInstance(instance);
			initializeRoot(instance);
			// going to dashboard
			eventService.fire(new ProjectOpenEvent(instance));
			return PmNavigationConstants.PROJECT;
		}
		return PmNavigationConstants.RELOAD_PAGE;
	}

	@Override
	public String cancelEditInstance(ProjectInstance instance) {
		reloadProjectInstance();
		return NavigationConstants.BACKWARD;
	}

	@Override
	public void onExistingInstanceInitPage(ProjectInstance instance) {
		// TODO Auto-generated method stub
	}

	@Override
	public void onNewInstanceInitPage(ProjectInstance instance) {
		// TODO Auto-generated method stub
	}

	@Override
	public FormViewMode getFormViewModeExternal(ProjectInstance instance) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String getNavigationString() {
		return getInstanceClass().getSimpleName().toLowerCase();
	}

	/**
	 * Reload project instance.
	 */
	private void reloadProjectInstance() {
		ProjectInstance instance = getDocumentContext().getInstance(ProjectInstance.class);
		if ((instance != null) && SequenceEntityGenerator.isPersisted(instance)) {
			getDocumentContext().addInstance(projectService.loadByDbId(instance.getId()));
		}
	}

	@Override
	protected String getDefinitionFilterType() {
		return ObjectTypesPm.PROJECT;
	}

	@Override
	protected InstanceService<ProjectInstance, ProjectDefinition> getInstanceService() {
		return projectService;
	}

}
