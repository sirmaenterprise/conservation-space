package com.sirma.itt.emf.audit.activity;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

import javax.inject.Inject;

import org.apache.solr.client.solrj.SolrQuery;
import org.apache.solr.client.solrj.SolrQuery.ORDER;
import org.easymock.Capture;
import org.easymock.EasyMock;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.joda.time.format.ISODateTimeFormat;
import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;

import com.sirma.itt.emf.audit.db.AuditService;
import com.sirma.itt.emf.audit.solr.query.ServiceResult;
import com.sirma.itt.emf.audit.solr.service.SolrServiceException;
import com.sirma.itt.seip.time.DateRange;

import de.akquinet.jbosscc.needle.annotation.ObjectUnderTest;
import de.akquinet.jbosscc.needle.junit.NeedleRule;

/**
 * Tests the logic in {@link AuditActivityRetriever} when it is enabled.
 *
 * @author Mihail Radkov
 */
public class AuditActivityRetrieverImplTest {

	/** The needle rule. */
	@Rule
	public NeedleRule needleRule = new NeedleRule();

	@ObjectUnderTest(id = "ar", implementation = AuditActivityRetrieverImpl.class)
	private AuditActivityRetriever activityRetriever;

	@Inject
	private AuditService auditService;

	/**
	 * Tests invoking {@link AuditActivityRetriever#getActivities(AuditActivityCriteria)} with a null value.
	 */
	@Test(expected = AssertionError.class)
	public void testWithNullCriteria() {
		// TODO: Do it like in the disabled test?
		Capture<SolrQuery> capturedQuery = setupMock();
		activityRetriever.getActivities(null);
		capturedQuery.getValue();
	}

	/**
	 * Tests invoking {@link AuditActivityRetriever#getActivities(AuditActivityCriteria)} with an empty
	 * {@link SolrQuery}.
	 */
	@Test
	public void testEmptyCriteria() {
		Capture<SolrQuery> capturedQuery = setupMock();

		AuditActivityCriteria criteria = new AuditActivityCriteria();
		activityRetriever.getActivities(criteria);

		SolrQuery constructedQuery = capturedQuery.getValue();

		assertEquals("Sort field should be 'eventdate' with desc order", "eventdate",
				constructedQuery.getSorts().get(0).getItem());
		assertEquals("Sord direction should be 'desc'", ORDER.desc, constructedQuery.getSorts().get(0).getOrder());
		assertEquals("Query should be '*:*'", "*:*", constructedQuery.getQuery());

		List<String> fiterQueries = Arrays.asList(constructedQuery.getFilterQueries());

		assertFalse("Query should not contain date range", fiterQueries.contains("eventdate:[* TO *]"));
	}

	/**
	 * Tests invoking {@link AuditActivityRetriever#getActivities(AuditActivityCriteria)} with an empty
	 * {@link DateRange} criteria.
	 */
	@Test
	public void testEmptyDateRangeCriteria() {
		Capture<SolrQuery> capturedQuery = setupMock();

		AuditActivityCriteria criteria = new AuditActivityCriteria();
		criteria.setDateRange(new DateRange(null, null));

		activityRetriever.getActivities(criteria);

		SolrQuery constructedQuery = capturedQuery.getValue();
		List<String> fiterQueries = Arrays.asList(constructedQuery.getFilterQueries());

		assertTrue("Query should not contain date range", fiterQueries.contains("eventdate:[* TO *]"));

	}

	/**
	 * Tests invoking {@link AuditActivityRetriever#getActivities(AuditActivityCriteria)} with two dates in a
	 * {@link DateRange} criteria.
	 */
	@Test
	public void testFullDateRangeCriteria() {
		Capture<SolrQuery> capturedQuery = setupMock();

		Date date1 = new Date();
		Date date2 = new Date(date1.getTime() + 1000L);

		AuditActivityCriteria criteria = new AuditActivityCriteria();
		criteria.setDateRange(new DateRange(date1, date2));

		activityRetriever.getActivities(criteria);

		SolrQuery constructedQuery = capturedQuery.getValue();
		List<String> fiterQueries = Arrays.asList(constructedQuery.getFilterQueries());

		String date1formatted = new DateTime(date1, DateTimeZone.UTC).toString(ISODateTimeFormat.dateTime());
		String date2formatted = new DateTime(date2, DateTimeZone.UTC).toString(ISODateTimeFormat.dateTime());
		String expected = "eventdate:[\"" + date1formatted + "\" TO \"" + date2formatted + "\"]";

		assertTrue(fiterQueries.contains(expected));
	}

