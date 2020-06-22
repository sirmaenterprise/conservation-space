package com.sirma.itt.seip.domain.instance;

import static com.sirma.itt.seip.util.EqualsHelper.nullSafeEquals;

import java.io.Serializable;
import java.util.Collections;
import java.util.Objects;
import java.util.Set;
import java.util.regex.Pattern;

import org.apache.commons.lang3.StringUtils;

/**
 * Represents an instance type definition
 *
 * @author BBonev
 */
public interface InstanceType extends Serializable {

	/**
	 * Gets an unique type identifier. This should be an full semantic class in string format or in URI format
	 *
	 * @return the id
	 */
	Serializable getId();

	/**
	 * Gets the type category like: case, document, project etc. This field is not unique.
	 *
	 * @return the category
	 */
	String getCategory();

	/**
	 * Gets the super types for the current type
	 *
	 * @return the super types
	 */
	Set<InstanceType> getSuperTypes();

	/**
	 * Gets the sub types of the current type.
	 *
	 * @return the sub types
	 */
	Set<InstanceType> getSubTypes();

	/**
	 * @return <code>true</code> if the current type is part of library or <code>false</code> if it is not
	 */
	default boolean isPartOflibrary() {
		return hasTrait("partOfObjectLibrary");
	}

	/**
	 * Checks if the current class is allowed to be searched via the UI.
	 *
	 * @return <code>true</code>, if is searchable and <code>false</code> if it's not
	 */
	default boolean isSearchable() {
		return hasTrait("searchable");
	}

	/**
	 * Checks if the user could create an instance with type the current class.
	 *
	 * @return true, if is creatable
	 */
	default boolean isCreatable() {
		return hasTrait("createable");
	}

	/**
	 * Checks if the user could upload a file of the current type
	 *
	 * @return true, if is uploadable
	 */
	default boolean isUploadable() {
		return hasTrait("uploadable");
	}

	/**
	 * Shows if the current instance is versionable. Default value is <code>true</code>.
	 *
	 * @return <code>true</code> if the instance is versionable or the property is missing, <code>false</code> if the
	 *         property is false
	 */
	boolean isVersionable();

	/**
	 * Shows if the current instance support mailbox. Default value is <code>false</code>.
	 *
	 * @return <code>true</code> if the instance have mailbox, <code>false</code> if the property is false or missing
	 */
	default boolean isMailboxSupportable() {
		return hasTrait("mailboxSupportable");
	}

	/**
	 * Checks if data type pattern matching is supported. This is relevant only for types that support
	 * {@link #isUploadable()}
	 *
	 * @return true if there is a configured pattern for allowed/preferred data types.
	 */
	default boolean isDataTypePatternSupported() {
		return StringUtils.isNotBlank(getProperty("acceptDataTypePattern"));
	}

	/**
	 * Checks if the given mimetype pattern matches the configured one if any
	 *
	 * @param mimetype
	 *            the mimetype
	 * @return true when there is no pattern configured or the pattern matches the given mimetype. <code>false</code> if
	 *         pattern is configured but the argument is empty or the pattern does not match the argument.
	 */
	default boolean isAllowedForMimetype(String mimetype) {
		String pattern = getProperty("acceptDataTypePattern");
		if (StringUtils.isBlank(pattern)) {
			return true;
		}
		if (StringUtils.isBlank(mimetype)) {
			return false;
		}
		return Pattern.compile(pattern).matcher(mimetype).find();
	}

	/**
	 * Checks if the current type matches the given category
	 *
	 * @param category
	 *            the category to check for
	 * @return true, if the given category matches to the category of the current type
	 */
	default boolean is(String category) {
		return nullSafeEquals(getCategory(), category);
	}

	/**
	 * Checks if the current type is same or one of it's super types is the same as the given type. <br>
	 * In other words if the given argument is a parent class of the current the method will return <code>true</code>
	 * also.
	 *
	 * @param referenceType
	 *            the reference type to check of
	 * @return true, if same type or or the given type is parent of the current
	 */
	default boolean instanceOf(InstanceType referenceType) {
		if (referenceType == null) {
			return false;
		}
		if (nullSafeEquals(getId(), referenceType.getId())) {
			return true;
		}
		return getSuperTypes().contains(referenceType);
	}

