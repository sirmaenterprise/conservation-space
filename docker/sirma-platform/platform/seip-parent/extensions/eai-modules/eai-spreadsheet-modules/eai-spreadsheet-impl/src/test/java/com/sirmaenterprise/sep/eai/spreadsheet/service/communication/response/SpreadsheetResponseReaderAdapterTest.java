package com.sirmaenterprise.sep.eai.spreadsheet.service.communication.response;

import static com.sirma.itt.seip.domain.instance.DefaultProperties.TITLE;
import static com.sirma.itt.seip.domain.instance.DefaultProperties.TYPE;
import static com.sirma.itt.seip.domain.instance.DefaultProperties.UNIQUE_IDENTIFIER;
import static com.sirmaenterprise.sep.eai.spreadsheet.configuration.SpreadsheetIntegrationConfigurationProvider.SYSTEM_ID;
import static com.sirmaenterprise.sep.eai.spreadsheet.model.EAISystemProperties.PART_OF;
import static com.sirmaenterprise.sep.eai.spreadsheet.service.communication.response.ResponseReaderMockProvider.createNewEntry;
import static com.sirmaenterprise.sep.eai.spreadsheet.service.communication.response.ResponseReaderMockProvider.mockDefinitionModel;
import static com.sirmaenterprise.sep.eai.spreadsheet.service.communication.response.ResponseReaderMockProvider.mockEntityType;
import static com.sirmaenterprise.sep.eai.spreadsheet.service.communication.response.ResponseReaderMockProvider.mockInstance;
import static com.sirmaenterprise.sep.eai.spreadsheet.service.communication.response.ResponseReaderMockProvider.mockPropertyDefinition;
import static com.sirmaenterprise.sep.eai.spreadsheet.service.communication.response.ResponseReaderMockProvider.mockPropertyType;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyCollection;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.atLeast;
import static org.mockito.Mockito.doCallRealMethod;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.Serializable;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.apache.commons.lang3.StringUtils;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Matchers;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.internal.verification.AtMost;
import org.mockito.runners.MockitoJUnitRunner;

import com.sirma.itt.seip.Executable;
import com.sirma.itt.seip.Pair;
import com.sirma.itt.seip.definition.DefinitionService;
import com.sirma.itt.seip.domain.definition.DataTypeDefinition;
import com.sirma.itt.seip.domain.definition.DefinitionModel;
import com.sirma.itt.seip.domain.definition.PropertyDefinition;
import com.sirma.itt.seip.domain.instance.ClassInstance;
import com.sirma.itt.seip.domain.instance.DefaultProperties;
import com.sirma.itt.seip.domain.instance.Instance;
import com.sirma.itt.seip.domain.instance.InstanceReference;
import com.sirma.itt.seip.domain.instance.InstanceType;
import com.sirma.itt.seip.domain.search.SearchArguments;
import com.sirma.itt.seip.domain.search.tree.Condition;
import com.sirma.itt.seip.domain.search.tree.SearchCriteriaBuilder;
import com.sirma.itt.seip.eai.exception.EAIException;
import com.sirma.itt.seip.eai.exception.EAIModelException;
import com.sirma.itt.seip.eai.exception.EAIReportableException;
import com.sirma.itt.seip.eai.model.communication.ResponseInfo;
import com.sirma.itt.seip.eai.model.error.ErrorBuilderProvider;
import com.sirma.itt.seip.eai.service.model.ModelConfiguration;
import com.sirma.itt.seip.eai.service.model.ModelService;
import com.sirma.itt.seip.event.EventService;
import com.sirma.itt.seip.instance.InstanceTypeResolver;
import com.sirma.itt.seip.instance.dao.InstanceService;
import com.sirma.itt.seip.instance.event.BeforeInstanceImportEvent;
import com.sirma.itt.seip.search.SearchService;
import com.sirma.itt.seip.security.context.SecurityContextManager;
import com.sirma.itt.seip.testutil.fakes.SecurityContextManagerFake;
import com.sirma.itt.seip.testutil.fakes.TaskExecutorFake;
import com.sirma.itt.seip.testutil.mocks.ConfigurationPropertyMock;
import com.sirma.itt.seip.util.ReflectionUtils;
import com.sirmaenterprise.sep.eai.spreadsheet.configuration.SpreadsheetIntegrationConfiguration;
import com.sirmaenterprise.sep.eai.spreadsheet.model.internal.SpreadsheetResultInstances;
import com.sirmaenterprise.sep.eai.spreadsheet.model.request.SpreadsheetReadServiceRequest;
import com.sirmaenterprise.sep.eai.spreadsheet.model.response.SpreadsheetEntry;
import com.sirmaenterprise.sep.eai.spreadsheet.model.response.SpreadsheetSheet;
import com.sirmaenterprise.sep.eai.spreadsheet.model.transform.SpreadsheetModelConverter;

