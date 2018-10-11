package com.sirma.itt.seip.instance.version.compare;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.util.HashMap;

import org.junit.Before;
import org.junit.Test;
import org.mockito.MockitoAnnotations;

/**
 * Test for {@link VersionCompareContext}.
 *
 * @author A. Kunchev
 */
@SuppressWarnings("static-method")
public class VersionCompareContextTest {

	@Before
	public void setUp() {
		MockitoAnnotations.initMocks(this);
	}

	@Test
	public void create() {
		VersionCompareContext context = VersionCompareContext
				.create("instance-id-v1.6", "instance-id-v1.8", new HashMap<>())
					.setOriginalInstanceId("instance-id");
		assertNotNull(context);
		assertEquals("instance-id-v1.6", context.getFirstIdentifier());
		assertEquals("instance-id-v1.8", context.getSecondIdentifier());
		assertEquals("instance-id", context.getOriginalInstanceId());
		assertNotNull(context.getAuthenticationHeaders());
	}

}
