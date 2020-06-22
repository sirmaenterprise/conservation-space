package com.sirma.itt.seip.instance.dao;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

import org.mockito.Matchers;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.testng.Assert;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import com.sirma.itt.seip.definition.AllowedChildConfiguration;
import com.sirma.itt.seip.definition.AllowedChildDefinition;
import com.sirma.itt.seip.definition.AllowedChildrenModel;
import com.sirma.itt.seip.definition.AllowedChildrenProvider;
import com.sirma.itt.seip.definition.DefinitionService;
import com.sirma.itt.seip.definition.model.AllowedChildConfigurationImpl;
import com.sirma.itt.seip.definition.model.AllowedChildDefinitionImpl;
import com.sirma.itt.seip.definition.model.BaseDefinition;
import com.sirma.itt.seip.domain.ObjectTypes;
import com.sirma.itt.seip.domain.definition.DefinitionModel;
import com.sirma.itt.seip.domain.instance.EmfInstance;
import com.sirma.itt.seip.domain.instance.Instance;
import com.sirma.itt.seip.testutil.EmfTest;

/**
 * Test for AllowedChildrenHelper class.
 *
 * @author BBonev
 */
@Test
public class AllowedChildrenHelperTest extends EmfTest {

	private Instance sourceInstance;

	private AllowedChildrenProvider<Instance> calculator;

	private DefinitionService definitionService;

	private TestDefinition model;

	private EmfInstance parentInstance;

	@Override
	@BeforeMethod
	public void beforeMethod() {
		createTypeConverter();
		MockitoAnnotations.initMocks(this);
		sourceInstance = createInstance("childInstance");
		parentInstance = new EmfInstance("emf:parentId");
		sourceInstance.setIdentifier("parentInstance");
		contextService.bindContext(sourceInstance, parentInstance);
		calculator = Mockito.mock(AllowedChildrenProvider.class);

		definitionService = Mockito.mock(DefinitionService.class);

		model = new TestDefinition();
		Mockito.when(definitionService.getInstanceDefinition(sourceInstance)).thenReturn(model);
		Mockito.when(definitionService.find(Matchers.anyString())).then(
				a -> createDef(a.getArgumentAt(0, String.class)));

		Mockito.when(calculator.getDefinition(sourceInstance)).thenReturn(model);
		Mockito.when(calculator.getDefinition(ObjectTypes.TOPIC)).thenReturn(DefinitionModel.class);

		Mockito.when(calculator.getAllDefinitions(sourceInstance, ObjectTypes.TOPIC)).thenReturn(
				builDefinitions("def1,def5,def2,def3,def4"));
	}

	/**
	 * Test get allowed children_default.
	 */
	public void testGetAllowedChildren_default() {

		model.getAllowedChildren().add(createChildDef("def1", null, null));
		model.getAllowedChildren().add(createChildDef("def2", null, null));

		Mockito.when(calculator.calculateActive(sourceInstance, ObjectTypes.TOPIC)).thenReturn(false);

		List<DefinitionModel> allowedChildren = AllowedChildrenHelper.getAllowedChildren(sourceInstance, calculator,
				definitionService, ObjectTypes.TOPIC);
		Assert.assertFalse(allowedChildren.isEmpty());
		Assert.assertEquals(allowedChildren.size(), 2);
	}

	/**
	 * Test get allowed children_ active_true_1.
	 */
	public void testGetAllowedChildren_Active_true_1() {

		model.getAllowedChildren().add(createChildDef("def1", "def1,def2,def3", "def4"));
		model.getAllowedChildren().add(createChildDef("all", null, null));

		Mockito.when(calculator.calculateActive(sourceInstance, ObjectTypes.TOPIC)).thenReturn(true);
		Mockito.when(calculator.getActive(sourceInstance, ObjectTypes.TOPIC)).thenReturn(
				Arrays.asList(createInstance("def1")));

		List<DefinitionModel> allowedChildren = AllowedChildrenHelper.getAllowedChildren(sourceInstance, calculator,
				definitionService, ObjectTypes.TOPIC);
		Assert.assertFalse(allowedChildren.isEmpty());
		Assert.assertEquals(allowedChildren.size(), 4);
	}

