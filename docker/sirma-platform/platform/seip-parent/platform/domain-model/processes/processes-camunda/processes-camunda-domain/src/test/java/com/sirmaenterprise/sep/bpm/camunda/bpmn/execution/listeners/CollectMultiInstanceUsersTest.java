package com.sirmaenterprise.sep.bpm.camunda.bpmn.execution.listeners;

import static com.sirmaenterprise.sep.bpm.camunda.MockProvider.mockDelegateExecution;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.anyVararg;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import javax.enterprise.inject.Instance;

import org.camunda.bpm.engine.IdentityService;
import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.identity.Group;
import org.camunda.bpm.engine.identity.GroupQuery;
import org.camunda.bpm.engine.identity.User;
import org.camunda.bpm.engine.identity.UserQuery;
import org.camunda.bpm.engine.impl.el.Expression;
import org.camunda.bpm.engine.impl.el.FixedValue;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import com.sirma.itt.seip.util.ReflectionUtils;
import com.sirmaenterprise.sep.bpm.camunda.MockProvider;

/**
 * @author bbanchev
 */
@RunWith(MockitoJUnitRunner.class)
public class CollectMultiInstanceUsersTest {
	@Mock
	private Instance<IdentityService> identityServiceInstance;
	@Mock
	private IdentityService identityService;
	@InjectMocks
	private CollectMultiInstanceUsers collectMultiInstanceUsers;
	private DelegateExecution delegateExecution;

	@Before
	public void setUp() throws Exception {
		when(identityServiceInstance.get()).thenReturn(identityService);
		delegateExecution = mockDelegateExecution(MockProvider.DEFAULT_ENGINE, DelegateExecution.class);
	}

	/**
	 * Test method for
	 * {@link com.sirmaenterprise.sep.bpm.camunda.bpmn.execution.listeners.CollectMultiInstanceUsers#resolveUsers(org.camunda.bpm.engine.delegate.DelegateExecution, java.lang.String)}
	 * .
	 */
	@Test
	public void testResolveUsers() throws Exception {
		List<String> usersIds = new LinkedList<>();
		usersIds.add("user1");
		usersIds.add("user2");
		usersIds.add("group1");

		List<User> users = new LinkedList<>();
		users.add(mockUser("user1"));
		users.add(mockUser("user2"));

		List<User> usersFromGroup = new LinkedList<>();
		users.add(mockUser("user1"));
		users.add(mockUser("user3"));

		List<Group> groups = new LinkedList<>();
		groups.add(mockGroup("group1"));

		UserQuery userQuery = mock(UserQuery.class);
		when(identityService.createUserQuery()).thenReturn(userQuery);
		when(userQuery.userIdIn(anyVararg())).thenReturn(userQuery);

		GroupQuery groupQuery = mock(GroupQuery.class);
		when(identityService.createGroupQuery()).thenReturn(groupQuery);
		when(groupQuery.groupIdIn(anyVararg())).thenReturn(groupQuery);
		when(groupQuery.list()).thenReturn(groups);
		when(userQuery.memberOfGroup(eq("group1"))).thenReturn(userQuery);
		when(userQuery.list()).thenReturn(users, usersFromGroup);

		when(delegateExecution.getVariable(eq("assignees"))).thenReturn(usersIds);
		when(delegateExecution.getVariable(eq("assigneesInvalid"))).thenReturn(null);
		Set<String> resolveUsers = collectMultiInstanceUsers.resolveUsers(delegateExecution,
				"assignees,assigneesInvalid");
		verify(userQuery).userIdIn(eq("user1"), eq("user2"), eq("group1"));
		assertEquals(3, resolveUsers.size());
		assertTrue(resolveUsers.contains("user1"));
		assertTrue(resolveUsers.contains("user2"));
		assertTrue(resolveUsers.contains("user3"));

	}

	@Test(expected = NullPointerException.class)
	public void testResolveUsersWithNullValue() throws Exception {
		collectMultiInstanceUsers.resolveUsers(delegateExecution, null);
	}

	@Test
	public void testResolveUsersWithInvalidValues() throws Exception {
		mockEmptyQueries();
		when(delegateExecution.getVariable(eq("assignees"))).thenReturn(null);
		when(delegateExecution.getVariable(eq("assigneesInvalid"))).thenReturn(null);
		Set<String> resolveUsers = collectMultiInstanceUsers.resolveUsers(delegateExecution,
				"assignees,assigneesInvalid");
		assertEquals(0, resolveUsers.size());

	}

	private void mockEmptyQueries() {
		UserQuery userQuery = mock(UserQuery.class);
		when(identityService.createUserQuery()).thenReturn(userQuery);
		doReturn(userQuery).when(userQuery).userIdIn();
		when(userQuery.list()).thenReturn(Collections.emptyList());

		GroupQuery groupQuery = mock(GroupQuery.class);
		when(identityService.createGroupQuery()).thenReturn(groupQuery);
		doReturn(groupQuery).when(groupQuery).groupIdIn();
		when(groupQuery.list()).thenReturn(Collections.emptyList());
	}

	private User mockUser(String id) {
		User user = mock(User.class);
		when(user.getId()).thenReturn(id);
		return user;
	}

	private Group mockGroup(String id) {
		Group user = mock(Group.class);
		when(user.getId()).thenReturn(id);
		return user;
	}

	@Test(expected = NullPointerException.class)
	public void testNotifyInvalidSource() throws Exception {
		ReflectionUtils.setFieldValue(collectMultiInstanceUsers, "source", null);
		ReflectionUtils.setFieldValue(collectMultiInstanceUsers, "target", mock(FixedValue.class));
		collectMultiInstanceUsers.validateParameters();
	}

	@Test(expected = NullPointerException.class)
	public void testNotifyInvalidTarget() throws Exception {
		ReflectionUtils.setFieldValue(collectMultiInstanceUsers, "source", mock(Expression.class));
		ReflectionUtils.setFieldValue(collectMultiInstanceUsers, "target", null);
		collectMultiInstanceUsers.validateParameters();
	}

	@Test
	public void testNotify() throws Exception {
		mockEmptyQueries();
		Expression source = mock(Expression.class);
		when(source.getValue(delegateExecution)).thenReturn("assignees");
		FixedValue target = mock(FixedValue.class);
		when(target.getValue(delegateExecution)).thenReturn("assigneesall");
		ReflectionUtils.setFieldValue(collectMultiInstanceUsers, "source", source);
		ReflectionUtils.setFieldValue(collectMultiInstanceUsers, "target", target);
		collectMultiInstanceUsers.notify(delegateExecution, collectMultiInstanceUsers);
		verify(delegateExecution).setVariable(eq("assigneesall"), eq(new LinkedHashSet<>()));
	}
}
