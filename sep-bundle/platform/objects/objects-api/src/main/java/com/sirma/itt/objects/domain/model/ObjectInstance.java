/*
 *
 */
package com.sirma.itt.objects.domain.model;

import java.io.Serializable;
import java.util.LinkedHashMap;
import java.util.Map;

import com.sirma.itt.emf.instance.model.EmfInstance;
import com.sirma.itt.emf.instance.model.InstanceReference;
import com.sirma.itt.emf.instance.model.Lockable;
import com.sirma.itt.objects.constants.ObjectProperties;

/**
 * Instance implementation that represents a business object.
 *
 * @author BBonev
 */
public class ObjectInstance extends EmfInstance implements Lockable {

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
		builder.append("ObjectInstance [id=");
		builder.append(getId());
		builder.append(", identifier=");
		builder.append(getIdentifier());
		builder.append(", revision=");
		builder.append(getRevision());
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
		return (String) getProperties().get(ObjectProperties.LOCKED_BY);
	}

	@Override
	public void setLockedBy(String lockedBy) {
		getProperties().put(ObjectProperties.LOCKED_BY, lockedBy);
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

	@Override
	public InstanceReference getOwningReference() {
		// this is null due to the fact that the object has more then one parent
		// and currently there is no way to know what to return here
		return null;
	}

	@Override
	public void setOwningReference(InstanceReference reference) {
		//No implementation needed
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
