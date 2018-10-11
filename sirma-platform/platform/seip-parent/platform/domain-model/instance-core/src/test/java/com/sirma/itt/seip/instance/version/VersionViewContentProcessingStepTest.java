package com.sirma.itt.seip.instance.version;

import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.verify;

import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import com.sirma.itt.seip.domain.instance.EmfInstance;
import com.sirma.itt.seip.event.EventService;

/**
 * Test for {@link CopyContentOnNewVersionStep}.
 *
 * @author A. Kunchev
 */
public class VersionViewContentProcessingStepTest {

	@InjectMocks
	private VersionViewContentProcessingStep step;

	@Mock
	private EventService eventService;

	@Before
	public void setup() {
		step = new VersionViewContentProcessingStep();
		MockitoAnnotations.initMocks(this);
	}

	@Test
	public void getName() {
		assertEquals("versionViewContentProcessing", step.getName());
	}

	@Test
	public void execute_eventFired() {
		step.execute(VersionContext.create(new EmfInstance()));
		verify(eventService).fire(any(CreateVersionContentEvent.class));
	}

}
