package com.sirma.itt.emf.rendition;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sirma.itt.emf.converter.TypeConverter;
import com.sirma.itt.emf.converter.TypeConverterUtil;
import com.sirma.itt.emf.db.DbDao;
import com.sirma.itt.emf.db.EmfQueries;
import com.sirma.itt.emf.domain.Pair;
import com.sirma.itt.emf.time.TimeTracker;

/**
 * Callable object for fetching thumbnails for list of instances.
 *
 * @author BBonev
 */
public class ThumbnailLoader implements Callable<Void> {

	/** The Constant LOGGER. */
	private static final Logger LOGGER = LoggerFactory.getLogger(ThumbnailLoader.class);

	/** The db. */
	private DbDao db;

	/** The data. */
	private List<Object[]> data;

	/** The providers map. */
	private Map<String, ThumbnailProvider> providersMap;

	/** The max thumbnail retry count. */
	private Integer maxThumbnailRetryCount;

	/**
	 * Instantiates a new thumbnail loader.
	 *
	 * @param db
	 *            the db dao reference
	 * @param providers
	 *            the providers
	 * @param maxThumbnailRetryCount
	 *            the max thumbnail retry count
	 * @param data
	 *            the data
	 */
	public ThumbnailLoader(DbDao db, Map<String, ThumbnailProvider> providers,
			int maxThumbnailRetryCount, List<Object[]> data) {
		this.db = db;
		providersMap = providers;
		this.maxThumbnailRetryCount = maxThumbnailRetryCount;
		this.data = data;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Void call() throws Exception {
		if ((data == null) || data.isEmpty()) {
			return null;
		}

		TypeConverter converter = TypeConverterUtil.getConverter();
		int successful = 0;
		int failed = 0;
		TimeTracker timeTracker = TimeTracker.createAndStart();
		for (Object[] array : data) {
			String id = array[0].toString();
			String endPoint = null;
			if (array[1] != null) {
				endPoint = array[1].toString();
			}
			String providerName = array[2].toString();
			Integer retries = converter.convert(Integer.class, array[3]);

			try {
				String thumbnail = getThumbnailFromProvider(providerName, endPoint);

				if (thumbnail == null) {
					thumbnailNotFound(id, retries);
					failed++;
				} else {
					addThumbnailToInstance(id, thumbnail);
					successful++;
				}
			} catch (Exception e) {
				LOGGER.debug(
						"Could not process thumbnail synchronization for entry {} and end point {}",
						id, endPoint);
			}
		}

		LOGGER.debug(
				"Completed thumbnail sync batch for {} entries with {} successfull and {} failed and took {} s",
				data.size(), successful, failed, timeTracker.stopInSeconds());

		return null;
	}

	/**
	 * Adds the thumbnail to instance.
	 *
	 * @param id
	 *            the id
	 * @param thumbnail
	 *            the thumbnail
	 */
	private void addThumbnailToInstance(String id, String thumbnail) {
		updateThumbnailEntity(id, null, thumbnail);
	}

	/**
	 * Thumbnail not found.
	 *
	 * @param id
	 *            the id
	 * @param retries
	 *            the retries
	 */
	private void thumbnailNotFound(String id, Integer retries) {
		Integer count = retries;
		if (count == null) {
			count = 1;
		} else {
			count++;
		}
		String thumbnailId = null;
		if (count >= maxThumbnailRetryCount) {
			// this will stop trying to fetch the same instance again
			thumbnailId = ThumbnailService.MAX_RETRIES;
			LOGGER.warn("Reached max retries for thumbnail entity with id " + id);
		}
		updateThumbnailEntity(id, count, thumbnailId);
	}

	/**
	 * Update thumbnail entity.
	 *
	 * @param id
	 *            the id
	 * @param retries
	 *            the retries
	 * @param thumbnail
	 *            the thumbnail id
	 */
	private void updateThumbnailEntity(String id, Integer retries, String thumbnail) {
		List<Pair<String, Object>> args = new ArrayList<Pair<String, Object>>(3);
		args.add(new Pair<String, Object>("id", id));
		args.add(new Pair<String, Object>("retries", retries));
		args.add(new Pair<String, Object>("thumbnail", thumbnail));

		int update = db.executeUpdateInNewTx(EmfQueries.UPDATE_THUMBNAIL_DATA_KEY, args);
		if (update != 1) {
			LOGGER.warn("Failed to update thumbnail entry. Check count=" + update);
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
	public String getThumbnailFromProvider(String providerName, String endPoint) {
		if ((providerName == null) || (endPoint == null)) {
			// invalid input data
			return null;
		}
		ThumbnailProvider provider = providersMap.get(providerName);
		if (provider == null) {
			LOGGER.warn("No provider found for name " + providerName);
			return null;
		}

		return provider.getThumbnail(endPoint);
	}

}