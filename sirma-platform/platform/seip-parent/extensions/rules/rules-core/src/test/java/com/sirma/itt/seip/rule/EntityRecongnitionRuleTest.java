package com.sirma.itt.seip.rule;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.Spy;
import org.testng.Assert;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import com.sirma.itt.emf.rule.RuleContext;
import com.sirma.itt.emf.rule.RuleMatcher;
import com.sirma.itt.emf.rule.RuleOperation;
import com.sirma.itt.emf.rule.RulePrecondition;
import com.sirma.itt.seip.Executable;
import com.sirma.itt.seip.concurrent.FutureCallback;
import com.sirma.itt.seip.concurrent.GenericAsyncTask;
import com.sirma.itt.seip.concurrent.TaskExecutor;
import com.sirma.itt.seip.context.Context;
import com.sirma.itt.seip.domain.instance.EmfInstance;
import com.sirma.itt.seip.domain.instance.Instance;
import com.sirma.itt.seip.domain.util.DependencyResolver;
import com.sirma.itt.seip.rule.model.EntityRecognitionConfigBuilder;
import com.sirma.itt.seip.rule.model.ParallelismMode;
import com.sirma.itt.seip.rule.model.RecognitionConfig;
import com.sirma.itt.seip.testutil.EmfTest;
import com.sirma.itt.seip.testutil.fakes.TransactionSupportFake;
import com.sirma.itt.seip.tx.TransactionSupport;

/**
 * The Class EntityRecongnitionRuleTest.
 *
 * @author BBonev
 */
@Test
public class EntityRecongnitionRuleTest extends EmfTest {

	/** The rule. */
	@InjectMocks
	EntityRecongnitionRule rule;

	/** The config builder. */
	@Mock
	EntityRecognitionConfigBuilder configBuilder;

	/** The transaction support. */
	@Spy
	TransactionSupport transactionSupport = new TransactionSupportFake();

	/** The event service. */
	@Spy
	TaskExecutor taskExecutor = new TaskExecutorMock();

	/** The config. */
	@Spy
	RecognitionConfig config = new RecognitionConfig();

	/** The data resolver. */
	@Mock
	DependencyResolver dataResolver;

	/** The rule matcher. */
	@Mock
	RuleMatcher ruleMatcher;

	/** The rule operation. */
	@Mock
	RuleOperation ruleOperation;

	@Mock
	RulePrecondition fastPrecondition;

	@Mock
	RulePrecondition slowPrecondition;

	/**
	 * Before method.
	 */
	@BeforeMethod
	@Override
	public void beforeMethod() {
		super.beforeMethod();

		config.setDataProviders(new LinkedList<DependencyResolver>());
		config.getDataProviders().add(dataResolver);
		config.setMatchers(new LinkedList<RuleMatcher>());
		config.getMatchers().add(ruleMatcher);
		config.setOperations(new LinkedList<RuleOperation>());
		config.getOperations().add(ruleOperation);

	}

	/**
	 * Tests method isApplicable scenario when super method call is true and not all precondition are true.
	 */
	@Test
	public void isApplicableWithPreconditionsFalseTest() {
		// Setup test
		RuleContext context = new RuleContext();
		Instance instance = Mockito.mock(Instance.class);
		context.put(RuleContext.PROCESSING_INSTANCE, instance);

		RulePrecondition preconditionOne = Mockito.mock(RulePrecondition.class);
		Mockito.when(preconditionOne.checkPreconditions(context)).thenReturn(false);

		RulePrecondition preconditionTwo = Mockito.mock(RulePrecondition.class);
		Mockito.when(preconditionTwo.checkPreconditions(context)).thenReturn(true);

		Collection<RulePrecondition> preconditions = new ArrayList<>();
		preconditions.add(preconditionOne);
		preconditions.add(preconditionTwo);

		Mockito.doReturn(preconditions).when(config).getFastPreconditions();

		// Execute tested method
		boolean result = rule.isApplicable(context);

		// Verification
		Assert.assertFalse(result);
	}

	/**
	 * Tests method isApplicable scenario when super method call is true and all precondition are true.
	 */
	@Test
	public void isApplicableWithPreconditionsTrueTest() {
		// Setup test
		RuleContext context = new RuleContext();
		Instance instance = Mockito.mock(Instance.class);
		context.put(RuleContext.PROCESSING_INSTANCE, instance);

		RulePrecondition preconditionOne = Mockito.mock(RulePrecondition.class);
		Mockito.when(preconditionOne.checkPreconditions(context)).thenReturn(true);

		RulePrecondition preconditionTwo = Mockito.mock(RulePrecondition.class);
		Mockito.when(preconditionTwo.checkPreconditions(context)).thenReturn(true);

		Collection<RulePrecondition> preconditions = new ArrayList<>();
		preconditions.add(preconditionOne);
		preconditions.add(preconditionTwo);

		Mockito.doReturn(preconditions).when(config).getFastPreconditions();

		// Execute tested method
		boolean result = rule.isApplicable(context);

		// Verification
		Assert.assertTrue(result);
	}

