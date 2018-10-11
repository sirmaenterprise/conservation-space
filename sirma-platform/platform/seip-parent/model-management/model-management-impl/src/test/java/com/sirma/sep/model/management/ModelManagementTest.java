package com.sirma.sep.model.management;

import com.sirma.itt.seip.concurrent.locks.ContextualReadWriteLock;
import com.sirma.itt.seip.context.Contextual;
import com.sirma.itt.seip.context.ContextualReference;
import com.sirma.itt.seip.definition.SemanticDefinitionService;
import com.sirma.itt.seip.definition.dozer.DefinitionsDozerProvider;
import com.sirma.itt.seip.definition.label.LabelService;
import com.sirma.itt.seip.definition.model.LabelImpl;
import com.sirma.itt.seip.domain.codelist.CodelistService;
import com.sirma.itt.seip.domain.codelist.event.ResetCodelistEvent;
import com.sirma.itt.seip.domain.codelist.model.CodeValue;
import com.sirma.itt.seip.domain.definition.DisplayType;
import com.sirma.itt.seip.domain.instance.ClassInstance;
import com.sirma.itt.seip.domain.instance.PropertyInstance;
import com.sirma.itt.seip.event.EventService;
import com.sirma.itt.seip.mapping.ObjectMapper;
import com.sirma.itt.seip.plugin.Extension;
import com.sirma.itt.semantic.NamespaceRegistryService;
import com.sirma.itt.semantic.model.vocabulary.EMF;
import com.sirma.itt.semantic.model.vocabulary.Proton;
import com.sirma.sep.definition.DefinitionImportService;
import com.sirma.sep.model.ModelImportCompleted;
import com.sirma.sep.model.ModelNode;
import com.sirma.sep.model.management.codelists.CodeListsProvider;
import com.sirma.sep.model.management.definition.DefinitionModelAttributes;
import com.sirma.sep.model.management.definition.DefinitionModelConverter;
import com.sirma.sep.model.management.hierarchy.ModelHierarchyClass;
import com.sirma.sep.model.management.hierarchy.ModelHierarchyDefinition;
import com.sirma.sep.model.management.meta.ModelMetaInfo;
import com.sirma.sep.model.management.meta.ModelMetaInfoValidation;
import com.sirma.sep.model.management.meta.ModelsMetaInfo;
import com.sirma.sep.model.management.response.ModelResponse;

import org.apache.commons.lang3.StringUtils;
import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.model.vocabulary.DC;
import org.eclipse.rdf4j.model.vocabulary.DCTERMS;
import org.eclipse.rdf4j.model.vocabulary.RDF;
import org.eclipse.rdf4j.model.vocabulary.RDFS;
import org.eclipse.rdf4j.model.vocabulary.SKOS;
import org.jglue.cdiunit.AdditionalClasses;
import org.jglue.cdiunit.AdditionalClasspaths;
import org.jglue.cdiunit.AdditionalPackages;
import org.jglue.cdiunit.CdiRunner;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;

import javax.enterprise.inject.Produces;
import javax.inject.Inject;

import java.io.File;
import java.io.Serializable;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Component tests for the model calculation & selection functionality in {@link ModelManagementServiceImpl} and related classes.
 * <p>
 * It stubs external for the module services such as {@link SemanticDefinitionService} and {@link CodelistService}.
 *
 * @author Mihail Radkov
 */
@RunWith(CdiRunner.class)
@AdditionalClasses({ ModelManagementServiceImpl.class, DefinitionsDozerProvider.class })
@AdditionalPackages({ DefinitionModelConverter.class, CodeListsProvider.class })
@AdditionalClasspaths({ ObjectMapper.class, Extension.class, EventService.class })
public class ModelManagementTest {

	@Produces
	@Mock
	private SemanticDefinitionService semanticDefinitionService;

	@Produces
	@Mock
	private DefinitionImportService definitionImportService;
	private DefinitionImportServiceStub definitionImportServiceStub;

	@Produces
	@Mock
	private CodelistService codelistService;

	@Produces
	@Mock
	private LabelService labelService;

	@Produces
	@Mock
	private NamespaceRegistryService namespaceRegistryService;

	@Produces
	private ContextualReadWriteLock modelsLock = ContextualReadWriteLock.create();

	@Produces
	private Contextual<Models> modelsContext = ContextualReference.create();

	@Inject
	private EventService eventService;

	@Inject
	private ModelManagementService modelManagementService;