/**
 * Test for {@link SpreadsheetResponseReaderAdapter}.
 */
@RunWith(MockitoJUnitRunner.class)
public class SpreadsheetResponseReaderAdapterTest {

	@InjectMocks
	private SpreadsheetResponseReaderAdapter spreadsheetResponseReaderAdapter;

	@Mock
	private ModelService modelService;
	@Mock
	private SpreadsheetIntegrationConfiguration eaiConfiguration;
	@Mock
	private InstanceService instanceService;
	@Mock
	private InstanceTypeResolver resolver;
	@Mock
	private DefinitionService definitionService;
	@Mock
	private SearchService searchService;
	@Mock
	private RelationQueryProcessor contextQueryProcessor;
	@Mock
	private SecurityContextManager securityContextManager;
	@Mock
	private TaskExecutorFake taskExecutor;
	@Mock
	private ModelValidationService modelValidationService;
	@Mock
	private ModelConfiguration modelConfiguration;
	@Mock
	private SpreadsheetModelConverter modelConverter;
	@Mock
	private SpreadsheetSheet sheet;
	@Mock
	private ClassInstance typeClass;
	@Mock
	private EventService eventService;

	private ResponseInfo responseInfo;
	private static final String DEF_ID = "type";
	private PropertyDefinition[] defaultProperties = new PropertyDefinition[] {
			mockPropertyDefinition("dcterms:" + UNIQUE_IDENTIFIER, "an..50", null),
			mockPropertyDefinition("emf:" + TYPE, "an..50", null),
			mockPropertyDefinition("emf:" + TITLE, "an..50", null),
			mockPropertyDefinition("emf:references", DataTypeDefinition.URI, null) };

	@Before
	public void prepareData() throws Exception {
		responseInfo = workingScenario();
	}

	@Test(expected = EAIException.class)
	public void testParseResponseWrongParameter() throws Exception {
		mockImportableInstance(true, false);
		spreadsheetResponseReaderAdapter.parseResponse(mock(ResponseInfo.class));
	}

	@Test
	public void testParseResponseEmpty() throws Exception {
		mockImportableInstance(true, false);
		sheet = mock(SpreadsheetSheet.class);
		when(sheet.getEntries()).thenReturn(Collections.emptyList());
		when(responseInfo.getResponse()).thenReturn(sheet);

		when(modelService.getModelConfiguration(Matchers.eq(SYSTEM_ID))).thenReturn(modelConfiguration);
		SpreadsheetResultInstances parseResponse = spreadsheetResponseReaderAdapter.parseResponse(responseInfo);
		assertNull(parseResponse.getInstances());
		assertNotNull("No entries to process!", parseResponse.getError());
	}

	@Test
	public void testParseResponseWithMissingMandatory() throws Exception {
		mockImportableInstance(true, false);
		sheet.getEntries().get(0).getProperties().remove("emf:" + TITLE);
		Mockito.doAnswer(invocation -> {
			ErrorBuilderProvider builder = invocation.getArgumentAt(1, ErrorBuilderProvider.class);
			IntegrationData data = invocation.getArgumentAt(0, IntegrationData.class);
			if (!data.getSource().getProperties().containsKey("emf:" + TITLE)) {
				builder.append("mandatory is missing");
			}
			return null;
		}).when(modelValidationService).validateInstanceModel(any(), any());
		SpreadsheetResultInstances parseResponse = spreadsheetResponseReaderAdapter.parseResponse(responseInfo);
		EAIReportableException error = parseResponse.getError();
		assertNotNull(error);
		assertNotNull(parseResponse.getInstances());
		assertEquals(2, parseResponse.getInstances().size());
		verify(instanceService, atLeast(2)).createInstance(any(), any(), any());
	}

