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
import com.sirma.sep.instance.properties.expression.evaluation.TextPropertyValueEvaluator;

/**
 * Test for {@link TextPropertyValueEvaluator}.
 *
 * @author <a href="mailto:ivo.rusev@sirma.bg">Ivo Rusev</a>
 * @since 17/07/2017
 */
public class TextPropertyValueEvaluatorTest {

	@InjectMocks
	private TextPropertyValueEvaluator cut;
	@Mock
	private PropertyDefinition property;
	@Mock
	private DataTypeDefinition dataTypeDefinition;
	@Mock
	private Instance instance;

	@Before
	public void init() {
		cut = new TextPropertyValueEvaluator();
		MockitoAnnotations.initMocks(this);

		when(property.getDataType()).thenReturn(dataTypeDefinition);
		when(dataTypeDefinition.getName()).thenReturn(DataTypeDefinition.TEXT);
	}

	@Test
	public void test_evaluate() {
		when(instance.getString("fieldName")).thenReturn("fieldValue");
		Serializable fieldName = cut.evaluate(instance, "fieldName");
		assertEquals("fieldValue", fieldName);
	}

	@Test
	public void canEvaluate_success() {
		when(dataTypeDefinition.getName()).thenReturn(DataTypeDefinition.TEXT);
		assertTrue(cut.canEvaluate(property, null));
	}

	@Test
	public void canEvaluate_failClField() {
		when(dataTypeDefinition.getName()).thenReturn(DataTypeDefinition.TEXT);
		when(property.getCodelist()).thenReturn(200);
		assertFalse(cut.canEvaluate(property, null));
	}

	@Test
	public void canEvaluate_failBooleanField() {
		when(dataTypeDefinition.getName()).thenReturn(DataTypeDefinition.BOOLEAN);
		assertFalse(cut.canEvaluate(property, null));
	}
}