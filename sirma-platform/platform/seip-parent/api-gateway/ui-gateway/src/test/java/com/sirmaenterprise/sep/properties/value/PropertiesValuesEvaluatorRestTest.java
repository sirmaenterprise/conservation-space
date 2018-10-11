package com.sirmaenterprise.sep.properties.value;

import static org.mockito.Matchers.anyObject;
import static org.mockito.Matchers.eq;

import java.io.Serializable;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Matchers;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.runners.MockitoJUnitRunner;

import com.sirma.itt.seip.definition.DefinitionService;
import com.sirma.itt.seip.domain.definition.ControlDefinition;
import com.sirma.itt.seip.domain.definition.ControlParam;
import com.sirma.itt.seip.domain.definition.DefinitionModel;
import com.sirma.itt.seip.domain.definition.PropertyDefinition;
import com.sirma.itt.seip.domain.instance.Instance;
import com.sirma.itt.seip.instance.InstanceTypeResolver;
import com.sirma.itt.seip.rest.utils.JsonKeys;
import com.sirma.itt.seip.testutil.mocks.ControlDefintionMock;
import com.sirma.itt.seip.testutil.mocks.ControlParamMock;
import com.sirmaenterprise.sep.properties.expression.evaluation.PropertiesValuesEvaluatorService;

/**
 * Test for {@link PropertiesValuesEvaluatorRest} class.
 *
 * @author Boyan Tonchev.
 */
@RunWith(MockitoJUnitRunner.class)
public class PropertiesValuesEvaluatorRestTest {

	private static final String NEW_INSTANCE_DEFINITION_ID = "newInstanceDefinitionId";
	private static final String NEW_INSTANCE_SINGLE_VALUED_PROPERTY_NAME = "generatedFieldSingelValued";
	private static final String NEW_INSTANCE_MULTI_VALUED_PROPERTY_NAME = "generatedFieldMultiValued";
	private static final String NEW_INSTANCE_VALUE_SUGGEST_PROPERTY_NAME = "generatedFieldValueSuggest";
	private static final String INSTANCE_ID = "instanceId";
	private static final String REAL_INSTANCE_PROPERTY_NAME = "emf:email";
	private static final String INSTANCE_PROPERTY_NAME = "emf:createdBy.emf:email";
	private static final String INSTANCE_PROPERTY_VALUE = "instanceValue";
	private static final String INSTANCE_PROPERTY_EVALUATED_VALUE = "instanceEvaluatedValue";

	private static final Collection<String> INSTANCE_MULTI_VALUED_PROPERTY_VALUE = Arrays.asList("value one",
			"value_two");
	private static final String INSTANCE_MULTI_VALUED_PROPERTY_EVALUATED_VALUE = "evaluated value one and value two";

	private static final String DEFAULT_VALUE_PATTERN = "default_value_pattern";

	@Mock
	private DefinitionModel definitionModel;

	@Mock
	private InstanceTypeResolver instanceTypeResolver;

	@Mock
	private DefinitionService definitionService;

	@Mock
	private PropertiesValuesEvaluatorService expressionTemplateService;

	@InjectMocks
	private PropertiesValuesEvaluatorRest propertiesValuesEvaluatorRest;

	@Before
	public void setUp() {
		Mockito.when(definitionService.find(NEW_INSTANCE_DEFINITION_ID)).thenReturn(definitionModel);
	}

	@Test
	public void should_ReturnNull_When_PropertyDefinitionOfNewInstanceNotFoundValuedButValueSingleValue() {
		InstanceBuilder instanceBuilder = new InstanceBuilder(INSTANCE_ID);
		instanceBuilder.addProperty(REAL_INSTANCE_PROPERTY_NAME, INSTANCE_PROPERTY_VALUE,
				INSTANCE_PROPERTY_EVALUATED_VALUE);
		setUpInstanceResolver(Collections.singletonList(instanceBuilder));
		Mockito.when(definitionModel.getField(NEW_INSTANCE_SINGLE_VALUED_PROPERTY_NAME)).thenReturn(Optional.empty());
		PropertiesValuesEvaluatorRequest request = new PropertiesValuesEvaluatorRequest();
		request.addObjectProperty(INSTANCE_ID, INSTANCE_PROPERTY_NAME, NEW_INSTANCE_SINGLE_VALUED_PROPERTY_NAME);
		request.setNewInstanceDefinitionId(NEW_INSTANCE_DEFINITION_ID);

		PropertiesValuesEvaluatorResponse result = propertiesValuesEvaluatorRest.eval(request);

		assertPropertyValue(result, null);
	}

