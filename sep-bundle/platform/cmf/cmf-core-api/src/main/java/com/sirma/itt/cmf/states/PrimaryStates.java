package com.sirma.itt.cmf.states;

import com.sirma.itt.emf.state.PrimaryStateType;
import com.sirma.itt.emf.util.EqualsHelper;

/**
 * Default implementation for the supported primary states
 * 
 * @author BBonev
 */
public enum PrimaryStates implements PrimaryStateType {

	/** The initial state of the object. Equals to no state. */
	INITIAL(PrimaryStateType.INITIAL),
	/** The opened state or in progress. */
	OPENED(PrimaryStateType.IN_PROGRESS),
	/** The canceled state. It's abnormal end state */
	CANCELED(PrimaryStateType.CANCELED),
	/** The deleted. */
	DELETED(PrimaryStateType.DELETED),
	/** The archived. */
	ARCHIVED(PrimaryStateType.ARCHIVED),
	/** The on hold. */
	ON_HOLD(PrimaryStateType.ON_HOLD),
	/** The completed. */
	COMPLETED(PrimaryStateType.COMPLETED),
	/** The submitted. */
	SUBMITTED(PrimaryStateType.SUBMITTED),
	/** The approved. */
	APPROVED(PrimaryStateType.APPROVED);

	/** The type. */
	private String type;

	/**
	 * Instantiates a new CMF primary states.
	 * 
	 * @param type
	 *            the type
	 */
	private PrimaryStates(String type) {
		this.type = type;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public String getType() {
		return type;
	}

	/**
	 * Gets the state type by id.
	 * 
	 * @param id
	 *            the id
	 * @return the state type by id
	 */
	public static PrimaryStateType getStateTypeById(String id) {
		if (EqualsHelper.nullSafeEquals(id, PrimaryStateType.INITIAL)) {
			return INITIAL;
		} else if (EqualsHelper.nullSafeEquals(id, PrimaryStateType.IN_PROGRESS)) {
			return OPENED;
		} else if (EqualsHelper.nullSafeEquals(id, PrimaryStateType.CANCELED)) {
			return CANCELED;
		} else if (EqualsHelper.nullSafeEquals(id, PrimaryStateType.DELETED)) {
			return DELETED;
		} else if (EqualsHelper.nullSafeEquals(id, PrimaryStateType.ARCHIVED)) {
			return ARCHIVED;
		} else if (EqualsHelper.nullSafeEquals(id, PrimaryStateType.ON_HOLD)) {
			return ON_HOLD;
		} else if (EqualsHelper.nullSafeEquals(id, PrimaryStateType.COMPLETED)) {
			return COMPLETED;
		}
		return null;
	}

}
