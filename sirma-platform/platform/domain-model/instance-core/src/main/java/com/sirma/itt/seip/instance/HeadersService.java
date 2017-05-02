package com.sirma.itt.seip.instance;

import java.io.Serializable;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.event.Observes;
import javax.inject.Inject;

import com.sirma.itt.commons.utils.string.StringUtils;
import com.sirma.itt.seip.Pair;
import com.sirma.itt.seip.collections.CollectionUtils;
import com.sirma.itt.seip.definition.DictionaryService;
import com.sirma.itt.seip.domain.definition.DefinitionModel;
import com.sirma.itt.seip.domain.definition.PropertyDefinition;
import com.sirma.itt.seip.domain.definition.label.LabelProvider;
import com.sirma.itt.seip.domain.instance.DefaultProperties;
import com.sirma.itt.seip.domain.instance.Instance;
import com.sirma.itt.seip.expressions.ExpressionContext;
import com.sirma.itt.seip.expressions.ExpressionContextProperties;
import com.sirma.itt.seip.expressions.ExpressionsManager;
import com.sirma.itt.seip.instance.dao.InstanceDao;
import com.sirma.itt.seip.instance.dao.ServiceRegistry;
import com.sirma.itt.seip.instance.event.InstanceChangeEvent;
import com.sirma.itt.seip.instance.event.InstanceOpenEvent;
import com.sirma.itt.seip.instance.properties.PropertiesService;
import com.sirma.itt.seip.security.UserPreferences;

/**
 * Service for generating and working with instance headers. Additionally there is an observer method to listen for
 * {@link InstanceChangeEvent} to generate headers on instance modification. NOTE: The observer does not generate
 * tooltip header.
 *
 * @author BBonev
 */
@ApplicationScoped
public class HeadersService {

	/** The expressions manager. */
	@Inject
	private ExpressionsManager expressionsManager;
	/** The dictionary service. */
	@Inject
	private DictionaryService dictionaryService;
	/** The label provider. */
	@Inject
	private LabelProvider labelProvider;
	@Inject
	private UserPreferences userPreferences;
	/** The properties service. */
	@Inject
	private PropertiesService propertiesService;
	/** The service register. */
	@Inject
	private ServiceRegistry serviceRegistry;

	/** The header properties. we does not update the tooltip header */
	private static final String[] HEADER_PROPERTIES = CollectionUtils.toArray(DefaultProperties.HEADERS, String.class);

	void generateHeadersOnInstanceOpen(@Observes InstanceOpenEvent<? extends Instance> event) {
		Instance instance = event.getInstance();
		generateInstanceHeadersIfNeeded(instance);
	}

	/**
	 * Generate instance headers if needed.
	 *
	 * @param instance
	 *            the instance
	 */
	public void generateInstanceHeadersIfNeeded(Instance instance) {
		if (!isHeadersPresent(instance)) {
			generateInstanceHeaders(instance, false);
		}
	}

	private static boolean isHeadersPresent(Instance instance) {
		for (String header : HEADER_PROPERTIES) {
			if (instance.getProperties().containsKey(header)) {
				// if any of the headers is present no need to continue
				return true;
			}
		}
		return false;
	}

	/**
	 * Generate default headers for an instance. The method could save the generated headers automatically when
	 * generated if the second argument is <code>true</code>. The generated headers are set in the instance properties.
	 * The evaluated headers will be evaluated only once.
	 *
	 * @param instance
	 *            the instance
	 * @param persist
	 *            to persist to persist the instance after generation or not
	 */
	public void generateInstanceHeaders(Instance instance, boolean persist) {
		evalInternal(instance, persist, HEADER_PROPERTIES);
	}

	/**
	 * Generates the given list of headers for the provided instance.The evaluated headers will be evaluated only once.
	 *
	 * @param instance
	 *            the instance
	 * @param persist
	 *            to persist to persist the instance after generation or not
	 * @param headers
	 *            the header fields to generate
	 */
	public void generateInstanceHeaders(Instance instance, boolean persist, String... headers) {
		evalInternal(instance, persist, headers);
	}

