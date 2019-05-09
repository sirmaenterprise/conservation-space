package com.sirma.sep.model.management;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.Optional;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyList;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.when;

import com.sirma.sep.cls.CodeListService;
import com.sirma.sep.cls.model.CodeValue;

/**
 * Stubs a {@link org.mockito.Mock} of {@link CodeListService}.
 *
 * @author Mihail Radkov
 */
class CodelistServiceStub {

	private Map<String, Map<String, CodeValue>> values;

	CodelistServiceStub(CodeListService codelistService) {
		values = new HashMap<>();
		when(codelistService.getCodeValues(anyString())).thenAnswer(invocation -> {
			String codeListId = (String) invocation.getArguments()[0];
			return new LinkedList(values.getOrDefault(codeListId, new HashMap<>()).values());
		});
		when(codelistService.getCodeValue(anyString(), anyString())).thenAnswer(invocation -> {
			String codeListId = (String) invocation.getArguments()[0];
			String codeValueId = (String) invocation.getArguments()[1];
			Map<String, CodeValue> codeValues = values.getOrDefault(codeListId, new HashMap<>());
			return Optional.ofNullable(codeValues.get(codeValueId));
		});
	}

	public CodelistServiceStub withValueForList(CodeValue value) {
		Map<String, CodeValue> listValues = values.computeIfAbsent(value.getCodeListValue(), l -> new HashMap<>());
		listValues.put(value.getValue(), value);
		return this;
	}

}
