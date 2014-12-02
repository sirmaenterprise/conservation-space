package com.sirma.itt.emf.util;

import java.io.Serializable;
import java.util.Locale;

import javax.enterprise.context.SessionScoped;
import javax.enterprise.inject.Produces;
import javax.faces.context.FacesContext;


/**
 * Produces current locale and locale language from {@link FacesContext} or
 * {@link Locale#getDefault()} if not set
 * 
 * @author BBonev
 */
@SessionScoped
public class CurrentLocaleProducer implements Serializable {
	/**
	 * Comment for serialVersionUID.
	 */
	private static final long serialVersionUID = -1412792100927298260L;

	private Locale locale;

	/**
	 * Gets the current locale language.
	 * 
	 * @return the current locale language
	 */
	@Produces
	@CurrentLocale
	public String getCurrentLocaleLanguage() {
		if (locale == null) {
			locale = getLocale();
		}
		return locale.getLanguage();
	}

	/**
	 * Gets the current locale.
	 * 
	 * @return the current locale
	 */
	public Locale getCurrentLocale() {
		if (locale == null) {
			locale = getLocale();
		}
		return locale;
	}

	/**
	 * Gets the locale.
	 *
	 * @return the locale
	 */
	private Locale getLocale() {
		FacesContext context = FacesContext.getCurrentInstance();
		if ((context != null) && (context.getViewRoot() != null)) {
			Locale locale = context.getViewRoot().getLocale();
			if (locale != null) {
				return locale;
			}
		}
		return Locale.getDefault();
	}

}
