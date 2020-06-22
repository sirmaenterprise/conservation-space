package com.sirma.sep.definitions;

import static com.sirma.sep.definitions.DefinitionModelSerializationConstants.DEFAULT_VALUE_PATTERN;
import static com.sirma.sep.definitions.DefinitionModelSerializationConstants.FIELD_VALIDATION_MANDATORY_LBL;
import static com.sirma.sep.definitions.DefinitionModelSerializationConstants.FIELD_VALIDATION_NOT_MATCH_FILTER;
import static com.sirma.sep.definitions.DefinitionModelSerializationConstants.PROPERY_BINDING_KEY;
import static com.sirma.sep.definitions.DefinitionModelSerializationConstants.PROPERY_FUNCTION_KEY;
import static com.sirma.sep.definitions.DefinitionModelSerializationConstants.RELATED_FIELDS;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;

import javax.json.Json;
import javax.json.JsonObject;
import javax.json.stream.JsonGenerator;

import org.apache.commons.io.output.ByteArrayOutputStream;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.Spy;

import com.sirma.itt.seip.convert.TypeConverter;
import com.sirma.itt.seip.convert.TypeConverterUtil;
import com.sirma.itt.seip.definition.DefinitionHelper;
import com.sirma.itt.seip.definition.RegionDefinition;
import com.sirma.itt.seip.definition.util.DefinitionUtil;
import com.sirma.itt.seip.domain.Ordinal;
import com.sirma.itt.seip.domain.codelist.CodelistService;
import com.sirma.itt.seip.domain.definition.Condition;
import com.sirma.itt.seip.domain.definition.ControlDefinition;
import com.sirma.itt.seip.domain.definition.ControlParam;
import com.sirma.itt.seip.domain.definition.DataTypeDefinition;
import com.sirma.itt.seip.domain.definition.DefinitionModel;
import com.sirma.itt.seip.domain.definition.DisplayType;
import com.sirma.itt.seip.domain.definition.PropertyDefinition;
import com.sirma.itt.seip.domain.definition.label.LabelProvider;
import com.sirma.itt.seip.domain.instance.DefaultProperties;
import com.sirma.itt.seip.domain.instance.EmfInstance;
import com.sirma.itt.seip.domain.instance.Instance;
import com.sirma.itt.seip.domain.instance.InstanceReference;
import com.sirma.itt.seip.expressions.ExpressionsManager;
import com.sirma.itt.seip.instance.dao.InstanceLoadDecorator;
import com.sirma.itt.seip.instance.state.StateService;
import com.sirma.itt.seip.instance.state.StateTransitionManager;
import com.sirma.itt.seip.testutil.fakes.InstanceTypeFake;
import com.sirma.itt.seip.testutil.mocks.ConditionMock;
import com.sirma.itt.seip.testutil.mocks.ControlDefintionMock;
import com.sirma.itt.seip.testutil.mocks.DataTypeDefinitionMock;
import com.sirma.itt.seip.testutil.mocks.InstanceContextServiceMock;
import com.sirma.itt.seip.testutil.mocks.InstanceReferenceMock;
import com.sirma.itt.seip.testutil.mocks.PropertyDefinitionMock;

import net.javacrumbs.jsonunit.JsonAssert;

/**
 * Test for {@link DefinitionModelToJsonSerializerImpl}.
 *
 * @author A. Kunchev
 */
public class DefinitionModelToJsonSerializerImplTest {

	private static final String DEFINITION_MODEL_JSON = "definition-model-test.json";
	private static final String DEFINITION_MODEL_FILTERED_JSON = "definition-model-test-filtered.json";

	@InjectMocks
	private DefinitionModelToJsonSerializerImpl serializer;

	@Mock
	private LabelProvider labelProvider;

	@Mock
	private StateService stateService;

	@Mock
	private StateTransitionManager stateTransitionManager;

	@Mock
	private CodelistService codelistService;

	@Mock
	private DefinitionHelper definitionHelper;

	@Mock
	private InstanceLoadDecorator instanceLoadDecorator;

	@Mock
	private TypeConverter typeConverter;

	@Mock
	private ExpressionsManager expressionsManager;

	@Spy
	private InstanceContextServiceMock contextService;

	@Before
	public void setup() {
		MockitoAnnotations.initMocks(this);
		when(labelProvider.getValue(FIELD_VALIDATION_MANDATORY_LBL)).thenReturn("The field is mandatory!");

		TypeConverterUtil.setTypeConverter(typeConverter);
		when(typeConverter.convert(eq(InstanceReference.class), any(Instance.class)))
				.then(a -> new InstanceReferenceMock(a.getArgumentAt(1, Instance.class)));
	}

