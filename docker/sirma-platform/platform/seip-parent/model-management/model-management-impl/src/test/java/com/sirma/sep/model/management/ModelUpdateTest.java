package com.sirma.sep.model.management;

import static com.sirma.sep.model.management.ModelAssert.assertModelNodeLabels;
import static com.sirma.sep.model.management.ModelAssert.hasMultiLangStringAttribute;
import static com.sirma.sep.model.management.ModelsFakeCreator.createClass;
import static com.sirma.sep.model.management.ModelsFakeCreator.createCodeValue;
import static com.sirma.sep.model.management.ModelsFakeCreator.createProperty;
import static com.sirma.sep.model.management.ModelsFakeCreator.createStringMap;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyList;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

import javax.enterprise.inject.Produces;
import javax.inject.Inject;

import com.sirma.sep.model.management.exception.UpdateModelFailed;
import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.model.vocabulary.DCTERMS;
import org.eclipse.rdf4j.model.vocabulary.RDF;
import org.eclipse.rdf4j.model.vocabulary.XMLSchema;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;

import com.sirma.itt.seip.collections.CollectionUtils;
import com.sirma.itt.seip.domain.codelist.event.ResetCodelistEvent;
import com.sirma.itt.seip.domain.validation.ValidationReport;
import com.sirma.itt.semantic.model.vocabulary.EMF;
import com.sirma.itt.semantic.model.vocabulary.Proton;
import com.sirma.sep.model.management.definition.DefinitionModelAttributes;
import com.sirma.sep.model.management.definition.DefinitionModelControlParams;
import com.sirma.sep.model.management.deploy.DeploymentModels;
import com.sirma.sep.model.management.deploy.definition.DefinitionModelDeployer;
import com.sirma.sep.model.management.deploy.semantic.SemanticModelDeployer;
import com.sirma.sep.model.management.hierarchy.ModelHierarchyClass;
import com.sirma.sep.model.management.hierarchy.ModelHierarchyDefinition;
import com.sirma.sep.model.management.hierarchy.ModelHierarchyNode;
import com.sirma.sep.model.management.operation.ModelChangeSet;
import com.sirma.sep.model.management.operation.ModelChangeSetInfo;
import com.sirma.sep.model.management.request.ModelDeploymentRequest;
import com.sirma.sep.model.management.request.ModelUpdateRequest;
import com.sirma.sep.model.management.response.ModelResponse;
import com.sirma.sep.model.management.response.ModelUpdateResponse;

/**
 * Integration test for model persist and deploy with initial entry point of {@link ModelUpdater}
 *
 * @author <a href="mailto:borislav.bonev@sirma.bg">Borislav Bonev</a>
 * @since 02/08/2018
 */
public class ModelUpdateTest extends BaseModelDeploymentTest {

	@Produces
	@Mock
	private DefinitionModelDeployer definitionModelDeployer;

	@Produces
	@Mock
	private SemanticModelDeployer semanticModelDeployer;

	@Inject
	private ModelManagementService modelManagementService;

	@Before
	public void setUp() throws Exception {
		semanticDefinitionServiceStub.withRootClass(createClass(Proton.ENTITY))
				.withClass(EMF.CASE).withLabels("en=Case", "bg=Преписка").withParent(Proton.ENTITY)
				.withProperty("creator", "Case creator")
				.withProperty("createable", true)
				.withProperty("uploadable", false)
				.withProperty("searchable", true)
				.withProperty("allowInheritLibraryPermissions", false)
				.done()
				.withClass(EMF.ACTIVITY).withParent(Proton.ENTITY).done()
				.withClass(EMF.PROJECT).withParent(EMF.ACTIVITY).done()
				.withClass(EMF.DOCUMENT).withParent(Proton.ENTITY).done();

		semanticDefinitionServiceStub.withProperty(createProperty(EMF.OCR_CONTENT))
				.withProperty(DCTERMS.TITLE).withLabels("en=Title").done()
				.withProperty(createProperty(EMF.TYPE))
				.withProperty(EMF.STATUS)
				.withLabels("en=Status")
				.withProperty("propertyType", "")
				.withProperty("domainClass", "ptop:Entity")
				.withProperty("rangeClass", "xsd:string")
				.withProperty("isSearchable", true)
				.done()
				.withRelation(createProperty(EMF.CREATED_BY))
				.withProperty(EMF.OCR_CONTENT)
				.withLabels("en=OCR content")
				.withProperty("domainClass", "emf:Document")
				.withProperty("rangeClass", "xsd:string")
				.withProperty("isSearchable", true)
				.done();

		codelistServiceStub.withValueForList(createCodeValue("PR0001", 2, "en=Main Project", "bg=Основен проект"))
				.withValueForList(createCodeValue("CS0001", 4, "en=Main Case", "bg=Основна Преписка"))
				.withValueForList(createCodeValue("CS0002", 4, "en=Case with WF", "bg=Преписка създаване на работен процес"));

		withLabelDefinitionFor("model.management.abstract", "en=Abstract", "bg=Абстрактна");
		withLabelDefinitionFor("model.management.identifier", "en=Identifier", "bg=Идентификатор");

		withDefinitions("entity.xml", "PR0001.xml", "CS0001.xml", "CS0002.xml", "genericCase.xml", "genericProject.xml",
				"genericTestCase.xml", "genericTestProject.xml");

		when(definitionModelDeployer.validateDefinitions(anyList(), any(DeploymentModels.class))).thenReturn(ValidationReport.valid());
		when(semanticModelDeployer.validateSemanticClass(any(), any(DeploymentModels.class))).thenReturn(ValidationReport.valid());
		when(semanticModelDeployer.validateSemanticProperty(any(), any(DeploymentModels.class))).thenReturn(ValidationReport.valid());
	}

