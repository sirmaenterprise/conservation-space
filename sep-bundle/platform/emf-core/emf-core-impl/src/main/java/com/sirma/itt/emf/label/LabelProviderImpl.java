package com.sirma.itt.emf.label;

import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.Set;

import javax.annotation.PostConstruct;
import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.context.ContextNotActiveException;
import javax.enterprise.inject.Produces;
import javax.inject.Inject;
import javax.inject.Named;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sirma.itt.emf.configuration.Config;
import com.sirma.itt.emf.configuration.EmfConfigurationProperties;
import com.sirma.itt.emf.evaluation.ExpressionEvaluatorManager;
import com.sirma.itt.emf.plugin.ExtensionPoint;
import com.sirma.itt.emf.security.AuthenticationService;
import com.sirma.itt.emf.security.context.SecurityContextManager;
import com.sirma.itt.emf.security.model.User;
import com.sirma.itt.emf.util.CollectionUtils;

/**
 * Default implementation for the {@link LabelProvider} interface. Caches the located
 * {@link ResourceBundle} instances for a given language.
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
	private AuthenticationService authenticationService;

	@Inject
	@Config(name = EmfConfigurationProperties.SYSTEM_LANGUAGE, defaultValue = "bg")
	private String defaultLanguage;

	@Inject
	@ExtensionPoint(value = LabelBundleProvider.TARGET_NAME, reverseOrder = true)
	private Iterable<LabelBundleProvider> labelProviders;

	/** Provided resource bundles mapped by language */
	private Map<String, List<ResourceBundle>> bundles;

	/**
	 * Initialize the bundle providers.
	 */
	@PostConstruct
	public void initializeProviders() {
		bundles = new HashMap<String, List<ResourceBundle>>();
	}

	@Override
	public String getLabel(String labelId) {
		return getLabelFromCache(labelId, getLanguage());
	}

	/**
	 * Gets the language.
	 *
	 * @return the language
	 */
	private String getLanguage() {
		try {
			User user = authenticationService.getCurrentUser();
			if (user != null) {
				return user.getLanguage();
			}
		} catch (ContextNotActiveException e) {
			User loggedUser = SecurityContextManager.getFullAuthentication();
			if (loggedUser != null) {
				return loggedUser.getLanguage();
			}
		}
		return defaultLanguage;
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
		if (labelId.startsWith(ExpressionEvaluatorManager.EXPRESSION_PREFIX)) {
			String evaluated = manager.evaluate(labelId, String.class);
			if ((evaluated != null)
					&& !evaluated.startsWith(ExpressionEvaluatorManager.EXPRESSION_PREFIX)) {
				return evaluated;
			}
		}

		LabelDefinition definition = labelService.getLabel(labelId);
		if (definition == null) {
			return labelId;
		}

		String label = definition.getLabels().get(language);
		if (label == null) {
			label = labelId;
		}
		return label;
	}

	@Override
	public String getValue(final String key) {
		String language = getLanguage();
		String label = null;
		for (ResourceBundle bundle : getBundles(language)) {
			if (bundle.containsKey(key)) {
				label = bundle.getString(key);
				break;
			}
		}
		if (label == null) {
			label = getLabel(key, language);
		}
		return label;
	}

	@Override
	public Iterable<ResourceBundle> getBundles(String language) {
		List<ResourceBundle> result = bundles.get(language);

		if (result == null) {
			// iterate all providers and get the ResourceBundles for the current language and cache
			// it
			result = new ArrayList<>();
			Locale locale = null;
			if (language == null) {
				LOGGER.warn("Missing param for bundle language! Using default: " + defaultLanguage);
				locale = new Locale(defaultLanguage);
			} else {
				locale = new Locale(language);
			}
			for (LabelBundleProvider provider : labelProviders) {
				ResourceBundle bundle = provider.getBundle(locale);
				if (bundle != null) {
					result.add(bundle);
				} else {
					LOGGER.debug("Labels bundle not found for extension " + provider.getClass());
				}
			}
			bundles.put(language, result);
		}
		return result;
	}

	@Override
	public Iterable<ResourceBundle> getBundles() {
		return getBundles(getLanguage());
	}

	/**
	 * Provide a fake Map implementation that calls the label provider in its get(key) method in
	 * order to use expressions like label['label.key'].
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
