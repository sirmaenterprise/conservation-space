package com.sirma.itt.seip.instance.relation;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import com.sirma.itt.seip.definition.SemanticDefinitionService;
import com.sirma.itt.seip.domain.instance.PropertyInstance;
import com.sirma.itt.seip.testutil.EmfTest;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

public class RelationshipsRestServiceTest extends EmfTest {
	@InjectMocks
	private RelationshipsRestService rest;
	@Mock
	private SemanticDefinitionService semanticDefinitionService;

	@Before
	public void setUp() {
		rest = new RelationshipsRestService();
		MockitoAnnotations.initMocks(this);
		List<PropertyInstance> values = new ArrayList<>();
		values.add(createRelation("Test"));
		values.add(createRelation("Hello"));
		values.add(createRelation("Bonjour!"));
		Mockito.when(semanticDefinitionService.getSearchableRelations()).thenReturn(values);
	}

	@Test
	public void should_ReturnRelationIfo_When_RelationIsNotFound() {
		Mockito.when(semanticDefinitionService.getRelation("1232131")).thenReturn(createRelation("Hello"));
		assertEquals("{\"id\":\"1232131\",\"title\":\"Hello\"}", rest.retrieveRelationship("1232131"));
	}

	@Test
	public void should_ReturnEmptyJson_When_RelationIsNotFound() {
		assertEquals("{}", rest.retrieveRelationship("nonExistId"));
	}

	@Test
	public void clientQueryFilteringTest() {
		String actual = rest.retrieveRelationships(new ArrayList(), new ArrayList(), new ArrayList(), "test");
		assertEquals("Test", getTitles(actual));
	}

	@Test
	public void noClientQueryTest() {
		String actual = rest.retrieveRelationships(new ArrayList(), new ArrayList(), new ArrayList(), "");
		assertEquals("TestHelloBonjour!", getTitles(actual));
	}

	@Test
	public void invalidQueryTest() {
		String actual = rest.retrieveRelationships(new ArrayList(), new ArrayList(), new ArrayList(), "some-string");
		assertEquals("", getTitles(actual));
	}

	@SuppressWarnings("boxing")
	private static PropertyInstance createRelation(String title) {
		PropertyInstance testInstance = new PropertyInstance();
		testInstance.setLabel("en", title);
		testInstance.setId(1232131L);
		testInstance.setProperties(new HashMap<>());
		testInstance.setRangeClass("testRangeClass");
		return testInstance;
	}

	private String getTitles(String array) {
		StringBuilder titles = new StringBuilder();
		try {
			JSONArray results = new JSONArray(array);
			int arrayLen = results.length();
			for (int i = 0; i < arrayLen; i++) {
				JSONObject result = results.getJSONObject(i);
				titles.append(result.getString("title"));
			}
		} catch (JSONException e) {
			fail("Error getting titles");
		}
		return titles.toString();
	}
}