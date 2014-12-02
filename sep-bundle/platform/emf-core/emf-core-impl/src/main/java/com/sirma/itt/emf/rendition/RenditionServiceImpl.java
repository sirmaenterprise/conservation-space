package com.sirma.itt.emf.rendition;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.Callable;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sirma.itt.emf.concurrent.TxAsyncCallableEvent;
import com.sirma.itt.emf.db.DbDao;
import com.sirma.itt.emf.db.EmfQueries;
import com.sirma.itt.emf.domain.Pair;
import com.sirma.itt.emf.event.EventService;
import com.sirma.itt.emf.instance.model.Instance;
import com.sirma.itt.emf.properties.DefaultProperties;
import com.sirma.itt.emf.util.CollectionUtils;

/**
 * Default implementation for the rendition service.
 * 
 * @author BBonev
 */
@ApplicationScoped
public class RenditionServiceImpl implements RenditionService {

	/** The Constant LOGGER. */
	private static final Logger LOGGER = LoggerFactory.getLogger(RenditionServiceImpl.class);

	/** The db dao. */
	@Inject
	private DbDao dbDao;

	/** The event service. */
	@Inject
	private EventService eventService;

	/** The synchronization service. */
	@Inject
	private ThumbnailService synchronizationService;

	/**
	 * {@inheritDoc}
	 */
	@Override
	public String getPrimaryThumbnail(Instance instance) {
		return getThumbnailForInstance(instance, "primary");
	}

	/**
	 * Gets the thumbnail for instance.
	 * 
	 * @param instance
	 *            the instance
	 * @param purpose
	 *            the purpose
	 * @return the thumbnail for instance
	 */
	private String getThumbnailForInstance(Instance instance, String purpose) {
		if (instance == null) {
			return null;
		}
		// if already loaded return it
		Serializable thumbnail = null;
		if (instance.getProperties() != null) {
			thumbnail = instance.getProperties().get(DefaultProperties.THUMBNAIL_IMAGE);
		}
		// if null or not a string then we should get the instance thumbnail again
		if (!(thumbnail instanceof String)) {
			thumbnail = loadThumbnail(instance.getId(), purpose);
			if (thumbnail != null) {
				// add thumbnail to the instance properties
				if (instance.getProperties() != null) {
					setThumbnailToInstance(instance, thumbnail);
				}
				return thumbnail.toString();
			}
			return buildDefaultThumbnail(instance);
		}
		return thumbnail.toString();
	}

	/**
	 * Builds the default thumbnail for the instance.
	 * 
	 * @param instance
	 *            the instance
	 * @return the default thumbnail
	 */
	private String buildDefaultThumbnail(Instance instance) {
		// TODO Auto-generated method stub
		return null;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public String getDefaultThumbnail(Instance instance) {
		String thumbnailForInstance = getThumbnailForInstance(instance, DEFAULT_PURPOSE);
		if (thumbnailForInstance == null) {
			return null;
		}
		if (!thumbnailForInstance.startsWith(BASE64_IMAGE_PREFIX)) {
			thumbnailForInstance = BASE64_IMAGE_PREFIX + thumbnailForInstance;
		}
		return thumbnailForInstance;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public String getThumbnail(Serializable id) {
		if (id instanceof Instance) {
			// for backward compatibility
			return loadThumbnail(((Instance) id).getId(), DEFAULT_PURPOSE);
		}
		return loadThumbnail(id, DEFAULT_PURPOSE);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public String getThumbnail(Serializable id, String purpose) {
		return loadThumbnail(id, DEFAULT_PURPOSE);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public <I extends Instance> I loadThumbnail(I instance) {
		getThumbnailForInstance(instance, DEFAULT_PURPOSE);
		return instance;
	}

	@Override
	public <I extends Instance> void loadThumbnails(Collection<I> instances) {
		if ((instances == null) || instances.isEmpty()) {
			return;
		}
		Map<Serializable, I> mapping = CollectionUtils.createLinkedHashMap(instances.size());
		for (I instance : instances) {
			if (instance.getId() != null) {
				mapping.put(instance.getId(), instance);
			}
		}
		if (mapping.isEmpty()) {
			return;
		}
		Map<Serializable, String> thumbnails = loadThumbnailsInternal(mapping.keySet(),
				DEFAULT_PURPOSE);
		for (Entry<Serializable, String> entry : thumbnails.entrySet()) {
			I instance = mapping.remove(entry.getKey());
			if (instance != null) {
				setThumbnailToInstance(instance, entry.getValue());
			}
		}
		// schedule thumbnail check for instances that does not have a thumbnail
		scheduleThumbnailCheck(mapping.keySet());
		// free for GC
		mapping.clear();
		thumbnails.clear();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public <S extends Serializable> Map<S, String> getThumbnails(Collection<S> ids) {
		return loadThumbnailsInternal(ids, DEFAULT_PURPOSE);
	}

	/**
	 * Schedule thumbnail check for instances that does not have a thumbnail.
	 * 
	 * @param ids
	 *            the instance ids to schedule
	 */
	private void scheduleThumbnailCheck(Set<Serializable> ids) {
		if (ids.isEmpty()) {
			return;
		}
		eventService.fire(new TxAsyncCallableEvent(new ThumbnailCheck(
				new ArrayList<Serializable>(ids))));
	}

	/**
	 * Sets the thumbnail to instance.
	 * 
	 * @param instance
	 *            the instance
	 * @param thumbnail
	 *            the thumbnail
	 */
	protected void setThumbnailToInstance(Instance instance, Serializable thumbnail) {
		instance.getProperties().put(DefaultProperties.THUMBNAIL_IMAGE, thumbnail);
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
	@SuppressWarnings("unchecked")
	private <S extends Serializable> Map<S, String> loadThumbnailsInternal(Collection<S> ids,
			String purpose) {
		List<Pair<String, Object>> args = new ArrayList<Pair<String, Object>>(1);
		args.add(new Pair<String, Object>("ids", ids));
		args.add(new Pair<String, Object>("purpose", purpose));
		List<Object[]> fetched = dbDao.fetchWithNamed(EmfQueries.QUERY_THUMBNAILS_BY_IDS_KEY, args);
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
			if ((id instanceof Serializable) && (thumbnail instanceof String)) {
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
	private String loadThumbnail(Serializable id, String purpose) {
		List<Pair<String, Object>> args = new ArrayList<Pair<String, Object>>(1);
		args.add(new Pair<String, Object>("id", id));
		args.add(new Pair<String, Object>("purpose", purpose));

		List<String> fetched = dbDao.fetchWithNamed(
				EmfQueries.QUERY_THUMBNAIL_BY_INSTANCE_ID_AND_PURPOSE_KEY, args);
		if (fetched.isEmpty()) {
			return null;
		}
		return fetched.get(0);
	}

	/**
	 * Callable to trigger thumbnail check for list of instances
	 * 
	 * @author BBbonev
	 */
	class ThumbnailCheck implements Callable<Void> {

		/** The ids. */
		private final Collection<? extends Serializable> ids;

		/**
		 * Instantiates a new thumbnail check.
		 * 
		 * @param ids
		 *            the ids
		 */
		public ThumbnailCheck(Collection<? extends Serializable> ids) {
			this.ids = ids;
		}

		/**
		 * Call.
		 * 
		 * @return the void
		 * @throws Exception
		 *             the exception
		 */
		@Override
		public Void call() throws Exception {
			synchronizationService.scheduleCheck(ids);
			return null;
		}

	}
}