	@Test
	public void modelUpdate_shouldIncreaseModelVersion() throws Exception {
		ModelUpdateRequest changes = loadChanges("update-class-definition-property-request.json");

		long initialVersion = modelManagementService.getModels().getVersion();

		ModelUpdateResponse updateResponse = modelManagementService.updateModel(changes);

		verifyModelVersion(initialVersion, updateResponse);
	}

	@Test
	public void modelUpdate_shouldReturnChangesAfterTheCurrentVersion() throws Exception {
		ModelUpdateRequest changes = loadChanges("update-class-definition-property-request.json");

		ModelUpdateResponse updateResponse = modelManagementService.updateModel(changes);

		// PR0001 & Case should have two updates each
		expectChangesFor(updateResponse, "PR0001", "PR0001");
		expectChangesFor(updateResponse, EMF.STATUS.toString());
		expectChangesFor(updateResponse, EMF.CASE.toString(), EMF.CASE.toString());
	}

	@Test
	public void modelUpdate_shouldNotReturnChangesThatAreAlreadyKnown() throws Exception {
		ModelUpdateRequest changes = loadChanges("update-class-definition-property-request.json");

		ModelUpdateResponse updateResponse = modelManagementService.updateModel(changes);
		changes = new ModelUpdateRequest();
		changes.setModelVersion(updateResponse.getModelVersion());

		updateResponse = modelManagementService.updateModel(changes);

		assertTrue(updateResponse.getChangeSets().isEmpty());
		assertEquals("Model version should be the same if no changes are applied", changes.getModelVersion(),
				updateResponse.getModelVersion());
	}

	@Test
	public void modelUpdate_shouldNotProcessTheSameChangesTwice() throws Exception {
		ModelUpdateRequest changes = loadChanges("update-class-definition-property-request.json");

		ModelUpdateResponse updateResponse = modelManagementService.updateModel(changes);
		changes.setModelVersion(updateResponse.getModelVersion());

		updateResponse = modelManagementService.updateModel(changes);

		assertTrue(updateResponse.getChangeSets().isEmpty());
		assertEquals("Model version should be the same if no changes are applied", changes.getModelVersion(),
				updateResponse.getModelVersion());
	}

	@Test(expected = ModelValidationException.class)
	public void modelUpdate_shouldNotUpdateReadOnlyAttributes() throws Exception {
		ModelUpdateRequest changes = loadChanges("update-readOnly-attributes.json");

		modelManagementService.updateModel(changes);
	}

	@Test
	public void modelUpdate_shouldAllowEditingReadOnlyAttributessIfEmpty() {
		ModelUpdateRequest changes = loadChanges("update-readOnly-empty-attributes.json");

		modelManagementService.updateModel(changes);

		ModelProperty property = modelManagementService.getProperties()
				.stream()
				.filter(p -> p.getId().equals(EMF.OCR_CONTENT.toString()))
				.findFirst()
				.orElse(null);

		assertNotNull("Property should be present", property);

		ModelAttribute propertyType = property.getAttribute(RDF.TYPE.toString()).get();
		String expected = "http://ittruse.ittbg.com/ontology/enterpriseManagementFramework#DefinitionDataProperty";

		assertNotNull("Property should have a type attribute", propertyType);
		assertEquals("Wrong update value ", expected, propertyType.getValue());
	}
	
