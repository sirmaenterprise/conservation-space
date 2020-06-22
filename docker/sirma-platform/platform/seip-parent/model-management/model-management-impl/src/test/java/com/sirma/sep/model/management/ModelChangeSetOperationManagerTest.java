package com.sirma.sep.model.management;

import static org.junit.Assert.*;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.MockitoAnnotations;
import org.mockito.Spy;

import com.sirma.itt.seip.testutil.mocks.InstanceProxyMock;
import com.sirma.sep.model.management.exception.ChangeSetCollisionException;
import com.sirma.sep.model.management.meta.ModelMetaInfo;
import com.sirma.sep.model.management.meta.ModelsMetaInfo;
import com.sirma.sep.model.management.operation.ModelChangeSet;
import com.sirma.sep.model.management.operation.ModelChangeSetInfo;
import com.sirma.sep.model.management.operation.ModelChangeSetOperation;
import com.sirma.sep.model.management.operation.ModifyAttributeChangeSetOperation;

/**
 * Test for {@link ModelChangeSetOperationManager}
 *
 * @author <a href="mailto:borislav.bonev@sirma.bg">Borislav Bonev</a>
 * @since 30/07/2018
 */
public class ModelChangeSetOperationManagerTest {
	@InjectMocks
	private ModelChangeSetOperationManager operationManager;
	@Spy
	private InstanceProxyMock<ModelChangeSetOperation> operations = new InstanceProxyMock<>();

	@Before
	public void setUp() throws Exception {
		operations = new InstanceProxyMock<>();
		operations.add(new ModifyAttributeChangeSetOperation());

		MockitoAnnotations.initMocks(this);
	}

	@Test
	public void execute_shouldProcessChangeSets() throws Exception {
		Models model = createEmptyModel();
		List<ModelChangeSetInfo> changes = Arrays.asList(addClassProperty("emf:Case", "ptop:title", "Test Case"),
				addClassProperty("emf:Case", "ptop:description", "Test Case description"),
				addDefinitionProperty("testCase", "title", "Case definition"),
				addDefinitionProperty("testCase", "description", "Case definition description"));

		operationManager.execute(model, changes, (models, change) -> assertNotNull(change),
				(changeSetValidationFailed, changeSetInfo) -> {
					fail(changeSetValidationFailed.getMessage());
					return false;
				});

		assertTrue(model.getClasses().containsKey("emf:Case"));
		assertEquals("Test Case", model.getClasses().get("emf:Case").findAttributeValue("ptop:title"));
		assertTrue(model.getDefinitions().containsKey("testCase"));
		assertEquals("Case definition", model.getDefinitions().get("testCase").findAttributeValue("title"));
	}

	@Test
	public void execute_shouldNotProcessChangesForInvalidOpName() {
		Models model = createEmptyModel();
		ModelChangeSetInfo changeSetInfo = addClassProperty("emf:Case", "ptop:title", "Test Case");
		changeSetInfo.getChangeSet().setOperation("someInvalidOperation");

		operationManager.execute(model, Collections.singleton(changeSetInfo),
				(models, change) -> fail("Should not have been called"),
				(exception, change) -> {
					assertTrue("Execute should have failed with missing operation change, but got "
									+ exception.getMessage(),
							exception.getMessage().toLowerCase().contains("operation"));
					return false;
				});
	}

	@Test
	public void execute_shouldNotProcessChangesForMissingOpName() {
		Models model = createEmptyModel();
		ModelChangeSetInfo changeSetInfo = addClassProperty("emf:Case", "ptop:title", "Test Case");
		changeSetInfo.getChangeSet().setOperation(null);

		operationManager.execute(model, Collections.singleton(changeSetInfo),
				(models, change) -> fail("Should not have been called"),
				(exception, change) -> {
					assertTrue("Execute should have failed with missing operation change, but got "
									+ exception.getMessage(),
							exception.getMessage().toLowerCase().contains("missing operation identifier"));
					return false;
				});
	}

