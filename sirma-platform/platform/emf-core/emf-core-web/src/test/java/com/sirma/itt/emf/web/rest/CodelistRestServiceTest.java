package com.sirma.itt.emf.web.rest;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

import org.json.JSONObject;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.testng.Assert;
import org.testng.annotations.Test;

import com.sirma.itt.seip.configuration.SystemConfiguration;
import com.sirma.itt.seip.domain.codelist.CodelistService;
import com.sirma.itt.seip.domain.codelist.model.CodeValue;
import com.sirma.itt.seip.json.JsonUtil;

import net.javacrumbs.jsonunit.JsonAssert;

/**
 * Tests for codelist rest service.
 *
 * @author svelikov
 */
@Test
public class CodelistRestServiceTest {

	private static final String CL_PROPERTY = "extra1";
	private static final String LABEL = "Engineering department";
	private static final String LANGUAGE = "en";
	private static final String KEY = "ENG";
	private static final int CODELIST = 100;

	@InjectMocks
	private CodelistRestService service;

	@Mock
	private CodelistService codelistService;

	@Mock
	SystemConfiguration systemConfiguration;

	/**
	 * Instantiates a new codelist rest service test.
	 */
	public CodelistRestServiceTest() {
		service = new CodelistRestService() {
			@Override
			protected String getlanguage(String language) {
				return language;
			}
		};
		MockitoAnnotations.initMocks(this);
	}

	/**
	 * Code value label should be null when arguments are missing.
	 */
	public void codeValueLabelShouldBeNullWhenArgumentsAreMissing() {
		String codeValueLabel = service.getCodeValueLabel(null, null, null);
		Assert.assertNull(codeValueLabel);
	}

	/**
	 * Label should be null when not found.
	 */
	public void labelShouldBeNullWhenNotFound() {
		CodeValue cv = createCodevalue(CODELIST, KEY, LANGUAGE, LABEL);
		Mockito.when(codelistService.getCodeValue(CODELIST, KEY)).thenReturn(cv);
		String codeValueLabel = service.getCodeValueLabel(CODELIST, KEY, "bg");
		Assert.assertNull(codeValueLabel);
	}

	/**
	 * Label should be retrieved according to properties.
	 */
	public void labelShouldBeRetrievedAccordingToProperties() {
		CodeValue cv = createCodevalue(CODELIST, KEY, LANGUAGE, LABEL);
		Mockito.when(codelistService.getCodeValue(CODELIST, KEY)).thenReturn(cv);
		String codeValueLabel = service.getCodeValueLabel(CODELIST, KEY, LANGUAGE);
		Assert.assertEquals(codeValueLabel, LABEL);
	}

	/**
	 * Codevalue should be null when parameters are missing.
	 */
	public void codevalueShouldBeNullWhenParametersAreMissing() {
		String codevalue = service.getCodevalue(null, null, null);
		Assert.assertNull(codevalue);
	}

	/**
	 * Codevalue should be returned in json format when parameters are present.
	 */
	public void codevalueShouldBeReturnedInJsonFormatWhenParametersArePresent() {
		CodeValue cv = createCodevalue(CODELIST, KEY, LANGUAGE, LABEL);
		Mockito.when(codelistService.getCodeValue(CODELIST, KEY)).thenReturn(cv);
		String codevalue = service.getCodevalue(CODELIST, KEY, LANGUAGE);
		JSONObject expected = JsonUtil.createObjectFromString(
				"{\"locale\":\"en\",\"codelist\":100,\"localizedDescription\":\"Engineering department\",\"value\":\"ENG\",\"descriptions\":{\"en\":\"Engineering department\"}}");
		JsonAssert.assertJsonEquals(expected.toString(), codevalue);
	}

	/**
	 * Retrieve code values returns null when codelist argument is missing.
	 */
	public void retrieveCodeValuesReturnsNullWhenCodelistArgumentIsMissing() {
		String codeValues = service.retrieveCodeValues(null, null, null, true, null, false, null, null);
		Assert.assertNull(codeValues);
	}

	/**
	 * Retrieve code values returns empty json array when no codevalues are found by service.
	 */
	public void retrieveCodeValuesReturnsEmptyJsonArrayWhenNoCodevaluesAreFoundByService() {
		String codeValues = service.retrieveCodeValues(CODELIST, null, null, true, null, false, null, null);
		Assert.assertEquals(codeValues, "[]");
	}

	/**
	 * Retrieve code values returns empty json array when argument is missing and no codevalues are found by service.
	 */
	public void retrieveCodeValuesReturnsEmptyJsonArrayWhenArgumentIsMissingAndNoCodevaluesAreFoundByService() {
		String codeValues = service.retrieveCodeValues(CODELIST, KEY, null, true, null, false, null, null);
		Assert.assertEquals(codeValues, "[]");
	}

	/**
	 * Retrieve code values returns empty object if mapped is true and service didnt find values.
	 */
	public void retrieveCodeValuesReturnsEmptyObjectIfMappedIsTrueAndServiceDidntFindValues() {
		String codeValues = service.retrieveCodeValues(CODELIST, KEY, CL_PROPERTY, true, null, true, null, null);
		Assert.assertEquals(codeValues, "{}");
	}

