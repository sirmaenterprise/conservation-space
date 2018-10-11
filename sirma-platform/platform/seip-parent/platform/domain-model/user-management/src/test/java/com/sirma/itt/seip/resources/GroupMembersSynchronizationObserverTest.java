package com.sirma.itt.seip.resources;

import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.Spy;

import com.sirma.itt.seip.domain.instance.event.ObjectPropertyAddEvent;
import com.sirma.itt.seip.domain.instance.event.ObjectPropertyRemoveEvent;
import com.sirma.itt.seip.io.ResourceLoadUtil;
import com.sirma.itt.seip.testutil.fakes.TransactionSupportFake;
import com.sirma.itt.seip.tx.TransactionSupport;
import com.sirmaenterprise.sep.jms.api.MessageSender;
import com.sirmaenterprise.sep.jms.api.SendOptions;
import net.javacrumbs.jsonunit.JsonAssert;

/**
 * Test for {@link GroupMembersSynchronizationObserver}
 *
 * @author <a href="mailto:borislav.bonev@sirma.bg">Borislav Bonev</a>
 * @since 30/10/2017
 */
public class GroupMembersSynchronizationObserverTest {
	@InjectMocks
	private GroupMembersSynchronizationObserver observer;

	@Mock
	private MessageSender remoteUserStoreMessageSender;
	@Mock
	private ResourceService resourceService;
	@Mock
	private Resource everyone;

	private TransactionSupport transactionSupport = new TransactionSupportFake();

	@Spy
	private GroupMembersChangesBuffer changesBuffer = new GroupMembersChangesBuffer(transactionSupport);

	@Before
	public void setUp() throws Exception {
		MockitoAnnotations.initMocks(this);
		when(everyone.getId()).thenReturn("emf:everyone");
		when(resourceService.getAllOtherUsers()).thenReturn(everyone);
		when(remoteUserStoreMessageSender.getDefaultSendOptions()).thenReturn(SendOptions.create());

		observer.init();
	}

	@Test
	public void onGroupMemberAdded_shouldDoNothingOnNotCorrectProperty() throws Exception {
		ObjectPropertyAddEvent event = mock(ObjectPropertyAddEvent.class);
		when(event.getObjectPropertyName()).thenReturn("ptop:partOf");

		observer.onGroupMemberAdded(event);

		changesBuffer.flushChanges();

		verifyMessageSenderNotInvoked();
	}

	@Test
	public void onGroupMemberAdded() {
		ObjectPropertyAddEvent event = mockAdd("emf:GROUP_test", "ptop:hasMember","emf:user-test.com");

		observer.onGroupMemberAdded(event);

		changesBuffer.flushChanges();

		String expected = ResourceLoadUtil.loadResource(getClass(), "add-group-members.json");

		verifyMessageSender(expected);
	}

	@Test
	public void onGroupMemberChanged() throws Exception {
		observer.onGroupMemberAdded(mockAdd("emf:GROUP_test", "ptop:hasMember","emf:user3-test.com"));
		observer.onGroupMemberAdded(mockAdd("emf:GROUP_test", "ptop:hasMember","emf:user4-test.com"));
		observer.onGroupMemberRemoved(mockRemove("emf:GROUP_test", "ptop:hasMember","emf:user1-test.com"));
		observer.onGroupMemberRemoved(mockRemove("emf:GROUP_test", "ptop:hasMember","emf:user2-test.com"));

		changesBuffer.flushChanges();

		String expected = ResourceLoadUtil.loadResource(getClass(), "change-group-members.json");

		verifyMessageSender(expected);
	}

	private ObjectPropertyAddEvent mockAdd(String source, String property, String target) {
		ObjectPropertyAddEvent event = mock(ObjectPropertyAddEvent.class);
		when(event.getObjectPropertyName()).thenReturn(property);
		when(event.getSourceId()).thenReturn(source);
		when(event.getTargetId()).thenReturn(target);
		return event;
	}
	private ObjectPropertyRemoveEvent mockRemove(String source, String property, String target) {
		ObjectPropertyRemoveEvent event = mock(ObjectPropertyRemoveEvent.class);
		when(event.getObjectPropertyName()).thenReturn(property);
		when(event.getSourceId()).thenReturn(source);
		when(event.getTargetId()).thenReturn(target);
		return event;
	}

	@Test
	public void onGroupMemberRemoved_shouldDoNothingOnNotCorrectProperty() throws Exception {
		ObjectPropertyRemoveEvent event = mock(ObjectPropertyRemoveEvent.class);
		when(event.getObjectPropertyName()).thenReturn("ptop:partOf");

		observer.onGroupMemberRemoved(event);

		changesBuffer.flushChanges();

		verifyMessageSenderNotInvoked();
	}

	@Test
	public void onGroupMemberRemoved() throws Exception {
		ObjectPropertyRemoveEvent event = mockRemove("emf:GROUP_test", "ptop:hasMember", "emf:user-test.com");

		observer.onGroupMemberRemoved(event);

		changesBuffer.flushChanges();

		String expected = ResourceLoadUtil.loadResource(getClass(), "remove-group-members.json");

		verifyMessageSender(expected);
	}

	@Test
	public void onGroupMemberRemoved_shouldDoNothingIfEveryoneGroup() throws Exception {
		ObjectPropertyRemoveEvent event = mock(ObjectPropertyRemoveEvent.class);
		when(event.getObjectPropertyName()).thenReturn("ptop:hasMember");
		when(event.getSourceId()).thenReturn("emf:everyone");
		when(event.getTargetId()).thenReturn("emf:user-test.com");

		observer.onGroupMemberRemoved(event);

		changesBuffer.flushChanges();

		verifyMessageSenderNotInvoked();
	}

	@Test
	public void onGroupMemberAdd_shouldDoNothingIfEveryoneGroup() throws Exception {
		ObjectPropertyAddEvent event = mock(ObjectPropertyAddEvent.class);
		when(event.getObjectPropertyName()).thenReturn("ptop:hasMember");
		when(event.getSourceId()).thenReturn("emf:everyone");
		when(event.getTargetId()).thenReturn("emf:user-test.com");

		observer.onGroupMemberAdded(event);

		changesBuffer.flushChanges();

		verifyMessageSenderNotInvoked();
	}

	private void verifyMessageSender(String expectedJson) {
		ArgumentCaptor<String> jsonCaptor = ArgumentCaptor.forClass(String.class);
		ArgumentCaptor<SendOptions> optionsCaptor = ArgumentCaptor.forClass(SendOptions.class);

		verify(remoteUserStoreMessageSender).sendText(jsonCaptor.capture(), optionsCaptor.capture());

		assertEquals(RemoteUserStoreQueueListener.MODIFY_GROUP_MEMBERS,
				optionsCaptor.getValue().getProperties().get(RemoteUserStoreQueueListener.OPERATION));
		JsonAssert.assertJsonEquals(expectedJson, jsonCaptor.getValue());
	}

	private void verifyMessageSenderNotInvoked() {
		verify(remoteUserStoreMessageSender, never()).sendText(anyString(), any(SendOptions.class));
	}

}
