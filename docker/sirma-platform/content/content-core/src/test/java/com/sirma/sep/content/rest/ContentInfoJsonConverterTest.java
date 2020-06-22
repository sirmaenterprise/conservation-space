package com.sirma.sep.content.rest;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;

import javax.json.Json;
import javax.json.JsonObject;
import javax.json.JsonReader;
import javax.json.stream.JsonGenerator;

import org.junit.Test;
import org.testng.Assert;

import com.sirma.itt.seip.domain.instance.DefaultProperties;
import com.sirma.itt.seip.rest.utils.JsonKeys;
import com.sirma.sep.content.ContentInfo;
import com.sirma.sep.content.rest.ContentInfoJsonConverter;

/**
 * Test class for {@link ContentInfoJsonConverter}
 * 
 * @author Nikolay Ch
 */
public class ContentInfoJsonConverterTest {

	@Test
	public void testConetentInfoToJsonCovertion() {
		ContentInfo info = mock(ContentInfo.class);
		when(info.exists()).thenReturn(Boolean.TRUE);
		when(info.getName()).thenReturn("fileName");
		when(info.getMimeType()).thenReturn("text/plain");
		when(info.getLength()).thenReturn(10L);
		when(info.getContentId()).thenReturn("contentId");
		when(info.getChecksum()).thenReturn("checksum");

		ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
		JsonGenerator generator = Json.createGenerator(outputStream);
		
		generator.writeStartObject();
		ContentInfoJsonConverter.convertAndWriteToGenerator(generator, info);
		generator.writeEnd();
		
		generator.flush();
		
		InputStream inputStream = new ByteArrayInputStream(outputStream.toByteArray());

		JsonReader parser = Json.createReader(inputStream);

		JsonObject contentInfoJson = parser.readObject();

		Assert.assertTrue(contentInfoJson.containsKey(DefaultProperties.NAME));
		Assert.assertTrue(contentInfoJson.containsKey(DefaultProperties.PRIMARY_CONTENT_ID));
		Assert.assertTrue(contentInfoJson.containsKey(DefaultProperties.CONTENT_LENGTH));
		Assert.assertTrue(contentInfoJson.containsKey(DefaultProperties.MIMETYPE));
		Assert.assertTrue(contentInfoJson.containsKey(JsonKeys.CONTENT_CHECKSUM));
	}
}
