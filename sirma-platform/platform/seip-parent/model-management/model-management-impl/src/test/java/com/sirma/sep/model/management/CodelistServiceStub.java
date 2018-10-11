package com.sirma.sep.model.management;

import com.sirma.itt.seip.domain.codelist.CodelistService;
import com.sirma.itt.seip.domain.codelist.model.CodeValue;

import java.util.HashMap;
import java.util.Map;

import static org.mockito.Matchers.anyInt;
import static org.mockito.Mockito.when;

/**
 * Stubs a {@link org.mockito.Mock} of {@link CodelistService}.
 *
 * @author Mihail Radkov
 */
class CodelistServiceStub {

	private Map<Integer, Map<String, CodeValue>> values;

	CodelistServiceStub(CodelistService codelistService) {
		values = new HashMap<>();
		when(codelistService.getCodeValues(anyInt())).thenAnswer(invocation -> {
			Integer codeListId = (Integer) invocation.getArguments()[0];
			return values.getOrDefault(codeListId, new HashMap<>());
		});
	}

	public CodelistServiceStub withValueForList(CodeValue value, Integer listId) {
		Map<String, CodeValue> listValues = values.computeIfAbsent(listId, l -> new HashMap<>());
		listValues.put(value.getIdentifier(), value);
		return this;
	}

}
