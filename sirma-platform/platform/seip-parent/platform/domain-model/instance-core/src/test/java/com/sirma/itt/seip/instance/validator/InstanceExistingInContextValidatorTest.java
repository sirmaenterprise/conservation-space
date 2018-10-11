package com.sirma.itt.seip.instance.validator;

import java.util.ArrayList;
import java.util.List;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Matchers;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.Spy;
import org.mockito.runners.MockitoJUnitRunner;

import com.sirma.itt.seip.definition.DefinitionServiceImpl;
import com.sirma.itt.seip.definition.model.PropertyDefinitionProxy;
import com.sirma.itt.seip.domain.definition.DefinitionModel;
import com.sirma.itt.seip.domain.definition.PropertyDefinition;
import com.sirma.itt.seip.domain.definition.label.LabelProvider;
import com.sirma.itt.seip.domain.instance.DefaultProperties;
import com.sirma.itt.seip.domain.instance.Instance;
import com.sirma.itt.seip.domain.instance.InstancePropertyNameResolver;
import com.sirma.itt.seip.instance.ObjectInstance;
import com.sirma.itt.seip.instance.context.InstanceContextService;
import com.sirma.itt.seip.instance.revision.RevisionService;
import com.sirma.itt.seip.instance.state.Operation;
import com.sirma.itt.seip.instance.validation.ValidationContext;
import com.sirma.itt.seip.testutil.mocks.DefinitionMock;

/**
 * @author Boyan Tonchev.
 */
@RunWith(MockitoJUnitRunner.class)
public class InstanceExistingInContextValidatorTest {

	@Mock
	private RevisionService revisionService;

	@Spy
	private InstanceValidationServiceImpl instanceValidationService;

	@Mock
	private LabelProvider labelProvider;

	@Spy
	private DefinitionServiceImpl definitionService;

	@Spy
	private InstancePropertyNameResolver fieldConverter = InstancePropertyNameResolver.NO_OP_INSTANCE;

	@InjectMocks
	private InstanceExistingInContextValidator instanceExistingInContextValidator;

	@Test
	public void should_NotExecuteValidation_When_InstanceIsRevisionAndIsIntegrated() {
		Instance instance = new InstanceBuilder()
				.addExistingInContextProperty(ExistingInContext.WITHOUT_CONTEXT.toString())
				.setRevision()
				.addIntegratedProperty(true)
				.build();
		ValidationContext validationContext = new ValidationContext(instance, Operation.NO_OPERATION);

		instanceExistingInContextValidator.validate(validationContext);

		Mockito.verify(instanceValidationService, Mockito.never()).canExistInContext(Matchers.any(DefinitionModel.class));
		Mockito.verify(instanceValidationService, Mockito.never()).canExistWithoutContext(Matchers.any(DefinitionModel.class));
	}

	@Test
	public void should_NotExecuteValidation_When_InstanceIsRevisionAndIsNotIntegrated() {
		Instance instance = new InstanceBuilder()
				.addExistingInContextProperty(ExistingInContext.WITHOUT_CONTEXT.toString())
				.setRevision()
				.build();
		ValidationContext validationContext = new ValidationContext(instance, Operation.NO_OPERATION);

		instanceExistingInContextValidator.validate(validationContext);

		Mockito.verify(instanceValidationService, Mockito.never()).canExistInContext(Matchers.any(DefinitionModel.class));
		Mockito.verify(instanceValidationService, Mockito.never()).canExistWithoutContext(Matchers.any(DefinitionModel.class));
	}

	@Test
	public void should_ExecuteValidation_When_InstanceIsIntegrated() {
		Instance instance = new InstanceBuilder()
				.addExistingInContextProperty(ExistingInContext.WITHOUT_CONTEXT.toString())
				.addIntegratedProperty(true)
				.build();
		ValidationContext validationContext = new ValidationContext(instance, Operation.NO_OPERATION);

		instanceExistingInContextValidator.validate(validationContext);

		Mockito.verify(instanceValidationService).canExistWithoutContext(Matchers.any(DefinitionModel.class));
	}

	@Test
	public void should_ExecuteValidation_When_InstanceIsNotIntegrated() {
		Instance instance = new InstanceBuilder()
				.addExistingInContextProperty(ExistingInContext.WITHOUT_CONTEXT.toString())
				.addIntegratedProperty(false)
				.build();
		ValidationContext validationContext = new ValidationContext(instance, Operation.NO_OPERATION);

		instanceExistingInContextValidator.validate(validationContext);

		Mockito.verify(instanceValidationService).canExistWithoutContext(Matchers.any(DefinitionModel.class));
	}

	@Test
	public void should_NotHaveErrorMessage_When_DefinitionPropertyExistingInContextIsSetToWithoutAndHaveNotContext() {
		Instance instance = new InstanceBuilder()
				.addExistingInContextProperty(ExistingInContext.WITHOUT_CONTEXT.toString())
				.build();
		ValidationContext validationContext = new ValidationContext(instance, Operation.NO_OPERATION);

		instanceExistingInContextValidator.validate(validationContext);

		Assert.assertTrue(validationContext.getMessages().isEmpty());
	}

