package com.sirma.itt.emf.solr.services;

import java.io.Serializable;
import java.util.Collection;
import java.util.Date;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.time.DateFormatUtils;
import org.apache.solr.client.solrj.util.ClientUtils;

import com.sirma.itt.emf.solr.constants.SolrQueryConstants;
import com.sirma.itt.seip.time.DateRange;

/**
 * A helper interface for the solr query building.
 *
 * @author nvelkov
 */
public class SolrQueryHelper {

	protected static final String SOLR_DATE_FORMAT_PATTERN = "yyyy-MM-dd'T'HH:mm:ss'Z'";

	/**
	 * Creates a SOLR filter query from the given list of URIs, joining them with an OR statement. Creates sub queries
	 * for each 1000 records to bypass SOLR max boolean clause.
	 *
	 * @param uris
	 *            the URIs
	 * @param fieldName
	 *            the field name
	 * @return the created Solr query
	 */
	public static StringBuilder createUriQuery(Collection<? extends Serializable> uris, String fieldName) {
		StringBuilder filterQuery = new StringBuilder(uris.size() * 150);
		filterQuery.append("{!df=").append(fieldName).append(" q.op=OR} ((");
		int currentIndex = 0;
		for (Serializable uri : uris) {
			filterQuery.append(" \"");
			String escapedUri = ClientUtils.escapeQueryChars(uri.toString());
			filterQuery.append(escapedUri).append("\"");
			if (currentIndex > 0 && currentIndex % 1000 == 0) {
				filterQuery.append(") (");
			}

			currentIndex++;
		}

		return filterQuery.append("))");
	}

	/**
	 * Builds SOLR filter query based on given date range for given field. The build query looks like this:
	 * <code>fieldName:[firstDate TO secondDate]</code>. If one of the dates in the passed {@link DateRange} object is
	 * missing(passed <code>null</code> value), it is replaced by {@link SolrQueryConstants#WILD_CARD} symbol. The dates
	 * are converted to ISO8601, before adding them in the query.
	 * <p>
	 * Possible queries based on the passed date range: <br />
	 * <ul>
	 * <li><code>fieldName:[* TO secondDate]</code> - when the first date is missing or its not relevant</li>
	 * <li><code>fieldName:[firstDate TO *]</code> - when the second date is missing or its not relevant</li>
	 * <li><code>fieldName:[* TO *]</code> - when the both dates are missing. This case should be avoided, because the
	 * filter will do nothing, but the method could handle it</li>
	 * </ul>
	 *
	 * @param fieldName
	 *            the name of the field on which will be based the filter query
	 * @param range
	 *            {@link DateRange} from which will be build the filter
	 * @return filter query based on the period given in the date range
	 * @throws IllegalArgumentException
	 *             if some of the input parameters is missing or when the date range is valid
	 */
	public static StringBuilder buildDateRangeFilterQuery(String fieldName, DateRange range) {
		if (StringUtils.isBlank(fieldName) || range == null) {
			throw new IllegalArgumentException();
		}

		Date firstDate = range.getFirst();
		Date secondDate = range.getSecond();
		boolean firstNotNull = firstDate != null;
		boolean secondNotNull = secondDate != null;
		if (firstNotNull && secondNotNull) {
			validateRange(firstDate, secondDate);
		}

		String start = SolrQueryConstants.WILD_CARD;
		String end = SolrQueryConstants.WILD_CARD;
		if (firstNotNull) {
			start = DateFormatUtils.format(firstDate, SOLR_DATE_FORMAT_PATTERN);
		}

		if (secondNotNull) {
			end = DateFormatUtils.format(secondDate, SOLR_DATE_FORMAT_PATTERN);
		}

		return new StringBuilder(fieldName.length() + start.length() + end.length() + 10)
				.append(fieldName)
					.append(":[")
					.append(start)
					.append(" TO ")
					.append(end)
					.append("]");
	}

	private static void validateRange(Date first, Date second) {
		if (first.compareTo(second) > 0) {
			throw new IllegalArgumentException(
					"Date range isn't valid. Second date value is less than the first date value.");
		}
	}

}
