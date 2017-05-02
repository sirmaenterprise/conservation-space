package com.sirma.cmf.web.rest;

import java.io.Serializable;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import javax.ws.rs.core.MultivaluedHashMap;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;

import org.codehaus.jettison.json.JSONArray;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;
import org.mockito.InjectMocks;
import org.mockito.Matchers;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.testng.Assert;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import com.sirma.itt.commons.utils.reflection.ReflectionUtils;
import com.sirma.itt.emf.web.rest.util.SearchResultTransformer;
import com.sirma.itt.seip.ShortUri;
import com.sirma.itt.seip.Uri;
import com.sirma.itt.seip.convert.TypeConverter;
import com.sirma.itt.seip.domain.definition.label.LabelProvider;
import com.sirma.itt.seip.domain.instance.Instance;
import com.sirma.itt.seip.domain.search.SearchArguments;
import com.sirma.itt.seip.domain.search.SearchRequest;
import com.sirma.itt.seip.domain.search.SearchablePropertyProperties;
import com.sirma.itt.seip.domain.search.facet.Facet;
import com.sirma.itt.seip.domain.search.facet.FacetConfiguration;
import com.sirma.itt.seip.domain.search.facet.FacetProperties;
import com.sirma.itt.seip.resources.EmfUser;
import com.sirma.itt.seip.resources.ResourceService;
import com.sirma.itt.seip.resources.User;
import com.sirma.itt.seip.search.SearchConfiguration;
import com.sirma.itt.seip.search.SearchService;
import com.sirma.itt.seip.search.facet.FacetService;
import com.sirma.itt.seip.security.context.SecurityContext;

/**
 * Test the search rest service.
 *
 * @author nvelkov
 */
public class SearchRestServiceTest {

	@Mock
	private SearchService searchService;

	private SearchResultTransformer searchResultTransformer = new SearchResultTransformer();

	@Mock
	private FacetService facetService;

	@Mock
	private SecurityContext securityContext;

	@Mock
	private ResourceService resourceService;

	@Mock
	private TypeConverter typeConverter;

	@Mock
	private LabelProvider labelProvider;

	@Mock
	private SearchConfiguration searchConfiguration;

	@InjectMocks
	private SearchRestService searchRestService;

	@BeforeMethod
	public void init() {
		MockitoAnnotations.initMocks(this);
	}

	/**
	 * Test the getAvailableFacets method when normal facets are provided and returned as a json array.
	 *
	 * @throws JSONException
	 *             json exception
	 */
	@Test
	public void testGetAvailableFacets() throws JSONException {
		Facet testFacet = createFacet();
		User testUser = createUser();

		mockFacetService(testFacet);
		mockSecurityContext(testUser);
		mockResourceService(testUser);
		mockTypeConverter();
		initSearchResultTransformer();

		Response response = searchRestService.getAvailableFacets(createUriInfo());
		JSONObject facetJson = new JSONArray(response.getEntity().toString()).getJSONObject(0);

		Assert.assertEquals(testFacet.getId(), facetJson.get(SearchablePropertyProperties.ID));
		Assert.assertEquals(testFacet.getFacetConfiguration().getState(), facetJson.get(FacetProperties.STATE));
		Assert.assertEquals(testFacet.getText(), facetJson.get(SearchablePropertyProperties.TEXT));
		Assert.assertEquals(testFacet.getPropertyType(), facetJson.get(SearchablePropertyProperties.PROPERTY_TYPE));
	}

	/**
	 * Test the getAvailableFacets method when there are too much results and facetting can't be performed on them.
	 */
	@Test
	public void testGetAvailableFacetsTooMuchResults() {
		User testUser = createUser();

		SearchArguments<Instance> searchArgs = createArguments();
		searchArgs.setTotalItems(1001);
		Mockito.when(searchConfiguration.getSearchResultMaxSize()).thenReturn(1000);
		Mockito.when(facetService.getAvailableFacets(Matchers.any(SearchRequest.class))).thenReturn(searchArgs);
		mockSecurityContext(testUser);
		mockResourceService(testUser);
		mockTypeConverter();
		initSearchResultTransformer();

		Response response = searchRestService.getAvailableFacets(createUriInfo());

		Assert.assertEquals(response.getStatus(), 403);
	}

	/**
	 * Test the getAvailableFacets method when there are no results and facetting can't be performed on them.
	 */
	@Test
	public void testGetAvailableFacetsNoResults() {
		User testUser = createUser();

		SearchArguments<Instance> searchArgs = createArguments();
		searchArgs.setTotalItems(0);
		Mockito.when(searchConfiguration.getSearchResultMaxSize()).thenReturn(1000);
		Mockito.when(facetService.getAvailableFacets(Matchers.any(SearchRequest.class))).thenReturn(searchArgs);
		mockSecurityContext(testUser);
		mockResourceService(testUser);
		mockTypeConverter();
		initSearchResultTransformer();

		Response response = searchRestService.getAvailableFacets(createUriInfo());

		Assert.assertEquals(response.getStatus(), 403);
	}

