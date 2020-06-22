package com.sirma.sep.model.management;

import static com.sirma.sep.model.management.ModelsFakeCreator.createClass;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.stream.Collectors;

import javax.inject.Inject;

import org.apache.commons.io.IOUtils;
import org.junit.Before;
import org.xmlunit.diff.DefaultNodeMatcher;
import org.xmlunit.diff.ElementSelectors;
import org.xmlunit.matchers.CompareMatcher;

import com.sirma.itt.emf.semantic.persistence.ValueConverter;
import com.sirma.itt.seip.convert.DefaultTypeConverter;
import com.sirma.itt.seip.convert.TypeConverterImpl;
import com.sirma.itt.seip.convert.TypeConverterUtil;
import com.sirma.itt.seip.io.ResourceLoadUtil;
import com.sirma.itt.semantic.model.vocabulary.Proton;
import com.sirma.sep.model.management.deploy.ModelDeployer;
import com.sirma.sep.model.management.request.ModelUpdateRequest;
import com.sirma.sep.model.management.request.ModelDeploymentRequest;

/**
 * Base abstract test for preparing dependent services for model deployment tests.
 *
 * @author Mihail Radkov
 */
public abstract class BaseModelDeploymentTest extends BaseModelManagementComponentTest {

	@Inject
	protected ModelUpdateHandler updateHandler;
	@Inject
	protected ModelPersistence modelPersistence;
	@Inject
	protected ModelDeployer modelDeployer;

	@Inject
	protected ModelManagementService modelManagementService;

	@Before
	public void setUpSenderService() {
		senderServiceStub.registerQueueHandler(ModelPersistence.MODEL_UPDATE_QUEUE, updateHandler::onModelChange,
				ModelPersistence.MODEL_UPDATE_RESPONSE_QUEUE, modelPersistence::onModelUpdated);
		senderServiceStub.registerQueueHandler(ModelDeployer.MODEL_DEPLOY_QUEUE, modelDeployer::onDeployModelRequest);
	}

	@Before
	public void setUpLabelDefinitions() {
		withLabelDefinitionFor("CASE_DEFAULT_HEADER", "en=Case default header");
		withLabelDefinitionFor("CASE_COMPACT_HEADER", "en=Case compact header");
		withLabelDefinitionFor("CASE_BREADCRUMB_HEADER", "en=Case breadcrumb header");
		withLabelDefinitionFor("CASE_TOOLTIP_HEADER", "en=Case tooltip header");
		withLabelDefinitionFor("CASE_DEFAULT_HEADER_IMU", "en=Case overridden default header");
		withLabelDefinitionFor("CASE_COMPACT_HEADER_IMU", "en=Case overridden compact header");
		withLabelDefinitionFor("PROJECT_DEFAULT_HEADER", "en=Project default header");
		withLabelDefinitionFor("PROJECT_COMPACT_HEADER", "en=Project compact header");
		withLabelDefinitionFor("PROJECT_BREADCRUMB_HEADER", "en=Project breadcrumb header");
		withLabelDefinitionFor("PROJECT_TOOLTIP_HEADER", "en=Project tooltip header");
	}

	@Before
	public void setUpTypeConverter() {
		// Needed for converting semantic values/statements
		TypeConverterImpl converter = new TypeConverterImpl();
		new DefaultTypeConverter().register(converter);
		new ValueConverter().register(converter);
		TypeConverterUtil.setTypeConverter(converter);
	}

	@Before
	public void setUpSemanticDefinitionService() {
		semanticDefinitionServiceStub.withRootClass(createClass(Proton.ENTITY))
				.withClass(Proton.OBJECT).withParent(Proton.ENTITY).done();
	}

	protected ModelUpdateRequest loadChanges(String path) {
		String changesJson = ResourceLoadUtil.loadResource(getClass(), path);
		com.fasterxml.jackson.databind.ObjectMapper mapper = new com.fasterxml.jackson.databind.ObjectMapper();
		try {
			return mapper.readValue(changesJson, ModelUpdateRequest.class);
		} catch (IOException e) {
			fail(e.getMessage());
			return null;
		}
	}

	protected ModelDeploymentRequest buildDeploymentRequest(DeploymentValidationReport validationResponse) {
		return new ModelDeploymentRequest().setModelsToDeploy(validationResponse.getNodes()
				.stream()
				.map(DeploymentValidationReport.ValidationReportEntry::getId)
				.collect(Collectors.toList())).setVersion(validationResponse.getVersion());
	}

	protected void assertValidReport(DeploymentValidationReport report) {
		assertFalse(report.isEmpty());
		assertTrue(report.isValid());
		assertTrue(report.getGenericErrors().isEmpty());
		assertTrue(report.getFailedEntries().isEmpty());
	}

	protected void assertInvalidReport(DeploymentValidationReport report) {
		assertFalse(report.isEmpty());
		assertFalse(report.isValid());
		assertTrue(!report.getGenericErrors().isEmpty() || !report.getFailedEntries().isEmpty());
	}

	protected void deployChanges(String... changesFiles) {
		Arrays.asList(changesFiles).forEach(changesFile -> {
			ModelUpdateRequest changes = loadChanges(changesFile);
			modelManagementService.updateModel(changes);
		});

		DeploymentValidationReport deployRequest = modelManagementService.validateDeploymentCandidates();
		assertValidReport(deployRequest);

		DeploymentValidationReport deployReport = modelManagementService.deployChanges(buildDeploymentRequest(deployRequest));
		assertValidReport(deployReport);
	}

	protected void verifyImportedModel(String actualFilename, String expectedFilename) {
		String expected = readFile(expectedFilename);
		assertNotNull(expected);

		InputStream inputStream = modelImportServiceStub.getImportedModels().get(actualFilename);
		assertNotNull(inputStream);

		String actual = readStream(inputStream);
		assertNotNull(actual);

		assertThat(actual,
				   CompareMatcher
						   .isSimilarTo(expected)
						   .normalizeWhitespace()
						   .ignoreWhitespace()
						   .ignoreComments()
						   .withNodeMatcher(new DefaultNodeMatcher(ElementSelectors.byNameAndAllAttributes)));
	}

	private String readFile(String fileName) {
		try {
			return IOUtils.toString(getClass().getResource(fileName).toURI());
		} catch (Exception e) {
			throw new IllegalArgumentException(e);
		}
	}

	private static String readStream(InputStream stream) {
		try {
			return IOUtils.toString(stream);
		} catch (IOException e) {
			throw new IllegalArgumentException(e);
		}
	}
}
