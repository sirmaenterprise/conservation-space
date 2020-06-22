package com.sirma.itt.sep.content.extraction.patch;

import static org.mockito.Matchers.argThat;

import java.util.Arrays;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Matchers;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.Spy;
import org.mockito.runners.MockitoJUnitRunner;

import com.sirma.itt.cmf.content.extract.TikaContentExtractor;
import com.sirma.itt.seip.configuration.db.Configuration;
import com.sirma.itt.seip.configuration.db.ConfigurationManagement;
import com.sirma.itt.seip.testutil.CustomMatcher;
import com.sirma.itt.seip.testutil.fakes.TransactionSupportFake;
import com.sirma.itt.seip.tx.TransactionSupport;

import liquibase.database.Database;
import liquibase.exception.CustomChangeException;

/**
 * Tests for {@link UpdateMimeTypePatternTask}.
 *
 * @author Boyan Tonchev.
 */
@RunWith(MockitoJUnitRunner.class)
public class UpdateMimeTypePatternTaskTest {

	private static final String DEFAULT_PATTERN = "^(?!audio.+|video.+|image.+|application/octet-stream|text/xml|text/x?html|application/xml).+";

	@Mock
	private ConfigurationManagement configurationManagement;

	@Spy
	private TransactionSupport transactionSupport = new TransactionSupportFake();

	@InjectMocks
	private UpdateMimeTypePatternTask updateMimeTypePatterTask;

	@Test
	public void should_UpdateMimeTypeConfiguration_When_OnlyRemoveMimetypesIsSet() throws CustomChangeException {
		Configuration configuration = createConfiguration(DEFAULT_PATTERN);
		updateMimeTypePatterTask.setRemoveMimetypes("audio.+");

		updateMimeTypePatterTask.execute(Mockito.mock(Database.class));

		Mockito.verify(configurationManagement)
				.updateSystemConfiguration(argThat(matchesConfiguration(configuration,
																		"^(?!video.+|image.+|application/octet-stream|text/xml|text/x?html|application/xml).+")));
	}

	@Test
	public void should_UpdateMimeTypeConfiguration_When_OnlyAddMimetypesIsSet() throws CustomChangeException {
		Configuration configuration = createConfiguration(DEFAULT_PATTERN);
		updateMimeTypePatterTask.setAddMimetypes("application/x-tar");

		updateMimeTypePatterTask.execute(Mockito.mock(Database.class));

		Mockito.verify(configurationManagement)
				.updateSystemConfiguration(argThat(matchesConfiguration(configuration,
																		"^(?!audio.+|video.+|image.+|application/octet-stream|text/xml|text/x?html|application/xml|application/x-tar).+")));
	}

	@Test
	public void should_UpdateMimeTypeConfiguration_When_AddMimetypesAndRemoveMimetypesAreSet()
			throws CustomChangeException {
		Configuration configuration = createConfiguration(DEFAULT_PATTERN);
		updateMimeTypePatterTask.setAddMimetypes("application/x-tar");
		updateMimeTypePatterTask.setRemoveMimetypes("audio.+");

		updateMimeTypePatterTask.execute(Mockito.mock(Database.class));

		Mockito.verify(configurationManagement)
				.updateSystemConfiguration(argThat(matchesConfiguration(configuration,
																		"^(?!video.+|image.+|application/octet-stream|text/xml|text/x?html|application/xml|application/x-tar).+")));
	}

	@Test
	public void should_NotUpdateMimeTypeConfiguration_When_ConfigurationIsNotFound() throws CustomChangeException {
		updateMimeTypePatterTask.setAddMimetypes("application/x-tar");
		updateMimeTypePatterTask.setRemoveMimetypes("audio.+");

		updateMimeTypePatterTask.execute(Mockito.mock(Database.class));

		Mockito.verify(configurationManagement, Mockito.never()).updateSystemConfiguration(Matchers.any(Configuration.class));
	}

	@Test
	public void should_NotUpdateMimeTypeConfiguration_When_AddMimetypesAndRemoveMimetypesAreEmpty()
			throws CustomChangeException {
		updateMimeTypePatterTask.setAddMimetypes("");
		updateMimeTypePatterTask.setRemoveMimetypes("");

		updateMimeTypePatterTask.execute(Mockito.mock(Database.class));

		Mockito.verify(configurationManagement, Mockito.never()).updateSystemConfiguration(Matchers.any(Configuration.class));
	}

	@Test
	public void should_NotUpdateMimeTypeConfiguration_When_AddMimetypesAndRemoveMimetypesAreNull()
			throws CustomChangeException {
		updateMimeTypePatterTask.setAddMimetypes(null);
		updateMimeTypePatterTask.setRemoveMimetypes(null);

		updateMimeTypePatterTask.execute(Mockito.mock(Database.class));

		Mockito.verify(configurationManagement, Mockito.never()).updateSystemConfiguration(Matchers.any(Configuration.class));
	}

	@Test
	public void should_NotUpdateMimeTypeConfiguration_When_ThereIsNotConfigurationValue()
			throws CustomChangeException {
		Configuration configuration = createConfiguration(null);
		updateMimeTypePatterTask.setRemoveMimetypes("audio.+");

		updateMimeTypePatterTask.execute(Mockito.mock(Database.class));

		Mockito.verify(configurationManagement, Mockito.never()).updateSystemConfiguration(Matchers.any(Configuration.class));
	}

	private Configuration createConfiguration(String mimeTypePattern) {
		Configuration configuration = new Configuration();
		configuration.setRawValue(mimeTypePattern);
		configuration.setConfigurationKey(TikaContentExtractor.MIMETYPE_PATTERN_ID);
		Mockito.when(configurationManagement.getSystemConfigurations()).thenReturn(Arrays.asList(configuration));
		return configuration;
	}

	private CustomMatcher<Configuration> matchesConfiguration(
			Configuration expectedConfiguration, String expectedMimetypePattern) {
		return CustomMatcher.of(configuration -> {
			Assert.assertEquals(expectedConfiguration, configuration);
			Assert.assertEquals(expectedMimetypePattern, configuration.getValue());
		});
	}
}