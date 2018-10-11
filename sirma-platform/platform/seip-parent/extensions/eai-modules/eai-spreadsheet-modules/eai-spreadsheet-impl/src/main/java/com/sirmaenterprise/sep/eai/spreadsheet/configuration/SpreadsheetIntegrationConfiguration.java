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

	@ConfigurationPropertyDefinition(type = String.class, sensitive = true, label = "JNLP template for EAI content tool. Value is String#format template with needed dynamic arguments.", defaultValue = "<?xml version=\"1.0\" encoding=\"utf-8\"?>\n<jnlp spec=\"1.0+\" xmlns:jfx=\"http://javafx.com\" codebase=\"%251s\">\n\t<information>\n\t\t<title>EAI content import tool</title>\n\t\t<vendor>Sirma Solutions</vendor>\n\t\t<description>SEP content import tool</description>\n\t</information>\n\t<security>\n\t\t<all-permissions/>\n\t</security>\n\t<update check=\"always\" policy=\"always\"/>\n\t<resources>\n\t\t<j2se version=\"1.8+\" href=\"http://java.sun.com/products/autodl/j2se\"/>\n\t\t<jar href=\"%252s\" download=\"eager\" />\n\t</resources>\n\t<jfx:javafx-desc main-class=\"com.sirma.itt.seip.eai.content.tool.Main\" name=\"eai-content-tool\">\n\t\t<fx:param name=\"apiUrl\" value=\"%253s\"/>\n\t\t<fx:param name=\"authorization\" value=\"%254s\"/>\n\t\t<fx:param name=\"uri\" value=\"%255s\"/>\n\t</jfx:javafx-desc>\n</jnlp>")
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
	private ConfigurationProperty<String> contentToolJNLP;

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
	public ConfigurationProperty<String> getContentToolJNLP() {
		return contentToolJNLP;
	}

}
