package com.sirma.itt.emf.cls.event;

import com.sirma.itt.emf.event.EmfEvent;
import com.sirma.itt.emf.util.Documentation;

/**
 * Event fired after uploading an excel file with code lists and values.
 * 
 * @author Mihail Radkov
 */
@Documentation("Event fired after uploading an excel file with code lists and values.")
public final class CodeListUploadEvent implements EmfEvent {

	/** The upload's result message. */
	private String message;

	/**
	 * Class constructor. It takes:
	 * 
	 * @param message
	 *            the upload's result message
	 */
	public CodeListUploadEvent(String message) {
		super();
		this.message = message;
	}

	/**
	 * Getter for the upload's result message.
	 * 
	 * @return the upload's result message
	 */
	public String getMessage() {
		return message;
	}

}
