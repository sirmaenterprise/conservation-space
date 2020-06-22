package com.sirma.itt.seip.resources;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import com.sirma.itt.seip.domain.definition.PropertyDefinition;
import com.sirma.itt.seip.domain.instance.ClassInstance;
import com.sirma.itt.seip.domain.instance.EmfInstance;
import com.sirma.itt.seip.instance.validation.FieldValidationContext;
import com.sirma.itt.seip.instance.validation.PropertyValidationError;
import com.sirma.itt.seip.instance.validator.errors.FieldValidationErrorBuilder;

/**
 * Test for {@link UsernameValidator}
 *
 * @author <a href="mailto:borislav.bonev@sirma.bg">Borislav Bonev</a>
 * @since 11/09/2017
 */
public class UsernameValidatorTest {

	@InjectMocks
	private UsernameValidator validator;

	@Mock
	private ResourceService resourceService;
	@Mock
	private FieldValidationErrorBuilder builder;

	@Before
	public void setUp() throws Exception {
		MockitoAnnotations.initMocks(this);

		when(builder.buildCustomError(any(), any(), any())).thenReturn(mock(PropertyValidationError.class));
	}

	@Test
	public void validate_shouldCallResourceValidation() throws Exception {
		when(resourceService.validateUserName("user@tenant.com")).thenReturn(Boolean.TRUE);
		FieldValidationContext context = new FieldValidationContext();
		context.setValue("user@tenant.com");
		long count = validator.validate(context).count();
		assertEquals(0L, count);
		verify(builder, never()).buildCustomError(any(), any(), any());
	}

	@Test
	public void validate_shouldReturnErrorIfUserNameIsInvalid() throws Exception {
		when(resourceService.validateUserName("user@tenant.com")).thenReturn(Boolean.FALSE);
		FieldValidationContext context = new FieldValidationContext();
		context.setValue("user@tenant.com");
		long count = validator.validate(context).count();
		assertEquals(1L, count);
		verify(builder).buildCustomError(any(), any(), any());
	}

	@Test
	public void isApplicable_shouldAcceptOnlyUsersAndUserNameField() throws Exception {
		FieldValidationContext context = new FieldValidationContext();
		EmfInstance instance = new EmfInstance();
		ClassInstance type = new ClassInstance();
		type.setCategory("user");
		instance.setType(type);
		context.setInstance(instance);
		PropertyDefinition property = mock(PropertyDefinition.class);
		when(property.getName()).thenReturn(ResourceProperties.USER_ID);
		context.setPropertyDefinition(property);

		assertTrue(validator.isApplicable(context));
	}

	@Test
	public void isApplicable_shouldNotNonUsers() throws Exception {
		FieldValidationContext context = new FieldValidationContext();
		EmfInstance instance = new EmfInstance();
		ClassInstance type = new ClassInstance();
		type.setCategory("task");
		instance.setType(type);
		context.setInstance(instance);
		PropertyDefinition property = mock(PropertyDefinition.class);
		when(property.getName()).thenReturn(ResourceProperties.USER_ID);
		context.setPropertyDefinition(property);

		assertFalse(validator.isApplicable(context));
	}

	@Test
	public void isApplicable_shouldNotAcceptNonUserIdProperties() throws Exception {
		FieldValidationContext context = new FieldValidationContext();
		EmfInstance instance = new EmfInstance();
		ClassInstance type = new ClassInstance();
		type.setCategory("user");
		instance.setType(type);
		context.setInstance(instance);
		PropertyDefinition property = mock(PropertyDefinition.class);
		when(property.getName()).thenReturn(ResourceProperties.EMAIL);
		context.setPropertyDefinition(property);

		assertFalse(validator.isApplicable(context));
	}

}