	@Test(expected = ModelValidationException.class)
	public void modelUpdate_shouldNotHandleNotSupportedAttributes() throws Exception {
		ModelUpdateRequest changes = loadChanges("deploy-unknown-class-attributes.json");

		modelManagementService.updateModel(changes);
	}

	@Test
	public void prepareForPublish_ShouldReturnAllModifiedNodes() {
		// add some changes to the model
		ModelUpdateRequest changes = loadChanges("update-class-definition-property-request.json");
		modelManagementService.updateModel(changes);

		DeploymentValidationReport deployRequest = modelManagementService.validateDeploymentCandidates();
		assertNotNull(deployRequest);
		assertFalse(deployRequest.getNodes().isEmpty());
		assertTrue(deployRequest.isValid());
		List<String> modifiedNodes = deployRequest.getNodes()
				.stream()
				.map(DeploymentValidationReport.ValidationReportEntry::getId)
				.sorted()
				.collect(Collectors.toList());
		// ptop:Entity comes as it's a domain of the modified status property
		assertEquals(Arrays.asList("PR0001", EMF.CASE.toString(), Proton.ENTITY.toString()), modifiedNodes);
	}

	@Test
	public void prepareForPublish_ShouldReturnFailingChangesNodes() {
		definitionImportServiceStub.reset();
		withDefinitions("PR0001-1-valid.xml");

		// add some valid changes to the model
		ModelUpdateRequest changes = loadChanges("update-definition-with-failing-changes.json");
		modelManagementService.updateModel(changes);

		definitionImportServiceStub.reset();
		withDefinitions("PR0001-1-invalid.xml");

		// trigger model reload to try to read the changes from the database
		eventService.fire(new ResetCodelistEvent());

		DeploymentValidationReport deployRequest = modelManagementService.validateDeploymentCandidates();
		assertNotNull(deployRequest);
		List<String> modifiedNodes = deployRequest.getNodes()
				.stream()
				.map(DeploymentValidationReport.ValidationReportEntry::getId)
				.collect(Collectors.toList());
		assertEquals(Collections.singletonList("PR00011"), modifiedNodes);
	}

	@Test
	public void publishChanges_shouldGenerateDeploymentRequestThatIncludesPropertyDomainClasses() {
		ModelUpdateRequest changes = loadChanges("update-class-definition-property-request.json");
		modelManagementService.updateModel(changes);

		DeploymentValidationReport publishRequest = modelManagementService.validateDeploymentCandidates();
		assertTrue(publishRequest.isValid());
		ModelDeploymentRequest deploymentRequest = new ModelDeploymentRequest().setModelsToDeploy(publishRequest.getNodes()
				.stream()
				.map(DeploymentValidationReport.ValidationReportEntry::getId)
				.collect(Collectors.toList())).setVersion(55555L);
		DeploymentValidationReport deployResponse = modelManagementService.deployChanges(deploymentRequest);
		assertTrue(deployResponse.isValid());

		verifyDeployedSemanticNodes(Collections.singletonList(EMF.STATUS.toString()),
				Arrays.asList(EMF.CASE.toString(), Proton.ENTITY.toString()));
		verifyDeployedDefinitions(Collections.singletonList("PR0001"));
	}

	@Test
	public void publishChanges_shouldGenerateDeploymentRequestForPropertyDomain() {
		ModelUpdateRequest changes = loadChanges("update-property-request.json");
		modelManagementService.updateModel(changes);

		DeploymentValidationReport publishRequest = modelManagementService.validateDeploymentCandidates();
		assertTrue(publishRequest.isValid());
		ModelDeploymentRequest deploymentRequest = new ModelDeploymentRequest().setModelsToDeploy(publishRequest.getNodes()
				.stream()
				.map(DeploymentValidationReport.ValidationReportEntry::getId)
				.collect(Collectors.toList())).setVersion(55555L);
		DeploymentValidationReport deployResponse = modelManagementService.deployChanges(deploymentRequest);
		assertTrue(deployResponse.isValid());

		verifyDeployedSemanticNodes(Arrays.asList(EMF.OCR_CONTENT.toString(), EMF.STATUS.toString()),
				Arrays.asList(Proton.ENTITY.toString(), EMF.DOCUMENT.toString()));
		verifyDeployedDefinitions(Collections.emptyList());
	}

