package com.sirma.itt.emf.solr.services;

import static com.sirma.itt.seip.collections.CollectionUtils.emptyList;
import static com.sirma.itt.seip.collections.CollectionUtils.emptyMap;
import static com.sirma.itt.seip.collections.CollectionUtils.isEmpty;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.argThat;
import static org.mockito.Mockito.verify;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.solr.client.solrj.SolrClient;
import org.apache.solr.client.solrj.request.UpdateRequest;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import com.sirma.itt.seip.testutil.CustomMatcher;

/**
 * Test for {@link SolrDataServiceImpl}.
 *
 * @author A. Kunchev
 */
public class SolrDataServiceImplTest {

	private SolrDataService service;

	@Mock
	private SolrClient solrClient;

	@Before
	public void setup() {
		service = new SolrDataServiceImpl();
		MockitoAnnotations.initMocks(this);
	}

	@Test(expected = NullPointerException.class)
	public void importData_nullClient() throws Exception {
		Map<String, Object> map = new HashMap<>();
		map.put("key", "value");
		service.addData(null, map);
	}

	@Test
	public void importData_nullData() throws Exception {
		Map<String, Object> data = null;
		service.addData(solrClient, data);
		verify(solrClient).request(argThat(CustomMatcher.of((UpdateRequest request) -> isEmpty(request.getDocuments()),
				"Found non empty request")), any());
	}

	@Test
	public void importData_emptyData() throws Exception {
		service.addData(solrClient, emptyMap());
		verify(solrClient).request(argThat(CustomMatcher.of((UpdateRequest request) -> isEmpty(request.getDocuments()),
				"Found non empty request")), any());
	}

	@Test
	public void importData_successful() throws Exception {
		Map<String, Object> data = new HashMap<>();
		data.put("string", "value-1");
		data.put("integer", 1);
		data.put("array", new String[] { "value-2", "value-3" });

		service.addData(solrClient, data);
		verify(solrClient).request(any(UpdateRequest.class), any());
	}

	@Test(expected = NullPointerException.class)
	public void importDataCollection_nullClient() throws Exception {
		service.addData(null, Arrays.asList(new HashMap<>()));
	}

	@Test
	public void importDataCollection_nullCollection() throws Exception {
		List<Map<String, Object>> data = null;
		service.addData(solrClient, data);
		verify(solrClient).request(argThat(CustomMatcher.of((UpdateRequest request) -> isEmpty(request.getDocuments()),
				"Found non empty request")), any());
	}

	@Test
	public void importDataCollection_emptyCollection() throws Exception {
		service.addData(solrClient, emptyList());
		verify(solrClient).request(argThat(CustomMatcher.of((UpdateRequest request) -> isEmpty(request.getDocuments()),
				"Found non empty request")), any());
	}

	@Test
	public void importDataCollection_successful() throws Exception {
		Map<String, Object> map1 = new HashMap<>();
		map1.put("string", "value-1");
		Map<String, Object> map2 = new HashMap<>();
		map2.put("integer", 1);
		Map<String, Object> map3 = new HashMap<>();
		map3.put("array", new String[] { "value-2", "value-3" });

		service.addData(solrClient, Arrays.asList(map1, map2, map3));
		verify(solrClient).request(any(UpdateRequest.class), any());
	}

}
