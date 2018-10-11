package com.sirma.itt.seip.rule;

import java.io.Serializable;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

import javax.inject.Inject;
import javax.inject.Named;

import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sirma.itt.emf.rule.BaseDynamicInstanceRule;
import com.sirma.itt.emf.rule.DynamicInstanceRule;
import com.sirma.itt.emf.rule.RuleContext;
import com.sirma.itt.emf.rule.RuleMatcher;
import com.sirma.itt.emf.rule.RuleOperation;
import com.sirma.itt.emf.rule.RulePrecondition;
import com.sirma.itt.emf.rule.RuleState;
import com.sirma.itt.seip.concurrent.TaskExecutor;
import com.sirma.itt.seip.configuration.Options;
import com.sirma.itt.seip.context.Context;
import com.sirma.itt.seip.domain.instance.Instance;
import com.sirma.itt.seip.domain.util.DependencyResolver;
import com.sirma.itt.seip.json.JsonUtil;
import com.sirma.itt.seip.rule.model.EntityRecognitionConfigBuilder;
import com.sirma.itt.seip.rule.model.RecognitionConfig;
import com.sirma.itt.seip.time.TimeTracker;
import com.sirma.itt.seip.tx.TransactionSupport;

/**
 * Rule that recognizes an entity in the current processed object. The object that is recognized is via some query.
 * <p>
 * The implementation is eligible for parallel processing on 2 levels. The first level is data provider level : each
 * data provider could be processed in parallel to the other data providers. The second level is the parallel processing
 * the data from each data provider.
 * <p>
 * The rule configuration should have the following format:
 *
 * <pre>
 * <code>{
 * "preconditions" : [{
 * 	"name" : "propertyChanged",
 * 	"ruleConfig" : {
 * 		"propertyMapping" : [{
 *  			"from" : "name"
 * 		}]
 * 	}
 * }],
 * "dataProviders" : [{
 * 	"name" : "queryProvider",
 * 	"ruleConfig" : {
 * 		"query" : "exampleEntityRecognitionRule/query",
 * 		"propertyMapping" : [{
 * 			"from" : "name",
 * 			"to" : "referenceName"
 * 		}]
 * 	}
 * }],
 * "matchers" : [{
 * 	"name" : "propertyMatcher",
 * 	"asyncSupport" : true,
 * 	"objectTypes" : ["document", "object"],
 * 	"onOperations" : ["upload", "uploadNewVersion"],
 * 	"onDefinitions" : [],
 * 	"ruleConfig" : {
 * 		"checkForProperties" : [ "name" ],
 * 		"searchInProperties": [ "title" ],
 * 		"ignoreCase" : true
 * 	}
 * 	},
 * 	{
 * 	"name" : "contentMatcher",
 * 	"asyncSupport" : true,
 * 	"objectTypes" : ["document"],
 * 	"onOperations" : ["uploadNewVersion"],
 * 	"ruleConfig" : {
 * 		"checkForProperties" : [ "name" ],
 * 		"ignoreCase" : true
 * 	}
 * }],
 * "operations" : [{
 * 	"name" : "copyProperties",
 * 	"asyncSupport" : true,
 * 	"objectTypes" : ["document"],
 * 	"onOperations" : ["upload"],
 * 	"ruleConfig" : {
 * 		"onDuplicate" : {
 * 			"operation" : "concatenate|skip|override",
 * 			"separator" : ","
 * 		},
 * 		"propertyMapping" : [{
 * 			"from" : "name",
 * 			"to" : "referenceName",
 * 			"onDuplicate" : {
 * 				"operation" : "concatenate|skip|override",
 * 				"separator" : ","
 * 			}
 * 		]
 * 	}
 * 	},
 * 	{
 * 	"name" : "createRelation",
 * 	"asyncSupport" : true,
 * 	"ruleConfig" : {
 * 		"relationId" : "emf:references",
 * 		"inverseRelation" : "emf:references",
 * 		"simpleOnly" : true,
 * 		"properties" : { }
 * 	}
 * }],
 * "ruleConfig" : {
 * 		"parallelism" : "NONE|DATA_PROVIDER"
 * 	}
 * }</code>
 * </pre>
 *
 * @author BBonev
 */
@Named(EntityRecongnitionRule.NAME)
public class EntityRecongnitionRule extends BaseDynamicInstanceRule implements DynamicInstanceRule {

	public static final String NAME = "entityRecongnition";
	/** The Constant LOGGER. */
	private static final Logger LOGGER = LoggerFactory.getLogger(EntityRecongnitionRule.class);

	private static final String IS_PROCESSING_STARTED_CALLED = "isProcessingStartedCalled";
	private static final String NOT_FINISHED_PROCESSING = "notFinishedProcessing";

