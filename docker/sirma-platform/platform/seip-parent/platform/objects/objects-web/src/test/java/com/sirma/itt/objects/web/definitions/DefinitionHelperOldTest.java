package com.sirma.itt.objects.web.definitions;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.json.JSONObject;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.testng.Assert;
import org.testng.annotations.Test;

import com.sirma.itt.objects.web.definitions.DefinitionHelperOld;
import com.sirma.itt.seip.definition.DefinitionService;
import com.sirma.itt.seip.definition.model.BaseRegionDefinition;
import com.sirma.itt.seip.definition.model.ControlDefinitionImpl;
import com.sirma.itt.seip.definition.model.FieldDefinitionImpl;
import com.sirma.itt.seip.domain.Identity;
import com.sirma.itt.seip.domain.Ordinal;
import com.sirma.itt.seip.domain.definition.ControlParam;
import com.sirma.itt.seip.domain.definition.DataTypeDefinition;
import com.sirma.itt.seip.domain.definition.DisplayType;
import com.sirma.itt.seip.domain.definition.PropertyDefinition;
import com.sirma.itt.seip.domain.definition.label.LabelProvider;
import com.sirma.itt.seip.domain.instance.EmfInstance;
import com.sirma.itt.seip.domain.instance.Instance;
import com.sirma.itt.seip.domain.rest.BadRequestException;
import com.sirma.itt.seip.domain.rest.InternalServerErrorException;
import com.sirma.itt.seip.instance.state.StateService;
import com.sirma.itt.seip.instance.state.StateTransitionManager;
import com.sirma.itt.seip.io.ResourceLoadUtil;
import com.sirma.itt.seip.testutil.mocks.ControlParamMock;
import com.sirma.itt.seip.testutil.mocks.DataTypeDefinitionMock;
import com.sirma.itt.seip.testutil.mocks.PropertyDefinitionMock;

import net.javacrumbs.jsonunit.JsonAssert;

/**
 * TODO TO BE REMOVED WHEN THE WEB IS MERGED
 * <p>
 * Tests for the helper class.
 *
 * @author svelikov
 */
public class DefinitionHelperOldTest {

	@InjectMocks
	private DefinitionHelperOld definitionHelper = new DefinitionHelperOld();

	@Mock
	protected DefinitionService definitionService;

	@Mock
	protected StateTransitionManager transitionManager;

	@Mock
	private StateService stateService;

	/**
	 * Instantiates a new definition helper test.
	 */
	public DefinitionHelperOldTest() {
		MockitoAnnotations.initMocks(this);
	}

	@Test(expectedExceptions = BadRequestException.class)
	public void loadDefinition_throws_exception_when_missing_arguments() {
		definitionHelper.loadDefinition(null, null);
	}

	@Test(expectedExceptions = BadRequestException.class)
	public void loadDefinition_throws_exception_when_arguments_are_empty() {
		definitionHelper.loadDefinition("", "");
	}

	@Test(expectedExceptions = InternalServerErrorException.class)
	public void toJsonModel_throws_exception_when_missing_arguments() {
		definitionHelper.toJsonModel(null, null, null, null);
	}

	@Test
	public void toJsonModel_returns_empty_model() {
		Map<String, ?> externalModel = new HashMap<>();
		List<Ordinal> definitionFields = new LinkedList<>();
		LabelProvider labelProvider = Mockito.mock(LabelProvider.class);
		JSONObject jsonModel = definitionHelper.toJsonModel(externalModel, definitionFields, labelProvider,
				new HashSet<>());
		JsonAssert.assertJsonEquals("{\"validationModel\":{},\"viewModel\":{\"fields\":[]}}", jsonModel.toString());
	}

	@Test
	public void toJsonModel_builds_model_for_new_instance() {
		Map<String, Object> externalModel = new HashMap<>();
		externalModel.put("title", null);
		externalModel.put("status", "DRAFT");
		externalModel.put("identifier", null);
		externalModel.put("controlField", "COL1");

		List<Ordinal> definitionFields = new LinkedList<>();
		definitionFields.add(buildFieldDefinition("title", DataTypeDefinition.TEXT, DataTypeDefinition.TEXT,
				DisplayType.EDITABLE, true, null, null));

		definitionFields.add(buildFieldDefinition("status", DataTypeDefinition.TEXT, DataTypeDefinition.TEXT,
				DisplayType.READ_ONLY, false, null, null));

		definitionFields.add(buildFieldDefinition("identifier", DataTypeDefinition.TEXT, DataTypeDefinition.TEXT,
				DisplayType.READ_ONLY, false, "1234", null));

		definitionFields.add(buildFieldDefinition("description", DataTypeDefinition.TEXT, DataTypeDefinition.TEXT,
				DisplayType.READ_ONLY, false, "${expression}", "rezolved expression"));

		PropertyDefinition fieldWithControl = buildFieldDefinition("controlField", DataTypeDefinition.TEXT,
				DataTypeDefinition.TEXT, DisplayType.EDITABLE, false, "COL1", "COL1");
		addControlDefinition((PropertyDefinitionMock) fieldWithControl);
		definitionFields.add(fieldWithControl);

		LabelProvider labelProvider = Mockito.mock(LabelProvider.class);
		Mockito.when(labelProvider.getLabel(Mockito.anyString())).thenReturn("field.label");

		JSONObject jsonModel = definitionHelper.toJsonModel(externalModel, definitionFields, labelProvider,
				new HashSet<>());
		JsonAssert.assertJsonEquals(ResourceLoadUtil.loadResource(getClass(), "definition-model-response.json"),
				jsonModel.toString());
	}

