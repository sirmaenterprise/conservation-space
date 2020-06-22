package com.sirma.itt.seip.eai.content.tool.params;

import java.lang.invoke.MethodHandles;
import java.lang.reflect.Field;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sirma.itt.seip.eai.content.tool.exception.EAIRuntimeException;

import javafx.application.Application.Parameters;

/**
 * The {@link ParametersProvider} is wrapper for dynamic javafx parameters provided through JNLP.
 *
 * @author bbanchev
 */
public class ParametersProvider {
	private static final Logger LOGGER = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());
	/** The spreadsheet uri. */
	public static final String PARAM_CONTENT_URI = "uri";
	/** The server api url. */
	public static final String PARAM_API_URL = "apiUrl";
	/** The server api authorization header. */
	public static final String PARAM_AUTHORIZATION = "authorization";

	private static Map<String, String> parameters;

	private ParametersProvider() {
		// utility class
	}

	/**
	 * Sets the parameters.
	 *
	 * @param parameters
	 *            the parameters
	 */
	public static void setParameters(Parameters parameters) {
		LOGGER.debug("Setting runtime parameters - named: '{}'", parameters.getNamed());
		validateParemeters(parameters);
		ParametersProvider.parameters = parameters.getNamed();
	}

	/**
	 * Gets the parameters.
	 *
	 * @return the parameters
	 */
	public static Map<String, String> getParameters() {
		return parameters;
	}

	/**
	 * Validates the required parameters - all constant fields.
	 *
	 * @param source
	 *            the provided parameters
	 */
	private static void validateParemeters(Parameters source) {
		Field[] fields = ParametersProvider.class.getDeclaredFields();
		for (Field field : fields) {
			if (!field.getType().isAssignableFrom(String.class)) {
				continue;
			}
			StringBuilder errors = new StringBuilder();
			try {
				String key = field.get(ParametersProvider.class).toString();
				if (key == null || source.getNamed().get(key) == null) {
					errors.append("Missing parameter key: ").append(key).append("\n");
				}
			} catch (Exception e) {// NOSONAR
				errors.append("Could not access system data: ").append(e.getMessage()).append("\n");
			}
			if (errors.length() > 0) {
				throw new EAIRuntimeException(errors.toString());
			}
		}
	}

	/**
	 * Gets a parameter by key
	 *
	 * @param key
	 *            the key value to get
	 * @return the value for key - might be null
	 */
	public static String get(String key) {
		return getParameters().get(key);
	}

}
