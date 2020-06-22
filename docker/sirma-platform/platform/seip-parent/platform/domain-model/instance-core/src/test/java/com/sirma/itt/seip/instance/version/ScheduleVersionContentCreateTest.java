package com.sirma.itt.seip.instance.version;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.argThat;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.InputStream;
import java.io.Serializable;
import java.util.Date;
import java.util.List;

import org.apache.commons.io.IOUtils;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import com.sirma.itt.seip.Pair;
import com.sirma.itt.seip.domain.instance.EmfInstance;
import com.sirma.itt.seip.domain.instance.Instance;
import com.sirma.itt.seip.exception.EmfConfigurationException;
import com.sirma.itt.seip.exception.EmfRuntimeException;
import com.sirma.itt.seip.tasks.SchedulerConfiguration;
import com.sirma.itt.seip.tasks.SchedulerContext;
import com.sirma.itt.seip.tasks.SchedulerService;
import com.sirma.itt.seip.tasks.TransactionMode;
import com.sirma.itt.seip.testutil.CustomMatcher;
import com.sirma.sep.content.Content;
import com.sirma.sep.content.ContentInfo;
import com.sirma.sep.content.InstanceContentService;

/**
 * Test for {@link ScheduleVersionContentCreate}.
 *
 * @author A. Kunchev
 */
public class ScheduleVersionContentCreateTest {

	@InjectMocks
	private ScheduleVersionContentCreate schedule;

	@Mock
	private SchedulerService schedulerService;

	@Mock
	private InstanceContentService instanceContentService;

	@Mock
	private VersionDao versionDao;

	@Before
	public void setup() {
		schedule = new ScheduleVersionContentCreate();
		MockitoAnnotations.initMocks(this);
	}

	@Test
	public void onCreateVersionContent_notUpdateVersionWithContent_contentSaveNotCalled() throws Exception {
		String versionId = "version-instance-id";
		String originalInstanceId = "original-instance-id";
		stubGetPrimaryViewContent(versionId, true);
		VersionContext context = VersionContext
				.create(new EmfInstance(originalInstanceId))
					.setVersionInstance(new EmfInstance(versionId))
					.setVersionMode(VersionMode.MINOR);
		schedule.onCreateVersionContent(new CreateVersionContentEvent(context));
		verify(instanceContentService, never()).getContent(eq(originalInstanceId), eq(Content.PRIMARY_VIEW));
		verify(instanceContentService, never()).saveContent(any(Instance.class), any(Content.class));
	}

	@Test(expected = IllegalArgumentException.class)
	public void onCreateVersionContent_emptyVersionId() {
		try {
			VersionContext context = VersionContext
					.create(new EmfInstance("id"), new Date(), Boolean.TRUE)
						.setVersionInstance(new EmfInstance(""));
			schedule.onCreateVersionContent(new CreateVersionContentEvent(context));
		} finally {
			verify(schedulerService, never()).schedule(eq("scheduleVersionContentCreate"),
					argThat(configurationMatcher()), argThat(contextMatcher()));
		}
	}

	@Test
	public void onCreateVersionContent_versionViewExistModeUpdate_originalViewContentDoNotExists_saveContent_notCalled() {
		String versionId = "version-instance-id";
		String originalInstanceId = "original-instance-id";
		stubGetPrimaryViewContent(originalInstanceId, false);
		stubGetPrimaryViewContent(versionId, true);
		VersionContext context = VersionContext
				.create(new EmfInstance(originalInstanceId), new Date(), Boolean.TRUE)
					.setVersionInstance(new EmfInstance(versionId))
					.setVersionMode(VersionMode.UPDATE);
		schedule.onCreateVersionContent(new CreateVersionContentEvent(context));
		verify(instanceContentService, never()).saveContent(any(Instance.class), any(Content.class));
	}

	private void stubGetPrimaryViewContent(String id, boolean isContentExsits) {
		ContentInfo info = mock(ContentInfo.class);
		when(info.exists()).thenReturn(isContentExsits);
		when(instanceContentService.getContent(id, Content.PRIMARY_VIEW)).thenReturn(info);
	}

	@Test
	public void onCreateVersionContent_allData() {
		String originalInstanceId = "original-instance-id";
		stubGetPrimaryViewContent(originalInstanceId, true);
		String versionInstanceId = "version-instance-id";
		stubGetPrimaryViewContent(versionInstanceId, false);
		VersionContext context = VersionContext
				.create(new EmfInstance(originalInstanceId), new Date(), Boolean.TRUE)
					.setVersionInstance(new EmfInstance(versionInstanceId));
		schedule.onCreateVersionContent(new CreateVersionContentEvent(context));
		verify(schedulerService).schedule(eq("scheduleVersionContentCreate"), argThat(configurationMatcher()),
				argThat(contextMatcher()));
	}