	@Test
	public void serialize() throws IOException {
		DefinitionModel model = mock(DefinitionModel.class);
		when(model.getIdentifier()).thenReturn("testDefinition");
		when(labelProvider.getValue(FIELD_VALIDATION_NOT_MATCH_FILTER)).thenReturn("Filter don't match!");

		Instance instance = prepareInstance();

		prepareStubs(model, instance);

		try (ByteArrayOutputStream stream = new ByteArrayOutputStream();
				JsonGenerator generator = Json.createGenerator(stream)) {
			generator.writeStartObject();
			serializer.serialize(model, instance, "operation", generator);
			generator.writeEnd().flush();

			ClassLoader classLoader = getClass().getClassLoader();
			JsonObject expected = Json
					.createReader(classLoader.getResourceAsStream(DEFINITION_MODEL_JSON))
						.readObject();
			JsonObject actual = Json.createReader(new ByteArrayInputStream(stream.toByteArray())).readObject();
			JsonAssert.assertJsonEquals(expected, actual);
		}
	}

	@Test
	public void serializeWithFilter() throws IOException {
		DefinitionModel model = mock(DefinitionModel.class);
		when(model.getIdentifier()).thenReturn("testDefinition");

		Instance instance = prepareInstance();

		prepareStubs(model, instance);

		try (ByteArrayOutputStream stream = new ByteArrayOutputStream();
				JsonGenerator generator = Json.createGenerator(stream)) {
			generator.writeStartObject();
			serializer.serialize(model, instance, "operation",
					new HashSet<>(Arrays.asList("type", "title", "description")), generator);
			generator.writeEnd().flush();

			ClassLoader classLoader = getClass().getClassLoader();
			JsonObject expected = Json
					.createReader(classLoader.getResourceAsStream(DEFINITION_MODEL_FILTERED_JSON))
						.readObject();
			JsonObject actual = Json.createReader(new ByteArrayInputStream(stream.toByteArray())).readObject();
			JsonAssert.assertJsonEquals(expected, actual);
		}
	}

	private EmfInstance prepareInstance() {
		Instance parentInstance = new EmfInstance();
		parentInstance.setId("parentInstanceId");
		parentInstance.add(DefaultProperties.HEADER_BREADCRUMB, "parentCompactHeader");
		InstanceTypeFake.setType(parentInstance);

		EmfInstance currentInstance = new EmfInstance();
		currentInstance.setId("currentInstanceId");
		InstanceTypeFake.setType(currentInstance);

		contextService.bindContext(currentInstance, InstanceReferenceMock.createGeneric(parentInstance));
		currentInstance.add(DefaultProperties.HEADER_BREADCRUMB, "currentCompactHeader");
		return currentInstance;
	}

	private void prepareStubs(DefinitionModel model, Instance instance) {
		when(definitionHelper.getDefinitionLabel(model)).thenReturn("testDefinitionLabel");
		List<Ordinal> fields = buildDefinitionFields();
		when(definitionHelper.collectAllFields(model)).thenReturn(fields);
		when(stateService.getPrimaryState(instance)).thenReturn("DRAFT");
		when(stateTransitionManager.getRequiredFields(instance, "DRAFT", "operation"))
				.thenReturn(Collections.emptySet());
		when(labelProvider.getValue("validation.lettersAndDigits"))
				.thenReturn("Invalid format. Use letters and digists only up to {0} signs.");
		when(labelProvider.getLabel(anyString())).thenAnswer(a -> {
			return a.getArguments()[0];
		});
		when(codelistService.getDescription(210, "testDefinition")).thenReturn("testDefinitionLabel");
	}

	private static List<Ordinal> buildDefinitionFields() {
		List<Ordinal> fields = Arrays.asList(buildIdentifierField(), buildTypeField(), buildTitleField(),
				buildCreatedOnField(), buildContentField(), buildDepartmentField(), buildEmailField(),
				buildGeneratedField(), buildRegion());
		DefinitionUtil.sort(fields);
		return fields;
	}

