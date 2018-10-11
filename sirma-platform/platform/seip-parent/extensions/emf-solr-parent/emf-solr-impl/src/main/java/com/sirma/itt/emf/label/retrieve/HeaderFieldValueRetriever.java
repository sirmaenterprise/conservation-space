package com.sirma.itt.emf.label.retrieve;

import static com.sirma.itt.seip.domain.instance.DefaultProperties.HEADER_BREADCRUMB;
import static com.sirma.itt.seip.domain.instance.DefaultProperties.URI;

import java.io.Serializable;
import java.lang.invoke.MethodHandles;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.inject.Inject;
import javax.inject.Singleton;

import org.apache.solr.client.solrj.SolrQuery;
import org.apache.solr.client.solrj.response.QueryResponse;
import org.apache.solr.common.SolrDocument;
import org.apache.solr.common.params.CommonParams;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sirma.itt.emf.solr.constants.SolrQueryConstants;
import com.sirma.itt.emf.solr.exception.SolrClientException;
import com.sirma.itt.emf.solr.services.SolrConnector;
import com.sirma.itt.seip.Pair;
import com.sirma.itt.seip.collections.CollectionUtils;
import com.sirma.itt.seip.concurrent.FragmentedWork;
import com.sirma.itt.seip.domain.instance.DefaultProperties;
import com.sirma.itt.seip.domain.instance.Instance;
import com.sirma.itt.seip.domain.search.SearchRequest;
import com.sirma.itt.seip.instance.HeadersService;
import com.sirma.itt.seip.instance.InstanceTypeResolver;
import com.sirma.itt.seip.instance.dao.InstanceService;
import com.sirma.itt.seip.plugin.Extension;
import com.sirma.itt.semantic.NamespaceRegistryService;

/**
 * Field value retriever that fetches instance's {@link DefaultProperties#HEADER_BREADCRUMB} property. The headers data
 * is fetched and the headers are build dynamically.
 *
 * @author BBonev
 */
@Singleton
@Extension(target = FieldValueRetriever.TARGET_NAME, order = 50)
public class HeaderFieldValueRetriever extends ObjectFieldValueRetriever {
	private static final Logger LOGGER = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

	private static final String LIMIT = "limit";
	private static final String OFFSET = "offset";

	/** maximum number of URIs to be send to SOLR with a single request */
	private static final int RECORDS_PER_SOLR_REQUEST = 1000;

	private static final Collection<String> DEFAULT_FIELD = Arrays.asList(URI, "instanceType");

	/** The set of supported objects that are returned by the method {@link #getSupportedObjects()}. */
	private static final List<String> SUPPORTED_FIELDS = Arrays.asList(FieldId.HEADER);

	@Inject
	private SolrConnector solrConnector;

	@Inject
	private HeadersService headersService;

	@Inject
	private InstanceTypeResolver instanceTypeResolver;

	@Inject
	private InstanceService instanceService;

	@Inject
	private NamespaceRegistryService namespaceRegistryService;

	@Override
	public RetrieveResponse getValues(String filter, SearchRequest additionalParameters, Integer offset,
			Integer limit) {
		SearchRequest params = additionalParameters;
		// pass the additional parameters for offset and limit
		if (params == null && (offset != null || limit != null)) {
			params = new SearchRequest(new HashMap<>());
			params.add(OFFSET, offset == null ? null : offset.toString());
			params.add(LIMIT, limit == null ? null : limit.toString());
		}
		List<Pair<String, String>> result = loadHeaders(doSolrSearch(filter, params).stream())
				.entrySet()
					.stream()
					.map(entry -> new Pair<>(entry.getKey(), entry.getValue()))
					.collect(Collectors.toList());
		return new RetrieveResponse(Long.valueOf(result.size()), result);
	}

	/**
	 * Perform a solr search based on the additional parameters.
	 *
	 * @param additionalParameters
	 *            the additional parameters
	 * @return the solr documents that match the search
	 */
	Collection<SolrDocument> doSolrSearch(String filter, SearchRequest additionalParameters) {
		SolrQuery query = buildSolrQuery(filter, additionalParameters);
		return doSolrSearchInternal(query);
	}

	/**
	 * Load the headers for each {@link SolrDocument} in the stream.
	 *
	 * @param solrDocumentStream
	 *            the stream of solr documents
	 * @return uri to header mapping
	 */
	private Map<String, String> loadHeaders(Stream<SolrDocument> solrDocumentStream) {
		List<String> uris = solrDocumentStream
				.map(this::toInstanceId)
					.filter(Objects::nonNull)
					.collect(Collectors.toList());
		return getLabels(CollectionUtils.toArray(uris, String.class));
	}

