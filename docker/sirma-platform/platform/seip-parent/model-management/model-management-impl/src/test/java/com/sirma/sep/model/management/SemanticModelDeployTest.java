package com.sirma.sep.model.management;

import static com.sirma.sep.model.management.ModelsFakeCreator.createClass;
import static com.sirma.sep.model.management.ModelsFakeCreator.createCodeValue;
import static com.sirma.sep.model.management.ModelsFakeCreator.createProperty;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import javax.enterprise.event.Observes;
import javax.inject.Inject;

import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.model.Resource;
import org.eclipse.rdf4j.model.vocabulary.DC;
import org.eclipse.rdf4j.model.vocabulary.DCTERMS;
import org.eclipse.rdf4j.model.vocabulary.OWL;
import org.eclipse.rdf4j.model.vocabulary.RDF;
import org.eclipse.rdf4j.model.vocabulary.RDFS;
import org.eclipse.rdf4j.model.vocabulary.XMLSchema;
import org.junit.Before;
import org.junit.Test;

import org.mockito.Spy;

import com.sirma.itt.seip.definition.event.LoadSemanticDefinitions;
import com.sirma.itt.seip.domain.codelist.event.ResetCodelistEvent;
import com.sirma.itt.seip.instance.validation.InstanceValidationResult;
import com.sirma.itt.seip.instance.validation.PropertyValidationError;
import com.sirma.itt.semantic.model.vocabulary.EMF;
import com.sirma.itt.semantic.model.vocabulary.Proton;
import com.sirma.itt.semantic.model.vocabulary.Security;
import com.sirma.sep.model.management.DeploymentValidationReport.ValidationReportEntry;
import com.sirma.sep.model.management.meta.ModelMetaInfo;
import com.sirma.sep.model.management.request.ModelDeploymentRequest;
import com.sirma.sep.model.management.exception.UpdateModelFailed;

/**
 * Tests the semantic deployment of classes and properties, more precisely via {@link com.sirma.sep.model.management.deploy.semantic.SemanticModelDeployer}
 *
 * @author <a href="mailto:borislav.bonev@sirma.bg">Borislav Bonev</a>
 * @since 20/08/2018
 */
public class SemanticModelDeployTest extends BaseModelDeploymentTest {

	private IRI PRODUCT;
	private IRI DATA_IMPORT_REPORT;

	@Inject
	private ModelManagementService modelManagementService;

	@Spy
	private ModelUpdater modelUpdater;

	private boolean semanticCacheReset = false;

	{
		PRODUCT = valueFactory.createIRI(EMF.NAMESPACE, "Product");
		DATA_IMPORT_REPORT = valueFactory.createIRI(EMF.NAMESPACE, "DataImportReport");
	}

