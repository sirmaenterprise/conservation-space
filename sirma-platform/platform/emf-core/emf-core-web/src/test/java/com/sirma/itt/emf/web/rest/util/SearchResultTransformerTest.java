package com.sirma.itt.emf.web.rest.util;

import java.io.Serializable;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import com.google.common.collect.Sets;
import com.sirma.itt.emf.web.header.InstanceHeaderBuilder;
import com.sirma.itt.seip.domain.instance.ClassInstance;
import com.sirma.itt.seip.domain.instance.EmfInstance;
import com.sirma.itt.seip.domain.instance.Instance;
import com.sirma.itt.seip.domain.search.SearchablePropertyProperties;
import com.sirma.itt.seip.domain.search.facet.Facet;
import com.sirma.itt.seip.domain.search.facet.FacetConfiguration;
import com.sirma.itt.seip.domain.search.facet.FacetProperties;
import com.sirma.itt.seip.json.JsonUtil;

/**
 * Test the {@link SearchResultTransformer}.
 *
 * @author nvelkov
 */
public class SearchResultTransformerTest {

	@Mock
	private InstanceHeaderBuilder treeHeaderBuilder;

	@InjectMocks
	private SearchResultTransformer SearchResultTransformer = new SearchResultTransformer();

	/**
	 * Initialize mocks and mock dictionary service.
	 */
	@BeforeClass
	public void init() {
		MockitoAnnotations.initMocks(this);
	}

	/**
	 * Test if the {@link SearchResultTransformer#getFacets(java.util.Collection)} method returns the correct
	 * information - the id and state of the facets.
	 *
	 * @throws JSONException
	 *             if an exception occurs
	 */
	@Test
	public void testGetFacetIds() throws JSONException {
		Facet facet = new Facet();
		facet.setId("someId");
		FacetConfiguration facetConfiguration = new FacetConfiguration();
		facetConfiguration.setState("expanded");
		facet.setFacetConfiguration(facetConfiguration);
		JSONArray array = SearchResultTransformer.getFacets(Arrays.asList(facet));
		Assert.assertEquals(array.getJSONObject(0).get(SearchablePropertyProperties.ID), "someId");
		Assert.assertEquals(array.getJSONObject(0).get(FacetProperties.STATE), "expanded");
	}

	/**
	 * Test if the {@link SearchResultTransformer#transformFacets(Map)} method returns the correct information - all
	 * facet properties.
	 *
	 * @throws JSONException
	 *             if an exception occurs
	 */
	@Test
	public void testTransformFacets() throws JSONException {
		Facet facet = new Facet();
		facet.setId("someId");
		facet.setCodelists(Sets.newHashSet(9));
		FacetConfiguration facetConfiguration = new FacetConfiguration();
		facetConfiguration.setState("expanded");
		facet.setFacetConfiguration(facetConfiguration);

		Map<String, Facet> facets = new HashMap<>();
		facets.put(facet.getId(), facet);
		JSONArray array = SearchResultTransformer.transformFacets(facets);
		Assert.assertEquals(array.getJSONObject(0).get(SearchablePropertyProperties.ID), "someId");
		Assert.assertEquals(array.getJSONObject(0).getJSONArray(SearchablePropertyProperties.CODELISTS).get(0), 9);
	}

	/**
	 * Test if the {@link SearchResultTransformer#transformResult(int, java.util.List, java.util.List, JSONObject)}
	 * method returns the correct information - all facet and instance properties.
	 *
	 * @throws JSONException
	 *             if an exception occurs
	 */
	@Test
	public void testTransformResultsWithFacets() throws JSONException {
		Facet facet = new Facet();
		facet.setId("id");
		Map<String, Facet> facets = new HashMap<>();
		facets.put(facet.getId(), facet);
		Instance instance = mockInstance(null);
		JSONObject result = new JSONObject();

		Map<Instance, Boolean> instances = new HashMap<>();
		instances.put(instance, true);
		SearchResultTransformer.transformResult(1, instances, null, result, facets);
		Assert.assertEquals(result.getJSONArray("facets").getJSONObject(0).get("id"), "id");
	}

	/**
	 * Test if the {@link SearchResultTransformer#transformResult(int, java.util.List, java.util.List, JSONObject)}
	 * method returns the correct information - all instance properties.
	 *
	 * @throws JSONException
	 *             if an exception occurs
	 */
	@Test
	public void testTransformResults() throws JSONException {
		Map<String, Serializable> properties = new HashMap<>();
		properties.put("propertyKey", "propertyValue");
		Instance instance = mockInstance(properties);
		JSONObject result = new JSONObject();
		SearchResultTransformer.transformResult(1, Arrays.asList(instance), Arrays.asList("propertyKey"), result);
		Assert.assertEquals(result.getJSONArray("values").getJSONObject(0).get("dbId"), "someId");
		Assert.assertEquals(result.getJSONArray("values").getJSONObject(0).get("propertyKey"), "propertyValue");
	}

	/**
	 * Test if the {@link SearchResultTransformer#transformResult(int, java.util.List, java.util.List, JSONObject)}
	 * method returns the correct information when an instance with no properties is supplied - the instance should be
	 * skipped and not added to the results.
	 *
	 * @throws JSONException
	 *             if an exception occurs
	 */
	@Test
	public void testTransformResultsInstanceWithNoProperties() throws JSONException {
		Instance instance = mockInstance(null);
		JSONObject result = new JSONObject();

		SearchResultTransformer.transformResult(1, Arrays.asList(instance), Arrays.asList("propertyKey"), result);
		Assert.assertTrue(JsonUtil.isNullOrEmpty(result.getJSONArray("values")));
	}

	/**
	 * Mock an {@link Instance}.
	 *
	 * @param properties
	 *            the properties of the instance
	 * @return the instance
	 */
	private static Instance mockInstance(Map<String, Serializable> properties) {
		Instance instance = new EmfInstance();
		instance.setId("someId");
		instance.setProperties(properties);
		ClassInstance classInstance = new ClassInstance();
		classInstance.setCategory("test");
		instance.setType(classInstance.type());
		return instance;
	}
}