	@Test
	public void publishChanges_shouldNotDeployChildClassesAndTheirDomainProperties() {
		ModelUpdateRequest changes = loadChanges("update-property-request.json");
		modelManagementService.updateModel(changes);

		DeploymentValidationReport publishRequest = modelManagementService.validateDeploymentCandidates();
		assertTrue(publishRequest.isValid());
		ModelDeploymentRequest deploymentRequest = new ModelDeploymentRequest().setModelsToDeploy(
				Collections.singletonList(Proton.ENTITY.toString())).setVersion(55555L);
		DeploymentValidationReport deployResponse = modelManagementService.deployChanges(deploymentRequest);
		assertTrue(deployResponse.isValid());

		verifyDeployedSemanticNodes(Collections.singletonList(EMF.STATUS.toString()), Collections.singletonList(Proton.ENTITY.toString()));
		verifyDeployedDefinitions(Collections.emptyList());
	}

	@Test
	public void modelUpdate_shouldUpdateHierarchyNodesLabels() {
		ModelUpdateRequest changes = loadChanges("update-class-definition-property-request.json");

		modelManagementService.updateModel(changes);

		// Updating the models nodes should reflect the changes in the hierarchy (map references)
		List<ModelHierarchyClass> modelHierarchy = modelManagementService.getModelHierarchy();
		expectHierarchyClassLabels(modelHierarchy, EMF.CASE, "en=Case edit 1", "bg=Преписка едит 1");
		expectHierarchyDefinitionLabels(modelHierarchy, EMF.PROJECT, "PR0001", "en=Main Project edit 1", "bg=Основен проект едит 1");
	}

	@Test
	public void modelUpdate_changesToASubFieldShouldAddItToARegionIfParentIsPartOfOne() {
		ModelUpdateRequest changes = loadChanges("update-project-child-field-in-a-region.json");

		modelManagementService.updateModel(changes);

		ModelResponse modelResponse = modelManagementService.getModel("PR0001");
		ModelDefinition definition = modelResponse.getDefinitions()
				.stream()
				.filter(def -> def.getId().equals("PR0001"))
				.findFirst()
				.orElse(null);
		assertNotNull("Definition PR0001 should be in the response", definition);
		ModelField hasThumbnail = definition.getFieldsMap().get("hasThumbnail");
		assertNotNull("Definition PR0001 should have hasThumbnail field", hasThumbnail);
		assertEquals("Wrong region ", "systemRelations", hasThumbnail.findRegionId());
		assertEquals("Wrong updated value ", "hasThumbnail updated", hasThumbnail.getLabels().get("en"));
		assertEquals("Wrong inherited value ", EMF.NAMESPACE + "hasThumbnail",
				hasThumbnail.findAttribute("uri").map(ModelAttribute::getValue).orElse(null));
	}

	@Test
	public void modelUpdate_shouldHandleRestoreAndEditChanges() {
		Path attributePath = Path.parsePath("definition=PR0001/field=hasWatcher/attribute=order");
		// prepare data
		ModelUpdateRequest changes = loadChanges("prepare-model-4-restore.json");
		modelManagementService.updateModel(changes);

		ModelAttribute order = (ModelAttribute) modelManagementService.getModels()
				.walk(attributePath);
		assertEquals(10, order.getValue());
		assertTrue("The path should resolve to ModelField", order.getContext() instanceof ModelField);
		assertEquals("The field should have been overridden in child definition", "PR0001",
				order.getContext().getContext().getId());
		assertTrue("The field hasWatcher should be part of at least one region",
				((ModelField) order.getContext()).getContext().getRegions()
						.stream()
						.flatMap(region -> region.getFields().stream())
						.anyMatch(field -> field.equals("hasWatcher")));

		// do restore of a single field
		changes = loadChanges("restore-and-edit-model-field.json");
		modelManagementService.updateModel(changes);

		ModelDefinition definition = (ModelDefinition) modelManagementService.getModels()
				.walk(attributePath.cutOffTail());

		ModelField modelField = definition.getFieldsMap().get("hasWatcher");
		assertTrue("Model field should be in the definition", modelField != null);
		Integer updatedOrder = modelField.getAttributeValue("order");
		assertEquals(new Integer(50), updatedOrder);
	}

	@Test
	public void modelUpdate_shouldRestoreModelAttribute() {
		Path attributePath = Path.parsePath("definition=PR0001/field=hasThumbnail/attribute=displayType");
		// prepare data
		ModelUpdateRequest changes = loadChanges("prepare-model-4-restore.json");
		modelManagementService.updateModel(changes);

		ModelAttribute displayType = (ModelAttribute) modelManagementService.getModels()
				.walk(attributePath);
		assertEquals("EDITABLE", displayType.getValue());

		// do restore of a single attribute
		changes = loadChanges("restore-model-attribute.json");
		modelManagementService.updateModel(changes);

		ModelDefinition definition = (ModelDefinition) modelManagementService.getModels()
				.walk(attributePath.cutOffTail());

		Optional<ModelField> modelField = definition.getFields()
				.stream()
				.filter(field -> "hasThumbnail".equals(field.getId()))
				.findFirst();
		assertTrue("Model field should still be in the definition", modelField.isPresent());
		ModelField field = modelField.get();
		assertFalse("The field should not have an attribute displayType", field.hasAttribute("displayType"));
	}

