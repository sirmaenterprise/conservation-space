package com.sirma.sep.model.management;

import static com.sirma.sep.model.management.ModelsFakeCreator.createCodeValue;
import static com.sirma.sep.model.management.ModelsFakeCreator.createStringMap;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Collections;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Spy;
import org.mockito.ArgumentCaptor;

import com.sirma.itt.emf.cls.validator.exception.CodeValidatorException;
import com.sirma.itt.seip.definition.label.LabelDefinition;
import com.sirma.itt.seip.exception.RollbackedRuntimeException;
import com.sirma.itt.seip.domain.validation.ValidationReport;
import com.sirma.sep.cls.model.CodeDescription;
import com.sirma.sep.cls.model.CodeValue;
import com.sirma.sep.model.management.DeploymentValidationReport.ValidationReportEntry;
import com.sirma.sep.model.management.deploy.ModelDeployer;
import com.sirma.sep.model.management.deploy.definition.DefinitionModelDeployer;
import com.sirma.sep.model.management.request.ModelDeploymentRequest;
import com.sirma.sep.model.management.request.ModelUpdateRequest;
import com.sirma.sep.model.management.exception.UpdateModelFailed;

/**
 * Tests the {@link ModelDefinition} validation and deployment process via
 * {@link DefinitionModelDeployer},
 * {@link com.sirma.sep.model.management.deploy.definition.DefinitionChangeSetManager} and
 * {@link com.sirma.sep.model.management.definition.export.GenericDefinitionConverter}.
 *
 * @author Mihail Radkov
 */
public class ModelDefinitionDeployTest extends BaseModelDeploymentTest {

	@Spy
	private ModelUpdater modelUpdater;

	@Before
	public void beforeEach() {
		codelistServiceStub.withValueForList(createCodeValue("PR0001", 2, "en=Main Project", "bg=Основен проект"));

		// For modifying fields & regions labels/tooltips in PR0001
		// TODO: why not add them in the definition ? they are now loaded automatically.
		withLabelDefinitionFor("type.label", "en=Type", "bg=Тип");
		withLabelDefinitionDefinedIn("title.label", "PR0001");
		withLabelDefinitionFor("description.PR0001.tooltip", "en=Description tooltip");
		withLabelDefinitionFor("uniqueIdentifier.label", "en=Unique identifier");
		withLabelDefinitionDefinedIn("functional.PR0001.tooltip", "PR0001", "en=Functional tooltip");
		withLabelDefinitionDefinedIn("relationships.region.label", "PR0001", "en=Relationships", "bg=Връзки");
		withLabelDefinitionFor("timeAndEffort.region.label", "en=Time and effort");
		withLabelDefinitionFor("state.label", "en=State");
	}

	@Test
	public void shouldDeployCodelistChanges() {
		withDefinitions("entity.xml", "genericProject.xml", "genericTestProject.xml", "PR0001.xml");

		ModelUpdateRequest changes = loadChanges("deploy-definition-changes.json");
		modelManagementService.updateModel(changes);

		DeploymentValidationReport deployRequest = modelManagementService.validateDeploymentCandidates();
		assertValidReport(deployRequest);

		assertEquals(1, deployRequest.getNodes().size());
		assertEquals("PR0001", deployRequest.getNodes().iterator().next().getId());

		DeploymentValidationReport deployReport = modelManagementService.deployChanges(buildDeploymentRequest(deployRequest));
		assertValidReport(deployReport);

		verifyPersistedCodeValue("PR0001", "en=Main Project edit 3", "bg=Основен проект 2");
	}

	@Test
	public void shouldDeployChangesAccordingTheRequestedVersion() {
		withDefinitions("entity.xml", "genericProject.xml", "genericTestProject.xml", "PR0001.xml");

		ModelUpdateRequest changes = loadChanges("deploy-definition-changes.json");
		modelManagementService.updateModel(changes);

		// First deployment request with version 12 should be valid
		DeploymentValidationReport deployRequest = modelManagementService.validateDeploymentCandidates();
		assertValidReport(deployRequest);
		assertEquals(12, deployRequest.getVersion());

		// Add one more change to raise the version to 13
		ModelUpdateRequest additionalChanges = loadChanges("deploy-additional-definition-changes.json");
		modelManagementService.updateModel(additionalChanges);

		// The second deployment request should be valid too and with version 13
		DeploymentValidationReport secondDeployRequest = modelManagementService.validateDeploymentCandidates();
		assertValidReport(secondDeployRequest);
		assertEquals(13, secondDeployRequest.getVersion());

		// But use the first deployment request where the version is still 3
		DeploymentValidationReport deployReport = modelManagementService.deployChanges(buildDeploymentRequest(deployRequest));
		assertValidReport(deployReport);

		// Should have NOT applied version 4
		verifyPersistedCodeValue("PR0001", "en=Main Project edit 3", "bg=Основен проект 2");
	}

