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
import com.sirma.itt.seip.domain.rest.EmfApplicationException;
import com.sirma.itt.seip.exception.RollbackedRuntimeException;
import com.sirma.itt.seip.export.PDFExporter;
import com.sirma.itt.seip.instance.revision.PublishInstanceRequest;
import com.sirma.itt.seip.instance.state.Operation;
import com.sirma.itt.seip.rest.secirity.SecurityTokensManager;
import com.sirma.itt.seip.security.User;
import com.sirma.itt.seip.security.context.SecurityContext;
import com.sirmaenterprise.sep.content.idoc.Idoc;

/**
 * Test for {@link ExportTabsToPdfPublishStep}
 *
 * @author BBonev
 */
public class ExportTabsToPdfPublishStepTest {

	private static final String TEST_IDOC = "/publish-idoc.html";
	private static final String TEST_IDOC_NO_TABS_TO_EXPORT = "/publish-idoc-no-tabs-to-export.html";
	private static final String EXPORT_ADDRESS = "/#/idoc/emf:instance?tab=abc2945a-ef95-41ed-8452-2bc812b7d3ac&tab=ec4983d3-aac2-4e16-f763-73c154008db3&jwt=jwtToken&mode=print";

	@InjectMocks
	private ExportTabsToPdfPublishStep toPdfPublishStep;

	@Mock
	private PDFExporter exporter;
	@Mock
	private SecurityContext securityContext;
	@Mock
	private SecurityTokensManager tokensManager;
	@Mock
	private InstanceContentService instanceContentService;

	private File exportFile;

	@Before
	public void beforeMethod() throws TimeoutException {
		MockitoAnnotations.initMocks(this);
		when(securityContext.getAuthenticated()).thenReturn(mock(User.class));
		when(tokensManager.getCurrentJwtToken()).thenReturn(Optional.of("jwtToken"));

		when(instanceContentService.saveContent(any(Serializable.class), any(Content.class))).then(a -> {
			ContentInfo info = mock(ContentInfo.class);
			when(info.exists()).thenReturn(Boolean.TRUE);
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
		loadView(context, TEST_IDOC);

		toPdfPublishStep.execute(context);

		verify(exporter).export(EXPORT_ADDRESS);
	}

	@Test(expected = RollbackedRuntimeException.class)
	public void shouldFailOnExportTimeout() throws Exception {
		Instance revision = new EmfInstance();
		revision.setId("emf:instance-r1.0");
		Instance instanceToPublish = new EmfInstance();
		instanceToPublish.setId("emf:instance");
		PublishInstanceRequest request = new PublishInstanceRequest(instanceToPublish, new Operation(), null, null);
		PublishContext context = new PublishContext(request, revision);
		loadView(context, TEST_IDOC);

		reset(exporter);
		when(exporter.export(anyString())).thenThrow(new TimeoutException());

		toPdfPublishStep.execute(context);
	}

	@Test(expected=EmfApplicationException.class)
	public void shouldFailWhenThereAreNoConfiguredTabs(){
		Instance revision = new EmfInstance();
		revision.setId("emf:instance-r1.0");
		Instance instanceToPublish = new EmfInstance();
		instanceToPublish.setId("emf:instance");
		PublishInstanceRequest request = new PublishInstanceRequest(instanceToPublish, new Operation(), null, null);
		PublishContext context = new PublishContext(request, revision);
		loadView(context, TEST_IDOC_NO_TABS_TO_EXPORT);

		toPdfPublishStep.execute(context);
	}
	private static void loadView(PublishContext context, String file) {
		try (InputStream input = ExportTabsToPdfPublishStepTest.class.getResourceAsStream(file)) {
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

	private File createExportFile() {
		try {
			exportFile = File.createTempFile("dummy", ".pdf");
		} catch (IOException e) {
			fail(e.getMessage());
		}

		return exportFile;
	}

}