	@Before
	public void before() {
		SemanticDefinitionServiceStub semanticDefinitionServiceStub = new SemanticDefinitionServiceStub(semanticDefinitionService);
		definitionImportServiceStub = new DefinitionImportServiceStub(definitionImportService);
		CodelistServiceStub codelistServiceStub = new CodelistServiceStub(codelistService);

		semanticDefinitionServiceStub.withRootClass(getClass(Proton.ENTITY))
				.withClass(getClass(EMF.CASE, "en=Case", "bg=Преписка"), Proton.ENTITY)
				.withClass(getClass(EMF.ACTIVITY), Proton.ENTITY)
				.withClass(getClass(EMF.PROJECT), EMF.ACTIVITY);

		// Properties in class instances are not mapped by URIs...
		semanticDefinitionServiceStub.withPropertyForClass("creator", "Case creator", EMF.CASE)
				.withPropertyForClass("createable", true, EMF.CASE)
				.withPropertyForClass("uploadable", false, EMF.CASE)
				.withPropertyForClass("searchable", true, EMF.CASE);

		semanticDefinitionServiceStub.withProperty(getProperty(RDF.TYPE))
				.withProperty(getProperty(RDFS.LABEL, "en=Label"))
				.withProperty(getProperty(DCTERMS.TITLE, "en=Title"))
				.withProperty(getProperty(EMF.TYPE))
				.withProperty(getProperty(EMF.STATUS, "en=Status"))
				.fillProperty(EMF.STATUS, getMap("domainClass=ptop:Entity", "rangeClass=string"))
				.withRelation(getProperty(EMF.CREATED_BY));

		codelistServiceStub.withValueForList(getValue("PR0001", "en=Main Project", "bg=Основен проект"), 2)
				.withValueForList(getValue("CS0001", "en=Main Case", "bg=Основна Преписка"), 4)
				.withValueForList(getValue("CS0002", "en=Case with WF", "bg=Преписка създаване на работен процес"), 4);

		withLabelDefinitionFor("model.management.definition.abstract", "en=Abstract", "bg=Абстрактна");
		withLabelDefinitionFor("model.management.definition.identifier", "en=Identifier", "bg=Идентификатор");
		withLabelDefinitionFor("model.management.field.codeList", "en=Controlled vocabulary", "bg=Номенклатура");
		withLabelDefinitionFor("model.management.region.displayType", "en=Display type", "bg=Визуализация");
		withLabelDefinitionFor("type.label", "en=Type", "bg=Тип");
		withLabelDefinitionFor("description.PR0001.tooltip", "en=Description tooltip");
		withLabelDefinitionFor("generalDetails.region.label", "en=General details");

		stubNamespaceRegistry();
	}

	@Test
	public void shouldProperlyConvertClassModel() {
		ModelResponse response = modelManagementService.getModel(EMF.CASE.toString());
		ModelClass caseClass = response.getClasses().get(0);
		assertClassModel(caseClass, EMF.CASE, Proton.ENTITY);
		assertModelNodeLabels(caseClass, "en=Case", "bg=Преписка");

		// Should have transferred case properties as attributes via the model meta information
		hasAttribute(caseClass, DC.CREATOR.toString(), "string", "Case creator");
		hasAttribute(caseClass, EMF.IS_SEARCHABLE.toString(), "boolean", true);
		hasAttribute(caseClass, EMF.IS_CREATEABLE.toString(), "boolean", true);
		hasAttribute(caseClass, EMF.IS_UPLOADABLE.toString(), "boolean", false);
	}

	@Test
	public void shouldProperlyConvertPropertyModel() {
		List<ModelProperty> properties = modelManagementService.getProperties();
		assertModelProperties(properties, RDF.TYPE, RDFS.LABEL, DCTERMS.TITLE, EMF.TYPE, EMF.STATUS, EMF.CREATED_BY);

		Optional<ModelProperty> status = properties.stream().filter(p -> p.getId().equals(EMF.STATUS.toString())).findFirst();
		assertTrue(status.isPresent());
		assertModelNodeLabels(status.get(), "en=Status");
		hasAttribute(status.get(), RDFS.DOMAIN, ModelAttributeType.URI, "ptop:Entity");
		hasAttribute(status.get(), RDFS.RANGE, ModelAttributeType.SEMANTIC_TYPE, "string");

		Optional<ModelProperty> type = properties.stream().filter(p -> p.getId().equals(EMF.TYPE.toString())).findFirst();
		assertTrue(type.isPresent());
		assertTrue(type.get().getLabels().isEmpty());
	}

