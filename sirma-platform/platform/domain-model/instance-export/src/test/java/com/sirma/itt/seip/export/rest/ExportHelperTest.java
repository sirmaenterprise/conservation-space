package com.sirma.itt.seip.export.rest;

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
import java.util.concurrent.TimeoutException;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import com.sirma.itt.seip.StringPair;
import com.sirma.itt.seip.configuration.ConfigurationProperty;
import com.sirma.itt.seip.content.Content;
import com.sirma.itt.seip.content.InstanceContentService;
import com.sirma.itt.seip.domain.event.AuditableEvent;
import com.sirma.itt.seip.domain.instance.EmfInstance;
import com.sirma.itt.seip.domain.instance.Instance;
import com.sirma.itt.seip.domain.rest.EmfApplicationException;
import com.sirma.itt.seip.event.EventService;
import com.sirma.itt.seip.export.ExportHelper;
import com.sirma.itt.seip.export.HtmlToPDFExporter;
import com.sirma.itt.seip.instance.DomainInstanceService;
import com.sirma.itt.seip.testutil.mocks.InstanceReferenceMock;

public class ExportHelperTest {

	@InjectMocks
	private ExportHelper exportHelper;

	@Mock
	private HtmlToPDFExporter pdfExporter;

	@Mock
	private InstanceContentService instanceContentService;

	@Mock
	private EventService eventService;

	@Mock
	private DomainInstanceService domainInstanceService;

	@Mock
	private ConfigurationProperty<Integer> expirationTime;

	@Before
	public void setup() {
		MockitoAnnotations.initMocks(this);
		when(expirationTime.get()).thenReturn(1);
	}

	@Test(expected = EmfApplicationException.class)
	public void perform_failedToExportFile() throws TimeoutException {
		File file = mock(File.class);
		when(file.exists()).thenReturn(false, false);
		when(pdfExporter.export(any(), any())).thenReturn(file);
		ExportPDFRequest request = new ExportPDFRequest();
		request.setTargetId("target-id");
		exportHelper.createDownloadableURL(file, null, "target-id", "application/pdf", "-export-pdf", "exportPDF");
	}

	@Test
	public void perform_successful() throws IOException {
		File file = mock(File.class);
		when(file.exists()).thenReturn(true, true);

		EmfInstance instance = new EmfInstance();
		instance.setReference(new InstanceReferenceMock());
		instance.setId("target-id");
		when(domainInstanceService.loadInstance(anyString())).thenReturn(instance);

		ExportPDFRequest request = new ExportPDFRequest();
		request.setTargetId("target-id");
		request.setUrl("page-to-export-url");
		request.setUserOperation(ExportPDFRequest.EXPORT_PDF);
		request.setCookies(new StringPair[] { new StringPair() });

		String downloadLink = exportHelper.createDownloadableURL(file, null, "target-id", "application/pdf", "-export-pdf",
				"exportPDF");

		assertNotNull(downloadLink);
		verify(instanceContentService).saveContent(any(Instance.class), any(Content.class));
		verify(instanceContentService).deleteContent(eq("target-id"), anyString(), eq(1), eq(TimeUnit.HOURS));

		ArgumentCaptor<AuditableEvent> eventCaptor = ArgumentCaptor.forClass(AuditableEvent.class);
		verify(eventService).fire(eventCaptor.capture());

		AuditableEvent capturedValue = eventCaptor.getValue();
		assertNotNull(capturedValue);
		Assert.assertEquals(instance, capturedValue.getInstance());

		file.delete();
	}

}
