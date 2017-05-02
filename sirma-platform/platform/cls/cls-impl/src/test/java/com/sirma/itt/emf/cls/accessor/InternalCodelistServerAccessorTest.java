package com.sirma.itt.emf.cls.accessor;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.Serializable;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.easymock.Capture;
import org.easymock.EasyMock;
import org.junit.Before;
import org.junit.Test;

import com.sirma.itt.commons.utils.reflection.ReflectionUtils;
import com.sirma.itt.emf.cls.entity.CodeList;
import com.sirma.itt.emf.cls.entity.CodeListDescription;
import com.sirma.itt.emf.cls.entity.CodeValueDescription;
import com.sirma.itt.emf.cls.retriever.CodeListSearchCriteria;
import com.sirma.itt.emf.cls.retriever.CodeValueSearchCriteria;
import com.sirma.itt.emf.cls.retriever.SearchResult;
import com.sirma.itt.emf.cls.service.CodeListService;
import com.sirma.itt.emf.cls.service.CodeListServiceImpl;
import com.sirma.itt.seip.domain.codelist.CodelistPropertiesConstants;
import com.sirma.itt.seip.domain.codelist.model.CodeValue;

/**
 * Tests the functionality of {@link InternalCodelistServerAccessor}.
 *
 * @author Vilizar Tsonev
 */
public class InternalCodelistServerAccessorTest {

	private InternalCodelistServerAccessor codelistAccessor;

	private CodeListService codeListService;

	/**
	 * Initializes the mocked objects and the object under test.
	 */
	@Before
	public void before() {
		codelistAccessor = new InternalCodelistServerAccessor();
		codeListService = EasyMock.createMock(CodeListServiceImpl.class);
		ReflectionUtils.setField(codelistAccessor, "codeListService", codeListService);
	}

	/**
	 * Tests if {@link CodeListService#getCodeLists(CodeListSearchCriteria)} is invoked with the correct
	 * {@link CodeListSearchCriteria} when getting all code lists.
	 */
	@Test
	public void testCodeListCriteria() {
		List<CodeList> resultsList = new ArrayList<CodeList>();
		SearchResult searchResult = new SearchResult();
		searchResult.setResults(resultsList);

		Capture<CodeListSearchCriteria> capturedArgument = Capture.newInstance();
		EasyMock
				.expect(codeListService.getCodeLists(
						EasyMock.and(EasyMock.capture(capturedArgument), EasyMock.isA(CodeListSearchCriteria.class))))
					.andReturn(searchResult)
					.once();
		EasyMock.replay(codeListService);
		codelistAccessor.getAllCodelists("EN");
		EasyMock.verify(codeListService);

		assertEquals(-1, capturedArgument.getValue().getLimit());
		assertEquals(0, capturedArgument.getValue().getOffset());
		assertTrue(capturedArgument.getValue().isExcludeValues());
	}

