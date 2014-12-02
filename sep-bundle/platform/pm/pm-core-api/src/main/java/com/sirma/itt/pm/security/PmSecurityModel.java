package com.sirma.itt.pm.security;

import com.sirma.itt.emf.security.SecurityModel;
import com.sirma.itt.emf.security.model.RoleIdentifier;

/**
 * Interface to define the security model for PM module. Here are defined the possible user roles in
 * relation to project module
 *
 * @author BBonev
 */
public interface PmSecurityModel extends SecurityModel {

	/**
	 * The Enum PmRole for PM module
	 */
	enum PmRoles implements RoleIdentifier {
		/** The administrator. */
		PROJECT_MANAGER("PROJECT_MANAGER", 80);

		/** The identifier. */
		private String identifier;
		private int priority;

		/**
		 * Instantiates a new pm role.
		 *
		 * @param identifier
		 *            the identifier
		 * @param priority
		 *            is the global prioriry
		 */
		private PmRoles(String identifier, int priority) {
			this.identifier = identifier;
			this.priority = priority;
		}

		/**
		 * {@inheritDoc}
		 */
		@Override
		public String getIdentifier() {
			return identifier;
		}

		/**
		 * Finds the identifier based on {@link #getIdentifier()} return value for each enum entry.
		 *
		 * @param id
		 *            is the id to find
		 * @return the found entry or null
		 */
		public static RoleIdentifier getIdentifier(String id) {
			if (id == null) {
				return null;
			}
			String roleId = id.toUpperCase();
			for (PmRoles role : values()) {
				if (role.getIdentifier().equals(roleId)) {
					return role;
				}
			}
			return null;
		}

		@Override
		public int getGlobalPriority() {
			return priority;
		}
	}

}
