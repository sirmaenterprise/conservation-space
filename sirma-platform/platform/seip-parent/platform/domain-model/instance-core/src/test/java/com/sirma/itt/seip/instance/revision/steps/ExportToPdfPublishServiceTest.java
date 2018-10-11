package com.sirma.itt.seip.instance.revision.steps;

import static org.junit.Assert.fail;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.Serializable;
import java.net.URI;
import java.util.Collections;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.Spy;
import org.mockito.runners.MockitoJUnitRunner;

import com.sirma.itt.seip.domain.instance.DefaultProperties;
import com.sirma.itt.seip.domain.instance.EmfInstance;
import com.sirma.itt.seip.domain.instance.Instance;
import com.sirma.itt.seip.exception.EmfException;
import com.sirma.itt.seip.exception.RollbackedRuntimeException;
import com.sirma.itt.seip.instance.DomainInstanceService;
import com.sirma.itt.seip.instance.revision.PublishInstanceRequest;
import com.sirma.itt.seip.instance.state.Operation;
import com.sirma.itt.seip.security.User;
import com.sirma.itt.seip.security.context.SecurityContext;
import com.sirma.itt.seip.testutil.fakes.TransactionSupportFake;
import com.sirma.itt.seip.tx.TransactionSupport;
import com.sirma.sep.content.Content;
import com.sirma.sep.content.ContentInfo;
import com.sirma.sep.content.InstanceContentService;
import com.sirma.sep.content.idoc.Idoc;
import com.sirma.sep.export.ExportService;
import com.sirma.sep.export.ExportURIBuilder;
import com.sirma.sep.export.pdf.PDFExportRequest;

/**
 * Test for {@link ExportToPdfPublishService}.
 */
@RunWith(MockitoJUnitRunner.class)
public class ExportToPdfPublishServiceTest {
	private static final String TEST_IDOC = "/publish-idoc.html";

	@Mock
	private SecurityContext securityContext;
	@Mock
	private DomainInstanceService domainInstanceService;
	@Mock
	private ExportService exportService;
	@Mock
	private InstanceContentService instanceContentService;
	@InjectMocks
	private ExportToPdfPublishService publishStepService;
	@Mock
	private ExportURIBuilder uriBuilder;
	@Spy
	private TransactionSupport transactionSupport = new TransactionSupportFake();

	private File exportFile;
	private ContentInfo content;

	@Before
	public void beforeMethod() throws Exception {
		MockitoAnnotations.initMocks(this);
		when(securityContext.getAuthenticated()).thenReturn(mock(User.class));
		when(uriBuilder.getCurrentJwtToken()).thenReturn("jwtToken");
		when(uriBuilder.generateURIForTabs(any(), any(), any())).thenReturn(new URI("http://ses.com"));
		content = mock(ContentInfo.class);
		when(content.getContentId()).thenReturn("contentId");
		when(content.getLength()).thenReturn(1L);
		when(content.getName()).thenReturn("exportedFile.pdf");
		when(content.getMimeType()).thenReturn("application/pdf");
		when(content.exists()).thenReturn(Boolean.TRUE);
		when(instanceContentService.saveContent(any(Serializable.class), any(Content.class))).then(a -> content);
		when(exportService.export(any(PDFExportRequest.class))).thenReturn(createExportFile());
	}

	@Test
	public void shouldExportTheMarkedTabs() throws Exception {
		Instance revision = mock(EmfInstance.class);
		revision.setId("emf:instance-r1.0");
		when(revision.getString(eq(DefaultProperties.NAME))).thenReturn("filename.txt");
		Instance instanceToPublish = new EmfInstance();
		instanceToPublish.setId("emf:instance");
		PublishInstanceRequest request = new PublishInstanceRequest(instanceToPublish, new Operation(), null, null);
		PublishContext context = new PublishContext(request, revision);
		loadView(context);

		publishStepService.publishInstance(() -> "token", () -> ExportToPdfPublishService.getExportedTabs(context),
				() -> "emf:instance-r1.0", () -> revision);

		verify(exportService).export(any(PDFExportRequest.class));
	}

	@Test(expected = RollbackedRuntimeException.class)
	public void shouldFailOnNotSavedContent() throws EmfException {
		Instance revision = new EmfInstance();
		revision.setId("emf:instance-r1.0");
		Instance instanceToPublish = new EmfInstance();
		instanceToPublish.setId("emf:instance");
		PublishInstanceRequest request = new PublishInstanceRequest(instanceToPublish, new Operation(), null, null);
		PublishContext context = new PublishContext(request, revision);
		loadView(context);
		when(content.exists()).thenReturn(Boolean.FALSE);
		publishStepService.publishInstance(() -> "token", () -> ExportToPdfPublishService.getExportedTabs(context),
				() -> "emf:instance-r1.0", () -> revision);
	}

	@Test(expected = EmfException.class)
	public void shouldFailOnNoTabs() throws EmfException {
		Instance revision = new EmfInstance();
		revision.setId("emf:instance-r1.0");
		publishStepService.publishInstance(() -> "token", () -> null, () -> "emf:instance-r1.0", () -> revision);
	}

	@Test(expected = EmfException.class)
	public void shouldFailForNonExistingRevision() throws Exception {
		publishStepService.publishInstance(() -> "token", () -> Collections.singletonList("tab1"),
										   () -> "emf:instance-r1.0", () -> null);
	}

	@Test(expected = RuntimeException.class)
	public void shouldFailOnExportFail() throws EmfException {
		Instance revision = new EmfInstance();
		revision.setId("emf:instance-r1.0");
		Instance instanceToPublish = new EmfInstance();
		instanceToPublish.setId("emf:instance");
		PublishInstanceRequest request = new PublishInstanceRequest(instanceToPublish, new Operation(), null, null);
		PublishContext context = new PublishContext(request, revision);
		loadView(context);
		when(exportService.export(any(PDFExportRequest.class))).thenThrow(new RuntimeException());
		publishStepService.publishInstance(() -> "token", () -> ExportToPdfPublishService.getExportedTabs(context),
				() -> "emf:instance-r1.0", () -> revision);
	}

	private static void loadView(PublishContext context) {
		try (InputStream input = ExportTabsToPdfPublishStepTest.class.getResourceAsStream(TEST_IDOC)) {
			context.setView(Idoc.parse(input));
		} catch (IOException e) {
			fail(e.getMessage());
		}
	}

	@After
	public void afterMethod() {
		if (exportFile != null) {
			exportFile.delete();
		}
	}

	private static File createExportFile() {
		return mock(File.class);
	}

}
