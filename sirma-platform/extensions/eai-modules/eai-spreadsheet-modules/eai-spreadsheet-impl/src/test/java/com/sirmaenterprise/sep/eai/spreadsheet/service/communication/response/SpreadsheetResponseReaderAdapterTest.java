package com.sirmaenterprise.sep.eai.spreadsheet.service.communication.response;

import static com.sirma.itt.seip.domain.instance.DefaultProperties.TITLE;
import static com.sirma.itt.seip.domain.instance.DefaultProperties.TYPE;
import static com.sirma.itt.seip.domain.instance.DefaultProperties.UNIQUE_IDENTIFIER;
import static com.sirmaenterprise.sep.eai.spreadsheet.configuration.SpreadsheetIntegrationConfigurationProvider.SYSTEM_ID;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.doCallRealMethod;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.Serializable;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Predicate;
import java.util.stream.Stream;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Matchers;
import org.mockito.Mock;
import org.mockito.internal.verification.AtLeast;
import org.mockito.internal.verification.AtMost;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.runners.MockitoJUnitRunner;
import org.mockito.stubbing.Answer;

import com.sirma.itt.commons.utils.reflection.ReflectionUtils;
import com.sirma.itt.seip.Executable;
import com.sirma.itt.seip.convert.TypeConverter;
import com.sirma.itt.seip.definition.DictionaryService;
import com.sirma.itt.seip.definition.SemanticDefinitionService;
import com.sirma.itt.seip.domain.codelist.CodelistService;
import com.sirma.itt.seip.domain.definition.DataTypeDefinition;
import com.sirma.itt.seip.domain.definition.DefinitionModel;
import com.sirma.itt.seip.domain.definition.PropertyDefinition;
import com.sirma.itt.seip.domain.instance.ClassInstance;
import com.sirma.itt.seip.domain.instance.Instance;
import com.sirma.itt.seip.domain.instance.InstanceType;
import com.sirma.itt.seip.domain.search.SearchArguments;
import com.sirma.itt.seip.domain.search.tree.Condition;
import com.sirma.itt.seip.eai.configuration.EAIConfigurationProvider;
import com.sirma.itt.seip.eai.exception.EAIException;
import com.sirma.itt.seip.eai.exception.EAIReportableException;
import com.sirma.itt.seip.eai.model.communication.ResponseInfo;
import com.sirma.itt.seip.eai.model.mapping.EntityProperty;
import com.sirma.itt.seip.eai.model.mapping.EntityProperty.EntityPropertyMapping;
import com.sirma.itt.seip.eai.model.mapping.EntityType;
import com.sirma.itt.seip.eai.service.EAIConfigurationService;
import com.sirma.itt.seip.eai.service.model.ModelConfiguration;
import com.sirma.itt.seip.eai.service.model.ModelService;
import com.sirma.itt.seip.instance.InstanceTypeResolver;
import com.sirma.itt.seip.instance.SemanticInstanceTypes;
import com.sirma.itt.seip.instance.dao.InstanceService;
import com.sirma.itt.seip.instance.validation.Validator;
import com.sirma.itt.seip.permissions.InstanceAccessEvaluator;
import com.sirma.itt.seip.search.SearchService;
import com.sirma.itt.seip.testutil.fakes.SecurityContextManagerFake;
import com.sirma.itt.seip.testutil.fakes.TaskExecutorFake;
import com.sirma.itt.seip.testutil.mocks.ConfigurationPropertyMock;
import com.sirmaenterprise.sep.eai.spreadsheet.configuration.SpreadsheetIntegrationConfiguration;
import com.sirmaenterprise.sep.eai.spreadsheet.model.internal.SpreadsheetResultInstances;
import com.sirmaenterprise.sep.eai.spreadsheet.model.request.SpreadsheetReadServiceRequest;
import com.sirmaenterprise.sep.eai.spreadsheet.model.response.SpreadsheetEntry;
import com.sirmaenterprise.sep.eai.spreadsheet.model.response.SpreadsheetSheet;
import com.sirmaenterprise.sep.eai.spreadsheet.model.transform.SpreadsheetModelConverter;

