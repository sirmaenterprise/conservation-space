package com.sirma.itt.migration.constants;


/**
 * Enumeration defining the possible values for a migrated file
 *
 * @author BBonev
 */
public enum MigrationStatus {
	/**
	 * Not yet added to the register.
	 */
	NOT_ADDED(-2, " "),
	/**
	 * The file is not yet migrated
	 */
	NOT_MIGRATED(0, "Not migrated"),
	/**
	 * The file is migrated
	 */
	MIGRATED(1, "Migrated"),
	/**
	 * The file will not be migrated for now
	 */
	WILL_NOT_BE_MIGRATED(-1, "Will not be migrated"),
	/**
	 * The file is for migration but the migration was unsuccessful
	 */
	FAILED_TO_MIGRATE(-3, "Failed to migrate");

	private final Integer status;
	private String displayName;

	/**
	 * Initialize the code
	 * 
	 * @param status
	 *            is the status code
	 * @param name
	 *            is the display name of the value
	 */
	private MigrationStatus(Integer status, String name) {
		this.status = status;
		displayName = name;
	}

	/**
	 * Returns the status code for the current enumeration value
	 *
	 * @return the code
	 */
	public Integer getStatusCode() {
		return status;
	}

	/**
	 * Returns a {@link MigrationStatus} enumeration value by status code.
	 * Default code is {@link #NOT_MIGRATED}.
	 *
	 * @param value
	 *            is the code to search for
	 * @return the enumeration value that represent the given code, if not found
	 *         the {@value #NOT_MIGRATED} will be returned.
	 */
	public static MigrationStatus getStatus(int value) {
		switch (value) {
			case 1:
				return MIGRATED;
			case -1:
				return WILL_NOT_BE_MIGRATED;
			case -2:
				return NOT_ADDED;
			case -3:
				return FAILED_TO_MIGRATE;
			case 0:
			default:
				return NOT_MIGRATED;
		}
	}

	@Override
	public String toString() {
		return displayName;
	}
}
