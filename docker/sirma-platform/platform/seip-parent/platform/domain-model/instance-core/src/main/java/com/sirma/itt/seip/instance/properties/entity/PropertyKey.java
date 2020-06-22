package com.sirma.itt.seip.instance.properties.entity;

import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.Embeddable;

import com.sirma.itt.seip.db.PersistenceUnits;
import com.sirma.itt.seip.db.discovery.PersistenceUnitBinding;
import com.sirma.itt.seip.instance.properties.PropertyEntryKey;
import com.sirma.itt.seip.util.EqualsHelper;

/**
 * Compound key for persisting property identifier.
 *
 * @author BBonev
 */
@PersistenceUnitBinding(PersistenceUnits.PRIMARY)
@Embeddable
public class PropertyKey implements Serializable, Comparable<PropertyKey>, PropertyEntryKey {
	/**
	 * Comment for serialVersionUID.
	 */
	private static final long serialVersionUID = -603911266167061093L;

	/** The property id. */
	@Column(name = "propertyId", nullable = false)
	private Long propertyId;

	/** The list index. */
	@Column(name = "listIndex", nullable = true)
	private Integer listIndex;

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder(50);
		sb
				.append("PropertyKey")
					.append(" [propertyId=")
					.append(listIndex)
					.append(", listIndex=")
					.append(propertyId)
					.append("]");
		return sb.toString();
	}

	@Override
	public int hashCode() {
		return (propertyId == null ? 0 : propertyId.hashCode()) + (listIndex == null ? 0 : listIndex.hashCode());
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		} else if (!(obj instanceof PropertyKey)) {
			return false;
		}
		// Compare in order of selectivity
		PropertyKey that = (PropertyKey) obj;
		return EqualsHelper.nullSafeEquals(propertyId, that.propertyId)
				&& EqualsHelper.nullSafeEquals(listIndex, that.listIndex);
	}

	/**
	 * throws ClassCastException if the object is not of the correct type.
	 *
	 * @param that
	 *            the that
	 * @return the int
	 */
	@Override
	public int compareTo(PropertyKey that) {
		if (propertyId == null && that.propertyId == null) {
			return 0;
		} else if (propertyId == null && that.propertyId != null) {
			return 1;
		} else if (propertyId != null && that.propertyId == null) {
			return -1;
		} else if (propertyId != null) {
			// Comparison by priority: qnameId, listIndex, localeId, nodeId
			int compare = propertyId.compareTo(that.propertyId);
			if (compare != 0) {
				return compare;
			}
		}

		return listIndex.compareTo(that.listIndex);
	}

	/**
	 * Getter method for propertyId.
	 *
	 * @return the propertyId
	 */
	@Override
	public Long getPropertyId() {
		return propertyId;
	}

	/**
	 * Setter method for propertyId.
	 *
	 * @param propertyId
	 *            the propertyId to set
	 */
	@Override
	public void setPropertyId(Long propertyId) {
		this.propertyId = propertyId;
	}

	/**
	 * Gets the list index.
	 *
	 * @return the list index
	 */
	@Override
	public Integer getListIndex() {
		return listIndex;
	}

	/**
	 * Sets the list index.
	 *
	 * @param listIndex
	 *            the new list index
	 */
	@Override
	public void setListIndex(Integer listIndex) {
		this.listIndex = listIndex;
	}
}