@RunWith(MockitoJUnitRunner.class)
public class SpreadsheetResponseReaderAdapterTest {
	@Mock
	private DictionaryService dictionaryService;
	@Mock
	private SpreadsheetIntegrationConfiguration eaiConfiguration;
	@Mock
	private InstanceAccessEvaluator instanceAccessEvaluator;
	@Mock
	private InstanceService instanceService;
	@Mock
	private ModelService modelService;
	@Mock
	private InstanceTypeResolver resolver;
	@Mock
	private SemanticDefinitionService semanticDefinitionService;
	@Mock
	private SemanticInstanceTypes semanticInstanceTypes;
	@Mock
	private Validator validator;
	@Mock
	private TaskExecutorFake taskExecutor;
	@InjectMocks
	private SpreadsheetResponseReaderAdapter spreadsheetResponseReaderAdapter;
	@Mock
	private ModelConfiguration modelConfiguration;
	@Mock
	private CodelistService codelistService;
	@Mock
	private EAIConfigurationService integrationService;
	@Mock
	private EAIConfigurationProvider configurationProvider;
	@Mock
	private TypeConverter typeConverter;
	@Mock
	private RelationQueryProcessor contextQueryProcessor;
	@Mock
	private SearchService searchService;
	@Mock
	private SecurityContextManagerFake securityContextManager;
	@InjectMocks
	private SpreadsheetModelConverter modelConverter;

	@Mock
	private SpreadsheetSheet sheet;
	@Mock
	private ClassInstance typeClass;

	private ResponseInfo responseInfo;
	private static final String DEF_ID = "type";
	private PropertyDefinition[] defaultProperties = new PropertyDefinition[] {
			mockPropertyDefinition("emf:" + UNIQUE_IDENTIFIER, "an..50", null),
			mockPropertyDefinition("emf:" + TYPE, "an..50", null),
			mockPropertyDefinition("emf:" + TITLE, "an..50", null) };

	@Before
	public void prepareData() throws Exception {
		responseInfo = workingScenario();
	}

	@Test(expected = EAIException.class)
	public void testParseResponseWrongParameter() throws Exception {
		spreadsheetResponseReaderAdapter.parseResponse(mock(ResponseInfo.class));
	}

	@Test
	public void testParseResponseEmpty() throws Exception {
		sheet = mock(SpreadsheetSheet.class);
		when(sheet.getEntries()).thenReturn(Collections.emptyList());
		when(responseInfo.getResponse()).thenReturn(sheet);

		when(modelService.getModelConfiguration(Matchers.eq(SYSTEM_ID))).thenReturn(modelConfiguration);
		SpreadsheetResultInstances parseResponse = spreadsheetResponseReaderAdapter.parseResponse(responseInfo);
		assertNull(parseResponse.getInstances());
		assertNotNull("No entries to process!", parseResponse.getError());
	}

	@Test
	public void testParseResponseWithInvalidProperty() throws Exception {

		sheet.getEntries().get(0).getProperties().put("emf:unknown", "val");
		SpreadsheetResultInstances parseResponse = spreadsheetResponseReaderAdapter.parseResponse(responseInfo);
		EAIReportableException error = parseResponse.getError();
		assertNotNull(error);
		assertNotNull(parseResponse.getInstances());
		assertEquals(2, parseResponse.getInstances().size());
		verify(instanceService, new AtLeast(2)).createInstance(any(), any(), any());
	}

	@Test
	public void testParseResponseWithMissingMandatory() throws Exception {
		sheet.getEntries().get(0).getProperties().remove("emf:" + TITLE);
		SpreadsheetResultInstances parseResponse = spreadsheetResponseReaderAdapter.parseResponse(responseInfo);
		EAIReportableException error = parseResponse.getError();
		assertNotNull(error);
		assertNotNull(parseResponse.getInstances());
		assertEquals(2, parseResponse.getInstances().size());
		verify(instanceService, new AtLeast(2)).createInstance(any(), any(), any());
	}

	@Test
	public void testParseResponseNoPermissions() throws Exception {
		when(instanceAccessEvaluator.canWrite(any())).thenReturn(false);
		SpreadsheetResultInstances parseResponse = spreadsheetResponseReaderAdapter.parseResponse(responseInfo);
		EAIReportableException error = parseResponse.getError();
		assertNotNull(error);
		assertNotNull(parseResponse.getInstances());
		assertEquals(0, parseResponse.getInstances().size());
		verify(instanceService, new AtMost(0)).createInstance(any(), any(), any());
	}

