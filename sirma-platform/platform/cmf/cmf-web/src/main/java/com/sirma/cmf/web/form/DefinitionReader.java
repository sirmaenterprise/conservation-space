package com.sirma.cmf.web.form;

import java.io.Serializable;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.faces.component.UIComponent;
import javax.faces.component.html.HtmlOutputText;
import javax.faces.component.html.HtmlPanelGroup;
import javax.faces.context.FacesContext;

import org.richfaces.component.UIOutputPanel;

import com.sirma.cmf.web.form.builder.FormBuilder;
import com.sirma.cmf.web.form.builder.FormBuilderHelper;
import com.sirma.cmf.web.util.CmfRncUtil;
import com.sirma.cmf.web.util.Priority;
import com.sirma.itt.commons.utils.string.StringUtils;
import com.sirma.itt.seip.Pair;
import com.sirma.itt.seip.definition.util.PathHelper;
import com.sirma.itt.seip.domain.Ordinal;
import com.sirma.itt.seip.domain.PathElement;
import com.sirma.itt.seip.domain.definition.Condition;
import com.sirma.itt.seip.domain.definition.Conditional;
import com.sirma.itt.seip.domain.definition.ControlDefinition;
import com.sirma.itt.seip.domain.definition.ControlParam;
import com.sirma.itt.seip.domain.definition.DataTypeDefinition;
import com.sirma.itt.seip.domain.definition.DefinitionModel;
import com.sirma.itt.seip.domain.definition.DisplayType;
import com.sirma.itt.seip.domain.definition.PropertyDefinition;
import com.sirma.itt.seip.domain.definition.RegionDefinition;
import com.sirma.itt.seip.domain.instance.DefaultProperties;
import com.sirma.itt.seip.domain.instance.PropertyModel;

/**
 * Reader for definition properties.
 *
 * @author svelikov
 */
public class DefinitionReader extends DefinitionReaderBase implements Reader, Serializable {

	private static final long serialVersionUID = -2039853826732307612L;

	private static final String FIELD_PATH_REPLACEMENT_PATTERN = "/|\\$|:";

	private final Map<Pair<String, Priority>, List<Condition>> conditions = new HashMap<>();

	private final List<String> basePaths = new ArrayList<>();

	/** Fields that should not be rendered on the page. */
	private static final List<String> SKIP_FIELDS = new ArrayList<>(Arrays.asList(
			DefaultProperties.HEADER_DEFAULT, DefaultProperties.HEADER_COMPACT, DefaultProperties.HEADER_BREADCRUMB));

	@Override
	public void readSortables(List<Ordinal> sortables, DefinitionModel definitionModel, PropertyModel propertyModel,
			UIComponent container, FormViewMode formViewMode, String rootInstanceName, Set<String> requiredFiedls) {
		if (hasMissingArguments(sortables, propertyModel, container)) {
			return;
		}
		initReader();
		createInstanceName(propertyModel, rootInstanceName);
		conditions.clear();
		for (Ordinal field : sortables) {
			readField(field, propertyModel, formViewMode, container, requiredFiedls);
		}
		setContainerDoneStyleClass(container);
		exportCmfRncJson(definitionModel, propertyModel);
	}

	/**
	 * Export cmf rnc json.
	 *
	 * @param definitionModel
	 *            the definition model
	 * @param propertyModel
	 *            the property model
	 */
	private void exportCmfRncJson(DefinitionModel definitionModel, PropertyModel propertyModel) {

		// This is the base part for the ID-s for fields used inside rnc
		// expressions. This string is used in client-side rnc module to build
		// field id-s.
		String basePath = PathHelper.getPath((PathElement) definitionModel) + "_";
		basePath = basePath.replaceAll(FIELD_PATH_REPLACEMENT_PATTERN, "_");

		// Save condition is one that is applied on root definition tag and
		// defines the condition which must be applied on save action button.
		String saveCondition = null;
		if (definitionModel instanceof Condition) {
			saveCondition = ((Condition) definitionModel).getExpression();
		}
		Map<String, List<Object>> scriptResultMapper = new HashMap<>();
		Map<Pair<String, Priority>, List<Condition>> sortedConditionsByPriority = CmfRncUtil
				.sortConditionsByPriority(conditions);

		// Call utility function to create JSON to be injected in the generated form
		String json = CmfRncUtil.createCmfRncJson(sortedConditionsByPriority, basePath, saveCondition,
				scriptResultMapper);

		if (!StringUtils.isNullOrEmpty(json)) {
			Map<String, Object> viewMap = FacesContext.getCurrentInstance().getViewRoot().getViewMap();
			viewMap.put(basePath, json);
			basePaths.add(basePath);
			viewMap.put("basePaths", basePaths);
		}
	}

