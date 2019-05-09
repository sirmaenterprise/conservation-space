package com.sirma.itt.seip.content.actions.icons;

import static org.mockito.Matchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.verify;

import java.io.Serializable;
import java.util.Collections;
import java.util.Map;

import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import com.sirma.itt.seip.content.ContentResourceManagerService;

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

	@Before
	public void setup() {
		MockitoAnnotations.initMocks(this);
	}

	@Test
	public void addIcons() {
		AddIconsRequest request = new AddIconsRequest();
		request.setUserOperation("addIcons");
		request.setTargetId("targetId");
		Map<Serializable, String> purposeIconMapping = Collections.singletonMap(1, "firstIcon");
		request.setPurposeIconMapping(purposeIconMapping);
		doNothing().when(managerService).uploadContent(any(Serializable.class), any());
		addIconsAction.perform(request);
		verify(managerService).uploadContent("targetId", purposeIconMapping);
	}
}