package com.sirmaenterprise.sep.models;

import static com.sirma.itt.seip.domain.instance.DefaultProperties.SEMANTIC_TYPE;
import static com.sirma.itt.seip.domain.instance.DefaultProperties.TYPE;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.fail;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyMap;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.contains;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Stream;

import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.FileUploadException;
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

import com.sirma.itt.seip.Uri;
import com.sirma.itt.seip.convert.TypeConverter;
import com.sirma.itt.seip.definition.DefinitionService;
import com.sirma.itt.seip.definition.SemanticDefinitionService;
import com.sirma.itt.seip.definition.model.PropertyDefinitionProxy;
import com.sirma.itt.seip.domain.codelist.CodelistService;
import com.sirma.itt.seip.domain.definition.DefinitionModel;
import com.sirma.itt.seip.domain.instance.ClassInstance;
import com.sirma.itt.seip.domain.instance.DefaultProperties;
import com.sirma.itt.seip.domain.instance.Instance;
import com.sirma.itt.seip.domain.instance.InstanceReference;
import com.sirma.itt.seip.domain.rest.EmfApplicationException;
import com.sirma.itt.seip.instance.InstanceTypeResolver;
import com.sirma.itt.seip.instance.ObjectInstance;
import com.sirma.itt.seip.instance.dao.InstanceService;
import com.sirma.itt.seip.instance.validation.InstanceValidationService;
import com.sirma.itt.seip.instance.validator.ExistingInContext;
import com.sirma.itt.seip.permissions.InstanceAccessEvaluator;
import com.sirma.itt.seip.resources.EmfUser;
import com.sirma.itt.seip.resources.ResourceService;
import com.sirma.itt.seip.security.UserPreferences;
import com.sirma.itt.seip.template.Template;
import com.sirma.itt.seip.template.TemplateProperties;
import com.sirma.itt.seip.template.TemplateService;
import com.sirma.itt.seip.testutil.mocks.DefinitionMock;
import com.sirma.itt.seip.testutil.mocks.InstanceReferenceMock;
import com.sirma.itt.seip.testutil.mocks.PropertyDefinitionMock;
import com.sirma.itt.semantic.model.vocabulary.EMF;
import com.sirma.sep.content.type.MimeTypeResolver;
import com.sirma.sep.definition.DefinitionImportService;
import com.sirma.sep.definition.DefinitionInfo;
import com.sirma.sep.model.ModelExportRequest;
import com.sirma.sep.model.ModelImportService;

/**
 * Test class for {@link ModelsResource}
 *
 * @author BBonev
 */
public class ModelsResourceTest {

	private static final String PARENT_PREFIX = "parentOf-";
	private static final String PURPOSE_CREATE = "CREATE";
	private static final String PURPOSE_UPLOAD = "UPLOAD";
	private static final String ERROR_MESSAGE_CREATE = "create error message";
	private static final String ERROR_MESSAGE_UPLOAD = "upload error message";
	private static final String CONTEXT_ID_FILTER = "context";

	@InjectMocks
	@Spy
	private ModelsResource modelsResource;
	@Mock
	private ModelImportService modelImportService;
	@Mock
	private InstanceService instanceService;
	@Mock
	private SemanticDefinitionService semanticDefinitionService;
	@Mock
	private DefinitionService definitionService;
	@Mock
	private MimeTypeResolver mimeTypeResolver;
	@Mock
	private InstanceTypeResolver instanceResolver;
	@Mock
	private UserPreferences userPreferences;
	@Mock
	private CodelistService codelistService;
	@Mock
	private TypeConverter typeConverter;
	@Mock
	private InstanceValidationService contextValidationHelper;
	@Mock
	private InstanceAccessEvaluator accessEvaluator;
	@Spy
	private SearchServiceMock searchService;
	@Mock
	private TemplateService templateService;
	@Mock
	private DefinitionImportService definitionImportService;
	@Mock
	private ResourceService resourceService;

	@Before
	public void init() {
		MockitoAnnotations.initMocks(this);
		when(userPreferences.getLanguage()).thenReturn("en");
		when(mimeTypeResolver.resolveFromName(contains("txt"))).thenReturn("text/plain");
		when(mimeTypeResolver.resolveFromName(contains("jpg"))).thenReturn("image/jpg");
		when(instanceResolver.resolveReference("emf:instance")).thenReturn(Optional.of(mock(InstanceReference.class)));
		when(instanceResolver.resolveReference("emf:notFound")).thenReturn(Optional.empty());
		when(codelistService.getDescription(any(), anyString()))
				.then(a -> "Description of " + a.getArgumentAt(1, String.class));
		when(typeConverter.convert(eq(Uri.class), anyString())).then(a -> {
			Uri uri = mock(Uri.class);
			when(uri.toString()).thenReturn(a.getArgumentAt(1, String.class));
			return uri;
		});
		when(typeConverter.tryConvert(eq(Uri.class), anyString())).then(a -> {
			Uri uri = mock(Uri.class);
			when(uri.toString()).thenReturn(a.getArgumentAt(1, String.class));
			return uri;
		});
		when(accessEvaluator.actionAllowed(any(Serializable.class), anyString())).then(invocation -> {
			Serializable id = invocation.getArgumentAt(0, Serializable.class);

			if (id instanceof String) {
				String strId = (String) id;
				if (strId.contains("emf:Case")) {
					return Boolean.FALSE;
				}
			}

			return Boolean.TRUE;
		});

		when(definitionImportService.getImportedDefinitions()).thenReturn(existingDefinitions);
	}

	@Test
	public void should_ValueOfExistingInContextBeBOTH_When_ValueIsInvalid() {
		DefinitionModel model = createDefinitionModelWithExistingInContextConfiguration(DefaultProperties.EXISTING_IN_CONTEXT, "NOT_SUPPORTED_VALUE");
		Mockito.when(definitionService.find("definition-with-not-supported-value")).thenReturn(model);

		Assert.assertEquals(ExistingInContext.BOTH.toString(), modelsResource.getExistingInContext("definition-with-not-supported-value"));
	}

	@Test
	public void should_ReturnValueOfExistingInContext_When_FieldInDefinitionIsSet() {
		DefinitionModel model = createDefinitionModelWithExistingInContextConfiguration(DefaultProperties.EXISTING_IN_CONTEXT, ExistingInContext.WITHOUT_CONTEXT.toString());
		Mockito.when(definitionService.find("definition-with-filed")).thenReturn(model);

		Assert.assertEquals(ExistingInContext.WITHOUT_CONTEXT.toString(), modelsResource.getExistingInContext("definition-with-filed"));
	}

	@Test
	public void should_ValueOfExistingInContextBeBOTH_When_FieldInDefinitionIsNotSet() {
		DefinitionMock model = new DefinitionMock();
		Mockito.when(definitionService.find("definition-without-filed")).thenReturn(model);

		Assert.assertEquals(ExistingInContext.BOTH.toString(), modelsResource.getExistingInContext("definition-without-filed"));
	}

	@Test
	public void should_ValueOfExistingInContextBeNull_When_DefinitionModelNotFound() {
		Assert.assertNull(modelsResource.getExistingInContext("not-existing-definition-id"));
	}

