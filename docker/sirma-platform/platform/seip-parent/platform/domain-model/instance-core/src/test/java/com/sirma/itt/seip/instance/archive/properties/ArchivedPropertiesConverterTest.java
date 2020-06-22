package com.sirma.itt.seip.instance.archive.properties;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.io.Serializable;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.AdditionalAnswers;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import com.sirma.itt.seip.convert.TypeConverter;
import com.sirma.itt.seip.convert.TypeConverterUtil;
import com.sirma.itt.seip.definition.DefinitionService;
import com.sirma.itt.seip.domain.definition.PrototypeDefinition;
import com.sirma.itt.seip.domain.instance.EmfInstance;
import com.sirma.itt.seip.instance.properties.entity.ValueTypeConverter.ValueType;
import com.sirma.itt.seip.testutil.mocks.DataTypeDefinitionMock;
import com.sirma.itt.seip.testutil.mocks.DefinitionMock;

/**
 * Unit test for {@link ArchivedPropertiesConverter}.
 *
 * @author A. Kunchev
 */
@RunWith(MockitoJUnitRunner.class)
public class ArchivedPropertiesConverterTest {

	@InjectMocks
	private ArchivedPropertiesConverter converter;

	@Mock
	private DefinitionService definitionService;

	@Mock
	private TypeConverter typeConverter;

	@Test
	public void toPersistent() {
		when(definitionService.getInstanceDefinition(any())).thenReturn(new DefinitionMock());
		PrototypeDefinition prototypeMock = createPrototypeMock();
		when(definitionService.getPrototype(anyString(), any())).thenReturn(prototypeMock);
		EmfInstance version = new EmfInstance();
		version.add("title", "Light");

		Map<String, Serializable> result = converter.toPersistent(version);

		assertFalse(result.isEmpty());
		assertTrue(result.containsKey("title"));
		assertNotNull(result.get("title"));
	}

	private static PrototypeDefinition createPrototypeMock() {
		PrototypeDefinition prototype = mock(PrototypeDefinition.class);
		when(prototype.getDataType()).thenReturn(new DataTypeDefinitionMock(ValueType.STRING.toString()));
		return prototype;
	}

	@Test
	public void toInstanceProperties() {
		TypeConverterUtil.setTypeConverter(typeConverter);
		when(typeConverter.convert(String.class, "Light")).then(AdditionalAnswers.returnsSecondArg());
		LinkedHashMap<Object, Object> value = new LinkedHashMap<>();
		value.put("value", "Light");
		value.put("type", "text");
		Map<String, Serializable> toConvert = Collections.singletonMap("title", value);
		Map<String, Serializable> result = converter.toInstanceProperties(toConvert);

		assertFalse(result.isEmpty());
		assertTrue(result.containsKey("title"));
		assertEquals("Light", result.get("title"));
	}
}