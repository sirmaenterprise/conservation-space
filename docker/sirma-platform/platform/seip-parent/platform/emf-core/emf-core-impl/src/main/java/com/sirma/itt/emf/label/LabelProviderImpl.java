package com.sirma.itt.emf.label;

import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.function.Supplier;

import javax.annotation.PostConstruct;
import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.Produces;
import javax.inject.Inject;
import javax.inject.Named;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sirma.itt.seip.MutationObservable;
import com.sirma.itt.seip.collections.CollectionUtils;
import com.sirma.itt.seip.collections.ContextualMap;
import com.sirma.itt.seip.definition.label.LabelDefinition;
import com.sirma.itt.seip.definition.label.LabelService;
import com.sirma.itt.seip.domain.definition.PropertyDefinition;
import com.sirma.itt.seip.domain.definition.label.LabelResolverProvider;
import com.sirma.itt.seip.domain.definition.label.LabelProvider;
import com.sirma.itt.seip.domain.definition.label.LabelResolver;
import com.sirma.itt.seip.expressions.ExpressionEvaluatorManager;
import com.sirma.itt.seip.plugin.ExtensionPoint;
import com.sirma.itt.seip.security.UserPreferences;

/**
 * Default implementation for the {@link LabelProvider} interface. Caches the located {@link LabelResolver} instances
 * for a given language.
 *
 * @author BBonev
 */
@Named("labelProvider")
@ApplicationScoped
public class LabelProviderImpl implements LabelProvider {

	/** The Constant logger. */
	private static final Logger LOGGER = LoggerFactory.getLogger(LabelProviderImpl.class);
	private static final String DEFAULT_LANGUAGE = Locale.ENGLISH.getLanguage();

	/** The label service. */
	@Inject
	private LabelService labelService;
	/** The manager. */
	@Inject
	private ExpressionEvaluatorManager manager;

	@Inject
	private UserPreferences userPreferences;

	@Inject
	@ExtensionPoint(value = LabelResolverProvider.TARGET_NAME, reverseOrder = true)
	private Iterable<LabelResolverProvider> labelProviders;

	/** Provided resource resolvers mapped by language */
	@Inject
	private ContextualMap<String, List<LabelResolver>> resolvers;

	@PostConstruct
	void init() {
		// on change in label providers clear the cache, note that this will clear only the cache for the current tenant
		// and not all of them
		MutationObservable.registerToAll(labelProviders, () -> {
			LOGGER.info("Reloading label providers");
			resolvers.clear();
		});
	}

	@Override
	public String getLabel(String labelId) {
		return getLabelFromCache(labelId, getLanguage());
	}

	/**
	 * Gets the current user language.
	 *
	 * @return the language
	 */
	private String getLanguage() {
		return userPreferences.getLanguage();
	}

	@Override
	public String getLabel(String labelId, String language) {
		return getLabelFromCache(labelId, language);
	}

	/**
	 * Gets the label from cache.
	 *
	 * @param labelId
	 *            the label id
	 * @param language
	 *            the language
	 * @return the label from cache
	 */
	private String getLabelFromCache(String labelId, String language) {
		if (labelId == null) {
			return NO_LABEL;
		}
		// check if the given label id is an expression and if so tries to
		// evaluate it. The check is only for optimization
		if (manager.isExpression(labelId)) {
			String evaluated = manager.evaluate(labelId, String.class);
			if (evaluated != null && !manager.isExpression(evaluated)) {
				return evaluated;
			}
		}

		LabelDefinition definition = labelService.getLabel(labelId);
		if (definition == null) {
			return getLabelOrDefault(labelId, language);
		}

		String label = definition.getLabels().get(language);
		if (StringUtils.isBlank(label) && !isDefaultLanguage(language)) {
			label = definition.getLabels().get(DEFAULT_LANGUAGE);
		}
		if (StringUtils.isBlank(label)) {
			label = getLabelOrDefault(labelId, language);
		}
		return label;
	}

	private static boolean isDefaultLanguage(String language) {
		return DEFAULT_LANGUAGE.equals(language);
	}

	@Override
	public String getValue(final String key) {
		String language = getLanguage();
		return getResolverValue(key, language, () -> getLabelFromCache(key, language));
	}

	@Override
	public String getBundleValue(final String key) {
		return getLabelOrDefault(key, getLanguage());
	}

