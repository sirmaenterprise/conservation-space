package com.sirma.itt.cmf.constants;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import com.sirma.itt.seip.configuration.ConfigurationProperty;
import com.sirma.itt.seip.configuration.annotation.Configuration;
import com.sirma.itt.seip.configuration.annotation.ConfigurationPropertyDefinition;

/**
 * Defines all CMF specific configuration name properties.
 *
 * @author BBonev
 */
@ApplicationScoped
public class CmfConfigurationProperties {

	@Inject
	@Configuration
	@ConfigurationPropertyDefinition(name = "codelist.documentTitle", type = Integer.class, defaultValue = "210", label = "Codelist number of document type field.")
	private ConfigurationProperty<Integer> documentTypeCodelist;

	@Inject
	@Configuration
	@ConfigurationPropertyDefinition(name = "codelist.caseDefinitions", type = Integer.class, defaultValue = "200", label = "Codelist number of case definition type.")
	private ConfigurationProperty<Integer> caseTypeCodelist;

	@Inject
	@Configuration
	@ConfigurationPropertyDefinition(name = "codelist.taskDefinition", type = Integer.class, defaultValue = "227", label = "Codelist number of task definition type")
	private ConfigurationProperty<Integer> taskTypeCodelist;

	@Inject
	@Configuration
	@ConfigurationPropertyDefinition(name = "codelist.documentDefaultAttachmentType", defaultValue = "OT210027", label = "Codelist number for common document type")
	private ConfigurationProperty<String> documentDefaultAttachmentType;

	/**
	 * Gets the case type codelist.
	 *
	 * @return the case type codelist
	 */
	public ConfigurationProperty<Integer> getCaseTypeCodelist() {
		return caseTypeCodelist;
	}

	/**
	 * Gets the document default attachment type.
	 *
	 * @return the document default attachment type
	 */
	public ConfigurationProperty<String> getDocumentDefaultAttachmentType() {
		return documentDefaultAttachmentType;
	}

	/**
	 * Gets the document type codelist.
	 *
	 * @return the document type codelist
	 */
	public ConfigurationProperty<Integer> getDocumentTypeCodelist() {
		return documentTypeCodelist;
	}

	/**
	 * Gets the task type codelist.
	 *
	 * @return the task type codelist
	 */
	public ConfigurationProperty<Integer> getTaskTypeCodelist() {
		return taskTypeCodelist;
	}
}
