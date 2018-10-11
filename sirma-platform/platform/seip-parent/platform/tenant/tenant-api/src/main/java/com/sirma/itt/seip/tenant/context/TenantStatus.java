package com.sirma.itt.seip.tenant.context;

/**
 * The tenant status.
 * 
 * @author nvelkov
 */
public enum TenantStatus {
	ACTIVE(1), INACTIVE(2), DELETED(3);

	private final int value;

	public int getValue() {
		return value;
	}

	private TenantStatus(int value) {
		this.value = value;
	}

	/**
	 * Create a {@link TenantStatus} from the given integer.
	 * 
	 * @param status
	 *            the status int
	 * @return the created {@link TenantStatus}
	 */
	public static TenantStatus fromInteger(int status) {
		switch (status) {
			case 1:
				return ACTIVE;
			case 2:
				return INACTIVE;
			case 3:
				return DELETED;
			default:
				break;
		}
		return null;
	}
}