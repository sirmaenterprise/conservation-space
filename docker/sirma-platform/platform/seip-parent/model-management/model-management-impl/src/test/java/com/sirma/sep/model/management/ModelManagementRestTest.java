package com.sirma.sep.model.management;

import com.sirma.sep.model.management.hierarchy.ModelHierarchyClass;
import com.sirma.sep.model.management.meta.ModelMetaInfo;
import com.sirma.sep.model.management.meta.ModelsMetaInfo;
import com.sirma.sep.model.management.request.ModelDeploymentRequest;
import com.sirma.sep.model.management.request.ModelUpdateRequest;
import com.sirma.sep.model.management.response.ModelResponse;
import com.sirma.sep.model.management.response.ModelUpdateResponse;
import com.sirma.sep.model.management.rest.ModelManagementRestService;

import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.Collections;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import javax.ws.rs.core.Response;

/**
 * Tests the model hierarchy retrieval and model select response propagation in {@link ModelManagementRestService}.
 *
 * @author Mihail Radkov
 */
public class ModelManagementRestTest {

	@Mock
	private ModelManagementService modelManagementService;

	@InjectMocks
	private ModelManagementRestService managementRestService;

	@Before
	public void beforeEach() {
		MockitoAnnotations.initMocks(this);
		when(modelManagementService.getModelHierarchy()).thenReturn(getTestHierarchy());
		when(modelManagementService.getMetaInfo()).thenReturn(getMetaInfo());
		when(modelManagementService.getProperties()).thenReturn(getProperties());
		when(modelManagementService.getModel(eq("def1"))).thenReturn(getTestResponse());
		when(modelManagementService.updateModel(any(ModelUpdateRequest.class))).thenReturn(new ModelUpdateResponse());
		when(modelManagementService.validateDeploymentCandidates()).thenReturn(new DeploymentValidationReport());
	}

	@Test
	public void shouldReturnModelHierarchy() {
		List<ModelHierarchyClass> hierarchy = managementRestService.getHierarchy();
		assertEquals("class1", hierarchy.get(0).getId());
	}

	@Test
	public void shouldReturnModelMetaInformation() {
		ModelsMetaInfo metaInfo = managementRestService.getMetaInfo();
		assertEquals("searchable", metaInfo.getSemantics().iterator().next().getId());
	}

	@Test
	public void shouldReturnModelProperties() {
		List<ModelProperty> properties = managementRestService.getProperties();
		assertEquals("property_1", properties.get(0).getId());
	}

	@Test
	public void shouldReturnModelResponse() {
		ModelResponse response = managementRestService.getModel("def1");
		assertEquals("def1", response.getDefinitions().get(0).getId());
	}

	@Test
	public void shouldNotReturnModelResponseForMissingModel() {
		assertNull(managementRestService.getModel("missing_model"));
	}

	@Test
	public void shouldPropagateModelsUpdateRequest() {
		ModelUpdateRequest updateRequest = new ModelUpdateRequest();
		assertNotNull(managementRestService.updateModel(updateRequest));
		verify(modelManagementService).updateModel(eq(updateRequest));
	}

	@Test
	public void shouldProvideDeploymentValidation() {
		assertNotNull(managementRestService.runDeploymentValidation());
		verify(modelManagementService).validateDeploymentCandidates();
	}

	@Test
	public void shouldProduceSuccessfulResponseForValidDeployment() {
		when(modelManagementService.deployChanges(any(ModelDeploymentRequest.class))).thenReturn(new DeploymentValidationReport());
		Response deploymentResponse = managementRestService.deploy(new ModelDeploymentRequest());
		assertEquals(Response.Status.ACCEPTED.getStatusCode(), deploymentResponse.getStatus());
	}

	@Test
	public void shouldProduceBadResponseForInvalidDeployment() {
		DeploymentValidationReport invalidDeploymentReport = new DeploymentValidationReport();
		invalidDeploymentReport.failedDeploymentValidationFor("invalid_node", Collections.singletonList("Error!"));
		when(modelManagementService.deployChanges(any(ModelDeploymentRequest.class))).thenReturn(invalidDeploymentReport);
		Response deploymentResponse = managementRestService.deploy(new ModelDeploymentRequest());
		assertEquals(Response.Status.BAD_REQUEST.getStatusCode(), deploymentResponse.getStatus());
	}

	private static List<ModelHierarchyClass> getTestHierarchy() {
		ModelClass modelClass = new ModelClass().setId("class1");
		ModelHierarchyClass hierarchyClass = new ModelHierarchyClass(modelClass);
		return Collections.singletonList(hierarchyClass);
	}

	private static ModelsMetaInfo getMetaInfo() {
		ModelsMetaInfo metaInfo = new ModelsMetaInfo();
		metaInfo.setSemantics(Collections.singletonList(new ModelMetaInfo().setId("searchable")));
		return metaInfo;
	}

	private static List<ModelProperty> getProperties() {
		return Collections.singletonList(new ModelProperty().setId("property_1"));
	}

	private static ModelResponse getTestResponse() {
		ModelResponse response = new ModelResponse();
		response.setClasses(Collections.emptyList());
		ModelDefinition definition = new ModelDefinition();
		definition.setId("def1");
		response.setDefinitions(Collections.singletonList(definition));
		return response;
	}

}
