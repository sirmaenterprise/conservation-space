package com.sirma.itt.emf.security.model;

import com.sirma.itt.emf.resources.ResourceType;
import com.sirma.itt.emf.resources.model.EmfResource;
import com.sirma.itt.emf.resources.model.Resource;

/**
 * The EmfGroup is holder for authority groups
 */
public class EmfGroup extends EmfResource implements Group {

	/** The Constant serialVersionUID. */
	private static final long serialVersionUID = -1983712618050970361L;

	/**
	 * Instantiates a new emf group.
	 */
	public EmfGroup() {
		// default constructor
	}

	/**
	 * Instantiates a new emf group.
	 *
	 * @param groupId
	 *            the groupid
	 * @param displayName
	 *            the display name
	 */
	public EmfGroup(String groupId, String displayName) {
		setIdentifier(groupId);
		setDisplayName(displayName);
		setType(ResourceType.GROUP);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public EmfGroup clone() {
		EmfGroup emfGroup = new EmfGroup(getIdentifier(), getDisplayName());
		emfGroup.setId(getId());
		return emfGroup;
	}

	@Override
	public String getName() {
		return getIdentifier();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((identifier == null) ? 0 : identifier.hashCode());
		result = prime * result + ((type == null) ? 0 : type.hashCode());
		return result;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (!(obj instanceof Resource))
			return false;
		Resource other = (Resource) obj;
		if (type != other.getType())
			return false;
		if (identifier == null) {
			if (other.getIdentifier() != null)
				return false;
		} else if (!identifier.equals(other.getIdentifier()))
			return false;
		return true;
	}
}
