package com.sirma.sep.content.rendition;

import java.io.Serializable;
import java.lang.invoke.MethodHandles;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Map;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.transaction.Transactional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sirma.itt.seip.Entity;
import com.sirma.itt.seip.Pair;
import com.sirma.itt.seip.collections.CollectionUtils;
import com.sirma.itt.seip.db.DatabaseIdManager;
import com.sirma.itt.seip.db.DbDao;
import com.sirma.itt.seip.db.exceptions.DatabaseException;
import com.sirma.itt.seip.domain.instance.InstanceReference;
import com.sirma.itt.seip.util.DigestUtils;

/**
 * Data access service for {@link ThumbnailEntity} and {@link ThumbnailMappingEntity}.
 *
 * @author BBonev
 */
@ApplicationScoped
class ThumbnailDao {

	private static final String PURPOSE = "purpose";
	private static final Logger LOGGER = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

	@Inject
	private DbDao dbDao;
	@Inject
	private DatabaseIdManager idManager;

	/**
	 * Persist the given entity
	 *
	 * @param <E>
	 *            the element type
	 * @param entity
	 *            the entity
	 * @return the e
	 */
	protected <E extends Entity<?>> E persist(E entity) {
		return dbDao.saveOrUpdate(entity);
	}

	/**
	 * Load thumbnails internal.
	 *
	 * @param <S>
	 *            the generic key type
	 * @param ids
	 *            the key set
	 * @param purpose
	 *            the purpose to look for
	 * @return the map
	 */
	@SuppressWarnings({ "unchecked", "boxing" })
	<S extends Serializable> Map<S, String> loadThumbnails(Collection<S> ids, String purpose) {
		List<Pair<String, Object>> args = new ArrayList<>(1);
		args.add(new Pair<>("ids", ids));
		args.add(new Pair<>(PURPOSE, purpose));
		List<Object[]> fetched = dbDao.fetchWithNamed(ThumbnailMappingEntity.QUERY_THUMBNAILS_BY_IDS_KEY, args);
		if (fetched.isEmpty()) {
			return Collections.emptyMap();
		}
		if (LOGGER.isTraceEnabled()) {
			LOGGER.trace("Loaded {} thumbnails out of {} requested", fetched.size(), ids.size());
		}
		Map<S, String> result = CollectionUtils.createLinkedHashMap(fetched.size());
		for (Object[] array : fetched) {
			Object id = array[0];
			Object thumbnail = array[1];
			if (id instanceof Serializable && thumbnail instanceof String) {
				result.put((S) id, (String) thumbnail);
			}
		}
		return result;
	}

	/**
	 * Load thumbnail for the given instance and purpose.
	 *
	 * @param id
	 *            the instance id
	 * @param purpose
	 *            the purpose
	 * @return the thumbnail if any or <code>null</code>
	 */
	String loadThumbnail(Serializable id, String purpose) {
		List<Pair<String, Object>> args = new ArrayList<>(1);
		args.add(new Pair<>("id", id));
		args.add(new Pair<>(PURPOSE, purpose));

		List<String> fetched = dbDao
				.fetchWithNamed(ThumbnailMappingEntity.QUERY_THUMBNAIL_BY_INSTANCE_ID_AND_PURPOSE_KEY, args);
		if (fetched.isEmpty()) {
			return null;
		}
		return fetched.get(0);
	}

	/**
	 * Adds the new thumbnail that will store the given thumbnail content.
	 *
	 * @param thumbnail
	 *            the thumbnail
	 * @return the thumbnail identifier that can be used for loading
	 */
	String addNewThumbnail(String thumbnail) {
		return saveThumbnail(null, thumbnail, null, null);
	}

