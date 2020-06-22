package com.sirma.itt.emf.solr.services.impl.facet;

import static org.mockito.Matchers.argThat;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

import org.apache.solr.client.solrj.response.FacetField;
import org.apache.solr.client.solrj.response.QueryResponse;
import org.apache.solr.common.SolrDocument;
import org.apache.solr.common.SolrDocumentList;
import org.apache.solr.common.params.FacetParams.FacetRangeOther;
import org.mockito.InjectMocks;
import org.mockito.Matchers;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import com.google.common.collect.Sets;
import org.apache.commons.lang3.StringUtils;
import com.sirma.itt.emf.label.retrieve.FieldId;
import com.sirma.itt.emf.label.retrieve.FieldValueRetrieverParameters;
import com.sirma.itt.emf.label.retrieve.FieldValueRetrieverService;
import com.sirma.itt.seip.convert.TypeConverter;
import com.sirma.itt.seip.domain.definition.label.LabelProvider;
import com.sirma.itt.seip.domain.instance.DefaultProperties;
import com.sirma.itt.seip.domain.instance.Instance;
import com.sirma.itt.seip.domain.search.SearchArguments;
import com.sirma.itt.seip.domain.search.SearchRequest;
import com.sirma.itt.seip.domain.search.facet.Facet;
import com.sirma.itt.seip.domain.search.facet.FacetQueryParameters;
import com.sirma.itt.seip.domain.search.facet.FacetValue;
import com.sirma.itt.seip.testutil.CustomMatcher;
import com.sirma.itt.semantic.NamespaceRegistryService;

/**
 * Tests the utility methods for facet result transforming in {@link FacetResultTransformer}.
 *
 * @author Mihail Radkov
 * @since 1.10.1
 */
public class FacetResultTransformerTest {

	@Mock
	private LabelProvider labelProvider;

	@Mock
	private NamespaceRegistryService namespaceRegistryService;

	@Mock
	private FieldValueRetrieverService fieldValueRetrievalService;

	@InjectMocks
	private FacetResultTransformer facetResultTransformer = new FacetResultTransformer();

	/**
	 * Initializes mocks.
	 */
	@BeforeClass
	public void initMocks() {
		MockitoAnnotations.initMocks(this);
	}

	/**
	 * Tests the logic inside {@link FacetResultTransformer#extractFacetsFromResponse(SearchArguments, QueryResponse)}
	 */
	@Test
	public void testFacetValuesExtraction() {
		Facet facet = new Facet();
		facet.setId("facet");
		facet.setSolrFieldName("myFacet");

		Map<String, Facet> facets = new HashMap<>();
		facets.put(facet.getId(), facet);

		FacetField field = new FacetField("myFacet");
		field.add("a", 11);
		field.add("b", 2);
		field.add("c", 0);
		field.add("d", -9001);
		field.add(null, 4);

		SearchArguments<Instance> arguments = new SearchArguments<>();
		arguments.setFacets(facets);

		QueryResponse response = Mockito.mock(QueryResponse.class);
		Mockito.when(response.getFacetFields()).thenReturn(Arrays.asList(field));

		facetResultTransformer.extractFacetsFromResponse(arguments, response);

		List<FacetValue> values = facet.getValues();

		Assert.assertNotNull(values);
		Assert.assertEquals(values.size(), 3);
		Assert.assertTrue(valueExist(values, "a", 11));
		Assert.assertTrue(valueExist(values, "b", 2));
		Assert.assertTrue(valueExist(values, FacetQueryParameters.NO_VALUE, 4));
	}

