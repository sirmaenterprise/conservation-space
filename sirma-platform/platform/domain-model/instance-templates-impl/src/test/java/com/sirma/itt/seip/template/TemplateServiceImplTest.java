package com.sirma.itt.seip.template;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyBoolean;
import static org.mockito.Matchers.anyList;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.argThat;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.Serializable;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.dozer.DozerBeanMapper;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Matchers;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.mockito.Spy;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

import com.sirma.itt.commons.utils.string.StringUtils;
import com.sirma.itt.seip.Pair;
import com.sirma.itt.seip.collections.CollectionUtils;
import com.sirma.itt.seip.concurrent.locks.ContextualReadWriteLock;
import com.sirma.itt.seip.content.Content;
import com.sirma.itt.seip.content.ContentAdapterService;
import com.sirma.itt.seip.content.ContentInfo;
import com.sirma.itt.seip.content.InstanceContentService;
import com.sirma.itt.seip.content.descriptor.ByteArrayAndPropertiesDescriptor;
import com.sirma.itt.seip.convert.TypeConverter;
import com.sirma.itt.seip.db.DbDao;
import com.sirma.itt.seip.definition.DefintionAdapterService;
import com.sirma.itt.seip.definition.DictionaryService;
import com.sirma.itt.seip.definition.compile.DefinitionCompiler;
import com.sirma.itt.seip.definition.compile.DefinitionCompilerCallback;
import com.sirma.itt.seip.definition.dozer.DefinitionsDozerProvider;
import com.sirma.itt.seip.domain.definition.DefinitionModel;
import com.sirma.itt.seip.domain.definition.PropertyDefinition;
import com.sirma.itt.seip.domain.definition.label.LabelProvider;
import com.sirma.itt.seip.domain.instance.DefaultProperties;
import com.sirma.itt.seip.domain.instance.EmfInstance;
import com.sirma.itt.seip.domain.instance.Instance;
import com.sirma.itt.seip.domain.instance.InstanceReference;
import com.sirma.itt.seip.domain.search.SearchArguments;
import com.sirma.itt.seip.domain.security.ActionTypeConstants;
import com.sirma.itt.seip.event.EventService;
import com.sirma.itt.seip.instance.DomainInstanceService;
import com.sirma.itt.seip.instance.InstanceSaveContext;
import com.sirma.itt.seip.instance.InstanceTypeResolver;
import com.sirma.itt.seip.instance.dao.InstanceDao;
import com.sirma.itt.seip.instance.dozer.CommonDozerProvider;
import com.sirma.itt.seip.instance.state.Operation;
import com.sirma.itt.seip.io.FileAndPropertiesDescriptor;
import com.sirma.itt.seip.io.FileDescriptor;
import com.sirma.itt.seip.io.TempFileProvider;
import com.sirma.itt.seip.mapping.ObjectMapper;
import com.sirma.itt.seip.mapping.dozer.DozerObjectMapper;
import com.sirma.itt.seip.monitor.Statistics;
import com.sirma.itt.seip.search.SearchService;
import com.sirma.itt.seip.security.context.SecurityContext;
import com.sirma.itt.seip.tasks.DefaultSchedulerConfiguration;
import com.sirma.itt.seip.tasks.SchedulerConfiguration;
import com.sirma.itt.seip.tasks.SchedulerContext;
import com.sirma.itt.seip.tasks.SchedulerService;
import com.sirma.itt.seip.template.dozer.TemplateDozerProvider;
import com.sirma.itt.seip.template.schedule.TemplateActivateScheduler;
import com.sirma.itt.seip.testutil.CustomMatcher;
import com.sirma.itt.seip.testutil.fakes.TransactionSupportFake;
import com.sirma.itt.seip.testutil.mocks.InstanceReferenceMock;
import com.sirma.itt.seip.time.TimeTracker;
import com.sirmaenterprise.sep.content.idoc.Idoc;

/**
 * Test the {@link TemplateServiceImpl}.
 *
 * @author nvelkov
 * @author Vilizar Tsonev
 */
public class TemplateServiceImplTest {

	@InjectMocks
	private TemplateServiceImpl templateService;

	@Mock
	private TempFileProvider fileProvider;

	@Mock
	private InstanceDao instanceDao;

	@Mock
	private EventService eventService;

	@Mock
	private DefinitionCompiler compiler;

	@Mock
	private DbDao dbDao;

	@Mock
	private ContentAdapterService adapterService;

	@Mock
	private LabelProvider labelProvider;

	@Mock
	private SecurityContext securityContext;

	@Mock
	private TypeConverter typeConverter;

	@Spy
	private ObjectMapper mapper;

