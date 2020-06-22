package com.sirma.itt.emf.solr.services.impl.facet;

import java.io.Serializable;
import java.lang.invoke.MethodHandles;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.solr.client.solrj.SolrQuery;
import org.apache.solr.client.solrj.util.ClientUtils;
import org.apache.solr.common.params.FacetParams;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.joda.time.format.ISODateTimeFormat;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sirma.itt.seip.domain.instance.Instance;
import com.sirma.itt.seip.domain.search.SearchArguments;
import com.sirma.itt.seip.domain.search.facet.Facet;
import com.sirma.itt.seip.domain.search.facet.FacetQueryParameters;

/**
 * Helper class related to Apache Solr specific operations with facets.
 *
 * @author Mihail Radkov
 * @since 1.10.1
 */
public class FacetSolrHelper {

	private static final Logger LOGGER = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

	/**
	 * Adds default facet parameters to the provided {@link SolrQuery} object.
	 *
	 * @param parameters
	 *            - the provided Solr parameters
	 */
	public void addDefaultFacetParameters(SolrQuery parameters) {
		parameters.setFacet(true);
		parameters.setFacetMissing(true);
		parameters.set(FacetParams.FACET_MINCOUNT, 1);
		parameters.set(FacetParams.FACET_DATE_INCLUDE, FacetParams.FacetRangeInclude.LOWER.name());
		parameters.set(FacetParams.FACET_LIMIT, -1);
	}

	/**
	 * Assigns the facet arguments in the provided {@link SearchArguments} to the given {@link SolrQuery}. The arguments
	 * map must consist of non null keys & values.
	 *
	 * @param arguments
	 *            - the search arguments
	 * @param parameters
	 *            - the Solr query parameters
	 */
	public <E extends Instance, S extends SearchArguments<E>> void assignFacetArgumentsToSolrQuery(S arguments,
			SolrQuery parameters) {
		Map<String, Serializable> facetArguments = arguments.getFacetArguments();
		facetArguments.entrySet().stream().filter(e -> entryIsNotNull(e)).forEach(
				e -> parameters.add(e.getKey(), e.getValue().toString()));
	}



	/**
	 * Generates a Solr query for multiple text values based on the provided {@link Facet} and its selected values.
	 *
	 * @param query
	 *            - the query builder
	 * @param facet
	 *            - the provided facet
	 */
	public void generateSolrTextQuery(StringBuilder query, Facet facet) {
		String field = facet.getSolrFieldName();
		boolean appendSpace = false;

		query.append(field).append(":(");
		for (String selectedValue : facet.getSelectedValues()) {
			if (appendSpace) {
				query.append(" ");
			} else {
				appendSpace = true;
			}
			query.append(ClientUtils.escapeQueryChars(selectedValue));
		}
		query.append(")");
	}

	/**
	 * Generates a date range Solr query based on the provided {@link Facet} and its selected values. <br/>
	 * <b>NOTE</b>: The selected values should be a string pair separated by {@link FacetQueryParameters#DATE_SEPARATOR}
	 * .
	 *
	 * @param query
	 *            - the query builder
	 * @param facet
	 *            - the provided facet
	 */
	// TODO: Use TypeConverter for dates ?
	public void generateSolrDateQuery(StringBuilder query, Facet facet) {
		String field = facet.getSolrFieldName();
		boolean appendOr = false;

		for (String value : facet.getSelectedValues()) {
			String[] dates = value.split(FacetQueryParameters.DATE_SEPARATOR);
			if (dates.length <= 1) {
				LOGGER.warn("Facet date argument were not provided correctly! {}", value);
				continue;
			}

			if (appendOr) {
				query.append(" OR ");
			} else {
				appendOr = true;
			}

			String start = FacetQueryParameters.DATE_UNSPECIFIED;
			if (!start.equals(dates[0].trim())) {
				start = generateSolrDate(dates[0].trim());
			}
			String end = FacetQueryParameters.DATE_UNSPECIFIED;
			if (dates.length > 1 && !end.equals(dates[1].trim())) {
				end = generateSolrDate(dates[1].trim());
			}

			query.append(field).append(":[");
			query.append(start);
			query.append(" TO ");
			query.append(end);
			query.append("]");
		}
	}

	/**
	 * Transforms the provided date string into an UTC date and surrounds it with quotation marks. This is done to
	 * satisfy Solr's conventions for date querying.
	 *
	 * @param date
	 *            - the date as string
	 * @return - the generated Solr supported date
	 */
	private String generateSolrDate(String date) {
		StringBuilder builder = new StringBuilder();
		DateTime endDate = new DateTime(date, DateTimeZone.UTC);
		builder.append("\"");
		builder.append(endDate.toString(ISODateTimeFormat.dateTime()));
		builder.append("\"");
		return builder.toString();
	}

	/**
	 * Performs null checks on the key and value for the provided {@link Entry}.
	 *
	 * @param entry
	 *            - the entry to check
	 * @return true if it is not null or false otherwise
	 */
	private boolean entryIsNotNull(Entry<?, ?> entry) {
		return entry.getKey() != null && entry.getValue() != null;
	}
}
