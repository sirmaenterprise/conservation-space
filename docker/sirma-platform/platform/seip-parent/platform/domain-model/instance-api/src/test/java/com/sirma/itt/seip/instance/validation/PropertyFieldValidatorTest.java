package com.sirma.itt.seip.instance.validation;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import com.sirma.itt.seip.domain.definition.PropertyDefinition;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.when;

/**
 * Tests collecting values in PropertyFieldValidator.
 *
 * @author <a href="mailto:ivo.rusev@sirma.bg">Ivo Rusev</a>
 * @since 18/05/2017
 */
public class PropertyFieldValidatorTest {

	@Mock
	private PropertyDefinition propertyDefinition;

	@Before
	public void init() {
		MockitoAnnotations.initMocks(this);
		when(propertyDefinition.isMultiValued()).thenReturn(true);
	}

	@Test
	public void collectValues_multiValued() {
		FieldValidationContext ctx = new FieldValidationContext();
		List<String> multivalued = new ArrayList<>();
		multivalued.add("1");
		multivalued.add("2");
		multivalued.add("3");
		ctx.setValue((Serializable) multivalued);
		ctx.setPropertyDefinition(propertyDefinition);
		assertEquals(3, PropertyFieldValidator.collectValues(ctx).count());
	}

	@Test
	public void collectValues_nullValue() {
		FieldValidationContext ctx = new FieldValidationContext();
		ctx.setValue(null);
		ctx.setPropertyDefinition(propertyDefinition);
		assertEquals(0, PropertyFieldValidator.collectValues(ctx).count());
	}

	@Test
	public void collectValues_singleValue() {
		when(propertyDefinition.isMultiValued()).thenReturn(false);
		FieldValidationContext ctx = new FieldValidationContext();
		ctx.setValue("singleValue");
		ctx.setPropertyDefinition(propertyDefinition);
		assertEquals(1, PropertyFieldValidator.collectValues(ctx).count());
	}
}