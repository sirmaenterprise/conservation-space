package com.sirma.itt.seip.instance.actions.delete;

import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Optional;

import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import com.sirma.itt.seip.domain.event.AuditableEvent;
import com.sirma.itt.seip.event.EventService;
import com.sirma.itt.seip.exceptions.InstanceNotFoundException;
import com.sirma.itt.seip.instance.DomainInstanceService;
import com.sirma.itt.seip.instance.InstanceTypeResolver;
import com.sirma.itt.seip.rest.exceptions.BadRequestException;
import com.sirma.itt.seip.testutil.mocks.InstanceReferenceMock;

/**
 * Test for {@link DeleteAction}.
 *
 * @author A. Kunchev
 */
public class DeleteActionTest {

	@InjectMocks
	private DeleteAction action;

	@Mock
	private DomainInstanceService domainInstanceService;

	@Mock
	private InstanceTypeResolver instanceTypeResolver;

	@Mock
	private EventService eventService;

	@Before
	public void setup() {
		action = new DeleteAction();
		MockitoAnnotations.initMocks(this);
	}

	@Test
	public void getName() {
		assertEquals(DeleteRequest.DELETE_OPERATION, action.getName());
	}

	@Test(expected = BadRequestException.class)
	public void prerform_nullInstanceIds() {
		DeleteRequest request = new DeleteRequest();
		request.setTargetId(null);
		action.perform(request);
		verify(domainInstanceService, never()).delete(anyString());
	}

	@Test(expected = BadRequestException.class)
	public void prerform_emptyInstanceIds() {
		DeleteRequest request = new DeleteRequest();
		request.setTargetId("");
		action.perform(request);
		verify(domainInstanceService, never()).delete(anyString());
	}

	@Test(expected = InstanceNotFoundException.class)
	public void prerform_failedToLoadInstance() {
		DeleteRequest request = new DeleteRequest();
		request.setTargetId("instance-id");
		when(instanceTypeResolver.resolveReference("instance-id")).thenReturn(Optional.empty());
		action.perform(request);
		verify(domainInstanceService, never()).delete("instance-id");
		verify(eventService, never()).fire(any(AuditableEvent.class));
	}

	@Test
	public void prerform_successful() {
		DeleteRequest request = new DeleteRequest();
		request.setTargetId("instance-id");
		when(instanceTypeResolver.resolveReference("instance-id"))
				.thenReturn(Optional.of(InstanceReferenceMock.createGeneric("instance-id")));
		action.perform(request);
		verify(domainInstanceService).delete("instance-id");
		verify(eventService).fire(any(AuditableEvent.class));
	}

}
