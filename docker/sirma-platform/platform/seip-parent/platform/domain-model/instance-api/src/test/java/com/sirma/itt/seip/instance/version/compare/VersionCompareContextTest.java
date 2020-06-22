package com.sirma.itt.seip.instance.version.compare;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import org.junit.Test;

/**
 * Test for {@link VersionCompareContext}.
 *
 * @author A. Kunchev
 */
@SuppressWarnings("static-method")
public class VersionCompareContextTest {

	@Test
	public void create() {
		VersionCompareContext context = VersionCompareContext
				.create("instance-id-v1.6", "instance-id-v1.8", "xxx.yyy.zzz")
				.setOriginalInstanceId("instance-id");

		assertNotNull(context);
		assertEquals("instance-id-v1.6", context.getFirstIdentifier());
		assertEquals("instance-id-v1.8", context.getSecondIdentifier());
		assertEquals("instance-id", context.getOriginalInstanceId());
		assertEquals("xxx.yyy.zzz", context.getAuthentication());
	}

}
