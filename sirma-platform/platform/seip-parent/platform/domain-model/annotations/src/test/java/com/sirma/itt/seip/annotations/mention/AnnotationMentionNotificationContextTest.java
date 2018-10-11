package com.sirma.itt.seip.annotations.mention;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.Collections;

import org.junit.Before;
import org.junit.Test;

import com.sirma.itt.seip.domain.instance.EmfInstance;
import com.sirma.itt.seip.domain.instance.Instance;
import com.sirma.itt.seip.resources.EmfUser;

public class AnnotationMentionNotificationContextTest {

	private Instance userInstance;
	private Instance commentedInstance;
	private Instance commenterInstance;
	private String commentsOn;
	private String applicationName;
	private String ui2Url;

	@Before
	public void setUpBeforeClass() throws Exception {
		userInstance = new EmfUser();
		userInstance.add("email", "user@email.com");
		commentedInstance = new EmfInstance();
		commenterInstance = new EmfUser();
		commentsOn = "http://ittruse.ittbg.com/ontology/enterpriseManagementFramework#123456789";
		applicationName = "SES";
		ui2Url = "http://10.131.2.162";

	}

	@Test
	public void test() {
		AnnotationMentionNotificationContext context = new AnnotationMentionNotificationContext(userInstance,
				commentedInstance, commenterInstance, commentsOn, applicationName, ui2Url);
		assertTrue("You are mentioned in a comment".equals(context.getSubject()));
		assertTrue(Collections.singletonList("user@email.com").equals(context.getSendTo()));
		assertEquals(6, context.getModel().size());
	}

}
