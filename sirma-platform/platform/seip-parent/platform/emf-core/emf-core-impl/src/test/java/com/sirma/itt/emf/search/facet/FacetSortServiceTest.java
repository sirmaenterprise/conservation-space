/**
 *
 */
package com.sirma.itt.emf.search.facet;

import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import org.testng.Assert;
import org.testng.annotations.Test;

import com.sirma.itt.seip.domain.search.facet.Facet;
import com.sirma.itt.seip.domain.search.facet.FacetConfiguration;
import com.sirma.itt.seip.domain.search.facet.FacetSortOptions;
import com.sirma.itt.seip.domain.search.facet.FacetValue;
import com.sirma.itt.seip.search.facet.FacetSortService;
import com.sirma.itt.seip.search.facet.FacetSortServiceImpl;

/**
 * Tests the implementation logic in {@link FacetSortServiceImpl}.
 *
 * @author Mihail Radkov
 */
public class FacetSortServiceTest {

	private FacetSortService facetSortService = new FacetSortServiceImpl();

	/**
	 * Tests the sorting in {@link FacetSortServiceImpl#sort(Collection)} when no facet configuration is provided.
	 */
	@Test(expectedExceptions = IllegalArgumentException.class)
	public void testSortingWithoutConfiguration() {
		FacetValue value = generateValue("1", "one", 1);

		Facet facet = new Facet();
		facet.setValues(Arrays.asList(value));

		facetSortService.sort(Arrays.asList(facet));
	}

	/**
	 * Tests the sorting in {@link FacetSortServiceImpl#sort(Collection)} when configuration is configured to sort by
	 * title in default direction.
	 */
	@Test
	public void testSortingByLabelsAscending() {
		List<FacetValue> testValues = getTestValues();

		Facet facet = new Facet();
		facet.setValues(testValues);

		FacetConfiguration configuration = new FacetConfiguration();
		configuration.setSort(FacetSortOptions.SORT_ALPHABETICAL.getValue());

		facet.setFacetConfiguration(configuration);

		Collection<Facet> sorted = facetSortService.sort(Arrays.asList(facet));

		Assert.assertEquals(1, sorted.size());
		Iterator<Facet> iterator = sorted.iterator();
		Facet next = iterator.next();

		List<FacetValue> values = next.getValues();
		Assert.assertEquals(3, values.size());

		Assert.assertEquals("3", values.get(0).getId());
		Assert.assertEquals("1", values.get(1).getId());
		Assert.assertEquals("2", values.get(2).getId());
	}

	/**
	 * Tests the sorting in {@link FacetSortServiceImpl#sort(Collection)} when configuration is configured to sort by
	 * count/match in descending direction.
	 */
	@Test
	public void testSortingByCountsDescending() {
		List<FacetValue> testValues = getTestValues();

		Facet facet = new Facet();
		facet.setValues(testValues);

		FacetConfiguration configuration = new FacetConfiguration();
		configuration.setSort(FacetSortOptions.SORT_MATCH.getValue());
		configuration.setSortOrder(FacetSortOptions.SORT_DESCENDING.getValue());

		facet.setFacetConfiguration(configuration);

		Collection<Facet> sorted = facetSortService.sort(Arrays.asList(facet));

		Assert.assertEquals(1, sorted.size());
		Iterator<Facet> iterator = sorted.iterator();
		Facet next = iterator.next();

		List<FacetValue> values = next.getValues();
		Assert.assertEquals(3, values.size());

		Assert.assertEquals("2", values.get(0).getId());
		Assert.assertEquals("1", values.get(1).getId());
		Assert.assertEquals("3", values.get(2).getId());
	}

	/**
	 * Tests the ordering of {@link Facet}s in {@link FacetSortServiceImpl#sort(Collection)}.
	 */
	@Test
	public void sortFacetsByOrder() {
		List<FacetValue> testValues = getTestValues();

		Facet facet1 = new Facet();
		facet1.setId("1");
		facet1.setValues(testValues);

		FacetConfiguration configuration1 = new FacetConfiguration();
		configuration1.setOrder(2);
		facet1.setFacetConfiguration(configuration1);

		Facet facet2 = new Facet();
		facet2.setId("2");
		facet1.setValues(testValues);

		FacetConfiguration configuration2 = new FacetConfiguration();
		configuration2.setOrder(1);
		facet2.setFacetConfiguration(configuration2);

		Facet facet3 = new Facet();
		facet3.setId("3");
		facet3.setValues(testValues);

		FacetConfiguration configuration3 = new FacetConfiguration();
		configuration3.setOrder(3);
		facet3.setFacetConfiguration(configuration3);

		Collection<Facet> sorted = facetSortService.sort(Arrays.asList(facet1, facet2, facet3));
		Assert.assertEquals(3, sorted.size());

		Iterator<Facet> iterator = sorted.iterator();

		Assert.assertEquals("2", iterator.next().getId());
		Assert.assertEquals("1", iterator.next().getId());
		Assert.assertEquals("3", iterator.next().getId());
	}

	/**
	 * Generates test {@link FacetValue}s.
	 *
	 * @return the test values as list
	 */
	private List<FacetValue> getTestValues() {
		FacetValue value1 = generateValue("1", "one choo choo", 11);
		FacetValue value2 = generateValue("2", "two two moo", 21);
		FacetValue value3 = generateValue("3", "a value that is 3", 3);
		return Arrays.asList(value1, value2, value3);
	}

	/**
	 * Generate a test {@link FacetValue} based on the provided parameters:
	 *
	 * @param id
	 *            - the id of the value
	 * @param text
	 *            - the label of the value
	 * @param count
	 *            - the count of the value
	 * @return the generated test value
	 */
	private FacetValue generateValue(String id, String text, long count) {
		FacetValue value = new FacetValue();
		value.setCount(count);
		value.setId(id);
		value.setText(text);
		return value;
	}
}