	@Test
	public void shouldProperlyConvertDefinitionModel() {
		withDefinitions("entity.xml", "genericCase.xml", "genericProject.xml", "genericTestCase.xml", "genericTestProject.xml",
				"CS0001.xml", "PR0001.xml");

		ModelResponse modelResponse = modelManagementService.getModel("CS0001");
		ModelDefinition caseDefinition = modelResponse.getDefinitions().get(0);

		assertDefinitionModel(caseDefinition, "CS0001", EMF.CASE, "genericTestCase", false);
		assertModelNodeLabels(caseDefinition, "en=Main Case", "bg=Основна Преписка");

		hasAttribute(caseDefinition, DefinitionModelAttributes.ABSTRACT, ModelAttributeType.BOOLEAN, false);
		hasAttribute(caseDefinition, DefinitionModelAttributes.RDF_TYPE, ModelAttributeType.URI, EMF.CASE.toString());

		hasFields(caseDefinition, "type", "status", "description", "priority");
		isMissingFields(caseDefinition, "rdf:type", "title");

		// Some fields may not be declared in the current definition but they can be fetched by using the parent references
		assertTrue(caseDefinition.getFieldByName("rdf:type").isPresent());
		assertTrue(caseDefinition.getFieldByName("title").isPresent());

		hasFieldAttribute(caseDefinition, "status", DefinitionModelAttributes.DISPLAY_TYPE, ModelAttributeType.DISPLAY_TYPE,
				DisplayType.READ_ONLY.toString());
		hasFieldAttribute(caseDefinition, "status", DefinitionModelAttributes.TYPE, ModelAttributeType.TYPE,
				"an..35");

		// type display type is declared in entity -> test if it is fetched from it
		hasFieldAttribute(caseDefinition, "type", DefinitionModelAttributes.DISPLAY_TYPE, ModelAttributeType.DISPLAY_TYPE,
				DisplayType.READ_ONLY.toString());
		hasFieldAttribute(caseDefinition, "type", DefinitionModelAttributes.TYPE, ModelAttributeType.TYPE,
				"an..180");

		hasFieldAttribute(caseDefinition, "priority", DefinitionModelAttributes.ORDER, ModelAttributeType.INTEGER,
				30);
		hasFieldAttribute(caseDefinition, "priority", DefinitionModelAttributes.CODE_LIST, ModelAttributeType.CODE_LIST,
				29);
		hasFieldAttribute(caseDefinition, "priority", DefinitionModelAttributes.DISPLAY_TYPE, ModelAttributeType.DISPLAY_TYPE,
				DisplayType.EDITABLE.toString());

		// Test multiValued attr
		hasFieldAttribute(caseDefinition, "references", DefinitionModelAttributes.MULTI_VALUED, ModelAttributeType.BOOLEAN,
				Boolean.TRUE);

		// Fetch Another branch
		modelResponse = modelManagementService.getModel("PR0001");
		ModelDefinition testProject = modelResponse.getDefinitions().get(0);

		// Check mandatory attributes
		hasFieldAttribute(testProject, "priority", DefinitionModelAttributes.MANDATORY, ModelAttributeType.BOOLEAN,
				true);
		// Inherited from entity.xml
		hasFieldAttribute(testProject, "title", DefinitionModelAttributes.MANDATORY, ModelAttributeType.BOOLEAN,
				true);
		// Not specified in the XML, should be false
		hasFieldAttribute(testProject, "checkbox", DefinitionModelAttributes.MANDATORY, ModelAttributeType.BOOLEAN,
				false);

		// Check labels and tooltip
		assertFieldLabels(testProject, "type", "en=Type", "bg=Тип");
		// Title has no label definition therefore should not have labels
		assertFieldLabels(testProject, "title");
		hasFieldAttribute(testProject, "description", DefinitionModelAttributes.TOOLTIP, ModelAttributeType.MULTI_LANG_STRING,
				new HashMap<>(getMap("en=Description tooltip")));
		// Missing tooltip
		hasNoFieldAttribute(testProject, "title", DefinitionModelAttributes.TOOLTIP);
	}

	@Test
	public void shouldProperlyConvertRegionModels() {
		withDefinitions("entity.xml", "genericProject.xml", "genericTestProject.xml", "PR0001.xml");
		ModelResponse modelResponse = modelManagementService.getModel("PR0001");

		ModelDefinition entity = modelResponse.getDefinitions().get(3);
		assertNoRegions(entity);

		ModelDefinition genericProject = modelResponse.getDefinitions().get(2);
		assertNoRegions(genericProject);

		ModelDefinition genericTestProject = modelResponse.getDefinitions().get(1);
		hasRegions(genericTestProject, "systemRelations");
		regionHasFields(genericTestProject, "systemRelations", "hasThumbnail", "emf:hasTemplate", "parentOf");

		ModelDefinition mainProject = modelResponse.getDefinitions().get(0);
		hasRegions(mainProject, "generalDetails", "specificDetails", "relationships", "timeAndEffort", "numericProperties");
		regionHasFields(mainProject, "generalDetails", "type", "title", "identifier", "description", "status");
		regionHasFields(mainProject, "specificDetails", "activityType", "examinationPurpose", "treatmentPurpose");
		regionHasFields(mainProject, "relationships", "ownedBy", "approvedBy", "hasAttachment", "references");
		regionHasFields(mainProject, "timeAndEffort", "plannedStartDate", "plannedEndDate", "actualStartDate", "actualEndDate");
		regionHasFields(mainProject, "numericProperties", "numericN3", "numericN5", "numericN6", "numericN10");

		// See that regions also has attributes
		hasRegionAttributes(genericTestProject, "systemRelations", DefinitionModelAttributes.DISPLAY_TYPE, ModelAttributeType.DISPLAY_TYPE,
				DisplayType.READ_ONLY.toString());
		hasRegionAttributes(mainProject, "generalDetails", DefinitionModelAttributes.ORDER, ModelAttributeType.INTEGER,
				10);
		// And labels
		hasRegionLabels(mainProject, "generalDetails", "en=General details");
	}

