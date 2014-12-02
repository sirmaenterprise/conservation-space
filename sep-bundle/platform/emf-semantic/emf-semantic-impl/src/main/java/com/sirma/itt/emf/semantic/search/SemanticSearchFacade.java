package com.sirma.itt.emf.semantic.search;

import java.io.Serializable;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

import javax.annotation.PostConstruct;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.ws.rs.core.MultivaluedMap;

import org.apache.commons.lang.StringUtils;

import com.sirma.itt.cmf.search.SearchPreprocessorEngine;
import com.sirma.itt.emf.configuration.Config;
import com.sirma.itt.emf.configuration.EmfConfigurationProperties;
import com.sirma.itt.emf.instance.model.Instance;
import com.sirma.itt.emf.plugin.Extension;
import com.sirma.itt.emf.search.Query;
import com.sirma.itt.emf.search.SearchDialects;
import com.sirma.itt.emf.search.model.SearchArguments;
import com.sirma.itt.emf.search.model.Sorter;
import com.sirma.itt.emf.time.DateRange;

/**
 * The SemanticSearchFacade.
 */
@Extension(target = SearchPreprocessorEngine.NAME, priority = 2, enabled = true, order = 2)
@ApplicationScoped
public class SemanticSearchFacade implements SearchPreprocessorEngine {
	/** The converter date format pattern. */
	@Inject
	@Config(name = EmfConfigurationProperties.CONVERTER_DATE_FORMAT, defaultValue = "dd.MM.yyyy")
	private String converterDateFormatPattern;
	private SimpleDateFormat dateFormat;

