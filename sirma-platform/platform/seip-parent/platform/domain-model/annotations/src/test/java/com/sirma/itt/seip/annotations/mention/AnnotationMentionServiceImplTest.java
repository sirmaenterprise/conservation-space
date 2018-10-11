package com.sirma.itt.seip.annotations.mention;

import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import com.sirma.itt.seip.configuration.ConfigurationProperty;
import com.sirma.itt.seip.configuration.SystemConfiguration;
import com.sirma.itt.seip.domain.instance.EmfInstance;
import com.sirma.itt.seip.instance.InstanceTypeResolver;
import com.sirma.itt.seip.mail.MailNotificationContext;
import com.sirma.itt.seip.mail.MailNotificationService;
import com.sirma.itt.seip.resources.EmfUser;
import com.sirma.itt.seip.resources.ResourceService;
import com.sirma.itt.seip.resources.User;
import com.sirma.itt.seip.testutil.mocks.InstanceReferenceMock;

public class AnnotationMentionServiceImplTest {
	@Mock
	private MailNotificationService mailNotificationService;

	@Mock
	private SystemConfiguration systemConfiguration;

	@Mock
	private InstanceTypeResolver typeResolver;

	@Mock
	private ResourceService resourceService;

	@InjectMocks
	private AnnotationMentionServiceImpl mentionService;

	@Before
	public void setUpBeforeClass() throws Exception {
		MockitoAnnotations.initMocks(this);

		EmfInstance instance = new EmfInstance();
		instance.add("email", "123");

		ConfigurationProperty<String> configProperty = mock(ConfigurationProperty.class);
		when(systemConfiguration.getApplicationName()).thenReturn(configProperty);
		when(systemConfiguration.getUi2Url()).thenReturn(configProperty);

		when(typeResolver.resolveReference("instanceId")).then(
				a -> Optional.of(InstanceReferenceMock.createGeneric(a.getArgumentAt(0, String.class))));
		when(typeResolver.resolveReference("userId")).then(
				a -> Optional.of(InstanceReferenceMock.createGeneric(a.getArgumentAt(0, String.class))));
		when(typeResolver.resolveReference("invalid")).thenReturn(Optional.empty());

		User user1 = new EmfUser("userId1");
		user1.setActive(true);
		user1.setEmail("userId1@tenant.com");
		when(resourceService.findResource("userId1")).thenReturn(user1);

		User user2 = new EmfUser("userId2");
		user2.setActive(true);
		user2.setEmail("userId2@tenant.com");
		when(resourceService.findResource("userId2")).thenReturn(user2);

		User user3 = new EmfUser("userId3");
		user3.setActive(false);
		user3.setEmail("userId3@tenant.com");
		when(resourceService.findResource("userId3")).thenReturn(user3);
	}

	@Test
	public void sendNotifications_shouldSendMailsOnlyToActiveResourcesWithEmails() {
		List<Serializable> mentioned = new ArrayList<>();
		mentioned.add("userId1");
		mentioned.add("userId2");
		mentioned.add("userId3");
		mentioned.add("userId4");
		String commentedInstanceId = "instanceId";
		String commentsOn = "http://ittruse.ittbg.com/ontology/enterpriseManagementFramework#123456789";
		String commentedBy = "userId";

		mentionService.sendNotifications(mentioned, commentedInstanceId, commentsOn, commentedBy);
		verify(mailNotificationService, times(2)).sendEmail(any(MailNotificationContext.class));
	}

	@Test
	public void sendNotifications_shouldDoNothingIfCannotFindInstance() {
		List<Serializable> mentioned = new ArrayList<>();
		mentioned.add("userId1");
		String commentedInstanceId = "invalid";
		String commentsOn = "http://ittruse.ittbg.com/ontology/enterpriseManagementFramework#123456789";
		String commentedBy = "userId";

		mentionService.sendNotifications(mentioned, commentedInstanceId, commentsOn, commentedBy);
		verify(mailNotificationService, never()).sendEmail(any(MailNotificationContext.class));
	}

	@Test
	public void sendNotifications_shouldDoNothingIfCannotFindICommentedBy() {
		List<Serializable> mentioned = new ArrayList<>();
		mentioned.add("userId1");
		String commentedInstanceId = "instanceId";
		String commentsOn = "http://ittruse.ittbg.com/ontology/enterpriseManagementFramework#123456789";
		String commentedBy = "invalid";

		mentionService.sendNotifications(mentioned, commentedInstanceId, commentsOn, commentedBy);
		verify(mailNotificationService, never()).sendEmail(any(MailNotificationContext.class));
	}

}
