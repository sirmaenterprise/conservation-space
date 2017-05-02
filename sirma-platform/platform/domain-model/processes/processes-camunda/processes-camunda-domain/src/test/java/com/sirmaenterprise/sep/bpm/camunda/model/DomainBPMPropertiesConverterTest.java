package com.sirmaenterprise.sep.bpm.camunda.model;

import static com.sirmaenterprise.sep.bpm.camunda.model.DomainBPMPropertiesConverter.mergeProperties;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import com.sirma.itt.seip.definition.DictionaryService;
import com.sirma.itt.seip.domain.definition.DefinitionModel;
import com.sirma.itt.seip.domain.definition.PropertyDefinition;
import com.sirma.itt.seip.domain.instance.Instance;
import com.sirmaenterprise.sep.bpm.camunda.exception.CamundaIntegrationRuntimeException;
import com.sun.org.apache.xpath.internal.operations.Bool;
import org.camunda.bpm.engine.form.FormField;
import org.camunda.bpm.engine.variable.impl.VariableMapImpl;
import org.camunda.bpm.engine.variable.impl.value.ObjectValueImpl;
import org.camunda.bpm.engine.variable.impl.value.PrimitiveTypeValueImpl;
import org.camunda.bpm.engine.variable.impl.value.UntypedValueImpl;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.internal.verification.AtMost;
import org.mockito.runners.MockitoJUnitRunner;

import com.sirma.itt.seip.convert.TypeConverter;

/**
 * @author bbanchev
 */
@RunWith(MockitoJUnitRunner.class)
public class DomainBPMPropertiesConverterTest {
	@Mock
	private TypeConverter typeConverter;
	@Mock
	private DictionaryService dictionaryService;

	@InjectMocks
	private DomainBPMPropertiesConverter domainBPMPropertiesConverter;

	@Before
	public void setUp() throws Exception {
		MockitoAnnotations.initMocks(this);
	}

	private static class TestConverter implements Serializable {
		private static final long serialVersionUID = 6855186991848387161L;

	}

	/**
	 * Test method for
	 * {@link com.sirmaenterprise.sep.bpm.camunda.model.DomainBPMPropertiesConverter#convertDataFromSEIPtoCamunda(java.util.Map, java.util.List)}.
	 */
	@Test
	public void testConvertDataFromSEIPtoCamundaNoFilter() throws Exception {
		Map<String, Serializable> properties = new HashMap<String, Serializable>();
		Map<String, Object> fromSEIPtoCamunda = domainBPMPropertiesConverter.convertDataFromSEIPtoCamunda(properties,
				null);
		assertEquals(0, fromSEIPtoCamunda.size());
		fillDefaultProperties(properties);
		fromSEIPtoCamunda = domainBPMPropertiesConverter.convertDataFromSEIPtoCamunda(properties, null);
		assertEquals(properties.size(), fromSEIPtoCamunda.size());
		verify(typeConverter, new AtMost(0)).convert(any(), any());
		TestConverter converted = new TestConverter();
		properties.put("object", converted);
		fromSEIPtoCamunda = domainBPMPropertiesConverter.convertDataFromSEIPtoCamunda(properties, null);
		assertEquals(properties.size(), fromSEIPtoCamunda.size());
		verify(typeConverter).convert(eq(String.class), eq(converted));
	}

	@Test
	public void testConvertDataFromSEIPtoCamundaWithFilter() throws Exception {
		Map<String, Serializable> properties = new HashMap<String, Serializable>();
		fillDefaultProperties(properties);
		List<FormField> filter = new LinkedList<>();
		FormField field1 = mock(FormField.class);
		FormField field2 = mock(FormField.class);
		FormField field3 = mock(FormField.class);
		filter.add(field1);
		filter.add(field2);
		filter.add(field3);
		when(field1.getId()).thenReturn("String");
		when(field2.getId()).thenReturn("List1");
		when(field3.getId()).thenReturn("null");
		Map<String, Object> fromSEIPtoCamunda = domainBPMPropertiesConverter.convertDataFromSEIPtoCamunda(properties,
				filter);
		assertEquals(2, fromSEIPtoCamunda.size());
	}

	private void fillDefaultProperties(Map<String, Serializable> properties) {
		properties.put("String", "value");
		properties.put("Date", new Date());
		properties.put("Integer", new Integer(1));
		properties.put("Double", new Double(1d));
		properties.put("Float", new Float(1f));
		properties.put("Map1", new HashMap<>());
		properties.put("Map2", new LinkedHashMap<>());
		properties.put("List1", new LinkedList<>());
		properties.put("List2", new ArrayList<>());
	}

	/**
	 * Test method for
	 * {@link com.sirmaenterprise.sep.bpm.camunda.model.DomainBPMPropertiesConverter#convertDataFromCamundaToSEIP(org.camunda.bpm.engine.variable.VariableMap, com.sirma.itt.seip.domain.instance.Instance)}.
	 */
	@Test
	public void testConvertDataFromCamundaToSEIP() throws Exception {
		Map<String, Serializable> fromCamundaToSEIP = domainBPMPropertiesConverter.convertDataFromCamundaToSEIP(null,
				null);
		assertNotNull(fromCamundaToSEIP);
		assertEquals(0, fromCamundaToSEIP.size());
		VariableMapImpl source = new VariableMapImpl();
		source.put("string", new PrimitiveTypeValueImpl.StringValueImpl("value1"));
		source.put("date", new PrimitiveTypeValueImpl.DateValueImpl(new Date()));
		source.put("unserialized", new UntypedValueImpl(new Object()));
		fromCamundaToSEIP = domainBPMPropertiesConverter.convertDataFromCamundaToSEIP(source, null);
		assertNotNull(fromCamundaToSEIP);
		assertEquals(2, fromCamundaToSEIP.size());
	}