	/**
	 * Tests method isApplicable scenario when super method call is true.
	 */
	@Test
	public void isApplicableFalseSuperMethodTrueTest() {
		// Setup test
		Context<String, Object> context = new Context<>();
		Instance instance = Mockito.mock(Instance.class);
		context.put(RuleContext.PROCESSING_INSTANCE, instance);

		// Execute tested method
		boolean result = rule.isApplicable(context);

		// Verification
		Assert.assertTrue(result);
	}

	/**
	 * Tests method isApplicable scenario when super method call is false.
	 */
	@Test
	public void isApplicableFalseSuperMethodFalseTest() {
		// Setup test
		Context<String, Object> context = new Context<>();

		// Execute tested method
		boolean result = rule.isApplicable(context);

		// Verification
		Assert.assertFalse(result);
	}

	/**
	 * Tests method isApplicable scenario when configuration is null.
	 */
	@Test
	public void isApplicableTWithNullConfigurationTest() {
		// Setup test
		Context<String, Object> context = new Context<>();
		EntityRecongnitionRule entityRecongnitionRule = Mockito.mock(EntityRecongnitionRule.class);
		Mockito.doCallRealMethod().when(entityRecongnitionRule).isApplicable(context);

		// Execute tested method
		boolean result = entityRecongnitionRule.isApplicable(context);

		// Verification
		Assert.assertFalse(result);
	}

	/**
	 * Test execute.
	 */
	public void testExecute() {
		Instance instance = new EmfInstance();
		instance.setId("emf:instance");
		RuleContext context = RuleContext.create(instance, null, null);

		Instance matched = new EmfInstance();
		matched.setId("emf:matched");

		when(dataResolver.resolveDependencies(instance)).thenReturn(Collections.singletonList(matched));

		when(ruleMatcher.isApplicable(context)).thenReturn(true);
		when(ruleMatcher.match(eq(context), eq(matched), any(Context.class))).thenReturn(true);

		when(ruleOperation.isApplicable(context)).thenReturn(true);

		rule.execute(context);

		verify(dataResolver).isLazyLoadingSupported();
		verify(dataResolver).resolveDependencies(instance);

		verify(ruleMatcher).match(eq(context), eq(matched), any(Context.class));

		verify(ruleOperation).processingStarted(any(Context.class), any(Context.class));
		verify(ruleOperation).execute(eq(context), eq(matched), any(Context.class));
		verify(ruleOperation).processingEnded(any(Context.class), any(Context.class));
	}

	/**
	 * Test execute.
	 */
	public void testExecute_withBoundaries() {
		RuleContext context = new RuleContext();
		Instance instance = new EmfInstance();
		instance.setId("emf:instance");
		context.put(RuleContext.PROCESSING_INSTANCE, instance);

		Instance matched = new EmfInstance();
		matched.setId("emf:matched");

		when(dataResolver.resolveDependencies(instance)).thenReturn(Collections.singletonList(matched));

		when(ruleMatcher.isApplicable(context)).thenReturn(true);
		when(ruleMatcher.match(eq(context), eq(matched), any(Context.class))).thenReturn(true);

		when(ruleOperation.isApplicable(context)).thenReturn(true);

		rule.execute(context);

		verify(dataResolver).isLazyLoadingSupported();
		verify(dataResolver).resolveDependencies(instance);

		verify(ruleMatcher).match(eq(context), eq(matched), any(Context.class));

		verify(ruleOperation).processingStarted(any(Context.class), any(Context.class));
		verify(ruleOperation).execute(eq(context), eq(matched), any(Context.class));
		verify(ruleOperation).processingEnded(any(Context.class), any(Context.class));
	}

	/**
	 * Test execute in parallel_same data.
	 */
	public void testExecuteInParallel_sameData() {
		config.setParallelism(ParallelismMode.DATA_PROVIDER);

		RuleContext context = new RuleContext();
		Instance instance = new EmfInstance();
		instance.setId("emf:instance");
		context.put(RuleContext.PROCESSING_INSTANCE, instance);

		Instance matched = new EmfInstance();
		matched.setId("emf:matched");

		DependencyResolver secondProvider = mock(DependencyResolver.class);
		when(secondProvider.resolveDependencies(instance)).thenReturn(Collections.singletonList(matched));
		config.getDataProviders().add(secondProvider);

		when(dataResolver.resolveDependencies(instance)).thenReturn(Collections.singletonList(matched));

		when(ruleMatcher.isApplicable(context)).thenReturn(true);
		when(ruleMatcher.match(eq(context), eq(matched), any(Context.class))).thenReturn(true);

		when(ruleOperation.isApplicable(context)).thenReturn(true);

		rule.execute(context);

		verify(dataResolver).isLazyLoadingSupported();
		verify(dataResolver).resolveDependencies(instance);

		verify(secondProvider).isLazyLoadingSupported();
		verify(secondProvider).resolveDependencies(instance);

		verify(ruleMatcher).match(eq(context), eq(matched), any(Context.class));

		verify(ruleOperation).processingStarted(eq(context), any(Context.class));

		verify(ruleOperation).execute(eq(context), eq(matched), any(Context.class));

		verify(ruleOperation).processingEnded(eq(context), any(Context.class));
	}

