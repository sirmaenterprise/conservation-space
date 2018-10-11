package com.sirma.sep.content.rest;

import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyInt;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.atLeast;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.util.Arrays;

import javax.servlet.ServletException;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.core.MultivaluedHashMap;
import javax.ws.rs.core.Response;

import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

import com.sirma.itt.seip.rest.Range;
import com.sirma.itt.seip.security.authentication.AuthenticationContext;
import com.sirma.itt.seip.security.context.SecurityContextManager;
import com.sirma.itt.seip.testutil.mocks.ConfigurationPropertyMock;
import com.sirma.sep.content.Content;
import com.sirma.sep.content.ContentConfigurations;
import com.sirma.sep.content.rest.ContentDownloadService;
import com.sirma.sep.content.rest.ContentDownloadServlet;

/**
 * Tests for {@link ContentDownloadServlet}
 *
 * @author BBonev
 */
public class ContentDownloadServletTest {
	@InjectMocks
	private ContentDownloadServlet servlet;

	@Mock
	private ContentDownloadService downloadService;

	@Mock
	private HttpServletRequest req;

	@Mock
	private HttpServletResponse resp;

	@Mock
	private ContentConfigurations contentConfigurations;

	@Mock
	private SecurityContextManager securityContextManager;

	int responseStatus;

	@Before
	public void beforeMethod() throws IOException {
		MockitoAnnotations.initMocks(this);
		when(resp.getOutputStream()).thenReturn(mock(ServletOutputStream.class));
		when(contentConfigurations.getShareCodeSecretKey()).thenReturn(new ConfigurationPropertyMock<String>("secret"));
		doAnswer(new Answer<Integer>() {
			@Override
			public Integer answer(InvocationOnMock invocation) throws Throwable {
				responseStatus = invocation.getArgumentAt(0, Integer.class);
				return null;
			}
		}).when(resp).setStatus(anyInt());
	}

	@Test
	public void getHead() throws Exception {
		mockValidURI();
		mockValidHeader();

		servlet.doHead(req, resp);

		verify(resp, atLeast(1)).addHeader(anyString(), anyString());
		verify(resp).setStatus(anyInt());
	}

	@Test
	public void getHeadCustomPurpose() throws Exception {
		mockValidURI();
		mockValidHeader();
		mockPurpose();

		servlet.doHead(req, resp);

		verify(resp, atLeast(1)).addHeader(anyString(), anyString());
		verify(resp).setStatus(anyInt());
	}

	@Test
	public void getHead_invalidRequest() throws Exception {
		mockInvalidRequest();
		mockValidHeader();
		mockPurpose();

		servlet.doHead(req, resp);

		verify(resp, never()).addHeader(anyString(), anyString());
		verify(resp).setStatus(HttpServletResponse.SC_BAD_REQUEST);
	}

	@Test
	public void doGet() throws Exception {
		mockValidURI();
		when(req.getParameter("shareCode")).thenReturn("code");
		mockSecurityContextManager();
		servlet.doGet(req, resp);

		verify(downloadService).sendContent("emf:instance", Content.PRIMARY_CONTENT, Range.ALL, true, resp, null);
	}

	@Test
	public void doGetCustomPurpose() throws Exception {
		mockValidURI();
		mockPurpose();
		mockSecurityContextManager();
		
		servlet.doGet(req, resp);

		verify(downloadService).sendContent("emf:instance", "purpose", Range.ALL, true, resp, null);
	}

	@Test
	public void doGetInvalidRequest() throws Exception {
		mockInvalidRequest();

		servlet.doGet(req, resp);

		verify(resp).setStatus(HttpServletResponse.SC_BAD_REQUEST);
		verify(downloadService, never()).sendContent("emf:instance", Content.PRIMARY_CONTENT, Range.ALL, true, resp,
				null);
	}

	@Test
	public void should_getContentWithShareCode() throws ServletException, IOException {
		mockValidURI();

		when(contentConfigurations.getShareCodeSecretKey()).thenReturn(new ConfigurationPropertyMock<>("secretKey"));
		mockSecurityContextManager();

		servlet.doGet(req, resp);

		assertEquals(200, responseStatus);
		verify(securityContextManager).initializeExecution(any(AuthenticationContext.class));
		verify(downloadService).sendContent("emf:instance", Content.PRIMARY_CONTENT, Range.ALL, true, resp, null);
		verify(securityContextManager).endContextExecution();
	}

	@Test
	public void should_getErrorCode_whenContextExecution_hasFailed() throws ServletException, IOException {
		mockValidURI();
		when(contentConfigurations.getShareCodeSecretKey()).thenReturn(new ConfigurationPropertyMock<>("secretKey"));

		servlet.doGet(req, resp);
		assertEquals(403, responseStatus);
	}

	private void mockInvalidRequest() {
		when(req.getRequestURI()).thenReturn("/");
	}

	@SuppressWarnings("boxing")
	private void mockValidHeader() {
		Response response = mock(Response.class);
		when(response.getStatus()).thenReturn(HttpServletResponse.SC_OK);
		MultivaluedHashMap<String, String> multivaluedMap = new MultivaluedHashMap<>();
		multivaluedMap.put("header", Arrays.asList("headerValue"));
		when(response.getStringHeaders()).thenReturn(multivaluedMap);
		when(downloadService.getContentHeadResponse(anyString(), anyString())).thenReturn(response);
	}

	private void mockValidURI() {
		when(req.getRequestURI()).thenReturn("/emf:instance");
	}

	private void mockPurpose() {
		when(req.getParameter("purpose")).thenReturn("purpose");
	}
	
	private void mockSecurityContextManager() {
		when(securityContextManager.initializeExecution(any(AuthenticationContext.class))).thenReturn(true);
	}
}
