package com.sirma.itt.emf.cls.event;

import java.util.List;

import com.sirma.itt.seip.annotation.Documentation;
import com.sirma.itt.seip.event.EmfEvent;
import com.sirma.sep.cls.model.CodeList;

/**
 * Event fired after uploading an excel file with code lists and values. This event is fired <b>after the transaction is
 * completed</b>.
 *
 * @author Mihail Radkov
 */
@Documentation("Event fired after uploading an excel file with code lists and values.")
public final class CodeListUploadEvent implements EmfEvent {

	private List<CodeList> codeLists;

	/**
	 * Class constructor. It takes:
	 *
	 * @param persistedEntities
	 *            the list of codelist entities that were persisted from the excel.
	 */
	public CodeListUploadEvent(List<CodeList> persistedEntities) {
		super();
		this.codeLists = persistedEntities;
	}

	public List<CodeList> getCodeLists() {
		return codeLists;
	}

}