	@Mock
	private TemplatePreProcessor preProcessor;

	@Mock
	private Statistics statistics;

	@Mock
	private DomainInstanceService domainInstanceService;

	@Mock
	private InstanceContentService instanceContentService;

	@Mock
	private SearchService searchService;

	@Mock
	private DefintionAdapterService definitionAdapterService;

	@Mock
	private InstanceTypeResolver instanceTypeResolver;

	@Mock
	private SchedulerService schedulerService;

	@Mock
	private DictionaryService dictionaryService;

	@Spy
	private TransactionSupportFake transactionSupport;

	@Spy
	private ContextualReadWriteLock contextualLock = ContextualReadWriteLock.create();

	@Before
	public void init() {
		List<String> files = new LinkedList<>();
		files.add(DefinitionsDozerProvider.DOZER_COMMON_MAPPING_XML);
		files.add(CommonDozerProvider.DOZER_COMMON_MAPPING_XML);
		files.add(TemplateDozerProvider.DOZER_TEMPLATE_MAPPING_XML);
		mapper = new DozerObjectMapper(new DozerBeanMapper(files));
		MockitoAnnotations.initMocks(this);
		when(typeConverter.convert(eq(String.class), any(Object.class))).then(a -> {
			Object arg = a.getArgumentAt(1, Object.class);
			if(arg == null) {
				return arg;
			}
			return arg.toString();
		});
		when(statistics.createTimeStatistics(any(), anyString())).thenReturn(TimeTracker.createAndStart());
	}

	/**
	 * Test the default template retrieval.
	 */
	@Test
	public void testGetDefaultTemplate() {
		mockInstanceDao("id", "content", "", "sampleForType");
		mockCompiler();
		TemplateInstance instance = templateService.getDefaultTemplate();
		assertEquals("id", instance.getId());
		assertEquals("content", instance.getContent());
	}

	/**
	 * Test the default template retrieval.
	 */
	@Test
	public void testGetDefaultTemplate_missingFileInDms() {
		mockInstanceDao("id", "content", "", "sampleForType");
		TemplateInstance instance = templateService.getDefaultTemplate();
		assertEquals("id", instance.getId());
		assertNotEquals("content", instance.getContent());
	}

	/**
	 * Test the default template content retrieval.
	 */
	@Test
	public void testDefaultTemplateContent() {
		mockInstanceDao("id", "content", "", "sampleForType");
		mockCompiler();
		mockSearchService(true);

		String content = templateService.getDefaultTemplateContent();
		Assert.assertEquals("content", content);
	}

	/**
	 * Test the default template content retrieval when the default template hasn't be found. The default template from
	 * the constant should be returned instead.
	 */
	@Test
	public void testDefaultTemplateContentNotFound() {
		String expectedContent = "<div data-tabs-counter=\"2\">"
				+ "<section data-title=\"Tab1\"data-show-navigation=\"true\""
				+ "data-show-comments=\"true\"data-default=\"true\"></section></div>";

		String content = templateService.getDefaultTemplateContent();
		Assert.assertEquals(expectedContent, content);
	}

	/**
	 * Tests {@link TemplateService#getTemplates(String)} when only groupID is provided. Verifies that the DbDao
	 * is invoked with the correct parameters.
	 */
	@Test
	public void testGetTemplates() {
		String documentType = "image";
		List<Pair<String, Object>> params = new ArrayList<>(2);
		params.add(new Pair<String, Object>(TemplateProperties.GROUP_ID, documentType));
		templateService.getTemplates(documentType);
		verify(dbDao).fetchWithNamed(Mockito.eq(TemplateEntity.QUERY_TEMPLATES_FOR_USER_AND_GROUP_ID_KEY),
				Mockito.eq(params));
	}

	/**
	 * Tests {@link TemplateService#getTemplates(String, String)} when groupID and purpose are provided.
	 * Verifies that the DbDao is invoked with the correct parameters.
	 */
	@Test
	public void testGetTemplatesWithPurpose() {
		String documentType = "image";
		String purpose = TemplatePurposes.UPLOADABLE;
		List<Pair<String, Object>> params = new ArrayList<>(2);
		params.add(new Pair<String, Object>(TemplateProperties.GROUP_ID, documentType));
		params.add(new Pair<String, Object>(TemplateProperties.PURPOSE, purpose));
		templateService.getTemplates(documentType, purpose);
		verify(dbDao).fetchWithNamed(Mockito.eq(TemplateEntity.QUERY_TEMPLATES_FOR_USER_GROUP_ID_PURPOSE_KEY),
				Mockito.eq(params));
	}