	@Before
	public void setUp() throws Exception {
		// Stubbing required for building the models
		semanticDefinitionServiceStub
				.withRootClass(createClass(Proton.ENTITY));
		semanticDefinitionServiceStub
				.withClass(Proton.OBJECT)
				.withParent(Proton.ENTITY).done();
		semanticDefinitionServiceStub.
				withClass(PRODUCT)
				.withParent(Proton.OBJECT)
				.withProperty("uploadable", false).done();
		semanticDefinitionServiceStub
				.withClass(DATA_IMPORT_REPORT)
				.withParent(Proton.OBJECT)
				.withLabels("EN=Data Import Report", "DE=Datenimportbericht", "FI=Tietojen tuontiraportti")
				.withProperty("creator", "Kiril Penev")
				.withProperty("createable", false)
				.withProperty("uploadable", false)
				.withProperty("searchable", true)
				.withProperty("allowInheritLibraryPermissions", false).done();
		semanticDefinitionServiceStub
				.withClass(EMF.ACTIVITY)
				.withParent(Proton.ENTITY).done();
		semanticDefinitionServiceStub
				.withClass(EMF.CASE)
				.withLabels("en=Case", "bg=Преписка")
				.withParent(EMF.ACTIVITY)
				.withProperty("creator", "Case creator")
				.withProperty("createable", true)
				.withProperty("uploadable", false)
				.withProperty("searchable", true)
				.withProperty("allowInheritLibraryPermissions", false).done();
		semanticDefinitionServiceStub
				.withClass(EMF.PROJECT)
				.withParent(EMF.ACTIVITY).done();

		semanticDefinitionServiceStub
				.withProperty(createProperty(RDF.TYPE));
		semanticDefinitionServiceStub
				.withProperty(createProperty(EMF.TYPE));
		semanticDefinitionServiceStub
				.withProperty(DCTERMS.TITLE).withLabels("en=Title").done();
		semanticDefinitionServiceStub
				.withProperty(EMF.STATUS)
				.withLabels("en=Status")
				.withProperty("domainClass", "ptop:Entity")
				.withProperty("rangeClass", "xsd:string")
				.withProperty("isSearchable", true)
				.withLanguageProperty("creator", "en=Kiril Penev", "bg=Кирил Пенев")
				.done();
		semanticDefinitionServiceStub
				.withObjectProperty(EMF.CREATED_BY)
				.withLabels("en=Created by")
				.withProperty("domainClass", "ptop:Entity")
				.withProperty("rangeClass", "ptop:Agent")
				.withProperty("autoAssignPermissionRole", "CONSUMER")
				.done();

		codelistServiceStub
				.withValueForList(createCodeValue("PR0001", 2, "en=Main Project", "bg=Основен проект"))
				.withValueForList(createCodeValue("CS0001", 4, "en=Main Case", "bg=Основна Преписка"))
				.withValueForList(createCodeValue("CS0002", 4, "en=Case with WF", "bg=Преписка създаване на работен процес"));

		withLabelDefinitionFor("model.management.abstract", "en=Abstract", "bg=Абстрактна");
		withLabelDefinitionFor("model.management.identifier", "en=Identifier", "bg=Идентификатор");

		withDefinitions("entity.xml", "PR0001.xml", "CS0001.xml", "CS0002.xml", "genericCase.xml", "genericProject.xml",
				"genericTestCase.xml", "genericTestProject.xml");

		semanticCacheReset = false;
	}

	@Test
	public void shouldNotDeployChangesTwice() {
		withClass(DATA_IMPORT_REPORT)
				.withLanguageProperty(resolveProperty(DCTERMS.TITLE), "Data Import Report", "en")
				.withLanguageProperty(resolveProperty(DC.CREATOR), "Kiril Penev", "en")
				.withProperty(resolveProperty(EMF.IS_UPLOADABLE), false)
				.withProperty(resolveProperty(Security.ALLOW_INHERIT_LIBRARY_PERMISSIONS), false);

		applyChanges("deploy-class-changes.json");

		DeploymentValidationReport deployRequest = modelManagementService.validateDeploymentCandidates();
		assertValidReport(deployRequest);
		assertEquals(2, deployRequest.getNodes().size());

		DeploymentValidationReport deployResponse = modelManagementService.deployChanges(buildDeploymentRequest(deployRequest));
		assertValidReport(deployResponse);

		// Should have deployed emf:DataImportReport, while emf:Product has no changes for deploying after aggregation
		DeploymentValidationReport secondDeployResponse = modelManagementService.validateDeploymentCandidates();
		assertValidReport(secondDeployResponse);
		assertEquals(1, secondDeployResponse.getNodes().size());
	}

	@Test
	public void shouldNotifyForDeployedClassChanges() {
		withClass(DATA_IMPORT_REPORT)
				.withLanguageProperty(resolveProperty(DCTERMS.TITLE), "Data Import Report", "en")
				.withLanguageProperty(resolveProperty(DC.CREATOR), "Kiril Penev", "en")
				.withProperty(resolveProperty(EMF.IS_UPLOADABLE), false)
				.withProperty(resolveProperty(Security.ALLOW_INHERIT_LIBRARY_PERMISSIONS), false);

		applyChanges("deploy-class-changes.json");

		DeploymentValidationReport deployRequest = modelManagementService.validateDeploymentCandidates();
		assertValidReport(deployRequest);

		DeploymentValidationReport deployResponse = modelManagementService.deployChanges(buildDeploymentRequest(deployRequest));
		assertValidReport(deployResponse);

		assertTrue("Semantic cache should have been notified to reset", semanticCacheReset);
	}

	@SuppressWarnings("unused")
	void listenForSemanticChanges(@Observes LoadSemanticDefinitions event) {
		semanticCacheReset = true;
	}

