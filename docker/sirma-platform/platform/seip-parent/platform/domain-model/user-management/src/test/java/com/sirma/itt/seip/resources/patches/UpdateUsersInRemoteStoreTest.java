package com.sirma.itt.seip.resources.patches;

import static java.util.Arrays.asList;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.MockitoAnnotations.initMocks;

import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;

import com.sirma.itt.seip.Executable;
import com.sirma.itt.seip.resources.EmfUser;
import com.sirma.itt.seip.resources.ResourceService;
import com.sirma.itt.seip.resources.User;
import com.sirma.itt.seip.tx.TransactionSupport;
import com.sirmaenterprise.sep.jms.api.MessageSender;
import com.sirmaenterprise.sep.jms.api.SendOptions;
import com.sirmaenterprise.sep.jms.api.SenderService;

import liquibase.database.Database;

/**
 * Tests for {@link UpdateUsersInRemoteStore}.
 *
 * @author smustafov
 */
public class UpdateUsersInRemoteStoreTest {

	@Mock
	private ResourceService resourceService;
	@Mock
	private SenderService senderService;
	@Mock
	private TransactionSupport transactionSupport;

	@Mock
	private Database database;

	@InjectMocks
	private UpdateUsersInRemoteStore updateUsersInRemoteStore;

	@Before
	public void init() {
		initMocks(this);

		doAnswer(invocation -> {
			Executable executable = invocation.getArgumentAt(0, Executable.class);
			executable.execute();
			return null;
		}).when(transactionSupport).invokeInNewTx(any(Executable.class));

		when(resourceService.getAllUsers())
				.thenReturn(asList(buildUser("user1"), buildUser("user2"), buildUser("user3")));
	}

	@Test
	public void should_invokeMessageSenderForEveryUser() throws Exception {
		MessageSender messageSender = mock(MessageSender.class);
		when(senderService.createSender(anyString(), any(SendOptions.class))).thenReturn(messageSender);

		updateUsersInRemoteStore.execute(database);

		verify(messageSender, times(3)).sendText(anyString());
	}

	private static User buildUser(String id) {
		EmfUser user = new EmfUser();
		user.setId(id);
		return user;
	}

}
