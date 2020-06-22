package com.sirma.itt.seip.instance.validator;

import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.verify;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import com.sirma.itt.seip.domain.instance.EmfInstance;
import com.sirma.itt.seip.instance.InstanceSaveContext;
import com.sirma.itt.seip.instance.state.Operation;
import com.sirma.itt.seip.instance.validation.ValidationContext;
import com.sirma.itt.seip.instance.validation.Validator;

/**
 * Test for {@link OperationValidationStep}.
 *
 * @author A. Kunchev
 */
@RunWith(MockitoJUnitRunner.class)
public class OperationValidationStepTest {

	@InjectMocks
	private OperationValidationStep step;

	@Mock
	private Validator validator;

	@Test
	public void beforeSave_validatorServiceCalled() {
		step.beforeSave(InstanceSaveContext.create(new EmfInstance(), new Operation()));
		verify(validator).validate(any(ValidationContext.class));
	}

	@Test
	public void getName() {
		assertEquals("operationValidation", step.getName());
	}
}