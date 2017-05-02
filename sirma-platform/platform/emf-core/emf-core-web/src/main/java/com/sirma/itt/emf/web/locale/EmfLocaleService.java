package com.sirma.itt.emf.web.locale;

import java.io.Serializable;
import java.util.Locale;

import javax.enterprise.context.SessionScoped;
import javax.faces.context.FacesContext;
import javax.inject.Inject;
import javax.inject.Named;

import com.sirma.itt.seip.configuration.SystemConfiguration;

/**
 * LocaleService implementation for EMF.
 *
 * @author svelikov
 */
@Named
@SessionScoped
public class EmfLocaleService implements Serializable, LocaleService {

	private static final long serialVersionUID = 4447542165741667061L;

	@Inject
	private SystemConfiguration systemConfiguration;

	@Override
	public void setDefaultLocale() {
		setLocale(systemConfiguration.getSystemLanguage());
	}

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
		FacesContext.getCurrentInstance().getViewRoot().setLocale(new Locale(locale));
	}

	/**
	 * Getter method for language.
	 *
	 * @return the language
	 */
	public String getLanguage() {
		return systemConfiguration.getSystemLanguage();
	}

}
