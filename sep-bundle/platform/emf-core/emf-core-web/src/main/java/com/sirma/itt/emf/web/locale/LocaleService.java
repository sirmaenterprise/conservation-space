package com.sirma.itt.emf.web.locale;

/**
 * Locale service is in charge for changing the locale for the application.
 * 
 * @author svelikov
 */
public interface LocaleService {

	/**
	 * Sets the default locale.
	 */
	void setDefaultLocale();

	/**
	 * Changes the current locale.
	 * 
	 * @param locale
	 *            New locale.
	 */
	void changeLocale(String locale);

}
