package com.sirma.itt.seip.permissions.mails;

import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.when;

import java.io.Serializable;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

import com.sirma.itt.seip.domain.instance.DefaultProperties;
import com.sirma.itt.seip.domain.instance.EmfInstance;
import com.sirma.itt.seip.domain.instance.InstanceReference;
import com.sirma.itt.seip.permissions.PermissionService;
import com.sirma.itt.seip.permissions.SecurityModel;
import com.sirma.itt.seip.permissions.role.ResourceRole;
import com.sirma.itt.seip.resources.EmfGroup;
import com.sirma.itt.seip.resources.EmfUser;
import com.sirma.itt.seip.resources.ResourceProperties;
import com.sirma.itt.seip.resources.ResourceService;

/**
 * Tests for MailExtractor.
 *
 * @author A. Kunchev
 */
public class UsersMailExtractorTest {

	private static final String USER = "user";

	private static final String GROUP_USERS = "GROUP_USERS";

	@InjectMocks
	private UsersMailExtractorImpl extractor = new UsersMailExtractorImpl();

	@Mock
	private PermissionService permissionService;

	@Mock
	private ResourceService resourceService;

	@Before
	public void setUp() {
		MockitoAnnotations.initMocks(this);
		extractor.init();
	}

	/**
	 * I don't know why, but for some reason 'MockitoAnnotations.initMocks(this)' doesn't resets the mocks and this
	 * method is necessary.
	 */
	@After
	public void reset() {
		Mockito.reset(resourceService, permissionService);
	}

	@Test
	public void extractMails_user_oneMail() {
		when(resourceService.findResource(USER)).thenReturn(prepareUser(USER));
		Collection<String> result = extractor.extractMails(Arrays.asList(USER), new EmfInstance());
		assertEquals(1, result.size());
	}

	@Test
	public void extractMails_noUserMail_emptyCollection() {
		when(resourceService.findResource(USER)).thenReturn(new EmfUser(USER));
		Collection<String> result = extractor.extractMails(Arrays.asList(USER), new EmfInstance());
		assertEquals(0, result.size());
	}

	@Test
	public void extractMails_groupWithTwoUsersNoMails_emptyCollection() {
		groupWithTwoUsersInternal(new EmfUser("simpleUser1"), new EmfUser("simpleUser2"), 0);
	}

	@Test
	public void extractMails_groupWithTwoUsers_twoMails() {
		groupWithTwoUsersInternal(prepareUser("user1"), prepareUser("user2"), 2);
	}

	private void groupWithTwoUsersInternal(EmfUser firstUser, EmfUser secondUser, int expectedNumberMails) {
		EmfGroup group = new EmfGroup("group_users", GROUP_USERS);
		when(resourceService.findResource("group_users")).thenReturn(group);
		when(resourceService.getContainedResources(group.getId()))
				.thenReturn(Arrays.asList(firstUser, secondUser));
		Collection<String> result = extractor.extractMails(Arrays.asList("group_users"), new EmfInstance());
		assertEquals(expectedNumberMails, result.size());
	}

	// TODO no idea why premissionService returns empty map
	public void extractMails_role_oneMail() {
		EmfUser user = prepareUser(USER);
		ResourceRole resourceRole = new ResourceRole();
		resourceRole.setRole(SecurityModel.BaseRoles.MANAGER);
		Map<String, ResourceRole> roleMap = new HashMap<>();
		roleMap.put(user.getIdentifier(), resourceRole);

		when(permissionService.getPermissionAssignments(any(InstanceReference.class))).thenReturn(roleMap);

		when(resourceService.findResource(USER)).thenReturn(user);
		Collection<String> result = extractor.extractMails(Arrays.asList("MANAGER"), new EmfInstance());
		assertEquals(1, result.size());
	}

	@Test
	public void extractMails_properties_threeMails() {
		EmfInstance instance = new EmfInstance();
		EmfGroup group = new EmfGroup(GROUP_USERS, GROUP_USERS);
		instance.add(DefaultProperties.TITLE, "[GROUP_USERS, user¶ first.last@tenant.com, alabala]");
		when(resourceService.findResource(GROUP_USERS)).thenReturn(group);
		when(resourceService.getContainedResources(group.getId()))
				.thenReturn(Arrays.asList(prepareUser("groupUser")));
		when(resourceService.findResource(USER)).thenReturn(prepareUser(USER));
		Collection<String> result = extractor.extractMails(Arrays.asList(DefaultProperties.TITLE), instance);
		assertEquals(3, result.size());
	}

