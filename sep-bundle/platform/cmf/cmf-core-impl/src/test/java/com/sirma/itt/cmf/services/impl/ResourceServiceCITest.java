package com.sirma.itt.cmf.services.impl;

import java.util.List;

import javax.inject.Inject;

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.testng.Assert;
import org.testng.annotations.Test;

import com.sirma.itt.cmf.testutil.BaseArquillianCITest;
import com.sirma.itt.cmf.testutil.CmfTestResourcePackager;
import com.sirma.itt.emf.resources.GroupService;
import com.sirma.itt.emf.resources.ResourceService;
import com.sirma.itt.emf.resources.ResourceType;
import com.sirma.itt.emf.resources.model.Resource;
import com.sirma.itt.emf.security.model.EmfUser;

/**
 * Test for resource service. All the base methods are currently included. Arquillian is used to
 * test the integration.
 *
 * @author bbanchev
 */
public class ResourceServiceCITest extends BaseArquillianCITest {
	@Inject
	private ResourceService resourceService;

	/**
	 * Creates the deployment.
	 *
	 * @return the web archive
	 */
	@Deployment
	public static WebArchive createDeployment() {
		System.out.println("Starting test: " + ResourceServiceCITest.class );
		return defaultBuilder(new CmfTestResourcePackager()).packageWar();
	}

	/**
	 * Test method for
	 * {@link com.sirma.itt.emf.resources.ResourceService#assignResource(com.sirma.itt.emf.resources.model.Resource, com.sirma.itt.emf.security.model.RoleIdentifier, com.sirma.itt.emf.instance.model.Instance)}
	 * .
	 */
	@Test(enabled = false, dependsOnMethods = "testGetContainedResources")
	public void testAssignResourceRRoleIdentifierInstance() throws Exception {
		throw new RuntimeException("not yet implemented");
	}

	/**
	 * Test method for
	 * {@link com.sirma.itt.emf.resources.ResourceService#assignResource(java.lang.String, com.sirma.itt.emf.resources.ResourceType, com.sirma.itt.emf.security.model.RoleIdentifier, com.sirma.itt.emf.instance.model.Instance)}
	 * .
	 */
	@Test(enabled = false)
	public void testAssignResourceStringResourceTypeRoleIdentifierInstance() throws Exception {
		throw new RuntimeException("not yet implemented");
	}

	/**
	 * Test method for
	 * {@link com.sirma.itt.emf.resources.ResourceService#assignResources(com.sirma.itt.emf.instance.model.Instance, java.util.Map)}
	 * .
	 */
	@Test(enabled = false)
	public void testAssignResources() throws Exception {
		throw new RuntimeException("not yet implemented");
	}

	/**
	 * Test method for
	 * {@link com.sirma.itt.emf.resources.ResourceService#setResources(java.util.Map, com.sirma.itt.emf.instance.model.Instance)}
	 * .
	 */
	@Test(enabled = false)
	public void testSetResources() throws Exception {
		throw new RuntimeException("not yet implemented");
	}

	/**
	 * Test method for
	 * {@link com.sirma.itt.emf.resources.ResourceService#saveResource(com.sirma.itt.emf.resources.model.Resource)}
	 * .
	 */
	@Test(enabled = false)
	public void testSaveResource() throws Exception {
		throw new RuntimeException("not yet implemented");
	}

	/**
	 * Test method for
	 * {@link com.sirma.itt.emf.resources.ResourceService#getResource(java.io.Serializable)}.
	 */
	@Test(enabled = true)
	public void testGetResourceSerializable() throws Exception {
		Resource resource = resourceService.getResource("admin");

		Assert.assertNull(resource, "The resource should be null");
		resource = resourceService.getResource("emf:admin");
		assertNotNull(resource, "Should be found by primary id");
	}

	/**
	 * Test method for
	 * {@link com.sirma.itt.emf.resources.ResourceService#getResource(java.lang.String, com.sirma.itt.emf.resources.ResourceType)}
	 * .
	 */
	@Test(enabled = true)
	public void testGetResourceStringResourceType() throws Exception {
		String id = "admin";
		ResourceType resType = ResourceType.USER;
		Resource resource = resourceService.getResource(id, resType);
		assertNotNull(resource, id + " " + resType + " must exist!");
		id = "Consumer";
		resource = resourceService.getResource(id, resType);
		assertNotNull(resource, id + " " + resType + " must exist!");

		id = "GROUP_Consumers";
		resType = ResourceType.GROUP;
		resource = resourceService.getResource(id, resType);
		assertNotNull(resource, id + " " + resType + " must exist!");

		id = "GROUP_ALFRESCO_ADMINISTRATORS";
		resource = resourceService.getResource(id, resType);
		assertNotNull(resource, id + " " + resType + " must exist!");
	}

	/**
	 * Test method for
	 * {@link com.sirma.itt.emf.resources.ResourceService#getResources(com.sirma.itt.emf.instance.model.Instance)}
	 * .
	 */
	@Test(enabled = false)
	public void testGetResourcesInstance() throws Exception {
		throw new RuntimeException("not yet implemented");
	}

	/**
	 * Test method for
	 * {@link com.sirma.itt.emf.resources.ResourceService#getResourcesByRole(com.sirma.itt.emf.instance.model.Instance, com.sirma.itt.emf.security.model.RoleIdentifier)}
	 * .
	 */
	@Test(enabled = false)
	public void testGetResourcesByRole() throws Exception {
		throw new RuntimeException("not yet implemented");
	}

	/**
	 * Test method for
	 * {@link com.sirma.itt.emf.resources.ResourceService#getResourceRoles(com.sirma.itt.emf.instance.model.Instance)}
	 * .
	 */
	@Test(enabled = false)
	public void testGetResourceRoles() throws Exception {
		throw new RuntimeException("not yet implemented");
	}