	private static CustomMatcher<SchedulerConfiguration> configurationMatcher() {
		return CustomMatcher.of((SchedulerConfiguration configuration) -> {
			assertTrue(configuration.isRemoveOnSuccess());
			assertTrue(configuration.isPersistent());
			assertEquals(TransactionMode.REQUIRED, configuration.getTransactionMode());
			assertEquals(5, configuration.getMaxRetryCount());
			assertTrue(configuration.isIncrementalDelay());
			assertEquals(5, configuration.getMaxActivePerGroup());
		});
	}

	private static CustomMatcher<SchedulerContext> contextMatcher() {
		return CustomMatcher.of((SchedulerContext context) -> {
			assertEquals("version-instance-id", context.get("versionInstanceId"));
			assertEquals("original-instance-id", context.get("originalInstanceId"));
			assertNotNull(context.getIfSameType("versionCreatedOn", Date.class));
			assertTrue(context.getIfSameType("processWidgets", Boolean.class));
		});
	}

	@Test
	public void execute_withOriginalViewContent_withWidgetProcessing_versionContentSaved() throws Exception {
		ContentInfo info = mock(ContentInfo.class);
		when(info.exists()).thenReturn(true);
		when(info.getInputStream()).thenReturn(getViewContent());
		when(instanceContentService.getContent(any(Serializable.class), eq(Content.PRIMARY_VIEW))).thenReturn(info);
		ContentInfo versionInfo = mock(ContentInfo.class);
		when(versionInfo.exists()).thenReturn(true);
		when(instanceContentService.saveContent(any(Serializable.class), any(Content.class))).thenReturn(versionInfo);
		SchedulerContext context = buildTestContext(Boolean.TRUE);
		schedule.execute(context);
		verify(info).getInputStream();
		verify(info, never()).asString();
		verify(instanceContentService, times(1)).saveContent(any(Instance.class), any(Content.class));
	}

	@Test
	public void execute_withOriginalViewContent_withoutWidgetProcessing_versionContentSaved() throws Exception {
		ContentInfo info = mock(ContentInfo.class);
		when(info.exists()).thenReturn(true);
		when(info.asString()).thenReturn(IOUtils.toString(getViewContent()));
		when(instanceContentService.getContent(any(Serializable.class), eq(Content.PRIMARY_VIEW))).thenReturn(info);
		ContentInfo versionInfo = mock(ContentInfo.class);
		when(versionInfo.exists()).thenReturn(true);
		when(instanceContentService.saveContent(any(Serializable.class), any(Content.class))).thenReturn(versionInfo);
		SchedulerContext context = buildTestContext(Boolean.FALSE);
		schedule.execute(context);
		verify(info).asString();
		verify(info, never()).getInputStream();
		verify(instanceContentService, times(1)).saveContent(any(Instance.class), any(Content.class));
	}

	@Test(expected = EmfRuntimeException.class)
	public void execute_withOriginalViewContent_versionContentSaveFailed() throws Exception {
		ContentInfo info = mock(ContentInfo.class);
		when(info.exists()).thenReturn(true);
		when(info.getInputStream()).thenReturn(getViewContent());
		when(instanceContentService.getContent(any(Serializable.class), eq(Content.PRIMARY_VIEW))).thenReturn(info);
		ContentInfo versionInfo = mock(ContentInfo.class);
		when(versionInfo.exists()).thenReturn(false);
		when(instanceContentService.saveContent(any(Serializable.class), any(Content.class))).thenReturn(versionInfo);
		SchedulerContext context = buildTestContext(Boolean.TRUE);
		schedule.execute(context);
	}

	private static SchedulerContext buildTestContext(Boolean processWidgets) {
		SchedulerContext context = new SchedulerContext(6);
		context.put("contentId", "content-id");
		context.put("versionInstanceId", "version-instance-id");
		context.put("originalInstanceId", "original-instance-id");
		context.put("versionCreatedOn", new Date());
		context.put("processWidgets", processWidgets);
		context.put("isVersionModeUpdate", false);
		return context;
	}

	private static InputStream getViewContent() {
		return CopyContentOnNewVersionStepTest.class
				.getClassLoader()
					.getResourceAsStream("idoc-view-content-versions-test.html");
	}

	@Test(expected = EmfConfigurationException.class)
	public void validateInput_nullVersionDate() throws Exception {
		SchedulerContext context = buildTestContext(Boolean.TRUE);
		context.replace("versionCreatedOn", null);
		schedule.beforeExecute(context);
	}

	@Test
	public void validateInput() {
		List<Pair<String, Class<?>>> validateInput = schedule.validateInput();
		assertFalse(validateInput.isEmpty());
		assertEquals(4, validateInput.size());
	}
}
