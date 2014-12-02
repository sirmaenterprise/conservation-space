package com.sirma.cmf.web;


/**
 * Base table action backing bean.
 * 
 * @author svelikov
 */
public abstract class TableAction extends EntityAction {

	/** none|single|multiple|multipleKeyboardFree. */
	private String selectionMode = "single";

	/**
	 * Gets the selection mode.
	 * 
	 * @return the selectionMode
	 */
	public String getSelectionMode() {
		return selectionMode;
	}

	/**
	 * Sets the selection mode.
	 * 
	 * @param selectionMode
	 *            the selectionMode to set
	 */
	public void setSelectionMode(String selectionMode) {
		this.selectionMode = selectionMode;
	}

}
