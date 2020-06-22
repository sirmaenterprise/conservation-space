package com.sirma.sep.instance.properties.expression.evaluation;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.Serializable;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import com.sirma.itt.seip.domain.definition.DataTypeDefinition;
import com.sirma.itt.seip.domain.definition.PropertyDefinition;
import com.sirma.itt.seip.domain.instance.Instance;
import com.sirma.itt.seip.instance.HeadersService;
import com.sirma.itt.seip.instance.DomainInstanceService;

/**
 * Tests for {@link ObjectPropertyToObjectValueEvaluator}.
 *
 * @author Stella D
 */
public class ObjectPropertyToObjectValueEvaluatorTest {

	@InjectMocks
	private ObjectPropertyToObjectValueEvaluator cut;

	@Mock
	private PropertyDefinition source;

	@Mock
	private PropertyDefinition destination;

	@Mock
	private DataTypeDefinition dataTypeDefinition;

	@Mock
	private DataTypeDefinition destinationTypeDefinition;

	@Mock
	private DomainInstanceService instanceService;

	@Mock
	private Instance instance;

	@Mock
	private HeadersService headersService;

	@Before
	public void init() {
		cut = new ObjectPropertyToObjectValueEvaluator();
		MockitoAnnotations.initMocks(this);

		when(source.getDataType()).thenReturn(dataTypeDefinition);
		when(dataTypeDefinition.getName()).thenReturn(DataTypeDefinition.URI);
		when(destination.getDataType()).thenReturn(destinationTypeDefinition);
		when(destinationTypeDefinition.getName()).thenReturn(DataTypeDefinition.URI);

		when(headersService.generateInstanceHeader(any(), any()))
				.thenReturn("<a href=\"www.something.com\">some-text</a> ");
	}

	@Test
	public void test_evaluate_singleValue() {
		Instance objectProperty = mock(Instance.class);
		when(objectProperty.getId()).thenReturn("emf:123456");
		List<Instance> instances = Collections.singletonList(objectProperty);
		when(instanceService.loadInstances(any(Collection.class))).thenReturn(instances);
		when(instance.get("fieldName")).thenReturn("emf:123");

		Serializable fieldName = cut.evaluate(instance, "fieldName");
		// verify called services.
		verify(headersService).generateInstanceHeader(any(), any());
		verify(instanceService).loadInstances(any());
		// verify the result
		assertEquals(
				"[{\"id\":\"emf:123456\",\"headers\":{\"compact_header\":\"<a href=\\\"www.something.com\\\">some-text</a> \"}}]",
				fieldName);
	}

	@Test
	public void test_evaluate_multipleValues() {
		Instance objectPropertyOne = mock(Instance.class);
		Instance objectPropertyTwo = mock(Instance.class);
		when(objectPropertyOne.getId()).thenReturn("emf:123456");
		when(objectPropertyTwo.getId()).thenReturn("emf:654321");
		List<Instance> instances = Arrays.asList(objectPropertyOne, objectPropertyTwo);
		when(instanceService.loadInstances(any(Collection.class))).thenReturn(instances);

		Collection ids = Arrays.asList("", "");
		when(instance.get("fieldName")).thenReturn((Serializable) ids);
		Serializable fieldName = cut.evaluate(instance, "fieldName");
		// verify called services.
		verify(headersService, times(2)).generateInstanceHeader(any(), any());
		verify(instanceService).loadInstances(any());
		// verify the result
		assertEquals(
				"[{\"id\":\"emf:123456\",\"headers\":{\"compact_header\":\"<a href=\\\"www.something.com\\\">some-text</a> \"}},{\"id\":\"emf:654321\",\"headers\":{\"compact_header\":\"<a href=\\\"www.something.com\\\">some-text</a> \"}}]",
				fieldName);
	}

	@Test
	public void canEvaluate_success() {
		assertTrue(cut.canEvaluate(source, destination));
	}

	@Test
	public void canEvaluate_fail() {
		when(dataTypeDefinition.getName()).thenReturn(DataTypeDefinition.BOOLEAN);
		assertFalse(cut.canEvaluate(source, null));
	}
}