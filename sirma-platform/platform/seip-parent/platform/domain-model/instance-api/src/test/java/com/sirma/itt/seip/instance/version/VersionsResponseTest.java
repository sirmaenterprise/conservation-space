package com.sirma.itt.seip.instance.version;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.util.Collections;

import org.junit.Test;

/**
 * Test for {@link VersionsResponse}.
 *
 * @author A. Kunchev
 */
public class VersionsResponseTest {

	@Test
	public void emptyResponse() {
		VersionsResponse emptyResponse = VersionsResponse.emptyResponse();
		assertNotNull(emptyResponse);
		assertEquals(Collections.emptyList(), emptyResponse.getResults());
		assertEquals(0, emptyResponse.getTotalCount());
	}

	@Test
	public void isEmpty_emptyResponceMethod_true() {
		assertTrue(VersionsResponse.emptyResponse().isEmpty());
	}

	@Test
	public void isEmpty_newObject_true() {
		assertTrue(new VersionsResponse().isEmpty());
	}

	@Test
	public void isEmpty_false() {
		VersionsResponse versionsResponse = new VersionsResponse();
		versionsResponse.setTotalCount(100);
		assertFalse(versionsResponse.isEmpty());
	}

}