	/**
	 * Save thumbnail.
	 *
	 * @param sourceId
	 *            the source id
	 * @param thumbnail
	 *            the thumbnail
	 * @param endPoint
	 *            the end point
	 * @param providerName
	 *            the provider name
	 * @return the generated thumbnail id
	 */
	String saveThumbnail(Serializable sourceId, String thumbnail, String endPoint, String providerName) {
		if (sourceId == null && thumbnail == null) {
			return null;
		}
		String id = (String) sourceId;
		if (id == null) {
			id = DigestUtils.calculateDigest(thumbnail);
		}
		ThumbnailEntity entity = getOrCreateThumbnailEntity(id);
		if (entity.getId() == null) {
			entity.setId(id);
			entity.setThumbnail(thumbnail);
			entity.setEndPoint(endPoint);
			entity.setProviderName(providerName);
			entity.setRetries(null);

			// create restore point before doing anything
			idManager.createRestorePoint(id);
			// ensure the id is registered to persist the entity and not to merge it
			idManager.registerId(id);
			try {
				dbDao.saveOrUpdate(entity);
			} finally {
				// unregister id only if was not registered before that
				// this could happen because the id is the same as the target instance so it's
				// possible to be registered before reaching this method - when the instance is
				// saved for the first time
				idManager.unregisterId(id);
				idManager.restoreSavedPoint(id);
			}
		} else {
			// reset thumbnail if registered again
			entity.setThumbnail(thumbnail);
			entity.setRetries(null);
			if (endPoint != null && providerName != null) {
				entity.setEndPoint(endPoint);
				entity.setProviderName(providerName);
			}

			// create restore point before doing anything
			idManager.createRestorePoint(entity.getId());
			// remove all knowledge for the current id as when saving the instance to point out is
			// already saved
			// we are doing this because the entity id is the same as the original document and
			// collisions could occur. We will restore the state after that
			idManager.unregisterId(entity.getId());
			try {
				dbDao.saveOrUpdate(entity);
			} finally {
				// restore the state
				idManager.restoreSavedPoint(entity.getId());
			}
		}
		return id;
	}

	/**
	 * Gets the or create thumbnail entity.
	 *
	 * @param id
	 *            the id to look for
	 * @return the or create thumbnail entity
	 */
	private ThumbnailEntity getOrCreateThumbnailEntity(String id) {
		ThumbnailEntity entity = null;
		try {
			entity = dbDao.find(ThumbnailEntity.class, id);
		} catch (DatabaseException e) {
			LOGGER.trace("Failed to fetch thumbnail entity. Probably does not exists", e);
		}
		if (entity == null) {
			entity = new ThumbnailEntity();
		}
		return entity;
	}

	/**
	 * Prepares/populates new thumbnail mapping entity. First tries to find, if there is record for the given instance,
	 * if so this entity is returned. If there is no record for the passed instance, new entity is created and returned.
	 *
	 * @param instance
	 *            the instance for which we set or search thumbnail record
	 * @param purpose
	 *            the purpose
	 * @return new or existing thumbnail mapping entity
	 */
	ThumbnailMappingEntity getOrCreateThumbnailMappingEntity(InstanceReference instance, String purpose) {
		List<Pair<String, Object>> args = new ArrayList<>(2);
		args.add(new Pair<>("id", instance.getId()));
		args.add(new Pair<>(PURPOSE, purpose));
		List<ThumbnailMappingEntity> list = dbDao
				.fetchWithNamed(ThumbnailMappingEntity.QUERY_THUMBNAIL_MAPPING_BY_ID_AND_PURPOSE_KEY, args);

		ThumbnailMappingEntity entity;
		if (list.isEmpty()) {
			entity = new ThumbnailMappingEntity();
			entity.setInstanceId(instance.getId());
			entity.setInstanceType(instance.getReferenceType());
		} else {
			entity = list.get(0);
		}
		entity.setPurpose(purpose);
		return entity;
	}

