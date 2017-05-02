package com.sirma.cmf.web.autocomplete;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;

import javax.ws.rs.core.MultivaluedHashMap;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.mockito.InjectMocks;
import org.mockito.Matchers;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.testng.Assert;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import com.sirma.itt.emf.label.retrieve.FieldValueRetrieverService;
import com.sirma.itt.emf.label.retrieve.RetrieveResponse;
import com.sirma.itt.seip.Pair;
import com.sirma.itt.seip.domain.definition.label.LabelProvider;
import com.sirma.itt.seip.domain.search.SearchRequest;
import com.sirma.itt.seip.search.SearchConfiguration;

/**
 * Test the {@link AutocompleteRestService}.
 *
 * @author nvelkov
 */
public class AutocompleteRestServiceTest {

	@Mock
	private FieldValueRetrieverService fieldValueRetrieverService;

	@Mock
	private SearchConfiguration searchConfiguration;

	@Mock
	private LabelProvider labelProvider;

	@InjectMocks
	private AutocompleteRestService autoCompleteRestService;

	private static final String LABEL_SUFFIX = " label ";

	/**
	 * Init the mocked instances.
	 */
	@BeforeMethod
	public void init() {
		MockitoAnnotations.initMocks(this);
	}

	/**
	 * Test the load labels method with expected result containing the values and their respective labels in the form of
	 * the value + the LABEL_SUFFIX variable.
	 *
	 * @throws JSONException
	 *             thrown when parsing the result
	 */
	@Test
	public void testLoadLabels() throws JSONException {
		mockFieldValueRetrieverService();
		mockLabelProvider();
		List<String> values = new ArrayList<>();
		values.add("value1");
		values.add("value2");

		Response response = autoCompleteRestService.loadLabels("header", values, Mockito.mock(UriInfo.class));
		JSONArray result = new JSONArray(response.getEntity().toString());
		Assert.assertEquals(result.getJSONObject(0).get("label"), "value2" + LABEL_SUFFIX, "The label is incorrect");
		Assert.assertEquals(result.getJSONObject(1).get("label"), "value1" + LABEL_SUFFIX, "The label is incorrect");
	}

	/**
	 * Test the load labels method when no labels have been found so an error message is returned instead.
	 */
	@Test
	public void testLoadLabelsNoLabelsFound() {
		mockLabelProvider();
		List<String> values = new ArrayList<>();
		values.add("value1");
		values.add("value2");

		Response response = autoCompleteRestService.loadLabels("header", values, Mockito.mock(UriInfo.class));
		String result = (String) response.getEntity();
		System.out.println(result);
		Assert.assertTrue(result.contains("error"), "The label is incorrect");
	}

	/**
	 * Test the load values method which should return a result containing values and their labels based on the passed
	 * query offset and limit.
	 *
	 * @throws JSONException
	 *             thrown when parsing the result
	 */
	@Test
	public void testLoadValues() throws JSONException {
		mockFieldValueRetrieverService();

		UriInfo uriInfo = Mockito.mock(UriInfo.class);
		MultivaluedMap<String, String> params = new MultivaluedHashMap<>(1);
		params.add("q", "someQuery");
		Mockito.when(uriInfo.getQueryParameters()).thenReturn(params);

		JSONObject response = new JSONObject(autoCompleteRestService.load("header", uriInfo));
		Assert.assertEquals(response.get("total"), 1, "The result size should've been 1.");
		Assert.assertEquals(response.getJSONArray("results").getJSONObject(0).get("id"), "value",
				"The id should've been 'value'");
		Assert.assertEquals(response.getJSONArray("results").getJSONObject(0).get("text"), "label",
				"The text should've been 'label'");
	}

	/**
	 * Mock the fieldValueRetrieverService to retrieve fake labels, generated in the createLabel method.
	 */
	private void mockFieldValueRetrieverService() {
		Mockito
				.when(fieldValueRetrieverService.getLabels(Matchers.anyString(), Matchers.any(),
						Matchers.any(SearchRequest.class)))
					.thenAnswer(answer -> Arrays.stream(answer.getArgumentAt(1, String[].class)).collect(
							Collectors.toMap(Function.identity(), value -> createLabel(value))));

		List<Pair<String, String>> values = new ArrayList<>();
		values.add(new Pair<>("value", "label"));
		RetrieveResponse response = new RetrieveResponse(1L, values);

		Mockito
				.when(fieldValueRetrieverService.getValues(Matchers.anyString(), Matchers.anyString(),
						Matchers.any(SearchRequest.class), Matchers.anyInt(), Matchers.anyInt()))
					.thenReturn(response);
	}

	/**
	 * Mock the label provider to return an error string. The labelProvider in {@link AutocompleteRestService} is only
	 * used when no labels have been found.
	 */
	private void mockLabelProvider() {
		Mockito.when(labelProvider.getValue(Matchers.anyString())).thenReturn("error");
	}

	/**
	 * Create a fake label for the provided value. Used for mocking purposes.
	 *
	 * @param value
	 *            the value
	 * @return the fake label. The label will contain the original value with the label suffix appended at the end.
	 */
	private static String createLabel(String value) {
		return value + LABEL_SUFFIX;
	}
}
