package com.sirma.itt.seip.rule.preconditions;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.doAnswer;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.assertTrue;

import java.io.Serializable;
import java.util.HashMap;

import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import com.sirma.itt.emf.rule.RuleContext;
import com.sirma.itt.seip.context.Context;
import com.sirma.itt.seip.domain.instance.EmfInstance;
import com.sirma.itt.seip.domain.instance.Instance;
import com.sirma.itt.seip.domain.search.SearchArguments;
import com.sirma.itt.seip.rule.model.RelationDirection;
import com.sirma.itt.seip.search.SearchService;
import com.sirma.itt.seip.testutil.EmfTest;

/**
 * @author BBonev
 */
@Test
public class RelationExistsPreconditionTest extends EmfTest {

	@Mock
	SearchService searchService;

	@InjectMocks
	RelationExistsPrecondition precondition;

	@BeforeMethod
	@Override
	public void beforeMethod() {
		precondition = new RelationExistsPrecondition();
		super.beforeMethod();
	}

	public void testInvalidConfiguration() {
		assertFalse(precondition.configure(null));

		Context<String, Object> context = new Context<>();
		assertFalse(precondition.configure(context));

		context.put(RelationExistsPrecondition.RELATION_ID, "test");
		assertFalse(precondition.configure(context));
	}

	@SuppressWarnings("unchecked")
	public void testRelationDoesNotExist() {
		configure("emf:references", true, null, null);

		EmfInstance instance = new EmfInstance();
		instance.setId("emf:instance");
		instance.setProperties(new HashMap<String, Serializable>());

		doAnswer(invocation -> {
			SearchArguments arguments = (SearchArguments) invocation.getArguments()[1];
			String stringQuery = arguments.getStringQuery();
			assertNotNull(stringQuery);
			arguments.setTotalItems(0);
			return null;
		}).when(searchService).searchAndLoad(eq(Instance.class), any(SearchArguments.class));

		assertFalse(precondition.checkPreconditions(RuleContext.create(instance, instance, null)));
	}

	@SuppressWarnings("unchecked")
	public void testSimpleRelation_Outgoing() {
		configure("emf:references", true, null, null);

		EmfInstance instance = new EmfInstance();
		instance.setId("emf:instance");
		instance.setProperties(new HashMap<String, Serializable>());

		doAnswer(invocation -> {
			SearchArguments arguments = (SearchArguments) invocation.getArguments()[1];
			String stringQuery = arguments.getStringQuery();
			assertNotNull(stringQuery);
			assertTrue(stringQuery.contains("emf:instance emf:references"));
			arguments.setTotalItems(1);
			return null;
		}).when(searchService).search(eq(Instance.class), any(SearchArguments.class));

		assertTrue(precondition.checkPreconditions(RuleContext.create(instance, instance, null)));
	}

	@SuppressWarnings("unchecked")
	public void testSimpleRelation_Ingoing() {
		configure("emf:references", true, RelationDirection.INGOING, null);

		EmfInstance instance = new EmfInstance();
		instance.setId("emf:instance");
		instance.setProperties(new HashMap<String, Serializable>());

		doAnswer(invocation -> {
			SearchArguments arguments = (SearchArguments) invocation.getArguments()[1];
			String stringQuery = arguments.getStringQuery();
			assertNotNull(stringQuery);
			assertTrue(stringQuery.contains("emf:references emf:instance"));
			arguments.setTotalItems(1);
			return null;
		}).when(searchService).search(eq(Instance.class), any(SearchArguments.class));

		assertTrue(precondition.checkPreconditions(RuleContext.create(instance, instance, null)));
	}

	@SuppressWarnings("unchecked")
	public void testSimpleRelation_Ingoing_typeFilter() {
		configure("emf:references", true, RelationDirection.INGOING, "emf:Document");

		EmfInstance instance = new EmfInstance();
		instance.setId("emf:instance");
		instance.setProperties(new HashMap<String, Serializable>());

		doAnswer(invocation -> {
			SearchArguments arguments = (SearchArguments) invocation.getArguments()[1];
			String stringQuery = arguments.getStringQuery();
			assertNotNull(stringQuery);
			assertTrue(stringQuery.contains("emf:references emf:instance"));
			assertTrue(stringQuery.contains("a emf:Document"));
			arguments.setTotalItems(1);
			return null;
		}).when(searchService).search(eq(Instance.class), any(SearchArguments.class));

		assertTrue(precondition.checkPreconditions(RuleContext.create(instance, instance, null)));
	}

