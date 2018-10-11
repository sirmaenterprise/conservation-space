package com.sirma.itt.seip.instance.actions.delete;

import static org.mockito.Mockito.verify;

import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import com.sirma.itt.seip.instance.actions.Actions;

public class DeleteRestServiceTest {

	@Mock
	private Actions actions;

	@InjectMocks
	private DeleteRestService service;

	@Before
	public void init() {
		service = new DeleteRestService();
		MockitoAnnotations.initMocks(this);
	}

	@Test
	public void should_CallActionsService() {
		final DeleteRequest request = new DeleteRequest();

		service.delete(request);

		verify(actions).callAction(request);
	}

}
