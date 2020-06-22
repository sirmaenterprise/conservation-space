package com.sirma.itt.seip.template;

import static com.sirma.itt.seip.domain.ObjectTypes.TEMPLATE;
import static com.sirma.itt.seip.domain.instance.DefaultProperties.STATUS;
import static com.sirma.itt.seip.domain.instance.DefaultProperties.TITLE;
import static com.sirma.itt.seip.template.TemplateActions.ACTIVATE_TEMPLATE;
import static com.sirma.itt.seip.template.TemplateActions.DEACTIVATE_TEMPLATE;
import static com.sirma.itt.seip.template.TemplateActions.RELOAD_TEMPLATE;
import static com.sirma.itt.seip.template.TemplateActions.SET_TEMPLATE_AS_PRIMARY;
import static com.sirma.itt.seip.template.TemplateProperties.FOR_OBJECT_TYPE;
import static com.sirma.itt.seip.template.TemplateProperties.IS_PRIMARY_TEMPLATE;
import static com.sirma.itt.seip.template.TemplateProperties.TEMPLATE_PURPOSE;
import static com.sirma.itt.seip.template.TemplateProperties.TEMPLATE_RULE;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyList;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.argThat;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.doReturn;
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
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.mockito.Spy;

import com.sirma.itt.seip.Entity;
import com.sirma.itt.seip.Pair;
import com.sirma.itt.seip.adapters.AdaptersConfiguration;
import com.sirma.itt.seip.concurrent.locks.ContextualReadWriteLock;
import com.sirma.itt.seip.content.ContentAdapterService;
import com.sirma.itt.seip.convert.TypeConverter;
import com.sirma.itt.seip.db.DatabaseIdManager;
import com.sirma.itt.seip.db.DbDao;
import com.sirma.itt.seip.definition.DefinitionService;
import com.sirma.itt.seip.definition.dozer.DefinitionsDozerProvider;
import com.sirma.itt.seip.domain.definition.DefinitionModel;
import com.sirma.itt.seip.domain.definition.PropertyDefinition;
import com.sirma.itt.seip.domain.definition.label.LabelProvider;
import com.sirma.itt.seip.domain.instance.DefaultProperties;
import com.sirma.itt.seip.domain.instance.EmfInstance;
import com.sirma.itt.seip.domain.instance.Instance;
import com.sirma.itt.seip.domain.instance.InstancePropertyNameResolver;
import com.sirma.itt.seip.domain.instance.InstanceReference;
import com.sirma.itt.seip.domain.instance.InstanceType;
import com.sirma.itt.seip.domain.rest.EmfApplicationException;
import com.sirma.itt.seip.domain.security.ActionTypeConstants;
import com.sirma.itt.seip.event.EventService;
import com.sirma.itt.seip.instance.DomainInstanceService;
import com.sirma.itt.seip.instance.InstanceSaveContext;
import com.sirma.itt.seip.instance.InstanceTypeResolver;
import com.sirma.itt.seip.instance.dao.InstanceDao;
import com.sirma.itt.seip.instance.dao.InstanceService;
import com.sirma.itt.seip.instance.dozer.CommonDozerProvider;
import com.sirma.itt.seip.instance.state.Operation;
import com.sirma.itt.seip.resources.User;
import com.sirma.itt.seip.security.context.SecurityContext;
import com.sirma.itt.seip.tasks.DefaultSchedulerConfiguration;
import com.sirma.itt.seip.tasks.SchedulerConfiguration;
import com.sirma.itt.seip.tasks.SchedulerContext;
import com.sirma.itt.seip.tasks.SchedulerService;
import com.sirma.itt.seip.template.db.TemplateContentEntity;
import com.sirma.itt.seip.template.db.TemplateDao;
import com.sirma.itt.seip.template.db.TemplateEntity;
import com.sirma.itt.seip.template.dozer.TemplateDozerProvider;
import com.sirma.itt.seip.template.exceptions.InvalidTemplateOperationException;
import com.sirma.itt.seip.template.exceptions.MissingTemplateException;
import com.sirma.itt.seip.template.rules.TemplateRuleTranslator;
import com.sirma.itt.seip.template.schedule.TemplateActivateScheduler;
import com.sirma.itt.seip.testutil.CustomMatcher;
import com.sirma.itt.seip.testutil.fakes.InstanceTypeFake;
import com.sirma.itt.seip.testutil.fakes.TransactionSupportFake;
import com.sirma.itt.seip.testutil.mocks.ConfigurationPropertyMock;
import com.sirma.itt.seip.testutil.mocks.InstanceReferenceMock;
import com.sirma.sep.content.Content;
import com.sirma.sep.content.ContentInfo;
import com.sirma.sep.content.InstanceContentService;

/**
 * Test the {@link TemplateServiceImpl}.
 *
 * @author nvelkov
 * @author Vilizar Tsonev
 */
public class TemplateServiceImplTest {

	// instantiated this way in order to simulate a pseudo-integration test and test the whole functionality.
	@Spy
	@InjectMocks
	private TemplateInstanceHelper templateInstanceHelper = new TemplateInstanceHelper();

	// instantiated this way in order to simulate a pseudo-integration test and test the whole functionality.
	@Spy
	@InjectMocks
	private TemplateDao templateDao = new TemplateDao();

	@Spy
	@InjectMocks
	private TemplateServiceImpl templateService = new TemplateServiceImpl(templateInstanceHelper, templateDao);

	@Mock
	private InstanceDao instanceDao;

	@Mock
	private EventService eventService;

	@Mock
	private DbDao dbDao;

	@Mock
	private ContentAdapterService adapterService;

	@Mock
	private LabelProvider labelProvider;

	@Mock
	private TypeConverter typeConverter;

	@Mock
	private TemplatePreProcessor preProcessor;

	@Mock
	private DomainInstanceService domainInstanceService;

	@Mock
	private InstanceService instanceService;

	@Mock
	private InstanceContentService instanceContentService;

	@Mock
	private InstanceTypeResolver instanceTypeResolver;

	@Mock
	private SchedulerService schedulerService;

	@Mock
	private DefinitionService definitionService;

	@Mock
	private DatabaseIdManager databaseIdManager;

	@Mock
	private TemplateRuleTranslator templateRuleTranslator;

	@Mock
	private AdaptersConfiguration adaptersConfiguration;

	@Mock
	private SecurityContext securityContext;

	@Spy
	private TransactionSupportFake transactionSupport;

	@Spy
	private ContextualReadWriteLock contextualLock = ContextualReadWriteLock.create();

	@Spy
	private InstancePropertyNameResolver nameResolver = InstancePropertyNameResolver.NO_OP_INSTANCE;

	@Rule
	public TemporaryFolder tempFolder = new TemporaryFolder();

	private static final String DEFAULT_TEMPLATE_ID = "emf:defaultTemplate";

	/**
	 * Used when a default template is not found so this is returned as the default template content.
	 */
	private static final String DEFAULT_TEMPLATE_CONTENT = "<div data-tabs-counter=\"2\">"
			+ "<section data-title=\"Tab1\"data-show-navigation=\"true\""
			+ "data-show-comments=\"true\"data-default=\"true\"></section></div>";

	@Before
	public void init() {
		List<String> files = new LinkedList<>();
		files.add(DefinitionsDozerProvider.DOZER_COMMON_MAPPING_XML);
		files.add(CommonDozerProvider.DOZER_COMMON_MAPPING_XML);
		files.add(TemplateDozerProvider.DOZER_TEMPLATE_MAPPING_XML);
		MockitoAnnotations.initMocks(this);
		when(typeConverter.convert(eq(String.class), any(Object.class))).then(a -> {
			Object arg = a.getArgumentAt(1, Object.class);
			if (arg == null) {
				return arg;
			}
			return arg.toString();
		});

		SchedulerConfiguration dummyConfiguration = new DefaultSchedulerConfiguration();
		when(schedulerService.buildEmptyConfiguration(any())).thenReturn(dummyConfiguration);

		when(adaptersConfiguration.getDmsContainerId()).thenReturn(new ConfigurationPropertyMock<>("test"));

		DefinitionModel definition = mock(DefinitionModel.class);
		when(definition.getIdentifier()).thenReturn(TemplateProperties.TEMPLATE_DEFINITION_ID);

		when(definitionService.find(eq(TemplateProperties.TEMPLATE_DEFINITION_ID))).thenReturn(definition);
		when(securityContext.isActive()).thenReturn(Boolean.TRUE);
		User user = mock(User.class);
		when(user.getIdentityId()).thenReturn("user");
		when(securityContext.getAuthenticated()).thenReturn(user);
	}

	@Test
	@SuppressWarnings("unchecked")
	public void should_Synchronize_Relational_Record_When_Changes_In_Definition() {
		Template testTemplate = constructTemplate("template", "Template", "test", "emf:InstanceNew", "sampleForType",
				true, "");

		TemplateEntity existingEntity = new TemplateEntity();
		existingEntity.setId(Long.valueOf(100));
		existingEntity.setCorrespondingInstance("emf:InstanceOld");
		List<TemplateEntity> entities = Collections.singletonList(existingEntity);
		when(dbDao.fetchWithNamed(eq(TemplateEntity.QUERY_TEMPLATES_BY_TEMPLATE_IDS_KEY), anyList()))
		.thenReturn(entities);
		when(instanceService.loadDeleted(eq("emf:InstanceNew"))).thenReturn(Optional.empty());

		templateService.saveOrUpdateImportedTemplate(testTemplate);

		ArgumentCaptor<TemplateEntity> captor = ArgumentCaptor.forClass(TemplateEntity.class);
		verify(dbDao).saveOrUpdate(captor.capture());
		assertEquals(Long.valueOf(100), captor.getValue().getId());
		assertEquals("emf:InstanceNew", captor.getValue().getCorrespondingInstance());
	}

