package com.sirma.sep.email.patch;

import static org.mockito.Matchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.Spy;

import com.sirma.itt.seip.db.DbDao;
import com.sirma.itt.seip.instance.state.Operation;
import com.sirma.itt.seip.resources.EmfUser;
import com.sirma.itt.seip.resources.Resource;
import com.sirma.itt.seip.resources.ResourceService;
import com.sirma.itt.seip.resources.User;
import com.sirma.itt.seip.testutil.mocks.ConfigurationPropertyMock;
import com.sirma.itt.seip.tx.TransactionSupport;
import com.sirma.sep.email.EmailIntegrationConstants;
import com.sirma.sep.email.configuration.EmailIntegrationConfiguration;
import com.sirma.sep.email.entity.MailboxSupportable;
import com.sirma.sep.email.observer.TransactionSupportFake;

/**
 * Tests for {@link CreateEmailAddressForExistingUsersPatch}.
 *
 * @author smustafov
 */
public class CreateEmailAddressForExistingUsersPatchTest {

	@InjectMocks
	private CreateEmailAddressForExistingUsersPatch patch;

	@Mock
	private EmailIntegrationConfiguration emailIntegrationConfiguration;
	@Mock
	private ResourceService resourceService;
	@Mock
	private DbDao dbDao;
	@Spy
	private TransactionSupport transactionSupport = new TransactionSupportFake();

	@Before
	public void beforeEach() {
		MockitoAnnotations.initMocks(this);

		when(emailIntegrationConfiguration.getTenantDomainAddress()).thenReturn(new ConfigurationPropertyMock<>(""));
		when(emailIntegrationConfiguration.getTestEmailPrefix()).thenReturn(new ConfigurationPropertyMock<>(""));
	}

	@Test
	public void should_DoNothing_When_MailboxNotSupported() throws Exception {
		List<User> users = Arrays.asList(createUser("user1@tenant.com", null), createUser("user2@tenant.com", null));
		when(resourceService.getAllUsers()).thenReturn(users);
		mockDbDao(false);

		patch.execute(null);

		verify(resourceService, never()).updateResource(any(), any());
	}

	@Test
	public void should_DoNothing_When_UserAlreadyHasEmailAddress() throws Exception {
		List<User> users = Collections.singletonList(createUser("user@tenant.com", "user@tenant.com"));
		when(resourceService.getAllUsers()).thenReturn(users);
		mockDbDao(true);

		patch.execute(null);

		verify(resourceService, never()).updateResource(any(), any());
	}

	@Test
	public void should_CreateEmailAddress_ForUsersWithoutOne() throws Exception {
		List<User> users = Arrays
				.asList(createUser("user1@tenant.com", "user1@tenant.com"), createUser("user2@tenant.com", null));
		when(resourceService.getAllUsers()).thenReturn(users);
		mockDbDao(true);

		patch.execute(null);

		verify(resourceService).updateResource(any(Resource.class), any(Operation.class));
	}

	private User createUser(String userId, String emailAddress) {
		EmfUser user = new EmfUser();
		user.setName(userId);
		user.setId(userId);
		user.add(EmailIntegrationConstants.EMAIL_ADDRESS, emailAddress);
		when(resourceService.findResource(userId)).thenReturn(user);
		return user;
	}

	private void mockDbDao(boolean mailboxSupported) {
		List<Object> results = Collections.singletonList("User");
		if (!mailboxSupported) {
			results = Collections.emptyList();
		}
		when(dbDao.fetchWithNamed(MailboxSupportable.QUERY_MAILBOX_SUPPORTABLE_KEY, Collections.emptyList()))
				.thenReturn(results);
	}

}
