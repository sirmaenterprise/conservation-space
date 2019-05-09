package com.sirma.itt.seip.rule;

import static com.sirma.itt.emf.rule.DynamicInstanceRule.ASYNC_SUPPORT;
import static com.sirma.itt.emf.rule.DynamicInstanceRule.CONFIG;
import static com.sirma.itt.emf.rule.DynamicInstanceRule.DEFINED_IN;
import static com.sirma.itt.emf.rule.DynamicInstanceRule.INSTANCE_TYPES;
import static com.sirma.itt.emf.rule.DynamicInstanceRule.ON_DEFINITIONS;
import static com.sirma.itt.emf.rule.DynamicInstanceRule.OPERATIONS;

import java.lang.invoke.MethodHandles;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;
import java.util.stream.Stream;

import javax.annotation.PostConstruct;
import javax.enterprise.event.Observes;
import javax.enterprise.inject.spi.BeanManager;
import javax.inject.Inject;
import javax.inject.Singleton;

import org.apache.commons.lang3.StringUtils;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sirma.itt.emf.rule.DynamicInstanceRule;
import com.sirma.itt.emf.rule.InstanceRule;
import com.sirma.itt.emf.rule.RuleContext;
import com.sirma.itt.seip.collections.CollectionUtils;
import com.sirma.itt.seip.collections.ContextualMap;
import com.sirma.itt.seip.concurrent.TaskExecutor;
import com.sirma.itt.seip.concurrent.locks.ContextualReadWriteLock;
import com.sirma.itt.seip.configuration.ConfigurationProperty;
import com.sirma.itt.seip.configuration.annotation.Configuration;
import com.sirma.itt.seip.configuration.annotation.ConfigurationPropertyDefinition;
import com.sirma.itt.seip.context.Context;
import com.sirma.itt.seip.convert.TypeConverter;
import com.sirma.itt.seip.definition.DefinitionService;
import com.sirma.itt.seip.definition.event.DefinitionsChangedEvent;
import com.sirma.itt.seip.definition.util.PathHelper;
import com.sirma.itt.seip.domain.ObjectTypes;
import com.sirma.itt.seip.domain.PathElement;
import com.sirma.itt.seip.domain.definition.DefinitionModel;
import com.sirma.itt.seip.domain.definition.GenericDefinition;
import com.sirma.itt.seip.domain.definition.PropertyDefinition;
import com.sirma.itt.seip.json.JsonUtil;
import com.sirma.itt.seip.plugin.ExtensionPoint;
import com.sirma.itt.seip.util.CDI;

/**
 * Stores all instantiated rules and provides means for accessing them.<br>
 * TODO: add option to enable and disabled rules at runtime<br>
 * TODO: add rest service to manage the active rules in the system
 *
 * @author BBonev
 */
@Singleton
public class RuleStore {

	private static final Logger LOGGER = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

	private static final Pattern SPILT_PATTERN = Pattern.compile("\\s*,\\s*");

	private Map<String, List<InstanceRule>> rulesCache = new HashMap<>(64);

	@Inject
	private ContextualMap<String, List<InstanceRule>> defRulesCache;

	/**
	 * Rule mapping by {@link InstanceRule#getRuleInstanceName()}
	 */
	@Inject
	private ContextualMap<String, InstanceRule> ruleMapping;

	@Inject
	private ContextualReadWriteLock ruleAccessLock;

	@Inject
	@ExtensionPoint(InstanceRule.TARGET_NAME)
	private Iterable<InstanceRule> rules;

	@Inject
	private DefinitionService definitionService;

	@Inject
	private BeanManager beanManager;

	@Inject
	private TypeConverter typeConverter;

	@Inject
	private TaskExecutor taskExecutor;

	@Inject
	@Configuration
	@ConfigurationPropertyDefinition(name = "rules.store.inactiveRules", sensitive = true, defaultValue = "dummy", type = Set.class, label = "A list of not active rules")
	private ConfigurationProperty<Set<String>> inactiveRules;

	/**
	 * Initialize.
	 */
	@PostConstruct
	void initialize() {
		populateOperationCache(rules, rulesCache);
		prepareDefInstanceRules();
	}

	/**
	 * Trigger asynchronous rule reload. The method returns immediately.
	 */
	public void reloadRules() {
		onDefinitionLoaded(null);
	}