	/** The configuration. */
	private RecognitionConfig configuration;

	@Inject
	private EntityRecognitionConfigBuilder configBuilder;
	@Inject
	private TransactionSupport transactionSupport;
	@Inject
	private TaskExecutor taskExecutor;

	private String ruleName;

	@Override
	public String getPrimaryOperation() {
		return NAME;
	}

	@Override
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public boolean configure(Context<String, Object> context) {
		super.configure(context);
		Map map = context.getIfSameType(CONFIG, Map.class);
		if (map != null) {
			configuration = configBuilder.parse(map);
			if (configuration == null) {
				LOGGER.warn("No property configurations found in config/properties. The rule {} will be disabled!",
						context.get(DEFINED_IN));
			}
		}
		ruleName = context.getIfSameType(DEFINED_IN, String.class);
		return configuration != null;
	}

	/**
	 * Checks if is applicable.
	 *
	 * @return true, if is applicable
	 */
	@Override
	public boolean isApplicable(Context<String, Object> context) {
		if (configuration == null || !super.isApplicable(context)) {
			return false;
		}
		for (RulePrecondition precondition : configuration.getFastPreconditions()) {
			if (context instanceof RuleContext && !precondition.checkPreconditions((RuleContext) context)) {
				return false;
			}
		}
		return true;
	}

	@Override
	public String getRuleInstanceName() {
		return ruleName;
	}

	@Override
	public void execute(final RuleContext processingContext) {
		processingContext.setState(new EntityRecognitionState());

		if (checkForFailedPreconditions(processingContext)) {
			return;
		}

		final Instance currentInstance = getProcessedInstance(processingContext);
		// if enabled parallel processing each thread should have a separate matcher context
		boolean shouldExecuteInParallel = configuration.shouldExecuteDataProvidersInParallel();
		final Set<Serializable> processedInstances = Collections.synchronizedSet(new HashSet<Serializable>(128));
		// if data provider returns the current instance we will skip it
		processedInstances.add(currentInstance.getId());

		initializeParallelProcessing(processingContext);
		// TODO: update implementation not to use events but thread pool and to store the returned
		// futures and countDownLatch
		for (final DependencyResolver resolver : configuration.getDataProviders()) {
			updateState(processingContext, ExecutionState.EXECUTE_QUERY, resolver.toString());
			if (shouldExecuteInParallel) {
				taskExecutor.submit(() -> {
					processDataProvider(processingContext, currentInstance, resolver, processedInstances);
				});
			} else {
				processDataProvider(processingContext, currentInstance, resolver, processedInstances);
			}
		}
	}

	private boolean checkForFailedPreconditions(final RuleContext processingContext) {
		for (RulePrecondition precondition : configuration.getSlowPreconditions()) {
			updateState(processingContext, ExecutionState.PRE_CONDITION, precondition);
			if (!precondition.checkPreconditions(processingContext)) {
				LOGGER.warn("Entity rule recongnition skipped due to failed precondition: " + precondition.getName());
				return true;
			}
		}
		return false;
	}

	/**
	 * Initialize runtime configuration properties that control the synchronization between multiple parallel data
	 * providers.
	 *
	 * @param processingContext
	 *            the processing context
	 */
	private void initializeParallelProcessing(Context<String, Object> processingContext) {
		// option if the begin instance is executed or not
		processingContext.put(IS_PROCESSING_STARTED_CALLED, new AtomicBoolean(false));
		// the number of active threads so that can decrement after execution
		processingContext.put(NOT_FINISHED_PROCESSING, new AtomicInteger(configuration.getDataProviders().size()));
	}

	/**
	 * Process data provider.
	 *
	 * @param processingContext
	 *            the context
	 * @param currentInstance
	 *            the current instance
	 * @param resolver
	 *            the resolver
	 * @param processedInstances
	 *            collection to keep track of the processed instances, not to process an instance twice.
	 */
	private void processDataProvider(RuleContext processingContext, Instance currentInstance,
			DependencyResolver resolver, Set<Serializable> processedInstances) {
		Iterator<Instance> iterator;
		try {
			// disabled thumbnails because we does not need them when executing rules.
			Options.DISABLE_POST_INSTANCE_LOAD_DECORATION.enable();
			if (resolver.isLazyLoadingSupported()) {
				iterator = resolver.resolveDependenciesLazily(currentInstance);
				LOGGER.debug("Running recognition using lazy data provider. Potential matches unknown. Batch size {}",
						resolver.currentBatchSize());
			} else {
				Collection<Instance> dependencies = resolver.resolveDependencies(currentInstance);
				LOGGER.debug("Running reconginition on {} potential matches", dependencies.size());
				iterator = dependencies.iterator();
			}
		} catch (Exception e) {
			notifyForProcessingEnded(processingContext, new Context<String, Object>());
			LOGGER.warn("Failed to load data from data provider {} due to error", resolver, e);
			return;
		} finally {
			Options.DISABLE_POST_INSTANCE_LOAD_DECORATION.disable();
		}
		TimeTracker tracker = TimeTracker.createAndStart();
		processData(iterator, processedInstances, processingContext, new Context<String, Object>());
		LOGGER.debug("Completed data processing using {} data provider and took {} s", resolver,
				tracker.stopInSeconds());
	}

