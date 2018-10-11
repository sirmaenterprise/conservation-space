package com.sirma.itt.seip.tenant;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.ws.rs.core.MultivaluedHashMap;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.Response;

import org.apache.commons.fileupload.FileItem;
import org.apache.commons.io.IOUtils;
import org.jboss.resteasy.plugins.providers.multipart.InputPart;
import org.jboss.resteasy.plugins.providers.multipart.MultipartFormDataInput;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Matchers;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.mockito.Spy;

import com.sirma.itt.seip.Pair;
import com.sirma.itt.seip.io.TempFileProvider;
import com.sirma.itt.seip.tenant.TenantInitializationStatusService.Status;
import com.sirma.itt.seip.tenant.wizard.TenantInitializationExternalModel;
import com.sirma.itt.seip.tenant.wizard.TenantInitializationModel;
import com.sirma.itt.seip.tenant.wizard.TenantInitializationModelBuilder;
import com.sirma.itt.seip.tenant.wizard.TenantInitializationModelRetriever;
import com.sirma.itt.seip.tenant.wizard.TenantStepData;
import com.sirma.itt.seip.tenant.wizard.exception.TenantCreationException;
import com.sirma.itt.seip.tenant.wizard.exception.TenantDeletionException;
import com.sirma.itt.seip.testutil.fakes.TaskExecutorFake;
import com.sirma.itt.seip.util.ReflectionUtils;
import com.sirma.sep.content.upload.UploadRequest;

/**
 * Test the tenant rest service
 *
 * @author nvelkov
 */
public class TenantRestServiceTest {

	@Mock
	private TenantManagementService tenantService;

	@Spy
	private TenantInitializationModelBuilder modelBuilder;

	@Mock
	private TenantInitializationModelRetriever modelRetriever;

	@Mock
	private TenantInitializationStatusService statusService;

	@InjectMocks
	private TenantRestService tenantRestService;

	/**
	 * Init the mocks and mock the model builder.
	 *
	 * @throws URISyntaxException the uri syntax exception
	 */
	@Before
	public void init() throws URISyntaxException {
		MockitoAnnotations.initMocks(this);

		TempFileProvider fileProvider = mock(TempFileProvider.class);
		ReflectionUtils.setFieldValue(modelBuilder, "fileProvider", fileProvider);
		ReflectionUtils.setFieldValue(tenantRestService, "executor", new TaskExecutorFake());
		File file = new File(this.getClass().getResource("/test").toURI());
		when(fileProvider.createTempDir(anyString())).thenReturn(file);
		when(fileProvider.createTempFile(anyString(), anyString())).thenReturn(file);
	}

	/**
	 * Test the create method when a model has been selected by it's id.
	 *
	 * @throws IOException if an exception has been thrown.
	 */
	@Test
	public void testCreate() throws IOException {
		mockTenantExternalModel();
		ArgumentCaptor<TenantInitializationModel> modelCaptor = ArgumentCaptor
				.forClass(TenantInitializationModel.class);
		Response response = tenantRestService.create(mockInput("DMSInitialization_attachment_path", "model1,model2"));
		verify(tenantService).create(modelCaptor.capture());

		TenantInitializationModel model = modelCaptor.getValue();
		assertNotNull(model.get("step"));
		assertNotNull(model.get("DMSInitialization"));
		assertEquals(Response.Status.OK.getStatusCode(), response.getStatus());
	}

	/**
	 * Test the create method when a model has been manually selected and uploaded as a file.
	 *
	 * @throws IOException if an exception has been thrown.
	 */
	@Test
	public void testCreateManuallySelectedModels() throws IOException {
		mockTenantExternalModel();
		ArgumentCaptor<TenantInitializationModel> modelCaptor = ArgumentCaptor
				.forClass(TenantInitializationModel.class);
		Response response = tenantRestService.create(mockInput("DMSInitialization_attachment_definitions",
				new ByteArrayInputStream("fileContent".getBytes(StandardCharsets.UTF_8))));
		verify(tenantService).create(modelCaptor.capture());

		TenantInitializationModel model = modelCaptor.getValue();
		assertNotNull(model.get("step"));
		assertNotNull(model.get("DMSInitialization"));
		assertEquals(Response.Status.OK.getStatusCode(), response.getStatus());
	}

