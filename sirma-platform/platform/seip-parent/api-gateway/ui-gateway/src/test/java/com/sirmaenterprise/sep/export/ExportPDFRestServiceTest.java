package com.sirmaenterprise.sep.export;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.File;

import javax.json.Json;
import javax.servlet.http.HttpServletResponse;

import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import com.sirma.itt.seip.domain.rest.InternalServerErrorException;
import com.sirma.itt.seip.rest.Range;
import com.sirma.sep.content.rest.ContentDownloadService;
import com.sirma.sep.export.ExportService;
import com.sirma.sep.export.pdf.PDFExportRequest;

/**
 * Test for {@link ExportPDFRestService}.
 *
 * @author A. Kunchev
 */
public class ExportPDFRestServiceTest {

	@InjectMocks
	private ExportPDFRestService rest;

	@Mock
	private ExportService exportService;

	@Mock
	private ContentDownloadService contentDownloadService;

	@Before
	public void init() {
		rest = new ExportPDFRestService();
		MockitoAnnotations.initMocks(this);
	}

	@Test(expected = InternalServerErrorException.class)
	public void exportPDF_exportError() {
		when(exportService.export(any())).thenThrow(new RuntimeException());
		rest.exportPDF(mock(HttpServletResponse.class), "test content");
	}

	@Test
	public void exportPDF_internalServicesCalled() {
		File file = mock(File.class);
		when(file.getName()).thenReturn("pdf-file");
		when(exportService.export(any(PDFExportRequest.class))).thenReturn(file);
		HttpServletResponse response = mock(HttpServletResponse.class);
		String requestString = Json
				.createObjectBuilder()
					.add("url", "http://localhost/emf:id")
					.add("fileName", "pdf-file")
					.add("instanceId", "emf:id")
					.build()
					.toString();

		rest.exportPDF(response, requestString);
		verify(exportService).export(any(PDFExportRequest.class));
		verify(contentDownloadService).sendFile(eq(file), eq(Range.ALL), eq(false), eq(response), eq("pdf-file"),
				eq("application/pdf"));
	}

}
