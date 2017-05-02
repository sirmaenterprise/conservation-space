package com.sirma.cmf.web.menu;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.inject.Named;

import com.sirma.itt.emf.web.application.ApplicationConfigurationProvider;

/**
 * UI Header backing bean.
 *
 * @author svelikov
 */
@Named
@ApplicationScoped
public class HeaderAction {

	@Inject
	private ApplicationConfigurationProvider configurationProvider;

	/**
	 * Getter method for logoImageName.
	 *
	 * @return the logoImageName
	 */
	public String getLogoImageName() {
		return configurationProvider.getLogoImageName();
	}

	/**
	 * Getter method for faviconImageName.
	 *
	 * @return the faviconImageName
	 */
	public String getFaviconImageName() {
		return configurationProvider.getFaviconImageName();
	}

}
