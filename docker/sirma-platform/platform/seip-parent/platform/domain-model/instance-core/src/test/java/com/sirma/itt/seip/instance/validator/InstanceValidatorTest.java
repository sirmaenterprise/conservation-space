package com.sirma.itt.seip.instance.validator;

import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.anyString;
import static org.mockito.Mockito.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import com.sirma.itt.seip.definition.DefinitionService;
import com.sirma.itt.seip.domain.definition.ControlDefinition;
import com.sirma.itt.seip.domain.definition.DefinitionModel;
import com.sirma.itt.seip.domain.definition.PropertyDefinition;
import com.sirma.itt.seip.domain.instance.Instance;
import com.sirma.itt.seip.expressions.conditions.ConditionType;
import com.sirma.itt.seip.expressions.conditions.ConditionsManager;
import com.sirma.itt.seip.instance.state.Operation;
import com.sirma.itt.seip.instance.state.StateService;
import com.sirma.itt.seip.instance.state.StateTransitionManager;
import com.sirma.itt.seip.instance.validation.FieldValidationContext;
import com.sirma.itt.seip.instance.validation.InstanceValidationResult;
import com.sirma.itt.seip.instance.validation.PropertyValidationError;
import com.sirma.itt.seip.instance.validation.ValidationContext;
import com.sirma.itt.seip.instance.validator.errors.CodelistFieldValidationError;
import com.sirma.itt.seip.security.context.SecurityContextManager;
import com.sirma.itt.seip.testutil.fakes.SecurityContextManagerFake;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.Spy;

/**
 * Tests for the class {@link InstanceValidator}.
 * <p>
 *
 * @author <a href="mailto:ivo.rusev@sirma.bg">Ivo Rusev</a>
 * @since 18/05/2017
 */
public class InstanceValidatorTest {

	private static final String HTML_VALUE = "<b>some</b> <i>string</> <span style=\"font-size:25px\">value</span>";
	private static final String STRIPPED_HTML_VALUE = "some string value";

	@InjectMocks
	private InstanceValidator cut;

	@Mock
	private DefinitionService definitionService;
	@Mock
	private DefinitionPropertyValidationService propertyValidationService;
	@Mock
	private StateService stateService;
	@Mock
	private StateTransitionManager stateTransitionManager;
	@Mock
	private ConditionsManager conditionsManager;
	// InstanceValidator wraps the validation in a security context, that's why we need this @Spy.
	@Spy
	private SecurityContextManager securityContextManager = new SecurityContextManagerFake();
	@Mock
	private Instance instance;
	@Mock
	private DefinitionModel definition;

	@Before
	public void init() {
		cut = new InstanceValidator();
		MockitoAnnotations.initMocks(this);

		stubDefinitionModel();
		mockCollectingMandatoryFields();

		PropertyValidationError errorMock = mock(CodelistFieldValidationError.class);
		when(propertyValidationService.validate(any())).thenReturn(Collections.singletonList(errorMock));
	}

	@Test
	public void validate_clErrors() {
		ValidationContext ctx = new ValidationContext(instance, new Operation("create"));
		InstanceValidationResult result = cut.validate(ctx);
		verify(conditionsManager).getVerifiedFieldsByType(any(DefinitionModel.class), eq(ConditionType.OPTIONAL),
				any(Instance.class));
		verify(stateTransitionManager).getRequiredFields(any(Instance.class), any(), anyString());
		verify(stateService).getPrimaryState(any(Instance.class));
		verify(propertyValidationService, times(3)).validate(any(FieldValidationContext.class));
		Assert.assertEquals(3, result.getErrorMessages().size());
		Assert.assertEquals(false, result.hasPassed());
	}