	@Test
	public void shouldMergeDuplicatedFields() {
		withDefinitions("PR0001.xml");
		ModelResponse modelResponse = modelManagementService.getModel("PR0001");

		ModelDefinition definition = modelResponse.getDefinitions().get(0);
		assertNoDuplicatedFields(definition);
		// The following two fields are defined as system but then overridden in a region with different display types - verify they are merged
		hasFieldAttribute(definition, "type", DefinitionModelAttributes.DISPLAY_TYPE, ModelAttributeType.DISPLAY_TYPE,
				DisplayType.READ_ONLY.toString());
		hasFieldAttribute(definition, "title", DefinitionModelAttributes.DISPLAY_TYPE, ModelAttributeType.DISPLAY_TYPE,
				DisplayType.EDITABLE.toString());
	}

	@Test
	public void shouldNotConvertFieldsWithoutUri() {
		withDefinitions("entity.xml", "genericProject.xml");

		ModelResponse modelResponse = modelManagementService.getModel("genericProject");
		ModelDefinition definition = modelResponse.getDefinitions().get(0);
		isMissingFields(definition, "default_header", "compact_header", "breadcrumb_header", "tooltip_header");
	}

	@Test
	public void shouldSelectModelResponse() {
		withDefinitions("entity.xml", "genericCase.xml", "genericProject.xml", "genericTestCase.xml", "genericTestProject.xml",
				"CS0001.xml", "PR0001.xml");

		// Selecting a definition model
		ModelResponse modelResponse = modelManagementService.getModel("PR0001");

		List<ModelDefinition> definitions = modelResponse.getDefinitions();
		assertEquals(4, definitions.size());

		assertDefinitionModel(definitions.get(0), "PR0001", EMF.PROJECT, "genericTestProject", false);
		assertModelNodeLabels(definitions.get(0), "en=Main Project", "bg=Основен проект");

		assertDefinitionModel(definitions.get(1), "genericTestProject", EMF.PROJECT, "genericProject", true);
		assertModelNodeLabels(definitions.get(1), "en=genericTestProject");

		assertDefinitionModel(definitions.get(2), "genericProject", EMF.PROJECT, "entity", true);
		assertModelNodeLabels(definitions.get(2), "en=genericProject");

		assertDefinitionModel(definitions.get(3), "entity", Proton.ENTITY, null, true);
		assertModelNodeLabels(definitions.get(3), "en=entity");

		List<ModelClass> classes = modelResponse.getClasses();
		assertEquals(3, classes.size());
		assertClassModel(classes.get(0), Proton.ENTITY, null);
		assertClassModel(classes.get(1), EMF.PROJECT, EMF.ACTIVITY);
		assertClassModel(classes.get(2), EMF.ACTIVITY, Proton.ENTITY);

		// Selecting a semantic model
		modelResponse = modelManagementService.getModel(EMF.CASE.toString());

		classes = modelResponse.getClasses();
		assertEquals(2, classes.size());
		assertClassModel(classes.get(0), EMF.CASE, Proton.ENTITY);
		assertClassModel(classes.get(1), Proton.ENTITY, null);
	}

	@Test
	public void shouldNotSelectMissingModels() {
		withDefinitions("entity.xml", "genericCase.xml", "genericTestCase.xml", "CS0001.xml");

		// CS0002 is not loaded
		ModelResponse modelResponse = modelManagementService.getModel("CS0002");
		assertEmptyResponse(modelResponse);
	}

	@Test
	public void shouldRecalculateModelsOnModelsImport() {
		withDefinitions("entity.xml");

		// Trigger calculation, genericCase should not be available as model yet
		ModelResponse modelResponse = modelManagementService.getModel("genericCase");
		assertEmptyResponse(modelResponse);

		withDefinitions("entity.xml", "genericCase.xml");

		eventService.fire(new ModelImportCompleted());

		// genericCase -> GEC10001
		modelResponse = modelManagementService.getModel("GEC10001");
		assertDefinitionModel(modelResponse.getDefinitions().get(0), "GEC10001", EMF.CASE, "entity", true);
	}