	/**
	 * Test the create method when a model has not been selected.
	 *
	 * @throws IOException if an exception has been thrown.
	 */
	@Test
	public void testCreateError() throws IOException {
		mockTenantExternalModel();
		TenantCreationException exception = new TenantCreationException(new TenantCreationException("cause"));
		doThrow(exception).when(tenantService).create(any(TenantInitializationModel.class));
		Response response = tenantRestService.create(mockInput("DMSInitialization_attachment_path", "model1,model2"));

		assertEquals(Response.Status.INTERNAL_SERVER_ERROR.getStatusCode(), response.getStatus());
	}

	/**
	 * Test the tenant creation completed status.
	 */
	@Test
	public void should_returnCompleted_when_Successful() {
		when(statusService.isCompleted(anyString())).thenReturn(true);
		when(statusService.getStatus(anyString())).thenReturn(new Pair<>(Status.COMPLETED, "completed"));
		Response response = tenantRestService.getStatus("tenant");
		assertEquals("completed", response.getEntity());
		assertEquals(Response.Status.OK.getStatusCode(), response.getStatus());
	}

	/**
	 * Test the successful tenant deletion.
	 */
	@Test
	public void should_returnCompleted_when_deleted() {
		Response response = tenantRestService.delete("tenantId");
		verify(tenantService).delete("tenantId");
		assertEquals(Response.Status.OK.getStatusCode(), response.getStatus());
	}

	/**
	 * Test the tenant deletion with an exception.
	 */
	@Test
	public void should_returnError_on_exceptionWhileDeleting() {
		when(statusService.getStatus(anyString())).thenReturn(new Pair<>(Status.FAILED, "failed"));
		doThrow(new TenantDeletionException(new TenantDeletionException())).when(tenantService).delete(anyString());
		Response response = tenantRestService.delete("tenantId");
		verify(tenantService).delete("tenantId");
		assertEquals(Response.Status.INTERNAL_SERVER_ERROR.getStatusCode(), response.getStatus());
	}

	/**
	 * Test the successful tenant update.
	 *
	 * @throws IOException if an exception has been thrown.
	 */
	@Test
	public void should_returnCompleted_when_updated() throws IOException {
		mockTenantExternalModel();
		ArgumentCaptor<TenantInitializationModel> modelCaptor = ArgumentCaptor
				.forClass(TenantInitializationModel.class);

		Response response = tenantRestService.update("tenantId",
				mockInput("DMSInitialization_attachment_path", "model1,model2"));
		verify(tenantService).update(modelCaptor.capture(), eq("tenantId"));

		TenantInitializationModel model = modelCaptor.getValue();
		assertNotNull(model.get("step"));
		assertNotNull(model.get("DMSInitialization"));
		assertEquals(Response.Status.OK.getStatusCode(), response.getStatus());
	}

	/**
	 * Test the tenant update with an exception.
	 *
	 * @throws IOException if an exception has been thrown.
	 */
	@Test
	public void should_returnError_on_exceptionWhileUpdating() throws IOException {
		when(statusService.getStatus(anyString())).thenReturn(new Pair<>(Status.FAILED, "failed"));
		Response response = tenantRestService.update("tenantId",
				mockInput("DMSInitialization_attachment_path", "model1,model2"));
		assertEquals(Response.Status.INTERNAL_SERVER_ERROR.getStatusCode(), response.getStatus());
	}

