package com.sirma.itt.seip.domain.instance;

import static com.sirma.itt.seip.domain.instance.DefaultProperties.PRIMARY_CONTENT_ID;
import static com.sirma.itt.seip.domain.instance.DefaultProperties.READ_ALLOWED;
import static com.sirma.itt.seip.domain.instance.DefaultProperties.WRITE_ALLOWED;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

/**
 * Tests for {@link Instance}.
 *
 * @author A. Kunchev
 */
@SuppressWarnings("static-method")
public class InstanceTest {

	@Test
	public void hasPrimaryContent_doesNotContains() {
		Instance instance = new EmfInstance();
		assertFalse(instance.isUploaded());
	}

	@Test
	public void hasPrimaryContent_contains() {
		Instance instance = new EmfInstance();
		instance.add(PRIMARY_CONTENT_ID, "content-id");
		assertTrue(instance.isUploaded());
	}

	@Test
	public void isReadAllowed_notAllowed() {
		Instance instance = new EmfInstance();
		instance.add(READ_ALLOWED, false);
		assertFalse(instance.isReadAllowed());
	}

	@Test
	public void isReadAllowed_allowed() {
		Instance instance = new EmfInstance();
		instance.add(READ_ALLOWED, true);
		assertTrue(instance.isReadAllowed());
	}

	@Test
	public void isWriteAllowed_notAllowed() {
		Instance instance = new EmfInstance();
		instance.add(WRITE_ALLOWED, false);
		assertFalse(instance.isWriteAllowed());
	}

	@Test
	public void isWriteAllowed_allowed() {
		Instance instance = new EmfInstance();
		instance.add(WRITE_ALLOWED, true);
		assertTrue(instance.isWriteAllowed());
	}

}