	/**
	 * Find rules that are applicable for execution of the given instance and operation. All returned rules are tested
	 * for applicability before turn and passed at the moment of calling this method.
	 *
	 * @param operation
	 *            the operation that triggered rule activation
	 * @param context
	 *            the execution context that will be used for rule invocation
	 * @return the list with rules to execute or empty collection if non are found or all were non applicable
	 */
	public Collection<InstanceRule> findRules(String operation, Context<String, Object> context) {
		List<InstanceRule> toReturn = new LinkedList<>();

		collectRulesFromCache(rulesCache, operation, context, toReturn);
		ruleAccessLock.readLock().lock();
		try {
			collectRulesFromCache(defRulesCache, operation, context, toReturn);
		} finally {
			ruleAccessLock.readLock().unlock();
		}

		// if not found in cache try in all operations
		if (CollectionUtils.isEmpty(toReturn)) {
			for (InstanceRule rule : rules) {
				if (rule.isApplicable(context)) {
					toReturn.add(rule);
				}
			}
		}
		return toReturn;
	}

	/**
	 * Gets the rule by definition id. The method tries to load the given definition and instantiate a rule from it if
	 * valid.
	 *
	 * @param definitionId
	 *            the definition id to look for a rule
	 * @return the instance rule or <code>null</code> if no rule found or failed to configure it
	 */
	public InstanceRule getRuleById(String definitionId) {
		InstanceRule instanceRule = getRuleFromCache(definitionId);
		if (instanceRule != null) {
			return instanceRule;
		}

		DefinitionModel definition = findDefinition(definitionId);
		if (definition == null) {
			return null;
		}

		InstanceRule createdRule = instantiateRuleFromDefinition(definition);
		if (configureRule(createdRule, definition)) {
			addToRuleIdMappingCache(definitionId, createdRule);
			return createdRule;
		}
		return null;
	}

	private InstanceRule getRuleFromCache(String definitionId) {
		ruleAccessLock.readLock().lock();
		try {
			InstanceRule instanceRule = ruleMapping.get(definitionId);
			if (instanceRule != null) {
				return instanceRule;
			}
			return null;
		} finally {
			ruleAccessLock.readLock().unlock();
		}
	}

	private void addToRuleIdMappingCache(String definitionId, InstanceRule createdRule) {
		try {
			ruleAccessLock.writeLock().lock();
			ruleMapping.put(definitionId, createdRule instanceof ActivatingInstanceRule ? createdRule
					: new ActivatingInstanceRule(createdRule, isRuleActive(createdRule)));
		} finally {
			ruleAccessLock.writeLock().unlock();
		}
	}

	private DefinitionModel findDefinition(String definitionId) {
		if (definitionId == null) {
			return null;
		}
		List<GenericDefinition> allDefinitions = definitionService.getAllDefinitions(GenericDefinition.class);
		for (GenericDefinition definition : allDefinitions) {
			if (isRuleDefinition(definition) && definitionId.equals(definition.getIdentifier())) {
				return definition;
			}
		}
		return null;
	}

	/**
	 * List active rules.
	 *
	 * @return the collection
	 */
	public Stream<InstanceRule> listActiveRules() {
		return ruleMapping.values().stream().filter(ActivatingInstanceRule::isActive);
	}

	/**
	 * List inactive rules
	 *
	 * @return the collection
	 */
	public Stream<InstanceRule> listInactiveRules() {
		return ruleMapping.values().stream().filter(ActivatingInstanceRule::isInactive);
	}

	/**
	 * Activate rule identified by the given name
	 *
	 * @param ruleName
	 *            the rule name
	 */
	public void activateRule(String ruleName) {
		InstanceRule instanceRule = ruleMapping.get(ruleName);
		if (!(instanceRule instanceof ActivatingInstanceRule)) {
			LOGGER.warn("Could not activate rule {} because is not activatable or not found", ruleName);
			return;
		}

		((ActivatingInstanceRule) instanceRule).activate();
	}

	/**
	 * Deactivate rule identified by the given name. The deactivated rule will not be returned by the method
	 * {@link #findRules(String, Context)} and {@link #listActiveRules()}
	 *
	 * @param ruleName
	 *            the rule name
	 */
	public void deactivateRule(String ruleName) {
		InstanceRule instanceRule = ruleMapping.get(ruleName);
		if (!(instanceRule instanceof ActivatingInstanceRule)) {
			LOGGER.warn("Could not deactivate rule {} because is not deactivatable or not found", ruleName);
			return;
		}

		((ActivatingInstanceRule) instanceRule).deactivate();
	}

