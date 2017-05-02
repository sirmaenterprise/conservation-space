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

import com.sirma.itt.seip.configuration.Options;
import com.sirma.itt.seip.plugin.Extension;
import com.sirma.itt.seip.search.NamedQueries;
import com.sirma.itt.seip.search.NamedQueries.Params;
import com.sirma.itt.seip.search.NamedQueries.Projections;
import com.sirma.itt.semantic.model.vocabulary.EMF;
import com.sirma.itt.semantic.queries.QueryBuilderCallback;

/**
 * Query builder extension for semantic query that allows loading one or more instances
 *
 * @author BBonev
 */
@Extension(target = QueryBuilderCallback.PLUGIN_NAME, order = 14)
public class SelectInstancesQueryCallback implements QueryBuilderCallback {

	private static final Set<String> PARAM_NAMES = Collections
			.unmodifiableSet(new HashSet<>(Arrays.asList(Params.URIS)));

	static final String URI = " ?" + Projections.URI;
	static final String PROPERTY_NAME = " ?" + Projections.PROPERTY_NAME;
	static final String PROPERTY_VALUE = " ?" + Projections.PROPERTY_VALUE;

	static final String IS_DETELED = "\t " + URI + " " + SPARQLQueryHelper.IS_NOT_DELETED + "\n";

	static final String SELECT_SINGLE = URI + " = %s ";

	/**
	 * <pre>
	 * SELECT DISTINCT ?uri ?propertyName ?propertyValue WHERE {
	 *		 ?uri ?propertyName ?propertyValue .
	 *		 ?uri emf:isDeleted "false"^^xsd:boolean .
	 *		 {
	 *			 ?propertyName a emf:DefinitionObjectProperty.
	 *			 ?propertyValue emf:isDeleted "false"^^xsd:boolean .
	 *		 } UNION {
	 *			 ?propertyName a emf:DefinitionDataProperty.
	 *		 }
	 *		 FILTER(
	 * </pre>
	 */
	static final String SELECT_MULTIPLE_START = "SELECT DISTINCT" + URI + PROPERTY_NAME + PROPERTY_VALUE + " WHERE {\n"
			+ "\t " + URI + PROPERTY_NAME + PROPERTY_VALUE + " .\n"
			+ "%s"// isDeleted is added here if enabled
			+ "\t {\n" + "\t\t" + PROPERTY_NAME + " a " + EMF.PREFIX + SPARQLQueryHelper.URI_SEPARATOR
			+ EMF.DEFINITION_OBJECT_PROPERTY.getLocalName() + ".\n" + "\t\t" + PROPERTY_VALUE
			+ SPARQLQueryHelper.IS_NOT_DELETED + "\n" + "\t } UNION {\n" + "\t\t" + PROPERTY_NAME + " a " + EMF.PREFIX
			+ SPARQLQueryHelper.URI_SEPARATOR + EMF.DEFINITION_DATA_PROPERTY.getLocalName() + ".\n" + "\t }\n FILTER(";

	static final String SELECT_MULTIPLE_END = ").}\n";

	@Override
	public String singleValue(Serializable object, Map<String, Object> params, List<Function<String, String>> filters) {
		String uri = object.toString();
		if (!uri.startsWith("<") && uri.contains("http")) {
			uri = "<" + uri + ">";
		}
		return String.format(SELECT_SINGLE, uri);
	}

	@Override
	public String getStart(List<Function<String, String>> filters, Collection<String> projections) {
		if (Options.ALLOW_LOADING_OF_DELETED_INSTANCES.isEnabled()) {
			return String.format(SELECT_MULTIPLE_START, "");
		}
		return String.format(SELECT_MULTIPLE_START, IS_DETELED);
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
		return NamedQueries.SELECT_BY_IDS;
	}

	@Override
	public String collectionParamName() {
		return Params.URIS;
	}

	@Override
	public String getSeparator() {
		return SPARQLQueryHelper.FILTER_OR;
	}
}