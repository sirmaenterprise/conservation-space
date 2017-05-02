package com.sirma.itt.seip.annotations;

import java.io.Serializable;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;

import com.sirma.itt.emf.semantic.queries.SPARQLQueryHelper;
import com.sirma.itt.seip.plugin.Extension;
import com.sirma.itt.seip.search.NamedQueries;
import com.sirma.itt.seip.search.NamedQueries.Params;
import com.sirma.itt.semantic.queries.QueryBuilderCallback;

/**
 * {@link QueryBuilderCallback} implementation for batch loading of annotations by annotation id.
 *
 * @author BBonev
 */
@Extension(target = QueryBuilderCallback.PLUGIN_NAME, order = 102)
public class AnnotationBatchLoadQueryBuilderCallback implements QueryBuilderCallback {

	private static final String BATCH_QUERY_START = "SELECT ?instance ?instanceType ?hasBody ?content ?createdBy ?createdOn ?modifiedBy ?modifiedOn ?status ?hasTarget ?replyTo WHERE {\n"+
			"	 ?instance a oa:Annotation ;\n" +
			"	     emf:instanceType ?instanceType ;\n" +
			"	     emf:modifiedBy ?modifiedBy ;\n" +
			"	     emf:modifiedOn ?modifiedOn ;\n" +
			"	     emf:createdBy ?createdBy ;\n" +
			"	     emf:createdOn ?createdOn ;\n" +
			"	     emf:content ?content ;\n" +
			"	     oa:hasBody ?hasBody ;\n" +
			"	     emf:isDeleted \"false\"^^xsd:boolean;\n" +
			"	     emf:status ?status;\n" +
			"	     oa:hasTarget ?hasTarget.\n" +
			"	     optional { ?instance emf:replyTo ?replyTo. }\n" +
			"FILTER (";
			private static final String BATCH_QUERY_EMD = ")\n }";

	private static final Set<String> PARAM_NAMES = Collections
			.unmodifiableSet(new HashSet<>(Arrays.asList(Params.URIS)));

	private static final String SINGLE_TEMPLATE = " ?instance = %s ";

	@Override
	public String getName() {
		return NamedQueries.LOAD_ANNOTATIONS;
	}

	@Override
	public String getStart(List<Function<String, String>> filters, Collection<String> projections) {
		return BATCH_QUERY_START;
	}

	@Override
	public String singleValue(Serializable object, Map<String, Object> params, List<Function<String, String>> filters) {
		return String.format(SINGLE_TEMPLATE, object);
	}

	@Override
	public String getEnd() {
		return BATCH_QUERY_EMD;
	}

	@Override
	public String collectionParamName() {
		return Params.URIS;
	}

	@Override
	public Set<String> paramNames() {
		return PARAM_NAMES;
	}
	
	@Override
	public String getSeparator() {
		return SPARQLQueryHelper.FILTER_OR;
	}

}