	void onDefinitionLoaded(@Observes DefinitionsChangedEvent event) {
		taskExecutor.executeAsync(this::prepareDefInstanceRules);
	}

	private void populateOperationCache(Iterable<InstanceRule> iterable, Map<String, List<InstanceRule>> cache) {
		for (InstanceRule instanceRule : iterable) {
			for (String operation : instanceRule.getSupportedOperations()) {
				CollectionUtils.addValueToMap(cache, operation, instanceRule);
			}
			ruleMapping.put(instanceRule.getRuleInstanceName(), instanceRule);
		}
	}

	private void prepareDefInstanceRules() {
		ruleAccessLock.writeLock().lock();
		try {
			ruleMapping.clear();
			defRulesCache.clear();

			List<GenericDefinition> allDefinitions = definitionService.getAllDefinitions(GenericDefinition.class);
			List<InstanceRule> defRules = new LinkedList<>();
			for (GenericDefinition definition : allDefinitions) {
				if (!isRuleDefinition(definition)) {
					continue;
				}

				buildAndConfigureRuleFromDefinition(definition, defRules);
			}
			populateOperationCache(defRules, defRulesCache);
		} finally {
			ruleAccessLock.writeLock().unlock();
		}
	}

	private static boolean isRuleDefinition(GenericDefinition definition) {
		return ObjectTypes.RULE.equalsIgnoreCase(definition.getType());
	}

	private void buildAndConfigureRuleFromDefinition(DefinitionModel definition, List<InstanceRule> defRules) {
		InstanceRule instanceRule = instantiateRuleFromDefinition(definition);
		if (instanceRule == null) {
			LOGGER.warn("No valid rule found in definition: " + definition.getIdentifier());
			// no valid rule found
			return;
		}

		if (configureRule(instanceRule, definition)) {
			defRules.add(new ActivatingInstanceRule(instanceRule, isRuleActive(instanceRule)));
		} else {
			LOGGER.trace("Skipping rule {} due to failed configuration from definition: {}",
					instanceRule.getClass().getName(), definition.getIdentifier());
		}
		// if the rule is not configured correctly we can't use it
	}

	private boolean isRuleActive(InstanceRule instanceRule) {
		return !inactiveRules.get().contains(instanceRule.getRuleInstanceName());
	}

	/**
	 * @return true, if the rule was successfully initialized and <code>false</code> if not and should be ignored
	 */
	private boolean configureRule(InstanceRule rule, DefinitionModel definition) {
		if (!(rule instanceof DynamicInstanceRule)) {
			// all good if not implementing the interface
			return true;
		}
		DynamicInstanceRule instanceRule = (DynamicInstanceRule) rule;

		Context<String, Object> configuration = readConfiguration(definition, instanceRule);

		configuration.put(DEFINED_IN, PathHelper.getPath((PathElement) definition));

		try {
			return instanceRule.configure(configuration);
		} catch (RuntimeException e) {
			LOGGER.error("Failed to configure rule {} due to invalid configuration. " + "The rule will be ignored!",
					PathHelper.getPath((PathElement) definition), e);
			return false;
		}
	}

	private Context<String, Object> readConfiguration(DefinitionModel definition, DynamicInstanceRule instanceRule) {
		Context<String, Object> context = new Context<>();
		Set<String> operations = new LinkedHashSet<>();
		Set<String> instanceTypes = new LinkedHashSet<>();
		Set<String> definitions = new LinkedHashSet<>();
		Boolean asyncSupport = Boolean.FALSE;

		for (PropertyDefinition property : definition.getFields()) {
			boolean isEmpty = property.getDefaultValue().isEmpty();
			if (isEmpty) {
				continue;
			}
			switch (property.getIdentifier()) {
				case OPERATIONS:
					operations.addAll(splitValue(property));
					break;
				case INSTANCE_TYPES:
					instanceTypes.addAll(splitValue(property));
					break;
				case CONFIG:
					JSONObject jsonConfig = JsonUtil.createObjectFromString(property.getDefaultValue());
					context.put(CONFIG, JsonUtil.toMap(jsonConfig));
					break;
				case ASYNC_SUPPORT:
					asyncSupport = Boolean.valueOf(property.getDefaultValue());
					break;
				case ON_DEFINITIONS:
					definitions.addAll(splitValue(property));
					break;
				default:
					// collect all other properties from that does not have a specific control
					if (property.getControlDefinition() == null) {
						context.put(property.getIdentifier(), convertValue(property));
					}
					break;
			}
		}
		instanceRule.setSupportedOperations(operations);
		instanceRule.setSupportedTypes(instanceTypes);
		instanceRule.setIsAsyncSupported(asyncSupport);
		instanceRule.setSupportedDefinitions(definitions);
		return context;
	}