	@Test
	public void testParseResponseWithPermissionError() throws Exception {
		mockImportableInstance(true, false);
		Mockito.doThrow(EAIReportableException.class).when(modelValidationService).validateCreatablePermissions(any());
		SpreadsheetResultInstances parseResponse = spreadsheetResponseReaderAdapter.parseResponse(responseInfo);
		EAIReportableException error = parseResponse.getError();
		assertNotNull(error);
		assertNotNull(parseResponse.getInstances());
		assertEquals(0, parseResponse.getInstances().size());
		verify(instanceService, new AtMost(0)).createInstance(any(), any(), any());
	}

	@Test
	public void testParseResponseWithValidationError() throws Exception {
		mockImportableInstance(true, false);
		Mockito.doAnswer(invocation -> {
			ErrorBuilderProvider argumentAt = invocation.getArgumentAt(1, ErrorBuilderProvider.class);
			argumentAt.append("Invalid permissions!");
			return null;
		}).when(modelValidationService).validatePropertyModel(any(IntegrationData.class), any());
		SpreadsheetResultInstances parseResponse = spreadsheetResponseReaderAdapter.parseResponse(responseInfo);
		verify(modelValidationService, atLeast(3)).validatePropertyModel(any(IntegrationData.class), any());

		EAIReportableException error = parseResponse.getError();
		assertNotNull(error);
		assertNotNull(parseResponse.getInstances());
		assertEquals(0, parseResponse.getInstances().size());
		verify(instanceService, atLeast(3)).createInstance(any(), any(), any());
	}

	@Test
	public void testParseResponseValid() throws Exception {
		mockImportableInstance(true, false);
		SpreadsheetResultInstances parseResponse = spreadsheetResponseReaderAdapter.parseResponse(responseInfo);
		EAIReportableException error = parseResponse.getError();
		assertNull(error != null ? error.getMessage() : "", error);
		assertNotNull(parseResponse.getInstances());
		assertEquals(3, parseResponse.getInstances().size());
		verify(instanceService, atLeast(3)).createInstance(any(), any(), any());
	}

	@Test
	public void testParseResponseValidForUpdate() throws Exception {
		mockImportableInstance(true, false);
		SpreadsheetSheet items = (SpreadsheetSheet) responseInfo.getResponse();
		when(responseInfo.getResponse()).thenReturn(items);
		items.getEntries().get(0).getProperties().put("dcterms:" + DefaultProperties.UNIQUE_IDENTIFIER, "emf:id");
		Instance createdInstance = mock(Instance.class);
		InstanceReference ref = mock(InstanceReference.class);
		when(ref.getId()).thenReturn("emf:id");
		when(ref.toInstance()).thenReturn(createdInstance);
		InstanceType instanceType = mock(InstanceType.class);
		when(createdInstance.type()).thenReturn(instanceType);
		when(createdInstance.getIdentifier()).thenReturn(DEF_ID);
		when(createdInstance.getId()).thenReturn("emf:id");
		when(resolver.resolveReferences(anyCollection())).thenReturn(Collections.singletonList(ref));
		SpreadsheetResultInstances parseResponse = spreadsheetResponseReaderAdapter.parseResponse(responseInfo);
		EAIReportableException error = parseResponse.getError();
		assertNull(error != null ? error.getMessage() : "", error);
		assertNotNull(parseResponse.getInstances());
		assertEquals(3, parseResponse.getInstances().size());
		verify(ref, atLeast(1)).toInstance();
		verify(instanceService, atLeast(2)).createInstance(any(), any(), any());
	}

	@Test
	public void testParseResponseValidGetDbIdFromModelConfiguration() throws Exception {
		mockImportableInstance(true, false);
		when(eaiConfiguration.getIdentifierPropertyURI())
				.thenReturn(new ConfigurationPropertyMock<>(StringUtils.EMPTY));
		SpreadsheetResultInstances parseResponse = spreadsheetResponseReaderAdapter.parseResponse(responseInfo);
		EAIReportableException error = parseResponse.getError();
		assertNull(error != null ? error.getMessage() : "", error);
		assertNotNull(parseResponse.getInstances());
		assertEquals(3, parseResponse.getInstances().size());
		verify(instanceService, atLeast(3)).createInstance(any(), any(), any());
	}

