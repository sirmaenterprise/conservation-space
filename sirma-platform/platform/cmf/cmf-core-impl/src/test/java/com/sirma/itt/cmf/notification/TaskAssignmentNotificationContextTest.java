package com.sirma.itt.cmf.notification;

import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.when;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Map;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import com.sirma.itt.seip.domain.instance.DefaultProperties;
import com.sirma.itt.seip.domain.instance.EmfInstance;
import com.sirma.itt.seip.domain.instance.Instance;
import com.sirma.itt.seip.mail.MailResourceProvider;
import com.sirma.itt.seip.resources.EmfUser;
import com.sirma.itt.seip.resources.Resource;
import com.sirma.itt.seip.resources.ResourceProperties;

public class TaskAssignmentNotificationContextTest {

	private TaskAssignmentNotificationContext context;

	@Mock
	private MailResourceProvider mailResourceProvider;

	@Before
	public void setup() {
		MockitoAnnotations.initMocks(this);
		when(mailResourceProvider.getLabel(anyString())).thenReturn("New task of type {0}");
	}


	@Test
	public void getTemplateId() {
		context = new TaskAssignmentNotificationContext(null, new EmfUser(), mailResourceProvider);
		String templateId = context.getTemplateId();
		assertEquals("email_task_assign", templateId);
	}


	@Test
	public void getSubject_nullProperty() {
		context = new TaskAssignmentNotificationContext(new EmfInstance(), new EmfUser(), mailResourceProvider);
		when(mailResourceProvider.getDisplayableProperty(any(Instance.class), any(Resource.class), anyString()))
				.thenReturn(null);

		String subject = context.getSubject();
		assertEquals("New task of type ", subject);
	}

	@Test
	public void getSubject_notNullProperty() {
		when(mailResourceProvider.getDisplayableProperty(any(Instance.class), any(Resource.class), anyString()))
				.thenReturn("taskType");
		context = new TaskAssignmentNotificationContext(new EmfInstance(), new EmfUser(), mailResourceProvider);

		String subject = context.getSubject();
		assertEquals("New task of type taskType", subject);
	}

	@Test
	public void getSendTo_nullUser() {
		context = new TaskAssignmentNotificationContext(null, null, mailResourceProvider);
		Collection<String> sendTo = context.getSendTo();
		assertEquals(Collections.emptyList(), sendTo);
	}

	@Test
	public void getSendTo_notNullUser_withoutEmail() {
		context = new TaskAssignmentNotificationContext(null, new EmfUser(), mailResourceProvider);
		Collection<String> sendTo = context.getSendTo();
		assertEquals(Collections.emptyList(), sendTo);
	}

	@Test
	public void getSendTo_notNullUser() {
		EmfUser user = new EmfUser();
		user.add(ResourceProperties.EMAIL, "user@sirma.bg");
		context = new TaskAssignmentNotificationContext(null, user, mailResourceProvider);
		Collection<String> sendTo = context.getSendTo();
		assertEquals(Arrays.asList("user@sirma.bg"), sendTo);
	}


	@Test
	public void getModel_propertyTitle() {
		EmfInstance task = new EmfInstance();
		task.add(DefaultProperties.TITLE, "title");

		getModelInternal(task, "title");
	}

	@Test
	public void getModel_propertyName() {
		EmfInstance task = new EmfInstance();
		task.add(DefaultProperties.NAME, "name");

		getModelInternal(task, "name");
	}

	private void getModelInternal(EmfInstance task, String expected) {
		context = new TaskAssignmentNotificationContext(task, new EmfUser(), mailResourceProvider);
		Map<? extends String, ? extends Object> model = context.getModel();
		assertEquals(model.size(), 5);
		assertEquals(expected, model.get(DefaultProperties.TITLE));
	}

}
