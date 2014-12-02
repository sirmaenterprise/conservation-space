package com.sirma.itt.cmf.search;

import javax.ws.rs.core.MultivaluedMap;

import com.sirma.itt.emf.instance.model.Instance;
import com.sirma.itt.emf.plugin.Plugin;
import com.sirma.itt.emf.search.model.SearchArguments;
import com.sirma.itt.emf.util.Documentation;

/**
 * The SearchPreprocessorEngine holds methods that are used to prepare queries for various searches
 * as basic, advanced, etc.
 *
 * @author bbanchev
 */
@Documentation("Extensions for search engine preprocessing of arguments. Specific logic for parsing and preparing queries.")
public interface SearchPreprocessorEngine extends Plugin {

	/** The name. */
	String NAME = "SearchPreprocessorEngine";

	/**
	 * Prepare basic query based on the arguments map, the same, as provided for
	 * {@link #isApplicable(MultivaluedMap)}
	 *
	 * @param queryArguments
	 *            the query arguments to handle
	 * @param searchArguments
	 *            the arguments to update
	 * @throws Exception
	 *             the exception on any error
	 */
	public void prepareBasicQuery(MultivaluedMap<String, String> queryArguments,
			SearchArguments<Instance> searchArguments) throws Exception;

	/**
	 * Checks if is applicable for the arguments - can handle such arguments.
	 *
	 * @param queryParams
	 *            the query params
	 * @return true, if is applicable
	 */
	public boolean isApplicable(MultivaluedMap<String, String> queryParams);
}