	/**
	 * Process data passed via the given iterator. The method could realize parallel processing if the input instance if
	 * needed.
	 *
	 * @param iterator
	 *            the iterator
	 * @param processedInstances
	 *            the processed instances
	 * @param processingContext
	 *            the processing context
	 * @param context
	 *            the context
	 */
	private void processData(Iterator<Instance> iterator, Set<Serializable> processedInstances,
			RuleContext processingContext, Context<String, Object> context) {
		int skipped = 0;
		AtomicInteger processed = new AtomicInteger(0);
		int totalProcessed = 0;
		try {
			while (iterator.hasNext()) {
				// if rule execution is interrupted end
				if (Thread.currentThread().isInterrupted()) {
					return;
				}
				Instance instance = iterator.next();
				totalProcessed++;
				// if the instance has been processed by other data provider we will skip it
				if (processedInstances.contains(instance.getId())) {
					skipped++;
					continue;
				}
				// when matcher is activated we run the operations and continue to the next instance
				if (runMatchersOnInstance(processingContext, context, instance, totalProcessed)) {
					// if notification for processing fails we should terminate all data provider
					// processing failed we will
					notifyForProcessingStarted(processingContext, context);
					onMatch(processingContext, instance, context, processed);
				}
				updateState(processingContext, ExecutionState.GET_NEXT, "").onInstance(null);
				processedInstances.add(instance.getId());
			}
		} finally {
			// notify for end no matter what
			notifyForProcessingEnded(processingContext, context);
			LOGGER.debug("Processed {} out of {} and skipped {} instances", processed, totalProcessed, skipped);
		}
	}

	/**
	 * Notify for processing started. Calls all operations to notify them that a processing using the current context is
	 * starting.
	 *
	 * @param processingContext
	 *            the processing context
	 * @param context
	 *            the context
	 */
	private void notifyForProcessingStarted(final RuleContext processingContext,
			final Context<String, Object> context) {
		if (shouldNotifyForProcessingStarted(processingContext)) {
			transactionSupport.invokeInNewTx(() -> {
				for (RuleOperation rule : configuration.getOperations()) {
					if (rule.isApplicable(processingContext)) {
						updateState(processingContext, ExecutionState.BEFORE_OPERATION, rule);
						rule.processingStarted(processingContext, context);
					}
				}
				return null;
			});
		}
	}

	/**
	 * Should notify for processing started. Atomically checks if the processing was not called on other thread and if
	 * not marks that is called. The method is intended for multi-threaded environment
	 *
	 * @param processingContext
	 *            the processing context
	 * @return true, if successful
	 */
	private static boolean shouldNotifyForProcessingStarted(Context<String, Object> processingContext) {
		AtomicBoolean atomicBoolean = processingContext.getIfSameType(IS_PROCESSING_STARTED_CALLED,
				AtomicBoolean.class);
		return atomicBoolean.compareAndSet(false, true);
	}

	/**
	 * Notify for processing ended. Calls all operations to notify them that a processing using the current context is
	 * ending.
	 *
	 * @param processingContext
	 *            the processing context
	 * @param context
	 *            the context
	 */
	private void notifyForProcessingEnded(final RuleContext processingContext, final Context<String, Object> context) {
		if (shouldCallProcessingEnd(processingContext)) {
			transactionSupport.invokeInNewTx(() -> {
				for (RuleOperation rule : configuration.getOperations()) {
					if (rule.isApplicable(processingContext)) {
						updateState(processingContext, ExecutionState.AFTER_OPERATION, rule);
						rule.processingEnded(processingContext, context);
					}
				}
				return null;
			});
		}
	}

	/**
	 * Should call processing end. Atomically notifies that the current processing is ending. If it's the last operation
	 * to finish then returns <code>true</code> so that the current thread should run the end operations.The method is
	 * intended for multi-threaded environment.
	 *
	 * @param processingContext
	 *            the processing context
	 * @return true, if should call processing end
	 */
	private static boolean shouldCallProcessingEnd(Context<String, Object> processingContext) {
		AtomicBoolean atomicBoolean = processingContext.getIfSameType(IS_PROCESSING_STARTED_CALLED,
				AtomicBoolean.class);
		AtomicInteger atomicInteger = processingContext.getIfSameType(NOT_FINISHED_PROCESSING, AtomicInteger.class);
		if (atomicBoolean.get()) {
			return atomicInteger.compareAndSet(0, atomicInteger.decrementAndGet());
		}
		atomicInteger.decrementAndGet();
		return false;
	}

