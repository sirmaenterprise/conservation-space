package com.sirma.itt.seip.search;

import com.sirma.itt.seip.collections.CollectionUtils;
import com.sirma.itt.seip.domain.instance.Instance;
import com.sirma.itt.seip.domain.search.SearchArguments;
import com.sirma.itt.seip.domain.search.SearchRequest;
import com.sirma.itt.seip.domain.search.Sorter;
import com.sirma.itt.seip.search.facet.FacetService;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Matchers;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.mockito.Spy;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

/**
 * Tests the logic in {@link SearchServiceImpl}.
 *
 * @author Mihail Radkov
 */
public class SearchServiceTest {

	@Spy
	private Iterable<SearchEngine> engines = new ArrayList<>();

	@Mock
	private SearchConfiguration searchConfiguration;

	@Mock
	private javax.enterprise.inject.Instance<FacetService> facetService;

	@InjectMocks
	private SearchService searchService;

	@Before
	public void initialize() {
		searchService = new SearchServiceImpl();
		MockitoAnnotations.initMocks(this);

		// Skipping faceting
		Mockito.when(facetService.isUnsatisfied()).thenReturn(true);
		stubConfigurationService(1000);
	}

	@Test
	public void shouldPrepareSearchArguments() {
		Map<String, List<String>> requestMap = CollectionUtils.createHashMap(5);
		SearchRequest request = new SearchRequest(requestMap);
		stubSearchEngine(request);

		SearchArguments<Instance> searchArguments = searchService.parseRequest(request);
		Assert.assertNotNull(searchArguments);
		Assert.assertNotNull(searchArguments.getArguments());
		Assert.assertEquals(0, searchArguments.getArguments().size());
	}

	@Test
	public void shouldReadDefaultSearchParameters() {
		Map<String, List<String>> requestMap = CollectionUtils.createHashMap(5);
		SearchRequest request = new SearchRequest(requestMap);
		request.add(SearchQueryParameters.PAGE_SIZE, "25");
		request.add(SearchQueryParameters.PAGE_NUMBER, "2");
		request.add(SearchQueryParameters.MAX_SIZE, "5000");
		request.add(SearchQueryParameters.ORDER_BY, "title");
		request.add(SearchQueryParameters.ORDER_DIRECTION, Sorter.SORT_DESCENDING);
		stubSearchEngine(request);

		SearchArguments<Instance> searchArguments = searchService.parseRequest(request);
		Assert.assertNotNull(searchArguments);
		Assert.assertEquals(25, searchArguments.getPageSize());
		Assert.assertEquals(2, searchArguments.getPageNumber());
		Assert.assertEquals(5000, searchArguments.getMaxSize());

		List<Sorter> sorters = searchArguments.getSorters();
		Assert.assertNotNull(sorters);
		Assert.assertEquals(1, sorters.size());
		Assert.assertEquals("title", searchArguments.getSorters().get(0).getSortField());
		Assert.assertFalse(searchArguments.getSorters().get(0).isAscendingOrder());
	}

	@Test
	public void shouldUseConfigurationForMaxSizeIfNotProvided() {
		Map<String, List<String>> requestMap = CollectionUtils.createHashMap(5);
		SearchRequest request = new SearchRequest(requestMap);
		stubSearchEngine(request);

		SearchArguments<Instance> searchArguments = searchService.parseRequest(request);
		Assert.assertNotNull(searchArguments);
		Assert.assertEquals(1000, searchArguments.getMaxSize());
	}

	private void stubSearchEngine(SearchRequest request) {
		SearchEngine stubbedEngine = Mockito.mock(SearchEngine.class);
		Mockito.when(stubbedEngine.prepareSearchArguments(Matchers.eq(request), Matchers.any())).thenReturn(true);
		((Collection<SearchEngine>) engines).add(stubbedEngine);
	}

	private void stubConfigurationService(int maxSize) {
		Mockito.when(searchConfiguration.getSearchResultMaxSize()).thenReturn(maxSize);
	}

}