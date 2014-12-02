package com.sirma.itt.emf.state;

/**
 * Marker interface for primary state distinguishing.
 * 
 * @author BBonev
 */
public interface PrimaryStateType {

	String INITIAL = "INIT";
	/** The opened state or in progress. */
	String IN_PROGRESS = "IN_PROGRESS";
	/** The canceled/stopped state. It's abnormal end state */
	String CANCELED = "STOPPED";
	/** The deleted. */
	String DELETED = "DELETED";
	/** The archived. */
	String ARCHIVED = "ARCHIVED";
	/** The on hold. */
	String ON_HOLD = "ON_HOLD";
	/** The completed. */
	String COMPLETED = "COMPLETED";
	/** The submitted. */
	String SUBMITTED = "SUBMITTED";
	/** The approved. */
	String APPROVED = "APPROVED";


	/**
	 * Gets the type of the current type
	 * 
	 * @return the type
	 */
	String getType();

}
