package com.sirma.itt.emf.semantic.query;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import org.antlr.v4.runtime.ANTLRInputStream;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.tree.ParseTree;
import org.antlr.v4.runtime.tree.ParseTreeWalker;
import org.apache.log4j.Logger;

import com.sirma.itt.emf.search.SearchService;
import com.sirma.itt.emf.semantic.query.grammar.QueryToSparql;
import com.sirma.itt.emf.semantic.query.grammar.SirmaLexer;
import com.sirma.itt.emf.semantic.query.grammar.SirmaParser;
import com.sirma.itt.emf.semantic.query.grammar.SparqlQuery;

/**
 * Parses the query written in Sirma Query language and SPARQL query
 *
 * @author kirq4e
 */
@ApplicationScoped
public class QueryParser {

	@Inject
	private SearchService searchService;

	/** The logger. */
	@Inject
	protected Logger logger;

	/**
	 * Parses the query written in Sirma Query language and SPARQL query
	 *
	 * @param query
	 *            Sirma query
	 * @return SPARQL query
	 */
	public SparqlQuery parseQuery(String query) {
		ANTLRInputStream input;
		input = new ANTLRInputStream(query);

		SirmaLexer lexer = new SirmaLexer(input);
		CommonTokenStream tokens = new CommonTokenStream(lexer);
		SirmaParser parser = new SirmaParser(tokens);
		parser.setPredicateValidator(new PredicateValidator(searchService));
		ParseTree t = parser.query();

		ParseTreeWalker walker = new ParseTreeWalker();
		QueryToSparql listener = new QueryToSparql();
		walker.walk(listener, t);
		logger.debug(t.toStringTree(parser));
		SparqlQuery sparqlQuery = listener.getQuery();
		logger.debug(sparqlQuery.buildQuery());

		return sparqlQuery;

	}

}