	@Override
	public String getPropertyLabel(PropertyDefinition propertyDefinition) {
		return resolvePropertyLabel(propertyDefinition, LabelProvider::buildUriLabelId, propertyDefinition.getLabelId());
	}

	@Override
	public String getPropertyTooltip(PropertyDefinition propertyDefinition) {
		return resolvePropertyLabel(propertyDefinition, LabelProvider::buildUriTooltipId, propertyDefinition.getTooltipId());
	}

	private String resolvePropertyLabel(PropertyDefinition propertyDefinition, Function<String, String> uriToLabelMapper, String definitionLabelId) {
		String language = getLanguage();
		return getDefinitionLabelOrElse(definitionLabelId, language, () -> {
			if (PropertyDefinition.hasUri().test(propertyDefinition)) {
				String uriKey = uriToLabelMapper.apply(PropertyDefinition.resolveUri().apply(propertyDefinition));

				if (isDefaultLanguage(language)) {
					return getResolverValue(uriKey, DEFAULT_LANGUAGE, () -> uriKey + "/" + definitionLabelId);
				}
			/*
			  The logic runs the following steps and returns the first found value:
				1. check if the given label has value for the given language
				2. check if the given uri id has value for the given language
				3. check if the given label has value for the default language
				4. check if the given uri id has value for the default language
			 */
				return getResolverValue(uriKey, language,
						() -> getDefinitionLabelOrElse(definitionLabelId, DEFAULT_LANGUAGE,
								() -> getResolverValue(uriKey, DEFAULT_LANGUAGE,
										() -> uriKey + "/" + definitionLabelId)));
			}
			return getDefinitionLabelOrElse(definitionLabelId, DEFAULT_LANGUAGE, () -> definitionLabelId);
		});
	}

	private String getDefinitionLabelOrElse(String labelId, String language, Supplier<String> defaultValueSupplier) {
		if (labelId == null) {
			return defaultValueSupplier.get();
		}
		LabelDefinition definition = labelService.getLabel(labelId);
		if (definition == null) {
			return defaultValueSupplier.get();
		}
		Map<String, String> labels = definition.getLabels();
		if (labels == null) {
			return defaultValueSupplier.get();
		}
		String label = labels.get(language);
		if (StringUtils.isEmpty(label)) {
			return defaultValueSupplier.get();
		}
		return label;
	}

	private String getLabelOrDefault(String key, String language) {
		return getResolverValue(key, language, () -> {
			// same language no need to get it twice just return the key
			if (isDefaultLanguage(language)) {
				return key;
			}
			return getResolverValue(key, DEFAULT_LANGUAGE, () -> key);
		});
	}

	/*
	 * Resolvers are bundle files and semantic repository
	 */
	private String getResolverValue(final String key, String language, Supplier<String> defaultValueSupplier) {
		for (LabelResolver resolver : getResolvers(language)) {
			if (resolver.containsLabel(key)) {
				String value = resolver.getLabel(key);
				if (StringUtils.isNotBlank(value)) {
					return value;
				}
			}
		}
		return defaultValueSupplier.get();
	}

	private Iterable<LabelResolver> getResolvers(String language) {
		return resolvers.computeIfAbsent(language, this::getResolversInternal);
	}

	private List<LabelResolver> getResolversInternal(String language) {
		LOGGER.debug("Computing resolvers for locale: {}", language);
		List<LabelResolver> result = new ArrayList<>();
		Locale locale;
		if (language == null) {
			String userLang = userPreferences.getLanguage();
			LOGGER.warn("Missing param for resolver language! Using default: {}", userLang);
			locale = new Locale(userLang);
		} else {
			locale = new Locale(language);
		}
		for (LabelResolverProvider provider : labelProviders) {
			LabelResolver resolver = provider.getLabelResolver(locale);
			if (resolver != null) {
				result.add(resolver);
			} else {
				LOGGER.debug("Labels resolver not found for extension {}", provider.getClass());
			}
		}
		return result;
	}

	/**
	 * Provide a fake Map implementation that calls the label provider in its get(key) method in order to use
	 * expressions like label['label.key'].
	 *
	 * @return constructed map.
	 */
	@Produces
	@Named("label")
	@ApplicationScoped
	public Map<String, String> produceLabelsMap() {
		return new AbstractMap<String, String>() {
			@Override
			public String get(Object key) {
				return getValue((String) key);
			}

			@Override
			public Set<java.util.Map.Entry<String, String>> entrySet() {
				return CollectionUtils.emptySet();
			}
		};
	}

}
