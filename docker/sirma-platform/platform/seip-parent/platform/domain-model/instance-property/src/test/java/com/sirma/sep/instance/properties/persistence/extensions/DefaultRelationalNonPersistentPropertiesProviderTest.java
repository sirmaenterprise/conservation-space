package com.sirma.sep.instance.properties.persistence.extensions;

import static com.sirma.itt.seip.domain.instance.DefaultProperties.HAS_FAVOURITE;
import static com.sirma.itt.seip.domain.instance.DefaultProperties.HEADER_BREADCRUMB;
import static com.sirma.itt.seip.domain.instance.DefaultProperties.HEADER_COMPACT;
import static com.sirma.itt.seip.domain.instance.DefaultProperties.HEADER_DEFAULT;
import static com.sirma.itt.seip.domain.instance.DefaultProperties.HEADER_TOOLTIP;
import static com.sirma.itt.seip.domain.instance.DefaultProperties.LOCKED_BY;
import static com.sirma.itt.seip.domain.instance.DefaultProperties.LOCKED_BY_MESSAGE;
import static com.sirma.itt.seip.domain.instance.DefaultProperties.LOCKED_INFO;
import static com.sirma.itt.seip.domain.instance.DefaultProperties.MARKED_FOR_DOWNLOAD;
import static com.sirma.itt.seip.domain.instance.DefaultProperties.THUMBNAIL_IMAGE;
import static com.sirma.itt.seip.instance.version.VersionProperties.IS_VERSION;
import static com.sirma.itt.seip.instance.version.VersionProperties.QUERIES_RESULTS;
import static com.sirma.itt.seip.instance.version.VersionProperties.VERSION_CREATED_ON;
import static com.sirma.itt.seip.instance.version.VersionProperties.VERSION_MODE;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.junit.Before;
import org.junit.Test;

/**
 * Test for {@link DefaultRelationalNonPersistentPropertiesProvider}.
 *
 * @author A. Kunchev
 */
public class DefaultRelationalNonPersistentPropertiesProviderTest {

	private static final List<String> PROPERTIES = Arrays.asList(MARKED_FOR_DOWNLOAD, HAS_FAVOURITE, HEADER_BREADCRUMB,
			HEADER_COMPACT, HEADER_DEFAULT, HEADER_TOOLTIP, LOCKED_BY, LOCKED_BY_MESSAGE, LOCKED_INFO, "rdfType",
			"SessionIndex", "Subject", VERSION_CREATED_ON, IS_VERSION, VERSION_MODE, QUERIES_RESULTS, THUMBNAIL_IMAGE);

	private DefaultRelationalNonPersistentPropertiesProvider provider;

	@Before
	public void setup() {
		provider = new DefaultRelationalNonPersistentPropertiesProvider();
	}

	@Test
	public void getNonPersistentProperties() {
		Set<String> nonPersistentProperties = provider.getNonPersistentProperties();
		assertNotNull(nonPersistentProperties);
		assertFalse(nonPersistentProperties.isEmpty());
		assertEquals(17, nonPersistentProperties.size());
		assertEquals(Collections.unmodifiableSet(new HashSet<>(PROPERTIES)), nonPersistentProperties);
	}
}