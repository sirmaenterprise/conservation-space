package com.sirma.itt.emf.cls.persister;

import com.sirma.itt.emf.cls.entity.CodeList;
import com.sirma.itt.emf.cls.entity.CodeListSheet;
import com.sirma.itt.emf.cls.entity.CodeValue;

import jxl.Sheet;

/**
 * Parses an excel sheet and creates {@link CodeList} and {@link CodeValue} objects from it's rows.
 *
 * @author nvelkov
 */
public interface SheetParser {

	/**
	 * Parses a sheet and converts it's rows into {@link CodeList} and {@link CodeValue} objects.
	 *
	 * @param sheet
	 *            the sheet to be parsed
	 * @return the parsed {@link CodeList} and {@link CodeValue} objects
	 * @throws PersisterException
	 *             if there is malformed sheet data
	 */
	CodeListSheet parseXLS(Sheet sheet) throws PersisterException;
}