	/**
	 * Schedule thumbnail checks for the given instance ids
	 *
	 * @param <S>
	 *            the generic type
	 * @param ids
	 *            the instance ids
	 * @return the number of updated database entries
	 */
	<S extends Serializable> int scheduleThumbnailChecks(Collection<S> ids) {
		List<Pair<String, Object>> args = new ArrayList<>(3);
		args.add(new Pair<>("instanceId", ids));
		args.add(new Pair<>(PURPOSE, RenditionService.DEFAULT_PURPOSE));
		args.add(new Pair<>("thumbnail", ThumbnailService.MAX_RETRIES));
		args.add(new Pair<>("lastFailTimeThreshold", getOneHourBack().getTime()));
		return dbDao.executeUpdate(ThumbnailEntity.UPDATE_RESCHEDULE_THUMBNAIL_CHECK_FOR_INSTANCES_KEY, args);
	}

	/**
	 * Delete thumbnail by thumbnail entity id
	 *
	 * @param thumbnailId
	 *            the thumbnail id
	 * @return the number of deleted rows
	 */
	int deleteThumbnail(Serializable thumbnailId) {
		List<Pair<String, Object>> args = new ArrayList<>(1);
		args.add(new Pair<>("id", thumbnailId));
		int deleted = dbDao.executeUpdate(ThumbnailEntity.DELETE_THUMBNAIL_BY_SOURCE_ID_KEY, args);
		deleted += dbDao.executeUpdate(ThumbnailMappingEntity.DELETE_THUMBNAIL_MAPPINGS_BY_SOURCE_ID_KEY, args);
		return deleted;
	}

	/**
	 * Delete thumbnail by instance id and/or purpose
	 *
	 * @param instanceId
	 *            the instance id
	 * @param purpose
	 *            the purpose
	 * @return the number of deleted rows
	 */
	int deleteThumbnail(Serializable instanceId, String purpose) {
		List<Pair<String, Object>> args = new ArrayList<>(2);
		args.add(new Pair<>("id", instanceId));

		String query = ThumbnailMappingEntity.DELETE_THUMBNAIL_MAPPINGS_BY_INSTANCE_ID_KEY;
		if (purpose != null) {
			query = ThumbnailMappingEntity.DELETE_THUMBNAIL_MAPPINGS_BY_INSTANCE_ID_AND_PURPOSE_KEY;
			args.add(new Pair<>(PURPOSE, purpose));
		}

		return dbDao.executeUpdate(query, args);
	}

	/**
	 * Update thumbnail entity.
	 *
	 * @param id
	 *            the id
	 * @param retries
	 *            the retries
	 * @param thumbnail
	 *            the thumbnail
	 * @param lastFailTime
	 *            the last fail time
	 * @return the int
	 */
	@Transactional
	int updateThumbnailEntity(String id, Integer retries, String thumbnail, Date lastFailTime) {
		List<Pair<String, Object>> args = new ArrayList<>(3);
		args.add(new Pair<>("id", id));
		args.add(new Pair<>("retries", retries));
		args.add(new Pair<>("thumbnail", thumbnail));
		args.add(new Pair<>("lastFailTime", lastFailTime));
		LOGGER.trace("Updating thumbnail entity - id:[{}], retries:[{}], lastFailTime:[{}], available thumbnail:[{}]",
				id, retries, lastFailTime, thumbnail != null);
		return dbDao.executeUpdateInNewTx(ThumbnailEntity.UPDATE_THUMBNAIL_DATA_KEY, args);
	}

	/**
	 * Gets the thumbnails for synchronization.
	 *
	 * @param maxRetryCount
	 *            the max retry count
	 * @return the thumbnails for synchronization
	 */
	List<Object[]> getThumbnailsForSynchronization(Integer maxRetryCount) {
		List<Pair<String, Object>> args = new ArrayList<>(2);
		args.add(new Pair<>("retries", maxRetryCount));
		// select entries that are older than one hour current time
		args.add(new Pair<>("lastFailTimeThreshold", getOneHourBack().getTime()));
		return dbDao.fetchWithNamed(ThumbnailEntity.QUERY_THUMBNAILS_FOR_SYNC_KEY, args);
	}

	private static Calendar getOneHourBack() {
		Calendar calendar = Calendar.getInstance();
		calendar.add(Calendar.HOUR_OF_DAY, -1);
		return calendar;
	}
}