	@Test
	public void validate_clErrors_multivalued() {
		when(instance.get("clField")).thenReturn((Serializable) Collections.singletonList("CL_VALUE"));
		ValidationContext ctx = new ValidationContext(instance, new Operation("create"));
		InstanceValidationResult result = cut.validate(ctx);
		verify(conditionsManager).getVerifiedFieldsByType(any(DefinitionModel.class), eq(ConditionType.OPTIONAL),
				any(Instance.class));
		Assert.assertEquals(3, result.getErrorMessages().size());
		Assert.assertEquals(false, result.hasPassed());
	}

	@Test
	public void test_validate_invalidState() {
		ValidationContext ctx = new ValidationContext(instance, new Operation(""));
		cut.validate(ctx);
		ArgumentCaptor<FieldValidationContext> captor = ArgumentCaptor.forClass(FieldValidationContext.class);
		verify(propertyValidationService, times(3)).validate(captor.capture());
		Set<Serializable> values = captor.getAllValues()
				.stream()
				.map(FieldValidationContext::getValue)
				.collect(Collectors.toSet());
		assertTrue(values.contains(STRIPPED_HTML_VALUE));
		verify(stateService, never()).getPrimaryState(any(Instance.class));
	}

	/**
	 * Mocks the definitions model. Adds two property definitions, one for cl
	 * and one more simple string field.
	 */
	private void stubDefinitionModel() {
		when(definitionService.getInstanceDefinition(any())).thenReturn(definition);

		// Mock a first definition property, which is a CL field.
		PropertyDefinition clProperty = mock(PropertyDefinition.class);
		when(clProperty.getName()).thenReturn("clField");
		when(instance.get("clField")).thenReturn("CL_VALUE");
		when(clProperty.getCodelist()).thenReturn(200);

		// Mock a second property.
		PropertyDefinition stringProperty = mock(PropertyDefinition.class);
		when(stringProperty.getName()).thenReturn("stringField");
		when(instance.get("stringField")).thenReturn("some-string-value");

		// Mock richtext property.
		PropertyDefinition richtextProperty = mock(PropertyDefinition.class);
		when(richtextProperty.getName()).thenReturn("richtextProperty");
		ControlDefinition controlDefinition = mock(ControlDefinition.class);
		when(controlDefinition.getIdentifier()).thenReturn("RICHTEXT");
		when(richtextProperty.getControlDefinition()).thenReturn(controlDefinition);
		when(instance.get("richtextProperty")).thenReturn(HTML_VALUE);

		// add the mocked properties to the definition
		final List<PropertyDefinition> properties = new ArrayList<>();
		properties.add(clProperty);
		properties.add(stringProperty);
		properties.add(richtextProperty);
		when(definition.fieldsStream()).then(a -> properties.stream());
	}

	private void mockCollectingMandatoryFields() {
		// Stub mandatory fields that come directly from the definition
		Set<String> mandatory = new HashSet<>();
		mandatory.add("firstField");
		mandatory.add("secondField");

		// Stub optional fields
		when(stateTransitionManager.getRequiredFields(any(), any(), any())).thenReturn(mandatory);

		// stub mandatory fields that come from condition evaluation
		PropertyDefinition mockMandatoryField = mock(PropertyDefinition.class);
		when(mockMandatoryField.getName()).thenReturn("thirdField");
		List<PropertyDefinition> mandatoryFieldDefinitions = new ArrayList<>();
		mandatoryFieldDefinitions.add(mockMandatoryField);

		// stub optional fields that come from condition evaluation
		PropertyDefinition mockOptionalField = mock(PropertyDefinition.class);
		when(mockOptionalField.getName()).thenReturn("optField");
		mandatoryFieldDefinitions.add(mockOptionalField);

		when(conditionsManager.getVerifiedFieldsByType(any(), eq(ConditionType.OPTIONAL), any()))
				.thenReturn(Stream.empty());
		when(conditionsManager.getVerifiedFieldsByType(any(), eq(ConditionType.MANDATORY), any()))
				.thenReturn(mandatoryFieldDefinitions.stream());
	}
}