	@Test
	public void should_Build_Response_Containing_Exported_File_And_Name() {
		File exportedFile = withExportedModelFile("models.zip");

		Response response = modelsResource.exportModels(new ModelExportRequest());

		assertEquals(exportedFile, response.getEntity());
		assertEquals("models.zip", response.getHeaders().get("X-File-Name").get(0));
		assertEquals(MediaType.APPLICATION_OCTET_STREAM, response.getMediaType().toString());
	}

	@Test
	public void should_Return_All_Imported_Models() {
		withExistingUser("user1");
		withExistingInstance("user1", "User 1 Header", "");

		Date modifiedOn = new Date();

		withExistingDefinition("def1", "def1.xml", false, "user1", modifiedOn);
		withExistingDefinition("def2", "def2.xml", true, "user1", modifiedOn);

		Template template1 = new Template();
		template1.setId("template1");
		template1.setModifiedBy("user1");
		template1.setModifiedOn(modifiedOn);
		template1.setPrimary(true);
		template1.setPurpose("uploadable");
		template1.setCorrespondingInstance("emf:template1");

		Template template2 = new Template();
		template2.setId("template2");
		template2.setPrimary(true);
		template2.setPurpose("uploadable");
		template2.setModifiedOn(modifiedOn);
		template2.setCorrespondingInstance("emf:template2");

		withExistingInstance("emf:template1", "Template 1 Header", "Audio label");
		withExistingInstance("emf:template2", "Template 2 Header", "Audio label");

		withExistingTemplates(template1, template2);

		ImportedModelsInfo result = modelsResource.getImportedModels();

		ImportedTemplate expectedTmpl1 = new ImportedTemplate();
		expectedTmpl1.setId("template1");
		expectedTmpl1.setTitle("Template 1 Header");
		expectedTmpl1.setForObjectType("Audio label");
		expectedTmpl1.setPrimary(true);
		expectedTmpl1.setPurpose("uploadable");
		expectedTmpl1.setModifiedBy("User 1 Header");
		expectedTmpl1.setModifiedOn(modifiedOn);

		ImportedTemplate expectedTmpl2 = new ImportedTemplate();
		expectedTmpl2.setId("template2");
		expectedTmpl2.setTitle("Template 2 Header");
		expectedTmpl2.setForObjectType("Audio label");
		expectedTmpl2.setPrimary(true);
		expectedTmpl2.setPurpose("uploadable");
		expectedTmpl2.setModifiedOn(modifiedOn);

		List<ImportedTemplate> expectedTemplates = Arrays.asList(expectedTmpl1, expectedTmpl2);

		ImportedDefinition defModel1 = new ImportedDefinition();
		defModel1.setId("def1");
		defModel1.setTitle("Description of def1");
		defModel1.setAbstract(false);
		defModel1.setFileName("def1.xml");
		defModel1.setModifiedOn(modifiedOn);
		defModel1.setModifiedBy("User 1 Header");

		ImportedDefinition defModel2 = new ImportedDefinition();
		defModel2.setId("def2");
		defModel2.setTitle("Description of def2");
		defModel2.setAbstract(true);
		defModel2.setFileName("def2.xml");
		defModel2.setModifiedOn(modifiedOn);
		defModel2.setModifiedBy("User 1 Header");

		verifyImportedDefinitions(Arrays.asList(defModel1, defModel2), result.getDefinitions());
		assertEquals(expectedTemplates, result.getTemplates());
	}

	private void verifyImportedDefinitions(List<ImportedDefinition> expectedDefinitions, List<ImportedDefinition> importedDefinitions) {
		assertEquals(expectedDefinitions.size(), importedDefinitions.size());

		for (int i = 0; i < expectedDefinitions.size(); i++) {
			ImportedDefinition expected = expectedDefinitions.get(i);
			ImportedDefinition imported = importedDefinitions.get(i);

			assertEquals(expected.getId(), imported.getId());
			assertEquals(expected.getTitle(), imported.getTitle());
			assertEquals(expected.getFileName(), imported.getFileName());
			assertEquals(expected.isAbstract(), imported.isAbstract());
			assertEquals(expected.getModifiedBy(), imported.getModifiedBy());
			assertEquals(expected.getModifiedOn(), imported.getModifiedOn());
		}
	}

	@Test
	public void should_Use_RawTitle_Instead_Of_Header_For_Mail_Templates() {
		Template mailTemplate = new Template();
		mailTemplate.setId("sampleMail");
		mailTemplate.setTitle("rawTitle");
		mailTemplate.setForType("emailTemplate");
		mailTemplate.setModifiedBy("user1");
		withExistingUser("user1");
		withExistingInstance("user1", "User 1 Header", "");

		withExistingTemplates(mailTemplate);

		ImportedModelsInfo result = modelsResource.getImportedModels();

		ImportedTemplate expectedMailTmpl = new ImportedTemplate();
		expectedMailTmpl.setId("sampleMail");
		expectedMailTmpl.setTitle("rawTitle");
		expectedMailTmpl.setForObjectType("emailTemplate");
		expectedMailTmpl.setModifiedBy("User 1 Header");
		List<ImportedTemplate> expectedTemplates = Arrays.asList(expectedMailTmpl);

		assertEquals(expectedTemplates, result.getTemplates());
	}

	@Test
	public void should_UseRawValues_When_TemplateInstanceCantBeFetched() {
		Template tmpl1 = new Template();
		tmpl1.setId("template1");
		tmpl1.setCorrespondingInstance("emf:tmpl1");
		tmpl1.setTitle("rawTitle");
		tmpl1.setForType("rawForType");
		tmpl1.setModifiedBy("user1");
		withExistingUser("user1");
		when(instanceResolver.resolveReference(eq("user1"))).thenReturn(Optional.empty());
		when(instanceResolver.resolveReference(eq("emf:tmpl1"))).thenReturn(Optional.empty());

		withExistingTemplates(tmpl1);
		ImportedModelsInfo result = modelsResource.getImportedModels();

		ImportedTemplate expectedTmpl = new ImportedTemplate();
		expectedTmpl.setId("template1");
		expectedTmpl.setTitle("rawTitle");
		expectedTmpl.setForObjectType("rawForType");
		expectedTmpl.setModifiedBy("user1");
		List<ImportedTemplate> expectedTemplates = Arrays.asList(expectedTmpl);

		assertEquals(expectedTemplates, result.getTemplates());
	}


	@Test(expected = EmfApplicationException.class)
	public void should_Throw_Exception_If_User_ModifiedBy_User_NotFound() {
		Template template1 = new Template();
		template1.setId("template1");
		template1.setModifiedBy("user1");
		template1.setModifiedOn(new Date());
		template1.setCorrespondingInstance("emf:template1");
		when(resourceService.findResource(anyString())).thenReturn(null);
		withExistingInstance("emf:template1", "Template 1 Header", "");
		withExistingTemplates(template1);

		modelsResource.getImportedModels();
	}

	@Test
	public void should_NotFilterDefinitionWithPropertyWithoutContext_When_ParameterContextIdIsNotNullWhenPurposeIsSearch() {
		setUpExistingInContextTest(true, false);
		Mockito.when(instanceResolver.resolveReference(CONTEXT_ID_FILTER)).thenReturn(Optional.empty());
		Set<String> purpose = new HashSet<>(1);
		purpose.add("search");

		modelsResource.getModelsInfo(null, purpose, null, null, CONTEXT_ID_FILTER, null);

		Mockito.verify(contextValidationHelper, never()).canExistInContext(Matchers.any());
		Mockito.verify(contextValidationHelper, never()).canExistWithoutContext(Matchers.any());
	}

