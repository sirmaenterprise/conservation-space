package com.sirma.sep.cls;

import java.util.List;
import java.util.Optional;

import com.sirma.itt.emf.cls.persister.CodeListPersister;
import com.sirma.sep.cls.model.CodeList;
import com.sirma.sep.cls.model.CodeValue;

/**
 * Service for loading {@link CodeList} & {@link CodeValue}.
 * <p>
 * This service disallows modifications or the creation of new objects. For that use {@link CodeListPersister}.
 *
 * @author Mihail Radkov
 */
public interface CodeListService {
	
	/**
	 * Fetches a given {@link CodeList} in the application. For a provided code list number
	 * <p>
	 * This will not retrieve their {@link CodeValue}, for that use {@link #getCodeLists(string, boolean)}.
	 * <p>
	 * If the {@link CodeList} is not available in the application then empty optional is returned
	 * 
	 * @param codeList
	 *            - code list identifier represented as a string
	 * @return the requested code list
	 */
	Optional<CodeList> getCodeList(String codeList);

	/**
	 * Fetches a given {@link CodeList} in the application. For a provided code list number
	 * <p>
	 * This will retrieve their {@link CodeValue} if <code>true</code> is provided.
	 * <p>
	 * If the {@link CodeList} is not available in the application then empty optional is returned
	 * 
	 * @param codeList
	 *            - code list identifier represented as a string
	 * @param loadValues
	 *            - controls if the {@link CodeValue} for each {@link CodeList} will be loaded too
	 * @return the requested code list
	 */
	Optional<CodeList> getCodeList(String codeList, boolean loadValues);

	/**
	 * Fetches all available {@link CodeList} in the application.
	 * <p>
	 * This will not retrieve their {@link CodeValue}, for that use {@link #getCodeLists(boolean)}.
	 * <p>
	 * If no {@link CodeList} are available in the application then empty {@link List} will be returned.
	 *
	 * @return the available {@link CodeList}
	 */
	List<CodeList> getCodeLists();

	/**
	 * Fetches all available {@link CodeList} in the application.
	 * <p>
	 * This will retrieve their {@link CodeValue} if <code>true</code> is provided.
	 * <p>
	 * If no {@link CodeList} are available in the application then empty {@link List} will be returned.
	 *
	 * @param loadValues controls if the {@link CodeValue} for each {@link CodeList} will be loaded too
	 * @return the available {@link CodeList}
	 */
	List<CodeList> getCodeLists(boolean loadValues);

	/**
	 * Fetches all available {@link CodeValue} for specific parent {@link CodeList} value.
	 * <p>
	 * If there are no {@link CodeValue} for the specified {@link CodeList} value, then an empty {@link List} will be returned.
	 *
	 * @param codeListValue the value of the parent {@link CodeList}
	 * @return the available children {@link CodeValue} or empty {@link List} in case of missing values
	 */
	List<CodeValue> getCodeValues(String codeListValue);

	/**
	 * Tries to fetch a {@link CodeValue} corresponding to the provided {@link CodeList} and {@link CodeValue} identifiers.
	 *
	 * @param codeList identifier for the {@link CodeList} to which the requested {@link CodeValue} belongs
	 * @param codeValue identifier of the requested {@link CodeValue}
	 * @return {@link Optional} with the fetched {@link CodeValue} or {@link Optional#empty()} if no value corresponds
	 */
	Optional<CodeValue> getCodeValue(String codeList, String codeValue);

}
