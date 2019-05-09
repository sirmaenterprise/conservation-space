package com.sirma.sep.model.management;

import static com.sirma.sep.model.management.ModelAssert.assertAttributeNotPresent;
import static com.sirma.sep.model.management.ModelAssert.assertModelNodeLabels;
import static com.sirma.sep.model.management.ModelAssert.hasAttribute;
import static com.sirma.sep.model.management.ModelAssert.hasInheritedAttribute;
import static com.sirma.sep.model.management.ModelAssert.hasMultiLangStringAttribute;
import static com.sirma.sep.model.management.ModelsFakeCreator.createClass;
import static com.sirma.sep.model.management.ModelsFakeCreator.createCodeValue;
import static com.sirma.sep.model.management.ModelsFakeCreator.createMap;
import static com.sirma.sep.model.management.ModelsFakeCreator.createProperty;
import static com.sirma.sep.model.management.ModelsFakeCreator.createStringMap;
import static java.util.Collections.emptyMap;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

import java.io.File;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import javax.enterprise.inject.Produces;
import javax.inject.Inject;

import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.model.vocabulary.DC;
import org.eclipse.rdf4j.model.vocabulary.DCTERMS;
import org.eclipse.rdf4j.model.vocabulary.RDF;
import org.eclipse.rdf4j.model.vocabulary.RDFS;
import org.eclipse.rdf4j.model.vocabulary.XMLSchema;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;

import com.sirma.itt.seip.Executable;
import com.sirma.itt.seip.collections.CollectionUtils;
import com.sirma.itt.seip.definition.SemanticDefinitionService;
import com.sirma.itt.seip.definition.event.SemanticDefinitionsReloaded;
import com.sirma.itt.seip.domain.codelist.CodelistService;
import com.sirma.itt.seip.domain.codelist.event.ResetCodelistEvent;
import com.sirma.itt.seip.domain.definition.DisplayType;
import com.sirma.itt.seip.event.EmfEvent;
import com.sirma.itt.seip.io.ResourceLoadUtil;
import com.sirma.itt.semantic.model.vocabulary.EMF;
import com.sirma.itt.semantic.model.vocabulary.Proton;
import com.sirma.sep.model.ModelImportCompleted;
import com.sirma.sep.model.ModelNode;
import com.sirma.sep.model.management.definition.DefinitionModelAttributes;
import com.sirma.sep.model.management.definition.DefinitionModelControlParams;
import com.sirma.sep.model.management.deploy.definition.DefinitionModelDeployer;
import com.sirma.sep.model.management.hierarchy.ModelHierarchyClass;
import com.sirma.sep.model.management.hierarchy.ModelHierarchyDefinition;
import com.sirma.sep.model.management.meta.ModelMetaInfo;
import com.sirma.sep.model.management.meta.ModelMetaInfoRule;
import com.sirma.sep.model.management.meta.ModelMetaInfoValidation;
import com.sirma.sep.model.management.meta.ModelsMetaInfo;
import com.sirma.sep.model.management.operation.ModelChangeSetStatus;
import com.sirma.sep.model.management.persistence.ModelChangeEntity;
import com.sirma.sep.model.management.response.ModelResponse;

/**
 * Component tests for the model calculation & selection functionality in {@link ModelManagementServiceImpl} and related classes.
 * <p>
 * It stubs external for the module services such as {@link SemanticDefinitionService} and {@link CodelistService}.
 *
 * @author Mihail Radkov
 */
public class ModelManagementTest extends BaseModelManagementComponentTest {

	// Cannot place in the base..
	@Produces
	@Mock
	protected DefinitionModelDeployer deploymentService;

	@Inject
	private ModelManagementService modelManagementService;

	@Before
	public void before() {
		semanticDefinitionServiceStub
				.withRootClass(createClass(Proton.ENTITY))
				.withClass(createClass(EMF.CASE, "en=Case", "bg=Преписка"), Proton.ENTITY)
				.withClass(createClass(EMF.ACTIVITY), Proton.ENTITY)
				.withClass(createClass(EMF.PROJECT), EMF.ACTIVITY);

		// Properties in class instances are not mapped by URIs...
		semanticDefinitionServiceStub
				.withPropertyForClass("creator", "Case creator", EMF.CASE)
				.withPropertyForClass("createable", true, EMF.CASE)
				.withPropertyForClass("uploadable", false, EMF.CASE)
				.withPropertyForClass("searchable", true, EMF.CASE);

		semanticDefinitionServiceStub
				.withProperty(createProperty(RDF.TYPE))
				.withProperty(RDFS.LABEL).withLabels("en=Label").done()
				.withDataProperty(DCTERMS.TITLE).withLabels("en=Title").done()
				.withDataProperty(EMF.TYPE).done()
				.withDataProperty(EMF.STATUS)
				.withLabels("en=Status")
				.withProperty("domainClass", "ptop:Entity")
				.withProperty("rangeClass", "xsd:string")
				.done()
				.withObjectProperty(EMF.CREATED_BY).done();

		codelistServiceStub
				.withValueForList(createCodeValue("PR0001", 2, "en=Main Project", "bg=Основен проект"))
				.withValueForList(createCodeValue("PR00012", 3, "en=Project with action executions overrides", "bg=Основен проект"))
				.withValueForList(createCodeValue("CS0001", 4, "en=Main Case", "bg=Основна Преписка"))
				.withValueForList(createCodeValue("CS0002", 4, "en=Case with WF", "bg=Преписка създаване на работен процес"));

		withLabelDefinitionFor("model.management.definition.abstract", "en=Abstract", "bg=Абстрактна");
		withLabelDefinitionFor("model.management.definition.identifier", "en=Identifier", "bg=Идентификатор");
		withLabelDefinitionFor("model.management.field.codeList", "en=Controlled vocabulary", "bg=Номенклатура");
		withLabelDefinitionFor("model.management.region.displayType", "en=Display type", "bg=Визуализация");
		withLabelDefinitionFor("model.management.header.label", "en=Header", "bg=Хедър");

		// These are not in the definition XMLs so add them manually
		withLabelDefinitionFor("type.label", "en=Type", "bg=Тип");
		withLabelDefinitionFor("description.PR0001.tooltip", "en=Description tooltip");
		withLabelDefinitionFor("generalDetails.region.label", "en=General details");

		// actions type headers
		withLabelDefinitionFor("model.management.action.purpose", "en=Purpose", "bg=Предназначение");
		withLabelDefinitionFor("lock.PR0001.tooltip", "en=Lock tooltip");
		withLabelDefinitionFor("createSurvey.label", "en=Create survey", "bg=Създай проучване");
		withLabelDefinitionFor("complete.label", "en=Complete", "bg=Приключи");
		withLabelDefinitionFor("complete.tooltip", "en=Complete tooltip");

		// groups type headers
		withLabelDefinitionFor("model.management.actionGroup.order", "en=Order", "bg=Поредност");
		withLabelDefinitionFor("objectManagementChangeStatus.group.label", "en=Change status", "bg=Промяна на състояние");
		// description labels
		withLabelDefinitionFor("model.management.definition.abstract.description",
				"en=This is description for Abstract", "bg=Това е описание за Абстрактна");
		withLabelDefinitionFor("model.management.definition.identifier.description",
				"en=This is description for Identifier", "bg=Това е описание за Идентификатор");
		withLabelDefinitionFor("model.management.field.codeList.description",
				"en=This is description for Controlled vocabulary", "bg=Това е описание за Номенклатура");
	}

