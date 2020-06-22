package com.sirma.sep.keycloak.login;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;

import org.junit.Test;
import org.keycloak.models.UserCredentialModel;

/**
 * Tests for {@link SepRealmBasedAuthenticatorFactory}.
 *
 * @author smustafov
 */
public class SepRealmBasedAuthenticatorFactoryTest {

	private SepRealmBasedAuthenticatorFactory factory = new SepRealmBasedAuthenticatorFactory();

	@Test
	public void should_HaveSingleInstanceOfAuthenticator() {
		assertEquals(factory.create(null), factory.create(null));
		assertEquals(factory.create(null), new SepRealmBasedAuthenticatorFactory().create(null));
	}

	@Test
	public void should_HaveId() {
		assertEquals(SepRealmBasedAuthenticatorFactory.AUTHENTICATOR_ID, factory.getId());
	}

	@Test
	public void should_HaveDisplayType() {
		assertEquals(SepRealmBasedAuthenticatorFactory.DISPLAY_TYPE, factory.getDisplayType());
	}

	@Test
	public void should_HaveHelpText() {
		assertEquals(SepRealmBasedAuthenticatorFactory.HELP_TEXT, factory.getHelpText());
	}

	@Test
	public void should_HaveTwo_RequirementChoices() {
		assertArrayEquals(SepRealmBasedAuthenticatorFactory.REQUIREMENT_CHOICES, factory.getRequirementChoices());
	}

	@Test
	public void should_HavePassword_For_ReferenceCategory() {
		assertEquals(UserCredentialModel.PASSWORD, factory.getReferenceCategory());
	}

	@Test
	public void should_NotBeConfigurable() {
		assertFalse("The authenticator should not be configurable", factory.isConfigurable());
	}

	@Test
	public void should_NotAllowUserSetup() {
		assertFalse("The authenticator should not allow user setup", factory.isUserSetupAllowed());
	}

}