	/**
	 * Tests {@link TemplateService#getPrimaryTemplate(String)} when only groupID is provided. Verifies that the
	 * DbDao is invoked with the correct parameters.
	 */
	@Test
	public void testGetPrimaryTemplate() {
		String documentType = "image";
		List<Pair<String, Object>> params = new ArrayList<>(2);
		params.add(new Pair<String, Object>(TemplateProperties.GROUP_ID, documentType));
		templateService.getPrimaryTemplate(documentType);
		verify(dbDao).fetchWithNamed(Mockito.eq(TemplateEntity.QUERY_PRIMARY_TEMPLATE_FOR_GROUP_KEY),
				Mockito.eq(params));
	}

	/**
	 * Tests {@link TemplateService#getPrimaryTemplate(String, String)} when groupID and purpose are provided.
	 * Verifies that the DbDao is invoked with the correct parameters.
	 */
	@Test
	public void testGetPrimaryTemplateWithPurpose() {
		String documentType = "image";
		String purpose = TemplatePurposes.UPLOADABLE;
		List<Pair<String, Object>> params = new ArrayList<>(2);
		params.add(new Pair<String, Object>(TemplateProperties.GROUP_ID, documentType));
		params.add(new Pair<String, Object>(TemplateProperties.PURPOSE, purpose));
		templateService.getPrimaryTemplate(documentType, purpose);
		verify(dbDao).fetchWithNamed(Mockito.eq(TemplateEntity.QUERY_PRIMARY_TEMPLATE_FOR_GROUP_AND_PURPOSE_KEY),
				Mockito.eq(params));
	}

	/**
	 * Tests {@link TemplateServiceImpl#reload()} that should fire event after reloading the templates.
	 */
	@Test
	public void testReload_shouldFireEvent() {
		templateService.reload();
		verify(eventService).fire(any(TemplatesSynchronizedEvent.class));
	}

	/**
	 * Tests {@link TemplateServiceImpl#reload()} that should skip template with no content.
	 */
	@Test
	public void testReload_shouldSkipTemplateWithEmptyContent() {
		mockInstanceDao("id", null, "", "sampleForType");
		mockCompiler();
		mockSearchService(true);
		mockDomainInstanceService();
		templateService.reload();
		verify(schedulerService, times(0)).schedule(anyString(), any(SchedulerConfiguration.class),
				any(SchedulerContext.class));
	}

	@Test
	public void should_Schedule_Corresponding_Instance_For_Activation() {
		mockInstanceDao("myTemplate", "some content", null, "sampleForType");
		mockSearchService(true);
		mockCompiler();
		mockDomainInstanceService();
		mockReturnedDefinition(true, true, "emf:sampleSemanticClass");
		SchedulerConfiguration dummyConfiguration = new DefaultSchedulerConfiguration();
		when(schedulerService.buildEmptyConfiguration(any())).thenReturn(dummyConfiguration);

		templateService.reload();
		
		ArgumentCaptor<SchedulerContext> captor = ArgumentCaptor.forClass(SchedulerContext.class);
		verify(schedulerService).schedule(eq(TemplateActivateScheduler.BEAN_ID), any(SchedulerConfiguration.class),
				captor.capture());
		List<String> instancesForActivation = Collections.singletonList("savedInstanceId");
		assertEquals(instancesForActivation, captor.getValue().get("correspondingInstanceIds"));
	}

	@Test
	public void should_Not_Try_To_Activate_Email_Templates() {
		mockInstanceDao("myTemplate", "some content", null, "emailTemplate");
		mockCompiler();

		templateService.reload();

		verify(domainInstanceService, never()).createInstance(anyString(), anyString());
		verify(domainInstanceService, never()).save(any(InstanceSaveContext.class));
		verify(schedulerService, never()).schedule(anyString(), any(), any());
	}

	@Test
	public void should_Not_Persist_In_Content_Store_When_Reloading_Templates_And_Instance_Exists() {
		mockInstanceDao("myTemplate", "some content", "correspondingInstanceId", "sampleForType");
		mockCompiler();
		mockExistingTemplateInstance(Boolean.FALSE);
		SchedulerConfiguration dummyConfiguration = new DefaultSchedulerConfiguration();
		when(schedulerService.buildEmptyConfiguration(any())).thenReturn(dummyConfiguration);

		templateService.reload();
		verify(definitionAdapterService, never()).uploadDefinition(any(), any(ByteArrayAndPropertiesDescriptor.class));
	}