	@Test
	public void testConvertDataFromCamundaToSEIP_with_target_instance_no_definition() {
		Instance target = mock(Instance.class);
		when(dictionaryService.getInstanceDefinition(target)).thenReturn(null);
		VariableMapImpl source = new VariableMapImpl();
		source.put("string", new PrimitiveTypeValueImpl.StringValueImpl("value1"));
		source.put("date", new PrimitiveTypeValueImpl.DateValueImpl(new Date()));
		source.put("unserialized", new UntypedValueImpl(new Object()));
		Map<String, Serializable> fromCamundaToSEIP = domainBPMPropertiesConverter.convertDataFromCamundaToSEIP(source, target);
		assertNotNull(fromCamundaToSEIP);
		assertEquals(2, fromCamundaToSEIP.size());
	}

	@Test
	public void testConvertDataFromCamundaToSEIP_with_target_instance_with_definition() {
		Instance target = mock(Instance.class);
		DefinitionModel definitionModel = mock(DefinitionModel.class);
		when(dictionaryService.getInstanceDefinition(target)).thenReturn(definitionModel);

		Map<String, PropertyDefinition> model = new HashMap<>(4);
		VariableMapImpl source = new VariableMapImpl();

		source.put("string", new PrimitiveTypeValueImpl.StringValueImpl("value1"));
		PropertyDefinition stringPropDef = mock(PropertyDefinition.class);
		when(stringPropDef.isMultiValued()).thenReturn(Boolean.FALSE);
		model.put("string", stringPropDef);

		source.put("date", new PrimitiveTypeValueImpl.DateValueImpl(new Date()));
		PropertyDefinition datePropDef = mock(PropertyDefinition.class);
		when(datePropDef.isMultiValued()).thenReturn(Boolean.FALSE);
		model.put("date", datePropDef);

		List<String> list1 = new ArrayList(1);
		list1.add("list1");
		source.put("List1", new ObjectValueImpl(list1));
		PropertyDefinition listPropDef = mock(PropertyDefinition.class);
		when(listPropDef.isMultiValued()).thenReturn(Boolean.TRUE);
		model.put("List1", listPropDef);

		source.put("string1", new PrimitiveTypeValueImpl.StringValueImpl("value1"));
		PropertyDefinition string1PropDef = mock(PropertyDefinition.class);
		when(string1PropDef.isMultiValued()).thenReturn(Boolean.TRUE);
		model.put("string1", string1PropDef);

		source.put("unserialized", new UntypedValueImpl(new Object()));

		when(definitionModel.getFieldsAsMap()).thenReturn(model);
		Map<String, Serializable> fromCamundaToSEIP = domainBPMPropertiesConverter.convertDataFromCamundaToSEIP(source, target);
		assertNotNull(fromCamundaToSEIP);
		assertEquals(4, fromCamundaToSEIP.size());
	}

	@Test(expected = CamundaIntegrationRuntimeException.class)
	public void testConvertDataFromCamundaToSEIP_with_colletion_value() {
		Instance target = mock(Instance.class);
		DefinitionModel definitionModel = mock(DefinitionModel.class);
		when(dictionaryService.getInstanceDefinition(target)).thenReturn(definitionModel);

		Map<String, PropertyDefinition> model = new HashMap<>(1);
		VariableMapImpl source = new VariableMapImpl();

		List<String> list1 = new ArrayList(1);
		list1.add("list1");
		source.put("List1", new ObjectValueImpl(list1));
		PropertyDefinition listPropDef = mock(PropertyDefinition.class);
		when(listPropDef.isMultiValued()).thenReturn(Boolean.FALSE);
		model.put("List1", listPropDef);

		when(definitionModel.getFieldsAsMap()).thenReturn(model);
		domainBPMPropertiesConverter.convertDataFromCamundaToSEIP(source, target);
	}

	@Test
	public void testMergePropertiesScenarioEquals() throws Exception {
		HashMap<String, Object> source = generateBaseMap();
		HashMap<String, Object> target = generateBaseMap();
		assertFalse(mergeProperties(target, source));
	}

	@Test
	public void testMergePropertiesScenarioNonEqualCollectionModified1() throws Exception {
		HashMap<String, Object> source = generateBaseMap();
		HashMap<String, Object> target = generateBaseMap();
		((Collection) source.get("collection")).add("val2");
		assertTrue(mergeProperties(target, source));
	}

	@Test
	public void testMergePropertiesScenarioNonEqualCollectionModified2() throws Exception {
		HashMap<String, Object> source = generateBaseMap();
		HashMap<String, Object> target = generateBaseMap();
		((Collection) target.get("collection")).clear();
		assertTrue(mergeProperties(target, source));
	}

	@Test
	public void testMergePropertiesScenarioNonEqualCollectionNonModified() throws Exception {
		HashMap<String, Object> source = generateBaseMap();
		HashMap<String, Object> target = generateBaseMap();
		((Collection) target.get("collection")).add("val2");
		assertFalse(mergeProperties(target, source));
	}

	private HashMap<String, Object> generateBaseMap() {
		HashMap<String, Object> map = new HashMap<>();
		map.put("id1", "val1");
		map.put("id2", "val2");
		List<String> sourceCollection = new LinkedList<>();
		sourceCollection.add("val1");
		map.put("collection", sourceCollection);
		return map;
	}

}
