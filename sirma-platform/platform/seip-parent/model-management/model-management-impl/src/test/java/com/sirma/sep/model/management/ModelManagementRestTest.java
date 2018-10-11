package com.sirma.sep.model.management;

import com.sirma.sep.model.management.hierarchy.ModelHierarchyClass;
import com.sirma.sep.model.management.meta.ModelMetaInfo;
import com.sirma.sep.model.management.meta.ModelsMetaInfo;
import com.sirma.sep.model.management.response.ModelResponse;
import com.sirma.sep.model.management.rest.ModelManagementRestService;

import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.Collections;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.when;

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
	}

	@Test
	public void shouldReturnModelHierarchy() {
		List<ModelHierarchyClass> hierarchy = managementRestService.getHierarchy();
		assertEquals("class1", hierarchy.get(0).getId());
	}

	@Test
	public void shouldReturnModelMetaInformation() {
		ModelsMetaInfo metaInfo = managementRestService.getMetaInfo();
		assertEquals("searchable", metaInfo.getSemantics().get(0).getId());
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

	private static List<ModelHierarchyClass> getTestHierarchy() {
		ModelHierarchyClass class1 = new ModelHierarchyClass();
		class1.setId("class1").setParentId(null);

		return Collections.singletonList(class1);
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