	/**
	 * Tests {@link TemplateServiceImpl#reload()} with new primary template and expects the old primary template
	 * to be changed to non primary.
	 */
	@Test
	public void should_Mark_Old_Primary_Template_As_False() {
		mockSearchService(false);
		mockDomainInstanceService();
		mockExistingTemplateInstance(Boolean.TRUE);
		mockInstanceContentService();
		when(definitionAdapterService.uploadDefinition(any(), any(FileAndPropertiesDescriptor.class)))
				.thenReturn("dmsId");

		TemplateInstance oldPrimaryTemplate = new TemplateInstance();
		oldPrimaryTemplate.setId("oldId");
		oldPrimaryTemplate.setPrimary(Boolean.TRUE);
		List<Long> fetched = new ArrayList<>();
		when(dbDao.fetchWithNamed(anyString(), anyList())).thenReturn(fetched);
		when(instanceDao.loadInstancesByDbKey(fetched)).thenReturn(Arrays.asList(oldPrimaryTemplate));

		templateService.activate("newId");

		ArgumentCaptor<TemplateInstance> captor = ArgumentCaptor.forClass(TemplateInstance.class);
		verify(instanceDao).persistChanges(captor.capture());
		assertFalse(captor.getValue().getPrimary().booleanValue());
	}

	/**
	 * Covers a specific case when activating a template which already exists in RDB. The logic for resetting the
	 * "primary" flag of existing primary templates for that type should not try to reset the template that we have
	 * already loaded and we're activating (the template with the same identifier).
	 */
	@Test
	public void should_Not_Mark_Old_Primary_As_False_If_Same_Id() {
		mockSearchService(false);
		mockDomainInstanceService();
		// activating a template with title loadedTemplateTitle which already exists in the RDB
		// its identifier becomes loadedtemplatetitle
		mockExistingTemplateInstance(Boolean.TRUE);
		mockInstanceContentService();
		when(definitionAdapterService.uploadDefinition(any(), any(FileAndPropertiesDescriptor.class)))
				.thenReturn("dmsId");

		TemplateInstance oldPrimaryTemplate = new TemplateInstance();
		// identifier gets auto generated from title (titles are unique)
		oldPrimaryTemplate.setIdentifier("loadedtemplatetitle");
		oldPrimaryTemplate.setPrimary(Boolean.TRUE);
		List<Long> fetched = new ArrayList<>();
		when(dbDao.fetchWithNamed(anyString(), anyList())).thenReturn(fetched);
		when(instanceDao.loadInstancesByDbKey(fetched)).thenReturn(Arrays.asList(oldPrimaryTemplate));

		templateService.activate("emf:sampleTemplate");

		verify(instanceDao, never()).persistChanges(any(TemplateInstance.class));
	}

	@Test
	public void should_Set_Default_Values_For_Missing_Properties() {
		String tenantId = "tenant.com";
		TemplateInstance templateInstance = new TemplateInstance();

		when(securityContext.getCurrentTenantId()).thenReturn(tenantId);
		templateService.activate(templateInstance);

		assertTrue(StringUtils.isNotNullOrEmpty(templateInstance.getIdentifier()));
		assertEquals(TemplateProperties.DEFAULT_GROUP, templateInstance.getForType());
		assertEquals(TemplatePurposes.CREATABLE, templateInstance.getPurpose());
	}

	@Test
	public void Should_Create_New_Instance_With_All_Provided_Data() {
		TemplateInstance inputTemplateData = mockInputTemplateInstance(Boolean.TRUE, "sampleForType");
		Instance newlyCreatedInstance = mockDomainInstanceService();
		mockInstanceContentService();
		mockSearchService(true);
		mockReturnedDefinition(true, true, "emf:sampleSemanticClass");

		String returnedId = templateService.create(inputTemplateData);

		// Verify that newly created instance's add method is called the correct number of times with the correct
		// key-values
		ArgumentCaptor<String> requestCaptor = ArgumentCaptor.forClass(String.class);
		verify(newlyCreatedInstance).add(eq(DefaultProperties.TEMP_CONTENT_VIEW), requestCaptor.capture());
		assertNotNull(requestCaptor.getValue());
		// some tab-related html is added at the end of the content when parsing, so we assert only the beginning of
		// the string (which is the original content we have passed)
		assertTrue(requestCaptor.getValue().startsWith("Sample content of the source instance"));
		verify(newlyCreatedInstance).add(eq(DefaultProperties.TITLE), eq("sampleTitle"));
		verify(newlyCreatedInstance).add(eq(TemplateProperties.EMF_FOR_OBJECT_TYPE), eq("sampleForType"));
		verify(newlyCreatedInstance).add(eq(TemplateProperties.EMF_TEMPLATE_PURPOSE), eq(TemplatePurposes.CREATABLE));
		verify(newlyCreatedInstance).add(eq(TemplateProperties.IS_PRIMARY_TEMPLATE), eq(Boolean.TRUE));

		verify(domainInstanceService, Mockito.times(1)).save(any(InstanceSaveContext.class));
		assertEquals("savedInstanceId", returnedId);
	}

