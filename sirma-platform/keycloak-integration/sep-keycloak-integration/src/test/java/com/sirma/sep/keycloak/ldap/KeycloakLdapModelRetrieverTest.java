package com.sirma.sep.keycloak.ldap;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;

import java.util.List;

import org.junit.Test;
import org.keycloak.representations.idm.ComponentRepresentation;

/**
 * Tests for {@link KeycloakLdapModelRetriever}.
 *
 * @author smustafov
 */
public class KeycloakLdapModelRetrieverTest {

	private KeycloakLdapModelRetriever modelRetriever = new KeycloakLdapModelRetriever();

	@Test
	public void should_RetrieveDefaultModels() {
		List<ComponentRepresentation> models = modelRetriever.retrieve("testLdap");

		assertEquals(11, models.size());
		for (ComponentRepresentation model : models) {
			assertEquals("testLdap", model.getParentId());
			assertFalse(model.getConfig().isEmpty());
		}
	}

}
