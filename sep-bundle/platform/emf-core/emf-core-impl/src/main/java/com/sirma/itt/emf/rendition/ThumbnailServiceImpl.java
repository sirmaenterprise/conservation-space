package com.sirma.itt.emf.rendition;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.ejb.Stateless;
import javax.ejb.TransactionAttribute;
import javax.ejb.TransactionAttributeType;
import javax.inject.Inject;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sirma.itt.emf.db.DbDao;
import com.sirma.itt.emf.db.EmfQueries;
import com.sirma.itt.emf.db.SequenceEntityGenerator;
import com.sirma.itt.emf.domain.Pair;
import com.sirma.itt.emf.exceptions.CmfDatabaseException;
import com.sirma.itt.emf.instance.model.Instance;
import com.sirma.itt.emf.instance.model.InstanceReference;
import com.sirma.itt.emf.rendition.entity.ThumbnailEntity;
import com.sirma.itt.emf.rendition.entity.ThumbnailMappingEntity;
import com.sirma.itt.emf.util.CollectionUtils;
import com.sirma.itt.emf.util.DigestUtils;

/**
 * The default implementation of {@link ThumbnailService} works with asynchronous
 * thumbnail retrieval via ThumbnailProvider implementation.
 *
 * @author BBonev
 */
@Stateless
public class ThumbnailServiceImpl implements ThumbnailService {

	private static final String PURPOSE = "purpose";
	private static final String[] EMPTY_STRING_ARRAY = new String[0];
	/** The Constant DEFAULT_PURPOSE. */
	private static final String DEFAULT_PURPOSE = RenditionService.DEFAULT_PURPOSE;
	/** The Constant LOGGER. */
	private static final Logger LOGGER = LoggerFactory.getLogger(ThumbnailServiceImpl.class);

	/**
	 * Temporary set to contain the instance ids that need to be rescheduled for checking. We keep
	 * this set in case of multiple writes to the same rows in the database could create deadlocks
	 * and unexpected race conditions. We will optimize the write not to execute updated for entries
	 * updated by other threads/calls. This will reduce the database updates.
	 */
	private static final Set<Serializable> CURRENT_CHECKING_IDS = new HashSet<Serializable>(256);

	/** The providers. */
	@Inject
	private javax.enterprise.inject.Instance<ThumbnailProvider> providers;

	/** The db dao. */
	@Inject
	private DbDao dbDao;

