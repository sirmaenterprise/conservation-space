package com.sirma.itt.emf.cls.event;

import com.sirma.itt.emf.cls.entity.CodeValue;
import com.sirma.itt.emf.event.EmfEvent;
import com.sirma.itt.emf.util.Documentation;

/**
 * Event fired after code value persisting in the DB.
 * 
 * @author Mihail Radkov
 */
@Documentation("Event fired after code value persisting in the DB")
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
