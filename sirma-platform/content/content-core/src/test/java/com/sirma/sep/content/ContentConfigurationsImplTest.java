package com.sirma.sep.content;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.mockito.Matchers.anyCollection;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.runners.MockitoJUnitRunner;

import com.sirma.itt.seip.configuration.convert.ConverterContext;
import com.sirma.itt.seip.configuration.db.ConfigurationManagement;
import com.sirma.sep.content.ContentConfigurationsImpl;

/**
 * Test the content configurations.
 * 
 * @author nvelkov
 */
@RunWith(MockitoJUnitRunner.class)
public class ContentConfigurationsImplTest {

	@Test
	public void should_buildAndInsertSecretKey_whenMissing() {
		ConverterContext context = mock(ConverterContext.class);
		when(context.getRawValue()).thenReturn("");

		ConfigurationManagement management = mock(ConfigurationManagement.class);

		String secret = ContentConfigurationsImpl.buildShareCodeSecretKey(context, null, management);
		verify(management).addConfigurations(anyCollection());
		assertNotNull(secret);
	}

	@Test
	public void should_returnSecretKey_whenNotMissing() {
		ConverterContext context = mock(ConverterContext.class);
		when(context.getRawValue()).thenReturn("secret");

		ConfigurationManagement management = mock(ConfigurationManagement.class);

		String secret = ContentConfigurationsImpl.buildShareCodeSecretKey(context, null, management);
		assertEquals("secret", secret);
	}
}
