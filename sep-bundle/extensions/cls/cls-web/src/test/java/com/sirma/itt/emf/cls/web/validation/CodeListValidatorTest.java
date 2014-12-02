package com.sirma.itt.emf.cls.web.validation;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.Date;
import java.util.Map;

import javax.inject.Inject;

import org.easymock.EasyMock;
import org.junit.Rule;
import org.junit.Test;

import com.sirma.itt.emf.cls.entity.CodeList;
import com.sirma.itt.emf.cls.retriever.CodeListSearchCriteria;
import com.sirma.itt.emf.cls.retriever.SearchResult;
import com.sirma.itt.emf.cls.service.CodeListService;

import de.akquinet.jbosscc.needle.annotation.ObjectUnderTest;
import de.akquinet.jbosscc.needle.junit.NeedleRule;

/**
 * Tests server side validations for creating and updating {@link CodeList}
 */
public class CodeListValidatorTest {
	
	@Rule
	public NeedleRule needleRule = new NeedleRule();
	
	@Inject
	private CodeListService codeListService;

	@ObjectUnderTest(implementation = CodeListValidator.class)
	private Validator<CodeList> codeListValidator;

	/**
	 * Test for validating of create code list with missing data for some fields.
	 */
	@Test
	public void testCreateCodeListMissingFields() {
		CodeList codeList = new CodeList();
		codeList.setValidTo(new Date(new Date().getTime() - 6000));

		Map<String, String> validationResult = codeListValidator.validate(codeList, false);

		assertTrue(!validationResult.isEmpty());
		assertEquals(validationResult.size(), 3);
		assertTrue(validationResult.get("validTo") != null && validationResult.get("validTo").equals("Validity end date must be greater than current date."));
		assertTrue(validationResult.get("value") != null && validationResult.get("value").equals("This field is required."));
		assertTrue(validationResult.get("validFrom") != null && validationResult.get("validFrom").equals("This field is required."));
	}

	/**
	 * Test for validating of create code list with wrong value according a regex validation.
	 */
	@Test
	public void testCreateCodeListInvalidValue() {
		CodeList codeList = new CodeList();
		codeList.setValidFrom(new Date());
		codeList.setValidTo(new Date(new Date().getTime() + 1000*60*60));
		codeList.setValue("8_$%");

		Map<String, String> validationResult = codeListValidator.validate(codeList, false);

		assertTrue(!validationResult.isEmpty());
		assertEquals(validationResult.size(), 1);
		assertTrue(validationResult.get("value") != null && validationResult.get("value").equals("Must start with a letter. No special characters allowed."));
	}

	/**
	 * Test for validating of create code list with value which is already present in the database.
	 */
	@Test
	public void testCreateCodeListDuplicatedValue() {
		CodeList codeList = new CodeList();
		codeList.setValidFrom(new Date());
		codeList.setValidTo(new Date(new Date().getTime() + 1000*60*60));
		codeList.setValue("duplicatedValue");
		
		SearchResult searchResult = new SearchResult();
		searchResult.setTotal(1);
		EasyMock.expect(codeListService.getCodeLists(EasyMock.anyObject(CodeListSearchCriteria.class))).andReturn(searchResult);
		EasyMock.replay(codeListService);
		
		Map<String, String> validationResult = codeListValidator.validate(codeList, false);

		assertTrue(!validationResult.isEmpty());
		assertTrue(validationResult.get("value") != null && validationResult.get("value").equals("There is already a code list with the same ID."));
	}

	/**
	 * Test for validating of create code list with invalid valid to date (must be greated than valid from date).
	 */
	@Test
	public void testCreateCodeListInvalidValidTo() {
		Date aDate = new Date(new Date().getTime() + 1000*60*60);

		CodeList codeList = new CodeList();
		codeList.setValidFrom(aDate);
		codeList.setValidTo(aDate);
		codeList.setValue("validValue");

		SearchResult searchResult = new SearchResult();
		searchResult.setTotal(0);
		EasyMock.expect(codeListService.getCodeLists(EasyMock.anyObject(CodeListSearchCriteria.class))).andReturn(searchResult);
		EasyMock.replay(codeListService);
		
		Map<String, String> validationResult = codeListValidator.validate(codeList, false);

		assertTrue(!validationResult.isEmpty());
		assertEquals(validationResult.size(), 1);
		assertTrue(validationResult.get("validTo") != null && validationResult.get("validTo").equals("Validity end date must be greater than validity start date."));
	}

	/**
	 * Test for validating of create code list with valid data.
	 */
	@Test
	public void testCreateCodeListValid() {
		CodeList codeList = new CodeList();
		codeList.setValidFrom(new Date());
		codeList.setValidTo(new Date(new Date().getTime() + 1000*60*60));
		codeList.setValue("validValue");
		
		SearchResult searchResult = new SearchResult();
		searchResult.setTotal(0);
		EasyMock.expect(codeListService.getCodeLists(EasyMock.anyObject(CodeListSearchCriteria.class))).andReturn(searchResult);
		EasyMock.replay(codeListService);
		
		Map<String, String> validationResult = codeListValidator.validate(codeList, false);

		assertTrue(validationResult.isEmpty());
	}
	
	/**
	 * Test for validating of update code list for non existing value.
	 */
	@Test
	public void testUpdateCodeListNotExisting() {
		CodeList codeList = new CodeList();
		codeList.setValidFrom(new Date());
		codeList.setValidTo(new Date(new Date().getTime() + 1000*60*60));
		codeList.setValue("notExistingValue");
		
		SearchResult searchResult = new SearchResult();
		searchResult.setTotal(0);
		EasyMock.expect(codeListService.getCodeLists(EasyMock.anyObject(CodeListSearchCriteria.class))).andReturn(searchResult);
		EasyMock.replay(codeListService);
		
		Map<String, String> validationResult = codeListValidator.validate(codeList, true);

		assertTrue(!validationResult.isEmpty());
		assertTrue(validationResult.get("value") != null && validationResult.get("value").equals("There is no code list with this ID."));
	}
	
	/**
	 * Test for validating of update code list with vallid data.
	 */
	@Test
	public void testUpdateCodeListValid() {
		CodeList codeList = new CodeList();
		codeList.setValidFrom(new Date());
		codeList.setValidTo(new Date(new Date().getTime() + 1000*60*60));
		codeList.setValue("validValue");
		
		SearchResult searchResult = new SearchResult();
		searchResult.setTotal(1);
		EasyMock.expect(codeListService.getCodeLists(EasyMock.anyObject(CodeListSearchCriteria.class))).andReturn(searchResult);
		EasyMock.replay(codeListService);
		
		Map<String, String> validationResult = codeListValidator.validate(codeList, true);

		assertTrue(validationResult.isEmpty());
	}
}