	/**
	 * Tests the {@link InternalCodelistServerAccessor#getCodeValues(Integer)} method.
	 */
	@Test
	public void testGetCodeValues() {
		// init the expected code value
		CodeValue expectedValue1 = new CodeValue();
		expectedValue1.setCodelist(1);
		expectedValue1.setValue("INIT");
		Map<String, Serializable> map = new HashMap<>(7);
		map.put("bg", "описание на български");
		map.put("en", "english description");
		map.put("cz", "něco v české");
		map.put("fr", "description en français");
		map.put(CodelistPropertiesConstants.COMMENT, "english comment");
		map.put(CodelistPropertiesConstants.EXTRA1, "extra1");
		map.put(CodelistPropertiesConstants.EXTRA2, "extra2");
		map.put(CodelistPropertiesConstants.EXTRA3, "extra3");
		map.put(CodelistPropertiesConstants.MASTER_VALUE, "333");
		expectedValue1.setProperties(Collections.unmodifiableMap(map));
		// init the expected map of code values
		Map<String, CodeValue> expectedCodeValuesMap = new HashMap<>();
		expectedCodeValuesMap.put("INIT", expectedValue1);
		// init the code value that the mocked CodeListService will return
		com.sirma.itt.emf.cls.entity.CodeValue codeValue1 = new com.sirma.itt.emf.cls.entity.CodeValue();
		codeValue1.setCodeListId("1");
		codeValue1.setValue("INIT");
		// english description
		CodeValueDescription englishDescription = new CodeValueDescription();
		englishDescription.setDescription("english description");
		englishDescription.setLanguage("en");
		englishDescription.setComment("english comment");
		// bulgarian description
		CodeValueDescription bulgarianDescription = new CodeValueDescription();
		bulgarianDescription.setDescription("описание на български");
		bulgarianDescription.setLanguage("bg");
		// czech description
		CodeValueDescription czechDescription = new CodeValueDescription();
		czechDescription.setDescription("něco v české");
		czechDescription.setLanguage("cz");
		// french description
		CodeValueDescription frenchDecription = new CodeValueDescription();
		frenchDecription.setDescription("description en français");
		frenchDecription.setLanguage("fr");
		codeValue1.setDescriptions(Arrays.asList(new CodeValueDescription[] { englishDescription, frenchDecription,
				bulgarianDescription, czechDescription }));
		codeValue1.setExtra1("extra1");
		codeValue1.setExtra2("extra2");
		codeValue1.setExtra3("extra3");
		codeValue1.setMasterValue("333");
		// init the SearchResult that will be returned by the mocked CodeListService
		SearchResult searchResult = new SearchResult();
		searchResult.setResults(Arrays.asList(new com.sirma.itt.emf.cls.entity.CodeValue[] { codeValue1 }));

		EasyMock
				.expect(codeListService.getCodeValues(EasyMock.anyObject(CodeValueSearchCriteria.class)))
					.andReturn(searchResult)
					.anyTimes();
		EasyMock.replay(codeListService);
		assertEquals(expectedCodeValuesMap, codelistAccessor.getCodeValues(1, "EN"));
	}

	/**
	 * Tests the {@link InternalCodelistServerAccessor#getAllCodelists()} method.
	 */
	@Test
	public void testGetAllCodelists() {
		SearchResult expectedResult = new SearchResult();
		List<CodeList> expectedList = new ArrayList<CodeList>();
		// first codelist
		CodeList cl1 = new CodeList();
		cl1.setValue("1");
		// add BG description
		CodeListDescription bulgarianDescription1 = new CodeListDescription();
		bulgarianDescription1.setDescription("Първа код листа");
		bulgarianDescription1.setLanguage("bg");
		// add EN description
		CodeListDescription englishDescription1 = new CodeListDescription();
		englishDescription1.setDescription("First code list");
		englishDescription1.setLanguage("en");
		// Add CZ description
		CodeListDescription czechDescription1 = new CodeListDescription();
		czechDescription1.setDescription("první kód list");
		czechDescription1.setLanguage("cz");
		cl1.setDescriptions(Arrays
				.asList(new CodeListDescription[] { bulgarianDescription1, englishDescription1, czechDescription1 }));
		// second codelist
		CodeList cl2 = new CodeList();
		cl2.setValue("2");
		CodeListDescription englishDescription2 = new CodeListDescription();
		englishDescription2.setDescription("Second code list");
		englishDescription2.setLanguage("en");
		cl2.setDescriptions(Arrays.asList(new CodeListDescription[] { englishDescription2 }));
		// populate the expected results list
		expectedList.add(cl1);
		expectedList.add(cl2);
		expectedResult.setResults(expectedList);

		EasyMock
				.expect(codeListService.getCodeLists(EasyMock.anyObject(CodeListSearchCriteria.class)))
					.andReturn(expectedResult)
					.anyTimes();
		EasyMock.replay(codeListService);

		Map<BigInteger, String> expected = new HashMap<>();
		expected.put(new BigInteger("1"), "First code list");
		expected.put(new BigInteger("2"), "Second code list");
		Map<BigInteger, String> actual = codelistAccessor.getAllCodelists("EN");
		assertEquals(expected, actual);
	}

}
