package com.sirmaenterprise.sep.roles;

import static com.sirma.itt.seip.util.EqualsHelper.nullSafeEquals;

import java.util.Objects;

import com.sirma.itt.seip.permissions.role.RoleIdentifier;

/**
 * DTO that represents an user defined or internal role
 *
 * @since 2017-03-27
 * @author <a href="mailto:borislav.bonev@sirma.bg">Borislav Bonev</a>
 */
public class RoleDefinition implements RoleIdentifier {

	private static final long serialVersionUID = 3734934429732942489L;

	private String id;
	private int order;
	private boolean enabled;
	private boolean canWrite;
	private boolean canRead;
	private boolean internal;
	private boolean userDefined;

	/**
	 * Default constructor
	 */
	public RoleDefinition() {
		// nothing to do, use default values
	}

	/**
	 * Initialize a role definition instance using the given {@link RoleIdentifier}. The definition will be
	 * automatically enabled.
	 *
	 * @param identifier
	 *            to use as source for the role definition
	 */
	public RoleDefinition(RoleIdentifier identifier) {
		Objects.requireNonNull(identifier, "Role identifier is required");
		id = identifier.getIdentifier();
		order = identifier.getGlobalPriority();
		canRead = identifier.canRead();
		canWrite = identifier.canWrite();
		internal = identifier.isInternal();
		userDefined = identifier.isUserDefined();
		enabled = true;
	}

	public String getId() {
		return id;
	}

	public RoleDefinition setId(String id) {
		this.id = id;
		return this;
	}

	public int getOrder() {
		return order;
	}

	public RoleDefinition setOrder(int order) {
		this.order = order;
		return this;
	}

	public boolean isEnabled() {
		return enabled;
	}

	public RoleDefinition setEnabled(boolean enabled) {
		this.enabled = enabled;
		return this;
	}

	public boolean isCanWrite() {
		return canWrite;
	}

	public RoleDefinition setCanWrite(boolean canWrite) {
		this.canWrite = canWrite;
		return this;
	}

	public boolean isCanRead() {
		return canRead;
	}

	public RoleDefinition setCanRead(boolean canRead) {
		this.canRead = canRead;
		return this;
	}

	@Override
	public boolean isInternal() {
		return internal;
	}

	public RoleDefinition setInternal(boolean internal) {
		this.internal = internal;
		return this;
	}

	@Override
	public boolean isUserDefined() {
		return userDefined;
	}

	public RoleDefinition setUserDefined(boolean userDefined) {
		this.userDefined = userDefined;
		return this;
	}

	@Override
	public String getIdentifier() {
		return getId();
	}

	@Override
	public int getGlobalPriority() {
		return getOrder();
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (!(obj instanceof RoleDefinition)) {
			return false;
		}
		RoleDefinition other = (RoleDefinition) obj;
		return nullSafeEquals(id, other.getId());
	}

	@Override
	public int hashCode() {
		return 31 + ((id == null) ? 0 : id.hashCode());
	}

	@Override
	public boolean canRead() {
		return isCanRead();
	}

	@Override
	public boolean canWrite() {
		return isCanWrite();
	}
}
