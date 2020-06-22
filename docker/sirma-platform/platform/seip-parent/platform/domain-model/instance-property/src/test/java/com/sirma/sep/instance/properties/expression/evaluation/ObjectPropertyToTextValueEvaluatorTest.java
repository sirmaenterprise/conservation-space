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
 * Tests for {@link ObjectPropertyToTextValueEvaluator}.
 *
 * @author <a href="mailto:ivo.rusev@sirma.bg">Ivo Rusev</a>
 * @since 17/07/2017
 */
public class ObjectPropertyToTextValueEvaluatorTest {

	@InjectMocks
	private ObjectPropertyToTextValueEvaluator cut;

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
		cut = new ObjectPropertyToTextValueEvaluator();
		MockitoAnnotations.initMocks(this);

		when(source.getDataType()).thenReturn(dataTypeDefinition);
		when(dataTypeDefinition.getName()).thenReturn(DataTypeDefinition.URI);
		when(destination.getDataType()).thenReturn(destinationTypeDefinition);
		when(destinationTypeDefinition.getName()).thenReturn(DataTypeDefinition.TEXT);

		when(headersService.generateInstanceHeader(any(), any())).thenReturn(
				"<a href=\"www.something.com\">some-text</a> ");
	}

	@Test
	public void test_evaluate_singleValue() {
		Instance objectProperty = mock(Instance.class);
		List<Instance> instances = Collections.singletonList(objectProperty);
		when(instanceService.loadInstances(any(Collection.class))).thenReturn(instances);
		when(instance.get("fieldName")).thenReturn("emf:123");

		Serializable fieldName = cut.evaluate(instance, "fieldName");
		// verify called services.
		verify(headersService).generateInstanceHeader(any(), any());
		verify(instanceService).loadInstances(any());
		// verify the result
		assertEquals("some-text", fieldName);
	}

	@Test
	public void test_getInstances_singleValue() {
		when(instance.get("fieldName")).thenReturn("emf:123");
		cut.getInstances(instance, "fieldName", instanceService);
		verify(instanceService).loadInstances(Collections.singleton("emf:123"));
	}

	@Test
	public void test_evaluate_multipleValues() {
		Instance objectPropertyOne = mock(Instance.class);
		Instance objectPropertyTwo = mock(Instance.class);
		List<Instance> instances = Arrays.asList(objectPropertyOne, objectPropertyTwo);
		when(instanceService.loadInstances(any(Collection.class))).thenReturn(instances);

		Collection ids = Arrays.asList("", "");
		when(instance.get("fieldName")).thenReturn((Serializable) ids);
		Serializable fieldName = cut.evaluate(instance, "fieldName");
		// verify called services.
		verify(headersService, times(2)).generateInstanceHeader(any(), any());
		verify(instanceService).loadInstances(any());
		// verify the result
		assertEquals("some-text, some-text", fieldName);
	}

	@Test
	public void test_getInstances_multipleValues() {
		Collection ids = Arrays.asList("emf:123", "emf:456");
		when(instance.get("fieldName")).thenReturn((Serializable) ids);
		cut.getInstances(instance, "fieldName", instanceService);
		verify(instanceService).loadInstances(ids);
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