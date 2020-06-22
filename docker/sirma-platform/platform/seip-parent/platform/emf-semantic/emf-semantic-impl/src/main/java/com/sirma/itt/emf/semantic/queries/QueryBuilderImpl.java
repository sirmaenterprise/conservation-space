package com.sirma.itt.emf.semantic.queries;

import java.io.Serializable;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;

import javax.annotation.PostConstruct;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.apache.commons.lang3.StringUtils;
import com.sirma.itt.emf.semantic.persistence.ValueConverter;
import com.sirma.itt.seip.Pair;
import com.sirma.itt.seip.plugin.ExtensionPoint;
import com.sirma.itt.seip.search.NamedQueries;
import com.sirma.itt.semantic.queries.QueryBuildRequest;
import com.sirma.itt.semantic.queries.QueryBuilder;
import com.sirma.itt.semantic.queries.QueryBuilderCallback;

/**
 * SPARQL named query builder
 *
 * @author Valeri Tishev
 */
@ApplicationScoped
public class QueryBuilderImpl implements QueryBuilder {

	private static final Logger LOGGER = LoggerFactory.getLogger(QueryBuilderImpl.class);

	@Inject
	@ExtensionPoint(QueryBuilderCallback.PLUGIN_NAME)
	private Iterable<QueryBuilderCallback> buildersCollection;

	@Inject
	private SparqlQueryFilterProvider filterProvider;

	private Map<String, QueryBuilderCallback> builders = new HashMap<>();

	/**
	 * Initialize.
	 */
	@PostConstruct
	public void initialize() {
		for (QueryBuilderCallback builderCallback : buildersCollection) {
			builders.put(builderCallback.getName(), builderCallback);
		}
	}

	/**
	 * Builds predefined SPARQL query by given name
	 *
	 * @param <E>
	 *            the query parameters element type
	 * @param name
	 *            the query name
	 * @param params
	 *            the query parameters
	 * @return the the query or {@code null} in case no named query is defined for given name
	 */
	@Override
	public <E extends Pair<String, Object>> String buildQueryByName(String name, List<E> params) {
		return buildQueryByName(name, params, NamedQueries.Filters.IS_NOT_DELETED);
	}

	@Override
	@SuppressWarnings("unchecked")
	public String buildQuery(QueryBuildRequest buildRequest) {
		QueryBuilderCallback callback = builders.get(buildRequest.getQueryName());
		if (callback == null) {
			LOGGER.warn("Undefined SPARQL query for name [{}]", buildRequest.getQueryName());
			return null;
		}
		List<Function<String, String>> filters = buildRequest.getAllFilters(filterProvider::getFilterBuilder);
		Collection<String> projections = buildRequest.getProjections();

		Set<String> names = callback.paramNames();
		if (!buildRequest.hasParameters() && names.isEmpty()) {
			return buildQuery(callback, Collections.emptyList(), Collections.emptyMap(), filters, projections);
		}

		Map<String, Object> properties = buildRequest.getParameters(names);

		Object paramValue = properties.get(callback.collectionParamName());
		if (paramValue instanceof Collection<?>) {
			return buildQuery(callback, (Collection<Serializable>) paramValue, properties, filters, projections);
		} else if (paramValue instanceof Serializable) {
			return buildQuery(callback, Arrays.asList((Serializable) paramValue), properties, filters, projections);
		}
		return buildQuery(callback, Collections.emptyList(), properties, filters, projections);
	}

	private static String buildQuery(QueryBuilderCallback callback, Collection<Serializable> objectUris,
			Map<String, Object> properties, List<Function<String, String>> filters, Collection<String> projections) {
		StringBuilder builder = new StringBuilder();

		// if we have empty collection and we require collections parameter we cannot continue
		if (objectUris.isEmpty() && callback.collectionParamName() != null) {
			return null;
		}
		builder.append(callback.getStart(filters, projections));
		boolean isFirst = true;
		Iterator<Serializable> iterator = objectUris.iterator();
		while (iterator.hasNext()) {
			Serializable uri = iterator.next();
			String singleValue = callback.singleValue(uri, properties, filters);
			if (StringUtils.isNotBlank(singleValue)) {
				if (isFirst) {
					isFirst = false;
				} else {
					builder.append(callback.getSeparator());
				}
				builder.append(singleValue);
			}
		}
		builder.append(callback.getEnd());
		return builder.toString();
	}

	@Override
	public Function<String, String> buildValueFilter(String predicate, Object object) {
		Object converted;
		if (object instanceof Serializable) {
			converted = ValueConverter.createLiteral((Serializable) object).stringValue();
		} else {
			converted = object;
		}

		return QueryBuilder.super.buildValueFilter(predicate, converted);
	}

}
