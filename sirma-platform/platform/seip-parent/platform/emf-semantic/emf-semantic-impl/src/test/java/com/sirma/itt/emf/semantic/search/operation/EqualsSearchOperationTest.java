package com.sirma.itt.emf.semantic.search.operation;

import com.sirma.itt.emf.semantic.NamespaceRegistry;
import com.sirma.itt.seip.domain.search.tree.Rule;
import com.sirma.itt.seip.testutil.search.SearchOperationUtils;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Tests the query building in {@link EqualsSearchOperation}.
 *
 * @author Mihail Radkov
 */
public class EqualsSearchOperationTest {

	@Mock
	private NamespaceRegistry nameSpaceRegistry;

	@InjectMocks
	private EqualsSearchOperation equalsSearchOperation;

	@Before
	public void initialize() {
		equalsSearchOperation = new EqualsSearchOperation();
		MockitoAnnotations.initMocks(this);
	}

	@Test
	public void testIsApplicableString() {
		Rule rule = SearchOperationUtils.createRule("a", "string", "equals", "test");
		Assert.assertTrue(equalsSearchOperation.isApplicable(rule));

		rule = SearchOperationUtils.createRule("a", "string", "", "test");
		Assert.assertFalse(equalsSearchOperation.isApplicable(rule));
	}

	@Test
	public void testIsApplicableNumeric() {
		Rule rule = SearchOperationUtils.createRule("a", "numeric", "equals", "32");
		Assert.assertTrue(equalsSearchOperation.isApplicable(rule));

		rule = SearchOperationUtils.createRule("a", "numeric", "", "test");
		Assert.assertFalse(equalsSearchOperation.isApplicable(rule));
	}

	@Test
	public void testBuildOperationForString() {
		Rule rule = SearchOperationUtils.createRule("a", "string", "equals", "test");
		StringBuilder builder = new StringBuilder();

		equalsSearchOperation.buildOperation(builder, rule);

		String query = builder.toString().replaceAll("\\?v.{32}", "VAR");
		String expected = " ?instance a VAR.  FILTER ( regex(lcase(str(VAR)), \"^\\\\Qtest\\\\E$\", \"i\")) ";
		Assert.assertEquals(expected, query);
	}

	@Test
	public void testBuildOperationForDate() {
		Rule rule = SearchOperationUtils.createRule("a", "dateTime", "equals", "test");
		StringBuilder builder = new StringBuilder();

		equalsSearchOperation.buildOperation(builder, rule);

		String expected = " ?instance a \"test\"^^xsd:dateTime. ";
		Assert.assertEquals(expected, builder.toString());
	}

	@Test
	public void testBuildOperationForUri() {
		final String SHORT_URI = "emf:shortUri";
		final String FULL_URI = "http://shortUri";

		Mockito.when(nameSpaceRegistry.buildFullUri(SHORT_URI)).thenReturn(FULL_URI);

		Rule rule = SearchOperationUtils.createRule("rdf:type", "", "equals", SHORT_URI);
		StringBuilder builder = new StringBuilder();

		equalsSearchOperation.buildOperation(builder, rule);

		String expected = " ?instance rdf:type <" + FULL_URI + ">. ";
		Assert.assertEquals(expected, builder.toString());
	}

	@Test
	public void testBuildOperationForUriWithSemanticAndDefinitionIds() {
		String uri = "http:uri";
		String secondUri = "http:seconduri";
		Rule rule = SearchOperationUtils.createRule("rdf:type", "", "equals", Arrays.asList(uri, secondUri, "DT20001"));
		StringBuilder builder = new StringBuilder();

		Mockito.when(nameSpaceRegistry.buildFullUri(uri)).thenReturn(uri);
		Mockito.when(nameSpaceRegistry.buildFullUri(secondUri)).thenReturn(secondUri);

		equalsSearchOperation.buildOperation(builder, rule);
		String expr = "\\{\\{ \\?instance rdf:type <http:uri>\\. \\}"
				+ " UNION \\{ \\?instance rdf:type <http:seconduri>\\. \\}\\}" + " UNION \\{ \\?instance emf:type .* "
				+ " FILTER \\( regex\\(lcase\\(str\\(.*\\)\\), \"\\^.*Qdt20001.*E\\$\", \"i\"\\)\\) \\}";
		Pattern pattern = Pattern.compile(expr);
		Matcher matcher = pattern.matcher(builder.toString());
		Assert.assertTrue("The returned result didn't match the expression ", matcher.matches());
	}

	@Test
	public void testBuildOperationForNumeric() {
		Rule rule = SearchOperationUtils.createRule("a", "numeric", "equals", "32");
		StringBuilder builder = new StringBuilder();

		equalsSearchOperation.buildOperation(builder, rule);
		String query = builder.toString().replaceAll("\\?v.{32}", "VAR");
		Assert.assertEquals(" ?instance a VAR.  FILTER ( VAR = 32 ) ", query);
	}

	@Test
	public void testBuildOperationForEmptyValues() {
		Rule rule = SearchOperationUtils.createRule("a", "", "equals", Collections.emptyList());
		StringBuilder builder = new StringBuilder();

		equalsSearchOperation.buildOperation(builder, rule);

		Assert.assertEquals("", builder.toString());
	}

	@Test
	public void testBuildOperationForEmptyUri() {
		Rule rule = SearchOperationUtils.createRule("a", "", "equals", new ArrayList<>());
		StringBuilder builder = new StringBuilder();

		equalsSearchOperation.buildOperation(builder, rule);

		Assert.assertEquals("", builder.toString());
	}

	@Test
	public void testBuildOperationForEmptyDate() {
		Rule rule = SearchOperationUtils.createRule("a", "dateTime", "equals", "");
		StringBuilder builder = new StringBuilder();

		equalsSearchOperation.buildOperation(builder, rule);

		Assert.assertEquals("", builder.toString());
	}

	@Test
	public void testBuildOperationForEmptyNumeric() {
		Rule rule = SearchOperationUtils.createRule("a", "numeric", "equals");
		StringBuilder builder = new StringBuilder();

		equalsSearchOperation.buildOperation(builder, rule);
		Assert.assertEquals("", builder.toString());
	}
}
