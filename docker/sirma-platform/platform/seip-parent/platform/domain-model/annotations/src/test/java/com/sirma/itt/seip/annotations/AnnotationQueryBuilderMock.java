package com.sirma.itt.seip.annotations;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.sirma.itt.emf.mocks.search.QueryBuilderMock;
import com.sirma.itt.seip.util.ReflectionUtils;
import com.sirma.itt.semantic.queries.QueryBuilderCallback;

/**
 * Initializes the collection of {@link QueryBuilderCallback} implementations needed for the annotation tests.
 *
 * @author Vilizar Tsonev
 */
public class AnnotationQueryBuilderMock extends QueryBuilderMock {

	/**
	 * Constructs the mock
	 *
	 * @param context
	 *            is the context
	 */
	public AnnotationQueryBuilderMock(Map<String, Object> context) {
		super(context);
		List<QueryBuilderCallback> builderCallbacks = new ArrayList<>();
		builderCallbacks.add(new AnnotationBatchLoadQueryBuilderCallback());
		ReflectionUtils.setFieldValue(this, "buildersCollection", builderCallbacks);
		initialize();
	}

}
