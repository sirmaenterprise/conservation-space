package com.sirma.itt.emf.mocks.search;

import java.util.Map;

import com.sirma.itt.emf.semantic.search.SemanticSearchServiceFilterExtension;
import com.sirma.itt.seip.util.ReflectionUtils;
import com.sirma.itt.semantic.queries.QueryBuilder;

/**
 * Mock for SemanticSearchServiceFilterExtension - initializes the fields of the service so it can be used in the tests
 *
 * @author kirq4e
 */
public class SemanticSearchServiceFilterExtensionMock extends SemanticSearchServiceFilterExtension {

	/**
	 * Initializes the fields of the service
	 *
	 * @param context
	 *            parameters needed for initialization
	 */
	public SemanticSearchServiceFilterExtensionMock(Map<String, Object> context) {
		QueryBuilder builder = new QueryBuilderMock(context);
		ReflectionUtils.setFieldValue(this, "queryBuilder", builder);
	}

}