	/**
	 * Tests the logic inside {@link FacetResultTransformer#extractFacetsFromResponse(SearchArguments, QueryResponse)}
	 * when there are results but no there is facet for them.
	 */
	@Test
	public void testFacetValuesExtractionWithMissingFacet() {
		Facet facet = new Facet();
		facet.setId("facet");
		facet.setSolrFieldName("myFacet");

		Map<String, Facet> facets = new HashMap<>();
		facets.put(facet.getId(), facet);

		FacetField field = new FacetField("myOtherFacet");
		field.add("a", 11);

		SearchArguments<Instance> arguments = new SearchArguments<>();
		arguments.setFacets(facets);

		QueryResponse response = Mockito.mock(QueryResponse.class);
		Mockito.when(response.getFacetFields()).thenReturn(Arrays.asList(field));

		facetResultTransformer.extractFacetsFromResponse(arguments, response);

		List<FacetValue> values = facet.getValues();
		Assert.assertTrue(values == null || values.isEmpty());
	}

	/**
	 * Tests the logic inside {@link FacetResultTransformer#extractFacetsFromResponse(SearchArguments, QueryResponse)}
	 * when there are results but with no values.
	 */
	@Test
	public void testFacetValuesExtractionWithEmptyValues() {
		Facet facet = new Facet();
		facet.setId("facet");
		facet.setSolrFieldName("myFacet");

		Map<String, Facet> facets = new HashMap<>();
		facets.put(facet.getId(), facet);

		FacetField field = new FacetField("myFacet");

		SearchArguments<Instance> arguments = new SearchArguments<>();
		arguments.setFacets(facets);

		QueryResponse response = Mockito.mock(QueryResponse.class);
		Mockito.when(response.getFacetFields()).thenReturn(Arrays.asList(field));

		facetResultTransformer.extractFacetsFromResponse(arguments, response);

		List<FacetValue> values = facet.getValues();
		Assert.assertTrue(values == null || values.isEmpty());
	}

	/**
	 * Tests the logic inside {@link FacetResultTransformer#extractFacetsFromResponse(SearchArguments, QueryResponse)}
	 * when there are results but with no values.
	 */
	@Test
	public void testFacetValuesExtractionWithMissingValues() {
		Facet facet = new Facet();
		facet.setId("facet");
		facet.setSolrFieldName("myFacet");

		Map<String, Facet> facets = new HashMap<>();
		facets.put(facet.getId(), facet);

		SearchArguments<Instance> arguments = new SearchArguments<>();
		arguments.setFacets(facets);

		QueryResponse response = Mockito.mock(QueryResponse.class);
		Mockito.when(response.getFacetFields()).thenReturn(Arrays.asList());

		facetResultTransformer.extractFacetsFromResponse(arguments, response);

		List<FacetValue> values = facet.getValues();
		Assert.assertTrue(values == null || values.isEmpty());
	}

	/**
	 * Test the removal of invalid object facet values.
	 */
	@Test
	public void testRemoveInvalidObjectFacetValues() {
		SolrDocumentList solrDocumentList = new SolrDocumentList();

		SolrDocument validSolrDocument = new SolrDocument();
		validSolrDocument.setField(DefaultProperties.URI, "validResult");
		solrDocumentList.add(validSolrDocument);

		Facet facet = new Facet();
		FacetValue validFacetValue = new FacetValue();
		validFacetValue.setId("validResult");
		FacetValue invalidFacetValue = new FacetValue();
		invalidFacetValue.setId("invalidResult");
		List<FacetValue> facetValues = new ArrayList<>();
		facetValues.add(validFacetValue);
		facetValues.add(invalidFacetValue);
		facet.setValues(facetValues);

		facetResultTransformer.removeInvalidObjectFacetValues(solrDocumentList, facet);
		Assert.assertEquals(facet.getValues().size(), 1);
		Assert.assertEquals(facet.getValues().get(0).getId(), validSolrDocument.get(DefaultProperties.URI));
	}

