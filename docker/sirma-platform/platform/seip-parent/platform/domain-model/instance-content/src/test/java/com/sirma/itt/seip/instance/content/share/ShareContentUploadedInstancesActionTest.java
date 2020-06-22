package com.sirma.itt.seip.instance.content.share;

import com.sirma.itt.seip.tasks.SchedulerContext;
import com.sirma.itt.seip.tasks.SchedulerRetryException;
import com.sirma.sep.content.ContentImport;
import com.sirma.sep.content.ContentInfo;
import com.sirma.sep.content.InstanceContentService;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.io.Serializable;

import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Tests for {@link ShareContentUploadedInstancesAction}.
 *
 * @author <a href="mailto:ivo.rusev@sirma.bg">Ivo Rusev</a>
 * @since 13/09/2017
 */
public class ShareContentUploadedInstancesActionTest {

	@InjectMocks
	private ShareContentUploadedInstancesAction cut;

	@Mock
	private InstanceContentService instanceContentService;

	@Before
	public void init() {
		MockitoAnnotations.initMocks(this);
		ContentInfo content = mock(ContentInfo.class);
		when(content.exists()).thenReturn(true);
		when(instanceContentService.getContent(any(Serializable.class), any(String.class))).thenReturn(content);
		when(instanceContentService.importContent(any(ContentImport.class))).thenReturn(null);
	}

	@Test(expected = SchedulerRetryException.class)
	public void test_execute_noContentsImported() throws Exception {
		SchedulerContext context = BaseShareInstanceContentAction.createContext("instanceId", "title", "token",
																				"contentId", "format");

		cut.execute(context);
	}

	@Test
	public void test_execute() throws Exception {
		SchedulerContext context = BaseShareInstanceContentAction.createContext("instanceId", "title", "token",
																				"contentId", "format");
		when(instanceContentService.importContent(any(ContentImport.class))).thenReturn("something");
		cut.execute(context);
		verify(instanceContentService).importContent(any(ContentImport.class));
	}

}