	/**
	 * Test ontology upload when an archive is present alongside with individual files
	 *
	 * @throws IOException if an exception has been thrown.
	 */
	@Test
	public void should_returnCompleted_when_uploading_ontology() throws IOException {
		mockTenantExternalModel();
		ArgumentCaptor<TenantInitializationModel> modelCaptor = ArgumentCaptor
				.forClass(TenantInitializationModel.class);

		UploadRequest request = mockOntologyUploadRequest();

		Response response = tenantRestService.uploadOntologyModel("tenantId", request);
		verify(tenantService).update(modelCaptor.capture(), eq("tenantId"));

		TenantInitializationModel model = modelCaptor.getValue();
		TenantStepData stepData = model.get("SemanticDbUpdate");

		assertNotNull(model.get("step"));
		assertNotNull(stepData);
		assertEquals(4, stepData.getModels().size());
		assertEquals(Response.Status.OK.getStatusCode(), response.getStatus());
	}

	/**
	 * Test the ontology upload with an exception.
	 *
	 * @throws IOException if an exception has been thrown.
	 */
	@Test
	public void should_returnError_on_exceptionWhileUploadingOntology() throws IOException {
		when(statusService.isInProgress(anyString())).thenReturn(true);
		Response response = tenantRestService.uploadOntologyModel("tenantId", mockOntologyUploadRequest());
		assertEquals(Response.Status.INTERNAL_SERVER_ERROR.getStatusCode(), response.getStatus());
	}

	/**
	 * Test the tenant creation failed status.
	 */
	@Test
	public void should_returnFailed_when_Failed() {
		when(statusService.hasFailed(anyString())).thenReturn(true);
		when(statusService.getStatus(anyString())).thenReturn(new Pair<>(Status.FAILED, "failed"));
		Response response = tenantRestService.getStatus("tenant");
		assertEquals("failed", response.getEntity());
		assertEquals(Response.Status.INTERNAL_SERVER_ERROR.getStatusCode(), response.getStatus());
	}

	/**
	 * Test the tenant creation in progress status.
	 */
	@Test
	public void should_returnInProgress_when_InProgress() {
		when(statusService.isInProgress(anyString())).thenReturn(true);
		when(statusService.getStatus(Matchers.anyString())).thenReturn(new Pair<>(Status.IN_PROGRESS, "in progress"));
		Response response = tenantRestService.getStatus("tenant");
		assertEquals("Tenant creation is currently at step in progress", response.getEntity());
		assertEquals(Response.Status.ACCEPTED.getStatusCode(), response.getStatus());
	}

	/**
	 * Test the tenant creation with a tenantId of a tenant that's currently being created by another user.
	 *
	 * @throws IOException if an exception has been thrown.
	 */
	@Test
	public void should_returnError_when_creatingTenantThatsAlreadyBeingCreated() throws IOException {
		mockTenantExternalModel();
		when(statusService.getStatus(anyString())).thenReturn(new Pair<>(Status.IN_PROGRESS, "in progress"));
		when(statusService.isInProgress(anyString())).thenReturn(true);
		Response response = tenantRestService
				.create(mockInput("DMSInitialization_attachment_path", "model1,model2"));
		assertEquals(Response.Status.INTERNAL_SERVER_ERROR.getStatusCode(), response.getStatus());
	}

	/**
	 * Test the tenant deletion with a tenantId of a tenant that's currently being created by another user.
	 *
	 * @throws IOException if an exception has been thrown.
	 */
	@Test
	public void should_returnError_when_deletingTenantThatsAlreadyBeingCreated() throws IOException {
		when(statusService.getStatus(anyString())).thenReturn(new Pair<>(Status.IN_PROGRESS, "in progress"));
		when(statusService.isInProgress(anyString())).thenReturn(true);
		Response response = tenantRestService.delete("tenantId");
		assertEquals(Response.Status.INTERNAL_SERVER_ERROR.getStatusCode(), response.getStatus());
	}

	/**
	 * Test the tenant updating with a tenantId of a tenant that's currently being created by another user.
	 *
	 * @throws IOException if an exception has been thrown.
	 */
	@Test
	public void should_returnError_when_updatingTenantThatsAlreadyBeingCreated() throws IOException {
		when(statusService.getStatus(anyString())).thenReturn(new Pair<>(Status.IN_PROGRESS, "in progress"));
		when(statusService.isInProgress(anyString())).thenReturn(true);
		Response response = tenantRestService.update("tenanId",
				mockInput("DMSInitialization_attachment_path", "model1,model2"));
		assertEquals(Response.Status.INTERNAL_SERVER_ERROR.getStatusCode(), response.getStatus());
	}

