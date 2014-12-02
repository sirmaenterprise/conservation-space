/*
 *
 */
package com.sirma.itt.cmf.testutil;

import java.util.Date;
import java.util.Map;

import org.mockito.Mockito;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

import com.sirma.itt.cmf.beans.model.CaseInstance;
import com.sirma.itt.cmf.beans.model.WorkflowInstanceContext;
import com.sirma.itt.cmf.util.datatype.InstanceToLinkSourceConverterProvider;
import com.sirma.itt.commons.utils.reflection.ReflectionUtils;
import com.sirma.itt.commons.utils.string.StringUtils;
import com.sirma.itt.emf.converter.Converter;
import com.sirma.itt.emf.converter.TypeConverter;
import com.sirma.itt.emf.db.SequenceEntityGenerator;
import com.sirma.itt.emf.definition.DictionaryService;
import com.sirma.itt.emf.definition.model.DataType;
import com.sirma.itt.emf.definition.model.DataTypeDefinition;
import com.sirma.itt.emf.domain.Pair;
import com.sirma.itt.emf.entity.LinkSourceId;
import com.sirma.itt.emf.instance.model.InitializedInstance;
import com.sirma.itt.emf.instance.model.Instance;
import com.sirma.itt.emf.util.CollectionUtils;
import com.sirma.itt.emf.util.EmfTest;

/**
 * The Class CmfTest.
 *
 * @author BBonev
 */
public abstract class CmfTest extends EmfTest {

	private static Map<String, String> typeMapping = CollectionUtils.addToMap(null,
			new Pair<String, String>("workflow", WorkflowInstanceContext.class.getName()),
			new Pair<String, String>("workflowinstance", WorkflowInstanceContext.class.getName()),
			new Pair<String, String>("case", CaseInstance.class.getName()),
			new Pair<String, String>("caseinstance", CaseInstance.class.getName()),
			new Pair<String, String>("project", "com.sirma.itt.pm.domain.model.ProjectInstance"),
			new Pair<String, String>("projectinstance", "com.sirma.itt.pm.domain.model.ProjectInstance"));

	/**
	 * {@inheritDoc}
	 */
	@Override
	public TypeConverter createTypeConverter() {
		TypeConverter typeConverter = super.createTypeConverter();

		InstanceToLinkSourceConverterProvider converterProvider = new InstanceToLinkSourceConverterProvider();
		DictionaryService service = Mockito.mock(DictionaryService.class);
		Mockito.when(service.getDataTypeDefinition(Mockito.anyString())).then(
				new Answer<DataTypeDefinition>() {

					@Override
					public DataTypeDefinition answer(InvocationOnMock invocation) throws Throwable {
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
					}

				});

		// instance is not found
		typeConverter.addConverter(LinkSourceId.class, InitializedInstance.class,
				new Converter<LinkSourceId, InitializedInstance>() {
					@Override
					public InitializedInstance convert(LinkSourceId source) {
						if (!SequenceEntityGenerator.isIdRegistered(source.getIdentifier())
								&& (source.getReferenceType() != null)) {
							if (source.getReferenceType().getJavaClassName() != null) {
								try {
									Class<?> instanceClass = Class.forName(source
											.getReferenceType().getJavaClassName());
									Instance instance = (Instance) com.sirma.itt.emf.util.ReflectionUtils
											.newInstance(instanceClass);
									instance.setId(source.getIdentifier());
									return new InitializedInstance(instance);
								} catch (ClassNotFoundException e) {
									e.printStackTrace();
								}
							}
						}
						return new InitializedInstance(null);
					}
				});

		ReflectionUtils.setField(converterProvider, "dictionaryService", service);
		converterProvider.register(typeConverter);

		return typeConverter;
	}
}