	@Test
	public void shouldProperlyConvertClassModel() {
		ModelResponse response = modelManagementService.getModel(EMF.CASE.toString());
		ModelClass caseClass = response.getClasses().get(0);
		assertClassModel(caseClass, EMF.CASE, Proton.ENTITY);
		assertModelNodeLabels(caseClass, "en=Case", "bg=Преписка");

		// Should have transferred case properties as attributes via the model meta information
		hasAttribute(caseClass, DC.CREATOR.toString(), "multiLangString", Collections.singletonMap("en", "Case creator"));
		hasAttribute(caseClass, EMF.IS_SEARCHABLE.toString(), "boolean", true);
		hasAttribute(caseClass, EMF.IS_CREATEABLE.toString(), "boolean", true);
		hasAttribute(caseClass, EMF.IS_UPLOADABLE.toString(), "boolean", false);
		assertTrue("The class should have been marked as deployed", caseClass.isDeployed());
	}

	@Test
	public void shouldProperlyConvertPropertyModel() {
		List<ModelProperty> properties = modelManagementService.getProperties();
		assertModelProperties(properties, RDF.TYPE, RDFS.LABEL, DCTERMS.TITLE, EMF.TYPE, EMF.STATUS, EMF.CREATED_BY);

		Optional<ModelProperty> status = properties.stream().filter(p -> p.getId().equals(EMF.STATUS.toString())).findFirst();
		assertTrue(status.isPresent());
		assertModelNodeLabels(status.get(), "en=Status");
		hasAttribute(status.get(), RDFS.DOMAIN, ModelAttributeType.URI,
				"http://www.ontotext.com/proton/protontop#Entity");
		hasAttribute(status.get(), RDFS.RANGE, ModelAttributeType.URI, XMLSchema.STRING.toString());
		assertTrue("The property should have been marked as deployed", status.get().isDeployed());

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
		assertTrue("The definition should have been marked as deployed", caseDefinition.isDeployed());

		hasAttribute(caseDefinition, DefinitionModelAttributes.ABSTRACT, ModelAttributeType.BOOLEAN, false);
		hasAttribute(caseDefinition, DefinitionModelAttributes.RDF_TYPE, ModelAttributeType.URI, EMF.CASE.toString());

		hasFields(caseDefinition, "type", "status", "description", "priority");
		isMissingFields(caseDefinition, "rdf:type", "title");
		assertTrue("The definition fields should have been marked as deployed",
				caseDefinition.getFields().stream().allMatch(ModelNode::isDeployed));

		// Some fields may not be declared in the current definition but they can be fetched by using the parent references
		assertTrue(caseDefinition.findFieldByName("rdf:type").isPresent());
		assertTrue(caseDefinition.findFieldByName("title").isPresent());

		hasFieldAttribute(caseDefinition, "status", DefinitionModelAttributes.DISPLAY_TYPE, ModelAttributeType.OPTION,
				DisplayType.READ_ONLY.toString());
		hasFieldAttribute(caseDefinition, "status", DefinitionModelAttributes.TYPE, ModelAttributeType.TYPE,
				"an..35");

		// type's display type is declared in entity -> test if it is fetched from it
		hasInheritedFieldAttribute(caseDefinition, "type", DefinitionModelAttributes.DISPLAY_TYPE, ModelAttributeType.OPTION,
				DisplayType.READ_ONLY.toString());
		hasFieldAttribute(caseDefinition, "type", DefinitionModelAttributes.TYPE, ModelAttributeType.TYPE,
				"an..180");

		hasFieldAttribute(caseDefinition, "priority", DefinitionModelAttributes.ORDER, ModelAttributeType.INTEGER,
				30);
		hasFieldAttribute(caseDefinition, "priority", DefinitionModelAttributes.CODE_LIST, ModelAttributeType.CODE_LIST,
				29);
		hasFieldAttribute(caseDefinition, "priority", DefinitionModelAttributes.DISPLAY_TYPE, ModelAttributeType.OPTION,
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
		// Not specified in the XML and not inherited, should be missing
		hasNoFieldAttribute(testProject, "checkbox", DefinitionModelAttributes.MANDATORY);

		// Check labels and tooltip
		assertFieldLabels(testProject, "type", "en=Type", "bg=Тип");
		// Title has no label definition therefore should not have labels
		assertFieldLabels(testProject, "title");
		hasFieldAttribute(testProject, "description", DefinitionModelAttributes.TOOLTIP, ModelAttributeType.MULTI_LANG_STRING,
				new HashMap<>(createMap("en=Description tooltip")));
		// Missing tooltip
		hasNoFieldAttribute(testProject, "title", DefinitionModelAttributes.TOOLTIP);
	}

	@Test
	public void convertShouldPreserveFieldsOrdering() {
		withDefinitions("entity.xml", "genericCase.xml", "genericTestCase.xml", "CS0002.xml");

		ModelResponse modelResponse = modelManagementService.getModel("CS0002");
		ModelDefinition cs0002 = modelResponse.getDefinitions().get(0);

		Set<String> expectedFields = new LinkedHashSet<>(Arrays.asList("type", "identifier", "emf:version", "status"));
		Set<String> actualFields = cs0002.getFieldsMap().keySet();

		assertSameElementsOrder(expectedFields, actualFields);
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
		assertTrue("The definition regions should have been marked as deployed",
				mainProject.getRegions().stream().allMatch(ModelNode::isDeployed));

		// See that regions also has attributes
		hasRegionAttribute(genericTestProject, "systemRelations", DefinitionModelAttributes.DISPLAY_TYPE, ModelAttributeType.OPTION,
				DisplayType.READ_ONLY.toString());
		hasRegionAttribute(mainProject, "generalDetails", DefinitionModelAttributes.ORDER, ModelAttributeType.INTEGER,
				10);
		// And labels
		hasRegionLabels(mainProject, "generalDetails", "en=General details");
	}

	@Test
	public void shouldProperlyConvertControlParams() {
		withDefinitions("entity.xml", "genericProject.xml", "genericTestProject.xml", "PR0001.xml");
		ModelResponse modelResponse = modelManagementService.getModel("PR0001");
		ModelDefinition mainProject = modelResponse.getDefinitions().get(0);

		ModelField description = getField(mainProject, "description").orElseThrow(null);
		assertNotNull(description);
		fieldHasControl(description, DefinitionModelControlParams.DEFAULT_VALUE_PATTERN);
		controlHasParams(description.getControlsMap().get(DefinitionModelControlParams.DEFAULT_VALUE_PATTERN), "template");

		ModelField approvedBy = getField(mainProject, "approvedBy").orElse(null);
		assertNotNull(approvedBy);
		fieldHasControl(approvedBy, "PICKER");
		controlHasParams(approvedBy.getControlsMap().get("PICKER"), "range", "restrictions");
		fieldHasControl(approvedBy, DefinitionModelControlParams.DEFAULT_VALUE_PATTERN);
		controlHasParams(approvedBy.getControlsMap().get(DefinitionModelControlParams.DEFAULT_VALUE_PATTERN), "template");
		
		ModelField richtextInfo = getField(mainProject, "richtextInfo").orElse(null);
		assertNotNull(richtextInfo);
		fieldHasControl(richtextInfo, "RICHTEXT");
		
		fieldHasControl(richtextInfo, DefinitionModelControlParams.DEFAULT_VALUE_PATTERN);
		controlHasParams(richtextInfo.getControlsMap().get(DefinitionModelControlParams.DEFAULT_VALUE_PATTERN), "template");
	}

	@Test
	public void shouldProperlyConvertHeaders() {
		withDefinitions("entity.xml", "genericProject.xml");

		// Re-stub for easier testing
		withLabelDefinitionFor("CASE_DEFAULT_HEADER", "en=Case default header");
		withLabelDefinitionFor("CASE_COMPACT_HEADER", "en=Case compact header");
		withLabelDefinitionFor("CASE_BREADCRUMB_HEADER", "en=Case breadcrumb header");
		withLabelDefinitionFor("CASE_TOOLTIP_HEADER", "en=Case tooltip header");
		withLabelDefinitionFor("CASE_DEFAULT_HEADER_IMU", "en=Case overridden default header");
		withLabelDefinitionFor("CASE_COMPACT_HEADER_IMU", "en=Case overridden compact header");
		withLabelDefinitionFor("PROJECT_DEFAULT_HEADER", "en=Project default header", "bg=Проект дефолтен хедър");
		withLabelDefinitionFor("PROJECT_COMPACT_HEADER", "en=Project compact header", "bg=Проект компактен хедър");
		withLabelDefinitionFor("PROJECT_BREADCRUMB_HEADER", "en=Project breadcrumb header", "bg=Проект бредкръмб хедър");
		withLabelDefinitionFor("PROJECT_TOOLTIP_HEADER", "en=Project tooltip header", "bg=Проект тултип хедър");

		ModelResponse modelResponse = modelManagementService.getModel("genericProject");

		ModelDefinition entity = modelResponse.getDefinitions().get(1);
		assertTrue(entity.getHeaders().isEmpty());

		Map<String, Map<String, String>> expectedHeaderLabels = new HashMap<>();
		expectedHeaderLabels.put(ModelHeaderType.DEFAULT,
				createStringMap("en=Project default header", "bg=Проект дефолтен хедър"));
		expectedHeaderLabels.put(ModelHeaderType.COMPACT,
				createStringMap("en=Project compact header", "bg=Проект компактен хедър"));
		expectedHeaderLabels.put(ModelHeaderType.BREADCRUMB,
				createStringMap("en=Project breadcrumb header", "bg=Проект бредкръмб хедър"));
		expectedHeaderLabels.put(ModelHeaderType.TOOLTIP,
				createStringMap("en=Project tooltip header", "bg=Проект тултип хедър"));

		ModelDefinition genericProject = modelResponse.getDefinitions().get(0);

		// if headers are present in model
		assertTrue(genericProject.getHeadersMap().keySet().containsAll(expectedHeaderLabels.keySet()));
		// All headers should be marked as deployed
		assertTrue(genericProject.getHeaders().stream().allMatch(ModelNode::isDeployed));

		// if headers have proper values
		expectedHeaderLabels.keySet().forEach(headerId -> {
			ModelHeader modelHeader = genericProject.getHeadersMap().get(headerId);
			assertEquals(headerId, modelHeader.getId());
			assertEquals(expectedHeaderLabels.get(headerId), modelHeader.getLabels());

			// Should have assigned their header type attribute
			Optional<ModelAttribute> headerType = modelHeader.getAttribute(DefinitionModelAttributes.HEADER_TYPE);
			assertTrue(headerType.isPresent());
			assertEquals(headerId, headerType.get().getValue());
		});
	}

	@Test
	public void shouldProperlyConvertActions() {
		withDefinitions("entity.xml", "genericProject.xml", "genericTestProject.xml", "PR0001.xml");
		ModelDefinition pr0001Model = modelManagementService.getModel("PR0001").getDefinitions().get(0);

		Map<String, ModelAction> actions = pr0001Model.getActionsMap();
		assertEquals(25, actions.size());

		// verification of action "lock"
		AbstractModelNode lock = actions.get("lock");
		assertNotNull(lock);
		hasAttribute(lock, DefinitionModelAttributes.ORDER, ModelAttributeType.INTEGER, 1);
		hasAttribute(lock, DefinitionModelAttributes.TOOLTIP_ID, ModelAttributeType.STRING, "lock.PR0001.tooltip");
		hasMultiLangStringAttribute(lock, DefinitionModelAttributes.TOOLTIP, "en=Lock tooltip");

		// verification of action "createSurvey"
		AbstractModelNode createSurvey = actions.get("createSurvey");
		assertNotNull(createSurvey);
		hasAttribute(createSurvey, DefinitionModelAttributes.ORDER, ModelAttributeType.INTEGER, 2);
		assertModelNodeLabels(createSurvey, "en=Create survey", "bg=Създай проучване");
		assertAttributeNotPresent(createSurvey, "eventId");
		hasAttribute(createSurvey, DefinitionModelAttributes.PURPOSE, ModelAttributeType.OPTION, "createInstance");
		hasAttribute(createSurvey, DefinitionModelAttributes.ACTION_PATH, ModelAttributeType.STRING, "/");
		hasAttribute(createSurvey, DefinitionModelAttributes.LABEL_ID, ModelAttributeType.STRING, "createSurvey.label");

		// verification of action "exportPDF"
		AbstractModelNode complete = actions.get("complete");
		assertNotNull(complete);
		hasAttribute(complete, DefinitionModelAttributes.ORDER, ModelAttributeType.INTEGER, 6);
		assertModelNodeLabels(complete, "en=Complete", "bg=Приключи");
		assertAttributeNotPresent(complete, "eventId");
		hasAttribute(complete, DefinitionModelAttributes.PURPOSE, ModelAttributeType.OPTION, "transition");
		hasAttribute(complete, DefinitionModelAttributes.TOOLTIP_ID, ModelAttributeType.STRING, "complete.tooltip");
		hasMultiLangStringAttribute(complete, DefinitionModelAttributes.TOOLTIP, "en=Complete tooltip");
		hasAttribute(complete, DefinitionModelAttributes.GROUP, ModelAttributeType.STRING, "objectManagementChangeStatus");
	}

	@Test
	public void shouldProperlyConvertActionExecutions() {
		withDefinitions("entity.xml", "genericProject.xml", "genericTestProject.xml", "PR0001.xml",
				"PR0001-action-executions-overrides.xml");
		// GIVEN:
		// Definitions are loaded.
		// Definition "PR0001" has:
		// 1. An action "lock" which have not action executions;
		// 2. An action "addAttachments" which have create relation action execution;
		// 3. An action "complete" which have execute script action execution.

		// Definition "PR00012" is child of "PR0001". It has:
		// 1. An action "lock" which overrides order attribute;
		// 2. An action "addAttachments" which overrides order attribute;
		// 3. An action "complete" which has configuration for execute script with different id than parent action.

		// WHEN:
		// Model definition is populated.
		ModelDefinition pr0001Model = modelManagementService.getModels().getDefinitions().get("PR00012");

		// THEN:
		Map<String, ModelAction> actions = pr0001Model.getActionsMap();

		// 1. The action lock order attribute has to override parents one and its type have to be "system"
		ModelAction lock = actions.get("lock");
		assertNotNull(lock);
		hasAttribute(lock, DefinitionModelAttributes.ORDER, ModelAttributeType.INTEGER, 2);
		assertTrue(lock.getActionExecutions().isEmpty());

		// 2. The action addAttachments order attribute has to override parents one and its type have to be "createRelation"
		ModelAction addAttachments = actions.get("addAttachments");
		assertNotNull(addAttachments);
		assertTrue(addAttachments.getActionExecutions().isEmpty());
		hasAttribute(addAttachments, DefinitionModelAttributes.ORDER, ModelAttributeType.INTEGER, 3);

		// 3. The complete lock order attribute has to override parents one and its type have to be "scriptExecution"
		ModelAction complete = actions.get("complete");
		assertNotNull(complete);
		Collection<ModelActionExecution> actionExecutions = complete.getActionExecutions();
		assertTrue(actionExecutions.size() == 1);
		hasAttribute(actionExecutions.iterator().next(), DefinitionModelAttributes.TYPE, ModelAttributeType.OPTION,
				ModelActionExecution.EXECUTE_SCRIPT_MODELING);
		hasAttribute(complete, DefinitionModelAttributes.ORDER, ModelAttributeType.INTEGER, 6);
		assertEquals(1, complete.getActionExecutions().size());

		ModelAttribute valueAttribute = complete.getActionExecutions().iterator().next().getAttribute("value").orElse(null);
		assertNotNull(valueAttribute);
		assertEquals("var script;", valueAttribute.getValue());
	}

	@Test
	public void shouldProperlyConvertActionGroups() {
		withDefinitions("entity.xml", "genericProject.xml", "genericTestProject.xml", "PR0001.xml");
		ModelDefinition pr0001Model = modelManagementService.getModel("PR0001").getDefinitions().get(0);

		Map<String, ModelActionGroup> actionGroups = pr0001Model.getActionGroupsMap();
		AbstractModelNode objectManagementChangeStatus = actionGroups.get("objectManagementChangeStatus");
		hasAttribute(objectManagementChangeStatus, DefinitionModelAttributes.ORDER, ModelAttributeType.INTEGER, 1);
		hasAttribute(objectManagementChangeStatus, DefinitionModelAttributes.PARENT, ModelAttributeType.STRING, "objectManagement");
		assertModelNodeLabels(objectManagementChangeStatus, "en=Change status", "bg=Промяна на състояние");
		hasAttribute(objectManagementChangeStatus, DefinitionModelAttributes.TYPE, ModelAttributeType.STRING, "menu");
		hasAttribute(objectManagementChangeStatus, DefinitionModelAttributes.LABEL_ID, ModelAttributeType.STRING,
				"objectManagementChangeStatus.group.label");
	}

	@Test
	public void shouldNotConvertAttributesThatAreNotOverridden() {
		withDefinitions("entity.xml", "genericProject.xml", "genericTestProject.xml", "PR0001.xml", "PR0002.xml");
		ModelResponse modelResponse = modelManagementService.getModel("PR0002");

		// These are defined in pr0001 but skipped in pr0002
		ModelDefinition pr0002 = modelResponse.getDefinitions().get(0);
		hasNoFieldAttribute(pr0002, "type", DefinitionModelAttributes.MANDATORY);
		hasNoFieldAttribute(pr0002, "type", DefinitionModelAttributes.MULTI_VALUED);
	}

	@Test
	public void shouldNotInheritRegionIdWhenMovedOutsideRegion() {
		withDefinitions("entity.xml", "testObject.xml", "CO1007.xml");
		ModelResponse modelResponse = modelManagementService.getModel("CO1007");

		ModelDefinition modelDefinition = modelResponse.getDefinitions().get(0);
		hasNoFieldAttribute(modelDefinition, "basePhysicalDimensions", DefinitionModelAttributes.REGION_ID);
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
		shouldRecalculateModelsOnEvent(new ModelImportCompleted());
	}

	@Test
	public void shouldRecalculateOnModelsImportOnlyIfAlreadyCalculated() {
		shouldNotRecalculateModelsUnlessAlreadyCalculated(new ModelImportCompleted());
	}

	@Test
	public void shouldIncludeFailingNonDeployedChangesOnRecalculation_whenDataCollisionDetected() throws InterruptedException {
		// what to load initially
		withDefinitions("entity.xml");

		String selector = "definition=entity/field=status/attribute=displayType";
		// trigger initial calculation
		Models model = modelManagementService.getModels();
		ModelAttribute attribute = (ModelAttribute) model.select(selector);
		assertEquals("READ_ONLY", attribute.getValue());

		// add non deployed change
		ModelChangeEntity entity = new ModelChangeEntity();
		entity.setAppliedVersion(1L);
		entity.setAppliedOn(new Date());
		entity.setStatus(ModelChangeSetStatus.APPLIED.toString());
		entity.setChangeData(ResourceLoadUtil.loadResource(getClass(), "failing-change-entity-attribute.json"));
		ModelChangeEntity updatedEntity = dbDao.saveOrUpdate(entity);

		eventService.fire(new ModelImportCompleted());

		model = modelManagementService.getModels();
		attribute = (ModelAttribute) model.select(selector);
		assertEquals("EDITABLE", attribute.getValue());
		assertNull(updatedEntity.getFailedOn());
		assertEquals(ModelChangeSetStatus.APPLIED.toString(), updatedEntity.getStatus());
		assertNotNull(updatedEntity.getStatusMessage());
	}

	@Test
	public void shouldIncludeNonDeployedChangesOnRecalculation() throws InterruptedException {
		// what to load initially
		withDefinitions("entity.xml");

		String selector = "definition=entity/field=status/attribute=displayType";
		// trigger initial calculation
		Models model = modelManagementService.getModels();
		ModelAttribute attribute = (ModelAttribute) model.select(selector);
		assertEquals("READ_ONLY", attribute.getValue());

		// add non deployed change
		ModelChangeEntity entity = new ModelChangeEntity();
		entity.setAppliedVersion(1L);
		entity.setAppliedOn(new Date());
		entity.setStatus(ModelChangeSetStatus.APPLIED.toString());
		entity.setChangeData(ResourceLoadUtil.loadResource(getClass(), "change-entity-attribute.json"));
		dbDao.saveOrUpdate(entity);

		eventService.fire(new ModelImportCompleted());

		model = modelManagementService.getModels();
		attribute = (ModelAttribute) model.select(selector);
		assertEquals("EDITABLE", attribute.getValue());
	}

	@Test
	public void shouldRecalculateModelsOnCodelistReset() {
		shouldRecalculateModelsOnEvent(new ResetCodelistEvent());
	}

	@Test
	public void shouldRecalculateOnCodelistResetOnlyIfAlreadyCalculated() {
		shouldNotRecalculateModelsUnlessAlreadyCalculated(new ResetCodelistEvent());
	}

	@Test
	public void shouldRecalculateModelsOnSemanticDefinitionReset() {
		shouldRecalculateModelsOnEvent(new SemanticDefinitionsReloaded());
	}

	@Test
	public void shouldRecalculateOnSemanticDefinitionResetOnlyIfAlreadyCalculated() {
		shouldNotRecalculateModelsUnlessAlreadyCalculated(new SemanticDefinitionsReloaded());
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

		verifyCollection(metaInfo.getSemantics(),
				metaInfo.getProperties(),
				metaInfo.getDefinitions(),
				metaInfo.getFields(),
				metaInfo.getRegions(),
				metaInfo.getHeaders(),
				metaInfo.getActions(),
				metaInfo.getActionGroups());

		hasMetaInfo(metaInfo.getSemantics(), "title", DCTERMS.TITLE, "label", emptyMap(), true);
		hasMetaInfoLabels(metaInfo.getSemantics(), "title", "en=Title");
		hasMetaInfo(metaInfo.getSemantics(), "createable", EMF.IS_CREATEABLE, "boolean", false, false);
		verifyMetaInfoOrder(metaInfo.getSemantics());

		hasMetaInfo(metaInfo.getProperties(), "title", DCTERMS.TITLE, "label", emptyMap(), true);
		hasMetaInfoLabels(metaInfo.getProperties(), "title", "en=Title");
		hasMetaInfo(metaInfo.getProperties(), "definition", DCTERMS.DESCRIPTION, "multiLangString", emptyMap(), true);
		verifyMetaInfoOrder(metaInfo.getProperties());

		hasMetaInfo(metaInfo.getDefinitions(), "identifier", null, "identifier", "", true);
		hasMetaInfoLabels(metaInfo.getDefinitions(), "identifier", "en=Identifier", "bg=Идентификатор");
		hasMetaInfo(metaInfo.getDefinitions(), "abstract", null, "boolean", true, false);
		hasMetaInfoLabels(metaInfo.getDefinitions(), "abstract", "en=Abstract", "bg=Абстрактна");
		verifyMetaInfoOrder(metaInfo.getDefinitions());

		hasMetaInfoDescriptions(metaInfo.getDefinitions(), "abstract", "en=This is description for Abstract",
				"bg=Това е описание за Абстрактна");
		hasMetaInfoDescriptions(metaInfo.getDefinitions(), "identifier", "en=This is description for Identifier",
				"bg=Това е описание за Идентификатор");
		hasMetaInfoDescriptions(metaInfo.getFields(), "codeList", "en=This is description for Controlled vocabulary",
				"bg=Това е описание за Номенклатура");
	}

	@Test
	public void shouldProduceImmutableMetaInformation() {
		ModelsMetaInfo metaInfo = modelManagementService.getMetaInfo();
		assertTrue(metaInfo.isSealed());

		assertThrows(IllegalStateException.class, () -> metaInfo.setSemantics(Collections.emptyList()));
		assertThrows(IllegalStateException.class, () -> metaInfo.setProperties(Collections.emptyList()));
		assertThrows(IllegalStateException.class, () -> metaInfo.setDefinitions(Collections.emptyList()));
		assertThrows(IllegalStateException.class, () -> metaInfo.setFields(Collections.emptyList()));
		assertThrows(IllegalStateException.class, () -> metaInfo.setRegions(Collections.emptyList()));
	}

	@Test
	public void shouldProduceFieldsValidationRules() {
		ModelsMetaInfo metaInfo = modelManagementService.getMetaInfo();
		Optional<ModelMetaInfo> found = metaInfo.getFields().stream().filter(info -> "mandatory".equals(info.getId()))
				.findFirst();
		assertTrue(found.isPresent());
		ModelMetaInfo modelMetaInfo = found.get();
		ModelMetaInfoValidation validationModel = modelMetaInfo.getValidationModel();
		assertNotNull(validationModel);
		List<ModelMetaInfoRule> rules = validationModel.getRules();
		assertNotNull(rules);
		assertNotNull(rules.get(0).getValues());
		assertNotNull(rules.get(0).getExpressions());
		assertNotNull(rules.get(0).getErrorLabel());
	}

	@Test
	public void shouldProduceFieldsMetaInformation() {
		ModelsMetaInfo metaInfo = modelManagementService.getMetaInfo();

		Map<String, String> defaultValue = new HashMap<>();
		defaultValue.put("missing", "HIDDEN");
		defaultValue.put("create", "EDITABLE");

		hasMetaInfo(metaInfo.getFields(), "name", null, "identifier", "", true);
		hasMetaInfo(metaInfo.getFields(), "type", null, "type", "", true);
		hasMetaInfo(metaInfo.getFields(), "uri", null, "uri", "", true);
		hasMetaInfo(metaInfo.getFields(), "value", null, "value", "", false);
		hasMetaInfo(metaInfo.getFields(), "order", null, "integer", null, false);
		hasMetaInfo(metaInfo.getFields(), "codeList", null, "codeList", null, false);
		hasMetaInfoLabels(metaInfo.getFields(), "codeList", "en=Controlled vocabulary", "bg=Номенклатура");
		hasMetaInfo(metaInfo.getFields(), "displayType", null, ModelAttributeType.OPTION, defaultValue, false);
		hasMetaInfo(metaInfo.getFields(), "previewEmpty", null, "boolean", true, false);
		hasMetaInfo(metaInfo.getFields(), "multiValued", null, "boolean", false, false);
		verifyMetaInfoOrder(metaInfo.getFields());
	}

	@Test
	public void shouldProduceRegionsMetaInformation() {
		ModelsMetaInfo metaInfo = modelManagementService.getMetaInfo();

		Map<String, String> defaultValue = new HashMap<>();
		defaultValue.put("missing", "HIDDEN");
		defaultValue.put("create", "EDITABLE");
		
		hasMetaInfo(metaInfo.getRegions(), "identifier", null, "identifier", "", true);
		hasMetaInfo(metaInfo.getRegions(), "label", null, "label", emptyMap(), true);
		hasMetaInfo(metaInfo.getRegions(), "order", null, "integer", null, false);
		hasMetaInfo(metaInfo.getRegions(), "displayType", null, ModelAttributeType.OPTION, defaultValue, false);
		hasMetaInfoLabels(metaInfo.getRegions(), "displayType", "en=Display type", "bg=Визуализация");
		verifyMetaInfoOrder(metaInfo.getRegions());
	}

	@Test
	public void shouldProduceControlsMetaInformation() {
		ModelsMetaInfo metaInfo = modelManagementService.getMetaInfo();
		hasMetaInfo(metaInfo.getControls(), "id", null, "string", "", true);
	}

	@Test
	public void shouldProduceControlParamsMetaInformation() {
		ModelsMetaInfo metaInfo = modelManagementService.getMetaInfo();
		hasMetaInfo(metaInfo.getControlParams(), "id", null, "string", "", true);
		hasMetaInfo(metaInfo.getControlParams(), "type", null, "string", "", true);
		hasMetaInfo(metaInfo.getControlParams(), "name", null, "string", "", true);
		hasMetaInfo(metaInfo.getControlParams(), "value", null, "source", "", false);
	}

	@Test
	public void shouldProduceHeadersMetaInformation() {
		ModelsMetaInfo metaInfo = modelManagementService.getMetaInfo();

		hasMetaInfo(metaInfo.getHeaders(), DefinitionModelAttributes.LABEL_ID, null, ModelAttributeType.STRING, "", false);
		hasMetaInfo(metaInfo.getHeaders(), DefinitionModelAttributes.LABEL, null, ModelAttributeType.LABEL, new LinkedHashMap<>(), true);
		hasMetaInfo(metaInfo.getHeaders(), DefinitionModelAttributes.HEADER_TYPE, null, ModelAttributeType.OPTION, "", true);
		hasMetaInfoLabels(metaInfo.getHeaders(), DefinitionModelAttributes.LABEL, "en=Header", "bg=Хедър");

		verifyMetaInfoOrder(metaInfo.getHeaders());
	}

	@Test
	public void shouldProperlyLoadMetaInformationVisibility() {
		ModelsMetaInfo metaInfo = modelManagementService.getMetaInfo();
		Map<String, ModelMetaInfo> fieldsMapping = metaInfo.getFieldsMapping();
		// By default label & tooltip should be visible
		assertTrue(fieldsMapping.get(DefinitionModelAttributes.LABEL).isVisible());
		assertTrue(fieldsMapping.get(DefinitionModelAttributes.TOOLTIP).isVisible());
		// But their label & tooltip identifiers should be defined as not visible
		assertFalse(fieldsMapping.get(DefinitionModelAttributes.LABEL_ID).isVisible());
		assertFalse(fieldsMapping.get(DefinitionModelAttributes.TOOLTIP_ID).isVisible());
	}

	@Test
	public void shouldProduceActionsMetaInformation() {
		Collection<ModelMetaInfo> actions = modelManagementService.getMetaInfo().getActions();

		hasMetaInfo(actions, DefinitionModelAttributes.ID, null, ModelAttributeType.IDENTIFIER, "", true);
		hasMetaInfo(actions, DefinitionModelAttributes.LABEL, null, ModelAttributeType.LABEL, "", false);
		hasMetaInfo(actions, DefinitionModelAttributes.LABEL_ID, null, ModelAttributeType.STRING, "", false);
		hasMetaInfo(actions, DefinitionModelAttributes.TOOLTIP, null, ModelAttributeType.MULTI_LANG_STRING, "", false);
		hasMetaInfo(actions, DefinitionModelAttributes.TOOLTIP_ID, null, ModelAttributeType.STRING, "", false);
		hasMetaInfo(actions, DefinitionModelAttributes.PURPOSE, null, ModelAttributeType.OPTION, "", false);
		hasMetaInfo(actions, DefinitionModelAttributes.ORDER, null, ModelAttributeType.INTEGER, null, false);
		hasMetaInfo(actions, DefinitionModelAttributes.CONFIRMATION, null, ModelAttributeType.MULTI_LANG_STRING, "", false);
		hasMetaInfo(actions, DefinitionModelAttributes.ACTION_PATH, null, ModelAttributeType.STRING, "", false);
		hasMetaInfo(actions, DefinitionModelAttributes.GROUP, null, ModelAttributeType.STRING, "", false);
		hasMetaInfoLabels(actions, DefinitionModelAttributes.PURPOSE, "en=Purpose", "bg=Предназначение");
		verifyMetaInfoOrder(actions);
	}

	@Test
	public void shouldProduceActionExecutionMetaInformation() {
		Collection<ModelMetaInfo> actionExecutions = modelManagementService.getMetaInfo().getActionExecutions();

		hasMetaInfo(actionExecutions, "id", null, "identifier", "", true);
		hasMetaInfo(actionExecutions, "value", null, "source", "", false);
		hasMetaInfo(actionExecutions, "phase", null, "option", "after", true);
		hasMetaInfo(actionExecutions, "async", null, "boolean", "false", false);
	}

	@Test
	public void shouldProduceActionGroupsMetaInformation() {
		Collection<ModelMetaInfo> actionGroups = modelManagementService.getMetaInfo().getActionGroups();

		hasMetaInfo(actionGroups, "id", null, "identifier", "", true);
		hasMetaInfo(actionGroups, "label", null, "label", "", true);
		hasMetaInfo(actionGroups, "labelId", null, "string", "", false);
		hasMetaInfo(actionGroups, "parent", null, "string", "", false);
		hasMetaInfo(actionGroups, "order", null, "integer", null, false);
		hasMetaInfo(actionGroups, "type", null, "string", "", false);
		hasMetaInfoLabels(actionGroups, "order", "en=Order", "bg=Поредност");
		verifyMetaInfoOrder(actionGroups);
	}

	@Test
	public void shouldDeleteExportedDefinitionsAfterModelCalculation() {
		File entityXml = definitionImportServiceStub.withDefinition("entity.xml");
		modelManagementService.getModelHierarchy();
		assertFalse(entityXml.exists());
	}

	@Test
	public void shouldProperlyExtractTypeOption() {
		withDefinitions("entity.xml", "genericProject.xml", "genericTestProject.xml", "PR0001.xml");
		ModelResponse modelResponse = modelManagementService.getModel("PR0001");
		ModelDefinition mainProject = modelResponse.getDefinitions().get(0);

		ModelField field = getField(mainProject, "status").orElse(null);
		assertNotNull(field);
		ModelField parent = field.getParentReference();
		Optional<ModelAttribute> option = parent.getAttribute(DefinitionModelAttributes.TYPE_OPTION);
		assertEquals("CODELIST", option.get().getValue());

		field = getField(mainProject, "accumulatedEstimatedEffortHours").orElse(null);
		assertNotNull(field);
		parent = field.getParentReference();
		option = field.getAttribute(DefinitionModelAttributes.TYPE_OPTION);
		Optional<ModelAttribute> parentOption = parent.getAttribute(DefinitionModelAttributes.TYPE_OPTION);

		assertFalse(option.isPresent());
		assertEquals("NUMERIC_TYPE", parentOption.get().getValue());
	}
	
	private void shouldRecalculateModelsOnEvent(EmfEvent event) {
		withDefinitions("entity.xml", "genericCase.xml", "genericTestCase.xml", "CS0001.xml");

		// Trigger calculation, CS0002 should not be available as model yet
		ModelResponse emptyResponse = modelManagementService.getModel("CS0002");
		assertEmptyResponse(emptyResponse);

		withDefinitions("CS0002.xml");

		eventService.fire(event);

		ModelResponse modelResponse = modelManagementService.getModel("CS0002");
		assertDefinitionModel(modelResponse.getDefinitions().get(0), "CS0002", EMF.CASE, "genericTestCase", false);
	}

	private void shouldNotRecalculateModelsUnlessAlreadyCalculated(EmfEvent event) {
		withDefinitions("entity.xml");
		eventService.fire(event);
		verify(semanticDefinitionService, never()).getRootClass();
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
		// Without hierarchy resolving
		Optional<ModelField> field = getField(definition, fieldName);
		assertTrue(field.isPresent());
		assertModelNodeLabels(field.get(), expectedLabels);
	}

	private static void assertModelProperties(List<ModelProperty> properties, IRI... expectedIris) {
		List<String> expected = Arrays.stream(expectedIris).map(IRI::toString).collect(Collectors.toList());
		assertEquals(expected.size(), properties.size());
		properties.forEach(property -> assertTrue(expected.contains(property.getId())));
	}

	private static void hasFields(ModelDefinition definition, String... expectedFields) {
		Set<String> fieldNames = definition.getFieldsMap().keySet();
		Arrays.asList(expectedFields).forEach(expectedField -> assertTrue(fieldNames.contains(expectedField)));
	}

	private static void hasFieldAttribute(ModelDefinition definition, String fieldName, String attributeId, String attributeType,
			Serializable attributeValue) {
		// Without hierarchy resolving
		Optional<ModelField> field = getField(definition, fieldName);
		assertTrue(field.isPresent());
		hasAttribute(field.get(), attributeId, attributeType, attributeValue);
	}

	private static void hasInheritedFieldAttribute(ModelDefinition definition, String fieldName, String attributeId, String attributeType,
			Serializable attributeValue) {
		// WITH hierarchy resolving
		Optional<ModelField> field = definition.findFieldByName(fieldName);
		assertTrue(field.isPresent());
		hasInheritedAttribute(field.get(), attributeId, attributeType, attributeValue);
	}

	private static void hasNoFieldAttribute(ModelDefinition definition, String fieldName, String attributeName) {
		// Without hierarchy resolving for field and attribute
		Optional<ModelField> field = getField(definition, fieldName);
		assertTrue(field.isPresent());
		assertFalse(field.get().getAttribute(attributeName).isPresent());
	}

	private static void isMissingFields(ModelDefinition definition, String... expectedMissingFields) {
		Set<String> fieldNames = definition.getFieldsMap().keySet();
		Arrays.asList(expectedMissingFields).forEach(expectedMissingField -> assertFalse(fieldNames.contains(expectedMissingField)));
	}

	private static void assertNoRegions(ModelDefinition definition) {
		assertTrue(definition.getRegions().isEmpty());
		definition.getFields().forEach(field -> assertNull(field.findRegionId()));
	}

	private static void hasRegions(ModelDefinition definition, String... regionIds) {
		// Without hierarchy resolving for fields and regions
		Set<String> presentRegions = definition.getRegionsMap().keySet();

		Set<String> fieldsRegionIds = definition.getFields()
				.stream()
				.filter(ModelField::hasRegionId)
				.map(ModelField::getRegionId)
				.collect(Collectors.toSet());

		Arrays.asList(regionIds).forEach(regionId -> {
			assertTrue(presentRegions.contains(regionId) && fieldsRegionIds.contains(regionId));
			// Region ID should be attribute as well
			hasRegionAttribute(definition, regionId, DefinitionModelAttributes.IDENTIFIER, DefinitionModelAttributes.IDENTIFIER, regionId);
		});
	}

	private static void regionHasFields(ModelDefinition definition, String regionName, String... fieldIds) {
		// Without hierarchy resolving
		Optional<ModelRegion> regionByName = getRegion(definition, regionName);
		assertTrue(regionByName.isPresent());

		ModelRegion modelRegion = regionByName.get();
		Arrays.asList(fieldIds).forEach(fieldId -> assertTrue(modelRegion.getFields().contains(fieldId)));
	}

	private static void fieldHasControl(ModelField field, String controlName) {
		assertTrue(field.getControlsMap().containsKey(controlName));
	}

	private static void controlHasParams(ModelControl control, String... controlParamIds) {
		Arrays.asList(controlParamIds)
				.forEach(controlParamId -> assertTrue(control.getControlParamsMap().containsKey(controlParamId)));
	}

	private static void hasRegionAttribute(ModelDefinition definition, String regionName, String name, String type,
			Serializable value) {
		// Without hierarchy resolving
		Optional<ModelRegion> regionByName = getRegion(definition, regionName);
		assertTrue(regionByName.isPresent());
		hasAttribute(regionByName.get(), name, type, value);
	}

	private static void hasRegionLabels(ModelDefinition definition, String regionName, String... expectedLabels) {
		// Without hierarchy resolving
		Optional<ModelRegion> regionByName = getRegion(definition, regionName);
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

	private static void verifyCollection(Collection... collections) {
		Arrays.asList(collections).forEach(collection -> assertTrue(CollectionUtils.isNotEmpty(collection)));
	}

	private static void hasMetaInfo(Collection<ModelMetaInfo> infos, String id, IRI iri, String type, Object defaultValue,
			boolean mandatory) {
		Optional<ModelMetaInfo> found = getModelMetaInfoById(infos, id);
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

	private static void hasMetaInfoLabels(Collection<ModelMetaInfo> infos, String id, String... labels) {
		Optional<ModelMetaInfo> found = getModelMetaInfoById(infos, id);
		assertTrue(found.isPresent());

		ModelMetaInfo modelMetaInfo = found.get();
		Map<String, String> labelsMap = createStringMap(labels);
		assertEquals(labelsMap, modelMetaInfo.getLabels());
	}

	private static void hasMetaInfoDescriptions(Collection<ModelMetaInfo> infos, String id, String... descriptions) {
		Optional<ModelMetaInfo> found = getModelMetaInfoById(infos, id);
		assertTrue(found.isPresent());

		ModelMetaInfo modelMetaInfo = found.get();
		Map<String, String> descriptionsMap = createStringMap(descriptions);
		assertEquals(descriptionsMap, modelMetaInfo.getDescriptions());
	}

	private static Optional<ModelMetaInfo> getModelMetaInfoById(Collection<ModelMetaInfo> infos, String id) {
		return infos.stream().filter(info -> id.equals(info.getId())).findFirst();
	}

	private static void verifyMetaInfoOrder(Collection<ModelMetaInfo> metaInfo) {
		LinkedList<ModelMetaInfo> metaInfoList = new LinkedList<>(metaInfo);
		for (int index = 0; index < metaInfoList.size(); index++) {
			assertEquals(index, metaInfoList.get(index).getOrder());
		}
	}

	private static void assertEmptyResponse(ModelResponse response) {
		assertTrue(response.getClasses().isEmpty());
		assertTrue(response.getDefinitions().isEmpty());
	}

	private static void assertSameElementsOrder(Set<String> expected, Set<String> actual) {
		List<String> expectedList = new ArrayList<>(expected);
		List<String> actualList = new ArrayList<>(actual);
		for (int i = 0; i < expectedList.size(); i++) {
			assertEquals(expectedList.get(i), actualList.get(i));
		}
	}

	private <E extends Exception> void assertThrows(Class<E> expectedClass, Executable executable) {
		try {
			executable.execute();
			fail("Should blow with " + expectedClass.getSimpleName());
		} catch (Exception e) {
			assertEquals(expectedClass, e.getClass());
		}
	}

	private static Optional<ModelField> getField(ModelDefinition definition, String fieldName) {
		return Optional.ofNullable(definition.getFieldsMap().get(fieldName));
	}

	private static Optional<ModelRegion> getRegion(ModelDefinition definition, String regionName) {
		return Optional.ofNullable(definition.getRegionsMap().get(regionName));
	}
}
