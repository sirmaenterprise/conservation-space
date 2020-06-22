package com.sirma.itt.seip.permissions;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import com.sirma.itt.seip.permissions.model.PermissionId;
import com.sirma.itt.seip.permissions.model.RoleId;
import com.sirma.itt.seip.permissions.role.RoleIdentifier;

/**
 * Defines some basic security constants.
 *
 * @author BBonev
 */
public interface SecurityModel {

	/** The permission read. */
	PermissionIdentifier PERMISSION_READ = new PermissionId("read");
	/** The permission edit. */
	PermissionIdentifier PERMISSION_EDIT = new PermissionId("edit");
	/** The permission delete. */
	PermissionIdentifier PERMISSION_DELETE = new PermissionId("delete");

	/**
	 * The EmfRole enum provides basic roles for emf functionality
	 */
	class BaseRoles {
		/**
		 * The administrator.<br>
		 * <strong> Full permission for anything.</strong>
		 */
		public static final RoleIdentifier ADMINISTRATOR = new RoleId("ADMINISTRATOR", 100)
				.setCanRead(true)
					.setCanWrite(true)
					.setInternal(true)
					.setUserDefined(false);

		/**
		 * The manager.<br>
		 * <strong>Almost full permissions, but could be less than {@link #ADMINISTRATOR}</strong>
		 */
		public static final RoleIdentifier MANAGER = new RoleId("MANAGER", 99)
				.setCanRead(true)
					.setCanWrite(true)
					.setInternal(false)
					.setUserDefined(false);
		/**
		 * The collaborator.<br>
		 * <strong> Has edit permissions on the content created by him and on the content created by other users. Has
		 * delete permissions only on the content created by him.</strong>
		 */
		public static final RoleIdentifier COLLABORATOR = new RoleId("COLLABORATOR", 15)
				.setCanRead(true)
					.setCanWrite(true)
					.setInternal(false)
					.setUserDefined(false);
		/**
		 * The contributor.<br>
		 * <strong> Has edit/ delete permissions on the content he created.</strong>
		 */
		public static final RoleIdentifier CONTRIBUTOR = new RoleId("CONTRIBUTOR", 9)
				.setCanRead(true)
					.setCanWrite(true)
					.setInternal(false)
					.setUserDefined(false);
		/**
		 * The creator. This role is relevant only for object that are not part of permission model - tasks, workflows,
		 * comments, topics, relations, etc.<br>
		 * <strong> This is the user who created an object. For example the creator of a task could create sub-task of
		 * this task </strong>
		 */
		public static final RoleIdentifier CREATOR = new RoleId("CREATOR", 10)
				.setCanRead(true)
					.setCanWrite(true)
					.setInternal(true)
					.setUserDefined(false);
		/**
		 * The consumer.<br>
		 * <strong> Has view only permissions on the content of a instances. Cannot create new content (upload
		 * documents, create cases, assign tasks, etc.)</strong>
		 */
		public static final RoleIdentifier CONSUMER = new RoleId("CONSUMER", 5)
				.setCanRead(true)
					.setCanWrite(false)
					.setInternal(false)
					.setUserDefined(false);
		/**
		 * The viewer.<br>
		 * <strong> Knows only content exists, but has no permission to see it </strong>
		 */
		public static final RoleIdentifier VIEWER = new RoleId("VIEWER", 0)
				.setCanRead(true)
					.setCanWrite(false)
					.setInternal(true)
					.setUserDefined(false);
		/**
		 * The viewer.<br>
		 * <strong> Knows only content exists, but has no permission to see it </strong>
		 */
		public static final RoleIdentifier NO_PERMISSION = new RoleId("NO_PERMISSION", -1)
				.setCanRead(false)
					.setCanWrite(false)
					.setInternal(true)
					.setUserDefined(false);

		/** Collection of all roles */
		public static final List<RoleIdentifier> ALL = Collections.unmodifiableList(Arrays.asList(ADMINISTRATOR,
				MANAGER, COLLABORATOR, CONTRIBUTOR, CREATOR, CONSUMER, VIEWER, NO_PERMISSION));

		/** The list of default public roles that can be assigned by users */
		public static final List<RoleIdentifier> PUBLIC = Collections
				.unmodifiableList(Arrays.asList(CONSUMER, CONTRIBUTOR, COLLABORATOR, MANAGER));

		/**
		 * Finds the identifier based on {@link RoleIdentifier#getIdentifier()} return value for each enum entry
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
			for (RoleIdentifier role : ALL) {
				if (role.getIdentifier().equals(roleId)) {
					return role;
				}
			}
			return null;
		}

	}

	/**
	 * The ActivitiRoles provides roles related to processes in SEP
	 */
	class ActivitiRoles {

		/**
		 * The assignee.<br>
		 * <strong> Assignee is any registered user to whom has been assigned a task. When user is assginee of a task in
		 * a case, he gets individual permissions of a contributor/ collaborator (to be able to set it up) till he
		 * completes his task </strong>
		 */
		public static final RoleIdentifier ASSIGNEE = new RoleId("ASSIGNEE", 15)
				.setCanRead(true)
					.setCanWrite(true)
					.setInternal(true)
					.setUserDefined(false);

		public static final RoleIdentifier POSSIBLE_ASSIGNEE = new RoleId("POSSIBLE_ASSIGNEE", 16)
				.setCanRead(true)
					.setCanWrite(false)
					.setInternal(true)
					.setUserDefined(false);
	}
}
