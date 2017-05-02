package com.sirma.itt.emf.cls.web.validation;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.Date;
import java.util.Map;

import javax.inject.Inject;

import org.easymock.EasyMock;
import org.junit.Rule;
import org.junit.Test;

import com.sirma.itt.emf.cls.entity.CodeValue;
import com.sirma.itt.emf.cls.retriever.CodeListSearchCriteria;
import com.sirma.itt.emf.cls.retriever.CodeValueSearchCriteria;
import com.sirma.itt.emf.cls.retriever.SearchResult;
import com.sirma.itt.emf.cls.service.CodeListService;

import de.akquinet.jbosscc.needle.annotation.ObjectUnderTest;
import de.akquinet.jbosscc.needle.junit.NeedleRule;

/**
 * Tests server side validation for creating and updating {@link CodeValue}.
 */
public class CodeValueValidatorTest {

	@Rule
	public NeedleRule needleRule = new NeedleRule();

	@Inject
	private CodeListService codeListService;

	@ObjectUnderTest(implementation = CodeValueValidator.class)
	private Validator<CodeValue> codeValueValidator;

	/**
	 * Test for validating of create code value with missing data for some fields.
	 */
	@Test
	public void testCreateCodeValueMissingFields() {
		CodeValue codeValue = new CodeValue();

		Map<String, String> validationResult = codeValueValidator.validate(codeValue, false);

		assertTrue(!validationResult.isEmpty());
		assertEquals(validationResult.size(), 3);
		assertTrue(validationResult.get("codeListId") != null
				&& validationResult.get("codeListId").equals("This field is required."));
		assertTrue(validationResult.get("value") != null
				&& validationResult.get("value").equals("This field is required."));
		assertTrue(validationResult.get("validFrom") != null
				&& validationResult.get("validFrom").equals("This field is required."));
	}

	/**
	 * Test for validating of create code value with invalid dates.
	 */
	@Test
	public void testCreateCodeValueInvalidDates() {
		CodeValue codeValue = new CodeValue();
		codeValue.setValidFrom(new Date(new Date().getTime() - 1000 * 60 * 60));
		codeValue.setValidTo(new Date(new Date().getTime() - 1000 * 60 * 60 * 2));

		Map<String, String> validationResult = codeValueValidator.validate(codeValue, false);

		assertTrue(!validationResult.isEmpty());
		assertEquals(validationResult.size(), 4);
		assertTrue(validationResult.get("codeListId") != null
				&& validationResult.get("codeListId").equals("This field is required."));
		assertTrue(validationResult.get("value") != null
				&& validationResult.get("value").equals("This field is required."));
		assertTrue(validationResult.get("validFrom") != null
				&& validationResult.get("validFrom").equals("Validity start date must be greater than current date."));
		assertTrue(validationResult.get("validTo") != null && validationResult
				.get("validTo")
					.equals("Validity end date must be greater than validity start date."));
	}

	/**
	 * Test for validating of create code value with invalid code list id.
	 */
	@Test
	public void testCreateCodeValueInvalidCodeListId() {
		CodeValue codeValue = new CodeValue();
		codeValue.setValidFrom(new Date(new Date().getTime() + 1000 * 60 * 60));
		codeValue.setValidTo(new Date(new Date().getTime() + 1000 * 60 * 60 * 2));
		codeValue.setValue("validValue");
		codeValue.setCodeListId("invalidCodeListId");

		SearchResult searchResult = new SearchResult();
		searchResult.setTotal(0);
		EasyMock.expect(codeListService.getCodeLists(EasyMock.anyObject(CodeListSearchCriteria.class))).andReturn(
				searchResult);

		SearchResult searchResultCV = new SearchResult();
		searchResultCV.setTotal(0);
		EasyMock.expect(codeListService.getCodeValues(EasyMock.anyObject(CodeValueSearchCriteria.class))).andReturn(
				searchResultCV);

		EasyMock.replay(codeListService);

		Map<String, String> validationResult = codeValueValidator.validate(codeValue, false);

		assertTrue(!validationResult.isEmpty());
		assertEquals(validationResult.size(), 1);
		assertTrue(validationResult.get("codeListId") != null
				&& validationResult.get("codeListId").equals("Code list with this ID does not exists."));
	}

