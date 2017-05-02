package com.sirmaenterprise.sep.helprequest;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Matchers;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.testng.Assert;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import com.sirma.itt.seip.configuration.ConfigurationProperty;
import com.sirma.itt.seip.configuration.SystemConfiguration;
import com.sirma.itt.seip.domain.codelist.CodelistService;
import com.sirma.itt.seip.domain.codelist.model.CodeValue;
import com.sirma.itt.seip.mail.MailService;
import com.sirma.itt.seip.mail.MessageSender;
import com.sirma.itt.seip.mail.attachments.MailAttachment;
import com.sirma.itt.seip.security.User;
import com.sirma.itt.seip.security.context.SecurityContext;

/**
 * Test for HelpRequestService.
 * 
 * @author Boyan Tonchev
 *
 */
@SuppressWarnings("static-method")
public class HelpRequestServiceTest {


	private static final String LOCALE_LANGUAGE = "en";
	
	
	private static final String TYPE_VALUE = "Bug";
	private static final String USER_EMAIL = "email of user";
	private static final String RECIPIENT_EMAIL = "email of recipient";
	private static final Integer CL_600 = 600;
	
	private static final String SUBJECT = "subject";
	private static final String TYPE = "RH02";
	private static final String MESSAGE = "content of mail";
	
	private HelpRequestMessage helpRequestMessage;
	
	@Mock
	private CodeValue codeValue;
	
	@Mock
	private User currentUser;
	
	// Mocks to be injected
	@Mock
	private ConfigurationProperty<String> helpSuportEmail;
	
	@Mock
	private MailService mailService;
	
	@Mock
	private ConfigurationProperty<Integer> codelistMailType;
	
	@Mock
	private SecurityContext securityContext;
	
	@Mock
	private MessageSender messageSender;
	
	@Mock
	private CodelistService codelistService;
	
	@Mock
	private SystemConfiguration systemConfiguration;
	
	@InjectMocks
	private HelpRequestService helpRequestService;
	
	@BeforeMethod
	public void setup() {
		MockitoAnnotations.initMocks(this);
		Mockito.when(systemConfiguration.getSystemLanguage()).thenReturn(LOCALE_LANGUAGE);
		Mockito.when(codeValue.getDescription(Matchers.eq(new Locale(LOCALE_LANGUAGE)))).thenReturn(TYPE_VALUE);
		Mockito.when(codelistService.getCodeValue(CL_600, TYPE)).thenReturn(codeValue);
		Mockito.when(currentUser.getProperties()).thenReturn(new HashMap<String, Serializable>(0));
		Mockito.when(securityContext.getAuthenticated()).thenReturn(currentUser);
		Mockito.when(helpSuportEmail.get()).thenReturn(RECIPIENT_EMAIL);
		Mockito.when(helpSuportEmail.requireConfigured()).thenReturn(helpSuportEmail);
		Mockito.when(codelistMailType.get()).thenReturn(CL_600);
		
		helpRequestMessage = new HelpRequestMessage();
		helpRequestMessage.setType(TYPE);
		helpRequestMessage.setSubject(SUBJECT);
		helpRequestMessage.setDescription(MESSAGE);
	}
	
	/**
	 * Test method sendHelpRequest scenario without recipient.
	 */
	@Test
	public void sendHelpRequestWithoutCcRecipientTest() {
		//execution of tested method.
		ArgumentCaptor<String> message = ArgumentCaptor.forClass(String.class);
		ArgumentCaptor<String> subject = ArgumentCaptor.forClass(String.class);
		ArgumentCaptor<String> mailgroupId = ArgumentCaptor.forClass(String.class);
		ArgumentCaptor<String[]> recipients = ArgumentCaptor.forClass(String[].class);
		ArgumentCaptor<String[]> ccRecipients = ArgumentCaptor.forClass(String[].class);
		ArgumentCaptor<MailAttachment[]> mailAttachments = ArgumentCaptor.forClass(MailAttachment[].class);
		Map<String, Serializable> userProperties = new HashMap<>(1);
		Mockito.when(currentUser.getProperties()).thenReturn(userProperties);
		
		//execute tested method.
		helpRequestService.sendHelpRequest(helpRequestMessage);

		//verification of tested method.
		Mockito.verify(messageSender).constructMessage(message.capture(), subject.capture(), mailgroupId.capture(), recipients.capture(), ccRecipients.capture(), mailAttachments.capture());
		Assert.assertEquals(message.getValue(), MESSAGE);
		Assert.assertEquals(subject.getValue(), TYPE_VALUE + " : " + SUBJECT);
		Assert.assertNull(mailgroupId.getValue());
		Assert.assertEquals(recipients.getValue(), new String[] {RECIPIENT_EMAIL});
		Assert.assertEquals(ccRecipients.getValue(), new String[0]);
		Assert.assertNull(mailAttachments.getValue());
	}
	
	/**
	 * Test method sendHelpRequest.
	 */
	@Test
	public void sendHelpRequestTest() {
		//execution of tested method.
		ArgumentCaptor<String> message = ArgumentCaptor.forClass(String.class);
		ArgumentCaptor<String> subject = ArgumentCaptor.forClass(String.class);
		ArgumentCaptor<String> mailgroupId = ArgumentCaptor.forClass(String.class);
		ArgumentCaptor<String[]> recipients = ArgumentCaptor.forClass(String[].class);
		ArgumentCaptor<String[]> ccRecipients = ArgumentCaptor.forClass(String[].class);
		ArgumentCaptor<MailAttachment[]> mailAttachments = ArgumentCaptor.forClass(MailAttachment[].class);
		Map<String, Serializable> userProperties = new HashMap<>(1);
		userProperties.put(helpRequestService.PROPERTY_USER_EMAIL, USER_EMAIL);
		Mockito.when(currentUser.getProperties()).thenReturn(userProperties);
		
		//execute tested method.
		helpRequestService.sendHelpRequest(helpRequestMessage);
		
		//verification of tested method.
		Mockito.verify(messageSender).constructMessage(message.capture(), subject.capture(), mailgroupId.capture(), recipients.capture(), ccRecipients.capture(), mailAttachments.capture());
		Assert.assertEquals(message.getValue(), MESSAGE);
		Assert.assertEquals(subject.getValue(), TYPE_VALUE + " : " + SUBJECT);
		Assert.assertNull(mailgroupId.getValue());
		Assert.assertEquals(recipients.getValue(), new String[] {RECIPIENT_EMAIL});
		Assert.assertEquals(ccRecipients.getValue(), new String[] {USER_EMAIL});
		Assert.assertNull(mailAttachments.getValue());
	}
}
