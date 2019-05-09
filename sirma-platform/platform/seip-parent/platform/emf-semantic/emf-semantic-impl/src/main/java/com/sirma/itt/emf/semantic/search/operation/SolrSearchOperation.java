package com.sirma.itt.emf.semantic.search.operation;

import javax.inject.Inject;

import com.sirma.itt.emf.semantic.queries.SPARQLQueryHelper;
import com.sirma.itt.seip.domain.search.SearchOperation;
import com.sirma.itt.semantic.configuration.SemanticConfiguration;

/**
 * Builds SPARQL query with embedded solr one.
 *
 * @author Boyan Tonchev.
 */
public abstract class SolrSearchOperation implements SearchOperation {

	@Inject
	private SemanticConfiguration semanticConfigurations;

	/**
	 * Builds SPARQL query and add <code>solrSearchQuery</code> to it.
	 *
	 * @param semanticQueryBuilder - semantic query builder.
	 * @param solrSearchQuery      - the solr query.
	 */
	protected void buildOperation(StringBuilder semanticQueryBuilder, String solrSearchQuery) {
		String ftsIndexName = semanticConfigurations.getFtsIndexName().get();
		semanticQueryBuilder.append(SPARQLQueryHelper.LINE_SEPARATOR);
		semanticQueryBuilder.append("?search a ").append(ftsIndexName).append(" ; ");
		semanticQueryBuilder.append(SPARQLQueryHelper.LINE_SEPARATOR);
		semanticQueryBuilder.append(SPARQLQueryHelper.SOLR_QUERY)
				.append(" '''")
				.append(solrSearchQuery)
				.append("''' ; ");
		semanticQueryBuilder.append(SPARQLQueryHelper.LINE_SEPARATOR);
		SemanticSearchOperationUtils.appendTriple(semanticQueryBuilder, "", SPARQLQueryHelper.SOLR_ENTITIES,
												  SPARQLQueryHelper.OBJECT_VARIABLE);
		semanticQueryBuilder.append(SPARQLQueryHelper.LINE_SEPARATOR);
	}
}
