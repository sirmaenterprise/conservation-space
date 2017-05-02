package com.sirma.itt.emf.cls.persister;

import java.util.Collection;

import com.sirma.itt.emf.cls.entity.CodeListSheet;

/**
 * Provides a method for persisting a {@link CodeListSheet} object to a database.
 *
 * @author Nikolay Velkov
 */
public interface XLSProcessor {

	/**
	 * Persist the given codelist sheet to a database. The sheet must have been validated beforehand.
	 *
	 * @param sheet
	 *            the sheet
	 * @throws PersisterException
	 *             the persister exception
	 */
	void persistSheet(CodeListSheet sheet) throws PersisterException;

	/**
	 * Delete any existing code lists from the database.
	 */
	void deleteCodeLists();

	/**
	 * Merge all {@link CodeListSheet} objects and return a {@link CodeListSheet} containing the merged results. The
	 * Merged {@link CodeListSheet} will contain all codelists from all sheets. If there are overlapping codelists,
	 * their codevalues wlll be merged.<br>
	 * <b>NOTE: There is no priority for overlapping codevalues. The codevalue of the last iterated codelist will
	 * override the others.</b>
	 *
	 * @param sheets
	 *            the {@link CodeListSheet} to be merged
	 * @return a {@link CodeListSheet} containing the merged codelists.
	 */
	CodeListSheet mergeSheets(Collection<CodeListSheet> sheets);
}
