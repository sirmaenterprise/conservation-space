package com.sirma.itt.emf.audit.solr;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import org.junit.Rule;
import org.junit.Test;

import com.sirma.itt.emf.audit.solr.query.SolrQueryParams;
import com.sirma.itt.emf.audit.solr.query.SolrResult;
import com.sirma.itt.emf.audit.solr.service.SolrServiceException;
import com.sirma.itt.emf.audit.solr.service.SolrServiceImpl;

import de.akquinet.jbosscc.needle.annotation.InjectInto;
import de.akquinet.jbosscc.needle.annotation.ObjectUnderTest;
import de.akquinet.jbosscc.needle.junit.NeedleRule;

/**
 * Tests {@link SolrServiceImpl} when the audit module is disabled.
 * 
 * @author Mihail Radkov
 */
public class SolrServiceDisabledTest {

	/** The solr service. */
	@ObjectUnderTest(id = "solr")
	private static SolrServiceImpl solrService;

	/** The needle rule. */
	@Rule
	public NeedleRule needleRule = new NeedleRule();

	/** Disables {@link SolrServiceImpl}. */
	@InjectInto(targetComponentId = "solr")
	private static Boolean enabled = false;

	/**
	 * Tests the data import.
	 */
	@Test
	public void testDataImport() {
		assertTrue(solrService.dataImport(true));
	}

	/**
	 * Tests the DB IDs retrieving when the module is disabled.
	 * 
	 * @throws SolrServiceException
	 *             if a problem occurs with Solr
	 */
	@Test
	public void testQuering() throws SolrServiceException {
		SolrQueryParams params = new SolrQueryParams();
		params.setQuery("*:*");

		SolrResult result = solrService.getIDsFromQuery(params);
		assertEquals(0, result.getTotal());
		assertTrue(result.getIds().isEmpty());
	}
}
