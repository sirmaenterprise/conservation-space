package com.sirma.itt.seip.instance.dao;

import static com.sirma.itt.seip.collections.CollectionUtils.emptySet;
import static com.sirma.itt.seip.search.NamedQueries.CHECK_EXISTING_INSTANCE;
import static com.sirma.itt.seip.testutil.CustomMatcher.ofPredicate;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyObject;
import static org.mockito.Matchers.argThat;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.CALLS_REAL_METHODS;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyZeroInteractions;
import static org.mockito.Mockito.when;

import java.io.Serializable;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Stream;

import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

import com.sirma.itt.seip.definition.DefinitionService;
import com.sirma.itt.seip.domain.definition.ControlDefinition;
import com.sirma.itt.seip.domain.definition.DefinitionModel;
import com.sirma.itt.seip.domain.definition.PropertyDefinition;
import com.sirma.itt.seip.domain.instance.Instance;
import com.sirma.itt.seip.instance.properties.RichtextPropertiesDao;
import com.sirma.itt.seip.instance.InstanceTypeResolver;
import com.sirma.itt.seip.search.NamedQueries.Params;
import com.sirma.itt.seip.search.SearchService;

/**
 * Test for {@link BaseInstanceDaoImpl}.
 *
 * @author A. Kunchev
 */
public class BaseInstanceDaoImplTest {

	private static final String INSTANCE_ID = "emf:123456";
	private static final String DEFINITION_ID = "CS0004";
	private static final Long PROPERTY_ID = Long.valueOf(56);
	private static final String FIELD_NAME = "test";
	private static final String RICHTEXT = "RICHTEXT";
	private static final String ORIGINAL_HTML_VALUE = "<b>This</b> <i>is test</i> value";
	private static final String STRIPPED_HTML_VALUE = "This is test value";

	@InjectMocks
	private InstanceDao dao;

	@Mock
	private SearchService searchService;

	@Mock
	private DefinitionService definitionService;

	@Mock
	private RichtextPropertiesDao richtextPropertiesDao;

	@Mock
	private InstanceTypeResolver typeResolver;

	@Before
	public void setup() {
		dao = mock(BaseInstanceDaoImpl.class, CALLS_REAL_METHODS);
		MockitoAnnotations.initMocks(this);
	}

	@Test
	public void exist_nullIdentifiers() {
		Map<Serializable, Boolean> existing = dao.exist(null, false);
		assertTrue(existing.isEmpty());
		verifyZeroInteractions(searchService);
	}

	@Test
	public void exist_emptyIdentifiers() {
		Map<Serializable, Boolean> existing = dao.exist(emptySet(), false);
		assertTrue(existing.isEmpty());
		verifyZeroInteractions(searchService);
	}

	@Test
	public void exist_ShouldCallSemanticSearchOnIncludeDeletedIsTrue() {
		Collection<String> ids = Arrays.asList("instance-id-1", "instance-id-2");
		when(searchService.stream(any(), any())).thenReturn(Stream.empty());

		Map<String, Boolean> existing = dao.exist(ids, true);
		assertFalse(existing.isEmpty());
		verify(searchService).getFilter(eq(CHECK_EXISTING_INSTANCE), eq(Instance.class),
				argThat(ofPredicate(context -> ids.equals(context.getIfSameType(Params.URIS, Collection.class))
						&& context.getIfSameType(Params.INCLUDE_DELETED, Boolean.class))));
		verify(searchService).stream(any(), any());
	}

	@Test
	public void exist_ShouldCallInstanceTypeResolverOnIncludeDeletedIsFalse() {
		Collection<String> ids = Arrays.asList("instance-id-1", "instance-id-2");
		when(typeResolver.exist(ids)).thenReturn(new HashMap<>());

		dao.exist(ids, false);
		verify(typeResolver).exist(ids);
	}

	@Test
	public void exist_duplicateIdentifiers_shouldReturnDistinctResults() {
		Collection<String> ids = Arrays.asList("instance-id-1", "instance-id-1");
		when(searchService.stream(any(), any())).thenReturn(Stream.empty());

		Map<String, Boolean> existing = dao.exist(ids, true);
		assertEquals(1, existing.size());
	}

	@Test
	public void should_restore_richText_values() {

		Instance instance = prepareMocks();
		((BaseInstanceDaoImpl) dao).restoreRichTextFields(instance);

		ArgumentCaptor<Map> value = ArgumentCaptor.forClass(Map.class);
		verify(instance).addAllProperties(value.capture());

		Map<String, Serializable> expected = new HashMap<>();
		expected.put(FIELD_NAME, ORIGINAL_HTML_VALUE);
		assertEquals(expected, value.getValue());
	}

	@Test
	public void should_strip_richText_values() {

		Instance instance = prepareMocks();
		((BaseInstanceDaoImpl) dao).stripRichTextFields(instance);

		ArgumentCaptor<String> updatedProperty = ArgumentCaptor.forClass(String.class);
		ArgumentCaptor<String> value = ArgumentCaptor.forClass(String.class);
		verify(instance).add(updatedProperty.capture(), value.capture());

		assertEquals(FIELD_NAME, updatedProperty.getValue());
		assertEquals(STRIPPED_HTML_VALUE, value.getValue());
	}

	private Instance prepareMocks() {

		Instance instance = Mockito.mock(Instance.class);
		Mockito.when(instance.getId()).thenReturn(INSTANCE_ID);
		Mockito.when(instance.getIdentifier()).thenReturn(DEFINITION_ID);

		DefinitionModel instanceDefinition = Mockito.mock(DefinitionModel.class);
		Mockito.when(definitionService.getInstanceDefinition(instance)).thenReturn(instanceDefinition);

		ControlDefinition controlDefinition = Mockito.mock(ControlDefinition.class);
		Mockito.when(controlDefinition.getIdentifier()).thenReturn(RICHTEXT);

		PropertyDefinition property = Mockito.mock(PropertyDefinition.class);
		Map<String, Serializable> properties = new HashMap<>();
		properties.put(FIELD_NAME, STRIPPED_HTML_VALUE);
		Mockito.when(instance.getProperties()).thenReturn(properties);

		Mockito.when(instanceDefinition.getField(anyObject())).thenReturn(Optional.of(property));
		Mockito.when(property.getId()).thenReturn(PROPERTY_ID);
		Mockito.when(property.getControlDefinition()).thenReturn(controlDefinition);

		Map<String, Serializable> richtextProperties = new HashMap<>();
		richtextProperties.put(FIELD_NAME, ORIGINAL_HTML_VALUE);
		Mockito.when(richtextPropertiesDao.fetchByInstanceId(INSTANCE_ID)).thenReturn(richtextProperties);

		return instance;
	}

}