	@Test
	public void should_HaveErrorMessage_When_DefinitionPropertyExistingInContextIsSetToInContextAndHaveNotContext() {
		Instance instance = new InstanceBuilder()
				.addExistingInContextProperty(ExistingInContext.IN_CONTEXT.toString())
				.build();
		ValidationContext validationContext = new ValidationContext(instance, Operation.NO_OPERATION);

		instanceExistingInContextValidator.validate(validationContext);

		Assert.assertFalse(validationContext.getMessages().isEmpty());
	}

	@Test
	public void should_NotHaveErrorMessage_When_DefinitionPropertyExistingInContextIsSetToBothAndHaveNotContext() {
		Instance instance = new InstanceBuilder()
				.addExistingInContextProperty(ExistingInContext.BOTH.toString())
				.build();
		ValidationContext validationContext = new ValidationContext(instance, Operation.NO_OPERATION);

		instanceExistingInContextValidator.validate(validationContext);

		Assert.assertTrue(validationContext.getMessages().isEmpty());
	}

	@Test
	public void should_HaveErrorMessage_When_DefinitionPropertyExistingInContextIsSetToWithoutAndHaveContext() {
		Instance instance = new InstanceBuilder().setContext()
				.addExistingInContextProperty(ExistingInContext.WITHOUT_CONTEXT.toString())
				.build();
		ValidationContext validationContext = new ValidationContext(instance, Operation.NO_OPERATION);

		instanceExistingInContextValidator.validate(validationContext);

		Assert.assertFalse(validationContext.getMessages().isEmpty());
	}


	@Test
	public void should_NotHaveErrorMessage_When_DefinitionPropertyExistingInContextIsSetToInContextAndHaveContext() {
		Instance instance = new InstanceBuilder().setContext()
				.addExistingInContextProperty(ExistingInContext.IN_CONTEXT.toString())
				.build();
		ValidationContext validationContext = new ValidationContext(instance, Operation.NO_OPERATION);

		instanceExistingInContextValidator.validate(validationContext);

		Assert.assertTrue(validationContext.getMessages().isEmpty());
	}

	@Test
	public void should_NotHaveErrorMessage_When_DefinitionPropertyExistingInContextIsSetToBothAndHaveContext() {
		Instance instance = new InstanceBuilder().setContext()
				.addExistingInContextProperty(ExistingInContext.BOTH.toString())
				.build();
		ValidationContext validationContext = new ValidationContext(instance, Operation.NO_OPERATION);

		instanceExistingInContextValidator.validate(validationContext);

		Assert.assertTrue(validationContext.getMessages().isEmpty());
	}

	@Test
	public void should_NotHaveErrorMessage_When_DefinitionOfInstanceHaveNotPropertyExistingInContextAndHaveContext() {
		Instance instance = new InstanceBuilder().setContext().build();
		ValidationContext validationContext = new ValidationContext(instance, Operation.NO_OPERATION);

		instanceExistingInContextValidator.validate(validationContext);

		Assert.assertTrue(validationContext.getMessages().isEmpty());
	}

	@Test
	public void should_NotHaveErrorMessage_When_DefinitionOfInstanceHaveNotPropertyExistingInContextAndNoContext() {
		Instance instance = new InstanceBuilder().build();
		ValidationContext validationContext = new ValidationContext(instance, Operation.NO_OPERATION);

		instanceExistingInContextValidator.validate(validationContext);

		Assert.assertTrue(validationContext.getMessages().isEmpty());
	}

	private class InstanceBuilder {
		private Instance instance = new ObjectInstance();
		private DefinitionMock instanceDefinition = new DefinitionMock();
		private List<PropertyDefinition> fields = new ArrayList<>(3);
		private List<PropertyDefinition> configurations = new ArrayList<>();

		private InstanceBuilder setContext() {
			instance.add(InstanceContextService.HAS_PARENT, "emf:parentId");
			return this;
		}

		private InstanceBuilder setRevision() {
			Mockito.when(revisionService.isRevision(instance)).thenReturn(true);
			return this;
		}

		private InstanceBuilder addExistingInContextProperty(String existInContext) {
			PropertyDefinitionProxy propertyDefinition = new PropertyDefinitionProxy();
			propertyDefinition.setName(DefaultProperties.EXISTING_IN_CONTEXT);
			propertyDefinition.setValue(existInContext);
			configurations.add(propertyDefinition);
			return this;
		}

		private InstanceBuilder addIntegratedProperty(Boolean integratedValue) {
			PropertyDefinitionProxy propertyDefinition = new PropertyDefinitionProxy();
			propertyDefinition.setUri("emf:integrated");
			propertyDefinition.setName("integrated");
			instance.add("integrated", integratedValue);
			fields.add(propertyDefinition);
			return this;
		}

		private Instance build() {
			instanceDefinition.setFields(fields);
			instanceDefinition.setConfigurations(configurations);
			Mockito.doReturn(instanceDefinition).when(definitionService).getInstanceDefinition(instance);
			return instance;
		}
	}
}
