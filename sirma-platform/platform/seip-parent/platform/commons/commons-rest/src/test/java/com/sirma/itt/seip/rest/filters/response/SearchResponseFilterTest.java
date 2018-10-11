package com.sirma.itt.seip.rest.filters.response;

import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.container.ContainerResponseContext;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.UriBuilder;
import javax.ws.rs.core.UriInfo;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

import com.sirma.itt.seip.rest.models.SearchResponseWrapper;

/**
 * Tests for {@link SearchResponseFilter}.
 * 
 * @author yasko
 */
public class SearchResponseFilterTest {

	@Mock
	private SearchResponseWrapper<?> entity;

	@Mock
	private MultivaluedMap<String, Object> headers;

	@Mock
	private UriBuilder uriBuilder;

	@Mock
	private UriInfo uriInfo;

	@Mock
	private ContainerRequestContext request;

	@Mock
	private ContainerResponseContext response;

	private SearchResponseFilter filter = new SearchResponseFilter();

	@Before
	public void init() {
		MockitoAnnotations.initMocks(this);

		when(uriBuilder.toTemplate()).thenReturn("/search?q=test");
		when(uriInfo.getRequestUriBuilder()).thenReturn(uriBuilder);
		when(request.getUriInfo()).thenReturn(uriInfo);
		when(response.getHeaders()).thenReturn(headers);
		when(response.getEntity()).thenReturn(entity);
	}

	@Test
	public void testUnlimitedRequest() throws Exception {
		when(entity.getLimit()).thenReturn(-1);

		filter.filter(request, response);
		verify(headers, never()).add(Mockito.anyString(), Mockito.any());
	}

	@Test
	public void testLastPageRequest() throws Exception {
		when(entity.getTotal()).thenReturn(10L);
		when(entity.getLimit()).thenReturn(5);
		when(entity.getOffset()).thenReturn(5);

		String expected = "</search?q=test&offset=10&limit=5>; rel=\"next\", </search?q=test&offset=5&limit=5>; rel=\"last\"";
		filter.filter(request, response);
		verify(headers).add(HttpHeaders.LINK, expected);
	}

	@Test
	public void testRequest() throws Exception {
		when(entity.getTotal()).thenReturn(100L);
		when(entity.getLimit()).thenReturn(5);
		when(entity.getOffset()).thenReturn(5);

		String expected = "</search?q=test&offset=10&limit=5>; rel=\"next\", </search?q=test&offset=95&limit=5>; rel=\"last\"";
		filter.filter(request, response);
		verify(headers).add(HttpHeaders.LINK, expected);
	}

	@Test
	public void testNoResults() throws Exception {
		when(entity.getTotal()).thenReturn(0L);
		when(entity.getLimit()).thenReturn(5);
		when(entity.getOffset()).thenReturn(5);

		filter.filter(request, response);
		verify(headers, never()).add(anyString(), anyString());
	}
}
