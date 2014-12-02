package com.sirma.itt.emf.cls.event;

import com.sirma.itt.emf.cls.entity.CodeList;
import com.sirma.itt.emf.event.EmfEvent;
import com.sirma.itt.emf.util.Documentation;

/**
 * Event fired after code list persisting in DB.
 * 
 * @author Mihail Radkov
 */
@Documentation("Event fired after code list persisting in DB")
public final class CodeListPersistEvent implements EmfEvent {

	/** The persisted code list. */
	private final CodeList codeList;

	/**
	 * Class constructor. Takes the persisted code list.
	 * 
	 * @param codeList
	 *            the persisted code list
	 */
	public CodeListPersistEvent(CodeList codeList) {
		this.codeList = codeList;
	}

	/**
	 * Returns the persisted code list.
	 * 
	 * @return the persisted code list.
	 */
	public CodeList getCodeList() {
		return codeList;
	}

}
