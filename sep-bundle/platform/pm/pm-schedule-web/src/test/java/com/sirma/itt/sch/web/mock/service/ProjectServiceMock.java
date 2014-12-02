package com.sirma.itt.sch.web.mock.service;

import java.io.Serializable;
import java.util.List;
import java.util.Map;

import javax.enterprise.inject.Alternative;

import com.sirma.itt.emf.domain.model.DefinitionModel;
import com.sirma.itt.emf.instance.model.Instance;
import com.sirma.itt.emf.state.operation.Operation;
import com.sirma.itt.pm.domain.definitions.ProjectDefinition;
import com.sirma.itt.pm.domain.model.ProjectInstance;
import com.sirma.itt.pm.services.ProjectService;

/**
 * The Class ProjectServiceMock.
 *
 * @author svelikov
 */
@Alternative
public class ProjectServiceMock implements ProjectService {

	@Override
	public Class<ProjectDefinition> getInstanceDefinitionClass() {
		return null;
	}

	@Override
	public ProjectInstance createInstance(ProjectDefinition definition, Instance parent) {
		return null;
	}

	@Override
	public ProjectInstance createInstance(ProjectDefinition definition, Instance parent,
			Operation operation) {
		return null;
	}

	@Override
	public ProjectInstance save(ProjectInstance instance, Operation operation) {
		return null;
	}

	@Override
	public List<ProjectInstance> loadInstances(Instance owner) {
		return null;
	}

	@Override
	public ProjectInstance loadByDbId(Serializable id) {
		return null;
	}

	@Override
	public ProjectInstance load(Serializable instanceId) {
		return null;
	}

	@Override
	public <S extends Serializable> List<ProjectInstance> load(List<S> ids) {
		return null;
	}

	@Override
	public <S extends Serializable> List<ProjectInstance> load(List<S> ids, boolean allProperties) {
		return null;
	}

	@Override
	public Map<String, List<DefinitionModel>> getAllowedChildren(ProjectInstance owner) {
		return null;
	}

	@Override
	public void refresh(ProjectInstance instance) {


	}

	@Override
	public List<DefinitionModel> getAllowedChildren(ProjectInstance owner, String type) {

		return null;
	}

	@Override
	public boolean isChildAllowed(ProjectInstance owner, String type) {

		return false;
	}

	@Override
	public ProjectInstance cancel(ProjectInstance instance) {

		return null;
	}

	@Override
	public ProjectInstance clone(ProjectInstance instance, Operation operation) {

		return null;
	}

	@Override
	public <S extends Serializable> List<ProjectInstance> loadByDbId(List<S> ids) {

		return null;
	}

	@Override
	public <S extends Serializable> List<ProjectInstance> loadByDbId(List<S> ids,
			boolean allProperties) {

		return null;
	}

	@Override
	public void delete(ProjectInstance instance, Operation operation,
			boolean permanent) {

	}

	@Override
	public void attach(ProjectInstance targetInstance, Operation operation, Instance... children) {
		// TODO Auto-generated method stub

	}

	@Override
	public void detach(ProjectInstance sourceInstance, Operation operation, Instance... instances) {
		// TODO Auto-generated method stub

	}

}
