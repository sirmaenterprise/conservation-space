package com.sirma.itt.seip.export.rest;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.io.File;
import java.io.IOException;
import java.util.concurrent.TimeoutException;

import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Matchers;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import com.sirma.itt.seip.StringPair;
import com.sirma.itt.seip.export.ExportHelper;
import com.sirma.itt.seip.export.PDFExporter;
import com.sirma.itt.seip.rest.exceptions.ResourceException;

/**
 * Test for {@link ExportPDFAction}.
 *
 * @author A. Kunchev
 */
public class ExportPDFActionTest {

	@InjectMocks
	private ExportPDFAction action;

	@Mock
	private ExportHelper exportHelper;

	@Mock
	private PDFExporter exporter;

	@Before
	public void setup() {
		action = new ExportPDFAction();
		MockitoAnnotations.initMocks(this);
	}

	@Test
	public void getName() {
		assertEquals(ExportPDFRequest.EXPORT_PDF, action.getName());
	}

	@Test
	public void perform_successful() throws IOException {
		mock(File.class);

		ExportPDFRequest request = new ExportPDFRequest();
		request.setTargetId("target-id");
		request.setUrl("page-to-export-url");
		request.setUserOperation(ExportPDFRequest.EXPORT_PDF);
		request.setCookies(new StringPair[] { new StringPair() });

		when(exportHelper.createDownloadableURL(null, null, "target-id", "application/pdf", "-export-pdf", "exportPDF"))
				.thenReturn("/url/someUrl");
		String downloadLink = action.perform(request);

		assertNotNull(downloadLink);
	}

	@Test(expected=ResourceException.class)
	public void perform_timeout() throws TimeoutException {
		ExportPDFRequest request = new ExportPDFRequest();
		request.setTargetId("target-id");
		request.setUrl("page-to-export-url");
		request.setUserOperation(ExportPDFRequest.EXPORT_PDF);
		request.setCookies(new StringPair[] { new StringPair() });
		when(exporter.export(Matchers.anyString(), Matchers.any())).thenThrow(new TimeoutException());
		action.perform(request);
	}
}