	/**
	 * Tests invoking {@link AuditActivityRetriever#getActivities(AuditActivityCriteria)} with specifying what username
	 * to be included.
	 */
	@Test
	public void testIncludedUsernameCriteria() {
		Capture<SolrQuery> capturedQuery = setupMock();

		AuditActivityCriteria criteria = new AuditActivityCriteria();
		criteria.setIncludedUsername("includedUsername");

		activityRetriever.getActivities(criteria);

		SolrQuery constructedQuery = capturedQuery.getValue();
		List<String> fiterQueries = Arrays.asList(constructedQuery.getFilterQueries());

		assertTrue(fiterQueries.contains("+username:includedUsername"));
	}

	/**
	 * Tests invoking {@link AuditActivityRetriever#getActivities(AuditActivityCriteria)} with specifying what username
	 * to be excluded.
	 */
	@Test
	public void testExcludedUsernameCriteria() {
		Capture<SolrQuery> capturedQuery = setupMock();

		AuditActivityCriteria criteria = new AuditActivityCriteria();
		criteria.setExcludedUsername("excludedUsername");

		activityRetriever.getActivities(criteria);

		SolrQuery constructedQuery = capturedQuery.getValue();
		List<String> fiterQueries = Arrays.asList(constructedQuery.getFilterQueries());

		assertTrue(fiterQueries.contains("-username:excludedUsername"));
	}

	/**
	 * Tests invoking {@link AuditActivityRetriever#getActivities(AuditActivityCriteria)} with providing an empty
	 * {@link List} of IDs for context search.
	 */
	@Test
	public void testEmptyContextCriteria() {
		Capture<SolrQuery> capturedQuery = setupMock();

		AuditActivityCriteria criteria = new AuditActivityCriteria();
		criteria.setIds(new ArrayList<String>());

		activityRetriever.getActivities(criteria);

		SolrQuery constructedQuery = capturedQuery.getValue();
		List<String> fiterQueries = Arrays.asList(constructedQuery.getFilterQueries());

		// The context filter query should not be added.
		for (String filterQuery : fiterQueries) {
			Assert.assertFalse(filterQuery.contains("context"));
		}
	}

	/**
	 * Tests invoking {@link AuditActivityRetriever#getActivities(AuditActivityCriteria)} with providing a {@link List}
	 * of IDs for context search. The list contains null and empty strings which should be excluded in the filter query.
	 */
	@Test
	public void testContextCriteria() {
		Capture<SolrQuery> capturedQuery = setupMock();

		AuditActivityCriteria criteria = new AuditActivityCriteria();
		criteria.setIds(Arrays.asList("", "emf:1-4", "bam-1234", " ", "emf:1-3:4", null));

		activityRetriever.getActivities(criteria);

		SolrQuery constructedQuery = capturedQuery.getValue();
		List<String> fiterQueries = Arrays.asList(constructedQuery.getFilterQueries());

		String expected = "context:(emf\\:1\\-4 OR bam\\-1234 OR emf\\:1\\-3\\:4) OR objectsystemid:(emf\\:1\\-4 OR bam\\-1234 OR emf\\:1\\-3\\:4)";
		assertTrue(fiterQueries.contains(expected));

	}

	/**
	 * Setups a {@link Capture} to {@link AuditService#getActivitiesBySolrQuery(SolrQuery)} mock which will hold the
	 * passed {@link SolrQuery}.
	 *
	 * @return the {@link Capture}
	 */
	private Capture<SolrQuery> setupMock() {
		Capture<SolrQuery> capturedQuery = new Capture<>();
		try {
			EasyMock.expect(auditService.getActivitiesBySolrQuery(EasyMock.capture(capturedQuery))).andAnswer(
					() -> new ServiceResult());
		} catch (SolrServiceException e) {
			e.printStackTrace();
		}
		EasyMock.replay(auditService);
		return capturedQuery;
	}
}
