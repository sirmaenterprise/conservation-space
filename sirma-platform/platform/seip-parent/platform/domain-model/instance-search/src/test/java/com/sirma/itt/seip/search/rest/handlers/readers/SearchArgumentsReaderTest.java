package com.sirma.itt.seip.search.rest.handlers.readers;

import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.ParameterizedType;
import java.util.Arrays;
import java.util.List;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.UriInfo;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.mockito.Spy;

import com.sirma.itt.seip.domain.instance.Instance;
import com.sirma.itt.seip.domain.search.SearchArguments;
import com.sirma.itt.seip.domain.search.SearchRequest;
import com.sirma.itt.seip.domain.search.tree.Condition;
import com.sirma.itt.seip.domain.search.tree.Condition.Junction;
import com.sirma.itt.seip.domain.search.tree.Rule;
import com.sirma.itt.seip.domain.search.tree.SearchNode;
import com.sirma.itt.seip.domain.search.tree.SearchNode.NodeType;
import com.sirma.itt.seip.rest.utils.request.RequestInfo;
import com.sirma.itt.seip.search.SearchService;
import com.sirma.itt.seip.search.converters.JsonToConditionConverter;

/**
 * Unit tests for {@link SearchArgumentsReader}.
 *
 * @author yasko
 */
public class SearchArgumentsReaderTest {

	@Mock
	private MultivaluedMap<String, String> map;

	@Mock
	private UriInfo uriInfo;

	@Mock
	private HttpHeaders headers;

	@Mock
	private RequestInfo requestInfo;

	@Mock
	private SearchService searchService;

	@Spy
	private JsonToConditionConverter converter = new JsonToConditionConverter();

	@InjectMocks
	private SearchArgumentsReader reader = new SearchArgumentsReader();

	@Before
	public void init() {
		MockitoAnnotations.initMocks(this);

		Mockito.when(map.size()).thenReturn(0);
		Mockito.when(uriInfo.getQueryParameters()).thenReturn(map);
		Mockito.when(requestInfo.getUriInfo()).thenReturn(uriInfo);
	}

	@Test
	public void testIsReadable() {
		ParameterizedType generic = Mockito.mock(ParameterizedType.class);
		Mockito.when(generic.getActualTypeArguments()).thenReturn(new Class[] { Instance.class });
		Assert.assertTrue(reader.isReadable(SearchArguments.class, generic, null, null));

		Mockito.when(generic.getActualTypeArguments()).thenReturn(new Class[] { String.class });
		Assert.assertFalse(reader.isReadable(SearchArguments.class, generic, null, null));

		Mockito.when(generic.getActualTypeArguments()).thenReturn(new Class[] {});
		Assert.assertFalse(reader.isReadable(SearchArguments.class, generic, null, null));

		Mockito.when(generic.getActualTypeArguments()).thenReturn(null);
		Assert.assertFalse(reader.isReadable(SearchArguments.class, generic, null, null));

		Mockito.when(generic.getActualTypeArguments()).thenReturn(new Class[] { Instance.class });
		Assert.assertFalse(reader.isReadable(String.class, generic, null, null));
	}

	@Test
	public void testReadStringFromSingleCondRule() throws WebApplicationException, IOException {
		Condition tree = readFromCondRuleFile("tree-single-cond-rule.json");
		Assert.assertNotNull(tree);
		Assert.assertEquals(Junction.AND, tree.getCondition());

		List<SearchNode> rules = tree.getRules();
		Assert.assertEquals(1, rules.size());
		assertRule((Rule) rules.get(0), "rdf:type", "in", "string", Arrays.asList("emf:Test"));
	}

	@Test
	public void testReadNumericFromSingleCondRule() throws WebApplicationException, IOException {
		Condition tree = readFromCondRuleFile("tree-single-cond-rule-numeric.json");

		Assert.assertNotNull(tree);
		Assert.assertEquals(Junction.AND, tree.getCondition());

		List<SearchNode> rules = tree.getRules();
		Assert.assertEquals(1, rules.size());

		assertRule((Rule) rules.get(0), "rdf:type", "equals", "numeric", Arrays.asList("123"));
	}

