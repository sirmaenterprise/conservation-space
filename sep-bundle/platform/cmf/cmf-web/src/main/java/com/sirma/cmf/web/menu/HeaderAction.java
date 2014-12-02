package com.sirma.cmf.web.menu;

import javax.inject.Inject;
import javax.inject.Named;

import com.sirma.itt.emf.configuration.Config;
import com.sirma.itt.emf.web.config.EmfWebConfigurationProperties;

/**
 * UI Header backing bean.
 * 
 * @author svelikov
 */
@Named
public class HeaderAction {

	@Inject
	@Config(name = EmfWebConfigurationProperties.APPLICATION_LOGO_IMAGE_PATH, defaultValue = "logo.png")
	private String logoImageName;

	@Inject
	@Config(name = EmfWebConfigurationProperties.APPLICATION_FAVICON_IMAGE_PATH, defaultValue = "images:favicon.png")
	private String faviconImageName;

	/**
	 * Getter method for logoImageName.
	 * 
	 * @return the logoImageName
	 */
	public String getLogoImageName() {
		return logoImageName;
	}

	/**
	 * Setter method for logoImageName.
	 * 
	 * @param logoImageName
	 *            the logoImageName to set
	 */
	public void setLogoImageName(String logoImageName) {
		this.logoImageName = logoImageName;
	}

	/**
	 * Getter method for faviconImageName.
	 * 
	 * @return the faviconImageName
	 */
	public String getFaviconImageName() {
		return faviconImageName;
	}

	/**
	 * Setter method for faviconImageName.
	 * 
	 * @param faviconImageName
	 *            the faviconImageName to set
	 */
	public void setFaviconImageName(String faviconImageName) {
		this.faviconImageName = faviconImageName;
	}

}
