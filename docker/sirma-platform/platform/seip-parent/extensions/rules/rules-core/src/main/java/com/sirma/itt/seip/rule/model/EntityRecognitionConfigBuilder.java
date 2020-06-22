/*
 *
 */
package com.sirma.itt.seip.rule.model;

import static com.sirma.itt.emf.rule.DynamicInstanceRule.ASYNC_SUPPORT;
import static com.sirma.itt.emf.rule.DynamicInstanceRule.CONFIG;
import static com.sirma.itt.emf.rule.DynamicInstanceRule.DEFINED_IN;
import static com.sirma.itt.emf.rule.DynamicInstanceRule.INSTANCE_TYPES;
import static com.sirma.itt.emf.rule.DynamicInstanceRule.ON_DEFINITIONS;
import static com.sirma.itt.emf.rule.DynamicInstanceRule.OPERATIONS;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.spi.BeanManager;
import javax.inject.Inject;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sirma.itt.emf.rule.DynamicSupportable;
import com.sirma.itt.emf.rule.RuleMatcher;
import com.sirma.itt.emf.rule.RuleOperation;
import com.sirma.itt.emf.rule.RulePrecondition;
import com.sirma.itt.seip.collections.CollectionUtils;
import com.sirma.itt.seip.context.Configurable;
import com.sirma.itt.seip.context.Context;
import com.sirma.itt.seip.domain.exceptions.DefinitionValidationException;
import com.sirma.itt.seip.domain.util.DependencyResolver;
import com.sirma.itt.seip.util.CDI;

/**
 * Entity recognition configuration parser and builder.
 *
 * @author BBonev
 */
@ApplicationScoped
public class EntityRecognitionConfigBuilder {

	private static final Logger LOGGER = LoggerFactory.getLogger(EntityRecognitionConfigBuilder.class);
	/** The bean manager. */
	@Inject
	private BeanManager beanManager;

	/**
	 * Parses the.
	 *
	 * @param source
	 *            the source
	 * @return the recognition config
	 */
	public RecognitionConfig parse(Map<String, Object> source) {
		if (source == null || source.isEmpty()) {
			return null;
		}

		String ruleName = (String) source.get(DEFINED_IN);

		RecognitionConfig config = new RecognitionConfig();
		config.setPreconditions(buildSubElements("preconditions", RulePrecondition.class, source));
		config.setDataProviders(buildSubElements("dataProviders", DependencyResolver.class, source));
		if (CollectionUtils.isEmpty(config.getDataProviders())) {
			LOGGER.warn("No valid data providers found for rule {}. Rule will be disabled!", ruleName);
			return null;
		}
		config.setMatchers(buildSubElements("matchers", RuleMatcher.class, source));
		if (CollectionUtils.isEmpty(config.getMatchers())) {
			LOGGER.warn("No valid matchers found for rule {}. Rule will be disabled!", ruleName);
			return null;
		}
		config.setOperations(buildSubElements("operations", RuleOperation.class, source));
		if (CollectionUtils.isEmpty(config.getOperations())) {
			LOGGER.warn("No valid operations found for rule {}. Rule will be disabled!", ruleName);
			return null;
		}
		setParallelismMode(source, config);
		return config;
	}

	@SuppressWarnings("rawtypes")
	private void setParallelismMode(Map<String, Object> source, RecognitionConfig config) {
		Object specialConfig = source.get(CONFIG);
		if (specialConfig instanceof Map) {
			Map map = (Map) specialConfig;
			String object = (String) map.get("parallelism");
			config.setParallelism(ParallelismMode.parse(object));
		}
	}

