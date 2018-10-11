package com.sirma.itt.emf.semantic.queries;

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
import com.sirma.itt.semantic.model.vocabulary.EMF;
import com.sirma.itt.semantic.queries.QueryBuilderCallback;

/**
 * Query builder callback that builds a semantic query that is capable of finding one or more instances building a query
 * constructed only by passed filters and custom projections.
 *
 * @author BBonev
 */
@Extension(target = QueryBuilderCallback.PLUGIN_NAME, order = 20)
public class DynamicQueryInstanceSelectCallback implements QueryBuilderCallback {

	private static final Set<String> PARAM_NAMES = Collections.unmodifiableSet(new HashSet<>(Arrays.asList()));

	static final String INSTANCE = SPARQLQueryHelper.OBJECT_VARIABLE;
	static final String INSTANCE_TYPE = SPARQLQueryHelper.OBJECT_TYPE_VARIABLE;

	static final String SELECT = "SELECT DISTINCT " + INSTANCE + " " + INSTANCE_TYPE;

	static final String WHERE = " WHERE { " + INSTANCE + " " + EMF.PREFIX + ":" + EMF.INSTANCE_TYPE.getLocalName() + " "
			+ INSTANCE_TYPE + " . ";

	static final String SELECT_MULTIPLE_END = " }";

	@Override
	public String singleValue(Serializable object, Map<String, Object> params, List<Function<String, String>> filters) {
		return "";
	}

	@Override
	public String getStart(List<Function<String, String>> filters, Collection<String> projections) {
		StringBuilder builder = new StringBuilder(512);
		builder.append(SELECT);
		for (String projection : projections) {
			String prefix = projection.contains("?") ? " " : " ?";
			builder.append(prefix).append(projection);
		}
		builder.append(WHERE);
		builder.append(SPARQLQueryHelper.buildFilters(INSTANCE, filters));
		return builder.toString();
	}

	@Override
	public String getEnd() {
		return SELECT_MULTIPLE_END;
	}

	@Override
	public Set<String> paramNames() {
		return PARAM_NAMES;
	}

	@Override
	public String getName() {
		return NamedQueries.DYNAMIC_QUERY;
	}

	@Override
	public String collectionParamName() {
		return null;
	}

}
