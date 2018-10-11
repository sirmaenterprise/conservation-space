package com.sirma.itt.seip.instance;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.util.Date;

import org.junit.Test;

import com.sirma.itt.seip.domain.instance.EmfInstance;
import com.sirma.itt.seip.instance.state.Operation;
import com.sirma.itt.seip.instance.version.VersionContext;
import com.sirma.itt.seip.instance.version.VersionMode;

/**
 * Test for {@link InstanceSaveContext}.
 *
 * @author A. Kunchev
 */
@SuppressWarnings("static-method")
public class InstanceSaveContextTest {

	@Test(expected = NullPointerException.class)
	public void createInstanceOperation_nullInstance() {
		InstanceSaveContext.create(null, new Operation());
	}

	@Test(expected = NullPointerException.class)
	public void createInstanceOperation_nullOperation() {
		InstanceSaveContext.create(new EmfInstance(), null);
	}

	@Test(expected = NullPointerException.class)
	public void createInstanceOperation_nullVersionDate() {
		InstanceSaveContext.create(new EmfInstance(), new Operation(), null);
	}

	@Test
	public void createInstanceOperation_successfulWithDefaultMinorVersionMode() {
		Date versionDate = new Date();
		EmfInstance target = new EmfInstance("instance-id");
		Operation operation = new Operation();
		InstanceSaveContext context = InstanceSaveContext.create(target, operation, versionDate);

		assertNotNull(context);
		assertEquals(versionDate, context.getVersionCreationDate());
		assertEquals(VersionMode.MINOR, context.getVersionContext().getVersionMode());
		assertEquals(target, context.getInstance());
		assertEquals("instance-id", context.getInstanceId());
		assertEquals(operation, context.getOperation());
	}

	@Test(expected = NullPointerException.class)
	public void setVersionMode_nullMode() {
		InstanceSaveContext.create(new EmfInstance(), new Operation()).getVersionContext().setVersionMode(null);
	}

	@Test
	public void setVersionMode_versionModeNone() {
		InstanceSaveContext context = InstanceSaveContext.create(new EmfInstance(), new Operation());
		context.getVersionContext().setVersionMode(VersionMode.NONE);
		assertEquals(VersionMode.NONE, context.getVersionContext().getVersionMode());
	}

	@Test(expected = IllegalArgumentException.class)
	public void setPropertyIfNotNull_nullKey() {
		InstanceSaveContext.create(new EmfInstance(), new Operation()).setPropertyIfNotNull(null, "value");
	}

	@Test(expected = IllegalArgumentException.class)
	public void setPropertyIfNotNull_emptyKey() {
		InstanceSaveContext.create(new EmfInstance(), new Operation()).setPropertyIfNotNull("", "value");
	}

	@Test
	public void setPropertyIfNotNull_nullValue() {
		InstanceSaveContext context = InstanceSaveContext
				.create(new EmfInstance(), new Operation())
					.setPropertyIfNotNull("key", null);
		assertNull(context.get("key"));
	}

	@Test
	public void setPropertyIfNotNull_successfulPropertySet() {
		EmfInstance instance = new EmfInstance();
		InstanceSaveContext context = InstanceSaveContext
				.create(instance, new Operation())
					.setPropertyIfNotNull("property-1", "value")
					.setPropertyIfNotNull("property-2", instance);

		assertNotNull(context.get("property-1"));
		assertNotNull(context.get("property-2"));
		assertEquals("value", context.getIfSameType("property-1", String.class));
		assertEquals(instance, context.getIfSameType("property-2", EmfInstance.class));
	}

	@Test
	public void buildVersionContext_withSetingWidgetProcessing() {
		InstanceSaveContext saveContext = InstanceSaveContext.create(new EmfInstance(), new Operation());
		VersionContext versionContext = saveContext.getVersionContext().setWidgetsProcessing(Boolean.FALSE);
		assertNotNull(versionContext);
		assertNotNull(versionContext.getTargetInstance());
		assertNotNull(versionContext.getCreationDate());
		assertFalse(versionContext.shouldProcessWidgets());
	}

	@Test
	public void buildVersionContext_withoutSetingWidgetProcessing() {
		VersionContext versionContext = InstanceSaveContext
				.create(new EmfInstance(), new Operation())
					.getVersionContext();
		assertNotNull(versionContext);
		assertNotNull(versionContext.getTargetInstance());
		assertNotNull(versionContext.getCreationDate());
		assertTrue(versionContext.shouldProcessWidgets());
	}

	@Test(expected = NullPointerException.class)
	public void disableValidation_shouldFailForEmptyReason() {
		InstanceSaveContext.create(new EmfInstance(), new Operation()).disableValidation("");
	}

	@Test(expected = NullPointerException.class)
	public void disableValidation_shouldFailForNullReason() {
		InstanceSaveContext.create(new EmfInstance(), new Operation()).disableValidation(null);
	}

	@Test
	public void isValidationEnabled_shouldReturnTrueByDefault() {
		assertTrue(InstanceSaveContext.create(new EmfInstance(), new Operation()).isValidationEnabled());
	}

	@Test
	public void disableValidation_shouldChangeValidationPolicy() {
		InstanceSaveContext context = InstanceSaveContext.create(new EmfInstance(), new Operation())
				.disableValidation("test");
		assertFalse(context.isValidationEnabled());
		assertEquals("test", context.getDisableValidationReason());
	}


}
