package com.sirma.sep.content.rest;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyInt;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.when;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;

import javax.json.JsonException;
import javax.ws.rs.WebApplicationException;

import org.junit.Test;

import com.sirma.sep.content.ContentInfo;
import com.sirma.sep.content.rest.ContentInfoWriter;

import net.javacrumbs.jsonunit.JsonAssert;

/**
 * Test for {@link ContentInfoWriter}
 *
 * @author BBonev
 */
public class ContentInfoWriterTest {
	ContentInfoWriter writer = new ContentInfoWriter();

	@Test
	public void writeExisting() throws Exception {
		ByteArrayOutputStream entityStream = new ByteArrayOutputStream();
		ContentInfo info = mock(ContentInfo.class);
		when(info.exists()).thenReturn(Boolean.TRUE);
		when(info.getName()).thenReturn("fileName");
		when(info.getMimeType()).thenReturn("text/plain");
		when(info.getLength()).thenReturn(10L);
		when(info.getContentId()).thenReturn("contentId");
		when(info.getChecksum()).thenReturn("checksum");
		
		writer.writeTo(info, null, null, null, null, null, entityStream);
		String response = new String(entityStream.toByteArray(), StandardCharsets.UTF_8);
		JsonAssert.assertJsonEquals(
				"{\"emf:contentId\":\"contentId\",\"name\":\"fileName\",\"mimetype\":\"text/plain\",\"size\":10, \"checksum\":\"checksum\"}",
				response);
	}

	@Test(expected = JsonException.class)
	public void write_failToWrite() throws Exception {
		ByteArrayOutputStream entityStream = spy(new ByteArrayOutputStream());
		ContentInfo info = mock(ContentInfo.class);
		when(info.exists()).thenReturn(Boolean.TRUE);
		when(info.getName()).thenReturn("fileName");
		when(info.getMimeType()).thenReturn("text/plain");
		when(info.getLength()).thenReturn(10L);
		when(info.getContentId()).thenReturn("contentId");
		when(info.getChecksum()).thenReturn("md5");
		
		doThrow(IOException.class).when(entityStream).write(any(byte[].class), anyInt(), anyInt());

		writer.writeTo(info, null, null, null, null, null, entityStream);
	}

	@Test(expected = WebApplicationException.class)
	public void writeNonExisting() throws Exception {
		writer.writeTo(ContentInfo.DO_NOT_EXIST, null, null, null, null, null, null);
	}

	@Test
	public void writeNonExisting_withContentId() throws Exception {
		ByteArrayOutputStream entityStream = new ByteArrayOutputStream();
		ContentInfo info = mock(ContentInfo.class);
		when(info.exists()).thenReturn(Boolean.FALSE);
		when(info.getName()).thenReturn("fileName");
		when(info.getMimeType()).thenReturn("text/plain");
		when(info.getLength()).thenReturn(10L);
		when(info.getContentId()).thenReturn("contentId");
		when(info.getChecksum()).thenReturn("md5");
		
		writer.writeTo(info, null, null, null, null, null, entityStream);
		String response = new String(entityStream.toByteArray(), StandardCharsets.UTF_8);
		JsonAssert.assertJsonEquals(
				"{\"emf:contentId\":\"contentId\",\"name\":\"fileName\",\"mimetype\":\"text/plain\",\"size\":10,\"checksum\":\"md5\"}",
				response);
	}

	@Test
	public void isWriteable() throws Exception {
		assertTrue(writer.isWriteable(ContentInfo.class, null, null, null));
		assertFalse(writer.isWriteable(Object.class, null, null, null));
	}
}
