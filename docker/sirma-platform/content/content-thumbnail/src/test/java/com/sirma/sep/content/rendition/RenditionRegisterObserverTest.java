package com.sirma.sep.content.rendition;

import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyZeroInteractions;
import static org.mockito.Mockito.when;

import java.util.Optional;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.runners.MockitoJUnitRunner;

import com.sirma.itt.seip.domain.instance.DefaultProperties;
import com.sirma.itt.seip.domain.instance.EmfInstance;
import com.sirma.itt.seip.domain.instance.Instance;
import com.sirma.itt.seip.domain.instance.InstancePropertyNameResolver;
import com.sirma.itt.seip.domain.instance.InstanceReference;
import com.sirma.itt.seip.instance.InstanceTypeResolver;
import com.sirma.itt.seip.instance.event.AfterInstanceDeleteEvent;
import com.sirma.itt.seip.instance.event.AfterInstancePersistEvent;
import com.sirma.itt.seip.instance.relation.LinkConstants;
import com.sirma.sep.content.event.ContentAssignedEvent;

/**
 * Tests for {@link RenditionRegisterObserver}.
 *
 * @author A. Kunchev
 */
@RunWith(MockitoJUnitRunner.class)
public class RenditionRegisterObserverTest {

	@InjectMocks
	private RenditionRegisterObserver observer;

	@Mock
	private ThumbnailService thumbnailService;

	@Mock
	private InstanceTypeResolver instanceTypeResolver;

	@Spy
	private InstancePropertyNameResolver nameResolver = InstancePropertyNameResolver.NO_OP_INSTANCE;

	@Test
	public void onUploaded_creatableInstance_serviceNotCalled() {
		observer.onUploaded(new AfterInstancePersistEvent<>(new EmfInstance()));
		verifyZeroInteractions(thumbnailService);
	}

	@Test
	public void onUploaded_resultInRegisteredThumbnail() {
		EmfInstance instance = new EmfInstance();
		instance.add(DefaultProperties.PRIMARY_CONTENT_ID, "content-id");
		observer.onUploaded(new AfterInstancePersistEvent<>(instance));
		verify(thumbnailService).register(instance);
	}

	@Test
	public void onUploaded_hasThumbnailPropertyPresent_thumbnailServiceNotCalled() {
		EmfInstance instance = new EmfInstance();
		instance.add(DefaultProperties.PRIMARY_CONTENT_ID, "content-id");
		instance.add(LinkConstants.HAS_THUMBNAIL, "some-instance-id");
		observer.onUploaded(new AfterInstancePersistEvent<>(instance));
		verifyZeroInteractions(thumbnailService);
	}

	@Test
	public void onDeleted_nullInstance_serviceNotCalled() {
		observer.onDeleted(new AfterInstanceDeleteEvent<>(null));
		verifyZeroInteractions(thumbnailService);
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
		prepareInstanceResolving(null);
		observer.onThumbnailChange(new ContentAssignedEvent("instance-id", null));
		verifyZeroInteractions(thumbnailService);
	}

	@Test
	public void onThumbnailChange_notUploadable_serviceNotCalled() {
		EmfInstance instance = new EmfInstance("instance-id");
		prepareInstanceResolving(instance);
		observer.onThumbnailChange(new ContentAssignedEvent("instance-id", null));
		verifyZeroInteractions(thumbnailService);
	}

	@Test
	public void onThumbnailChange_hasThumbnailPropertyPresent_serviceNotCalled() {
		EmfInstance instance = new EmfInstance("instance-id");
		instance.add(DefaultProperties.PRIMARY_CONTENT_ID, "content-id");
		instance.add(LinkConstants.HAS_THUMBNAIL, "some-instance-id");
		prepareInstanceResolving(instance);
		observer.onThumbnailChange(new ContentAssignedEvent("instance-id", null));
		verifyZeroInteractions(thumbnailService);
	}

	@Test
	public void onThumbnailChange_thumbnailChanged() {
		EmfInstance instance = new EmfInstance("instance-id");
		instance.add(DefaultProperties.PRIMARY_CONTENT_ID, "content-id");
		prepareInstanceResolving(instance);
		observer.onThumbnailChange(new ContentAssignedEvent("instance-id", null));
		verify(thumbnailService).register(any(Instance.class));
	}

	private void prepareInstanceResolving(Instance instanceToReturn) {
		InstanceReference reference = mock(InstanceReference.class);
		when(reference.toInstance()).thenReturn(instanceToReturn);
		when(instanceTypeResolver.resolveReference(any())).thenReturn(Optional.of(reference));
	}
}