	@Test
	public void should_NotFilterDefinitionWithPropertyInContext_When_ParameterContextIdIsNotNull() {
		setUpExistingInContextTest(false, true);
		Mockito.when(instanceResolver.resolveReference(CONTEXT_ID_FILTER)).thenReturn(Optional.empty());

		ModelsInfo info = modelsResource.getModelsInfo(null, null, null, null, CONTEXT_ID_FILTER, null);

		assertContainsDefinitionId(info, "def1");
		assertContainsDefinitionId(info, "def2");
	}

	@Test
	public void should_NotFilterDefinitionWithPropertyBoth_When_ParameterContextIdIsNotNull() {
		setUpExistingInContextTest(true, true);
		Mockito.when(instanceResolver.resolveReference(CONTEXT_ID_FILTER)).thenReturn(Optional.empty());

		ModelsInfo info = modelsResource.getModelsInfo(null, null, null, null, CONTEXT_ID_FILTER, null);

		assertContainsDefinitionId(info, "def1");
		assertContainsDefinitionId(info, "def2");
	}

	@Test
	public void should_FilterDefinitionWithPropertyWithoutContext_When_ParameterContextIdIsNotNull() {
		setUpExistingInContextTest(true, false);
		Mockito.when(instanceResolver.resolveReference(CONTEXT_ID_FILTER)).thenReturn(Optional.empty());

		ModelsInfo info = modelsResource.getModelsInfo(null, null, null, null, CONTEXT_ID_FILTER, null);

		assertContainsDefinitionId(info, "def1");
		assertNotContainsDefinitionId(info, "def2");
	}

	@Test
	public void should_NotFilterDefinition_When_ParameterContextIdIsNotNullAndExistInContextIsNotSet() {
		mockDefinitions("def1", "def2");
		mockClassInstance("emf:def1", false, false, false);
		mockClassInstance("emf:def2", false, false, false);

		List<String> instanceIds = new ArrayList<>();
		instanceIds.add("emf:def1");
		instanceIds.add("emf:def2");
		mockLibraryFilter(instanceIds);
		Mockito.when(instanceResolver.resolveReference(CONTEXT_ID_FILTER)).thenReturn(Optional.empty());

		ModelsInfo info = modelsResource.getModelsInfo(null, null, null, null, CONTEXT_ID_FILTER, null);

		assertContainsDefinitionId(info, "emf:def1");
		assertContainsDefinitionId(info, "emf:def2");
	}

	@Test
	public void should_NotFilterDefinitionWithPropertyWithoutContext_When_ParameterContextIdIsNull() {
		setUpExistingInContextTest(true, false);

		ModelsInfo info = modelsResource.getModelsInfo(null, null, null, null, null, null);

		assertContainsDefinitionId(info, "def1");
		assertContainsDefinitionId(info, "def2");
	}

	@Test
	public void should_NotFilterDefinitionWithPropertyBoth_When_ParameterContextIdIsNull() {
		setUpExistingInContextTest(true, true);

		ModelsInfo info = modelsResource.getModelsInfo(null, null, null, null, null, null);

		assertContainsDefinitionId(info, "def1");
		assertContainsDefinitionId(info, "def2");
	}

	@Test
	public void should_FilterDefinitionWithPropertyInContext_When_ParameterContextIdIsNull() {
		setUpExistingInContextTest(false, true);

		ModelsInfo info = modelsResource.getModelsInfo(null, null, null, null, null, null);

		assertContainsDefinitionId(info, "def1");
		assertNotContainsDefinitionId(info, "def2");
	}

	@Test
	public void should_NotFilterDefinition_When_ParameterContextIdIsNullAndExistInContextIsNotSet() {
		mockDefinitions("def1", "def2");
		mockClassInstance("emf:def1", false, false, false);
		mockClassInstance("emf:def2", false, false, false);

		List<String> instanceIds = new ArrayList<>();
		instanceIds.add("emf:def1");
		instanceIds.add("emf:def2");
		mockLibraryFilter(instanceIds);

		ModelsInfo info = modelsResource.getModelsInfo(null, null, null, null, null, null);

		assertContainsDefinitionId(info, "emf:def1");
		assertContainsDefinitionId(info, "emf:def2");
	}


	@Test
	public void should_ParseFilesFromHttpRequestAndCallImportService() throws IOException, FileUploadException {
		FileItem item = mock(FileItem.class);
		when(item.getName()).thenReturn("f1.zip");
		when(item.getInputStream()).thenReturn(new ByteArrayInputStream("test".getBytes()));

		doReturn(Arrays.asList(item)).when(modelsResource).extractUploadedFiles(any(), any());

		modelsResource.importModels(null);

		ArgumentCaptor<Map<String, InputStream>> captor = ArgumentCaptor.forClass(Map.class);

		verify(modelImportService).importModel(captor.capture());

		assertNotNull(captor.getValue().get("f1.zip"));
	}

	@Test(expected = ModelImportException.class)
	public void should_ThrowAnErrorWhenThereAreNoFilesProvided() throws IOException, FileUploadException {
		doReturn(Arrays.asList()).when(modelsResource).extractUploadedFiles(any(), any());

		when(modelImportService.importModel(anyMap())).thenReturn(Arrays.asList("Error occured"));

		modelsResource.importModels(null);
	}

	@Test(expected = ModelImportException.class)
	public void should_ThrowAnErrorWhenThereAreErrorsReturnByTheImportProcess() throws IOException, FileUploadException {
		FileItem item = mock(FileItem.class);
		when(item.getName()).thenReturn("f1.zip");
		when(item.getInputStream()).thenReturn(new ByteArrayInputStream("test".getBytes()));

		doReturn(Arrays.asList(item)).when(modelsResource).extractUploadedFiles(any(), any());

		when(modelImportService.importModel(anyMap())).thenReturn(Arrays.asList("Error occured"));

		modelsResource.importModels(null);
	}

	@Test(expected = IllegalStateException.class)
	public void should_ThrowExceptionWhenFileParsingperationFails() throws IOException, FileUploadException {
		FileItem item = mock(FileItem.class);
		when(item.getName()).thenReturn("f1.zip");
		when(item.getInputStream()).thenReturn(new ByteArrayInputStream("test".getBytes()));

		doThrow(new FileUploadException()).when(modelsResource).extractUploadedFiles(any(), any());

		modelsResource.importModels(null);
	}

	@Test(expected = ModelImportException.class)
	public void should_ThrowAnErrorWhenTwoFilesWithTheSameNameAreProvided() throws IOException, FileUploadException {
		FileItem item = mock(FileItem.class);
		when(item.getName()).thenReturn("f1.zip");
		when(item.getInputStream()).thenReturn(new ByteArrayInputStream("test".getBytes()));

		FileItem item2 = mock(FileItem.class);
		when(item2.getName()).thenReturn("f1.zip");
		when(item2.getInputStream()).thenReturn(new ByteArrayInputStream("test".getBytes()));

		doReturn(Arrays.asList(item, item2)).when(modelsResource).extractUploadedFiles(any(), any());

		modelsResource.importModels(null);
	}

