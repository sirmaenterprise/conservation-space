package com.sirma.cmf.mock.service;

import javax.enterprise.inject.Alternative;

import com.sirma.itt.emf.domain.Context;
import com.sirma.itt.emf.instance.model.Instance;
import com.sirma.itt.emf.search.SearchService;
import com.sirma.itt.emf.search.model.SearchArguments;
import com.sirma.itt.emf.search.model.SearchFilter;
import com.sirma.itt.emf.search.model.SearchFilterConfig;

/**
 * The Class SearchServiceMock.
 */
@Alternative
public class SearchServiceMock implements SearchService {

	@Override
	public <E extends Instance, S extends SearchArguments<E>> void search(Class<?> target,
			S arguments) {

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
	public <S extends SearchArguments<?>> S buildSearchArguments(SearchFilter filter,
			Class<?> resultType, Context<String, Object> context) {
		// TODO Auto-generated method stub
		return null;
	}

}
