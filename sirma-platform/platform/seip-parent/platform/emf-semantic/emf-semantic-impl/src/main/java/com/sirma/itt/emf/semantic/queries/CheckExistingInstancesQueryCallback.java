package com.sirma.itt.emf.semantic.queries;

import static com.sirma.itt.emf.semantic.queries.SPARQLQueryHelper.buildFilters;

import java.io.Serializable;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;

import com.sirma.itt.seip.plugin.Extension;
import com.sirma.itt.seip.search.NamedQueries;
import com.sirma.itt.seip.search.NamedQueries.Params;
import com.sirma.itt.semantic.model.vocabulary.EMF;
import com.sirma.itt.semantic.queries.QueryBuilderCallback;

/**
 * Query call for building semantic query for checking if one or more instances exists
 *
 * @author BBonev
 */
@Extension(target = QueryBuilderCallback.PLUGIN_NAME, order = 12)
public class CheckExistingInstancesQueryCallback implements QueryBuilderCallback {

	private static final Set<String> PARAM_NAMES = Collections
			.unmodifiableSet(new HashSet<>(Arrays.asList(Params.URIS)));

	@Override
	public String singleValue(Serializable object, Map<String, Object> params, List<Function<String, String>> filters) {
		return String.format("{ select (%s as ?instance) ?instanceType where {%s emf:"
				+ EMF.INSTANCE_TYPE.getLocalName() + " ?instanceType . %s} }", object, object,
				buildFilters(object.toString(), filters));
	}

	@Override
	public String getStart(List<Function<String, String>> filters, Collection<String> projections) {
		return "SELECT DISTINCT ?instance ?instanceType WHERE {";
	}

	@Override
	public String getEnd() {
		return "}";
	}

	@Override
	public Set<String> paramNames() {
		return PARAM_NAMES;
	}

	@Override
	public String getName() {
		return NamedQueries.CHECK_EXISTING_INSTANCE;
	}

	@Override
	public String collectionParamName() {
		return Params.URIS;
	}
}