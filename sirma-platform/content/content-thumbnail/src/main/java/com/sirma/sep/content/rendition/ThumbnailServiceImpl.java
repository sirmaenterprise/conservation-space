package com.sirma.sep.content.rendition;

import java.io.Serializable;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import javax.inject.Inject;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sirma.itt.seip.collections.CollectionUtils;
import com.sirma.itt.seip.domain.instance.Instance;
import com.sirma.itt.seip.domain.instance.InstanceReference;
import com.sirma.itt.seip.plugin.ExtensionPoint;
import com.sirma.itt.seip.plugin.Plugins;

/**
 * The default implementation of {@link ThumbnailService} works with asynchronous thumbnail retrieval via
 * ThumbnailProvider implementation.
 *
 * @author BBonev
 */
public class ThumbnailServiceImpl implements ThumbnailService {

	private static final String[] EMPTY_STRING_ARRAY = new String[0];
	/** Default purpose used to store trumbnails when no purpose is provided. */
	private static final String DEFAULT_PURPOSE = RenditionService.DEFAULT_PURPOSE;
	private static final Logger LOGGER = LoggerFactory.getLogger(ThumbnailServiceImpl.class);

	/**
	 * Temporary set to contain the instance ids that need to be rescheduled for checking. We keep this set in case of
	 * multiple writes to the same rows in the database could create deadlocks and unexpected race conditions. We will
	 * optimize the write not to execute updated for entries updated by other threads/calls. This will reduce the
	 * database updates.
	 */
	private static final Set<Serializable> CURRENT_CHECKING_IDS = new HashSet<>(256);

	/** The providers. */
	@Inject
	@ExtensionPoint(ThumbnailProvider.TARGET_NAME)
	private Plugins<ThumbnailProvider> providers;

	@Inject
	private ThumbnailDao thumbnailDao;

	@Override
	public void register(Instance instance) {
		if (instance == null) {
			return;
		}
		registerInternal(instance.toReference(), instance, DEFAULT_PURPOSE, null);
	}

	@Override
	public void register(Instance target, Instance thumbnailSource) {
		if (target == null) {
			return;
		}
		registerInternal(target.toReference(), thumbnailSource, DEFAULT_PURPOSE, null);
	}

	@Override
	public void register(Instance target, Instance thumbnailSource, String purpose) {
		if (target == null) {
			return;
		}
		registerInternal(target.toReference(), thumbnailSource, purpose, null);
	}

	@Override
	public void register(InstanceReference target, Instance thumbnailSource, String purpose) {
		if (target == null) {
			return;
		}

		if (StringUtils.isBlank(purpose)) {
			registerInternal(target, thumbnailSource, DEFAULT_PURPOSE, null);
		} else {
			registerInternal(target, thumbnailSource, purpose, null);
		}
	}

	@Override
	public void copyThumbnailFromSource(InstanceReference target, InstanceReference thumbnailSource) {
		if (target == null || thumbnailSource == null) {
			return;
		}

		ThumbnailMappingEntity sourceEntity = thumbnailDao.getOrCreateThumbnailMappingEntity(thumbnailSource,
				DEFAULT_PURPOSE);
		if (sourceEntity.getId() == null) {
			return;
		}

		ThumbnailMappingEntity entity = new ThumbnailMappingEntity();
		entity.setThumbnailId(sourceEntity.getThumbnailId());
		entity.setInstanceType(target.getReferenceType());
		entity.setPurpose(sourceEntity.getPurpose());
		entity.setInstanceId(target.getId());
		thumbnailDao.persist(entity);
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
	private void registerInternal(InstanceReference target, Instance thumbnailSource, String purpose,
			String thumbnail) {
		boolean targetNull = target == null;
		boolean sourceNull = thumbnailSource == null && thumbnail == null;
		if (targetNull || sourceNull) {
			LOGGER.warn(
					"Tried to register instance for thumbnail but some required data is missing! Target - {} OK, source - {} OK",
					targetNull ? "NOT" : "", sourceNull ? "NOT" : "");
			return;
		}
		String[] endPointInfo = getEndPoint(thumbnailSource);
		boolean hasValidEndpoint = endPointInfo.length != 0;
		if (hasValidEndpoint || StringUtils.isNotEmpty(thumbnail)) {
			// cannot handle instance or no providers at all
			saveThumbnailEntry(target, thumbnailSource, purpose, thumbnail, endPointInfo, hasValidEndpoint);
		}
	}

	private void saveThumbnailEntry(InstanceReference target, Instance thumbnailSource, String purpose,
			String thumbnail, String[] endPointInfo, boolean hasValidEndpoint) {
		ThumbnailMappingEntity entity = thumbnailDao.getOrCreateThumbnailMappingEntity(target, purpose);
		String thumbnailId;
		if (hasValidEndpoint && thumbnailSource != null && endPointInfo != null) {
			// update the end point
			thumbnailId = thumbnailDao.saveThumbnail(thumbnailSource.getId(), thumbnail, endPointInfo[0],
					endPointInfo[1]);
		} else {
			// if we already have a thumbnail we should save it and set the id
			thumbnailId = thumbnailDao.addNewThumbnail(thumbnail);
		}
		entity.setThumbnailId(thumbnailId);

		// save the entity to DB or update the existing one
		thumbnailDao.persist(entity);
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

	@Override
	public <S extends Serializable> void scheduleCheck(Collection<S> ids) {
		if (CollectionUtils.isEmpty(ids)) {
			return;
		}

		Set<S> copy = getNonConflictingIdsToSchedule(ids);
		// this means that all of the given ids are already schedule for update
		if (copy.isEmpty()) {
			return;
		}

		try {
			int update = thumbnailDao.scheduleThumbnailChecks(copy);
			if (update > 0) {
				LOGGER.debug("Rescheduled {} out of {} thumbnail instance checks", update, ids.size());
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
	private static <S extends Serializable> void releaseConflictingIds(Set<S> ids) {
		synchronized (CURRENT_CHECKING_IDS) {
			CURRENT_CHECKING_IDS.removeAll(ids);
		}
	}

	/**
	 * Gets the non conflicting ids to schedule. Returns only the new ids that are not scheduled for update.
	 *
	 * @param <S>
	 *            the generic type
	 * @param ids
	 *            the ids
	 * @return the non conflicting ids to schedule
	 */
	private static <S extends Serializable> Set<S> getNonConflictingIdsToSchedule(Collection<S> ids) {
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

	@Override
	public void addThumbnail(InstanceReference reference, String thumbnail) {
		addThumbnailInternal(reference, thumbnail, DEFAULT_PURPOSE);
	}

	@Override
	public void addThumbnail(InstanceReference reference, String thumbnail, String purpose) {
		addThumbnailInternal(reference, thumbnail, purpose);
	}

	@Override
	public void deleteThumbnail(Serializable sourceInstanceId) {
		if (sourceInstanceId == null) {
			return;
		}
		int deleted = thumbnailDao.deleteThumbnail(sourceInstanceId);
		LOGGER.trace("Removed {} thumbnail entries", deleted);
	}

	@Override
	public void removeThumbnail(Serializable instanceId, String purpose) {
		if (instanceId == null) {
			return;
		}
		int deleted = thumbnailDao.deleteThumbnail(instanceId, purpose);
		LOGGER.trace("Removed {} thumbnail entries for instance {}", deleted, instanceId);
	}

	/**
	 * Adds the thumbnail internal. *
	 *
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



}