	@Test
	public void getAllDefinitions_filterByDefinition() throws Exception {
		mockDefinitions("def1", "def2", "def3");
		mockClassInstance("emf:def1", false, false, false);
		mockClassInstance("emf:def2", false, false, false);
		mockClassInstance("emf:def3", false, false, false);

		List<String> instanceIds = new ArrayList<>();
		instanceIds.add("emf:def1");
		instanceIds.add("emf:def2");
		instanceIds.add("emf:def3");
		mockLibraryFilter(instanceIds);

		ModelsInfo info = modelsResource.getModelsInfo(null, null, null, null, null, new HashSet<>(Arrays.asList("def1")));

		// 1x class, 1x def
		assertEquals(2, info.size());

		Iterator<ModelInfo> iterator = info.iterator();
		List<String> returnedIdsOfModels = new ArrayList<>();
		while (iterator.hasNext()) {
			returnedIdsOfModels.add(iterator.next().getId());
		}
		//
		Assert.assertTrue(returnedIdsOfModels.contains("def1"));
		Assert.assertTrue(returnedIdsOfModels.contains("emf:def1"));

		Assert.assertFalse(returnedIdsOfModels.contains("def2"));
		Assert.assertFalse(returnedIdsOfModels.contains("emf:def2"));

		Assert.assertFalse(returnedIdsOfModels.contains("def3"));
		Assert.assertFalse(returnedIdsOfModels.contains("emf:def3"));
	}

	@Test
	public void getAllDefinitionsWithOutFiltering() throws Exception {
		mockDefinitions("def1", "def2", "def3");
		mockClassInstance("emf:def1", false, false, false);
		mockClassInstance("emf:def2", false, false, false);
		mockClassInstance("emf:def3", false, false, false);

		List<String> instanceIds = new ArrayList<>();
		instanceIds.add("emf:def1");
		instanceIds.add("emf:def2");
		instanceIds.add("emf:def3");
		mockLibraryFilter(instanceIds);

		ModelsInfo info = modelsResource.getModelsInfo(null, null, null, null, null, null);

		// 3x parent class, 3x class, 3x def
		assertEquals(9, info.size());
	}

	@Test
	public void should_GenerateCreateErrorMessage_When_PurposeIsCreate() {

		mockDefinitions("def1", "def2", "def3");
		mockClassInstance("emf:def1", false, true, false);
		mockClassInstance("emf:def2", false, true, false);
		mockClassInstance("emf:def3", false, true, false);

		List<String> instanceIds = new ArrayList<>();
		instanceIds.add("emf:def1");
		instanceIds.add("emf:def2");
		instanceIds.add("emf:def3");
		mockLibraryFilter(instanceIds);

		Mockito.when(instanceResolver.resolveReference(CONTEXT_ID_FILTER)).thenReturn(Optional.empty());
		Mockito.when(contextValidationHelper.canCreateOrUploadIn(CONTEXT_ID_FILTER, PURPOSE_CREATE))
				.thenReturn(Optional.of(ERROR_MESSAGE_CREATE));

		ModelsInfo info = modelsResource.getModelsInfo(null, new HashSet<>(Arrays.asList(PURPOSE_CREATE.toLowerCase())), null, null,
													   CONTEXT_ID_FILTER, null);
		assertEquals(6, info.size());
		verify(contextValidationHelper, atLeastOnce()).canCreateOrUploadIn(CONTEXT_ID_FILTER, PURPOSE_CREATE);
		assertEquals(ERROR_MESSAGE_CREATE, info.getErrorMessage());
	}

	@Test
	public void should_GenerateUploadErrorMessage_When_PurposeIsUpload() {
		mockDefinitions("def1", "def2", "def3");
		mockClassInstance("emf:def1", false, true, false);
		mockClassInstance("emf:def2", false, true, false);
		mockClassInstance("emf:def3", false, true, false);

		List<String> instanceIds = new ArrayList<>();
		instanceIds.add("emf:def1");
		instanceIds.add("emf:def2");
		instanceIds.add("emf:def3");
		mockLibraryFilter(instanceIds);
		Mockito.when(instanceResolver.resolveReference(CONTEXT_ID_FILTER)).thenReturn(Optional.empty());
		Mockito.when(contextValidationHelper.canCreateOrUploadIn(CONTEXT_ID_FILTER, PURPOSE_UPLOAD))
				.thenReturn(Optional.of(ERROR_MESSAGE_UPLOAD));

		ModelsInfo info = modelsResource.getModelsInfo(null, new HashSet<>(Arrays.asList(PURPOSE_UPLOAD.toLowerCase())), null, null,
													   CONTEXT_ID_FILTER, null);

		verify(contextValidationHelper, atLeastOnce()).canCreateOrUploadIn(CONTEXT_ID_FILTER, PURPOSE_UPLOAD);
		assertEquals(ERROR_MESSAGE_UPLOAD, info.getErrorMessage());
	}

	@Test
	public void should_GenerateCreateUploadErrorMessage_When_PurposeIsCreateUploadSearch() {
		mockDefinitions("def1", "def2", "def3");
		mockClassInstance("emf:def1", false, true, false);
		mockClassInstance("emf:def2", false, true, false);
		mockClassInstance("emf:def3", false, true, false);

		List<String> instanceIds = new ArrayList<>();
		instanceIds.add("emf:def1");
		instanceIds.add("emf:def2");
		instanceIds.add("emf:def3");
		mockLibraryFilter(instanceIds);
		Mockito.when(instanceResolver.resolveReference(CONTEXT_ID_FILTER)).thenReturn(Optional.empty());
		Mockito.when(contextValidationHelper.canCreateOrUploadIn(CONTEXT_ID_FILTER, PURPOSE_CREATE))
				.thenReturn(Optional.of(ERROR_MESSAGE_CREATE));
		Mockito.when(contextValidationHelper.canCreateOrUploadIn(CONTEXT_ID_FILTER, PURPOSE_UPLOAD))
				.thenReturn(Optional.of(ERROR_MESSAGE_UPLOAD));

		ModelsInfo info = modelsResource.getModelsInfo(null, new HashSet<>(Arrays.asList(PURPOSE_CREATE.toLowerCase(), PURPOSE_UPLOAD.toLowerCase())), null, null,
											CONTEXT_ID_FILTER, null);
		assertEquals(6, info.size());
		verify(contextValidationHelper, atLeastOnce()).canCreateOrUploadIn(CONTEXT_ID_FILTER, PURPOSE_CREATE);
		assertEquals(ERROR_MESSAGE_CREATE + System.lineSeparator() + ERROR_MESSAGE_UPLOAD, info.getErrorMessage());
	}

	@Test
	public void should_NotGenerateUploadErrorMessage_When() {
		mockDefinitions("def1", "def2", "def3");
		mockClassInstance("emf:def1", false, true, false);
		mockClassInstance("emf:def2", false, true, false);
		mockClassInstance("emf:def3", false, true, false);

		List<String> instanceIds = new ArrayList<>();
		instanceIds.add("emf:def1");
		instanceIds.add("emf:def2");
		instanceIds.add("emf:def3");
		mockLibraryFilter(instanceIds);

		Mockito.when(contextValidationHelper.canCreateOrUploadIn(CONTEXT_ID_FILTER, PURPOSE_CREATE))
				.thenReturn(Optional.empty());
		Mockito.when(instanceResolver.resolveReference(CONTEXT_ID_FILTER)).thenReturn(Optional.empty());

		ModelsInfo info = modelsResource.getModelsInfo(null, new HashSet<>(Arrays.asList(PURPOSE_CREATE.toLowerCase())), null, null,
													   CONTEXT_ID_FILTER, null);
		String errorMessage = info.getErrorMessage();
		assertEquals(6, info.size());
	}

