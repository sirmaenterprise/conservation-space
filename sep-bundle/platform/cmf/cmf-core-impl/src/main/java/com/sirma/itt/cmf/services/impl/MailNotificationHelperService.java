package com.sirma.itt.cmf.services.impl;

import javax.enterprise.context.ApplicationScoped;
import javax.faces.context.ExternalContext;
import javax.faces.context.FacesContext;
import javax.inject.Inject;

import com.sirma.itt.emf.codelist.CodelistService;
import com.sirma.itt.emf.configuration.Config;
import com.sirma.itt.emf.configuration.ConfigurationFactory;
import com.sirma.itt.emf.configuration.EmfConfigurationProperties;
import com.sirma.itt.emf.definition.DictionaryService;
import com.sirma.itt.emf.instance.model.Instance;
import com.sirma.itt.emf.label.LabelProvider;
import com.sirma.itt.emf.resources.ResourceService;
import com.sirma.itt.emf.resources.ResourceType;
import com.sirma.itt.emf.resources.model.Resource;
import com.sirma.itt.emf.security.context.SecurityContextManager;
import com.sirma.itt.emf.security.model.User;
import com.sirma.itt.emf.services.LinkProviderService;

/**
 * The MailNotificationHelperService is intended to wrap some functionality and injection needed by
 * the ftl engine to build and send notifications
 */
@ApplicationScoped
public class MailNotificationHelperService {

	/** The resource service. */
	@Inject
	private ResourceService resourceService;

	/** The label provider. */
	@Inject
	private LabelProvider labelProvider;

	/** The codelist service. */
	@Inject
	private CodelistService codelistService;

	/** The dictionary service. */
	@Inject
	private DictionaryService dictionaryService;

	/** The link provider service. */
	@Inject
	private LinkProviderService linkProviderService;

	@Inject
	@Config(name = EmfConfigurationProperties.SYSTEM_DEFAULT_HOST_NAME, defaultValue = "localhost")
	private String host;
	@Inject
	@Config(name = EmfConfigurationProperties.SYSTEM_DEFAULT_HOST_PORT, defaultValue = "8080")
	private String port;
	@Inject
	@Config(name = EmfConfigurationProperties.SYSTEM_DEFAULT_HOST_PROTOCOL, defaultValue = "http")
	private String protocol;

	@Inject
	private ConfigurationFactory configurationFactory;

	/**
	 * Gets the display name for a resource.
	 *
	 * @param user
	 *            the user. Could be the userId or an instance of {@link Resource}
	 * @return the display name or empty string if no information is available
	 */
	public String getDisplayName(Object user) {
		if (user instanceof Resource) {
			return ((Resource) user).getDisplayName();
		} else if (user instanceof String) {
			Resource resource = getResourceService()
					.getResource(user.toString(), ResourceType.USER);
			if (resource != null) {
				return resource.getDisplayName();
			}
		}
		return "";
	}

	/**
	 * Helper method to find the user language.
	 *
	 * @param user
	 *            is the object to get language for
	 * @return the user language or the system if could not be retrieved
	 */
	public String getUserLanguage(Object user) {
		if (user instanceof User) {
			return SecurityContextManager.getUserLanguage((Resource) user);
		} else if (user instanceof String) {
			Resource resource = getResourceService()
					.getResource(user.toString(), ResourceType.USER);
			if (resource != null) {
				return SecurityContextManager.getUserLanguage(resource);
			}
		}
		return SecurityContextManager.getSystemLanguage();
	}

	/**
	 * Builds the full uri.
	 *
	 * @param instance
	 *            the instance
	 * @return the string
	 */
	public String buildFullURI(Instance instance) {
		String buildLink = linkProviderService.buildLink(instance);

		if (FacesContext.getCurrentInstance() != null) {
			ExternalContext externalContext = FacesContext.getCurrentInstance()
					.getExternalContext();
			// HttpServletRequest request = (HttpServletRequest) externalContext.getRequest();
			return new StringBuilder(externalContext.getRequestScheme()).append("://")
					.append(externalContext.getRequestServerName()).append(":")
					.append(externalContext.getRequestServerPort()).append(buildLink).toString();
		}
		// fallback
		return new StringBuilder(protocol).append("://").append(host).append(":").append(port)
				.append(buildLink).toString();
	}

	/**
	 * Gets the resource service.
	 *
	 * @return the resourceService
	 */
	public ResourceService getResourceService() {
		return resourceService;
	}

	/**
	 * Gets the label provider.
	 *
	 * @return the labelProvider
	 */
	public LabelProvider getLabelProvider() {
		return labelProvider;
	}

	/**
	 * Gets the codelist service.
	 *
	 * @return the codelistService
	 */
	public CodelistService getCodelistService() {
		return codelistService;
	}

	/**
	 * Gets the dictionary service.
	 *
	 * @return the dictionaryService
	 */
	public DictionaryService getDictionaryService() {
		return dictionaryService;
	}

	/**
	 * Finds a property by given key from the {@link ConfigurationFactory}
	 *
	 * @param config
	 *            is the config key
	 * @return the property value or null
	 */
	public String getConfigProperty(String config) {
		return configurationFactory.getConfiguration(config);
	}

}
