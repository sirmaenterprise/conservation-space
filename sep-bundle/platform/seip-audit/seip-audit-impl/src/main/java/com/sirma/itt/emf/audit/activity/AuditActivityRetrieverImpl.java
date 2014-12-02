package com.sirma.itt.emf.audit.activity;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;

import javax.inject.Inject;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.time.DateFormatUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sirma.itt.emf.audit.configuration.AuditConfigurationProperties;
import com.sirma.itt.emf.audit.db.AuditService;
import com.sirma.itt.emf.audit.solr.query.ServiceResult;
import com.sirma.itt.emf.audit.solr.query.SolrQueryParams;
import com.sirma.itt.emf.audit.solr.service.SolrServiceException;
import com.sirma.itt.emf.configuration.Config;
import com.sirma.itt.emf.time.DateRange;
import com.sirma.itt.emf.time.TimeTracker;

/**
 * Class that retrieves activities from the audit DB. Uses {@link AuditService} for this purpose.
 * Works with {@link AuditActivityCriteria} for specifying what activities to be retrieved.
 * 
 * @author Mihail Radkov
 */
public class AuditActivityRetrieverImpl implements AuditActivityRetriever, Serializable {

	private static final long serialVersionUID = 4889608707185146547L;

	private static final Logger LOGGER = LoggerFactory.getLogger(AuditActivityRetrieverImpl.class);

	private static final String DATE_FORMAT = "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'";

	@Inject
	private AuditService auditService;

	@Inject
	@Config(name = AuditConfigurationProperties.AUDIT_ENABLED, defaultValue = "false")
	private Boolean auditEnabled;

	@Override
	public List<AuditActivity> getActivities(AuditActivityCriteria activityCriteria) {
		if (!auditEnabled || activityCriteria == null) {
			return Collections.emptyList();
		}
		TimeTracker tracker = TimeTracker.createAndStart();

		SolrQueryParams query = new SolrQueryParams();
		query.setQuery("*:*");
		// XXX: Optimization is needed!
		List<String> filters = new ArrayList<String>();
		addIfNotNull(filters, constructContext(activityCriteria.getIds()));
		addIfNotNull(filters, constructDateRange(activityCriteria.getDateRange()));
		addIfNotNull(filters, constructUser(activityCriteria.getIncludedUsername(), true));
		addIfNotNull(filters, constructUser(activityCriteria.getExcludedUsername(), false));
		sortBy(query, "eventdate", false);
		// We don't need authentication events.
		filters.add("-actionid:login");
		filters.add("-actionid:logout");
		query.setFilters(filters.toArray(new String[filters.size()]));

		LOGGER.debug("Solr query constrcuted in {} ms", tracker.stop());

		try {
			ServiceResult result = auditService.getActivitiesBySolrQuery(query);
			return result.getRecords();
		} catch (SolrServiceException e) {
			LOGGER.warn(e.getMessage(), e);
			return Collections.emptyList();
		}
	}

	/**
	 * Adds the provided {@link String} to the {@link List} if not null.
	 * 
	 * @param filters
	 *            the provided {@link List}
	 * @param filter
	 *            the provided {@link String}
	 */
	private void addIfNotNull(List<String> filters, String filter) {
		// TODO: What about empty?
		// XXX: The author does not like this method.
		if (filter != null) {
			filters.add(filter);
		}
	}

	/**
	 * Constructs Solr filter query upon the context by provided {@link List} of system IDs of EMF
	 * instances. If the {@link List} is null then null is returned.
	 * 
	 * @param ids
	 *            the {@link List} of system IDs
	 * @return Solr filter query as {@link String}
	 */
	private String constructContext(List<String> ids) {
		List<String> stripped = stripList(ids);
		if (!CollectionUtils.isEmpty(stripped)) {
			StringBuilder filter = new StringBuilder("context:(");
			filter.append(escapeSolrQuery(stripped.get(0)));
			for (int i = 1; i < stripped.size(); i++) {
				filter.append(" OR ");
				filter.append(escapeSolrQuery(stripped.get(i)));
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
	private List<String> stripList(List<String> ids) {
		List<String> stripedList = new LinkedList<>();
		if (!CollectionUtils.isEmpty(ids)) {
			stripedList.addAll(ids);
			stripedList.removeAll(Arrays.asList("", " ", null));
		}
		return stripedList;
	}

	/**
	 * Escapes ':' and '-' from provided {@link String}, because Solr interpretates them as special
	 * characters.
	 * 
	 * @param context
	 *            the {@link String} to be escaped
	 * @return the escaped {@link String}
	 */
	private String escapeSolrQuery(String context) {
		StringBuilder sb = new StringBuilder(context.length());
		for (int i = 0; i < context.length(); i++) {
			if (context.charAt(i) == ':') {
				sb.append("\\:");
			} else if (context.charAt(i) == '-') {
				sb.append("\\-");
			} else {
				sb.append(context.charAt(i));
			}
		}
		return sb.toString();
	}

	/**
	 * Constructs a Solr filter query for a date range by provided {@link DateRange}. If the
	 * provided range is null, the method returns null as well.
	 * 
	 * @param dateRange
	 *            the provided date range
	 * @return the query as {@link String}
	 */
	private String constructDateRange(DateRange dateRange) {
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
	 * Formats the provided date in ISO8601 standard, surrounds it with quotes and then adds it to
	 * the builder. If the date is null then '*' is added.
	 * 
	 * @param filterBuilder
	 *            the builder
	 * @param date
	 *            the provided date
	 */
	private void constructDate(StringBuilder filterBuilder, Date date) {
		if (date != null) {
			String isoDate = DateFormatUtils.format(date, DATE_FORMAT);
			filterBuilder.append("\"");
			filterBuilder.append(isoDate);
			filterBuilder.append("\"");
		} else {
			filterBuilder.append("*");
		}
	}

	/**
	 * Constructs Solr filter query for including or excluding specified username by a boolean
	 * parameter. If the username is null, then the result will be null as well.
	 * 
	 * @param user
	 *            the provided username
	 * @param include
	 *            specifies if the username is to be or not to be searched for
	 * @return the Solr query as {@link String}
	 */
	private String constructUser(String user, boolean include) {
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
	 * Sort activities results by field name. Can be sort ascending or descending depending on the
	 * boolean parameter.
	 * 
	 * @param query
	 *            the Solr query
	 * @param sortField
	 *            the sort field
	 * @param sortDirection
	 *            true for ascending or false for descending
	 */
	private void sortBy(SolrQueryParams query, String sortField, boolean sortDirection) {
		if (query != null && StringUtils.isNotEmpty(sortField)) {
			query.setSortField(sortField);
			if (sortDirection) {
				query.setSortOrder("asc");
			} else {
				query.setSortOrder("desc");
			}
		}
	}
}
