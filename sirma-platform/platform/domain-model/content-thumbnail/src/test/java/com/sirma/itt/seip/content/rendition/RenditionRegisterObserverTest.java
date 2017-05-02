package com.sirma.itt.seip.content.rendition;

import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.Serializable;
import java.util.Optional;

import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import com.sirma.itt.seip.content.Content;
import com.sirma.itt.seip.content.event.ContentUpdatedEvent;
import com.sirma.itt.seip.domain.instance.DefaultProperties;
import com.sirma.itt.seip.domain.instance.EmfInstance;
import com.sirma.itt.seip.domain.instance.Instance;
import com.sirma.itt.seip.domain.instance.InstanceReference;
import com.sirma.itt.seip.instance.InstanceTypeResolver;
import com.sirma.itt.seip.instance.event.AfterInstanceDeleteEvent;
import com.sirma.itt.seip.instance.event.AfterInstancePersistEvent;

/**
 * Tests for {@link RenditionRegisterObserver}.
 *
 * @author A. Kunchev
 */
public class RenditionRegisterObserverTest {

	@InjectMocks
	private RenditionRegisterObserver observer;

	@Mock
	private ThumbnailService thumbnailService;

	@Mock
	private InstanceTypeResolver instanceTypeResolver;

	@Before
	public void setup() {
		observer = new RenditionRegisterObserver();
		MockitoAnnotations.initMocks(this);
	}

	@Test
	public void onUploaded_nullInstance_serviceNotCalled() {
		observer.onUploaded(new AfterInstancePersistEvent<>(null));
		verify(thumbnailService, never()).register(any(Instance.class));
	}

	@Test
	public void onUploaded_thumbnailRegistered() {
		EmfInstance instance = new EmfInstance();
		instance.add(DefaultProperties.PRIMARY_CONTENT_ID, "content-id");
		observer.onUploaded(new AfterInstancePersistEvent<>(instance));
		verify(thumbnailService).register(instance);
	}

	@Test
	public void onDeleted_nullInstance_serviceNotCalled() {
		observer.onDeleted(new AfterInstanceDeleteEvent<>(null));
		verify(thumbnailService, never()).deleteThumbnail(any(Serializable.class));
	}

	@Test
	public void onDeleted_thumbnailDeleted() {
		EmfInstance instance = new EmfInstance();
		instance.setId("instance-id");
		observer.onDeleted(new AfterInstanceDeleteEvent<>(instance));
		verify(thumbnailService).deleteThumbnail("instance-id");
	}

	@Test
	public void onThumbnailChange_nullInstance_serviceNotCalled() {
		observer.onThumbnailChange(new ContentUpdatedEvent(null, null, null));
		verify(thumbnailService, never()).register(any(Instance.class), any(Instance.class));
	}

	@Test
	public void onThumbnailChange_notPrimaryContentPurpose_serviceNotCalled() {
		EmfInstance instance = new EmfInstance();
		instance.add(DefaultProperties.PRIMARY_CONTENT_ID, "content-id");
		prepareInstanceResolving(instance);
		Content content = Content.createEmpty().setPurpose(Content.PRIMARY_VIEW);
		observer.onThumbnailChange(new ContentUpdatedEvent(instance, content, null));
		verify(thumbnailService, never()).register(instance, instance);
	}

	@Test
	public void onThumbnailChange_thumbnailChanged() {
		EmfInstance instance = new EmfInstance();
		instance.add(DefaultProperties.PRIMARY_CONTENT_ID, "content-id");
		prepareInstanceResolving(instance);
		Content content = Content.createEmpty().setPurpose(Content.PRIMARY_CONTENT);
		observer.onThumbnailChange(new ContentUpdatedEvent(instance, content, null));
		verify(thumbnailService).register(instance, instance);
	}

	@Test
	public void onThumbnailChange_nullResolvedInstance() {
		EmfInstance instance = new EmfInstance();
		prepareInstanceResolving(null);
		Content content = Content.createEmpty().setPurpose(Content.PRIMARY_CONTENT);
		observer.onThumbnailChange(new ContentUpdatedEvent(instance, content, null));
		verify(thumbnailService, never()).register(instance, instance);
	}

	@Test
	public void onThumbnailChange_nonInstanceOwner() {
		Content content = Content.createEmpty().setPurpose(Content.PRIMARY_CONTENT);
		observer.onThumbnailChange(new ContentUpdatedEvent("", content, null));
		verify(thumbnailService, never()).register(any(), any());
	}

	private void prepareInstanceResolving(Instance instanceToReturn) {
		InstanceReference reference = mock(InstanceReference.class);
		when(reference.toInstance()).thenReturn(instanceToReturn);
		when(instanceTypeResolver.resolveReference(any())).thenReturn(Optional.of(reference));
	}

}
