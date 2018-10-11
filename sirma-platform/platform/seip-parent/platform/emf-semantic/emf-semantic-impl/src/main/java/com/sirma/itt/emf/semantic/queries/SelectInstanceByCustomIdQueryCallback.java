package com.sirma.itt.emf.semantic.queries;

import java.io.Serializable;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.function.Function;

import org.eclipse.rdf4j.model.Value;

import com.sirma.itt.emf.semantic.persistence.ValueConverter;
import com.sirma.itt.seip.plugin.Extension;
import com.sirma.itt.seip.search.NamedQueries;
import com.sirma.itt.seip.search.NamedQueries.Params;
import com.sirma.itt.seip.search.NamedQueries.Projections;
import com.sirma.itt.semantic.model.vocabulary.EMF;
import com.sirma.itt.semantic.queries.QueryBuilderCallback;

/**
 * Query builder callback that builds a semantic query that is capable of finding one or more instances by property name
 * and multiple values for the configured name. By default the implementation treats the values as literals unless
 * parameter is passed {@link Params#IS_URI} with value {@code true}.
 * <p>
 * The property name should be passed via parameter {@link Params#PROPERTY_ID}. The property values that need to be
 * matched must be passed via configuration property {@link Params#URIS}. If the passed values are not literals then the
 * configuration property {@link Params#IS_URI} with value {@code true} must be passed as well.
 *
 * @author BBonev
 */
@Extension(target = QueryBuilderCallback.PLUGIN_NAME, order = 30)
public class SelectInstanceByCustomIdQueryCallback implements QueryBuilderCallback {

	/*
	 * Note that if you change some of the configuration property names update the documentation of
	 * NamedQueries.SELECT_BY_CUSTOM_ID
	 */

	private static final Set<String> PARAM_NAMES = Collections
			.unmodifiableSet(new HashSet<>(Arrays.asList(Params.URIS, Params.PROPERTY_ID, Params.IS_URI)));

	static final String INSTANCE = SPARQLQueryHelper.OBJECT_VARIABLE;
	static final String INSTANCE_TYPE = SPARQLQueryHelper.OBJECT_TYPE_VARIABLE;
	static final String PROPERTY_VALUE = Projections.PROPERTY_VALUE;

	static final String SELECT_SINGLE = "\n\t { \n\t\t" + INSTANCE + " %s ?" + PROPERTY_VALUE + ".\n\t\t" + INSTANCE
			+ " %s %s." + "\n\t}";

	static final String SELECT_MULTIPLE_START = "SELECT DISTINCT " + INSTANCE + " " + INSTANCE_TYPE + " ?"
			+ PROPERTY_VALUE + " WHERE { \n\t" + INSTANCE + " emf:" + EMF.INSTANCE_TYPE.getLocalName() + " "
			+ INSTANCE_TYPE + " .";

	static final String SELECT_MULTIPLE_END = "\n}";

	@Override
	public String singleValue(Serializable object, Map<String, Object> params, List<Function<String, String>> filters) {
		String paramName = Objects.toString(params.get(Params.PROPERTY_ID), null);
		if (paramName == null || object == null) {
			// ignore request
			return null;
		}
		String value;
		if (isUri(params)) {
			value = object.toString();
			if (!value.startsWith("<") && value.contains("http")) {
				value = "<" + value + ">";
			}
		} else {
			Value literal = ValueConverter.createLiteral(object);
			value = Objects.toString(literal, null);
		}
		if (value == null) {
			return null;
		}
		return String.format(SELECT_SINGLE, paramName, paramName, value);
	}

	private static boolean isUri(Map<String, Object> params) {
		Object value = params.get(Params.IS_URI);
		if (value instanceof Boolean) {
			return ((Boolean) value).booleanValue();
		}
		return false;
	}

	@Override
	public String getStart(List<Function<String, String>> filters, Collection<String> projections) {
		StringBuilder builder = new StringBuilder(512);
		builder.append(SELECT_MULTIPLE_START);
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
		return NamedQueries.SELECT_BY_CUSTOM_ID;
	}

	@Override
	public String collectionParamName() {
		return Params.URIS;
	}

}
