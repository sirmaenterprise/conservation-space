package com.sirma.sep.content.rest;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.io.ByteArrayOutputStream;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

import org.junit.Test;

import com.sirma.sep.content.ContentInfo;
import com.sirma.sep.content.rest.ContentInfoMapperWriter;

import net.javacrumbs.jsonunit.JsonAssert;

/**
 * Test for {@link ContentInfoMapperWriter}.
 * 
 * @author Nikolay Ch
 */
public class ContentInfoMapperWirterTest {

	ContentInfoMapperWriter writer = new ContentInfoMapperWriter();

	@Test
	public void testWriteExisting() throws Exception {
		ByteArrayOutputStream entityStream = new ByteArrayOutputStream();
		ContentInfo info = mock(ContentInfo.class);
		when(info.exists()).thenReturn(Boolean.TRUE);
		when(info.getName()).thenReturn("fileName");
		when(info.getMimeType()).thenReturn("text/plain");
		when(info.getLength()).thenReturn(10L);
		when(info.getContentId()).thenReturn("contentId");
		when(info.getChecksum()).thenReturn("checksum");

		Map<String, ContentInfo> contentMapper = new HashMap<String, ContentInfo>();

		contentMapper.put("emf:1", info);

		writer.writeTo(contentMapper, null, null, null, null, null, entityStream);
		String response = new String(entityStream.toByteArray(), StandardCharsets.UTF_8);
		JsonAssert.assertJsonEquals(
				"{\"emf:1\":{\"emf:contentId\":\"contentId\",\"name\":\"fileName\",\"mimetype\":\"text/plain\",\"size\":10, \"checksum\":\"checksum\"}}",
				response);
	}

	@Test
	public void testIsWriteableWithCorrectArguments() throws Exception {
		ParameterizedType type = mock(ParameterizedType.class);
		when(type.getActualTypeArguments()).thenReturn(new Type[] { String.class, ContentInfo.class });
		assertTrue(writer.isWriteable(Map.class, type, null, null));
	}

	@Test
	public void testIsWriteableWithWrongEntryType() throws Exception {
		ParameterizedType type = mock(ParameterizedType.class);
		when(type.getActualTypeArguments()).thenReturn(new Type[] { String.class, String.class });
		assertFalse(writer.isWriteable(Map.class, type, null, null));
		assertFalse(writer.isWriteable(Object.class, null, null, null));
	}
}
