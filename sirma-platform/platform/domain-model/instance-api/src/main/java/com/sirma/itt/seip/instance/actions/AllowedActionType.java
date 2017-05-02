package com.sirma.itt.seip.instance.actions;

import com.sirma.itt.seip.domain.security.ActionTypeConstants;

/**
 * Allowed actions enum.
 *
 * @author svelikov
 */
public enum AllowedActionType {

	// READ PERMISSIONS
	// OPEN_CASE("open_case"),//
	// OPEN_DOCUMENT("open_document"),//
	DOWNLOAD(ActionTypeConstants.DOWNLOAD), //
	PRINT(ActionTypeConstants.PRINT), //
	// There is no action for this type.
	DOCUMENT_HISTORY_PREVIEW(ActionTypeConstants.HISTORY_PREVIEW), //
	// READ_COMMENT("forum_read_comment"),//

	// CREATE PERMISSION
	DOCUMENT_UPLOAD(ActionTypeConstants.UPLOAD), //
	DOCUMENT_ATTACH(ActionTypeConstants.ATTACH_DOCUMENT), //
	ATTACH_TO(ActionTypeConstants.ATTACH_TO), //
	DOCUMENT_UPLOAD_NEW_VERSION(ActionTypeConstants.UPLOAD_NEW_VERSION), //
	DOCUMENT_MOVE_SAME_CASE(ActionTypeConstants.MOVE_SAME_CASE), //
	DOCUMENT_MOVE(ActionTypeConstants.MOVE), //
	LINK(ActionTypeConstants.LINK), //
	COPY_CONTENT(ActionTypeConstants.COPY_CONTENT), //
	PUBLISH(ActionTypeConstants.PUBLISH), //

	// EDIT PERMISSION
	REASSIGN_TASK(ActionTypeConstants.REASSIGN_TASK), //
	STANDALONE_EDIT_TASK(ActionTypeConstants.EDIT_DETAILS), //
	EDIT_TASK(ActionTypeConstants.EDIT_DETAILS), //
	TASK_START_PROGRESS(ActionTypeConstants.RESTART), //
	TASK_HOLD(ActionTypeConstants.SUSPEND), //
	TASK_CLAIM(ActionTypeConstants.CLAIM), //
	TASK_RELEASE(ActionTypeConstants.RELEASE), //
	CASE_CLOSE(ActionTypeConstants.COMPLETE), //
	EDIT_STRUCTURED_DOCUMENT(ActionTypeConstants.EDIT_STRUCTURED_DOCUMENT), //
	DOCUMENT_EDIT_PROPERTIES(ActionTypeConstants.EDIT_DETAILS), //
	CASE_EDIT(ActionTypeConstants.EDIT_DETAILS), //
	// There is no action for this type.
	DOCUMENT_VERSION_REVERT(ActionTypeConstants.REVERT), //
	SIGN_DOCUMENT(ActionTypeConstants.SIGN), //

	// DELETE PERMISSIONS
	CASE_DELETE(ActionTypeConstants.DELETE), //
	DOCUMENT_DELETE(ActionTypeConstants.DELETE), //

	// LOCK PERMISSIONS
	LOCK(ActionTypeConstants.LOCK), //
	UNLOCK(ActionTypeConstants.UNLOCK), //
	DOCUMENT_EDIT_OFFLINE(ActionTypeConstants.EDIT_OFFLINE), //
	DOCUMENT_EDIT_ONLINE(ActionTypeConstants.EDIT_ONLINE), //
	CANCEL_EDIT_OFFLINE(ActionTypeConstants.STOP_EDIT), //
	STOP(ActionTypeConstants.STOP), //

	UPLOAD(ActionTypeConstants.UPLOAD), //
	CLONE(ActionTypeConstants.CLONE), //
	EXPORT(ActionTypeConstants.EXPORT); //

	/**
	 * The action type.
	 */
	private final String type;

	/**
	 * Constructor.
	 *
	 * @param type
	 *            The action type.
	 */
	private AllowedActionType(String type) {
		this.type = type;
	}

	/**
	 * Finds the {@link AllowedActionType} in this enum if exists and returns it.
	 *
	 * @param type
	 *            The action type to search for.
	 * @return {@link AllowedActionType} or null if no one is found.
	 */
	public static AllowedActionType getActionType(String type) {
		AllowedActionType[] types = values();
		for (AllowedActionType action : types) {
			if (action.type.equals(type)) {
				return action;
			}
		}
		return null;
	}

	/**
	 * Getter method for type.
	 *
	 * @return the type
	 */
	public String getType() {
		return type;
	}
}
