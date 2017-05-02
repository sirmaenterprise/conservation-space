package com.sirma.itt.seip.instance.dao;

import java.io.Serializable;
import java.util.Collection;
import java.util.Collections;

import com.sirma.itt.seip.Entity;
import com.sirma.itt.seip.Pair;
import com.sirma.itt.seip.db.DbDao;
import com.sirma.itt.seip.domain.DmsAware;
import com.sirma.itt.seip.model.InstanceEntity;

/**
 * Callback implementation that handles search by DMS id.
 *
 * @author BBonev
 */
public class DmsIdInstanceEntityLoadCallback extends BaseSecondaryIdEntityLoadCallback {

	/**
	 * Instantiates a new dms id instance load callback.
	 *
	 * @param dbDao
	 *            the db dao
	 */
	public DmsIdInstanceEntityLoadCallback(DbDao dbDao) {
		super(dbDao);
	}

	@Override
	public Serializable getId(Entity<? extends Serializable> entity) {
		if (entity instanceof DmsAware) {
			return ((DmsAware) entity).getDmsId();
		}
		return null;
	}

	@Override
	public Object fetchByKey(Serializable key) {
		return fetchByKeys(Collections.singletonList(key));
	}

	@Override
	public Collection<Entity<? extends Serializable>> loadPersistedEntities(Collection<? extends Serializable> ids) {
		return fetchByKeys(ids);
	}

	/**
	 * Fetch by keys.
	 *
	 * @param ids
	 *            the ids
	 * @return the collection
	 */
	private Collection<Entity<? extends Serializable>> fetchByKeys(Collection<?> ids) {
		return getDbDao().fetchWithNamed(InstanceEntity.QUERY_INSTANCE_ENTITIES_BY_DMS_ID_KEY,
				Collections.singletonList(new Pair<String, Object>("dmsId", ids)));
	}

	@Override
	public Entity<? extends Serializable> createEntityFromId(Serializable id) {
		return new DmsAwareProxy(id);
	}

	/**
	 * Dummy query object for the cache to match the interfaces
	 *
	 * @author BBonev
	 */
	private static class DmsAwareProxy implements Entity<Serializable>, DmsAware {

		private Serializable dmsId;
		private Serializable id;

		/**
		 * Instantiates a new dms aware proxy.
		 *
		 * @param dmsId
		 *            the dms id
		 */
		public DmsAwareProxy(Serializable dmsId) {
			this.dmsId = dmsId;
		}

		@Override
		public String getDmsId() {
			return (String) dmsId;
		}

		@Override
		public void setDmsId(String dmsId) {
			this.dmsId = dmsId;
		}

		@Override
		public Serializable getId() {
			return id;
		}

		@Override
		public void setId(Serializable id) {
			this.id = id;
		}
	}

}