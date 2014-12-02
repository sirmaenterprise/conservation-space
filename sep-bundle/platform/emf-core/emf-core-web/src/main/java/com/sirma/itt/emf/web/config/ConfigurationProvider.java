package com.sirma.itt.emf.web.config;

import java.util.Map;

import javax.enterprise.context.ApplicationScoped;
import javax.faces.context.FacesContext;
import javax.inject.Inject;
import javax.inject.Named;

import com.sirma.itt.emf.configuration.SystemConfiguration;

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
	private SystemConfiguration configurationFactory;

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
	 * Getter for properties from config.
	 * 
	 * @param name
	 *            The property to get
	 * @return the found config or null
	 */
	public String getConfigParam(final String name) {
		return configurationFactory.getConfiguration(name);
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
	 * Provides access to context parameters map that contain values from web.xml.
	 * 
	 * @return the contextParams
	 */
	@SuppressWarnings("unchecked")
	public Map<String, String> getContextParams() {
		if (contextParams == null) {
			contextParams = getFacesContext().getExternalContext().getInitParameterMap();
		}
		return contextParams;
	}

}