	@Test
	public void shouldRecalculateOnModelsImportOnlyIfAlreadyCalculated() {
		withDefinitions("entity.xml");

		eventService.fire(new ModelImportCompleted());

		verify(semanticDefinitionService, never()).getRootClass();
	}

	@Test
	public void shouldRecalculateModelsOnCodelistReset() {
		withDefinitions("entity.xml", "genericCase.xml", "genericTestCase.xml", "CS0001.xml", "CS0002.xml");

		eventService.fire(new ResetCodelistEvent());

		ModelResponse modelResponse = modelManagementService.getModel("CS0002");
		assertDefinitionModel(modelResponse.getDefinitions().get(0), "CS0002", EMF.CASE, "genericTestCase", false);
	}

	@Test
	public void shouldRecalculateOnCodelistResetOnlyIfAlreadyCalculated() {
		withDefinitions("entity.xml");

		eventService.fire(new ResetCodelistEvent());

		verify(semanticDefinitionService, never()).getRootClass();
	}

	@Test
	public void shouldProduceModelsHierarchy() {
		withDefinitions("entity.xml", "genericCase.xml", "genericProject.xml", "genericTestCase.xml", "genericTestProject.xml",
				"CS0001.xml", "PR0001.xml");

		List<ModelHierarchyClass> modelHierarchy = modelManagementService.getModelHierarchy();
		assertNotNull(modelHierarchy);

		hasHierarchyClass(modelHierarchy, Proton.ENTITY.toString(), null);
		hasHierarchyClass(modelHierarchy, EMF.CASE.toString(), Proton.ENTITY.toString());
		hasHierarchyClass(modelHierarchy, EMF.ACTIVITY.toString(), Proton.ENTITY.toString());
		hasHierarchyClass(modelHierarchy, EMF.PROJECT.toString(), EMF.ACTIVITY.toString());

		hasHierarchyDefinition(modelHierarchy, Proton.ENTITY.toString(), "entity", true);
		hasHierarchyDefinition(modelHierarchy, EMF.CASE.toString(), "GEC10001", true);
		hasHierarchyDefinition(modelHierarchy, EMF.CASE.toString(), "genericTestCase", true);
		hasHierarchyDefinition(modelHierarchy, EMF.CASE.toString(), "CS0001", false);
		hasHierarchyDefinition(modelHierarchy, EMF.PROJECT.toString(), "genericProject", true);
		hasHierarchyDefinition(modelHierarchy, EMF.PROJECT.toString(), "genericTestProject", true);
		hasHierarchyDefinition(modelHierarchy, EMF.PROJECT.toString(), "PR0001", false);
	}

	@Test
	public void shouldProduceModelsMetaInformation() {
		ModelsMetaInfo metaInfo = modelManagementService.getMetaInfo();
		assertNotNull(metaInfo);

		verifyCollection(metaInfo.getSemantics());
		verifyCollection(metaInfo.getProperties());
		verifyCollection(metaInfo.getDefinitions());
		verifyCollection(metaInfo.getFields());
		verifyCollection(metaInfo.getRegions());

		hasMetaInfo(metaInfo.getSemantics(), "title", DCTERMS.TITLE, "label", "", true);
		hasMetaInfoLabels(metaInfo.getSemantics(), "title", "en=Title");
		hasMetaInfo(metaInfo.getSemantics(), "createable", EMF.IS_CREATEABLE, "boolean", true, false);
		verifyMetaInfoOrder(metaInfo.getSemantics());

		hasMetaInfo(metaInfo.getProperties(), "label", RDFS.LABEL, "label", "", true);
		hasMetaInfoLabels(metaInfo.getProperties(), "label", "en=Label");
		hasMetaInfo(metaInfo.getProperties(), "definition", SKOS.DEFINITION, "multiLangString", "", false);
		verifyMetaInfoOrder(metaInfo.getProperties());

		hasMetaInfo(metaInfo.getDefinitions(), "identifier", null, "identifier", "", true);
		hasMetaInfoLabels(metaInfo.getDefinitions(), "identifier", "en=Identifier", "bg=Идентификатор");
		hasMetaInfo(metaInfo.getDefinitions(), "abstract", null, "boolean", true, false);
		hasMetaInfoLabels(metaInfo.getDefinitions(), "abstract", "en=Abstract", "bg=Абстрактна");
		verifyMetaInfoOrder(metaInfo.getDefinitions());
	}

