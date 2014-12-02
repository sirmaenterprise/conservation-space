package com.sirma.itt.emf.instance;

import java.io.Serializable;
import java.util.Map;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.event.Observes;
import javax.inject.Inject;

import com.sirma.itt.commons.utils.string.StringUtils;
import com.sirma.itt.emf.definition.DictionaryService;
import com.sirma.itt.emf.definition.model.PropertyDefinition;
import com.sirma.itt.emf.evaluation.ExpressionContext;
import com.sirma.itt.emf.evaluation.ExpressionContextProperties;
import com.sirma.itt.emf.evaluation.ExpressionsManager;
import com.sirma.itt.emf.evaluation.el.ElExpressionParser;
import com.sirma.itt.emf.event.instance.InstanceChangeEvent;
import com.sirma.itt.emf.instance.dao.InstanceDao;
import com.sirma.itt.emf.instance.dao.ServiceRegister;
import com.sirma.itt.emf.instance.model.Instance;
import com.sirma.itt.emf.label.LabelProvider;
import com.sirma.itt.emf.properties.DefaultProperties;
import com.sirma.itt.emf.properties.PropertiesService;
import com.sirma.itt.emf.security.context.SecurityContextManager;
import com.sirma.itt.emf.security.model.User;
import com.sirma.itt.emf.util.CollectionUtils;

/**
 * Service for generating and working with instance headers. Additionally there is an observer
 * method to listen for {@link InstanceChangeEvent} to generate headers on instance modification.
 * NOTE: The observer does not generate tooltip header.
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
	/** The properties service. */
	@Inject
	private PropertiesService propertiesService;
	/** The service register. */
	@Inject
	private ServiceRegister serviceRegister;

	/** The header properties. we does not update the tooltip header */
	private static final String[] HEADER_PROPERTIES = DefaultProperties.HEADERS
			.toArray(new String[DefaultProperties.HEADERS.size()]);

	/**
	 * Generate default headers for an instance. The method could save the generated headers
	 * automatically when generated if the second argument is <code>true</code>. The generated
	 * headers are set in the instance properties. The evaluated headers will be evaluated only
	 * once.
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
	 * Generates the given list of headers for the provided instance.The evaluated headers will be
	 * evaluated only once.
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
	 * Generates and returns the header associated with the given field for the provided instance.
	 * The return header will be fully evaluated.
	 * 
	 * @param instance
	 *            the instance
	 * @param header
	 *            the header
	 * @return the generated header.
	 */
	public String generateInstanceHeader(Instance instance, String header) {
		ExpressionContext context = createContext(instance);
		String evaluated = evaluateLabel(instance, context, header);
		// the fields has a second expression
		if (StringUtils.isNotNullOrEmpty(evaluated)
				&& ((evaluated.charAt(0) != ElExpressionParser.DEFAULT_EXPRESSION_ID) || (evaluated
						.charAt(0) != ElExpressionParser.LAZY_EXPRESSION_ID))) {
			evaluated = expressionsManager.evaluateRule(evaluated, String.class, context, instance);
		}
		return evaluated;
	}

	/**
	 * The method is called on every change of an instance.
	 *
	 * @param event
	 *            the event
	 */
	public void onInstanceChange(@Observes InstanceChangeEvent<Instance> event) {
		Instance instance = event.getInstance();
		evalInternal(instance, false, HEADER_PROPERTIES);
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
		ExpressionContext context = createContext(instance);

		// collect header properties in the map
		Map<String, Serializable> properties = CollectionUtils.createLinkedHashMap(headers.length);
		for (String headerKey : headers) {
			String defaultLabel = evaluateLabel(instance, context, headerKey);
			if (defaultLabel != null) {
				properties.put(headerKey, defaultLabel);
			}
		}
		// add the headers to the instance
		instance.getProperties().putAll(properties);
		if (saveProperties) {
			InstanceDao<Instance> dao = serviceRegister.getInstanceDao(instance);
			if (!propertiesService.isModelSupported(instance) && (dao != null)) {
				dao.saveProperties(instance, false);
			} else {
				// if needed add only the new properties the DB
				propertiesService.saveProperties(instance, instance.getRevision(), instance,
						properties, true);
			}
		}
	}

	/**
	 * This method is called when information about an HeaderFieldsUpdate which was previously
	 * requested using an asynchronous interface becomes available.
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

	/**
	 * Evaluates the label for the given property.
	 * 
	 * @param instance
	 *            the instance
	 * @param context
	 *            the context
	 * @param property
	 *            the property
	 * @return the label or <code>null</code> if not defined
	 */
	private String evaluateLabel(Instance instance, ExpressionContext context, String property) {
		PropertyDefinition definition = dictionaryService.getProperty(property,
				instance.getRevision(), instance);
		if (definition != null) {
			String labelId = definition.getLabelId();
			if (StringUtils.isNullOrEmpty(labelId)) {
				return null;
			}
			context.put(ExpressionContextProperties.TARGET_FIELD, (Serializable) definition);
			String label = labelProvider.getLabel(labelId, getUserLanguage());
			String value = expressionsManager.evaluateRule(label, String.class, context, instance);
			return value != null ? value : label;
		}
		return null;
	}

	/**
	 * Fetches the user language if present if not tries to fetch the system language.
	 *
	 * @return the user language
	 */
	private String getUserLanguage() {
		User authentication = SecurityContextManager.getFullAuthentication();
		String language = null;
		if (authentication != null) {
			language = authentication.getLanguage();
		}
		if (language == null) {
			language = (SecurityContextManager.getAdminUser()).getLanguage();
		}
		return language;
	}
}
