package com.sirma.itt.emf.semantic.archive;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyList;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.atMost;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotNull;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.function.Supplier;

import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.Spy;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import com.sirma.itt.seip.concurrent.TaskExecutor;
import com.sirma.itt.seip.context.Context;
import com.sirma.itt.seip.db.DefaultDbIdGenerator;
import com.sirma.itt.seip.domain.instance.Instance;
import com.sirma.itt.seip.domain.search.SearchArguments;
import com.sirma.itt.seip.domain.search.SearchInstance;
import com.sirma.itt.seip.expressions.ExpressionsManager;
import com.sirma.itt.seip.instance.ObjectInstance;
import com.sirma.itt.seip.instance.archive.TransactionIdHolder;
import com.sirma.itt.seip.instance.dao.InstanceService;
import com.sirma.itt.seip.instance.dao.ServiceRegistry;
import com.sirma.itt.seip.search.SearchService;
import com.sirma.itt.seip.testutil.mocks.ConfigurationPropertyMock;

/**
 * The Class LazyDeletedInstanceResolverTest.
 *
 * @author BBonev
 */
@Test
public class LazyDeletedInstanceResolverTest {

	/** The search service. */
	@Mock
	private SearchService searchService;

	/** The task executor. */
	@Mock
	private TaskExecutor taskExecutor;

	/** The id holder. */
	@Spy
	private TransactionIdHolder idHolder = new TransactionIdHolder(new DefaultDbIdGenerator());

	/** The instance service. */
	@Mock
	private InstanceService instanceService;

	/** The evaluator. */
	@Mock
	private ExpressionsManager evaluator;

	/** The resolver. */
	@InjectMocks
	private LazyDeletedInstanceResolver resolver;

	@Mock
	Supplier batchSizeSupplier;

	@Spy
	ConfigurationPropertyMock<Integer> batchSize = new ConfigurationPropertyMock<>();

	/** The arguments. */
	SearchArguments<Instance> arguments = new SearchArguments<>();

	/**
	 * Before method.
	 */
	@SuppressWarnings("unchecked")
	@BeforeMethod
	public void beforeMethod() {
		MockitoAnnotations.initMocks(this);
		batchSize.setSupplier(batchSizeSupplier);

		when(evaluator.evaluate(anyString(), eq(Integer.class))).thenReturn(5);
		when(instanceService.loadByDbId(anyList())).then(invocation -> {
			Collection<String> ids = (Collection<String>) invocation.getArguments()[0];
			return createInstancesFromIds(ids);
		});
		when(searchService.getFilter(anyString(), eq(SearchInstance.class), any(Context.class))).thenReturn(arguments);

		when(batchSizeSupplier.get()).thenReturn(50);
	}

	/**
	 * Creates the instances from ids.
	 *
	 * @param ids
	 *            the ids
	 * @return the list
	 */
	private List<Instance> createInstancesFromIds(Collection<String> ids) {
		List<Instance> result = new ArrayList<>(ids.size());
		for (Serializable id : ids) {
			Instance instance = new ObjectInstance();
			instance.setId(id);
			result.add(instance);
		}
		return result;
	}

	/**
	 * Test lazy loading_single query.
	 */
	public void testLazyLoading_singleQuery() {

		Instance instance = new ObjectInstance();
		instance.setId("emf:instance");

		arguments.setResult(createInstancesFromIds(
				Arrays.asList("emf:child1", "emf:child2", "emf:child3", "emf:child4", "emf:child5")));

		Iterator<Instance> iterator = resolver.resolveDependenciesLazily(instance);
		int count = 0;
		while (iterator.hasNext()) {
			count++;
			Instance inst = iterator.next();
			assertNotNull(inst);
			assertEquals(inst.getId(), "emf:child" + count);
		}
		assertEquals(count, 5);
	}

	/**
	 * Test lazy loading_multi query.
	 */
	@SuppressWarnings("unchecked")
	public void testLazyLoading_multiQuery() {
		when(batchSizeSupplier.get()).thenReturn(5);

		// the data will be 8 elements but the batch is only 5
		when(evaluator.evaluate(anyString(), eq(Integer.class))).thenReturn(8);

		Instance instance = new ObjectInstance();
		instance.setId("emf:instance");

		// the first 5 elements
		arguments.setResult(createInstancesFromIds(
				Arrays.asList("emf:child1", "emf:child2", "emf:child3", "emf:child4", "emf:child5")));

		Iterator<Instance> iterator = resolver.resolveDependenciesLazily(instance);
		int count = 0;
		while (iterator.hasNext()) {
			count++;
			Instance inst = iterator.next();
			assertNotNull(inst);
			assertEquals(inst.getId(), "emf:child" + count);
			if (count == 5) {
				// run the first batch
				break;
			}
		}
		assertEquals(count, 5);

		arguments.setResult(createInstancesFromIds(Arrays.asList("emf:child6", "emf:child7", "emf:child8")));
		while (iterator.hasNext()) {
			count++;
			Instance inst = iterator.next();
			assertNotNull(inst);
			assertEquals(inst.getId(), "emf:child" + count);
		}

		verify(searchService, atMost(2)).search(any(Class.class), any(SearchArguments.class));
	}

	/**
	 * Test full data loading_single query.
	 */
	@SuppressWarnings("unchecked")
	public void testFullDataLoading_singleQuery() {
		when(batchSizeSupplier.get()).thenReturn(4);

		Instance instance = new ObjectInstance();
		instance.setId("emf:instance");

		arguments.setResult(createInstancesFromIds(
				Arrays.asList("emf:child1", "emf:child2", "emf:child3", "emf:child4", "emf:child5")));

		Collection<Instance> collection = resolver.resolveDependencies(instance);
		int count = 0;
		Iterator<Instance> iterator = collection.iterator();
		while (iterator.hasNext()) {
			count++;
			Instance inst = iterator.next();
			assertNotNull(inst);
			assertEquals(inst.getId(), "emf:child" + count);
		}
		assertEquals(count, 5);

		verify(searchService).search(any(Class.class), any(SearchArguments.class));
	}
}