	@Test
	public void modelUpdate_shouldRestoreModelAttributeAndContainingNodeIfNoLongerHaveVisibleAttributes() {
		Path attributePath = Path.parsePath("definition=PR0001/field=partOf/attribute=displayType");
		// prepare data
		ModelUpdateRequest changes = loadChanges("prepare-model-4-attribute-and-node-restore.json");
		modelManagementService.updateModel(changes);

		ModelAttribute displayType = (ModelAttribute) modelManagementService.getModels()
				.walk(attributePath);
		assertEquals("EDITABLE", displayType.getValue());

		// do restore of a single attribute
		changes = loadChanges("restore-model-attribute-and-field.json");
		modelManagementService.updateModel(changes);

		ModelDefinition definition = (ModelDefinition) modelManagementService.getModels()
				.walk(attributePath.cutOffTail());

		ModelField modelField = definition.getFieldsMap().get("hasThumbnail");
		assertNull("Model field should not be in the definition", modelField);
	}

	@Test
	public void modelUpdate_shouldRestoreModelAttribute_shouldRemoveTheModelFieldIfLastRestoredAttribute() {
		Path attributePath = Path.parsePath("definition=PR0001/field=hasWatcher/attribute=order");
		// prepare data
		ModelUpdateRequest changes = loadChanges("prepare-model-4-restore.json");
		modelManagementService.updateModel(changes);

		ModelAttribute order = (ModelAttribute) modelManagementService.getModels()
				.walk(attributePath);
		assertEquals(10, order.getValue());

		// do restore of a single attribute
		changes = loadChanges("restore-last-model-attribute.json");
		modelManagementService.updateModel(changes);

		ModelDefinition definition = (ModelDefinition) modelManagementService.getModels()
				.walk(attributePath.cutOffTail());

		ModelField modelField = definition.getFieldsMap().get("hasWatcher");
		assertFalse("Model field should not be in the definition anymore", modelField != null);
	}

	@Test
	public void modelUpdate_shouldRestoreModelAttribute_shouldRemoveTheModelRegionIfLastRestoredAttribute() {
		Path regionPath = Path.parsePath("definition=PR0001/region=systemRelations");
		// prepare data
		ModelUpdateRequest changes = loadChanges("prepare-model-4-restore.json");
		modelManagementService.updateModel(changes);

		// do restore of a single attribute
		changes = loadChanges("restore-last-region-model-attribute.json");
		modelManagementService.updateModel(changes);

		ModelDefinition definition = (ModelDefinition) modelManagementService.getModels()
				.walk(regionPath.cutOffTail());

		assertFalse("Model region should still not be in the definition anymore",
				definition.getRegionsMap().containsKey("systemRelations"));
	}

	@Test
	public void modelUpdate_shouldRestoreModelField() {
		Path attributePath = Path.parsePath("definition=PR0001/field=hasWatcher/attribute=order");
		// prepare data
		ModelUpdateRequest changes = loadChanges("prepare-model-4-restore.json");
		modelManagementService.updateModel(changes);

		ModelAttribute order = (ModelAttribute) modelManagementService.getModels()
				.walk(attributePath);
		assertEquals(10, order.getValue());
		assertTrue("The path should resolve to ModelField", order.getContext() instanceof ModelField);
		assertEquals("The field should have been overridden in child definition", "PR0001",
				order.getContext().getContext().getId());
		assertTrue("The field hasWatcher should be part of at least one region",
				((ModelField) order.getContext()).getContext().getRegions()
						.stream()
						.flatMap(region -> region.getFields().stream())
						.anyMatch(field -> field.equals("hasWatcher")));

		// do restore of a single field
		changes = loadChanges("restore-model-field.json");
		modelManagementService.updateModel(changes);

		ModelDefinition definition = (ModelDefinition) modelManagementService.getModels()
				.walk(attributePath.cutOffTail());

		assertFalse("Model field should not be in the definition any more", definition.getFieldsMap().containsKey("hasWatcher"));
		assertTrue("The field hasWatcher should not be part of any region", definition.getRegions()
				.stream()
				.flatMap(region -> region.getFields().stream())
				.noneMatch(field -> field.equals("hasWatcher")));
	}

