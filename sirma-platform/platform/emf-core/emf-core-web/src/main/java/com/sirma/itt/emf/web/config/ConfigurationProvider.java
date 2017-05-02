package com.sirma.itt.emf.web.config;

import java.util.Map;

import javax.enterprise.context.ApplicationScoped;
import javax.faces.context.FacesContext;
import javax.inject.Inject;
import javax.inject.Named;

import com.sirma.itt.seip.configuration.SystemConfiguration;

/**
 * Provides quick access to configuration parameters and parameters declared in web.xml.
 *
 * @author svelikov
 */
@Named
@ApplicationScoped
public class ConfigurationProvider {
	/** Format used from the jquery datepicker. */
	public static final String DATE_FORMAT = "dateFormat";

	/** Format used from the date converter. */
	public static final String CONVERTER_DATE_FORMAT = "converterDateFormat";

	/** Format used from date converter. */
	public static final String CONVERTER_DATETIME_FORMAT = "converterDateTimeFormat";

	public static final String PROJECT_STAGE = "javax.faces.PROJECT_STAGE";

	public static final String SINGLE_LINE_FIELD_MAX_LENGTH = "singleLineFieldMaxLength";

	private Map<String, String> contextParams;

	@Inject
	private SystemConfiguration systemConfiguration;

	/**
	 * Getter for context-param by name from web.xml.
	 *
	 * @param name
	 *            The name of the context-param to be retrieved.
	 * @return context-param name if any.
	 */
	public String getContextParam(final String name) {
		return getFacesContext().getExternalContext().getInitParameter(name);
	}

	/**
	 * Getter for {@link FacesContext}.
	 *
	 * @return {@link FacesContext}
	 */
	private FacesContext getFacesContext() {
		return FacesContext.getCurrentInstance();
	}

	/**
	 * Gets the system language from the configuration.
	 *
	 * @return the language as string.
	 */
	public String getSystemLanguage() {
		return systemConfiguration.getSystemLanguage();
	}

	/**
	 * Provides access to context parameters map that contain values from web.xml.
	 *
	 * @return the contextParams
	 */
	public Map<String, String> getContextParams() {
		if (contextParams == null) {
			contextParams = getFacesContext().getExternalContext().getInitParameterMap();
		}
		return contextParams;
	}

}