	@Test
	public void shouldProduceFieldsMetaInformation() {
		ModelsMetaInfo metaInfo = modelManagementService.getMetaInfo();
		hasMetaInfo(metaInfo.getFields(), "name", null, "identifier", "", true);
		hasMetaInfo(metaInfo.getFields(), "type", null, "type", "", true);
		hasMetaInfo(metaInfo.getFields(), "uri", null, "uri", "", true);
		hasMetaInfo(metaInfo.getFields(), "value", null, "string", "", false);
		hasMetaInfo(metaInfo.getFields(), "order", null, "integer", 0, false);
		hasMetaInfo(metaInfo.getFields(), "codeList", null, "codeList", "", false);
		hasMetaInfoLabels(metaInfo.getFields(), "codeList", "en=Controlled vocabulary", "bg=Номенклатура");
		hasMetaInfo(metaInfo.getFields(), "displayType", null, "displayType", "HIDDEN", false);
		hasMetaInfo(metaInfo.getFields(), "previewEmpty", null, "boolean", false, false);
		hasMetaInfo(metaInfo.getFields(), "multiValued", null, "boolean", false, false);
		verifyMetaInfoOrder(metaInfo.getFields());
	}

	@Test
	public void shouldProduceRegionsMetaInformation() {
		ModelsMetaInfo metaInfo = modelManagementService.getMetaInfo();
		hasMetaInfo(metaInfo.getRegions(), "identifier", null, "identifier", "", true);
		hasMetaInfo(metaInfo.getRegions(), "label", null, "label", "", true);
		hasMetaInfo(metaInfo.getRegions(), "order", null, "integer", 0, false);
		hasMetaInfo(metaInfo.getRegions(), "displayType", null, "displayType", "EDITABLE", false);
		hasMetaInfoLabels(metaInfo.getRegions(), "displayType", "en=Display type", "bg=Визуализация");
		verifyMetaInfoOrder(metaInfo.getRegions());
	}

	@Test
	public void shouldDeleteExportedDefinitionsAfterModelCalculation() {
		File entityXml = loadFile("entity.xml");
		definitionImportServiceStub.withDefinition(entityXml);
		modelManagementService.getModelHierarchy();
		assertFalse(entityXml.exists());
	}

	private static void assertClassModel(ModelClass modelClass, IRI iri, IRI parentIri) {
		assertEquals(iri.toString(), modelClass.getId());
		if (parentIri != null) {
			assertEquals(parentIri.toString(), modelClass.getParent());
		} else {
			assertNull(modelClass.getParent());
		}
	}

	private static void assertDefinitionModel(ModelDefinition definition, String id, IRI rdfType, String parentId, boolean isAbstract) {
		assertEquals(id, definition.getId());
		assertEquals(rdfType.toString(), definition.getRdfType());
		assertEquals(parentId, definition.getParent());
		assertEquals(isAbstract, definition.isAbstract());
	}

	private static void assertFieldLabels(ModelDefinition definition, String fieldName, String... expectedLabels) {
		Optional<ModelField> fieldByName = definition.getFieldByName(fieldName);
		assertTrue(fieldByName.isPresent());
		assertModelNodeLabels(fieldByName.get(), expectedLabels);
	}

	private static void assertModelNodeLabels(ModelNode modelNode, String... expectedLabels) {
		Map<String, Serializable> expectedMap = getMap(expectedLabels);
		assertEquals(expectedMap.size(), modelNode.getLabels().size());
		expectedMap.forEach((lang, expectedDescription) -> assertEquals(expectedDescription, modelNode.getLabels().get(lang)));
	}

	private static void assertModelProperties(List<ModelProperty> properties, IRI... expectedIris) {
		List<String> expected = Arrays.stream(expectedIris).map(IRI::toString).collect(Collectors.toList());
		assertEquals(expected.size(), properties.size());
		properties.forEach(property -> assertTrue(expected.contains(property.getId())));
	}

	private static void hasFields(ModelDefinition definition, String... expectedFields) {
		List<String> fieldNames = definition.getFields().stream().map(ModelField::getId).collect(Collectors.toList());
		Arrays.asList(expectedFields).forEach(expectedField -> assertTrue(fieldNames.contains(expectedField)));
	}

	private static void hasFieldAttribute(ModelDefinition definition, String fieldName, String attributeId, String attributeType,
			Serializable attributeValue) {
		Optional<ModelField> field = definition.getFieldByName(fieldName);
		assertTrue(field.isPresent());
		hasAttribute(field.get(), attributeId, attributeType, attributeValue);
	}

	private static void hasNoFieldAttribute(ModelDefinition definition, String fieldName, String attributeName) {
		Optional<ModelField> fieldByName = definition.getFieldByName(fieldName);
		assertTrue(fieldByName.isPresent());
		assertFalse(fieldByName.get().getAttribute(attributeName).isPresent());
	}

	private static void hasAttribute(ModelNode model, String attributeName, String attributeType, Serializable attributeValue) {
		Optional<ModelAttribute> attribute = model.getAttribute(attributeName);
		assertTrue(attribute.isPresent());
		assertEquals(attributeType, attribute.get().getType());
		assertEquals(attributeValue, attribute.get().getValue());
	}

