package com.sirma.itt.seip.instance.validation;

import com.sirma.itt.seip.domain.definition.PropertyDefinition;
import com.sirma.itt.seip.domain.instance.Instance;
import org.junit.Test;

import java.io.Serializable;
import java.util.Map;
import java.util.Set;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

/**
 * Test for the class {@link FieldValidationContext}.
 *
 * @author <a href="mailto:ivo.rusev@sirma.bg">Ivo Rusev</a>
 * @since 18/05/2017
 */
public class FieldValidationContextTest {

	@Test
	public void copy() {
		FieldValidationContext cut = new FieldValidationContext();
		cut.setDynamicClFilters(mock(Map.class));
		cut.setMandatoryFields(mock(Set.class));
		cut.setOptionalFields(mock(Set.class));
		cut.setValue(mock(Serializable.class));
		cut.setInstance(mock(Instance.class));
		cut.setPropertyDefinition(mock(PropertyDefinition.class));

		FieldValidationContext copy = cut.copy();
		assertNotNull(copy.getDynamicClFilters());
		assertNotNull(copy.getMandatoryFields());
		assertNotNull(copy.getOptionalFields());
		assertNull(copy.getInstance());
		assertNull(copy.getValue());
		assertNull(copy.getPropertyDefinition());
	}

}