	@Test
	public void prepareDeployRequest_shouldValidateDeploymentToBeWithWarnings() {
		withClass(DATA_IMPORT_REPORT)
				.withLanguageProperty(resolveProperty(DCTERMS.TITLE), "Data Import Report 2", "en")
				.withLanguageProperty(resolveProperty(DC.CREATOR), "Kiril Penev 2", "en")
				.withProperty(resolveProperty(EMF.IS_UPLOADABLE), false)
				.withProperty(resolveProperty(Security.ALLOW_INHERIT_LIBRARY_PERMISSIONS), false);

		applyChanges("deploy-class-changes.json");

		DeploymentValidationReport deployRequest = modelManagementService.validateDeploymentCandidates();
		assertValidReport(deployRequest);

		List<ValidationReportEntry> reportEntries = new LinkedList<>(deployRequest.getNodes());

		ValidationReportEntry productValidationInfo = reportEntries.get(0);
		assertEquals(PRODUCT.toString(), productValidationInfo.getId());
		assertTrue(productValidationInfo.isValid());

		ValidationReportEntry dataImportValidationInfo = reportEntries.get(1);
		assertEquals(DATA_IMPORT_REPORT.toString(), dataImportValidationInfo.getId());
		assertTrue(dataImportValidationInfo.isValid());
		// Should contain warnings
		assertEquals(2, dataImportValidationInfo.getMessages().size());
	}

	@Test
	public void shouldDeployClassChangesEvenIfDatabaseDoesNotMatch() {
		withClass(DATA_IMPORT_REPORT)
				.withLanguageProperty(resolveProperty(DCTERMS.TITLE), "Data Import Report", "en")
				.withLanguageProperty(resolveProperty(DC.CREATOR), "Kiril Penev 2", "en")
				.withProperty(resolveProperty(EMF.IS_UPLOADABLE), false)
				.withProperty(resolveProperty(Security.ALLOW_INHERIT_LIBRARY_PERMISSIONS), false);

		applyChanges("deploy-class-changes.json");

		DeploymentValidationReport deployRequest = modelManagementService.validateDeploymentCandidates();
		assertValidReport(deployRequest);
		assertFalse(deployRequest.getEntriesWithWarnings().isEmpty());

		// Try to deploy with warnings
		try {
			DeploymentValidationReport deployResponse = modelManagementService.deployChanges(buildDeploymentRequest(deployRequest));
			assertValidReport(deployResponse);
		} finally {
			expectClass(DATA_IMPORT_REPORT)
					.withLanguageProperty(resolveProperty(DCTERMS.TITLE), "Data Import Report EN 3", "en")
					.withLanguageProperty(resolveProperty(DC.CREATOR), "Borislav Bonev", "en")
					.withProperty(resolveProperty(EMF.IS_UPLOADABLE), true)
					.withProperty(resolveProperty(Security.ALLOW_INHERIT_LIBRARY_PERMISSIONS), true)
					.validateState();
		}
	}

	@Test
	public void shouldDeployClassChangesIfDatabaseDoesNotHaveTheOldData() {
		withClass(DATA_IMPORT_REPORT)
				.withLanguageProperty(resolveProperty(DCTERMS.TITLE), "Data Import Report", "en")
				.withLanguageProperty(resolveProperty(DC.CREATOR), "Kiril Penev", "en")
				.withProperty(resolveProperty(EMF.IS_UPLOADABLE), false)
				.withProperty(resolveProperty(Security.ALLOW_INHERIT_LIBRARY_PERMISSIONS), false);

		applyChanges("deploy-class-changes.json");

		DeploymentValidationReport deployRequest = modelManagementService.validateDeploymentCandidates();
		assertValidReport(deployRequest);

		try {
			DeploymentValidationReport deployResponse = modelManagementService.deployChanges(buildDeploymentRequest(deployRequest));
			assertValidReport(deployResponse);
		} finally {
			expectClass(DATA_IMPORT_REPORT)
					.withLanguageProperty(resolveProperty(DCTERMS.TITLE), "Data Import Report EN 3", "en")
					.withLanguageProperty(resolveProperty(DC.CREATOR), "Borislav Bonev", "en")
					.withProperty(resolveProperty(EMF.IS_UPLOADABLE), true)
					.withProperty(resolveProperty(Security.ALLOW_INHERIT_LIBRARY_PERMISSIONS), true)
					.validateState();
		}
	}

