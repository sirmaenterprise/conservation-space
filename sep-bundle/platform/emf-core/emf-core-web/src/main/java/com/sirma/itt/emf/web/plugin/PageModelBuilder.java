package com.sirma.itt.emf.web.plugin;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import org.apache.log4j.Logger;

import com.sirma.itt.emf.configuration.Config;
import com.sirma.itt.emf.configuration.EmfConfigurationProperties;
import com.sirma.itt.emf.label.LabelProvider;
import com.sirma.itt.emf.plugin.Plugin;
import com.sirma.itt.emf.security.AuthenticationService;
import com.sirma.itt.emf.security.AuthorityService;
import com.sirma.itt.emf.web.application.EmfApplication;
import com.sirma.itt.emf.web.rest.FreemarkerProvider;

import freemarker.template.TemplateException;

/**
 * Class responsible for building page models for provided templates
 * 
 * @author bbonev
 */
@ApplicationScoped
public class PageModelBuilder {

	private static final Logger LOGGER = Logger.getLogger(PageModelBuilder.class);

	@Inject
	private FreemarkerProvider freemarkerProvider;

	@Inject
	private LabelProvider labelProvider;

	@Inject
	private AuthorityService authorityService;

	@Inject
	private AuthenticationService authenticationService;

	@Config(name = EmfConfigurationProperties.SYSTEM_DEFAULT_HOST_NAME)
	@Inject
	private String hostName;

	@Config(name = EmfConfigurationProperties.SYSTEM_DEFAULT_HOST_PORT)
	@Inject
	private String port;

	@Config(name = EmfConfigurationProperties.SYSTEM_DEFAULT_HOST_PROTOCOL, defaultValue = "http")
	@Inject
	private String protocol;

	@Inject
	private EmfApplication emfApplication;

	private String serverBaseUrl;

	private String serverBaseAndContext;

	/**
	 * Builds the template.
	 * *
	 * 
	 * @param model
	 *            the model
	 * @param templatePath
	 *            the template path
	 * @return the string
	 */
	public String buildTemplate(Map<String, Object> model, String templatePath) {
		String processedTemplated = "";
		try {
			processedTemplated = getFreemarkerProvider().processTemplateByFullPath(model,
					templatePath);
		} catch (IOException | TemplateException e) {
			if (LOGGER.isDebugEnabled()) {
				LOGGER.debug(e.getMessage(), e);
			}
		}
		return processedTemplated;
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
		String baseUrl = getServerBaseAndContext();
		Map<String, Object> model = new HashMap<String, Object>();
		// some prefix
		model.put("id", fragmentName);
		model.put("name", fragmentName);
		model.put("href", baseUrl + href);
		if (labelId != null) {
			String lbl = getLabelProvider().getValue(labelId);
			if (lbl != null) {
				model.put("label", lbl);
			}
		}
		if (icon != null) {
			model.put("icon", baseUrl + "/images/" + icon);
		}
		model.put("submenus", htmlPageFragments);
		if (isDropdown == null) {
			model.put("isSubMenu", Boolean.FALSE);
		} else {
			model.put("isSubMenu", isDropdown);
		}
		return model;
	}

	/**
	 * Iterates over <code>iterator's</code> element and collect their htmls.
	 * 
	 * @param iterator
	 *            the iterator
	 * @return the map
	 */
	public List<String> loadHtmlPageFragments(Iterator<? extends Plugin> iterator) {
		List<String> list = new ArrayList<String>();
		while (iterator.hasNext()) {
			Plugin nextElement = iterator.next();
			if (nextElement instanceof PageModel) {
				list.add(((PageModel) nextElement).getPageFragment());
			}
		}
		return list;
	}

	/**
	 * Gets the emf server base ur *
	 * 
	 * @return the emf server base url
	 */
	public String getEmfServerBaseUrl() {
		if (serverBaseUrl == null) {
			serverBaseUrl = new StringBuilder(protocol).append("://").append(hostName).append(":")
					.append(port).toString();
		}
		return serverBaseUrl;
	}

	/**
	 * Gets the server base URL and application context path attach *
	 * 
	 * @return the server base and context
	 */
	public String getServerBaseAndContext() {
		if (serverBaseAndContext == null) {
			serverBaseAndContext = getEmfServerBaseUrl() + getEmfContextPath();
		}
		return serverBaseAndContext;
	}

	/**
	 * Gets the freemarker provi * .
	 * 
	 * @return the freemarkerProvider
	 */
	protected FreemarkerProvider getFreemarkerProvider() {
		return freemarkerProvider;
	}

	/**
	 * Gets the label prov * r.
	 * 
	 * @return the labelProvider
	 */
	protected LabelProvider getLabelProvider() {
		return labelProvider;
	}

	/**
	 * Gets the authority se * ce.
	 * 
	 * @return the authorityService
	 */
	protected AuthorityService getAuthorityService() {
		return authorityService;
	}

	/**
	 * Gets the authentication s * ice.
	 * 
	 * @return the authenticationService
	 */
	protected AuthenticationService getAuthenticationService() {
		return authenticationService;
	}

	/**
	 * Gets the ho * name.
	 * 
	 * @return the hostName
	 */
	protected String getHostName() {
		return hostName;
	}

	/**
	 * Gets * port.
	 * 
	 * @return the port
	 */
	protected String getPort() {
		return port;
	}

	/**
	 * Gets the * otocol.
	 * 
	 * @return the protocol
	 */
	protected String getProtocol() {
		return protocol;
	}

	/**
	 * Gets the emf co * xt path.
	 * 
	 * @return emf context path.
	 */
	public String getEmfContextPath() {
		return emfApplication.getContextPath();
	}

}