	@Test
	public void extractMails_properties_array_threeMails() {
		EmfInstance instance = new EmfInstance();
		EmfGroup group = new EmfGroup(GROUP_USERS, GROUP_USERS);
		instance.add(DefaultProperties.TITLE,
				new String[] { "GROUP_USERS", "user", "first.last@tenant.com", "alabala]" });
		when(resourceService.findResource(GROUP_USERS)).thenReturn(group);
		when(resourceService.getContainedResources(group.getId()))
				.thenReturn(Arrays.asList(prepareUser("groupUser")));
		when(resourceService.findResource(USER)).thenReturn(prepareUser(USER));
		Collection<String> result = extractor.extractMails(Arrays.asList(DefaultProperties.TITLE), instance);
		assertEquals(3, result.size());
	}

	@Test
	public void extractMails_properties_collection_threeMails() {
		EmfInstance instance = new EmfInstance();
		EmfGroup group = new EmfGroup(GROUP_USERS, GROUP_USERS);
		Collection<Serializable> collection = Arrays.asList("GROUP_USERS", "user", "first.last@tenant.com", "alabala");
		instance.add(DefaultProperties.TITLE, (Serializable) collection);
		when(resourceService.findResource(GROUP_USERS)).thenReturn(group);
		when(resourceService.getContainedResources(group.getId()))
				.thenReturn(Arrays.asList(prepareUser("groupUser")));
		when(resourceService.findResource(USER)).thenReturn(prepareUser(USER));
		Collection<String> result = extractor.extractMails(Arrays.asList(DefaultProperties.TITLE), instance);
		assertEquals(3, result.size());
	}

	@Test
	public void extractMails_emptyProperties_emptyCollection() {
		EmfInstance instance = new EmfInstance();
		instance.add(DefaultProperties.TITLE, "");
		Collection<String> result = extractor.extractMails(Arrays.asList(DefaultProperties.TITLE), instance);
		assertEquals(0, result.size());
	}

	@Test
	public void extractMails_nullProperties_emptyCollection() {
		EmfInstance instance = new EmfInstance();
		instance.add(DefaultProperties.TITLE, null);
		Collection<String> result = extractor.extractMails(Arrays.asList(DefaultProperties.TITLE), instance);
		assertEquals(0, result.size());
	}

	@Test
	public void extractMails_mail_oneMail() {
		Collection<String> result = extractor.extractMails(Arrays.asList("user.lastName@tenant.com"),
				new EmfInstance());
		assertEquals(1, result.size());
	}

	@Test
	public void extractMails_badMail_emptyCollection() {
		Collection<String> result = extractor.extractMails(Arrays.asList("user.las.t^Na@me@tena1nt.co5m"),
				new EmfInstance());
		assertEquals(0, result.size());
	}

	@Test
	public void extractMails_noHandler_emptyCollection() {
		Collection<String> result = extractor.extractMails(Arrays.asList("alabala"), new EmfInstance());
		assertEquals(0, result.size());
	}

	@Test
	public void extractMails_emptyCollection_emptyCollection() {
		Collection<String> result = extractor.extractMails(new LinkedList<>(), new EmfInstance());
		assertEquals(0, result.size());
	}

	@Test
	public void extractMails_nullInstance_emptyCollection() {
		Collection<String> result = extractor.extractMails(Arrays.asList("value"), null);
		assertEquals(0, result.size());
	}

	@Test
	public void extractMails_emptyCollectionnullInstance_emptyCollection() {
		Collection<String> result = extractor.extractMails(new LinkedList<>(), null);
		assertEquals(0, result.size());
	}

	// -------------------- common methods ---------------------------------

	private static EmfUser prepareUser(String id) {
		EmfUser user = new EmfUser(id);
		user.add(ResourceProperties.EMAIL, id + ".name@tenant.com");
		return user;
	}
}
