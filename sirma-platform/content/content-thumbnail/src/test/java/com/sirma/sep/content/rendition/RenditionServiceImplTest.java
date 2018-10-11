package com.sirma.sep.content.rendition;

import static org.junit.Assert.assertNull;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Matchers;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

import com.sirma.itt.seip.Executable;
import com.sirma.itt.seip.concurrent.TaskExecutor;
import com.sirma.itt.seip.domain.instance.DefaultProperties;
import com.sirma.itt.seip.domain.instance.EmfInstance;
import com.sirma.itt.seip.domain.instance.Instance;
import com.sirma.sep.content.rendition.RenditionService;
import com.sirma.sep.content.rendition.RenditionServiceImpl;
import com.sirma.sep.content.rendition.ThumbnailDao;
import com.sirma.sep.content.rendition.ThumbnailService;

/**
 * Tests the {@link RenditionService} implementation.
 *
 * @author Mihail Radkov
 */
public class RenditionServiceImplTest {

	@InjectMocks
	private RenditionServiceImpl renditionService;

	@Mock
	private ThumbnailDao thumbnailDao;

	@Mock
	private TaskExecutor taskExecutor;

	@Mock
	private ThumbnailService thumbnailService;

	@Before
	public void initialize() {
		MockitoAnnotations.initMocks(this);
		when(taskExecutor.executeAsyncInTx(any(Executable.class))).then(a -> {
					a.getArgumentAt(0, Executable.class).execute();
					return null;
				});
	}

	/**
	 * Tests the primary thumbnail retrieval when the provided instance has no property associated to
	 * {@link DefaultProperties#THUMBNAIL_IMAGE}.
	 */
	@Test
	public void testGetPrimaryThumbnail() {
		String thumbnailData = "thumbnail-data";
		Instance instance = new EmfInstance();
		instance.setId("emf:123");
		mockThumbnailDao("emf:123", "primary", thumbnailData);

		String thumbnail = renditionService.getPrimaryThumbnail(instance);
		Assert.assertEquals(thumbnailData, thumbnail);
		Assert.assertEquals(thumbnailData, instance.get(DefaultProperties.THUMBNAIL_IMAGE));
		// Should have called the DAO
		verify(thumbnailDao, Mockito.times(1)).loadThumbnail(any(), any());
	}

	/**
	 * Tests the primary thumbnail retrieval when the provided instance has a property associated to
	 * {@link DefaultProperties#THUMBNAIL_IMAGE}.
	 */
	@Test
	public void testGetPrimaryThumbnailWithExistingProperty() {
		String thumbnailData = "thumbnail-data";
		Instance instance = new EmfInstance();
		instance.add(DefaultProperties.THUMBNAIL_IMAGE, thumbnailData);

		String thumbnail = renditionService.getPrimaryThumbnail(instance);
		Assert.assertEquals(thumbnailData, thumbnail);
		Assert.assertEquals(thumbnailData, instance.get(DefaultProperties.THUMBNAIL_IMAGE));
		// Should NOT have called the DAO
		verify(thumbnailDao, Mockito.times(0)).loadThumbnail(any(), any());
	}

	@Test
	public void testGetNullThumbnail() {
		// Setup
		String thumbnailData = null;
		Instance instance = new EmfInstance();
		instance.setId("emf:456");
		mockThumbnailDao("emf:456", "default", thumbnailData);
		Collection<Serializable> ids = new ArrayList<>(1);
		ids.add(instance.getId());
		// Service call
		String thumbnail = renditionService.getPrimaryThumbnail(instance);
		// Asserts
		// We check if the returned thumbnail is null and that the schedule check has been invoked with the proper ids.
		Assert.assertNull(thumbnail);
		verify(thumbnailService).scheduleCheck(ids);
	}

	@Test
	public void getPrimaryThumbnail_nullInstance() {
		assertNull(renditionService.getPrimaryThumbnail(null));
	}

	@Test
	public void getPrimaryThumbnail_reachedMaxRetries() {
		EmfInstance instance = new EmfInstance();
		instance.setId("instnace-id");
		when(thumbnailDao.loadThumbnail("instnace-id", "primary")).thenReturn(ThumbnailService.MAX_RETRIES);
		assertNull(renditionService.getPrimaryThumbnail(instance));
	}

	private void mockThumbnailDao(String instanceId, String purpose, String thumbnail) {
		when(thumbnailDao.loadThumbnail(Matchers.eq(instanceId), Matchers.eq(purpose))).thenReturn(thumbnail);
	}
}
