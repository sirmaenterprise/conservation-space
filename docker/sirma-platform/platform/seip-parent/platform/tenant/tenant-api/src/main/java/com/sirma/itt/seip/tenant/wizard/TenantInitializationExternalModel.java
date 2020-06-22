package com.sirma.itt.seip.tenant.wizard;

/**
 * Java model representing the data contained in the manifest file when creating a tenant with an external model.
 *
 * @author nvelkov
 */
public class TenantInitializationExternalModel {

	/**
	 * Id of the model. Used as an indicator when retrieving the paths from which the models should be loaded.
	 */
	String id;
	/**
	 * Path to the definitions models.
	 */
	String definitionsPath;
	/**
	 * Path to the semantic models.
	 */
	String semanticPath;
	/**
	 * Label for the model.
	 */
	String label;

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getDefinitionsPath() {
		return definitionsPath;
	}

	public void setDefinitionsPath(String definitionsPath) {
		this.definitionsPath = definitionsPath;
	}

	public String getSemanticPath() {
		return semanticPath;
	}

	public void setSemanticPath(String semanticPath) {
		this.semanticPath = semanticPath;
	}

	public String getLabel() {
		return label;
	}

	public void setLabel(String label) {
		this.label = label;
	}

}