	/**
	 * {@inheritDoc}
	 */
	@Override
	@TransactionAttribute(TransactionAttributeType.REQUIRED)
	public void register(Instance instance) {
		if (instance == null) {
			return;
		}
		registerInternal(instance.toReference(), instance, DEFAULT_PURPOSE, null);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	@TransactionAttribute(TransactionAttributeType.REQUIRED)
	public void register(Instance target, Instance thumbnailSource) {
		if (target == null) {
			return;
		}
		registerInternal(target.toReference(), thumbnailSource, DEFAULT_PURPOSE, null);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	@TransactionAttribute(TransactionAttributeType.REQUIRED)
	public void register(Instance target, Instance thumbnailSource, String purpose) {
		if (target == null) {
			return;
		}
		registerInternal(target.toReference(), thumbnailSource, purpose, null);
	}

	/**
	 * Register instance internal.
	 *
	 * @param target
	 *            the target
	 * @param thumbnailSource
	 *            the thumbnail source
	 * @param purpose
	 *            the purpose
	 * @param thumbnail
	 *            the thumbnail
	 */
	private void registerInternal(InstanceReference target, Instance thumbnailSource,
			String purpose, String thumbnail) {
		if ((target == null) || ((thumbnailSource == null) && (thumbnail == null))) {
			LOGGER.warn(
					"Tried to register instance for thumbnail but some required data is missing! Target - {} OK, source - {} OK",
					target == null ? "NOT" : "",
					(thumbnailSource == null) && (thumbnail == null) ? "NOT" : "");
			return;
		}
		String[] endPointInfo = getEndPoint(thumbnailSource);
		if ((endPointInfo == null) && StringUtils.isNotEmpty(thumbnail)) {
			// cannot handle instance or no providers at all
			return;
		}

		List<Pair<String, Object>> args = new ArrayList<Pair<String, Object>>(2);
		args.add(new Pair<String, Object>("id", target.getIdentifier()));
		args.add(new Pair<String, Object>(PURPOSE, purpose));
		List<ThumbnailMappingEntity> list = dbDao.fetchWithNamed(
				EmfQueries.QUERY_THUMBNAIL_MAPPING_BY_ID_AND_PURPOSE_KEY, args);

		ThumbnailMappingEntity entity;
		if (list.isEmpty()) {
			entity = new ThumbnailMappingEntity();
			entity.setInstanceId(target.getIdentifier());
			entity.setInstanceType(target.getReferenceType());
		} else {
			entity = list.get(0);
		}
		entity.setPurpose(purpose);
		String thumbnailId;
		if (endPointInfo != null) {
			// update the end point
			thumbnailId = saveThumbnail(thumbnailSource.getId(), thumbnail, endPointInfo[0],
					endPointInfo[1], dbDao);
		} else {
			// if we already have a thumbnail we should save it and set the id
			thumbnailId = saveThumbnail(null, thumbnail, null, null, dbDao);
		}
		entity.setThumbnailId(thumbnailId);

		// save the entity to DB or update the existing one
		dbDao.saveOrUpdate(entity);
	}

	/**
	 * Gets the end point for the given source instance if possible.
	 *
	 * @param thumbnailSource
	 *            the thumbnail source
	 * @return the end point or <code>null</code>
	 */
	private String[] getEndPoint(Instance thumbnailSource) {
		if (thumbnailSource == null) {
			return EMPTY_STRING_ARRAY;
		}
		for (ThumbnailProvider provider : providers) {
			String endPoint = provider.createThumbnailEndPoint(thumbnailSource);
			if (endPoint != null) {
				return new String[] { endPoint, provider.getName() };
			}
		}
		return EMPTY_STRING_ARRAY;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	@TransactionAttribute(TransactionAttributeType.REQUIRED)
	public <S extends Serializable> void scheduleCheck(Collection<S> ids) {
		if ((ids == null) || ids.isEmpty()) {
			return;
		}

		Set<S> copy = getNonConflictingIdsToSchedule(ids);
		// this means that all of the given ids are already schedule for update
		if (copy.isEmpty()) {
			return;
		}

		try {
			List<Pair<String, Object>> args = new ArrayList<Pair<String, Object>>(3);
			args.add(new Pair<String, Object>("instanceId", copy));
			args.add(new Pair<String, Object>(PURPOSE, DEFAULT_PURPOSE));
			args.add(new Pair<String, Object>("thumbnail", MAX_RETRIES));
			int update = dbDao.executeUpdate(
					EmfQueries.UPDATE_RESCHEDULE_THUMBNAIL_CHECK_FOR_INSTANCES_KEY, args);
			if (update > 0) {
				LOGGER.debug("Rescheduled {} out of {} thumbnail instance checks", update,
						ids.size());
			}
		} finally {
			releaseConflictingIds(copy);
		}
	}

	/**
	 * Release conflicting ids. After finishing work with for this ids we should release them.
	 * 
	 * @param <S>
	 *            the generic type
	 * @param ids
	 *            the ids to release
	 */
	private <S extends Serializable> void releaseConflictingIds(Set<S> ids) {
		synchronized (CURRENT_CHECKING_IDS) {
			CURRENT_CHECKING_IDS.removeAll(ids);
		}
	}

	/**
	 * Gets the non conflicting ids to schedule. Returns only the new ids that are not scheduled for
	 * update.
	 * 
	 * @param <S>
	 *            the generic type
	 * @param ids
	 *            the ids
	 * @return the non conflicting ids to schedule
	 */
	private <S extends Serializable> Set<S> getNonConflictingIdsToSchedule(Collection<S> ids) {
		Set<S> copy = CollectionUtils.createHashSet(ids.size());
		copy.addAll(ids);
		// we lock for concurrent modifications
		// we will leave in the temp set only ids that are not currently scheduled for rescheduling
		synchronized (CURRENT_CHECKING_IDS) {
			copy.removeAll(CURRENT_CHECKING_IDS);
			CURRENT_CHECKING_IDS.addAll(copy);
		}
		return copy;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	@TransactionAttribute(TransactionAttributeType.REQUIRED)
	public void addThumbnail(InstanceReference reference, String thumbnail) {
		addThumbnailInternal(reference, thumbnail, DEFAULT_PURPOSE);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	@TransactionAttribute(TransactionAttributeType.REQUIRED)
	public void addThumbnail(InstanceReference reference, String thumbnail, String purpose) {
		addThumbnailInternal(reference, thumbnail, purpose);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	@TransactionAttribute(TransactionAttributeType.REQUIRED)
	public void deleteThumbnail(Serializable sourceInstanceId) {
		if (sourceInstanceId == null) {
			return;
		}
		List<Pair<String, Object>> args = new ArrayList<Pair<String, Object>>(1);
		args.add(new Pair<String, Object>("id", sourceInstanceId));
		int deleted = dbDao.executeUpdate(EmfQueries.DELETE_THUMBNAIL_BY_SOURCE_ID_KEY, args);
		deleted += dbDao.executeUpdate(EmfQueries.DELETE_THUMBNAIL_MAPPINGS_BY_SOURCE_ID_KEY, args);
		LOGGER.debug("Removed {} thumbnail entries", deleted);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	@TransactionAttribute(TransactionAttributeType.REQUIRED)
	public void removeThumbnail(Serializable instanceId, String purpose) {
		if (instanceId == null) {
			return;
		}
		List<Pair<String, Object>> args = new ArrayList<Pair<String, Object>>(2);
		args.add(new Pair<String, Object>("id", instanceId));

		String qeury = EmfQueries.DELETE_THUMBNAIL_MAPPINGS_BY_INSTANCE_ID_KEY;
		if (purpose != null) {
			qeury = EmfQueries.DELETE_THUMBNAIL_MAPPINGS_BY_INSTANCE_ID_AND_PURPOSE_KEY;
			args.add(new Pair<String, Object>(PURPOSE, purpose));
		}

		int deleted = dbDao.executeUpdate(qeury, args);
		LOGGER.debug("Removed {} thumbnail entries for instance {}", deleted, instanceId);
	}

	/**
	 * Adds the thumbnail internal.	 * *
	 * @param reference
	 *            the reference
	 * @param thumbnail
	 *            the thumbnail
	 * @param purpose
	 *            the purpose
	 */
	private void addThumbnailInternal(InstanceReference reference, String thumbnail, String purpose) {
		registerInternal(reference, null, purpose, thumbnail);
	}



	/**
	 * Save thumbnail. * *
	 * 
	 * @param sourceId
	 *            the source id
	 * @param thumbnail
	 *            the thumbnail
	 * @param endPoint
	 *            the end point
	 * @param providerName
	 *            the provider name
	 * @param db
	 *            the db
	 * @return the generated thumbnail id
	 */
	protected String saveThumbnail(Serializable sourceId, String thumbnail, String endPoint,
			String providerName, DbDao db) {
		if ((sourceId == null) && (thumbnail == null)) {
			return null;
		}
		String id = (String) sourceId;
		if (id == null) {
			id = DigestUtils.calculateDigest(thumbnail);
		}
		ThumbnailEntity entity = getOrCreateThumbnailEntity(id);
		if (entity.getId() == null) {
			entity = new ThumbnailEntity();
			entity.setId(id);
			entity.setThumbnail(thumbnail);
			entity.setEndPoint(endPoint);
			entity.setProviderName(providerName);
			entity.setRetries(null);

			// create restore point before doing anything
			SequenceEntityGenerator.createRestorePoint(id);
			// ensure the id is registered to persist the entity and not to merge it
			SequenceEntityGenerator.registerId(id);
			try {
				db.saveOrUpdate(entity);
			} finally {
				// unregister id only if was not registered before that
				// this could happen because the id is the same as the target instance so it's
				// possible to be registered before reaching this method - when the instance is
				// saved for the first time
				SequenceEntityGenerator.unregister(id);
				SequenceEntityGenerator.restoreSavedPoint(id);
			}
		} else {
			// reset thumbnail if registered again
			entity.setThumbnail(thumbnail);
			entity.setRetries(null);
			if ((entity.getEndPoint() == null) && (endPoint != null)) {
				entity.setEndPoint(endPoint);
				entity.setProviderName(providerName);
			}

			// create restore point before doing anything
			SequenceEntityGenerator.createRestorePoint(entity.getId());
			// remove all knowledge for the current id as when saving the instance to point out is
			// already saved
			// we are doing this because the entity id is the same as the original document and
			// collisions could occur. We will restore the state after that
			SequenceEntityGenerator.unregister(entity.getId());
			try {
				db.saveOrUpdate(entity);
			} finally {
				// restore the state
				SequenceEntityGenerator.restoreSavedPoint(entity.getId());
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
		} catch (CmfDatabaseException e) {
			LOGGER.trace("Failed to fetch thumbnail entity. Probably does not exists", e);
		}
		if (entity == null) {
			entity = new ThumbnailEntity();
		}
		return entity;
	}

}
