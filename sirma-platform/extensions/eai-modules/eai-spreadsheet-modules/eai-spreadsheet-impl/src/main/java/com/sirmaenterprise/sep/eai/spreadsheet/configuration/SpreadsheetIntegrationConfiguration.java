package com.sirmaenterprise.sep.eai.spreadsheet.configuration;

import javax.inject.Inject;
import javax.inject.Singleton;

import com.sirma.itt.seip.configuration.ConfigurationProperty;
import com.sirma.itt.seip.configuration.annotation.Configuration;
import com.sirma.itt.seip.configuration.annotation.ConfigurationPropertyDefinition;
import com.sirma.itt.seip.domain.instance.DefaultProperties;
import com.sirma.itt.semantic.model.vocabulary.EMF;

/**
 * Configuration holder for specific spreadsheet API configurations.
 * 
 * @author bbanchev
 */
@Singleton
public class SpreadsheetIntegrationConfiguration {
	@ConfigurationPropertyDefinition(type = String.class, sensitive = true, name = "eai.spreadsheet.config.model.property.type.uri", label = "URI for property that defines the instance type.", defaultValue = EMF.PREFIX
			+ ":" + DefaultProperties.TYPE)
	private static final String EAI_SPREADSHEET_PROPERTY_TYPE_URI = "eai.spreadsheet.config.model.property.type.uri";
	@ConfigurationPropertyDefinition(type = String.class, sensitive = true, name = "eai.spreadsheet.config.model.property.identifier.uri", label = "URI for property that defines the instance uri. Leave it empty to use model each model property!", defaultValue = EMF.PREFIX
			+ ":" + DefaultProperties.ENTITY_IDENTIFIER)
	private static final String EAI_SPREADSHEET_PROPERTY_IDENTIFIER_URI = "eai.spreadsheet.config.model.property.identifier.uri";

	@ConfigurationPropertyDefinition(type = String.class, sensitive = true, name = "eai.spreadsheet.config.report.definitionId", label = "Definition id for shpreadsheet validation report!", defaultValue = "DataImportReport")
	private static final String EAI_SPREADSHEET_REPORT_DEFINITION_ID = "eai.spreadsheet.config.report.definitionId";

	@ConfigurationPropertyDefinition(type = Integer.class, sensitive = true, name = "eai.spreadsheet.system.parallelism.count", defaultValue = "5", label = "Property that defines at how many parallel task an import request should be split into!")
	private static final String EAI_SPREADSHEET_SYSTEM_PARALLELISM_COUNT = "eai.spreadsheet.system.parallelism.count";

	@Inject
	@Configuration(EAI_SPREADSHEET_PROPERTY_TYPE_URI)
	private ConfigurationProperty<String> typePropertyURI;
	@Inject
	@Configuration(EAI_SPREADSHEET_PROPERTY_IDENTIFIER_URI)
	private ConfigurationProperty<String> identifierPropertyURI;
	@Inject
	@Configuration(EAI_SPREADSHEET_REPORT_DEFINITION_ID)
	private ConfigurationProperty<String> reportDefinitionId;
	@Inject
	@Configuration(EAI_SPREADSHEET_SYSTEM_PARALLELISM_COUNT)
	private ConfigurationProperty<Integer> parallelismCount;

	/**
	 * Defines the {@value #EAI_SPREADSHEET_PROPERTY_TYPE_URI} value
	 * 
	 * @return the configuration for {@value #EAI_SPREADSHEET_PROPERTY_TYPE_URI}
	 */
	public ConfigurationProperty<String> getTypePropertyURI() {
		return typePropertyURI;
	}

	/**
	 * Defines the {@value #EAI_SPREADSHEET_PROPERTY_IDENTIFIER_URI} value
	 * 
	 * @return the configuration for {@value #EAI_SPREADSHEET_PROPERTY_IDENTIFIER_URI}
	 */
	public ConfigurationProperty<String> getIdentifierPropertyURI() {
		return identifierPropertyURI;
	}

	/**
	 * Defines the {@value #EAI_SPREADSHEET_REPORT_DEFINITION_ID} value
	 * 
	 * @return the configuration for {@value #EAI_SPREADSHEET_REPORT_DEFINITION_ID}
	 */
	public ConfigurationProperty<String> getReportDefinitionId() {
		return reportDefinitionId;
	}

	/**
	 * Defines the {@value #EAI_SPREADSHEET_SYSTEM_PARALLELISM_COUNT} value
	 * 
	 * @return the configuration for {@value #EAI_SPREADSHEET_SYSTEM_PARALLELISM_COUNT}
	 */
	public ConfigurationProperty<Integer> getParallelismCount() {
		return parallelismCount;
	}
}
