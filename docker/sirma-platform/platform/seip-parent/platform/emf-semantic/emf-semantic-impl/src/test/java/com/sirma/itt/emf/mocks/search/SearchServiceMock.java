package com.sirma.itt.emf.mocks.search;

import java.util.Arrays;
import java.util.Map;

import com.sirma.itt.seip.util.ReflectionUtils;
import com.sirma.itt.emf.semantic.search.SemanticSearchEngine;
import com.sirma.itt.seip.context.Context;
import com.sirma.itt.seip.domain.instance.Instance;
import com.sirma.itt.seip.domain.search.SearchArguments;
import com.sirma.itt.seip.search.SearchService;
import com.sirma.itt.seip.search.SearchServiceFilterExtension;
import com.sirma.itt.seip.search.SearchServiceImpl;

/**
 * Search service mock - initializes the service for tests
 *
 * @author kirq4e
 */
public class SearchServiceMock extends SearchServiceImpl implements SearchService {

	private SearchServiceFilterExtension filter;
	private SemanticSearchEngine engine;

	/**
	 * Initializes the service for the tests
	 *
	 * @param context
	 *            neede for initialization
	 */
	public SearchServiceMock(Map<String, Object> context) {
		filter = new SemanticSearchServiceFilterExtensionMock(context);
		engine = new SemanticSearchEngineMock(context);

		ReflectionUtils.setFieldValue(this, "engines", Arrays.asList(engine));
		ReflectionUtils.setFieldValue(this, "searchConfiguration", new SearchConfigurationMock());
		engine.init();
	}

	@Override
	public <E, S extends SearchArguments<E>> S getFilter(String filterName, Class<E> resultType,
			Context<String, Object> context) {
		return filter.buildSearchArguments(filterName, context);
	}

	@Override
	public <E extends Instance, S extends SearchArguments<E>> void search(Class<?> target, S arguments) {
		engine.search(target, arguments);
	}


	@Override
	public <E extends Instance, S extends SearchArguments<E>> void searchAndLoad(Class<?> target, S arguments) {
		engine.search(target, arguments);
	}

}
