package com.sirmaenterprise.sep.roles.rest;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.stream.Stream;

import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import com.sirma.itt.seip.domain.definition.label.LabelProvider;
import com.sirma.itt.seip.permissions.SecurityModel.BaseRoles;
import com.sirmaenterprise.sep.roles.RoleDefinition;
import com.sirmaenterprise.sep.roles.RoleManagement;

/**
 * Test for {@link RoleResource}
 *
 * @author BBonev
 */
public class RoleResourceTest {

	@InjectMocks
	private RoleResource roleResource;

	@Mock
	private RoleManagement roleManagement;

	@Mock
	private LabelProvider labelProvider;

	@Before
	public void beforeMethod() {
		MockitoAnnotations.initMocks(this);

		when(roleManagement.getRoles()).then(
				a -> Stream.of(new RoleDefinition(BaseRoles.ADMINISTRATOR), new RoleDefinition(BaseRoles.CONSUMER),
						new RoleDefinition(BaseRoles.COLLABORATOR), new RoleDefinition(BaseRoles.MANAGER)));
		when(labelProvider.getLabel(anyString())).then(a -> a.getArgumentAt(0, String.class));
	}

	@Test
	public void should_returnRolesSorted() throws Exception {
		List<RoleResponse> roles = roleResource.getRoles();

		assertEquals("Internal roles should not be returned", 3, roles.size());
		// roles are sorted by global id so this should be the first role
		RoleResponse role = roles.get(0);
		assertEquals("CONSUMER", role.getId());
		assertEquals("consumer.label", role.getLabel());
		assertEquals(5, role.getOrder());
		assertTrue(role.isCanRead());
		assertFalse(role.isCanWrite());
	}
}
