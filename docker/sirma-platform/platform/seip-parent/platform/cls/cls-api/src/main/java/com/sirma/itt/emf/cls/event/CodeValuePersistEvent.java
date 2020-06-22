package com.sirma.itt.emf.cls.event;

import com.sirma.itt.seip.annotation.Documentation;
import com.sirma.itt.seip.event.EmfEvent;
import com.sirma.sep.cls.model.CodeValue;

/**
 * Event fired after code value persisting in the DB. Fired only for changes to single values done through the UI. For
 * changes done through the excel there is a separate event.
 *
 * @author Mihail Radkov
 */
@Documentation("Event fired after code value persisting in the DB, this is done only for single values. This means it will not be fired when uploading an excel sheet.")
public final class CodeValuePersistEvent implements EmfEvent {

	/** The persisted code value. */
	private final CodeValue codeValue;

	/**
	 * Class constructor. Takes the persisted code value.
	 *
	 * @param codeValue
	 *            the persisted code value
	 */
	public CodeValuePersistEvent(CodeValue codeValue) {
		this.codeValue = codeValue;
	}

	/**
	 * Returns the persisted code value.
	 *
	 * @return the persisted code value.
	 */
	public CodeValue getCodeValue() {
		return codeValue;
	}
}
