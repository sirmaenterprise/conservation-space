package com.sirma.itt.seip.instance.version;

import static com.sirma.itt.seip.collections.CollectionUtils.emptyList;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;

import java.util.Collections;
import java.util.Set;

import org.junit.Test;

/**
 * Tests for {@link VersionProperties}.
 *
 * @author Boyan Tonchev
 * @author A. Kunchev
 */
@SuppressWarnings("static-method")
public class VersionPropertiesTest {

	@Test
	public void getAll_returnsAllProperties() {
		assertEquals(11, VersionProperties.getAll().size());
	}

	@Test
	public void should_returnAllWhenNothingToSkip() {
		// null branch
		Set<String> properties = VersionProperties.get(null);
		assertEquals(VersionProperties.getAll().size(), properties.size());

		// empty branch
		Set<String> versionProperties = VersionProperties.get(emptyList());
		assertEquals(VersionProperties.getAll().size(), versionProperties.size());
	}

	@Test
	public void should_ReturnVersionPropertiesWithoutSkippedOne() {
		Set<String> versionProperties = VersionProperties.get(
				Collections.singleton(VersionProperties.DEFINITION_ID));

		assertFalse(versionProperties.contains(VersionProperties.DEFINITION_ID));
	}
}