package com.sirma.itt.seip.instance.dao;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

import org.mockito.Matchers;
import org.mockito.Mockito;
import org.testng.Assert;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import com.sirma.itt.seip.definition.AllowedChildrenProvider;
import com.sirma.itt.seip.definition.DictionaryService;
import com.sirma.itt.seip.definition.model.AllowedChildConfigurationImpl;
import com.sirma.itt.seip.definition.model.AllowedChildDefinitionImpl;
import com.sirma.itt.seip.definition.model.BaseDefinition;
import com.sirma.itt.seip.domain.ObjectTypes;
import com.sirma.itt.seip.domain.definition.AllowedChildConfiguration;
import com.sirma.itt.seip.domain.definition.AllowedChildDefinition;
import com.sirma.itt.seip.domain.definition.AllowedChildrenModel;
import com.sirma.itt.seip.domain.definition.DefinitionModel;
import com.sirma.itt.seip.domain.instance.EmfInstance;
import com.sirma.itt.seip.domain.instance.Instance;
import com.sirma.itt.seip.domain.instance.OwnedModel;
import com.sirma.itt.seip.permissions.PermissionsEnum;
import com.sirma.itt.seip.testutil.EmfTest;

/**
 * Test for AllowedChildrenHelper class.
 *
 * @author BBonev
 */
@Test
public class AllowedChildrenHelperTest extends EmfTest {

	/** The source instance. */
	private Instance sourceInstance;

	/** The calculator. */
	private AllowedChildrenProvider<Instance> calculator;

	/** The dictionary service. */
	private DictionaryService dictionaryService;

	/** The model. */
	private TestDefinition model;

	/** The parent instance. */
	private EmfInstance parentInstance;

	/**
	 * Before method.
	 */
	@Override
	@BeforeMethod
	public void beforeMethod() {
		createTypeConverter();

		sourceInstance = createInstance("childInstance");
		parentInstance = new EmfInstance();
		sourceInstance.setIdentifier("parentInstance");
		((OwnedModel) sourceInstance).setOwningInstance(parentInstance);
		calculator = Mockito.mock(AllowedChildrenProvider.class);

		dictionaryService = Mockito.mock(DictionaryService.class);

		model = new TestDefinition();
		Mockito.when(dictionaryService.getInstanceDefinition(sourceInstance)).thenReturn(model);
		Mockito.when(dictionaryService.find(Matchers.anyString())).then(
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
				dictionaryService, ObjectTypes.TOPIC);
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
				dictionaryService, ObjectTypes.TOPIC);
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
				dictionaryService, ObjectTypes.TOPIC);
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
				dictionaryService, ObjectTypes.TOPIC);
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
				dictionaryService, ObjectTypes.TOPIC);
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
	private Instance createInstance(String id) {
		EmfInstance sourceInstance = new EmfInstance();
		sourceInstance.setIdentifier(id);
		sourceInstance.setId(id);
		return sourceInstance;
	}

	/**
	 * Creates the child def.
	 *
	 * @param id
	 *            the id
	 * @param allowed
	 *            the allowed
	 * @param denied
	 *            the denied
	 * @return the allowed child definition impl
	 */
	private AllowedChildDefinitionImpl createChildDef(String id, String allowed, String denied) {
		AllowedChildDefinitionImpl childDefinition = new AllowedChildDefinitionImpl();
		childDefinition.setType(ObjectTypes.TOPIC);
		childDefinition.setIdentifier(id);
		List<AllowedChildConfiguration> permissions = childDefinition.getPermissions();
		if (allowed != null) {
			permissions.add(createPermission(PermissionsEnum.ALLOW, allowed));
		}
		if (denied != null) {
			permissions.add(createPermission(PermissionsEnum.DENY, denied));
		}
		return childDefinition;
	}

	/**
	 * Creates the permission.
	 *
	 * @param permission
	 *            the permission
	 * @param definitions
	 *            the definitions
	 * @return the allowed child configuration impl
	 */
	private AllowedChildConfigurationImpl createPermission(PermissionsEnum permission, String definitions) {
		AllowedChildConfigurationImpl configuration = new AllowedChildConfigurationImpl();
		configuration.setXmlValues(definitions);
		configuration.setIdentifier(permission.toString());
		return configuration;
	}

	/**
	 * Buil definitions.
	 *
	 * @param id
	 *            the id
	 * @return the list
	 */
	private List<DefinitionModel> builDefinitions(String id) {
		String[] ids = id.split(",");
		List<DefinitionModel> definitions = new ArrayList<>(ids.length);
		for (int i = 0; i < ids.length; i++) {
			String string = ids[i];
			definitions.add(createDef(string));
		}
		return definitions;
	}

	/**
	 * Creates the def.
	 *
	 * @param string
	 *            the string
	 * @return the definition model
	 */
	private DefinitionModel createDef(String string) {
		TestDefinition definition = new TestDefinition();
		definition.setIdentifier(string);
		return definition;
	}

	/**
	 * The Class TestDefinition.
	 */
	private static class TestDefinition extends BaseDefinition<TestDefinition>implements AllowedChildrenModel {

		/** The allowed children. */
		private List<AllowedChildDefinition> allowedChildren = new LinkedList<AllowedChildDefinition>();

		/**
		 * Gets the allowed children.
		 *
		 * @return the allowed children
		 */
		@Override
		public List<AllowedChildDefinition> getAllowedChildren() {
			return allowedChildren;
		}

	}

}
