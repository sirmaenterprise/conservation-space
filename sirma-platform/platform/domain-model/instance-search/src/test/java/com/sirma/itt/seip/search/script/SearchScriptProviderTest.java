package com.sirma.itt.seip.search.script;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Collections;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.runners.MockitoJUnitRunner;

import com.sirma.itt.seip.context.Context;
import com.sirma.itt.seip.convert.TypeConverter;
import com.sirma.itt.seip.domain.instance.Instance;
import com.sirma.itt.seip.domain.search.SearchArguments;
import com.sirma.itt.seip.domain.search.SearchArguments.QueryResultPermissionFilter;
import com.sirma.itt.seip.domain.search.SearchInstance;
import com.sirma.itt.seip.script.ScriptInstance;
import com.sirma.itt.seip.search.SearchService;

/**
 * Unit test for {@link SearchScriptProvider}
 *
 * @author Valeri Tishev
 *
 */
@RunWith(MockitoJUnitRunner.class)
public class SearchScriptProviderTest {

	@InjectMocks
	private SearchScriptProvider CUT;

	@Mock
	private SearchService searchService;

	@Mock
	private TypeConverter typeConverter;

	/**
	 * Initialize the class under test (CUT)
	 */
	@Before
	public void init() {
		CUT = new SearchScriptProvider();
		MockitoAnnotations.initMocks(this);
	}

	/**
	 * Test with {@code null} {@link SearchArguments}
	 */
	@Test(expected = NullPointerException.class)
	public void withNullSearchArguments() {
		CUT.with(null);
	}

	/**
	 * Test building empty {@link SearchArguments}
	 */
	@Test
	public void getBuildEmptySearchArguments() {
		SearchArguments<Instance> searchArguments = CUT.buildEmptySearchArguments();
		assertNotNull(searchArguments);
		assertEquals(0, searchArguments.getMaxSize());
		assertEquals(QueryResultPermissionFilter.NONE, searchArguments.getPermissionsType());
	}

	/**
	 * Test search with empty results
	 */
	@Test
	public void searchWithEmptyResults() {
		SearchArguments<Instance> searchArguments = CUT.buildEmptySearchArguments();
		List<Instance> results = Collections.emptyList();
		searchArguments.setResult(results);
		ScriptInstance[] result = CUT.with(searchArguments);
		assertEquals(0, result.length);
	}

	@Test
	@SuppressWarnings("unchecked")
	public void buildArgumentsForPredefinedQuery_nullFilter() {
		SearchArguments<SearchInstance> result = CUT.buildArgumentsForPredefinedQuery(null, new Context<>());
		assertNotNull(result);
		verify(searchService, never()).getFilter(anyString(), eq(SearchInstance.class), any(Context.class));
	}

	@Test
	@SuppressWarnings("unchecked")
	public void buildArgumentsForPredefinedQuery_emptyFilter() {
		SearchArguments<SearchInstance> result = CUT.buildArgumentsForPredefinedQuery("", new Context<>());
		assertNotNull(result);
		verify(searchService, never()).getFilter(anyString(), eq(SearchInstance.class), any(Context.class));
	}

	@Test
	@SuppressWarnings("unchecked")
	public void buildArgumentsForPredefinedQuery_nullContext() {
		when(searchService.getFilter(eq("query"), eq(SearchInstance.class), any(Context.class)))
				.thenReturn(new SearchArguments<>());
		SearchArguments<SearchInstance> result = CUT.buildArgumentsForPredefinedQuery("query", null);
		assertNotNull(result);
		verify(searchService).getFilter(eq("query"), eq(SearchInstance.class), any(Context.class));
	}

	@Test
	public void buildArgumentsForPredefinedQuery_withContext() {
		Context<String, Object> context = new Context<>();
		when(searchService.getFilter("query", SearchInstance.class, context)).thenReturn(new SearchArguments<>());
		SearchArguments<SearchInstance> result = CUT.buildArgumentsForPredefinedQuery("query", context);
		assertNotNull(result);
		verify(searchService).getFilter("query", SearchInstance.class, context);
	}

}