	/**
	 * Test for validating of create code value with existing code value.
	 */
	@Test
	public void testCreateCodeValueExistingCodeValue() {
		CodeValue codeValue = new CodeValue();
		codeValue.setValidFrom(new Date(new Date().getTime() + 1000 * 60 * 60));
		codeValue.setValidTo(new Date(new Date().getTime() + 1000 * 60 * 60 * 2));
		codeValue.setValue("existingValue");
		codeValue.setCodeListId("validCodeListId");

		SearchResult searchResult = new SearchResult();
		searchResult.setTotal(1);
		EasyMock.expect(codeListService.getCodeLists(EasyMock.anyObject(CodeListSearchCriteria.class))).andReturn(
				searchResult);

		SearchResult searchResultCV = new SearchResult();
		searchResultCV.setTotal(1);
		EasyMock.expect(codeListService.getCodeValues(EasyMock.anyObject(CodeValueSearchCriteria.class))).andReturn(
				searchResultCV);

		EasyMock.replay(codeListService);

		Map<String, String> validationResult = codeValueValidator.validate(codeValue, false);

		assertTrue(!validationResult.isEmpty());
		assertEquals(validationResult.size(), 1);
		assertTrue(validationResult.get("value") != null
				&& validationResult.get("value").equals("There is already a code value with the same ID."));
	}

	/**
	 * Test for validating of create code value with valid data.
	 */
	@Test
	public void testCreateCodeValueValid() {
		CodeValue codeValue = new CodeValue();
		codeValue.setValidFrom(new Date(new Date().getTime() + 1000 * 60 * 60));
		codeValue.setValidTo(new Date(new Date().getTime() + 1000 * 60 * 60 * 2));
		codeValue.setValue("validValue");
		codeValue.setCodeListId("validCodeListId");

		SearchResult searchResult = new SearchResult();
		searchResult.setTotal(1);
		EasyMock.expect(codeListService.getCodeLists(EasyMock.anyObject(CodeListSearchCriteria.class))).andReturn(
				searchResult);

		SearchResult searchResultCV = new SearchResult();
		searchResultCV.setTotal(0);
		EasyMock.expect(codeListService.getCodeValues(EasyMock.anyObject(CodeValueSearchCriteria.class))).andReturn(
				searchResultCV);

		EasyMock.replay(codeListService);

		Map<String, String> validationResult = codeValueValidator.validate(codeValue, false);

		assertTrue(validationResult.isEmpty());
	}

	/**
	 * Test for validating of update code value for non exising value.
	 */
	@Test
	public void testUpdateCodeValueNotExisting() {
		CodeValue codeValue = new CodeValue();
		codeValue.setValidFrom(new Date(new Date().getTime() + 1000 * 60 * 60));
		codeValue.setValidTo(new Date(new Date().getTime() + 1000 * 60 * 60 * 2));
		codeValue.setValue("notExisitngValue");
		codeValue.setCodeListId("validCodeListId");

		SearchResult searchResult = new SearchResult();
		searchResult.setTotal(1);
		EasyMock.expect(codeListService.getCodeLists(EasyMock.anyObject(CodeListSearchCriteria.class))).andReturn(
				searchResult);

		SearchResult searchResultCV = new SearchResult();
		searchResultCV.setTotal(0);
		EasyMock.expect(codeListService.getCodeValues(EasyMock.anyObject(CodeValueSearchCriteria.class))).andReturn(
				searchResultCV);

		EasyMock.replay(codeListService);

		Map<String, String> validationResult = codeValueValidator.validate(codeValue, true);

		assertTrue(!validationResult.isEmpty());
		assertEquals(validationResult.size(), 1);
		assertTrue(validationResult.get("value") != null
				&& validationResult.get("value").equals("There is no value with this ID in this code list."));
	}

	/**
	 * Test for validating of update code value with valid data.
	 */
	@Test
	public void testUpdateCodeValueValid() {
		CodeValue codeValue = new CodeValue();
		codeValue.setValidFrom(new Date(new Date().getTime() + 1000 * 60 * 60));
		codeValue.setValidTo(new Date(new Date().getTime() + 1000 * 60 * 60 * 2));
		codeValue.setValue("notExisitngValue");
		codeValue.setCodeListId("validCodeListId");

		SearchResult searchResult = new SearchResult();
		searchResult.setTotal(1);
		EasyMock.expect(codeListService.getCodeLists(EasyMock.anyObject(CodeListSearchCriteria.class))).andReturn(
				searchResult);

		SearchResult searchResultCV = new SearchResult();
		searchResultCV.setTotal(1);
		EasyMock.expect(codeListService.getCodeValues(EasyMock.anyObject(CodeValueSearchCriteria.class))).andReturn(
				searchResultCV);

		EasyMock.replay(codeListService);

		Map<String, String> validationResult = codeValueValidator.validate(codeValue, true);

		assertTrue(validationResult.isEmpty());
	}

}