	@Test
	public void shouldDeployClassChanges() {
		withClass(DATA_IMPORT_REPORT)
				.withLanguageProperty(resolveProperty(DCTERMS.TITLE), "Data Import Report", "en")
				.withLanguageProperty(resolveProperty(DC.CREATOR), "Kiril Penev", "en")
				.withProperty(resolveProperty(EMF.IS_UPLOADABLE), false)
				.withProperty(resolveProperty(Security.ALLOW_INHERIT_LIBRARY_PERMISSIONS), false);

		applyChanges("deploy-class-changes.json");

		DeploymentValidationReport deployRequest = modelManagementService.validateDeploymentCandidates();
		assertValidReport(deployRequest);

		DeploymentValidationReport deployResponse = modelManagementService.deployChanges(buildDeploymentRequest(deployRequest));
		assertValidReport(deployResponse);

		expectClass(DATA_IMPORT_REPORT)
				.withLanguageProperty(resolveProperty(DCTERMS.TITLE), "Data Import Report EN 3", "en")
				.withLanguageProperty(resolveProperty(DC.CREATOR), "Borislav Bonev", "en")
				.withProperty(resolveProperty(EMF.IS_UPLOADABLE), true)
				.withProperty(resolveProperty(Security.ALLOW_INHERIT_LIBRARY_PERMISSIONS), true)
				.validateState();
	}

	@Test
	public void afterDeploymentAndRecalculationTheModelVersionShouldNotChange() {
		withClass(DATA_IMPORT_REPORT)
				.withLanguageProperty(resolveProperty(DCTERMS.TITLE), "Data Import Report", "en")
				.withLanguageProperty(resolveProperty(DC.CREATOR), "Kiril Penev", "en")
				.withProperty(resolveProperty(EMF.IS_UPLOADABLE), false)
				.withProperty(resolveProperty(Security.ALLOW_INHERIT_LIBRARY_PERMISSIONS), false);

		applyChanges("deploy-class-changes.json");

		DeploymentValidationReport deployRequest = modelManagementService.validateDeploymentCandidates();
		assertValidReport(deployRequest);

		DeploymentValidationReport deployResponse = modelManagementService.deployChanges(buildDeploymentRequest(deployRequest));
		assertValidReport(deployResponse);
		long currentVersion = modelManagementService.getModels().getVersion();

		withDefinitions("entity.xml", "PR0001.xml", "CS0001.xml", "CS0002.xml", "genericCase.xml", "genericProject.xml",
				"genericTestCase.xml", "genericTestProject.xml");
		eventService.fire(new ResetCodelistEvent());

		long newVersion = modelManagementService.getModels().getVersion();
		assertEquals(currentVersion, newVersion);
	}

	@Test
	public void shouldDeployChangesAccordingToTheRequestedModelsVersion() {
		withClass(DATA_IMPORT_REPORT)
				.withLanguageProperty(resolveProperty(DCTERMS.TITLE), "Data Import Report", "en")
				.withLanguageProperty(resolveProperty(DCTERMS.TITLE), "Tietojen tuontiraportti", "fi")
				.withLanguageProperty(resolveProperty(DC.CREATOR), "Kiril Penev", "en")
				.withProperty(resolveProperty(EMF.IS_UPLOADABLE), false)
				.withProperty(resolveProperty(Security.ALLOW_INHERIT_LIBRARY_PERMISSIONS), false);

		// Apply first changes
		applyChanges("deploy-class-changes.json");

		// First deployment request should be valid
		DeploymentValidationReport deployRequest = modelManagementService.validateDeploymentCandidates();
		assertValidReport(deployRequest);
		assertEquals(11, deployRequest.getVersion());

		// Apply second changes
		applyChanges("deploy-additional-class-changes.json");

		// Second deployment request with the second changes should also be valid
		DeploymentValidationReport secondDeployRequest = modelManagementService.validateDeploymentCandidates();
		assertValidReport(secondDeployRequest);
		assertEquals(14, secondDeployRequest.getVersion());

		// Use the first deployment request where second changes should not be present and should be with the first models version
		DeploymentValidationReport deployResponse = modelManagementService.deployChanges(buildDeploymentRequest(deployRequest));
		assertValidReport(deployResponse);

		// Should have NOT deployed the second changes
		expectClass(DATA_IMPORT_REPORT)
				.withLanguageProperty(resolveProperty(DCTERMS.TITLE), "Data Import Report EN 3", "en")
				// Finish should be the same
				.withLanguageProperty(resolveProperty(DCTERMS.TITLE), "Tietojen tuontiraportti", "fi")
				.withLanguageProperty(resolveProperty(DC.CREATOR), "Borislav Bonev", "en")
				.withProperty(resolveProperty(EMF.IS_UPLOADABLE), true)
				.validateState();
	}