	@Test
	public void shouldNotDeployInvalidCodeValues() {
		withDefinitions("entity.xml", "genericProject.xml", "genericTestProject.xml", "PR0001.xml");

		// Treat all code values as invalid
		withInvalidCodeValues();

		ModelUpdateRequest changes = loadChanges("deploy-definition-changes.json");

		try {
			modelManagementService.updateModel(changes);
		} catch (UpdateModelFailed e) {
			// as models are invalid, an exception should be thrown
			DeploymentValidationReport report = e.getValidationReport();
			assertInvalidReport(report);
			assertEquals(1, report.getNodes().size());
			ValidationReportEntry validationReportEntry = report.getNodes().iterator().next();
			assertFalse(validationReportEntry.isValid());
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

	@Test
	public void shouldIgnoreEmptyDeploymentRequest() {
		withDefinitions("entity.xml", "genericProject.xml", "genericTestProject.xml", "PR0001.xml");

		ModelUpdateRequest changes = loadChanges("deploy-definition-changes.json");
		modelManagementService.updateModel(changes);

		DeploymentValidationReport deployRequest = modelManagementService.validateDeploymentCandidates();
		assertFalse(deployRequest.isEmpty());
		assertTrue(deployRequest.isValid());

		ModelDeploymentRequest empty = new ModelDeploymentRequest().setModelsToDeploy(Collections.emptyList());
		DeploymentValidationReport deployReport = modelManagementService.deployChanges(empty);
		assertTrue(deployReport.isEmpty());
	}

	@Test
	public void shouldValidateDefinitionChanges() {
		withDefinitions("entity.xml", "genericProject.xml", "genericTestProject.xml", "PR0001.xml");

		modelImportServiceStub.withInvalidModels("Invalid definitions");

		ModelUpdateRequest changes = loadChanges("deploy-definition-changes.json");
		try {
			modelManagementService.updateModel(changes);
		} catch (UpdateModelFailed e) {
			// as models are invalid, an exception should be thrown
			DeploymentValidationReport report = e.getValidationReport();
			assertInvalidReport(report);
			assertEquals(1, report.getNodes().size());
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

		// Should have not imported any models due to second validation during deploy
		verifyNoImportedModels();
	}

	@Test(expected = RollbackedRuntimeException.class)
	public void shouldRollbackIfDefinitionsCannotBeDeployedAfterValidation() {
		// Re-stub the sender service to synchronously deploy the models (easy to capture the exception)
		senderServiceStub.registerSyncQueueHandler(ModelDeployer.MODEL_DEPLOY_QUEUE, modelDeployer::onDeployModelRequest);

		withDefinitions("entity.xml", "genericProject.xml", "genericTestProject.xml", "PR0001.xml");

		when(modelImportService.validateModel(any())).thenReturn(ValidationReport.valid());
		when(modelImportService.importModel(any())).thenReturn(new ValidationReport().addError("Invalid definitions"));

		ModelUpdateRequest changes = loadChanges("deploy-definition-changes.json");
		modelManagementService.updateModel(changes);

		// Should be valid
		DeploymentValidationReport deployRequest = modelManagementService.validateDeploymentCandidates();
		assertValidReport(deployRequest);

		// But for some reason invalid during deployment
		modelManagementService.deployChanges(buildDeploymentRequest(deployRequest));
		fail("Should have not deployed the definition models");
	}

	@Test
	public void shouldProperlyDeployDefinitions() {
		withDefinitions("entity.xml", "genericProject.xml", "genericTestProject.xml", "PR0001.xml");

		deployChanges("deploy-definition-changes.json");

		verifyImportedModels(1);
		verifyImportedModel("PR0001.xml", "PR0001_edited.xml");
	}

	@Test
	public void shouldPersistUpdatedLabelAttributes() {
		withDefinitions("entity.xml", "genericProject.xml", "genericTestProject.xml", "PR0001.xml", "PR0002.xml");

		deployChanges("deploy-complex-definition-changes.json");

		// Should have not modified the existing type.label because its not defined in PR0001
		// Should have created new label id and defined in PR0001
		assertLabels("type.label", null, "en=Type", "bg=Тип");
		assertLabels("PR0001.type.label", "PR0001", "en=Type edit", "bg=Тип редакция", "fi=Tyyppi");

		// Add labels for present label ID defined in PR0001
		assertLabels("title.label", "PR0001", "en=Title", "bg=Заглавие");

		// Create tooltips and generate tooltip ID
		assertLabels("PR0001.title.tooltip", "PR0001", "en=Title tooltip attribute", "bg=Заглавие тултип атрибут");

		// Creating new tooltip id (description.PR0001.tooltip -> PR0001.description.tooltip) because the previous was not defined in PR0001
		assertLabels("PR0001.description.tooltip", "PR0001", "en=Description tooltip edit", "bg=Описание тултип");

		// uniqueIdentifier.label should not be removed because it wasn't defined in PR0001
		assertLabels("uniqueIdentifier.label", null, "en=Unique identifier");
		// functional.PR0001.tooltip should be removed because it was defined in PR0001
		assertLabels("functional.PR0001.tooltip", null);

		// relationships.region.label is "defined" in PR0001 so it should be updated
		assertLabels("relationships.region.label", "PR0001", "en=Relationships edit", "bg=Връзки редакция");
		// Should have not modified the existing timeAndEffort.region.label because its not defined in PR0001
		// Should have created new label id defined in PR0001
		assertLabels("PR0001.timeAndEffort.label", "PR0001", "en=Time and effort edit");
	}

	@Test
	public void shouldPersistUpdatedDefinitionHeaders() {
		withDefinitions("entity.xml", "genericProject.xml", "genericTestProject.xml", "PR0001.xml", "PR0002.xml", "PR1000.xml");

		deployChanges("deploy-complex-definition-changes.json", "deploy-complex-headers-changes.json");

		// Default header for genericProject should be modified and new language added
		// It should not be modified when being overridden in child definitions
		assertLabels("PROJECT_DEFAULT_HEADER", "genericProject",
				"en=UPDATED default header in English",
				"de=UPDATED default header in German",
				"bg=ОБНОВЕН дефолтен хедър на Български",
				"fi=NEW default header in Fin");

		// New header for PR0002 but label for DE is inherited & overridden
		assertLabels("PR0002.breadcrumb_header.label", "PR0002",
				"en=NEW breadcrumb header for PR0002 in English",
				"bg=НОВ дефолтен хедър за PR0002 на Български",
				"de=breadcrumb header in German");

		// breadcrumb header for genericProject should be removed
		assertLabels("PROJECT_BREADCRUMB_HEADER", "genericProject", "en=", "de=", "bg=");

		// default_header for PR1000 should be updated under new label ID because the old one was not defined in PR1000
		assertLabels("PR1000.default_header.label", "PR1000",
				"en=Overridden default header in English",
				"de=Overridden default header in German",
				"bg=Пренабит дефолтен хедър на Български");
	}

	@Test
	public void shouldProperlyDeployComplexDefinitionChanges() {

		//
		// This test asserts before and after XMLs to verify that everything is properly copied and exported
		// while the other tests check code list and label services
		//

		withDefinitions("entity.xml", "genericProject.xml", "genericTestProject.xml", "PR0001.xml", "PR0002.xml", "PR1000.xml");

		deployChanges("deploy-complex-definition-changes.json", "deploy-complex-headers-changes.json");

		// genericProject -> genericTestProject -> PR0001 -> PR0002 -> PR1000
		// But genericTestProject should be skipped because there are no changes for it
		verifyImportedModels(4);
		verifyImportedModel("genericProject.xml", "genericProject_edited_complex.xml");
		verifyImportedModel("PR0001.xml", "PR0001_edited_complex.xml");
		verifyImportedModel("PR0002.xml", "PR0002_edited_complex.xml");
		verifyImportedModel("PR1000.xml", "PR1000_edited_complex.xml");
	}

	@Test
	public void shouldRemoveRegionLabelsOfRestoredRegion() {
		withDefinitions("entity.xml", "genericProject4Restore.xml", "genericTestProject4Restore.xml", "PR00014Restore.xml");

		withLabelDefinitionDefinedIn("someField.label", "genericTestProject4Restore", "en=Some field label");
		withLabelDefinitionDefinedIn("regionToRestore.someRegion.label", "genericTestProject4Restore", "en=Some region label");

		deployChanges("restore-definition-elements.json");

		verifyImportedModels(1);

		assertLabels("someField.label", null);
		assertLabels("regionToRestore.someRegion.label", null);
	}

	@Test
	public void shouldRemoveRestoredLabels() {
		withDefinitions("entity.xml", "genericProject4Restore.xml", "genericTestProject4Restore.xml", "PR00014Restore.xml");

		withLabelDefinitionDefinedIn("someField.label", "genericTestProject4Restore", "en=Some field label");
		withLabelDefinitionDefinedIn("regionToRestore.someRegion.label", "genericTestProject4Restore", "en=Some region label");

		deployChanges("restore-region-and-field-labels.json");

		verifyImportedModels(1);

		assertLabels("someField.label", null);
		assertLabels("regionToRestore.someRegion.label", null);
	}

	@Test
	public void shouldProperlyDeployComplexRestoreChanges() {

		//
		// This test asserts before and after XMLs to verify that everything is properly copied and exported
		// after applying restore changes.
		//

		withDefinitions("entity.xml", "genericProject.xml", "genericTestProject.xml", "PR0001.xml");

		deployChanges("deploy-complex-restore-changes.json");

		// genericProject & PR0001
		verifyImportedModels(2);
		verifyImportedModel("genericProject.xml", "genericProject_restored.xml");
		verifyImportedModel("PR0001.xml", "PR0001_restored.xml");
	}

	@Test
	public void shouldNotDeployRestoredFieldWithInvisibleAttributes() {
		withDefinitions("entity.xml", "PR1002.xml");

		deployChanges("restore-PR1002-has-watcher.json");

		verifyImportedModels(1);
		verifyImportedModel("PR1002.xml", "PR1002_restored.xml");
	}

	@Test
	public void shouldProperlyDeployControlDefinitions() {
		withDefinitions("entity.xml", "genericProject.xml", "genericTestProject.xml", "PR1001.xml");

		deployChanges("deploy-controls-changes.json");

		verifyImportedModels(1);
		verifyImportedModel("PR1001.xml", "PR1001_edited.xml");
	}

	private void verifyNoImportedModels() {
		verifyImportedModels(0);
	}

	private void verifyImportedModels(int count) {
		assertEquals(count, modelImportServiceStub.getImportedModels().size());
	}

	private void verifyPersistedCodeValue(String value, String... descriptions) {
		ArgumentCaptor<CodeValue> valueCaptor = ArgumentCaptor.forClass(CodeValue.class);
		verify(codeListPersister).persist(valueCaptor.capture());

		CodeValue persistedValue = valueCaptor.getValue();
		assertNotNull(persistedValue);
		assertEquals(value, persistedValue.getValue());
		verifyCodeValueDescriptions(persistedValue, descriptions);
	}

	private void assertLabels(String labelId, String definedIn, String... labels) {
		LabelDefinition labelDefinition = labelServiceStub.getLabelsMap().get(labelId);
		assertNotNull(labelDefinition);

		Set<String> expectedDefinedIn = definedIn != null ? Collections.singleton(definedIn) : Collections.emptySet();
		Map<String, String> expectedLabels = createStringMap(labels);

		assertEquals(labelId, labelDefinition.getIdentifier());
		assertEquals(expectedDefinedIn, labelDefinition.getDefinedIn());
		assertEquals(expectedLabels, labelDefinition.getLabels());
	}

	private void withInvalidCodeValues() {
		doThrow(new CodeValidatorException("", Collections.singletonList("Invalid data"))).when(codeValidator).validateCodeValue(any());
	}

	private static void verifyCodeValueDescriptions(CodeValue value, String... descriptions) {
		Map<String, CodeDescription> updatedDescriptions = value.getDescriptions()
				.stream()
				.collect(Collectors.toMap(CodeDescription::getLanguage, Function.identity()));
		Map<String, String> expected = createStringMap(descriptions);
		assertEquals(updatedDescriptions.size(), expected.size());
		expected.forEach((lang, name) -> assertTrue(updatedDescriptions.get(lang).getName().equals(name)));
	}

}
