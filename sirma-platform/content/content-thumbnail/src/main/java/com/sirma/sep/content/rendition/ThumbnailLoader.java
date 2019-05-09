package com.sirma.sep.content.rendition;

import java.io.Serializable;
import java.lang.invoke.MethodHandles;
import java.util.Date;

import javax.inject.Inject;
import javax.inject.Singleton;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sirma.itt.seip.plugin.ExtensionPoint;
import com.sirma.itt.seip.plugin.Plugins;

/**
 * Performs actual thumbnail synchronize/download providers the proper {@link ThumbnailProvider} and updates the loaded
 * thumbnail to the database.
 *
 * @author BBonev
 */
@Singleton
class ThumbnailLoader {

	private static final Logger LOGGER = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());
	private static final ThumbnailProvider NO_OP_PROVIDER = new NoOpProvider();

	@Inject
	@ExtensionPoint(ThumbnailProvider.TARGET_NAME)
	private Plugins<ThumbnailProvider> providers;

	@Inject
	private ThumbnailDao thumbnailDao;

	@Inject
	private ThumbnailConfigurations thumbnailConfigurations;

	/**
	 * Load thumbnail from the {@link ThumbnailProvider}s
	 *
	 * @param id
	 *            the id
	 * @param endPoint
	 *            the end point
	 * @param providerName
	 *            the provider name
	 * @param retries
	 *            the retries
	 */
	void load(String id, String endPoint, String providerName, Integer retries) {
		String thumbnail = null;
		try {
			thumbnail = getThumbnailFromProvider(providerName, endPoint);
		} catch (Exception e) {
			String string = "Could not retrieve thumbnail for entry {} and end point {}";
			LOGGER.debug(string, id, endPoint);
			LOGGER.trace(string, id, endPoint, e);
		}
		try {
			if (thumbnail == null) {
				thumbnailNotFound(id, retries);
			} else {
				updateThumbnailEntity(id, null, thumbnail, null);
			}
		} catch (Exception e) {
			String string = "Could not save thumbnail for entry {}";
			LOGGER.debug(string, id);
			LOGGER.trace(string, id, e);
		}
	}

	/**
	 * Thumbnail not found.
	 */
	@SuppressWarnings("boxing")
	private void thumbnailNotFound(String id, Integer retries) {
		int count = retries == null ? 1 : retries + 1;
		String thumbnailId = null;
		Date lastFailTime = null;
		if (count >= thumbnailConfigurations.getMaxThumbnailRetryCount()) {
			// this will stop trying to fetch the same instance again
			thumbnailId = ThumbnailService.MAX_RETRIES;
			lastFailTime = new Date();
			LOGGER.warn("Reached max retries for thumbnail entity with id {}", id);
		}
		updateThumbnailEntity(id, count, thumbnailId, lastFailTime);
	}

	/**
	 * Update thumbnail entity.
	 */
	private void updateThumbnailEntity(String id, Integer retries, String thumbnail, Date lastFailTime) {
		int update = thumbnailDao.updateThumbnailEntity(id, retries, thumbnail, lastFailTime);
		if (update != 1) {
			LOGGER.warn("Failed to update thumbnail entry. Check count={}", update);
		}
	}

	/**
	 * Gets the thumbnail from provider.
	 *
	 * @param providerName
	 *            the provider name
	 * @param endPoint
	 *            the end point
	 * @return the thumbnail from provider
	 */
	private String getThumbnailFromProvider(String providerName, String endPoint) {
		if (providerName == null || endPoint == null) {
			// invalid input data
			return null;
		}
		return providers.get(providerName).orElse(NO_OP_PROVIDER).getThumbnail(endPoint);
	}

	/**
	 * Empty provider implementation
	 *
	 * @author BBonev
	 */
	private static class NoOpProvider implements ThumbnailProvider {

		@Override
		public String createThumbnailEndPoint(Serializable source) {
			return null;
		}

		@Override
		public String getThumbnail(String endPoint) {
			LOGGER.warn("No provider found for end point: {}", endPoint);
			return null;
		}

		@Override
		public String getName() {
			return null;
		}

	}

}
