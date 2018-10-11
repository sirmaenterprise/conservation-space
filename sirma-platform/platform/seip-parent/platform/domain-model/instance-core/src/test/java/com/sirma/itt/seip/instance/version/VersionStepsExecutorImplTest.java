package com.sirma.itt.seip.instance.version;

import static org.mockito.Mockito.verify;

import java.util.LinkedList;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.Spy;

import com.sirma.itt.seip.domain.instance.EmfInstance;
import com.sirma.itt.seip.plugin.Plugins;

/**
 * Test for {@link VersionStepsExecutorImpl}.
 *
 * @author A. Kunchev
 */
public class VersionStepsExecutorImplTest {

	@InjectMocks
	private VersionStepsExecutorImpl executor;

	private List<VersionStep> steps = new LinkedList<>();

	@Spy
	private Plugins<VersionStep> versionSteps = new Plugins<>("", steps);

	@Mock
	private VersionStep versionStep;

	@Before
	public void setup() {
		MockitoAnnotations.initMocks(this);
		steps.clear();
		steps.add(versionStep);
	}

	@Test(expected = NullPointerException.class)
	public void execute_nullContext() {
		executor.execute(null);
	}

	@Test
	public void execute_withoutErrors() {
		VersionContext context = VersionContext.create(new EmfInstance());
		executor.execute(context);
		verify(versionStep).execute(context);
	}

}