	/**
	 * Initialize the date format
	 */
	@PostConstruct
	private void init() {
		dateFormat = new SimpleDateFormat(converterDateFormatPattern, Locale.ENGLISH);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void prepareBasicQuery(MultivaluedMap<String, String> queryParams,
			SearchArguments<Instance> searchArgs) throws Exception {
		searchArgs.setDialect(SearchDialects.SPARQL);
		searchArgs.setQuery(Query.getEmpty());

		String queryText = queryParams.getFirst("queryText");
		if (StringUtils.isNotBlank(queryText)) {
			searchArgs.setStringQuery(queryText);
		} else {
			readQueryArguments(queryParams, searchArgs);
		}
		String pageNumber = queryParams.getFirst("pageNumber");
		if (StringUtils.isNotBlank(pageNumber)) {
			searchArgs.setPageNumber(Integer.parseInt(pageNumber));
		}
		String pageSize = queryParams.getFirst("pageSize");
		if (StringUtils.isNotBlank(pageSize)) {
			searchArgs.setPageSize(Integer.parseInt(pageSize));
		}

		String orderBy = queryParams.getFirst("orderBy");
		if (StringUtils.isNotBlank(orderBy)) {
			searchArgs.setOrdered(true);

			String orderDirection = queryParams.getFirst("orderDirection");
			if (StringUtils.isNotBlank(orderDirection)) {
				searchArgs.setSorter(new Sorter(orderBy,
						"asc".equalsIgnoreCase(orderDirection) ? Sorter.SORT_ASCENDING
								: Sorter.SORT_DESCENDING));
			}
		} else {
			orderBy = "emf:modifiedOn";
			searchArgs.setOrdered(true);
			searchArgs.setSorter(new Sorter(orderBy, Sorter.SORT_DESCENDING));
		}
	}

	/**
	 * Read query arguments.
	 * 
	 * @param queryParams
	 *            the query params
	 * @param searchArgs
	 *            the search args
	 * @throws ParseException
	 *             the parse exception
	 */
	private void readQueryArguments(MultivaluedMap<String, String> queryParams,
			SearchArguments<Instance> searchArgs) throws ParseException {
		setNonRepeatingElements(queryParams.get("location[]"), "context", searchArgs);
		setNonRepeatingElements(queryParams.get("objectRelationship[]"), "relations", searchArgs);

		readSubTypes(queryParams, searchArgs);

		String metaText = queryParams.getFirst("metaText");
		if (StringUtils.isNotBlank(metaText)) {
			// searchArgs.setQuery(Query.getEmpty().or("dcterms:title",
			// metaText).or("dcterms:description", metaText));
			searchArgs.getArguments().put("fts", metaText);
			// searchArgs.getArguments().put("description", metaText);
		}

		String mimeType = queryParams.getFirst("mimetype");
		if (StringUtils.isNotBlank(mimeType)) {
			searchArgs.getArguments().put("emf:mimetype", mimeType);
		}

		String identifier = queryParams.getFirst("identifier");
		if (StringUtils.isNotBlank(identifier)) {
			searchArgs.getArguments().put("dcterms:identifier", identifier);
		}
		readDateRange(queryParams, searchArgs);

		readRdfTypes(queryParams, searchArgs);

		setNonRepeatingElements(queryParams.get("createdBy[]"), "emf:createdBy", searchArgs);
	}

	/**
	 * Sets the non repeating elements.
	 * 
	 * @param <E>
	 *            the element type
	 * @param source
	 *            the source
	 * @param target
	 *            the target
	 * @param searchArgs
	 *            the search args
	 */
	private <E> void setNonRepeatingElements(Collection<E> source, String target,
			SearchArguments<Instance> searchArgs) {
		if ((source != null) && !source.isEmpty()) {
			// remove duplicate values
			searchArgs.getArguments().put(target, new ArrayList<E>(new HashSet<>(source)));
		}
	}

	/**
	 * Read sub types.
	 * 
	 * @param queryParams
	 *            the query params
	 * @param searchArgs
	 *            the search args
	 */
	private void readSubTypes(MultivaluedMap<String, String> queryParams,
			SearchArguments<Instance> searchArgs) {
		List<String> subType = queryParams.get("subType[]");
		if ((subType != null) && !subType.isEmpty()) {
			Set<String> subTypes = new HashSet<>();
			Set<String> rdfTypes = new HashSet<>();
			for (String subTypeString : subType) {
				// check the subType if it is URI
				if (subTypeString.contains(":")) {
					rdfTypes.add(subTypeString);
				} else {
					subTypes.add(subTypeString);
				}
			}
			if (!subTypes.isEmpty()) {
				searchArgs.getArguments().put("emf:type", new ArrayList<String>(subTypes));
			}
			// when there is an URI in the subType list
			if (!rdfTypes.isEmpty()) {
				searchArgs.getArguments().put("rdf:type", new ArrayList<String>(rdfTypes));
			}
		}
	}

	/**
	 * Read date range.
	 * 
	 * @param queryParams
	 *            the query params
	 * @param searchArgs
	 *            the search args
	 * @throws ParseException
	 *             the parse exception
	 */
	private void readDateRange(MultivaluedMap<String, String> queryParams,
			SearchArguments<Instance> searchArgs) throws ParseException {
		Date start = null;
		Date end = null;
		String createdFrom = queryParams.getFirst("createdFromDate");
		if (StringUtils.isNotBlank(createdFrom)) {
			start = dateFormat.parse(createdFrom);
		}
		String createdTo = queryParams.getFirst("createdToDate");
		if (StringUtils.isNotBlank(createdTo)) {
			Calendar calendar = GregorianCalendar.getInstance();
			calendar.setTime(dateFormat.parse(createdTo));
			calendar.set(Calendar.HOUR_OF_DAY, 23);
			calendar.set(Calendar.MINUTE, 59);
			calendar.set(Calendar.SECOND, 59);
			end = calendar.getTime();
		}
		if ((start != null) || (end != null)) {
			searchArgs.getArguments().put("emf:createdOn", new DateRange(start, end));
		}
	}

	/**
	 * Read rdf types.
	 * 
	 * @param queryParams
	 *            the query params
	 * @param searchArgs
	 *            the search args
	 */
	@SuppressWarnings("unchecked")
	private void readRdfTypes(MultivaluedMap<String, String> queryParams,
			SearchArguments<Instance> searchArgs) {
		List<String> objectTypes = queryParams.get("objectType[]");
		if ((objectTypes != null) && !objectTypes.isEmpty()) {
			List<String> objectTypeValues = null;
			if (searchArgs.getArguments().containsKey("rdf:type")) {
				Serializable serializable = searchArgs.getArguments().get("rdf:type");
				// append to existing list
				if (serializable instanceof Collection) {
					Collection<String> collection = (Collection<String>) serializable;
					objectTypeValues = new ArrayList<String>(objectTypes.size()
							+ collection.size());
					// remove duplicate if any
					objectTypeValues.addAll(new HashSet<String>(collection));
				} else if (serializable instanceof String) {
					objectTypeValues = new ArrayList<String>(objectTypes.size() + 1);
					objectTypeValues.add(serializable.toString());
				}
			}
			if (objectTypeValues == null) {
				objectTypeValues = new ArrayList<>(objectTypes.size());
			}

			for (String objectType : objectTypes) {
				objectTypeValues.add(objectType);
			}

			searchArgs.getArguments().put("rdf:type", (Serializable) objectTypeValues);
		}
	}

	@Override
	public boolean isApplicable(MultivaluedMap<String, String> queryParams) {
		if (queryParams == null) {
			return false;
		}
		// enable always as this is currently the last engine to check
		// Update logic if order is changed
		return true;
	}

}
