package com.sirma.itt.emf.semantic;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.testng.Assert.assertEquals;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

import javax.ws.rs.core.MultivaluedMap;

import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import com.sirma.itt.emf.semantic.search.SemanticSearchEngine;
import com.sirma.itt.seip.context.Context;
import com.sirma.itt.seip.domain.instance.Instance;
import com.sirma.itt.seip.domain.search.SearchArguments;
import com.sirma.itt.seip.domain.search.SearchDialects;
import com.sirma.itt.seip.domain.search.SearchFilter;
import com.sirma.itt.seip.domain.search.SearchFilterConfig;
import com.sirma.itt.seip.domain.search.SearchRequest;
import com.sirma.itt.seip.domain.search.Sorter;
import com.sirma.itt.seip.search.SearchArgumentProvider;
import com.sirma.itt.seip.search.SearchConfiguration;
import com.sirma.itt.seip.search.SearchEngine;
import com.sirma.itt.seip.search.SearchServiceImpl;
import com.sirma.itt.seip.search.facet.FacetService;
import com.sirma.itt.seip.testutil.EmfTest;
import com.sirma.itt.seip.testutil.mocks.InstanceProxyMock;
import com.sun.jersey.core.util.MultivaluedMapImpl;

/**
 * Unit tests for SearchServiceImpl methods
 *
 * @author S.Djulgerova
 */
@Test
public class SearchServiceImplTest extends EmfTest {

	/** search service implementation mock. */
	@InjectMocks
	private SearchServiceImpl searchServiceImpl;

	@Spy
	private Iterable<SearchEngine> engines = new ArrayList<>();

	@Spy
	private Iterable<SearchArgumentProvider> argumentProviders = new ArrayList<>();

	/** The semantic search engine. */
	@InjectMocks
	private SemanticSearchEngine semanticSearchEngine;

	@Mock
	FacetService facetService;

	@Spy
	private javax.enterprise.inject.Instance<FacetService> facetServiceInstance = new InstanceProxyMock<>(null);
	@Mock
	SearchConfiguration searchConfiguration;

	private static final String QUERY_NAME = "allCases";
	private static final String QUERY = "?instance a emf:Case .";

	@Override
	@BeforeMethod
	public void beforeMethod() {
		searchServiceImpl = new SearchServiceImpl() {

			private static final long serialVersionUID = 1L;

			@Override
			public <E> SearchFilterConfig getFilterConfiguration(String placeHolder, Class<E> resultType) {
				List<SearchFilter> first = new LinkedList<>();
				List<SearchFilter> second = new LinkedList<>();
				first.add(new SearchFilter("QUERY_NAME", "All Cases", "", null));
				second.add(new SearchFilter("emf:modifiedOn", "Created Date", "", null));
				return new SearchFilterConfig(first, second);
			}

			@Override
			public <S extends SearchArguments<?>> S buildSearchArguments(SearchFilter filter, Class<?> resultType,
					Context<String, Object> context) {
				SearchArguments args = new SearchArguments();
				args.setStringQuery(QUERY);
				args.setDialect(SearchDialects.SPARQL);
				args.setQueryName(QUERY_NAME);
				return (S) args;

			}
		};

		super.beforeMethod();

		((InstanceProxyMock<FacetService>) facetServiceInstance).set(facetService);

		((Collection<SearchEngine>) engines).add(semanticSearchEngine);

		((Collection<SearchArgumentProvider>) argumentProviders).add(mock(SearchArgumentProvider.class));

		when(searchConfiguration.getSearchResultMaxSize()).thenReturn(25);
	}

	/**
	 * Test parse request method when definition id is supplied
	 */
	public void testParseRequest() {

		// Wrong data test
		SearchArguments<Instance> searchArgs = searchServiceImpl.parseRequest(null);
		assertEquals(searchArgs, null);

		// Correct data test
		MultivaluedMap<String, String> queryParams = new MultivaluedMapImpl();
		queryParams.add("pageNumber", "1");
		queryParams.add("searchType", "basic");
		queryParams.add("pageSize", "25");
		queryParams.add("orderBy", "emf:modifiedOn");
		queryParams.add("instanceId",
				"http://ittruse.ittbg.com/ontology/enterpriseManagementFramework#cbcc8341-b015-4cd7-98bc-f69ab53e0584");
		queryParams.add("orderDirection", "desc");
		queryParams.add("queryName", QUERY_NAME);
		queryParams.add("definition", "projectdashboard_dashlet_cases");
		queryParams.add("userURI", "http://ittruse.ittbg.com/ontology/enterpriseManagementFramework#admin");

		searchArgs = searchServiceImpl.parseRequest(new SearchRequest(queryParams));

		// Correct arguments that have to be return if method succeed
		SearchArguments expetedArguments = new SearchArguments();
		expetedArguments.addSorter(new Sorter("emf:modifiedOn", "false"));
		expetedArguments.setDialect(SearchDialects.SPARQL);
		expetedArguments.setQueryName(QUERY_NAME);
		expetedArguments.setPageNumber(1);
		expetedArguments.setStringQuery(QUERY);

		assertEquals(searchArgs, expetedArguments);
	}

}
