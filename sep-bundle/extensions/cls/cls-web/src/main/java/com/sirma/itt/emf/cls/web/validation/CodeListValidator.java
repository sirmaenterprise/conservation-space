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

	@Override
	public Map<String, String> validate(CodeList code, boolean update) {
		Map<String, String> errorsMap = new HashMap<String, String>();

		if (code.getValidFrom() == null) {
			errorsMap.put("validFrom", "This field is required.");
		}
		
		if (code.getValidTo() != null) {
			if (code.getValidTo().before(new Date())) {
				errorsMap.put("validTo", "Validity end date must be greater than current date.");
			} else if (code.getValidFrom() != null && code.getValidTo().compareTo(code.getValidFrom()) <= 0) {
				errorsMap.put("validTo", "Validity end date must be greater than validity start date.");
			}
		}

		if (!update) {
			if (code.getValue() == null || code.getValue().isEmpty()) {
				errorsMap.put("value", "This field is required.");
			} else if (!code.getValue().matches("^[a-zA-Z][a-zA-Z0-9_]*$")) {
				// Check against "^[a-zA-Z][a-zA-Z0-9_]*$"
				errorsMap.put("value", "Must start with a letter. No special characters allowed.");
			} else {
				// Check for duplicate ID
				CodeListSearchCriteria criteria = new CodeListSearchCriteria();
				criteria.setExcludeValues(true);
				criteria.setIds(Arrays.asList(code.getValue()));
				SearchResult codeLists = codeListService.getCodeLists(criteria);
				if (codeLists.getTotal() > 0) {
					errorsMap.put("value", "There is already a code list with the same ID.");
				}	
			}
		} else {
			// Check for existing ID
			CodeListSearchCriteria criteria = new CodeListSearchCriteria();
			criteria.setExcludeValues(true);
			criteria.setIds(Arrays.asList(code.getValue()));
			SearchResult codeLists = codeListService.getCodeLists(criteria);
			if (codeLists.getTotal() == 0) {
				errorsMap.put("value", "There is no code list with this ID.");
			}
		}

		return errorsMap;
	}
}
