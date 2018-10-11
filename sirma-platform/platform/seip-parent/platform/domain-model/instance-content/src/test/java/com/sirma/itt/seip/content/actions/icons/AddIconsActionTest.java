package com.sirma.itt.seip.content.actions.icons;

import static org.mockito.Mockito.times;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Matchers;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

import com.sirma.itt.seip.content.ContentResourceManagerService;
import com.sirma.itt.seip.exception.EmfRuntimeException;

/**
 * Test for the add icons action executor.
 *
 * @author Nikolay Ch
 */
public class AddIconsActionTest {
	@Mock
	private ContentResourceManagerService managerService;

	@InjectMocks
	private AddIconsAction addIconsAction = new AddIconsAction();

	/**
	 * Setup the mocks.
	 */
	@Before
	public void setup() {
		MockitoAnnotations.initMocks(this);
	}

	/**
	 * Test the add icons action with a null.
	 */
	@Test(expected = EmfRuntimeException.class)
	public void testAddIconsRequestNull() {
		addIconsAction.perform(null);
	}

	/**
	 * Test the add icons action with a valid data.
	 */
	@Test
	public void testAddIcons() {
		AddIconsRequest request = new AddIconsRequest();
		request.setUserOperation("addIcons");
		request.setTargetId("targetId");
		Map<Serializable, String> purposeIconMapping = new HashMap<Serializable, String>();
		purposeIconMapping.put(1, "firstIcon");
		request.setPurposeIconMapping(purposeIconMapping);
		Mockito.doNothing().when(managerService).uploadContent(Mockito.any(Serializable.class),
				Matchers.<Map<Serializable, String>> any());
		addIconsAction.perform(request);
		Mockito.verify(managerService, times(1)).uploadContent("targetId", purposeIconMapping);
	}

}