package com.sirma.itt.emf.web.plugin;

import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.inject.Inject;

import com.sirma.itt.emf.label.LabelProvider;
import com.sirma.itt.emf.plugin.Plugin;
import com.sirma.itt.emf.security.AuthenticationService;
import com.sirma.itt.emf.security.AuthorityService;
import com.sirma.itt.emf.web.rest.FreemarkerProvider;

/**
 * The Class AbstractPageModel.
 */
public abstract class AbstractPageModel implements PageModel {

	@Inject
	protected PageModelBuilder modelBuilder;

	/**
	 * Builds the template.
	 *
	 * @param model
	 *            the model
	 * @param templatePath
	 *            the template path
	 * @return the string
	 */
	public String buildTemplate(Map<String, Object> model, String templatePath) {
		return modelBuilder.buildTemplate(model, templatePath);
	}

	/**
	 * Creates the model.
	 *
	 * @param fragmentName
	 *            the fragment name
	 * @param href
	 *            the href
	 * @param labelId
	 *            the label
	 * @param icon
	 *            the icon
	 * @param htmlPageFragments
	 *            the html page fragments
	 * @param isDropdown
	 *            the is dropdown
	 * @return the map
	 */
	public Map<String, Object> createModel(Object fragmentName, String href, String labelId,
			String icon, List<String> htmlPageFragments, Boolean isDropdown) {
		return modelBuilder.createModel(fragmentName, href, labelId, icon, htmlPageFragments,
				isDropdown);
	}

	/**
	 * Iterates over <code>iterator's</code> element and collect their htmls.
	 *
	 * @param iterator
	 *            the iterator
	 * @return the map
	 */
	public List<String> loadHtmlPageFragments(Iterator<? extends Plugin> iterator) {
		return modelBuilder.loadHtmlPageFragments(iterator);
	}

	/**
	 * Gets the emf server base url.
	 *
	 * @return the emf server base url
	 */
	protected String getEmfServerBaseUrl() {
		return modelBuilder.getEmfServerBaseUrl();
	}

	/**
	 * Gets the server base URL and application context path attached.
	 *
	 * @return the server base and context
	 */
	protected String getServerBaseAndContext() {
		return modelBuilder.getServerBaseAndContext();
	}

	/**
	 * Gets the freemarker provider.
	 *
	 * @return the freemarkerProvider
	 */
	protected FreemarkerProvider getFreemarkerProvider() {
		return modelBuilder.getFreemarkerProvider();
	}

	/**
	 * Gets the label provider.
	 *
	 * @return the labelProvider
	 */
	protected LabelProvider getLabelProvider() {
		return modelBuilder.getLabelProvider();
	}

	/**
	 * Gets the authority service.
	 *
	 * @return the authorityService
	 */
	protected AuthorityService getAuthorityService() {
		return modelBuilder.getAuthorityService();
	}

	/**
	 * Gets the authentication service.
	 *
	 * @return the authenticationService
	 */
	protected AuthenticationService getAuthenticationService() {
		return modelBuilder.getAuthenticationService();
	}

	/**
	 * Gets the host name.
	 *
	 * @return the hostName
	 */
	protected String getHostName() {
		return modelBuilder.getHostName();
	}

	/**
	 * Gets the port.
	 *
	 * @return the port
	 */
	protected String getPort() {
		return modelBuilder.getPort();
	}

	/**
	 * Gets the protocol.
	 *
	 * @return the protocol
	 */
	protected String getProtocol() {
		return modelBuilder.getProtocol();
	}

	/**
	 * Gets the emf context path.
	 *
	 * @return emf context path.
	 */
	protected String getEmfContextPath() {
		return modelBuilder.getEmfContextPath();
	}
}