	/**
	 * Test the tenant creation status when the tenant creation steps haven't started yet.
	 */
	@Test
	public void should_returnOk_when_notStarted() {
		Response response = tenantRestService.getStatus("tenant");
		assertEquals("No available status for tenant creation process for that tenant!", response.getEntity());
		assertEquals(Response.Status.OK.getStatusCode(), response.getStatus());

		// Make sure that the status is not cleared out. We need to keep it while the operation is
		// complete
		verify(statusService, never()).setStatus("tenant", null, null);
	}

	/**
	 * Mock the {@link MultipartFormDataInput} that is being passed to the method when the create rest service is
	 * invoked. If the selected model is DMSInitialization_attachment_path, then a model has been selected by it's
	 * modelId, otherwise it will process it as a normal model.
	 *
	 * @param selectedModelKey the selected model key
	 * @param selectedModelKey the selected model data - either model ids or an input stream
	 * @return the mocked {@link MultipartFormDataInput}
	 * @throws IOException if an exception has been thrown
	 */
	private static MultipartFormDataInput mockInput(String selectedModelKey, Object selectedModelData)
			throws IOException {
		MultipartFormDataInput input = Mockito.mock(MultipartFormDataInput.class);
		InputPart tenantModelPart = Mockito.mock(InputPart.class);
		InputPart modelsPart = Mockito.mock(InputPart.class);
		Map<String, List<InputPart>> formDataMap = new HashMap<>();
		// tenantmodel is the json data that is being passed from the web. (The one populated in the
		// tenant creation form).
		formDataMap.put("tenantmodel", Arrays.asList(tenantModelPart));
		formDataMap.put(selectedModelKey, Arrays.asList(modelsPart));

		when(input.getFormDataMap()).thenReturn(formDataMap);
		when(tenantModelPart.getBody(any(), any())).thenReturn(
				"{'data':[{'id':'TenantInitialization', 'properties':[{'id':'tenantid','value':'tenantId'}]}]}");
		when(modelsPart.getBody(any(), any())).thenReturn(selectedModelData);

		MultivaluedMap<String, String> headers = new MultivaluedHashMap<>();
		headers.add("Content-Disposition", "filename=\"file.zip\"");
		when(modelsPart.getHeaders()).thenReturn(headers);

		return input;
	}

	private static UploadRequest mockOntologyUploadRequest() throws IOException {
		FileItem ontologyOne = mockFileItem("ontology.zip");
		FileItem ontologyTwo = mockFileItem("turtle.ttl");
		FileItem ontologyFour = mockFileItem("namespace.ns");
		FileItem ontologyThree = mockFileItem("changelog.xml");

		UploadRequest request = mock(UploadRequest.class);
		when(request.getRequestItems())
				.thenReturn(Arrays.asList(ontologyOne, ontologyTwo, ontologyThree, ontologyFour));
		return request;
	}

	private static FileItem mockFileItem(String filename) throws IOException {
		FileItem fileItem = mock(FileItem.class);
		try (InputStream stream = IOUtils.toInputStream(filename)) {
			when(fileItem.getName()).thenReturn(filename);
			when(fileItem.getInputStream()).thenReturn(stream);
			return fileItem;
		}
	}

	private void mockTenantExternalModel() {
		TenantInitializationExternalModel model = new TenantInitializationExternalModel();
		model.setId("model1");
		model.setDefinitionsPath("definitions.zip");
		model.setSemanticPath("semantic.zip");
		when(modelRetriever.getModel(anyString())).thenReturn(model);
		when(modelRetriever.getPathInputStream(anyString()))
				.thenReturn(new ByteArrayInputStream("path".getBytes(StandardCharsets.UTF_8)));
	}
}