	@Test
	@SuppressWarnings("unchecked")
	public void should_Not_Synchronize_Relational_Record_When_No_Changes_In_Definition() {
		Template testTemplate = constructTemplate("template", "Template", "test", "emf:InstanceOld", "sampleForType",
				true, "");

		TemplateEntity existingEntity = new TemplateEntity();
		existingEntity.setId(Long.valueOf(100));
		existingEntity.setCorrespondingInstance("emf:InstanceOld");
		List<TemplateEntity> entities = Collections.singletonList(existingEntity);
		when(dbDao.fetchWithNamed(eq(TemplateEntity.QUERY_TEMPLATES_BY_TEMPLATE_IDS_KEY), anyList()))
		.thenReturn(entities);
		when(instanceService.loadDeleted(eq("emf:InstanceOld"))).thenReturn(Optional.empty());

		templateService.saveOrUpdateImportedTemplate(testTemplate);

		verify(dbDao, never()).saveOrUpdate(any());
	}

	@Test
	public void should_NotLoadContent_When_NoTemplateIdIsProvided() {
		assertNull(templateService.getContent(null));
	}

	@Test
	public void should_Load_Correct_Content_From_Template_Definition() {
		withExistingTemplateInRelationalDB(constructTemplate("template1", "sampleTitle", "test", "emf:Instance1",
				"ET220001", true, "department == some content"));

		withExistingTemplateContent("template1" , "Test123");

		String actualContent = templateService.getContent("template1");
		assertEquals("Test123", actualContent);
	}

	@Test(expected = EmfApplicationException.class)
	public void should_Throw_Exception_If_Template_Is_Missing_A_Content() {
		withExistingTemplateInRelationalDB(constructTemplate("testtemplate1", "sampleTitle", "test", "emf:Instance1",
				"ET220001", true, "department == some content"));

		templateService.getContent("testtemplate1");
	}

	@Test
	public void should_not_load_content_not_found_template() {
		String actualContent = templateService.getContent("template1");
		assertNull(actualContent);
	}

	@Test
	public void should_Load_Default_Template_Content_When_DefaultTemplate_Requested() {
		String actualContent = templateService.getContent(DEFAULT_TEMPLATE_ID);
		assertEquals(DEFAULT_TEMPLATE_CONTENT, actualContent);
	}

	@Test
	@SuppressWarnings("unchecked")
	public void should_Retrieve_CorrespondingInstanceId_From_DB_When_Missing_In_Passed_Template() {
		Template testTemplate = constructTemplate("template", "Template", "test", null, "sampleForType", true, "");

		TemplateEntity entity = new TemplateEntity();
		entity.setCorrespondingInstance("existingCorrespondingInstanceRDB");
		List<TemplateEntity> entities = Collections.singletonList(entity);
		when(dbDao.fetchWithNamed(eq(TemplateEntity.QUERY_TEMPLATES_BY_TEMPLATE_IDS_KEY), anyList()))
		.thenReturn(entities);

		when(instanceService.loadDeleted(eq("existingCorrespondingInstanceRDB"))).thenReturn(Optional.empty());

		templateService.saveOrUpdateImportedTemplate(testTemplate);

		// if the identifier in the template definition is with mixed upper and lower cases, it must be converted to
		// lower before search, because only lower-case identifiers are stored in RDB
		ArgumentCaptor<List> captor = ArgumentCaptor.forClass(List.class);
		verify(dbDao, times(2)).fetchWithNamed(eq(TemplateEntity.QUERY_TEMPLATES_BY_TEMPLATE_IDS_KEY),
				captor.capture());
		Pair<String, Object> arg = (Pair<String, Object>) captor.getValue().get(0);
		List<String> passedIds = (List<String>) arg.getSecond();
		assertFalse(passedIds.isEmpty());
		assertEquals("template", passedIds.get(0));

		verify(instanceService).loadDeleted(eq("existingCorrespondingInstanceRDB"));
	}

	@Test
	public void should_Schedule_Corresponding_Instance_For_Activation() {
		Template testTemplate = constructTemplate("myTemplate", "My Template", "test", null, "sampleForType", true, "");
		withExistingTemplateInstance(null, null, null);
		mockDomainInstanceService();
		definitionWithSemanticTypeExists(true, true, "emf:sampleSemanticClass", "sampleForType");

		templateService.saveOrUpdateImportedTemplate(testTemplate);

		ArgumentCaptor<SchedulerContext> captor = ArgumentCaptor.forClass(SchedulerContext.class);
		verify(schedulerService).schedule(eq(TemplateActivateScheduler.BEAN_ID), any(SchedulerConfiguration.class),
				captor.capture());
		assertEquals("savedInstanceId", captor.getValue().get("correspondingInstanceId"));
	}

	@Test
	public void should_Not_Try_To_Activate_Email_Templates() {
		Template testTemplate = constructTemplate("myTemplate", "My Template", "test", null, "emailTemplate", true, "");

		templateService.saveOrUpdateImportedTemplate(testTemplate);

		verify(domainInstanceService, never()).createInstance(anyString(), anyString());
		verify(domainInstanceService, never()).save(any(InstanceSaveContext.class));
		verify(schedulerService, never()).schedule(anyString(), any(), any());
	}

	@Test
	public void should_UpdateInstance_WhenReloadingTemplates_And_TemplateDataDiffersFromTemplateInstance() {
		Template testTemplate = constructTemplate("myTemplate", "My Template", "some content",
				"correspondingInstanceId", "sampleForType", true, "(department == \"DEV\" || department == \"BA\")");
		when(templateRuleTranslator.translate(eq("(department == \"DEV\" || department == \"BA\")"),
				eq("sampleForType"))).thenReturn("ruleDescription");

		withExistingCorrespondingInstance(constructTemplate("loadedTemplateId", "loadedTemplateTitle", "some content",
				"correspondingInstanceId", "sampleForType", true, "(department == \"TEST\" || department == \"BA\")"),
				null, "ACTIVE");

		templateService.saveOrUpdateImportedTemplate(testTemplate);

		ArgumentCaptor<InstanceSaveContext> captor = ArgumentCaptor.forClass(InstanceSaveContext.class);
		verify(domainInstanceService).save(captor.capture());

		String updatedContent = captor
				.getValue()
				.getInstance()
				.getProperties()
				.get(DefaultProperties.TEMP_CONTENT_VIEW)
				.toString();

		String updatedRule = captor.getValue().getInstance().getAsString(TemplateProperties.TEMPLATE_RULE);
		String updatedRuleDescription = captor.getValue().getInstance().getAsString(
				TemplateProperties.TEMPLATE_RULE_DESCRIPTION);

		assertEquals("some content", updatedContent);
		assertEquals("(department == \"DEV\" || department == \"BA\")", updatedRule);
		assertEquals("ruleDescription", updatedRuleDescription);
		assertEquals(RELOAD_TEMPLATE, captor.getValue().getOperation().getOperation());
	}

	@Test
	public void should_NotUpdateInstance_WhenReloadingTemplates_And_InstanceStatusIsDraft() {
		Template testTemplate = constructTemplate("myTemplate", "My Template", "some content",
				"correspondingInstanceId", "sampleForType", true, "");
		mockInstanceContentService();

		withExistingCorrespondingInstance(constructTemplate("loadedTemplateId", "loadedTemplateTitle", "some content",
				"correspondingInstanceId", "sampleForType", true, "department == loadedTemplateRule"), null, "DRAFT");

		templateService.saveOrUpdateImportedTemplate(testTemplate);

		verify(domainInstanceService, never()).save(any());
	}

	@Test
	public void should_NotUpdateInstance_WhenReloadingTemplates_And_InstanceStatusIsUPDATED() {
		Template testTemplate = constructTemplate("myTemplate", "My Template", "some content",
				"correspondingInstanceId", "sampleForType", true, "");
		mockInstanceContentService();

		withExistingCorrespondingInstance(constructTemplate("loadedTemplateId", "loadedTemplateTitle", "some content",
				"correspondingInstanceId", "sampleForType", true, "department == loadedTemplateRule"), null, "UPDATED");

		templateService.saveOrUpdateImportedTemplate(testTemplate);

		verify(domainInstanceService, never()).save(any());
	}

	@Test
	public void should_Activate_Instance_When_Reloading_Templates_And_Instance_Is_Inactive() {
		Template testTemplate = constructTemplate("myTemplate", "My Template", "some content",
				"correspondingInstanceId", "sampleForType", true, "");
		mockInstanceContentService();

		withExistingCorrespondingInstance(constructTemplate("loadedTemplateId", "sampleTitle", "some content",
				"correspondingInstanceId", "sampleForType", false, ""), null, "INACTIVE");

		templateService.saveOrUpdateImportedTemplate(testTemplate);

		// verify instance is reloaded
		ArgumentCaptor<InstanceSaveContext> reloadCaptor = ArgumentCaptor.forClass(InstanceSaveContext.class);
		verify(domainInstanceService).save(reloadCaptor.capture());
		assertEquals(RELOAD_TEMPLATE, reloadCaptor.getValue().getOperation().getOperation());

		// verify instance is scheduled for activation
		ArgumentCaptor<SchedulerContext> captor = ArgumentCaptor.forClass(SchedulerContext.class);
		verify(schedulerService).schedule(eq(TemplateActivateScheduler.BEAN_ID), any(SchedulerConfiguration.class),
				captor.capture());
		assertEquals("correspondingInstanceId", captor.getValue().get("correspondingInstanceId"));
	}

	@Test
	public void should_UpdateInstance_WhenReloadingTemplates_And_TemplateContentIsChanged() {
		final String TITLE_VALUE = "Template title";
		final String OBJECT_TYPE = "test type";

		Template testTemplate = constructTemplate("myTemplate", "My Template", "some content",
				"correspondingInstanceId", "sampleForType", true, "");
		mockInstanceContentService();

		withExistingCorrespondingInstance(constructTemplate("loadedTemplateId", TITLE_VALUE, "some content",
				"correspondingInstanceId", OBJECT_TYPE, true, "department == loadedTemplateRule"), null, "ACTIVE");

		templateService.saveOrUpdateImportedTemplate(testTemplate);

		ArgumentCaptor<InstanceSaveContext> captor = ArgumentCaptor.forClass(InstanceSaveContext.class);
		verify(domainInstanceService).save(captor.capture());

		String updatedContent = captor
				.getValue()
				.getInstance()
				.getProperties()
				.get(DefaultProperties.TEMP_CONTENT_VIEW)
				.toString();

		assertEquals("some content", updatedContent);
		assertEquals(RELOAD_TEMPLATE, captor.getValue().getOperation().getOperation());
	}