	/**
	 * Tests the logic inside {@link FacetResultTransformer#formatDatesToUTC(SearchArguments)} when the faceting is
	 * disabled.
	 */
	@Test
	public void testUTCDateFormattingWithoutFaceting() {
		Map<String, Facet> facets = new HashMap<>();

		Facet facet = new Facet();
		facet.setId("myFacet");

		Facet dateFacet = new Facet();
		dateFacet.setSolrType("tdates");
		dateFacet.setId("myFacet");

		FacetValue dateFacetValue = new FacetValue();
		dateFacetValue.setId("2015-06-10T12:34:56.78Z");
		dateFacet.setValues(Arrays.asList(dateFacetValue));

		SearchArguments<Instance> searchArguments = new SearchArguments<>();
		searchArguments.setFacets(facets);
		searchArguments.setFaceted(false);

		facetResultTransformer.formatDatesToUTC(searchArguments);
		Assert.assertEquals(dateFacetValue.getId(), "2015-06-10T12:34:56.78Z");

		searchArguments.setFacets(null);
		searchArguments.setFaceted(true);

		facetResultTransformer.formatDatesToUTC(searchArguments);
		Assert.assertEquals(dateFacetValue.getId(), "2015-06-10T12:34:56.78Z");
	}

	/**
	 * Tests the logic inside {@link FacetResultTransformer#formatDatesToUTC(SearchArguments)}.
	 */
	@Test
	public void testUTCDateFormatting() {
		Map<String, Facet> facets = new HashMap<>();

		Facet facet = new Facet();
		facet.setId("myFacet");

		FacetValue facetValue = new FacetValue();
		facetValue.setId("some_id");

		facet.setValues(Arrays.asList(facetValue));
		facets.put(facet.getId(), facet);

		Facet dateFacet = new Facet();
		dateFacet.setSolrType("tdates");
		dateFacet.setId("myFacet");

		FacetValue dateFacetValue = new FacetValue();
		dateFacetValue.setId("2015-06-10T12:34:56.78Z");

		FacetValue dateOtherFacetValue = new FacetValue();
		dateOtherFacetValue.setId(FacetRangeOther.BETWEEN.name().toLowerCase());

		dateFacet.setValues(Arrays.asList(dateFacetValue, dateOtherFacetValue));
		facets.put(dateFacet.getId(), dateFacet);

		SearchArguments<Instance> searchArguments = new SearchArguments<>();
		searchArguments.setFacets(facets);
		searchArguments.setFaceted(true);

		facetResultTransformer.formatDatesToUTC(searchArguments);

		Assert.assertEquals(facet.getValues().size(), 1);
		Assert.assertEquals(facetValue.getId(), "some_id");

		Assert.assertEquals(dateFacet.getValues().size(), 2);
		Assert.assertEquals(dateFacetValue.getId(), "2015-06-10T12:34:56.780Z");
		Assert.assertEquals(dateOtherFacetValue.getId(), FacetRangeOther.BETWEEN.name().toLowerCase());
	}

	/**
	 * Tests the logic inside {@link FacetResultTransformer#removeUnselectedFacetValues(Facet)} when there are
	 * {@link FacetValue} but no selected ones.
	 */
	@Test
	public void testRemovingUnselectedFacetsValuesWithNoSelectedValues() {
		Facet facet = new Facet();
		facet.setId("myFacet");

		List<FacetValue> values = new ArrayList<>();

		FacetValue facetValue1 = new FacetValue();
		facetValue1.setId("some_id1");
		values.add(facetValue1);

		FacetValue facetValue2 = new FacetValue();
		facetValue2.setId("some_id2");
		values.add(facetValue2);

		FacetValue facetValue3 = new FacetValue();
		facetValue3.setId("some_id3");
		values.add(facetValue3);

		facet.setValues(values);

		facetResultTransformer.removeUnselectedFacetValues(facet);

		Assert.assertEquals(facet.getValues().size(), 3);
		Assert.assertTrue(valueExist(facet.getValues(), "some_id1", 0));
		Assert.assertTrue(valueExist(facet.getValues(), "some_id2", 0));
		Assert.assertTrue(valueExist(facet.getValues(), "some_id3", 0));
	}

