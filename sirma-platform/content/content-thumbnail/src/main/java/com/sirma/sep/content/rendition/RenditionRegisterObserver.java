package com.sirma.sep.content.rendition;

import static com.sirma.itt.seip.instance.relation.LinkConstants.HAS_THUMBNAIL;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.event.Observes;
import javax.inject.Inject;

import com.sirma.itt.seip.domain.instance.Instance;
import com.sirma.itt.seip.domain.instance.InstancePropertyNameResolver;
import com.sirma.itt.seip.domain.instance.InstanceReference;
import com.sirma.itt.seip.instance.InstanceTypeResolver;
import com.sirma.itt.seip.instance.event.AfterInstanceDeleteEvent;
import com.sirma.itt.seip.instance.event.AfterInstancePersistEvent;
import com.sirma.sep.content.event.ContentAssignedEvent;
import com.sirma.sep.content.event.ContentUpdatedEvent;

/**
 * Observer to listen for particular events and to register the instance for thumbnail generation.
 *
 * @author BBonev
 */
@ApplicationScoped
public class RenditionRegisterObserver {

	@Inject
	private ThumbnailService thumbnailService;
	@Inject
	private InstanceTypeResolver instanceResolver;
	@Inject
	private InstancePropertyNameResolver nameResolver;

	/**
	 * Register the newly uploaded instance for thumbnail retrieval. This cannot be done in the content assigned
	 * event (the method below) because the newly created instance cannot be resolved.
	 *
	 * @param event {@link AfterInstancePersistEvent} which triggers thumbnail registration
	 */
	public <I extends Instance> void onUploaded(@Observes AfterInstancePersistEvent<I, ?> event) {
		Instance instance = event.getInstance();
		if (shouldRegister(instance)) {
			thumbnailService.register(instance);
		}
	}

	private boolean shouldRegister(Instance instance) {
		return instance.isUploaded() && !instance.isPropertyPresent(HAS_THUMBNAIL, nameResolver);
	}

	/**
	 * Updates instance thumbnail, when there is a change in the instance content.
	 *
	 * @param event {@link ContentAssignedEvent} which triggers thumbnail update
	 */
	public void onThumbnailChange(@Observes ContentAssignedEvent event) {
		instanceResolver
				.resolveReference(event.getInstanceId())
				.map(InstanceReference::toInstance)
				.filter(this::shouldRegister)
				.ifPresent(thumbnailService::register);
	}

	/**
	 * Removes a thumbnail references for all instance that were using the currently removed document
	 *
	 * @param event {@link AfterInstanceDeleteEvent} which triggers the delete of the thumbnail entry
	 */
	public <I extends Instance> void onDeleted(@Observes AfterInstanceDeleteEvent<I, ?> event) {
		Instance instance = event.getInstance();
		if (instance == null) {
			return;
		}

		thumbnailService.deleteThumbnail(instance.getId());
	}
}
