/*
 *
 */
package com.sirma.itt.cmf.testutil;

import static org.mockito.Matchers.anyString;

import java.util.Date;
import java.util.Map;

import org.mockito.Mockito;

import com.sirma.itt.commons.utils.reflection.ReflectionUtils;
import com.sirma.itt.commons.utils.string.StringUtils;
import com.sirma.itt.seip.collections.CollectionUtils;
import com.sirma.itt.seip.convert.TypeConverter;
import com.sirma.itt.seip.definition.DictionaryService;
import com.sirma.itt.seip.domain.definition.DataTypeDefinition;
import com.sirma.itt.seip.domain.instance.InitializedInstance;
import com.sirma.itt.seip.domain.instance.Instance;
import com.sirma.itt.seip.instance.convert.InstanceToInstanceReferenceConverterProvider;
import com.sirma.itt.seip.model.DataType;
import com.sirma.itt.seip.model.LinkSourceId;
import com.sirma.itt.seip.testutil.EmfTest;

/**
 * The Class CmfTest.
 *
 * @author BBonev
 */
public abstract class CmfTest extends EmfTest {

	private static Map<String, String> typeMapping = CollectionUtils.addToMap(null);

	@Override
	public TypeConverter createTypeConverter() {
		TypeConverter typeConverter = super.createTypeConverter();

		InstanceToInstanceReferenceConverterProvider converterProvider = new InstanceToInstanceReferenceConverterProvider();
		DictionaryService service = Mockito.mock(DictionaryService.class);
		Mockito.when(service.getDataTypeDefinition(anyString())).then(invocation -> {
			String name = (String) invocation.getArguments()[0];
			if (StringUtils.isNullOrEmpty(name)) {
				return null;
			}
			String localName = name;
			if (typeMapping.containsKey(localName)) {
				localName = typeMapping.get(localName);
			}

			if (localName.indexOf(".", 1) > 0) {
				// probably we can cache the data type instance not to create it every
				// time
				DataType dataType = new DataType();
				if (Date.class.toString().equals(localName)) {
					dataType.setName(DataTypeDefinition.DATETIME);
				}
				dataType.setJavaClassName(localName);
				dataType.setName(localName.substring(localName.lastIndexOf(".") + 1));
				return dataType;
			}
			return null;
		});

		// instance is not found
		typeConverter.addConverter(LinkSourceId.class, InitializedInstance.class, source -> {
			if (!idManager.isIdRegistered(source.getIdentifier()) && source.getReferenceType() != null) {
				if (source.getReferenceType().getJavaClassName() != null) {
					try {
						Class<?> instanceClass = Class.forName(source.getReferenceType().getJavaClassName());
						Instance instance = (Instance) com.sirma.itt.seip.util.ReflectionUtils
								.newInstance(instanceClass);
						instance.setId(source.getIdentifier());
						return new InitializedInstance(instance);
					} catch (ClassNotFoundException e) {
						e.printStackTrace();
					}
				}
			}
			return new InitializedInstance(null);
		});

		ReflectionUtils.setField(converterProvider, "dictionaryService", service);
		converterProvider.register(typeConverter);

		return typeConverter;
	}
}
