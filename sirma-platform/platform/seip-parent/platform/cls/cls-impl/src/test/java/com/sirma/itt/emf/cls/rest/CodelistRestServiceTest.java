package com.sirma.itt.emf.cls.rest;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.anyInt;

import java.io.Serializable;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

import com.sirma.itt.seip.domain.codelist.CodelistService;
import com.sirma.itt.seip.domain.codelist.model.CodeValue;

/**
 * Tests for codelist rest service.
 *
 * @author svelikov
 * @author Vilizar Tsonev
 */
public class CodelistRestServiceTest {

	@InjectMocks
	private CodelistRestService service;

	@Mock
	private CodelistService codelistService;

	/**
	 * Instantiates a new codelist rest service test.
	 */
	@Before
	public void beforeEach() {
		MockitoAnnotations.initMocks(this);
	}

	@Test
	public void should_Return_Null_When_No_CL_Provided() {
		List<CodeValueInfo> retrievedCodeValues = service.retrieveCodeValues(null, null, null, false, null, "en",
				"eng");
		assertTrue(retrievedCodeValues.isEmpty());
	}

	@Test
	public void should_Filter_CodeValues_By_Search_Term() {
		CodeValue engineeringDepartment = createCodevalue(100, "ENG", "en", "Engineering Department");
		CodeValue managementDepartment = createCodevalue(100, "MNG", "en", "Management department");
		withExistingCodeValues(engineeringDepartment, managementDepartment);

		List<CodeValueInfo> retrievedCodeValues = service.retrieveCodeValues(100, null, null, false, null, "en", "eng");

		Map<String, Serializable> descriptions = new HashMap<String, Serializable>(1);
		descriptions.put("en", "Engineering Department");
		CodeValueInfo engineeringVal = new CodeValueInfo("ENG", "Engineering Department", 100, descriptions);

		assertEquals(Arrays.asList(engineeringVal), retrievedCodeValues);
	}

	@Test
	public void should_Filter_CodeValues_By_Search_Term_With_More_Than_One_Matching() {
		CodeValue engineeringDepartment = createCodevalue(100, "ENG", "en", "Engineering Department");
		CodeValue engTestDepartment = createCodevalue(100, "ENGTST", "en", "Test containseng");
		CodeValue managementDepartment = createCodevalue(100, "MNG", "en", "Management department");
		CodeValue qaDepartment = createCodevalue(100, "QA", "en", "QA department");
		withExistingCodeValues(engineeringDepartment, engTestDepartment, managementDepartment, qaDepartment);

		List<CodeValueInfo> retrievedCodeValues = service.retrieveCodeValues(100, null, null, false, null, "en", "eng");

		Map<String, Serializable> descriptions1 = new HashMap<String, Serializable>(1);
		descriptions1.put("en", "Engineering Department");
		CodeValueInfo engineeringVal = new CodeValueInfo("ENG", "Engineering Department", 100, descriptions1);

		Map<String, Serializable> descriptions2 = new HashMap<String, Serializable>(1);
		descriptions2.put("en", "Test containseng");
		CodeValueInfo testVal = new CodeValueInfo("ENGTST", "Test containseng", 100, descriptions2);

		assertEquals(Arrays.asList(testVal, engineeringVal), retrievedCodeValues);
	}

	private static CodeValue createCodevalue(int codelist, String key, String language, String label) {
		CodeValue cv = new CodeValue();
		cv.setCodelist(codelist);
		cv.setValue(key);
		Map<String, Serializable> properties = new HashMap<String, Serializable>(1);
		properties.put(language, label);
		cv.setProperties(properties);
		return cv;
	}

	private void withExistingCodeValues(CodeValue... codeValues) {
		Map<String, CodeValue> codeValuesMap = new HashMap<>();
		for (CodeValue codeValue : codeValues) {
			codeValuesMap.put(codeValue.getValue(), codeValue);
		}

		Mockito.when(codelistService.getCodeValues(anyInt())).thenReturn(codeValuesMap);
	}
}
