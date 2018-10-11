package com.sirma.itt.seip.instance.definition.validator;

import java.util.Optional;

import com.sirma.itt.seip.domain.instance.EmfInstance;
import com.sirma.itt.seip.domain.instance.Instance;
import com.sirma.itt.seip.domain.instance.InstanceReference;
import com.sirma.itt.seip.instance.state.Operation;
import com.sirma.itt.seip.instance.validation.InstanceValidationService;
import com.sirma.itt.seip.instance.validation.ValidationContext;
import com.sirma.itt.seip.instance.validation.Validator;
import com.sirma.itt.seip.instance.validator.ContextStateValidator;
import com.sirma.itt.seip.security.context.SecurityContextManager;
import com.sirma.itt.seip.testutil.mocks.InstanceContextServiceMock;
import com.sirma.itt.seip.testutil.mocks.InstanceReferenceMock;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Matchers;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.mockito.Spy;

/**
 * Test the context state validator.
 *
 * @author nvelkov
 */
public class ContextStateValidatorTest {
	private static final Operation CREATE = new Operation("CREATE");
	private static final Operation MOVE = new Operation("MOVE");

	@Mock
	private InstanceValidationService instanceValidationService;

	@Mock
	private SecurityContextManager securityContextManager;

	@Spy
	private InstanceContextServiceMock contextService;

	@InjectMocks
	private Validator validator = new ContextStateValidator();

	@Before
	public void init() {
		MockitoAnnotations.initMocks(this);
	}

	@Test
	public void testValidatorMissingContext() {
		Mockito.when(securityContextManager.isAuthenticatedAsAdmin()).thenReturn(false);
		Instance instance = InstanceReferenceMock.createGeneric("instance").toInstance();
		ValidationContext context = new ValidationContext(instance, CREATE);
		validator.validate(context);
		Mockito.verifyZeroInteractions(instanceValidationService);
	}

	@Test
	public void testValidator() {
		EmfInstance instance = new EmfInstance();
		instance.setId("instanceId");

		Mockito.when(securityContextManager.isAuthenticatedAsAdmin()).thenReturn(false);
		Mockito.when(instanceValidationService.canCreateOrUploadIn(Matchers.any(Instance.class))).thenReturn(
				Optional.empty());
		Instance owningInstance = Mockito.mock(Instance.class);
		Mockito.when(owningInstance.getId()).thenReturn("owningInstanceId");
		InstanceReference reference = InstanceReferenceMock.createGeneric(owningInstance);
		contextService.bindContext(instance, reference);
		ValidationContext context = new ValidationContext(instance, CREATE);
		validator.validate(context);
		context = new ValidationContext(instance, MOVE);
		validator.validate(context);
		Mockito.verify(instanceValidationService, Mockito.times(2)).canCreateOrUploadIn(owningInstance);
	}

	@Test
	public void testValidatorError() {
		EmfInstance instance = new EmfInstance();
		instance.setId("instanceId");

		Instance owningInstance = Mockito.mock(Instance.class);
		Mockito.when(securityContextManager.isAuthenticatedAsAdmin()).thenReturn(false);
		Mockito.when(owningInstance.getId()).thenReturn("owningInstanceId");
		contextService.bindContext(instance, owningInstance);
		Mockito.when(instanceValidationService.canCreateOrUploadIn(owningInstance)).thenReturn(Optional.of("error"));

		ValidationContext context = new ValidationContext(instance, CREATE);
		validator.validate(context);
		Assert.assertEquals(1, context.getMessages().size());
		Assert.assertEquals("error", context.getMessages().get(0).getMessage());
	}

	@Test
	public void testValidatorNotAuthenticatedAsAdmin() {
		Mockito.when(securityContextManager.isAuthenticatedAsAdmin()).thenReturn(true);
		ValidationContext context = new ValidationContext(new EmfInstance(), CREATE);
		validator.validate(context);
		Mockito.verify(instanceValidationService, Mockito.never()).canCreateOrUploadIn(Matchers.any());
	}

	@Test
	public void testValidatorWithNotCreateOperation() {
		Mockito.when(securityContextManager.isAuthenticatedAsAdmin()).thenReturn(false);
		ValidationContext context = new ValidationContext(new EmfInstance(), new Operation("addRelation"));
		validator.validate(context);
		Mockito.verify(instanceValidationService, Mockito.never()).canCreateOrUploadIn(Matchers.any());
	}
}
