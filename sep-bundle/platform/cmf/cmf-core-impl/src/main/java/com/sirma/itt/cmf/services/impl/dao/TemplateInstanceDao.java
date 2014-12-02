package com.sirma.itt.cmf.services.impl.dao;

import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Set;

import javax.enterprise.context.ApplicationScoped;

import com.sirma.itt.cmf.beans.definitions.TemplateDefinition;
import com.sirma.itt.cmf.beans.entity.TemplateEntity;
import com.sirma.itt.cmf.db.DbQueryTemplates;
import com.sirma.itt.cmf.domain.ObjectTypesCmf;
import com.sirma.itt.commons.utils.string.StringUtils;
import com.sirma.itt.emf.cache.CacheConfiguration;
import com.sirma.itt.emf.cache.CacheTransactionMode;
import com.sirma.itt.emf.cache.Eviction;
import com.sirma.itt.emf.cache.Expiration;
import com.sirma.itt.emf.cache.lookup.BaseEntityLookupDao;
import com.sirma.itt.emf.cache.lookup.EntityLookupCache.EntityLookupCallbackDAOAdaptor;
import com.sirma.itt.emf.configuration.RuntimeConfiguration;
import com.sirma.itt.emf.configuration.RuntimeConfigurationProperties;
import com.sirma.itt.emf.db.DbDao;
import com.sirma.itt.emf.definition.model.PropertyDefinition;
import com.sirma.itt.emf.domain.Pair;
import com.sirma.itt.emf.domain.model.Entity;
import com.sirma.itt.emf.domain.model.Node;
import com.sirma.itt.emf.instance.dao.BaseInstanceDaoImpl;
import com.sirma.itt.emf.instance.dao.InstanceType;
import com.sirma.itt.emf.instance.model.Instance;
import com.sirma.itt.emf.properties.DefaultProperties;
import com.sirma.itt.emf.template.TemplateInstance;
import com.sirma.itt.emf.template.TemplateProperties;
import com.sirma.itt.emf.util.Documentation;

/**
 * Instance dao implementation for template instance
 * 
 * @author BBonev
 */
