package com.sirma.itt.emf.audit.activity;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

import javax.inject.Inject;

import org.apache.commons.lang.time.DateFormatUtils;
import org.easymock.Capture;
import org.easymock.EasyMock;
import org.easymock.IAnswer;
import org.junit.Rule;
import org.junit.Test;

import com.sirma.itt.emf.audit.db.AuditService;
import com.sirma.itt.emf.audit.solr.query.ServiceResult;
import com.sirma.itt.emf.audit.solr.query.SolrQueryParams;
import com.sirma.itt.emf.audit.solr.service.SolrServiceException;
import com.sirma.itt.emf.time.DateRange;

import de.akquinet.jbosscc.needle.annotation.InjectInto;
import de.akquinet.jbosscc.needle.annotation.ObjectUnderTest;
import de.akquinet.jbosscc.needle.junit.NeedleRule;

/**
 * Tests the logic in {@link AuditActivityRetriever} when it is enabled.
 * 
 * @author Mihail Radkov
 */
public class AuditActivityRetrieverEnabledTest {

	/** The needle rule. */
	@Rule
	public NeedleRule needleRule = new NeedleRule();

	@ObjectUnderTest(id = "ar", implementation = AuditActivityRetrieverImpl.class)
	private AuditActivityRetriever activityRetriever;

	@Inject
	private AuditService auditService;

	/** Forces {@link AuditActivityRetriever} to be disabled. */
	@InjectInto(targetComponentId = "ar")
	private Boolean enabled = true;

	private final static String DATE_FORMAT = "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'";

	/**
	 * Tests invoking {@link AuditActivityRetriever#getActivities(AuditActivityCriteria)} with a
	 * null value.
	 */
	@Test(expected = AssertionError.class)
	public void testWithNullCriteria() {
		// TODO: Do it like in the disabled test?
		Capture<SolrQueryParams> capturedQuery = setupMock();
		activityRetriever.getActivities(null);
		capturedQuery.getValue();
	}

	/**
	 * Tests invoking {@link AuditActivityRetriever#getActivities(AuditActivityCriteria)} with an
	 * empty {@link SolrQueryParams}.
	 */
	@Test
	public void testEmptyCriteria() {
		Capture<SolrQueryParams> capturedQuery = setupMock();

		AuditActivityCriteria criteria = new AuditActivityCriteria();
		activityRetriever.getActivities(criteria);

		SolrQueryParams constructedQuery = capturedQuery.getValue();

		assertEquals("Sort field should be 'eventdate'", "eventdate",
				constructedQuery.getSortField());
		assertEquals("Sord direction should be 'desc'", "desc", constructedQuery.getSortOrder());
		assertEquals("Query should be '*:*'", "*:*", constructedQuery.getQuery());

		List<String> fiterQueries = Arrays.asList(constructedQuery.getFilters());
		assertTrue("Query should exclude 'login' actions", fiterQueries.contains("-actionid:login"));
		assertTrue("Query should exclude 'logout' actions",
				fiterQueries.contains("-actionid:logout"));

		assertEquals(2, fiterQueries.size());

		assertFalse("Query should not contain date range",
				fiterQueries.contains("eventdate:[* TO *]"));
	}

	/**
	 * Tests invoking {@link AuditActivityRetriever#getActivities(AuditActivityCriteria)} with an
	 * empty {@link DateRange} criteria.
	 */
	@Test
	public void testEmptyDateRangeCriteria() {
		Capture<SolrQueryParams> capturedQuery = setupMock();

		AuditActivityCriteria criteria = new AuditActivityCriteria();
		criteria.setDateRange(new DateRange(null, null));

		activityRetriever.getActivities(criteria);

		SolrQueryParams constructedQuery = capturedQuery.getValue();
		List<String> fiterQueries = Arrays.asList(constructedQuery.getFilters());

		assertTrue("Query should not contain date range",
				fiterQueries.contains("eventdate:[* TO *]"));

	}

