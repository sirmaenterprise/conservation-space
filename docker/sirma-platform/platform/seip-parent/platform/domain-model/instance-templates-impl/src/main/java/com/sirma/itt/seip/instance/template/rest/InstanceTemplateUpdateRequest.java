package com.sirma.itt.seip.instance.template.rest;

/**
 * Represents the request data accepted by instance template endpoint.
 *
 * @author Adrian Mitev
 */
public class InstanceTemplateUpdateRequest {

	private String templateInstance;
	private String instance;

	public String getTemplateInstance() {
		return templateInstance;
	}

	public void setTemplateInstance(String templateInstance) {
		this.templateInstance = templateInstance;
	}

	public String getInstance() {
		return instance;
	}

	public void setInstance(String instance) {
		this.instance = instance;
	}

}
