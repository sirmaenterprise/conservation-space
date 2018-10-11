package com.sirma.sep.content.idoc.sanitizer;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.charset.StandardCharsets;

import org.apache.commons.io.FileUtils;
import org.jsoup.Jsoup;
import org.junit.Assert;
import org.junit.Test;

import com.sirma.sep.content.idoc.sanitizer.IdocSanitizer;

/**
 * Tests methods of {@link IdocSanitizer}.
 *
 * @author Adrian Mitev
 */
public class IdocSanitizerTest {

	private IdocSanitizer sanitizer = new IdocSanitizer();

	@Test(expected = IllegalArgumentException.class)
	public void shouldThrowExceptionIfNullContent() {
		String content = null;
		sanitizer.sanitize(content, null);
	}

	@Test
	public void shouldProperlySanitizeIdocAsString() {
		String content = readFileAsString("/sanitize/widget.xml");
		String expected = unprettyPrintHtml(readFileAsString("/sanitize/document/result.xml"));

		String result = sanitizer.sanitize(content, null);

		result = unprettyPrintHtml(result);

		Assert.assertEquals(expected.trim(), result.trim());
	}

	@Test
	public void shouldSanitizeTaintedLayouts(){
		String content = readFileAsString("/sanitize/tained-layout/document.xml");
		String expected = unprettyPrintHtml(readFileAsString("/sanitize/tained-layout/result.xml"));

		String result = sanitizer.sanitize(Jsoup.parse(content), null).toString();

		result = unprettyPrintHtml(result);

		Assert.assertEquals(expected.trim(), result.trim());
	}

	@Test
	public void shouldProperlySanitizeIdocAsJsoupDocument() {
		String content = readFileAsString("/sanitize/widget.xml");
		String expected = unprettyPrintHtml(readFileAsString("/sanitize/document/result.xml"));

		String result = sanitizer.sanitize(Jsoup.parse(content), null).toString();

		result = unprettyPrintHtml(result);

		Assert.assertEquals(expected.trim(), result.trim());
	}

	@Test
	public void shouldSanitizeTemplate() {
		String content = readFileAsString("/sanitize/widget.xml");
		String expected = unprettyPrintHtml(readFileAsString("/sanitize/template/result.xml"));

		String result = sanitizer.sanitizeTemplate(content, null);

		result = unprettyPrintHtml(result);

		Assert.assertEquals(result, expected);
	}

	@Test
	public void shouldSanitizeIdocWithSections() {
		String content = readFileAsString("/sanitize/sections/document.xml");
		String expected = unprettyPrintHtml(readFileAsString("/sanitize/sections/result.xml"));

		String result = sanitizer.sanitize(content, null);

		result = unprettyPrintHtml(result);

		Assert.assertEquals(expected, result);
	}

	@Test
	public void shouldSanitizeForImageAttributes() {
		String content = readFileAsString("/sanitize/image/document.xml");
		String expected = unprettyPrintHtml(readFileAsString("/sanitize/image/result.xml"));

		String result = sanitizer.sanitize(content, null);

		result = unprettyPrintHtml(result);

		Assert.assertEquals(expected, result);
	}

	@Test
	public void shouldSanitizeDataIdsAdded() {
		String content = readFileAsString("/sanitize/template/template.xml");
		String result = sanitizer.sanitize(content);
		Assert.assertTrue(result.contains("data-id"));
	}

	private static String unprettyPrintHtml(String html) {
		String notPretty = html.replace((char) 10, ' ');
		notPretty = notPretty.replace((char) 13, ' ');
		notPretty = notPretty.replaceAll(">\\s+<", "><");

		// remove whitespaces between html attributes
		notPretty = notPretty.replaceAll("\\s+", " ");

		return notPretty;
	}

	private static String readFileAsString(String filePath) {
		URL url = IdocSanitizerTest.class.getResource(filePath);
		try {
			return FileUtils.readFileToString(new File(url.toURI()), StandardCharsets.UTF_8);
		} catch (IOException | URISyntaxException e) {
			throw new RuntimeException(e);
		}
	}

}
