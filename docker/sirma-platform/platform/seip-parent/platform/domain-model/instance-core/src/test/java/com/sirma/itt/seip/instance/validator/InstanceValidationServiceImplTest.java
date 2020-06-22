package com.sirma.itt.seip.instance.validator;

import com.sirma.itt.seip.definition.model.PropertyDefinitionProxy;
import com.sirma.itt.seip.domain.definition.DefinitionModel;
import com.sirma.itt.seip.domain.definition.PropertyDefinition;
import com.sirma.itt.seip.domain.definition.label.LabelProvider;
import com.sirma.itt.seip.domain.instance.DefaultProperties;
import com.sirma.itt.seip.domain.instance.Instance;
import com.sirma.itt.seip.domain.instance.InstanceReference;
import com.sirma.itt.seip.instance.InstanceTypeResolver;
import com.sirma.itt.seip.instance.state.Operation;
import com.sirma.itt.seip.instance.validation.ValidationContext;
import com.sirma.itt.seip.permissions.action.AuthorityService;
import com.sirma.itt.seip.testutil.mocks.DefinitionMock;

import org.apache.commons.lang.StringUtils;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.util.Collections;
import java.util.Optional;

import static org.mockito.Mockito.*;

/**
 * Tests for the instance validator class.
 *
 * @author <a href="mailto:ivo.rusev@sirma.bg">Ivo Rusev</a>
 * @since 18/05/2017
 */
@RunWith(MockitoJUnitRunner.class)
public class InstanceValidationServiceImplTest {

	@InjectMocks
	private InstanceValidationServiceImpl cut;

	@Mock
	private InstanceValidator instanceValidator;

	@Mock
	private AuthorityService authorityService;

	@Mock
	private LabelProvider labelProvider;

	@Mock
	private InstanceTypeResolver instanceTypeResolver;

	@Test
	public void should_CanNotExistWithoutContext_When_PropertyIsSetToInContext() {
		DefinitionModel instanceDefinition = createDefinitionModelWithExistingInContextConfiguration(ExistingInContext.IN_CONTEXT.toString());

		Assert.assertFalse(cut.canExistWithoutContext(instanceDefinition));
	}

	@Test
	public void should_CanExistWithoutContext_When_PropertyIsSetToBoth() {
		DefinitionModel instanceDefinition = createDefinitionModelWithExistingInContextConfiguration(ExistingInContext.BOTH.toString());

		Assert.assertTrue(cut.canExistWithoutContext(instanceDefinition));
	}

	@Test
	public void should_CanExistWithoutContext_When_PropertyIsSetToWithoutContext() {
		DefinitionModel instanceDefinition = createDefinitionModelWithExistingInContextConfiguration(ExistingInContext.WITHOUT_CONTEXT.toString());

		Assert.assertTrue(cut.canExistWithoutContext(instanceDefinition));
	}

	@Test
	public void should_CanExistWithoutContext_When_PropertyIsNotSetInDefinition() {
		DefinitionModel instanceDefinition = createDefinitionModelWithExistingInContextConfiguration(null);

		Assert.assertTrue(cut.canExistWithoutContext(instanceDefinition));
	}

	@Test
	public void should_CanNotExistInContext_When_PropertyIsSetToWithoutContext() {
		DefinitionModel instanceDefinition = createDefinitionModelWithExistingInContextConfiguration(ExistingInContext.WITHOUT_CONTEXT.toString());

		Assert.assertFalse(cut.canExistInContext(instanceDefinition));
	}

	@Test
	public void should_CanExistInContext_When_PropertyIsSetToBoth() {
		DefinitionModel instanceDefinition = createDefinitionModelWithExistingInContextConfiguration(ExistingInContext.BOTH.toString());

		Assert.assertTrue(cut.canExistInContext(instanceDefinition));
	}

	@Test
	public void should_CanExistInContext_When_PropertyIsSetToInContext() {
		DefinitionModel instanceDefinition = createDefinitionModelWithExistingInContextConfiguration(ExistingInContext.IN_CONTEXT.toString());

		Assert.assertTrue(cut.canExistInContext(instanceDefinition));
	}

	@Test
	public void should_CanExistInContext_When_PropertyIsNotSetInDefinition() {
		DefinitionModel instanceDefinition = createDefinitionModelWithExistingInContextConfiguration(null);

		Assert.assertTrue(cut.canExistInContext(instanceDefinition));
	}

	@Test
	public void testValidate() {
		ValidationContext ctx = new ValidationContext(mock(Instance.class), mock(Operation.class));
		cut.validate(ctx);
		verify(instanceValidator, times(1)).validate(ctx);
	}

	@Test
	public void testCanCreateOrUploadIn_noValidationError() {
		when(authorityService.isActionAllowed(any(Instance.class), any(String.class), any(String.class))).thenReturn(
				true);

		Optional<String> result = cut.canCreateOrUploadIn(mock(Instance.class));
		Assert.assertFalse(result.isPresent());
	}

	@Test
	public void testCanCreateOrUploadIn_validationError() {
		when(authorityService.isActionAllowed(any(Instance.class), any(String.class), any(String.class))).thenReturn(
				false);
		when(labelProvider.getValue(any(String.class))).thenReturn("some_value");

		Optional<String> result = cut.canCreateOrUploadIn(mock(Instance.class));

		verify(labelProvider, times(1)).getValue(any(String.class));
		Assert.assertTrue(result.isPresent());
	}

	@Test
	public void testCanCreateOrUploadIn_validationError2() {
		when(labelProvider.getValue(any(String.class))).thenReturn("some_value");

		Optional<InstanceReference> mock = Optional.of(mock(InstanceReference.class));
		when(instanceTypeResolver.resolveReference("emf:id")).thenReturn(mock);

		Optional<String> result = cut.canCreateOrUploadIn("emf:id", "CREATE");

		verify(labelProvider, times(1)).getValue(any(String.class));
		Assert.assertTrue(result.isPresent());
	}

	private DefinitionModel createDefinitionModelWithExistingInContextConfiguration(String existingInContextValue) {
		DefinitionMock definitionModel = new DefinitionMock();
		if (StringUtils.isNotBlank(existingInContextValue)) {
			PropertyDefinitionProxy propertyDefinition = new PropertyDefinitionProxy();
			propertyDefinition.setName(DefaultProperties.EXISTING_IN_CONTEXT);
			propertyDefinition.setValue(existingInContextValue);
			definitionModel.setConfigurations(Collections.singletonList(propertyDefinition));
		}
		return definitionModel;
	}
}