	@Test
	public void should_ReturnPropertyValueAsList_When_PropertyOfNewInstanceIsMultiValuedButValueSingleValue() {
		InstanceBuilder instanceBuilder = new InstanceBuilder(INSTANCE_ID);
		instanceBuilder.addProperty(REAL_INSTANCE_PROPERTY_NAME, INSTANCE_PROPERTY_VALUE,
				INSTANCE_PROPERTY_EVALUATED_VALUE);
		setUpInstanceResolver(Collections.singletonList(instanceBuilder));
		setupPropertyDefinition(NEW_INSTANCE_SINGLE_VALUED_PROPERTY_NAME, true);
		PropertiesValuesEvaluatorRequest request = new PropertiesValuesEvaluatorRequest();
		request.addObjectProperty(INSTANCE_ID, INSTANCE_PROPERTY_NAME, NEW_INSTANCE_SINGLE_VALUED_PROPERTY_NAME);
		request.setNewInstanceDefinitionId(NEW_INSTANCE_DEFINITION_ID);

		PropertiesValuesEvaluatorResponse result = propertiesValuesEvaluatorRest.eval(request);

		assertPropertyValue(result, (Serializable) Collections.singleton(INSTANCE_PROPERTY_VALUE));
	}

	@Test
	public void should_NotEvaluatePropertyValue_When_PropertyIsNull() {
		InstanceBuilder instanceBuilder = new InstanceBuilder(INSTANCE_ID);
		instanceBuilder.addProperty(REAL_INSTANCE_PROPERTY_NAME, null, INSTANCE_MULTI_VALUED_PROPERTY_EVALUATED_VALUE);
		setUpInstanceResolver(Collections.singletonList(instanceBuilder));

		PropertiesValuesEvaluatorRequest request = new PropertiesValuesEvaluatorRequest();
		request.addObjectProperty(INSTANCE_ID, INSTANCE_PROPERTY_NAME, NEW_INSTANCE_SINGLE_VALUED_PROPERTY_NAME);
		request.setNewInstanceDefinitionId(NEW_INSTANCE_DEFINITION_ID);
		PropertiesValuesEvaluatorResponse result = propertiesValuesEvaluatorRest.eval(request);
		assertPropertyValue(result, null);
	}

	@Test
	public void should_NotEvaluatePropertyValue_When_PropertyOfNewInstanceIsSingleValuedControlButValueIsCollection() {
		InstanceBuilder instanceBuilder = new InstanceBuilder(INSTANCE_ID);
		instanceBuilder.addProperty(REAL_INSTANCE_PROPERTY_NAME, (Serializable) INSTANCE_MULTI_VALUED_PROPERTY_VALUE,
				INSTANCE_MULTI_VALUED_PROPERTY_EVALUATED_VALUE);
		setUpInstanceResolver(Collections.singletonList(instanceBuilder));
		setupPropertyDefinition(NEW_INSTANCE_SINGLE_VALUED_PROPERTY_NAME, false);
		PropertiesValuesEvaluatorRequest request = new PropertiesValuesEvaluatorRequest();
		request.addObjectProperty(INSTANCE_ID, INSTANCE_PROPERTY_NAME, NEW_INSTANCE_SINGLE_VALUED_PROPERTY_NAME);
		request.setNewInstanceDefinitionId(NEW_INSTANCE_DEFINITION_ID);

		PropertiesValuesEvaluatorResponse result = propertiesValuesEvaluatorRest.eval(request);

		assertPropertyValue(result, null);
	}

	@Test
	public void should_EvaluatePropertyValue_When_PropertyOfNewInstanceIsValueSuggestControlAndSingleValued() {
		InstanceBuilder instanceBuilder = new InstanceBuilder(INSTANCE_ID);
		instanceBuilder.addProperty(REAL_INSTANCE_PROPERTY_NAME, (Serializable) INSTANCE_MULTI_VALUED_PROPERTY_VALUE,
				INSTANCE_MULTI_VALUED_PROPERTY_EVALUATED_VALUE);
		setUpInstanceResolver(Collections.singletonList(instanceBuilder));
		setupPropertyDefinition(NEW_INSTANCE_VALUE_SUGGEST_PROPERTY_NAME, false, DEFAULT_VALUE_PATTERN);
		PropertiesValuesEvaluatorRequest request = new PropertiesValuesEvaluatorRequest();
		request.addObjectProperty(INSTANCE_ID, INSTANCE_PROPERTY_NAME, NEW_INSTANCE_VALUE_SUGGEST_PROPERTY_NAME);
		request.setNewInstanceDefinitionId(NEW_INSTANCE_DEFINITION_ID);

		PropertiesValuesEvaluatorResponse eval = propertiesValuesEvaluatorRest.eval(request);
		assertPropertyValue(eval, INSTANCE_MULTI_VALUED_PROPERTY_EVALUATED_VALUE);
	}

