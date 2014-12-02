package com.sirma.itt.emf.solr.services.ws;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.solr.client.solrj.SolrQuery;
import org.apache.solr.client.solrj.response.FacetField;
import org.apache.solr.client.solrj.response.QueryResponse;
import org.apache.solr.common.SolrDocument;
import org.apache.solr.common.SolrDocumentList;
import org.apache.solr.common.util.NamedList;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.mockito.Mockito;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.testng.Assert;
import org.testng.annotations.Test;

import com.sirma.itt.commons.utils.reflection.ReflectionUtils;
import com.sirma.itt.emf.solr.exception.SolrClientException;
import com.sirma.itt.emf.solr.services.SolrConnector;
import com.sirma.itt.emf.solr.services.query.parser.SolrQueryParserImpl;

/**
 * Tests of the solr rest services. We are testing the format of the returned results here, not the
 * actual data, as solr takes care of faceting & searching so we don't need to test that. The
 * methods under test are merely mediators between solr and the client and the only logic that they
 * provide is the logic, that filters the result and returns only the relevant stuff.
 */
public class SearchRestServiceTest {

	/** The rest service. */
	private SearchRestService restService = new SearchRestService();

	/**
	 * Testing the facet query. It should return only the facets of solr's {@link QueryResponse}.
	 * 
	 * @throws SolrClientException
	 *             the solr client exception
	 */
	@Test
	public void testFacetQuery() throws SolrClientException {
		List<FacetField> facets = new ArrayList<FacetField>();
		FacetField facet = new FacetField("fake");
		facet.add("fake", 5);
		facets.add(facet);
		mockSolrResult(facets, null, new ArrayList<FacetField>());
		String result = restService.facetQuery("", "", new ArrayList<String>(), null, null, null,
				null, true);
		Assert.assertEquals(result,
				"{\"facetDates\":[],\"facetFields\":[{\"fake\":[{\"count\":5,\"name\":\"fake\"}]}]}");
	}

	/**
	 * Testing the query. It should return only {@link SolrDocument} objects.
	 * 
	 * @throws SolrClientException
	 *             the solr client exception
	 * @throws JSONException
	 *             when the result cant be parsed to json
	 */
	@Test
	public void testQuery() throws SolrClientException, JSONException {
		SolrDocumentList documents = new SolrDocumentList();
		SolrDocument caseDocument = new SolrDocument();
		caseDocument.addField("createdBy", "admin");
		caseDocument.addField("title", "somethingsomethingdarkside");
		documents.add(caseDocument);
		mockSolrResult(new ArrayList<FacetField>(), documents, new ArrayList<FacetField>());
		String result = restService.query("", "5", null, null, null, null, null, null, "solr");
		JSONObject resultObject = new JSONObject(result);
		JSONObject expectedObject = new JSONObject(
				"{\"data\":[{\"createdBy\":\"admin\",\"title\":\"somethingsomethingdarkside\"}],\"paging\":{\"total\":0,\"skip\":\"0\",\"found\":1}}");
		Assert.assertEquals(jsonToMap(expectedObject), jsonToMap(resultObject));
	}

	/**
	 * Mocking the result that solr returns. We are not testing the actualy solr logic, so we can
	 * skip solr and just mock the result object that it provides.
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
	public void mockSolrResult(final List<FacetField> returnedFacets,
			final SolrDocumentList returnedDocuments, final List<FacetField> returnedDateFacets)
			throws SolrClientException {
		SolrConnector solrConnector = Mockito.mock(SolrConnector.class);

		Answer<QueryResponse> answer = new Answer<QueryResponse>() {

			@Override
			public QueryResponse answer(InvocationOnMock invocation) throws Throwable {
				QueryResponse response = new QueryResponse();
				final NamedList<Object> responseObject = new NamedList<Object>();
				responseObject.add("response", returnedDocuments);
				response.setResponse(responseObject);
				ReflectionUtils.setField(response, "_facetFields", returnedFacets);
				ReflectionUtils.setField(response, "_facetDates", returnedDateFacets);
				return response;
			}
		};

		Mockito.when(solrConnector.queryWithGet(Mockito.any(SolrQuery.class))).thenAnswer(answer);

		Mockito.when(solrConnector.queryWithPost(Mockito.any(SolrQuery.class))).thenAnswer(answer);

		ReflectionUtils.setField(restService, "solrConnector", solrConnector);
		SolrQueryParserImpl ftsQueryParserImpl = new SolrQueryParserImpl();
		ReflectionUtils.setField(ftsQueryParserImpl, "template", "all_text:({0})");
		ReflectionUtils.setField(ftsQueryParserImpl, "escapeRegex", "([:\\[\\]])");
		ftsQueryParserImpl.init();
		ReflectionUtils.setField(restService, "parser", ftsQueryParserImpl);
	}

	/**
	 * Converst {@link JSONObject} to map.
	 * 
	 * @param json
	 *            the json
	 * @return the map
	 * @throws JSONException
	 *             the jSON exception
	 */
	private Map jsonToMap(JSONObject json) throws JSONException {
		Map<String, Object> retMap = new HashMap<String, Object>();

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
	private Map toMap(JSONObject object) throws JSONException {
		Map<String, Object> map = new HashMap<String, Object>();

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
	private List toList(JSONArray array) throws JSONException {
		List<Object> list = new ArrayList<Object>();
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
