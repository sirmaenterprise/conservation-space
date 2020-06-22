package com.sirma.itt.seip.domain.definition;

import static com.sirma.itt.seip.domain.instance.DefaultProperties.NOT_USED_PROPERTY_VALUE;
import static com.sirma.itt.seip.util.EqualsHelper.nullSafeEquals;

import java.io.Serializable;
import java.util.Collections;
import java.util.EnumSet;
import java.util.Set;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Stream;

import org.apache.commons.lang3.StringUtils;

import com.sirma.itt.seip.domain.Ordinal;
import com.sirma.itt.seip.domain.PathElement;
import com.sirma.itt.seip.domain.definition.label.Displayable;
import com.sirma.itt.seip.domain.instance.DefaultProperties;

/**
 * Read-only definition of a Property.
 *
 * @author BBonev
 */
public interface PropertyDefinition
		extends Ordinal, PathElement, Displayable, Conditional, PrototypeDefinition, Controllable, Serializable {

	/**
	 * Filter that selects a property by it's name.
	 *
	 * @param name
	 *            the name to match.
	 * @return the predicate that selects a property that matches the given name.
	 * @see #getIdentifier()
	 * @see #getName()
	 */
	static Predicate<PropertyDefinition> hasName(String name) {
		return property -> nullSafeEquals(property.getIdentifier(), name);
	}

	/**
	 * Filter that selects a property by it's {@link #getUri()} value.
	 *
	 * @param uri
	 *            the uri to match
	 * @return the predicate that selects a property that matches the given uri
	 * @see #getUri()
	 */
	static Predicate<PropertyDefinition> hasUri(String uri) {
		return property -> nullSafeEquals(property.getUri(), uri);
	}

	/**
	 * Filter properties that matches the given type name. For possible type names see {@link DataTypeDefinition}
	 *
	 * @param typeName
	 *            the type name to check for
	 * @return the predicate that returns true if the data type name patches
	 * @see DataTypeDefinition
	 */
	static Predicate<PropertyDefinition> hasType(String typeName) {
		return property -> (property.getDataType() != null && nullSafeEquals(property.getDataType().getName(), typeName, true))
					|| nullSafeEquals(property.getType(), typeName, true);
	}

	/**
	 * Resolve URI for the given {@link PropertyDefinition} or null if no valid is found. An URI role could be the
	 * property name if in the correct format or the {@link #getUri()} field value if not equal to
	 * {@value DefaultProperties#NOT_USED_PROPERTY_VALUE}
	 *
	 * @return the function that returns short URI value or <code>null</code>
	 */
	static Function<PropertyDefinition, String> resolveUri() {
		return property -> {
			if (property.getIdentifier().contains(":")) {
				return property.getIdentifier();
			} else if (!NOT_USED_PROPERTY_VALUE.equals(property.getUri())) {
				return property.getUri();
			}
			return null;
		};
	}

	/**
	 * Filter that selects a property by it's display type.
	 *
	 * @param type
	 *            the display type to match.
	 * @return the predicate that selects a property that matches the given name.
	 * @see #getIdentifier()
	 * @see #getName()
	 */
	static Predicate<PropertyDefinition> byDisplayType(DisplayType type) {
		return property -> nullSafeEquals(property.getDisplayType(), type);
	}

	/**
	 * Filter that selects a property by it's display type.
	 *
	 * @param type
	 *            the display type to match.
	 * @param types
	 *            the additional types to match
	 * @return the predicate that selects a property that matches the given name.
	 * @see #getIdentifier()
	 * @see #getName()
	 */
	static Predicate<PropertyDefinition> byDisplayType(DisplayType type, DisplayType... types) {
		EnumSet<DisplayType> enumSet = EnumSet.of(type, types);
		return property -> enumSet.contains(property.getDisplayType());
	}

	/**
	 * Checks if the property has any default value set.
	 *
	 * @return the predicate
	 */
	static Predicate<PropertyDefinition> hasValue() {
		return property -> StringUtils.isNotBlank(property.getDefaultValue());
	}

	/**
	 * Checks for control with the given name ignoring case.
	 *
	 * @param controlName
	 *            the control name
	 * @return the predicate
	 */
	static Predicate<PropertyDefinition> hasControl(String controlName) {
		return property -> property.getControlDefinition() != null
				&& nullSafeEquals(property.getControlDefinition().getIdentifier(), controlName, true);
	}

	/**
	 * Returns a predicate that returns <code>true</code> if the given {@link PropertyDefinition} is considered an
	 * object property. For now object property is a property of type {@link DataTypeDefinition#URI}.
	 *
	 * @return the predicate that tests if a property definition points to an object property
	 */
	static Predicate<PropertyDefinition> isObjectProperty() {
		return hasType(DataTypeDefinition.URI);
	}

	/**
	 * Checks if a {@link PropertyDefinition} has a defined codelist.
	 *
	 * @return the predicate that checks for codelist present
	 */
	static Predicate<PropertyDefinition> hasCodelist() {
		return property -> property.getCodelist() != null && property.getCodelist().intValue() > 0;
	}

	/**
	 * Checks is the given property has a valid URI.
	 *
	 * @return the predicate that checks if the property name is in URI format or {@link #getUri()} field is valid
	 */
	static Predicate<PropertyDefinition> hasUri() {
		return property -> property.getName().contains(":") || StringUtils.isNotEmpty(property.getUri())
				&& !nullSafeEquals(property.getUri(), DefaultProperties.NOT_USED_PROPERTY_VALUE, false);
	}

	/**
	 * Checks if a given {@link PropertyDefinition} has a {@link PropertyDefinition#getDmsType()} field that has a valid
	 * value.
	 *
	 * @return the predicate to check for presence of dms type.
	 */
	static Predicate<PropertyDefinition> hasDmsType() {
		return property -> !nullSafeEquals(property.getDmsType(), DefaultProperties.NOT_USED_PROPERTY_VALUE, false);
	}

	/**
	 * Stream the current property and the properties of it's control definition returned by
	 * {@link #getControlDefinition()} if any.
	 *
	 * @return the stream of the current property and the properties found in it's control definition
	 */
	default Stream<PropertyDefinition> stream() {
		if (getControlDefinition() == null) {
			return Stream.of(this);
		}
		return Stream.concat(Stream.of(this), getControlDefinition().fieldsStream());
	}

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
	 * Checks if is override. This could return a default value if it is <code>null</code>
	 *
	 * @return <code>true</code> if is override or <code>false</code> if it is not
	 */
	Boolean isOverride();

	/**
	 * Checks if is override. This respects if the value is <code>null</code>
	 *
	 * @return <code>true</code> if is override, <code>false</code> if it is not or <code>null</code> if there is no defined value
	 */
	Boolean getOverride();

	/**
	 * Checks if is multi valued. This could return a default value if it is <code>null</code>
	 *
	 * @return true =&gt; multi-valued, false =&gt; single-valued
	 */
	@Override
	Boolean isMultiValued();

	/**
	 * Returns if this property is multivalued. This respects if the value is <code>null</code>
	 *
	 * @return <code>true</code> if the property is multivalued, <code>false</code> if it is not or <code>null</code> if there is no defined value
	 */
	Boolean getMultiValued();

	/**
	 * Checks if is mandatory. This could return a default value if it is <code>null</code>
	 *
	 * @return true =&gt; mandatory, false =&gt; optional
	 */
	Boolean isMandatory();

	/**
	 * Returns if this property is mandatory. This respects if the value is <code>null</code>
	 *
	 * @return <code>true</code> if the property is mandatory, <code>false</code> if it is not or <code>null</code> if there is no defined value
	 */
	Boolean getMandatory();

	/**
	 * Checks if is mandatory enforced.
	 *
	 * @return Returns true if the system enforces the presence of {@link #isMandatory() mandatory} properties, or false
	 *         if the system just marks objects that don't have all mandatory properties present.
	 */
	Boolean isMandatoryEnforced();

	/**
	 * Checks if is protected.
	 *
	 * @return true =&gt; system maintained, false =&gt; client may maintain
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
	 * Returns if this property should be displayed if it is empty. This respects if the value is <code>null</code>
	 *
	 * @return <code>true</code> if the property should be displayed if empty, <code>false</code> if not or <code>null</code> if there is no defined value
	 */
	Boolean getPreviewEmpty();

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

	/**
	 * Gets the dependent fields based on the RnC rules. These are the fields that should be considered when displaying
	 * the current field
	 *
	 * @return the dependent fields
	 */
	default Set<String> getDependentFields() {
		return Collections.emptySet();
	}

	/**
	 * Gets value of attribute "unique" as it set in definition.
	 * To check if a given property is unique use {@link PropertyDefinition#isUniqueProperty()}
	 *
	 * @return the value of attribute "unique".
	 */
	Boolean isUnique();

    /**
     * Checks if a given {@link PropertyDefinition} has to be processed as unique.
     *
     * @return the predicate to checks if property is unique.
     */
    static Predicate<PropertyDefinition> isUniqueProperty() {
        return propertyDefinition -> {
            if (propertyDefinition.isMultiValued() || PropertyDefinition.isObjectProperty().test(propertyDefinition)
                    || PropertyDefinition.hasType(DataTypeDefinition.DATETIME).test(propertyDefinition)) {
                return false;
            }
            return Boolean.TRUE.equals(propertyDefinition.isUnique());
        };
    }
}