	@Test
	public void shouldBeAbleToDeployAfterMultipleUpdates() {
		withClass(DATA_IMPORT_REPORT)
				.withLanguageProperty(resolveProperty(DCTERMS.TITLE), "Data Import Report", "en")
				.withLanguageProperty(resolveProperty(DCTERMS.TITLE), "Tietojen tuontiraportti", "fi")
				.withLanguageProperty(resolveProperty(DC.CREATOR), "Kiril Penev", "en")
				.withProperty(resolveProperty(EMF.IS_UPLOADABLE), false)
				.withProperty(resolveProperty(Security.ALLOW_INHERIT_LIBRARY_PERMISSIONS), false);

		// Apply first changes
		applyChanges("deploy-class-changes.json");

		// Apply second changes
		applyChanges("deploy-additional-class-changes.json");

		// Second deployment request with the first & second changes should be valid
		DeploymentValidationReport deployRequest = modelManagementService.validateDeploymentCandidates();
		assertValidReport(deployRequest);
		assertEquals(14, deployRequest.getVersion());

		DeploymentValidationReport deployResponse = modelManagementService.deployChanges(buildDeploymentRequest(deployRequest));
		assertValidReport(deployResponse);

		// Should have deployed the second changes
		expectClass(DATA_IMPORT_REPORT)
				.withLanguageProperty(resolveProperty(DCTERMS.TITLE), "Data Import Report EN 444", "en")
				.withLanguageProperty(resolveProperty(DCTERMS.TITLE), "Tietojen tuontiraporttiiiii 2", "fi")
				.withLanguageProperty(resolveProperty(DC.CREATOR), "Mihail Radkov", "en")
				.withProperty(resolveProperty(EMF.IS_UPLOADABLE), false)
				.validateState();
	}

	@Test
	public void modelDeploy_shouldNotDeployChangesForNotPickedSubClasses() {
		withClass(EMF.ACTIVITY)
				.withProperty(resolveProperty(Security.ALLOW_INHERIT_LIBRARY_PERMISSIONS), false);
		withClass(EMF.CASE)
				.withLanguageProperty(resolveProperty(DCTERMS.TITLE), "Case", "en")
				.withLanguageProperty(resolveProperty(DCTERMS.TITLE), "Преписка", "bg");

		applyChanges("class-hierarchy-changes.json");

		DeploymentValidationReport validationReport = modelManagementService.validateDeploymentCandidates();
		assertValidReport(validationReport);

		ModelDeploymentRequest deployRequest = new ModelDeploymentRequest();
		deployRequest.setVersion(validationReport.getVersion());
		deployRequest.setModelsToDeploy(Collections.singletonList(EMF.ACTIVITY.toString()));

		DeploymentValidationReport deploymentValidationReport = modelManagementService.deployChanges(deployRequest);
		assertValidReport(deploymentValidationReport);

		expectClass(EMF.ACTIVITY)
				.withProperty(resolveProperty(Security.ALLOW_INHERIT_LIBRARY_PERMISSIONS), true)
				.validateState();
		expectClass(EMF.CASE)
				.withLanguageProperty(resolveProperty(DCTERMS.TITLE), "Case", "en")
				.withLanguageProperty(resolveProperty(DCTERMS.TITLE), "Преписка", "bg")
				.validateState();
	}

	@Test
	public void modelDeploy_shouldValidateMissingClassProperties() {
		// unexpected value for IS_UPLOADABLE -> 1 warning
		// Missing ALLOW_INHERIT_LIBRARY_PERMISSIONS should be a warning
		withClass(DATA_IMPORT_REPORT)
				.withLanguageProperty(resolveProperty(DCTERMS.TITLE), "Data Import Report", "en")
				.withLanguageProperty(resolveProperty(DC.CREATOR), "Kiril Penev", "en")
				.withProperty(resolveProperty(EMF.IS_UPLOADABLE), true);

		applyChanges("deploy-class-changes.json");

		DeploymentValidationReport deployRequest = modelManagementService.validateDeploymentCandidates();
		assertValidReport(deployRequest);

		List<ValidationReportEntry> entriesWithWarnings = deployRequest.getEntriesWithWarnings();
		assertEquals(1, entriesWithWarnings.size());
		assertEquals(1, entriesWithWarnings.get(0).getMessages().size());
	}

