package com.sirma.itt.seip.permissions;

/**
 * Event is based on {@link PermissionAssignmentChange} but includes information what is changed in inheritance model
 * and how exactly
 *
 * @author bbanchev
 */
public class PermissionInheritanceChange extends PermissionAssignmentChange {

	private final String inheritedFromBefore;
	private final String inheritedFromAfter;
	private final boolean managersOnly;

	/**
	 * Instantiates a new inheritance permission change set.
	 *
	 * @param inheritedFromBefore
	 *            - the inherited from before - if null it is just set
	 * @param inheritedFromAfter
	 *            - the inherited from after - if null it is just removed
	 * @param managersOnly
	 *            if true only the manager permissions will be inherited.
	 */
	public PermissionInheritanceChange(String inheritedFromBefore, String inheritedFromAfter,
			boolean managersOnly) {
		super(null, null, null);
		this.inheritedFromBefore = inheritedFromBefore;
		this.inheritedFromAfter = inheritedFromAfter;
		this.managersOnly = managersOnly;
	}

	/**
	 * Getter method for inheritedFromBefore.
	 *
	 * @return the inheritedFromBefore
	 */
	public String getInheritedFromBefore() {
		return inheritedFromBefore;
	}

	/**
	 * Getter method for managersOnly.
	 *
	 * @return the managersOnly
	 */
	public boolean isManagersOnly() {
		return managersOnly;
	}

	/**
	 * Getter method for inheritedFromAfter.
	 *
	 * @return the inheritedFromAfter
	 */
	public String getInheritedFromAfter() {
		return inheritedFromAfter;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = super.hashCode();
		result = prime * result + (inheritedFromAfter == null ? 0 : inheritedFromAfter.hashCode());
		result = prime * result + (inheritedFromBefore == null ? 0 : inheritedFromBefore.hashCode());
		result = prime * result + (managersOnly ? 1231 : 1237);
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (!super.equals(obj)) {
			return false;
		}
		if (getClass() != obj.getClass()) {
			return false;
		}
		PermissionInheritanceChange other = (PermissionInheritanceChange) obj;
		if (inheritedFromAfter == null) {
			if (other.inheritedFromAfter != null) {
				return false;
			}
		} else if (!inheritedFromAfter.equals(other.inheritedFromAfter)) {
			return false;
		}
		if (inheritedFromBefore == null) {
			if (other.inheritedFromBefore != null) {
				return false;
			}
		} else if (!inheritedFromBefore.equals(other.inheritedFromBefore)) {
			return false;
		}
		if (managersOnly != other.managersOnly) {
			return false;
		}
		return true;
	}

	@Override
	public String toString() {
		return "PermissionInheritanceChangeSet [inheritedFromBefore=" + inheritedFromBefore + ", inheritedFromAfter="
				+ inheritedFromAfter + ", managersOnly=" + managersOnly + "]";
	}

}
