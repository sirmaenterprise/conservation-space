package com.sirma.itt.emf.solr.services;

import java.util.LinkedHashMap;
import java.util.TimeZone;

import javax.enterprise.inject.Produces;
import javax.inject.Inject;

import org.apache.solr.client.solrj.SolrClient;
import org.apache.solr.client.solrj.SolrQuery;
import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.common.params.CommonParams;
import org.apache.solr.common.util.NamedList;
import org.jglue.cdiunit.CdiRunner;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;

import com.sirma.itt.emf.solr.configuration.SolrConfiguration;
import com.sirma.itt.emf.solr.configuration.SolrSearchConfiguration;
import com.sirma.itt.emf.solr.constants.SolrQueryConstants;
import com.sirma.itt.emf.solr.exception.SolrClientException;
import com.sirma.itt.seip.configuration.ConfigurationProperty;

@RunWith(CdiRunner.class)
public class SolrConnectorTest {

	@Mock
	@Produces
	SolrConfiguration solrConfig;

	@Mock
	@Produces
	SolrSearchConfiguration searchConfiguration;

	@Mock
	SolrClient client;

	@Mock
	NamedList<Object> res;

	@Mock
	ConfigurationProperty<String> statusFilter;

	@Inject
	SolrConnectorImpl connector;

	@Before
	public void init() throws Exception {
		Mockito.when(statusFilter.isSet()).thenReturn(Boolean.TRUE);
		Mockito.when(statusFilter.get()).thenReturn("test filter");
		Mockito.when(searchConfiguration.getStatusFilterQuery()).thenReturn(statusFilter);

		Mockito.when(res.size()).thenReturn(0);
		Mockito.when(res.get("schema")).thenReturn(new LinkedHashMap<>());
		Mockito.when(client.request(Mockito.any(), Mockito.any())).thenReturn(res);
		Mockito.when(solrConfig.getSolrServer()).thenReturn(client);
	}

	@Test
	public void testRetrieveSchema() {
		Assert.assertNotNull(connector.retrieveSchema());
	}

	@Test
	public void testRetrieveSchemaException() throws Exception {
		Mockito.when(client.request(Mockito.any(), Mockito.any())).thenThrow(new SolrServerException("bummer"));
		Assert.assertNull(connector.retrieveSchema());
	}

	@Test
	public void testQueryDefaults() throws Exception {
		SolrQuery query = Mockito.spy(new SolrQuery());
		connector.query(query);

		Mockito.verify(query).set(CommonParams.TZ, TimeZone.getDefault().getID());
		Mockito.verify(query).set(CommonParams.ROWS, 25);
		Mockito.verify(query).set(CommonParams.Q, SolrQueryConstants.QUERY_DEFAULT_EMPTY);
		Mockito.verify(query).set(CommonParams.DF, "title,content");
		Mockito.verify(query).add(CommonParams.FQ, "test filter");
	}

	@Test
	public void testQueryDefaultsSet() throws SolrClientException {
		SolrQuery query = Mockito.mock(SolrQuery.class);
		Mockito.when(statusFilter.isSet()).thenReturn(Boolean.FALSE);
		Mockito.when(query.get(Mockito.anyString())).thenReturn("value");

		connector.query(query);

		Mockito.verify(query, Mockito.never()).set(Mockito.anyString(), Mockito.any());
		Mockito.verify(query, Mockito.never()).add(CommonParams.FQ, "test filter");
	}
}