	@Test(expected = NullPointerException.class)
	public void Should_Throw_NPE_When_No_SourceInstanceId() {
		Instance sourceInstance = Mockito.mock(Instance.class);
		TemplateInstance inputTemplateData = Mockito.mock(TemplateInstance.class);
		when(inputTemplateData.getOwningInstance()).thenReturn(sourceInstance);
		templateService.create(inputTemplateData);
	}

	@Test(expected = NullPointerException.class)
	public void Should_Throw_NPE_When_No_ForType() {
		Instance sourceInstance = mock(Instance.class);
		when(sourceInstance.getId()).thenReturn("sourceInstanceId");
		TemplateInstance inputTemplateData = mock(TemplateInstance.class);
		when(inputTemplateData.getOwningInstance()).thenReturn(sourceInstance);
		templateService.create(inputTemplateData);
	}

	@Test
	public void Should_Set_New_Template_Primary_When_No_Primary_Exists() {
		TemplateInstance inputTemplateData = mockInputTemplateInstance(Boolean.FALSE, "sampleForType");
		Instance newlyCreatedInstance = mockDomainInstanceService();
		mockInstanceContentService();
		mockSearchService(true);
		mockReturnedDefinition(true, true, "emf:sampleSemanticClass");

		templateService.create(inputTemplateData);
		verify(newlyCreatedInstance).add(eq(TemplateProperties.IS_PRIMARY_TEMPLATE), eq(Boolean.TRUE));
	}

	@Test
	public void should_Create_And_Upload_To_DMS_When_No_Instance_Specified() {
		mockInstanceDao("myTemplate", "some content", null, "sampleForType");
		mockSearchService(true);
		mockCompiler();
		mockDomainInstanceService();
		mockReturnedDefinition(true, true, "emf:sampleSemanticClass");

		SchedulerConfiguration dummyConfiguration = new DefaultSchedulerConfiguration();
		when(schedulerService.buildEmptyConfiguration(any())).thenReturn(dummyConfiguration);

		templateService.reload();

		verify(domainInstanceService).createInstance(eq(TemplateProperties.TEMPLATE_DEFINITION_ID),
				anyString());
		verify(domainInstanceService, Mockito.times(1))
				.save(argThat(CustomMatcher.of((InstanceSaveContext saveContext) -> {
					assertEquals("newlyCreatedInstanceId", saveContext.getInstanceId());
					assertNotNull(saveContext.getOperation());
					assertEquals(ActionTypeConstants.CREATE, saveContext.getOperation().getOperation());
				})));
		verify(definitionAdapterService).uploadDefinition(any(), any(ByteArrayAndPropertiesDescriptor.class));
	}

	@Test
	public void should_Create_New_Instance_When_Id_Specified_And_Doesnt_Exist_In_DB() {
		mockInstanceDao("myTemplate", "some content", "correspondingInstanceId", "sampleForType");
		mockSearchService(true);
		mockCompiler();
		mockDomainInstanceService();
		mockReturnedDefinition(true, true, "emf:sampleSemanticClass");
		when(instanceTypeResolver.resolveReference(eq("correspondingInstanceId"))).thenReturn(Optional.empty());
		SchedulerConfiguration dummyConfiguration = new DefaultSchedulerConfiguration();
		when(schedulerService.buildEmptyConfiguration(any())).thenReturn(dummyConfiguration);

		templateService.reload();

		verify(domainInstanceService).createInstance(eq(TemplateProperties.TEMPLATE_DEFINITION_ID),
				anyString());
		verify(domainInstanceService, Mockito.times(1))
				.save(argThat(CustomMatcher.of((InstanceSaveContext saveContext) -> {
					assertEquals("newlyCreatedInstanceId", saveContext.getInstanceId());
					assertNotNull(saveContext.getOperation());
					assertEquals(ActionTypeConstants.CREATE, saveContext.getOperation().getOperation());
				})));
	}