	@SuppressWarnings("unchecked")
	public void testComplexRelation_Outgoing() {
		configure("emf:references", false, null, null);

		EmfInstance instance = new EmfInstance();
		instance.setId("emf:instance");
		instance.setProperties(new HashMap<String, Serializable>());

		doAnswer(invocation -> {
			SearchArguments arguments = (SearchArguments) invocation.getArguments()[1];
			String stringQuery = arguments.getStringQuery();
			assertNotNull(stringQuery);
			assertTrue(stringQuery.contains("emf:relationType emf:references"));
			assertTrue(stringQuery.contains("emf:source emf:instance"));
			arguments.setTotalItems(1);
			return null;
		}).when(searchService).search(eq(Instance.class), any(SearchArguments.class));

		assertTrue(precondition.checkPreconditions(RuleContext.create(instance, instance, null)));
	}

	@SuppressWarnings("unchecked")
	public void testComplexRelation_Ingoing() {
		configure("emf:references", false, RelationDirection.INGOING, null);

		EmfInstance instance = new EmfInstance();
		instance.setId("emf:instance");
		instance.setProperties(new HashMap<String, Serializable>());

		doAnswer(invocation -> {
			SearchArguments arguments = (SearchArguments) invocation.getArguments()[1];
			String stringQuery = arguments.getStringQuery();
			assertNotNull(stringQuery);
			assertTrue(stringQuery.contains("emf:relationType emf:references"));
			assertTrue(stringQuery.contains("emf:destination emf:instance"));
			arguments.setTotalItems(1);
			return null;
		}).when(searchService).search(eq(Instance.class), any(SearchArguments.class));

		assertTrue(precondition.checkPreconditions(RuleContext.create(instance, instance, null)));
	}

	@SuppressWarnings("unchecked")
	public void testComplexRelation_Ingoing_typeFilter_fullUri() {
		configure("emf:references", false, RelationDirection.INGOING,
				"http://ittruse.ittbg.com/ontology/enterpriseManagementFramework#Document");

		EmfInstance instance = new EmfInstance();
		instance.setId("emf:instance");
		instance.setProperties(new HashMap<String, Serializable>());

		doAnswer(invocation -> {
			SearchArguments arguments = (SearchArguments) invocation.getArguments()[1];
			String stringQuery = arguments.getStringQuery();
			assertNotNull(stringQuery);
			assertTrue(stringQuery.contains("emf:relationType emf:references"));
			assertTrue(stringQuery.contains("emf:destination emf:instance"));
			assertTrue(stringQuery
					.contains("a <http://ittruse.ittbg.com/ontology/enterpriseManagementFramework#Document>"));
			arguments.setTotalItems(1);
			return null;
		}).when(searchService).search(eq(Instance.class), any(SearchArguments.class));

		assertTrue(precondition.checkPreconditions(RuleContext.create(instance, instance, null)));
	}

	@SuppressWarnings("unchecked")
	public void testComplexRelation_Ingoing_typeFilter_instanceType() {
		configure("emf:references", false, RelationDirection.INGOING, "documentinstance");

		EmfInstance instance = new EmfInstance();
		instance.setId("emf:instance");
		instance.setProperties(new HashMap<String, Serializable>());

		doAnswer(invocation -> {
			SearchArguments arguments = (SearchArguments) invocation.getArguments()[1];
			String stringQuery = arguments.getStringQuery();
			assertNotNull(stringQuery);
			assertTrue(stringQuery.contains("emf:relationType emf:references"));
			assertTrue(stringQuery.contains("emf:destination emf:instance"));
			assertTrue(stringQuery.contains("emf:definitionId documentinstance"));
			arguments.setTotalItems(1);
			return null;
		}).when(searchService).search(eq(Instance.class), any(SearchArguments.class));

		assertTrue(precondition.checkPreconditions(RuleContext.create(instance, instance, null)));
	}

	private void configure(String relationId, boolean simpleOnly, RelationDirection direction, String otherType) {
		Context<String, Object> context = new Context<>();
		context.put(RelationExistsPrecondition.RELATION_ID, relationId);
		context.put(RelationExistsPrecondition.SIMPLE_ONLY, simpleOnly);
		if (direction != null) {
			context.put(RelationExistsPrecondition.DIRECTION, direction.toString());
		}
		context.put(RelationExistsPrecondition.OTHER_TYPE, otherType);
		assertTrue(precondition.configure(context));
	}

}
