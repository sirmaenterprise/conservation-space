package com.sirma.itt.seip.content.actions.icons;

import static org.mockito.Mockito.verify;

import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import com.sirma.itt.seip.instance.actions.Actions;

/**
 * Tests the rest service for the add icons action.
 *
 * @author Nikolay Ch
 */
public class AddIconsRestServiceTest {

	@InjectMocks
	private AddIconsRestService service;

	@Mock
	private Actions actions;

	/**
	 * Setup the mocks.
	 */
	@Before
	public void setup() {
		MockitoAnnotations.initMocks(this);
	}

	/**
	 * Test if the add icons action is executed when the rest service is called.
	 */
	@Test
	public void testExecuteMoveAction() {
		AddIconsRequest request = new AddIconsRequest();
		service.uploadIcons(request);
		verify(actions).callAction(request);
	}

}
