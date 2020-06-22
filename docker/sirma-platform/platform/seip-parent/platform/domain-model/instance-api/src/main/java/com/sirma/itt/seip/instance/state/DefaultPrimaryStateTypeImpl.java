package com.sirma.itt.seip.instance.state;

import java.io.Serializable;

/**
 * Default implementation to realize the interface
 *
 * @author BBonev
 */
public class DefaultPrimaryStateTypeImpl implements PrimaryStates, Serializable {

	/**
	 * Comment for serialVersionUID.
	 */
	private static final long serialVersionUID = -7180073716140849523L;
	/** The type. */
	private final String type;

	/**
	 * Instantiates a new default primary state type impl.
	 *
	 * @param type
	 *            the type
	 */
	public DefaultPrimaryStateTypeImpl(String type) {
		this.type = type;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public String getType() {
		return type;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + (type == null ? 0 : type.hashCode());
		return result;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null) {
			return false;
		}
		if (!(obj instanceof PrimaryStates)) {
			return false;
		}
		PrimaryStates other = (PrimaryStates) obj;
		if (type == null) {
			if (other.getType() != null) {
				return false;
			}
		} else if (!type.equals(other.getType())) {
			return false;
		}
		return true;
	}

}