package com.sirma.itt.seip.resources.patches;

import static java.util.Arrays.asList;
import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyBoolean;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.MockitoAnnotations.initMocks;

import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;

import com.sirma.itt.seip.Executable;
import com.sirma.itt.seip.domain.instance.DefaultProperties;
import com.sirma.itt.seip.instance.state.Operation;
import com.sirma.itt.seip.resources.EmfUser;
import com.sirma.itt.seip.resources.ResourceService;
import com.sirma.itt.seip.resources.User;
import com.sirma.itt.seip.tx.TransactionSupport;

import liquibase.database.Database;

/**
 * Tests for {@link DeleteUsersWithInitialStatus}.
 *
 * @author smustafov
 */
public class DeleteUsersWithInitialStatusTest {

	@Mock
	private ResourceService resourceService;
	@Mock
	private TransactionSupport transactionSupport;
	@Mock
	private Database database;

	@InjectMocks
	private DeleteUsersWithInitialStatus deleteUsers;

	@Before
	public void before() {
		initMocks(this);

		doAnswer(invocation -> {
			Executable executable = invocation.getArgumentAt(0, Executable.class);
			executable.execute();
			return null;
		}).when(transactionSupport).invokeInNewTx(any(Executable.class));
	}

	@Test
	public void should_Delete_OnlyUsersWithInitialStatus() throws Exception {
		when(resourceService.getAllUsers()).thenReturn(asList(buildUser("user1", "ACTIVE"),
				buildUser("user2", "INACTIVE"), buildUser("user3", "INIT"), buildUser("user4", "ACTIVE")));

		ArgumentCaptor<User> argCaptor = ArgumentCaptor.forClass(User.class);

		deleteUsers.execute(database);

		verify(resourceService).delete(argCaptor.capture(), any(Operation.class), eq(true));

		User deleted = argCaptor.getValue();
		assertEquals("user3", deleted.getId());
		assertEquals("INIT", deleted.get(DefaultProperties.STATUS));
	}

	@Test
	public void should_Delete_OnlyUsersWithInitialStatus_MultipleUsers() throws Exception {
		when(resourceService.getAllUsers()).thenReturn(asList(buildUser("user1", "ACTIVE"), buildUser("user2", "INIT"),
				buildUser("user3", "INIT"), buildUser("user4", "ACTIVE"), buildUser("user5", "INIT"),
				buildUser("user6", "INACTIVE")));

		deleteUsers.execute(database);

		verify(resourceService, times(3)).delete(any(User.class), any(Operation.class), eq(true));
	}

	@Test
	public void should_DoNothing_When_NoUsersWithInitialStatus() throws Exception {
		when(resourceService.getAllUsers())
				.thenReturn(asList(buildUser("user1", "ACTIVE"), buildUser("user2", "INACTIVE"),
						buildUser("user3", "INACTIVE"), buildUser("user4", "ACTIVE"), buildUser("user5", "ACTIVE"),
						buildUser("user6", "INACTIVE")));

		deleteUsers.execute(database);

		verify(resourceService, never()).delete(any(User.class), any(Operation.class), anyBoolean());
	}

	@Test
	public void should_SaveUserWithActiveStatus_When_UsersHasNoStatus() throws Exception {
		User user = buildUser("user1", "ACTIVE");
		user.remove(DefaultProperties.STATUS);
		when(resourceService.getAllUsers()).thenReturn(asList(user));
		when(resourceService.getResource("user1")).thenReturn(user);

		deleteUsers.execute(database);

		verify(resourceService).updateResource(eq(user), any(Operation.class));
		verify(resourceService, never()).delete(any(User.class), any(Operation.class), anyBoolean());
	}

	private static User buildUser(String id, String status) {
		EmfUser user = new EmfUser();
		user.setId(id);
		user.add(DefaultProperties.STATUS, status);
		return user;
	}

}
