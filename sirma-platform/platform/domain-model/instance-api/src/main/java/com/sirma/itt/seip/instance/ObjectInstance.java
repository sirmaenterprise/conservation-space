/*
 *
 */
package com.sirma.itt.seip.instance;

import java.io.Serializable;
import java.util.LinkedHashMap;
import java.util.Map;

import com.sirma.itt.seip.domain.instance.DefaultProperties;
import com.sirma.itt.seip.domain.instance.EmfInstance;
import com.sirma.itt.seip.domain.instance.Lockable;

/**
 * Instance implementation that represents a business object.
 *
 * @author BBonev
 */
public class ObjectInstance extends EmfInstance implements Lockable, Cloneable {

	/**
	 * Comment for serialVersionUID.
	 */
	private static final long serialVersionUID = 2949311977301845446L;

	/**
	 * {@inheritDoc}
	 */
	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append(getClass().getSimpleName()).append(" [id=");
		builder.append(getId());
		builder.append(", identifier=");
		builder.append(getIdentifier());
		builder.append(", container=");
		builder.append(getContainer());
		builder.append(", dmsId=");
		builder.append(getDmsId());
		builder.append(", contentManagementId=");
		builder.append(getContentManagementId());
		builder.append(", properties=");
		builder.append(getProperties());
		builder.append("]");
		return builder.toString();
	}

	@Override
	public boolean isLocked() {
		return getLockedBy() != null;
	}

	@Override
	public String getLockedBy() {
		return getString(DefaultProperties.LOCKED_BY);
	}

	@Override
	public void setLockedBy(String lockedBy) {
		add(DefaultProperties.LOCKED_BY, lockedBy);
	}

	@Override
	public Map<String, Serializable> getProperties() {
		Map<String, Serializable> map = super.getProperties();
		if (map == null) {
			map = new LinkedHashMap<>();
			setProperties(map);
		}
		return map;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = super.hashCode();
		return result * prime;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (!super.equals(obj)) {
			return false;
		}
		if (getClass() != obj.getClass()) {
			return false;
		}
		return true;
	}

}
