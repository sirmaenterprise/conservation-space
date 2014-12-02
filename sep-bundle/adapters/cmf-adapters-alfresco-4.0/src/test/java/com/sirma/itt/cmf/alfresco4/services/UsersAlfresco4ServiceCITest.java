package com.sirma.itt.cmf.alfresco4.services;

import java.util.List;

import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import com.sirma.itt.cmf.services.adapter.CMFUserService;
import com.sirma.itt.cmf.test.BaseAlfrescoTest;
import com.sirma.itt.emf.adapter.DMSException;
import com.sirma.itt.emf.security.model.EmfUser;
import com.sirma.itt.emf.security.model.User;

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
	 * Test method for
	 * {@link com.sirma.itt.cmf.alfresco4.services.UsersAlfresco4Service#getAllUsers()}.
	 */
	@Test
	public final void testGetAllUsers() throws Exception {
		List<User> allUsers = userAdapter.getAllUsers();
		assertTrue(allUsers != null, "Null should not be returned");
		assertTrue(allUsers.size() > 1, "List should be at least with system and admin users");
		String userName = "admin";
		assertTrue(allUsers.contains(new EmfUser(userName)), userName + " should be in the list");
		userName = "system";
		assertTrue(allUsers.contains(new EmfUser(userName)), userName + " should be in the list");
		userName = this.userName;
		assertTrue(allUsers.contains(new EmfUser(userName)), userName + " should be in the list");
	}

	/**
	 * Test method for
	 * {@link com.sirma.itt.cmf.alfresco4.services.UsersAlfresco4Service#getFilteredUsers(java.lang.String)}
	 * .
	 */
	@Test(dependsOnMethods = { "testGetAllUsers" })
	public final void testGetFilteredUsers() throws Exception {
		try {
			for (int i = (int) 'a'; i < (int) 'z'; i++) {
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
			assertTrue(user.getIdentifier() != null, "Identfier should not be null: " + user);
			assertTrue(
					user.getIdentifier().contains(letter)
							|| user.getIdentifier().contains(letter.toUpperCase())
							|| user.getDisplayName().contains(letter)
							|| user.getDisplayName().contains(letter.toUpperCase()),
					"Identfier should contain '" + letter + "': " + user.getIdentifier());
		}
		allUsers.removeAll(filteredUsers);
		for (User user : allUsers) {
			assertTrue(user.getIdentifier() != null, "Identfier should not be null: " + user);
			assertTrue(
					!(user.getIdentifier().contains(letter)
							|| user.getIdentifier().contains(letter.toUpperCase())
							|| user.getDisplayName().contains(letter) || user.getDisplayName()
							.contains(letter.toUpperCase())), "Identfier should not contain '"
							+ letter + "': " + user);
		}
	}

	/**
	 * Test method for
	 * {@link com.sirma.itt.cmf.alfresco4.services.UsersAlfresco4Service#getUserRole(java.lang.String, java.lang.String)}
	 * .
	 */
	@Test(enabled = false)
	public final void testGetUserRole() throws Exception {
		throw new RuntimeException("not yet implemented");
	}

	/**
	 * Test method for
	 * {@link com.sirma.itt.cmf.alfresco4.services.UsersAlfresco4Service#findUser(java.lang.String)}
	 * .
	 */
	@Test
	public final void testFindUser() throws Exception {
		try {
			User synchronize = userAdapter.findUser("admin");
			assertNotNull(synchronize, "Admin user is known but not found!");
		} catch (DMSException e) {
			fail(e);
		}
		try {
			userAdapter.findUser("blablablamissinguser");
			Assert.fail("User should not be returned!");
		} catch (DMSException e) {
			assertTrue(e.getMessage().contains("Missing user"), "Error should be for missing user");
		}
	}

}
