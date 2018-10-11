package com.sirma.sep.content.rendition;

import static com.sirma.itt.seip.domain.instance.DefaultProperties.THUMBNAIL_IMAGE;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import com.sirma.itt.seip.collections.CollectionUtils;
import com.sirma.itt.seip.concurrent.TaskExecutor;
import com.sirma.itt.seip.configuration.Options;
import com.sirma.itt.seip.domain.instance.Instance;

/**
 * Default implementation for the rendition service.
 *
 * @author BBonev
 */
@ApplicationScoped
public class RenditionServiceImpl implements RenditionService {

	@Inject
	private TaskExecutor taskExecutor;
	@Inject
	private ThumbnailService thumbnailService;
	@Inject
	private ThumbnailDao thumbnailDao;

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

		if (instance.isValueNull(THUMBNAIL_IMAGE)) {
			String thumbnail = thumbnailDao.loadThumbnail(instance.getId(), purpose);
			if (ThumbnailService.MAX_RETRIES.equals(thumbnail)) {
				return null;
			}
			if (thumbnail == null) {
				scheduleThumbnailCheck(Collections.singleton(instance.getId()));
			}

			instance.add(THUMBNAIL_IMAGE, thumbnail);
			return thumbnail;
		}
		// if already loaded return it
		return instance.getString(THUMBNAIL_IMAGE);
	}

	@Override
	public String getDefaultThumbnail(Instance instance) {
		String thumbnailForInstance = getThumbnailForInstance(instance, DEFAULT_PURPOSE);
		if (thumbnailForInstance == null) {
			return null;
		}
		if (!thumbnailForInstance.startsWith("data:image")) {
			thumbnailForInstance = BASE64_IMAGE_PREFIX + thumbnailForInstance;
		}
		return thumbnailForInstance;
	}

	@Override
	public String getThumbnail(Serializable id) {
		if (id instanceof Instance) {
			// for backward compatibility
			return thumbnailDao.loadThumbnail(((Instance) id).getId(), DEFAULT_PURPOSE);
		}
		return thumbnailDao.loadThumbnail(id, DEFAULT_PURPOSE);
	}

	@Override
	public String getThumbnail(Serializable id, String purpose) {
		return thumbnailDao.loadThumbnail(id, DEFAULT_PURPOSE);
	}

	@Override
	public <I extends Instance> I loadThumbnail(I instance) {
		getThumbnailForInstance(instance, DEFAULT_PURPOSE);
		return instance;
	}

	@Override
	public <I extends Instance> void loadThumbnails(Collection<I> instances) {
		if (CollectionUtils.isEmpty(instances) || Options.DISABLE_POST_INSTANCE_LOAD_DECORATION.isEnabled()) {
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
		Map<Serializable, String> thumbnails = thumbnailDao.loadThumbnails(mapping.keySet(), DEFAULT_PURPOSE);
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
		return thumbnailDao.loadThumbnails(ids, DEFAULT_PURPOSE);
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
		// create a copy eagerly otherwise the collection may be cleared at the time of accessing it
		// in new thread
		Collection<Serializable> list = new ArrayList<>(ids);
		taskExecutor.executeAsyncInTx(() -> thumbnailService.scheduleCheck(list));
	}

	/**
	 * Sets the thumbnail to instance.
	 *
	 * @param instance
	 *            the instance
	 * @param thumbnail
	 *            the thumbnail
	 */
	@SuppressWarnings("static-method")
	protected void setThumbnailToInstance(Instance instance, Serializable thumbnail) {
		instance.addIfNotPresent(THUMBNAIL_IMAGE, thumbnail);
	}
}