	/**
	 * Test get allowed children_ active_true_2.
	 */
	public void testGetAllowedChildren_Active_true_2() {

		model.getAllowedChildren().add(createChildDef("def1", "def1,def2,def3", "def4"));
		model.getAllowedChildren().add(createChildDef("all", null, "def2"));

		Mockito.when(calculator.calculateActive(sourceInstance, ObjectTypes.TOPIC)).thenReturn(true);
		Mockito.when(calculator.getActive(sourceInstance, ObjectTypes.TOPIC)).thenReturn(
				Arrays.asList(createInstance("def1")));

		List<DefinitionModel> allowedChildren = AllowedChildrenHelper.getAllowedChildren(sourceInstance, calculator,
				definitionService, ObjectTypes.TOPIC);
		Assert.assertFalse(allowedChildren.isEmpty());
		Assert.assertEquals(allowedChildren.size(), 3);
	}

	/**
	 * Test get allowed children_ active_true_3.
	 */
	public void testGetAllowedChildren_Active_true_3() {

		model.getAllowedChildren().add(createChildDef("def1", "def1,def2,def3", "def4"));
		model.getAllowedChildren().add(createChildDef("def2", "def3", "def5"));
		model.getAllowedChildren().add(createChildDef("all", null, null));

		Mockito.when(calculator.calculateActive(sourceInstance, ObjectTypes.TOPIC)).thenReturn(true);
		Mockito.when(calculator.getActive(sourceInstance, ObjectTypes.TOPIC)).thenReturn(
				Arrays.asList(createInstance("def1"), createInstance("def2")));

		List<DefinitionModel> allowedChildren = AllowedChildrenHelper.getAllowedChildren(sourceInstance, calculator,
				definitionService, ObjectTypes.TOPIC);
		Assert.assertFalse(allowedChildren.isEmpty());
		Assert.assertEquals(allowedChildren.size(), 3);
	}

	/**
	 * Test get allowed children_ active_false.
	 */
	public void testGetAllowedChildren_Active_false() {

		model.getAllowedChildren().add(createChildDef("def1", "def1,def2,def3", "def4"));
		model.getAllowedChildren().add(createChildDef("all", null, null));

		Mockito.when(calculator.calculateActive(sourceInstance, ObjectTypes.TOPIC)).thenReturn(false);

		List<DefinitionModel> allowedChildren = AllowedChildrenHelper.getAllowedChildren(sourceInstance, calculator,
				definitionService, ObjectTypes.TOPIC);
		Assert.assertFalse(allowedChildren.isEmpty());
		Assert.assertEquals(allowedChildren.size(), 5);
	}

	/**
	 * Creates the instance.
	 *
	 * @param id
	 *            the id
	 * @return the instance
	 */
	private static Instance createInstance(String id) {
		EmfInstance sourceInstance = new EmfInstance(id);
		sourceInstance.setIdentifier(id);
		return sourceInstance;
	}

	private static AllowedChildDefinitionImpl createChildDef(String id, String allowed, String denied) {
		AllowedChildDefinitionImpl childDefinition = new AllowedChildDefinitionImpl();
		childDefinition.setType(ObjectTypes.TOPIC);
		childDefinition.setIdentifier(id);
		List<AllowedChildConfiguration> permissions = childDefinition.getPermissions();
		if (allowed != null) {
			permissions.add(createPermission("ALLOW", allowed));
		}
		if (denied != null) {
			permissions.add(createPermission("DENY", denied));
		}
		return childDefinition;
	}

	private static AllowedChildConfigurationImpl createPermission(String permission, String definitions) {
		AllowedChildConfigurationImpl configuration = new AllowedChildConfigurationImpl();
		configuration.setXmlValues(definitions);
		configuration.setIdentifier(permission.toString());
		return configuration;
	}

	private static List<DefinitionModel> builDefinitions(String id) {
		String[] ids = id.split(",");
		List<DefinitionModel> definitions = new ArrayList<>(ids.length);
		for (int i = 0; i < ids.length; i++) {
			String string = ids[i];
			definitions.add(createDef(string));
		}
		return definitions;
	}

	private static DefinitionModel createDef(String string) {
		TestDefinition definition = new TestDefinition();
		definition.setIdentifier(string);
		return definition;
	}

	private static class TestDefinition extends BaseDefinition<TestDefinition> implements AllowedChildrenModel {

		private static final long serialVersionUID = 1L;

		private List<AllowedChildDefinition> allowedChildren = new LinkedList<>();

		@Override
		public List<AllowedChildDefinition> getAllowedChildren() {
			return allowedChildren;
		}
	}
}
