package com.sirma.sep.content;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;

import org.apache.commons.io.IOUtils;
import org.junit.Test;

import com.sirma.itt.seip.exception.EmfRuntimeException;
import com.sirma.sep.content.Content;
import com.sirma.sep.content.ContentInfo;

/**
 * Tests for {@link Content} default methods
 *
 * @author BBonev
 */
public class ContentTest {

	@Test(expected = NullPointerException.class)
	public void test_setContent_nullByteData() throws Exception {
		Content.createEmpty().setContent((byte[]) null);
	}

	@Test(expected = NullPointerException.class)
	public void test_setContent_nullFile() throws Exception {
		Content.createEmpty().setContent((File) null);
	}

	@Test
	public void test_setContent_byteData() throws Exception {
		byte[] data = "test".getBytes(StandardCharsets.UTF_8);
		Content content = Content.createEmpty().setContent(data);
		assertEquals("test", content.getContent().asString());
	}

	@Test
	public void test_setContent_file() throws Exception {
		File file = File.createTempFile("test", null);
		try (OutputStream output = new FileOutputStream(file)) {
			IOUtils.write("test", output);
		}
		try {
			Content content = Content.createEmpty().setContent(file);
			assertEquals("test", content.getContent().asString());
		} finally {
			file.delete();
		}
	}

	@Test
	public void setStringContent() throws Exception {
		Content content = Content.createEmpty().setContent("test", StandardCharsets.UTF_8);
		assertEquals("test", IOUtils.toString(content.getContent().getInputStream()));

		content = Content.createEmpty().setContent("test", (String) null);
		assertEquals("test", IOUtils.toString(content.getContent().getInputStream()));
	}

	@Test(expected = EmfRuntimeException.class)
	public void test_setContent_file_nonExisting() throws Exception {
		Content.createEmpty().setContent(new File("someNonExistingFile.bin")).getContent().asString();
	}

	@Test
	public void isVersionable_withoutSettingIt_false() {
		assertFalse(Content.createEmpty().isVersionable());
	}

	@Test
	public void isVersionable_settingItToTrue_true() {
		Content content = Content.createEmpty();
		content.setVersionable(true);
		assertTrue(content.isVersionable());
	}

	@Test
	public void createFromContentInfoShouldCopyAllPropertiesExceptTheContent() throws Exception {
		ContentInfo info = mock(ContentInfo.class);
		when(info.getCharset()).thenReturn("utf-8");
		when(info.getContentPurpose()).thenReturn("purpose");
		when(info.getMimeType()).thenReturn("mimetype");
		when(info.getName()).thenReturn("name");
		when(info.isView()).thenReturn(Boolean.TRUE);
		when(info.isIndexable()).thenReturn(Boolean.TRUE);

		Content content = Content.createFrom(info);
		assertEquals("utf-8", content.getCharset());
		assertEquals("purpose", content.getPurpose());
		assertEquals("mimetype", content.getMimeType());
		assertEquals("name", content.getName());
		assertTrue(content.isView());
		assertTrue(content.isIndexable());
	}

	@Test(expected = NullPointerException.class)
	public void createFromContentInfoShouldThrowNPEOnNullInfo() throws Exception {
		Content.createFrom(null);
	}
}
