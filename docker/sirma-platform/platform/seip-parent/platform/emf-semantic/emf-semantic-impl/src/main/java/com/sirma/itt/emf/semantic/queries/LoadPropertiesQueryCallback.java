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
import com.sirma.itt.seip.search.NamedQueries.Params;
import com.sirma.itt.seip.search.NamedQueries.Projections;
import com.sirma.itt.semantic.queries.QueryBuilderCallback;

/**
 * Query callback for building semantic properties for loading the properties for one or more instances
 *
 * @author BBonev
 */
@Extension(target = QueryBuilderCallback.PLUGIN_NAME, order = 10)
public class LoadPropertiesQueryCallback implements QueryBuilderCallback {
	private static final Set<String> PARAM_NAMES = Collections
			.unmodifiableSet(new HashSet<>(Arrays.asList(Params.URIS)));

	static final String SELECT_PROPERTIES = "\t { \n" + "\t\t SELECT DISTINCT (%s as ?" + Projections.URI + ")" + " ?"
			+ Projections.PROPERTY_NAME + " ?" + Projections.PROPERTY_VALUE + " { \n" + "\t\t\t %s ?" + Projections.PROPERTY_NAME
			+ " ?" + Projections.PROPERTY_VALUE + " . \n" + "\t\t } \n" + "\t }";

	@Override
	public String singleValue(Serializable object, Map<String, Object> params, List<Function<String, String>> filters) {
		return String.format(SELECT_PROPERTIES, object, object);
	}

	@Override
	public String getStart(List<Function<String, String>> filters, Collection<String> projections) {
		return "SELECT DISTINCT" + " ?" + Projections.URI + " ?" + Projections.PROPERTY_NAME + " ?" + Projections.PROPERTY_VALUE
				+ " WHERE { \n";
	}

	@Override
	public String getEnd() {
		return "\n}";
	}

	@Override
	public Set<String> paramNames() {
		return PARAM_NAMES;
	}

	@Override
	public String collectionParamName() {
		return Params.URIS;
	}

	@Override
	public String getName() {
		return NamedQueries.LOAD_PROPERTIES;
	}
}