	private static void hasAttribute(ModelNode model, IRI attribute, String attributeType, Serializable attributeValue) {
		hasAttribute(model, attribute.toString(), attributeType, attributeValue);
	}

	private static void isMissingFields(ModelDefinition definition, String... expectedMissingFields) {
		List<String> fieldNames = definition.getFields().stream().map(ModelField::getId).collect(Collectors.toList());
		Arrays.asList(expectedMissingFields).forEach(expectedMissingField -> assertFalse(fieldNames.contains(expectedMissingField)));
	}

	private static void assertNoDuplicatedFields(ModelDefinition definition) {
		List<String> fieldNames = definition.getFields().stream().map(AbstractModelNode::getId).collect(Collectors.toList());
		List<String> duplicatedFields = fieldNames.stream()
				.filter(f -> Collections.frequency(fieldNames, f) > 1)
				.distinct()
				.collect(Collectors.toList());
		if (duplicatedFields.size() > 0) {
			fail("Duplicated fields in " + definition.getId() + " > " + duplicatedFields.toString());
		}
	}

	private static void assertNoRegions(ModelDefinition definition) {
		assertTrue(definition.getRegions().isEmpty());
		definition.getFields().forEach(field -> assertNull(field.getRegionId()));
	}

	private static void hasRegions(ModelDefinition definition, String... regionIds) {
		List<String> presentRegions = definition.getRegions().stream().map(ModelRegion::getId).collect(Collectors.toList());
		Set<String> fieldsRegionIds = definition.getFields()
				.stream()
				.filter(field -> StringUtils.isNotBlank(field.getRegionId()))
				.map(ModelField::getRegionId)
				.collect(Collectors.toSet());

		Arrays.asList(regionIds).forEach(regionId -> assertTrue(presentRegions.contains(regionId) && fieldsRegionIds.contains(regionId)));
	}

	private static void regionHasFields(ModelDefinition definition, String regionName, String... fieldIds) {
		Optional<ModelRegion> regionByName = definition.getRegionByName(regionName);
		assertTrue(regionByName.isPresent());

		ModelRegion modelRegion = regionByName.get();
		Arrays.asList(fieldIds).forEach(fieldId -> assertTrue(modelRegion.getFields().contains(fieldId)));
	}

	private static void hasRegionAttributes(ModelDefinition definition, String regionName, String name, String type, Serializable value) {
		Optional<ModelRegion> regionByName = definition.getRegionByName(regionName);
		assertTrue(regionByName.isPresent());
		hasAttribute(regionByName.get(), name, type, value);
	}

	private static void hasRegionLabels(ModelDefinition definition, String regionName, String... expectedLabels) {
		Optional<ModelRegion> regionByName = definition.getRegionByName(regionName);
		assertTrue(regionByName.isPresent());
		assertModelNodeLabels(regionByName.get(), expectedLabels);
	}

	private static void hasHierarchyClass(List<ModelHierarchyClass> classes, String id, String parentId) {
		Optional<ModelHierarchyClass> hierarchyClass = classes.stream().filter(c -> c.getId().equals(id)).findFirst();
		assertTrue(hierarchyClass.isPresent());
		assertEquals(id, hierarchyClass.get().getId());
		assertEquals(parentId, hierarchyClass.get().getParentId());
	}

	private static void hasHierarchyDefinition(List<ModelHierarchyClass> classes, String classId, String definitionId, boolean isAbstract) {
		Optional<ModelHierarchyClass> hierarchyClass = classes.stream().filter(c -> c.getId().equals(classId)).findFirst();
		assertTrue(hierarchyClass.isPresent());
		Optional<ModelHierarchyDefinition> definition =
				hierarchyClass.get().getSubTypes().stream().filter(d -> d.getId().equals(definitionId)).findFirst();
		assertTrue(definition.isPresent());
		assertEquals(isAbstract, definition.get().isAbstract());
	}

	private static void verifyCollection(Collection collection) {
		assertNotNull(collection);
		assertFalse(collection.isEmpty());
	}

	private static void hasMetaInfo(List<ModelMetaInfo> infos, String id, IRI iri, String type, Object defaultValue, boolean mandatory) {
		Optional<ModelMetaInfo> found = infos.stream().filter(info -> id.equals(info.getId())).findFirst();
		assertTrue(found.isPresent());

		ModelMetaInfo modelMetaInfo = found.get();
		if (iri != null) {
			assertEquals(iri.toString(), modelMetaInfo.getUri());
		}
		assertEquals(type, modelMetaInfo.getType());
		assertEquals(defaultValue, modelMetaInfo.getDefaultValue());

		ModelMetaInfoValidation validationModel = modelMetaInfo.getValidationModel();
		assertNotNull(validationModel);
		assertEquals(mandatory, validationModel.isMandatory());
	}

