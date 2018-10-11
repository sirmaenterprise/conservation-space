package com.sirma.itt.seip.definition;

import java.util.List;
import java.util.Set;

import com.sirma.itt.seip.domain.BidirectionalMapping;
import com.sirma.itt.seip.domain.definition.Condition;
import com.sirma.itt.seip.domain.definition.ControlDefinition;
import com.sirma.itt.seip.domain.definition.DataTypeDefinition;
import com.sirma.itt.seip.domain.definition.DisplayType;
import com.sirma.itt.seip.domain.definition.PropertyDefinition;

/**
 * Defines setter methods for the property definition used by the properties proxy.
 *
 * @author BBonev
 */
public interface WritablePropertyDefinition extends PropertyDefinition, BidirectionalMapping {

	/**
	 * Sets the value of the codelist property.
	 *
	 * @param value
	 *            allowed object is {@link int }
	 */
	void setCodelist(Integer value);

	/**
	 * Sets the value of the displayType property.
	 *
	 * @param value
	 *            allowed object is {@link String }
	 */
	void setDisplayType(DisplayType value);

	/**
	 * Sets the value of the mandatory property.
	 *
	 * @param value
	 *            allowed object is {@link Boolean }
	 */
	void setMandatory(Boolean value);

	/**
	 * Setter method for mandatoryEnforced.
	 *
	 * @param mandatoryEnforced
	 *            the mandatoryEnforced to set
	 */
	void setMandatoryEnforced(Boolean mandatoryEnforced);

	/**
	 * Setter method for multiValued.
	 *
	 * @param multiValued
	 *            the multiValued to set
	 */
	@Override
	void setMultiValued(Boolean multiValued);

	/**
	 * Sets the value of the name property.
	 *
	 * @param value
	 *            allowed object is {@link String }
	 */
	void setName(String value);

	/**
	 * Setter method for override.
	 *
	 * @param override
	 *            the override to set
	 */
	void setOverride(Boolean override);

	/**
	 * Sets the value of the rnc property.
	 *
	 * @param value
	 *            allowed object is {@link String }
	 */
	void setRnc(String value);

	/**
	 * Sets the value of the type property.
	 *
	 * @param value
	 *            allowed object is {@link String }
	 */
	void setType(String value);

	/**
	 * Setter method for typeDefinition.
	 *
	 * @param typeDefinition
	 *            the typeDefinition to set
	 */
	@Override
	void setDataType(DataTypeDefinition typeDefinition);

	/**
	 * Sets the value of the value property.
	 *
	 * @param value
	 *            allowed object is {@link String }
	 */
	void setValue(String value);

	/**
	 * Setter method for maxLength.
	 *
	 * @param maxLength
	 *            the maxLength to set
	 */
	void setMaxLength(Integer maxLength);

	/**
	 * Setter method for revision.
	 *
	 * @param revision
	 *            the revision to set
	 */
	void setRevision(Long revision);

	/**
	 * Setter method for parentPath.
	 *
	 * @param parentPath
	 *            the parentPath to set
	 */
	void setParentPath(String parentPath);

	/**
	 * Setter method for previewEmpty.
	 *
	 * @param previewEmpty
	 *            the previewEmpty to set
	 */
	void setPreviewEmpty(Boolean previewEmpty);

	/**
	 * Setter method for labelId.
	 *
	 * @param labelId
	 *            the labelId to set
	 */
	void setLabelId(String labelId);

	/**
	 * Setter method for tooltipId.
	 *
	 * @param tooltipId
	 *            the tooltipId to set
	 */
	void setTooltipId(String tooltipId);

	/**
	 * Setter method for controlDefinition.
	 *
	 * @param controlDefinition
	 *            the controlDefinition to set
	 */
	void setControlDefinition(ControlDefinition controlDefinition);

	/**
	 * Setter method for order.
	 *
	 * @param order
	 *            the order to set
	 */
	void setOrder(Integer order);

	/**
	 * Setter method for dmsType.
	 *
	 * @param dmsType
	 *            the dmsType to set
	 */
	void setDmsType(String dmsType);

	/**
	 * Sets the default properties.
	 */
	void setDefaultProperties();

	/**
	 * Setter method for filters.
	 *
	 * @param filters
	 *            the filters to set
	 */
	void setFilters(Set<String> filters);

	/**
	 * Setter method for conditions.
	 *
	 * @param conditions
	 *            the conditions to set
	 */
	void setConditions(List<Condition> conditions);

	/**
	 * Sets the container.
	 *
	 * @param container
	 *            the new container
	 */
	@Override
	void setContainer(String container);

	/**
	 * Sets the hash.
	 *
	 * @param hash
	 *            the new hash
	 */
	void setHash(Integer hash);

	/**
	 * Sets the prototype id.
	 *
	 * @param prototypeId
	 *            the new prototype id
	 */
	void setPrototypeId(Long prototypeId);

	/**
	 * Sets the uri.
	 *
	 * @param uri
	 *            the new uri
	 */
	void setUri(String uri);

	/**
	 * Sets the unique .If true value of field have to be unique for a certain tenant.
	 *
	 * @param unique
	 *         - is value unique.
	 */
	void setUnique(Boolean unique);
}