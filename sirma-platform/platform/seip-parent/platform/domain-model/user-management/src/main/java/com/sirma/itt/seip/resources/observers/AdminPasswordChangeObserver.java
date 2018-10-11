package com.sirma.itt.seip.resources.observers;

import static com.sirma.itt.seip.util.EqualsHelper.nullSafeEquals;

import javax.enterprise.event.Observes;
import javax.inject.Inject;
import javax.inject.Singleton;

import com.sirma.itt.seip.configuration.db.Configuration;
import com.sirma.itt.seip.configuration.db.ConfigurationManagement;
import com.sirma.itt.seip.resources.event.UserPasswordChangeEvent;
import com.sirma.itt.seip.security.configuration.SecurityConfiguration;
import com.sirma.itt.seip.security.context.SecurityContext;

/**
 * When the admin user changes his password we are updating the configuration value as well to be in sync.
 *
 * @author <a href="mailto:borislav.bonev@sirma.bg">Borislav Bonev</a>
 * @since 11/10/2017
 */
@Singleton
class AdminPasswordChangeObserver {

	@Inject
	private ConfigurationManagement configurationManagement;
	@Inject
	private SecurityConfiguration securityConfiguration;
	@Inject
	private SecurityContext securityContext;

	/**
	 * Update admin password configuration when the admin changes his password
	 *
	 * @param event password change event trigger
	 */
	void onPasswordChange(@Observes UserPasswordChangeEvent event) {
		if (nullSafeEquals(event.getUsername(), securityConfiguration.getAdminUserName().getOrFail())) {
			Configuration newPasswordConfiguration = new Configuration(
					securityConfiguration.getAdminUserPassword().getName(),
					event.getNewPassword(), securityContext.getCurrentTenantId());
			configurationManagement.updateConfiguration(newPasswordConfiguration);
		}
	}
}
