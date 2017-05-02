package com.sirma.itt.emf.cls.web.validation;

import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import javax.inject.Inject;

import com.sirma.itt.emf.cls.entity.CodeList;
import com.sirma.itt.emf.cls.retriever.CodeListSearchCriteria;
import com.sirma.itt.emf.cls.retriever.SearchResult;
import com.sirma.itt.emf.cls.service.CodeListService;

/**
 * Validates a {@link CodeList} before persisting it.
 */
public class CodeListValidator implements Validator<CodeList> {

	@Inject
	private CodeListService codeListService;

	private static final String CODE_VALUE = "value";
	private static final String VALID_TO = "validTo";

	@Override
	public Map<String, String> validate(CodeList code, boolean update) {
		Map<String, String> errorsMap = new HashMap<String, String>();

		if (code.getValidFrom() == null) {
			errorsMap.put("validFrom", "This field is required.");
		}

		if (code.getValidTo() != null) {
			if (code.getValidTo().before(new Date())) {
				errorsMap.put(VALID_TO, "Validity end date must be greater than current date.");
			} else if (code.getValidFrom() != null && code.getValidTo().compareTo(code.getValidFrom()) <= 0) {
				errorsMap.put(VALID_TO, "Validity end date must be greater than validity start date.");
			}
		}
		if (!update) {
			validateNewCodeList(code, errorsMap);
		} else {
			// Check for existing ID
			CodeListSearchCriteria criteria = new CodeListSearchCriteria();
			criteria.setExcludeValues(true);
			criteria.setIds(Arrays.asList(code.getValue()));
			SearchResult codeLists = codeListService.getCodeLists(criteria);
			if (codeLists.getTotal() == 0) {
				errorsMap.put(CODE_VALUE, "There is no code list with this ID.");
			}
		}

		return errorsMap;
	}

	/**
	 * Validates the ID of the new code list and if there is already a code list with the same ID
	 *
	 * @param code
	 *            is the code list
	 * @param errorsMap
	 *            is the errors map
	 */
	private void validateNewCodeList(CodeList code, Map<String, String> errorsMap) {
		if (code.getValue() == null || code.getValue().isEmpty()) {
			errorsMap.put(CODE_VALUE, "This field is required.");
		} else if (!code.getValue().matches("^[0-9]*$")) {
			// Check against "^[a-zA-Z][a-zA-Z0-9_]*$"
			errorsMap.put(CODE_VALUE, "Must be an integer value. No special characters allowed.");
		} else {
			// Check for duplicate ID
			CodeListSearchCriteria criteria = new CodeListSearchCriteria();
			criteria.setExcludeValues(true);
			criteria.setIds(Arrays.asList(code.getValue()));
			SearchResult codeLists = codeListService.getCodeLists(criteria);
			if (codeLists.getTotal() > 0) {
				errorsMap.put(CODE_VALUE, "There is already a code list with the same ID.");
			}
		}
	}
}
