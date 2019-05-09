package com.sirma.sep.content.rendition;

import java.io.Serializable;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import javax.inject.Inject;

import com.sirma.itt.seip.domain.instance.DefaultProperties;
import com.sirma.itt.seip.domain.instance.Instance;
import com.sirma.itt.seip.instance.dao.InstanceLoadDecorator;
import com.sirma.itt.seip.plugin.Extension;

/**
 * Concrete implementation of the InstanceLoadDecorator. The implementation loads thumbnails to the passed instances.
 * This extension is collected and called in the concrete implementation of InstanceLoadDecorator -
 * ChaningInstanceLoadDecorator.
 *
 * @author A. Kunchev
 */
@Extension(target = InstanceLoadDecorator.INSTANCE_DECORATOR, order = 10)
@Extension(target = InstanceLoadDecorator.VERSION_INSTANCE_DECORATOR, enabled = true, order = 10)
public class RenditionServiceInstanceLoadDecorator implements InstanceLoadDecorator {

	@Inject
	private RenditionService renditionService;

	@Override
	public <I extends Instance> void decorateInstance(I instance) {
		Serializable instanceId = instance.getId();
		instance.getOrCreateProperties()
				.computeIfAbsent(DefaultProperties.THUMBNAIL_IMAGE, k -> renditionService.getThumbnail(instanceId));
	}

	@Override
	public <I extends Instance> void decorateResult(Collection<I> collection) {
		List<Serializable> instancesWithoutThumbnails = collection.stream()
				.filter(this::withoutThumbnail)
				.map(Instance::getId)
				.collect(Collectors.toList());
		Map<Serializable, String> thumbnails = renditionService.getThumbnails(instancesWithoutThumbnails);
		collection.stream().filter(this::withoutThumbnail)
				.forEach(instance -> addThumbnailToInstance(thumbnails, instance));

	}

	private <I extends Instance> boolean withoutThumbnail(I instance) {
		return instance.isValueNull(DefaultProperties.THUMBNAIL_IMAGE);
	}

	private <I extends Instance> boolean addThumbnailToInstance(Map<Serializable, String> thumbnails, I instance) {
		return instance.addIfNotNull(DefaultProperties.THUMBNAIL_IMAGE, thumbnails.get(instance.getId()));
	}
}