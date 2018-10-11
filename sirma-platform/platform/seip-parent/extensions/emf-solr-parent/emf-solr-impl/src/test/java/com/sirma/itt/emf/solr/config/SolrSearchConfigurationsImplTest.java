package com.sirma.itt.emf.solr.config;

import java.lang.reflect.Field;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.runners.MockitoJUnitRunner;

import com.sirma.itt.seip.configuration.annotation.ConfigurationPropertyDefinition;

/**
 * @author Boyan Tonchev.
 */
@RunWith(MockitoJUnitRunner.class)
public class SolrSearchConfigurationsImplTest {

	@Test
	public void termEscapeRegexDefaultConfiguration() throws NoSuchFieldException {
		Field termEscapeRegex = SolrSearchConfigurationsImpl.class.getDeclaredField("termEscapeRegex");
		ConfigurationPropertyDefinition annotation = termEscapeRegex.getAnnotation(ConfigurationPropertyDefinition.class);
		String termEscapeRegexDefaultConfiguration = annotation.defaultValue();

		// ", ? and * have not be escaped because will broke fts search.
		Assert.assertFalse(termEscapeRegexDefaultConfiguration.contains("\\\""));
		Assert.assertFalse(termEscapeRegexDefaultConfiguration.contains("\\?"));
		Assert.assertFalse(termEscapeRegexDefaultConfiguration.contains("\\*"));

	}
}