	@Test
	public void testParseResponseValid() throws Exception {
		SpreadsheetResultInstances parseResponse = spreadsheetResponseReaderAdapter.parseResponse(responseInfo);
		EAIReportableException error = parseResponse.getError();
		assertNull(error != null ? error.getMessage() : "", error);
		assertNotNull(parseResponse.getInstances());
		assertEquals(3, parseResponse.getInstances().size());
		verify(instanceService, new AtLeast(3)).createInstance(any(), any(), any());
	}

	@Test
	public void testParseWithObjectPropertiesValid() throws Exception {
		List<SpreadsheetEntry> entries = sheet.getEntries();
		for (SpreadsheetEntry spreadsheetEntry : entries) {
			spreadsheetEntry.setConfiguration(
					Collections.singletonMap("emf:references", "emf:type=\"Project\" and dcterms:title=?"));
		}
		ReflectionUtils.setField(spreadsheetResponseReaderAdapter, "securityContextManager",
				new SecurityContextManagerFake());
		entries.get(0).getProperties().put("emf:references", "title0");
		entries.get(1).getProperties().put("emf:references", "title1");
		entries.get(2).getProperties().put("emf:references", "title2");
		DefinitionModel definitionModelMock = dictionaryService.find(DEF_ID);
		List<PropertyDefinition> fields = new LinkedList<>();
		fields.addAll(Arrays.asList(defaultProperties));
		fields.add(mockPropertyDefinition("emf:references", DataTypeDefinition.URI, null));
		setDefinitionFieldsStream(definitionModelMock, fields.toArray(new PropertyDefinition[fields.size()]));

		Condition condition = new Condition();
		when(contextQueryProcessor.convertToCondtion(any())).thenReturn(condition);
		SearchArguments<Object> searchArguments = new SearchArguments<>();
		when(searchService.parseRequest(any())).thenReturn(searchArguments);
		Instance relation = mock(Instance.class);
		when(relation.getId()).thenReturn("emf:id");
		searchArguments.setResult(Collections.singletonList(relation));
		SpreadsheetResultInstances parseResponse = spreadsheetResponseReaderAdapter.parseResponse(responseInfo);
		EAIReportableException error = parseResponse.getError();
		assertNull(error != null ? error.getMessage() : "", error);
		assertNotNull(parseResponse.getInstances());
		assertEquals(3, parseResponse.getInstances().size());
		verify(instanceService, new AtLeast(3)).createInstance(any(), any(), any());
		verify(searchService, new AtLeast(3)).parseRequest(any());
		verify(searchService, new AtLeast(3)).search(eq(Instance.class), any(SearchArguments.class));
		verify(contextQueryProcessor, new AtLeast(3)).convertToCondtion(any());
	}

	private ResponseInfo workingScenario() {
		mockEntityType(DEF_ID, typeClass);
		mockInstanceType("emf:" + DEF_ID);
		ResponseInfo responseInfo = mock(ResponseInfo.class);
		when(instanceAccessEvaluator.canWrite(eq(typeClass))).thenReturn(true);
		when(typeConverter.convert(any(Class.class), any(String.class))).thenAnswer(new Answer<Serializable>() {

			@Override
			public Serializable answer(InvocationOnMock invocation) throws Throwable {
				// same as source
				return (Serializable) invocation.getArgumentAt(1, Object.class);
			}
		});
		DefinitionModel definitionModel = mockDefinitionModel(DEF_ID);
		Instance createdInstance = mock(Instance.class);
		when(createdInstance.getIdentifier()).thenReturn(DEF_ID);
		when(dictionaryService.find(eq(DEF_ID))).thenReturn(definitionModel);
		when(dictionaryService.getInstanceDefinition(eq(createdInstance))).thenReturn(definitionModel);
		when(instanceService.createInstance(eq(definitionModel), Matchers.any(), Matchers.any()))
				.thenReturn(createdInstance);
		when(modelService.provideModelConverter(eq(SYSTEM_ID))).thenReturn(modelConverter);
		when(eaiConfiguration.getTypePropertyURI()).thenReturn(new ConfigurationPropertyMock<String>("emf:type"));
		when(eaiConfiguration.getIdentifierPropertyURI())
				.thenReturn(new ConfigurationPropertyMock<String>("dcterms:identifier"));
		when(eaiConfiguration.getParallelismCount()).thenReturn(new ConfigurationPropertyMock<Integer>(1));

		when(sheet.getEntries())
				.thenReturn(Arrays.asList(createNewEntry("1"), createNewEntry("2"), createNewEntry("3")));
		when(responseInfo.getResponse()).thenReturn(sheet);
		when(modelService.getModelConfiguration(eq(SYSTEM_ID))).thenReturn(modelConfiguration);
		when(integrationService.getIntegrationConfiguration(eq(SYSTEM_ID))).thenReturn(configurationProvider);
		when(configurationProvider.getModelConfiguration())
				.thenReturn(new ConfigurationPropertyMock<ModelConfiguration>(modelConfiguration));

		SpreadsheetReadServiceRequest readServiceRequest = mock(SpreadsheetReadServiceRequest.class);
		when(responseInfo.getRequest()).thenReturn(readServiceRequest);
		doCallRealMethod().when(taskExecutor).submit(any(Executable.class));
		doCallRealMethod().when(taskExecutor).waitForAll(any(Collection.class));
		return responseInfo;
	}

