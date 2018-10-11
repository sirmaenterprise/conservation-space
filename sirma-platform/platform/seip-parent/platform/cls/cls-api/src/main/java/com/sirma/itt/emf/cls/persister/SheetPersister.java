package com.sirma.itt.emf.cls.persister;

import com.sirma.sep.cls.parser.CodeListSheet;

/**
 * Provides a method for persisting a {@link CodeListSheet} object to a database.
 *
 * @author svetlozar.iliev
 */
public interface SheetPersister {

	/**
	 * Persist the given code list sheet to a database. The sheet must have been validated beforehand.
	 *
	 * @param sheet
	 *            the sheet
	 */
	void persist(CodeListSheet sheet);

	/**
	 * Delete any existing code lists from the database.
	 */
	void delete();
}
