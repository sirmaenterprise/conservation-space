package com.sirma.itt.emf.web.plugin;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import org.apache.log4j.Logger;

import com.sirma.itt.emf.web.application.ApplicationConfigurationProvider;
import com.sirma.itt.emf.web.rest.FreemarkerProvider;
import com.sirma.itt.seip.configuration.SystemConfiguration;
import com.sirma.itt.seip.domain.definition.label.LabelProvider;
import com.sirma.itt.seip.permissions.action.AuthorityService;
import com.sirma.itt.seip.plugin.Plugin;
import com.sirma.itt.seip.security.User;
import com.sirma.itt.seip.security.context.SecurityContext;

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
	private SecurityContext securityContext;

	@Inject
	private AuthorityService authorityService;

	@Inject
	private ApplicationConfigurationProvider emfApplication;

	@Inject
	private SystemConfiguration systemConfiguration;

	private String serverBaseAndContext;

	/**
	 * Builds the template. *
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
			processedTemplated = getFreemarkerProvider().processTemplateByFullPath(model, templatePath);
		} catch (TemplateException e) {
			LOGGER.debug(e.getMessage(), e);
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
	public Map<String, Object> createModel(Object fragmentName, String href, String labelId, String icon,
			List<String> htmlPageFragments, Boolean isDropdown) {
		String baseUrl = getServerBaseAndContext();
		Map<String, Object> model = new HashMap<>(9);
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
			model.put("icon", icon);
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
	@SuppressWarnings("static-method")
	public List<String> loadHtmlPageFragments(Iterator<? extends Plugin> iterator) {
		List<String> list = new ArrayList<>();
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
		return systemConfiguration.getSystemAccessUrl().get().toString();
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
	 * Gets the emf co * xt path.
	 *
	 * @return emf context path.
	 */
	public String getEmfContextPath() {
		return emfApplication.getContextPath();
	}

	/**
	 * Gets the current user.
	 *
	 * @return the current user
	 */
	protected User getCurrentUser() {
		return securityContext.getAuthenticated();
	}

}