	@Test
	public void should_UndeleteInstance_IfPreviouslyDeleted() {
		Template testTemplate = constructTemplate("myTemplate", "My Template", "some content",
				"correspondingInstanceId", "sampleForType", true, "");

		Template templateInstance = constructTemplate("loadedTemplateId", "loadedTemplateTitle", "some content",
				"correspondingInstanceId", "sampleForType", true, "department == loadedTemplateRule");
		withExistingCorrespondingInstance(templateInstance, Boolean.TRUE, null);

		templateService.saveOrUpdateImportedTemplate(testTemplate);

		ArgumentCaptor<InstanceSaveContext> captor = ArgumentCaptor.forClass(InstanceSaveContext.class);
		verify(domainInstanceService).save(captor.capture());

		Instance instance = captor.getValue().getInstance();

		assertFalse(instance.getBoolean(DefaultProperties.IS_DELETED));
		assertNull(instance.getString(DefaultProperties.DELETED_ON));
		assertEquals("ACTIVE", instance.getString(DefaultProperties.STATUS));
	}

	@Test
	public void Should_Create_New_Instance_With_All_Provided_Data() {
		Template inputTemplateData = mockInputTemplateInstance(Boolean.TRUE, "sampleForType");
		Instance newlyCreatedInstance = mockDomainInstanceService();
		withExistingTemplateInstance(null, null, null);
		definitionWithSemanticTypeExists(true, true, "emf:sampleSemanticClass", "sampleForType");

		String CONTENT = "Sample content";

		String returnedId = templateService.create(inputTemplateData, CONTENT);

		// some tab-related html is added at the end of the content when parsing, so only the beginning of
		// the string (which is the original content we have passed) is verified
		Map<String, Serializable> properties = newlyCreatedInstance.getProperties();
		assertTrue(properties.get(DefaultProperties.TEMP_CONTENT_VIEW).toString().startsWith(CONTENT));
		assertEquals("sampleTitle", properties.get(DefaultProperties.TITLE));
		assertEquals("sampleForType", properties.get(TemplateProperties.FOR_OBJECT_TYPE));
		assertEquals(TemplatePurposes.CREATABLE, properties.get(TemplateProperties.TEMPLATE_PURPOSE));
		assertTrue((Boolean) properties.get(TemplateProperties.IS_PRIMARY_TEMPLATE));

		verify(domainInstanceService, Mockito.times(1)).save(any(InstanceSaveContext.class));
		assertEquals("savedInstanceId", returnedId);
	}

	@Test(expected = NullPointerException.class)
	public void Should_Throw_NPE_When_No_SourceViewIsProvided() {
		templateService.create(new Template(), null);
	}

	@Test(expected = NullPointerException.class)
	public void Should_Throw_NPE_When_No_ForType() {
		Instance sourceInstance = mock(Instance.class);
		when(sourceInstance.getId()).thenReturn("sourceInstanceId");
		Template inputTemplateData = mock(Template.class);
		templateService.create(inputTemplateData, "test content");
	}

	@Test
	public void should_Create_And_Upload_To_DMS_When_No_Instance_Specified() {
		withExistingTemplateInstance(null, null, null);
		Template testTemplate = constructTemplate("myTemplate", "My Template", "some content", null, "sampleForType",
				true, "");
		mockDomainInstanceService();
		definitionWithSemanticTypeExists(true, true, "emf:sampleSemanticClass", "sampleForType");

		templateService.saveOrUpdateImportedTemplate(testTemplate);

		verify(domainInstanceService).createInstance(any(DefinitionModel.class), any());

		verify(domainInstanceService, Mockito.times(1))
		.save(argThat(CustomMatcher.of((InstanceSaveContext saveContext) -> {
			assertEquals("newlyCreatedInstanceId", saveContext.getInstanceId());
			assertNotNull(saveContext.getOperation());
			assertEquals(ActionTypeConstants.CREATE, saveContext.getOperation().getOperation());
		})));
	}

	@Test
	public void should_Create_New_Instance_When_Id_Specified_And_Doesnt_Exist_In_DB() {
		String INSTANCE_ID = "correspondingInstanceId";

		withExistingTemplateInstance(null, null, null);
		Template testTemplate = constructTemplate("myTemplate", "My Template", "some content", INSTANCE_ID,
				"sampleForType", true, "");
		mockDomainInstanceService();
		definitionWithSemanticTypeExists(true, true, "emf:sampleSemanticClass", "sampleForType");
		when(instanceService.loadDeleted(eq("correspondingInstanceId"))).thenReturn(Optional.empty());

		templateService.saveOrUpdateImportedTemplate(testTemplate);

		verify(domainInstanceService).createInstance(any(DefinitionModel.class), any());
		verify(domainInstanceService, Mockito.times(1))
		.save(argThat(CustomMatcher.of((InstanceSaveContext saveContext) -> {
			assertEquals(INSTANCE_ID, saveContext.getInstanceId());
			assertNotNull(saveContext.getOperation());
			assertEquals(ActionTypeConstants.CREATE, saveContext.getOperation().getOperation());
		})));
	}

	@Test
	public void should_Update_Content_With_Template_Ids() {
		String content = readFileAsString("/templates/template-with-widgets.html");
		withExistingTemplateInstance(null, null, null);
		Template testTemplate = constructTemplate("myTemplate", "My Template", content, null, "sampleForType", true,
				"");
		mockDomainInstanceService();
		definitionWithSemanticTypeExists(true, true, "emf:sampleSemanticClass", "sampleForType");
		when(instanceService.loadDeleted(eq("correspondingInstanceId"))).thenReturn(Optional.empty());

		templateService.saveOrUpdateImportedTemplate(testTemplate);

		ArgumentCaptor<Instance> captor = ArgumentCaptor.forClass(Instance.class);
		verify(domainInstanceService).createInstance(any(), captor.capture());
		assertEquals("emf:sampleSemanticClass", captor.getValue().getId());

		verify(domainInstanceService, Mockito.times(1))
		.save(argThat(CustomMatcher.of((InstanceSaveContext saveContext) -> {
			assertEquals("newlyCreatedInstanceId", saveContext.getInstanceId());
			assertNotNull(saveContext.getOperation());
			assertEquals(ActionTypeConstants.CREATE, saveContext.getOperation().getOperation());
		})));
	}

	@Test
	public void should_Not_Filter_Templates_If_No_Criteria_Provided() {
		Template template1 = constructTemplate("template1", "sampleTitle", "test", "emf:Instance1", "ET220001", true,
				"department == \"ENG\"");
		Template template2 = constructTemplate("template2", "sampleTitle", "test", "emf:Instance2", "ET220001", true,
				"department == \"QAL\" && functional == \"QAS\"");
		Template template3 = constructTemplate("template3", "sampleTitle", "test", "emf:Instance3", "ET220001", true,
				"");

		withExistingTemplatesInRelationalDB(Arrays.asList(template1, template2, template3));

		TemplateSearchCriteria criteria = new TemplateSearchCriteria("ET220001", "creatable", null);
		List<Template> resultTemplates = templateService.getTemplates(criteria);

		// all initially found 3 templates for the type and purpose should be left non-filtered + the default (blank)
		// template added as well
		assertEquals(4, resultTemplates.size());
	}

	@Test
	public void should_ProvideAllTemplatesWithoutRule_When_AnEmptyCriteriaIsProvided() {
		Template template1 = constructTemplate("template1", "sampleTitle", null, "emf:Instance1", "ET220001", true,
				"department == \"ENG\"");
		Template template2 = constructTemplate("template2", "sampleTitle", null, "emf:Instance2", "ET220001", true,
				"department == \"QAL\" && functional == \"QAS\"");
		Template template3 = constructTemplate("template3", "sampleTitle", null, "emf:Instance3", "ET220001", true,
				null);

		withExistingTemplatesInRelationalDB(Arrays.asList(template1, template2, template3));

		TemplateSearchCriteria criteria = new TemplateSearchCriteria("ET220001", "creatable", Collections.emptyMap());
		List<Template> resultTemplates = templateService.getTemplates(criteria);

		// all initially found 1 template for the type and purpose should be left non-filtered + the default (blank)
		// template added as well
		assertEquals(2, resultTemplates.size());

		assertEquals("template3", resultTemplates.get(0).getId());
	}

	@Test
	@SuppressWarnings("unchecked")
	public void should_Find_And_Filter_Templates_Correctly() {
		Template template1 = constructTemplate("template1", "sampleTitle", "test", "emf:Instance1", "ET220001", true,
				"department == \"ENG\"");
		Template template2 = constructTemplate("template2", "sampleTitle", "test", "emf:Instance2", "ET220001", true,
				"department == \"QAL\" && functional == \"QAS\"");
		Template template3 = constructTemplate("template3", "sampleTitle", "test", "emf:Instance3", "ET220001", true,
				"");
		withExistingTemplatesInRelationalDB(Arrays.asList(template1, template2, template3));

		Map<String, Serializable> filter = new HashMap<>();
		filter.put("department", "ENG");
		TemplateSearchCriteria criteria = new TemplateSearchCriteria("ET220001", "creatable", filter);

		List<Template> resultTemplates = templateService.getTemplates(criteria);

		ArgumentCaptor<List<Pair<String, Object>>> captor = ArgumentCaptor.forClass(List.class);
		verify(dbDao).fetchWithNamed(eq(TemplateEntity.QUERY_TEMPLATES_FOR_GROUP_ID_PURPOSE_KEY),
				captor.capture());
		// verify that the DB query is constructed with the correct params for forType (group) and purpose
		assertEquals("ET220001", captor.getValue().get(0).getSecond());
		assertEquals("creatable", captor.getValue().get(1).getSecond());
		// one left after filtering + the default (blank) template
		assertEquals(2, resultTemplates.size());
		// only template1 matches to the rule department = ENG
		assertNotNull(resultTemplates.get(0));
		// the returned filtered template should't have a content (it gets lazy-loaded)
		template1.setContent(null);
		assertEquals(template1, resultTemplates.get(0));
	}

