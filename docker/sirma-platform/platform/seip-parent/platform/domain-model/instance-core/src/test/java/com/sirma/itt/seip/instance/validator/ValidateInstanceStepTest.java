package com.sirma.itt.seip.instance.validator;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.Arrays;
import java.util.Collections;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Matchers;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import com.sirma.itt.seip.domain.instance.Instance;
import com.sirma.itt.seip.exception.RollbackedRuntimeException;
import com.sirma.itt.seip.instance.InstanceSaveContext;
import com.sirma.itt.seip.instance.state.Operation;
import com.sirma.itt.seip.instance.validation.InstanceValidationResult;
import com.sirma.itt.seip.instance.validation.InstanceValidationService;
import com.sirma.itt.seip.instance.validation.PropertyValidationError;
import com.sirma.itt.seip.instance.validation.PropertyValidationErrorTypes;

@RunWith(MockitoJUnitRunner.class)
public class ValidateInstanceStepTest {
	@Mock
	private InstanceValidationService instanceValidationService;
	@InjectMocks
	private ValidateInstanceStep validateInstanceStep;

	@Test(expected = RollbackedRuntimeException.class)
	public void testBeforeSaveNotValid() throws Exception {
		when(instanceValidationService.validate(Matchers.any()))
				.thenReturn(new InstanceValidationResult(Arrays.asList(new PropertyValidationError() {

					@Override
					public String getValidationType() {
						return PropertyValidationErrorTypes.INVALID_URI;
					}
				})));

		validateInstanceStep.beforeSave(InstanceSaveContext.create(mock(Instance.class), mock(Operation.class)));
	}

	@Test
	public void testBeforeSaveValid() throws Exception {
		when(instanceValidationService.validate(Matchers.any()))
				.thenReturn(new InstanceValidationResult(Collections.emptyList()));
		validateInstanceStep.beforeSave(InstanceSaveContext.create(mock(Instance.class), mock(Operation.class)));
	}

	@Test
	public void shouldNotThrowErrorOnDisabledValidation() throws Exception {
		when(instanceValidationService.validate(Matchers.any()))
				.thenReturn(new InstanceValidationResult(Arrays.asList(new PropertyValidationError() {

					@Override
					public String getValidationType() {
						return PropertyValidationErrorTypes.INVALID_URI;
					}
				})));

		validateInstanceStep.beforeSave(InstanceSaveContext.create(mock(Instance.class), mock(Operation.class)).disableValidation("test"));
	}

	@Test
	public void testGetNameConstant() throws Exception {
		Assert.assertEquals("validateInstanceStep", validateInstanceStep.getName());
	}
}
