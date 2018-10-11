package com.sirma.itt.seip.instance.version;

import static com.sirma.itt.seip.domain.instance.DefaultProperties.THUMBNAIL_IMAGE;
import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import com.sirma.itt.seip.domain.instance.EmfInstance;
import com.sirma.itt.seip.domain.instance.Instance;
import com.sirma.itt.seip.domain.instance.InstanceReference;
import com.sirma.itt.seip.testutil.mocks.InstanceReferenceMock;
import com.sirma.sep.content.rendition.ThumbnailService;

/**
 * Test for {@link VersionThumbnailStep}.
 *
 * @author A. Kunchev
 */
public class VersionThumbnailStepTest {

	@InjectMocks
	private VersionThumbnailStep step;

	@Mock
	private ThumbnailService thumbnailService;

	@Before
	public void setup() {
		step = new VersionThumbnailStep();
		MockitoAnnotations.initMocks(this);
	}

	@Test
	public void testGetName() {
		assertEquals("versionThumbnail", step.getName());
	}

	@Test
	public void execute_withoutVersionInstance_servicesNotCalled() {
		VersionContext context = VersionContext.create(new EmfInstance());
		step.execute(context);

		verify(thumbnailService, never()).addThumbnail(any(InstanceReference.class), eq("thumbnail-image"));
		verify(thumbnailService, never()).register(any(Instance.class));
	}

	@Test
	public void execute_thumbnailAsProperty_addThumbnailCalled() {
		EmfInstance target = new EmfInstance();
		InstanceReferenceMock.createGeneric(target);

		EmfInstance version = new EmfInstance();
		InstanceReferenceMock.createGeneric(version);
		version.add(THUMBNAIL_IMAGE, "thumbnail-image");

		VersionContext context = VersionContext.create(target);
		context.setVersionInstance(version);
		step.execute(context);

		verify(thumbnailService).addThumbnail(any(InstanceReference.class), eq("thumbnail-image"));
	}

	@Test
	public void execute_thumbnailForUploaded_registerThumbnailCalled() {
		EmfInstance target = new EmfInstance();
		InstanceReferenceMock.createGeneric(target);

		EmfInstance version = new EmfInstance();
		InstanceReferenceMock.createGeneric(version);

		VersionContext context = VersionContext.create(target);
		context.setVersionInstance(version);
		step.execute(context);

		verify(thumbnailService).register(any(Instance.class));
	}

}
