package com.sirma.itt.seip.eai.content.tool.service.net;

import static org.junit.Assert.assertEquals;

import java.net.URI;

import org.junit.Before;
import org.junit.Test;

/**
 * @author bbanchev
 */
public class URIBuilderTest {

	private URI rootUri;

	@Before
	public void setUp() throws Exception {
		rootUri = URI.create("http://localhost/");
	}

	@Test
	public void testAppendAndBuild() throws Exception {
		URIBuilder uriBuilder = new URIBuilder(rootUri);
		assertEquals(URI.create("http://localhost/test/"), uriBuilder.append("/test/").build());
		assertEquals(URI.create("http://localhost/test/test2/"), uriBuilder.append("/test2/").build());
		assertEquals(URI.create("http://localhost/test/test2/test3"), uriBuilder.append("test3").build());
		assertEquals(URI.create("http://localhost/test/test2/test3/test4/"), uriBuilder.append("test4/").build());
		assertEquals(URI.create("http://localhost/test/test2/test3/test4/test5?test6=test7"), uriBuilder.append("test5?test6=test7").build());
	}

}