	private static PropertyDefinition buildIdentifierField() {
		PropertyDefinitionMock identifier = new PropertyDefinitionMock();
		identifier.setOrder(50);
		identifier.setIdentifier("identifier");
		identifier.setDisplayType(DisplayType.READ_ONLY);
		DataTypeDefinitionMock identifierDataType = new DataTypeDefinitionMock(String.class, null);
		identifierDataType.setName(DataTypeDefinition.TEXT);
		identifier.setDataType(identifierDataType);
		identifier.setMaxLength(180);
		identifier.setLabelId("Identifier");
		identifier.setMandatory(false);
		identifier.setType("an..180");
		identifier.setPreviewEmpty(true);
		List<Condition> conditions = new ArrayList<>(2);
		conditions.add(buildCondition("hidden", "HIDDEN", "+[field1] AND -[field2]"));
		conditions.add(buildCondition("readonly", "READONLY", "[field1] IN (\"0120012\")"));
		identifier.setConditions(conditions);
		return identifier;
	}

	private static Condition buildCondition(String id, String renderAs, String expression) {
		ConditionMock condition = new ConditionMock();
		condition.setIdentifier(id);
		condition.setRenderAs(renderAs);
		condition.setExpression(expression);
		return condition;
	}

	private static PropertyDefinition buildContentField() {
		PropertyDefinitionMock content = new PropertyDefinitionMock();
		content.setIdentifier("content");
		content.setOrder(40);
		content.setPreviewEmpty(true);
		content.setDisplayType(DisplayType.SYSTEM);
		DataTypeDefinitionMock contentDataType = new DataTypeDefinitionMock(String.class, null);
		contentDataType.setName(DataTypeDefinition.ANY);
		content.setDataType(contentDataType);
		content.setLabelId("documents.content");
		content.setMandatory(false);
		content.setType(DataTypeDefinition.ANY);
		return content;
	}

	private static PropertyDefinition buildCreatedOnField() {
		PropertyDefinitionMock createdOn = new PropertyDefinitionMock();
		createdOn.setIdentifier("createdOn");
		createdOn.setOrder(30);
		createdOn.setPreviewEmpty(true);
		createdOn.setDisplayType(DisplayType.HIDDEN);
		DataTypeDefinitionMock createdOnDataType = new DataTypeDefinitionMock(String.class, null);
		createdOnDataType.setName(DataTypeDefinition.DATETIME);
		createdOn.setDataType(createdOnDataType);
		createdOn.setLabelId("Created on");
		createdOn.setMandatory(false);
		createdOn.setType(DataTypeDefinition.DATETIME);
		return createdOn;
	}

	private static PropertyDefinition buildTitleField() {
		PropertyDefinitionMock title = new PropertyDefinitionMock();
		title.setIdentifier("title");
		title.setUnique(true);
		title.setOrder(20);
		title.setPreviewEmpty(true);
		title.setDisplayType(DisplayType.EDITABLE);
		title.setTooltip("Test tooltip");
		DataTypeDefinitionMock titleDataType = new DataTypeDefinitionMock(String.class, null);
		titleDataType.setName(DataTypeDefinition.TEXT);
		title.setDataType(titleDataType);
		title.setLabelId("Title");
		title.setMandatory(true);
		title.setMaxLength(100);
		title.setType("an..100");
		List<Condition> conditions = new LinkedList<>();
		ConditionMock condition = new ConditionMock();
		condition.setIdentifier("mandatoryCondition");
		condition.setExpression("+[field1] AND [field2]");
		condition.setRenderAs("MANDATORY");
		conditions.add(condition);
		title.setConditions(conditions);
		return title;
	}

	private static PropertyDefinition buildTypeField() {
		PropertyDefinitionMock type = new PropertyDefinitionMock();
		type.setIdentifier("type");
		type.setOrder(10);
		DataTypeDefinitionMock typeDataType = new DataTypeDefinitionMock(String.class, null);
		typeDataType.setName(DataTypeDefinition.TEXT);
		type.setDataType(typeDataType);
		type.setPreviewEmpty(false);
		type.setDisplayType(DisplayType.READ_ONLY);
		type.setTooltip("Test tooltip");
		type.setCodelist(210);
		type.setType("an..180");
		type.setLabelId("Type");
		type.setMaxLength(180);
		type.setMandatory(false);
		return type;
	}

	private static PropertyDefinition buildDepartmentField() {
		PropertyDefinitionMock department = new PropertyDefinitionMock();
		department.setIdentifier("department");
		department.setOrder(60);
		department.setPreviewEmpty(false);
		department.setDisplayType(DisplayType.EDITABLE);
		department.setCodelist(503);
		department.setLabelId("Department");
		department.setMaxLength(35);
		department.setMandatory(true);
		DataTypeDefinitionMock departmentDataType = new DataTypeDefinitionMock(String.class, null);
		departmentDataType.setName(DataTypeDefinition.TEXT);
		department.setDataType(departmentDataType);
		department.setType("an..35");
		department.setControlDefinition(buildControlDefinition());
		return department;
	}

