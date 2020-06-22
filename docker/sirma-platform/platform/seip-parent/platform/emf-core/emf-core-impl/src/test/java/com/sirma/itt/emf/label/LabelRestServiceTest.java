package com.sirma.itt.emf.label;

import java.util.ArrayList;
import java.util.List;

import org.mockito.Matchers;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import com.sirma.itt.emf.label.retrieve.FieldValueRetrieverService;
import com.sirma.itt.seip.ShortUri;
import com.sirma.itt.seip.convert.TypeConverter;
import com.sirma.itt.seip.definition.label.SystemCode;
import com.sirma.itt.seip.domain.definition.label.LabelProvider;
import com.sirma.itt.seip.domain.search.SearchRequest;
import com.sirma.itt.seip.testutil.EmfTest;
import com.sirma.itt.seip.util.ReflectionUtils;

import net.javacrumbs.jsonunit.JsonAssert;

/**
 * Tests the functionality of {@link LabelRestService}.
 *
 * @author Vilizar Tsonev
 */
@Test
public class LabelRestServiceTest extends EmfTest {

	private LabelRestService labelsRestService;

	@Mock
	private FieldValueRetrieverService fieldValueRetrievalService;

	@Mock
	TypeConverter typeConverter;

	@Mock
	private LabelProvider labelProvider;

	/**
	 * Initialise the mocks for all tests.
	 */
	@BeforeClass
	protected void init() {
		labelsRestService = new LabelRestService();
	}

	/**
	 * Initialise fields and mocks before each test.
	 */
	@BeforeMethod
	protected void initTests() {
		MockitoAnnotations.initMocks(this);
		ReflectionUtils.setFieldValue(labelsRestService, "fieldValueRetrievalService", fieldValueRetrievalService);
		ReflectionUtils.setFieldValue(labelsRestService, "typeConverter", typeConverter);
		ReflectionUtils.setFieldValue(labelsRestService, "labelProvider", labelProvider);
	}

	/**
	 * Test the get bundle labels method with empty input data. Empty json object should be returned.
	 */
	@Test
	public void testGetBundleLabelsEmptyData() {
		String result = labelsRestService.getBundleLabels("");
		Assert.assertEquals(result, "{}");
	}

	/**
	 * Test the get bundle labels method with some input data. A json object containing the key-label pairs should be
	 * returned.
	 */
	@Test
	public void testGetBundleLabels() {
		Mockito.when(labelProvider.getBundleValue(Matchers.anyString())).thenReturn("label");
		String result = labelsRestService.getBundleLabels("[\"id1\",\"id2\"]");
		JsonAssert.assertJsonEquals("{\"id1\":\"label\",\"id2\":\"label\"}", result);
	}

	/**
	 * Tests the {@link LabelRestService#getLabels(java.util.List)} when retrieving label by
	 * code and code list number.
	 */
	public void testGetLabelsWithCodeLists() {
		// we suppose that the retriever returns 'Medium' for the given code and indicator (which
		// depends on the model)
		Mockito
				.when(fieldValueRetrievalService.getLabel(Matchers.eq("codelist"), Matchers.eq("0006-000084"),
						Matchers.any(SearchRequest.class)))
					.thenReturn("Medium");
		// create a dummy input
		List<SystemCode> inputCodes = new ArrayList<>();
		SystemCode systemCode = new SystemCode();
		systemCode.setCode("0006-000084");
		systemCode.setIndicator("codelist");
		systemCode.setIndicatorValue("208");
		inputCodes.add(systemCode);

		String expectedJson = "[{\"label\": \"Medium\", \"code\": \"0006-000084\"}]";
		String returnedJson = labelsRestService.getLabels(inputCodes);
		JsonAssert.assertJsonEquals(expectedJson, returnedJson);
	}

	/**
	 * Tests the {@link LabelRestService#getLabels(List)} when retrieving user display
	 * names by URI.
	 */
	public void testGetLabelsWithUsers() {
		// these depend on the model, so we predefine them for the test
		String fullUriString = "http://ittruse.ittbg.com/ontology/enterpriseManagementFramework#admin";
		String shortUriString = "emf:admin";
		// mock the type converter
		ShortUri shortUri = new ShortUri(shortUriString);
		Mockito.when(typeConverter.convert(ShortUri.class, fullUriString)).thenReturn(shortUri);
		// mock the retriever service
		Mockito
				.when(fieldValueRetrievalService.getLabel(Matchers.eq("usernamebyuri"), Matchers.eq(shortUriString),
						Matchers.any(SearchRequest.class)))
					.thenReturn("admin admin");
		// create a dummy input
		List<SystemCode> inputCodes = new ArrayList<>();
		SystemCode systemCode = new SystemCode();
		systemCode.setCode(fullUriString);
		systemCode.setIndicator("usernamebyuri");
		systemCode.setIndicatorValue("0");
		inputCodes.add(systemCode);

		String expectedJson = "[{\"label\": \"admin admin\", \"code\": \"" + fullUriString + "\"}]";
		String returnedJson = labelsRestService.getLabels(inputCodes);
		JsonAssert.assertJsonEquals(expectedJson, returnedJson);
	}
}