	@Test
	public void execute_shouldNotProcessChangesForInvalidProperty() {
		Models model = createEmptyModel();
		ModelChangeSetInfo changeSetInfo = addClassProperty("emf:Case", "ptop:creator", "Test Case");

		operationManager.execute(model, Collections.singleton(changeSetInfo),
				(models, change) -> fail("Should not have been called"),
				(exception, change) -> {
					assertTrue("Execute should have failed with invalid property found operation change, but got "
									+ exception.getMessage(),
							exception.getMessage().toLowerCase().contains("not supported node"));
					return false;
				});
	}

	@Test
	public void execute_shouldNotifyForFailingOperation() {
		Models model = createEmptyModel();
		ModelChangeSetInfo changeSetInfo = addClassProperty("emf:Case", "ptop:title", "Test Case");
		ModelAttribute node = (ModelAttribute) model.walk(changeSetInfo.getChangeSet().getPath());
		node.setValue("some value");

		operationManager.execute(model, Collections.singleton(changeSetInfo),
				(models, change) -> fail("Should not have been called"),
				(exception, change) -> {
					assertTrue("Execute should have failed with invalid change, but got " + exception.getMessage(),
							exception.getMessage().toLowerCase().contains("detected value collision"));
					return false;
				});
	}

	@Test
	public void execute_shouldApplyChangeEventOnCollisionOperationIfAllowed() {
		Models model = createEmptyModel();
		ModelChangeSetInfo changeSetInfo = addClassProperty("emf:Case", "ptop:title", "Test Case");
		ModelAttribute node = (ModelAttribute) model.walk(changeSetInfo.getChangeSet().getPath());
		node.setValue("some value");

		operationManager.execute(model, Collections.singleton(changeSetInfo),
				(models, change) -> assertNotNull(change),
				(exception, change) -> {
					assertTrue("Execute should have failed with invalid change, but got " + exception.getMessage(),
							exception.getMessage().toLowerCase().contains("detected value collision"));
					return exception instanceof ChangeSetCollisionException;
				});

		node = (ModelAttribute) model.walk(changeSetInfo.getChangeSet().getPath());
		assertEquals("The value should have been overridden", "Test Case", node.getValue());
	}

	private ModelChangeSetInfo addClassProperty(String classId, String attributeName, String attributeValue) {
		return new ModelChangeSetInfo().setChangeSet(new ModelChangeSet()
				.setSelector("class=" + classId + "/attribute=" + attributeName)
				.setNewValue(attributeValue)
				.setOperation("modifyAttribute"));
	}

	private ModelChangeSetInfo addDefinitionProperty(String definitionId, String attributeName,
			String attributeValue) {
		return new ModelChangeSetInfo().setChangeSet(new ModelChangeSet()
				.setSelector("/definition=" + definitionId + "/attribute=" + attributeName)
				.setNewValue(attributeValue)
				.setOperation("modifyAttribute"));
	}

	private Models createEmptyModel() {
		Models models = new Models();
		models.setClasses(new HashMap<>());
		models.setDefinitions(new HashMap<>());
		models.setProperties(new HashMap<>());
		ModelsMetaInfo modelsMetaInfo = new ModelsMetaInfo();
		List<ModelMetaInfo> semanticAttributes = new LinkedList<>();
		semanticAttributes.add(new ModelMetaInfo().setUri("ptop:title"));
		semanticAttributes.add(new ModelMetaInfo().setUri("ptop:description"));
		modelsMetaInfo.setSemantics(semanticAttributes);
		List<ModelMetaInfo> definitionAttributes = new LinkedList<>();
		definitionAttributes.add(new ModelMetaInfo().setId("title"));
		definitionAttributes.add(new ModelMetaInfo().setId("description"));
		modelsMetaInfo.setDefinitions(definitionAttributes);
		modelsMetaInfo.setFields(new LinkedList<>());
		modelsMetaInfo.setProperties(new LinkedList<>());
		modelsMetaInfo.setRegions(new LinkedList<>());
		models.setModelsMetaInfo(modelsMetaInfo);
		return models;
	}
}
