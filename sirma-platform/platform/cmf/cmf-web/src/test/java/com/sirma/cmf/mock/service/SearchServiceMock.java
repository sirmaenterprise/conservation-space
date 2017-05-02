package com.sirma.cmf.mock.service;

import java.util.function.Function;

import javax.enterprise.inject.Alternative;

import com.sirma.itt.seip.context.Context;
import com.sirma.itt.seip.domain.instance.Instance;
import com.sirma.itt.seip.domain.search.SearchArguments;
import com.sirma.itt.seip.domain.search.SearchFilter;
import com.sirma.itt.seip.domain.search.SearchFilterConfig;
import com.sirma.itt.seip.domain.search.SearchRequest;
import com.sirma.itt.seip.search.SearchService;

/**
 * The Class SearchServiceMock.
 */
@Alternative
public class SearchServiceMock implements SearchService {

	@Override
	public <E extends Instance, S extends SearchArguments<E>> void search(Class<?> target, S arguments) {
		// nothing to do
	}

	@Override
	public <E extends Instance, S extends SearchArguments<E>> void searchAndLoad(Class<?> target, S arguments) {
		// nothing to do
	}

	@Override
	public <E, S extends SearchArguments<E>> S getFilter(String filterName, Class<E> resultType,
			Context<String, Object> context) {
		return null;
	}

	@Override
	public <E> SearchFilterConfig getFilterConfiguration(String placeHolder, Class<E> resultType) {
		return null;
	}

	@Override
	public <S extends SearchArguments<?>> S buildSearchArguments(SearchFilter filter, Class<?> resultType,
			Context<String, Object> context) {
		return null;
	}

	@Override
	public <E extends Instance, S extends SearchArguments<E>> S parseRequest(SearchRequest request) {
		return null;
	}

	@Override
	public Function<String, String> escapeForDialect(String dialect) {
		return (s) -> s;
	}

}
