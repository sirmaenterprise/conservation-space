package com.sirma.itt.seip.instance.content.share;

import com.sirma.itt.seip.tasks.DefaultSchedulerConfiguration;
import com.sirma.itt.seip.tasks.RunAs;
import com.sirma.itt.seip.tasks.SchedulerConfiguration;
import com.sirma.itt.seip.tasks.SchedulerContext;
import com.sirma.itt.seip.tasks.SchedulerEntryType;
import com.sirma.itt.seip.tasks.SchedulerService;
import com.sirma.itt.seip.tasks.TransactionMode;
import org.junit.Before;
import org.junit.Test;
import org.mockito.MockitoAnnotations;

import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Tests for {@link BaseShareInstanceContentAction}.
 *
 * @author <a href="mailto:ivo.rusev@sirma.bg">Ivo Rusev</a>
 * @since 12/09/2017
 */
public class BaseShareInstanceContentActionTest {

	@Before
	public void init() {
		MockitoAnnotations.initMocks(this);
	}

	@Test
	public void test_buildImmediateConfiguration() throws Exception {
		SchedulerConfiguration schedulerConfiguration = BaseShareInstanceContentAction.buildImmediateConfiguration();
		assertEquals(SchedulerEntryType.TIMED, schedulerConfiguration.getType());
		assertEquals(TransactionMode.REQUIRED, schedulerConfiguration.getTransactionMode());
		assertEquals(10, schedulerConfiguration.getMaxRetryCount());
		assertEquals(RunAs.USER, schedulerConfiguration.getRunAs());
	}

	@Test
	public void test_buildEventConfiguration() throws Exception {
		SchedulerService scheduleServiceMock = mock(SchedulerService.class);
		String taskIdentifier = "id";
		when(scheduleServiceMock.buildConfiguration(any(ShareInstanceContentEvent.class))).thenReturn(
				new DefaultSchedulerConfiguration());
		SchedulerConfiguration schedulerConfiguration = BaseShareInstanceContentAction.buildEventConfiguration(
				scheduleServiceMock, taskIdentifier);
		assertEquals(10, schedulerConfiguration.getMaxRetryCount());
		assertEquals(RunAs.USER, schedulerConfiguration.getRunAs());
		assertEquals(taskIdentifier, schedulerConfiguration.getIdentifier());
	}

	@Test
	public void test_createContext() throws Exception {
		String contentId = "emf:123";
		String token = "jwt:token";
		String format = "pdf";
		SchedulerContext context = BaseShareInstanceContentAction.createContext("emf:id", "title", token, contentId,
																				format);
		ContentShareData data = context.getIfSameType(BaseShareInstanceContentAction.DATA, ContentShareData.class);
		assertEquals("emf:id", data.getInstanceId());
		assertEquals("title", data.getTitle());
		assertEquals(token, data.getToken());
		assertEquals(contentId, data.getContentId());
		assertEquals(format, data.getFormat());
	}
}