	@Test
	public void modelUpdate_shouldRestoreModelAction() {
		Path attributePath = Path.parsePath("definition=PR0001/action=lock/attribute=order");
		// prepare data
		ModelUpdateRequest changes = loadChanges("prepare-model-4-restore.json");
		modelManagementService.updateModel(changes);

		ModelAttribute order = (ModelAttribute) modelManagementService.getModels()
				.walk(attributePath);
		assertEquals(2, order.getValue());
		assertTrue("The path should resolve to ModelAction", order.getContext() instanceof ModelAction);
		assertEquals("The action should have been overridden in child definition", "PR0001",
				order.getContext().getContext().getId());

		// do restore of a single action
		changes = loadChanges("restore-model-action.json");
		modelManagementService.updateModel(changes);

		ModelDefinition definition = (ModelDefinition) modelManagementService.getModels()
				.walk(attributePath.cutOffTail());

		assertFalse("Model action should not be in the definition any more", definition.getActionsMap().containsKey("lock"));
	}

	@Test
	public void modelUpdate_shouldRestoreModelActionExecution() {
		Path attributePath = Path.parsePath("definition=PR0001/action=complete/actionExecution=sendMail/attribute=phase");

		ModelUpdateRequest changes = loadChanges("prepare-model-4-restore.json");
		modelManagementService.updateModel(changes);

		ModelAttribute phase = (ModelAttribute) modelManagementService.getModels()
				.walk(attributePath);
		assertEquals("before", phase.getValue());
		assertTrue("The path should resolve to ModelAction", phase.getContext() instanceof ModelActionExecution);
		assertEquals("The action should have been overridden in child definition", "PR0001",
					 phase.getContext().getContext().getContext().getId());

		// do restore of a single action
		changes = loadChanges("restore-model-action-execution.json");
		modelManagementService.updateModel(changes);

		ModelAction action = (ModelAction) modelManagementService.getModels()
				.walk(Path.parsePath("definition=PR0001/action=complete"));
		assertNull("Model action should not be in the definition any more", action.getActionExecution("sendMail"));
	}

	@Test
	public void modelUpdate_shouldRestoreModelActionGroup() {
		Path attributePath = Path.parsePath("definition=PR0001/actionGroup=objectManagement/attribute=order");
		// prepare data
		ModelUpdateRequest changes = loadChanges("prepare-model-4-restore.json");
		modelManagementService.updateModel(changes);

		ModelAttribute order = (ModelAttribute) modelManagementService.getModels()
				.walk(attributePath);
		assertEquals(5, order.getValue());
		assertTrue("The path should resolve to ModelActionGroup", order.getContext() instanceof ModelActionGroup);
		assertEquals("The action group should have been overridden in child definition", "PR0001",
				order.getContext().getContext().getId());

		// do restore of a single action group
		changes = loadChanges("restore-model-actionGroup.json");
		modelManagementService.updateModel(changes);

		ModelDefinition definition = (ModelDefinition) modelManagementService.getModels()
				.walk(attributePath.cutOffTail());

		assertFalse("Model action group should not be in the definition any more",
				definition.getActionGroupsMap().containsKey("objectManagement"));
	}

	@Test
	public void modelUpdate_shouldRestoreModelRegion() {
		Path regionPath = Path.parsePath("definition=PR0001/region=systemRelations");
		Path orderPath = Path.parsePath("definition=PR0001/region=systemRelations/attribute=order");
		// prepare data
		ModelUpdateRequest changes = loadChanges("prepare-model-4-restore.json");
		modelManagementService.updateModel(changes);

		Models models = modelManagementService.getModels();
		ModelRegion systemRelations = (ModelRegion) models.walk(regionPath);
		assertEquals("The region systemRelations should have 2 overridden fields",
				new LinkedHashSet<>(Arrays.asList("hasThumbnail", "hasWatcher")), systemRelations.getFields());
		assertEquals("The region should have been overridden in child definition", "PR0001",
				systemRelations.getContext().getId());
		ModelAttribute order = (ModelAttribute) models.walk(orderPath);
		assertEquals("The order property should be overridden in the child definition", 20, order.getValue());

		// do restore of a single region
		changes = loadChanges("restore-model-region.json");
		modelManagementService.updateModel(changes);

		ModelDefinition definition = (ModelDefinition) modelManagementService.getModels().walk(regionPath.cutOffTail());

		assertFalse("Model region should not be in the definition any more",
				definition.getRegionsMap().containsKey("systemRelations"));
		assertTrue("The region systemRelations should not be part of any field", definition.getFields()
				.stream()
				.map(ModelField::getRegionId)
				.filter(Objects::nonNull)
				.noneMatch(field -> field.equals("systemRelations")));
	}

