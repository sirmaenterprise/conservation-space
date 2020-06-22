package com.sirma.itt.seip.rest.utils;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.List;

import javax.websocket.server.PathParam;
import javax.ws.rs.HeaderParam;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.HttpHeaders;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.runners.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class JaxRsAnnotationParameterNameProviderTest {

	@Test
	public void testParamNameLookup() throws NoSuchMethodException, SecurityException {
		Method method = JaxRsAnnotationParameterNameProviderTest.class.getDeclaredMethod("testResource", String.class, String.class, String.class, String.class);

		List<String> expected = Arrays.asList("p1", "p2", "Accept", "param3");
		List<String> actual = new JaxRsAnnotationParameterNameProvider().getParameterNames(method);
		Assert.assertEquals(expected, actual);
	}

	@Test
	public void testNoParams() throws NoSuchMethodException, SecurityException {
		Method method = JaxRsAnnotationParameterNameProviderTest.class.getDeclaredMethod("testNoParams");

		List<String> actual = new JaxRsAnnotationParameterNameProvider().getParameterNames(method);
		Assert.assertEquals(0, actual.size());
	}

	public void testResource(@PathParam("p1") String p1, @QueryParam("p2") String p2, @HeaderParam(HttpHeaders.ACCEPT) String accept, String body) {
		// dummy method used as test data
	}
}
