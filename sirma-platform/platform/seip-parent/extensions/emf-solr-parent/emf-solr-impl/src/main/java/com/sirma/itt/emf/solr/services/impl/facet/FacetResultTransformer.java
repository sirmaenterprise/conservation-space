package com.sirma.itt.emf.solr.services.impl.facet;

import java.lang.invoke.MethodHandles;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import javax.inject.Inject;

import org.apache.commons.lang3.StringUtils;
import org.apache.solr.client.solrj.response.FacetField;
import org.apache.solr.client.solrj.response.FacetField.Count;
import org.apache.solr.client.solrj.response.QueryResponse;
import org.apache.solr.common.SolrDocument;
import org.apache.solr.common.SolrDocumentList;
import org.apache.solr.common.params.FacetParams.FacetRangeOther;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.joda.time.format.ISODateTimeFormat;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sirma.itt.emf.label.retrieve.FieldId;
import com.sirma.itt.emf.label.retrieve.FieldValueRetrieverParameters;
import com.sirma.itt.emf.label.retrieve.FieldValueRetrieverService;
import com.sirma.itt.seip.collections.CollectionUtils;
import com.sirma.itt.seip.domain.definition.label.LabelProvider;
import com.sirma.itt.seip.domain.instance.DefaultProperties;
import com.sirma.itt.seip.domain.instance.Instance;
import com.sirma.itt.seip.domain.search.SearchArguments;
import com.sirma.itt.seip.domain.search.SearchRequest;
import com.sirma.itt.seip.domain.search.facet.Facet;
import com.sirma.itt.seip.domain.search.facet.FacetQueryParameters;
import com.sirma.itt.seip.domain.search.facet.FacetValue;
import com.sirma.itt.semantic.NamespaceRegistryService;

/**
 * Transforms results from faceted searches.
 *
 * @author Mihail Radkov
 * @since 1.10.1
 */
public class FacetResultTransformer {

	private static final Logger LOGGER = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

	/** A collection of specific date ranges in the faceting. */
	private static final Collection<String> FACET_DATE_OTHER = new HashSet<>(
			Arrays.asList(FacetRangeOther.AFTER.name().toLowerCase(), FacetRangeOther.BEFORE.name().toLowerCase(),
					FacetRangeOther.BETWEEN.name().toLowerCase()));

	// TODO: So hardcoded... are there any constants for those?
	/** A collection of user types used to distinguish facets. */
	private static final Collection<String> USERS = new HashSet<>(Arrays.asList("emf:User", "ptop:Agent"));

	private static final String RDF_TYPE = "rdfType";
	private static final String TDATES_TYPE = "tdates";

	private static final String NO_VALUE_LABEL_KEY = "search.facet.novalue";

	@Inject
	private LabelProvider labelProvider;

	@Inject
	private NamespaceRegistryService namespaceRegistryService;

	@Inject
	private FieldValueRetrieverService fieldValueRetrievalService;

	/**
	 * Extracts the facet values from the provided Solr response and assigns them to their approtiate {@link Facet} in
	 * {@link SearchArguments}.
	 *
	 * @param <E>
	 *            the searched object type
	 * @param <S>
	 *            the build predefined filter arguments type
	 * @param arguments
	 *            - the search arguments
	 * @param queryResponse
	 *            - the Solr query response
	 */
	public <E extends Instance, S extends SearchArguments<E>> void extractFacetsFromResponse(S arguments,
			QueryResponse queryResponse) {
		Map<String, List<Facet>> solrFieldToFacet = arguments
				.getFacets()
					.values()
					.stream()
					.collect(Collectors.groupingBy(Facet::getSolrFieldName));

		extractFacets(solrFieldToFacet, queryResponse.getFacetFields());
		extractFacets(solrFieldToFacet, queryResponse.getFacetDates());
	}

	/**
	 * Iterates the provided facet fields and finds the corresponding facet in the given map based on its id.
	 *
	 * @param facets
	 *            - the map with facets
	 * @param fields
	 *            - the facet fields
	 */
	private static void extractFacets(Map<String, List<Facet>> facets, List<FacetField> fields) {
		if (CollectionUtils.isNotEmpty(fields)) {
			for (FacetField field : fields) {
				extractFacetValues(facets, field);
			}
		}
	}

	/**
	 * Adds the corresponding facet values to the facet map.
	 *
	 * @param facets
	 *            the map with the facets
	 * @param field
	 *            the facet fields
	 */
	private static void extractFacetValues(Map<String, List<Facet>> facets, FacetField field) {
		List<Facet> facetList = facets.get(field.getName());
		if (CollectionUtils.isNotEmpty(facetList)) {
			for (Facet facet : facetList) {
				if (facet != null) {
					facet.setValues(extractFacetValues(field));
				}
			}
		}
	}

