package com.sirma.itt.cmf.services.mock;

import java.io.Serializable;
import java.util.Arrays;
import java.util.Collection;

import javax.enterprise.inject.Produces;
import javax.inject.Inject;
import javax.inject.Singleton;

import com.sirma.itt.seip.Entity;
import com.sirma.itt.seip.Pair;
import com.sirma.itt.seip.annotation.Documentation;
import com.sirma.itt.seip.cache.CacheConfiguration;
import com.sirma.itt.seip.cache.CacheTransactionMode;
import com.sirma.itt.seip.cache.Eviction;
import com.sirma.itt.seip.cache.Expiration;
import com.sirma.itt.seip.cache.Transaction;
import com.sirma.itt.seip.cache.lookup.EntityLookupCacheContext;
import com.sirma.itt.seip.db.DbDao;
import com.sirma.itt.seip.domain.ObjectTypes;
import com.sirma.itt.seip.domain.instance.Instance;
import com.sirma.itt.seip.instance.dao.BasePrimaryIdEntityLoadCallback;
import com.sirma.itt.seip.instance.dao.BaseRelationalPersistCallback;
import com.sirma.itt.seip.instance.dao.BaseSecondaryIdEntityLoadCallback;
import com.sirma.itt.seip.instance.dao.DefaultInstanceLoader;
import com.sirma.itt.seip.instance.dao.EntityConverter;
import com.sirma.itt.seip.instance.dao.InstanceConverter;
import com.sirma.itt.seip.instance.dao.InstanceLoadCallback;
import com.sirma.itt.seip.instance.dao.InstanceLoader;
import com.sirma.itt.seip.instance.dao.InstanceType;
import com.sirma.itt.seip.instance.dao.OnInstanceLoadObserver;
import com.sirma.itt.seip.instance.properties.PropertiesService;
import com.sirma.itt.seip.mapping.ObjectMapper;
import com.sirma.itt.seip.template.TemplateEntity;
import com.sirma.itt.seip.template.TemplateInstance;

/**
 * Copy-paste class for package protected TemplateInstancePersistCallback
 */
public class TemplateLoaderProducerMock extends BaseRelationalPersistCallback {
	/** The Constant TEMPLATE_DEFINITION_CACHE. */
	@CacheConfiguration(eviction = @Eviction(strategy = "LRU", maxEntries = 100) , expiration = @Expiration(maxIdle = 600000, interval = 60000, lifespan = 600000) , transaction = @Transaction(mode = CacheTransactionMode.FULL_XA) , doc = @Documentation(""
			+ "Cache that contains template entities. The number of values is the number of the currently active templates. It's not heavily used so it could be relativly small.") )
	static final String TEMPLATE_DEFINITION_CACHE = "TEMPLATE_DEFINITION_CACHE";

	private final InstanceConverter instanceConverter;

	private final EntityConverter entityConverter;

	private final InstanceLoadCallback primaryIdLoadCallback;

	private final InstanceLoadCallback secondaryIdLoadCallback;

	/**
	 * Instantiates a new template instance persist callback.
	 *
	 * @param dbDao
	 *            the db dao
	 * @param propertiesService
	 *            the properties service
	 * @param cacheContext
	 *            the cache context
	 * @param mapper
	 *            the mapper
	 */
	@Inject
	public TemplateLoaderProducerMock(DbDao dbDao, PropertiesService propertiesService,
			EntityLookupCacheContext cacheContext, ObjectMapper mapper) {
		super(dbDao, propertiesService, cacheContext);

		primaryIdLoadCallback = new TemplatePrimaryIdLoadCallback(getDataSource());
		secondaryIdLoadCallback = new TemplateSecondaryIdLoadCallback(getDataSource());

		instanceConverter = entity -> mapper.map(entity, TemplateInstance.class);
		entityConverter = instance -> mapper.map(instance, TemplateEntity.class);
	}

	/**
	 * Produce template loader.
	 *
	 * @return the instance loader
	 */
	@Produces
	@Singleton
	@InstanceType(type = ObjectTypes.TEMPLATE)
	public InstanceLoader produceTemplateLoader() {
		return new DefaultInstanceLoader(this);
	}

	@Override
	public String getCacheName() {
		return TEMPLATE_DEFINITION_CACHE;
	}

	@Override
	public Class<? extends Entity<? extends Serializable>> getSupportedEntityClass() {
		return TemplateEntity.class;
	}

	@Override
	public EntityConverter getEntityConverter() {
		return entityConverter;
	}

	@Override
	public InstanceConverter getInstanceConverter() {
		return instanceConverter;
	}

	@Override
	public InstanceLoadCallback getPrimaryIdLoadHandler() {
		return primaryIdLoadCallback;
	}

	@Override
	public InstanceLoadCallback getSecondaryIdLoadHandler() {
		return secondaryIdLoadCallback;
	}

	@Override
	public OnInstanceLoadObserver addOnInstanceConvertedObserver(Class<? extends Instance> typeFilter,
			OnInstanceLoadObserver callable) {
		// not supported
		return null;
	}

	/**
	 * Primary id callback for template entity.
	 *
	 * @author BBonev
	 */
	private static class TemplatePrimaryIdLoadCallback extends BasePrimaryIdEntityLoadCallback {

		/**
		 * Instantiates a new template primary id load callback.
		 *
		 * @param dbDao
		 *            the db dao
		 */
		public TemplatePrimaryIdLoadCallback(DbDao dbDao) {
			super(dbDao);
		}

		@Override
		public Collection<Entity<? extends Serializable>> loadPersistedEntities(
				Collection<? extends Serializable> ids) {
			return getDbDao().fetchWithNamed(TemplateEntity.QUERY_TEMPLATES_BY_ID_KEY,
					Arrays.asList(new Pair<String, Object>("ids", ids)));
		}
	}

	/**
	 * Secondary id callback for template entity.
	 *
	 * @author BBonev
	 */
	private static class TemplateSecondaryIdLoadCallback extends BaseSecondaryIdEntityLoadCallback {

		/**
		 * Instantiates a new template secondary id load callback.
		 *
		 * @param dbDao
		 *            the db dao
		 */
		public TemplateSecondaryIdLoadCallback(DbDao dbDao) {
			super(dbDao);
		}

		@Override
		public Serializable getId(Entity<? extends Serializable> entity) {
			return ((TemplateEntity) entity).getTemplateId();
		}

		@Override
		public Object fetchByKey(Serializable key) {
			return getDbDao().fetchWithNamed(TemplateEntity.QUERY_TEMPLATE_BY_TEMPLATE_OR_DMS_ID_KEY,
					Arrays.asList(new Pair<String, Object>("templateId", key)));
		}

		@Override
		public Collection<Entity<? extends Serializable>> loadPersistedEntities(
				Collection<? extends Serializable> ids) {
			return getDbDao().fetchWithNamed(TemplateEntity.QUERY_TEMPLATES_BY_TEMPLATE_IDS_KEY,
					Arrays.asList(new Pair<String, Object>("templateId", ids)));
		}

		@Override
		public Entity<? extends Serializable> createEntityFromId(Serializable id) {
			TemplateEntity entity = new TemplateEntity();
			entity.setTemplateId((String) id);
			return entity;
		}

	}
}