	@Test
	public void getAllDefinitions_filterByClass() throws Exception {
		mockDefinitions("def1", "def2", "def3");
		mockClassInstance("emf:def1", false, false, false);
		mockClassInstance("emf:def2", false, false, false);
		mockClassInstance("emf:def3", false, false, false);

		List<String> instanceIds = new ArrayList<>();
		instanceIds.add("emf:def1");
		instanceIds.add("emf:def2");
		instanceIds.add("emf:def3");
		mockLibraryFilter(instanceIds);

		ModelsInfo info = modelsResource.getModelsInfo(new HashSet<>(Arrays.asList("emf:def1", "emf:def2")), null, null,
				null, null, null);

		// 2x class, 2x def
		assertEquals(4, info.size());
	}

	@Test
	public void getAllDefinitions_filterByClass_forSearch() throws Exception {
		mockDefinitions("def1", "def2", "def3");
		mockClassInstance("emf:def1", true, false, false);
		mockClassInstance("emf:def2", false, false, false);
		mockClassInstance("emf:def3", false, false, false);

		List<String> instanceIds = new ArrayList<>();
		instanceIds.add("emf:def1");
		instanceIds.add("emf:def2");
		instanceIds.add("emf:def3");
		mockLibraryFilter(instanceIds);

		ModelsInfo info = modelsResource.getModelsInfo(
				new HashSet<>(Arrays.asList("emf:def1", PARENT_PREFIX + "emf:def2")),
				new HashSet<>(Arrays.asList("search")), null, null, null, null);

		// 1x class, 1x def, 1 parent class of other class
		assertEquals(3, info.size());
	}

	@Test
	public void getAllDefinitions_filterByClassWithSubclasses() throws Exception {
		mockDefinitions("def1", "def1-subclass");
		mockClassInstance("emf:def1", false, false, false);

		List<String> instanceIds = new ArrayList<>();
		instanceIds.add("emf:def1");
		instanceIds.add("emf:def2");
		instanceIds.add("emf:def3");
		mockLibraryFilter(instanceIds);

		ModelsInfo info = modelsResource.getModelsInfo(new HashSet<>(Arrays.asList("emf:def1")), null, null, null, null, null);

		// 2x class, 2x def
		assertEquals(4, info.size());
	}

	@Test
	public void getAllDefinitions_forNotFoundContext() throws Exception {
		mockDefinitions("def1", "def2", "def3");
		mockClassInstance("emf:def1", false, false, false);
		mockClassInstance("emf:def2", false, false, false);
		mockClassInstance("emf:def3", false, false, false);

		List<String> instanceIds = new ArrayList<>();
		instanceIds.add("emf:def1");
		instanceIds.add("emf:def2");
		instanceIds.add("emf:def3");
		mockLibraryFilter(instanceIds);

		ModelsInfo info = modelsResource.getModelsInfo(null, null, null, null, "emf:notFound", null);

		// 3x parent class, 3x class, 3x def
		assertEquals(9, info.size());
	}

	@Test
	public void should_GetAllDefinition_When_AllowedChildrenMissing() {
		mockDefinitions("def1", "def2", "def3");
		mockClassInstance("emf:def1", false, false, false);
		mockClassInstance("emf:def2", false, false, false);
		mockClassInstance("emf:def3", false, false, false);

		List<String> instanceIds = new ArrayList<>();
		instanceIds.add("emf:def1");
		instanceIds.add("emf:def2");
		instanceIds.add("emf:def3");
		mockLibraryFilter(instanceIds);

		when(instanceService.getAllowedChildren(any(Instance.class))).thenReturn(new HashMap<>());

		ModelsInfo info = modelsResource.getModelsInfo(null, null, null, null, "emf:instance", null);

		// 3x parent class, 3x class, 3x def
		assertEquals(9, info.size());
	}

	@Test
	public void getAllDefinitions_forValidContext() throws Exception {
		mockDefinitions("def1", "def2", "def3");
		mockClassInstance("emf:def1", false, false, false);
		mockClassInstance("emf:def2", false, false, false);
		mockClassInstance("emf:def3", false, false, false);
		Map<String, List<DefinitionModel>> models = new HashMap<>();
		models.put("case", Arrays.asList(createDefinition("def1")));
		models.put("document", Arrays.asList(createDefinition("def2"), createDefinition("def3")));

		List<String> instanceIds = new ArrayList<>();
		instanceIds.add("emf:def1");
		instanceIds.add("emf:def2");
		instanceIds.add("emf:def3");
		mockLibraryFilter(instanceIds);

		when(instanceService.getAllowedChildren(any(Instance.class))).thenReturn(models);

		ModelsInfo info = modelsResource.getModelsInfo(null, null, null, null, "emf:instance", null);

		// 3x parent class, 3x class, 3x def
		assertEquals(9, info.size());
	}

	@Test
	public void getAllDefinitions_forValidContextWithModels_shouldNotReturnUserDefinition() throws Exception {
		mockDefinitions("def1", "def2", "def3", "userDefinition|" + EMF.USER.toString());
		mockClassInstance("emf:def1", false, false, false);
		mockClassInstance("emf:def2", false, false, false);
		mockClassInstance("emf:def3", false, false, false);
		mockClassInstance(EMF.USER.toString(), true, true, false);

		List<String> instanceIds = new ArrayList<>();
		instanceIds.add("emf:def1");
		instanceIds.add("emf:def2");
		instanceIds.add("emf:def3");
		instanceIds.add(EMF.USER.toString());
		mockLibraryFilter(instanceIds);

		Map<String, List<DefinitionModel>> models = new HashMap<>();
		when(instanceService.getAllowedChildren(any(Instance.class))).thenReturn(models);

		ModelsInfo info = modelsResource.getModelsInfo(null, null, null, null, "emf:instance", null);

		// 3x parent class, 3x class, 3x def
		assertEquals(9, info.size());
	}

	@Test
	public void getAllDefinitions_forValidContext_shouldSkipUserDefinition() throws Exception {
		mockDefinitions("def1", "def2", "def3", "userDefinition|" + EMF.USER.toString());
		mockClassInstance("emf:def1", false, false, false);
		mockClassInstance("emf:def2", false, false, false);
		mockClassInstance("emf:def3", false, false, false);
		mockClassInstance(EMF.USER.toString(), true, true, false);
		Map<String, List<DefinitionModel>> models = new HashMap<>();
		models.put("case", Arrays.asList(createDefinition("def1")));
		models.put("user", Arrays.asList(createDefinition("userDefinition", EMF.USER.toString())));
		models.put("document", Arrays.asList(createDefinition("def2"), createDefinition("def3")));

		List<String> instanceIds = new ArrayList<>();
		instanceIds.add("emf:def1");
		instanceIds.add("emf:def2");
		instanceIds.add("emf:def3");
		instanceIds.add(EMF.USER.toString());
		mockLibraryFilter(instanceIds);

		when(instanceService.getAllowedChildren(any(Instance.class))).thenReturn(models);

		ModelsInfo info = modelsResource.getModelsInfo(null, null, null, null, "emf:instance", null);

		// 3x parent class, 3x class, 3x def
		assertEquals(9, info.size());
	}

