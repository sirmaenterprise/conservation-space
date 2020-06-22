package com.sirma.itt.emf.mocks.search;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.sirma.itt.seip.util.ReflectionUtils;
import com.sirma.itt.emf.semantic.queries.CheckExistingInstancesQueryCallback;
import com.sirma.itt.emf.semantic.queries.LoadPropertiesQueryCallback;
import com.sirma.itt.emf.semantic.queries.QueryBuilderImpl;
import com.sirma.itt.emf.semantic.queries.SelectInstancesQueryCallback;
import com.sirma.itt.emf.semantic.queries.SparqlQueryFilterProvider;
import com.sirma.itt.semantic.queries.QueryBuilderCallback;

/**
 * Initializes the Query builder
 *
 * @author kirq4e
 */
public class QueryBuilderMock extends QueryBuilderImpl {

	/**
	 * Initializes the query builder
	 *
	 * @param context
	 *            needed parameters for initialization
	 */
	public QueryBuilderMock(Map<String, Object> context) {
		List<QueryBuilderCallback> builderCallbacks = new ArrayList<>();
		builderCallbacks.add(new CheckExistingInstancesQueryCallback());
		builderCallbacks.add(new LoadPropertiesQueryCallback());
		builderCallbacks.add(new SelectInstancesQueryCallback());
		ReflectionUtils.setFieldValue(this, "buildersCollection", builderCallbacks);
		ReflectionUtils.setFieldValue(this, "filterProvider", new SparqlQueryFilterProvider());
		initialize();
	}

}
