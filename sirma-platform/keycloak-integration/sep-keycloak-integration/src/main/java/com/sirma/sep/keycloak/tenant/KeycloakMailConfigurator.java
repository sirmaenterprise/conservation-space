package com.sirma.sep.keycloak.tenant;

import java.util.HashMap;
import java.util.Map;

import javax.annotation.PostConstruct;
import javax.inject.Inject;
import javax.inject.Singleton;

import org.apache.commons.lang3.StringUtils;
import org.keycloak.admin.client.resource.RealmResource;
import org.keycloak.representations.idm.RealmRepresentation;

import com.sirma.itt.seip.mail.MailConfiguration;
import com.sirma.itt.seip.mail.MessageSender;
import com.sirma.sep.keycloak.producers.KeycloakClientProducer;

/**
 * Mail configurator for Keycloak. Uses platform's base mail configurations.
 *
 * @author smustafov
 */
@Singleton
public class KeycloakMailConfigurator {

	static final String HOST = "host";
	static final String PORT = "port";
	static final String FROM = "from";
	static final String ENABLE_SSL = "ssl";
	static final String ENABLE_TLS = "starttls";
	static final String AUTH = "auth";
	static final String USERNAME = "user";
	static final String PASSWORD = "password"; // NOSONAR

	@Inject
	private MessageSender messageSender;

	@Inject
	private KeycloakClientProducer clientProducer;

	/**
	 * Registers config change listener which is invoked when mail configuration is changed for a specific tenant.
	 */
	@PostConstruct
	void registerChangeListener() {
		messageSender.addMailConfigurationChangeListener(this::handleConfigChange);
	}

	private void handleConfigChange(MailConfiguration mailConfiguration) {
		RealmResource realmResource = clientProducer.produceRealmResource();

		RealmRepresentation realmRepresentation = realmResource.toRepresentation();
		buildSettings(realmRepresentation, mailConfiguration);

		realmResource.update(realmRepresentation);
	}

	/**
	 * Configures mail smtp settings into the provided {@link RealmRepresentation}.
	 *
	 * @param realm {@link RealmRepresentation} which to be populated with smtp settings
	 */
	public void configureSmtpServer(RealmRepresentation realm) {
		buildSettings(realm, messageSender.getConfiguration());
	}

	private static void buildSettings(RealmRepresentation realm, MailConfiguration mailConfiguration) {
		Map<String, String> settings = new HashMap<>();
		settings.put(HOST, mailConfiguration.getProperty(MailConfiguration.HOST));
		settings.put(PORT, mailConfiguration.getProperty(MailConfiguration.PORT));
		settings.put(FROM, mailConfiguration.getProperty(MailConfiguration.FROM));
		settings.put(ENABLE_SSL, mailConfiguration.getProperty(MailConfiguration.ENABLE_SSL));
		settings.put(ENABLE_TLS, mailConfiguration.getProperty(MailConfiguration.ENABLE_TLS));

		String username = mailConfiguration.getProperty(MailConfiguration.USERNAME);
		if (StringUtils.isNotBlank(username)) {
			settings.put(AUTH, Boolean.TRUE.toString());
			settings.put(USERNAME, username);
			settings.put(PASSWORD, mailConfiguration.getProperty(MailConfiguration.PASSWORD));
		}

		realm.setSmtpServer(settings);
	}

}
