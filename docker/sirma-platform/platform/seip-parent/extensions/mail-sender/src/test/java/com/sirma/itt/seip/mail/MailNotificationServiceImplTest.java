package com.sirma.itt.seip.mail;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyCollectionOf;
import static org.mockito.Matchers.anyListOf;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.io.StringReader;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Matchers;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.mockito.Spy;

import com.sirma.itt.seip.Executable;
import com.sirma.itt.seip.concurrent.GenericAsyncTask;
import com.sirma.itt.seip.concurrent.TaskExecutor;
import com.sirma.itt.seip.concurrent.locks.ContextualLock;
import com.sirma.itt.seip.configuration.ConfigurationProperty;
import com.sirma.itt.seip.convert.TypeConverter;
import com.sirma.itt.seip.convert.TypeConverterUtil;
import com.sirma.itt.seip.domain.instance.EmfInstance;
import com.sirma.itt.seip.domain.instance.Instance;
import com.sirma.itt.seip.domain.instance.InstanceReference;
import com.sirma.itt.seip.instance.context.InstanceContextService;
import com.sirma.itt.seip.mail.attachments.MailAttachment;
import com.sirma.itt.seip.resources.EmfUser;
import com.sirma.itt.seip.resources.ResourceProperties;
import com.sirma.itt.seip.resources.User;
import com.sirma.itt.seip.script.ScriptInstance;
import com.sirma.itt.seip.template.Template;
import com.sirma.itt.seip.template.TemplateSearchCriteria;
import com.sirma.itt.seip.template.TemplateService;
import com.sirma.itt.seip.template.TemplatesSynchronizedEvent;
import com.sirma.itt.seip.testutil.fakes.TransactionSupportFake;
import com.sirma.itt.seip.testutil.mocks.InstanceContextServiceMock;
import com.sirma.itt.seip.testutil.mocks.InstanceReferenceMock;
import com.sirma.itt.seip.tx.TransactionSupport;
import com.sirma.itt.seip.util.ReflectionUtils;

import freemarker.cache.StringTemplateLoader;
import freemarker.core.ParseException;
import freemarker.template.Configuration;
import freemarker.template.MalformedTemplateNameException;
import freemarker.template.TemplateNotFoundException;

/**
 * Test for {@link MailNotificationServiceImpl}.
 *
 * @author A. Kunchev
 */
public class MailNotificationServiceImplTest {

	@InjectMocks
	private MailNotificationServiceImpl service = new MailNotificationServiceImpl();

	@Mock
	private TaskExecutor taskExecutor;

	@Mock
	private TemplateService documentTemplateService;

	@Mock
	private ContextualLock contextualLock;

	@Mock
	private ContextualTemplateLoader templateLoader;

	@Mock
	private MailService mailService;

	@Mock
	private ConfigurationProperty<Boolean> notificationEnabled;

	private Configuration configuration;

	@Mock
	private MailNotificationHelperService mailNotificationHelperService;

	@Mock
	private InstanceContextService contextInitializer;

	@Mock
	private TypeConverter typeConverter;
	@Spy
	private InstanceContextServiceMock contextService;
	@Spy
	private TransactionSupport transactionSupport = new TransactionSupportFake();

	@Before
	public void setup() {
		MockitoAnnotations.initMocks(this);
	}

	@Test
	public void onApplicationStart_locked() {
		when(contextualLock.tryLock()).thenReturn(false);
		service.onApplicationStart();
		verify(documentTemplateService, never()).getTemplates(any());
		verify(taskExecutor, never()).execute(anyListOf(GenericAsyncTask.class));
	}

	@Test
	public void onApplicationStart_unLocked() {
		when(contextualLock.tryLock()).thenReturn(true);
		when(documentTemplateService.getTemplates(any())).thenReturn(Arrays.asList(new Template()));
		service.onApplicationStart();
		verify(documentTemplateService).getTemplates(eq(new TemplateSearchCriteria("emailTemplate",null,null)));
		verify(taskExecutor).execute(anyListOf(GenericAsyncTask.class));
		verify(contextualLock).unlock();
	}

