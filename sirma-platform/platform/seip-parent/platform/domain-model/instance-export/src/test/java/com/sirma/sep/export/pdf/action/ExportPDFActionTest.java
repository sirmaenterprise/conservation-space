package com.sirma.sep.export.pdf.action;

import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.io.File;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Matchers;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import com.sirma.itt.seip.exception.RollbackedRuntimeException;
import com.sirma.sep.export.ContentExportException;
import com.sirma.sep.export.ExportHelper;
import com.sirma.sep.export.ExportService;
import com.sirma.sep.export.pdf.PDFExportRequest;

/**
 * Test for {@link ExportPDFAction}.
 *
 * @author A. Kunchev
 * @author bbanchev
 */
@RunWith(MockitoJUnitRunner.class)
public class ExportPDFActionTest {

	@InjectMocks
	private ExportPDFAction action;

	@Mock
	private ExportHelper exportHelper;

	@Mock
	private ExportService exporter;

	@Test
	public void getName() {
		assertEquals(ExportPDFRequest.EXPORT_PDF, action.getName());
	}

	@Test
	public void perform_successful() throws ContentExportException {
		File file = mock(File.class);
		ExportPDFRequest request = new ExportPDFRequest();
		request.setTargetId("target-id");
		request.setFileName("filename.pdf");
		request.setUrl("page-to-export-url");
		request.setUserOperation(ExportPDFRequest.EXPORT_PDF);
		when(exporter.export(Matchers.any(PDFExportRequest.class))).thenReturn(file);
		when(exportHelper.createDownloadableURL(eq(file), eq("filename.pdf"), eq("target-id"), eq("application/pdf"),
				eq("-export-pdf"), eq("exportPDF"))).thenReturn("/url/someUrl");
		String downloadLink = action.perform(request);
		assertEquals("/url/someUrl", downloadLink);
	}

	@Test(expected = RollbackedRuntimeException.class)
	public void perform_timeout() throws ContentExportException {
		ExportPDFRequest request = new ExportPDFRequest();
		request.setTargetId("target-id");
		request.setUrl("page-to-export-url");
		request.setUserOperation(ExportPDFRequest.EXPORT_PDF);
		when(exporter.export(Matchers.any(PDFExportRequest.class))).thenThrow(new RuntimeException());

		action.perform(request);
	}
}
