package com.sirma.itt.seip.instance.archive;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.atMost;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.testng.Assert.assertFalse;

import java.io.Serializable;
import java.util.Arrays;
import java.util.HashMap;

import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import com.sirma.itt.seip.domain.instance.EmfInstance;
import com.sirma.itt.seip.domain.instance.Instance;
import com.sirma.itt.seip.domain.instance.InstanceReference;
import com.sirma.itt.seip.domain.security.ActionTypeConstants;
import com.sirma.itt.seip.domain.util.DependencyResolver;
import com.sirma.itt.seip.domain.util.DependencyResolvers;
import com.sirma.itt.seip.exception.EmfConfigurationException;
import com.sirma.itt.seip.instance.state.Operation;
import com.sirma.itt.seip.tasks.SchedulerContext;
import com.sirma.itt.seip.testutil.EmfTest;
import com.sirma.itt.seip.testutil.fakes.DbIdGeneratorFake;
import com.sirma.itt.seip.testutil.fakes.TransactionSupportFake;
import com.sirma.itt.seip.testutil.mocks.InstanceReferenceMock;
import com.sirma.itt.seip.tx.TransactionSupport;

/**
 * Test for {@link DeleteInstanceSchedulerExecutor}
 *
 * @author BBonev
 */
@Test
public class DeleteInstanceSchedulerExecutorTest extends EmfTest {

	/** The Constant DELETE. */
	private static final Operation DELETE = new Operation(ActionTypeConstants.DELETE);

	/** The transaction support. */
	@Spy
	private TransactionSupport transactionSupport = new TransactionSupportFake();

	/** The id holder. */
	@Spy
	private TransactionIdHolder idHolder = new TransactionIdHolder(new DbIdGeneratorFake());

	/** The archive service. */
	@Mock
	private ArchiveService archiveService;

	/** The resolvers. */
	@Mock
	private DependencyResolvers resolvers;

	/** The dependency resolver. */
	@Mock
	private DependencyResolver dependencyResolver;

	/** The executor. */
	@InjectMocks
	private DeleteInstanceSchedulerExecutor executor;

	/**
	 * Before method.
	 */
	@Override
	@BeforeMethod
	public void beforeMethod() {
		super.beforeMethod();

		when(resolvers.getResolver(any(Instance.class))).thenReturn(dependencyResolver);
	}

	/**
	 * Clean up.
	 */
	@AfterMethod
	public void cleanUp() {
		assertFalse(idHolder.isTransactionActive());
	}

	/**
	 * Test configuration checks_not valid.
	 *
	 * @throws Exception
	 *             the exception
	 */
	@Test(expectedExceptions = EmfConfigurationException.class)
	public void testConfigurationChecks_notValid() throws Exception {
		SchedulerContext context = new SchedulerContext();
		executor.beforeExecute(context);
	}

	/**
	 * Test configuration checks_valid.
	 *
	 * @throws Exception
	 *             the exception
	 */
	public void testConfigurationChecks_valid() throws Exception {
		Instance instance = new EmfInstance();
		SchedulerContext context = createContext(instance);
		executor.beforeExecute(context);
	}

	/**
	 * Test execute_no dependencies.
	 *
	 * @throws Exception
	 *             the exception
	 */
	public void testExecute_noDependencies() throws Exception {
		Instance instance = new EmfInstance();
		SchedulerContext context = createContext(instance);
		executor.execute(context);

		verify(archiveService, atMost(1)).delete(instance, DELETE, true);
	}

	/**
	 * Test execute_lazydependencies.
	 *
	 * @throws Exception
	 *             the exception
	 */
	public void testExecute_lazydependencies() throws Exception {
		Instance instance = new EmfInstance();
		SchedulerContext context = createContext(instance);

		when(dependencyResolver.isLazyLoadingSupported()).thenReturn(true);

		Instance child1 = new EmfInstance();
		child1.setId("emf:child1");
		Instance child2 = new EmfInstance();
		child2.setId("emf:child2");
		when(dependencyResolver.resolveDependenciesLazily(instance))
				.thenReturn(Arrays.asList(child1, child2).iterator());

		executor.execute(context);

		verify(archiveService).delete(eq(child1), eq(DELETE), eq(true));
		verify(archiveService).delete(eq(child2), eq(DELETE), eq(true));
		verify(archiveService).delete(eq(instance), eq(DELETE), eq(true));
		verify(archiveService, atMost(3)).delete(any(Instance.class), eq(DELETE), eq(true));
	}

	/**
	 * Test execute_non lazy dependencies.
	 *
	 * @throws Exception
	 *             the exception
	 */
	public void testExecute_nonLazyDependencies() throws Exception {
		Instance instance = new EmfInstance();
		SchedulerContext context = createContext(instance);

		when(dependencyResolver.isLazyLoadingSupported()).thenReturn(false);

		Instance child1 = new EmfInstance();
		child1.setId("emf:child1");
		Instance child2 = new EmfInstance();
		child2.setId("emf:child2");
		when(dependencyResolver.resolveDependencies(instance)).thenReturn(Arrays.asList(child1, child2));

		executor.execute(context);

		verify(archiveService).delete(eq(child1), eq(DELETE), eq(true));
		verify(archiveService).delete(eq(child2), eq(DELETE), eq(true));
		verify(archiveService).delete(eq(instance), eq(DELETE), eq(true));
		verify(archiveService, atMost(3)).delete(any(Instance.class), eq(DELETE), eq(true));
	}

	/**
	 * Test execute_lazydependencies_limit batch size.
	 *
	 * @throws Exception
	 *             the exception
	 */
	public void testExecute_lazydependencies_limitBatchSize() throws Exception {

		Instance instance = new EmfInstance();
		SchedulerContext context = createContext(instance);

		when(dependencyResolver.isLazyLoadingSupported()).thenReturn(true);
		// ensure multiple entry to the child executor
		when(dependencyResolver.currentBatchSize()).thenReturn(1);

		Instance child1 = new EmfInstance();
		child1.setId("emf:child1");
		Instance child2 = new EmfInstance();
		child2.setId("emf:child2");
		when(dependencyResolver.resolveDependenciesLazily(instance))
				.thenReturn(Arrays.asList(child1, child2).iterator());

		executor.execute(context);

		verify(archiveService).delete(eq(child1), eq(DELETE), eq(true));
		verify(archiveService).delete(eq(child2), eq(DELETE), eq(true));
		verify(archiveService).delete(eq(instance), eq(DELETE), eq(true));
		verify(archiveService, atMost(3)).delete(any(Instance.class), eq(DELETE), eq(true));
	}

	/**
	 * Creates the context.
	 *
	 * @param instance
	 *            the instance
	 * @return the scheduler context
	 */
	private SchedulerContext createContext(Instance instance) {
		SchedulerContext context = new SchedulerContext();
		instance.setId("emf:instanceId");
		instance.setProperties(new HashMap<String, Serializable>());
		InstanceReference reference = InstanceReferenceMock.createGeneric(instance);
		context.put(DeleteInstanceSchedulerExecutor.INSTANCE_REFERENCE, reference);
		context.put(DeleteInstanceSchedulerExecutor.IS_PERMANENT, false);
		context.put(DeleteInstanceSchedulerExecutor.TRANSACTION_ID, idHolder.createTransactionId());
		context.put(DeleteInstanceSchedulerExecutor.OPERATION, ActionTypeConstants.DELETE);
		return context;
	}
}
