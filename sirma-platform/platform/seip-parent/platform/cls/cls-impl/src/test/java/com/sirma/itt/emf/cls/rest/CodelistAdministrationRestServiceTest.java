package com.sirma.itt.emf.cls.rest;

import com.sirma.itt.seip.domain.codelist.event.ResetCodelistEvent;
import com.sirma.itt.seip.event.EventService;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Matchers;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

import javax.ws.rs.core.Response;

/**
 * Tests the code lists reload triggering in {@link CodelistAdministrationRestService}.
 *
 * @author Mihail Radkov
 */
public class CodelistAdministrationRestServiceTest {

	@Mock
	private EventService eventService;

	@InjectMocks
	private CodelistAdministrationRestService codelistAdministrationRestService;

	@Before
	public void beforeEach() {
		MockitoAnnotations.initMocks(this);
	}

	@Test
	public void shouldNotifyForReloading() {
		Response response = codelistAdministrationRestService.reloadCodeLists();
		Mockito.verify(eventService).fire(Matchers.any(ResetCodelistEvent.class));
		Assert.assertEquals(Response.Status.OK.getStatusCode(), response.getStatus());
	}
}
