package com.sirma.itt.seip.instance.version;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.util.Date;
import java.util.Optional;

import org.junit.Test;

import com.sirma.itt.seip.domain.instance.EmfInstance;
import com.sirma.itt.seip.domain.instance.Instance;

/**
 * Test for {@link VersionContext}.
 *
 * @author A. Kunchev
 */
@SuppressWarnings("static-method")
public class VersionContextTest {

	private static final String INSTANCE_ID = "instance-id";

	@Test(expected = NullPointerException.class)
	public void create_nullInstance() {
		VersionContext.create(null, new Date());
	}

	@Test(expected = NullPointerException.class)
	public void create_nullDate() {
		VersionContext.create(new EmfInstance(), null);
	}

	@Test
	public void create_withCorrectData() {
		EmfInstance instance = new EmfInstance();
		Date date = new Date();
		VersionContext context = VersionContext.create(instance, date);
		assertEquals(instance, context.getTargetInstance());
		assertEquals(date, context.getCreationDate());
	}

	@Test(expected = NullPointerException.class)
	public void setTargetInstanceId_nullInstance() {
		VersionContext.create(null);
	}

	@Test
	public void setTargetInstanceId_notNullInstance() {
		VersionContext context = VersionContext.create(new EmfInstance());
		assertNotNull(context.getTargetInstance());
	}

	@Test
	public void getTargetInstance_withInstance() {
		Instance target = new EmfInstance();
		target.setId(INSTANCE_ID);
		VersionContext context = VersionContext.create(target);
		assertEquals(INSTANCE_ID, context.getTargetInstanceId());
	}

	@Test(expected = NullPointerException.class)
	public void setVersionCreateDate_nullDate() {
		VersionContext.create(new EmfInstance(), null);
	}

	@Test
	public void setVersionCreateDate_notNullDate() {
		VersionContext context = VersionContext.create(new EmfInstance(), new Date());
		assertNotNull(context.getCreationDate());
	}

	@Test
	public void versionContextWithAllData() {
		Instance instance = new EmfInstance();
		instance.setId(INSTANCE_ID);
		Date createdOn = new Date();

		VersionContext context = VersionContext.create(instance, createdOn);

		assertEquals(instance, context.getTargetInstance());
		assertEquals(createdOn, context.getCreationDate());
		assertEquals(INSTANCE_ID, context.getTargetInstanceId());
	}

	@Test(expected = NullPointerException.class)
	public void setVersionInstance_nullInstance() {
		VersionContext context = VersionContext.create(new EmfInstance(), new Date());
		context.setVersionInstance(null);
	}

	@Test
	public void setVersionInstance_notNullInstance() {
		VersionContext context = VersionContext.create(new EmfInstance(), new Date());
		context.setVersionInstance(new EmfInstance());
		assertNotNull(context.getVersionInstance());
	}

	@Test
	public void getVersionInstance_withoutInstance() {
		VersionContext context = VersionContext.create(new EmfInstance(), new Date());
		Optional<Instance> optional = context.getVersionInstance();
		assertFalse(optional.isPresent());
	}

	@Test
	public void getVersionInstanceId_withInstance() {
		VersionContext context = VersionContext.create(new EmfInstance(), new Date());
		Instance target = new EmfInstance();
		target.setId(INSTANCE_ID);
		context.setVersionInstance(target);
		assertEquals(INSTANCE_ID, context.getVersionInstanceId());
	}

	@Test
	public void shouldProcessWidgets_defaultValue() {
		VersionContext context = VersionContext.create(new EmfInstance()).setWidgetsProcessing(null);
		assertTrue(context.shouldProcessWidgets());
	}

	@Test
	public void disableObjectPropertiesVersioning_getterShouldReturnFalse() {
		VersionContext context = VersionContext.create(new EmfInstance()).disableObjectPropertiesVersioning();
		assertFalse(context.isObjectPropertiesVersioningEnabled());
	}

	@Test
	public void isObjectPropertiesVersioningEnabled_defaultValue() {
		VersionContext context = VersionContext.create(new EmfInstance());
		assertTrue(context.isObjectPropertiesVersioningEnabled());
	}

}
