package com.sirma.sep.instance.properties.expression.evaluation;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.when;

import java.io.Serializable;

import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import com.sirma.itt.seip.domain.definition.DataTypeDefinition;
import com.sirma.itt.seip.domain.definition.PropertyDefinition;
import com.sirma.itt.seip.domain.instance.Instance;
import com.sirma.sep.instance.properties.expression.evaluation.NumericPropertyValueEvaluator;
import com.sirma.sep.instance.properties.expression.evaluation.TextPropertyValueEvaluator;

/**
 * Test for {@link TextPropertyValueEvaluator}.
 *
 * @author <a href="mailto:ivo.rusev@sirma.bg">Ivo Rusev</a>
 * @since 17/07/2017
 */
public class NumericPropertyValueEvaluatorTest {

	@InjectMocks
	private NumericPropertyValueEvaluator cut;
	@Mock
	private PropertyDefinition property;
	@Mock
	private DataTypeDefinition dataTypeDefinition;
	@Mock
	private Instance instance;

	@Before
	public void init() {
		cut = new NumericPropertyValueEvaluator();
		MockitoAnnotations.initMocks(this);

		when(property.getDataType()).thenReturn(dataTypeDefinition);
		when(dataTypeDefinition.getName()).thenReturn(DataTypeDefinition.TEXT);
	}

	@Test
	public void test_evaluate() {
		when(instance.get("fieldName")).thenReturn(123);
		Serializable fieldName = cut.evaluate(instance, "fieldName");
		assertEquals(123, fieldName);
		when(instance.get("fieldName")).thenReturn(123f);
		fieldName = cut.evaluate(instance, "fieldName");
		assertEquals(123f, fieldName);
	}

	@Test
	public void canEvaluate_successFloat() {
		when(dataTypeDefinition.getName()).thenReturn(DataTypeDefinition.FLOAT);
		assertTrue(cut.canEvaluate(property, null));
	}

	@Test
	public void canEvaluate_successInt() {
		when(dataTypeDefinition.getName()).thenReturn(DataTypeDefinition.INT);
		assertTrue(cut.canEvaluate(property, null));
	}

	@Test
	public void canEvaluate_successDouble() {
		when(dataTypeDefinition.getName()).thenReturn(DataTypeDefinition.DOUBLE);
		assertTrue(cut.canEvaluate(property, null));
	}

	@Test
	public void canEvaluate_successLong() {
		when(dataTypeDefinition.getName()).thenReturn(DataTypeDefinition.LONG);
		assertTrue(cut.canEvaluate(property, null));
	}

	@Test
	public void canEvaluate_fail() {
		when(dataTypeDefinition.getName()).thenReturn(DataTypeDefinition.BOOLEAN);
		assertFalse(cut.canEvaluate(property, null));
	}
}