package com.sirma.itt.seip.rest.client;

import static org.junit.Assert.assertEquals;

import java.net.URI;

import org.junit.Before;
import org.junit.Test;

import com.sirma.itt.seip.exception.EmfRuntimeException;

/**
 * @author bbanchev
 */
public class URIBuilderWrapperTest {

	@Before
	public void setUp() throws Exception {
	}

	@Test
	public void testConstruct() {
		URIBuilderWrapper uriBuilderWrapper = new URIBuilderWrapper("http://local/", "suburl", "/url2/");
		assertEquals(URI.create("http://local/suburl/url2/"), uriBuilderWrapper.build());

		uriBuilderWrapper = new URIBuilderWrapper("http://local/", "/suburl/", "/url2/");
		assertEquals(URI.create("http://local/suburl/url2/"), uriBuilderWrapper.build());
		uriBuilderWrapper = new URIBuilderWrapper(URI.create("http://local/"));
		assertEquals(URI.create("http://local/"), uriBuilderWrapper.build());
	}

	@Test
	public void testAppend() throws Exception {
		URIBuilderWrapper uriBuilderWrapper = new URIBuilderWrapper("http://local/", "/suburl/", "/url2/");
		uriBuilderWrapper.addParameter("test", "val");
		assertEquals(URI.create("http://local/suburl/url2/?test=val"), uriBuilderWrapper.build());
		uriBuilderWrapper = new URIBuilderWrapper("http://local", "/suburl/", "/url2/", "#/idoc");
		uriBuilderWrapper.addParameter("test", "val");
		uriBuilderWrapper.addParameter("test2", "val2");
		assertEquals(URI.create("http://local/suburl/url2/?test=val&test2=val2#/idoc"), uriBuilderWrapper.build());
	}

	@Test
	public void testCreate() {
		URI uri = URIBuilderWrapper.createURIByPaths("http://local/", "suburl", URI.create("/url2/"),
				new URIBuilderWrapper("/uri3/"));
		assertEquals(URI.create("http://local/suburl/url2/uri3/"), uri);
		uri = URIBuilderWrapper.createURIByPaths();
		assertEquals(URI.create("/"), uri);
		uri = URIBuilderWrapper.createURIByPaths(new String[] {});
		assertEquals(URI.create("/"), uri);
	}

	@Test(expected = EmfRuntimeException.class)
	public void testCreateInvalid() {
		URIBuilderWrapper.createURIByPaths("http://local/", "suburl", URI.create("/url2/"),
				new URIBuilderWrapper("/uri3/"), new Integer(1));
	}
}
