package com.sirma.itt.seip.instance.dao;

import java.io.Serializable;
import java.util.Collection;
import java.util.Collections;

import com.sirma.itt.seip.Entity;
import com.sirma.itt.seip.Pair;
import com.sirma.itt.seip.db.DbDao;
import com.sirma.itt.seip.model.InstanceEntity;

/**
 * Callback implementation that handle search by primary db id.
 *
 * @author BBonev
 */
public class DbIdInstanceEntityLoadCallback extends BasePrimaryIdEntityLoadCallback {

	/**
	 * Instantiates a new db id instance load callback.
	 *
	 * @param dbDao
	 *            the db dao
	 */
	public DbIdInstanceEntityLoadCallback(DbDao dbDao) {
		super(dbDao);
	}

	@Override
	public Collection<Entity<? extends Serializable>> loadPersistedEntities(Collection<? extends Serializable> ids) {
		return getDbDao().fetchWithNamed(InstanceEntity.QUERY_INSTANCE_ENTITIES_BY_ID_KEY,
				Collections.singletonList(new Pair<String, Object>("id", ids)));
	}
}