	private static List<String> splitValue(PropertyDefinition property) {
		return Arrays.asList(SPILT_PATTERN.split(property.getDefaultValue()));
	}

	private Object convertValue(PropertyDefinition property) {
		return typeConverter.convert(property.getDataType().getJavaClass(), property.getDefaultValue());
	}

	private InstanceRule instantiateRuleFromDefinition(DefinitionModel definition) {
		for (PropertyDefinition field : definition.getFields()) {
			if (CONFIG.equalsIgnoreCase(field.getName()) && field.getControlDefinition() != null
					&& StringUtils.isNotBlank(field.getControlDefinition().getIdentifier())) {

				String ruleId = field.getControlDefinition().getIdentifier();
				try {
					return CDI.instantiateBean(ruleId, InstanceRule.class, beanManager);
				} catch (Exception e) {
					LOGGER.warn("No named instance rule bean found with name {}." + " Rule will not be executed!",
							ruleId);
					LOGGER.trace("Could not find rule with name {}", ruleId, e);
				}
			}
		}
		return null;
	}

	/**
	 * Collect rules from cache using the specified key.
	 */
	private static <K> void collectRulesFromCache(Map<K, List<InstanceRule>> cache, K key,
			Context<String, Object> context, Collection<InstanceRule> collectedRules) {
		List<InstanceRule> instanceRules = cache.get(key);
		if (!CollectionUtils.isEmpty(instanceRules)) {
			for (InstanceRule rule : instanceRules) {
				if (rule.isApplicable(context)) {
					collectedRules.add(rule);
				}
			}
		}
	}

	/**
	 * Instance rule that can be activated and deactivated dinamically.
	 *
	 * @author BBonev
	 */
	private static class ActivatingInstanceRule implements InstanceRule {

		private final InstanceRule proxy;
		private boolean active = true;

		/**
		 * Instantiates a new activating instance rule.
		 *
		 * @param proxy
		 *            the proxy
		 * @param active
		 *            the active
		 */
		public ActivatingInstanceRule(InstanceRule proxy, boolean active) {
			this.proxy = proxy;
			this.active = active;
		}

		@Override
		public List<String> getSupportedObjects() {
			return proxy.getSupportedObjects();
		}

		@Override
		public boolean isAsyncSupported() {
			return proxy.isAsyncSupported();
		}

		@Override
		public Collection<String> getSupportedDefinitions() {
			return proxy.getSupportedDefinitions();
		}

		@Override
		public String getPrimaryOperation() {
			return proxy.getPrimaryOperation();
		}

		@Override
		public Collection<String> getSupportedOperations() {
			return proxy.getSupportedOperations();
		}

		@Override
		public boolean isApplicable(Context<String, Object> context) {
			return isActive() && proxy.isApplicable(context);
		}

		@Override
		public String getRuleInstanceName() {
			return proxy.getRuleInstanceName();
		}

		@Override
		public void execute(RuleContext context) {
			proxy.execute(context);
		}

		/**
		 * Activates the rule
		 */
		public void activate() {
			active = true;
		}

		/**
		 * Deactivates the rule
		 */
		public void deactivate() {
			active = false;
		}

		/**
		 * Checks if is active.
		 *
		 * @return true, if is active
		 */
		public boolean isActive() {
			return active;
		}

		/**
		 * Checks if the given rule is active.
		 *
		 * @param rule
		 *            the rule
		 * @return true, if is active
		 */
		public static boolean isActive(InstanceRule rule) {
			return !(rule instanceof ActivatingInstanceRule) || ((ActivatingInstanceRule) rule).isActive();
		}

		/**
		 * Checks if the given rule is active.
		 *
		 * @param rule
		 *            the rule
		 * @return true, if is active
		 */
		public static boolean isInactive(InstanceRule rule) {
			return !isActive(rule);
		}
	}
}