	/**
	 * Tests the logic inside {@link FacetResultTransformer#removeUnselectedFacetValues(Facet)} when there are
	 * {@link FacetValue} AND selected ones.
	 */
	@Test
	public void testRemovingUnselectedFacetsValues() {
		Facet facet = new Facet();
		facet.setId("myFacet");

		List<FacetValue> values = new ArrayList<>();

		FacetValue facetValue1 = new FacetValue();
		facetValue1.setId("some_id1");
		values.add(facetValue1);

		FacetValue facetValue2 = new FacetValue();
		facetValue2.setId("some_id2");
		values.add(facetValue2);

		FacetValue facetValue3 = new FacetValue();
		facetValue3.setId("some_id3");
		values.add(facetValue3);

		facet.setValues(values);
		facet.setSelectedValues(new HashSet<>(Arrays.asList("some_id1")));

		facetResultTransformer.removeUnselectedFacetValues(facet);

		Assert.assertEquals(facet.getValues().size(), 1);
		Assert.assertTrue(valueExist(facet.getValues(), "some_id1", 0));
	}

	/**
	 * Tests the logic inside {@link FacetResultTransformer#removeNullValues(List)} when there are {@link FacetValue}
	 * with correct IDs and some with {@link FacetQueryParameters#NO_VALUE} as IDs.
	 */
	@Test
	public void testRemovingNoValuedValues() {
		List<FacetValue> values = new ArrayList<>();

		FacetValue facetValue1 = new FacetValue();
		facetValue1.setId("some_id1");
		values.add(facetValue1);

		FacetValue facetValue2 = new FacetValue();
		facetValue2.setId(FacetQueryParameters.NO_VALUE);
		values.add(facetValue2);

		FacetValue facetValue3 = new FacetValue();
		facetValue3.setId("some_id3");
		values.add(facetValue3);

		facetResultTransformer.removeNullValues(values);

		Assert.assertEquals(values.size(), 2);
		Assert.assertTrue(valueExist(values, "some_id1", 0));
		Assert.assertTrue(valueExist(values, "some_id3", 0));
		Assert.assertFalse(valueExist(values, FacetQueryParameters.NO_VALUE, 0));
	}

	/**
	 * Tests the logic inside {@link FacetResultTransformer#assignFacetValuesLabels(Map, Facet)}.
	 */
	@Test
	public void testAssignFacetValuesLabels() {
		Facet facet = new Facet();
		facet.setId("myFacet");

		FacetValue facetValue1 = new FacetValue();
		facetValue1.setId("some_id1");
		mockTypeConverter("some_id1");
		facet.setValues(Arrays.asList(facetValue1));

		Map<String, String> labels = new HashMap<>();
		labels.put("some_id1", "What a test..");

		facetResultTransformer.assignFacetValuesLabels(labels, facet);

		Assert.assertEquals(facetValue1.getText(), "What a test..");
	}

	/**
	 * Tests the logic inside {@link FacetResultTransformer#assignLabels(java.util.Collection)} when the
	 * {@link FacetValue} already has a label.
	 */
	@Test
	public void testAssignLabelsWithExistingLabel() {
		Facet facet = new Facet();
		facet.setId("myFacet");

		FacetValue facetValue1 = new FacetValue();
		facetValue1.setId("some_id1");
		facetValue1.setText("Smashing label");

		facet.setValues(Arrays.asList(facetValue1));

		facetResultTransformer.assignLabels(Arrays.asList(facet));
		Assert.assertEquals(facetValue1.getText(), "Smashing label");
	}

	/**
	 * Tests the logic inside {@link FacetResultTransformer#assignLabels(java.util.Collection)} when the
	 * {@link FacetValue} is a date value.
	 */
	@Test
	public void testAssignLabelsForDateFacets() {
		Facet facet = new Facet();
		facet.setId("myFacet");
		facet.setSolrType("tdates");

		FacetValue facetValue1 = new FacetValue();
		facetValue1.setId("some_id1");

		facet.setValues(Arrays.asList(facetValue1));

		facetResultTransformer.assignLabels(Arrays.asList(facet));
		Assert.assertTrue(StringUtils.isBlank(facetValue1.getText()));
	}

