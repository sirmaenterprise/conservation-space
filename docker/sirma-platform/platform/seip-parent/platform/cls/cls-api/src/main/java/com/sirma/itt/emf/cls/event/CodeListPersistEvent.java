package com.sirma.itt.emf.cls.event;

import com.sirma.itt.seip.annotation.Documentation;
import com.sirma.itt.seip.event.EmfEvent;
import com.sirma.sep.cls.model.CodeList;

/**
 * Event fired after code list persisting in DB. Fired only for changes to single values done through the UI. For
 * changes done through the excel there is a separate event.
 *
 * @author Mihail Radkov
 */
@Documentation("Event fired after code list persisting in DB, this is done only for single values. This means it will not be fired when uploading an excel sheet.")
public final class CodeListPersistEvent implements EmfEvent {

	/** Object that holds the codelist data */
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

	public CodeList getCodeList() {
		return codeList;
	}

}
