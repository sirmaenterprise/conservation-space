package com.sirma.itt.seip.instance.version;

import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import com.sirma.itt.seip.domain.instance.EmfInstance;
import com.sirma.itt.seip.instance.InstanceSaveContext;
import com.sirma.itt.seip.instance.state.Operation;
import com.sirma.sep.content.InstanceContentService;

/**
 * Test for {@link SaveInstanceVersionStep}.
 *
 * @author A. Kunchev
 */
public class SaveInstanceVersionStepTest {

	@InjectMocks
	private SaveInstanceVersionStep step;

	@Mock
	private InstanceVersionService instanceVersionService;

	@Mock
	private InstanceContentService instanceContentService;

	@Before
	public void setup() {
		step = new SaveInstanceVersionStep();
		MockitoAnnotations.initMocks(this);
	}

	@Test
	public void afterSave_versionModeSetToNone() {
		InstanceSaveContext context = InstanceSaveContext.create(new EmfInstance(), new Operation());
		context.getVersionContext().setVersionMode(VersionMode.NONE);
		step.afterSave(context);
		verify(instanceVersionService, never()).saveVersion(any(VersionContext.class));
	}

	@Test
	public void afterSave_versionModeSetToMajor_versionServiceCalled() {
		InstanceSaveContext context = InstanceSaveContext.create(new EmfInstance(), new Operation());
		context.getVersionContext().setVersionMode(VersionMode.MAJOR);
		step.afterSave(context);
		verify(instanceVersionService).saveVersion(any(VersionContext.class));
	}

	@Test
	public void getName() {
		assertEquals("saveInstanceVersion", step.getName());
	}
}