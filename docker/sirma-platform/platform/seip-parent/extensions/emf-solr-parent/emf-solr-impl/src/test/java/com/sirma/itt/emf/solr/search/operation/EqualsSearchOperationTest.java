package com.sirma.itt.emf.solr.search.operation;

import com.sirma.itt.seip.ShortUri;
import com.sirma.itt.seip.convert.TypeConverter;
import com.sirma.itt.seip.definition.SemanticDefinitionService;
import com.sirma.itt.seip.domain.instance.ClassInstance;
import com.sirma.itt.seip.domain.search.tree.CriteriaWildcards;
import com.sirma.itt.seip.domain.search.tree.Rule;
import com.sirma.itt.seip.testutil.search.SearchOperationUtils;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Matchers;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Test class for {@link EqualsSearchOperation}
 *
 * @author Hristo Lungov
 */
public class EqualsSearchOperationTest {

	@InjectMocks
	private EqualsSearchOperation equalsSearchOperation;

	@Mock
	private TypeConverter typeConverter;

	@Mock
	private SemanticDefinitionService semanticDefinitionService;

	@Before
	public void initialize() {
		MockitoAnnotations.initMocks(this);
		Mockito.when(semanticDefinitionService.collectSubclasses(Matchers.any())).thenReturn(Collections.emptySet());
	}

	@Test
	public void test_IsApplicableString() {
		Rule rule = SearchOperationUtils.createRule("a", "string", "equals", "test");
		Assert.assertTrue(equalsSearchOperation.isApplicable(rule));

		rule = SearchOperationUtils.createRule("a", "string", "", "test");
		Assert.assertFalse(equalsSearchOperation.isApplicable(rule));
	}

	@Test
	public void test_BuildOperationForString() {
		Rule rule = SearchOperationUtils.createRule("a", "string", "equals", "test");
		StringBuilder builder = new StringBuilder();

		equalsSearchOperation.buildOperation(builder, rule);

		Assert.assertEquals("(a:(test))", builder.toString());
	}

	@Test
	public void test_BuildOperationForDate() {
		Rule rule = SearchOperationUtils.createRule("a", "dateTime", "equals", "test");
		StringBuilder builder = new StringBuilder();

		equalsSearchOperation.buildOperation(builder, rule);

		Assert.assertEquals("(a:(test))", builder.toString());
	}

	@Test
	public void test_BuildOperationForUri() {
		final String SHORT_URI = "emf:shortUri";
		final String FULL_URI = "http://shortUri";
		mockClassInstance(FULL_URI);
		mockTypeConverter(FULL_URI, SHORT_URI);

		Rule rule = SearchOperationUtils.createRule("types", "", "equals", FULL_URI);
		StringBuilder builder = new StringBuilder();

		equalsSearchOperation.buildOperation(builder, rule);
		Assert.assertEquals("(objecttype:(\"emf:shortUri\"))", builder.toString());
	}

	@Test
	public void test_BuildOperationForUriWithSemanticAndDefinitionIds() {
		String uri = "http:uri";
		String secondUri = "http:seconduri";
		mockClassInstance(uri);
		mockClassInstance(secondUri);
		mockTypeConverter(uri, "emf:uri");
		mockTypeConverter(secondUri, "emf:seconduri");

		Rule rule = SearchOperationUtils.createRule("types", "", "equals", Arrays.asList(uri, secondUri, "DT20001"));
		StringBuilder builder = new StringBuilder();

		equalsSearchOperation.buildOperation(builder, rule);
		Assert.assertEquals(
				"(objecttype:(\"emf:uri\") OR objecttype:(\"emf:seconduri\") OR objectsubtype:(\"DT20001\"))",
				builder.toString());
	}

	@Test
	public void test_BuildOperationForAnyObject() {
		Rule rule = SearchOperationUtils.createRule("types", "", "equals", CriteriaWildcards.ANY_OBJECT);
		StringBuilder builder = new StringBuilder();

		equalsSearchOperation.buildOperation(builder, rule);

		Assert.assertEquals("*:*", builder.toString());
	}

	@Test
	public void test_buildOperationWithSemanticSubclasses() {
		String uri = "http:uri";
		String secondUri = "http:seconduri";
		String firstSubClass = "http:first-sub-class";
		String secondSubClass = "http:second-sub-class";
		mockClassInstance(uri);
		mockClassInstance(secondUri);
		mockTypeConverter(uri, "emf:uri");
		mockTypeConverter(secondUri, "emf:seconduri");
		mockTypeConverter(firstSubClass, "emf:first-sub-class");
		mockTypeConverter(secondSubClass, "emf:second-sub-class");
		mockCollectSubClasses(uri, Arrays.asList(firstSubClass, secondSubClass));

		Rule rule = SearchOperationUtils.createRule("types", "", "equals", Arrays.asList(uri, secondUri));
		StringBuilder builder = new StringBuilder();

		equalsSearchOperation.buildOperation(builder, rule);
		Assert.assertEquals(
				"(objecttype:(\"emf:uri\") OR objecttype:(\"emf:first-sub-class\") OR objecttype:(\"emf:second-sub-class\") OR objecttype:(\"emf:seconduri\"))",
				builder.toString());
	}

	private void mockClassInstance(String uri) {
		ClassInstance classInstance = getClassInstance(uri);
		Mockito.when(semanticDefinitionService.getClassInstance(uri)).thenReturn(classInstance);
	}

	private void mockCollectSubClasses(String uri, List<String> subClassIds) {
		Set<ClassInstance> subClassInstances = subClassIds.stream()
				.map(this::getClassInstance)
				.collect(Collectors.toSet());
		Mockito.when(semanticDefinitionService.collectSubclasses(uri)).thenReturn(subClassInstances);
	}

	private ClassInstance getClassInstance(String uri) {
		ClassInstance classInstance = new ClassInstance();
		classInstance.setId(uri);
		return classInstance;
	}

	private void mockTypeConverter(String fullUri, String shortUri) {
		Mockito.when(typeConverter.convert(ShortUri.class, fullUri)).thenReturn(new ShortUri(shortUri));
	}

}