	private void addControlDefinition(PropertyDefinitionMock propertyDefinition) {
		ControlDefinitionImpl controlDefinition = new ControlDefinitionImpl();
		controlDefinition.setIdentifier("RADIO_BUTTON_GROUP");

		List<ControlParam> controlParams = new ArrayList<>();
		ControlParamMock param1 = new ControlParamMock();
		param1.setName("layout");
		param1.setValue("pageDirection");
		controlParams.add(param1);
		controlDefinition.setControlParams(controlParams);

		List<PropertyDefinition> fields = new ArrayList<>();
		PropertyDefinitionMock field1 = (PropertyDefinitionMock) buildFieldDefinition(null, DataTypeDefinition.TEXT,
				DataTypeDefinition.TEXT, DisplayType.EDITABLE, false, null, null);
		// PropertyDefinitionMock field1 = new PropertyDefinitionMock();
		field1.setName("COL1");
		field1.setLabelId("field1.label");
		fields.add(field1);
		controlDefinition.setFields(fields);

		propertyDefinition.setControlDefinition(controlDefinition);
	}

	private static PropertyDefinition buildFieldDefinition(String identifier, String type, String datatype,
			DisplayType displayType, boolean isMandatory, String defaultValue, String value) {
		PropertyDefinitionMock definition = new PropertyDefinitionMock();
		definition.setIdentifier(identifier);
		definition.setType(type);
		DataTypeDefinitionMock typeDefinition = new DataTypeDefinitionMock(String.class, datatype);
		typeDefinition.setName(DataTypeDefinition.TEXT);
		definition.setDataType(typeDefinition);
		definition.setDisplayType(displayType);
		definition.setMandatory(isMandatory);
		definition.setMultiValued(Boolean.TRUE);
		if (defaultValue != null) {
			definition.setDefaultValue(defaultValue);
		}
		if (value != null) {
			definition.setValue(value);
		}
		return definition;
	}

	@Test(expectedExceptions = InternalServerErrorException.class)
	public void collectSortedFields_throws_exception_when_missing_model() {
		definitionHelper.collectAllFields(null);
	}

	@Test
	public void collectAllFields_returns_empty_fields_list_when_no_fields_are_found() {
		BaseRegionDefinition<BaseRegionDefinition<?>> definitionModel = new BaseRegionDefinition<>();
		List<PropertyDefinition> fields = new LinkedList<>();
		definitionModel.setFields(fields);
		List<Ordinal> sortedFields = definitionHelper.collectAllFields(definitionModel);
		Assert.assertNotNull(sortedFields);
		Assert.assertTrue(sortedFields.isEmpty());
	}

	@Test
	public void collectAllFields_returns_sorted_fields_representable_fields_only() {
		BaseRegionDefinition<BaseRegionDefinition<?>> definitionModel = buildDefinitionModel();
		List<Ordinal> sortedFields = definitionHelper.collectAllFields(definitionModel);
		Assert.assertNotNull(sortedFields);
		Assert.assertTrue(sortedFields.size() == 3);
		Assert.assertTrue(sortedFields.get(0).getOrder() == 1);
		Assert.assertTrue(sortedFields.get(1).getOrder() == 2);
		Assert.assertTrue(sortedFields.get(2).getOrder() == 3);
	}

	@Test
	public void collectMandatoryFields_returns_mandatory_field_definitions() {
		Set<String> reqFields = buildMandatoryFieldIdsList();
		BaseRegionDefinition<BaseRegionDefinition<?>> definitionModel = buildDefinitionModel();
		List<Ordinal> mandatoryFields = definitionHelper.collectMandatoryFields(definitionModel, reqFields);
		Assert.assertTrue(mandatoryFields.size() == 1);
		Assert.assertEquals(((Identity) mandatoryFields.get(0)).getIdentifier(), "title");
	}

	@Test(expectedExceptions = InternalServerErrorException.class)
	public void getMandatoryFieldIds_throws_exception_when_missing_required_arguments() {
		definitionHelper.getMandatoryFieldIds(null, null, null);
	}

	@Test
	public void getMandatoryFieldIds_collects_and_returns_mandatory_field_ids() {
		Mockito.when(stateService.getPrimaryState(Mockito.any(Instance.class))).thenReturn("some_state");
		Set<String> reqFields = buildMandatoryFieldIdsList();
		Instance instance = new EmfInstance();
		Mockito.when(transitionManager.getRequiredFields(instance, "some_state", "approve")).thenReturn(reqFields);
		Set<String> mandatoryFieldIds = definitionHelper.getMandatoryFieldIds(buildDefinitionModel(), instance,
				"approve");
		Assert.assertEquals(mandatoryFieldIds, reqFields);
	}

	private static Set<String> buildMandatoryFieldIdsList() {
		Set<String> reqFields = new HashSet<>();
		reqFields.add("title");
		reqFields.add("status");
		return reqFields;
	}

	private static FieldDefinitionImpl createField(String id, int order, boolean isMandatory) {
		FieldDefinitionImpl property = new FieldDefinitionImpl();
		property.setIdentifier(id);
		property.setOrder(order);
		property.setMandatory(isMandatory);
		return property;
	}

	private static BaseRegionDefinition<BaseRegionDefinition<?>> buildDefinitionModel() {
		BaseRegionDefinition<BaseRegionDefinition<?>> definitionModel = new BaseRegionDefinition<>();
		List<PropertyDefinition> fields = new LinkedList<>();
		definitionModel.setFields(fields);
		fields.add(createField("tooltip_header", 3, false));
		fields.add(createField("size", 2, false));
		fields.add(createField("title", 1, true));
		return definitionModel;
	}

}