	@Test
	public void should_ProvideTheFirstTemplate_When_ASingleTemplateIsRequested() {
		Template template1 = constructTemplate("template1", "sampleTitle", "test", "emf:Instance1", "ET220001", true,
			"department == \"ENG\"");
		Template template2 = constructTemplate("template2", "sampleTitle", "test", "emf:Instance1", "ET220001", true,
			"department == \"ENG\"");

		withExistingTemplatesInRelationalDB(Arrays.asList(template1, template2));

		Template result = templateService.getTemplate(new TemplateSearchCriteria("ET220001", "creatable", null));

		assertEquals("template1", result.getId());
	}

	@Test
	public void should_SortTemplatesByRulesCount_When_MultiplePrimaryTemplatesAreRequested() {
		Template template1 = constructTemplate("template1", "sampleTitle", "test", "emf:Instance1", "ET220001", true,
											   "department == \"ENG\"");
		Template template2 = constructTemplate("template2", "sampleTitle", "test", "emf:Instance2", "ET220001", true,
											   "department == \"QAL\" && functional == \"QAS\"");
		Template template3 = constructTemplate("template3", "sampleTitle", "test", "emf:Instance3", "ET220001", true,
											   "");

		withExistingTemplatesInRelationalDB(Arrays.asList(template1, template2, template3));

		TemplateSearchCriteria criteria = new TemplateSearchCriteria("ET220001", "creatable", null);
		List<Template> result = templateService.getTemplates(criteria);

		assertEquals("template2", result.get(0).getId());
		assertEquals("template1", result.get(1).getId());
		assertEquals("template3", result.get(2).getId());
	}

	@Test
	public void primary_should_be_first_when_there_are_no_rules() {
		Template template1 = constructTemplate("template1", "title", "test", "emf:Instance1", "ET220001", false, "");
		Template template2 = constructTemplate("template2", "title", "test", "emf:Instance2", "ET220001", true, "");
		Template template3 = constructTemplate("template3", "title", "test", "emf:Instance3", "ET220001", false, "");

		withExistingTemplatesInRelationalDB(Arrays.asList(template1, template2, template3));

		TemplateSearchCriteria criteria = new TemplateSearchCriteria("ET220001", "creatable", null);
		List<Template> result = templateService.getTemplates(criteria);

		assertEquals("template2", result.get(0).getId());
		assertEquals("template1", result.get(1).getId());
		assertEquals("template3", result.get(2).getId());
	}

	@Test
	public void should_Return_Default_Content_When_Default_Template_Is_Requested() {
		Template template1 = constructTemplate(DEFAULT_TEMPLATE_ID, "sampleTitle", "test", "emf:Instance1", "ET220001",
				true, null);
		withExistingTemplatesInRelationalDB(Arrays.asList(template1));

		Template result = templateService.getTemplate(new TemplateSearchCriteria("ET220001", "creatable", null));

		assertEquals(DEFAULT_TEMPLATE_CONTENT, result.getContent());
	}

	@Test
	public void should_ProvideNoTemplate_When_SingleTemplateIsRequestedButThereAreNoEligibleTemplates() {
		doReturn(new ArrayList<>()).when(templateService).getTemplates(any());

		Template result = templateService.getTemplate(new TemplateSearchCriteria("ET220001", "creatable", null));

		assertNull(result);
	}

	@Test
	@SuppressWarnings("unchecked")
	public void should_Persist_Correct_Template_Data_When_Activating() {
		final String TEMPLATE_INSTANCE_VERSION = "1.10";

		mockDomainInstanceService(TEMPLATE_INSTANCE_VERSION);
		Instance instanceForActivation = withExistingTemplateInstance(
				constructTemplate("loadedTemplateId", "loadedTemplateTitle", "some content", "correspondingInstanceId",
						"sampleForType", true, "primary == true && (department == \"DEV\" || department == \"BA\")"),
				null, null);

		withExistingPrimariesInRelationalDB(Collections.emptyList());

		instanceForActivation.add(DefaultProperties.VERSION, TEMPLATE_INSTANCE_VERSION);

		mockInstanceContentService();

		// template already exists in RDB and its DB (system) ID is 7777
		TemplateEntity entity = new TemplateEntity();
		entity.setId(Long.valueOf(7777));
		List<TemplateEntity> entities = Collections.singletonList(entity);
		when(dbDao.fetchWithNamed(eq(TemplateEntity.QUERY_TEMPLATES_BY_TEMPLATE_IDS_KEY), anyList()))
		.thenReturn(entities);

		templateService.activate("loadedTemplateId");

		// content for the loaded instance must be extracted
		verify(instanceContentService).getContent(eq(instanceForActivation), eq(Content.PRIMARY_VIEW));

		// verify that all the correct template properties are persisted

		ArgumentCaptor<Entity> captor = ArgumentCaptor.forClass(Entity.class);
		verify(dbDao, times(2)).saveOrUpdate(captor.capture());
		TemplateEntity actual = (TemplateEntity) captor.getAllValues().get(0);
		// the template identifier should be automatically constructed using the title
		assertEquals("loadedtemplatetitle", actual.getTemplateId());
		assertEquals("loadedTemplateTitle", actual.getTitle());
		// The original DB ID of the existing template should be preserved and passed again when updating it
		assertEquals("sampleForType", actual.getGroupId());
		assertEquals("creatable", actual.getPurpose());
		assertEquals(Boolean.TRUE, actual.getPrimary());
		assertEquals("primary == true && (department == \"DEV\" || department == \"BA\")", actual.getRule());
		assertEquals(TEMPLATE_INSTANCE_VERSION, actual.getPublishedInstanceVersion());

		TemplateContentEntity contentEntity = (TemplateContentEntity) captor.getAllValues().get(1);
		assertEquals("Sample content of the source instance", contentEntity.getContent());
		assertEquals("loadedtemplatetitle.xml", contentEntity.getFileName());
	}

	@Test(expected = MissingTemplateException.class)
	public void should_Throw_Error_If_Instance_For_Activating_Does_Not_Exist() {
		withExistingTemplateInstance(null, null, null);
		templateService.activate("instanceId");
	}

	@Test(expected = InvalidTemplateOperationException.class)
	public void should_Throw_Error_If_Instance_For_Activating_Is_Not_Template() {
		Template template = constructTemplate("templateForActivation", "sampleTitle", "some content",
				"correspondingInstanceId", "sampleForType", false, "");
		InstanceType instanceType = InstanceTypeFake.buildForCategory("someRandomType");
		withExistingTemplateInstance(template, null, instanceType);

		templateService.activate("templateForActivation");
	}

	@Test
	public void should_Automatically_Set_Primary_If_No_Existing_Template_With_That_Rule() {
		withExistingTemplateInstance(
				constructTemplate("templateForActivation", "sampleTitle", "some content", "correspondingInstanceId",
						"sampleForType", false, "primary == true && (department == \"DEV\" || department == \"BA\")"),
				null, null);

		withExistingTemplateInstance(constructTemplate("existingPrimaryInstanceId", "sampleTitle", "testContent",
				"emf:Instance", "sampleForType", true, "primary == true"), null, null);
		withExistingPrimaryInRelationalDB(constructTemplate("existingPrimaryinRDB", "existingPrimaryinRDB",
				"testContent", "emf:Instance", "sampleForType", true, "primary == true"));

		mockDomainInstanceService();
		mockInstanceContentService();

		templateService.activate("templateForActivation");

		expectActivatedToBePrimary(true, "templateForActivation", "sampletitle");
	}

	@Test
	public void should_Automatically_Set_Primary_If_Existing_Has_No_Rule_But_New_One_Has() {
		withExistingTemplateInstance(
				constructTemplate("templateForActivation", "sampleTitle", "some content", "correspondingInstanceId",
						"sampleForType", false, "primary == true && (department == \"DEV\" || department == \"BA\")"),
				null, null);

		withExistingTemplateInstance(constructTemplate("existingPrimaryInstanceId", "sampleTitle", "testContent",
				"emf:Instance", "sampleForType", true, null), null, null);
		withExistingTemplateInRelationalDB(constructTemplate("existingPrimaryisnRDB", "existingPrimaryinRDB",
				"testContent", "emf:Instance", "sampleForType", true, null));

		mockDomainInstanceService();
		mockInstanceContentService();

		templateService.activate("templateForActivation");

		expectActivatedToBePrimary(true, "templateForActivation", "sampletitle");
	}

	@Test
	public void should_Not_Automatically_Set_Primary_If_Existing_Has_Same_Rule() {
		withExistingTemplateInstance(
				constructTemplate("templateForActivation", "sampleTitle", "some content", "correspondingInstanceId",
						"sampleForType", false, "primary == true && (department == \"DEV\" || department == \"BA\")"),
				null, null);

		// rule is the same, only the order is different
		withExistingTemplateInstance(
				constructTemplate("existingPrimaryInstanceId", "sampleTitle", "testContent", "emf:Instance",
						"sampleForType", true, "(department == \"DEV\" || department == \"BA\") && primary == true"),
				null, null);
		withExistingPrimaryInRelationalDB(
				constructTemplate("existingPrimaryinRDB", "existingPrimaryinRDB", "testContent", "emf:Instance",
						"sampleForType", true, "(department == \"DEV\" || department == \"BA\") && primary == true"));

		mockDomainInstanceService();
		mockInstanceContentService();

		templateService.activate("templateForActivation");

		expectActivatedToBePrimary(false, "templateForActivation", "sampletitle");
	}

	@Test
	public void should_Not_Automatically_Set_Primary_If_Both_New_And_Existing_Have_No_Rule() {
		withExistingTemplateInstance(constructTemplate("templateForActivation", "sampleTitle", "some content",
				"correspondingInstanceId", "sampleForType", false, null), null, null);

		withExistingTemplateInstance(constructTemplate("existingPrimaryInstanceId", "sampleTitle", "testContent",
				"emf:Instance", "sampleForType", true, null), null, null);
		withExistingTemplateInRelationalDB(constructTemplate("existingPrimaryinRDB", "existingPrimaryinRDB",
				"testContent", "emf:Instance", "sampleForType", true, null));

		mockDomainInstanceService();
		mockInstanceContentService();

		templateService.activate("templateForActivation");

		expectActivatedToBePrimary(false, "templateForActivation", "sampletitle");
	}

