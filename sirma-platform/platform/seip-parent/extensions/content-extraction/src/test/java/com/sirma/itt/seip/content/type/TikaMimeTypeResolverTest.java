package com.sirma.itt.seip.content.type;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

import org.apache.commons.io.IOUtils;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.MockitoAnnotations;

/**
 * Test for TikaMimeTypeResolver.
 *
 * @author A. Kunchev
 */
public class TikaMimeTypeResolverTest {

	private static final String STREAM_MIME_TYPE = "application/octet-stream";

	@InjectMocks
	private TikaMimeTypeResolver resolver = new TikaMimeTypeResolver();

	@Before
	public void setup() {
		MockitoAnnotations.initMocks(this);
	}

	@Test
	public void getMimeTypeBufferedInputStream_nullStream_nullResult() {
		BufferedInputStream stream = null;
		String mimeType = resolver.getMimeType(stream, null);
		assertNull(mimeType);
	}

	@Test
	public void getMimeTypeBufferedInputStream() throws IOException {
		File file = File.createTempFile("temp", ".tmp");
		try (BufferedInputStream stream = new BufferedInputStream(new FileInputStream(file))) {
			String mimeType = resolver.getMimeType(stream, "temp.tmp");
			assertNull(STREAM_MIME_TYPE, mimeType);
		} finally {
			file.deleteOnExit();
		}
	}

	@Test
	public void getMimeTypeFromStream() throws IOException {
		try (InputStream stream = new ByteArrayInputStream("<html/>".getBytes(StandardCharsets.UTF_8))) {
			String mimeType = resolver.getMimeType(stream, "test.html");
			assertEquals("text/html", mimeType);
		}
	}

	@Test
	public void getMimeTypeFile_nullFile() {
		File file = null;
		String mimeType = resolver.getMimeType(file);
		assertNull(mimeType);
	}

	@Test
	public void getMimeTypeFile_folder() {
		String mimeType = resolver.getMimeType(new File("."));
		assertNull(mimeType);
	}

	@Test
	public void test_withFile() throws Exception {
		File file = File.createTempFile("test", "txt", new File("."));
		try (FileOutputStream output = new FileOutputStream(file)) {
			IOUtils.write("test data", output);
		}
		try {
			String mimeType = resolver.getMimeType(file);
			assertEquals("text/plain", mimeType);
		} finally {
			file.delete();
		}
	}

	@Test
	public void test_withBytes_nullBytes_null() {
		byte[] bytes = null;
		String mimeType = resolver.getMimeType(bytes, null);
		assertNull(mimeType);
	}

	@Test
	public void test_withBytes_emptyBytesArray_null() {
		String mimeType = resolver.getMimeType(new byte[0], null);
		assertNull(mimeType);
	}

	@Test
	public void test_withBytes() {
		String mimeType = resolver.getMimeType("<html/>".getBytes(StandardCharsets.UTF_8), "test.html");
		assertEquals("text/html", mimeType);
	}

	@Test
	public void test_forName_invalidName() throws Exception {
		assertNull(resolver.resolveFromName(null));
		assertNull(resolver.resolveFromName(""));
		assertNull(resolver.resolveFromName("text"));
	}

	@Test
	public void test_forName() throws Exception {
		String name = resolver.resolveFromName("text.txt");
		assertEquals("text/plain", name);
		name = resolver.resolveFromName("text.dng");
		assertEquals("image/x-raw-adobe", name);
		name = resolver.resolveFromName("text.pef");
		assertEquals("image/x-raw-pentax", name);
		name = resolver.resolveFromName("text.psd");
		assertEquals("image/vnd.adobe.photoshop", name);
		name = resolver.resolveFromName("text.jp2");
		assertEquals("image/jp2", name);
		name = resolver.resolveFromName("text.ptif");
		assertEquals("image/x-ptif", name);
		name = resolver.resolveFromName("text.png");
		assertEquals("image/png", name);
		name = resolver.resolveFromName("text.bmp");
		assertEquals("image/bmp", name);
	}

}