	@Override
	public String getLabel(String value, SearchRequest additionalParameters) {
		return getLabels(new String[] { value }, additionalParameters).get(value);
	}

	@Override
	public Map<String, String> getLabels(String[] values, SearchRequest additionalParameters) {
		// fetch in batches with max size of RECORDS_PER_SOLR_REQUEST because of a Solr restriction
		return FragmentedWork
				.doWorkWithResult(Arrays.asList(values), RECORDS_PER_SOLR_REQUEST, this::loadInstances)
					.stream()
					.peek(this::generateHeader)
					.filter(i -> i.isPropertyPresent(HEADER_BREADCRUMB))
					.collect(Collectors.toMap(i -> String.valueOf(i.getId()), i -> i.getString(HEADER_BREADCRUMB)));
	}

	/**
	 * Retrieve the instances for the given ids using a {@link InstanceTypeResolver}. If there are deleted instances
	 * they are retrieved using {@link InstanceService}.
	 *
	 * @param uris
	 *            the uris of the objects
	 * @return the solr documents that match the search
	 */
	Collection<Instance> loadInstances(Collection<String> uris) {
		Collection<Instance> instances = new LinkedList<>(instanceTypeResolver.resolveInstances(uris));
		Set<String> deletedInstancesIds = getDeletedInstancesIds(uris, instances);
		deletedInstancesIds
				.forEach(deletedInstanceId -> instanceService.loadDeleted(deletedInstanceId).ifPresent(instances::add));
		return instances;
	}

	private Set<String> getDeletedInstancesIds(Collection<String> uris, Collection<Instance> resolveInstances) {
		Set<Serializable> loadedInstancesIds = resolveInstances
				.stream()
					.map(Instance::getId)
					.collect(Collectors.toSet());

		Set<String> convertedIds = uris
				.stream()
					.map(uri -> namespaceRegistryService.getShortUri(uri))
					.collect(Collectors.toSet());
		convertedIds.removeAll(loadedInstancesIds);
		return convertedIds;
	}

	private Collection<SolrDocument> doSolrSearchInternal(SolrQuery query) {
		try {
			QueryResponse queryResponse = solrConnector.queryWithPost(query);
			if (queryResponse != null) {
				return queryResponse.getResults();
			}
		} catch (SolrClientException e) {
			LOGGER.error(e.getMessage(), e);
		}
		return Collections.emptyList();
	}

	String toInstanceId(SolrDocument solrDoc) {
		// this will be the full uri from solr, but the instance type resolver works with full uris just fine
		return getSolrFieldValue(solrDoc, DefaultProperties.URI);
	}

	void generateHeader(Instance instance) {
		instance.addIfNotNull(HEADER_BREADCRUMB, headersService.generateInstanceHeader(instance, HEADER_BREADCRUMB));
	}

	/**
	 * Gets the solr field single value.
	 *
	 * @param document
	 *            the document
	 * @param fieldName
	 *            the field name
	 * @return the solr field value
	 */
	private static String getSolrFieldValue(SolrDocument document, String fieldName) {
		Object fieldValue = document.getFieldValue(fieldName);
		if (fieldValue instanceof List<?>) {
			List<?> values = (List<?>) fieldValue;
			if (!values.isEmpty()) {
				return String.valueOf(values.get(0));
			}
		} else if (fieldValue instanceof String) {
			return (String) fieldValue;
		}
		return null;
	}

	private SolrQuery buildSolrQuery(String filter, SearchRequest additionalParameters) {
		SolrQuery parameters = new SolrQuery();
		parameters.setParam(CommonParams.Q, SolrQueryConstants.QUERY_DEFAULT_ALL);
		parameters.setParam(CommonParams.FL, DEFAULT_FIELD.stream().collect(Collectors.joining(",")));
		parameters.setParam(CommonParams.START, String.valueOf(0));
		parameters.setParam(CommonParams.ROWS, String.valueOf(RECORDS_PER_SOLR_REQUEST));

		if (additionalParameters != null) {
			parameters.setParam(CommonParams.START, String.valueOf(additionalParameters.getFirst(OFFSET)));
			parameters.setParam(CommonParams.ROWS, String.valueOf(additionalParameters.getFirst(LIMIT)));
		}

		parameters.set(CommonParams.FQ, createFilterQuery(filter, additionalParameters).toString());
		parameters.setTimeAllowed(Integer.valueOf(2000));
		return parameters;
	}

	@Override
	public List<String> getSupportedObjects() {
		return SUPPORTED_FIELDS;
	}

}
