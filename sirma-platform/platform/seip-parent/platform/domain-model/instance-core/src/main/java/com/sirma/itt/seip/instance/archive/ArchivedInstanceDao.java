package com.sirma.itt.seip.instance.archive;

import java.io.Serializable;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import com.sirma.itt.seip.Entity;
import com.sirma.itt.seip.configuration.Options;
import com.sirma.itt.seip.domain.ObjectTypes;
import com.sirma.itt.seip.domain.definition.DefinitionModel;
import com.sirma.itt.seip.domain.instance.ArchivedDataAccess;
import com.sirma.itt.seip.domain.instance.ArchivedInstance;
import com.sirma.itt.seip.domain.instance.Instance;
import com.sirma.itt.seip.instance.dao.BaseInstanceDaoImpl;
import com.sirma.itt.seip.instance.dao.InstanceLoader;
import com.sirma.itt.seip.instance.dao.InstanceType;
import com.sirma.itt.seip.instance.properties.PropertiesService;

/**
 * Instance dao for accessing archived instances.
 *
 * @author BBonev
 */
@ApplicationScoped
@InstanceType(type = ObjectTypes.ARCHIVED)
public class ArchivedInstanceDao extends BaseInstanceDaoImpl<ArchivedEntity, String, String, DefinitionModel> {

	@Inject
	@ArchivedDataAccess
	private PropertiesService archivedPropertiesService;

	@Inject
	@InstanceType(type = ObjectTypes.ARCHIVED)
	private InstanceLoader instanceLoader;

	@Override
	protected InstanceLoader getInstanceLoader() {
		return instanceLoader;
	}

	@Override
	public void synchRevisions(Instance instance, Long revision) {
		instance.setRevision(revision);
	}

	 

	@Override
	public void loadProperties(Instance instance) {
		archivedPropertiesService.loadProperties(instance);
	}

	@Override
	protected void savePropertiesOnPersistChanges(Instance instance) {
		boolean addOnly = Options.ADD_ONLY_PROPERTIES.isEnabled();
		saveProperties(instance, addOnly);
	}

	@Override
	public void saveProperties(Instance instance, boolean addOnly) {
		Options.SAVE_PROPERTIES_WITHOUT_DEFINITION.enable();
		try {
			archivedPropertiesService.saveProperties(instance, addOnly);
		} finally {
			Options.SAVE_PROPERTIES_WITHOUT_DEFINITION.disable();
		}
	}

	@Override
	protected void checkForStaleData(Instance instance) {
		// no stale data checks
	}

	@Override
	protected Class<ArchivedInstance> getInstanceClass() {
		return ArchivedInstance.class;
	}

	@Override
	protected Class<ArchivedEntity> getEntityClass() {
		return ArchivedEntity.class;
	}

	@Override
	protected void populateInstanceForModel(Instance instance, DefinitionModel model) {
		// nothing to do here
	}

	@Override
	public <S extends Serializable> void delete(Entity<S> entity) {
		if (getInstanceClass().isInstance(entity)) {
			ArchivedInstance instance = getInstanceClass().cast(entity);
			// delete the archived instance and it's properties
			getDbDao().delete(getEntityClass(), entity.getId());
			archivedPropertiesService.removeProperties(instance, instance);
		} else if (getEntityClass().isInstance(entity)) {
			ArchivedEntity archivedEntity = getEntityClass().cast(entity);
			getDbDao().delete(getEntityClass(), entity.getId());
			archivedPropertiesService.removeProperties(archivedEntity, archivedEntity);
		}
	}
}