	/**
	 * Test execute in parallel_different data.
	 */
	public void testExecuteInParallel_differentData() {
		config.setParallelism(ParallelismMode.DATA_PROVIDER);

		RuleContext context = new RuleContext();
		Instance instance = new EmfInstance();
		instance.setId("emf:instance");
		context.put(RuleContext.PROCESSING_INSTANCE, instance);

		Instance matched1 = new EmfInstance();
		matched1.setId("emf:matched1");

		Instance matched2 = new EmfInstance();
		matched2.setId("emf:matched2");

		DependencyResolver secondProvider = mock(DependencyResolver.class);
		when(secondProvider.resolveDependencies(instance)).thenReturn(Collections.singletonList(matched2));
		config.getDataProviders().add(secondProvider);

		when(dataResolver.resolveDependencies(instance)).thenReturn(Collections.singletonList(matched1));

		when(ruleMatcher.isApplicable(context)).thenReturn(true);
		when(ruleMatcher.match(eq(context), eq(matched1), any(Context.class))).thenReturn(true);
		when(ruleMatcher.match(eq(context), eq(matched2), any(Context.class))).thenReturn(true);

		when(ruleOperation.isApplicable(context)).thenReturn(true);

		rule.execute(context);

		verify(dataResolver).isLazyLoadingSupported();
		verify(dataResolver).resolveDependencies(instance);

		verify(secondProvider).isLazyLoadingSupported();
		verify(secondProvider).resolveDependencies(instance);

		// processing started should be called only once no matter the number of threads
		verify(ruleOperation).processingStarted(eq(context), any(Context.class));

		verify(ruleMatcher).match(eq(context), eq(matched1), any(Context.class));
		verify(ruleOperation).execute(eq(context), eq(matched1), any(Context.class));

		verify(ruleMatcher).match(eq(context), eq(matched2), any(Context.class));
		verify(ruleOperation).execute(eq(context), eq(matched2), any(Context.class));

		// processing ended should be called only once no matter the number of threads
		verify(ruleOperation).processingEnded(eq(context), any(Context.class));
	}

	public void testFastPreconditions() {

	}

	/**
	 * The Class EventServiceMock.
	 */
	static class TaskExecutorMock implements TaskExecutor {

		@Override
		public <T extends GenericAsyncTask> void execute(List<T> tasks) {
			// implement me!

		}

		@Override
		public <T extends GenericAsyncTask> void execute(List<T> tasks, String executeAs) {
			// implement me!

		}

		@Override
		public <T extends GenericAsyncTask> Future<?> submit(List<T> tasks, boolean transactionalContext) {
			return null;
		}

		@Override
		public <T extends GenericAsyncTask> Future<Object> submit(List<T> tasks, boolean transactionalContext,
				String executeAs) {
			return null;
		}

		@Override
		public <T> Future<T> executeAsync(Supplier<T> supplier) {
			T t = supplier.get();
			return new Future<T>() {

				@Override
				public boolean cancel(boolean mayInterruptIfRunning) {
					// implement me!
					return false;
				}

				@Override
				public boolean isCancelled() {
					// implement me!
					return false;
				}

				@Override
				public boolean isDone() {
					// implement me!
					return false;
				}

				@Override
				public T get() throws InterruptedException, ExecutionException {
					return t;
				}

				@Override
				public T get(long timeout, TimeUnit unit)
						throws InterruptedException, ExecutionException, TimeoutException {
					// implement me!
					return null;
				}
			};
		}

		@Override
		public <T> Future<T> executeAsyncInTx(Supplier<T> supplier) {
			return executeAsync(supplier);
		}

		@Override
		public <T> Future<T> executeAsync(Supplier<T> supplier, FutureCallback<T> futureCallback) {
			// implement me!
			return null;
		}

		@Override
		public <T> Future<T> executeAsyncInTx(Supplier<T> supplier, FutureCallback<T> futureCallback) {
			// implement me!
			return null;
		}

		@Override
		public Future<?> executeAsync(Executable executable) {
			return null;
		}

		@Override
		public Future<?> executeAsyncInTx(Executable executable) {
			return null;
		}

		@Override
		public <V> Future<V> submit(Supplier<V> supplier, Consumer<V> onSuccess, Consumer<Throwable> onFail) {
			return null;
		}

		@Override
		public <V, R> Future<R> submitMapped(Supplier<V> supplier, Function<V, R> onSuccess,
				Function<Throwable, R> onFail) {
			return null;
		}

		@Override
		public <V> Future<V> submit(Supplier<V> supplier) {
			return null;
		}

		@Override
		public Future<?> submit(Executable supplier) {
			supplier.execute();
			return null;
		}

		@Override
		public void waitForAll(Collection<? extends Future<?>> futures) {
			// nothing to do here
		}

	}
}
