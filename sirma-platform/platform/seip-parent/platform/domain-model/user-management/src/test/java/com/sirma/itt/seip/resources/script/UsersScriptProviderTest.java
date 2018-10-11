package com.sirma.itt.seip.resources.script;

import static org.mockito.Matchers.any;
import static org.mockito.Mockito.when;
import static org.testng.Assert.assertTrue;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;

import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.testng.annotations.Test;

import com.sirma.itt.seip.resources.EmfGroup;
import com.sirma.itt.seip.resources.EmfUser;
import com.sirma.itt.seip.resources.ResourceService;
import com.sirma.itt.seip.resources.User;
import com.sirma.itt.seip.script.GlobalBindingsExtension;
import com.sirma.itt.seip.script.ScriptTest;

/**
 * Test {@link UsersScriptProvider}
 *
 * @author BBonev
 */
public class UsersScriptProviderTest extends ScriptTest {

	@InjectMocks
	private UsersScriptProvider scriptProvider;

	@Mock
	private ResourceService resourceService;

	@Test
	public void test_isCurrentUser() {
		when(resourceService.areEqual(any(), any())).thenReturn(Boolean.TRUE);
		Object object = eval("isCurrentUser('emf:admin')");
		assertTrue(Boolean.TRUE.equals(object));
	}

	@Test
	public void test_isCurrentUser_partOfCollection() {
		when(resourceService.areEqual(any(), any())).then(a -> {
			Object argument1 = readUserValue(a.getArgumentAt(0, Object.class));
			Object argument2 = readUserValue(a.getArgumentAt(1, Object.class));
			return Objects.equals(argument1, argument2);
		});
		Object found = eval("var l = new java.util.ArrayList(); "
				+ "l.add('someUser');"
				+ "l.add('emf:admin');"
				+ "isCurrentUser(l)");
		assertTrue(Boolean.TRUE.equals(found));

		Object notFound = eval("var l = new java.util.ArrayList(); "
				+ "l.add('someRandomUser');"
				+ "isCurrentUser(l)");
		assertTrue(Boolean.FALSE.equals(notFound));
	}

	private static Object readUserValue(Object o) {
		if (o instanceof User) {
			return ((User) o).getId();
		}
		return o;
	}

	@Override
	protected void provideBindings(List<GlobalBindingsExtension> bindingsExtensions) {
		super.provideBindings(bindingsExtensions);
		bindingsExtensions.add(scriptProvider);
	}

	@Test
	public void should_ReturnFalse_When_ParamIsNull() {
		Object result = eval("isCurrentUserMemberOf(null)");
		assertTrue(Boolean.FALSE.equals(result));

		when(securityContext.getAuthenticated()).thenReturn(null);
		result = eval("isCurrentUserMemberOf('emf:user')");
		assertTrue(Boolean.FALSE.equals(result));
	}

	@Test
	public void should_ReturnFalse_When_NoGroupFound() {
		when(resourceService.findResource("emf:group")).thenReturn(new EmfGroup());
		Object result = eval("isCurrentUserMemberOf('emf:group')");
		assertTrue(Boolean.FALSE.equals(result));
	}

	@Test
	public void should_ReturnFalse_When_NoUserFound() {
		when(resourceService.findResource(any())).thenReturn(new EmfUser(), null);
		Object result = eval("isCurrentUserMemberOf('emf:group')");
		assertTrue(Boolean.FALSE.equals(result));
	}

	@Test
	public void should_ReturnFalse_When_CurrentUserIsNotMemberOfGroup() {
		String groupId = "emf:collaborators";
		EmfGroup group = mockGroup(groupId, "emf:user1", "emf:user2");
		EmfUser user = mockUser("emf:admin");

		when(resourceService.findResource(any())).thenReturn(user, group);

		Object result = eval("isCurrentUserMemberOf('" + groupId + "')");
		assertTrue(Boolean.FALSE.equals(result));
	}

	@Test
	public void should_ReturnFalse_When_PassedGroupIsActuallyUser() {
		EmfUser user = mockUser("emf:admin");
		when(resourceService.findResource(any())).thenReturn(user);

		Object result = eval("isCurrentUserMemberOf('emf:group')");
		assertTrue(Boolean.FALSE.equals(result));
	}

	@Test
	public void should_ReturnTrue_When_CurrentUserIsMemberOfGroup() {
		String groupId = "emf:administrators";
		EmfGroup group = mockGroup(groupId, "emf:admin", "emf:otherAdmin");
		EmfUser user = mockUser("emf:admin");

		when(resourceService.findResource(any())).thenReturn(user, group);

		Object result = eval("isCurrentUserMemberOf('" + groupId + "')");
		assertTrue(Boolean.TRUE.equals(result));
	}

	@Test
	public void isCurrentUserMemberOf_Should_WorkWithCollection() {
		mockGroup("someGroup", "emf:someUser");
		EmfGroup group = mockGroup("emf:admins", "emf:admin");
		EmfUser user = mockUser("emf:admin");
		when(resourceService.findResource(securityContext.getAuthenticated())).thenReturn(user);
		when(resourceService.findResource("emf:admins")).thenReturn(group);

		Object result = eval("var list = new java.util.ArrayList();"
				+ "list.add('someGroup');"
				+ "list.add('emf:admins');"
				+ "isCurrentUserMemberOf(list)");
		assertTrue(Boolean.TRUE.equals(result));

		result = eval("var list = new java.util.ArrayList();"
				+ "list.add('someGroup');"
				+ "isCurrentUserMemberOf(list)");
		assertTrue(Boolean.FALSE.equals(result));
	}

	private static EmfUser mockUser(String userId) {
		EmfUser user = new EmfUser(userId);
		user.setId(userId);
		return user;
	}

	private EmfGroup mockGroup(String groupId, String... members) {
		EmfGroup group = new EmfGroup(groupId, groupId);
		when(resourceService.getContainedResourceIdentifiers(group)).thenReturn(Arrays.asList(members));
		return group;
	}

}