	@Test
	public void should_Demote_Existing_Primary_When_Both_Have_No_Rule_But_Same_Purpose() {
		withExistingTemplateInstance(constructTemplate("loadedTemplateId", "sampleTitle", "some content",
				"correspondingInstanceId", "sampleForType", true, null), null, null);

		withExistingTemplateInstance(constructTemplate("existingPrimaryInstanceId", "sampleTitle", "testContent",
				"emf:Instance", "sampleForType", true, null), null, null);
		withExistingPrimaryInRelationalDB(constructTemplate("existingPrimaryinRDB", "existingPrimaryinRDB",
				"testContent", "existingPrimaryInstanceId", "sampleForType", true, null));

		mockDomainInstanceService();
		mockInstanceContentService();

		templateService.activate("loadedTemplateId");

		verifyTemplateDemoted("existingPrimaryInstanceId", "existingPrimaryinRDB", ACTIVATE_TEMPLATE);
	}

	@Test
	public void should_Demote_Existing_Primary_Having_The_Same_Rule() {
		withExistingTemplateInstance(
				constructTemplate("loadedTemplateId", "sampleTitle", "some content", "correspondingInstanceId",
						"sampleForType", true, "primary == true && (department == \"DEV\" || department == \"BA\")"),
				null, null);

		withExistingTemplateInstance(
				constructTemplate("existingPrimaryInstanceId", "sampleTitle", "testContent", "emf:Instance",
						"sampleForType", true, "primary == true && (department == \"DEV\" || department == \"BA\")"),
				null, null);
		withExistingPrimaryInRelationalDB(constructTemplate("existingPrimaryinRDB", "existingPrimaryinRDB",
				"testContent", "existingPrimaryInstanceId", "sampleForType", true,
				"primary == true && (department == \"DEV\" || department == \"BA\")"));

		mockDomainInstanceService();
		mockInstanceContentService();

		templateService.activate("loadedTemplateId");

		verifyTemplateDemoted("existingPrimaryInstanceId", "existingPrimaryinRDB", ACTIVATE_TEMPLATE);
	}

	@Test
	public void should_Demote_Existing_Primary_Having_The_Same_Rule_But_In_Different_Order() {
		withExistingTemplateInstance(
				constructTemplate("loadedTemplateId", "sampleTitle", "some content", "correspondingInstanceId",
						"sampleForType", true,
						"primary == true && (department == \"DEV\" || department == \"HR\" || department == \"BA\")"),
				null, null);

		withExistingTemplateInstance(
				constructTemplate("existingPrimaryInstanceId", "sampleTitle", "testContent", "emf:Instance",
						"sampleForType", true,
						"(department == \"BA\" || department == \"DEV\" || department == \"HR\") && primary == true"),
				null, null);
		withExistingPrimaryInRelationalDB(constructTemplate("existingPrimaryinRDB", "existingPrimaryinRDB",
				"testContent", "existingPrimaryInstanceId", "sampleForType", true,
				"(department == \"BA\" || department == \"DEV\" || department == \"HR\") && primary == true"));

		mockDomainInstanceService();
		mockInstanceContentService();

		templateService.activate("loadedTemplateId");

		verifyTemplateDemoted("existingPrimaryInstanceId", "existingPrimaryinRDB", ACTIVATE_TEMPLATE);
	}

	@Test
	public void should_Not_Demote_Existing_Primary_If_It_Has_Different_Rule() {
		withExistingTemplateInstance(
				constructTemplate("loadedTemplateId", "sampleTitle", "some content", "correspondingInstanceId",
						"sampleForType", true, "primary == true && (department == \"DEV\" || department == \"BA\")"),
				null, null);

		withExistingPrimaryInRelationalDB(
				constructTemplate("existingPrimaryInstanceId", "sampleTitle", "testContent", "emf:Instance",
						"sampleForType", true, "primary == true && (department == \"QA\" || department == \"BA\")"));

		mockDomainInstanceService();
		mockInstanceContentService();

		templateService.activate("loadedTemplateId");

		verifyNoTemplateWasDemotedOnActivate();
	}

	@Test
	public void should_Not_Automatically_Set_Primary_If_controlPrimaryFlag_Is_False() {
		withExistingTemplateInstance(
				constructTemplate("templateForActivation", "sampleTitle", "some content", "correspondingInstanceId",
						"sampleForType", false, "primary == true && (department == \"DEV\" || department == \"BA\")"),
				null, null);

		withExistingTemplateInstance(constructTemplate("existingPrimaryInstanceId", "sampleTitle", "testContent",
				"emf:Instance", "sampleForType", true, "primary == true"), null, null);
		withExistingTemplateInRelationalDB(constructTemplate("existingPrimaryinRDB", "existingPrimaryinRDB",
				"testContent", "emf:Instance", "sampleForType", true, "primary == true"));

		mockDomainInstanceService();
		mockInstanceContentService();

		templateService.activate("templateForActivation", false);

		expectActivatedToBePrimary(false, "templateForActivation", "sampletitle");
		verifyNoTemplateWasDemotedOnActivate();
	}

	@Test
	public void should_Not_Demote_Existing_Primary_If_controlPrimaryFlag_Is_False() {
		withExistingTemplateInstance(
				constructTemplate("loadedTemplateId", "sampleTitle", "some content", "correspondingInstanceId",
						"sampleForType", true, "primary == true && (department == \"DEV\" || department == \"BA\")"),
				null, null);

		withExistingTemplateInstance(
				constructTemplate("existingPrimaryInstanceId", "sampleTitle", "testContent", "emf:Instance",
						"sampleForType", true, "primary == true && (department == \"DEV\" || department == \"BA\")"),
				null, null);
		withExistingTemplateInRelationalDB(
				constructTemplate("existingPrimaryinRDB", "existingPrimaryinRDB", "testContent", "emf:Instance",
						"sampleForType", true, "primary == true && (department == \"DEV\" || department == \"BA\")"));

		mockDomainInstanceService();
		mockInstanceContentService();

		templateService.activate("loadedTemplateId", false);

		verifyNoTemplateWasDemotedOnActivate();
	}

	@Test
	public void should_Not_Demote_Existing_Primary_If_It_Has_A_Rule_But_New_One_Doesnt() {
		withExistingTemplateInstance(constructTemplate("loadedTemplateId", "sampleTitle", "some content",
				"correspondingInstanceId", "sampleForType", true, null), null, null);

		withExistingPrimaryInRelationalDB(
				constructTemplate("existingPrimaryInstanceId", "sampleTitle", "testContent", "emf:Instance",
						"sampleForType", true, "primary == true && (department == \"QA\" || department == \"BA\")"));

		mockDomainInstanceService();
		mockInstanceContentService();

		templateService.activate("loadedTemplateId");

		verifyNoTemplateWasDemotedOnActivate();
	}

	@Test
	public void should_Not_Demote_Existing_Primary_If_It_Has_No_Rule_But_New_One_Has() {
		withExistingTemplateInstance(
				constructTemplate("loadedTemplateId", "sampleTitle", "some content", "correspondingInstanceId",
						"sampleForType", true, "primary == true && (department == \"DEV\" || department == \"BA\")"),
				null, null);

		withExistingPrimaryInRelationalDB(constructTemplate("existingPrimaryInstanceId", "sampleTitle", "testContent",
				"emf:Instance", "sampleForType", true, null));

		mockDomainInstanceService();
		mockInstanceContentService();

		templateService.activate("loadedTemplateId");

		verifyNoTemplateWasDemotedOnActivate();
	}

	@Test(expected = EmfApplicationException.class)
	public void should_Throw_Exception_When_Template_Title_Is_Missing() {
		mockDomainInstanceService();
		withExistingTemplateInstance(
				constructTemplate("loadedTemplateId", null, "some content", "correspondingInstanceId", "sampleForType",
						true, "primary == true && (department == \"DEV\" || department == \"BA\")"),
				null, null);
		mockInstanceContentService();

		templateService.activate("loadedTemplateId");
	}

	@Test
	public void should_Save_Activated_Template_With_Correct_Operation() {
		withExistingTemplateInstance(
				constructTemplate("loadedTemplateId", "sampleTitle", "some content", "correspondingInstanceId",
						"sampleForType", false, "primary == true && (department == \"DEV\" || department == \"BA\")"),
				null, null);

		mockInstanceContentService();
		mockDomainInstanceService();

		templateService.activate("loadedTemplateId");

		ArgumentCaptor<InstanceSaveContext> captor = ArgumentCaptor.forClass(InstanceSaveContext.class);
		verify(domainInstanceService, times(1)).save(captor.capture());

		assertEquals("loadedTemplateId", captor.getValue().getInstance().getId());
		assertEquals(new Operation(ACTIVATE_TEMPLATE), captor.getValue().getOperation());
	}

	@Test
	public void should_NotSaveTemplateInstance_WhenAlreadyInActiveState() {
		withExistingTemplateInstance(constructTemplate("loadedTemplateId", "sampleTitle", "some content", "correspondingInstanceId",
				"sampleForType", false, "primary == true && (department == \"DEV\" || department == \"BA\")"), "ACTIVE", null);

		mockInstanceContentService();
		mockDomainInstanceService();

		templateService.activate("loadedTemplateId");

		verify(domainInstanceService, never()).save(any());
	}

	@Test
	public void should_Set_Correpsonding_Library_As_Template_Parent() {
		Template inputTemplateData = mockInputTemplateInstance(Boolean.TRUE, "sampleForType");
		mockDomainInstanceService();
		withExistingTemplateInstance(null, null, null);
		definitionWithSemanticTypeExists(true, true, "emf:sampleSemanticClass", "sampleForType");

		templateService.create(inputTemplateData, "test");

		ArgumentCaptor<Instance> captor = ArgumentCaptor.forClass(Instance.class);
		verify(domainInstanceService).createInstance(any(), captor.capture());
		assertEquals("emf:sampleSemanticClass", captor.getValue().getId());
	}

