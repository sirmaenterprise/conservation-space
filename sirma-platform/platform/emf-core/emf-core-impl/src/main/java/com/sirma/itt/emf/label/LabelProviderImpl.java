package com.sirma.itt.emf.label;

import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.Set;
import java.util.function.Supplier;

import javax.annotation.PostConstruct;
import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.Produces;
import javax.inject.Inject;
import javax.inject.Named;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sirma.itt.seip.MutationObservable;
import com.sirma.itt.seip.collections.CollectionUtils;
import com.sirma.itt.seip.collections.ContextualMap;
import com.sirma.itt.seip.definition.label.LabelDefinition;
import com.sirma.itt.seip.definition.label.LabelService;
import com.sirma.itt.seip.domain.definition.label.LabelBundleProvider;
import com.sirma.itt.seip.domain.definition.label.LabelProvider;
import com.sirma.itt.seip.expressions.ExpressionEvaluatorManager;
import com.sirma.itt.seip.plugin.ExtensionPoint;
import com.sirma.itt.seip.security.UserPreferences;

/**
 * Default implementation for the {@link LabelProvider} interface. Caches the located {@link ResourceBundle} instances
 * for a given language.
 *
 * @author BBonev
 */
@Named("labelProvider")
@ApplicationScoped
public class LabelProviderImpl implements LabelProvider {

	/** The Constant logger. */
	private static final Logger LOGGER = LoggerFactory.getLogger(LabelProviderImpl.class);

	/** The label service. */
	@Inject
	private LabelService labelService;
	/** The manager. */
	@Inject
	private ExpressionEvaluatorManager manager;

	@Inject
	private UserPreferences userPreferences;

	@Inject
	@ExtensionPoint(value = LabelBundleProvider.TARGET_NAME, reverseOrder = true)
	private Iterable<LabelBundleProvider> labelProviders;

	/** Provided resource bundles mapped by language */
	@Inject
	private ContextualMap<String, List<ResourceBundle>> bundles;

	@PostConstruct
	void init() {
		// on change in label providers clear the cache, note that this will clear only the cache for the current tenant
		// and not all of them
		MutationObservable.registerToAll(labelProviders, () -> bundles.clear());
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
			return getBundleValue(labelId);
		}

		String label = definition.getLabels().get(language);
		if (label == null) {
			label = getBundleValue(labelId);
		}
		return label;
	}

	@Override
	public String getValue(final String key) {
		String language = getLanguage();
		return getBundleValue(key, language, () -> getLabelFromCache(key, language));
	}

	@Override
	public String getBundleValue(final String key) {
		return getBundleValue(key, getLanguage(), () -> key);
	}

	private String getBundleValue(final String key, String language, Supplier<String> defaultValueSupplier) {
		for (ResourceBundle bundle : getBundles(language)) {
			if (bundle.containsKey(key)) {
				return bundle.getString(key);
			}
		}
		return defaultValueSupplier.get();
	}

	@Override
	public Iterable<ResourceBundle> getBundles(String language) {
		return bundles.computeIfAbsent(language, this::getBundlesInternal);
	}

	private List<ResourceBundle> getBundlesInternal(String language) {
		LOGGER.debug("Computing bundles for locale: {}", language);
		List<ResourceBundle> result = new ArrayList<>();
		Locale locale = null;
		if (language == null) {
			String userLang = userPreferences.getLanguage();
			LOGGER.warn("Missing param for bundle language! Using default: {}", userLang);
			locale = new Locale(userLang);
		} else {
			locale = new Locale(language);
		}
		for (LabelBundleProvider provider : labelProviders) {
			ResourceBundle bundle = provider.getBundle(locale);
			if (bundle != null) {
				result.add(bundle);
			} else {
				LOGGER.debug("Labels bundle not found for extension {}", provider.getClass());
			}
		}
		return result;
	}

	@Override
	public Iterable<ResourceBundle> getBundles() {
		return getBundles(getLanguage());
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