	@Test
	public void modelUpdate_shouldRestoreModelHeader() {
		Path headerPath = Path.parsePath("definition=PR0001/header=breadcrumb_header");

		ModelUpdateRequest changes = loadChanges("prepare-model-4-restore.json");
		modelManagementService.updateModel(changes);

		ModelDefinition definition = (ModelDefinition) modelManagementService.getModels().walk(headerPath.cutOffTail());
		assertTrue("PR0001 should contain mapping for overridden breadcrumb header",
				definition.getHeadersMap().containsKey("breadcrumb_header"));

		ModelHeader breadcrumbHeader = (ModelHeader) modelManagementService.getModels().walk(headerPath);
		assertNotNull("PR0001 should have overridden breadcrumb header", breadcrumbHeader);
		assertTrue("breadcrumb header should have overridden label attribute",
				breadcrumbHeader.hasAttribute(DefinitionModelAttributes.LABEL));

		// Restore breadcrumb header
		changes = loadChanges("restore-model-header.json");
		modelManagementService.updateModel(changes);

		assertFalse("PR0001 should not have mapping for breadcrumb header", definition.getHeadersMap().containsKey("breadcrumb_header"));
	}

	@Test
	public void modelUpdate_shouldAllowNonEditableAttributesForNonDeployedNodes() throws UnsupportedEncodingException {
		String newPropertyUri = "http://ittruse.ittbg.com/ontology/enterpriseManagementFramework#isRevertable";
		Path newPropertyPath = Path.parsePath("property=" + URLEncoder.encode(newPropertyUri, StandardCharsets.UTF_8.toString()));

		ModelUpdateRequest changes = loadChanges("create-new-property.json");
		modelManagementService.updateModel(changes);

		assertTrue(modelManagementService.getProperties().stream().anyMatch(p -> newPropertyUri.equals(p.getId())));

		ModelProperty isRevertable = (ModelProperty) modelManagementService.getModels().walk(newPropertyPath);
		assertNotNull(isRevertable);
		assertEquals(newPropertyUri, isRevertable.getId());
		assertFalse(isRevertable.isDeployed());

		assertModelNodeLabels(isRevertable, "en=Is revertable", "bg=Реверсивно");
		hasMultiLangStringAttribute(isRevertable, "http://purl.org/dc/elements/1.1/creator", "en=Mihail Radkov", "bg=Михаил Радков");

		assertEquals(EMF.DEFINITION_DATA_PROPERTY.toString(),
				isRevertable.getAttributeValue("http://www.w3.org/1999/02/22-rdf-syntax-ns#type"));
		assertEquals("http://ittruse.ittbg.com/ontology/enterpriseManagementFramework#Project",
				isRevertable.getAttributeValue("http://www.w3.org/2000/01/rdf-schema#domain"));
		assertEquals(XMLSchema.BOOLEAN.toString(), isRevertable.getAttributeValue("http://www.w3.org/2000/01/rdf-schema#range"));
		assertEquals(true, isRevertable.getAttributeValue("http://ittruse.ittbg.com/ontology/enterpriseManagementFramework#isSearchable"));
	}


	@Test
	public void modelUpdate_shouldUpdateModelControl() {
		Path attributePath = Path.parsePath("definition=PR0001/field=functional/control=RELATED_FIELDS/attribute=id");
		// prepare data
		ModelUpdateRequest changes = loadChanges("update-model-control.json");
		modelManagementService.updateModel(changes);

		ModelAttribute id = (ModelAttribute) modelManagementService.getModels().walk(attributePath);
		assertEquals(DefinitionModelControlParams.DEFAULT_VALUE_PATTERN, id.getValue());
		assertTrue("The path should resolve to ModelControl", id.getContext() instanceof ModelControl);
		assertEquals("Control should be overridden in child definition", "RELATED_FIELDS",
				id.getContext().getId());
		assertEquals("Field should be overridden in child definition", "functional",
				id.getContext().getContext().getId());
	}

