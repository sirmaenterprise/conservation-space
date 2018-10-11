package com.sirma.itt.seip.instance.content.share;

import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.Collection;
import java.util.Collections;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Tests for {@link ShareInstanceContentRestService}.
 *
 * @author <a href="mailto:ivo.rusev@sirma.bg">Ivo Rusev</a>
 * @since 11/09/2017
 */
public class ShareInstanceContentRestServiceTest {

	@InjectMocks
	private ShareInstanceContentRestService cut;

	@Mock
	private ShareInstanceContentService shareInstanceContentService;

	@Before
	public void init() {
		cut = new ShareInstanceContentRestService();
		MockitoAnnotations.initMocks(this);
	}

	@Test
	public void test_getSharedContentLink() throws Exception {
		when(shareInstanceContentService.getSharedContentURI(any(), any())).thenReturn("http://ses.com/");
		String url = cut.getSharedContentImmediate("emf:id", "pdf");
		assertEquals("http://ses.com/", url);
		verify(shareInstanceContentService, times(1)).getSharedContentURI(any(), any());
	}

	@Test
	public void test_createContentShareTask() throws Exception {
		when(shareInstanceContentService.createContentShareTask(any(), any())).thenReturn("http://ses.com/");
		String url = cut.createContentShareTask("emf:id", "pdf");
		assertEquals("http://ses.com/", url);
		verify(shareInstanceContentService, times(1)).createContentShareTask(any(), any());
	}

	@Test
	public void test_triggerTaskExecution() throws Exception {
		cut.triggerTaskExecution(any(String.class));
		verify(shareInstanceContentService, times(1)).triggerContentShareTask(any(String.class));
	}

	@Test
	public void test_createTasksExecution() throws Exception {
		when(shareInstanceContentService.createContentShareTasks(any(Collection.class), any())).thenReturn(
				Collections.singletonMap("emf:originalId", "http://ses.com/"));
		List<String> sharedUrl = cut.createContentShareTasks(Collections.singletonList("emf:originalId"), "pdf");
		assertEquals(sharedUrl.size(), 1);
		assertEquals(sharedUrl.get(0), "http://ses.com/");
		verify(shareInstanceContentService, times(1)).createContentShareTasks(any(Collection.class), any());
	}

	@Test
	public void test_triggerTasksExecution() throws Exception {
		cut.triggerTasksExecution(any(Collection.class));
		verify(shareInstanceContentService, times(1)).triggerContentShareTasks(any(Collection.class));
	}
}