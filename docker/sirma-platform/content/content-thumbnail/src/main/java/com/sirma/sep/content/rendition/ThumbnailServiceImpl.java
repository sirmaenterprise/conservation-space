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
import com.sirma.itt.seip.plugin.ExtensionPoint;
import com.sirma.itt.seip.plugin.Plugins;
import com.sirma.itt.seip.util.EqualsHelper;

/**
 * The default implementation of {@link ThumbnailService} works with asynchronous thumbnail retrieval via
 * ThumbnailProvider implementation.
 *
 * @author BBonev
 */
public class ThumbnailServiceImpl implements ThumbnailService {

	private static final String[] EMPTY_STRING_ARRAY = new String[0];
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
	public void register(Serializable targetId, Serializable thumbnailSourceId, ThumbnailType purpose) {
		registerInternal(targetId, thumbnailSourceId, computePurpose(targetId, thumbnailSourceId, purpose), null);
	}

	private void registerInternal(Serializable targetId, Serializable thumbnailSourceId, ThumbnailType purpose, String thumbnail) {
		boolean targetNull = targetId == null;
		boolean sourceNull = thumbnailSourceId == null && thumbnail == null;
		if (targetNull || sourceNull) {
			LOGGER.warn(
					"Tried to register instance for thumbnail but some required data is missing! Target - {} OK, source - {} OK",
					targetNull ? "NOT" : "", sourceNull ? "NOT" : "");
			return;
		}
		String[] endPointInfo = getEndPoint(thumbnailSourceId);
		boolean hasValidEndpoint = endPointInfo.length != 0;
		if (hasValidEndpoint || StringUtils.isNotEmpty(thumbnail)) {
			// cannot handle instance or no providers at all
			saveThumbnailEntry(targetId, thumbnailSourceId, purpose, thumbnail, endPointInfo, hasValidEndpoint);
		}
	}

	private void saveThumbnailEntry(Serializable target, Serializable thumbnailSource, ThumbnailType purpose,
			String thumbnail, String[] endPointInfo, boolean hasValidEndpoint) {
		ThumbnailMappingEntity entity = thumbnailDao.getOrCreateThumbnailMappingEntity(target.toString(), purpose);
		String thumbnailId;
		if (hasValidEndpoint && thumbnailSource != null && endPointInfo != null) {
			// update the end point
			thumbnailId = thumbnailDao.saveThumbnail(thumbnailSource, thumbnail, endPointInfo[0],
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
	 * @param thumbnailSourceId
	 *            the thumbnail source
	 * @return the end point or <code>null</code>
	 */
	private String[] getEndPoint(Serializable thumbnailSourceId) {
		if (thumbnailSourceId == null) {
			return EMPTY_STRING_ARRAY;
		}
		for (ThumbnailProvider provider : providers) {
			String endPoint = provider.createThumbnailEndPoint(thumbnailSourceId);
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

	@Override
	public void addThumbnail(Serializable targetId, String thumbnail, ThumbnailType purpose) {
		registerInternal(targetId, null, computePurpose(targetId, null, purpose), thumbnail);
	}

	private ThumbnailType computePurpose(Serializable targetId, Serializable thumbnailSourceId, ThumbnailType purpose) {
		if (purpose != null) {
			return purpose;
		}
		if (thumbnailSourceId == null) {
			return ThumbnailType.SELF;
		}
		if (EqualsHelper.nullSafeEquals(targetId, thumbnailSourceId)) {
			return ThumbnailType.SELF;
		}
		return ThumbnailType.ASSIGNED;
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
	public void deleteThumbnail(Serializable instanceId) {
		if (instanceId == null) {
			return;
		}
		int deleted = thumbnailDao.deleteThumbnail(instanceId);
		LOGGER.trace("Removed {} thumbnail entries", deleted);
	}

	@Override
	public boolean removeThumbnail(Serializable instanceId, ThumbnailType purpose) {
		if (instanceId == null) {
			return false;
		}
		int deleted = thumbnailDao.deleteThumbnail(instanceId, purpose);
		LOGGER.trace("Removed {} thumbnail entries for instance {}", deleted, instanceId);
		return deleted > 0;
	}
}
