package com.sirma.itt.seip;

import java.io.Serializable;
import java.util.Objects;

/**
 * Represents a single property change stored by {@link PropertiesChanges} instance or returned by {@link Trackable}
 *
 * @author <a href="mailto:borislav.bonev@sirma.bg">Borislav Bonev</a>
 * @since 31/05/2018
 */
public class PropertyChange<V extends Serializable> implements Serializable {
	private final String property;
	private final ChangeType changeType;
	private final V newValue;
	private final V oldValue;

	/**
	 * Creates a change that represents an addition
	 *
	 * @param property is the property identifier that got new value
	 * @param newValue the new value assigned to the property
	 * @param <S> is the value type
	 * @return the change instance
	 */
	public static <S extends Serializable> PropertyChange<S> add(String property, S newValue) {
		return new PropertyChange<>(property, ChangeType.ADD, newValue, null);
	}

	/**
	 * Creates a change that represents a removal of value
	 *
	 * @param property is the property identifier that got value removed
	 * @param oldValue the value that was removed for the property
	 * @param <S> is the value type
	 * @return the change instance
	 */
	public static <S extends Serializable> PropertyChange<S> remove(String property, S oldValue) {
		return new PropertyChange<>(property, ChangeType.REMOVE, null, oldValue);
	}

	/**
	 * Creates a change that represents a value update
	 *
	 * @param property is the property identifier that got value update
	 * @param newValue the new value assigned to the property
	 * @param oldValue the value that was removed for the property
	 * @param <S> is the value type
	 * @return the change instance
	 */
	public static <S extends Serializable> PropertyChange<S> update(String property, S newValue, S oldValue) {
		return new PropertyChange<>(property, ChangeType.UPDATE, newValue, oldValue);
	}

	private PropertyChange(String property, ChangeType changeType, V newValue, V oldValue) {
		this.property = property;
		this.changeType = changeType;
		this.newValue = newValue;
		this.oldValue = oldValue;
	}

	public String getProperty() {
		return property;
	}

	public ChangeType getChangeType() {
		return changeType;
	}

	public V getNewValue() {
		return newValue;
	}

	public V getOldValue() {
		return oldValue;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) {
			return true;
		}
		if (!(o instanceof PropertyChange)) {
			return false;
		}
		PropertyChange<?> that = (PropertyChange<?>) o;
		return Objects.equals(property, that.property) &&
				changeType == that.changeType &&
				Objects.equals(newValue, that.newValue) &&
				Objects.equals(oldValue, that.oldValue);
	}

	@Override
	public int hashCode() {
		return Objects.hash(property, changeType, newValue, oldValue);
	}

	@Override
	public String toString() {
		return new StringBuilder(120)
				.append("PropertyChange{")
				.append("property='").append(property).append('\'')
				.append(", changeType=").append(changeType)
				.append(", newValue=").append(newValue)
				.append(", oldValue=").append(oldValue)
				.append('}')
				.toString();
	}

	/**
	 * Defines the types of properties changes
	 */
	public enum ChangeType {
		ADD,
		REMOVE,
		UPDATE
	}
}
