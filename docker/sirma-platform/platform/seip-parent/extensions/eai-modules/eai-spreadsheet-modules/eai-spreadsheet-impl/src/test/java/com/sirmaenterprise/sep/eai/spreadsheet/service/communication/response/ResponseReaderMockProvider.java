package com.sirmaenterprise.sep.eai.spreadsheet.service.communication.response;

import static com.sirma.itt.seip.domain.instance.DefaultProperties.TITLE;
import static com.sirma.itt.seip.domain.instance.DefaultProperties.TYPE;
import static com.sirma.itt.seip.domain.instance.DefaultProperties.UNIQUE_IDENTIFIER;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.io.Serializable;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;
import java.util.function.Predicate;
import java.util.stream.Stream;

import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

import com.sirma.itt.seip.domain.definition.DataTypeDefinition;
import com.sirma.itt.seip.domain.definition.DefinitionModel;
import com.sirma.itt.seip.domain.definition.PropertyDefinition;
import com.sirma.itt.seip.domain.instance.ClassInstance;
import com.sirma.itt.seip.domain.instance.Instance;
import com.sirma.itt.seip.domain.instance.InstanceType;
import com.sirma.itt.seip.eai.model.mapping.EntityProperty;
import com.sirma.itt.seip.eai.model.mapping.EntityProperty.EntityPropertyMapping;
import com.sirma.itt.seip.eai.model.mapping.EntityType;
import com.sirma.itt.seip.eai.service.model.ModelConfiguration;
import com.sirmaenterprise.sep.eai.spreadsheet.model.response.SpreadsheetEntry;

public class ResponseReaderMockProvider {

	static Instance mockInstance(String defId, String id, boolean isCreatable, boolean isUploadable) {
		Instance createdInstance = mock(Instance.class);
		InstanceType instanceType = mock(InstanceType.class);
		HashMap<String, Serializable> properties = new HashMap<>();
		when(createdInstance.getProperties()).thenReturn(properties);
		when(createdInstance.type()).thenReturn(instanceType);
		when(createdInstance.getIdentifier()).thenReturn(defId);
		when(createdInstance.getId()).thenReturn(id);
		// ArrayList<String> uris = new ArrayList<>();
		// uris.add("emf:123asd123");
		// when(createdInstance.get(anyString())).thenReturn(uris);
		when(instanceType.isCreatable()).thenReturn(isCreatable);
		when(instanceType.isUploadable()).thenReturn(isUploadable);
		return createdInstance;
	}

	static DefinitionModel mockDefinitionModel(String id, PropertyDefinition... defaultProperties) {
		DefinitionModel definitionModel = mock(DefinitionModel.class);
		when(definitionModel.getIdentifier()).thenReturn(id);
		setDefinitionFieldsStream(definitionModel, defaultProperties);
		return definitionModel;
	}

	static void setDefinitionFieldsStream(DefinitionModel definitionModel, PropertyDefinition... properties) {
		when(definitionModel.fieldsStream()).thenAnswer(new Answer<Stream<PropertyDefinition>>() {
			@Override
			public Stream<PropertyDefinition> answer(InvocationOnMock invocation) throws Throwable {
				return Stream.of(properties);
			}
		});
	}

	static PropertyDefinition mockPropertyDefinition(String uri, String type, Integer codelist) {
		PropertyDefinition property = mock(PropertyDefinition.class);
		when(property.getUri()).thenReturn(uri);
		when(property.getName()).thenReturn(uri.substring(uri.indexOf(':') + 1, uri.length()));
		when(property.getIdentifier()).thenReturn(uri);
		when(property.isMandatory()).thenReturn(true);
		when(property.getCodelist()).thenReturn(codelist);
		DataTypeDefinition dataType = mock(DataTypeDefinition.class);
		when(dataType.getJavaClass()).thenAnswer(new Answer<Class<?>>() {
			@Override
			public Class<?> answer(InvocationOnMock invocation) throws Throwable {
				return Object.class;
			}
		});
		when(dataType.getName()).thenReturn(type);
		when(property.getDataType()).thenReturn(dataType);
		return property;
	}

	static EntityType mockEntityType(ModelConfiguration modelConfiguration, String id, ClassInstance cls,
			EntityProperty... entityProperties) {
		EntityType entityType = mock(EntityType.class);
		List<EntityProperty> asList = Arrays.asList(entityProperties);
		when(entityType.getIdentifier()).thenReturn(id);
		when(entityType.getUri()).thenReturn("emf:" + id);
		when(entityType.getTitle()).thenReturn("Title_" + id);
		when(entityType.getProperties()).thenReturn(asList);
		when(modelConfiguration.getTypeByExternalName(eq(id))).thenReturn(entityType);
		when(modelConfiguration.getTypeByDefinitionId(eq(id))).thenReturn(entityType);
		when(cls.getId()).thenReturn("emf:" + id);
		return entityType;
	}

	static EntityProperty mockPropertyType(ModelConfiguration modelConfiguration, String definitionId, String ns,
			String id, Integer codelist, boolean mandatory) {
		EntityProperty entityProperty = mock(EntityProperty.class);
		when(entityProperty.getPropertyId()).thenReturn(id);
		String uri = ns + id;
		when(entityProperty.getUri()).thenReturn(uri);
		when(entityProperty.isMandatory()).thenReturn(mandatory);
		when(entityProperty.getCodelist()).thenReturn(codelist);
		when(entityProperty.getTitle()).thenReturn(id + "(" + uri + ")");
		when(entityProperty.getMapping(eq(EntityPropertyMapping.AS_DATA))).thenReturn(uri);
		when(entityProperty.getDataMapping()).thenReturn(uri);
		when(entityProperty.getMappings()).thenReturn(Collections.singletonMap(EntityPropertyMapping.AS_DATA, uri));
		if (id.equals(UNIQUE_IDENTIFIER)) {
			Predicate<EntityProperty> lambda = property -> UNIQUE_IDENTIFIER.equals(property.getPropertyId());
			when(modelConfiguration.getPropertyByFilter(eq(definitionId), eq(lambda))).thenReturn(entityProperty);
		}
		when(modelConfiguration.getPropertyByExternalName(eq(definitionId), eq(uri))).thenReturn(entityProperty);
		return entityProperty;
	}

	static SpreadsheetEntry createNewEntry(String id) {
		SpreadsheetEntry spreadsheetEntry = new SpreadsheetEntry("0", id);
		spreadsheetEntry.getProperties().put("emf:" + TYPE, "type");
		spreadsheetEntry.getProperties().put("emf:" + TITLE, "title");
		spreadsheetEntry.getProperties().put("dcterms:" + UNIQUE_IDENTIFIER, UUID.randomUUID().toString());
		return spreadsheetEntry;
	}

}