	@Test
	public void testReadStringFromNestedCond() throws WebApplicationException, IOException {
		Condition tree = readFromCondRuleFile("tree-nested-cond.json");
		Assert.assertNotNull(tree);
		Assert.assertEquals(Junction.AND, tree.getCondition());

		List<SearchNode> rules = tree.getRules();
		Assert.assertEquals(1, rules.size());

		SearchNode node = rules.get(0);
		Assert.assertEquals(NodeType.CONDITION, node.getNodeType());

		Condition nested = (Condition) node;
		Assert.assertEquals(Junction.OR, nested.getCondition());

		rules = nested.getRules();
		Assert.assertEquals(1, rules.size());
		assertRule((Rule) rules.get(0), "dcterms:title", "contains", "string", Arrays.asList("test"));
	}

	@Test
	public void testReadNumericFromNestedCond() throws WebApplicationException, IOException {
		Condition tree = readFromCondRuleFile("tree-nested-cond-numeric.json");
		Assert.assertNotNull(tree);
		Assert.assertEquals(Junction.AND, tree.getCondition());

		List<SearchNode> rules = tree.getRules();
		Assert.assertEquals(1, rules.size());

		SearchNode node = rules.get(0);
		Assert.assertEquals(NodeType.CONDITION, node.getNodeType());

		Condition nested = (Condition) node;
		Assert.assertEquals(Junction.OR, nested.getCondition());

		rules = nested.getRules();
		Assert.assertEquals(1, rules.size());
		assertRule((Rule) rules.get(0), "emf:numeric", "greater_than", "numeric", Arrays.asList("123"));
	}

	@Test
	public void testReadStringFromNoRules() throws WebApplicationException, IOException {
		Condition tree = readFromCondRuleFile("tree-no-rules.json");
		Assert.assertNotNull(tree);
		Assert.assertEquals(Junction.AND, tree.getCondition());

		List<SearchNode> rules = tree.getRules();
		Assert.assertNotNull(rules);
		Assert.assertEquals(0, rules.size());
	}

	@Test
	public void testReadNumericFromNoRules() throws WebApplicationException, IOException {
		Condition tree = readFromCondRuleFile("tree-no-rules-numeric.json");
		Assert.assertNotNull(tree);
		Assert.assertEquals(Junction.AND, tree.getCondition());

		List<SearchNode> rules = tree.getRules();
		Assert.assertNotNull(rules);
		Assert.assertEquals(0, rules.size());
	}

	@Test
	public void testStringRemoveNoValueRules() throws IOException {
		Condition tree = readFromCondRuleFile("tree-no-value-rule.json");
		Assert.assertNotNull(tree);
		Assert.assertEquals(Junction.AND, tree.getCondition());

		List<SearchNode> rules = tree.getRules();
		Assert.assertNotNull(rules);
		Assert.assertEquals(1, rules.size());
	}

	@Test
	public void testNumericRemoveNoValueRules() throws IOException {
		Condition tree = readFromCondRuleFile("tree-no-value-rule-numeric.json");
		Assert.assertNotNull(tree);
		Assert.assertEquals(Junction.AND, tree.getCondition());

		List<SearchNode> rules = tree.getRules();
		Assert.assertNotNull(rules);
		Assert.assertEquals(1, rules.size());
	}

	private SearchRequest verifySearchService() {
		ArgumentCaptor<SearchRequest> argCaptor = ArgumentCaptor.forClass(SearchRequest.class);
		Mockito.verify(searchService).parseRequest(argCaptor.capture());
		return argCaptor.getValue();
	}

	private static void assertRule(Rule rule, String field, String op, String type, Object value) {
		Assert.assertEquals(field, rule.getField());
		Assert.assertEquals(op, rule.getOperation());
		Assert.assertEquals(type, rule.getType());
		Assert.assertEquals(value, rule.getValues());
	}

	private static InputStream getTestStream(String file) {
		return SearchArgumentsReaderTest.class.getResourceAsStream(file);
	}

	private Condition readFromCondRuleFile(String fileName) throws WebApplicationException, IOException {
		InputStream in = getTestStream(fileName);

		reader.readFrom(null, null, null, null, map, in);
		SearchRequest request = verifySearchService();
		return request.getSearchTree();
	}
}