	private DefinitionModel mockDefinitionModel(String id) {
		DefinitionModel definitionModel = mock(DefinitionModel.class);
		when(definitionModel.getIdentifier()).thenReturn(id);
		setDefinitionFieldsStream(definitionModel, defaultProperties);
		return definitionModel;
	}

	private void setDefinitionFieldsStream(DefinitionModel definitionModel, PropertyDefinition... properties) {
		when(definitionModel.fieldsStream()).thenAnswer(new Answer<Stream<PropertyDefinition>>() {
			@Override
			public Stream<PropertyDefinition> answer(InvocationOnMock invocation) throws Throwable {
				return Stream.of(properties);
			}
		});
	}

	private PropertyDefinition mockPropertyDefinition(String uri, String type, Integer codelist) {
		PropertyDefinition property = mock(PropertyDefinition.class);
		when(property.getUri()).thenReturn(uri);
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

	private void mockInstanceType(String id) {
		InstanceType type = mock(InstanceType.class);
		when(type.isCreatable()).thenReturn(true);
		when(semanticInstanceTypes.from(eq(id))).thenReturn(Optional.of(type));
	}

	private void mockEntityType(String id, ClassInstance cls) {
		EntityType entityType = mock(EntityType.class);
		List<EntityProperty> asList = Arrays.asList(mockPropertyType(id, UNIQUE_IDENTIFIER, null, true),
				mockPropertyType(id, TITLE, null, true), mockPropertyType(id, TYPE, 1, true),
				mockPropertyType(id, "references", null, false));
		when(entityType.getIdentifier()).thenReturn(id);
		when(entityType.getUri()).thenReturn("emf:" + id);
		when(entityType.getTitle()).thenReturn("Title_" + id);
		when(entityType.getProperties()).thenReturn(asList);
		when(modelConfiguration.getTypeByExternalName(eq(id))).thenReturn(entityType);
		when(semanticDefinitionService.getClassInstance(eq("emf:" + id))).thenReturn(cls);
		when(modelConfiguration.getTypeByDefinitionId(eq(id))).thenReturn(entityType);
		when(cls.getId()).thenReturn("emf:" + id);
		when(instanceAccessEvaluator.canWrite(any())).thenReturn(Boolean.TRUE);
	}

	private EntityProperty mockPropertyType(String definitionId, String id, Integer codelist, boolean mandatory) {
		EntityProperty entityProperty = mock(EntityProperty.class);
		when(entityProperty.getPropertyId()).thenReturn(id);
		String uri = "emf:" + id;
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

	private SpreadsheetEntry createNewEntry(String id) {
		SpreadsheetEntry spreadsheetEntry = new SpreadsheetEntry("0", id);
		spreadsheetEntry.getProperties().put("emf:" + TYPE, "type");
		spreadsheetEntry.getProperties().put("emf:" + TITLE, "title");
		spreadsheetEntry.getProperties().put("emf:" + UNIQUE_IDENTIFIER, UUID.randomUUID().toString());
		return spreadsheetEntry;
	}

	@Test
	public void testGetName() throws Exception {
		assertEquals(SYSTEM_ID, spreadsheetResponseReaderAdapter.getName());
	}
}
