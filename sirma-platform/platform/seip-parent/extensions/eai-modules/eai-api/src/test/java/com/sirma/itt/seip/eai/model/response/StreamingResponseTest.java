package com.sirma.itt.seip.eai.model.response;

import static org.junit.Assert.assertEquals;

import java.io.IOException;
import java.io.InputStream;

import org.junit.Test;

public class StreamingResponseTest {

	@Test(expected = IOException.class)
	public void testStreamingResponse() throws Exception {
		InputStream input = StreamingResponse.class.getResourceAsStream("test");
		InputStream data = null;
		try (StreamingResponse streamingResponse = new StreamingResponse(input, "application/json", "UTF-8", 1L)) {
			data = streamingResponse.getStream();
			assertEquals(data, streamingResponse.getStream());
			assertEquals("application/json", streamingResponse.getContentType());
			assertEquals("UTF-8", streamingResponse.getContentEncoding());
			assertEquals(1L, streamingResponse.getContentLength());
		}
		data.read();
	}
}
