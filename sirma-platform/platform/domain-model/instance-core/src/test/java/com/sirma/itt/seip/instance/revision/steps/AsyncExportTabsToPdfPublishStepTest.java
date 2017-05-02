package com.sirma.itt.seip.instance.revision.steps;

import static org.junit.Assert.fail;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.Serializable;
import java.util.Optional;
import java.util.concurrent.TimeoutException;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import com.sirma.itt.seip.content.Content;
import com.sirma.itt.seip.content.ContentInfo;
import com.sirma.itt.seip.content.InstanceContentService;
import com.sirma.itt.seip.domain.instance.EmfInstance;
import com.sirma.itt.seip.domain.instance.Instance;
import com.sirma.itt.seip.exception.EmfRuntimeException;
import com.sirma.itt.seip.exception.RollbackedRuntimeException;
import com.sirma.itt.seip.export.PDFExporter;
import com.sirma.itt.seip.instance.DomainInstanceService;
import com.sirma.itt.seip.instance.InstanceSaveContext;
import com.sirma.itt.seip.instance.revision.PublishInstanceRequest;
import com.sirma.itt.seip.instance.state.Operation;
import com.sirma.itt.seip.rest.secirity.SecurityTokensManager;
import com.sirma.itt.seip.security.User;
import com.sirma.itt.seip.security.context.SecurityContext;
import com.sirma.itt.seip.tasks.DefaultSchedulerConfiguration;
import com.sirma.itt.seip.tasks.SchedulerConfiguration;
import com.sirma.itt.seip.tasks.SchedulerContext;
import com.sirma.itt.seip.tasks.SchedulerEntryType;
import com.sirma.itt.seip.tasks.SchedulerService;
import com.sirmaenterprise.sep.content.idoc.Idoc;

/**
 * Test for {@link ExportTabsToPdfPublishStep}
 *
 * @author BBonev
 */
public class AsyncExportTabsToPdfPublishStepTest {

	private static final String TEST_IDOC = "/publish-idoc.html";
	private static final String EXPORT_ADDRESS = "/#/idoc/emf:instance?tab=abc2945a-ef95-41ed-8452-2bc812b7d3ac&tab=ec4983d3-aac2-4e16-f763-73c154008db3&jwt=jwtToken&mode=print";

	@InjectMocks
	private AsyncExportTabsToPdfPublishStep toPdfPublishStep;

	@Mock
	private PDFExporter exporter;
	@Mock
	private SecurityContext securityContext;
	@Mock
	private SecurityTokensManager tokensManager;
	@Mock
	private InstanceContentService instanceContentService;

	@Mock
	private SchedulerService schedulerService;
	@Mock
	private DomainInstanceService domainInstanceService;

	private File exportFile;

	@Before
	public void beforeMethod() throws TimeoutException {
		MockitoAnnotations.initMocks(this);
		when(securityContext.getAuthenticated()).thenReturn(mock(User.class));
		when(tokensManager.getCurrentJwtToken()).thenReturn(Optional.of("jwtToken"));

		when(schedulerService.buildEmptyConfiguration(SchedulerEntryType.TIMED))
				.then(a -> new DefaultSchedulerConfiguration().setType(a.getArgumentAt(0, SchedulerEntryType.class)));
		when(schedulerService.schedule(anyString(), any(SchedulerConfiguration.class), any(SchedulerContext.class)))
				.then(a -> {
					SchedulerContext context = a.getArgumentAt(2, SchedulerContext.class);
					toPdfPublishStep.beforeExecute(context);
					// force immediate execution of the async task
					toPdfPublishStep.execute(context);
					return null;
				});
		when(instanceContentService.saveContent(any(Serializable.class), any(Content.class))).then(a -> {
			ContentInfo info = mock(ContentInfo.class);
			when(info.getContentId()).thenReturn("contentId");
			when(info.getLength()).thenReturn(1L);
			when(info.getName()).thenReturn("exportedFile.pdf");
			when(info.getMimeType()).thenReturn("application/pdf");
			return info;
		});
		when(exporter.export(anyString())).then(a -> createExportFile());
	}

	@Test
	public void shouldExportTheMarkedTabs() throws Exception {
		Instance revision = new EmfInstance();
		revision.setId("emf:instance-r1.0");
		Instance instanceToPublish = new EmfInstance();
		instanceToPublish.setId("emf:instance");
		PublishInstanceRequest request = new PublishInstanceRequest(instanceToPublish, new Operation(), null, null);
		PublishContext context = new PublishContext(request, revision);
		loadView(context);
		mockValidRevision();

		toPdfPublishStep.execute(context);

		verify(exporter).export(EXPORT_ADDRESS);
		verify(domainInstanceService).save(any(InstanceSaveContext.class));
	}

	@Test(expected = EmfRuntimeException.class)
	public void shouldFailForNonExistingRevision() throws Exception {
		Instance revision = new EmfInstance();
		revision.setId("emf:instance-r1.0");
		Instance instanceToPublish = new EmfInstance();
		instanceToPublish.setId("emf:instance");
		PublishInstanceRequest request = new PublishInstanceRequest(instanceToPublish, new Operation(), null, null);
		PublishContext context = new PublishContext(request, revision);
		loadView(context);

		toPdfPublishStep.execute(context);
	}

	@Test(expected = RollbackedRuntimeException.class)
	public void shouldFailOnExportTimeout() throws Exception {
		Instance revision = new EmfInstance();
		revision.setId("emf:instance-r1.0");
		Instance instanceToPublish = new EmfInstance();
		instanceToPublish.setId("emf:instance");
		PublishInstanceRequest request = new PublishInstanceRequest(instanceToPublish, new Operation(), null, null);
		PublishContext context = new PublishContext(request, revision);
		loadView(context);
		mockValidRevision();

		reset(exporter);
		when(exporter.export(anyString())).thenThrow(new TimeoutException());

		toPdfPublishStep.execute(context);
	}

	private static void loadView(PublishContext context) {
		try (InputStream input = AsyncExportTabsToPdfPublishStepTest.class.getResourceAsStream(TEST_IDOC)) {
			context.setView(Idoc.parse(input));
		} catch (IOException e) {
			fail(e.getMessage());
		}
	}

	private void mockValidRevision() {
		when(domainInstanceService.loadInstance(anyString())).then(a -> {
			EmfInstance instance = new EmfInstance();
			instance.setId(a.getArgumentAt(0, String.class));
			return instance;
		});
	}

	@After
	public void afterMethod() {
		if (exportFile != null) {
			exportFile.delete();
		}
	}

	private File createExportFile() {
		try {
			exportFile = File.createTempFile("dummy", ".pdf");
		} catch (IOException e) {
			fail(e.getMessage());
		}

		return exportFile;
	}

}