	@Test(expected = Exception.class)
	public void onApplicationStart_exception() {
		when(contextualLock.tryLock()).thenReturn(true);
		when(documentTemplateService.getTemplates(any())).thenReturn(Arrays.asList(new Template()));
		Mockito.doThrow(new Exception()).when(taskExecutor).execute(anyListOf(GenericAsyncTask.class));
		service.onApplicationStart();
	}

	@Test
	public void synchronizeTemplates() {
		service.synchronizeTemplates(new TemplatesSynchronizedEvent());
		verify(taskExecutor).executeAsync(any(Executable.class));
	}

	@Test
	public void sendEmail_nullConfiguration() {
		ReflectionUtils.setFieldValue(service, "configuration", null);
		service.sendEmail(mock(MailNotificationContext.class), "");
		verify(mailService, never()).enqueueMessage(anyCollectionOf(String.class), anyString(), anyString(),
				anyString(), any(MailAttachment.class));
		verify(mailService, never()).enqueueMessage(anyCollectionOf(String.class), anyString(), anyString(),
				anyString(), any(MailAttachment.class));
	}

	@Test
	public void sendEmail_nullNotificationContext() {
		service.sendEmail(null, "id");
		verify(mailService, never()).enqueueMessage(anyCollectionOf(String.class), anyString(), anyString(),
				anyString(), any(MailAttachment.class));
		verify(mailService, never()).enqueueMessage(anyCollectionOf(String.class), anyString(), anyString(),
				anyString(), any(MailAttachment.class));
	}

	@Test
	public void sendEmail_nullSendTo() {
		when(notificationEnabled.get()).thenReturn(true);
		service.sendEmail(mock(MailNotificationContext.class), "id");
		verify(mailService, never()).enqueueMessage(anyCollectionOf(String.class), anyString(), anyString(),
				anyString(), any(MailAttachment.class));
		verify(mailService, never()).enqueueMessage(anyCollectionOf(String.class), anyString(), anyString(),
				anyString(), any(MailAttachment.class));
	}

	@Test
	public void sendEmail_emptySendTo() {
		when(notificationEnabled.get()).thenReturn(true);
		MailNotificationContext delegate = mock(MailNotificationContext.class);
		when(delegate.getSendTo()).thenReturn(Collections.emptyList());
		service.sendEmail(delegate, "id");
		verify(mailService, never()).enqueueMessage(anyCollectionOf(String.class), anyString(), anyString(),
				anyString(), any(MailAttachment.class));
		verify(mailService, never()).enqueueMessage(anyCollectionOf(String.class), anyString(), anyString(),
				anyString(), any(MailAttachment.class));
	}

	@Test
	public void sendEmail_notificationDisabled() {
		when(notificationEnabled.get()).thenReturn(false);
		service.sendEmail(mock(MailNotificationContext.class), "id");
		verify(mailService, never()).enqueueMessage(anyCollectionOf(String.class), anyString(), anyString(),
				anyString(), any(MailAttachment.class));
		verify(mailService, never()).enqueueMessage(anyCollectionOf(String.class), anyString(), anyString(),
				anyString(), any(MailAttachment.class));
	}

	@Test(expected = RuntimeException.class)
	public void sendEmail_nullAttachments_exception()
			throws TemplateNotFoundException, MalformedTemplateNameException, ParseException, IOException {
		when(notificationEnabled.get()).thenReturn(true);
		MailNotificationContext delegate = mock(MailNotificationContext.class);
		when(delegate.getSendTo()).thenReturn(Arrays.asList("user@sirma.bg"));
		configuration = mock(Configuration.class);
		when(configuration.getTemplate(anyString())).thenThrow(new IOException());
		ReflectionUtils.setFieldValue(service, "configuration", configuration);
		service.sendEmail(delegate, "id");
	}

