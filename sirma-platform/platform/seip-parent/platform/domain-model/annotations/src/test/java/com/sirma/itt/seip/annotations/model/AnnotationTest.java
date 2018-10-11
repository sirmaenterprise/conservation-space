package com.sirma.itt.seip.annotations.model;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.io.Serializable;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Stream;

import org.junit.Test;

import com.sirma.itt.seip.domain.instance.EmfInstance;
import com.sirma.itt.seip.domain.instance.Instance;
import com.sirma.itt.seip.resources.EmfUser;
import com.sirma.itt.seip.resources.User;
import com.sirma.itt.semantic.model.vocabulary.EMF;
import com.sirma.itt.semantic.model.vocabulary.OA;

/**
 * Test for {@link Annotation}
 *
 * @author BBonev
 */
public class AnnotationTest {

	@Test
	public void isForEdit() throws Exception {
		Annotation annotation = new Annotation();
		annotation.add(OA.MOTIVATED_BY.toString(), OA.EDITING);
		assertTrue(annotation.isForEdit());
	}

	@Test
	public void isForEditInCollection() throws Exception {
		Annotation annotation = new Annotation();
		annotation.add(OA.MOTIVATED_BY.toString(), (Serializable) Arrays.asList(OA.EDITING));
		assertTrue(annotation.isForEdit());
	}

	@Test
	public void isNew() throws Exception {
		Annotation annotation = new Annotation();
		assertTrue(annotation.isNew());

		annotation.setId("emf:annotationId");
		assertFalse(annotation.isNew());
	}

	@Test
	public void getCurrentStatus() {
		Annotation annotation = new Annotation();
		assertTrue("INIT".equals(annotation.getCurrentStatus()));

		annotation.setId("emf:annotationId");
		annotation.add(EMF.STATUS.toString(), "OPEN");
		assertTrue("OPEN".equals(annotation.getCurrentStatus()));
	}

	@Test
	public void getTransition() {
		Annotation annotation = new Annotation();
		assertTrue("create".equals(annotation.getTransition()));

		annotation.setId("emf:annotationId");
		assertNull(annotation.getTransition());

		annotation.add(AnnotationProperties.ACTION.toString(), "suspend");
		assertTrue("suspend".equals(annotation.getTransition()));
	}

	@Test
	public void getTopic() {
		Annotation annotation = new Annotation();
		Annotation topic = new Annotation();
		topic.setId("topic");

		annotation.setTopic(topic);
		assertTrue("topic".equals(annotation.getTopic().getId()));
	}

	@Test
	public void isReply() {
		Annotation annotation = new Annotation();
		annotation.add((EMF.PREFIX + ":" + EMF.REPLY_TO.getLocalName()), "emf:123");

		assertTrue(annotation.isReply());
	}

	@Test
	public void isToString() {
		Annotation annotation = new Annotation();

		assertNotNull(annotation.toString());
	}

	@Test
	public void equals() throws Exception {
		Annotation a1 = new Annotation();
		a1.setId("annotation");
		Annotation a2 = new Annotation();
		a2.setId("annotation");

		Annotation reply = new Annotation();
		reply.setId("reply");

		a1.addReply(reply);
		a2.addReply(reply);

		assertTrue(a1.equals(a2));
		assertFalse(a1.equals(reply));

		Instance instance = new EmfInstance();
		instance.setId("annotation");

		assertFalse(a1.equals(instance));
	}

	@Test
	public void testHashCode() throws Exception {
		Annotation a1 = new Annotation();
		a1.setId("annotation");
		Annotation a2 = new Annotation();
		a2.setId("annotation");

		Annotation reply = new Annotation();
		reply.setId("reply");

		a1.addReply(reply);
		a2.addReply(reply);

		assertTrue(a1.hashCode() == a2.hashCode());
		assertFalse(a1.hashCode() == reply.hashCode());
	}

	@Test
	public void expandUsers() throws Exception {
		Annotation annotation = new Annotation();

		annotation.expandUsers(id -> null);

		annotation.add(EMF.CREATED_BY.toString(), "emf:admin");
		annotation.add(EMF.MODIFIED_BY.toString(), "emf:admin");

		EmfUser user = new EmfUser("admin");
		user.setId("emf:admin");

		annotation.expandUsers(id -> null);
		annotation.expandUsers(id -> user);

		assertTrue(annotation.getCreatedBy() instanceof User);
		assertTrue(annotation.get(EMF.PREFIX + ":" + EMF.CREATED_BY.getLocalName()) instanceof User);
		assertFalse(annotation.isPropertyPresent(EMF.CREATED_BY.toString()));

		assertTrue(annotation.getModifiedBy() instanceof User);
		assertTrue(annotation.get(EMF.PREFIX + ":" + EMF.MODIFIED_BY.getLocalName()) instanceof User);
		assertFalse(annotation.isPropertyPresent(EMF.MODIFIED_BY.toString()));

		annotation.expandUsers(id -> user);
	}

	@Test
	public void getUsers() throws Exception {
		Annotation annotation = new Annotation();
		Stream<Serializable> users = annotation.getUsers();
		assertNotNull(users);
		assertEquals(2L, users.count());

		annotation.add(EMF.CREATED_BY.toString(), "emf:admin");
		annotation.add(EMF.MODIFIED_BY.toString(), "emf:admin");

		users = annotation.getUsers();
		assertNotNull(users);
		assertEquals(2L, users.count());
	}

	@Test
	public void getMentionedUsers() {
		Annotation annotation = new Annotation();
		annotation.add(EMF.MENTIONED_USERS.toString(), "emf:userId");

		Set<Serializable> users = new HashSet();
		users.add("emf:userId");

		assertTrue(users.equals(annotation.getMentionedUsers()));
	}

	@Test
	public void getCommentsOn() {
		Annotation annotation = new Annotation();
		annotation.add(EMF.COMMENTS_ON.toString(),
				"http://ittruse.ittbg.com/ontology/enterpriseManagementFramework#123456789");

		assertTrue("http://ittruse.ittbg.com/ontology/enterpriseManagementFramework#123456789"
				.equals(annotation.getCommentsOn()));
	}
}
