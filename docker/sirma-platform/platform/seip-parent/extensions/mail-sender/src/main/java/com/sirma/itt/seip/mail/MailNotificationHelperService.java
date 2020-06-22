package com.sirma.itt.seip.mail;

import java.io.Serializable;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sirma.itt.seip.configuration.SystemConfiguration;
import com.sirma.itt.seip.domain.instance.Instance;
import com.sirma.itt.seip.instance.util.LinkProviderService;
import com.sirma.itt.seip.resources.Resource;
import com.sirma.itt.seip.resources.ResourceService;
import com.sirma.itt.seip.security.User;
import com.sirma.itt.seip.security.UserPreferences;

/**
 * The MailNotificationHelperService is intended to wrap some functionality and injection needed by the ftl engine to
 * build and send notifications.
 *
 * @author A. Kunchev
 */
@ApplicationScoped
public class MailNotificationHelperService {

	private static final Logger LOGGER = LoggerFactory.getLogger(MailNotificationHelperService.class);

	@Inject
	private ResourceService resourceService;

	@Inject
	private UserPreferences userPreferences;

	@Inject
	private SystemConfiguration systemConfiguration;

	@Inject
	private javax.enterprise.inject.Instance<LinkProviderService> linkProviderService;

	/**
	 * Builds the full uri.
	 *
	 * @param instance
	 * 		the instance
	 * @return the string
	 */
	public String buildFullURI(Instance instance) {
		if (linkProviderService.isUnsatisfied() || instance == null) {
			LOGGER.warn("No link provider installed. Cannot generate instance link!");
			return "";
		}

		return systemConfiguration.getUi2Url().get() + linkProviderService.get().buildLink(instance);
	}

	/**
	 * Gets the display name for a resource.
	 *
	 * @param user
	 * 		the user. Could be the userId or an instance of {@link Resource}
	 * @return the display name or empty string if no information is available
	 */
	public String getDisplayName(Object user) {
		if (user instanceof Resource) {
			return ((Resource) user).getDisplayName();
		} else if (user instanceof Serializable) {
			Resource resource = resourceService.findResource((Serializable) user);
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
	 * 		is the object to get language for
	 * @return the user language or the system if could not be retrieved
	 */
	public String getUserLanguage(Object user) {
		if (user instanceof User) {
			return userPreferences.getLanguage((User) user);
		} else if (user instanceof String) {
			Resource resource = resourceService.findResource((Serializable) user);
			if (resource instanceof User) {
				return userPreferences.getLanguage((User) resource);
			}
		}
		return systemConfiguration.getSystemLanguage();
	}

	/**
	 * Returns the URL address of the UI.
	 *
	 * @return URL address of the UI
	 */
	public String getUi2Url() {
		return systemConfiguration.getUi2Url().get();
	}

	/**
	 * Returns the configured application brand name.
	 *
	 * @return brand application name
	 */
	public String getApplicationName() {
		return systemConfiguration.getApplicationName().get();
	}

}
