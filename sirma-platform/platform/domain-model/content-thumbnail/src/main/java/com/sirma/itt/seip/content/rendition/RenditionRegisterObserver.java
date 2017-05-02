package com.sirma.itt.seip.content.rendition;

import static com.sirma.itt.seip.content.Content.PRIMARY_CONTENT;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.event.Observes;
import javax.inject.Inject;

import com.sirma.itt.seip.content.event.ContentUpdatedEvent;
import com.sirma.itt.seip.domain.instance.Instance;
import com.sirma.itt.seip.domain.instance.InstanceReference;
import com.sirma.itt.seip.instance.InstanceTypeResolver;
import com.sirma.itt.seip.instance.event.AfterInstanceDeleteEvent;
import com.sirma.itt.seip.instance.event.AfterInstancePersistEvent;

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

	/**
	 * Register the newly uploaded instance for thumbnail retrieval.
	 *
	 * @param event
	 *            {@link AfterInstancePersistEvent} which triggers thumbnail registration
	 */
	public <I extends Instance> void onUploaded(@Observes AfterInstancePersistEvent<I, ?> event) {
		Instance instance = event.getInstance();
		if (instance == null || !instance.isUploaded()) {
			return;
		}

		thumbnailService.register(instance);
	}

	/**
	 * Removes a thumbnail references for all instance that were using the currently removed document
	 *
	 * @param event
	 *            {@link AfterInstanceDeleteEvent} which triggers the delete of the thumbnail entry
	 */
	public <I extends Instance> void onDeleted(@Observes AfterInstanceDeleteEvent<I, ?> event) {
		Instance instance = event.getInstance();
		if (instance == null) {
			return;
		}

		thumbnailService.deleteThumbnail(instance.getId());
	}

	/**
	 * Updates instance thumbnail, when there is a change in the instance content.
	 *
	 * @param event
	 *            {@link ContentUpdatedEvent} which triggers thumbnail update
	 */
	public void onThumbnailChange(@Observes ContentUpdatedEvent event) {
		if (!(event.getOwner() instanceof Instance)) {
			return;
		}
		Instance dummyInstance = (Instance) event.getOwner();
		if (dummyInstance == null || !PRIMARY_CONTENT.equals(event.getContent().getPurpose())) {
			return;
		}

		Instance instance = instanceResolver
				.resolveReference(dummyInstance.getId())
					.map(InstanceReference::toInstance)
					.orElse(null);

		if (instance == null || !instance.isUploaded()) {
			return;
		}

		// we modify the entry in the DB instead of adding new one
		thumbnailService.register(instance, instance);
	}

}