	/**
	 * Checks if the current type is same or one of it's super types is the same as the given type. <br>
	 * In other words if the given argument is a parent class of the current the method will return <code>true</code>
	 * also.
	 *
	 * @param typeId
	 *            the id of the reference type
	 * @return true, if same type or or the given type is parent of the current
	 */
	default boolean instanceOf(Serializable typeId) {
		if (typeId == null) {
			return false;
		}
		// this here will allow checking with actual URI instance and also with string format
		String converted = typeId.toString();
		if (nullSafeEquals(getId(), converted)) {
			return true;
		}
		for (InstanceType superClass : getSuperTypes()) {
			if (nullSafeEquals(superClass.getId(), converted)) {
				return true;
			}
		}
		return false;
	}

	/**
	 * Checks for sub type identifier in all sub types. The method returns <code>true</code> if the given type id is a
	 * sub type of the current instance type or in any of their sub types.
	 *
	 * @param typeId
	 *            the type id to check
	 * @return true, if the given type id is a sub type to the current or any of it's sub types.
	 */
	default boolean hasSubType(Serializable typeId) {
		if (typeId == null) {
			return false;
		}
		return hasSubType(create(typeId));
	}

	/**
	 * Checks for sub type in all sub types. The method returns <code>true</code> if the given type is a sub type of the
	 * current instance type or in any of their sub types.
	 *
	 * @param type
	 *            the type to check
	 * @return true, if the given type is a sub type to the current or any of it's sub types.
	 */
	default boolean hasSubType(InstanceType type) {
		if (type == null) {
			return false;
		}
		if (getSubTypes().contains(type)) {
			return true;
		}
		for (InstanceType subType : getSubTypes()) {
			if (subType.hasSubType(type)) {
				return true;
			}
		}
		return false;
	}

	/**
	 * Checks if the current type has the given trait.
	 *
	 * @param trait
	 *            the trait to check for
	 * @return true if the current type has the given trait
	 */
	boolean hasTrait(String trait);

	/**
	 * Gets a configured property for the type.
	 *
	 * @param propertyName
	 *            the property name to get
	 * @return the property value or <code>null</code> if not configured/present
	 */
	String getProperty(String propertyName);

	/**
	 * Creates new instance type that have only an id. The returned instance is small serializable instance type
	 * implementation that can be used in caches with corresponding instances. But it should be materialized when
	 * loading from the cache.
	 *
	 * @param original
	 *            the original instance type to copy the id from.
	 * @return the instance type
	 */
	static InstanceType copyId(InstanceType original) {
		Objects.requireNonNull(original, "Cannot copy InstanceType from null");
		return create(original.getId());
	}

	/**
	 * Creates new instance type that have only an id.
	 *
	 * @param id
	 *            the type id
	 * @return the instance type
	 */
	static InstanceType create(Serializable id) {
		return new DefaultInstanceType(id);
	}

	/**
	 * Default instance type implementation that can be initialized via {@link ClassInstance}. The implementation
	 * supports all methods
	 *
	 * @author BBonev
	 */
	class DefaultInstanceType implements InstanceType, Serializable {

		private static final long serialVersionUID = 4938003488288794304L;
		private final Serializable id;

		/**
		 * Instantiates a new default instance type.
		 *
		 * @param id
		 *            the type id
		 */
		public DefaultInstanceType(Serializable id) {
			this.id = Objects.requireNonNull(id);
		}

		@Override
		public Serializable getId() {
			return id;
		}

		@Override
		public String getCategory() {
			return null;
		}

		@Override
		public boolean hasTrait(String trait) {
			return false;
		}

		@Override
		public int hashCode() {
			return 31 + id.hashCode();
		}

		@Override
		public boolean equals(Object obj) {
			if (this == obj) {
				return true;
			}
			if (!(obj instanceof InstanceType)) {
				return false;
			}
			InstanceType other = (InstanceType) obj;
			return nullSafeEquals(getId(), other.getId());
		}

		@Override
		public String toString() {
			return getId().toString();
		}

		@Override
		public Set<InstanceType> getSuperTypes() {
			return Collections.emptySet();
		}

		@Override
		public Set<InstanceType> getSubTypes() {
			return Collections.emptySet();
		}

		@Override
		public String getProperty(String propertyName) {
			return null;
		}

		@Override
		public boolean isVersionable() {
			return true;
		}
	}
}
