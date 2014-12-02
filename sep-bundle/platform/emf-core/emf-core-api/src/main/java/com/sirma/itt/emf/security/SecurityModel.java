package com.sirma.itt.emf.security;

import com.sirma.itt.emf.security.model.EmfPermission;
import com.sirma.itt.emf.security.model.Permission;
import com.sirma.itt.emf.security.model.RoleIdentifier;

/**
 * Defines some basic security constants.
 *
 * @author BBonev
 */
public interface SecurityModel {

	/** The permission read. */
	Permission PERMISSION_READ = new EmfPermission("read");
	/** The permission comment. */
	Permission PERMISSION_COMMENT = new EmfPermission("comment");
	/** The permission create. */
	Permission PERMISSION_CREATE = new EmfPermission("create");

	/** The permission edit. */
	Permission PERMISSION_EDIT = new EmfPermission("edit");
	/** The permission edit. */
	Permission PERMISSION_CUSTOMIZE = new EmfPermission("customize");
	/** The permission delete. */
	Permission PERMISSION_DELETE = new EmfPermission("delete");

	/** The permission lock. */
	Permission PERMISSION_LOCK = new EmfPermission("lock");

	/** The permission revert. */
	Permission PERMISSION_REVERT = new EmfPermission("revert");

	/**
	 * The external permissions. In this permission are located all other actions that are not
	 * distributed between the roles, but are added to the actions registry.
	 */
	Permission PERMISSION_EXTERNAL = new EmfPermission("external");

	/**
	 * The EmfRole enum provides basic roles for emf functionality
	 */
	enum BaseRoles implements RoleIdentifier {
		/**
		 * The administrator.<br>
		 * <strong> Full permission for anything.</strong>
		 */
		ADMINISTRATOR("ADMINISTRATOR", 100),

		/**
		 * The manager.<br>
		 * <strong>Almost full permissions, but could be less than {@link #ADMINISTRATOR}</strong>
		 */
		MANAGER("MANAGER", 20),
		/**
		 * The collaborator.<br>
		 * <strong> Has edit permissions on the content created by him and on the content created by
		 * other users. Has delete permissions only on the content created by him.</strong>
		 */
		COLLABORATOR("COLLABORATOR", 15),
		/**
		 * The contributor.<br>
		 * <strong> Has edit/ delete permissions on the content he created.</strong>
		 */
		CONTRIBUTOR("CONTRIBUTOR", 9),
		/**
		 * The creator.<br>
		 * <strong> This is the user who created an object. For example the creator of a task could
		 * create sub-task of this task </strong>
		 */
		CREATOR("CREATOR", 10),
		/**
		 * The consumer.<br>
		 * <strong> Has view only permissions on the content of a instances. Cannot create new
		 * content (upload documents, create cases, assign tasks, etc.)</strong>
		 */
		CONSUMER("CONSUMER", 5),
		/**
		 * The viewer.<br>
		 * <strong> Knows only content exists, but has no permission to see it </strong>
		 */
		VIEWER("VIEWER", 0),
		/**
		 * The viewer.<br>
		 * <strong> Knows only content exists, but has no permission to see it </strong>
		 */
		NO_PERMISSION("NO_PERMISSION", -1);

		/** The identifier. */
		private String identifier;

		/** The priority. higher means greater authority. TODO */
		private int priority;

		/**
		 * Instantiates a new emf role.
		 *
		 * @param identifier
		 *            the identifier
		 * @param priority
		 *            the priority
		 */
		private BaseRoles(String identifier, int priority) {
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
		 * Finds the identifier based on {@link #getIdentifier()} return value for each enum entry
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
			for (BaseRoles role : values()) {
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

	/**
	 * The ActivitiRoles enum provides roles related to processes in emf
	 */
	enum ActivitiRoles implements RoleIdentifier {

		/**
		 * The assignee.<br>
		 * <strong> Assignee is any registered user to whom has been assigned a task. When user is
		 * assginee of a task in a case, he gets individual permissions of a contributor/
		 * collaborator (to be able to set it up) till he completes his task </strong>
		 */
		ASSIGNEE("ASSIGNEE", 15),

		POSSIBLE_ASSIGNEE("POSSIBLE_ASSIGNEE", 16);
		/** The identifier. */
		private String identifier;

		/** The priority. higher means greater authority. */
		private int priority;

		/**
		 * Instantiates a new emf role.
		 *
		 * @param identifier
		 *            the identifier
		 * @param priority
		 *            the priority
		 */
		private ActivitiRoles(String identifier, int priority) {
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
		 * Finds the identifier based on {@link #getIdentifier()} return value for each enum entry
		 *
		 * @param id
		 *            is the id to find
		 * @return the found entry or null
		 */
		public static ActivitiRoles getIdentifier(String id) {
			if (id == null) {
				return null;
			}
			String roleId = id.toUpperCase();
			for (ActivitiRoles role : values()) {
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