	/**
	 * Test the getAvailableFacets method when an exception is thrown during the user retrieval.
	 */
	@Test
	public void testGetAvailableFacetsWithoutUser() {
		Facet testFacet = createFacet();
		User testUser = createUser();

		mockFacetService(testFacet);
		mockSecurityContext(testUser);
		mockResourceService(null);
		mockTypeConverter();
		initSearchResultTransformer();

		Response response = searchRestService.getAvailableFacets(createUriInfo());

		Assert.assertEquals(response.getStatus(), 401);
	}

	/**
	 * Test the basic search.
	 */
	@Test
	public void testBasicSearch() {
		Facet testFacet = createFacet();
		User testUser = createUser();
		SearchArguments<Instance> searchArgs = createArguments();

		mockFacetService(testFacet);
		mockSecurityContext(testUser);
		mockResourceService(testUser);
		mockSearchService(searchArgs);
		mockTypeConverter();
		initSearchResultTransformer();

		Response response = searchRestService.performBasicSearch(createUriInfo());
		System.out.println(response);
	}

	/**
	 * Create an {@link UriInfo} with some queryparams in it.
	 *
	 * @return the uri info
	 */
	private static UriInfo createUriInfo() {
		UriInfo uriInfo = Mockito.mock(UriInfo.class);
		MultivaluedMap<String, String> queryParams = new MultivaluedHashMap<>();
		queryParams.put("key", Arrays.asList("value"));
		Mockito.when(uriInfo.getQueryParameters()).thenReturn(queryParams);
		return uriInfo;
	}

	/**
	 * Mock the facet service to return the provided facet when the available facets are requested.
	 *
	 * @param facet
	 *            the facet to be returned
	 */
	private void mockFacetService(Facet facet) {
		Map<String, Facet> facets = new HashMap<>();
		facets.put(facet.getId(), facet);
		SearchArguments<Instance> searchArgs = new SearchArguments<>();
		searchArgs.setFacets(facets);
		Mockito.when(facetService.getAvailableFacets(Matchers.any(SearchRequest.class))).thenReturn(searchArgs);
	}

	/**
	 * Mock the search service.
	 *
	 * @param searchArgs
	 *            the search arguments
	 */
	private void mockSearchService(SearchArguments<Instance> searchArgs) {
		Mockito.when(searchService.parseRequest(Matchers.any(SearchRequest.class))).thenReturn(searchArgs);
	}

	/**
	 * Mock the security context to return the provided user when the authenticated user is requested.
	 *
	 * @param user
	 *            the user to be returned
	 */
	private void mockSecurityContext(User user) {
		Mockito.when(securityContext.getAuthenticated()).thenReturn(user);
	}

	/**
	 * Mock the resource service to return the provided user when the user is requested.
	 *
	 * @param user
	 *            the user to be returned
	 */
	private void mockResourceService(User user) {
		Mockito.when(resourceService.loadByDbId(Matchers.any(Serializable.class))).thenReturn(user);
	}

	/**
	 * Mock the type converter so it returns an uri when a conversion is requested.
	 */
	private void mockTypeConverter() {
		Uri uri = new ShortUri("uri");
		Mockito.when(typeConverter.convert(Matchers.any(), Matchers.any(Serializable.class))).thenReturn(uri);
	}

	/**
	 * Initiate the search result transformer using reflection.
	 */
	private void initSearchResultTransformer() {
		ReflectionUtils.setField(searchRestService, "searchResultTransformer", searchResultTransformer);
	}

	/**
	 * Create a test facet.
	 *
	 * @return the facet
	 */
	private static Facet createFacet() {
		Facet facet = new Facet();
		facet.setId("facet");
		FacetConfiguration facetConfiguration = new FacetConfiguration();
		facetConfiguration.setState("expanded");
		facet.setFacetConfiguration(facetConfiguration);
		facet.setText("label");
		facet.setPropertyType("definition");
		return facet;
	}

	/**
	 * Craete test search arguments.
	 *
	 * @return the search arguments.
	 */
	private static SearchArguments<Instance> createArguments() {
		SearchArguments<Instance> searchArgs = new SearchArguments<>();
		return searchArgs;
	}

	/**
	 * Create a test user.
	 *
	 * @return the user
	 */
	private static User createUser() {
		User user = new EmfUser();
		user.setId("userId");
		return user;
	}
}
