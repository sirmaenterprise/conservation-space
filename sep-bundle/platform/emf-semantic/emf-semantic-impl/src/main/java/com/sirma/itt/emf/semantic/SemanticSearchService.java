package com.sirma.itt.emf.semantic;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import org.openrdf.OpenRDFException;
import org.openrdf.model.Statement;
import org.openrdf.model.Value;
import org.openrdf.query.BindingSet;
import org.openrdf.query.GraphQuery;
import org.openrdf.query.GraphQueryResult;
import org.openrdf.query.QueryEvaluationException;
import org.openrdf.query.QueryInterruptedException;
import org.openrdf.query.QueryLanguage;
import org.openrdf.query.TupleQuery;
import org.openrdf.query.TupleQueryResult;
import org.openrdf.repository.RepositoryConnection;
import org.openrdf.repository.RepositoryException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sirma.itt.emf.exceptions.EmfRuntimeException;
import com.sirma.itt.emf.util.CollectionUtils;
import com.sirma.itt.semantic.NamespaceRegistryService;

/**
 * Holds the implementation of searches
 *
 * @author Kiril Penev.
 */
@ApplicationScoped
public class SemanticSearchService {

	private static final Logger LOGGER = LoggerFactory.getLogger(SemanticSearchService.class);

	/** The connection. The connection will be forced to get new on every call */
	@Inject
	private javax.enterprise.inject.Instance<RepositoryConnection> connection;

	@Inject
	private NamespaceRegistryService namespaceRegistryService;

	/**
	 * Evaluate Tuple query - returns a map of property and value
	 *
	 * @param queryString
	 *            The SPARQL Query as String
	 * @param bindings
	 *            Query variables values to be replaced in the Query
	 * @param includeInferred
	 *            Flag if the query should include inferred statements
	 * @param queryTimeout
	 *            the qeury timeout to set if greater that zero
	 * @return Map of properties and their values
	 */
	public List<Map<String, Value>> executeTupleQuery(String queryString,
			Map<String, Value> bindings, boolean includeInferred, int queryTimeout) {

		String query = queryString;
		RepositoryConnection repositoryConnection = null;
		try {
			// append all namespaces that are used in the repository
			if (!queryString.startsWith("PREFIX")) {
				query = namespaceRegistryService.getNamespaces() + "\n" + query;
			}

			repositoryConnection = connection.get();
			TupleQuery tupleQuery = repositoryConnection.prepareTupleQuery(QueryLanguage.SPARQL,
					query);

			tupleQuery.setIncludeInferred(includeInferred);
			// set the query timeout if a needed
			if (queryTimeout > 0) {
				tupleQuery.setMaxQueryTime(queryTimeout);
			}

			// set query parameters
			for (Entry<String, Value> binding : bindings.entrySet()) {
				tupleQuery.setBinding(binding.getKey(), binding.getValue());
			}

			// evaluate query
			TupleQueryResult result = tupleQuery.evaluate();

			List<Map<String, Value>> resultList = new LinkedList<>();
			// handle result
			while (result.hasNext()) {
				BindingSet tuple = result.next();
				Set<String> bindingNames = tuple.getBindingNames();
				Map<String, Value> resultMap = CollectionUtils.createHashMap(bindingNames.size());
				for (String name : bindingNames) {
					Value value = tuple.getValue(name);
					if (value != null) {
						resultMap.put(name, value);
					}
				}
				resultList.add(resultMap);
			}

			result.close();
			return resultList;
		} catch (QueryInterruptedException e) {
			LOGGER.warn(
					"Semantic query execution exceeded the allowed execution time of {} s. The error is: {}",
					queryTimeout, e.getMessage());
			return Collections.emptyList();
		} catch (QueryEvaluationException e) {
			LOGGER.warn("Error while executing query: {}", e.getMessage(), e);
			return Collections.emptyList();
		} catch (OpenRDFException e) {
			throw new EmfRuntimeException(e);
		} finally {
			if (repositoryConnection != null) {
				try {
					repositoryConnection.close();
				} catch (RepositoryException e) {
					LOGGER.error("Failed to close connection", e);
				}
			}
		}
	}

	/**
	 * Execute Graph query
	 *
	 * @param queryString
	 *            The SPARQL Graph query as String
	 * @param bindings
	 *            Query variables values to be replaced in the Query
	 */
	public void executeGraphQuery(String queryString, Map<String, Value> bindings) {
		RepositoryConnection repositoryConnection = null;
		try {

			// append all namespaces that are used in the repository
			String queryWithNS = namespaceRegistryService.getNamespaces() + "\n" + queryString;

			repositoryConnection = connection.get();
			GraphQuery graphQuery = repositoryConnection.prepareGraphQuery(QueryLanguage.SPARQL,
					queryWithNS);

			// set query parameters
			for (Entry<String, Value> binding : bindings.entrySet()) {
				graphQuery.setBinding(binding.getKey(), binding.getValue());
			}

			// evaluate query
			GraphQueryResult result = graphQuery.evaluate();

			// handle result
			while (result.hasNext()) {
				Statement statement = result.next();
				String uri = statement.getSubject().stringValue();
			}
			result.close();
		} catch (OpenRDFException e) {
			throw new EmfRuntimeException(e);
		} finally {
			if (repositoryConnection != null) {
				try {
					repositoryConnection.close();
				} catch (RepositoryException e) {
					LOGGER.error("Failed to close connection", e);
				}
			}
		}
	}

}
