/*
 * Copyright (C) 2005-2010 Alfresco Software Limited. This file is part of Alfresco Alfresco is free
 * software: you can redistribute it and/or modify it under the terms of the GNU Lesser General
 * Public License as published by the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version. Alfresco is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A
 * PARTICULAR PURPOSE. See the GNU Lesser General Public License for more details. You should have
 * received a copy of the GNU Lesser General Public License along with Alfresco. If not, see
 * <http://www.gnu.org/licenses/>.
 */
package com.sirma.itt.emf.properties.entity;

import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.Embeddable;

import com.sirma.itt.emf.util.EqualsHelper;

/**
 * Compound key for persistence of.
 * 
 * @author BBonev
 * @since 3.4
 */
@Embeddable
public class PropertyKey implements Serializable, Comparable<PropertyKey> {
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
		StringBuilder sb = new StringBuilder();
		sb.append("NodePropertyKey").append(" [propertyId=").append(listIndex)
				.append(", listIndex=").append(propertyId).append("]");
		return sb.toString();
	}

	@Override
	public int hashCode() {
		return (propertyId == null ? 0 : propertyId.hashCode())
				+ (listIndex == null ? 0 : listIndex.hashCode());
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
		return (EqualsHelper.nullSafeEquals(propertyId, that.propertyId) && EqualsHelper
				.nullSafeEquals(listIndex, that.listIndex));
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
		if ((propertyId == null) && (that.propertyId == null)) {
			return 0;
		} else if ((propertyId == null) && (that.propertyId != null)) {
			return 1;
		} else if ((propertyId != null) && (that.propertyId == null)) {
			return -1;
		}
		
		// Comparison by priority: qnameId, listIndex, localeId, nodeId
		int compare = propertyId.compareTo(that.propertyId);
		if (compare != 0) {
			return compare;
		}

		return listIndex.compareTo(that.listIndex);
	}

	/**
	 * Getter method for propertyId.
	 * 
	 * @return the propertyId
	 */
	public Long getPropertyId() {
		return propertyId;
	}

	/**
	 * Setter method for propertyId.
	 * 
	 * @param propertyId
	 *            the propertyId to set
	 */
	public void setPropertyId(Long propertyId) {
		this.propertyId = propertyId;
	}

	/**
	 * Gets the list index.
	 * 
	 * @return the list index
	 */
	public Integer getListIndex() {
		return listIndex;
	}

	/**
	 * Sets the list index.
	 * 
	 * @param listIndex
	 *            the new list index
	 */
	public void setListIndex(Integer listIndex) {
		this.listIndex = listIndex;
	}
}
