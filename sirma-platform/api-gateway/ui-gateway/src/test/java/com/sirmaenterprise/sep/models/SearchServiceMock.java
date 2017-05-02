package com.sirmaenterprise.sep.models;

import java.util.List;
import java.util.function.Function;

import com.sirma.itt.seip.context.Context;
import com.sirma.itt.seip.domain.instance.Instance;
import com.sirma.itt.seip.domain.search.SearchArguments;
import com.sirma.itt.seip.domain.search.SearchFilter;
import com.sirma.itt.seip.domain.search.SearchFilterConfig;
import com.sirma.itt.seip.domain.search.SearchRequest;
import com.sirma.itt.seip.search.SearchService;

/**
 * Mock class for the search service.
 *
 * @author Nikolay Ch
 */
public class SearchServiceMock implements SearchService {
	private List<Instance> instanceFilter;

	/**
	 * Setter for the filter.
	 *
	 * @param instanceFilter
	 *            the list with the filter instances
	 */
	public void setFilter(List<Instance> instanceFilter) {
		this.instanceFilter = instanceFilter;
	}

	@Override
	public <E, S extends SearchArguments<E>> S getFilter(String filterName, Class<E> resultType,
			Context<String, Object> context) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public <E> SearchFilterConfig getFilterConfiguration(String placeHolder, Class<E> resultType) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public <S extends SearchArguments<?>> S buildSearchArguments(SearchFilter filter, Class<?> resultType,
			Context<String, Object> context) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public <E extends Instance, S extends SearchArguments<E>> void search(Class<?> target, S arguments) {
		arguments.setResult((List<E>) instanceFilter);
	}

	@Override
	public <E extends Instance, S extends SearchArguments<E>> void searchAndLoad(Class<?> target, S arguments) {
		// TODO Auto-generated method stub

	}

	@Override
	public <E extends Instance, S extends SearchArguments<E>> S parseRequest(SearchRequest request) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Function<String, String> escapeForDialect(String dialect) {
		// TODO Auto-generated method stub
		return null;
	}

}