	@Test
	public void modelDeploy_shouldDeployDifferentClassPropertyTypes() {
		withClass(DATA_IMPORT_REPORT)
				// Title is for some reason loaded as simple string
				.withProperty(resolveProperty(DCTERMS.TITLE), "Data Import Report")
				.withLanguageProperty(resolveProperty(DC.CREATOR), "Kiril Penev", "en")
				.withProperty(resolveProperty(EMF.IS_UPLOADABLE), false)
				.withProperty(resolveProperty(Security.ALLOW_INHERIT_LIBRARY_PERMISSIONS), false);

		applyChanges("deploy-class-changes.json");

		DeploymentValidationReport deployRequest = modelManagementService.validateDeploymentCandidates();
		assertValidReport(deployRequest);

		DeploymentValidationReport deployResponse = modelManagementService.deployChanges(buildDeploymentRequest(deployRequest));
		assertValidReport(deployResponse);

		expectClass(DATA_IMPORT_REPORT)
				// But should be saved as multi language with language from the change set
				.withLanguageProperty(resolveProperty(DCTERMS.TITLE), "Data Import Report EN 3", "en")
				.withLanguageProperty(resolveProperty(DC.CREATOR), "Borislav Bonev", "en")
				.withProperty(resolveProperty(EMF.IS_UPLOADABLE), true)
				.withProperty(resolveProperty(Security.ALLOW_INHERIT_LIBRARY_PERMISSIONS), true)
				.validateState();
	}

	@Test
	public void shouldNotDeployClassChangesIfInstanceValidationFails() {
		InstanceValidationResult invalid = new InstanceValidationResult(Collections.singletonList(mock(PropertyValidationError.class)));
		when(instanceValidationService.validate(any())).thenReturn(invalid);

		withClass(DATA_IMPORT_REPORT)
				.withLanguageProperty(resolveProperty(DCTERMS.TITLE), "Data Import Report", "en")
				.withLanguageProperty(resolveProperty(DC.CREATOR), "Kiril Penev", "en")
				.withProperty(resolveProperty(EMF.IS_UPLOADABLE), false)
				.withProperty(resolveProperty(Security.ALLOW_INHERIT_LIBRARY_PERMISSIONS), false);

		try {
			applyChanges("deploy-class-changes.json");
		} catch (UpdateModelFailed e) {
			// as models are invalid, an exception should be thrown
			DeploymentValidationReport report = e.getValidationReport();
			assertInvalidReport(report);
			assertEquals(2, report.getNodes().size());
		} finally {
			verify(modelUpdater, never()).dryRunUpdate(any(Models.class), any(ModelChanges.class));
		}

		// when validate, no changes should be applied
		DeploymentValidationReport deployRequest = modelManagementService.validateDeploymentCandidates();
		assertTrue(deployRequest.isEmpty());
		assertTrue(deployRequest.isValid());

		// same for deploy
		DeploymentValidationReport deployReport = modelManagementService.deployChanges(buildDeploymentRequest(deployRequest));
		assertTrue(deployReport.isEmpty());
	}

	//
	// PROPERTIES
	//

	@Test
	public void shouldDeploySemanticProperties() {
		// Prepare data in the stubbed repository connection. This data will be modified by the change sets
		withResource(EMF.STATUS)
				.withLiteral(EMF.IS_SEARCHABLE, true)
				.withLiteral(DCTERMS.TITLE, "Status", "en")
				.withLiteral(DC.CREATOR, "Kiril Penev", "en")
				.withLiteral(DC.CREATOR, "Кирил Пенев", "bg");
		withResource(EMF.CREATED_BY)
				.withLiteral(Security.AUTO_ASSIGN_PARENT_PERMISSION_ROLE, "CONSUMER");

		applyChanges("deploy-property-changes.json");

		DeploymentValidationReport deployRequest = modelManagementService.validateDeploymentCandidates();
		assertValidReport(deployRequest);

		DeploymentValidationReport deployResponse = modelManagementService.deployChanges(buildDeploymentRequest(deployRequest));
		assertValidReport(deployResponse);

		expectResource(EMF.STATUS)
				// Should be changed from true
				.withLiteral(EMF.IS_SEARCHABLE, false)
				// Should be edited
				.withLiteral(DCTERMS.TITLE, "Status edit", "en")
				// Should be new label in BG
				.withLiteral(DCTERMS.TITLE, "Статус", "bg")
				// Should not be touched
				.withLiteral(DC.CREATOR, "Kiril Penev", "en")
				// Should be removed
				.withoutLiteral(DC.CREATOR, "Кирил Пенев", "bg")
				.validateState();

		expectResource(EMF.CREATED_BY)
				// Should be added
				.withObject(OWL.INVERSEOF, "emf:hasCreated")
				// Should be added
				.withLiteral(EMF.AUDIT_EVENT, "+addCreatedBy")
				// Should be removed
				.withoutLiteral(Security.AUTO_ASSIGN_PARENT_PERMISSION_ROLE, "CONSUMER")
				.validateState();
	}

