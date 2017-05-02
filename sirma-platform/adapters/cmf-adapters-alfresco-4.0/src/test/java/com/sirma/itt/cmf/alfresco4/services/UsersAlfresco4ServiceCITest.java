package com.sirma.itt.cmf.alfresco4.services;

import java.util.List;

import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import com.sirma.itt.cmf.test.BaseAlfrescoTest;
import com.sirma.itt.emf.adapter.DMSException;
import com.sirma.itt.seip.resources.EmfUser;
import com.sirma.itt.seip.resources.User;
import com.sirma.itt.seip.resources.adapter.CMFUserService;

/**
 * Tests the {@link CMFUserService} impl in adapters.
 *
 * @author bbanchev
 */
public class UsersAlfresco4ServiceCITest extends BaseAlfrescoTest {
	private CMFUserService userAdapter;

	@BeforeClass
	@Override
	protected void setUp() {
		super.setUp();
		userAdapter = mockupProvider.mockUserAdapter();
	}

	/**
	 * Test method for {@link com.sirma.itt.cmf.alfresco4.services.UsersAlfresco4Service#getAllUsers()} .
	 */
	@Test
	public final void testGetAllUsers() throws Exception {
		List<User> allUsers = userAdapter.getAllUsers();
		assertNotNull(allUsers, "Null should not be returned");
		assertTrue(allUsers.size() > 1, "List should be at least with system and admin users");
		String username = "admin" + tenant;
		assertTrue(allUsers.contains(new EmfUser(username)), username + " should be in the list");
		username = "System" + tenant;
		assertTrue(allUsers.contains(new EmfUser(username)), username + " should be in the list");
		username = userName + tenant;
		assertTrue(allUsers.contains(new EmfUser(username)), username + " should be in the list");
	}

	/**
	 * Test method for
	 * {@link com.sirma.itt.cmf.alfresco4.services.UsersAlfresco4Service#getFilteredUsers(java.lang.String)} .
	 */
	@Test(dependsOnMethods = { "testGetAllUsers" })
	public final void testGetFilteredUsers() throws Exception {
		try {
			for (int i = 'a'; i < 'z'; i++) {
				testFindUser(Character.valueOf((char) i).toString());
			}
		} catch (DMSException e) {
			fail(e);
		}
	}

	/**
	 * Test intersection of user with specified filter according to all users.
	 *
	 * @param letter
	 *            is the letter to search
	 * @throws DMSException
	 *             on any error
	 */
	private void testFindUser(String letter) throws DMSException {
		List<User> allUsers = userAdapter.getAllUsers();
		List<User> filteredUsers = userAdapter.getFilteredUsers("*" + letter + "*");
		for (User user : filteredUsers) {
			assertTrue(user.getName() != null, "Identfier should not be null: " + user);
			assertTrue(user.getName().contains(letter) || user.getName().contains(letter.toUpperCase())
					|| user.getDisplayName().contains(letter) || user.getDisplayName().contains(letter.toUpperCase()),
					"Identfier should contain '" + letter + "': " + user.getName());
		}
		allUsers.removeAll(filteredUsers);
		for (User user : allUsers) {
			assertTrue(user.getName() != null, "Identfier should not be null: " + user);
			assertTrue(
					!(user.getName().contains(letter) || user.getName().contains(letter.toUpperCase())
							|| user.getDisplayName().contains(letter)
							|| user.getDisplayName().contains(letter.toUpperCase())),
					"Identfier should not contain '" + letter + "': " + user);
		}
	}

	/**
	 * Test method for {@link com.sirma.itt.cmf.alfresco4.services.UsersAlfresco4Service#findUser(java.lang.String)} .
	 */
	@Test
	public final void testFindUser() throws Exception {
		User synchronize = userAdapter.findUser("admin");
		assertNotNull(synchronize, "Admin user is known but not found!");
		userAdapter.findUser("blablablamissinguser");
		Assert.fail("User should not be returned!");
	}

}