	@Test
	public void should_Update_Content_With_Template_Ids() {
		String content = readFileAsString("/templates/template-with-widgets.html");
		mockInstanceDao("myTemplate", content, null, "sampleForType");
		mockSearchService(true);
		mockCompiler();
		mockDomainInstanceService();
		mockReturnedDefinition(true, true, "emf:sampleSemanticClass");
		when(instanceTypeResolver.resolveReference(eq("correspondingInstanceId"))).thenReturn(Optional.empty());
		SchedulerConfiguration dummyConfiguration = new DefaultSchedulerConfiguration();
		when(schedulerService.buildEmptyConfiguration(any())).thenReturn(dummyConfiguration);

		templateService.reload();

		verify(domainInstanceService).createInstance(eq(TemplateProperties.TEMPLATE_DEFINITION_ID),
				anyString());
		verify(domainInstanceService, Mockito.times(1))
				.save(argThat(CustomMatcher.of((InstanceSaveContext saveContext) -> {
					assertEquals("newlyCreatedInstanceId", saveContext.getInstanceId());
					assertNotNull(saveContext.getOperation());
					assertEquals(ActionTypeConstants.CREATE, saveContext.getOperation().getOperation());
				})));
		verify(definitionAdapterService, times(1)).uploadDefinition(any(),
				argThat(CustomMatcher.of((ByteArrayAndPropertiesDescriptor descriptor) -> {
					assertEquals("sampleIdentifier.xml", descriptor.getId());
					try {
						String uploadedContent = IOUtils.toString(descriptor.getInputStream(), "utf-8");
						Idoc document = Idoc.parse(uploadedContent);
						document.widgets().forEach(widget -> assertTrue(StringUtils.isNotNullOrEmpty(widget.getId())));
						document.getSections()
								.forEach(section -> assertTrue(StringUtils.isNotNullOrEmpty(section.getId())));
					} catch (IOException e) {
						e.printStackTrace();
					}
				})));
	}

	@Test
	public void should_Persist_Correct_Template_Data_When_Activating() {
		mockDomainInstanceService();
		Instance existingInstance = mockExistingTemplateInstance(Boolean.FALSE);
		mockInstanceContentService();
		when(definitionAdapterService.uploadDefinition(any(), any(FileAndPropertiesDescriptor.class)))
				.thenReturn("dmsId");

		templateService.activate("instanceId");

		// content for the loaded instance must be extracted
		verify(instanceContentService).getContent(eq(existingInstance), eq(Content.PRIMARY_VIEW));

		// the activated template must be uploaded to DMS (since true is passed as uploadToDms argument)
		verify(definitionAdapterService).uploadDefinition(any(), any(FileAndPropertiesDescriptor.class));

		// verify that all the correct template properties are persisted
		ArgumentCaptor<TemplateInstance> captor = ArgumentCaptor.forClass(TemplateInstance.class);
		verify(instanceDao).instanceUpdated(captor.capture(), eq(true));
		// the template identifier should be automatically constructed using the title
		assertEquals("loadedtemplatetitle", captor.getValue().getIdentifier());
		assertEquals("loadedTemplateTitle", captor.getValue().get(DefaultProperties.TITLE));
		assertEquals("sampleForType", captor.getValue().getForType());
		assertEquals("creatable", captor.getValue().getPurpose());
		assertEquals(Boolean.FALSE, captor.getValue().getPrimary());
	}

	@Test
	public void should_Reset_Existing_Primary_Active_Template_Instance() {
		mockSearchService(false);
		mockDomainInstanceService();
		mockExistingTemplateInstance(Boolean.TRUE);
		mockInstanceContentService();
		when(definitionAdapterService.uploadDefinition(any(), any(FileAndPropertiesDescriptor.class)))
				.thenReturn("dmsId");

		templateService.activate("instanceId");
		ArgumentCaptor<InstanceSaveContext> captor = ArgumentCaptor.forClass(InstanceSaveContext.class);
		verify(domainInstanceService, times(2)).save(captor.capture());
		List<InstanceSaveContext> capturedSaveContexts = captor.getAllValues();
		// get the argument from the first invocation of save
		InstanceSaveContext resetExistingPrimarySaveContext = capturedSaveContexts.get(0);

		assertEquals(resetExistingPrimarySaveContext.getOperation(), Operation.NO_OPERATION);
		assertEquals(resetExistingPrimarySaveContext.getInstance().get(TemplateProperties.IS_PRIMARY_TEMPLATE),
				Boolean.FALSE);
	}

	@Test
	public void should_Not_Search_For_Existing_Primary_If_New_Is_Secondary() {
		mockSearchService(false);
		mockExistingTemplateInstance(Boolean.FALSE);
		mockInstanceContentService();
		mockDomainInstanceService();
		when(definitionAdapterService.uploadDefinition(any(), any(FileAndPropertiesDescriptor.class)))
				.thenReturn("dmsId");

		templateService.activate("instanceId");
		verify(searchService, never()).searchAndLoad(any(), any());
		verify(domainInstanceService, times(1)).save(any());
	}