	@Test
	public void shouldDeployNewSemanticProperties() {
		applyChanges("create-new-property.json");

		DeploymentValidationReport deployRequest = modelManagementService.validateDeploymentCandidates();
		assertValidReport(deployRequest);

		DeploymentValidationReport deployResponse = modelManagementService.deployChanges(buildDeploymentRequest(deployRequest));
		assertValidReport(deployResponse);

		IRI isRevertable = valueFactory.createIRI(EMF.NAMESPACE, "isRevertable");
		expectResource(isRevertable)
				// Data properties should have the correct rdf:type
				.withObject(RDF.TYPE, EMF.DEFINITION_DATA_PROPERTY)
				.withObject(RDFS.DOMAIN, EMF.PROJECT)
				.withObject(RDFS.RANGE, XMLSchema.BOOLEAN)
				.withLiteral(DCTERMS.TITLE, "Is revertable", "en")
				.withLiteral(DCTERMS.TITLE, "Реверсивно", "bg")
				.withLiteral(DC.CREATOR, "Mihail Radkov", "en")
				.withLiteral(DC.CREATOR, "Михаил Радков", "bg")
				// should deploy change even if the new and the old values are the same
				.withLiteral(EMF.IS_SEARCHABLE, true)
				.withLiteral(EMF.IS_SYSTEM_PROPERTY, false)
				.validateState();
	}

	@Test
	public void shouldValidateSemanticPropertiesDeployment() {
		withResource(EMF.STATUS)
				// isSearchable is missing
				// .withLiteral(EMF.IS_SEARCHABLE, true)
				// Status is mismatch
				.withLiteral(DCTERMS.TITLE, "Status 2", "en")
				// Creator is mismatch + bg label is missing
				.withLiteral(DC.CREATOR, "Kiril Penev 2", "en");
		withResource(EMF.CREATED_BY)
				.withLiteral(Security.AUTO_ASSIGN_PARENT_PERMISSION_ROLE, "CONSUMER");

		applyChanges("deploy-property-changes.json");

		DeploymentValidationReport deployRequest = modelManagementService.validateDeploymentCandidates();
		assertValidReport(deployRequest);

		List<ValidationReportEntry> entriesWithWarnings = deployRequest.getEntriesWithWarnings();
		assertEquals(1, entriesWithWarnings.size());
		// Property warnings should be transferred to the owning class
		assertEquals(Proton.ENTITY.toString(), entriesWithWarnings.get(0).getId());
		assertEquals(3, entriesWithWarnings.get(0).getMessages().size());
	}

	private void applyChanges(String... changes) {
		Arrays.asList(changes).forEach(change -> modelManagementService.updateModel(loadChanges(change)));
	}

	private DomainInstanceServiceStub.InstanceBuilder withClass(IRI identifier) {
		return new DomainInstanceServiceStub.InstanceBuilder(identifier.toString(), domainInstanceServiceStub);
	}

	private DomainInstanceServiceStub.InstanceVerifier expectClass(IRI identifier) {
		return new DomainInstanceServiceStub.InstanceVerifier(identifier.toString(), domainInstanceServiceStub);
	}

	public StatementsBuilder withResource(Resource subject) {
		return new StatementsBuilder.DatabaseBuilder(subject, semanticDatabase);
	}

	public StatementsBuilder.DatabaseValidator expectResource(Resource subject) {
		return new StatementsBuilder.DatabaseValidator(subject, semanticDatabase);
	}

	private String resolveProperty(IRI iri) {
		ModelMetaInfo modelMetaInfo = modelManagementService.getMetaInfo().getSemanticsMapping().get(iri.toString());
		if (modelMetaInfo == null) {
			throw new IllegalArgumentException("Missing meta info mapping for semantic attribute with IRI=" + iri);
		}
		return modelMetaInfo.getId();
	}

}