	/**
	 * Read config.
	 *
	 * @param config
	 *            the config
	 * @return the base rule config
	 */
	@SuppressWarnings("unchecked")
	private BaseRuleConfig readConfig(Map<String, Object> config) {
		BaseRuleConfig ruleConfig = new BaseRuleConfig();
		ruleConfig.setName((String) config.get("name"));
		Object object = config.get(ASYNC_SUPPORT);
		if (object != null) {
			ruleConfig.setAsyncSupport(Boolean.valueOf(String.valueOf(object)));
		}
		Object objectTypes = config.get(INSTANCE_TYPES);
		if (objectTypes instanceof Collection) {
			Collection<?> collection = (Collection<?>) objectTypes;
			List<String> classes = new ArrayList<>(collection.size());
			for (Object type : collection) {
				CollectionUtils.addNonNullValue(classes, type.toString());
			}
			ruleConfig.setObjectTypes(classes);
		}
		Object onOperations = config.get(OPERATIONS);
		if (onOperations instanceof Collection) {
			ruleConfig.setOnOperations((Collection<String>) onOperations);
		}
		Object onDefinitions = config.get(ON_DEFINITIONS);
		if (onDefinitions instanceof Collection) {
			ruleConfig.setOnDefinitions((Collection<String>) onDefinitions);
		}
		return ruleConfig;
	}

	/**
	 * Builds the sub elements.
	 *
	 * @param <E>
	 *            the element type
	 * @param configName
	 *            the config name
	 * @param expectedType
	 *            the expected type
	 * @param source
	 *            the source
	 * @return the collection
	 */
	@SuppressWarnings("unchecked")
	public <E> Collection<E> buildSubElements(String configName, Class<E> expectedType, Map<String, Object> source) {
		Collection<?> providers = (Collection<?>) source.get(configName);
		Collection<E> target = Collections.emptyList();
		if (!CollectionUtils.isEmpty(providers)) {
			target = new ArrayList<>(providers.size());
			for (Object object : providers) {
				if (object instanceof Map) {
					Map<String, Object> map = (Map<String, Object>) object;
					String resolverName = (String) map.get("name");
					E resolver = instantiate(resolverName, expectedType, map);
					CollectionUtils.addNonNullValue(target, resolver);
				}
			}
		}
		return target;
	}

	/**
	 * Instantiate.
	 *
	 * @param <E>
	 *            the element type
	 * @param resolverName
	 *            the resolver name
	 * @param expectedType
	 *            the expected type
	 * @param map
	 *            the object
	 * @return the e
	 */
	@SuppressWarnings("unchecked")
	private <E> E instantiate(String resolverName, Class<E> expectedType, Map<String, Object> map) {
		if (StringUtils.isEmpty(resolverName)) {
			return null;
		}
		try {
			E bean = CDI.instantiateBean(resolverName, expectedType, beanManager);
			boolean isValid = true;
			if (bean instanceof Configurable) {
				Object object = map.get(CONFIG);
				if (object instanceof Map) {
					isValid = ((Configurable) bean).configure(new Context<>((Map<String, Object>) object));
				}
			}
			if (!isValid) {
				throw new DefinitionValidationException("Configurable instance with name " + resolverName + " and type "
						+ expectedType + " has invalid configuration " + map);
			}
			if (bean instanceof DynamicSupportable) {
				populateDynamicSupportable((DynamicSupportable) bean, map);
			}
			return bean;
		} catch (Exception e) {
			LOGGER.warn("Failed to instantiate and configure named bean [{}] of type [{}] using configuration {}.",
					resolverName, expectedType, map, e);
		}
		return null;
	}

	/**
	 * Populate dynamic supportable.
	 *
	 * @param supportable
	 *            the supportable
	 * @param map
	 *            the map
	 */
	private void populateDynamicSupportable(DynamicSupportable supportable, Map<String, Object> map) {
		BaseRuleConfig ruleConfig = readConfig(map);
		supportable.setIsAsyncSupported(ruleConfig.isAsyncSupport());
		supportable.setSupportedOperations(ruleConfig.getOnOperations());
		supportable.setSupportedTypes(ruleConfig.getObjectTypes());
		supportable.setSupportedDefinitions(ruleConfig.getOnDefinitions());
	}
}
