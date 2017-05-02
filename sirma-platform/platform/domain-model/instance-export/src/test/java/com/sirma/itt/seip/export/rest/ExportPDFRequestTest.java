package com.sirma.itt.seip.export.rest;

import static org.junit.Assert.assertEquals;

import org.junit.Before;
import org.junit.Test;

import com.sirma.itt.seip.StringPair;

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
		StringPair[] cookies = new StringPair[1];
		cookies[0] = new StringPair();
		cookies[0].setFirst("first");
		cookies[0].setSecond("second");
		request.setCookies(cookies);
	}

	@Test
	public void getOperation() {
		assertEquals("exportPDF", request.getOperation());
	}

	@Test
	public void getCookies() {
		assertEquals("first", request.getCookies()[0].getFirst());
		assertEquals("second", request.getCookies()[0].getSecond());
	}

	@Test(expected = IllegalArgumentException.class)
	public void setUrl_nullArgument() {
		ExportPDFRequest request = new ExportPDFRequest();
		request.setUrl(null);
	}

	@Test(expected = IllegalArgumentException.class)
	public void setUrl_emptyArgument() {
		ExportPDFRequest request = new ExportPDFRequest();
		request.setUrl("");
	}

	@Test
	public void setUrl_correct() {
		ExportPDFRequest request = new ExportPDFRequest();
		request.setUrl("someURL");
		assertEquals("someURL", request.getUrl());
	}

}
