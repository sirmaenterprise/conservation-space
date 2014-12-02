package com.sirma.itt.pm.schedule.security;

import com.sirma.itt.emf.security.model.Action;

/**
 * Enumeration of schedule actions.
 *
 * @author BBonev
 */
public enum ScheduleActions implements Action {

	/** The open. */
	OPEN(ScheduleActionConstants.OPEN),
	/** The edit details. */
	EDIT_DETAILS(ScheduleActionConstants.EDIT_DETAILS),
	/** The approve. */
	APPROVE(ScheduleActionConstants.APPROVE),
	/** The stop. */
	STOP(ScheduleActionConstants.STOP),
	/** The delete. */
	DELETE(ScheduleActionConstants.DELETE),
	/** The indent. */
	INDENT(ScheduleActionConstants.INDENT),
	/** The outdent. */
	OUTDENT(ScheduleActionConstants.OUTDENT),
	/** The change task color. */
	CHANGE_TASK_COLOR(ScheduleActionConstants.CHANGE_TASK_COLOR),
	/** The edit left label. */
	EDIT_LEFT_LABEL(ScheduleActionConstants.EDIT_LEFT_LABEL),
	/** The edit right label. */
	EDIT_RIGHT_LABEL(ScheduleActionConstants.EDIT_RIGHT_LABEL),
	/** The add task menu. */
	ADD_TASK_MENU(ScheduleActionConstants.ADD_TASK_MENU),
	/** The add task above. */
	ADD_TASK_ABOVE(ScheduleActionConstants.ADD_TASK_ABOVE),
	/** The add task below. */
	ADD_TASK_BELOW(ScheduleActionConstants.ADD_TASK_BELOW),
	/** The add milestone. */
	ADD_MILESTONE(ScheduleActionConstants.ADD_MILESTONE),
	/** The add child. */
	ADD_CHILD(ScheduleActionConstants.ADD_CHILD),
	/** The add successor. */
	ADD_SUCCESSOR(ScheduleActionConstants.ADD_SUCCESSOR),
	/** The add predecessor. */
	ADD_PREDECESSOR(ScheduleActionConstants.ADD_PREDECESSOR),
	/** The delete dependency menu. */
	DELETE_DEPENDENCY_MENU(ScheduleActionConstants.DELETE_DEPENDENCY_MENU);

	/**
	 * Instantiates a new schedule actions.
	 *
	 * @param id
	 *            the id
	 */
	private ScheduleActions(String id) {
		this.identifier = id;
	}

	/** The identifier. */
	private final String identifier;

	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean isSealed() {
		return true;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void seal() {

	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public String getIdentifier() {
		return identifier;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void setIdentifier(String identifier) {

	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public String getActionId() {
		return getIdentifier();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public String getLabel() {
		return "schedule." + identifier + ".btn.label";
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean isDisabled() {
		return false;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public String getDisabledReason() {
		return null;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public String getConfirmationMessage() {
		return "schedule." + identifier + ".confirm";
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public String getIconImagePath() {
		return null;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public String getOnclick() {
		return null;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean isImmediateAction() {
		return false;
	}
}