	private static PropertyDefinition buildEmailField() {
		PropertyDefinitionMock email = new PropertyDefinitionMock();
		email.setIdentifier("emailAddress");
		email.setPreviewEmpty(true);
		email.setDisplayType(DisplayType.EDITABLE);
		email.setLabelId("Email address");
		email.setMandatory(true);
		DataTypeDefinitionMock emailDataType = new DataTypeDefinitionMock(String.class, null);
		emailDataType.setName(DataTypeDefinition.TEXT);
		email.setDataType(emailDataType);

		ControlDefinition emailControlDefinition = new ControlDefintionMock();
		emailControlDefinition.setIdentifier("EMAIL");
		email.setControlDefinition(emailControlDefinition);
		return email;
	}

	private static PropertyDefinition buildGeneratedField() {
		PropertyDefinitionMock generated = new PropertyDefinitionMock();
		generated.setIdentifier("generatedField");
		generated.setParentPath("definitionId");
		generated.setPreviewEmpty(true);
		generated.setDisplayType(DisplayType.EDITABLE);
		generated.setLabelId("Generated field");
		generated.setMandatory(true);
		DataTypeDefinitionMock dataType = new DataTypeDefinitionMock(String.class, null);
		dataType.setName(DataTypeDefinition.TEXT);
		generated.setDataType(dataType);

		ControlDefinition controlDefinition = new ControlDefintionMock();
		controlDefinition.setIdentifier(DEFAULT_VALUE_PATTERN);
		controlDefinition.getControlParams().addAll(
				Arrays.asList(buildControlParam("emf:creator.name", "", PROPERY_BINDING_KEY, DEFAULT_VALUE_PATTERN),
						buildControlParam("emf:description", "", PROPERY_BINDING_KEY, DEFAULT_VALUE_PATTERN),
						buildControlParam("emf:type", "", PROPERY_BINDING_KEY, DEFAULT_VALUE_PATTERN),
						buildControlParam("sequence", "", PROPERY_FUNCTION_KEY, DEFAULT_VALUE_PATTERN),
						buildControlParam("expression", "", PROPERY_FUNCTION_KEY, DEFAULT_VALUE_PATTERN)));

		generated.setControlDefinition(controlDefinition);

		return generated;
	}

	private static ControlDefinition buildControlDefinition() {
		ControlDefinition departmentControlDefinition = new ControlDefintionMock();
		departmentControlDefinition.setIdentifier(RELATED_FIELDS);
		ControlParam inclusive = buildControlParam("INCLUSIVE", "true", "filterInclusive", RELATED_FIELDS);
		departmentControlDefinition.getControlParams().addAll(
				Arrays.asList(inclusive, buildControlParam("FILTER_SOURCE", "extra1", "filterSource", RELATED_FIELDS),
						buildControlParam("RERENDER", "functional", "fieldsToRerender", RELATED_FIELDS)));
		return departmentControlDefinition;
	}

	private static ControlParam buildControlParam(String name, String value, String id, String type) {
		ControlParam param = mock(ControlParam.class);
		when(param.getIdentifier()).thenReturn(id);
		when(param.getName()).thenReturn(name);
		when(param.getValue()).thenReturn(value);
		when(param.getType()).thenReturn(type);
		return param;
	}

	private static RegionDefinition buildRegion() {
		RegionDefinition region = mock(RegionDefinition.class);
		when(region.getIdentifier()).thenReturn("descriptionRegion");
		when(region.getLabel()).thenReturn("descriptionRegionLabel");
		when(region.getDisplayType()).thenReturn(DisplayType.EDITABLE);
		PropertyDefinition descriptionField = buildDescriptionField();
		when(region.getFields()).thenReturn(Arrays.asList(descriptionField));
		when(region.getOrder()).thenReturn(100);
		return region;
	}

	private static PropertyDefinition buildDescriptionField() {
		PropertyDefinitionMock description = new PropertyDefinitionMock();
		description.setOrder(110);
		description.setIdentifier("description");
		description.setPreviewEmpty(true);
		description.setDisplayType(DisplayType.EDITABLE);
		DataTypeDefinitionMock descriptionDataType = new DataTypeDefinitionMock(String.class, null);
		descriptionDataType.setName(DataTypeDefinition.TEXT);
		description.setDataType(descriptionDataType);
		description.setLabelId("Description");
		description.setMaxLength(180);
		description.setType("an..180");
		description.setMandatory(false);
		return description;
	}
}