	@Test
	public void should_EvaluatePropertyValue_When_PropertyOfNewInstanceIsValueSuggestControlAndMultiValued() {
		InstanceBuilder instanceBuilder = new InstanceBuilder(INSTANCE_ID);
		instanceBuilder.addProperty(REAL_INSTANCE_PROPERTY_NAME, (Serializable) INSTANCE_MULTI_VALUED_PROPERTY_VALUE,
				INSTANCE_MULTI_VALUED_PROPERTY_EVALUATED_VALUE);
		setUpInstanceResolver(Collections.singletonList(instanceBuilder));
		setupPropertyDefinition(NEW_INSTANCE_VALUE_SUGGEST_PROPERTY_NAME, true, DEFAULT_VALUE_PATTERN);
		PropertiesValuesEvaluatorRequest request = new PropertiesValuesEvaluatorRequest();
		request.addObjectProperty(INSTANCE_ID, INSTANCE_PROPERTY_NAME, NEW_INSTANCE_VALUE_SUGGEST_PROPERTY_NAME);
		request.setNewInstanceDefinitionId(NEW_INSTANCE_DEFINITION_ID);

		PropertiesValuesEvaluatorResponse eval = propertiesValuesEvaluatorRest.eval(request);

		assertPropertyValue(eval, INSTANCE_MULTI_VALUED_PROPERTY_EVALUATED_VALUE);
	}

	@Test
	public void should_NotEvaluatePropertyValue_When_PropertyOfNewInstanceIsMultiValuedControl() {
		InstanceBuilder instanceBuilder = new InstanceBuilder(INSTANCE_ID);
		instanceBuilder.addProperty(REAL_INSTANCE_PROPERTY_NAME, (Serializable) INSTANCE_MULTI_VALUED_PROPERTY_VALUE,
				INSTANCE_MULTI_VALUED_PROPERTY_EVALUATED_VALUE);
		setUpInstanceResolver(Collections.singletonList(instanceBuilder));
		setupPropertyDefinition(NEW_INSTANCE_MULTI_VALUED_PROPERTY_NAME, true);
		PropertiesValuesEvaluatorRequest request = new PropertiesValuesEvaluatorRequest();
		request.addObjectProperty(INSTANCE_ID, INSTANCE_PROPERTY_NAME, NEW_INSTANCE_MULTI_VALUED_PROPERTY_NAME);
		request.setNewInstanceDefinitionId(NEW_INSTANCE_DEFINITION_ID);

		PropertiesValuesEvaluatorResponse result = propertiesValuesEvaluatorRest.eval(request);

		assertPropertyValue(result, (Serializable) INSTANCE_MULTI_VALUED_PROPERTY_VALUE);
	}

	@Test
	public void should_NotEvaluatePropertyValue_When_PropertyOfNewInstanceIsSingleValuedControl() {
		setupInstanceBuilder();
		setupPropertyDefinition(NEW_INSTANCE_SINGLE_VALUED_PROPERTY_NAME, false);
		PropertiesValuesEvaluatorRequest request = new PropertiesValuesEvaluatorRequest();
		request.addObjectProperty(INSTANCE_ID, INSTANCE_PROPERTY_NAME, NEW_INSTANCE_SINGLE_VALUED_PROPERTY_NAME);
		request.setNewInstanceDefinitionId(NEW_INSTANCE_DEFINITION_ID);

		PropertiesValuesEvaluatorResponse result = propertiesValuesEvaluatorRest.eval(request);

		assertPropertyValue(result, INSTANCE_PROPERTY_VALUE);
	}

	@Test
	public void should_EvaluatePropertyValue_When_PropertyOfNewInstanceIsValueSuggestControlAndSingledValued() {
		setupInstanceBuilder();
		setupPropertyDefinition(NEW_INSTANCE_VALUE_SUGGEST_PROPERTY_NAME, false, DEFAULT_VALUE_PATTERN);
		PropertiesValuesEvaluatorRequest request = new PropertiesValuesEvaluatorRequest();
		request.addObjectProperty(INSTANCE_ID, INSTANCE_PROPERTY_NAME, NEW_INSTANCE_VALUE_SUGGEST_PROPERTY_NAME);
		request.setNewInstanceDefinitionId(NEW_INSTANCE_DEFINITION_ID);

		PropertiesValuesEvaluatorResponse eval = propertiesValuesEvaluatorRest.eval(request);

		assertPropertyValue(eval, INSTANCE_PROPERTY_EVALUATED_VALUE);
	}

