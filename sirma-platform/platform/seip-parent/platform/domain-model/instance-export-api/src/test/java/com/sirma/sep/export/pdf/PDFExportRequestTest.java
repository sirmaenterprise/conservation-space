package com.sirma.sep.export.pdf;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.net.URI;

import org.junit.Test;

import com.sirma.sep.export.pdf.PDFExportRequest.PDFExportRequestBuilder;

/**
 * Test for {@link PDFExportRequest}.
 *
 * @author A. Kunchev
 */
@SuppressWarnings("static-method")
public class PDFExportRequestTest {

	@Test
	public void getName() {
		assertEquals("pdf", new PDFExportRequestBuilder().setInstanceURI(URI.create("")).buildRequest().getName());
	}

	@Test
	public void getInstanceURI() {
		PDFExportRequest request = new PDFExportRequestBuilder()
				.setFileName("file-name")
					.setInstanceId("instance-id")
					.setInstanceURI(URI.create("http://localhost:8080/page"))
					.buildRequest();
		assertNotNull(request.getInstanceURI());
	}

}