	/**
	 * Tests invoking {@link AuditActivityRetriever#getActivities(AuditActivityCriteria)} with two
	 * dates in a {@link DateRange} criteria.
	 */
	@Test
	public void testFullDateRangeCriteria() {
		Capture<SolrQueryParams> capturedQuery = setupMock();

		Date date1 = new Date();
		Date date2 = new Date(date1.getTime() + 1000L);

		AuditActivityCriteria criteria = new AuditActivityCriteria();
		criteria.setDateRange(new DateRange(date1, date2));

		activityRetriever.getActivities(criteria);

		SolrQueryParams constructedQuery = capturedQuery.getValue();
		List<String> fiterQueries = Arrays.asList(constructedQuery.getFilters());

		String date1formatted = DateFormatUtils.format(date1, DATE_FORMAT);
		String date2formatted = DateFormatUtils.format(date2, DATE_FORMAT);
		String expected = "eventdate:[\"" + date1formatted + "\" TO \"" + date2formatted + "\"]";

		assertTrue(fiterQueries.contains(expected));
	}

	/**
	 * Tests invoking {@link AuditActivityRetriever#getActivities(AuditActivityCriteria)} with
	 * specifying what username to be included.
	 */
	@Test
	public void testIncludedUsernameCriteria() {
		Capture<SolrQueryParams> capturedQuery = setupMock();

		AuditActivityCriteria criteria = new AuditActivityCriteria();
		criteria.setIncludedUsername("includedUsername");

		activityRetriever.getActivities(criteria);

		SolrQueryParams constructedQuery = capturedQuery.getValue();
		List<String> fiterQueries = Arrays.asList(constructedQuery.getFilters());

		assertTrue(fiterQueries.contains("+username:includedUsername"));
	}

	/**
	 * Tests invoking {@link AuditActivityRetriever#getActivities(AuditActivityCriteria)} with
	 * specifying what username to be excluded.
	 */
	@Test
	public void testExcludedUsernameCriteria() {
		Capture<SolrQueryParams> capturedQuery = setupMock();

		AuditActivityCriteria criteria = new AuditActivityCriteria();
		criteria.setExcludedUsername("excludedUsername");

		activityRetriever.getActivities(criteria);

		SolrQueryParams constructedQuery = capturedQuery.getValue();
		List<String> fiterQueries = Arrays.asList(constructedQuery.getFilters());

		assertTrue(fiterQueries.contains("-username:excludedUsername"));
	}

	/**
	 * Tests invoking {@link AuditActivityRetriever#getActivities(AuditActivityCriteria)} with
	 * providing an empty {@link List} of IDs for context search.
	 */
	@Test
	public void testEmptyContextCriteria() {
		Capture<SolrQueryParams> capturedQuery = setupMock();

		AuditActivityCriteria criteria = new AuditActivityCriteria();
		criteria.setIds(new ArrayList<String>());

		activityRetriever.getActivities(criteria);

		SolrQueryParams constructedQuery = capturedQuery.getValue();
		List<String> fiterQueries = Arrays.asList(constructedQuery.getFilters());

		// The context filter query should not be added.
		assertEquals(2, fiterQueries.size());
	}

	/**
	 * Tests invoking {@link AuditActivityRetriever#getActivities(AuditActivityCriteria)} with
	 * providing a {@link List} of IDs for context search. The list contains null and empty strings
	 * which should be excluded in the filter query.
	 */
	@Test
	public void testContextCriteria() {
		Capture<SolrQueryParams> capturedQuery = setupMock();

		AuditActivityCriteria criteria = new AuditActivityCriteria();
		criteria.setIds(Arrays.asList("", "emf:1-4", "bam-1234", " ", "emf:1-3:4", null));

		activityRetriever.getActivities(criteria);

		SolrQueryParams constructedQuery = capturedQuery.getValue();
		List<String> fiterQueries = Arrays.asList(constructedQuery.getFilters());

		String expected = "context:(emf\\:1\\-4 OR bam\\-1234 OR emf\\:1\\-3\\:4)";
		assertTrue(fiterQueries.contains(expected));

	}

	/**
	 * Setups a {@link Capture} to {@link AuditService#getActivitiesBySolrQuery(SolrQueryParams)}
	 * mock which will hold the passed {@link SolrQueryParams}.
	 * 
	 * @return the {@link Capture}
	 */
	private Capture<SolrQueryParams> setupMock() {
		Capture<SolrQueryParams> capturedQuery = new Capture<SolrQueryParams>();
		try {
			EasyMock.expect(auditService.getActivitiesBySolrQuery(EasyMock.capture(capturedQuery)))
					.andAnswer(new IAnswer<ServiceResult>() {
						@Override
						public ServiceResult answer() throws Throwable {
							return new ServiceResult();
						}
					});
		} catch (SolrServiceException e) {
			e.printStackTrace();
		}
		EasyMock.replay(auditService);
		return capturedQuery;
	}
}