	/**
	 * Iterates the values of the provided facet field and extracts any value that has positive count. If the value
	 * lacks a name, it is considered a missing value.
	 *
	 * @param field
	 *            - the facet field
	 * @return list of extracted facet values
	 */
	private static List<FacetValue> extractFacetValues(FacetField field) {
		// use linked list because there is a lot removing from the list
		List<FacetValue> values = new LinkedList<>();
		for (Count count : field.getValues()) {
			if (count.getCount() > 0) {
				FacetValue value = new FacetValue();
				value.setCount(count.getCount());
				if (count.getName() == null) {
					value.setId(FacetQueryParameters.NO_VALUE);
				} else {
					value.setId(count.getName());
				}
				values.add(value);
			}
		}
		return values;
	}

	/**
	 * Finds all facets from the search arguments that are dates and converts their values to UTC date format if the
	 * faceting is enabled.
	 * <p>
	 * <b>NOTE</b>: This is done because Solr does not return always dates in full ISO format - sometimes the
	 * milliseconds are missing.
	 * </p>
	 *
	 * @param <E>
	 *            the searched object type
	 * @param <S>
	 *            the build predefined filter arguments type
	 * @param arguments
	 *            the search arguments
	 */
	public <E extends Instance, S extends SearchArguments<E>> void formatDatesToUTC(S arguments) {
		Map<String, Facet> facets = arguments.getFacets();
		if (arguments.isFaceted() && CollectionUtils.isNotEmpty(facets)) {
			for (Facet facet : facets.values()) {
				if (isDateFacet(facet) && !CollectionUtils.isEmpty(facet.getValues())) {
					dateFacetValuesToUTC(facet);
				}
			}
		}
	}

	/**
	 * Iterates through the provided facet's values and converts them to full ISO format if they are not specific to
	 * {@link #FACET_DATE_OTHER} .
	 *
	 * @param facet
	 *            the provided facet
	 */
	private static void dateFacetValuesToUTC(Facet facet) {
		for (FacetValue facetValue : facet.getValues()) {
			if (!FACET_DATE_OTHER.contains(facetValue.getId())) {
				DateTime date = new DateTime(facetValue.getId(), DateTimeZone.UTC);
				facetValue.setId(date.toString(ISODateTimeFormat.dateTime()));
			}
		}
	}

	/**
	 * Removes invalid values from the facet's values by comparing them to the returned, filtered values in the
	 * {@link SolrDocumentList} by their URIs. <br/>
	 * <b>NOTE</b>: Invalid values are the ones that are found in the facet but not in the Solr document list. This can
	 * mean that the user has no permissions for them.
	 *
	 * @param solrDocumentList
	 *            the filtered values
	 * @param facet
	 *            the values
	 */
	// TODO: Reuse the mapping between uri and header?
	public void removeInvalidObjectFacetValues(SolrDocumentList solrDocumentList, Facet facet) {
		Iterator<FacetValue> iterator = facet.getValues().iterator();
		while (iterator.hasNext()) {
			FacetValue value = iterator.next();
			boolean found = false;

			for (SolrDocument solrDocument : solrDocumentList) {
				if (value.getId().equals(solrDocument.get(DefaultProperties.URI))) {
					found = true;
					break;
				}
			}

			if (!found) {
				iterator.remove();
			}
		}
	}

	/**
	 * Removes all unselected values in the provided facet, but only if there are any selected ones.
	 *
	 * @param facet
	 *            - the provided facet
	 */
	public static void removeUnselectedFacetValues(Facet facet) {
		if (CollectionUtils.isNotEmpty(facet.getSelectedValues())) {
			Iterator<FacetValue> iterator = facet.getValues().iterator();
			while (iterator.hasNext()) {
				FacetValue value = iterator.next();
				if (!facet.getSelectedValues().contains(value.getId())) {
					iterator.remove();
				}
			}
		}
	}

	/**
	 * Iterates the provided facet's values and assigns them labels from the given mapping between URI and label.
	 *
	 * @param labels
	 *            - the mapping between URI and label
	 * @param facet
	 *            - the provided facet
	 */
	public void assignFacetValuesLabels(Map<String, String> labels, Facet facet) {
		Iterator<FacetValue> iterator = facet.getValues().iterator();
		while (iterator.hasNext()) {
			FacetValue facetValue = iterator.next();
			// TODO: What if there is no label/header ?
			String label = labels.get(namespaceRegistryService.getShortUri(facetValue.getId()));
			facetValue.setText(label);
		}
	}

	/**
	 * Removes any {@link FacetQueryParameters#NO_VALUE} values from the provided list of {@link FacetValue}s.
	 *
	 * @param facetValues
	 *            - the provided list of facet values
	 */
	public static void removeNullValues(List<FacetValue> facetValues) {
		Iterator<FacetValue> iterator = facetValues.iterator();
		while (iterator.hasNext()) {
			FacetValue facetValue = iterator.next();
			if (FacetQueryParameters.NO_VALUE.equals(facetValue.getId())) {
				iterator.remove();
			}
		}
	}

