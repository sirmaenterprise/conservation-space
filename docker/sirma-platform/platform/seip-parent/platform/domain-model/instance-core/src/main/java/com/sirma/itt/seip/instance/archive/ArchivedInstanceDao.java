package com.sirma.itt.seip.instance.archive;

import java.io.Serializable;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import com.sirma.itt.seip.Entity;
import com.sirma.itt.seip.domain.ObjectTypes;
import com.sirma.itt.seip.domain.definition.DefinitionModel;
import com.sirma.itt.seip.domain.instance.ArchivedInstance;
import com.sirma.itt.seip.domain.instance.Instance;
import com.sirma.itt.seip.instance.archive.properties.ArchivedPropertiesDao;
import com.sirma.itt.seip.instance.dao.BaseInstanceDaoImpl;
import com.sirma.itt.seip.instance.dao.InstanceLoader;
import com.sirma.itt.seip.instance.dao.InstanceType;

/**
 * Instance dao for accessing archived instances.
 *
 * @author BBonev
 */
@ApplicationScoped
@InstanceType(type = ObjectTypes.ARCHIVED)
public class ArchivedInstanceDao extends BaseInstanceDaoImpl<ArchivedEntity, String, String, DefinitionModel> {

	@Inject
	private ArchivedPropertiesDao archivedPropertiesDao;

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
		archivedPropertiesDao.load(instance);
	}

	@Override
	protected void savePropertiesOnPersistChanges(Instance instance) {
		saveProperties(instance, false);
	}

	@Override
	public void saveProperties(Instance instance, boolean addOnly) {
		archivedPropertiesDao.persist(instance);
	}

	@Override
	protected <S extends Serializable> void deleteProperties(Entity<S> entity) {
		archivedPropertiesDao.delete(entity.getId());
	}

	@Override
	protected void checkForStaleData(Instance instance) {
		// no stale data checks
	}

	@Override
	@SuppressWarnings("unchecked")
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
		getDbDao().delete(getEntityClass(), entity.getId());
		deleteProperties(entity);
	}
}