	@Test
	public void should_Save_Activated_Template_With_Correct_Operation() {
		mockExistingTemplateInstance(Boolean.FALSE);
		mockInstanceContentService();
		mockDomainInstanceService();
		when(definitionAdapterService.uploadDefinition(any(), any(FileAndPropertiesDescriptor.class)))
				.thenReturn("dmsId");
		
		templateService.activate("instanceId");
		
		ArgumentCaptor<InstanceSaveContext> captor = ArgumentCaptor.forClass(InstanceSaveContext.class);
		verify(domainInstanceService, times(1)).save(captor.capture());
		
		assertEquals("loadedTemplateId", captor.getValue().getInstance().getId());
		assertEquals(new Operation(ActionTypeConstants.ACTIVATE_TEMPLATE), captor.getValue().getOperation());
	}

	@Test
	public void should_Set_Correpsonding_Library_As_Template_Parent() {
		TemplateInstance inputTemplateData = mockInputTemplateInstance(Boolean.TRUE, "sampleForType");
		mockDomainInstanceService();
		mockInstanceContentService();
		mockSearchService(true);
		mockReturnedDefinition(true, true, "emf:sampleSemanticClass");

		templateService.create(inputTemplateData);
		verify(domainInstanceService).createInstance(eq(TemplateProperties.TEMPLATE_DEFINITION_ID),
				eq("emf:sampleSemanticClass"));
	}

	@Test
	public void should_Set_URI_As_Parent_If_Valid_URI_Is_Provided() {
		TemplateInstance inputTemplateData = mockInputTemplateInstance(Boolean.TRUE,
				"http://ittruse.ittbg.com/ontology/enterpriseManagementFramework#BusinessProcess");
		mockDomainInstanceService();
		mockInstanceContentService();
		mockSearchService(true);
		mockReturnedDefinition(false, false, "");

		templateService.create(inputTemplateData);
		verify(domainInstanceService).createInstance(eq(TemplateProperties.TEMPLATE_DEFINITION_ID),
				eq("http://ittruse.ittbg.com/ontology/enterpriseManagementFramework#BusinessProcess"));
	}

	@Test(expected = IllegalArgumentException.class)
	public void should_Throw_Exception_When_Neither_Valid_DefinitionID_Nor_URI_Passed() {
		TemplateInstance inputTemplateData = mockInputTemplateInstance(Boolean.TRUE, "invalidUri");
		mockDomainInstanceService();
		mockInstanceContentService();
		mockSearchService(true);
		mockReturnedDefinition(false, false, "");
		templateService.create(inputTemplateData);
	}

	@Test(expected = IllegalArgumentException.class)
	public void should_Throw_Exception_When_Definition_Is_Valid_But_RdfType_Missing() {
		TemplateInstance inputTemplateData = mockInputTemplateInstance(Boolean.TRUE, "sampleForType");
		mockDomainInstanceService();
		mockInstanceContentService();
		mockSearchService(true);
		mockReturnedDefinition(true, false, "");
		templateService.create(inputTemplateData);
	}

	private void mockReturnedDefinition(boolean hasDefinition, boolean hasRdfType, String libraryId) {
		if (hasDefinition) {
			DefinitionModel definitionModel = mock(DefinitionModel.class);
			Optional<PropertyDefinition> definitionFieldOptional = Optional.empty();
			if(hasRdfType) {
				PropertyDefinition property = mock(PropertyDefinition.class);
				when(property.getDefaultValue()).thenReturn(libraryId);
				definitionFieldOptional = Optional.of(property);
			}
			when(definitionModel.getField(eq(DefaultProperties.SEMANTIC_TYPE))).thenReturn(definitionFieldOptional);
			when(dictionaryService.find(anyString())).thenReturn(definitionModel);
		} else {
			when(dictionaryService.find(anyString())).thenReturn(null);
		}
	}

	private Instance mockExistingTemplateInstance(Boolean primaryTemplate) {
		EmfInstance loadedInstance = new EmfInstance();
		loadedInstance.setId("loadedTemplateId");
		loadedInstance.add(DefaultProperties.TITLE, "loadedTemplateTitle");
		loadedInstance.add(TemplateProperties.FOR_OBJECT_TYPE, "sampleForType");
		loadedInstance.add(TemplateProperties.TEMPLATE_PURPOSE, "creatable");
		loadedInstance.add(TemplateProperties.IS_PRIMARY_TEMPLATE, primaryTemplate);

		InstanceReference reference = new InstanceReferenceMock(loadedInstance);
		Optional<InstanceReference> loadedInstanceOptional = Optional.of(reference);
		when(instanceTypeResolver.resolveReference(anyString())).thenReturn(loadedInstanceOptional);
		return loadedInstance;
	}

