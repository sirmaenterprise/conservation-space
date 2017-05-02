package com.sirma.itt.seip.export.rest;

import static org.junit.Assert.assertEquals;

import org.junit.Before;
import org.junit.Test;

import com.sirma.itt.seip.StringPair;

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
	 * Test cookies.
	 */
	@Test
	public void getCookies() {
		StringPair[] cookies = new StringPair[1];
		cookies[0] = new StringPair();
		cookies[0].setFirst("first");
		cookies[0].setSecond("second");
		request.setCookies(cookies);
		assertEquals("first", request.getCookies()[0].getFirst());
		assertEquals("second", request.getCookies()[0].getSecond());
	}

	/**
	 * Test for null url.
	 */
	@Test(expected = IllegalArgumentException.class)
	public void setUrl_nullArgument() {
		request.setUrl(null);
	}

	/**
	 * Test for empty url.
	 */
	@Test(expected = IllegalArgumentException.class)
	public void setUrl_emptyArgument() {
		request.setUrl("");
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
