package com.sirma.sep.export.word.action;

import static org.junit.Assert.assertEquals;

import org.junit.Before;
import org.junit.Test;

import com.sirma.sep.export.word.WordExportRequest;

/**
 * Test for {@link ExportWordRequest}.
 *
 * @author Stella D
 */
public class ExportWordRequestTest {

	private ExportWordRequest request;

	/**
	 * Runs before each method and setup mockito.
	 */
	@Before
	public void setup() {
		request = new ExportWordRequest();
	}

	/**
	 * Test default operation.
	 */
	@Test
	public void getOperation() {
		assertEquals("exportWord", request.getOperation());
	}

	/**
	 * Test for null url convert.
	 */
	@Test(expected = NullPointerException.class)
	public void convert_nullId() {
		request.setTargetId(null);
		request.toWordExporterRequest();
	}

	/**
	 * Test for null url.
	 */
	@Test
	public void convert_valid() {
		request.setFileName("filename");
		request.setTargetId("id");
		request.setTabId("tab");
		WordExportRequest exporterRequest = request.toWordExporterRequest();
		assertEquals("filename", exporterRequest.getFileName());
		assertEquals("id", exporterRequest.getInstanceId());
		assertEquals("tab", exporterRequest.getTabId());
	}

	/**
	 * Test for correct url.
	 */
	@Test
	public void setUrl_correct() {
		request.setUrl("someURL");
		assertEquals("someURL", request.getUrl());
	}

	/**
	 * Test for correct tabId.
	 */
	@Test
	public void tabIdTest() {
		request.setTabId("someTabId");
		assertEquals("someTabId", request.getTabId());
	}

	/**
	 * Test for correct filename.
	 */
	@Test
	public void filenameTest() {
		request.setFileName("someFilename");
		assertEquals("someFilename", request.getFileName());
	}

}
