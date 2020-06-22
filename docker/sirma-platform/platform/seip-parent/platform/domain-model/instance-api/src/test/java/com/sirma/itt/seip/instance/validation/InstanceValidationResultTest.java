package com.sirma.itt.seip.instance.validation;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.mockito.Mockito.when;

/**
 * Tests for {@link InstanceValidationResult}.
 *
 * @author <a href="mailto:ivo.rusev@sirma.bg">Ivo Rusev</a>
 * @since 18/05/2017
 */
public class InstanceValidationResultTest {

	private InstanceValidationResult result;

	@Mock
	private PropertyValidationError booleanError;
	@Mock
	private PropertyValidationError uriError;

	@Before
	public void init() {
		MockitoAnnotations.initMocks(this);
		when(booleanError.getValidationType()).thenReturn(PropertyValidationErrorTypes.INVALID_BOOLEAN);
		when(uriError.getValidationType()).thenReturn(PropertyValidationErrorTypes.INVALID_URI);
		List<PropertyValidationError> error = new ArrayList<>();
		error.add(booleanError);
		error.add(uriError);
		result = new InstanceValidationResult(error);
	}

	@Test
	public void testGetErrorsByType() {
		assertEquals(1, result.getErrorsByType(PropertyValidationErrorTypes.INVALID_BOOLEAN).size());
		assertEquals(1, result.getErrorsByType(PropertyValidationErrorTypes.INVALID_URI).size());
		assertEquals(0, result.getErrorsByType(PropertyValidationErrorTypes.MISSING_MANDATORY_PROPERTY).size());
	}

	@Test
	public void testHasPassed() {
		assertFalse(result.hasPassed());
		assertEquals(2, result.getErrorMessages().size());
	}
}