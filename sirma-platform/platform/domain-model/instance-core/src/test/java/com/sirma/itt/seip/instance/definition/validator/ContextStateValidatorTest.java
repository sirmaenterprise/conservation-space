package com.sirma.itt.seip.instance.definition.validator;

import java.util.Optional;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Matchers;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

import com.sirma.itt.seip.domain.instance.EmfInstance;
import com.sirma.itt.seip.domain.instance.Instance;
import com.sirma.itt.seip.domain.instance.InstanceReference;
import com.sirma.itt.seip.instance.state.Operation;
import com.sirma.itt.seip.instance.validation.InstanceValidationService;
import com.sirma.itt.seip.instance.validation.ValidationContext;
import com.sirma.itt.seip.instance.validation.Validator;
import com.sirma.itt.seip.instance.validator.ContextStateValidator;
import com.sirma.itt.seip.security.context.SecurityContextManager;
import com.sirma.itt.seip.testutil.mocks.InstanceReferenceMock;

/**
 * Test the context state validator.
 *
 * @author nvelkov
 */
public class ContextStateValidatorTest {

	@Mock
	private InstanceValidationService instanceValidationService;

	@Mock
	private SecurityContextManager securityContextManager;

	@InjectMocks
	private Validator validator = new ContextStateValidator();

	@Before
	public void init() {
		MockitoAnnotations.initMocks(this);
	}

	@Test
	public void testValidatorMissingContext() {
		Mockito.when(securityContextManager.isAuthenticatedAsAdmin()).thenReturn(false);
		Instance instance = new EmfInstance();
		ValidationContext context = new ValidationContext(instance, Operation.NO_OPERATION);
		validator.validate(context);
		Mockito.verifyZeroInteractions(instanceValidationService);
	}

	@Test
	public void testValidator() {
		EmfInstance instance = new EmfInstance();
		instance.setId("instanceId");

		Mockito.when(securityContextManager.isAuthenticatedAsAdmin()).thenReturn(false);
		Mockito.when(instanceValidationService.canCreateOrUploadIn(Matchers.any(Instance.class)))
				.thenReturn(Optional.empty());
		Instance owningInstance = Mockito.mock(Instance.class);
		InstanceReference reference = new InstanceReferenceMock();
		reference.setIdentifier("owningInstanceId");
		Mockito.when(owningInstance.toReference()).thenReturn(reference);

		instance.setOwningInstance(owningInstance);
		ValidationContext context = new ValidationContext(instance, Operation.NO_OPERATION);
		validator.validate(context);
		Mockito.verify(instanceValidationService, Mockito.times(1)).canCreateOrUploadIn(owningInstance);

	}

	@Test
	public void testValidatorError() {
		EmfInstance instance = new EmfInstance();
		instance.setId("instanceId");

		Instance owningInstance = Mockito.mock(Instance.class);
		InstanceReference reference = new InstanceReferenceMock();
		reference.setIdentifier("owningInstanceId");
		Mockito.when(securityContextManager.isAuthenticatedAsAdmin()).thenReturn(false);
		Mockito.when(owningInstance.toReference()).thenReturn(reference);
		instance.setOwningInstance(owningInstance);

		Mockito.when(instanceValidationService.canCreateOrUploadIn(owningInstance)).thenReturn(Optional.of("error"));

		ValidationContext context = new ValidationContext(instance, Operation.NO_OPERATION);
		validator.validate(context);
		Assert.assertEquals(context.getMessages().size(), 1);
		Assert.assertEquals("error", context.getMessages().get(0).getMessage());
	}

	@Test
	public void testValidatorNotAuthenticatedAsAdmin() {
		Mockito.when(securityContextManager.isAuthenticatedAsAdmin()).thenReturn(true);
		ValidationContext context = new ValidationContext(new EmfInstance(), Operation.NO_OPERATION);
		validator.validate(context);
		Mockito.verify(instanceValidationService, Mockito.never()).canCreateOrUploadIn(Matchers.any());
	}
}
