package com.sirma.itt.seip.annotations.mention;

import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

import com.sirma.itt.seip.configuration.ConfigurationProperty;
import com.sirma.itt.seip.configuration.SystemConfiguration;
import com.sirma.itt.seip.domain.instance.EmfInstance;
import com.sirma.itt.seip.domain.instance.InstanceReference;
import com.sirma.itt.seip.instance.InstanceTypeResolver;
import com.sirma.itt.seip.mail.MailNotificationContext;
import com.sirma.itt.seip.mail.MailNotificationService;

public class AnnotationMentionServiceImplTest {
	@Mock
	private MailNotificationService mailNotificationService;

	@Mock
	private SystemConfiguration systemConfiguration;

	@Mock
	private InstanceTypeResolver typeResolver;

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

		InstanceReference reference = mock(InstanceReference.class);
		when(typeResolver.resolveReference(any())).thenReturn(Optional.of(reference));
		when(reference.toInstance()).thenReturn(instance);
	}

	@Test
	public void testSendNotifications() {
		List<Serializable> mentioned = new ArrayList();
		mentioned.add("userId1");
		mentioned.add("userId2");
		mentioned.add("userId3");
		String commentedInstanceId = "instanceId";
		String commentsOn = "http://ittruse.ittbg.com/ontology/enterpriseManagementFramework#123456789";
		String commentedBy = "commentedBy";

		mentionService.sendNotifications(mentioned, commentedInstanceId, commentsOn, commentedBy);
		Mockito.verify(mailNotificationService, Mockito.times(3)).sendEmail(any(MailNotificationContext.class));
	}

}
