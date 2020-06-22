package com.sirma.itt.seip.configuration;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.net.URI;

import org.junit.Assert;
import org.junit.Test;

import com.sirma.itt.seip.configuration.convert.GroupConverterContext;

/**
 * Test for {@link SystemConfigurtionImpl}
 *
 * @author BBonev
 */
public class SystemConfigurtionImplTest {

	@Test
	public void shouldReturnCorrectURIWhenSystemDefaultRestActivatorPathNotStartsWithBackslash() {
		buildRestInvokerURLTest("http://localhost/", "/emf", "api", "Scenario system.default.restActivatorPath starts with /");
	}

	@Test
	public void shouldReturnCorrectURIWhenSystemDefaultRestActivatorPathStartsWithBackslash() {
		buildRestInvokerURLTest("http://localhost/", "/emf", "/api", "Scenario system.default.restActivatorPath starts with /");
	}

	@Test
	public void shouldReturnCorrectURIWhenSystemDefaultContextPathNotEndsWithBackslash() {
		buildRestInvokerURLTest("http://localhost/", "emf", "/api", "Scenario system.default.restEmfPath starts with /");
	}

	@Test
	public void shouldReturnCorrectURIWhenSystemDefaultContextPathEndsWithBackslash() {
		buildRestInvokerURLTest("http://localhost/", "/emf", "/api", "Scenario system.default.restEmfPath starts with /");
	}

	@Test
	public void shouldReturnCorrectURIWhenSystemDefaultUrlNotEndsWithBackslash() {
		buildRestInvokerURLTest("http://localhost", "/emf", "/api", "Scenario system.default.url ends with /");
	}

	@Test
	public void shouldReturnCorrectURIWhenSystemDefaultUrlEndsWithBackslash() {
		buildRestInvokerURLTest("http://localhost/", "/emf", "/api", "Scenario system.default.url ends with /");
	}

	public void buildRestInvokerURLTest(String systemAccessURI, String systemContextPath, String restActivatorPath, String scenarioInfo) {
		GroupConverterContext context = mock(GroupConverterContext.class);
		when(context.get("system.default.url")).thenReturn(URI.create(systemAccessURI));
		when(context.get("system.default.context.path")).thenReturn(systemContextPath);
		when(context.get("system.default.restActivatorPath")).thenReturn(restActivatorPath);

		URI uri = SystemConfigurtionImpl.buildRestInvokerURL(context);

		Assert.assertNotNull(scenarioInfo, uri);
		Assert.assertEquals(scenarioInfo, uri, URI.create("http://localhost/emf/api"));
	}
	@Test
	public void buildRestURL() throws Exception {
		GroupConverterContext context = mock(GroupConverterContext.class);

		when(context.get("system.default.url")).thenReturn(URI.create("http://localhost/"),
				URI.create("http://localhost"));
		when(context.get("system.default.restActivatorPath")).thenReturn("/api", "api");

		URI restURL = SystemConfigurtionImpl.buildRestURL(context);
		assertNotNull(restURL);
		assertEquals(URI.create("http://localhost/api"), restURL);

		restURL = SystemConfigurtionImpl.buildRestURL(context);
		assertNotNull(restURL);
		assertEquals(URI.create("http://localhost/api"), restURL);
	}
}
