package com.sirma.itt.seip.rule;

import java.io.Serializable;
import java.lang.invoke.MethodHandles;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedDeque;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.inject.Inject;
import javax.inject.Singleton;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sirma.itt.emf.rule.InstanceRule;
import com.sirma.itt.emf.rule.RuleContext;
import com.sirma.itt.emf.rule.invoker.RuleExecutionStatusAccessor;
import com.sirma.itt.emf.rule.invoker.RuleRunner;
import com.sirma.itt.seip.configuration.ConfigurationProperty;
import com.sirma.itt.seip.configuration.annotation.Configuration;
import com.sirma.itt.seip.configuration.annotation.ConfigurationConverter;
import com.sirma.itt.seip.configuration.annotation.ConfigurationGroupDefinition;
import com.sirma.itt.seip.configuration.annotation.ConfigurationPropertyDefinition;
import com.sirma.itt.seip.configuration.convert.GroupConverterContext;
import com.sirma.itt.seip.domain.instance.Instance;
import com.sirma.itt.seip.monitor.Statistics;
import com.sirma.itt.seip.security.context.SecurityContextManager;
import com.sirma.itt.seip.time.TimeTracker;
import com.sirma.itt.seip.tx.TransactionSupport;

/**
 * {@link RuleRunner} implementation that provides means of executing rules in a fixed thread pool configured by
 * <code>rules.runner.maxConcurrentRules</code> configuration property. The default pool size is 10.
 *
 * @author BBonev
 */
@Singleton
public class ThreadPoolRuleRunner implements RuleRunner {
	private static final Logger LOGGER = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

	@Inject
	private TransactionSupport transactionSupport;
	@Inject
	private SecurityContextManager securityContextManager;

	@Inject
	private volatile Statistics statistics;

	@ConfigurationPropertyDefinition(defaultValue = "8", sensitive = true, type = Integer.class, label = "Maximum active rules", system = true, shared = false)
	private static final String RULES_RUNNER_MAX_CONCURRENT_RULES = "rules.runner.maxConcurrentRules";

	/**
	 * Store of the currently running rules over the instances identified by the map keys. The map values are
	 * {@link ConcurrentLinkedDeque}s to allow concurrent modifications.
	 */
	private Map<Serializable, Collection<RuleExecutionStatusAccessor>> runningRules = new ConcurrentHashMap<>(1024,
			0.9f, Math.min(Runtime.getRuntime().availableProcessors() / 2, 1));

	private ScheduledExecutorService cleaner;

	@ConfigurationGroupDefinition(system = true, type = ExecutorService.class, properties = {
			RULES_RUNNER_MAX_CONCURRENT_RULES })
	private static final String RULES_RUNNER_EXECUTOR = "rules.runner.executor";

	@Inject
	@Configuration(RULES_RUNNER_EXECUTOR)
	private ConfigurationProperty<ExecutorService> ruleExecutor;

	@ConfigurationConverter(RULES_RUNNER_EXECUTOR)
	static ExecutorService createRunner(GroupConverterContext context) {
		Integer maxActiveRules = context.get(RULES_RUNNER_MAX_CONCURRENT_RULES);
		return Executors.newFixedThreadPool(maxActiveRules);
	}

	@PostConstruct
	void initialize() {
		// stop the current executor server and create new one the old task will complete on
		// the old and new tasks will be scheduled to the new one
		ruleExecutor.addValueDestroyListener(ExecutorService::shutdown);

		// start cleaner to execute map cleaning for empty queues. Most of the time the thread will
		// be dormant
		cleaner = Executors.newScheduledThreadPool(1);
		cleaner.scheduleAtFixedRate(this::cleanFinished, 60, 60, TimeUnit.SECONDS);
	}

	private void cleanFinished() {
		runningRules.entrySet().removeIf(entry -> entry.getValue().isEmpty());
	}

	/**
	 * Terminate worker threads
	 */
	@PreDestroy
	void onShutdown() {
		ruleExecutor.get().shutdownNow();
		cleaner.shutdown();
	}

	private static Serializable createActiveRuleKey(Instance currentInstance) {
		return currentInstance.getId();
	}

	/**
	 * Register instance for processing.
	 *
	 * @param key
	 *            the key
	 * @return <code>true</code>, if successful registered and <code>false</code> if already registered
	 */
	private boolean registerInstanceForProcessing(Serializable key) {

		if (runningRules.containsKey(key)) {
			LOGGER.debug("Will skip rules on instance that is being processed by rules");
			return false;
		} else {
			runningRules.put(key, new ConcurrentLinkedDeque<>());
		}
		return true;
	}

	@Override
	public void scheduleRules(List<InstanceRule> rulesToRun, RuleContext context) {
		Serializable activeRuleKey = createActiveRuleKey(context.getTriggerInstance());
		if (!registerInstanceForProcessing(activeRuleKey)) {
			LOGGER.warn("Rule for {} already active. Ignoring request", activeRuleKey);
			return;
		}
		Collection<RuleExecutionStatusAccessor> collection = runningRules.get(activeRuleKey);

		RulesExecutionContext executionContext = new RulesExecutionContext(rulesToRun, context, statistics,
				collection, transactionSupport, securityContextManager);
		collection.add(executionContext);

		// link the callable and the produced future so that we can cancel them if needed without
		// storing the future somewhere else.
		executionContext.setFuture(ruleExecutor.get().submit(executionContext));
	}

	@Override
	public void runRules(List<InstanceRule> rulesToRun, RuleContext context) {
		for (InstanceRule rule : rulesToRun) {
			transactionSupport.invokeInNewTx(new RuleCallable(rule, context, new TimeTracker()));
		}
	}

	@Override
	public Map<Serializable, Collection<RuleExecutionStatusAccessor>> getAllActiveRules() {
		return Collections.unmodifiableMap(runningRules);
	}

	@Override
	public Collection<RuleExecutionStatusAccessor> getActiveRules(Serializable instanceId) {
		Collection<RuleExecutionStatusAccessor> runntingTasks = runningRules.get(instanceId);
		if (runntingTasks == null) {
			return Collections.emptyList();
		}
		return Collections.unmodifiableCollection(runntingTasks);
	}

	@Override
	public void cancelRunningRulesForInstance(Serializable instanceId) {
		runningRules.getOrDefault(instanceId, Collections.emptyList()).forEach(runningTask -> {
			RulesExecutionContext executionContext = (RulesExecutionContext) runningTask;
			if (executionContext.cancel()) {
				LOGGER.warn("Send cancel request for rule processing on {}", instanceId);
			} else {
				LOGGER.warn("Could not cancel task for instance {}", instanceId);
			}
		});
	}

}
