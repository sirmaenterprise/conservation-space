package com.sirma.itt.seip.instance.version;

import static com.sirma.itt.seip.domain.instance.DefaultProperties.VERSION;
import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.when;

import java.util.Optional;

import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import com.sirma.itt.seip.domain.instance.EmfInstance;
import com.sirma.itt.seip.domain.instance.Instance;
import com.sirma.itt.seip.instance.InstanceSaveContext;
import com.sirma.itt.seip.instance.InstanceTypeResolver;
import com.sirma.itt.seip.instance.state.Operation;
import com.sirma.itt.seip.testutil.mocks.InstanceReferenceMock;

/**
 * Test for {@link HandleVersionPropertyStep}.
 *
 * @author A. Kunchev
 */
public class HandleVersionPropertyStepTest {

	@InjectMocks
	private HandleVersionPropertyStep step;

	@Mock
	private InstanceTypeResolver instanceTypeResolver;

	@Mock
	private InstanceVersionService instanceVersionService;

	@Before
	public void setup() {
		step = new HandleVersionPropertyStep();
		MockitoAnnotations.initMocks(this);
	}

	@Test
	public void beforeSave_instanceNotPersistedWithVersionProperty_versionUnchanged() {
		Instance instance = new EmfInstance();
		instance.setId("not-persisted-instance-id");
		instance.add(VERSION, "1.0");
		InstanceSaveContext context = InstanceSaveContext.create(instance, new Operation());

		when(instanceTypeResolver.resolveReference("not-persisted-instance-id")).thenReturn(Optional.empty());

		step.beforeSave(context);
		assertEquals("1.0", context.getInstance().getString(VERSION));
	}

	@Test
	public void beforeSave_instanceNotPersistedWithOutVersionProperty_versionPropertySet() {
		doAnswer(a -> a.getArgumentAt(0, Instance.class).add(VERSION, "10.0"))
				.when(instanceVersionService)
					.populateVersion(any(Instance.class));
		Instance instance = new EmfInstance();
		instance.setId("not-persisted-instance-id");
		InstanceSaveContext context = InstanceSaveContext.create(instance, new Operation());

		when(instanceTypeResolver.resolveReference("not-persisted-instance-id")).thenReturn(Optional.empty());

		step.beforeSave(context);
		assertEquals("10.0", context.getInstance().getString(VERSION));
	}

	@Test
	public void beforeSave_instancePersistedWithoutVersionProperty_versionPropertySet() {
		doAnswer(a ->
			{
				a.getArgumentAt(0, Instance.class).add(VERSION, "1.0");
				return true;
			}).when(instanceVersionService).populateVersion(any(Instance.class));
		Instance instance = new EmfInstance();
		instance.setId("persisted-instance-id");
		InstanceSaveContext context = InstanceSaveContext.create(instance, new Operation());

		when(instanceTypeResolver.resolveReference("persisted-instance-id"))
		.thenReturn(Optional.of(new InstanceReferenceMock(instance)));

		step.beforeSave(context);
		assertEquals("1.0", context.getInstance().getString(VERSION));
	}

	@Test
	public void beforeSave_instancePersistedModeNone_versionUnchanged() {
		Instance instance = new EmfInstance();
		instance.setId("persisted-instance-id");
		instance.add(VERSION, "2.27");
		InstanceSaveContext context = InstanceSaveContext.create(instance, new Operation());
		context.getVersionContext().setVersionMode(VersionMode.NONE);

		when(instanceTypeResolver.resolveReference("persisted-instance-id"))
				.thenReturn(Optional.of(new InstanceReferenceMock(instance)));

		step.beforeSave(context);
		assertEquals("2.27", context.getInstance().getString(VERSION));
	}

	@Test
	public void beforeSave_instancePersistedModeUpdate_versionUnchanged() {
		Instance instance = new EmfInstance("persisted-instance-id");
		instance.add(VERSION, "4.44");
		InstanceSaveContext context = InstanceSaveContext.create(instance, new Operation());
		context.getVersionContext().setVersionMode(VersionMode.UPDATE);

		when(instanceTypeResolver.resolveReference("persisted-instance-id"))
				.thenReturn(Optional.of(new InstanceReferenceMock(instance)));

		step.beforeSave(context);
		assertEquals("4.44", context.getInstance().getString(VERSION));
	}

	@Test
	public void beforeSave_instancePersistedModeMinor_minorVersionIncremented() {
		Instance instance = new EmfInstance();
		instance.setId("persisted-instance-id");
		instance.add(VERSION, "1.0");
		InstanceSaveContext context = InstanceSaveContext.create(instance, new Operation());
		context.getVersionContext().setVersionMode(VersionMode.MINOR);

		when(instanceTypeResolver.resolveReference("persisted-instance-id"))
				.thenReturn(Optional.of(new InstanceReferenceMock(instance)));

		step.beforeSave(context);
		assertEquals("1.1", context.getInstance().getString(VERSION));
	}

	@Test
	public void beforeSave_instancePersistedModeMajor_majorVersionIncrementedMinorResetToZero() {
		Instance instance = new EmfInstance();
		instance.setId("persisted-instance-id");
		instance.add(VERSION, "1.15");
		InstanceSaveContext context = InstanceSaveContext.create(instance, new Operation());
		context.getVersionContext().setVersionMode(VersionMode.MAJOR);

		when(instanceTypeResolver.resolveReference("persisted-instance-id"))
				.thenReturn(Optional.of(new InstanceReferenceMock(instance)));

		step.beforeSave(context);
		assertEquals("2.0", context.getInstance().getString(VERSION));
	}

	@Test
	public void afterSave_setsTempVersionModeToUpdate() {
		Instance instance = new EmfInstance();
		step.afterSave(InstanceSaveContext.create(instance, new Operation()));
		assertEquals(VersionMode.UPDATE.toString(), instance.getString(VersionProperties.VERSION_MODE));
	}

	@Test
	public void getName() {
		assertEquals("setInstanceVersion", step.getName());
	}
}