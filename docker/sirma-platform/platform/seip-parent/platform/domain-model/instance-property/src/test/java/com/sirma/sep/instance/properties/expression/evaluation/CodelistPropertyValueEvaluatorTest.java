package com.sirma.sep.instance.properties.expression.evaluation;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.Serializable;
import java.util.Arrays;

import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

import com.sirma.itt.seip.definition.DefinitionService;
import com.sirma.itt.seip.domain.codelist.CodelistService;
import com.sirma.itt.seip.domain.definition.PropertyDefinition;
import com.sirma.itt.seip.domain.instance.Instance;
import com.sirma.sep.instance.properties.expression.evaluation.CodelistPropertyValueEvaluator;

/**
 * Test for {@link CodelistPropertyValueEvaluator}.
 *
 * @author <a href="mailto:ivo.rusev@sirma.bg">Ivo Rusev</a>
 * @since 17/07/2017
 */
public class CodelistPropertyValueEvaluatorTest {

	@InjectMocks
	private CodelistPropertyValueEvaluator evaluator;

	@Mock
	private PropertyDefinition property;

	@Mock
	private CodelistService cls;

	@Mock
	private DefinitionService definitionService;

	@Mock
	private Instance instance;

	@Before
	public void init() {
		evaluator = new CodelistPropertyValueEvaluator();
		MockitoAnnotations.initMocks(this);

		when(property.getCodelist()).thenReturn(200);
		when(cls.getDescription(any(), any())).thenReturn("cls-description");
		when(instance.get("fieldName")).thenReturn("fieldValue");
	}

	@Test
	public void should_ReturnNull_When_DefinitionPropertyIsNotSet() {
		assertNull(evaluator.evaluate(instance, "fieldName"));
	}

	@Test
	public void should_ReturnNull_When_PropertyIsNotSet() {
		assertNull(evaluator.evaluate(Mockito.mock(Instance.class), "fieldName"));
	}

	@Test
	public void evaluate_nullPropertyDefinition() {
		when(definitionService.getProperty("property-name", instance)).thenReturn(null);
		Serializable result = evaluator.evaluate(instance, "property-name");
		assertNull(result);
	}

	@Test
	public void evaluate_integerPropertyValue() {
		when(instance.get("property-name")).thenReturn(1);
		when(definitionService.getProperty("property-name", instance)).thenReturn(property);
		Serializable result = evaluator.evaluate(instance, "property-name");
		assertNull(result);
	}

	@Test
	public void evaluate_multivalue() {
		when(instance.get("multi-value-field-name"))
				.thenReturn((Serializable) Arrays.asList("cl-value-1", "cl-value-2", "cl-value-3"));
		when(cls.getDescription(eq(200), eq("cl-value-1"))).thenReturn("cl-description-1");
		when(cls.getDescription(eq(200), eq("cl-value-2"))).thenReturn("cl-description-2");
		when(cls.getDescription(eq(200), eq("cl-value-3"))).thenReturn("cl-description-3");
		when(definitionService.getProperty("multi-value-field-name", instance)).thenReturn(property);
		Serializable result = evaluator.evaluate(instance, "multi-value-field-name");
		assertEquals("cl-description-1, cl-description-2, cl-description-3", result);
	}

	@Test
	public void evaluate_singleValue() {
		when(definitionService.getProperty("fieldName", instance)).thenReturn(property);
		Serializable fieldName = evaluator.evaluate(instance, "fieldName");
		assertEquals("cls-description", fieldName);
		verify(definitionService).getProperty("fieldName", instance);
		verify(cls).getDescription(200, "fieldValue");
	}

	@Test
	public void canEvaluate_success() {
		assertTrue(evaluator.canEvaluate(property, null));
	}

	@Test
	public void canEvaluate_fail() {
		when(property.getCodelist()).thenReturn(-1);
		assertFalse(evaluator.canEvaluate(property, null));
		when(property.getCodelist()).thenReturn(null);
		assertFalse(evaluator.canEvaluate(property, null));
	}
}