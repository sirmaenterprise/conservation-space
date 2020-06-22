package com.sirma.sep.export;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.File;
import java.io.IOException;
import java.util.concurrent.TimeUnit;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import com.sirma.itt.seip.configuration.ConfigurationProperty;
import com.sirma.itt.seip.domain.event.AuditableEvent;
import com.sirma.itt.seip.domain.instance.EmfInstance;
import com.sirma.itt.seip.domain.instance.Instance;
import com.sirma.itt.seip.domain.rest.EmfApplicationException;
import com.sirma.itt.seip.event.EventService;
import com.sirma.itt.seip.instance.DomainInstanceService;
import com.sirma.itt.seip.testutil.mocks.InstanceReferenceMock;
import com.sirma.sep.content.Content;
import com.sirma.sep.content.InstanceContentService;

/**
 * Test for {@link ExportHelper}.
 *
 * @author bbanchev
 */
public class ExportHelperTest {

	@InjectMocks
	private ExportHelper exportHelper;

	@Mock
	private InstanceContentService instanceContentService;

	@Mock
	private EventService eventService;

	@Mock
	private DomainInstanceService domainInstanceService;

	@Mock
	private ConfigurationProperty<Integer> expirationTime;

	private File file;

	@Before
	public void setup() throws IOException {
		MockitoAnnotations.initMocks(this);
		when(expirationTime.get()).thenReturn(1);
		file = new File("myfile.pdf");
		file.createNewFile();
	}

	@After
	public void cleanUp() {
		file.delete();
	}

	@Test(expected = EmfApplicationException.class)
	public void perform_failedToExportFile() throws ContentExportException, IOException {
		exportHelper.createDownloadableURL(new File("someInvalidFile.pdf"), null, "target-id", "application/pdf", "-export-pdf", "exportPDF");
	}

	@Test(expected = EmfApplicationException.class)
	public void perform_failedToExportFileWithNull() throws ContentExportException {
		exportHelper.createDownloadableURL(null, null, "target-id", "application/pdf", "-export-pdf", "exportPDF");
	}

	@Test
	public void perform_successful() throws IOException {
		EmfInstance instance = new EmfInstance("target-id");
		InstanceReferenceMock.createGeneric(instance);
		when(domainInstanceService.loadInstance(anyString())).thenReturn(instance);

		String downloadLink = exportHelper.createDownloadableURL(file, null, "target-id", "application/pdf",
				"-export-pdf", "exportPDF");

		assertNotNull(downloadLink);
		verify(instanceContentService).saveContent(any(Instance.class), any(Content.class));
		verify(instanceContentService).deleteContent(eq("target-id"), anyString(), eq(1), eq(TimeUnit.HOURS));

		ArgumentCaptor<AuditableEvent> eventCaptor = ArgumentCaptor.forClass(AuditableEvent.class);
		verify(eventService).fire(eventCaptor.capture());

		AuditableEvent capturedValue = eventCaptor.getValue();
		assertNotNull(capturedValue);
		Assert.assertEquals(instance, capturedValue.getInstance());
	}

	@Test
	public void perform_successful_with_filename() throws IOException {
		EmfInstance instance = new EmfInstance("target-id");
		InstanceReferenceMock.createGeneric(instance);
		when(domainInstanceService.loadInstance(anyString())).thenReturn(instance);
		ArgumentCaptor<Content> contentSaveCaptor = ArgumentCaptor.forClass(Content.class);
		exportHelper.createDownloadableURL(file, "file", "target-id", "application/pdf", "-export-pdf", "exportPDF");
		verify(instanceContentService).saveContent(any(Instance.class), contentSaveCaptor.capture());
		assertEquals("file.pdf", contentSaveCaptor.getValue().getName());
	}

}