	/**
	 * Retrieve code values returns mapped codevalues.
	 */
	public void retrieveCodeValuesReturnsMappedCodevalues() {
		Map<String, CodeValue> codevalues = createCodevaluesMap(KEY, createCodevalue(CODELIST, KEY, LANGUAGE, LABEL));
		Mockito.when(codelistService.filterCodeValues(CODELIST, true, CL_PROPERTY, KEY)).thenReturn(codevalues);
		String codeValues = service.retrieveCodeValues(CODELIST, KEY, CL_PROPERTY, true, new String[] {}, true, null,
				null);
		Assert.assertEquals(codeValues,
				"{\"ENG\":{\"codelist\":100,\"value\":\"ENG\",\"descriptions\":{\"en\":\"Engineering department\"}}}");
	}

	/**
	 * Retrieve code values returns mapped codevalues that are filtered by custom filters.
	 */
	public void retrieveCodeValuesReturnsMappedCodevaluesThatAreFilteredByCustomFilters() {
		String qa = "QA";
		String[] customFilters = new String[] { "customFilter1", "customFilter2" };
		CodeValue engCodevalue = createCodevalue(CODELIST, KEY, LANGUAGE, LABEL);
		CodeValue qaCodevalue = createCodevalue(CODELIST, qa, LANGUAGE, "Quality assurance");
		Map<String, CodeValue> codevalues = createCodevaluesMap(KEY, engCodevalue);
		Map<String, CodeValue> filteredCodevalues = createCodevaluesMap(qa, qaCodevalue);
		filteredCodevalues.put(KEY, engCodevalue);

		Mockito.when(codelistService.filterCodeValues(CODELIST, true, CL_PROPERTY, KEY)).thenReturn(codevalues);
		Mockito.when(codelistService.getFilteredCodeValues(CODELIST, customFilters)).thenReturn(filteredCodevalues);

		String codeValues = service.retrieveCodeValues(CODELIST, KEY, CL_PROPERTY, true, customFilters, true, null,
				null);
		Assert.assertEquals(codeValues,
				"{\"ENG\":{\"codelist\":100,\"value\":\"ENG\",\"descriptions\":{\"en\":\"Engineering department\"}}}");
	}

	/**
	 * Verify the codevalue is converted to json.
	 */
	public void verifyTheCodevalueIsConvertedToJson() {
		Map<String, CodeValue> map = createCodevaluesMap(KEY, createCodevalue(CODELIST, KEY, LANGUAGE, LABEL));
		JSONObject codevalueAsJson = service.codevalueAsJson("en", map.entrySet().iterator().next());
		JSONObject expected = JsonUtil.createObjectFromString(
				"{\"ln\":\"en\",\"codelist\":100,\"value\":\"ENG\",\"label\":\"Engineering department\",\"descriptions\":{\"en\":\"Engineering department\"}}");
		JsonAssert.assertJsonEquals(expected.toString(), codevalueAsJson.toString());
	}

	/**
	 * Search by codelist value by query received from the client input.
	 */
	@SuppressWarnings("boxing")
	public void searchForCodelistValueByQueryTest() {
		CodeValue engineeringDepartment = createCodevalue(CODELIST, KEY, LANGUAGE, LABEL);
		Map<String, CodeValue> codevalues = createCodevaluesMap(KEY, engineeringDepartment);

		Mockito.when(codelistService.getCodeValues(100)).thenReturn(codevalues);
		String retrieveCodeValues = service.retrieveCodeValues(CODELIST, null, null, false, null, false, LANGUAGE,
				"eng");
		JSONObject received = JsonUtil.createObjectFromString(retrieveCodeValues);
		JSONObject expected = JsonUtil.createObjectFromString(
				"[{\"ln\":\"en\",\"codelist\":100,\"label\":\"Engineering department\",\"value\":\"ENG\",\"descriptions\":{\"en\":\"Engineering department\"}}]");
		JsonAssert.assertJsonEquals(expected, received);
		retrieveCodeValues = service.retrieveCodeValues(CODELIST, null, null, false, null, false, LANGUAGE, "Quality");
		Assert.assertEquals(retrieveCodeValues, "[]");

	}

	/**
	 * Creates the codevalue.
	 *
	 * @param codelist
	 *            the codelist
	 * @param key
	 *            the key
	 * @param language
	 *            the language
	 * @param label
	 *            the label
	 * @return the code value
	 */
	private CodeValue createCodevalue(int codelist, String key, String language, String label) {
		CodeValue cv = new CodeValue();
		cv.setCodelist(codelist);
		cv.setValue(key);
		Map<String, Serializable> properties = new HashMap<String, Serializable>(1);
		properties.put(language, label);
		cv.setProperties(properties);
		return cv;
	}

	/**
	 * Creates the codevalues map.
	 *
	 * @param key
	 *            the key
	 * @param cv
	 *            the cv
	 * @return the map
	 */
	private Map<String, CodeValue> createCodevaluesMap(String key, CodeValue cv) {
		Map<String, CodeValue> codevalues = new HashMap<String, CodeValue>(1);
		codevalues.put(key, cv);
		return codevalues;
	}
}