	/**
	 * Generates and returns the header associated with the given field for the provided instance. The return header
	 * will be fully evaluated.
	 *
	 * @param instance
	 *            the instance
	 * @param header
	 *            the header
	 * @return the generated header.
	 */
	public String generateInstanceHeader(Instance instance, String header) {
		if (instance == null || CollectionUtils.isEmpty(instance.getProperties())
				|| StringUtils.isNullOrEmpty(header)) {
			return null;
		}
		ExpressionContext context = createContext(instance);
		DefinitionModel model = dictionaryService.getInstanceDefinition(instance);
		if (model == null) {
			return null;
		}
		String evaluated = model
				.fieldsStream()
					.filter(field -> validateField(field, Collections.singleton(header)))
					.map(field -> evalLabel(field, instance, context))
					.findAny()
					.orElse(null);

		// the fields has a second expression
		if (StringUtils.isNotNullOrEmpty(evaluated) && expressionsManager.isExpression(evaluated)) {
			evaluated = expressionsManager.evaluateRule(evaluated, String.class, context, instance);
		}
		return evaluated;
	}

	/**
	 * Evaluates the header properties for the given instance.
	 *
	 * @param instance
	 *            the instance
	 * @param saveProperties
	 *            the save properties
	 * @param headers
	 *            the list of headers to update
	 */
	private void evalInternal(Instance instance, boolean saveProperties, String[] headers) {
		if (CollectionUtils.isEmpty(instance.getProperties()) || headers == null || headers.length == 0) {
			return;
		}
		DefinitionModel model = dictionaryService.getInstanceDefinition(instance);
		if (model == null) {
			// no model no headers
			return;
		}
		ExpressionContext context = createContext(instance);
		Set<String> headersToGenerate = new HashSet<>(Arrays.asList(headers));

		Map<String, String> generatedHeaders = model
				.fieldsStream()
					.filter(field -> validateField(field, headersToGenerate))
					.map(field -> new Pair<>(field.getIdentifier(), evalLabel(field, instance, context)))
					.filter(Pair.nonNullSecond())
					.collect(Pair.toMap());

		instance.addAllProperties(generatedHeaders);

		if (saveProperties) {
			InstanceDao dao = serviceRegistry.getInstanceDao(instance);
			if (!propertiesService.isModelSupported(instance) && dao != null) {
				dao.saveProperties(instance, false);
			} else {
				// if needed add only the new properties the DB
				propertiesService.saveProperties(instance, instance, generatedHeaders, true);
			}
		}
	}

	private static boolean validateField(PropertyDefinition field, Set<String> headersToGenerate) {
		return headersToGenerate.contains(field.getIdentifier()) && StringUtils.isNotNullOrEmpty(field.getLabelId());
	}

	/**
	 * This method is called when information about an HeaderFieldsUpdate which was previously requested using an
	 * asynchronous interface becomes available.
	 *
	 * @param instance
	 *            the instance
	 * @return the expression context
	 */
	private ExpressionContext createContext(Instance instance) {
		ExpressionContext context = new ExpressionContext();
		context.put(ExpressionContextProperties.CURRENT_INSTANCE, instance);
		context.put(ExpressionContextProperties.LANGUAGE, getUserLanguage());
		return context;
	}

	private String evalLabel(PropertyDefinition property, Instance instance, ExpressionContext context) {
		context.put(ExpressionContextProperties.TARGET_FIELD, (Serializable) property);
		String label = labelProvider.getLabel(property.getLabelId(), getUserLanguage());
		String value = expressionsManager.evaluateRule(label, String.class, context, instance);
		return value != null ? value : label;
	}

	/**
	 * Fetches the user language if present if not tries to fetch the system language.
	 *
	 * @return the user language
	 */
	private String getUserLanguage() {
		return userPreferences.getLanguage();
	}
}
