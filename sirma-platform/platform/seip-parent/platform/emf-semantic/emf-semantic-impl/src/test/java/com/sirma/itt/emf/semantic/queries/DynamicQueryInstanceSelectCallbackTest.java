package com.sirma.itt.emf.semantic.queries;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.Function;

import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.MockitoAnnotations;
import org.mockito.Spy;

import com.sirma.itt.seip.collections.CollectionUtils;
import com.sirma.itt.seip.search.NamedQueries;
import com.sirma.itt.semantic.model.vocabulary.EMF;
import com.sirma.itt.semantic.queries.QueryBuildRequest;
import com.sirma.itt.semantic.queries.QueryBuilderCallback;

/**
 * Test for {@link DynamicQueryInstanceSelectCallback}
 * 
 * @author BBonev
 */
public class DynamicQueryInstanceSelectCallbackTest {
	@InjectMocks
	private QueryBuilderImpl builder = new QueryBuilderImpl();
	@Spy
	private List<QueryBuilderCallback> buildersCollection = new ArrayList<>();
	@Spy
	private SparqlQueryFilterProvider filterProvider;

	@Before
	public void beforeMethod() {
		MockitoAnnotations.initMocks(this);
		buildersCollection.clear();
		buildersCollection.add(new DynamicQueryInstanceSelectCallback());
		builder.initialize();
	}

	@Test
	public void testBuilder() throws Exception {
		List<Function<String, String>> filters = new ArrayList<>();

		filters.add(builder.buildComposite(builder.buildValueFilter("emf:source", "source1"),
				builder.buildValueFilter("emf:destination", "destination1")));
		filters.add(builder.buildComposite(builder.buildValueFilter("emf:source", "source2"),
				builder.buildValueFilter("emf:destination", "destination2")));

		QueryBuildRequest buildRequest = new QueryBuildRequest(NamedQueries.DYNAMIC_QUERY)
				.addFilter(NamedQueries.Filters.IS_NOT_DELETED)
					.addFilter(NamedQueries.Filters.IS_NOT_REVISION)
					.addFilter(builder.buildUnion(CollectionUtils.toArray(filters, Function.class)))
					.addFilter(builder.buildUriesFilter("emf:type", Arrays.asList("emf:Case", EMF.TASK.toString())))
					.addFilter(builder.buildViariableFilter("emf:type", "type"))
					.addProjection("type");

		String query = builder.buildQuery(buildRequest);
		assertNotNull(query);
		assertTrue(query.contains("SELECT DISTINCT ?instance ?instanceType ?type"));
		assertTrue(query.contains("?instance emf:source \"source1\""));
		assertTrue(query.contains("?instance emf:destination \"destination1\"."));
		assertTrue(query.contains(
				"{ ?instance emf:type emf:Case. } UNION { ?instance emf:type <http://ittruse.ittbg.com/ontology/enterpriseManagementFramework#Task>. }"));
		assertTrue(query.contains("?instance emf:type ?type."));
	}
}
