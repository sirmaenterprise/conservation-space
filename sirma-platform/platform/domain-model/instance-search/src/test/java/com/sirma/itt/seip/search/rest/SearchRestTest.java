package com.sirma.itt.seip.search.rest;

import javax.ws.rs.core.UriInfo;

import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Matchers;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

import com.sirma.itt.seip.domain.instance.Instance;
import com.sirma.itt.seip.domain.rest.BadRequestException;
import com.sirma.itt.seip.domain.search.SearchArguments;
import com.sirma.itt.seip.domain.search.SearchRequest;
import com.sirma.itt.seip.search.SearchService;

/**
 * Tests the logic in {@link SearchRest}.
 * 
 * @author Mihail Radkov
 */
public class SearchRestTest {

	@Mock
	private SearchService searchService;

	@InjectMocks
	private SearchRest searchRest;

	@Before
	public void initialize() {
		MockitoAnnotations.initMocks(this);
	}

	/**
	 * Tests the correct arguments provisioning to the search service.
	 */
	@Test
	public void testSearchWithQueryParams() {
		UriInfo uriInfo = Mockito.mock(UriInfo.class);
		SearchArguments<Instance> arguments = new SearchArguments<>();
		Mockito.when(searchService.parseRequest(Matchers.any(SearchRequest.class))).thenReturn(arguments);
		searchRest.search(uriInfo);
		Mockito.verify(searchService, Mockito.times(1)).searchAndLoad(Matchers.eq(Instance.class), Matchers.eq(arguments));
	}

	/**
	 * Tests when the search request is not supported by the search service.
	 */
	@Test(expected = BadRequestException.class)
	public void testSearchWithoutSupportedSearchRequest() {
		UriInfo uriInfo = Mockito.mock(UriInfo.class);
		Mockito.when(searchService.parseRequest(Matchers.any(SearchRequest.class))).thenReturn(null);
		searchRest.search(uriInfo);
	}
}
