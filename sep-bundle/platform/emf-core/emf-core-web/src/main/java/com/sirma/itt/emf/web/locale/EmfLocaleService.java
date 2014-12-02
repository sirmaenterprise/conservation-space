package com.sirma.itt.emf.web.locale;

import java.io.Serializable;
import java.util.Locale;

import javax.enterprise.context.SessionScoped;
import javax.faces.context.FacesContext;
import javax.inject.Inject;
import javax.inject.Named;

import com.sirma.itt.emf.configuration.Config;
import com.sirma.itt.emf.configuration.EmfConfigurationProperties;


/**
 * LocaleService implementation for EMF.
 *
 * @author svelikov
 */
@Named
@SessionScoped
public class EmfLocaleService implements Serializable, LocaleService {

	/**
	 * Comment for serialVersionUID.
	 */
	private static final long serialVersionUID = 4447542165741667061L;

	@Inject
	@Config(name = EmfConfigurationProperties.SYSTEM_LANGUAGE, defaultValue = "bg")
	private String language;

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void setDefaultLocale() {
		setLocale(language);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void changeLocale(String locale) {
		setLocale(locale);
	}

	/**
	 * Set new locale.
	 *
	 * @param locale
	 *            Locale string/
	 */
	private void setLocale(String locale) {
		FacesContext.getCurrentInstance().getViewRoot()
				.setLocale(new Locale(locale));
	}

	/**
	 * Getter method for language.
	 *
	 * @return the language
	 */
	public String getLanguage() {
		return language;
	}

	/**
	 * Setter method for language.
	 *
	 * @param language
	 *            the language to set
	 */
	public void setLanguage(String language) {
		this.language = language;
	}

}
