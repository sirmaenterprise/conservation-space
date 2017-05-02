package com.sirma.itt.seip.instance.version;

import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import com.sirma.itt.seip.content.InstanceContentService;
import com.sirma.itt.seip.domain.instance.EmfInstance;
import com.sirma.itt.seip.instance.InstanceSaveContext;
import com.sirma.itt.seip.instance.state.Operation;

/**
 * Test for {@link CreateInstanceVersionStep}.
 *
 * @author A. Kunchev
 */
public class CreateInstanceVersionStepTest {

	@InjectMocks
	private CreateInstanceVersionStep step;

	@Mock
	private InstanceVersionService instanceVersionService;

	@Mock
	private InstanceContentService instanceContentService;

	@Before
	public void setup() {
		step = new CreateInstanceVersionStep();
		MockitoAnnotations.initMocks(this);
	}

	@Test
	public void afterSave_versionModeSetToNone() {
		InstanceSaveContext context = InstanceSaveContext.create(new EmfInstance(), new Operation());
		context.getVersionContext().setVersionMode(VersionMode.NONE);
		step.afterSave(context);
		verify(instanceVersionService, never()).createVersion(any(VersionContext.class));
	}

	@Test
	public void afterSave_versionModeSetToMajor_versionServiceCalled() {
		InstanceSaveContext context = InstanceSaveContext.create(new EmfInstance(), new Operation());
		context.getVersionContext().setVersionMode(VersionMode.MAJOR);
		step.afterSave(context);
		verify(instanceVersionService).createVersion(any(VersionContext.class));
	}

	@Test
	public void rollbackAfterSave_versionDeleteFailed() {
		doThrow(new IllegalArgumentException()).when(instanceVersionService).deleteVersion("version-instance-id");
		InstanceSaveContext context = InstanceSaveContext
				.create(new EmfInstance(), new Operation())
					.setPropertyIfNotNull("versionInstanceId", "version-instance-id");
		step.rollbackAfterSave(context, new NullPointerException());
		verify(instanceContentService).deleteAllContentForInstance("version-instance-id");
	}

	@Test
	public void rollbackAfterSave_versionContentDeleteFailed() {
		doThrow(new IllegalArgumentException())
				.when(instanceContentService)
					.deleteAllContentForInstance("version-instance-id");
		InstanceSaveContext context = InstanceSaveContext
				.create(new EmfInstance(), new Operation())
					.setPropertyIfNotNull("versionInstanceId", "version-instance-id");
		step.rollbackAfterSave(context, new NullPointerException());
		verify(instanceVersionService).deleteVersion("version-instance-id");
	}

	@Test
	public void rollbackAfterSave() {
		InstanceSaveContext context = InstanceSaveContext
				.create(new EmfInstance(), new Operation())
					.setPropertyIfNotNull("versionInstanceId", "version-instance-id");
		step.rollbackAfterSave(context, new NullPointerException());
		verify(instanceVersionService).deleteVersion("version-instance-id");
		verify(instanceContentService).deleteAllContentForInstance("version-instance-id");
	}

	@Test
	public void getName() {
		assertEquals("createInstanceVersion", step.getName());
	}

}
