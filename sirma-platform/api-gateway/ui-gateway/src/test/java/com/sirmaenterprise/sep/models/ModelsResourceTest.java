package com.sirmaenterprise.sep.models;

import static com.sirma.itt.seip.domain.instance.DefaultProperties.SEMANTIC_TYPE;
import static com.sirma.itt.seip.domain.instance.DefaultProperties.TYPE;
import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.contains;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Stream;

import javax.ws.rs.core.MediaType;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Matchers;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.mockito.Spy;

import com.sirma.itt.seip.Uri;
import com.sirma.itt.seip.content.type.MimeTypeResolver;
import com.sirma.itt.seip.convert.TypeConverter;
import com.sirma.itt.seip.definition.DictionaryService;
import com.sirma.itt.seip.definition.SemanticDefinitionService;
import com.sirma.itt.seip.domain.codelist.CodelistService;
import com.sirma.itt.seip.domain.definition.DefinitionModel;
import com.sirma.itt.seip.domain.instance.ClassInstance;
import com.sirma.itt.seip.domain.instance.Instance;
import com.sirma.itt.seip.domain.instance.InstanceReference;
import com.sirma.itt.seip.instance.InstanceTypeResolver;
import com.sirma.itt.seip.instance.ObjectInstance;
import com.sirma.itt.seip.instance.dao.InstanceService;
import com.sirma.itt.seip.instance.validation.InstanceValidationService;
import com.sirma.itt.seip.security.UserPreferences;
import com.sirma.itt.seip.testutil.mocks.DefinitionMock;
import com.sirma.itt.seip.testutil.mocks.PropertyDefinitionMock;

/**
 * Test class for {@link ModelsResource}
 *
 * @author BBonev
 */
public class ModelsResourceTest {

	private static final String PARENT_PREFIX = "parentOf-";

	@InjectMocks
	private ModelsResource modelsResource;

	@Mock
	private InstanceService instanceService;
	@Mock
	private SemanticDefinitionService semanticDefinitionService;
	@Mock
	private DictionaryService definitionService;
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
	@Spy
	private SearchServiceMock searchService;

	@Before
	public void beforeMethod() {
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
	public void getAllDefinitionsWithValidation() throws Exception {
		mockDefinitions("def1", "def2", "def3");
		mockClassInstance("emf:def1", false, true, false);
		mockClassInstance("emf:def2", false, true, false);
		mockClassInstance("emf:def3", false, true, false);

		List<String> instanceIds = new ArrayList<>();
		instanceIds.add("emf:def1");
		instanceIds.add("emf:def2");
		instanceIds.add("emf:def3");
		mockLibraryFilter(instanceIds);

		Mockito.when(contextValidationHelper.canCreateOrUploadIn(Matchers.anyString(), Matchers.anyString()))
				.thenReturn(Optional.empty());
		ModelsInfo info = modelsResource.getModelsInfo(null, new HashSet<>(Arrays.asList("create")), null, null,
				"context", null);
		assertEquals(6, info.size());

		Mockito.when(contextValidationHelper.canCreateOrUploadIn(Matchers.anyString(), Matchers.anyString()))
				.thenReturn(Optional.of("error message"));
		info = modelsResource.getModelsInfo(null, new HashSet<>(Arrays.asList("create", "upload")), null, null,
				"context", null);
		assertEquals(6, info.size());
		verify(contextValidationHelper, atLeastOnce()).canCreateOrUploadIn("context", "CREATE");
		assertEquals("error message", info.getErrorMessage());

		info = modelsResource.getModelsInfo(null, new HashSet<>(Arrays.asList("upload")), null, null, "context", null);
		assertEquals(0, info.size());
		verify(contextValidationHelper, atLeastOnce()).canCreateOrUploadIn("context", "UPLOAD");
		assertEquals("error message", info.getErrorMessage());
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

	private void mockDefinitions(String... definitionIds) {
		DefinitionModel[] models = new DefinitionModel[definitionIds.length];
		for (int i = 0; i < definitionIds.length; i++) {
			String defId = definitionIds[i];
			models[i] = createDefinition(defId);
		}
		when(definitionService.getAllDefinitions()).thenAnswer(a -> Stream.of(models));
	}

	private static DefinitionModel createDefinition(String defId) {
		DefinitionMock definitionMock = new DefinitionMock();
		definitionMock.setIdentifier(defId);

		PropertyDefinitionMock typeField = new PropertyDefinitionMock();
		typeField.setName(TYPE);
		typeField.setValue(defId);
		typeField.setCodelist(Integer.valueOf(1));
		definitionMock.getFields().add(typeField);

		PropertyDefinitionMock semanticTypeField = new PropertyDefinitionMock();
		semanticTypeField.setIdentifier(SEMANTIC_TYPE);
		semanticTypeField.setValue("emf:" + defId);
		definitionMock.getFields().add(semanticTypeField);

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
}