	@Test
	public void testParseResponseValidUploadableInstance() throws Exception {
		mockImportableInstance(false, true);
		SpreadsheetSheet items = (SpreadsheetSheet) responseInfo.getResponse();
		when(responseInfo.getResponse()).thenReturn(items);
		items.getEntries().get(0).getProperties().put(DefaultProperties.PRIMARY_CONTENT_ID, "123asd134asd123");
		items.getEntries().get(1).getProperties().put(DefaultProperties.PRIMARY_CONTENT_ID, "456dfg4365df356");
		items.getEntries().get(2).getProperties().put(DefaultProperties.PRIMARY_CONTENT_ID, "789ghj789hjk6789");
		SpreadsheetResultInstances parseResponse = spreadsheetResponseReaderAdapter.parseResponse(responseInfo);
		assertNull(parseResponse.getError());
	}

	@Test
	public void testParseResponseValidUploadableAndCreatableInstance() throws Exception {
		mockImportableInstance(true, true);
		SpreadsheetSheet items = (SpreadsheetSheet) responseInfo.getResponse();
		when(responseInfo.getResponse()).thenReturn(items);
		items.getEntries().get(0).getProperties().put(DefaultProperties.PRIMARY_CONTENT_ID, "123asd134asd123");
		items.getEntries().get(1).getProperties().put(DefaultProperties.PRIMARY_CONTENT_ID, "456dfg4365df356");
		items.getEntries().get(2).getProperties().put(DefaultProperties.PRIMARY_CONTENT_ID, "789ghj789hjk6789");
		SpreadsheetResultInstances parseResponse = spreadsheetResponseReaderAdapter.parseResponse(responseInfo);
		assertNull(parseResponse.getError());
	}

	@Test
	public void testParseWithObjectPropertiesValid() throws Exception {
		PropertyDefinition[] properties = new PropertyDefinition[] {
				mockPropertyDefinition("dcterms:" + UNIQUE_IDENTIFIER, "an..50", null),
				mockPropertyDefinition("emf:" + TYPE, "an..50", null),
				mockPropertyDefinition("emf:" + TITLE, "an..50", null),
				mockPropertyDefinition("emf:references", DataTypeDefinition.URI, null) };
		when(properties[3].isMultiValued()).thenReturn(Boolean.TRUE);
		DefinitionModel definitionModel = mockDefinitionModel(DEF_ID, properties);
		Instance importableInstance = mockInstance(DEF_ID, "emf:id", true, true);
		when(definitionService.find(eq(DEF_ID))).thenReturn(definitionModel);
		when(definitionService.getInstanceDefinition(eq(importableInstance))).thenReturn(definitionModel);
		when(instanceService.createInstance(eq(definitionModel), Matchers.any(), Matchers.any()))
				.thenReturn(importableInstance);

		mockObjectSearch();
		when(definitionService.find(eq(DEF_ID))).thenReturn(definitionModel);
		SpreadsheetResultInstances parseResponse = spreadsheetResponseReaderAdapter.parseResponse(responseInfo);
		EAIReportableException error = parseResponse.getError();
		assertNull(error != null ? error.getMessage() : "", error);
		assertNotNull(parseResponse.getInstances());
		assertEquals(3, parseResponse.getInstances().size());
		verify(instanceService, atLeast(3)).createInstance(any(), any(), any());
		verify(searchService, atLeast(3)).parseRequest(any());
		verify(searchService, atLeast(3)).search(eq(Instance.class), any(SearchArguments.class));
		verify(contextQueryProcessor, atLeast(3)).convertToCondtion(any());
	}