	private void mockSearchService(boolean emptyResult) {
		doAnswer(new Answer() {
			public Object answer(InvocationOnMock invocation) {
				Object[] args = invocation.getArguments();
				SearchArguments<Instance> searchArgs = (SearchArguments<Instance>) args[1];
				if (emptyResult) {
					searchArgs.setResult(CollectionUtils.emptyList());
				} else {
					EmfInstance primaryTemplate = new EmfInstance();
					primaryTemplate.setId("existingPrimaryTemplateId");
					List<Instance> instances = Collections.singletonList(primaryTemplate);
					searchArgs.setResult(instances);
				}
				return null;
			}
		}).when(searchService).searchAndLoad(any(), any());
	}

	private Instance mockDomainInstanceService() {
		// mock the instance returned after the initial create
		Instance newlyCreatedInstance = Mockito.mock(Instance.class);
		when(newlyCreatedInstance.getId()).thenReturn("newlyCreatedInstanceId");
		when(domainInstanceService.createInstance(eq(TemplateProperties.TEMPLATE_DEFINITION_ID),
				anyString())).thenReturn(newlyCreatedInstance);

		// mock the newly saved instance with only ID and no properties yet
		Instance savedInstance = Mockito.mock(Instance.class);
		when(savedInstance.getId()).thenReturn("savedInstanceId");
		when(domainInstanceService.save(any(InstanceSaveContext.class)))
				.thenReturn(savedInstance);
		return newlyCreatedInstance;
	}

	private void mockInstanceContentService() {
		InputStream contentInputStream = new ByteArrayInputStream(
				"Sample content of the source instance".getBytes(StandardCharsets.UTF_8));
		ContentInfo contentInfo = Mockito.mock(ContentInfo.class);
		when(contentInfo.exists()).thenReturn(Boolean.TRUE);
		when(contentInfo.getInputStream()).thenReturn(contentInputStream);
		when(instanceContentService.getContent(any(EmfInstance.class), eq(Content.PRIMARY_VIEW)))
				.thenReturn(contentInfo);
	}

	private static TemplateInstance mockInputTemplateInstance(Boolean primary, String forType) {
		Instance sourceInstance = Mockito.mock(Instance.class);
		when(sourceInstance.getId()).thenReturn("sourceInstanceId");
		TemplateInstance inputTemplateData = Mockito.mock(TemplateInstance.class);
		when(inputTemplateData.getOwningInstance()).thenReturn(sourceInstance);
		when(inputTemplateData.getForType()).thenReturn(forType);
		when(inputTemplateData.get(eq(DefaultProperties.TITLE))).thenReturn("sampleTitle");
		when(inputTemplateData.getPurpose()).thenReturn(TemplatePurposes.CREATABLE);
		when(inputTemplateData.getPrimary()).thenReturn(primary);
		return inputTemplateData;
	}

	private TemplateInstance mockInstanceDao(Serializable id, String content, String correspondingInstance,
			String forType) {
		TemplateInstance defaultTemplate = new TemplateInstance();
		defaultTemplate.setId(id);
		defaultTemplate.setIdentifier("sampleIdentifier");
		defaultTemplate.add(DefaultProperties.CONTENT, content);
		if (StringUtils.isNotNullOrEmpty(correspondingInstance)) {
			defaultTemplate.setCorrespondingInstance(correspondingInstance);
		}
		defaultTemplate.setForType(forType);
		defaultTemplate.add(DefaultProperties.TITLE, "sampleTitle");
		defaultTemplate.setPurpose(TemplatePurposes.CREATABLE);
		defaultTemplate.setPrimary(Boolean.TRUE);

		Mockito
				.when(instanceDao.loadInstance(Matchers.any(Serializable.class), Matchers.any(Serializable.class),
						Matchers.anyBoolean()))
					.thenReturn(defaultTemplate);
		Mockito.when(instanceDao.createInstance(Matchers.any(DefinitionModel.class), Matchers.anyBoolean())).thenReturn(
				defaultTemplate);
		return defaultTemplate;
	}

	private void mockCompiler() {
		when(adapterService.getContentDescriptor(any(TemplateInstance.class)))
				.then(a -> FileDescriptor.create(null, 0));

		TemplateDefinition definition = new TemplateDefinitionImpl();
		when(compiler.compileDefinitions(any(DefinitionCompilerCallback.class), anyBoolean()))
				.thenReturn(Arrays.asList(definition));
	}

	private static String readFileAsString(String filePath) {
		URL url = TemplateServiceImplTest.class.getResource(filePath);
		try {
			return FileUtils.readFileToString(new File(url.toURI()), StandardCharsets.UTF_8);
		} catch (IOException | URISyntaxException e) {
			throw new RuntimeException(e);
		}
	}
}
