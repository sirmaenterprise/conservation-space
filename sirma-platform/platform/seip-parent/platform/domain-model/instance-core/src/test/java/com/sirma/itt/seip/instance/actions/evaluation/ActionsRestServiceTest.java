package com.sirma.itt.seip.instance.actions.evaluation;

import static org.mockito.Matchers.any;
import static org.mockito.Mockito.verify;

import java.util.ArrayList;
import java.util.Arrays;

import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import com.sirma.itt.seip.instance.actions.Actions;

/**
 * Tests for {@link ActionsRestService}.
 *
 * @author A. Kunchev
 */
public class ActionsRestServiceTest {

	@InjectMocks
	private ActionsRestService service;

	@Mock
	private Actions actionsList;

	@Before
	public void setup() {
		service = new ActionsRestService();
		MockitoAnnotations.initMocks(this);
	}

	@Test
	public void getActions_emptyPath() {
		service.getActions("targetId", "contextId", "placeholder", new ArrayList<>());
		verify(actionsList).callAction(any(ActionsListRequest.class));
	}

	@Test
	public void getActions_emptyContextId() {
		service.getActions("targetId", "", "placeholder", Arrays.asList("path"));
		verify(actionsList).callAction(any(ActionsListRequest.class));
	}

	@Test
	public void getActions_nullContextId() {
		service.getActions("targetId", null, "placeholder", Arrays.asList("path"));
		verify(actionsList).callAction(any(ActionsListRequest.class));
	}

	@Test
	public void getActions_all_helperCalled() {
		service.getActions("targetId", "contextId", "placeholder", Arrays.asList("path", "path1"));
		verify(actionsList).callAction(any(ActionsListRequest.class));
	}

}
