package com.sirma.itt.emf.cls.web.validation;

import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import javax.inject.Inject;

import com.sirma.itt.emf.cls.entity.CodeList;
import com.sirma.itt.emf.cls.entity.CodeValue;
import com.sirma.itt.emf.cls.retriever.CodeListSearchCriteria;
import com.sirma.itt.emf.cls.retriever.CodeValueSearchCriteria;
import com.sirma.itt.emf.cls.retriever.SearchResult;
import com.sirma.itt.emf.cls.service.CodeListService;

/**
 * Validates a {@link CodeValue} before persisting it.
 */
public class CodeValueValidator implements Validator<CodeValue> {
	
	@Inject
	private CodeListService codeListService;

	@Override
	public Map<String, String> validate(CodeValue code, boolean update) {
		Map<String, String> errorsMap = new HashMap<String, String>();
		
		if (code.getCodeListId() == null) {
			errorsMap.put("codeListId", "This field is required.");
		} else {
			CodeListSearchCriteria clCriteria = new CodeListSearchCriteria();
			clCriteria.setIds(Arrays.asList(code.getCodeListId()));
			SearchResult codeLists= codeListService.getCodeLists(clCriteria);
			if (codeLists.getTotal() == 0) {
				errorsMap.put("codeListId", "Code list with this ID does not exists.");
			} else {
				String masterCV = code.getMasterValue();
				// Check if masterCV belongs to masterCL
				if (masterCV != null && !masterCV.isEmpty()) {
					CodeList codeList = (CodeList) codeLists.getResults().get(0);
					String masterCL = codeList.getMasterValue();
				
					CodeValueSearchCriteria criteria = new CodeValueSearchCriteria();
					criteria.setCodeListId(masterCL);
					criteria.setIds(Arrays.asList(masterCV));
					SearchResult masterCodeValues = codeListService.getCodeValues(criteria);
					if (masterCodeValues.getTotal() == 0) {
						errorsMap.put("masterValue", "Code value with this ID does not belong to master code list.");
					}
				}
			}
		}
		
		if (code.getValidFrom() == null) {
			errorsMap.put("validFrom", "This field is required.");
		} else if (code.getValidFrom().before(new Date())) {
			errorsMap.put("validFrom", "Validity start date must be greater than current date.");
		}
		
		if (code.getValidTo() != null && code.getValidFrom() != null && code.getValidTo().before(code.getValidFrom())) {
			errorsMap.put("validTo", "Validity end date must be greater than validity start date.");
		}
		
		if (update) {
			CodeValueSearchCriteria criteria = new CodeValueSearchCriteria();
			criteria.setCodeListId(code.getCodeListId());
			criteria.setIds(Arrays.asList(code.getValue()));
			SearchResult codeValues = codeListService.getCodeValues(criteria);
			if (codeValues.getTotal() == 0) {
				errorsMap.put("value", "There is no value with this ID in this code list.");
			}	
		} else {
			if (code.getValue() == null || code.getValue().isEmpty()) {
				errorsMap.put("value", "This field is required.");
			} else {
				// Check for duplicate ID
				CodeValueSearchCriteria criteria = new CodeValueSearchCriteria();
				criteria.setCodeListId(code.getCodeListId());
				criteria.setIds(Arrays.asList(code.getValue()));
				SearchResult codeValues = codeListService.getCodeValues(criteria);
				if (codeValues.getTotal() > 0) {
					errorsMap.put("value", "There is already a code value with the same ID.");
				}	
			}
		}
		
		return errorsMap;
	}

}
