package com.sirma.itt.seip.mail;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import java.util.Arrays;

import org.junit.Test;
import org.mockito.Mockito;

import com.sirma.itt.seip.domain.instance.EmfInstance;
import com.sirma.itt.seip.mail.attachments.MailAttachment;
import com.sirma.itt.seip.resources.EmfUser;
import com.sirma.itt.seip.script.ScriptInstance;

/**
 * Test for {@link DefaultNotificationContext}.
 *
 * @author A. Kunchev
 */
public class DefaultNotificationContextTest {

	private DefaultNotificationContext context;

	@Test
	public void getAttachments_nullAttachments_nullResult() {
		context = new DefaultNotificationContext();
		assertEquals(0, context.getAttachments().length);
	}

	@Test
	public void getAttachments_zeroLength_nullResult() {
		context = new DefaultNotificationContext("", null, "", "", null, null, null, new MailAttachment[0]);
		assertEquals(0, context.getAttachments().length);
	}

	@Test
	public void getAttachments_notZeroLength_oneResult() {
		context = new DefaultNotificationContext("", null, "", "", null, null, null, new MailAttachment[1]);
		assertEquals(1, context.getAttachments().length);
	}

	@Test
	public void getSendTo() {
		context = new DefaultNotificationContext("user@sirma.bg", null, "", "", null, null, null);
		assertEquals(Arrays.asList("user@sirma.bg"), context.getSendTo());
	}

	@Test
	public void getSubject_nullSubject() {
		context = new DefaultNotificationContext();
		assertNull(context.getSubject());
	}

	@Test
	public void getSubject_notNullSubject() {
		context = new DefaultNotificationContext("", null, "subject", "", null, null, null);
		assertEquals("subject", context.getSubject());
	}

	@Test
	public void getModel_populatedModel() {
		EmfUser user = new EmfUser();
		ScriptInstance target = Mockito.mock(ScriptInstance.class);
		Mockito.when(target.getTarget()).thenReturn(new EmfInstance());
		context = new DefaultNotificationContext("user@sirma.bg", user, "subject", "template", target,
				new ScriptInstance[1],
				null,
				new MailAttachment());

		assertEquals(5, context.getModel().size());
		assertEquals("user@sirma.bg", context.getModel().get("recipient"));
		assertEquals(user, context.getModel().get("user"));
		assertEquals(target, context.getModel().get("target"));
	}

	@Test
	public void getTemplateId_nullTemplateId() {
		context = new DefaultNotificationContext();
		assertNull(context.getTemplateId());
	}

	@Test
	public void getTemplateId_notNullTemplateId() {
		context = new DefaultNotificationContext("", null, "", "template", null, null, null);
		assertEquals("template", context.getTemplateId());
	}

}
