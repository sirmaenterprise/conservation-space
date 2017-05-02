package com.sirma.itt.seip.instance.actions.delete;

import static org.junit.Assert.assertEquals;

import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Matchers;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

import com.sirma.itt.seip.instance.DomainInstanceService;
import com.sirma.itt.seip.rest.exceptions.BadRequestException;

/**
 * Test for {@link DeleteAction}.
 *
 * @author A. Kunchev
 */
public class InstanceDeleteActionTest {

	@InjectMocks
	private DeleteAction action;

	@Mock
	private DomainInstanceService domainInstanceService;

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
	public void rerform_nullInstanceIds() {
		DeleteRequest request = new DeleteRequest();
		request.setTargetId(null);
		action.perform(request);
		Mockito.verify(domainInstanceService, Mockito.never()).delete(Matchers.anyString());
	}

	@Test(expected = BadRequestException.class)
	public void rerform_emptyInstanceIds() {
		DeleteRequest request = new DeleteRequest();
		request.setTargetId("");
		action.perform(request);
		Mockito.verify(domainInstanceService, Mockito.never()).delete(Matchers.anyString());
	}

	@Test
	public void rerform_successful() {
		DeleteRequest request = new DeleteRequest();
		request.setTargetId("instance-id");
		action.perform(request);
		Mockito.verify(domainInstanceService).delete("instance-id");
	}

}
