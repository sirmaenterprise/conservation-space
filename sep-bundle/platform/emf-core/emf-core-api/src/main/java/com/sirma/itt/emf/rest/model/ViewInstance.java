package com.sirma.itt.emf.rest.model;

import com.sirma.itt.commons.utils.string.StringUtils;
import com.sirma.itt.emf.instance.model.InstanceReference;
import com.sirma.itt.emf.instance.model.Lockable;

/**
 * The Class ViewInstance.
 * 
 * @author BBonev
 */
public class ViewInstance implements Lockable {

	/** The locked by. */
	private String lockedBy;

	/** The view reference. */
	private InstanceReference viewReference;

	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean isLocked() {
		return StringUtils.isNotNullOrEmpty(getLockedBy());
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public String getLockedBy() {
		return lockedBy;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void setLockedBy(String lockedBy) {
		this.lockedBy = lockedBy;
	}

	/**
	 * Getter method for viewReference.
	 * 
	 * @return the viewReference
	 */
	public InstanceReference getViewReference() {
		return viewReference;
	}

	/**
	 * Setter method for viewReference.
	 * 
	 * @param viewReference
	 *            the viewReference to set
	 */
	public void setViewReference(InstanceReference viewReference) {
		this.viewReference = viewReference;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("ViewInstance [lockedBy=");
		builder.append(lockedBy);
		builder.append(", viewReference=");
		builder.append(viewReference);
		builder.append("]");
		return builder.toString();
	}
}