	@Test
	public void should_Set_URI_As_Parent_If_Valid_URI_Is_Provided() {
		Template inputTemplateData = mockInputTemplateInstance(Boolean.TRUE,
				"http://ittruse.ittbg.com/ontology/enterpriseManagementFramework#BusinessProcess");
		mockDomainInstanceService();
		withExistingTemplateInstance(null, null, null);
		definitionWithSemanticTypeExists(false, false, "", null);

		templateService.create(inputTemplateData, "test");

		ArgumentCaptor<Instance> captor = ArgumentCaptor.forClass(Instance.class);
		verify(domainInstanceService).createInstance(any(), captor.capture());
		assertEquals("http://ittruse.ittbg.com/ontology/enterpriseManagementFramework#BusinessProcess", captor.getValue().getId());
	}

	@Test(expected = IllegalArgumentException.class)
	public void should_Throw_Exception_When_Neither_Valid_DefinitionID_Nor_URI_Passed() {
		Template inputTemplateData = mockInputTemplateInstance(Boolean.TRUE, "invalidUri");
		mockDomainInstanceService();
		withExistingTemplateInstance(null, null, null);
		definitionWithSemanticTypeExists(false, false, "", null);
		templateService.create(inputTemplateData, "test");
	}

	@Test(expected = IllegalArgumentException.class)
	public void should_Throw_Exception_When_Definition_Is_Valid_But_RdfType_Missing() {
		Template inputTemplateData = mockInputTemplateInstance(Boolean.TRUE, "sampleForType");
		mockDomainInstanceService();
		withExistingTemplateInstance(null, null, null);
		definitionWithSemanticTypeExists(true, false, "", null);
		templateService.create(inputTemplateData, "test");
	}

	@Test
	public void should_Correctly_Set_Active_Template_As_Primary() {
		Template inputTemplate = constructTemplate("inputTemplate", "sampleTitle", "some content",
				"correspondingInstanceId", "sampleForType", false, null);
		withExistingTemplateInstance(inputTemplate, "ACTIVE", null);
		withExistingTemplateInRelationalDB(constructTemplate("inputTemplateRDB", "existinginRDB", "testContent",
				"emf:Instance", "sampleForType", false, null));

		when(instanceTypeResolver.resolveReference(eq("emf:Instance"))).thenReturn(Optional.empty());

		templateService.setAsPrimaryTemplate("inputTemplate");

		verifyTemplateSetAsPrimary("inputTemplate", "inputTemplateRDB", true);
	}

	@Test
	public void should_Set_Non_Active_Template_As_Primary_Only_In_The_Instance() {
		Template inputTemplate = constructTemplate("inputTemplate", "sampleTitle", "some content",
				"correspondingInstanceId", "sampleForType", false, null);
		withExistingTemplateInstance(inputTemplate, "DRAFT", null);

		withExistingTemplateInRelationalDB(constructTemplate("inputTemplateRDB", "existinginRDB", "testContent",
				"emf:Instance", "sampleForType", false, null));

		templateService.setAsPrimaryTemplate("inputTemplate");

		verifyTemplateSetAsPrimary("inputTemplate", "inputTemplateRDB", false);
	}

	@Test
	public void should_Not_Demote_Existing_Primary_If_The_New_Is_Not_Active() {
		Template inputTemplate = constructTemplate("inputTemplate", "sampleTitle", "some content",
				"correspondingInstanceId", "sampleForType", false, null);
		withExistingTemplateInRelationalDB(constructTemplate("inputTemplateRDB", "existinginRDB", "testContent",
				"emf:Instance", "sampleForType", false, null));
		withExistingTemplateInstance(inputTemplate, "DRAFT", null);

		withExistingTemplateInstance(constructTemplate("existingPrimaryInstanceId", "sampleTitle", "testContent",
				"emf:Instance", "sampleForType", true, null), null, null);

		templateService.setAsPrimaryTemplate("inputTemplate");

		verifyNoTemplateWasDemotedOnSetAsPrimary(false);
	}

	@Test
	public void should_Not_Set_As_Primary_If_Template_Is_Already_Primary() {
		withExistingTemplateInstance(constructTemplate("inputTemplate", "sampleTitle", "some content",
				"correspondingInstanceId", "sampleForType", true, null), null, null);

		templateService.setAsPrimaryTemplate("inputTemplate");

		verify(domainInstanceService, never()).save(any());
		verify(instanceDao, never()).persistChanges(any());
	}

	@Test
	public void should_Demote_Existing_Primary_With_Same_Rule_When_Setting_As_Primary() {
		Template inputTemplate = constructTemplate("loadedTemplateId", "sampleTitle", "some content",
				"correspondingInstanceId", "sampleForType", false,
				"primary == true && (department == \"DEV\" || department == \"BA\")");
		withExistingTemplateInstance(inputTemplate, "ACTIVE", null);
		withExistingTemplateInRelationalDB(inputTemplate);

		withExistingTemplateInstance(
				constructTemplate("existingPrimaryInstanceId", "sampleTitle", "testContent", "emf:Instance",
						"sampleForType", true, "primary == true && (department == \"DEV\" || department == \"BA\")"),
				null, null);

		Template existingPrimaryInRDB = constructTemplate("existingPrimaryinRDB", "sampleTitle", "testContent",
				"existingPrimaryInstanceId", "sampleForType", true,
				"primary == true && (department == \"DEV\" || department == \"BA\")");

		withExistingPrimaryInRelationalDB(existingPrimaryInRDB);

		templateService.setAsPrimaryTemplate("loadedTemplateId");

		verifyTemplateDemoted("existingPrimaryInstanceId", "existingPrimaryinRDB", SET_TEMPLATE_AS_PRIMARY);
	}

	@Test
	public void should_Demote_Existing_Primary_With_No_Rule_Same_Purpose_When_Setting_As_Primary() {
		Template inputTemplate = constructTemplate("loadedTemplateId", "sampleTitle", "some content",
				"correspondingInstanceId", "sampleForType", false, null);
		withExistingTemplateInstance(inputTemplate, "ACTIVE", null);
		withExistingTemplateInRelationalDB(inputTemplate);

		Template existingPrimaryInRDB = constructTemplate("existingPrimaryinRDB", "sampleTitle", "testContent",
				"existingPrimaryInstanceId", "sampleForType", true, null);
		withExistingTemplateInstance(constructTemplate("existingPrimaryInstanceId", "sampleTitle", "testContent",
				"emf:Instance", "sampleForType", true, null), null, null);
		withExistingPrimaryInRelationalDB(existingPrimaryInRDB);

		templateService.setAsPrimaryTemplate("loadedTemplateId");

		verifyTemplateDemoted("existingPrimaryInstanceId", "existingPrimaryinRDB", SET_TEMPLATE_AS_PRIMARY);
	}

	@Test
	public void should_Not_Demote_Existing_Primary_If_Rule_Is_Different_When_Setting_As_Primary() {
		Template inputTemplate = constructTemplate("loadedTemplateId", "sampleTitle", "some content",
				"correspondingInstanceId", "sampleForType", false, "primary == true && department == \"DEV\"");
		withExistingTemplateInstance(inputTemplate, "ACTIVE", null);

		Template existingPrimary = constructTemplate("existingPrimaryInstanceId", "sampleTitle", "testContent",
				"emf:Instance", "sampleForType", true,
				"primary == true && (department == \"DEV\" || department == \"BA\")");
		withExistingTemplateInstance(existingPrimary, null, null);

		withExistingTemplatesInRelationalDB(inputTemplate, existingPrimary);

		templateService.setAsPrimaryTemplate("loadedTemplateId");

		verifyNoTemplateWasDemotedOnSetAsPrimary(true);
	}

	@Test
	public void should_Not_Demote_Existing_Primary_If_It_Has_No_Rule_When_Setting_As_Primary() {
		Template inputTemplate = constructTemplate("loadedTemplateId", "sampleTitle", "some content",
				"correspondingInstanceId", "sampleForType", false, "primary == true && department == \"DEV\"");
		withExistingTemplateInstance(inputTemplate, "ACTIVE", null);

		Template existingPrimary = constructTemplate("existingPrimaryInstanceId", "sampleTitle", "testContent",
				"emf:Instance", "sampleForType", true, null);
		withExistingTemplateInstance(existingPrimary, null, null);

		withExistingTemplatesInRelationalDB(inputTemplate, existingPrimary);

		templateService.setAsPrimaryTemplate("loadedTemplateId");

		verifyNoTemplateWasDemotedOnSetAsPrimary(true);
	}

	@Test
	public void should_Not_Demote_Existing_Primary_If_Input_Template_Has_No_Rule_When_Setting_As_Primary() {
		Template inputTemplate = constructTemplate("loadedTemplateId", "sampleTitle", "some content",
				"correspondingInstanceId", "sampleForType", false, null);
		withExistingTemplateInstance(inputTemplate, "ACTIVE", null);

		Template existingPrimary = constructTemplate("existingPrimaryInstanceId", "sampleTitle", "testContent",
				"emf:Instance", "sampleForType", true, "primary == true && department == \"DEV\"");
		withExistingTemplateInstance(existingPrimary, null, null);

		withExistingTemplatesInRelationalDB(inputTemplate, existingPrimary);

		templateService.setAsPrimaryTemplate("loadedTemplateId");

		verifyNoTemplateWasDemotedOnSetAsPrimary(true);
	}

	@Test
	public void should_Not_Demote_When_There_Is_No_Existing_Primary_When_Setting_As_Primary() {
		Template inputTemplate = constructTemplate("loadedTemplateId", "sampleTitle", "some content",
				"correspondingInstanceId", "sampleForType", false, null);
		withExistingTemplateInstance(inputTemplate, "ACTIVE", null);

		withExistingTemplateInRelationalDB(inputTemplate);

		withExistingPrimaryInRelationalDB(null);

		templateService.setAsPrimaryTemplate("loadedTemplateId");

		verifyNoTemplateWasDemotedOnSetAsPrimary(true);
	}

	@Test(expected = MissingTemplateException.class)
	public void should_Throw_Exception_If_Active_But_Missing_Record_In_RDB_When_Setting_As_Primary() {
		Template inputTemplate = constructTemplate("loadedTemplateId", "sampleTitle", "some content",
				"correspondingInstanceId", "sampleForType", false, null);
		withExistingTemplateInstance(inputTemplate, "ACTIVE", null);

		withExistingTemplateInRelationalDB(null);

		templateService.setAsPrimaryTemplate("loadedTemplateId");
	}

