package com.sirma.sep.keycloak;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

/**
 * Tests for {@link SepJpaEntityProvider}.
 *
 * @author smustafov
 */
public class SepJpaEntityProviderTest {

	@Test
	public void should_ReturnCorrectFactoryId() {
		SepJpaEntityProvider entityProvider = new SepJpaEntityProvider();
		assertEquals(SepJpaEntityProviderFactory.ID, entityProvider.getFactoryId());
	}

	@Test
	public void should_ReturnEmptyListWithEntities() {
		SepJpaEntityProvider entityProvider = new SepJpaEntityProvider();
		assertTrue(entityProvider.getEntities().isEmpty());
	}

	@Test
	public void should_ReturnCorrectChangelogLocation() {
		SepJpaEntityProvider entityProvider = new SepJpaEntityProvider();
		assertEquals(SepJpaEntityProvider.CHANGELOG_FILE, entityProvider.getChangelogLocation());
	}

}
