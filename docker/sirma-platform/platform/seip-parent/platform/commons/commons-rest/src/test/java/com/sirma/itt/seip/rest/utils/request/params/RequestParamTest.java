package com.sirma.itt.seip.rest.utils.request.params;

import static org.mockito.Mockito.when;

import java.util.Arrays;

import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.UriInfo;

import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.testng.Assert;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import com.sirma.itt.seip.rest.utils.request.RequestInfo;
import com.sirma.itt.seip.rest.utils.request.params.RequestParam.Type;

/**
 * Tests for {@link RequestParam#get(RequestInfo)}.
 * @author yasko
 */
@Test
public class RequestParamTest {

	@InjectMocks
	RequestInfo request;
	
	@Mock
	UriInfo uriInfo;

	@Mock
	HttpHeaders headers;
	
	@Mock
	MultivaluedMap<String, String> params;
	
	/** Init tests. */
	@BeforeMethod
	protected void init() {
		MockitoAnnotations.initMocks(this);
		
		when(uriInfo.getPathParameters()).thenReturn(params);
		when(uriInfo.getQueryParameters()).thenReturn(params);
		when(headers.getRequestHeader("test")).thenReturn(Arrays.asList("testing"));
		when(params.get("id")).thenReturn(Arrays.asList("1"));
		when(params.get("lang")).thenReturn(Arrays.asList("en"));
	}
	
	/**
	 * Test various param types and values.
	 * @param param Param to test.
	 * @param expectedValue expected param value.
	 */
	@Test(dataProvider = "params-provider")
	public void testGetParamValue(RequestParam<?> param, Object expectedValue) {
		Object value = param.get(request);
		Assert.assertEquals(value, expectedValue);
	}
	
	/**
	 * Provides test {@link RequestParam}s.
	 * @return two dimensional array containing test params.
	 */
	@DataProvider(name = "params-provider")
	protected Object[][] provideParams() {
		return new Object[][] {
			{ new RequestParam<>("test", Type.HEADER, RequestParamConverters.FIRST_ITEM), "testing" },
			{ new RequestParam<>("id", Type.PATH, RequestParamConverters.FIRST_ITEM), "1" },
			{ new RequestParam<>("lang", Type.QUERY, RequestParamConverters.FIRST_ITEM), "en" },
			{ new RequestParam<>(null, null, RequestParamConverters.FIRST_ITEM), null },
			{ new RequestParam<>("non-existent", Type.HEADER, RequestParamConverters.FIRST_ITEM, "default"), "default" }
		};
	}
}
