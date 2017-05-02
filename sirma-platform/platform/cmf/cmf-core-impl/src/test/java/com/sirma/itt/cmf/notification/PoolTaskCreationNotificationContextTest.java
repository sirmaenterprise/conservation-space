package com.sirma.itt.cmf.notification;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.when;

import java.util.Arrays;
import java.util.Collections;
import java.util.Map;

import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import com.sirma.itt.seip.convert.TypeConverter;
import com.sirma.itt.seip.convert.TypeConverterUtil;
import com.sirma.itt.seip.domain.instance.DefaultProperties;
import com.sirma.itt.seip.domain.instance.EmfInstance;
import com.sirma.itt.seip.domain.instance.Instance;
import com.sirma.itt.seip.domain.instance.InstanceReference;
import com.sirma.itt.seip.mail.MailResourceProvider;
import com.sirma.itt.seip.resources.EmfUser;
import com.sirma.itt.seip.resources.Resource;
import com.sirma.itt.seip.resources.ResourceProperties;
import com.sirma.itt.seip.testutil.mocks.InstanceReferenceMock;

/**
 * Test for {@link PoolTaskCreationNotificationContext}.
 *
 * @author A. Kunchev
 */
public class PoolTaskCreationNotificationContextTest {

	private static final String CASE_ID = "caseId";

	@InjectMocks
	private PoolTaskCreationNotificationContext context;

	@Mock
	private MailResourceProvider mailResourceProvider;

	@Mock
	private TypeConverter typeConverter;

	@Before
	public void setup() {
		MockitoAnnotations.initMocks(this);
		when(mailResourceProvider.getLabel(anyString())).thenReturn("New task of type {0}");

		when(mailResourceProvider.getDisplayableProperty(any(Instance.class), any(Resource.class), anyString()))
				.thenReturn("task");

		TypeConverterUtil.setTypeConverter(typeConverter);
		when(typeConverter.convert(eq(InstanceReference.class), any(Instance.class)))
				.then(a -> new InstanceReferenceMock(a.getArgumentAt(1, Instance.class)));
	}

	@Test
	public void getSendTo_nullUser_emptyCollection() {
		context = new PoolTaskCreationNotificationContext(new EmfInstance(), null, mailResourceProvider);
		assertEquals(Collections.emptyList(), context.getSendTo());
	}

	@Test
	public void getSendTo_nullEmail_emptyCollection() {
		context = new PoolTaskCreationNotificationContext(new EmfInstance(), new EmfUser(), mailResourceProvider);
		assertEquals(Collections.emptyList(), context.getSendTo());
	}

	@Test
	public void getSendTo_emptyEmail_emptyCollection() {
		EmfUser user = new EmfUser();
		user.add(ResourceProperties.EMAIL, "");
		context = new PoolTaskCreationNotificationContext(new EmfInstance(), user, mailResourceProvider);
		assertEquals(Collections.emptyList(), context.getSendTo());
	}

	@Test
	public void getSendTo_oneEmail_notEmptyCollection() {
		EmfUser user = new EmfUser();
		String email = "user@tenant.com";
		user.add(ResourceProperties.EMAIL, email);
		context = new PoolTaskCreationNotificationContext(new EmfInstance(), user, mailResourceProvider);
		assertEquals(email, context.getSendTo().iterator().next());
	}

	@Test
	public void getTemplateId_poolTaskTemplate() {
		context = new PoolTaskCreationNotificationContext(null, null, mailResourceProvider);
		String templateId = context.getTemplateId();
		assertEquals("email_task_pooled", templateId);
	}

	@Test
	public void getSubject_nullTaskType_unformattedMsg() {
		when(mailResourceProvider.getDisplayableProperty(any(Instance.class), any(Resource.class), anyString()))
				.thenReturn("");
		context = new PoolTaskCreationNotificationContext(new EmfInstance(), new EmfUser(), mailResourceProvider);
		String subject = context.getSubject();
		assertEquals("New task of type ", subject);
	}

	@Test
	public void getSubject_notNullTaskType_msgWithType() {
		EmfInstance task = new EmfInstance();
		task.add(DefaultProperties.TYPE, "task");
		task.setRevision(1L);
		context = new PoolTaskCreationNotificationContext(task, new EmfUser(), mailResourceProvider);
		String subject = context.getSubject();
		assertEquals("New task of type task", subject);
	}

	@Test
	public void getSendTo_userEmailResult() {
		EmfUser user = new EmfUser("userId");
		user.add(ResourceProperties.EMAIL, "user@sirma.bg");
		context = new PoolTaskCreationNotificationContext(null, user, mailResourceProvider);
		assertEquals(Arrays.asList("user@sirma.bg"), context.getSendTo());
	}

	@Test
	public void getModel_populatedModel() {
		EmfInstance parent = new EmfInstance();
		parent.setId(CASE_ID);
		EmfInstance task = new EmfInstance();
		task.setOwningInstance(parent);
		EmfUser user = new EmfUser("user");
		context = new PoolTaskCreationNotificationContext(task, user, mailResourceProvider);
		Map<? extends String, ? extends Object> model = context.getModel();
		assertNotNull(model);
		assertEquals(CASE_ID, ((Instance) model.get("rootContext")).getId());
	}

}