	@Test
	public void getAllDefinitions_forMissingContext_shouldNotSkipUserDefinition() throws Exception {
		mockDefinitions("def1", "def2", "def3", "userDefinition|" + EMF.USER.toString());
		mockClassInstance("emf:def1", false, false, false);
		mockClassInstance("emf:def2", false, false, false);
		mockClassInstance("emf:def3", false, false, false);
		mockClassInstance(EMF.USER.toString(), true, true, false);

		List<String> instanceIds = new ArrayList<>();
		instanceIds.add("emf:def1");
		instanceIds.add("emf:def2");
		instanceIds.add("emf:def3");
		instanceIds.add(EMF.USER.toString());
		mockLibraryFilter(instanceIds);

		ModelsInfo info = modelsResource.getModelsInfo(null, null, null, null, "emf:notFound", null);

		// 3x parent class, 3x class, 3x def
		assertEquals(12, info.size());
	}

	@Test
	public void getAllDefinitions_forValidContext_shouldSkipGroupDefinition() throws Exception {
		mockDefinitions("def1", "def2", "def3", "userDefinition|" + EMF.USER.toString(), "groupDefinition|" + EMF.GROUP.toString());
		mockClassInstance("emf:def1", false, false, false);
		mockClassInstance("emf:def2", false, false, false);
		mockClassInstance("emf:def3", false, false, false);
		mockClassInstance(EMF.USER.toString(), true, true, false);
		Map<String, List<DefinitionModel>> models = new HashMap<>();
		models.put("case", Arrays.asList(createDefinition("def1")));
		models.put("user", Arrays.asList(createDefinition("userDefinition", EMF.USER.toString())));
		models.put("group", Arrays.asList(createDefinition("groupDefinition", EMF.GROUP.toString())));
		models.put("document", Arrays.asList(createDefinition("def2"), createDefinition("def3")));

		List<String> instanceIds = new ArrayList<>();
		instanceIds.add("emf:def1");
		instanceIds.add("emf:def2");
		instanceIds.add("emf:def3");
		instanceIds.add(EMF.USER.toString());
		instanceIds.add(EMF.GROUP.toString());
		mockLibraryFilter(instanceIds);

		when(instanceService.getAllowedChildren(any(Instance.class))).thenReturn(models);

		ModelsInfo info = modelsResource.getModelsInfo(null, null, null, null, "emf:instance", null);

		// 3x parent class, 3x class, 3x def
		assertEquals(9, info.size());
	}

	@Test
	public void getAllDefinitions_forMissingContext_shouldNotSkipGroupDefinition() throws Exception {
		mockDefinitions("def1", "def2", "def3", "groupDefinition|" + EMF.GROUP.toString());
		mockClassInstance("emf:def1", false, false, false);
		mockClassInstance("emf:def2", false, false, false);
		mockClassInstance("emf:def3", false, false, false);
		mockClassInstance(EMF.GROUP.toString(), true, true, false);

		List<String> instanceIds = new ArrayList<>();
		instanceIds.add("emf:def1");
		instanceIds.add("emf:def2");
		instanceIds.add("emf:def3");
		instanceIds.add(EMF.GROUP.toString());
		mockLibraryFilter(instanceIds);

		ModelsInfo info = modelsResource.getModelsInfo(null, null, null, null, "emf:notFound", null);

		// 3x parent class, 3x class, 3x def
		assertEquals(12, info.size());
	}

	@Test
	public void getAllDefinitions_filterByPurpose_create() throws Exception {
		mockDefinitions("def1", "def2", "def3");
		mockClassInstance("emf:def1", false, true, false);
		mockClassInstance("emf:def2", false, false, false);
		mockClassInstance("emf:def3", false, false, false);

		List<String> instanceIds = new ArrayList<>();
		instanceIds.add("emf:def1");
		instanceIds.add("emf:def2");
		instanceIds.add("emf:def3");
		mockLibraryFilter(instanceIds);

		ModelsInfo info = modelsResource.getModelsInfo(null, new HashSet<>(Arrays.asList("create")), null, null, null, null);

		assertEquals(2, info.size());
	}

	@Test
	public void getAllDefinitions_filterByPurpose_upload() throws Exception {
		mockDefinitions("def1", "def2", "def3");
		mockClassInstance("emf:def1", false, false, true);
		mockClassInstance("emf:def2", false, false, false);
		mockClassInstance("emf:def3", false, false, false);

		List<String> instanceIds = new ArrayList<>();
		instanceIds.add("emf:def1");
		instanceIds.add("emf:def2");
		instanceIds.add("emf:def3");
		mockLibraryFilter(instanceIds);

		ModelsInfo info = modelsResource.getModelsInfo(null, new HashSet<>(Arrays.asList("upload")), null, null, null, null);

		// 1 class, 1 def
		assertEquals(2, info.size());
	}

	@Test
	public void getAllDefinitions_filterByPurpose_search() throws Exception {
		mockDefinitions("def1", "def2", "def3");
		mockClassInstance("emf:def1", true, false, false);
		mockClassInstance("emf:def2", false, false, false);
		mockClassInstance("emf:def3", false, false, false);

		List<String> instanceIds = new ArrayList<>();
		instanceIds.add("emf:def1");
		instanceIds.add("emf:def2");
		instanceIds.add("emf:def3");
		mockLibraryFilter(instanceIds);

		ModelsInfo info = modelsResource.getModelsInfo(null, new HashSet<>(Arrays.asList("search")), null, null, null, null);

		// parent class, class and definition
		assertEquals(3, info.size());
	}

	@Test
	public void getAllDefinitions_filterByMimetype() throws Exception {
		mockDefinitions("def1", "def2", "def3");
		mockClassInstance("emf:def1", false, false, false, "text/plain");
		mockClassInstance("emf:def2", false, false, false, "image/jpg");
		mockClassInstance("emf:def3", false, false, false);

		List<String> instanceIds = new ArrayList<>();
		instanceIds.add("emf:def1");
		instanceIds.add("emf:def2");
		instanceIds.add("emf:def3");
		mockLibraryFilter(instanceIds);

		ModelsInfo info = modelsResource.getModelsInfo(null, null, "text/plain", null, null, null);

		assertEquals(9, info.size());

		checkForDefault(info, 1);
	}

	private static void checkForDefault(ModelsInfo info, int expectedCount) {
		int foundDefault = 0;
		for (ModelInfo modelInfo : info) {
			if (modelInfo.isDefault()) {
				foundDefault++;
				break;
			}
		}
		assertEquals("Should have default element ", foundDefault, expectedCount);
	}

	@Test
	public void getAllDefinitions_filterByExtension() throws Exception {
		mockDefinitions("def1", "def2", "def3");
		mockClassInstance("emf:def1", false, false, false, "text/plain");
		mockClassInstance("emf:def2", false, false, false, "image/jpg");
		mockClassInstance("emf:def3", false, false, false);

		List<String> instanceIds = new ArrayList<>();
		instanceIds.add("emf:def1");
		instanceIds.add("emf:def2");
		instanceIds.add("emf:def3");
		mockLibraryFilter(instanceIds);

		ModelsInfo info = modelsResource.getModelsInfo(null, null, null, "txt", null, null);

		assertEquals(9, info.size());

		checkForDefault(info, 1);
	}

