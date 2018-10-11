package com.sirma.itt.emf.solr.services.impl.facet;

import java.io.Serializable;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

import org.apache.solr.client.solrj.SolrQuery;
import org.mockito.MockitoAnnotations;
import org.testng.Assert;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import com.sirma.itt.seip.domain.instance.Instance;
import com.sirma.itt.seip.domain.search.SearchArguments;
import com.sirma.itt.seip.domain.search.facet.Facet;
import com.sirma.itt.seip.domain.search.facet.FacetQueryParameters;

/**
 * Tests the utility methods for Solr specific operations for facets in {@link FacetSolrHelper}.
 *
 * @author Mihail Radkov
 * @since 1.10.1
 */
public class FacetSolrHelperTest {

	private FacetSolrHelper facetSolrHelper = new FacetSolrHelper();

	@BeforeMethod
	public void init() {
		MockitoAnnotations.initMocks(this);
	}

	/**
	 * Tests the logic inside {@link FacetSolrHelper#assignFacetArgumentsToSolrQuery(SearchArguments, SolrQuery)}.
	 */
	@Test
	public void testAssigningFacetArgumentsToSolrQuery() {
		Map<String, Serializable> facetArguments = new HashMap<>();
		facetArguments.put("1", "one");
		facetArguments.put("2", null);
		facetArguments.put(null, "three");

		SearchArguments<Instance> searchArguments = new SearchArguments<>();
		searchArguments.setFacetArguments(facetArguments);

		SolrQuery parameters = new SolrQuery();

		facetSolrHelper.assignFacetArgumentsToSolrQuery(searchArguments, parameters);
		Assert.assertEquals(parameters.getMap().size(), 1);

		String[] strings = parameters.getMap().get("1");
		Assert.assertNotNull(strings);
		Assert.assertEquals(strings[0], "one");
	}

	/**
	 * Tests the logic inside {@link FacetSolrHelper#generateSolrTextQuery(StringBuilder, Facet)}.
	 */
	@Test
	public void testSolrTextQueryBuilding() {
		Facet facet = new Facet();
		facet.setSolrFieldName("myFacet");

		Set<String> selectedVals = new LinkedHashSet<>();
		selectedVals.add("uno");
		selectedVals.add("does");
		facet.setSelectedValues(selectedVals);

		StringBuilder queryBuilder = new StringBuilder();

		facetSolrHelper.generateSolrTextQuery(queryBuilder, facet);

		Assert.assertEquals(queryBuilder.toString(), "myFacet:(uno does)");
	}

	/**
	 * Tests the logic inside {@link FacetSolrHelper#generateSolrDateQuery(StringBuilder, Facet)} when there are no
	 * dates provided but {@link FacetQueryParameters.DATE_UNSPECIFIED} and an incorrectly provided selected value.
	 */
	@Test
	public void testSolrDateQueryBuildingWithoutDates() {
		Facet facet = new Facet();
		facet.setSolrFieldName("myFacet");

		Set<String> selectedVals = new LinkedHashSet<>();
		selectedVals.add(FacetQueryParameters.DATE_UNSPECIFIED + FacetQueryParameters.DATE_SEPARATOR
				+ FacetQueryParameters.DATE_UNSPECIFIED);
		selectedVals.add("wrongValue");
		facet.setSelectedValues(selectedVals);

		StringBuilder queryBuilder = new StringBuilder();

		facetSolrHelper.generateSolrDateQuery(queryBuilder, facet);

		Assert.assertEquals(queryBuilder.toString(), "myFacet:[* TO *]");
	}

	/**
	 * Tests the logic inside {@link FacetSolrHelper#generateSolrDateQuery(StringBuilder, Facet)} when two ISO dates are
	 * provided as selected value.
	 */
	@Test
	public void testSolrDateQueryBuildingWithDates() {
		Facet facet = new Facet();
		facet.setSolrFieldName("myFacet");

		Set<String> selectedVals = new LinkedHashSet<>();
		selectedVals.add(FacetQueryParameters.DATE_UNSPECIFIED + FacetQueryParameters.DATE_SEPARATOR
				+ FacetQueryParameters.DATE_UNSPECIFIED);
		selectedVals.add("2011-01-01T12:13:15.000Z" + FacetQueryParameters.DATE_SEPARATOR + "2012-02-03T12:13:15.000Z");
		facet.setSelectedValues(selectedVals);

		StringBuilder queryBuilder = new StringBuilder();

		facetSolrHelper.generateSolrDateQuery(queryBuilder, facet);

		Assert.assertEquals(queryBuilder.toString(),
				"myFacet:[* TO *] OR myFacet:[\"2011-01-01T12:13:15.000Z\" TO \"2012-02-03T12:13:15.000Z\"]");
	}
}
