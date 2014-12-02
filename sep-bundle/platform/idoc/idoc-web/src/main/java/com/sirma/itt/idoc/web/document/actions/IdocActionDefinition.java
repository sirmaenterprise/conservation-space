package com.sirma.itt.idoc.web.document.actions;

/**
 * Holds constant identifier values for idoc specific actions.
 * 
 * @author yasko
 */
public enum IdocActionDefinition {

	/**
	 * 
	 */
	DOCUMENT_SAVE("document_idoc_save", "saveDocument()", "/emf/images/icon_saveTask.png"),

	/**
	 * 
	 */
	DOCUMENT_EDIT("document_idoc_edit", "editDocument()",
			"/emf/images/icon_document_editInline.png"),

	/**
	 * 
	 */
	DOCUMENT_CANCEL_CHANGES("document_idoc_cancel_changes", "cancelEditDocument()", null),

	/**
	 * 
	 */
	DOCUMENT_DELETE("document_idoc_delete", "deleteDocument()",
			"/emf/images/icon_document_delete.png"),

	/**
	 * 
	 */
	DOCUMENT_DOWNLOAD("document_idoc_download", "downloadDocument()",
			"/emf/images/icon_document_download.png"),

	/**
	 * 
	 */
	DOCUMENT_CLONE("document_idoc_clone", "iDocClonePickerInit()",
			"/emf/images/icon_document_delete.png"),

	/**
	 * 
	 */
	DOCUMENT_PRINT("document_idoc_print", "printDocument()", "/emf/images/print.png"),

	/**
	 * 
	 */
	DOCUMENT_EXPORT("document_idoc_export", "exportDocument()", "/emf/images/export.png"),

	/**
	 * 
	 */
	SAVE_AS_TEMPLATE("document_idoc_save_as_template", "saveAsTemplate()",
			"/emf/images/icon_document_delete.png"),

	/**
	 * 
	 */
	DOCUMENT_REVERT_TO_VERSION("document_idoc_revert_to_revision", "revertToVersion()",
			"/emf/images/icon_revert.png"),

	/**
	 * 
	 */
	DOCUMENT_CREATE("document_idoc_create", "createSubDocument()", null),

	/** 
	 * 
	 */
	DOCUMENT_UPLOAD("document_idoc_upload", "upload()", null);

	private String id;
	private String onClick;
	private String icon;

	/**
	 * Constructor.
	 * 
	 * @param id
	 *            String value.
	 * @param onClick
	 *            onclick action.
	 * @param icon
	 *            path to the action icon image.
	 */
	private IdocActionDefinition(String id, String onClick, String icon) {
		this.id = id;
		this.onClick = onClick;
		this.icon = icon;
	}

	/**
	 * Getter for the action id.
	 * 
	 * @return action id.
	 */
	public String getId() {
		return id;
	}

	/**
	 * Getter method for onClick.
	 * 
	 * @return the onClick
	 */
	public String getOnClick() {
		return onClick;
	}

	/**
	 * Getter method for icon.
	 * 
	 * @return the icon
	 */
	public String getIcon() {
		return icon;
	}

}
