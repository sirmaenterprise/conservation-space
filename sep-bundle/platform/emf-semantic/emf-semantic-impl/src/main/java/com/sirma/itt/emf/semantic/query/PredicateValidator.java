/**
 * 
 */
package com.sirma.itt.emf.semantic.query;

import java.util.List;

import com.sirma.itt.commons.utils.string.StringUtils;
import com.sirma.itt.emf.instance.model.CommonInstance;
import com.sirma.itt.emf.search.SearchService;
import com.sirma.itt.emf.search.model.SearchArguments;
import com.sirma.itt.semantic.search.SemanticQueries;

/**
 * @author kirq4e
 */
public class PredicateValidator {

	private SearchService searchService;

	/**
	 * Search service
	 * 
	 * @param searchService
	 *            search service
	 */
	public PredicateValidator(SearchService searchService) {
		this.searchService = searchService;

	}

	/**
	 * Retrieves the URI of the predicate from the semantic repository
	 * 
	 * @param label
	 *            The label of the predicate
	 * @return URI of the predicate
	 */
	public String getPredicateURI(String label) {

		SearchArguments<CommonInstance> searchArguments = searchService.getFilter(
				SemanticQueries.QUERY_PROPERTY_BY_NAME.getName(), CommonInstance.class, null);
		searchArguments.getArguments().put("labelValue", label);

		searchService.search(CommonInstance.class, searchArguments);
		List<CommonInstance> result = searchArguments.getResult();

		for (CommonInstance commonInstance : result) {
			if (StringUtils.isNotNullOrEmpty((String) commonInstance.getId())) {
				return (String) commonInstance.getId();
			}
		}

		return null;
	}

	/**
	 * Retrieves the URI of the class from the semantic repository
	 * 
	 * @param label
	 *            The label of the class
	 * @return URI of the class
	 */
	public String getClassURI(String label) {
		SearchArguments<CommonInstance> searchArguments = searchService.getFilter(
				SemanticQueries.QUERY_CLASS_BY_NAME.getName(), CommonInstance.class, null);
		searchArguments.getArguments().put("labelValue", "^" + label);

		searchService.search(CommonInstance.class, searchArguments);
		List<CommonInstance> result = searchArguments.getResult();

		for (CommonInstance commonInstance : result) {
			if (StringUtils.isNotNullOrEmpty((String) commonInstance.getId())) {
				return (String) commonInstance.getId();
			}
		}

		return null;
	}

}