	private static void hasMetaInfoLabels(List<ModelMetaInfo> infos, String id, String... labels) {
		Optional<ModelMetaInfo> found = infos.stream().filter(info -> id.equals(info.getId())).findFirst();
		assertTrue(found.isPresent());

		ModelMetaInfo modelMetaInfo = found.get();
		Map<String, String> labelsMap = getStringMap(labels);
		assertEquals(labelsMap, modelMetaInfo.getLabels());
	}

	private static void verifyMetaInfoOrder(List<ModelMetaInfo> metaInfoList) {
		for (int index = 0; index < metaInfoList.size(); index++) {
			assertEquals(index, metaInfoList.get(index).getOrder());
		}
	}

	private static void assertEmptyResponse(ModelResponse response) {
		assertTrue(response.getClasses().isEmpty());
		assertTrue(response.getDefinitions().isEmpty());
	}

	private static ClassInstance getClass(IRI iri, String... labels) {
		ClassInstance classInstance = new ClassInstance();
		classInstance.setId(iri.toString());

		classInstance.setSuperClasses(new ArrayList<>());
		classInstance.setSubClasses(new HashMap<>());
		classInstance.setFields(new HashMap<>());
		classInstance.add("description", iri.getLocalName() + " description");

		getStringMap(labels).forEach(classInstance::setLabel);

		return classInstance;
	}

	private static PropertyInstance getProperty(IRI iri, String... labels) {
		PropertyInstance propertyInstance = new PropertyInstance();
		propertyInstance.setId(iri.toString());

		getStringMap(labels).forEach(propertyInstance::setLabel);

		return propertyInstance;
	}

	private static CodeValue getValue(String id, String... descriptions) {
		CodeValue value = new CodeValue();
		value.setValue(id);
		Map<String, Serializable> descriptionsMap = getMap(descriptions);
		value.setProperties(descriptionsMap);
		return value;
	}

	private static Map<String, Serializable> getMap(String... keyValue) {
		if (keyValue == null) {
			return Collections.emptyMap();
		}
		return Arrays.stream(keyValue)
				.collect(Collectors.toMap(d -> d.substring(0, d.indexOf('=')), d -> d.substring(d.indexOf('=') + 1, d.length())));
	}

	private static Map<String, String> getStringMap(String... keyValue) {
		if (keyValue == null) {
			return Collections.emptyMap();
		}
		return Arrays.stream(keyValue)
				.collect(Collectors.toMap(d -> d.substring(0, d.indexOf('=')), d -> d.substring(d.indexOf('=') + 1, d.length())));
	}

	/**
	 * Stubs the import/export service with the provided definition file names. After the model calculation, the files will be deleted so
	 * if another set must be calculated this method must be invoked again with the full set of definitions to be exported.
	 *
	 * @param definitions file names of definition XMLs
	 */
	private void withDefinitions(String... definitions) {
		definitionImportServiceStub.clearDefinitions();
		Arrays.stream(definitions).forEach(definition -> definitionImportServiceStub.withDefinition(loadFile(definition)));
	}

	private File loadFile(String fileName) {
		try {
			// The exported files are deleted after models calculation -> need a copy
			File definitionFile = new File(getClass().getResource(fileName).toURI());
			File copy = new File(definitionFile.getPath() + ".copy");
			Files.copy(definitionFile.toPath(), copy.toPath(), StandardCopyOption.REPLACE_EXISTING);
			return copy;
		} catch (Exception e) {
			throw new IllegalArgumentException(e);
		}
	}

	private void withLabelDefinitionFor(String labelId, String... labels) {
		LabelImpl labelDefinition = new LabelImpl();
		labelDefinition.setLabels(getStringMap(labels));
		when(labelService.getLabel(eq(labelId))).thenReturn(labelDefinition);
	}

	private void stubNamespaceRegistry() {
		when(namespaceRegistryService.buildFullUri(anyString())).thenAnswer(invocation -> {
			String shortUri = invocation.getArgumentAt(0, String.class);
			String prefix = shortUri.substring(0, shortUri.indexOf(':'));

			if (StringUtils.isBlank(prefix)) {
				return shortUri;
			}

			String name = shortUri.substring(shortUri.indexOf(':') + 1);

			switch (prefix) {
				case EMF.PREFIX:
					return EMF.NAMESPACE + name;
				case Proton.PREFIX:
					return Proton.NAMESPACE + name;
				case DCTERMS.PREFIX:
					return DCTERMS.NAMESPACE + name;
				case SKOS.PREFIX:
					return SKOS.NAMESPACE + name;
				default:
					// Not recognized
					return shortUri;
			}
		});
	}
}
