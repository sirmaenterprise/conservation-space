package com.sirma.sep.instance.properties.expression.evaluation;

import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.mockito.Spy;

import com.sirma.itt.seip.definition.DefinitionService;
import com.sirma.itt.seip.domain.definition.DefinitionModel;
import com.sirma.itt.seip.domain.definition.PropertyDefinition;
import com.sirma.itt.seip.domain.instance.Instance;
import com.sirma.itt.seip.exception.EmfRuntimeException;
import com.sirma.itt.seip.plugin.Plugins;
import com.sirma.sep.instance.properties.expression.evaluation.PropertiesValuesEvaluatorServiceImpl;
import com.sirmaenterprise.sep.properties.expression.evaluation.PropertyValueEvaluator;

/**
 * Test for {@link PropertiesValuesEvaluatorServiceImpl}.
 *
 * @author <a href="mailto:ivo.rusev@sirma.bg">Ivo Rusev</a>
 * @since 17/07/2017
 */
public class PropertiesValuesEvaluatorServiceImplTest {

	@InjectMocks
	private PropertiesValuesEvaluatorServiceImpl propertiesValuesEvaluatorService;

	private List<PropertyValueEvaluator> plugins = new ArrayList<>();

	@Spy
	private Plugins<PropertyValueEvaluator> propertyEvaluators = new Plugins<>("", plugins);

	@Mock
	private PropertyValueEvaluator evaluatorOne;
	@Mock
	private PropertyValueEvaluator evaluatorTwo;
	@Mock
	private DefinitionService definitionService;

	@Mock
	private Instance instance;
	@Mock
	private DefinitionModel definitionModel;
	@Mock
	private PropertyDefinition propertyDefinition;

	@Before
	public void init() {
		propertiesValuesEvaluatorService = new PropertiesValuesEvaluatorServiceImpl();
		MockitoAnnotations.initMocks(this);

		when(definitionService.getInstanceDefinition(instance)).thenReturn(definitionModel);
		when(definitionModel.getField("fieldNameOne")).thenReturn(Optional.of(propertyDefinition));
		when(definitionModel.getField("fieldNameTwo")).thenReturn(Optional.of(propertyDefinition));

		when(evaluatorOne.canEvaluate(any(), any())).thenReturn(true);
		when(evaluatorTwo.canEvaluate(any(), any())).thenReturn(true);
		when(evaluatorOne.evaluate(any(), any())).thenReturn("one");
		when(evaluatorTwo.evaluate(any(), any())).thenReturn(2);

		plugins.add(evaluatorOne);
		plugins.add(evaluatorTwo);
	}

	@Test(expected = EmfRuntimeException.class)
	public void should_ThrowEmfRuntimeException_When_DefinitionNotFound() {
		propertiesValuesEvaluatorService.evaluate(Mockito.mock(Instance.class), Collections.emptyList());
	}

	@Test
	public void evaluate_usingPropertyDefinition() {
		Serializable fieldName = propertiesValuesEvaluatorService.evaluate(propertyDefinition, null, instance,
				"fieldNameOne");
		assertEquals(fieldName, "one");
		verify(evaluatorOne).canEvaluate(propertyDefinition, null);
		verify(evaluatorOne).evaluate(instance, "fieldNameOne");
	}

	@Test
	public void evaluate_multipleProperties() {
		List<String> fields = Arrays.asList("fieldNameOne", "fieldNameTwo");
		Map<String, Serializable> evaluate = propertiesValuesEvaluatorService.evaluate(instance, fields);
		assertEquals(2, evaluate.size());
		verify(evaluatorOne, times(2)).canEvaluate(propertyDefinition, null);
		verify(evaluatorOne, times(1)).evaluate(instance, "fieldNameOne");
		verify(evaluatorOne, times(1)).evaluate(instance, "fieldNameTwo");
	}
}