	/**
	 * Read field property that may be field or region.
	 *
	 * @param field
	 *            the field
	 * @param propertyModel
	 *            the property model
	 * @param formViewMode
	 *            the form view mode
	 * @param container
	 *            the container
	 * @param requiredFiedls
	 *            the list of required fields
	 */
	private void readField(Ordinal field, PropertyModel propertyModel, FormViewMode formViewMode, UIComponent container,
			Set<String> requiredFiedls) {

		try {
			if (field instanceof PropertyDefinition) {
				readFieldType((PropertyDefinition) field, propertyModel, formViewMode, container, requiredFiedls);
			} else if (field instanceof RegionDefinition) {
				readFieldType((RegionDefinition) field, propertyModel, formViewMode, container, requiredFiedls);
			} else {
				log.error("Not supported Sortable implementation for DefinitionReader.readField!");
			}
		} catch (Exception e) {
			log.error("Error reading definition!", e);
			throw new RuntimeException("Error reading definition", e);
		}
	}

	/**
	 * Read field of type PropertyDefinition.
	 *
	 * @param field
	 *            the field
	 * @param propertyModel
	 *            the property model
	 * @param formViewMode
	 *            the form view mode
	 * @param container
	 *            the container
	 * @param requiredFiedls
	 *            the list of required fields
	 */
	private void readFieldType(PropertyDefinition field, PropertyModel propertyModel, FormViewMode formViewMode,
			UIComponent container, Set<String> requiredFiedls) {

		if (SKIP_FIELDS.contains(field.getIdentifier())) {
			return;
		}

		callBuilderOnField(field, propertyModel, formViewMode, container,
				requiredFiedls.contains(field.getIdentifier()));

		extractAvailableConditions(field, field.getName());

		readControlConditions(field);
	}

	/**
	 * Read field of type RegionDefinition.
	 *
	 * @param field
	 *            the field
	 * @param propertyModel
	 *            the property model
	 * @param formViewMode
	 *            the form view mode
	 * @param container
	 *            the container
	 * @param requiredFiedls
	 *            the list of required fields
	 */
	private void readFieldType(RegionDefinition field, PropertyModel propertyModel, FormViewMode formViewMode,
			UIComponent container, Set<String> requiredFiedls) {
		buildRegion(field, propertyModel, formViewMode, container, requiredFiedls);

		RegionDefinition regionDefinition = field;
		extractAvailableConditions(field, regionDefinition.getIdentifier());

		// for regions we should get the conditions from region fields too
		List<PropertyDefinition> regionFields = regionDefinition.getFields();
		for (PropertyDefinition regionField : regionFields) {
			extractAvailableConditions(regionField, regionField.getName());
		}
	}

	/**
	 * If the field has control inside, then check if the control has fields and load the conditions.
	 *
	 * @param field
	 *            the field
	 */
	private void readControlConditions(PropertyDefinition field) {
		if (field.getControlDefinition() != null) {
			List<PropertyDefinition> fields = field.getControlDefinition().getFields();
			for (PropertyDefinition propertyDefinition : fields) {
				extractAvailableConditions(propertyDefinition, propertyDefinition.getName());
			}
		}
	}

	/**
	 * get field condition expressions and put them in conditions map.
	 *
	 * @param field
	 *            the field
	 * @param name
	 *            the name
	 */
	private void extractAvailableConditions(Conditional field, String name) {

		List<Condition> fieldConditions = field.getConditions();
		String propertyName = getIdForField((PathElement) field, name);

		if (fieldConditions != null && !fieldConditions.isEmpty()) {
			Pair<String, Priority> key = null;

			if (field instanceof RegionDefinition) {
				key = new Pair<>(propertyName, Priority.FIRST);
			} else if (field instanceof PropertyDefinition) {
				key = new Pair<>(propertyName, Priority.SECOND);
			}

			conditions.put(key, fieldConditions);
		}
	}

	/**
	 * Gets the id for field using the property definition argument.
	 *
	 * @param pathElement
	 *            the path element
	 * @param propertyName
	 *            the property name
	 * @return the id for field
	 */
	protected String getIdForField(PathElement pathElement, String propertyName) {
		return propertyName;
	}

