package com.sirma.itt.seip.template.rest;

/**
 * Represents the data required to create a template.
 *
 * @author Adrian Mitev
 */
public class TemplateCreateRequest {

	private String title;

	private String forType;

	private Boolean primary;

	private String purpose;

	private String sourceInstance;

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public String getForType() {
		return forType;
	}

	public void setForType(String forType) {
		this.forType = forType;
	}

	public Boolean isPrimary() {
		return primary;
	}

	public void setPrimary(Boolean primary) {
		this.primary = primary;
	}

	public String getPurpose() {
		return purpose;
	}

	public void setPurpose(String purpose) {
		this.purpose = purpose;
	}

	public String getSourceInstance() {
		return sourceInstance;
	}

	public void setSourceInstance(String sourceInstance) {
		this.sourceInstance = sourceInstance;
	}

}
