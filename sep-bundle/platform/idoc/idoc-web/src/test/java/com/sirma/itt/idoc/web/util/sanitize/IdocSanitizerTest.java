/**
 * Copyright (c) 2014 05.02.2014 , Sirma ITT. /* /**
 */
package com.sirma.itt.idoc.web.util.sanitize;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;

import org.apache.commons.io.FileUtils;
import org.testng.Assert;
import org.testng.annotations.Test;

import com.sirma.itt.emf.util.sanitize.ContentSanitizer;

/**
 * Tests methods of {@link IdocSanitizer}.
 * 
 * @author Adrian Mitev
 */
@Test
public class IdocSanitizerTest {

	private ContentSanitizer sanitizer = new IdocSanitizer();

	/**
	 * Test for {@link IdocSanitizer#sanitizeBeforeClone(String, String)}.
	 * Ensure id attributes are removed.
	 */
	public void testPrepareForClone() {

		String content = readFileAsString("/sanitize/clone/document.xml");
		String expected = unprettyPrintHtml(readFileAsString("/sanitize/clone/result.xml"));

		String origin = "http://10.131.2.149:8080";
		String result = sanitizer.sanitizeBeforeClone(content, origin);

		result = unprettyPrintHtml(result);

		Assert.assertEquals(result, expected);
	}

	/**
	 * Tests {@link IdocSanitizer#sanitize(String, String)} with data from file.
	 */
	public void testSanitizeDocument() {
		String content = readFileAsString("/sanitize/widget.xml");
		String expected = unprettyPrintHtml(readFileAsString("/sanitize/document/result.xml"));

		String result = sanitizer.sanitize(content, null);

		result = unprettyPrintHtml(result);

		Assert.assertEquals(result.trim(), expected.trim());
	}


	/**
	 * Tests {@link IdocSanitizer#sanitize(String, String)} with data from file.
	 */
	public void testSanitizeInternalLink() {
		String content = readFileAsString("/sanitize/internaLinkAndThumbnail.xml");
		String expected = unprettyPrintHtml(readFileAsString("/sanitize/internaLinkAndThumbnail.xml"));

		String origin = "http://10.131.2.149:8080";
		String result = sanitizer.sanitizeBeforeClone(content, origin);

		result = unprettyPrintHtml(result);

		Assert.assertEquals(result, expected);
	}

	/**
	 * Tests {@link IdocSanitizer#sanitizeTemplate(String, String)} with data from file.
	 */
	public void testSanitizeTemplate() {
		String content = readFileAsString("/sanitize/widget.xml");
		String expected = unprettyPrintHtml(readFileAsString("/sanitize/template/result.xml"));

		String result = sanitizer.sanitizeTemplate(content, null);

		result = unprettyPrintHtml(result);

		Assert.assertEquals(result, expected);
	}

	/**
	 * Removes pretty printing and whitespaces between empty html tags and attributes.
	 * 
	 * @param html
	 *            code to make unpretty.
	 * @return modified html
	 */
	private String unprettyPrintHtml(String html) {
		html = html.replace((char) 10, ' ');
		html = html.replace((char) 13, ' ');
		html = html.replaceAll(">\\s+<", "><");

		// remove whitespaces between html attributes
		html = html.replaceAll("\\s+", " ");

		return html;
	}

	/**
	 * Reads a file and return it as string.
	 * 
	 * @param filePath
	 *            file to read.
	 * @return file as string.
	 */
	private String readFileAsString(String filePath) {
		URL url = DefaultPolicyTest.class.getResource(filePath);
		try {
			return FileUtils.readFileToString(new File(url.toURI()));
		} catch (IOException e) {
			throw new RuntimeException(e);
		} catch (URISyntaxException e) {
			throw new RuntimeException(e);
		}
	}

}
