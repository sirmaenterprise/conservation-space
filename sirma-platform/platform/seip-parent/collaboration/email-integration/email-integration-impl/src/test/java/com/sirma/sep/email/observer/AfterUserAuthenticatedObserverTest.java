package com.sirma.sep.email.observer;

import static com.sirma.sep.email.EmailIntegrationConstants.USER_FULL_URI;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.doCallRealMethod;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import com.sirma.itt.emf.security.event.UserAuthenticatedEvent;
import com.sirma.itt.seip.resources.EmfUser;
import com.sirma.itt.seip.resources.Resource;
import com.sirma.itt.seip.security.User;
import com.sirma.itt.seip.security.context.SecurityContext;
import com.sirma.itt.seip.tasks.SchedulerConfiguration;
import com.sirma.itt.seip.tasks.SchedulerContext;
import com.sirma.itt.seip.tasks.SchedulerService;
import com.sirma.sep.email.exception.EmailIntegrationException;
import com.sirma.sep.email.service.MailboxSupportableService;

public class AfterUserAuthenticatedObserverTest {

	private static String TEST_MAIL = "test-mail@domain.com";

	@InjectMocks
	private AfterUserAuthenticatedObserver observer;

	@Mock
	private MailboxSupportableService mailboxSupportableService;

	@Mock
	private SchedulerService schedulerService;

	@Mock
	private SecurityContext securityService;

	@Before
	public void setUp() throws EmailIntegrationException {
		observer = mock(AfterUserAuthenticatedObserver.class);
		MockitoAnnotations.initMocks(this);
		doCallRealMethod().when(observer).mountShareFolder(any(UserAuthenticatedEvent.class));
		when(securityService.isSystemTenant()).thenReturn(false);
	}

	@Test
	public void shouldScheduleIfEligible() throws EmailIntegrationException {
		UserAuthenticatedEvent event = new UserAuthenticatedEvent(mockUser());
		when(mailboxSupportableService.isMailboxSupportable(USER_FULL_URI)).thenReturn(true);
		observer.mountShareFolder(event);
		verify(schedulerService, times(1)).schedule(anyString(), any(SchedulerConfiguration.class),
				any(SchedulerContext.class));
	}

	@Test
	public void shouldNotScheduleLoginIfSystemLogsIn() throws EmailIntegrationException{
		when(securityService.isSystemTenant()).thenReturn(true);
		UserAuthenticatedEvent event = new UserAuthenticatedEvent(mockUser(), false);
		observer.mountShareFolder(event);
		verify(schedulerService, times(0)).schedule(anyString(), any(SchedulerConfiguration.class),
				any(SchedulerContext.class));
	}

	@Test
	public void shouldNotScheduleLoginNotInitiatedByUser() throws EmailIntegrationException {
		UserAuthenticatedEvent event = new UserAuthenticatedEvent(mockUser(), false);
		when(mailboxSupportableService.isMailboxSupportable(USER_FULL_URI)).thenReturn(true);
		observer.mountShareFolder(event);
		verify(schedulerService, times(0)).schedule(anyString(), any(SchedulerConfiguration.class),
				any(SchedulerContext.class));
	}

	@Test
	public void shouldNotScheduleNotMailboxSupportable() throws EmailIntegrationException {
		UserAuthenticatedEvent event = new UserAuthenticatedEvent(mockUser());
		when(mailboxSupportableService.isMailboxSupportable(USER_FULL_URI)).thenReturn(false);
		observer.mountShareFolder(event);
		verify(schedulerService, times(0)).schedule(anyString(), any(SchedulerConfiguration.class),
				any(SchedulerContext.class));
	}

	private static User mockUser() {
		Resource user = new EmfUser();
		user.setId("user-id");
		user.setIdentifier("user-id");
		user.setName("stella@stella.com");
		user.getProperties().put("emailAddress", TEST_MAIL);

		return (User) user;
	}
}
