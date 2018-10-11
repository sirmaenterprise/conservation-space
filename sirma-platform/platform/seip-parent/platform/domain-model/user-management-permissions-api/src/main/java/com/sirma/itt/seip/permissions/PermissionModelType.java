package com.sirma.itt.seip.permissions;

/**
 * Contains information about the type of the permission model.
 * 
 * @author Adrian Mitev
 */
public final class PermissionModelType {

	public static final PermissionModelType UNDEFINED = new PermissionModelType(false, false, false);

	public static final PermissionModelType INHERITED = new PermissionModelType(true, false, false);

	public static final PermissionModelType LIBRARY = new PermissionModelType(false, true, false);

	public static final PermissionModelType SPECIAL = new PermissionModelType(false, false, true);

	private final boolean inherited;

	private final boolean library;

	private final boolean special;

	private final boolean calculated;

	/**
	 * Initializer.
	 * 
	 * @param inherited
	 *            true if the permissions should be inherited from the parent.
	 * @param library
	 *            true if the permissions should be inherited from the library.
	 * @param special
	 *            true if there are special permissions.
	 * @param calculated
	 *            if true, the permission model is calculated using the algorithm for active permissions.
	 */
	public PermissionModelType(boolean inherited, boolean library, boolean special, boolean calculated) {
		this.inherited = inherited;
		this.library = library;
		this.special = special;
		this.calculated = calculated;
	}

	/**
	 * Initializer.
	 * 
	 * @param inherited
	 *            true if the permissions should be inherited from the parent.
	 * @param library
	 *            true if the permissions should be inherited from the library.
	 * @param special
	 *            true if there are special permissions.
	 */
	public PermissionModelType(boolean inherited, boolean library, boolean special) {
		this(inherited, library, special, false);
	}

	/**
	 * Checks if the permission model type is defined.
	 * 
	 * @return true if defined, false otherwise.
	 */
	public boolean isDefined() {
		return inherited || library || special || calculated;
	}

	public boolean isInherited() {
		return inherited;
	}

	public boolean isLibrary() {
		return library;
	}

	public boolean isSpecial() {
		return special;
	}

	@Override
	public String toString() {
		return "PermissionModel [inherited=" + inherited + ", library=" + library + ", special=" + special + "]";
	}

}
