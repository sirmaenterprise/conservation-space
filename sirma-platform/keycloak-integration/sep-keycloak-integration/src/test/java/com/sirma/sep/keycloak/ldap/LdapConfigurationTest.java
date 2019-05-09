package com.sirma.sep.keycloak.ldap;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.junit.Test;
import org.keycloak.representations.idm.ComponentRepresentation;

import com.sirma.itt.seip.configuration.convert.GroupConverterContext;

/**
 * Tests for {@link LdapConfiguration}.
 *
 * @author smustafov
 */
public class LdapConfigurationTest {

	@Test
	public void should_BuildLdapComponent() {
		ComponentRepresentation ldapComponent = LdapConfiguration.buildLdapComponent(mockContext());

		assertEquals(LdapConstants.LDAP_PROVIDER_ID, ldapComponent.getName());
		assertEquals(LdapConstants.LDAP_PROVIDER_ID, ldapComponent.getProviderId());
		assertEquals(LdapConstants.LDAP_PROVIDER_TYPE, ldapComponent.getProviderType());

		assertFalse(ldapComponent.getConfig().isEmpty());
		assertEquals("true", ldapComponent.getConfig().getFirst("enabled"));
	}

	@Test
	public void should_BuildGroupMapperComponent() {
		ComponentRepresentation groupMapperComponent = LdapConfiguration.buildGroupMapperComponent(mockContext());

		assertEquals(LdapConstants.GROUP_MAPPER_ID, groupMapperComponent.getName());
		assertEquals(LdapConstants.GROUP_MAPPER_ID, groupMapperComponent.getProviderId());
		assertEquals(LdapConstants.LDAP_MAPPER_PROVIDER_TYPE, groupMapperComponent.getProviderType());

		assertFalse(groupMapperComponent.getConfig().isEmpty());
		assertEquals("LDAP_ONLY", groupMapperComponent.getConfig().getFirst(LdapConstants.GROUPS_MODE));
	}

	private GroupConverterContext mockContext() {
		GroupConverterContext context = mock(GroupConverterContext.class);
		when(context.get(anyString())).thenReturn("conf");
		when(context.get(LdapConfiguration.LDAP_EDIT_MODE_NAME)).thenReturn(LdapMode.WRITABLE);
		return context;
	}
}
