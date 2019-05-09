package com.sirma.sep.keycloak.tenant;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.when;

import java.util.Map;
import java.util.function.Consumer;

import org.junit.Before;
import org.junit.Test;
import org.keycloak.admin.client.resource.RealmResource;
import org.keycloak.representations.idm.RealmRepresentation;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import com.sirma.itt.seip.mail.MailConfiguration;
import com.sirma.itt.seip.mail.MessageSender;
import com.sirma.sep.keycloak.producers.KeycloakClientProducer;

/**
 * Tests for {@link KeycloakMailConfigurator}.
 */
public class KeycloakMailConfiguratorTest {

	@InjectMocks
	private KeycloakMailConfigurator mailConfigurator;

	@Mock
	private MessageSender messageSender;

	@Mock
	private KeycloakClientProducer clientProducer;

	@Mock
	private RealmResource realmResource;

	@Before
	public void beforeEach() {
		MockitoAnnotations.initMocks(this);
	}

	@Test
	public void should_HandleConfigChange() {
		mockListenerRegister(buildDefaultMailConfiguration());

		RealmRepresentation realmRepresentation = mockRealmResource();

		mailConfigurator.registerChangeListener();

		verifySmtpSettings(realmRepresentation);
	}

	@Test
	public void should_CorrectlyConfigureAuthentication_When_UsernameSet() {
		MailConfiguration mailConfiguration = MailConfiguration.createSMTPConfiguration();
		mailConfiguration.setServerHost("testHost");
		mailConfiguration.setServerPort(1080);
		mailConfiguration.setServerFrom("no-reply@sirma.bg");
		mailConfiguration.enableSSL(true);
		mailConfiguration.enableTLS(true);
		mailConfiguration.setProperty(MailConfiguration.USERNAME, "testUser");
		mailConfiguration.setProperty(MailConfiguration.PASSWORD, "test123");

		mockListenerRegister(mailConfiguration);

		RealmRepresentation realmRepresentation = mockRealmResource();

		mailConfigurator.registerChangeListener();

		Map<String, String> smtpServer = realmRepresentation.getSmtpServer();
		assertEquals("testHost", smtpServer.get(KeycloakMailConfigurator.HOST));
		assertEquals("1080", smtpServer.get(KeycloakMailConfigurator.PORT));
		assertEquals("no-reply@sirma.bg", smtpServer.get(KeycloakMailConfigurator.FROM));
		assertEquals("true", smtpServer.get(KeycloakMailConfigurator.ENABLE_SSL));
		assertEquals("true", smtpServer.get(KeycloakMailConfigurator.ENABLE_TLS));
		assertEquals("testUser", smtpServer.get(KeycloakMailConfigurator.USERNAME));
		assertEquals("test123", smtpServer.get(KeycloakMailConfigurator.PASSWORD));
		assertEquals("true", smtpServer.get(KeycloakMailConfigurator.AUTH));
	}

	@Test
	public void should_BuildSmtpConfigs() {
		when(messageSender.getConfiguration()).thenReturn(buildDefaultMailConfiguration());

		RealmRepresentation realmRepresentation = new RealmRepresentation();

		mailConfigurator.configureSmtpServer(realmRepresentation);

		verifySmtpSettings(realmRepresentation);
	}

	private void verifySmtpSettings(RealmRepresentation realmRepresentation) {
		Map<String, String> smtpServer = realmRepresentation.getSmtpServer();
		assertEquals("testHost", smtpServer.get(KeycloakMailConfigurator.HOST));
		assertEquals("5000", smtpServer.get(KeycloakMailConfigurator.PORT));
		assertEquals("no-reply@sirma.bg", smtpServer.get(KeycloakMailConfigurator.FROM));
		assertEquals("false", smtpServer.get(KeycloakMailConfigurator.ENABLE_SSL));
		assertEquals("false", smtpServer.get(KeycloakMailConfigurator.ENABLE_TLS));
		assertFalse(smtpServer.containsKey(KeycloakMailConfigurator.AUTH));
		assertFalse(smtpServer.containsKey(KeycloakMailConfigurator.USERNAME));
		assertFalse(smtpServer.containsKey(KeycloakMailConfigurator.PASSWORD));
	}

	private MailConfiguration buildDefaultMailConfiguration() {
		MailConfiguration mailConfiguration = MailConfiguration.createSMTPConfiguration();
		mailConfiguration.setServerHost("testHost");
		mailConfiguration.setServerPort(5000);
		mailConfiguration.setServerFrom("no-reply@sirma.bg");
		mailConfiguration.enableSSL(false);
		mailConfiguration.enableTLS(false);
		return mailConfiguration;
	}

	private RealmRepresentation mockRealmResource() {
		RealmRepresentation realmRepresentation = new RealmRepresentation();
		when(realmResource.toRepresentation()).thenReturn(realmRepresentation);
		when(clientProducer.produceRealmResource()).thenReturn(realmResource);
		return realmRepresentation;
	}

	private void mockListenerRegister(MailConfiguration mailConfiguration) {
		doAnswer(invocation -> {
			Consumer consumer = invocation.getArgumentAt(0, Consumer.class);
			consumer.accept(mailConfiguration);
			return null;
		}).when(messageSender).addMailConfigurationChangeListener(any());
	}

}
