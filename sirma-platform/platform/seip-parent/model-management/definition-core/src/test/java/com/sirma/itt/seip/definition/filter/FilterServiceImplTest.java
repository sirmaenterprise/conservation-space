package com.sirma.itt.seip.definition.filter;

import static org.junit.Assert.assertEquals;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import com.sirma.itt.seip.cache.lookup.EntityLookupCacheContext;
import com.sirma.itt.seip.db.DbDao;
import com.sirma.itt.seip.definition.model.FilterDefinitionImpl;
import com.sirma.itt.seip.definition.util.hash.HashCalculator;
import com.sirma.itt.seip.domain.filter.Filter;

/**
 * Test for {@link FilterServiceImp}.
 *
 * @author S.Djulgerova
 */
public class FilterServiceImplTest {

	@InjectMocks
	private FilterServiceImpl filterService;

	@Mock
	private DbDao dbDao;

	@Mock
	private EntityLookupCacheContext cacheContext;

	@Mock
	private HashCalculator hashCalculator;

	@Before
	public void init() {
		filterService = new FilterServiceImpl();
		MockitoAnnotations.initMocks(this);
	}

	@Test
	public void filterShouldReturnExcludedValues() {
		List<Filter> filters = new ArrayList<>();
		filters.add(initFilter("EXCLUDE", "filter2"));
		Set<String> toFilter = new HashSet<>(Arrays.asList("filter1", "filter2", "filter3", "filter4"));
		Set<String> expected = new HashSet<>(Arrays.asList("filter1", "filter3", "filter4"));
		filterService.filter(filters, toFilter);
		assertEquals(toFilter, expected);
	}

	@Test
	public void filterShouldReturnIncludedValues() {
		List<Filter> filters = new ArrayList<>();
		filters.add(initFilter("INCLUDE", "filter2"));
		Set<String> toFilter = new HashSet<>(Arrays.asList("filter1", "filter2", "filter3", "filter4"));
		Set<String> expected = new HashSet<>(Arrays.asList("filter2"));
		filterService.filter(filters, toFilter);
		assertEquals(toFilter, expected);
	}

	@Test
	public void filterShouldReturnIfNoFilter() {
		List<Filter> filters = null;
		Set<String> toFilter = new HashSet<>(Arrays.asList("filter1", "filter2", "filter3", "filter4"));
		Set<String> expected = new HashSet<>(Arrays.asList("filter1", "filter2", "filter3", "filter4"));
		filterService.filter(filters, toFilter);
		assertEquals(toFilter, expected);
	}

	@Test
	public void filterShouldReturnIfEmptyFilter() {
		List<Filter> filters = new ArrayList<>();
		Set<String> toFilter = new HashSet<>(Arrays.asList("filter1", "filter2", "filter3", "filter4"));
		Set<String> expected = new HashSet<>(Arrays.asList("filter1", "filter2", "filter3", "filter4"));
		filterService.filter(filters, toFilter);
		assertEquals(toFilter, expected);
	}

	@Test
	public void filterShouldReturnIfNoToFilter() {
		List<Filter> filters = new ArrayList<>();
		filters.add(initFilter("INCLUDE", "filter2"));
		Set<String> toFilter = null;
		filterService.filter(filters, toFilter);
		assertEquals(toFilter, null);
	}

	@Test
	public void filterShouldReturnIfEmptyToFilter() {
		List<Filter> filters = new ArrayList<>();
		filters.add(initFilter("INCLUDE", "filter2"));
		Set<String> toFilter = new HashSet<>();
		filterService.filter(filters, toFilter);
		assertEquals(toFilter, new HashSet<>());
	}

	@Test
	public void filterShouldReturnExcludedValues_twoFilters() {
		List<Filter> filters = new ArrayList<>();
		filters.add(initFilter("INCLUDE", "filter2"));
		filters.add(initFilter("INCLUDE", "filter2"));
		Set<String> toFilter = new HashSet<>(Arrays.asList("filter1", "filter2", "filter3", "filter4"));
		Set<String> expected = new HashSet<>(Arrays.asList("filter2"));
		filterService.filter(filters, toFilter);
		assertEquals(toFilter, expected);
	}

	@Test
	public void filterShouldReturnExcludedValues_twoFilters2() {
		List<Filter> filters = new ArrayList<>();
		String[] values = { "filter2", "filter3" };
		filters.add(initFilter("INCLUDE", values));
		filters.add(initFilter("EXCLUDE", "filter2"));

		Set<String> toFilter = new HashSet<>(Arrays.asList("filter1", "filter2", "filter3", "filter4"));
		Set<String> expected = new HashSet<>(Arrays.asList("filter3"));
		filterService.filter(filters, toFilter);
		assertEquals(toFilter, expected);
	}

	private static FilterDefinitionImpl initFilter(String mode, String... filterValue) {
		FilterDefinitionImpl filter = new FilterDefinitionImpl();
		filter.setMode(mode);
		Set<String> filterValues = new HashSet<>();
		for (String value : filterValue) {
			filterValues.add(value);
		}
		filter.setFilterValues(filterValues);
		return filter;
	}

}