	/**
	 * Run matchers on instance.
	 *
	 * @param processingContext
	 *            the processing context
	 * @param context
	 *            the context
	 * @param instance
	 *            the instance
	 * @param totalProcessed
	 *            the index of the currently processed instance
	 * @return true, if matched successful
	 */
	private boolean runMatchersOnInstance(RuleContext processingContext, Context<String, Object> context,
			Instance instance, int totalProcessed) {
		for (RuleMatcher matcher : configuration.getMatchers()) {
			updateState(processingContext, ExecutionState.MATCHING, matcher)
					.onInstance(instance)
						.totalProcessed(totalProcessed);
			if (matcher.isApplicable(processingContext) && matcher.match(processingContext, instance, context)) {
				return true;
			}
		}
		return false;
	}

	/**
	 * Execute all operations for the matched instance.
	 *
	 * @param processingContext
	 *            the processing context
	 * @param instance
	 *            the instance
	 * @param context
	 *            the context
	 * @param processed
	 *            processed count
	 */
	private void onMatch(final RuleContext processingContext, final Instance instance,
			final Context<String, Object> context, final AtomicInteger processed) {
		try {
			// we could have a configuration if to execute it in new transaction or in a transaction
			transactionSupport.invokeInNewTx(() -> runOperations(processingContext, instance, context));
			processed.incrementAndGet();
		} catch (Exception e) {
			LOGGER.warn("Failed to run operations on matched {} with id [{}] due to error",
					instance.getClass().getSimpleName(), instance.getId(), e);
		}
	}

	private void runOperations(RuleContext processingContext, Instance instance, Context<String, Object> context) {
		for (RuleOperation rule : configuration.getOperations()) {
			if (rule.isApplicable(processingContext)) {
				updateState(processingContext, ExecutionState.EXECUTING_OPERATION, rule).onInstance(instance);
				rule.execute(processingContext, instance, context);
			}
		}
	}

	private static EntityRecognitionState updateState(RuleContext context, ExecutionState executionState,
			String operation) {
		EntityRecognitionState recognitionState = context.getState();
		recognitionState.setState(executionState);
		recognitionState.setRunningOperation(operation);
		return recognitionState;
	}

	private static EntityRecognitionState updateState(RuleContext context, ExecutionState executionState,
			com.sirma.itt.seip.Named operation) {
		EntityRecognitionState recognitionState = context.getState();
		recognitionState.setState(executionState);
		recognitionState.setRunningOperation(operation.getName());
		return recognitionState;
	}

	/**
	 * Represents the current rule status
	 *
	 * @author BBonev
	 */
	static class EntityRecognitionState implements RuleState {

		volatile ExecutionState state = ExecutionState.NOT_RUN;
		volatile String runningOperation;
		volatile Serializable instanceId;
		volatile int total;

		@Override
		public JSONObject toJSONObject() {
			JSONObject result = new JSONObject();
			JsonUtil.addToJson(result, "doing", state);
			JsonUtil.addToJson(result, "executing", runningOperation);
			JsonUtil.addToJson(result, "currentInstance", instanceId);
			JsonUtil.addToJson(result, "processedSoFar", total);
			return result;
		}

		@Override
		public void fromJSONObject(JSONObject jsonObject) {
			// no need to initialize
		}

		/**
		 * Sets the state.
		 *
		 * @param state
		 *            the new state
		 */
		public void setState(ExecutionState state) {
			this.state = state;
		}

		/**
		 * Sets the running operation.
		 *
		 * @param operation
		 *            the new running operation
		 */
		public void setRunningOperation(String operation) {
			runningOperation = operation;
		}

		/**
		 * On instance.
		 *
		 * @param instance
		 *            the instance
		 * @return the entity recognition state
		 */
		public EntityRecognitionState onInstance(Instance instance) {
			instanceId = instance == null ? null : instance.getId();
			return this;
		}

		/**
		 * Total processed.
		 *
		 * @param totalProcessed
		 *            the total
		 * @return the entity recognition state
		 */
		public EntityRecognitionState totalProcessed(int totalProcessed) {
			total = totalProcessed;
			return this;
		}
	}

	/**
	 * Entity rule recognitions states.
	 *
	 * @author BBonev
	 */
	private enum ExecutionState {
		NOT_RUN, PRE_CONDITION, EXECUTE_QUERY, MATCHING, GET_NEXT, EXECUTING_OPERATION, BEFORE_OPERATION, AFTER_OPERATION;
	}

}
