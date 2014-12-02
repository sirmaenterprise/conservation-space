/**
 *
 */
/**
 * @author bbanchev
 */
package com.sirma.itt.cmf.security;

import static com.sirma.itt.emf.security.SecurityModel.BaseRoles.VIEWER;

import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import com.sirma.itt.cmf.security.provider.ActivitiRoleProviderExtension;
import com.sirma.itt.cmf.security.provider.CmfRoleProvider;
import com.sirma.itt.cmf.security.provider.CmfRoleProviderExtension;
import com.sirma.itt.cmf.security.provider.CollectableRoleProviderExtension;
import com.sirma.itt.cmf.security.provider.RoleProviderExtension;
import com.sirma.itt.commons.utils.reflection.ReflectionUtils;
import com.sirma.itt.emf.security.SecurityModel;
import com.sirma.itt.emf.security.model.ActionRegistry;
import com.sirma.itt.emf.security.model.Role;
import com.sirma.itt.emf.security.model.RoleIdentifier;
import com.sirma.itt.emf.security.registry.ActionRegistryMock;

/**
 * The TestRoleRegistering should test the role model chain update
 *
 * @author bbanchev
 */
@Test
public class TestRoleRegistering {

	/** The action registry mock. */
	protected ActionRegistry actionRegistryMock;

	/** The cmf role provider extension. */
	protected CmfRoleProviderExtension cmfRoleProviderExtension;

	/** The activiti role provider extension. */
	protected ActivitiRoleProviderExtension activitiRoleProviderExtension;

	/** The collectable role provider extension. */
	protected CollectableRoleProviderExtension collectableRoleProviderExtension;

	/**
	 * Test chain.
	 */
	@Test
	public void testChainSet() {
		Map<RoleIdentifier, Role> allRoles = getAllRoles();
		/**
		 * VIEWER<br>
		 * CONSUMER<br>
		 * CONTRIBUTOR<br>
		 * COLLABORATOR<br>
		 * MANAGER<br>
		 * ADMINISTRATOR<br>
		 * POSSIBLE_ASSIGNEE<br>
		 * ASSIGNEE<br>
		 * CREATOR<br>
		 */
		Assert.assertEquals(9, allRoles.size());

		Assert.assertTrue(allRoles.remove(VIEWER).getPermissions().isEmpty(),
				"No Permission for VIEWER should be set");
		Set<Entry<RoleIdentifier, Role>> entrySet = allRoles.entrySet();
		for (Entry<RoleIdentifier, Role> entry : entrySet) {
			Assert.assertTrue(
					entry.getValue().getPermissions().containsKey(SecurityModel.PERMISSION_READ),
					"Read permission at least are needed: " + entry.getKey());
		}
	}

	/**
	 * Gets the all roles.
	 *
	 * @return the all roles
	 */
	private Map<RoleIdentifier, Role> getAllRoles() {
		CmfRoleProvider roleProvider = new CmfRoleProvider();
		final LinkedList<RoleProviderExtension> providerExtensionsList = new LinkedList<RoleProviderExtension>();
		providerExtensionsList.add(cmfRoleProviderExtension);
		Iterable<RoleProviderExtension> providerExtensions = null;
		providerExtensions = new Iterable<RoleProviderExtension>() {

			@Override
			public Iterator<RoleProviderExtension> iterator() {

				return providerExtensionsList.iterator();
			}
		};
		ReflectionUtils.setField(roleProvider, "collection", providerExtensions);
		Map<RoleIdentifier, Role> allRoles = new HashMap<RoleIdentifier, Role>(
				roleProvider.provide());
		int curruntExpectedRoles = 6;
		Assert.assertEquals(curruntExpectedRoles, allRoles.size(),
				"Expected roles are [MANAGER, VIEWER, CREATOR, CONSUMER, CONTRIBUTOR, COLLABORATOR]");

		providerExtensionsList.add(activitiRoleProviderExtension);
		providerExtensions = new Iterable<RoleProviderExtension>() {

			@Override
			public Iterator<RoleProviderExtension> iterator() {

				return providerExtensionsList.iterator();
			}
		};
		ReflectionUtils.setField(roleProvider, "collection", providerExtensions);
		allRoles = roleProvider.provide();
		curruntExpectedRoles += 2;
		Assert.assertEquals(
				curruntExpectedRoles,
				allRoles.size(),
				"Expected roles are [COLLABORATOR, CREATOR, CONTRIBUTOR, CONSUMER, POSSIBLE_ASSIGNEE, ASSIGNEE, MANAGER, VIEWER]");

		providerExtensionsList.add(collectableRoleProviderExtension);
		providerExtensions = new Iterable<RoleProviderExtension>() {

			@Override
			public Iterator<RoleProviderExtension> iterator() {

				return providerExtensionsList.iterator();
			}
		};
		ReflectionUtils.setField(roleProvider, "collection", providerExtensions);
		allRoles = roleProvider.provide();
		curruntExpectedRoles += 1;
		Assert.assertEquals(
				curruntExpectedRoles,
				allRoles.size(),
				"Expected roles are [COLLABORATOR, CREATOR, CONTRIBUTOR, CONSUMER, POSSIBLE_ASSIGNEE, ASSIGNEE, ADMINISTRATOR, MANAGER, VIEWER]");

		return allRoles;
	}

	/**
	 * Sets it up with some needed classes
	 */
	@BeforeClass
	public void setUp() {

		cmfRoleProviderExtension = new CmfRoleProviderExtension();
		activitiRoleProviderExtension = new ActivitiRoleProviderExtension();
		collectableRoleProviderExtension = new CollectableRoleProviderExtension();
		actionRegistryMock = new ActionRegistryMock();
		ReflectionUtils.setField(cmfRoleProviderExtension, "actionRegistry", actionRegistryMock);

		ReflectionUtils.setField(activitiRoleProviderExtension, "actionRegistry",
				actionRegistryMock);
	}

}