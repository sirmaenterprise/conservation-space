package com.sirma.itt.seip.permissions.rest;

import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.when;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.List;

import org.apache.commons.io.IOUtils;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

import com.sirma.itt.seip.domain.instance.InstanceReference;
import com.sirma.itt.seip.domain.instance.InstanceType;
import com.sirma.itt.seip.permissions.SecurityModel;
import com.sirma.itt.seip.permissions.role.ResourceRole;
import com.sirma.itt.seip.resources.EmfGroup;
import com.sirma.itt.seip.resources.EmfUser;
import com.sirma.itt.seip.resources.ResourceProperties;
import com.sirma.itt.seip.resources.ResourceService;
import com.sirma.itt.seip.testutil.mocks.InstanceReferenceMock;

import net.javacrumbs.jsonunit.JsonAssert;

/**
 * Test for {@link PermissionsBodyWriter}
 *
 * @author BBonev
 */
public class PermissionsBodyWriterTest {

	@InjectMocks
	private PermissionsBodyWriter writer;

	@Mock
	private ResourceService resourceService;

	@Before
	public void init() {
		MockitoAnnotations.initMocks(this);

		EmfUser resource = new EmfUser("testUser");
		resource.setId("emf:testUser");
		resource.setDisplayName("Test User");
		resource.add(ResourceProperties.AVATAR, "avatarLocation");
		when(resourceService.loadByDbId(eq("emf:testUser"))).thenReturn(resource);

		EmfUser managerResource = new EmfUser("testUser");
		managerResource.setId("emf:managerUser");
		managerResource.setDisplayName("Test User");
		when(resourceService.loadByDbId(eq("emf:managerUser"))).thenReturn(managerResource);

		EmfGroup group = new EmfGroup("testGroup", "Test GROUP");
		group.setId("emf:testGroup");
		when(resourceService.loadByDbId(eq("emf:testGroup"))).thenReturn(group);
	}

	@Test
	public void writePermissions() throws Exception {
		Permissions permissions = buildPermissionsData();
		ByteArrayOutputStream entityStream = new ByteArrayOutputStream();
		writer.writeTo(permissions, null, null, null, null, null, entityStream);

		Object actual = new String(entityStream.toByteArray(), StandardCharsets.UTF_8);
		JsonAssert.assertJsonEquals(loadData(), actual);
	}

	private static Object loadData() throws IOException {
		try (InputStream stream = PermissionsBodyWriterTest.class
				.getClassLoader()
					.getResourceAsStream("permissions/permissions-v2.json")) {
			return IOUtils.toString(stream, StandardCharsets.UTF_8);
		}
	}

	private static Permissions buildPermissionsData() {
		Permissions permissions = new Permissions();
		permissions.setEditAllowed(true);
		permissions.setInheritedPermissions(true);
		permissions.setRoot(true);
		permissions.setRestoreAllowed(true);
		permissions.setAllowInheritLibraryPermissions(true);
		permissions.setAllowInheritParentPermissions(true);

		ResourceRole consumer = new ResourceRole();
		consumer.setRole(SecurityModel.BaseRoles.CONSUMER);
		consumer.setAuthorityId("emf:testUser");

		ResourceRole manager = new ResourceRole();
		manager.setRole(SecurityModel.BaseRoles.MANAGER);
		manager.setAuthorityId("emf:managerUser");

		ResourceRole contributors = new ResourceRole();
		contributors.setRole(SecurityModel.BaseRoles.CONTRIBUTOR);
		contributors.setAuthorityId("emf:testGroup");

		ResourceRole removed = new ResourceRole();
		removed.setRole(SecurityModel.BaseRoles.CONTRIBUTOR);

		List<ResourceRole> roles = Arrays.asList(consumer, contributors, removed);

		permissions.addRolesWithInteritedPermissions(roles);
		permissions.addRolesWithLibraryPermissions(roles);
		permissions.addRolesWithSpecialPermissions(roles);
		permissions.addRolesWithCalculatedPermissions(roles);
		permissions.addRolesWithManagerPermissions(Arrays.asList(manager));

		InstanceReference reference = new InstanceReferenceMock();
		InstanceType instanceType = Mockito.mock(InstanceType.class);
		Mockito.when(instanceType.is(anyString())).thenReturn(Boolean.TRUE);
		reference.setType(instanceType);
		permissions.setReference(reference);

		return permissions;
	}

}
