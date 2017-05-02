package com.sirma.itt.seip.domain.search;

import java.util.List;
import java.util.Map;

import com.sirma.itt.seip.collections.CollectionUtils;
import com.sirma.itt.seip.domain.search.tree.Condition;
import com.sirma.itt.seip.rest.Request;

/**
 * Object that represents a search request used to build proper search arguments instance with valid query.
 *
 * @author BBonev
 */
public class SearchRequest extends Request {
	private static final long serialVersionUID = 2436874474865316119L;
	private String dialect = SearchDialects.SPARQL;
	private Condition searchTree;

	/**
	 * Instantiates a new search request.
	 */
	public SearchRequest() {
		// default constructor
	}

	/**
	 * Instantiates a new search request.
	 *
	 * @param request
	 *            the request
	 */
	public SearchRequest(Map<String, List<String>> request) {
		super(request);
	}

	/**
	 * Adds value to the current request object.
	 *
	 * @param key
	 *            the key
	 * @param value
	 *            the value
	 */
	public void add(String key, String value) {
		CollectionUtils.addValueToMap(getRequest(), key, value);
	}

	/**
	 * Gets the dialect.
	 *
	 * @return the dialect
	 */
	public String getDialect() {
		return dialect;
	}

	/**
	 * Sets the dialect.
	 *
	 * @param dialect            the dialect to set
	 */
	public void setDialect(String dialect) {
		this.dialect = dialect;
	}

	/**
	 * Gets the search tree.
	 *
	 * @return the search tree
	 */
	public Condition getSearchTree() {
		return searchTree;
	}

	/**
	 * Sets the search tree.
	 *
	 * @param searchTree the new search tree
	 */
	public void setSearchTree(Condition searchTree) {
		this.searchTree = searchTree;
	}
}