	/**
	 * Tests the logic inside {@link FacetResultTransformer#assignLabels(java.util.Collection)} when the
	 * {@link FacetValue} is {@link FacetQueryParameters#NO_VALUE}.
	 */
	@Test
	public void testAssignLabelsForNoValueValues() {
		mockLabelProvider("What a label!");
		Facet facet = new Facet();
		facet.setId("myFacet");

		FacetValue facetValue1 = new FacetValue();
		facetValue1.setId(FacetQueryParameters.NO_VALUE);

		facet.setValues(Arrays.asList(facetValue1));

		facetResultTransformer.assignLabels(Arrays.asList(facet));
		Assert.assertEquals(facetValue1.getText(), "What a label!");
	}

	/**
	 * Tests the logic inside {@link FacetResultTransformer#assignLabels(java.util.Collection)} when the {@link Facet}
	 * is for code lists and there are multiple codelists returned, one of which does not contain a label for the
	 * provided id, so it shouldn't be included in the end result.
	 */
	@Test
	public void testAssignLabelsForCodelistFacetsMultipleCodelists() {
		mockRetrieverService(FieldId.CODE_LIST, "some_id", "label for 101", 101);
		mockRetrieverService(FieldId.CODE_LIST, "some_id", "label for 200", 200);
		mockRetrieverService(FieldId.CODE_LIST, "some_id", "some_id", 300);
		Facet facet = new Facet();
		facet.setId("myFacet");
		facet.setCodelists(Sets.newHashSet(101, 200, 300));

		FacetValue facetValue1 = new FacetValue();
		facetValue1.setId("some_id");

		facet.setValues(Arrays.asList(facetValue1));

		facetResultTransformer.assignLabels(Arrays.asList(facet));
		Assert.assertEquals(facetValue1.getText(), "label for 200, label for 101");
	}

	/**
	 * Tests the logic inside {@link FacetResultTransformer#assignLabels(java.util.Collection)} when the {@link Facet}
	 * is for code lists.
	 */
	@Test
	public void testAssignLabelsForCodelistFacets() {
		mockRetrieverService(FieldId.CODE_LIST, "some_id", "What a label!", 101);
		Facet facet = new Facet();
		facet.setId("myFacet");
		facet.setCodelists(Sets.newHashSet(101));

		FacetValue facetValue1 = new FacetValue();
		facetValue1.setId("some_id");

		facet.setValues(Arrays.asList(facetValue1));

		facetResultTransformer.assignLabels(Arrays.asList(facet));
		Assert.assertEquals(facetValue1.getText(), "What a label!");
	}

	/**
	 * Tests the logic inside {@link FacetResultTransformer#assignLabels(java.util.Collection)} when the {@link Facet}
	 * is for code lists but no labels are found so the label will be the id.
	 */
	@Test
	public void testAssignLabelsForCodelistFacetsNoLabelsFound() {
		// When no labels is found the service will just return the id so that's fine.
		mockRetrieverService(FieldId.CODE_LIST, "some_id", "some_id", 101);
		Facet facet = new Facet();
		facet.setId("myFacet");
		facet.setCodelists(Sets.newHashSet(101));

		FacetValue facetValue1 = new FacetValue();
		facetValue1.setId("some_id");

		facet.setValues(Arrays.asList(facetValue1));

		facetResultTransformer.assignLabels(Arrays.asList(facet));
		Assert.assertEquals(facetValue1.getText(), "some_id");
	}

	/**
	 * Tests the logic inside {@link FacetResultTransformer#assignLabels(java.util.Collection)} when the {@link Facet}
	 * is for users.
	 */
	@Test
	public void testAssignLabelsForUserFacets() {
		mockRetrieverService(FieldId.USERNAME_BY_URI, "Some uri", "What a label!", 0);
		mockTypeConverter("Some uri");

		Facet facet = new Facet();
		facet.setId("myFacet");
		facet.setRangeClass("emf:User");

		FacetValue facetValue1 = new FacetValue();
		facetValue1.setId("some_id");

		facet.setValues(Arrays.asList(facetValue1));

		facetResultTransformer.assignLabels(Arrays.asList(facet));
		Assert.assertEquals(facetValue1.getText(), "What a label!");
	}

