package com.sirma.itt.seip.testutil.mocks;

import java.io.Serializable;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import com.sirma.itt.seip.domain.definition.DefinitionModel;
import com.sirma.itt.seip.domain.instance.Instance;
import com.sirma.itt.seip.instance.dao.InstanceService;
import com.sirma.itt.seip.instance.state.Operation;

/**
 * Mock for {@link InstanceService} that allow easier unit testing.
 *
 * @author Mihail Radkov
 */
public class InstanceServiceMock implements InstanceService {

	private List<Instance> instancesToReturn;

	public InstanceServiceMock() {
	}

	public InstanceServiceMock(List<Instance> instances) {
		instancesToReturn = instances;
	}

	@Override
	public Instance createInstance(DefinitionModel definition, Instance parent) {
		return null;
	}

	@Override
	public Instance createInstance(DefinitionModel definition, Instance parent, Operation operation) {
		return null;
	}

	@Override
	public Instance save(Instance instance, Operation operation) {
		return null;
	}

	@Override
	public Instance cancel(Instance instance) {
		return null;
	}

	@Override
	public void refresh(Instance instance) {
		// Nothing to do
	}

	@Override
	public List<Instance> loadInstances(Instance owner) {
		return null;
	}

	@Override
	public Instance loadByDbId(Serializable id) {
		return null;
	}

	@Override
	public Instance load(Serializable instanceId) {
		return null;
	}

	@Override
	public <S extends Serializable> List<Instance> load(List<S> ids) {
		return null;
	}

	@Override
	public <S extends Serializable> List<Instance> loadByDbId(List<S> ids) {
		return instancesToReturn;
	}

	@Override
	public <S extends Serializable> List<Instance> load(List<S> ids, boolean allProperties) {
		return null;
	}

	@Override
	public <S extends Serializable> List<Instance> loadByDbId(List<S> ids, boolean allProperties) {
		return instancesToReturn;
	}

	@Override
	public boolean isChildAllowed(Instance owner, String type, String definitionId) {
		return false;
	}

	@Override
	public Map<String, List<DefinitionModel>> getAllowedChildren(Instance owner) {
		return null;
	}

	@Override
	public List<DefinitionModel> getAllowedChildren(Instance owner, String type) {
		return null;
	}

	@Override
	public boolean isChildAllowed(Instance owner, String type) {
		return false;
	}

	@Override
	public Instance clone(Instance instance, Operation operation) {
		return null;
	}

	@Override
	public Instance deepClone(Instance instanceToClone, Operation operation) {
		return null;
	}

	@Override
	public void delete(Instance instance, Operation operation, boolean permanent) {
		// Nothing to do
	}

	@Override
	public void attach(Instance targetInstance, Operation operation, Instance... children) {
		// Nothing to do
	}

	@Override
	public void detach(Instance sourceInstance, Operation operation, Instance... instances) {
		// Nothing to do
	}

	@Override
	public Instance publish(Instance instance, Operation operation) {
		return null;
	}

	@Override
	public Optional<Instance> loadDeleted(Serializable id) {
		return Optional.empty();
	}

}
