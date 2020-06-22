package com.sirma.sep.content.rendition;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import com.sirma.itt.seip.collections.CollectionUtils;
import com.sirma.itt.seip.concurrent.TaskExecutor;
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
	public String getThumbnail(Serializable id) {
		Serializable instanceId = id;
		if (id instanceof Instance) {
			// for backward compatibility
			instanceId = ((Instance) id).getId();
		}
		return thumbnailDao.loadThumbnail(instanceId);
	}

	@Override
	public String getThumbnail(Serializable id, ThumbnailType type) {
		return thumbnailDao.loadThumbnail(id, Objects.requireNonNull(type, "Thumbnail type is required"));
	}

	@Override
	public <S extends Serializable> Map<S, String> getThumbnails(Collection<S> ids) {
		// first get assigned thumbnails as they are with higher priority
		// then self thumbnails for the rest of the instances that does not have assigned thumbnails
		Map<S, String> assignedThumbnails = thumbnailDao.loadThumbnails(ids, ThumbnailType.ASSIGNED);
		List<S> notFound = ids.stream().filter(id -> !assignedThumbnails.containsKey(id)).collect(Collectors.toList());
		Map<S, String> selfThumbnails = thumbnailDao.loadThumbnails(notFound, ThumbnailType.SELF);
		Map<S, String> thumbnails = CollectionUtils.createHashMap(ids.size());
		thumbnails.putAll(selfThumbnails);
		thumbnails.putAll(assignedThumbnails);

		Set<Serializable> withoutThumbnails = ids.stream()
				.filter(id -> !thumbnails.containsKey(id))
				.collect(Collectors.toSet());
		scheduleThumbnailCheck(withoutThumbnails);
		return thumbnails;
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
}