	private void setupPropertyDefinition(String propertyName, boolean isMultiValued,
			String controlDefinitionIdentifier) {
		PropertyDefinition propertyDefinition = Mockito.mock(PropertyDefinition.class);
		Mockito.when(propertyDefinition.isMultiValued()).thenReturn(isMultiValued);

		ControlDefintionMock controlDefinition = new ControlDefintionMock();
		ControlParamMock controlParameter = new ControlParamMock();
		controlParameter.setName("testField");
		controlParameter.setValue("test");
		controlParameter.setType(DEFAULT_VALUE_PATTERN);
		List<ControlParam> params = Collections.singletonList(controlParameter);
		controlDefinition.setControlParams(params);

		ControlDefinition controlDef = Mockito.mock(ControlDefinition.class);
		Mockito.when(propertyDefinition.getControlDefinition()).thenReturn(controlDefinition);
		Mockito.when(controlDef.getIdentifier()).thenReturn(controlDefinitionIdentifier);
		Mockito.when(definitionModel.getField(propertyName)).thenReturn(Optional.of(propertyDefinition));
	}

	private void setupPropertyDefinition(String propertyName, boolean isMultiValued) {
		PropertyDefinition propertyDefinition = Mockito.mock(PropertyDefinition.class);
		Mockito.when(propertyDefinition.isMultiValued()).thenReturn(isMultiValued);
		Mockito.when(definitionModel.getField(propertyName)).thenReturn(Optional.of(propertyDefinition));
	}

	private static void assertPropertyValue(PropertiesValuesEvaluatorResponse result, Serializable propertyValue) {
		List<Map<String, Object>> instancesData = result.getInstancesData();
		Assert.assertTrue(instancesData.size() == 1);
		Map<String, Object> instanceData = instancesData.get(0);
		Assert.assertEquals(INSTANCE_ID, instanceData.get(JsonKeys.ID));
		List<Map<String, Serializable>> instanceProperties = (List<Map<String, Serializable>>) instanceData
				.get(JsonKeys.PROPERTIES);
		Assert.assertTrue(instanceProperties.size() == 1);
		Map<String, Serializable> property = instanceProperties.get(0);
		Assert.assertEquals(propertyValue, property.get(JsonKeys.PROPERTY_VALUE));
	}

	private InstanceBuilder setupInstanceBuilder() {
		InstanceBuilder instanceBuilder = new InstanceBuilder(INSTANCE_ID);
		instanceBuilder.addProperty(REAL_INSTANCE_PROPERTY_NAME, INSTANCE_PROPERTY_VALUE,
				INSTANCE_PROPERTY_EVALUATED_VALUE);
		setUpInstanceResolver(Collections.singletonList(instanceBuilder));

		return instanceBuilder;
	}

	private void setUpInstanceResolver(List<InstanceBuilder> instanceBuilders) {
		Map<Serializable, Instance> resolvedInstances = instanceBuilders.stream().map(InstanceBuilder::getInstance)
				.collect(Collectors.toMap(Instance::getId, instance -> instance));

		Mockito.when(instanceTypeResolver.resolveInstances(Matchers.anyCollection())).then(invocation -> {
			Collection<String> instancesIds = invocation.getArgumentAt(0, List.class);
			return instancesIds.stream().map(resolvedInstances::get).collect(Collectors.toList());
		});
	}

	private class InstanceBuilder {

		private Instance instance;
		private DefinitionModel localdefinitionModel;

		public InstanceBuilder(String instanceId) {
			instance = Mockito.mock(Instance.class);
			Mockito.when(instance.getId()).thenReturn(instanceId);
			localdefinitionModel = Mockito.mock(DefinitionModel.class);
			Mockito.when(definitionService.getInstanceDefinition(instance)).thenReturn(localdefinitionModel);
		}

		public InstanceBuilder addProperty(String propertyName, Serializable propertyValue,
				String evaluatedPropertyValue) {
			Mockito.when(instance.get(propertyName)).thenReturn(propertyValue);
			PropertyDefinition source = Mockito.mock(PropertyDefinition.class);
			Mockito.when(localdefinitionModel.getField(propertyName)).thenReturn(Optional.of(source));
			Mockito.when(expressionTemplateService.evaluate(eq(source), anyObject(), eq(instance), eq(propertyName)))
					.thenReturn(evaluatedPropertyValue);
			return this;
		}

		public Instance getInstance() {
			return instance;
		}
	}
}