	@Test(expected = MissingTemplateException.class)
	public void should_Throw_Exception_When_Instance_For_Setting_As_Primary_Does_Not_Exist() {
		withExistingTemplateInstance(null, null, null);
		templateService.setAsPrimaryTemplate("instanceId");
	}

	@Test(expected = InvalidTemplateOperationException.class)
	public void should_Throw_Error_If_Instance_For_Setting_As_Primary_Is_Not_Template() {
		Template template = constructTemplate("inputTemplate", "sampleTitle", "some content", "correspondingInstanceId",
				"sampleForType", false, "");
		InstanceType instanceType = InstanceTypeFake.buildForCategory("someRandomType");
		withExistingTemplateInstance(template, "", instanceType);

		templateService.setAsPrimaryTemplate("inputTemplate");
	}

	@Test
	public void should_Dactivate_Active_Template() {
		withExistingTemplateInstance(constructTemplate("inputTemplate", "sampleTitle", "some content",
				"correspondingInstanceId", "sampleForType", false, ""), "", null);

		Template templateRDB = constructTemplate("templateRecordRDB", "existingPrimaryinRDB", "testContent",
				"emf:Instance", "sampleForType", true, "primary == true");
		withExistingTemplateInRelationalDB(templateRDB);

		templateService.deactivate("inputTemplate");

		verifyTemplateDeactivated("inputTemplate", true);
	}

	@Test
	public void should_Dactivate_Non_Active_Template() {
		withExistingTemplateInstance(constructTemplate("inputTemplate", "sampleTitle", "some content",
				"correspondingInstanceId", "sampleForType", false, ""), "", null);
		// no record in RDB
		withExistingTemplateInRelationalDB(null);

		templateService.deactivate("inputTemplate");

		verifyTemplateDeactivated("inputTemplate", false);
	}

	@Test(expected = MissingTemplateException.class)
	public void should_Throw_Exception_When_Instance_For_Deactivation_Does_Not_Exist() {
		withExistingTemplateInstance(null, null, null);
		templateService.deactivate("inputTemplate");
	}

	@Test(expected = InvalidTemplateOperationException.class)
	public void should_Throw_Error_If_Instance_For_Deactivation_Is_Not_Template() {
		Template template = constructTemplate("inputTemplate", "sampleTitle", "some content", "correspondingInstanceId",
				"sampleForType", false, "");
		InstanceType instanceType = InstanceTypeFake.buildForCategory("someRandomType");
		withExistingTemplateInstance(template, "", instanceType);

		templateService.deactivate("inputTemplate");
	}

	@Test(expected = InvalidTemplateOperationException.class)
	public void should_Throw_Exception_When_Trying_To_Deactivate_Primary_Template() {
		withExistingTemplateInstance(constructTemplate("inputTemplate", "sampleTitle", "some content",
				"correspondingInstanceId", "sampleForType", true, ""), "ACTIVE", null);
		withExistingTemplateInRelationalDB(constructTemplate("templateRecordRDB", "existingPrimaryinRDB", "testContent",
				"emf:Instance", "sampleForType", true, "primary == true"));

		templateService.deactivate("inputTemplate");
	}

	@Test
	public void should_GetTemplateByTemplateId() {
		Template template1 = constructTemplate("id1", "sampleTitle", "test", "emf:Instance1", "ET220001", true, null);
		withExistingTemplateInRelationalDB(template1);

		Template result = templateService.getTemplate(template1.getId());

		assertEquals("id1", result.getId());
	}

	@Test
	public void should_GetTemplateByCorrespondingTemplateId() {
		Template template1 = constructTemplate("id1", "sampleTitle", "test", "emf:Instance1", "ET220001",
				true, null);
		withExistingTemplateInRelationalDB(template1);

		Template result = templateService.getTemplate(template1.getCorrespondingInstance());

		assertEquals("id1", result.getId());
	}


	private void definitionWithSemanticTypeExists(boolean exists, boolean hasRdfType, String libraryId, String definitionType) {
		if (exists) {
			DefinitionModel definitionModel = mock(DefinitionModel.class);
			Optional<PropertyDefinition> definitionFieldOptional = Optional.empty();
			if (hasRdfType) {
				PropertyDefinition property = mock(PropertyDefinition.class);
				when(property.getDefaultValue()).thenReturn(libraryId);
				definitionFieldOptional = Optional.of(property);
			}
			when(definitionModel.getField(eq(DefaultProperties.SEMANTIC_TYPE))).thenReturn(definitionFieldOptional);
			when(definitionService.find(eq(definitionType))).thenReturn(definitionModel);
		} else {
			when(definitionService.find(eq(definitionType))).thenReturn(null);
		}
	}

	private Instance withExistingTemplateInstance(Template template, String status, InstanceType type) {
		if (template == null) {
			when(instanceTypeResolver.resolveReference(anyString())).thenReturn(Optional.empty());
			return null;
		}
		EmfInstance loadedInstance = new EmfInstance();
		loadedInstance.setId(template.getId());
		loadedInstance.add(TITLE, template.getTitle());
		loadedInstance.add(FOR_OBJECT_TYPE, template.getForType());
		loadedInstance.add(TEMPLATE_PURPOSE, template.getPurpose());
		loadedInstance.add(IS_PRIMARY_TEMPLATE, template.getPrimary());
		loadedInstance.add(TEMPLATE_RULE, template.getRule());

		if (StringUtils.isNotBlank(status)) {
			loadedInstance.add(STATUS, status);
		}
		if (type != null) {
			loadedInstance.setType(type);
		} else {
			InstanceType instanceType = InstanceTypeFake.buildForCategory(TEMPLATE);
			loadedInstance.setType(instanceType);
		}

		InstanceReference reference = new InstanceReferenceMock(loadedInstance);
		Optional<InstanceReference> loadedInstanceOptional = Optional.of(reference);
		when(instanceTypeResolver.resolveReference(eq(template.getId()))).thenReturn(loadedInstanceOptional);
		return loadedInstance;
	}

	private EmfInstance withExistingCorrespondingInstance(Template template, Boolean isDeleted, String status) {
		EmfInstance loadedInstance = new EmfInstance();

		loadedInstance.setId(template.getId());
		loadedInstance.setIdentifier(template.getId());
		loadedInstance.add(TITLE, template.getTitle());
		loadedInstance.add(TEMPLATE_RULE, template.getRule());
		loadedInstance.add(FOR_OBJECT_TYPE, template.getForType());
		loadedInstance.add(TEMPLATE_PURPOSE, template.getPurpose());
		loadedInstance.add(IS_PRIMARY_TEMPLATE, template.getPrimary());
		if (StringUtils.isNotBlank(status)) {
			loadedInstance.add(STATUS, status);
		} else {
			loadedInstance.add(STATUS, "");
		}
		if (isDeleted != null) {
			loadedInstance.add(DefaultProperties.IS_DELETED, isDeleted);
		}

		Optional<Instance> loadedInstanceOptional = Optional.of(loadedInstance);
		when(instanceService.loadDeleted(anyString())).thenReturn(loadedInstanceOptional);

		return loadedInstance;
	}

	/**
	 * Verifies that the template with the provided instance ID (URI) and relational DB identifier is demoted (set as
	 * primary=false) in the 3 data stores: </br>
	 * 1. semantics </br>
	 * 2. relational DB </br>
	 * 3. content store (DMS) </br>
	 *
	 * @param templateInstanceId
	 *            the instance ID of the template that has to be demoted
	 * @param relationalDbIdentifier
	 *            the relational DB identifier (templateIdenitifier) of the template that has to be demoted
	 * @param action
	 *            the action that triggered the demote
	 */
	private void verifyTemplateDemoted(String templateInstanceId, String relationalDbIdentifier, String action) {
		// by default, get and assert the first invocation of all
		int semanticDemoteInvocationOrder = 0;
		int relationalDemoteInvocationOrder = 0;
		int timesPersistInRDB = 1;
		// depending on the action, the call order of the mocked dependencies is different
		if (SET_TEMPLATE_AS_PRIMARY.equals(action)) {
			semanticDemoteInvocationOrder = 0;
			relationalDemoteInvocationOrder = 0;
			timesPersistInRDB = 2;
		} else if (ACTIVATE_TEMPLATE.equals(action)) {
			// demoted template persisted in the semantics with the second call
			semanticDemoteInvocationOrder = 1;
			// demoted template persisted in RDB with the first call
			relationalDemoteInvocationOrder = 0;
			// demoted template persisted in DMS with the first call
			timesPersistInRDB = 3;
		}
		ArgumentCaptor<InstanceSaveContext> demotedInstanceCaptor = ArgumentCaptor.forClass(InstanceSaveContext.class);
		verify(domainInstanceService, times(2)).save(demotedInstanceCaptor.capture());
		List<InstanceSaveContext> capturedSaveContexts = demotedInstanceCaptor.getAllValues();
		InstanceSaveContext resetExistingPrimarySaveContext = capturedSaveContexts.get(semanticDemoteInvocationOrder);
		assertEquals("The demoted template instance is not with the expected ID", templateInstanceId,
				resetExistingPrimarySaveContext.getInstance().getId());
		assertEquals("The demoted template instance is not saved with the correct operation", Operation.NO_OPERATION,
				resetExistingPrimarySaveContext.getOperation());
		assertEquals("The demoted template instance is not saved with primary=false", Boolean.FALSE,
				resetExistingPrimarySaveContext.getInstance().get(TemplateProperties.IS_PRIMARY_TEMPLATE));

		ArgumentCaptor<TemplateEntity> relationalInstanceCaptor = ArgumentCaptor.forClass(TemplateEntity.class);
		verify(dbDao, times(timesPersistInRDB)).saveOrUpdate(relationalInstanceCaptor.capture());
		List<TemplateEntity> relationalInstanceContexts = relationalInstanceCaptor.getAllValues();
		TemplateEntity templateSavedRelationalDB = relationalInstanceContexts.get(relationalDemoteInvocationOrder);
		assertEquals("The demoted relational DB template record is not with the expected identifier",
				relationalDbIdentifier, templateSavedRelationalDB.getTemplateId());
		assertFalse("The demoted relational DB template record is not saved with primary=false",
				templateSavedRelationalDB.getPrimary());
	}

