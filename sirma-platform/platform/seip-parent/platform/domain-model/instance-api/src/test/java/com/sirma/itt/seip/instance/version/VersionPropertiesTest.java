package com.sirma.itt.seip.instance.version;

import java.util.Collections;
import java.util.Set;

import org.junit.Assert;
import org.junit.Test;

/**
 * Tests for {@link VersionProperties}.
 *
 * @author Boyan Tonchev.
 */
public class VersionPropertiesTest {

	@Test
	public void should_ReturnVersionPropertiesWithoutSkippedOne() {
		Set<String> versionProperties = VersionProperties.getVersionProperties(
				Collections.singleton(VersionProperties.DEFINITION_ID));

		Assert.assertFalse(versionProperties.contains(VersionProperties.DEFINITION_ID));
	}
}