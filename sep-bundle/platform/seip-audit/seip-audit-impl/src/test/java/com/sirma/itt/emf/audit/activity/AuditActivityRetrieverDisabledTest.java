package com.sirma.itt.emf.audit.activity;

import static org.junit.Assert.assertEquals;

import java.util.List;

import javax.inject.Inject;

import junit.framework.AssertionFailedError;

import org.easymock.EasyMock;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

import com.sirma.itt.emf.audit.db.AuditService;
import com.sirma.itt.emf.audit.solr.query.SolrQueryParams;
import com.sirma.itt.emf.audit.solr.service.SolrServiceException;

import de.akquinet.jbosscc.needle.annotation.InjectInto;
import de.akquinet.jbosscc.needle.annotation.ObjectUnderTest;
import de.akquinet.jbosscc.needle.junit.NeedleRule;

/**
 * Tests the logic in {@link AuditActivityRetriever} when it is disabled.
 * 
 * @author Mihail Radkov
 */
public class AuditActivityRetrieverDisabledTest {

	/** The needle rule. */
	@Rule
	public NeedleRule needleRule = new NeedleRule();

	@ObjectUnderTest(id = "ar", implementation = AuditActivityRetrieverImpl.class)
	private AuditActivityRetriever activityRetriever;

	@Inject
	private AuditService auditService;

	/** Forces {@link AuditActivityRetriever} to be disabled. */
	@InjectInto(targetComponentId = "ar")
	private Boolean enabled = false;

	/**
	 * Setup a mock on {@link AuditService#getActivitiesBySolrQuery(SolrQueryParams)} to throw
	 * {@link AssertionFailedError} if it is invoked even once.
	 * 
	 * @throws SolrServiceException
	 */
	@Before
	public void setupMock() throws SolrServiceException {
		EasyMock.expect(
				auditService.getActivitiesBySolrQuery(EasyMock.anyObject(SolrQueryParams.class)))
				.andThrow(
						new AssertionFailedError(
								"AuditService should not be called when the retriever is disabled!"))
				.anyTimes();
		EasyMock.replay(auditService);
	}

	/**
	 * Tests the activity retrieving when {@link AuditActivityRetriever} is disabled.
	 */
	@Test
	public void testDisabledRetriever() {
		AuditActivityCriteria criteria = new AuditActivityCriteria();
		List<AuditActivity> result = activityRetriever.getActivities(criteria);
		assertEquals("Result should be an empty list.", 0, result.size());
	}

	/**
	 * Verifies the mock object's calls.
	 */
	@After
	public void verifyMockCalls() {
		EasyMock.verify(auditService);
	}

}