	@Test
	public void testParseWithObjectPropertiesAndChangeContext() throws Exception {
		PropertyDefinition[] properties = new PropertyDefinition[] {
				mockPropertyDefinition("dcterms:" + UNIQUE_IDENTIFIER, "an..50", null),
				mockPropertyDefinition("emf:" + TYPE, "an..50", null),
				mockPropertyDefinition(PART_OF, DataTypeDefinition.URI, null),
				mockPropertyDefinition("emf:" + TITLE, "an..50", null),
				mockPropertyDefinition("emf:references", DataTypeDefinition.URI, null) };
		when(properties[4].isMultiValued()).thenReturn(Boolean.TRUE);
		DefinitionModel definitionModel = mockDefinitionModel(DEF_ID, properties);
		Instance importableInstance = mockInstance(DEF_ID, "emf:id", true, true);

		when(definitionService.find(eq(DEF_ID))).thenReturn(definitionModel);
		when(definitionService.getInstanceDefinition(eq(importableInstance))).thenReturn(definitionModel);
		when(instanceService.createInstance(eq(definitionModel), Matchers.any(), Matchers.any()))
				.thenReturn(importableInstance);

		mockObjectSearch();
		List<SpreadsheetEntry> entries = sheet.getEntries();
		entries.get(0).put(PART_OF, "emf:id2");
		entries.get(1).put(PART_OF, "test search partOf");
		entries.get(1).bind(PART_OF, "emf\\:type:\"type\" and emf\\:title:?");
		when(modelConverter.convertExternaltoSEIPProperty(eq(PART_OF), eq("emf:id2"), eq(DEF_ID)))
				.thenReturn(new Pair<>("partOf", "emf:id2"));
		InstanceReference parentRef = mock(InstanceReference.class);
		Instance parentInstance = mockInstance(DEF_ID, "emf:id2", true, true);
		when(parentRef.toInstance()).thenReturn(parentInstance);
		when(resolver.resolveReference(eq("emf:id2"))).thenReturn(Optional.of(parentRef));
		when(modelConverter.convertExternaltoSEIPProperty(eq(PART_OF), eq("test search partOf"), eq(DEF_ID)))
				.thenReturn(new Pair<>("partOf", (Serializable) Collections.singletonList("test search partOf")));
		SpreadsheetResultInstances parseResponse = spreadsheetResponseReaderAdapter.parseResponse(responseInfo);
		EAIReportableException error = parseResponse.getError();
		assertNull(error != null ? error.getMessage() : "", error);
		assertNotNull(parseResponse.getInstances());
		assertEquals(3, parseResponse.getInstances().size());
		verify(instanceService, atLeast(3)).createInstance(any(), any(), any());
		verify(searchService, atLeast(3)).parseRequest(any());
		verify(searchService, atLeast(2)).search(eq(Instance.class), any(SearchArguments.class));
		verify(searchService, atLeast(1)).searchAndLoad(eq(Instance.class), any(SearchArguments.class));
		verify(contextQueryProcessor, atLeast(3)).convertToCondtion(any());
	}

	@Test
	public void testParseWithObjectPropertiesMissing() throws Exception {
		mockImportableInstance(true, false);
		SearchArguments<Object> searchArguments = mockObjectSearch();
		searchArguments.setResult(Collections.emptyList());
		SpreadsheetResultInstances parseResponse = spreadsheetResponseReaderAdapter.parseResponse(responseInfo);
		EAIReportableException error = parseResponse.getError();
		assertNotNull(error != null ? error.getMessage() : "", error);
		assertNotNull(parseResponse.getInstances());
		// with the null configuration
		assertEquals(1, parseResponse.getInstances().size());
		verify(instanceService, atLeast(3)).createInstance(any(), any(), any());
		verify(searchService, atLeast(2)).parseRequest(any());
		verify(searchService, atLeast(2)).search(eq(Instance.class), any(SearchArguments.class));
		verify(contextQueryProcessor, atLeast(2)).convertToCondtion(any());
	}

	@Test
	public void testParseWithObjectPropertiesMultiple() throws Exception {
		mockImportableInstance(true, false);
		SearchArguments<Object> searchArguments = mockObjectSearch();
		List<Object> result = searchArguments.getResult();
		searchArguments.setResult(Arrays.asList(result.get(0), result.get(0)));
		SpreadsheetResultInstances parseResponse = spreadsheetResponseReaderAdapter.parseResponse(responseInfo);
		EAIReportableException error = parseResponse.getError();
		assertNotNull(error != null ? error.getMessage() : "", error);
		assertNotNull(parseResponse.getInstances());
		// with the null configuration
		assertEquals(1, parseResponse.getInstances().size());
		verify(instanceService, atLeast(3)).createInstance(any(), any(), any());
		verify(searchService, atLeast(2)).parseRequest(any());
		verify(searchService, atLeast(2)).search(eq(Instance.class), any(SearchArguments.class));
		verify(contextQueryProcessor, atLeast(2)).convertToCondtion(any());
	}