	/**
	 * Test method for
	 * {@link com.sirma.itt.emf.resources.ResourceService#getResourceRole(com.sirma.itt.emf.instance.model.Instance, java.lang.String, com.sirma.itt.emf.resources.ResourceType)}
	 * .
	 */
	@Test(enabled = false)
	public void testGetResourceRoleInstanceStringResourceType() throws Exception {
		throw new RuntimeException("not yet implemented");
	}

	/**
	 * Test method for
	 * {@link com.sirma.itt.emf.resources.ResourceService#getResourceRole(com.sirma.itt.emf.instance.model.Instance, com.sirma.itt.emf.resources.model.Resource)}
	 * .
	 */
	@Test(enabled = false)
	public void testGetResourceRoleInstanceResource() throws Exception {
		throw new RuntimeException("not yet implemented");
	}

	/**
	 * Test method for
	 * {@link com.sirma.itt.emf.resources.ResourceService#getAllResources(com.sirma.itt.emf.resources.ResourceType, java.lang.String)}
	 * .
	 */
	@Test(enabled = true)
	public void testGetAllResources() throws Exception {

		List<Resource> allResources = resourceService.getAllResources(ResourceType.GROUP, null);
		assertEquals(allResources.size(), 31, "31 test groups");

		allResources = resourceService.getAllResources(ResourceType.GROUP,
				GroupService.GroupSorter.GROUP_NAME.toString());
		assertEquals(allResources.size(), 31, "31 test groups");
		String lastId = "";
		for (Resource resource : allResources) {
			assertTrue(
					lastId.compareToIgnoreCase(resource.getIdentifier()) < 0,
					lastId + " compared to " + resource.getIdentifier() + " is "
							+ lastId.compareToIgnoreCase(resource.getIdentifier()));
			lastId = resource.getIdentifier();
		}

		allResources = resourceService.getAllResources(ResourceType.GROUP,
				GroupService.GroupSorter.DISPLAY_NAME.toString());
		assertEquals(allResources.size(), 31, "31 test groups");
		lastId = "";
		for (Resource resource : allResources) {
			assertTrue(
					lastId.compareToIgnoreCase(resource.getDisplayName()) <= 0,
					lastId + " compared to " + resource.getDisplayName() + " is "
							+ lastId.compareToIgnoreCase(resource.getDisplayName()));
			lastId = resource.getDisplayName();
		}
	}

	/**
	 * Test method for
	 * {@link com.sirma.itt.emf.resources.ResourceService#getOrCreateResource(com.sirma.itt.emf.resources.model.Resource)}
	 * .
	 */
	@Test(enabled = true)
	public void testGetOrCreateResource() throws Exception {
		EmfUser emfUser = new EmfUser();
		emfUser.setIdentifier("testUser");
		assertEquals(emfUser.getType(), ResourceType.USER, "Resource should be user!");
		EmfUser persisted = resourceService.getOrCreateResource(emfUser);
		assertEquals(persisted.getId(), "emf:testUser", "Id should be set!");
		assertEquals(persisted, emfUser, "getOrCreateResource should return consistent results!");
		EmfUser found = resourceService.getOrCreateResource(emfUser);
		assertEquals(persisted.getId(), "emf:testUser", "Id should be set!");
		assertEquals(found, persisted, "getOrCreateResource should return consistent results!");
	}

	/**
	 * Test method for
	 * {@link com.sirma.itt.emf.resources.ResourceService#getContainedResources(com.sirma.itt.emf.resources.model.Resource)}
	 * .
	 */
	@Test(enabled = true, dependsOnMethods = { "testGetResourceStringResourceType" })
	public void testGetContainedResources() throws Exception {
		String id = "GROUP_Consumers";
		ResourceType resType = ResourceType.GROUP;
		Resource resource = resourceService.getResource(id, resType);
		List<Resource> containedResources = resourceService.getContainedResources(resource);
		assertTrue(containedResources.size() == 2, id + " should contain users!");

		id = "GROUP_Dev";
		resource = resourceService.getResource(id, resType);
		containedResources = resourceService.getContainedResources(resource);
		assertTrue(containedResources.size() == 0, id + " should not contain users!");
	}

	/**
	 * Test method for
	 * {@link com.sirma.itt.emf.resources.ResourceService#getContainedResourceIdentifiers(com.sirma.itt.emf.resources.model.Resource)}
	 * .
	 */
	@Test(enabled = false)
	public void testGetContainedResourceIdentifiers() throws Exception {
		throw new RuntimeException("not yet implemented");
	}

	/**
	 * Test method for
	 * {@link com.sirma.itt.emf.resources.ResourceService#getContainingResources(com.sirma.itt.emf.resources.model.Resource)}
	 * .
	 */
	@Test(enabled = false)
	public void testGetContainingResources() throws Exception {
		throw new RuntimeException("not yet implemented");
	}

	/**
	 * Test method for
	 * {@link com.sirma.itt.emf.resources.ResourceService#getDisplayName(java.io.Serializable)}.
	 */
	@Test(enabled = false)
	public void testGetDisplayName() throws Exception {
		throw new RuntimeException("not yet implemented");
	}

	/**
	 * Test method for
	 * {@link com.sirma.itt.emf.resources.ResourceService#areEqual(java.lang.Object, java.lang.Object)}
	 * .
	 */
	@Test(enabled = false)
	public void testEquals() throws Exception {
		throw new RuntimeException("not yet implemented");
	}

	/**
	 * Check not null with assert/
	 *
	 * @param object
	 *            is the object to check
	 * @param message
	 *            the optional message
	 */
	private void assertNotNull(Object object, String message) {
		Assert.assertNotNull(object, message);
	}
}
