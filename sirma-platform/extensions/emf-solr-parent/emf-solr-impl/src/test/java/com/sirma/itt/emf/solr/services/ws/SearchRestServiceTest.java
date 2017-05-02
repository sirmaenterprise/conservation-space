package com.sirma.itt.emf.solr.services.ws;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.when;
import static org.mockito.MockitoAnnotations.initMocks;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

import org.apache.solr.client.solrj.SolrQuery;
import org.apache.solr.client.solrj.response.FacetField;
import org.apache.solr.client.solrj.response.QueryResponse;
import org.apache.solr.common.SolrDocument;
import org.apache.solr.common.SolrDocumentList;
import org.apache.solr.common.util.NamedList;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.stubbing.Answer;
import org.testng.Assert;

import com.sirma.itt.commons.utils.reflection.ReflectionUtils;
import com.sirma.itt.emf.solr.configuration.SolrSearchConfiguration;
import com.sirma.itt.emf.solr.exception.SolrClientException;
import com.sirma.itt.emf.solr.services.SolrConnector;
import com.sirma.itt.emf.solr.services.query.parser.SolrQueryParserImpl;
import com.sirma.itt.seip.ShortUri;
import com.sirma.itt.seip.Uri;
import com.sirma.itt.seip.convert.TypeConverter;
import com.sirma.itt.seip.permissions.action.AuthorityService;
import com.sirma.itt.seip.resources.EmfUser;
import com.sirma.itt.seip.resources.Resource;
import com.sirma.itt.seip.resources.ResourceService;
import com.sirma.itt.seip.security.context.SecurityContext;
import com.sirma.itt.seip.testutil.mocks.ConfigurationPropertyMock;

/**
 * Tests of the solr rest services. We are testing the format of the returned results here, not the actual data, as solr
 * takes care of faceting & searching so we don't need to test that. The methods under test are merely mediators between
 * solr and the client and the only logic that they provide is the logic, that filters the result and returns only the
 * relevant stuff.
 */
@org.testng.annotations.Test
public class SearchRestServiceTest {

	@Mock
	ResourceService resourceService;

	/** The rest service. */
	@InjectMocks
	SearchRestService restService = new SearchRestService();

	@Mock
	TypeConverter typeConverter;

	@Mock
	AuthorityService authorityService;

	@Mock
	SolrSearchConfiguration searchConfiguration;

	@Mock
	SolrConnector solrConnector;

	@InjectMocks
	SolrQueryParserImpl ftsQueryParserImpl;

	@Mock
	SecurityContext securityContext;

	/**
	 * Testing the query. It should return only {@link SolrDocument} objects.
	 *
	 * @throws SolrClientException
	 *             the solr client exception
	 * @throws JSONException
	 *             when the result can't be parsed to json
	 */
	public void testQuery() throws SolrClientException, JSONException {
		SolrDocumentList documents = new SolrDocumentList();
		SolrDocument caseDocument = new SolrDocument();
		caseDocument.addField("createdBy", "admin");
		caseDocument.addField("title", "somethingsomethingdarkside");
		documents.add(caseDocument);
		mockSolrResult(new ArrayList<FacetField>(), documents, new ArrayList<FacetField>());
		String result = restService.query("", "5", null, null, null, null, null, null, null, "solr");

		JSONObject resultObject = new JSONObject(result);
		JSONObject expectedObject = new JSONObject(
				"{\"data\":[{\"createdBy\":\"admin\",\"title\":\"somethingsomethingdarkside\"}],\"paging\":{\"total\":0,\"skip\":\"0\",\"found\":1}}");
		Assert.assertEquals(jsonToMap(expectedObject), jsonToMap(resultObject));
	}

	/**
	 * Mocking the result that solr returns. We are not testing the actualy solr logic, so we can skip solr and just
	 * mock the result object that it provides.
	 *
	 * @param returnedFacets
	 *            the returned facets
	 * @param returnedDocuments
	 *            the returned documents
	 * @param returnedDateFacets
	 *            the returned date facets
	 * @throws SolrClientException
	 *             the solr client exception
	 */
	void mockSolrResult(final List<FacetField> returnedFacets, final SolrDocumentList returnedDocuments,
			final List<FacetField> returnedDateFacets) throws SolrClientException {

		initMocks(this);

		Answer<QueryResponse> answer = invocation -> {
			QueryResponse response = new QueryResponse();
			final NamedList<Object> responseObject = new NamedList<>();
			responseObject.add("response", returnedDocuments);
			response.setResponse(responseObject);
			ReflectionUtils.setField(response, "_facetFields", returnedFacets);
			ReflectionUtils.setField(response, "_facetDates", returnedDateFacets);
			return response;
		};

		Mockito.when(solrConnector.queryWithGet(any(SolrQuery.class))).thenAnswer(answer);

		Mockito.when(solrConnector.queryWithPost(any(SolrQuery.class))).thenAnswer(answer);

		when(searchConfiguration.getStatusFilterQuery())
				.thenReturn(new ConfigurationPropertyMock<>((String) null, false));
		when(searchConfiguration.getFullTextSearchTemplate())
				.thenReturn(new ConfigurationPropertyMock<>("all_text:({0})"));
		when(searchConfiguration.getFullTextSearchEscapePattern())
				.thenReturn(new ConfigurationPropertyMock<>(Pattern.compile("([:\\[\\]])")));

		EmfUser user = new EmfUser();
		user.setId("emf:admin");
		Mockito.when(securityContext.getAuthenticated()).thenReturn(user);

		Mockito.when(authorityService.isAdminOrSystemUser(any(Resource.class))).thenReturn(Boolean.FALSE);

		Mockito.when(typeConverter.convert(Uri.class, "emf:admin")).thenReturn(new ShortUri("emf:admin"));

		Mockito.when(resourceService.loadByDbId(anyString())).thenReturn(user);
	}

	/**
	 * Converts {@link JSONObject} to map.
	 *
	 * @param json
	 *            the json
	 * @return the map
	 * @throws JSONException
	 *             the jSON exception
	 */
	private Map<String, Object> jsonToMap(JSONObject json) throws JSONException {
		Map<String, Object> retMap = new HashMap<>();

		if (json != JSONObject.NULL) {
			retMap = toMap(json);
		}
		return retMap;
	}

	/**
	 * Internal method recursively used to convert a {@link JSONObject} to a map.
	 *
	 * @param object
	 *            the object
	 * @return the map
	 * @throws JSONException
	 *             the jSON exception
	 */
	private Map<String, Object> toMap(JSONObject object) throws JSONException {
		Map<String, Object> map = new HashMap<>();

		Iterator<String> keysItr = object.keys();
		while (keysItr.hasNext()) {
			String key = keysItr.next();
			Object value = object.get(key);

			if (value instanceof JSONArray) {
				value = toList((JSONArray) value);
			}

			else if (value instanceof JSONObject) {
				value = toMap((JSONObject) value);
			}
			map.put(key, value);
		}
		return map;
	}

	/**
	 * Internal method recursively used to convert a {@link JSONArray} to a list.
	 *
	 * @param array
	 *            the array
	 * @return the map
	 * @throws JSONException
	 *             the jSON exception
	 */
	private List<Object> toList(JSONArray array) throws JSONException {
		List<Object> list = new ArrayList<>();
		for (int i = 0; i < array.length(); i++) {
			Object value = array.get(i);
			if (value instanceof JSONArray) {
				value = toList((JSONArray) value);
			}

			else if (value instanceof JSONObject) {
				value = toMap((JSONObject) value);
			}
			list.add(value);
		}
		return list;
	}
}
