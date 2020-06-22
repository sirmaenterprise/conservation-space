package com.sirma.itt.emf.cls.persister;

import java.util.List;

import com.sirma.sep.cls.model.CodeList;
import com.sirma.sep.cls.model.CodeValue;
import com.sirma.sep.cls.parser.CodeListSheet;

/**
 * Provides a method for persisting a {@link CodeListSheet} object to a database.
 *
 * @author svetlozar.iliev
 */
public interface CodeListPersister {

	/**
	 * Validates and overrides any previously persisted code lists in the database with the ones provided in the code list sheet.
	 *
	 * @param sheet the sheet with code lists
	 * @throws com.sirma.itt.emf.cls.validator.exception.CodeValidatorException if some of the provided code lists are invalid
	 */
	void override(CodeListSheet sheet);

	/**
	 * Validates and persists the provided {@link CodeList} and it's {@link com.sirma.sep.cls.model.CodeValue}.
	 * <p>
	 * If the {@link CodeList} or some of its {@link com.sirma.sep.cls.model.CodeValue} exists in the database, they will be updated.
	 *
	 * @param codeList - the code list to persist
	 * @throws com.sirma.itt.emf.cls.validator.exception.CodeValidatorException if the provided code list is invalid
	 */
	void persist(CodeList codeList);

	/**
	 * Validates and persists multiple {@link CodeList} and their {@link com.sirma.sep.cls.model.CodeValue}.
	 * <p>
	 * If some {@link CodeList} or  {@link com.sirma.sep.cls.model.CodeValue} exists in the database, they will be updated.
	 *
	 * @param codeLists - the collection of code lists to persist
	 * @throws com.sirma.itt.emf.cls.validator.exception.CodeValidatorException if some of the provided code lists are invalid
	 */
	void persist(List<CodeList> codeLists);

	/**
	 * Validates and persists the provided {@link CodeValue}.
	 * <p>
	 * If the {@link CodeValue} exists in the database, it will be updated.
	 *
	 * @param codeValue - the code value to persist
	 * @throws com.sirma.itt.emf.cls.validator.exception.CodeValidatorException if the provided code value is invalid
	 */
	void persist(CodeValue codeValue);

	/**
	 * Delete any existing {@link CodeList} from the database along with their related {@link com.sirma.sep.cls.model.CodeValue}
	 */
	void delete();
}
