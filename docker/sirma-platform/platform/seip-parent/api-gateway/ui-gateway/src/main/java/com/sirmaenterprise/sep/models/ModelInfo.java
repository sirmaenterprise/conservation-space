package com.sirmaenterprise.sep.models;

import static com.sirma.itt.seip.util.EqualsHelper.nullSafeEquals;

import org.apache.commons.lang3.StringUtils;

/**
 * Single model information uniquely identified by the model id, type and parent id
 *
 * @author BBonev
 */
public class ModelInfo {

	private String id;
	private String parentId;
	private String label;
	private String type;
	private boolean isDefault;
	private boolean isAccessible;
	private boolean isCreatable;
	private boolean isUploadable;

	/**
	 * Instantiates a new model info.
	 */
	public ModelInfo() {
		// default constructor
	}

	/**
	 * Instantiates a new model info.
	 *
	 * @param id
	 *            the id
	 * @param label
	 *            the label
	 * @param type
	 *            the type
	 */
	public ModelInfo(String id, String label, String type) {
		this(id, label, type, null, false);
	}

	/**
	 * Instantiates a new model info.
	 *
	 * @param id
	 *            the id
	 * @param label
	 *            the label
	 * @param type
	 *            the type
	 * @param parentId
	 *            the parent id
	 * @param isDefault
	 *            the is default
	 */
	public ModelInfo(String id, String label, String type, String parentId, boolean isDefault) {
		this.id = id;
		this.label = label;
		this.type = type;
		this.parentId = parentId;
		this.isDefault = isDefault;
	}

	/**
	 * The entry is valid when the model class information is not empty and the default model is not empty or the models
	 * are not empty
	 *
	 * @return true, if is valid
	 */
	boolean validate() {
		return StringUtils.isNotBlank(id) && StringUtils.isNotBlank(label) && StringUtils.isNotBlank(type);
	}

	/**
	 * Setter for the is accessible property.
	 *
	 * @param isAccessible
	 *            the value of the property
	 */
	public void setIsAccessible(boolean isAccessible) {
		this.isAccessible = isAccessible;
	}

	/**
	 * Getter for the isAccessible property.
	 *
	 * @return the value of the property
	 */
	public boolean getIsAccessible() {
		return isAccessible;
	}

	/**
	 * The model info unique identifier
	 *
	 * @return the id
	 */
	public String getId() {
		return id;
	}

	/**
	 * The model info unique identifier
	 *
	 * @param id
	 *            the id to set
	 */
	public void setId(String id) {
		this.id = id;
	}

	/**
	 * The model info unique parent identifier
	 *
	 * @return the parentId
	 */
	public String getParentId() {
		return parentId;
	}

	/**
	 * The model info unique parent identifier
	 *
	 * @param parentId
	 *            the parentId to set
	 */
	public void setParentId(String parentId) {
		this.parentId = parentId;
	}

	/**
	 * User friendly display information
	 *
	 * @return the label
	 */
	public String getLabel() {
		return label;
	}

	/**
	 * User friendly display information
	 *
	 * @param label
	 *            the label to set
	 */
	public void setLabel(String label) {
		this.label = label;
	}

	/**
	 * Model info type: class or definition
	 *
	 * @return the type
	 */
	public String getType() {
		return type;
	}

	/**
	 * Model info type: class or definition
	 *
	 * @param type
	 *            the type to set
	 */
	public void setType(String type) {
		this.type = type;
	}

	/**
	 * Is default element
	 *
	 * @return the isDefault
	 */
	public boolean isDefault() {
		return isDefault;
	}

	/**
	 * Is default element
	 *
	 * @param isDefault
	 *            the isDefault to set
	 */
	public void setDefault(boolean isDefault) {
		this.isDefault = isDefault;
	}

	/**
	 * Getter for isCreatable
	 * @return if model is creatable
	 */
	public boolean isCreatable() {
		return isCreatable;
	}

	/**
	 * Setter for isCreatable
	 * @param isCreatable to be set
	 */
	public void setCreatable(boolean isCreatable) {
		this.isCreatable = isCreatable;
	}

	/**
	 * Getter for isUploadable
	 * @return if model is uploadable
	 */
	public boolean isUploadable() {
		return isUploadable;
	}

	/**
	 * Setter for isUploadable
	 * @param isUploadable to be set
	 */
	public void setUploadable(boolean isUploadable) {
		this.isUploadable = isUploadable;
	}

	/**
	 * Checks if the current node points to a definition model.
	 *
	 * @return true, if is definition
	 */
	public boolean isDefinition() {
		return "definition".equals(getType());
	}

	/**
	 * Checks if the current node points to a class model.
	 *
	 * @return true, if is class
	 */
	public boolean isClass() {
		return "class".equals(getType());
	}

	/**
	 * Sets the model represented by the current node as class.
	 */
	public void setAsClass() {
		setType("class");
	}

	/**
	 * Sets the model represented by the current node as definition.
	 */
	public void setAsDefinition() {
		setType("definition");
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + (id == null ? 0 : id.hashCode());
		result = prime * result + (parentId == null ? 0 : parentId.hashCode());
		result = prime * result + (type == null ? 0 : type.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (!(obj instanceof ModelInfo)) {
			return false;
		}
		ModelInfo other = (ModelInfo) obj;
		return nullSafeEquals(id, other.id) && nullSafeEquals(parentId, other.parentId)
				&& nullSafeEquals(type, other.type);
	}

	@Override
	public String toString() {
		return new StringBuilder(256)
				.append("Model [id=")
					.append(id)
					.append(", parentId=")
					.append(parentId)
					.append(", label=")
					.append(label)
					.append(", type=")
					.append(type)
					.append(", isDefault=")
					.append(isDefault)
					.append("]")
					.toString();
	}

}