	@Test
	public void shouldFireBeforeInstanceImportEvent() throws Exception {
		mockImportableInstance(true, false);

		spreadsheetResponseReaderAdapter.parseResponse(responseInfo);

		verify(eventService, atLeast(3)).fire(any(BeforeInstanceImportEvent.class));
	}

	private SearchArguments<Object> mockObjectSearch() throws EAIException, EAIModelException {
		List<SpreadsheetEntry> entries = sheet.getEntries();
		for (SpreadsheetEntry spreadsheetEntry : entries) {
			spreadsheetEntry.bind("emf:references", "emf:type=\"Project\" and dcterms:title=?");
		}

		ReflectionUtils.setFieldValue(spreadsheetResponseReaderAdapter, "securityContextManager",
				new SecurityContextManagerFake());
		entries.get(0).getProperties().put("emf:references", "title0");
		entries.get(1).getBindings().clear();
		entries.get(2).getProperties().put("emf:references", Arrays.asList("title2", "title3"));

		Condition condition = SearchCriteriaBuilder.createConditionBuilder().build();
		when(contextQueryProcessor.convertToCondtion(any())).thenReturn(condition);
		SearchArguments<Object> searchArguments = new SearchArguments<>();
		when(searchService.parseRequest(any())).thenReturn(searchArguments);
		Instance relation = mock(Instance.class);
		when(relation.getId()).thenReturn("emf:id");
		searchArguments.setResult(Collections.singletonList(relation));
		when(modelConverter.convertExternaltoSEIPProperties(any(), any(Instance.class))).then(invocation -> invocation.getArgumentAt(0, Map.class));
		return searchArguments;
	}

	private void mockImportableInstance(boolean isCreatable, boolean isUploadable) {
		DefinitionModel definitionModel = mockDefinitionModel(DEF_ID, defaultProperties);
		Instance importableInstance = mockInstance(DEF_ID, "emf:id", isCreatable, isUploadable);
		when(definitionService.find(eq(DEF_ID))).thenReturn(definitionModel);
		when(definitionService.getInstanceDefinition(eq(importableInstance))).thenReturn(definitionModel);
		when(instanceService.createInstance(eq(definitionModel), Matchers.any(), Matchers.any()))
				.thenReturn(importableInstance);
	}

	private ResponseInfo workingScenario() {
		mockEntityType(modelConfiguration, DEF_ID, typeClass,
				mockPropertyType(modelConfiguration, DEF_ID, "dcterms:", UNIQUE_IDENTIFIER, null, true),
				mockPropertyType(modelConfiguration, DEF_ID, "emf:", TITLE, null, true),
				mockPropertyType(modelConfiguration, DEF_ID, "emf:", TYPE, 1, true),
				mockPropertyType(modelConfiguration, DEF_ID, "emf:", "references", null, false));
		ResponseInfo response = mock(ResponseInfo.class);

		when(modelService.provideModelConverter(eq(SYSTEM_ID))).thenReturn(modelConverter);
		when(eaiConfiguration.getTypePropertyURI()).thenReturn(new ConfigurationPropertyMock<>("emf:type"));
		when(eaiConfiguration.getIdentifierPropertyURI())
				.thenReturn(new ConfigurationPropertyMock<>("dcterms:identifier"));
		when(eaiConfiguration.getParallelismCount()).thenReturn(new ConfigurationPropertyMock<>(1));

		when(sheet.getEntries())
				.thenReturn(Arrays.asList(createNewEntry("1"), createNewEntry("2"), createNewEntry("3")));
		when(response.getResponse()).thenReturn(sheet);
		when(modelService.getModelConfiguration(eq(SYSTEM_ID))).thenReturn(modelConfiguration);

		SpreadsheetReadServiceRequest readServiceRequest = mock(SpreadsheetReadServiceRequest.class);
		when(response.getRequest()).thenReturn(readServiceRequest);
		doCallRealMethod().when(taskExecutor).submit(any(Executable.class));
		doCallRealMethod().when(taskExecutor).waitForAll(anyCollection());
		return response;
	}

	@Test
	public void testGetName() throws Exception {
		assertEquals(SYSTEM_ID, spreadsheetResponseReaderAdapter.getName());
	}
}