	@Test
	public void getAllDefinitions_filterByMimeTypeAndExtension() throws Exception {
		mockDefinitions("def1", "def2", "def3");
		mockClassInstance("emf:def1", false, false, false, "text/plain");
		mockClassInstance("emf:def2", false, false, false, "image/jpg");
		mockClassInstance("emf:def3", false, false, false);

		List<String> instanceIds = new ArrayList<>();
		instanceIds.add("emf:def1");
		instanceIds.add("emf:def2");
		instanceIds.add("emf:def3");
		mockLibraryFilter(instanceIds);

		ModelsInfo info = modelsResource.getModelsInfo(null, null, MediaType.APPLICATION_OCTET_STREAM, "txt", null, null);

		// 2x parent class, 2x class, 2x def
		assertEquals(9, info.size());

		checkForDefault(info, 1);
	}

	@Test
	public void getAllDefinitions_filterByNonAccessibleLibraries() throws Exception {
		mockDefinitions("def1", "def2", "def3");
		mockClassInstance("emf:def1", false, false, false, "text/plain");
		mockClassInstance("emf:def2", false, false, false, "image/jpg");
		mockClassInstance("emf:def3", false, false, false);

		searchService.setFilter(Collections.emptyList());

		ModelsInfo info = modelsResource.getModelsInfo(null, null, MediaType.APPLICATION_OCTET_STREAM, "txt", null, null);

		// 3x parent class, 3x class, 3x def, 0 accesible libraries
		assertEquals(0, info.size());
	}

	@Test
	public void getModelsInfo_shouldFilterLibraries_withCreatePermission() {
		mockDefinitions("Case", "Project");
		mockClassInstance("emf:Case", false, true, false);
		mockClassInstance("emf:Project", false, true, false);

		List<String> instanceIds = new ArrayList<>();
		instanceIds.add("emf:Case");
		instanceIds.add("emf:Project");
		mockLibraryFilter(instanceIds);

		ModelsInfo info = modelsResource.getModelsInfo(null, new HashSet<>(Arrays.asList("create")), null, null, null,
				null);

		assertEquals(2, info.size());
	}

	@Test
	public void getModelsInfo_shouldFilterLibraries_withReadPermission() {
		mockDefinitions("Case", "Project");
		mockClassInstance("emf:Case", true, true, false);
		mockClassInstance("emf:Project", true, true, false);

		List<String> instanceIds = new ArrayList<>();
		instanceIds.add("emf:Case");
		instanceIds.add("emf:Project");
		mockLibraryFilter(instanceIds);

		ModelsInfo info = modelsResource.getModelsInfo(null, new HashSet<>(Arrays.asList("search")), null, null, null,
				null);

		// 2x class, 1 project def, case should be filtered out
		assertEquals(3, info.size());
	}

	/**
	 * If class filter is added and a definition filter is added with definition from the same class,
	 * the definition filter is ignored. Both filters (class and definition) are aggregated with OR (similar to searching).
	 * @throws Exception
	 */
	@Test
	public void getModelsInfo_filterByClass_and_definitionFromSameClass() throws Exception {
		mockDefinitions("def1", "def2", "def3", "def4|emf:def1");

		mockClassInstance("emf:def1", false, false, false);
		mockClassInstance("emf:def2", false, false, false);
		mockClassInstance("emf:def3", false, false, false);

		List<String> instanceIds = new ArrayList<>();
		instanceIds.add("emf:def1");
		instanceIds.add("emf:def2");
		instanceIds.add("emf:def3");
		mockLibraryFilter(instanceIds);

		ModelsInfo info = modelsResource.getModelsInfo(new HashSet<>(Arrays.asList("emf:def1", "emf:def2")), null, null,
				null, null, new HashSet<>(Arrays.asList("def1")));

		// 2x class, 3x def, definition filter is ignored, def4 is not filtered out
		assertEquals(5, info.size());
	}

	/**
	 * If definition filter is added with definition which class is not amongst class filter,
	 * then its class is returned but only with defininitions included in definition filter.
	 * @throws Exception
	 */
	@Test
	public void getModelsInfo_filterByClass_and_definitionFromDifferentClass() throws Exception {
		mockDefinitions("def1", "def2", "def3", "def4|emf:def1");

		mockClassInstance("emf:def1", false, false, false);
		mockClassInstance("emf:def2", false, false, false);
		mockClassInstance("emf:def3", false, false, false);

		List<String> instanceIds = new ArrayList<>();
		instanceIds.add("emf:def1");
		instanceIds.add("emf:def2");
		instanceIds.add("emf:def3");
		mockLibraryFilter(instanceIds);

		ModelsInfo info = modelsResource.getModelsInfo(new HashSet<>(Arrays.asList("emf:def2")), null, null,
				null, null, new HashSet<>(Arrays.asList("def1")));

		// 2x class, 2x def, emf:def2 definitions are not filtered, def4 is filtered out
		assertEquals(4, info.size());
	}

	@Test
	public void getModelsInfo_filterByDefinitionFromParentClass() throws Exception {
		mockDefinitions("def1", "def2", "def3", "def4|parentOf-emf:def1");

		mockClassInstance("emf:def1", false, false, false);
		mockClassInstance("emf:def2", false, false, false);
		mockClassInstance("emf:def3", false, false, false);

		List<String> instanceIds = new ArrayList<>();
		instanceIds.add("emf:def1");
		instanceIds.add("emf:def2");
		instanceIds.add("emf:def3");
		mockLibraryFilter(instanceIds);

		ModelsInfo info = modelsResource.getModelsInfo(null, null, null, null, null, new HashSet<>(Arrays.asList("def1")));

		// 1x class, 1x def, def4 is filtered out
		assertEquals(2, info.size());
	}

	@Test
	public void getModelsInfo_filterByDefinitionAndSubClass() throws Exception {
		mockDefinitions("def1", "def2", "def3", "def4|emf:def1-subclass");

		mockClassInstance("emf:def1", false, false, false);
		mockClassInstance("emf:def2", false, false, false);
		mockClassInstance("emf:def3", false, false, false);

		List<String> instanceIds = new ArrayList<>();
		instanceIds.add("emf:def1");
		instanceIds.add("emf:def2");
		instanceIds.add("emf:def3");
		mockLibraryFilter(instanceIds);

		ModelsInfo info = modelsResource.getModelsInfo(new HashSet<>(Arrays.asList("emf:def1-subclass")), null, null,
				null, null, new HashSet<>(Arrays.asList("def1")));

		// 2x class, 2x def
		assertEquals(4, info.size());
	}

	private File withExportedModelFile(String fileName) {
		File exportedFile = mock(File.class);
		when(exportedFile.getName()).thenReturn(fileName);
		when(modelImportService.exportModel(any(ModelExportRequest.class))).thenReturn(exportedFile);
		return exportedFile;
	}

	private List<DefinitionInfo> existingDefinitions = new ArrayList<>();

	private void withExistingDefinition(String id, String fileName, boolean isAbstract, String modifiedBy, Date modifiedOn) {
		existingDefinitions.add(new DefinitionInfo(id, fileName, isAbstract, modifiedBy, modifiedOn));

		mockDefinitions(id);
	}

	private void withExistingTemplates(Template... templates) {
		when(templateService.getAllTemplates()).thenReturn(Arrays.asList(templates));
	}

	private void withExistingUser(String userId) {
		EmfUser user = mock(EmfUser.class);
		when(user.getId()).thenReturn(userId);
		when(resourceService.findResource(eq(userId))).thenReturn(user);
	}

