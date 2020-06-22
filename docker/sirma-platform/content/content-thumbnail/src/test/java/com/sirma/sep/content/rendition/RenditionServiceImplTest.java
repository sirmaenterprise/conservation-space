package com.sirma.sep.content.rendition;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyCollection;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyZeroInteractions;
import static org.mockito.Mockito.when;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.Spy;

import com.sirma.itt.seip.concurrent.TaskExecutor;
import com.sirma.itt.seip.testutil.fakes.TaskExecutorFake;

/**
 * Tests the {@link RenditionServiceImpl}.
 *
 * @author Borislav Bonev
 */
public class RenditionServiceImplTest {

	private static final String ASSIGNED_THUMBNAIL = "assigned thumbnail";
	private static final String SELF_THUMBNAIL = "self thumbnail";

	@InjectMocks
	private RenditionServiceImpl renditionService;

	@Mock
	private ThumbnailDao thumbnailDao;

	@Spy
	private TaskExecutor taskExecutor = new TaskExecutorFake();

	@Mock
	private ThumbnailService thumbnailService;

	@Before
	public void initialize() {
		MockitoAnnotations.initMocks(this);
	}

	@Test
	public void getThumbnails_shouldLoadAssignedAndSelfThumbnails() {
		mockThumbnailsDB();
		List<String> requestIds = Arrays.asList("emf:instance-1", "emf:instance-2", "emf:instance-3", "emf:instance-4",
				"emf:instance-5");

		Map<String, String> thumbnails = renditionService.getThumbnails(requestIds);

		assertEquals(ASSIGNED_THUMBNAIL, thumbnails.get("emf:instance-1"));
		assertEquals(SELF_THUMBNAIL, thumbnails.get("emf:instance-2"));
		assertEquals(ASSIGNED_THUMBNAIL, thumbnails.get("emf:instance-3"));
		assertEquals(SELF_THUMBNAIL, thumbnails.get("emf:instance-4"));
		assertEquals(ASSIGNED_THUMBNAIL, thumbnails.get("emf:instance-1"));

		verifyZeroInteractions(thumbnailService);
	}

	@Test
	public void getThumbnails_shouldScheduleCheckForNotFoundThumbnails() {
		mockNotThumbnails();
		List<String> requestIds = Arrays.asList("emf:instance-1", "emf:instance-4");

		Map<String, String> thumbnails = renditionService.getThumbnails(requestIds);

		assertTrue(thumbnails.isEmpty());
		verify(thumbnailService).scheduleCheck(requestIds);
	}

	@SuppressWarnings("unchecked")
	private void mockThumbnailsDB() {
		when(thumbnailDao.loadThumbnails(anyCollection(), any())).then(a -> {
			Collection<String> ids = a.getArgumentAt(0, Collection.class);
			ThumbnailType thumbnailType = a.getArgumentAt(1, ThumbnailType.class);
			// only odd ids have assigned thumbnail
			if (thumbnailType == ThumbnailType.ASSIGNED) {
				return ids.stream()
						.filter(id -> Integer.valueOf(id.substring(id.lastIndexOf('-') + 1)) % 2 == 1)
						.collect(Collectors.toMap(
								Function.identity(), i -> ASSIGNED_THUMBNAIL));
			}
			// all have self thumbnails
			return ids.stream().collect(Collectors.toMap(Function.identity(), i -> SELF_THUMBNAIL));
		});
	}

	@SuppressWarnings("unchecked")
	private void mockNotThumbnails() {
		when(thumbnailDao.loadThumbnails(anyCollection(), any())).thenReturn(Collections.emptyMap());
	}
}
