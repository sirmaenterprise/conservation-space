package com.sirma.itt.seip.annotations.rest;

import static javax.ws.rs.HttpMethod.GET;
import static javax.ws.rs.HttpMethod.HEAD;
import static javax.ws.rs.HttpMethod.OPTIONS;
import static javax.ws.rs.HttpMethod.POST;

import java.util.Arrays;
import java.util.Collection;
import java.util.function.BiConsumer;

import javax.ws.rs.core.Response;

import org.junit.Test;
import org.testng.Assert;

/**
 * Test for {@link AnnotationsRestOptions}
 *
 * @author BBonev
 */
public class AnnotationsRestOptionsTest {
	/**
	 * Tests all methods that handle OPTIONS requests
	 */
	@Test
	public void testHandleOptionsRequest() {
		BiConsumer<Response, Collection<String>> checkResponse = (response, allowedMethods) -> {
			Assert.assertNotNull(response);
			response.getAllowedMethods().containsAll(allowedMethods);
		};

		AnnotationsRestOptions restService = new AnnotationsRestOptions();

		Response optionsResponse = restService.handleCreateAnnotationOptions();
		checkResponse.accept(optionsResponse, Arrays.asList(POST, AnnotationsRestOptions.TRACE, OPTIONS));
		optionsResponse = restService.handleUpdateAnnotationOptions();
		checkResponse.accept(optionsResponse, Arrays.asList(POST, AnnotationsRestOptions.TRACE, OPTIONS));
		optionsResponse = restService.handleCreateMultipleAnnotationOptions();
		checkResponse.accept(optionsResponse, Arrays.asList(POST, AnnotationsRestOptions.TRACE, OPTIONS));
		optionsResponse = restService.handleDeleteAnnotationOptions();
		checkResponse.accept(optionsResponse, Arrays.asList(POST, AnnotationsRestOptions.TRACE, OPTIONS));
		optionsResponse = restService.handleSearchAnnotationOptions();
		checkResponse.accept(optionsResponse, Arrays.asList(GET, OPTIONS, AnnotationsRestOptions.TRACE, HEAD));
		optionsResponse = restService.handleIdOptions();
		checkResponse.accept(optionsResponse, Arrays.asList(GET, POST, AnnotationsRestOptions.PUT,
				AnnotationsRestOptions.DELETE, AnnotationsRestOptions.TRACE, OPTIONS, HEAD));
	}
}