@ApplicationScoped
@InstanceType(type = ObjectTypesCmf.TEMPLATE)
public class TemplateInstanceDao extends
		BaseInstanceDaoImpl<TemplateInstance, TemplateEntity, Long, String, TemplateDefinition> {

	/** The Constant TEMPLATE_DEFINITION_CACHE. */
	@CacheConfiguration(container = "cmf", eviction = @Eviction(strategy = "LRU", maxEntries = 100), expiration = @Expiration(maxIdle = 600000, interval = 60000, lifespan = 600000), transaction = CacheTransactionMode.FULL_XA, doc = @Documentation(""
			+ "Cache that contains template entities. The number of values is the number of the currently active templates. It's not heavily used so it could be relativly small."))
	private static final String TEMPLATE_DEFINITION_CACHE = "TEMPLATE_DEFINITION_CACHE";

	@Override
	protected void onInstanceUpdated(TemplateInstance instance) {
		// ensure the content will not be saved
		instance.getProperties().remove(TemplateProperties.CONTENT);
		instance.getProperties().remove(TemplateProperties.IS_CONTENT_LOADED);
	}

	@Override
	protected void updateModifierInfo(TemplateInstance instance, Date currentDate) {
		// not needed to update it
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void synchRevisions(TemplateInstance instance, Long revision) {
		instance.setRevision(revision);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected String getOwningInstanceQuery() {
		return null;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected void populateInstanceForModel(TemplateInstance instance, TemplateDefinition model) {
		populateProperties(instance, model);

		// ensure the default properties are set
		Node node = model.getChild(DefaultProperties.TYPE);
		if ((node instanceof PropertyDefinition)
				&& StringUtils.isNotNullOrEmpty(((PropertyDefinition) node).getDefaultValue())) {
			instance.setGroupId(((PropertyDefinition) node).getDefaultValue());
		} else {
			instance.setGroupId(TemplateProperties.DEFAULT_GROUP);
		}
		instance.setIdentifier(model.getIdentifier());
		node = model.getChild(TemplateProperties.PRIMARY);
		if (node instanceof PropertyDefinition) {
			String value = ((PropertyDefinition) node).getDefaultValue();
			instance.setPrimary(Boolean.valueOf(value));
		} else {
			instance.setPrimary(Boolean.FALSE);
		}
		node = model.getChild(TemplateProperties.PUBLIC);

		instance.setPrimary(Boolean.TRUE);
		if (node instanceof PropertyDefinition) {
			String value = ((PropertyDefinition) node).getDefaultValue();
			if (StringUtils.isNotNullOrEmpty(value)) {
				instance.setPrimary(Boolean.valueOf(value));
			}
		}
	}

	@Override
	public void saveProperties(TemplateInstance instance, boolean addOnly) {
		RuntimeConfiguration.setConfiguration(
				RuntimeConfigurationProperties.SAVE_PROPERTIES_WITHOUT_DEFINITION, Boolean.TRUE);
		try {
			super.saveProperties(instance, addOnly);
		} finally {
			RuntimeConfiguration
					.clearConfiguration(RuntimeConfigurationProperties.SAVE_PROPERTIES_WITHOUT_DEFINITION);
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected TemplateEntity createEntity(String dmsId) {
		TemplateEntity entity = new TemplateEntity();
		entity.setTemplateId(dmsId);
		return entity;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected Class<TemplateInstance> getInstanceClass() {
		return TemplateInstance.class;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected Class<TemplateEntity> getEntityClass() {
		return TemplateEntity.class;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected Class<TemplateDefinition> getDefinitionClass() {
		return TemplateDefinition.class;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected String getCacheEntityName() {
		return TEMPLATE_DEFINITION_CACHE;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected EntityLookupCallbackDAOAdaptor<Long, TemplateEntity, String> getEntityCacheProvider() {
		return new TemplateEntityLookup();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected <E extends Entity<Long>> List<E> findEntities(Set<String> ids) {
		return dbDao.fetchWithNamed(DbQueryTemplates.QUERY_TEMPLATES_BY_TEMPLATE_IDS_KEY,
				Arrays.asList(new Pair<String, Object>("templateId", ids)));
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected <E extends Entity<Long>> List<E> findEntitiesByPrimaryKey(Set<Long> ids) {
		return dbDao.fetchWithNamed(DbQueryTemplates.QUERY_TEMPLATES_BY_ID_KEY,
				Arrays.asList(new Pair<String, Object>("ids", ids)));
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected <E extends Instance> String getSecondaryKey(E instance) {
		return instance.getIdentifier();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected <E extends Entity<?>> Long getPrimaryKey(E entity) {
		return (Long) entity.getId();
	}

	/**
	 * Entity look cache callback for working with template entities.
	 * 
	 * @author BBonev
	 */
	public class TemplateEntityLookup extends BaseEntityLookupDao<TemplateEntity, String, Long> {

		/**
		 * {@inheritDoc}
		 */
		@Override
		protected Class<TemplateEntity> getEntityClass() {
			return TemplateEntity.class;
		}

		/**
		 * {@inheritDoc}
		 */
		@Override
		protected DbDao getDbDao() {
			return dbDao;
		}

		/**
		 * {@inheritDoc}
		 */
		@Override
		protected String getValueKeyInternal(TemplateEntity value) {
			return value.getTemplateId();
		}

		/**
		 * {@inheritDoc}
		 */
		@Override
		protected List<TemplateEntity> fetchEntityByValue(String key) {
			return dbDao.fetchWithNamed(DbQueryTemplates.QUERY_TEMPLATE_BY_TEMPLATE_OR_DMS_ID_KEY,
					Arrays.asList(new Pair<String, Object>("templateId", key)));
		}
	}

}