	/**
	 * Initialize builder for the field and invoke it.
	 *
	 * @param field
	 *            the field
	 * @param propertyModel
	 *            the property model
	 * @param formViewMode
	 *            the form view mode
	 * @param container
	 *            the container
	 * @param isRequired
	 *            if the fields is dynamically required
	 */
	private void callBuilderOnField(PropertyDefinition field, PropertyModel propertyModel, FormViewMode formViewMode,
			UIComponent container, boolean isRequired) {
		Object propertyValue = propertyModel.getProperties().get(field.getName());

		DisplayType displayType = field.getDisplayType();
		String type = field.getDataType().getName();
		if (log.isTraceEnabled()) {
			String message = MessageFormat.format(
					"CMFWeb: Reading property name: [{0}] with type: [{1}], value: [{2}] and DisplayType: [{3}]",
					field.getName(), type, propertyValue, displayType.name());
			log.trace(message);
		}

		// define builder type for current property
		FormBuilder builder = null;

		ControlDefinition controlDefinition = field.getControlDefinition();

		// if there is a control attached to the field, then go render it
		BuilderType builderType = null;
		if (controlDefinition != null) {
			String controlId = controlDefinition.getIdentifier().toUpperCase();
			builderType = BuilderType.getType(controlId);
			if (builderType == null) {
				log.warn("A web control with ID=" + controlId + " is not supported, yet!");
			}
		} else if (DataTypeDefinition.TEXT.equals(type) || DataTypeDefinition.INT.equals(type)
				|| DataTypeDefinition.FLOAT.equals(type) || DataTypeDefinition.DOUBLE.equals(type)
				|| DataTypeDefinition.LONG.equals(type)) {

			// a filed with a type text can be rendered as single or multyline field
			// The field has codelist attached.
			// !!! The fields with codelists can be rendered as controls or as normal fields
			// depending on whether there is a control definition inside the field.
			if (field.getCodelist() != null) {
				builderType = BuilderType.SELECT_ONE_MENU;
			} else if (isMultyline(field)) {
				// If the field has length more than one line (40chars)
				builderType = BuilderType.MULTY_LINE_FIELD;
			} else {
				// The field is a single line text field
				builderType = BuilderType.SINGLE_LINE_FIELD;
			}
		} else if (DataTypeDefinition.BOOLEAN.equals(type)) {

			// boolean is rendered as checkbox
			builderType = BuilderType.CHECKBOX_FIELD;
		} else if (DataTypeDefinition.DATE.equals(type) || DataTypeDefinition.DATETIME.equals(type)) {

			// date and datetime are rendered as text field with date picker and date/datetime
			// converter attached
			builderType = BuilderType.DATE_FIELD;
		}

		if (builderType != null) {
			builder = initBuilder(builderType, container, field, propertyValue, formViewMode, isRequired);
		}

		if (builder != null) {
			builder.build();
		}
	}

	/**
	 * Builds the region.
	 *
	 * @param field
	 *            the field
	 * @param propertyModel
	 *            the property model
	 * @param formViewMode
	 *            the form view mode
	 * @param container
	 *            the container
	 * @param requiredFiedls
	 *            the list of required fields
	 */
	private void buildRegion(RegionDefinition field, PropertyModel propertyModel, FormViewMode formViewMode,
			UIComponent container, Set<String> requiredFiedls) {

		RegionDefinition regionDefinition = field;

		DisplayType regionDisplayType = regionDefinition.getDisplayType();
		// render the region only if its display type is EDITABLE or
		// READ_ONLY
		if (DisplayType.EDITABLE == regionDisplayType || DisplayType.READ_ONLY == regionDisplayType) {

			// create the region panel
			UIOutputPanel regionPanel = createRegionPanel(regionDefinition);

			// create a label for the region
			HtmlOutputText regionLabel = getRegionLabel(regionDefinition);
			if (regionLabel != null) {
				regionPanel.getChildren().add(regionLabel);
			}

			// build the region content body panel
			HtmlPanelGroup regionBody = (HtmlPanelGroup) new FormBuilderHelper()
					.getComponent(ComponentType.OUTPUT_PANEL);
			regionBody.getAttributes().put("styleClass", "region-body");
			regionPanel.getChildren().add(regionBody);

			// read the fields from the region and place them inside
			List<PropertyDefinition> fields = regionDefinition.getFields();
			for (Ordinal regionField : fields) {
				readField(regionField, propertyModel, formViewMode, regionBody, requiredFiedls);
			}

			if (!hasVisibleFields(fields)) {
				String styleClass = (String) regionPanel.getAttributes().get("styleClass");
				styleClass += " hide";
				regionPanel.getAttributes().put("styleClass", styleClass);
			}

			container.getChildren().add(regionPanel);
		}
	}