	@Test
	public void sendEmail_withAttachments_sendFromNotNull_mailServiceCalled()
			throws TemplateNotFoundException, MalformedTemplateNameException, ParseException, IOException {
		when(notificationEnabled.get()).thenReturn(true);

		MailNotificationContext delegate = mock(MailNotificationContext.class);
		when(delegate.getSendTo()).thenReturn(Arrays.asList("user@sirma.bg"));
		when(delegate.getTemplateId()).thenReturn("templateId");
		MailAttachment mailAttachment = new MailAttachment();
		when(delegate.getAttachments()).thenReturn(new MailAttachment[] { mailAttachment });
		String subject = "subject of mail";
		when(delegate.getSubject()).thenReturn(subject);

		User user = new EmfUser();
		user.add(ResourceProperties.EMAIL, "fromUser@sirma.bg");
		when(delegate.getSendFrom()).thenReturn(user);

		service.init();
		service.templateLoader.putTemplate("templateId", "templateContent");
		Mockito.when(templateLoader.getReader(Matchers.anyObject(), Matchers.anyString())).thenReturn(
				new StringReader("templateContent"));
		Mockito.when(templateLoader.findTemplateSource("templateId")).thenReturn(new StringTemplateLoader());
		service.sendEmail(delegate, "id");

		verify(mailService).enqueueMessage(Arrays.asList("user@sirma.bg"), subject, "id", "templateContent",
				"fromUser@sirma.bg", mailAttachment);
	}

	@Test
	public void sendEmail_withAttachments_sendFromNull_mailServiceCalled()
			throws TemplateNotFoundException, MalformedTemplateNameException, ParseException, IOException {
		when(notificationEnabled.get()).thenReturn(true);

		MailNotificationContext delegate = mock(MailNotificationContext.class);
		when(delegate.getSendTo()).thenReturn(Arrays.asList("user@sirma.bg"));
		when(delegate.getTemplateId()).thenReturn("templateId");

		service.init();
		service.templateLoader.putTemplate("templateId", "templateContent");
		Mockito.when(templateLoader.getReader(Matchers.anyObject(), Matchers.anyString())).thenReturn(
				new StringReader("templateContent"));
		Mockito.when(templateLoader.findTemplateSource("templateId")).thenReturn(new StringTemplateLoader());
		service.sendEmail(delegate, "id");

		verify(mailService).enqueueMessage(Arrays.asList("user@sirma.bg"), null, "id", "templateContent");
	}

	@Test
	public void sendEmail_withAttachments_instanceWithParent_mailServiceCalled()
			throws TemplateNotFoundException, MalformedTemplateNameException, ParseException, IOException {
		when(notificationEnabled.get()).thenReturn(true);

		MailNotificationContext delegate = mock(MailNotificationContext.class);
		when(delegate.getSendTo()).thenReturn(Arrays.asList("user@sirma.bg"));
		when(delegate.getTemplateId()).thenReturn("templateId");

		Map<String, Object> model = new HashMap<>();
		ScriptInstance node = mock(ScriptInstance.class);
		EmfInstance instance = new EmfInstance();
		when(node.getTarget()).thenReturn(instance);
		when(node.getId()).thenReturn("instance-id");
		instance.setId("instance-id");
		node.setTarget(instance);
		model.put("target", node);

		when(delegate.getModel()).then(a -> {
			return model;
		});

		TypeConverterUtil.setTypeConverter(typeConverter);
		when(typeConverter.convert(eq(InstanceReference.class), any(Instance.class)))
				.then(a -> new InstanceReferenceMock(a.getArgumentAt(1, Instance.class)));

		service.init();
		Mockito.when(templateLoader.getReader(Matchers.anyObject(), Matchers.anyString())).thenReturn(
				new StringReader("templateContent"));
		Mockito.when(templateLoader.findTemplateSource("templateId")).thenReturn(new StringTemplateLoader());
		service.templateLoader.putTemplate("templateId", "templateContent");
		service.sendEmail(delegate, "id");

		verify(mailService).enqueueMessage(Arrays.asList("user@sirma.bg"), null, "id", "templateContent");
	}

}
