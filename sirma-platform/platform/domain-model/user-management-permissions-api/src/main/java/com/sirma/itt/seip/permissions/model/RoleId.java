package com.sirma.itt.seip.permissions.model;

import com.sirma.itt.seip.permissions.role.RoleIdentifier;
import com.sirma.itt.seip.util.EqualsHelper;

/**
 * Class that represents a role identifier object.
 *
 * @author BBonev
 */
public class RoleId implements RoleIdentifier {

	private static final long serialVersionUID = -9162655856061573456L;

	private String identifier;
	private Integer priority;

	private boolean isInternal;
	private boolean canRead;
	private boolean canWrite;
	private boolean isUserDefined;

	/**
	 * Instantiates a new role id.
	 */
	public RoleId() {
		// default constructor
	}

	/**
	 * Instantiates a new role id.
	 *
	 * @param id
	 *            the id
	 * @param priority
	 *            the priority
	 */
	public RoleId(String id, int priority) {
		identifier = id;
		this.priority = Integer.valueOf(priority);
	}

	@Override
	public String getIdentifier() {
		return identifier;
	}

	@Override
	public int getGlobalPriority() {
		Integer local = getPriority();
		if (local == null) {
			return 0;
		}
		return local.intValue();
	}

	public void setIdentifier(String identifier) {
		this.identifier = identifier;
	}

	public Integer getPriority() {
		return priority;
	}

	public void setPriority(Integer priority) {
		this.priority = priority;
	}

	@Override
	public boolean canRead() {
		return canRead;
	}

	@Override
	public boolean canWrite() {
		return canWrite;
	}

	@Override
	public boolean isInternal() {
		return isInternal;
	}

	@Override
	public boolean isUserDefined() {
		return isUserDefined;
	}

	public RoleId setInternal(boolean isInternal) {
		this.isInternal = isInternal;
		return this;
	}

	public RoleId setCanRead(boolean canRead) {
		this.canRead = canRead;
		return this;
	}

	public RoleId setCanWrite(boolean canWrite) {
		this.canWrite = canWrite;
		return this;
	}

	public RoleId setUserDefined(boolean isUserDefined) {
		this.isUserDefined = isUserDefined;
		return this;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + (identifier == null ? 0 : identifier.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (!(obj instanceof RoleIdentifier)) {
			return false;
		}
		RoleIdentifier other = (RoleIdentifier) obj;
		return EqualsHelper.nullSafeEquals(identifier, other.getIdentifier());
	}

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder(32);
		builder
				.append(identifier)
					.append("(")
					.append(priority)
					.append(")[canRead=")
					.append(canRead)
					.append(", canWrite=")
					.append(canWrite)
					.append(", internal=")
					.append(isInternal)
					.append("]");
		return builder.toString();
	}

}
