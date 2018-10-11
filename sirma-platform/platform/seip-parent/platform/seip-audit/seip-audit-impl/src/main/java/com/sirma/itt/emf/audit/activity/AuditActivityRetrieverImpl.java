package com.sirma.itt.emf.audit.activity;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import javax.inject.Inject;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.solr.client.solrj.SolrQuery;
import org.apache.solr.client.solrj.SolrQuery.ORDER;
import org.apache.solr.client.solrj.util.ClientUtils;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.joda.time.format.ISODateTimeFormat;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sirma.itt.emf.audit.db.AuditService;
import com.sirma.itt.emf.audit.solr.service.SolrServiceException;
import com.sirma.itt.seip.search.SearchConfiguration;
import com.sirma.itt.seip.time.DateRange;
import com.sirma.itt.seip.time.TimeTracker;

/**
 * Class that retrieves activities from the audit DB. Uses {@link AuditService} for this purpose. Works with
 * {@link AuditActivityCriteria} for specifying what activities to be retrieved.
 *
 * @author Mihail Radkov
 */
public class AuditActivityRetrieverImpl implements AuditActivityRetriever, Serializable {

	private static final List<String> LIST_CLEANING = Arrays.asList("", " ", null);

	private static final long serialVersionUID = 4889608707185146547L;

	private static final Logger LOGGER = LoggerFactory.getLogger(AuditActivityRetrieverImpl.class);

	@Inject
	private AuditService auditService;

	@Inject
	private SearchConfiguration searchConfiguration;

	// XXX: Optimization is needed!
	@Override
	public List<AuditActivity> getActivities(AuditActivityCriteria activityCriteria) {
		if (activityCriteria == null) {
			return Collections.emptyList();
		}
		TimeTracker tracker = TimeTracker.createAndStart();

		SolrQuery query = new SolrQuery();
		query.setQuery("*:*");

		List<String> filters = new ArrayList<>();
		String context = constructInQuery(activityCriteria.getIds(), "context");
		String ids = constructInQuery(activityCriteria.getIds(), "objectsystemid");

		if (context != null && ids != null) {
			filters.add(context + " OR " + ids);
		}
		// Excluding all activities that are not related to an instance.
		filters.add("+objectsystemid:*");

		addIfNotNullOrEmpty(filters, constructDateRange(activityCriteria.getDateRange()));
		addIfNotNullOrEmpty(filters, constructUser(activityCriteria.getIncludedUsername(), true));
		addIfNotNullOrEmpty(filters, constructUser(activityCriteria.getExcludedUsername(), false));
		sortBy(query, "eventdate", false);

		query.setFilterQueries(filters.toArray(new String[filters.size()]));
		query.setRows(searchConfiguration.getDashletPageSize());

		LOGGER.trace("Solr query constrcuted in {} ms", tracker.stop());

		try {
			return auditService.getActivitiesBySolrQuery(query).getRecords();
		} catch (SolrServiceException e) {
			LOGGER.warn(e.getMessage(), e);
			return Collections.emptyList();
		}
	}

	/**
	 * Adds the provided {@link String} to the {@link List} if not null or empty.
	 *
	 * @param filters
	 *            the provided {@link List}
	 * @param filter
	 *            the provided {@link String}
	 */
	private static void addIfNotNullOrEmpty(List<String> filters, String filter) {
		if (StringUtils.isNotEmpty(filter)) {
			filters.add(filter);
		}
	}

	/**
	 * Constructs Solr filter query of type <b>IN</b> by provided {@link List} of system IDs of EMF instances for the
	 * specific solr field. If the {@link List} is null then null is returned.
	 *
	 * @param ids
	 *            the {@link List} of system IDs
	 * @param field
	 *            - the specific solr field
	 * @return Solr filter query as {@link String}
	 */
	private static String constructInQuery(List<String> ids, String field) {
		List<String> stripped = stripList(ids);
		if (!CollectionUtils.isEmpty(stripped)) {
			StringBuilder filter = new StringBuilder(stripped.size() * 20);
			filter.append(field).append(":(");
			// changed to use iterator because the list returned from the method stripList is
			// LinkedList
			Iterator<String> it = stripped.iterator();
			filter.append(ClientUtils.escapeQueryChars(it.next()));
			while (it.hasNext()) {
				filter.append(" OR ");
				filter.append(ClientUtils.escapeQueryChars(it.next()));
			}
			filter.append(")");
			return filter.toString();
		}
		return null;
	}

	/**
	 * Removes any null, empty or blank {@link String} from the provided {@link List}.
	 *
	 * @param ids
	 *            the provided {@link List}
	 * @return new {@link List} without null, empty or blank {@link String}
	 */
	private static List<String> stripList(List<String> ids) {
		if (CollectionUtils.isEmpty(ids)) {
			return Collections.emptyList();
		}
		List<String> stripedList = new LinkedList<>(ids);
		stripedList.removeAll(LIST_CLEANING);
		return stripedList;
	}

	/**
	 * Constructs a Solr filter query for a date range by provided {@link DateRange}. If the provided range is null, the
	 * method returns null as well.
	 *
	 * @param dateRange
	 *            the provided date range
	 * @return the query as {@link String}
	 */
	private static String constructDateRange(DateRange dateRange) {
		if (dateRange != null) {
			StringBuilder filterBuilder = new StringBuilder();
			filterBuilder.append("eventdate:[");
			constructDate(filterBuilder, dateRange.getFirst());
			filterBuilder.append(" TO ");
			constructDate(filterBuilder, dateRange.getSecond());
			filterBuilder.append("]");
			return filterBuilder.toString();
		}
		return null;
	}

	/**
	 * Formats the provided date in ISO8601 standard, surrounds it with quotes and then adds it to the builder. If the
	 * date is null then '*' is added.
	 *
	 * @param filterBuilder
	 *            the builder
	 * @param date
	 *            the provided date
	 */
	private static void constructDate(StringBuilder filterBuilder, Date date) {
		if (date != null) {
			DateTime utcDate = new DateTime(date, DateTimeZone.UTC);
			String isoDate = utcDate.toString(ISODateTimeFormat.dateTime());
			filterBuilder.append("\"");
			filterBuilder.append(isoDate);
			filterBuilder.append("\"");
		} else {
			filterBuilder.append("*");
		}
	}

	/**
	 * Constructs Solr filter query for including or excluding specified username by a boolean parameter. If the
	 * username is null, then the result will be null as well.
	 *
	 * @param user
	 *            the provided username
	 * @param include
	 *            specifies if the username is to be or not to be searched for
	 * @return the Solr query as {@link String}
	 */
	private static String constructUser(String user, boolean include) {
		if (StringUtils.isNotEmpty(user)) {
			String filter = "";
			if (include) {
				filter = "+username:" + user;
			} else {
				filter = "-username:" + user;
			}
			return filter;
		}
		return null;
	}

	/**
	 * Sort activities results by field name. Can be sort ascending or descending depending on the boolean parameter.
	 *
	 * @param query
	 *            the Solr query
	 * @param sortField
	 *            the sort field
	 * @param sortDirection
	 *            true for ascending or false for descending
	 */
	private static void sortBy(SolrQuery query, String sortField, boolean sortDirection) {
		if (query != null && StringUtils.isNotEmpty(sortField)) {
			if (sortDirection) {
				query.setSort(sortField, ORDER.asc);
			} else {
				query.setSort(sortField, ORDER.desc);
			}
		}
	}
}