	private void verifyNoTemplateWasDemotedOnActivate() {
		// should be invoked only once, to save the activated template instance (but not to demote)
		verify(domainInstanceService, times(1)).save(any());
		// should be invoked two times - one to save the Template and one for the TemplateContentEntity
		verify(dbDao, times(2)).saveOrUpdate(any());
		// should be invoked only once, to upload to DMS the newly activated template (but not to demote)
	}

	private void verifyNoTemplateWasDemotedOnSetAsPrimary(boolean newTemplateIsActive) {
		verify(domainInstanceService, times(1)).save(any());
		if (newTemplateIsActive) {
			verify(dbDao, times(1)).saveOrUpdate(any());
		} else {
			verify(instanceDao, never()).persistChanges(any());
		}
	}

	private void verifyTemplateSetAsPrimary(String templateInstanceId, String relationalDbIdentifier, boolean active) {
		ArgumentCaptor<InstanceSaveContext> instanceCaptor = ArgumentCaptor.forClass(InstanceSaveContext.class);
		verify(domainInstanceService, times(1)).save(instanceCaptor.capture());
		List<InstanceSaveContext> capturedSaveContexts = instanceCaptor.getAllValues();
		InstanceSaveContext setAsPrimarySaveContext = capturedSaveContexts.get(0);
		assertEquals(templateInstanceId, setAsPrimarySaveContext.getInstance().getId());
		assertEquals(Boolean.TRUE, setAsPrimarySaveContext.getInstance().get(TemplateProperties.IS_PRIMARY_TEMPLATE));

		if (active) {
			ArgumentCaptor<TemplateEntity> relationalInstanceCaptor = ArgumentCaptor.forClass(TemplateEntity.class);
			verify(dbDao).saveOrUpdate(relationalInstanceCaptor.capture());
			TemplateEntity templateSavedRelationalDB = relationalInstanceCaptor.getValue();
			assertEquals(relationalDbIdentifier, templateSavedRelationalDB.getTemplateId());
			assertTrue(templateSavedRelationalDB.getPrimary());
		} else {
			verify(dbDao, never()).saveOrUpdate(any());
		}
	}

	private void verifyTemplateDeactivated(String templateInstanceId, boolean active) {
		if (active) {
			verify(dbDao, times(2)).delete(any(), any());
		}

		ArgumentCaptor<InstanceSaveContext> instanceSaveCaptor = ArgumentCaptor.forClass(InstanceSaveContext.class);
		verify(domainInstanceService).save(instanceSaveCaptor.capture());
		InstanceSaveContext setFirstAsPrimarySaveContext = instanceSaveCaptor.getValue();
		assertEquals(templateInstanceId, setFirstAsPrimarySaveContext.getInstance().getId());
		assertEquals(DEACTIVATE_TEMPLATE, setFirstAsPrimarySaveContext.getOperation().getOperation());
	}

	private void expectActivatedToBePrimary(boolean expectPrimary, String templateInstanceId,
			String relationalDbIdentifier) {
		ArgumentCaptor<InstanceSaveContext> demotedInstanceCaptor = ArgumentCaptor.forClass(InstanceSaveContext.class);
		verify(domainInstanceService).save(demotedInstanceCaptor.capture());
		InstanceSaveContext setFirstAsPrimarySaveContext = demotedInstanceCaptor.getValue();
		assertEquals(templateInstanceId, setFirstAsPrimarySaveContext.getInstance().getId());
		assertEquals(expectPrimary,
				setFirstAsPrimarySaveContext.getInstance().get(TemplateProperties.IS_PRIMARY_TEMPLATE));

		ArgumentCaptor<TemplateEntity> relationalInstanceCaptor = ArgumentCaptor.forClass(TemplateEntity.class);
		verify(dbDao, times(2)).saveOrUpdate(relationalInstanceCaptor.capture());
		assertEquals(relationalDbIdentifier, relationalInstanceCaptor.getAllValues().get(0).getTemplateId());
		assertEquals(expectPrimary, relationalInstanceCaptor.getAllValues().get(0).getPrimary());
	}

	@SuppressWarnings("unchecked")
	private void withExistingTemplateInRelationalDB(Template template) {
		List<TemplateEntity> list = Collections.emptyList();
		if(template != null) {
			list = Collections.singletonList(convertToEntity(template));
		}
		when(dbDao.fetchWithNamed(eq(TemplateEntity.QUERY_TEMPLATES_BY_INSTANCE_OR_TEMPLATE_ID_KEY), anyList()))
				.thenReturn(list);
	}

	/**
	 * The first template will be returned when the RDB is queried for a first time, and the second one will be returned
	 * after the second querying.
	 *
	 * @param first
	 *            returned on first RDB request
	 * @param second
	 *            returned on second RDB request
	 */
	@SuppressWarnings("unchecked")
	private void withExistingTemplatesInRelationalDB(Template first, Template second) {
		List<TemplateEntity> firstList = Collections.singletonList(convertToEntity(first));
		List<TemplateEntity> secondList = Collections.singletonList(convertToEntity(second));
		when(dbDao.fetchWithNamed(eq(TemplateEntity.QUERY_TEMPLATES_BY_INSTANCE_OR_TEMPLATE_ID_KEY), anyList()))
				.thenReturn(firstList, secondList);
	}

	@SuppressWarnings("unchecked")
	private void withExistingTemplatesInRelationalDB(List<Template> templates) {
		List<TemplateEntity> entities = templates
				.stream()
					.map(TemplateServiceImplTest::convertToEntity)
					.collect(Collectors.toList());

		when(dbDao.fetchWithNamed(eq(TemplateEntity.QUERY_TEMPLATES_FOR_GROUP_ID_PURPOSE_KEY), anyList()))
				.thenReturn(entities);
	}

	@SuppressWarnings("unchecked")
	private void withExistingPrimariesInRelationalDB(List<Template> templates) {
		List<TemplateEntity> entities = templates
				.stream()
					.filter(Objects::nonNull)
					.map(TemplateServiceImplTest::convertToEntity)
					.collect(Collectors.toList());

		when(dbDao.fetchWithNamed(eq(TemplateEntity.QUERY_PRIMARY_TEMPLATES_FOR_GROUP_AND_PURPOSE_KEY), anyList()))
				.thenReturn(entities);
	}

	private void withExistingPrimaryInRelationalDB(Template template) {
		withExistingPrimariesInRelationalDB(Arrays.asList(template));
	}

	private Instance mockDomainInstanceService() {
		return mockDomainInstanceService(null);
	}

	private Instance mockDomainInstanceService(String savedVersion) {
		// mock the instance returned after the initial create
		Instance newlyCreatedInstance = new EmfInstance();
		newlyCreatedInstance.setId("newlyCreatedInstanceId");

		when(domainInstanceService.createInstance(any(DefinitionModel.class), any())).then(invokation -> {
			DefinitionModel definition = invokation.getArgumentAt(0, DefinitionModel.class);

			if (definition.getIdentifier().equals(TemplateProperties.TEMPLATE_DEFINITION_ID)) {
				return newlyCreatedInstance;
			}

			return null;
		});

		// mock the newly saved instance with only ID and no properties yet
		Instance savedInstance = new EmfInstance();
		savedInstance.setId("savedInstanceId");
		savedInstance.add(DefaultProperties.VERSION, savedVersion);
		when(domainInstanceService.save(any(InstanceSaveContext.class))).thenReturn(savedInstance);
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

	private static Template mockInputTemplateInstance(Boolean primary, String forType) {
		Instance sourceInstance = Mockito.mock(Instance.class);
		when(sourceInstance.getId()).thenReturn("sourceInstanceId");
		Template inputTemplateData = Mockito.mock(Template.class);

		when(inputTemplateData.getForType()).thenReturn(forType);
		when(inputTemplateData.getTitle()).thenReturn("sampleTitle");
		when(inputTemplateData.getPurpose()).thenReturn(TemplatePurposes.CREATABLE);
		when(inputTemplateData.getPrimary()).thenReturn(primary);
		return inputTemplateData;
	}

	private static Template constructTemplate(Serializable id, String title, String content,
			String correspondingInstance, String forType, boolean primary, String rule) {
		Template defaultTemplate = new Template();
		defaultTemplate.setId((String) id);

		defaultTemplate.setContent(content);
		if (content != null) {
			defaultTemplate.setContentDigest("" + content.hashCode());
		}

		if (StringUtils.isNotBlank(correspondingInstance)) {
			defaultTemplate.setCorrespondingInstance(correspondingInstance);
		}
		defaultTemplate.setForType(forType);
		defaultTemplate.setTitle(title);
		defaultTemplate.setPurpose(TemplatePurposes.CREATABLE);
		defaultTemplate.setPrimary(primary);
		defaultTemplate.setRule(rule);

		return defaultTemplate;
	}

	@SuppressWarnings("unchecked")
	private void withExistingTemplateContent(String templateId, String content) {
		List<TemplateContentEntity> contents = new ArrayList<>();

		TemplateContentEntity contentEntity = new TemplateContentEntity();
		contentEntity.setId(templateId);
		contentEntity.setContent(content);

		contents.add(contentEntity);

		when(dbDao.fetchWithNamed(eq(TemplateContentEntity.QUERY_BY_TEMPLATE_ID_KEY), anyList())).thenReturn(contents);
	}

	private static String readFileAsString(String filePath) {
		URL url = TemplateServiceImplTest.class.getResource(filePath);
		try {
			return FileUtils.readFileToString(new File(url.toURI()), StandardCharsets.UTF_8);
		} catch (IOException | URISyntaxException e) {
			throw new RuntimeException(e);
		}
	}

	private static TemplateEntity convertToEntity(Template template) {
		TemplateEntity entity = new TemplateEntity();
		entity.setTemplateId(template.getId());
		entity.setTitle(template.getTitle());
		entity.setPurpose(template.getPurpose());
		entity.setGroupId(template.getForType());
		entity.setCorrespondingInstance(template.getCorrespondingInstance());
		entity.setPrimary(template.getPrimary());
		entity.setContentDigest(template.getContentDigest());
		entity.setRule(template.getRule());
		entity.setPublishedInstanceVersion(template.getPublishedInstanceVersion());
		return entity;
	}
}
