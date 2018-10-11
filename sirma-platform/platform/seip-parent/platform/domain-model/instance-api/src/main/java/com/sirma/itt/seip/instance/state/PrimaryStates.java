package com.sirma.itt.seip.instance.state;

import org.apache.commons.lang3.StringUtils;

import com.sirma.itt.seip.util.EqualsHelper;

/**
 * Marker interface for primary state distinguishing.
 *
 * @author BBonev
 */
public interface PrimaryStates {

	String INITIAL_KEY = "INIT";
	/** The opened state or in progress. */
	String IN_PROGRESS_KEY = "IN_PROGRESS";
	/** The canceled/stopped state. It's abnormal end state */
	String CANCELED_KEY = "STOPPED";
	/** The deleted. */
	String DELETED_KEY = "DELETED";
	/** The archived. */
	String ARCHIVED_KEY = "ARCHIVED";
	/** The on hold. */
	String ON_HOLD_KEY = "ON_HOLD";
	/** The completed. */
	String COMPLETED_KEY = "COMPLETED";
	/** The submitted. */
	String SUBMITTED_KEY = "SUBMITTED";
	/** The approved. */
	String APPROVED_KEY = "APPROVED";
	/** The obsolete. */
	String OBSOLETE_KEY = "OBSOLETE";
	/** The rejected. */
	String REJECTED_KEY = "REJECTED";
	String OPENED_KEY = "OPENED";

	String ACTIVE_KEY = "ACTIVE";

	PrimaryStates INITIAL = from(INITIAL_KEY);

	PrimaryStates IN_PROGRESS = from(IN_PROGRESS_KEY);
	PrimaryStates STOPPED = from(CANCELED_KEY);
	PrimaryStates DELETED = from(DELETED_KEY);
	PrimaryStates ARCHIVED = from(ARCHIVED_KEY);
	PrimaryStates ON_HOLD = from(ON_HOLD_KEY);
	PrimaryStates COMPLETED = from(COMPLETED_KEY);
	PrimaryStates SUBMITTED = from(SUBMITTED_KEY);
	PrimaryStates APPROVED = from(APPROVED_KEY);

	PrimaryStates OBSOLETE = from(OBSOLETE_KEY);
	PrimaryStates REJECTED = from(REJECTED_KEY);
	PrimaryStates OPENED = from(OPENED_KEY);

	String ACTIVE_STATES_CONFIG = IN_PROGRESS_KEY + "," + ON_HOLD_KEY + "," + SUBMITTED_KEY + "," + APPROVED_KEY + ","
			+ OBSOLETE_KEY + "," + REJECTED_KEY + "," + OPENED_KEY;

	/**
	 * Gets the type of the current type
	 *
	 * @return the type
	 */
	String getType();

	/**
	 * Instantiate a {@link PrimaryStates} using the default implementation from the given state id.
	 *
	 * @param state
	 *            the state
	 * @return the primary state or {@link #INITIAL} state if the argument is <code>null</code> or empty.
	 */
	static PrimaryStates from(String state) {
		if (StringUtils.isBlank(state)) {
			return INITIAL;
		}
		return new Impl(state.toUpperCase());
	}

	/**
	 * Implementation for the primary state
	 */
	class Impl implements PrimaryStates {

		private final String type;

		/**
		 * Instantiates a new state implementation.
		 *
		 * @param key
		 *            the key
		 */
		public Impl(String key) {
			type = key;
		}

		@Override
		public String getType() {
			return type;
		}

		@Override
		public boolean equals(Object obj) {
			if (this == obj) {
				return true;
			}
			if (!(obj instanceof PrimaryStates)) {
				return false;
			}
			PrimaryStates other = (PrimaryStates) obj;
			return EqualsHelper.nullSafeEquals(type, other.getType());
		}

		@Override
		public int hashCode() {
			int result = 1;
			result = 31 * result + (type == null ? 0 : type.hashCode());
			return result;
		}

	}

}