	/**
	 * Iterates the provided collection of facets and assigns them labels depending on their properties such as code
	 * list number, Solr type etc. <br/>
	 * <b>NOTE</b>: If a value already has a text/label - it's skipped from further operations.
	 *
	 * @param facets
	 *            - the provided facets
	 */
	public void assignLabels(Collection<Facet> facets) {
		String noValueLabel = labelProvider.getValue(NO_VALUE_LABEL_KEY);

		for (Facet facet : facets) {
			if (CollectionUtils.isEmpty(facet.getValues())) {
				continue;
			}
			for (FacetValue value : facet.getValues()) {
				// If I use more than once continue, Sonar makes a problem...
				boolean continueLoop = false;
				if (value.getText() != null || isDateFacet(facet)) {
					continueLoop = true;
				} else if (FacetQueryParameters.NO_VALUE.equalsIgnoreCase(value.getId())) {
					value.setText(noValueLabel);
					continueLoop = true;
				}
				if (continueLoop) {
					continue;
				}

				String label = fetchLabel(facet, value);
				value.setText(label);
			}
		}
	}

	/**
	 * Finds the possible label for a {@link FacetValue} depending on the {@link Facet} type - if it's a code list, user
	 * or object facet. If the facet is none of these, the resulting label will be the same as
	 * {@link FacetValue#getId()}.
	 *
	 * @param facet
	 *            - the value's facet
	 * @param value
	 *            - the value to assign a label to
	 * @return the label
	 */
	private String fetchLabel(Facet facet, FacetValue value) {
		Map<String, List<String>> requestMap = CollectionUtils.createHashMap(2);
		SearchRequest request = new SearchRequest(requestMap);
		request.add(FieldValueRetrieverParameters.FIELD, DefaultProperties.TITLE);
		String label = value.getId();

		if (isCodeListFacet(facet)) {
			label = joinLabels(getLabelsForCodelists(facet.getCodelists(), value.getId()));
		} else if (isUserFacet(facet)) {
			// the UsernameByURIFieldValueRetriever supports only short
			// URIs, so a conversion is needed.
			String shortUri = namespaceRegistryService.getShortUri(value.getId());
			label = fieldValueRetrievalService.getLabel(FieldId.USERNAME_BY_URI, shortUri, request);
		} else if (isRdfTypeFacet(facet)) {
			label = fieldValueRetrievalService.getLabel(FieldId.OBJECT_TYPE, value.getId(), request);
		} else {
			LOGGER.trace("Unknown facet type for [{}]", facet.getId());
		}

		return label;
	}

	private Set<String> getLabelsForCodelists(Set<Integer> codelists, String valueId) {
		Map<String, List<String>> requestMap = CollectionUtils.createHashMap(2);
		SearchRequest request = new SearchRequest(requestMap);
		Set<String> labels = CollectionUtils.createLinkedHashSet(codelists.size());
		for (int codelist : codelists) {
			requestMap.put(FieldValueRetrieverParameters.CODE_LIST_ID, Arrays.asList(Integer.toString(codelist)));
			String label = fieldValueRetrievalService.getLabel(FieldId.CODE_LIST, valueId, request);

			// We don't want to add the value as a label, but if the fieldValueRetrievalService doesn't find a matching
			// label, it will return exactly that, so we must filter it.
			if (!label.equals(valueId)) {
				labels.add(label);
			}
		}
		// However if there are no found labels in the end we must display something so we will add the value.
		if (CollectionUtils.isEmpty(labels)) {
			labels.add(valueId);
		}
		return labels;
	}

	private static String joinLabels(Iterable<String> labels) {
		return StringUtils.join(labels, ", ");
	}

	/**
	 * Checks if the provided {@link Facet} is a code list facet.
	 *
	 * @param facet
	 *            - the provided facet
	 * @return true if it is or false otherwise
	 */
	public static boolean isCodeListFacet(Facet facet) {
		return CollectionUtils.isNotEmpty(facet.getCodelists());
	}

	/**
	 * Checks the range class of the provided {@link Facet} if it's a user facet.
	 *
	 * @param facet
	 *            - the provided facet
	 * @return true if the faces is for users or false otherwise
	 */
	public boolean isUserFacet(Facet facet) {
		String rangeClass = facet.getRangeClass();
		return USERS.contains(rangeClass);
	}

	/**
	 * Checks if the provided {@link Facet} is a RDF type facet.
	 *
	 * @param facet
	 *            - the provided facet
	 * @return true if the faces is an object facet or false otherwise
	 */
	public boolean isRdfTypeFacet(Facet facet) {
		return RDF_TYPE.equalsIgnoreCase(facet.getId());
	}

	/**
	 * Checks if the provided {@link Facet} is a date facet.
	 *
	 * @param facet
	 *            - the provided facet
	 * @return true if it is a date facet or false otherwise
	 */
	public boolean isDateFacet(Facet facet) {
		return TDATES_TYPE.equalsIgnoreCase(facet.getSolrType());
	}
}