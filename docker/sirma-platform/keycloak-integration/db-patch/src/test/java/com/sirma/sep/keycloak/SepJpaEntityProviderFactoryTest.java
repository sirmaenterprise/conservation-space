package com.sirma.sep.keycloak;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import org.junit.Test;
import org.keycloak.connections.jpa.entityprovider.JpaEntityProvider;

/**
 * Tests for {@link SepJpaEntityProviderFactory}.
 *
 * @author smustafov
 */
public class SepJpaEntityProviderFactoryTest {

	@Test
	public void should_ReturnCorrectId() {
		SepJpaEntityProviderFactory providerFactory = new SepJpaEntityProviderFactory();
		assertEquals(SepJpaEntityProviderFactory.ID, providerFactory.getId());
	}

	@Test
	public void should_CreateCorrectProvider() {
		SepJpaEntityProviderFactory providerFactory = new SepJpaEntityProviderFactory();
		JpaEntityProvider entityProvider = providerFactory.create(null);
		assertTrue(entityProvider instanceof SepJpaEntityProvider);
	}

}
