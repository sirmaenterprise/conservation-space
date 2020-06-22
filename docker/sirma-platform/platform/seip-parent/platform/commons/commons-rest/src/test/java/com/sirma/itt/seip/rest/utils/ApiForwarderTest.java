package com.sirma.itt.seip.rest.utils;

import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import javax.servlet.RequestDispatcher;
import javax.servlet.http.HttpServletRequest;

import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

/**
 * Tests for {@link ApiForwarder}.
 * 
 * @author yasko
 */
@Test
public class ApiForwarderTest {

	ApiForwarder forwarder = new ApiForwarder();
	
	@Mock
	HttpServletRequest request;
	
	@Mock
	RequestDispatcher dispatcher;
	
	/** init tests **/
	@BeforeMethod
	protected void init() {
		MockitoAnnotations.initMocks(this);
		when(request.getContextPath()).thenReturn("");
		when(request.getRequestDispatcher(anyString())).thenReturn(dispatcher);
	}
	
	/**
	 * Test various request forwards.
	 * @param expected Expected request path (+query) after forward.
	 * @param toForward Uri to forward (no query).
	 * @param query query params included in the request.
	 * @throws Exception thrown form classes being tested.
	 */
	@Test(dataProvider = "request-provider")
	public void testForwarding(String expected, String toForward, String query) throws Exception {
		when(request.getRequestURI()).thenReturn(toForward);
		when(request.getQueryString()).thenReturn(query);
		
		forwarder.doFilter(request, null, null);
		verify(request).getRequestDispatcher(expected);
		verify(dispatcher).forward(request, null);
	}
	
	/**
	 * {@link DataProvider} for {@link #testForwarding(String, String, String)}.
	 * @return test data.
	 */
	@DataProvider(name = "request-provider")
	protected Object[][] provideRequest() {
		return new Object[][] {
			{ "/api", "/service", "" },
			{ "/api/test", "/service/test", null },
			{ "/api?test=true", "/service", "test=true" }
		};
	}
}
