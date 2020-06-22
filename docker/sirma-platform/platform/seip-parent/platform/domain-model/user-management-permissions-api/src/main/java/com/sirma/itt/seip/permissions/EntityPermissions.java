package com.sirma.itt.seip.permissions;

import java.util.HashSet;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Stream;

/**
 * Represents the permission model for an instance. This include the parent and library inheriatnce
 * sources and if they are enabled as well as the permission role assignments to an instance.
 *
 * @author <a href="mailto:borislav.bonev@sirma.bg">Borislav Bonev</a>
 * @since 28/06/2017
 */
public class EntityPermissions {
	private final String entityId;
	private final String parent;
	private final String library;
	private final boolean inheritFromParent;
	private final boolean inheritFromLibrary;
	private final boolean isLibrary;

	private Set<Assignment> assignments;

	/**
	 * Instantiate with information about the target entity and it's inheritance information
	 *
	 * @param entityId the target entity that this model applies
	 * @param parent the id of the parent instance used for parent inheritance when enabled
	 * @param library the id of the library used for library inheritance when enabled
	 * @param inheritFromParent specifies if the parent inheritance is enabled
	 * @param inheritFromLibrary specifies if library inheritance is enabled
	 * @param isLibrary identifies if the current instance is library itself or not
	 */
	public EntityPermissions(String entityId, String parent, String library, boolean inheritFromParent,
			boolean inheritFromLibrary, boolean isLibrary) {
		this.entityId = Objects.requireNonNull(entityId, "Entity id is required");
		this.parent = parent;
		this.library = library;
		this.inheritFromParent = inheritFromParent;
		this.inheritFromLibrary = inheritFromLibrary;
		this.isLibrary = isLibrary;
	}

	/**
	 * Instantiate for the given instance with disabled inheritance permissions
	 *
	 * @param entityId the target entity that this model applies
	 */
	public EntityPermissions(String entityId) {
		this(entityId, null, null, false, false, false);
	}

	/**
	 * Register particular authority to have the given role as permission assignment
	 *
	 * @param authority the authority id to assign, cannot be null
	 * @param role the role to assign to the authority, cannot be null
	 * @return the same instance to allow method chaining
	 */
	public EntityPermissions addAssignment(String authority, String role) {
		getOrCreateAssignments()
				.add(new Assignment(
						Objects.requireNonNull(authority, "Authority is required"),
						Objects.requireNonNull(role, "Role is required")
				));
		return this;
	}

	public String getParent() {
		return parent;
	}

	public String getLibrary() {
		return library;
	}

	public String getEntityId() {
		return entityId;
	}

	public boolean isInheritFromParent() {
		return inheritFromParent;
	}

	public boolean isInheritFromLibrary() {
		return inheritFromLibrary;
	}

	public boolean isLibrary() {
		return isLibrary;
	}

	/**
	 * Stream all registered assignments
	 *
	 * @return the assignments
	 */
	public Stream<Assignment> getAssignments() {
		if (assignments == null) {
			return Stream.empty();
		}
		return assignments.stream();
	}

	private Set<Assignment> getOrCreateAssignments() {
		if (assignments == null) {
			assignments = new HashSet<>();
		}
		return assignments;
	}

	/**
	 * Represent a single assignment for authority and role
	 *
	 * @author BBonev
	 */
	public static class Assignment {
		private final String authority;
		private final String role;

		/**
		 * Instantiate assignment
		 *
		 * @param authority the authority to set
		 * @param role the role to set
		 */
		public Assignment(String authority, String role) {
			this.authority = authority;
			this.role = role;
		}

		public String getAuthority() {
			return authority;
		}

		public String getRole() {
			return role;
		}

		@Override
		public boolean equals(Object o) {
			if (this == o) {
				return true;
			}
			if (!(o instanceof Assignment)) {
				return false;
			}
			Assignment that = (Assignment) o;
			return authority.equals(that.authority) && role.equals(that.role);
		}

		@Override
		public int hashCode() {
			int result = authority.hashCode();
			result = 31 * result + role.hashCode();
			return result;
		}

		@Override
		public String toString() {
			return new StringBuilder(60)
					.append("Assignment{")
					.append("authority='").append(authority).append('\'')
					.append(", role='").append(role).append('\'')
					.append('}')
					.toString();
		}
	}
}
