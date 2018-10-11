package com.sirma.itt.seip.permissions.rest;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;

import org.junit.Test;

import com.sirma.itt.seip.permissions.role.RoleIdentifier;

/**
 * Test for {@link PermissionsBodyReader}
 *
 * @author BBonev
 */
public class PermissionsBodyReaderTest {

	private static final String PERMISSIONS_RESQUEST = "{\n \"permissions\": [{\n \"id\": \"emf:managerUser\",\n \"special\": \"CONSUMER\"\n }],\n \"inheritedPermissionsEnabled\": true\n }";

	private PermissionsBodyReader reader = new PermissionsBodyReader();

	@Test
	public void testRead() throws Exception {
		Permissions permissions = reader.readFrom(null, null, null, null, null,
				new ByteArrayInputStream(PERMISSIONS_RESQUEST.getBytes(StandardCharsets.UTF_8)));

		assertNotNull(permissions);
		assertTrue(permissions.isInheritedPermissions());
		PermissionEntry permissionEntry = permissions.getForAuthority("emf:managerUser");
		assertNotNull(permissionEntry);

		assertEquals("CONSUMER", permissionEntry.getSpecial());
	}

	@Test
	public void testRead_emptyJson() throws Exception {
		Permissions permissions = reader.readFrom(null, null, null, null, null,
				new ByteArrayInputStream("{ }".getBytes(StandardCharsets.UTF_8)));

		assertNotNull(permissions);
		assertFalse(permissions.isInheritedPermissions());
		assertFalse(permissions.iterator().hasNext());
	}

	@Test
	public void testIsReadable() throws Exception {
		assertFalse(reader.isReadable(RoleIdentifier.class, null, null, null));
		assertTrue(reader.isReadable(Permissions.class, null, null, null));
	}
}
