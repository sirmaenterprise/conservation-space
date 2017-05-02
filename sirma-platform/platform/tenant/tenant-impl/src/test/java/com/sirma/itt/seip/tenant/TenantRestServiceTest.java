package com.sirma.itt.seip.tenant;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.ws.rs.core.MultivaluedHashMap;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.Response;

import org.jboss.resteasy.plugins.providers.multipart.InputPart;
import org.jboss.resteasy.plugins.providers.multipart.MultipartFormDataInput;
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

import com.sirma.itt.commons.utils.reflection.ReflectionUtils;
import com.sirma.itt.seip.io.TempFileProvider;
import com.sirma.itt.seip.tenant.wizard.TenantInitializationExternalModel;
import com.sirma.itt.seip.tenant.wizard.TenantInitializationModel;
import com.sirma.itt.seip.tenant.wizard.TenantInitializationModelBuilder;
import com.sirma.itt.seip.tenant.wizard.TenantInitializationModelRetriever;
import com.sirma.itt.seip.tenant.wizard.exception.TenantCreationException;

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

	@InjectMocks
	private TenantRestService tenantRestService;

	/**
	 * Init the mocks and mock the model builder.
	 *
	 * @throws URISyntaxException
	 *             the uri syntax exception
	 */
	@Before
	public void init() throws URISyntaxException {
		MockitoAnnotations.initMocks(this);

		TempFileProvider fileProvider = Mockito.mock(TempFileProvider.class);
		ReflectionUtils.setField(modelBuilder, "fileProvider", fileProvider);
		File file = new File(this.getClass().getResource("/test").toURI());
		Mockito.when(fileProvider.createTempDir(Matchers.anyString())).thenReturn(file);
		Mockito.when(fileProvider.createTempFile(Matchers.anyString(), Matchers.anyString())).thenReturn(file);
	}

	/**
	 * Test the create method when a model has been selected by it's id.
	 *
	 * @throws IOException
	 *             if an exception has been thrown.
	 */
	@Test
	public void testCreate() throws IOException {
		mockTenantExternalModel();
		ArgumentCaptor<TenantInitializationModel> modelCaptor = ArgumentCaptor
				.forClass(TenantInitializationModel.class);
		Response response = tenantRestService.create(mockInput("DMSInitialization_attachment_path", "model1,model2"));
		Mockito.verify(tenantService).create(modelCaptor.capture());

		TenantInitializationModel model = modelCaptor.getValue();
		Assert.assertNotNull(model.get("step"));
		Assert.assertNotNull(model.get("DMSInitialization"));
		Assert.assertEquals(200, response.getStatus());
	}

	/**
	 * Test the create method when a model has been manually selected and uploaded as a file.
	 *
	 * @throws IOException
	 *             if an exception has been thrown.
	 */
	@Test
	public void testCreateManuallySelectedModels() throws IOException {
		mockTenantExternalModel();
		ArgumentCaptor<TenantInitializationModel> modelCaptor = ArgumentCaptor
				.forClass(TenantInitializationModel.class);
		Response response = tenantRestService.create(mockInput("DMSInitialization_attachment_definitions",
				new ByteArrayInputStream("fileContent".getBytes(StandardCharsets.UTF_8))));
		Mockito.verify(tenantService).create(modelCaptor.capture());

		TenantInitializationModel model = modelCaptor.getValue();
		Assert.assertNotNull(model.get("step"));
		Assert.assertNotNull(model.get("DMSInitialization"));
		Assert.assertEquals(200, response.getStatus());
	}

	/**
	 * Test the create method when a model has not been selected.
	 *
	 * @throws IOException
	 *             if an exception has been thrown.
	 */
	@Test
	public void testCreateError() throws IOException {
		mockTenantExternalModel();
		TenantCreationException exception = new TenantCreationException(new TenantCreationException("cause"));
		Mockito.doThrow(exception).when(tenantService).create(Matchers.any(TenantInitializationModel.class));
		Response response = tenantRestService.create(mockInput("DMSInitialization_attachment_path", "model1,model2"));

		Assert.assertEquals(500, response.getStatus());
	}

	/**
	 * Mock the {@link MultipartFormDataInput} that is being passed to the method when the create rest service is
	 * invoked. If the selected model is DMSInitialization_attachment_path, then a model has been selected by it's
	 * modelId, otherwise it will process it as a normal model.
	 *
	 * @param selectedModelKey
	 *            the selected model key
	 * @param selectedModelKey
	 *            the selected model data - either model ids or an input stream
	 * @return the mocked {@link MultipartFormDataInput}
	 * @throws IOException
	 *             if an exception has been thrown
	 */
	private static MultipartFormDataInput mockInput(String selectedModelKey, Object selectedModelData)
			throws IOException {
		MultipartFormDataInput input = Mockito.mock(MultipartFormDataInput.class);
		InputPart tenantModelPart = Mockito.mock(InputPart.class);
		InputPart modelsPart = Mockito.mock(InputPart.class);

		Map<String, List<InputPart>> formDataMap = new HashMap<>();
		// tenantmodel is the json data that is being passed from the web. (The one populated in the tenant creation
		// form).
		formDataMap.put("tenantmodel", Arrays.asList(tenantModelPart));
		formDataMap.put(selectedModelKey, Arrays.asList(modelsPart));

		Mockito.when(input.getFormDataMap()).thenReturn(formDataMap);
		Mockito.when(tenantModelPart.getBody(Matchers.any(), Matchers.any())).thenReturn("{'data':[{'id':'step'}]}");
		Mockito.when(modelsPart.getBody(Matchers.any(), Matchers.any())).thenReturn(selectedModelData);

		MultivaluedMap<String, String> headers = new MultivaluedHashMap<>();
		headers.add("Content-Disposition", "filename=\"file.zip\"");
		Mockito.when(modelsPart.getHeaders()).thenReturn(headers);

		return input;
	}

	private void mockTenantExternalModel() {
		TenantInitializationExternalModel model = new TenantInitializationExternalModel();
		model.setId("model1");
		model.setDefinitionsPath("definitions.zip");
		model.setSemanticPath("semantic.zip");
		Mockito.when(modelRetriever.getModel(Matchers.anyString())).thenReturn(model);
		Mockito.when(modelRetriever.getPathInputStream(Matchers.anyString())).thenReturn(
				new ByteArrayInputStream("path".getBytes(StandardCharsets.UTF_8)));
	}
}
