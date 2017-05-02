package com.sirma.itt.seip.permissions.rest;

/**
 * Entry to represent a permission assigned to an authority
 *
 * @author BBonev
 */
class PermissionEntry {

	private String authority;
	private String calculated;
	private String special;
	private boolean isManager;
	private String inherited;
	private String library;

	/**
	 * Instantiates a new permission entry.
	 */
	public PermissionEntry() {
		// default constructor
	}

	public String getAuthority() {
		return authority;
	}

	public PermissionEntry setAuthority(String authority) {
		this.authority = authority;
		return this;
	}

	public String getCalculated() {
		return calculated;
	}

	public void setCalculated(String calculated) {
		this.calculated = calculated;
	}

	public String getSpecial() {
		return special;
	}

	public void setSpecial(String special) {
		this.special = special;
	}

	public boolean isManager() {
		return isManager;
	}

	public void setManager(String manager) {
		this.isManager = true;
	}

	public String getInherited() {
		return inherited;
	}

	public void setInherited(String inherited) {
		this.inherited = inherited;
	}

	public String getLibrary() {
		return library;
	}

	public void setLibrary(String library) {
		this.library = library;
	}

}
