package com.sirma.itt.seip.tenant.wizard;

import static org.junit.Assert.assertEquals;

import java.util.Arrays;
import java.util.Collection;

import org.junit.Test;

import com.sirma.itt.seip.configuration.db.Configuration;
import com.sirma.itt.seip.tenant.wizard.exception.TenantDeletionException;

/**
 * Tests for {@link TenantDeletionContext}.
 *
 * @author smustafov
 */
public class TenantDeletionContextTest {

	@Test
	public void getConfigValue_Should_ReturnConfigValue_When_ConfigExists() {
		TenantDeletionContext context = new TenantDeletionContext(null, false);

		context.setConfigurations(createConfigurations());

		assertEquals("sample", context.getConfigValue("test2.config"));
		assertEquals("true", context.getConfigValue("test1.config"));
	}

	@Test(expected = TenantDeletionException.class)
	public void getConfigValue_Should_ThrowException_When_ConfigMissing() {
		TenantDeletionContext context = new TenantDeletionContext(null, false);

		context.setConfigurations(createConfigurations());

		context.getConfigValue("notExisting");
	}

	private Collection<Configuration> createConfigurations() {
		Configuration config1 = new Configuration("test1.config", "true");
		config1.setRawValue("true");

		Configuration config2 = new Configuration("test2.config", "sample");
		config2.setRawValue("sample");

		return Arrays.asList(config1, config2);
	}

}
