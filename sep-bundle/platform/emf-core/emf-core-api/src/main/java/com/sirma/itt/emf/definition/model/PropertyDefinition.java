package com.sirma.itt.emf.definition.model;

import java.util.Set;

import com.sirma.itt.emf.domain.DisplayType;
import com.sirma.itt.emf.domain.model.PathElement;
import com.sirma.itt.emf.domain.model.Sortable;
import com.sirma.itt.emf.label.Displayable;

/**
 * Read-only definition of a Property.
 * 
 * @author BBonev
 */
public interface PropertyDefinition extends Sortable, PathElement, Displayable, Conditional,
		PrototypeDefinition, Controllable {

	/**
	 * Gets the name.
	 * 
	 * @return the qualified name of the property
	 */
	String getName();

	/**
	 * Gets the default value.
	 * 
	 * @return the default value
	 */
	String getDefaultValue();

	/**
	 * Gets the data type.
	 * 
	 * @return the qualified name of the property type
	 */
	@Override
	DataTypeDefinition getDataType();

	/**
	 * Checks if is override.
	 * 
	 * @return true, if is override
	 */
	Boolean isOverride();

	/**
	 * Checks if is multi valued.
	 * 
	 * @return true => multi-valued, false => single-valued
	 */
	@Override
	Boolean isMultiValued();

	/**
	 * Checks if is mandatory.
	 * 
	 * @return true => mandatory, false => optional
	 */
	Boolean isMandatory();

	/**
	 * Checks if is mandatory enforced.
	 * 
	 * @return Returns true if the system enforces the presence of {@link #isMandatory() mandatory}
	 *         properties, or false if the system just marks objects that don't have all mandatory
	 *         properties present.
	 */
	Boolean isMandatoryEnforced();

	/**
	 * Checks if is protected.
	 * 
	 * @return true => system maintained, false => client may maintain
	 */
	Boolean isProtected();

	/**
	 * Gets the max length for text properties.
	 * 
	 * @return the max length
	 */
	Integer getMaxLength();

	/**
	 * Gets the display type for the property.
	 * 
	 * @return The display type that should be applied to the property.
	 */
	@Override
	DisplayType getDisplayType();

	/**
	 * Gets the previewEnabled attribute.
	 * 
	 * @return Whether the field should be rendered if is empty.
	 */
	Boolean isPreviewEnabled();

	/**
	 * Gets the property revision.
	 * 
	 * @return the revision
	 */
	Long getRevision();

	/**
	 * Gets the parent path for the given property.
	 * 
	 * @return the parent path
	 */
	String getParentPath();

	/**
	 * Gets the codelist if is set.
	 * 
	 * @return the codelist number
	 */
	Integer getCodelist();

	/**
	 * Getter for the property type.
	 * 
	 * @return The type.
	 */
	String getType();

	/**
	 * Gets the control definition.
	 * 
	 * @return the control definition
	 */
	@Override
	ControlDefinition getControlDefinition();

	/**
	 * Gets the value of the rnc property.
	 * 
	 * @return the rnc possible object is {@link String }
	 */
	String getRnc();

	/**
	 * Gets the dms type.
	 * 
	 * @return the dms type
	 */
	String getDmsType();

	/**
	 * Gets the filters if any for filtering the field values.
	 * 
	 * @return the filters
	 */
	Set<String> getFilters();

	/**
	 * Gets the container.
	 * 
	 * @return the container
	 */
	@Override
	String getContainer();

	/**
	 * Gets the hash. The hash of the field calculated for the current field definition.
	 * 
	 * @return the hash
	 */
	Integer getHash();

	/**
	 * Gets the prototype id.
	 * 
	 * @return the prototype id
	 */
	Long getPrototypeId();

	/**
	 * Gets the properties URI identifier.
	 * 
	 * @return the uri
	 */
	String getUri();
}
