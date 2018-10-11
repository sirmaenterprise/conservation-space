package com.sirma.itt.seip.search;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Matchers;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.Spy;

import com.sirma.itt.seip.collections.CollectionUtils;
import com.sirma.itt.seip.domain.instance.Instance;
import com.sirma.itt.seip.domain.search.SearchArguments;
import com.sirma.itt.seip.domain.search.SearchRequest;
import com.sirma.itt.seip.domain.search.Sorter;
import com.sirma.itt.seip.search.facet.FacetService;

/**
 * Tests the logic in {@link SearchServiceImpl}.
 *
 * @author Mihail Radkov
 */
public class SearchServiceTest {

	@Spy
	private List<SearchEngine> engines = new ArrayList<>();

	@Mock
	private SearchConfiguration searchConfiguration;

	@Mock
	private javax.enterprise.inject.Instance<FacetService> facetService;

	@Mock
	private SearchEngine stubbedEngine;

	@InjectMocks
	private SearchService searchService;

	@Before
	public void initialize() {
		searchService = new SearchServiceImpl();
		MockitoAnnotations.initMocks(this);

		// Skipping faceting
		when(facetService.isUnsatisfied()).thenReturn(true);
		stubConfigurationService(1000);

		engines.clear();
		engines.add(stubbedEngine);
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
	public void shouldPrepareOrderByCodeValue() {
		Map<String, List<String>> requestMap = CollectionUtils.createHashMap(5);
		requestMap.put(SearchQueryParameters.ORDER_BY, Arrays.asList("emf:status"));
		requestMap.put(SearchQueryParameters.ORDER_BY_CODELIST_NUMBERS, Arrays.asList("2,3"));

		SearchRequest request = new SearchRequest(requestMap);
		stubSearchEngine(request);

		SearchArguments<Instance> searchArguments = searchService.parseRequest(request);
		Assert.assertNotNull(searchArguments);
		Assert.assertNotNull(searchArguments.getArguments());
		Assert.assertEquals(0, searchArguments.getArguments().size());

		Sorter sorter = searchArguments.getFirstSorter();

		Assert.assertEquals("emf:status", sorter.getSortField());
		Assert.assertEquals(true, sorter.isCodeListValue());
		Assert.assertNotNull(sorter.getCodelistNumbers());
		Assert.assertEquals(2, sorter.getCodelistNumbers().size());
	}
	
	@Test
	public void shouldPrepareOrderBy() {
		Map<String, List<String>> requestMap = CollectionUtils.createHashMap(5);
		requestMap.put(SearchQueryParameters.ORDER_BY, Arrays.asList("emf:modifiedOn"));

		SearchRequest request = new SearchRequest(requestMap);
		stubSearchEngine(request);

		SearchArguments<Instance> searchArguments = searchService.parseRequest(request);
		Assert.assertNotNull(searchArguments);
		Assert.assertNotNull(searchArguments.getArguments());
		Assert.assertEquals(0, searchArguments.getArguments().size());

		Sorter sorter = searchArguments.getFirstSorter();

		Assert.assertEquals("emf:modifiedOn", sorter.getSortField());
		Assert.assertEquals(false, sorter.isCodeListValue());
		Assert.assertEquals(Collections.emptyList(), sorter.getCodelistNumbers());
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
		Assert.assertTrue(searchArguments.getSorters().get(0).isMissingValuesAllowed());
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

	@Test
	public void shouldStreamEngineData() {
		when(stubbedEngine.isSupported(eq(Instance.class), any())).thenReturn(Boolean.TRUE);
		when(stubbedEngine.stream(any())).thenReturn(Stream.of(mock(ResultItem.class)));

		Stream<ResultItem> stream = searchService.stream(new SearchArguments<Instance>(), ResultItemTransformer.asIs());

		Assert.assertEquals(1L, stream.count());
	}

	@Test
	public void shouldReturnEmptyStreamIfNotSupported() {
		when(stubbedEngine.isSupported(eq(Instance.class), any())).thenReturn(Boolean.FALSE);

		Stream<ResultItem> stream = searchService.stream(new SearchArguments<Instance>(), ResultItemTransformer.asIs());

		Assert.assertEquals(0L, stream.count());
	}

	@Test(expected = IllegalArgumentException.class)
	public void groupBy_ShouldRequireGroupByFields() {
		searchService.groupBy(new SearchArguments<Instance>());
	}

	@Test
	public void groupBy_shouldPerformGrouping() {
		ResultItem item = mock(ResultItem.class);
		when(item.getGroupBy()).thenReturn("property");
		when(item.getGroupByValue()).thenReturn("value1");
		when(item.getGroupByCount()).thenReturn(2);
		when(stubbedEngine.isSupported(eq(Instance.class), any())).thenReturn(Boolean.TRUE);
		when(stubbedEngine.stream(any())).thenReturn(Stream.of(item));

		SearchArguments<Instance> arguments = new SearchArguments<>();
		arguments.setGroupBy(Collections.singletonList("property"));

		Map<String, Map<String, Integer>> groupBy = searchService.groupBy(arguments);

		Assert.assertEquals(1, groupBy.size());
		Assert.assertEquals(Integer.valueOf(2), groupBy.get("property").get("value1"));
	}

	private void stubSearchEngine(SearchRequest request) {
		when(stubbedEngine.prepareSearchArguments(eq(request), Matchers.any())).thenReturn(true);
	}

	private void stubConfigurationService(int maxSize) {
		when(searchConfiguration.getSearchResultMaxSize()).thenReturn(maxSize);
	}

}
