package com.sirma.sep.export.pdf.action;

import static org.junit.Assert.assertEquals;

import java.net.URI;

import org.junit.Before;
import org.junit.Test;

import com.sirma.sep.export.pdf.PDFExportRequest;

/**
 * Test for {@link ExportPDFRequest}.
 *
 * @author A. Kunchev
 */
public class ExportPDFRequestTest {

	private ExportPDFRequest request;

	@Before
	public void setup() {
		request = new ExportPDFRequest();
	}

	@Test
	public void getOperation() {
		assertEquals("exportPDF", request.getOperation());
	}


	@Test
	@SuppressWarnings("static-method")
	public void setUrl_correct() {
		ExportPDFRequest pdfRequest = new ExportPDFRequest();
		pdfRequest.setUrl("someURL");
		assertEquals("someURL", pdfRequest.getUrl());
	}
	/**
	 * Test for null url convert.
	 */
	@Test(expected = IllegalArgumentException.class)
	public void convert_illegal_nullURL() {
		request.setUrl(null);
		request.setFileName("filename");
		request.toPDFExportRequest();
	}

	/**
	 * Test for null url.
	 */
	@Test
	public void convert_valid() {
		request.setUrl("http://uri/");
		request.setFileName("filename");
		request.setContextPath(null);
		request.setTargetId("id");
		PDFExportRequest exportRequest = request.toPDFExportRequest();
		assertEquals(URI.create("http://uri/"), exportRequest.getInstanceURI());
		assertEquals("filename", exportRequest.getFileName());
		assertEquals("id", exportRequest.getInstanceId());
	}

}
