package com.sirma.itt.seip.instance.revision.steps;

import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.LinkedList;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.Spy;

import com.sirma.itt.seip.plugin.Plugins;

/**
 * Test for {@link PublishStepRunner}
 *
 * @author BBonev
 */
public class PublishStepRunnerTest {

	@InjectMocks
	private PublishStepRunner stepRunner;

	private List<PublishStep> plugins = new LinkedList<>();
	@Spy
	private Plugins<PublishStep> steps = new Plugins<>(null, plugins);

	@Mock
	private PublishStep step1;
	@Mock
	private PublishStep step2;
	@Mock
	private PublishStep step3;

	@Before
	public void beforeMethod() {
		MockitoAnnotations.initMocks(this);
		plugins.clear();
		plugins.add(step1);
		plugins.add(step2);
		plugins.add(step3);
		when(step1.getName()).thenReturn("step1");
		when(step2.getName()).thenReturn("step2");
		when(step3.getName()).thenReturn("step3");
	}

	@Test
	public void shouldRunOnlySelectedSteps() throws Exception {
		stepRunner.getRunner(new String[] { "step3", "step1" }).run(mock(PublishContext.class));
		verify(step1).execute(any());
		verify(step2, never()).execute(any());
		verify(step3).execute(any());
	}
}