	/**
	 * Checks for visible fields in region.
	 *
	 * @param fields
	 *            the fields
	 * @return true, if successful
	 */
	private static boolean hasVisibleFields(List<PropertyDefinition> fields) {
		boolean hasVisibleFields = false;
		for (PropertyDefinition propertyDefinition : fields) {
			String fieldDisplayType = propertyDefinition.getDisplayType().name();
			if (DisplayType.EDITABLE.name().equalsIgnoreCase(fieldDisplayType)
					|| DisplayType.READ_ONLY.name().equalsIgnoreCase(fieldDisplayType)) {
				hasVisibleFields = true;
				break;
			}
		}
		return hasVisibleFields;
	}

	/**
	 * Creates the region panel.
	 *
	 * @param regionDefinition
	 *            the region definition
	 * @return the uI output panel
	 */
	private UIOutputPanel createRegionPanel(RegionDefinition regionDefinition) {
		UIOutputPanel regionPanel = new UIOutputPanel();

		ControlDefinition controlDefinition = regionDefinition.getControlDefinition();

		log.trace("CMFWeb: Found region with ID: [" + regionDefinition.getIdentifier() + "] - going to render it");

		// determine region panel id
		String panelId = regionDefinition.getIdentifier();
		if (StringUtils.isNullOrEmpty(panelId)) {
			log.warn("CMFWeb: Missing regionDefinition ID. Please check definition!");
		} else {
			regionPanel.setId(panelId);
		}

		StringBuilder styleClass = new StringBuilder("generated-region");
		styleClass.append(" region-").append(panelId);

		if (controlDefinition != null) {

			// get and apply style class if is defined
			List<ControlParam> controlParams = controlDefinition.getControlParams();
			if (controlParams != null) {
				ControlParam styleClassParameter = new FormBuilderHelper().getParameterByName("STYLE_CLASS",
						controlParams);
				if (styleClassParameter != null && StringUtils.isNotNullOrEmpty(styleClassParameter.getValue())) {
					styleClass.append(" ").append(styleClassParameter.getValue());
				}
			}
		}

		regionPanel.getAttributes().put("styleClass", styleClass.toString());

		return regionPanel;
	}

	/**
	 * Gets the region label.
	 *
	 * @param regionDefinition
	 *            the region definition
	 * @return the region label
	 */
	private static HtmlOutputText getRegionLabel(RegionDefinition regionDefinition) {

		String label = regionDefinition.getLabel();
		String tooltip = regionDefinition.getTooltip();

		HtmlOutputText regionLabel = null;
		if (StringUtils.isNotNullOrEmpty(label)) {
			regionLabel = (HtmlOutputText) new FormBuilderHelper().getComponent(ComponentType.OUTPUT_TEXT);
			regionLabel.setValue(label);
			String styleClass = BuilderCssConstants.CMF_GENERATED_REGION_LABEL + " "
					+ BuilderCssConstants.CMF_DYNAMIC_GROUP_LABEL;
			regionLabel.getAttributes().put("styleClass", styleClass);
		}

		if (StringUtils.isNotNullOrEmpty(tooltip)) {
			// add tooltip
		}

		return regionLabel;
	}

	/**
	 * Creates the instance name.
	 *
	 * @param propertyModel
	 *            the property model
	 * @param rootInstanceName
	 *            the root instance name
	 */
	private void createInstanceName(PropertyModel propertyModel, String rootInstanceName) {

		String modelInstanceName = StringUtils.lowerFirstChar(propertyModel.getClass().getSimpleName());
		setInstance(propertyModel);

		if (StringUtils.isNullOrEmpty(rootInstanceName)) {
			setInstanceName(modelInstanceName);
		} else {
			// think if this can be leaved as is
			// assume instance name that is passed is a map with instance
			// wrappers like: workflowsHistoryMap[1]
			// we should attach the actual instance name
			setInstanceName(rootInstanceName + "." + modelInstanceName);

			setBaseInstanceName(rootInstanceName);
		}
	}

	/**
	 * Checks for missing arguments.
	 *
	 * @param sortables
	 *            the sortables
	 * @param propertyModel
	 *            the property model
	 * @param container
	 *            the container
	 * @return true, if successful
	 */
	private boolean hasMissingArguments(List<Ordinal> sortables, PropertyModel propertyModel, UIComponent container) {
		StringBuilder argumentsCheck = new StringBuilder(
				"CMFWeb: DefinitionReader.read is missing required arguments: ");

		boolean missingRequiredArguments = false;
		if (sortables == null) {
			missingRequiredArguments = true;
			argumentsCheck.append(" List<Sortable>");
		}
		if (propertyModel == null) {
			missingRequiredArguments = true;
			argumentsCheck.append(" PropertyModel");
		}
		if (container == null) {
			missingRequiredArguments = true;
			argumentsCheck.append(" UIComponent [container panel]");
		}
		if (missingRequiredArguments) {
			log.warn(argumentsCheck);
		}

		return missingRequiredArguments;
	}

}