	@Test
	public void modelUpdate_shouldUpdateModelControlParam() {
		Path attributePath = Path.parsePath(
				"definition=PR0001/field=functional/control=RELATED_FIELDS/controlParam=filterSource/attribute=value");
		// prepare data
		ModelUpdateRequest changes = loadChanges("update-model-controlParam.json");
		modelManagementService.updateModel(changes);

		ModelAttribute value = (ModelAttribute) modelManagementService.getModels().walk(attributePath);
		assertEquals("extra2", value.getValue());
		assertTrue("The path should resolve to ModelControlParam", value.getContext() instanceof ModelControlParam);
		assertEquals("Control param should be overridden in child definition", "filterSource",
				value.getContext().getId());
		assertEquals("Control should be overridden in child definition", "RELATED_FIELDS",
				value.getContext().getContext().getId());
	}

	@Test(expected = UpdateModelFailed.class)
	public void modelUpdate_shouldThrowException_WhenValidationDoNotPass() {
		ValidationReport report = new ValidationReport();
		report.addError("Error!");
		when(definitionModelDeployer.validateDefinitions(anyList(), any(DeploymentModels.class))).thenReturn(report);

		// add some changes to the model
		ModelUpdateRequest changes = loadChanges("update-class-definition-property-request.json");
		modelManagementService.updateModel(changes);
	}
	
	private void verifyDeployedSemanticNodes(List<String> expectedProperties, List<String> expectedClasses) {
		ArgumentCaptor<List<Path>> propertiesCaptor = ArgumentCaptor.forClass(List.class);
		ArgumentCaptor<List<Path>> classesCaptor = ArgumentCaptor.forClass(List.class);
		verify(semanticModelDeployer).deploySemanticNodes(propertiesCaptor.capture(), classesCaptor.capture(), any());

		if (CollectionUtils.isEmpty(expectedProperties)) {
			assertTrue(propertiesCaptor.getValue().isEmpty());
		} else {
			expectedDeployed(propertiesCaptor.getValue(), expectedProperties);
		}

		if (CollectionUtils.isEmpty(expectedClasses)) {
			assertTrue(classesCaptor.getValue().isEmpty());
		} else {
			expectedDeployed(classesCaptor.getValue(), expectedClasses);
		}
	}

	private void verifyDeployedDefinitions(List<String> expected) {
		ArgumentCaptor<List<Path>> definitionsCaptor = ArgumentCaptor.forClass(List.class);
		verify(definitionModelDeployer).deployDefinitions(definitionsCaptor.capture(), any());
		expectedDeployed(definitionsCaptor.getValue(), expected);
	}

	private void expectedDeployed(List<Path> captured, List<String> expected) {
		List<String> expectedNodeIds = expected.stream().sorted().collect(Collectors.toList());
		List<String> actualNodeIds = captured.stream().map(Path::getValue).sorted().collect(Collectors.toList());
		assertEquals(expectedNodeIds, actualNodeIds);
	}

	private void verifyModelVersion(long initialVersion, ModelUpdateResponse updateResponse) {
		assertNotNull(updateResponse);
		assertNotEquals("Model version should have changed", initialVersion, updateResponse.getModelVersion());
		assertEquals("Actual model version should be the same as the one in the response",
				modelManagementService.getModels().getVersion(), updateResponse.getModelVersion());
	}

	private void expectChangesFor(ModelUpdateResponse response, String... expected) {
		List<String> modifiedNodes = response.getChangeSets().stream().map(ModelChangeSetInfo::getChangeSet).map(
				ModelChangeSet::getPath).map(Path::getValue).collect(Collectors.toList());
		modifiedNodes.retainAll(Arrays.asList(expected));
		assertEquals(Arrays.asList(expected), modifiedNodes);
	}

	private static void expectHierarchyClassLabels(List<ModelHierarchyClass> hierarchy, IRI classId, String... expectedLabels) {
		ModelHierarchyClass hierarchyClass = getHierarchyNode(hierarchy, classId.toString());
		assertNotNull(hierarchyClass);
		assertEquals(createStringMap(expectedLabels), hierarchyClass.getLabels());
	}

	private static void expectHierarchyDefinitionLabels(List<ModelHierarchyClass> hierarchy, IRI classId, String defId,
			String... expectedLabels) {
		ModelHierarchyClass hierarchyClass = getHierarchyNode(hierarchy, classId.toString());
		assertNotNull(hierarchyClass);
		ModelHierarchyDefinition hierarchyDefinition = getHierarchyNode(hierarchyClass.getSubTypes(), defId);
		assertNotNull(hierarchyDefinition);
		assertEquals(createStringMap(expectedLabels), hierarchyDefinition.getLabels());
	}

	private static <H extends ModelHierarchyNode> H getHierarchyNode(List<H> nodes, String nodeId) {
		return nodes.stream().filter(node -> node.getId().equals(nodeId)).findFirst().orElse(null);
	}
}