	private void withExistingInstance(String id, String header, String forTypeLabel) {
		Instance instance = mock(Instance.class);
		when(instance.getString(eq(DefaultProperties.HEADER_COMPACT))).thenReturn(header);
		when(instance.getString(eq(TemplateProperties.FOR_OBJECT_TYPE_LABEL))).thenReturn(forTypeLabel);
		InstanceReference reference = new InstanceReferenceMock(instance);
		when(instanceResolver.resolveReference(eq(id))).thenReturn(Optional.of(reference));
	}

	private void mockDefinitions(String... definitionIds) {
		DefinitionModel[] models = new DefinitionModel[definitionIds.length];
		for (int i = 0; i < definitionIds.length; i++) {
			String defId = definitionIds[i];
			String[] ids = defId.split("\\|");
			if (ids.length == 1) {
				models[i] = createDefinition(defId);
				when(definitionService.find(defId)).thenReturn(models[i]);
			} else if (ids.length == 2) {
				models[i] = createDefinition(ids[0], ids[1], true, true);
				when(definitionService.find(ids[0])).thenReturn(models[i]);
			} else {
				fail("can handle only up to 2 parameters");
			}
		}
		when(definitionService.getAllDefinitions()).thenAnswer(a -> Stream.of(models));
	}

	private DefinitionModel createDefinition(String defId) {
		return createDefinition(defId, "emf:" + defId, true, true);
	}

	private DefinitionModel createDefinition(String defId, String semanticType) {
		return createDefinition(defId, semanticType, true, true);
	}

	private DefinitionModel createDefinition(String defId, String semanticType, boolean canExistWithoutContext, boolean canExistInContext) {
		DefinitionMock definitionMock = new DefinitionMock();
		definitionMock.setIdentifier(defId);

		PropertyDefinitionMock typeField = new PropertyDefinitionMock();
		typeField.setName(TYPE);
		typeField.setValue(defId);
		typeField.setCodelist(Integer.valueOf(1));
		definitionMock.getFields().add(typeField);

		PropertyDefinitionMock semanticTypeField = new PropertyDefinitionMock();
		semanticTypeField.setIdentifier(SEMANTIC_TYPE);
		semanticTypeField.setValue(semanticType);
		definitionMock.getFields().add(semanticTypeField);
		Mockito.when(contextValidationHelper.canExistWithoutContext(definitionMock)).thenReturn(canExistWithoutContext);
		Mockito.when(contextValidationHelper.canExistInContext(definitionMock)).thenReturn(canExistInContext);
		return definitionMock;
	}

	@SuppressWarnings("boxing")
	private void mockClassInstance(String id, boolean searchable, boolean createable, boolean uploadable,
			String pattern) {
		String parentId = PARENT_PREFIX + id;
		ClassInstance parentInstance = new ClassInstance();
		parentInstance.setId(parentId);
		parentInstance.add("searchable", true);
		parentInstance.add("uploadable", false);
		parentInstance.add("createable", false);
		parentInstance.addIfNotNull("acceptDataTypePattern", pattern);
		parentInstance.setLabel("en", parentId);
		parentInstance.preventModifications();

		ClassInstance classInstance = new ClassInstance();
		classInstance.setId(id);
		classInstance.add("searchable", searchable);
		classInstance.add("uploadable", uploadable);
		classInstance.add("createable", createable);
		classInstance.addIfNotNull("acceptDataTypePattern", pattern);
		classInstance.setLabel("en", id);
		classInstance.getSuperClasses().add(parentInstance);

		String subclassId = id + "-subclass";
		ClassInstance subclass = new ClassInstance();
		subclass.setId(subclassId);
		subclass.add("searchable", searchable);
		subclass.add("uploadable", uploadable);
		subclass.add("createable", createable);
		subclass.addIfNotNull("acceptDataTypePattern", pattern);
		subclass.setLabel("en", subclassId);
		subclass.getSuperClasses().add(classInstance);

		classInstance.getSubClasses().put(subclassId, subclass);
		classInstance.preventModifications();
		subclass.preventModifications();

		when(semanticDefinitionService.getClassInstance(parentId)).thenReturn(parentInstance);
		when(semanticDefinitionService.getClassInstance(id)).thenReturn(classInstance);
		when(semanticDefinitionService.getClassInstance(subclassId)).thenReturn(subclass);

		when(semanticDefinitionService.collectSubclasses(parentId))
				.thenReturn(new LinkedHashSet<>(Arrays.asList(parentInstance, classInstance)));
		when(semanticDefinitionService.collectSubclasses(id))
				.thenReturn(new LinkedHashSet<>(Arrays.asList(classInstance, subclass)));
		when(semanticDefinitionService.collectSubclasses(subclassId))
				.thenReturn(new LinkedHashSet<>(Arrays.asList(subclass)));
	}

	private void mockClassInstance(String id, boolean searchable, boolean createable, boolean uploadable) {
		mockClassInstance(id, searchable, createable, uploadable, null);
	}

	private void mockLibraryFilter(List<String> instanceIds) {
		List<Instance> libraries = new ArrayList<>();

		for (String id : instanceIds) {
			libraries.add(mockObjectInstance(id));
			libraries.add(mockObjectInstance(PARENT_PREFIX + id));
		}
		searchService.setFilter(libraries);
	}

	private static Instance mockObjectInstance(String id) {
		Instance instance = new ObjectInstance();
		instance.setId(id);
		return instance;
	}

	private void assertContainsDefinitionId(ModelsInfo info, String definitionId) {
		Iterator<ModelInfo> iterator = info.iterator();
		while(iterator.hasNext()) {
			ModelInfo next = iterator.next();
			if (next.getId().equals(definitionId)) {
				return;
			}
		}
		Assert.fail();
	}

	private void assertNotContainsDefinitionId(ModelsInfo info, String definitionId) {
		Iterator<ModelInfo> iterator = info.iterator();
		while(iterator.hasNext()) {
			ModelInfo next = iterator.next();
			if (next.getId().equals(definitionId)) {
				Assert.fail();
			}
		}
	}

	/**
	 * Setup test with two definition.
	 * <pre>
	 *   1.  First will can exist in context and without context.
	 *   2.  Second will can exist in context and without context depends of <code>canExistWithoutContext</code> <code>canExistInContext</code>.
	 *
	 * </pre>
	 */
	private void setUpExistingInContextTest(boolean canExistWithoutContext, boolean canExistInContext) {
		DefinitionModel def1 = createDefinition("def1");
		DefinitionModel def2 = createDefinition("def2", "emf:def2", canExistWithoutContext, canExistInContext);

		when(definitionService.getAllDefinitions()).thenAnswer(a -> Stream.of(def1, def2));

		mockClassInstance("emf:def1", false, false, false);
		mockClassInstance("emf:def2", false, false, false);

		List<String> instanceIds = new ArrayList<>();
		instanceIds.add("emf:def1");
		instanceIds.add("emf:def2");
		mockLibraryFilter(instanceIds);
	}

	private DefinitionModel createDefinitionModelWithExistingInContextConfiguration(String fieldName, String defaultValue) {
		DefinitionMock model = new DefinitionMock();
		PropertyDefinitionProxy propertyDefinition = new PropertyDefinitionProxy();
		propertyDefinition.setName(fieldName);
		propertyDefinition.setValue(defaultValue);
		model.setConfigurations(Collections.singletonList(propertyDefinition));
		return model;
	}
}
