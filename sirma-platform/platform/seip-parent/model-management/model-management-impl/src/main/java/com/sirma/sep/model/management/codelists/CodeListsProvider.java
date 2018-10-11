package com.sirma.sep.model.management.codelists;

import com.sirma.itt.seip.domain.codelist.CodelistService;
import com.sirma.itt.seip.domain.codelist.model.CodeValue;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.Map;

/**
 * Encapsulates the {@link CodelistService} from the model management functionality.
 *
 * @author Mihail Radkov
 */
@Singleton
public class CodeListsProvider {

	@Inject
	private CodelistService codelistService;

	/**
	 * Retrieves the values for the specified code list identifier.
	 *
	 * @param codelistId
	 * 		the code list identifier for which values belong to
	 * @return map with the code values
	 */
	public Map<String, CodeValue> getValues(Integer codelistId) {
		return codelistService.getCodeValues(codelistId);
	}
}
