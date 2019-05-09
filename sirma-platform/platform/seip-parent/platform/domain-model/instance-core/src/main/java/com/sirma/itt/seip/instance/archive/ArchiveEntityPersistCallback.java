package com.sirma.itt.seip.instance.archive;

import java.io.Serializable;
import java.util.Arrays;
import java.util.Collection;

import javax.enterprise.inject.Produces;
import javax.inject.Inject;
import javax.inject.Singleton;

import com.sirma.itt.seip.Entity;
import com.sirma.itt.seip.Pair;
import com.sirma.itt.seip.cache.lookup.EntityLookupCache;
import com.sirma.itt.seip.cache.lookup.EntityLookupCacheContext;
import com.sirma.itt.seip.db.DbDao;
import com.sirma.itt.seip.domain.ObjectTypes;
import com.sirma.itt.seip.domain.instance.ArchivedDataAccess;
import com.sirma.itt.seip.domain.instance.ArchivedInstance;
import com.sirma.itt.seip.domain.instance.Instance;
import com.sirma.itt.seip.instance.dao.BasePrimaryIdEntityLoadCallback;
import com.sirma.itt.seip.instance.dao.BaseRelationalPersistCallback;
import com.sirma.itt.seip.instance.dao.DefaultInstanceLoader;
import com.sirma.itt.seip.instance.dao.EntityConverter;
import com.sirma.itt.seip.instance.dao.EntityLookupDao;
import com.sirma.itt.seip.instance.dao.InstanceConverter;
import com.sirma.itt.seip.instance.dao.InstanceLoadCallback;
import com.sirma.itt.seip.instance.dao.InstanceLoader;
import com.sirma.itt.seip.instance.dao.InstanceType;
import com.sirma.itt.seip.instance.dao.NoOpInstanceLoadCallback;
import com.sirma.itt.seip.instance.dao.OnInstanceLoadObserver;
import com.sirma.itt.seip.instance.properties.PropertiesService;
import com.sirma.itt.seip.mapping.ObjectMapper;

/**
 * Archive entity/instance callback.
 *
 * @author BBonev
 */
@Singleton
public class ArchiveEntityPersistCallback extends BaseRelationalPersistCallback {

	private final InstanceConverter instanceConverter;
	private final EntityConverter entityConverter;
	private static final InstanceLoadCallback SECONDARY_ID_LOAD_CALLBACK = NoOpInstanceLoadCallback.instance();
	private final InstanceLoadCallback primaryIdLoadCallback;
	@SuppressWarnings("rawtypes")
	private EntityLookupCache noCache;

	/**
	 * Instantiates a new archive entity persist callback.
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
	public ArchiveEntityPersistCallback(DbDao dbDao, @ArchivedDataAccess PropertiesService propertiesService,
			EntityLookupCacheContext cacheContext, ObjectMapper mapper) {
		super(dbDao, propertiesService, cacheContext);

		noCache = new EntityLookupCache<>(new EntityLookupDao(this).enableSecondaryKeyManagement());

		primaryIdLoadCallback = new BasePrimaryIdEntityLoadCallback(dbDao) {
			@Override
			public Collection<Entity<? extends Serializable>> loadPersistedEntities(
					Collection<? extends Serializable> ids) {
				return getDbDao().fetchWithNamed(ArchivedEntity.QUERY_ARCHIVED_ENTITIES_BY_REFERENCE_ID_KEY,
						Arrays.asList(new Pair<String, Object>("id", ids)));
			}
		};
		instanceConverter = entity -> mapper.map(entity, ArchivedInstance.class);
		entityConverter = instance -> mapper.map(instance, ArchivedEntity.class);
	}

	/**
	 * Creates the loader for archived instances
	 *
	 * @return the instance loader
	 */
	@Produces
	@Singleton
	@InstanceType(type = ObjectTypes.ARCHIVED)
	public InstanceLoader createLoader() {
		return new DefaultInstanceLoader(this);
	}

	@Override
	public String getCacheName() {
		return null;
	}

	@Override
	public Class<? extends Entity<? extends Serializable>> getSupportedEntityClass() {
		return ArchivedEntity.class;
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
		return SECONDARY_ID_LOAD_CALLBACK;
	}

	@Override
	@SuppressWarnings("unchecked")
	public EntityLookupCache<Serializable, Entity<? extends Serializable>, Serializable> getCache() {
		return noCache;
	}

	@Override
	public EntityConverter getEntityConverter() {
		return entityConverter;
	}

	@Override
	public OnInstanceLoadObserver addOnInstanceConvertedObserver(Class<? extends Instance> typeFilter,
			OnInstanceLoadObserver callable) {
		// no need to implement if
		return null;
	}

}
