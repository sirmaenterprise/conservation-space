package com.sirma.itt.emf.cls.persister;

import jxl.Sheet;

/**
 * Provides a method for persisting a {@link Sheet} object to a database.
 * 
 * @author Nikolay Velkov
 */
public interface XLSProcessor {

	/**
	 * Persist the given sheet to a database. The sheet must have been validated beforehand.
	 * 
	 * @param sheet
	 *            the sheet
	 * @throws PersisterException
	 *             the persister exception
	 */
	void persistSheet(Sheet sheet) throws PersisterException;
}