	/**
	 * Tests the logic inside {@link FacetResultTransformer#assignLabels(java.util.Collection)} when the {@link Facet}
	 * is for object types.
	 */
	@Test
	public void testAssignLabelsForObjectTypeFacets() {
		mockRetrieverService(FieldId.OBJECT_TYPE, "some_id", "What a label!", 0);

		Facet facet = new Facet();
		facet.setId("rdfType");

		FacetValue facetValue1 = new FacetValue();
		facetValue1.setId("some_id");

		facet.setValues(Arrays.asList(facetValue1));

		facetResultTransformer.assignLabels(Arrays.asList(facet));
		Assert.assertEquals(facetValue1.getText(), "What a label!");
	}

	/**
	 * Tests the logic inside {@link FacetResultTransformer#assignLabels(java.util.Collection)} when then {@link Facet}
	 * is of unknown type.
	 */
	@Test
	public void testAssignLabelsForUnkownFacets() {
		Facet facet = new Facet();

		FacetValue facetValue1 = new FacetValue();
		facetValue1.setId("some_id");

		facet.setValues(Arrays.asList(facetValue1));

		facetResultTransformer.assignLabels(Arrays.asList(facet));
		Assert.assertEquals(facetValue1.getText(), "some_id");
	}

	/**
	 * Mocks the {@link FieldValueRetrieverService#getLabel(String, String, SearchRequest)} to expect some
	 * {@link FieldId} and a facet value id and to return the given label.
	 *
	 * @param fieldId
	 *            the expected ID
	 * @param facetValueId
	 *            the id of the facet value
	 * @param label
	 *            the label to return
	 * @param codelistId
	 *            the codelist from which the label is going to be retrieved
	 */
	private void mockRetrieverService(String fieldId, String facetValueId, String label, int codelistId) {
		if (codelistId != 0) {
			Mockito
					.when(fieldValueRetrievalService.getLabel(Matchers.eq(fieldId), Matchers.eq(facetValueId),
							argThat(CustomMatcher.of(request -> Integer.parseInt(
									request.get(FieldValueRetrieverParameters.CODE_LIST_ID).get(0)) == codelistId))))
						.thenReturn(label);
		} else {
			Mockito
					.when(fieldValueRetrievalService.getLabel(Matchers.eq(fieldId), Matchers.eq(facetValueId),
							Matchers.any(SearchRequest.class)))
						.thenReturn(label);
		}

	}

	/**
	 * Mocks the {@link LabelProvider#getValue(String)} to return the given label.
	 *
	 * @param label
	 *            - the given label
	 */
	private void mockLabelProvider(String label) {
		Mockito.when(labelProvider.getValue(Matchers.anyString())).thenReturn(label);
	}

	/**
	 * Mocks the {@link TypeConverter#convert(Class, Object)} to return the given uri.
	 *
	 * @param shortUri
	 *            - the given uri
	 */
	private void mockTypeConverter(String shortUri) {
		Mockito.when(namespaceRegistryService.getShortUri(Matchers.anyString())).thenReturn(shortUri);
	}

	/**
	 * Checks for specific {@link FacetValue} from the provided list, if it matches the provided parameters.
	 *
	 * @param values
	 *            - the provided list of values
	 * @param valueId
	 *            - the searched value ID
	 * @param count
	 *            - the search value count
	 * @return true if there is {@link FacetValue} that matches the parameters or false otherwise
	 */
	private static boolean valueExist(List<FacetValue> values, String valueId, long count) {
		for (FacetValue value : values) {
			if (valueId.equals(value.getId()) && count == value.getCount()) {
				return true;
			}
		}
		return false;
	}
}
