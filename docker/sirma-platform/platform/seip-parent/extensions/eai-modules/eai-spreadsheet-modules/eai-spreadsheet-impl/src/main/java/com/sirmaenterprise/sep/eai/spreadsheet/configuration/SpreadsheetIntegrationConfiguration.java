package com.sirmaenterprise.sep.eai.spreadsheet.configuration;

import java.io.File;

import javax.inject.Inject;
import javax.inject.Singleton;

import com.sirma.itt.seip.configuration.ConfigurationProperty;
import com.sirma.itt.seip.configuration.annotation.Configuration;
import com.sirma.itt.seip.configuration.annotation.ConfigurationConverter;
import com.sirma.itt.seip.configuration.annotation.ConfigurationPropertyDefinition;
import com.sirma.itt.seip.configuration.convert.ConverterContext;
import com.sirma.itt.seip.domain.instance.DefaultProperties;
import com.sirma.itt.semantic.model.vocabulary.EMF;

/**
 * Configuration holder for specific spreadsheet API configurations.
 * 
 * @author bbanchev
 */
@Singleton
public class SpreadsheetIntegrationConfiguration {
	@ConfigurationPropertyDefinition(type = String.class, sensitive = true, label = "URI for property that defines the instance type.", defaultValue = EMF.PREFIX
			+ ":" + DefaultProperties.TYPE)
	private static final String EAI_SPREADSHEET_PROPERTY_TYPE_URI = "eai.spreadsheet.config.model.property.type.uri";
	@ConfigurationPropertyDefinition(type = String.class, sensitive = true, label = "URI for property that defines the instance uri. Leave it empty to use model each model property!", defaultValue = EMF.PREFIX
			+ ":" + DefaultProperties.ENTITY_IDENTIFIER)
	private static final String EAI_SPREADSHEET_PROPERTY_IDENTIFIER_URI = "eai.spreadsheet.config.model.property.identifier.uri";

	@ConfigurationPropertyDefinition(type = String.class, sensitive = true, label = "Definition id for shpreadsheet validation report!", defaultValue = "DataImportReport")
	private static final String EAI_SPREADSHEET_REPORT_DEFINITION_ID = "eai.spreadsheet.config.report.definitionId";

	@ConfigurationPropertyDefinition(type = Integer.class, sensitive = true, defaultValue = "5", label = "Property that defines at how many parallel task an import request should be split into!")
	private static final String EAI_SPREADSHEET_SYSTEM_PARALLELISM_COUNT = "eai.spreadsheet.system.parallelism.count";

	@ConfigurationPropertyDefinition(type = File.class, sensitive = true, label = "Absolute or relative (to jboss.server.base.dir) path to JNLP template for EAI content tool. Value is String#format template with needed dynamic arguments.", defaultValue = "configuration/sep/content-tool-jnlp.xml")
	private static final String EAI_SPREADSHEET_CONTENT_TOOL_JNLP = "eai.spreadsheet.config.content.tool.jnlp";

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
	@Inject
	@Configuration(EAI_SPREADSHEET_CONTENT_TOOL_JNLP)
	private ConfigurationProperty<File> contentToolJNLP;

	@ConfigurationConverter(EAI_SPREADSHEET_CONTENT_TOOL_JNLP)
	static File readJNLPConfigurationXML(ConverterContext ctx) {
		File config = new File(ctx.getRawValue());
		if (config.isAbsolute()) {
			return config;
		}

		return new File(System.getProperty("jboss.server.base.dir"), ctx.getRawValue());
	}
	
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

	/**
	 * Defines the {@value #EAI_SPREADSHEET_CONTENT_TOOL_JNLP} value
	 * 
	 * @return the configuration for {@value #EAI_SPREADSHEET_CONTENT_TOOL_JNLP}
	 */
	public ConfigurationProperty<File> getContentToolJNLP() {
		return contentToolJNLP;
	}

}
