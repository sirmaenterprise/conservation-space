package com.sirma.itt.seip.permissions.role;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Represents a change in the permission model.
 *
 * @author Adrian Mitev
 */
public interface PermissionsChange {

	/**
	 * Represents a change which purpose is to add or replace a role assignment for a give authority.
	 *
	 * @author Adrian Mitev
	 */
	public class AddRoleAssignmentChange extends RoleAssignmentChange {
		AddRoleAssignmentChange(String authority, String role) {
			super(authority, role);
		}
	}

	/**
	 * Constructs a change set builder.
	 *
	 * @return constucted builder.
	 */
	static PermissionsChangeBuilder builder() {
		return new PermissionsChangeBuilder();
	}

	/**
	 * Represents a change which purpose is to remove a role assignment for a give authority.
	 *
	 * @author Adrian Mitev
	 */
	class RemoveRoleAssignmentChange extends RoleAssignmentChange {

		RemoveRoleAssignmentChange(String authority, String role) {
			super(authority, role);
		}
	}

	/**
	 * Represents a change which purpose is to set (or remove) the parent from which assignments are inherited.
	 *
	 * @author Adrian Mitev
	 */
	class ParentChange extends ValueHolder<String> implements PermissionsChange {

		ParentChange(String parent) {
			super(parent);
		}
	}

	/**
	 * Represents a change which purpose is to set the flag showing if all assignments should be inherited from the
	 * parent or only the manager assignments. When the flag is true all assignments of the parent instance are
	 * inherited. If false, only the managers assignments will be inherited.
	 *
	 * @author Adrian Mitev
	 */
	class InheritFromParentChange extends ValueHolder<Boolean> implements PermissionsChange {

		InheritFromParentChange(Boolean value) {
			super(value);
		}
	}

	/**
	 * Represents a change which purpose is to set (or remove) the library from which assignments are inherited.
	 *
	 * @author Adrian Mitev
	 */
	class LibraryChange extends ValueHolder<String> implements PermissionsChange {

		LibraryChange(String library) {
			super(library);
		}
	}

	/**
	 * Represents a change which purpose is to set the flag showing if all assignments should be inherited from the
	 * library or only the manager assignments. When the flag is true all assignments of the library instance are
	 * inherited. If false, only the managers assignments will be inherited.
	 *
	 * @author Adrian Mitev
	 */
	class InheritFromLibraryChange extends ValueHolder<Boolean> implements PermissionsChange {

		InheritFromLibraryChange(Boolean value) {
			super(value);
		}
	}

	/**
	 * Represents a change indicating that the particular instance is a library or not.
	 *
	 * @author Adrian Mitev
	 */
	class SetLibraryIndicatorChange extends ValueHolder<Boolean> implements PermissionsChange {

		SetLibraryIndicatorChange(Boolean value) {
			super(value);
		}
	}

	/**
	 * Helper class that builds a list of {@link PermissionsChange}.
	 *
	 * @author Adrian Mitev
	 */
	class PermissionsChangeBuilder {

		final List<PermissionsChange> changes;

		private PermissionsChangeBuilder() {
			changes = new ArrayList<>();
		}

		/**
		 * Adds a change which purpose is to add or replace a role assignment for a give authority.
		 *
		 * @param authority
		 *            authority for which a permission is added.
		 * @param role
		 *            role to be set.
		 * @return instantiated change.
		 */
		public PermissionsChangeBuilder addRoleAssignmentChange(String authority, String role) {
			changes.add(new AddRoleAssignmentChange(authority, role));
			return this;
		}

		/**
		 * Adds a change which purpose is to remove a role assignment for a give authority.
		 *
		 * @param authority
		 *            authority for which a permission is added.
		 * @param role
		 *            role to be set.
		 * @return instantiated change.
		 */
		public PermissionsChangeBuilder removeRoleAssignmentChange(String authority, String role) {
			changes.add(new RemoveRoleAssignmentChange(authority, role));
			return this;
		}

		/**
		 * Adds a change which purpose is to set (or remove) the parent from which assignments are inherited.
		 *
		 * @param parent
		 *            id of the parent.
		 * @return instantiated change.
		 */
		public PermissionsChangeBuilder parentChange(String parent) {
			changes.add(new ParentChange(parent));
			return this;
		}

		/**
		 * Adds a change which purpose is to set the flag showing if all assignments should be inherited from the parent
		 * or only the manager assignments. When the flag is true all assignments of the parent instance are inherited.
		 * If false, only the managers assignments will be inherited.
		 *
		 * @param value
		 *            flag value.
		 * @return instantiated change.
		 */
		public PermissionsChangeBuilder inheritFromParentChange(Boolean value) {
			changes.add(new InheritFromParentChange(value));
			return this;
		}

		/**
		 * Adds a change which purpose is to set (or remove) the library from which assignments are inherited.
		 *
		 * @param library
		 *            id of the library.
		 * @return instantiated change.
		 */
		public PermissionsChangeBuilder libraryChange(String library) {
			changes.add(new LibraryChange(library));
			return this;
		}

		/**
		 * Adds a change which purpose is to set the flag showing if all assignments should be inherited from the
		 * library or only the manager assignments. When the flag is true all assignments of the library instance are
		 * inherited. If false, only the managers assignments will be inherited.
		 *
		 * @param value
		 *            flag value.
		 * @return instantiated change.
		 */
		public PermissionsChangeBuilder inheritFromLibraryChange(Boolean value) {
			changes.add(new InheritFromLibraryChange(value));
			return this;
		}

		/**
		 * Adds a change indicating that the particular instance is a library or not.d.
		 *
		 * @param value
		 *            flag value.
		 * @return instantiated change.
		 */
		public PermissionsChangeBuilder setLibraryIndicatorChange(Boolean value) {
			changes.add(new SetLibraryIndicatorChange(value));
			return this;
		}

		/**
		 * Builds a list containing the added changes.
		 *
		 * @return unmodifiable list with the changes.
		 */
		public synchronized List<PermissionsChange> build() {
			return Collections.unmodifiableList(changes);
		}

		/**
		 * Builds a list of containing changes and clears the stored changes. Any calls to the methods {@link #build()}
		 * and {@link #buildAndReset()} will return empty lists unless any of the other methods is called to add some
		 * changes.
		 *
		 * @return unmodifiable list with the changes
		 */
		public synchronized List<PermissionsChange> buildAndReset() {
			// clone the list contents as the unmodifiable list is only a proxy
			List<PermissionsChange> changesCopy = Collections.unmodifiableList(new ArrayList<>(changes));
			changes.clear();
			return changesCopy;
		}
	}
}

abstract class ValueHolder<T> {

	private final T value;

	public ValueHolder(T value) {
		this.value = value;
	}

	public T getValue() {
		return value;
	}
}

abstract class RoleAssignmentChange implements PermissionsChange {

	private final String authority;
	private final String role;

	RoleAssignmentChange(String authority, String role) {
		this.authority = authority;
		this.role = role;
	}

	public String getAuthority() {
		return authority;
	}

	public String getRole() {
		return role;
	}
}
