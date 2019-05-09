package com.sirma.sep.model.management.definition.export;

import static com.sirma.sep.model.management.converter.ModelConverterUtilities.copyAttribute;
import static com.sirma.sep.model.management.converter.ModelConverterUtilities.toDisplayType;
import static com.sirma.sep.model.management.converter.ModelConverterUtilities.toInteger;

import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.inject.Inject;

import com.sirma.itt.seip.collections.CollectionUtils;
import com.sirma.itt.seip.definition.WritablePropertyDefinition;
import com.sirma.itt.seip.definition.model.ControlDefinitionImpl;
import com.sirma.itt.seip.definition.model.ControlParamImpl;
import com.sirma.itt.seip.definition.model.FieldDefinitionImpl;
import com.sirma.itt.seip.definition.model.PropertyDefinitionProxy;
import com.sirma.itt.seip.domain.definition.ControlDefinition;
import com.sirma.itt.seip.domain.definition.ControlParam;
import com.sirma.itt.seip.domain.definition.DataTypeDefinition;
import com.sirma.itt.seip.domain.definition.DisplayType;
import com.sirma.itt.seip.domain.definition.GenericDefinition;
import com.sirma.itt.seip.domain.definition.PropertyDefinition;
import com.sirma.itt.semantic.NamespaceRegistryService;
import com.sirma.sep.model.management.ModelAttribute;
import com.sirma.sep.model.management.ModelControl;
import com.sirma.sep.model.management.ModelControlParam;
import com.sirma.sep.model.management.ModelField;
import com.sirma.sep.model.management.ModelHeader;
import com.sirma.sep.model.management.definition.DefinitionModelAttributes;
import com.sirma.sep.model.management.definition.DefinitionModelControlParams;
import com.sirma.sep.model.management.meta.ControlOption;
import com.sirma.sep.model.management.meta.ControlOptionParam;
import com.sirma.sep.model.management.meta.ModelControlMetaInfo;

/**
 * Converter for copying {@link ModelField} attributes into {@link PropertyDefinition}.
 *
 * @author <a href="mailto:borislav.bonev@sirma.bg">Borislav Bonev</a>
 * @author Mihail Radkov
 */
class PropertyDefinitionConverter {

	private final NamespaceRegistryService namespaceRegistryService;
	private static final String TEMPLATE = "template";
	private static final String CONTROL = "control";

	/**
	 * Constructs the converter with the provided namespace registry for URI conversions.
	 *
	 * @param namespaceRegistryService used for URI conversions
	 */
	@Inject
	public PropertyDefinitionConverter(
			NamespaceRegistryService namespaceRegistryService) {
		this.namespaceRegistryService = namespaceRegistryService;
	}

	/**
	 * Creates a new {@link PropertyDefinition} from the provided {@link ModelField} attributes.
	 *
	 * @param modelField the field with which attributes the new property will be created
	 * @return the newly created property
	 */
	PropertyDefinition newProperty(ModelField modelField) {
		PropertyDefinitionProxy proxy = create();
		copyField(modelField, proxy);
		return proxy;
	}

	/**
	 * Creates new header {@link PropertyDefinition} for the given header key.
	 * <p>
	 * It'll generate label id based on the {@link GenericDefinition} identifier and the header key, e.g. <code>ID_KEY</code>
	 *
	 * @param definition used to generate header's label identifier
	 * @param headerKey used to generate headers' label identifier and is used as the property's identifier
	 * @return newly constructed header property
	 */
	PropertyDefinition newHeaderProperty(GenericDefinition definition, String headerKey) {
		PropertyDefinitionProxy proxy = create();
		proxy.setIdentifier(headerKey);
		proxy.setLabelId(generateHeaderId(definition, headerKey));
		return proxy;
	}

	/**
	 * Creates new system {@link PropertyDefinition} from the provided {@link ModelField}
	 *
	 * @param modelField the field which to be converted to system property
	 * @return the newly created property
	 */
	PropertyDefinition newSystemProperty(ModelField modelField) {
		PropertyDefinitionProxy proxy = create();
		proxy.setIdentifier(modelField.getId());
		proxy.setDisplayType(DisplayType.SYSTEM);
		return proxy;
	}

	/**
	 * Copies the attributes of the provided {@link ModelField} into the given {@link PropertyDefinition}
	 *
	 * @param modelField the field which attributes will be copied into the property
	 * @param propertyDefinition the property into which attributes will be copied
	 */
	void copyField(ModelField modelField, WritablePropertyDefinition propertyDefinition) {
		copyBaseAttributes(modelField, propertyDefinition);

		Collection<ModelControl> modelControls = modelField.getControls();
		Map<String, ModelControl> parentControlsMap = CollectionUtils.emptyMap();
		if (modelField.hasParent()) {
			parentControlsMap = getParentControls(modelField.getParentReference()).entrySet()
					.stream()
					.filter(entry -> !modelField.isDetached(modelField, CONTROL, entry.getKey()))
					.collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
		}
		// Add parent control first if any exist. This is needed because we should not lose controls.
		// For example if in parent definition there's control RICHTEXT and we want to add DEFAULT_VALUE_PATTERN 
		// than in child definition both controls should be available.
		if (modelField.hasParent() && controlsAreDifferent(modelField, parentControlsMap)) {
			Stream<ModelControl> mergedControls = Stream.of(modelControls, parentControlsMap.values()).flatMap(Collection::stream);
			modelControls = mergedControls.collect(Collectors.toList());
		}
		copyControls(modelControls, parentControlsMap, propertyDefinition);
	}

	private static Map<String, ModelControl> getParentControls(ModelField modelField) {
		Map<String, ModelControl> parentControls = modelField.getControlsMap();
		if (parentControls.isEmpty() && modelField.hasParent()) {
			parentControls = getParentControls(modelField.getParentReference());
		}
		return parentControls;
	}

	private static boolean controlsAreDifferent(ModelField modelField, Map<String, ModelControl> parentControls) {
		Map<String, ModelControl> fieldControls = modelField.getControlsMap();
		if (parentControls.isEmpty() || fieldControls.isEmpty()) {
			return false;
		}
		return !parentControls.keySet().equals(fieldControls.keySet());
	}

	/**
	 * Normalizes the properties of a header {@link PropertyDefinition}
	 *
	 * @param header model header used to retrieve the actual {@link DefinitionModelAttributes#LABEL_ID} value
	 * @param propertyDefinition the property to normalize
	 */
	void normalizeHeaderProperty(ModelHeader header, WritablePropertyDefinition propertyDefinition) {
		copyAttribute(header, DefinitionModelAttributes.LABEL_ID, propertyDefinition::setLabelId);
		propertyDefinition.setType(DataTypeDefinition.ANY);
		propertyDefinition.setDisplayType(DisplayType.SYSTEM);
	}

	private void copyBaseAttributes(ModelField modelField, WritablePropertyDefinition property) {
		property.setIdentifier(modelField.getId());
		copyAttribute(modelField, DefinitionModelAttributes.URI, this::getShortUri, property::setUri);
		copyAttribute(modelField, DefinitionModelAttributes.VALUE, Object::toString, property::setValue);
		copyAttribute(modelField, DefinitionModelAttributes.TYPE, property::setType);
		copyAttribute(modelField, DefinitionModelAttributes.RNC, property::setRnc);
		copyAttribute(modelField, DefinitionModelAttributes.ORDER, toInteger(), property::setOrder);
		copyAttribute(modelField, DefinitionModelAttributes.CODE_LIST, toInteger(), property::setCodelist);
		copyAttribute(modelField, DefinitionModelAttributes.PREVIEW_EMPTY, property::setPreviewEmpty);
		copyAttribute(modelField, DefinitionModelAttributes.MULTI_VALUED, property::setMultiValued);
		copyAttribute(modelField, DefinitionModelAttributes.MANDATORY, property::setMandatory);
		copyAttribute(modelField, DefinitionModelAttributes.DISPLAY_TYPE, toDisplayType(), property::setDisplayType);
		copyAttribute(modelField, DefinitionModelAttributes.LABEL_ID, property::setLabelId);
		copyAttribute(modelField, DefinitionModelAttributes.TOOLTIP_ID, property::setTooltipId);
		copyCodeListFromParent(modelField, property);
	}

	private void copyCodeListFromParent(ModelField modelField, WritablePropertyDefinition property) {
		ModelField parentReference = modelField.getParentReference();
		if (parentReference != null) {
			Optional<ModelAttribute> parentCodeListAttr = parentReference.findAttribute(DefinitionModelAttributes.CODE_LIST);
			Optional<ModelAttribute> codeListAttr = modelField.getAttribute(DefinitionModelAttributes.CODE_LIST);
			Optional<ModelAttribute> typeOptionAttr = modelField.getAttribute(DefinitionModelAttributes.TYPE_OPTION);

			// If the typeOption of the modelField is 'CODELIST' and the codeList attribute does not have value then
			// the value of the codeList attribute should be inherited
			if (typeOptionAttr.isPresent() && typeOptionAttr.get().getValue().equals("CODELIST")
					&& parentCodeListAttr.isPresent() && !codeListAttr.isPresent()) {
				property.setCodelist((Integer) parentCodeListAttr.get().getValue());
			}
		}
	}

	private static void copyControls(Collection<ModelControl> modelControls, Map<String, ModelControl> parentControls,
			WritablePropertyDefinition propertyDefinition) {
		if (modelControls.isEmpty()) {
			propertyDefinition.setControlDefinition(null);
			return;
		}
		ModelControl control = null;
		List<ControlOptionParam> controlOptions = new LinkedList<>();
		// If we have two controls here then one of it is DEFAULT_VALUE_PATTERN. In this case we have to extract control params
		// from DEFAULT_VALUE_PATTERN and to append them to the other control
		if (modelControls.size() > 1) {
			ModelControl defaultValuePattern = modelControls.stream()
					.filter(modelControl -> modelControl.getId().equalsIgnoreCase(DefinitionModelControlParams.DEFAULT_VALUE_PATTERN))
					.findFirst()
					.orElseThrow(() -> new IllegalArgumentException("Missing DEFAULT_VALUE_PATTERN control. Control params can not be extracted."));
			control = modelControls.stream()
					.filter(modelControl -> !modelControl.getId().equalsIgnoreCase(DefinitionModelControlParams.DEFAULT_VALUE_PATTERN))
					.findFirst()
					.orElseThrow(() -> new IllegalArgumentException("Missing control to which DEFAULT_VALUE_PATTERN control params to be added."));
			control.getControlParamsMap().put(TEMPLATE, defaultValuePattern.getControlParamsMap().get(TEMPLATE));
			fillControlOptionParams(defaultValuePattern, controlOptions);
		} else {
			control = modelControls.iterator().next();
		}

		ControlDefinition controlDefinition = propertyDefinition.getControlDefinition();
		if (controlDefinition == null) {
			controlDefinition = new ControlDefinitionImpl();
		}
		controlDefinition.setIdentifier(control.getId());
		propertyDefinition.setControlDefinition(controlDefinition);
		Map<String, ModelControlParam> parentControlParams = null;
		if (parentControls != null && parentControls.get(control.getId()) != null) {
			parentControlParams = parentControls.get(control.getId()).getControlParamsMap();
		}
		fillControlOptionParams(control, controlOptions);
		copyControlParams(control.getControlParams(), parentControlParams, controlDefinition, controlOptions);
	}

	private static void fillControlOptionParams(ModelControl control, List<ControlOptionParam> controlOptionParams) {
		List<ControlOption> controlOptions = ((ModelControlMetaInfo) control.getAttributesMetaInfo()
				.get(DefinitionModelAttributes.ID))
				.getControlOptions()
				.stream()
				.filter(option -> option.getId().equalsIgnoreCase(control.getId()))
				.collect(Collectors.toList());
		if (!controlOptions.isEmpty()) {
			controlOptionParams.addAll(controlOptions.get(0).getParams());
		}
	}

	private static void copyControlParams(Collection<ModelControlParam> modelControlParams,
			Map<String, ModelControlParam> parentControlParams, ControlDefinition controlDefinition,
			List<ControlOptionParam> controlOptions) {
		if (controlOptions.isEmpty()) {
			return;
		}
		List<ControlParam> controlParams = new LinkedList<>();
		for (ModelControlParam controlParam : modelControlParams) {
			copyControlParam(controlParams, controlParam, controlOptions);
		}
		if (parentControlParams != null) {
			parentControlParams.forEach((key, value) -> copyControlParam(controlParams, value, controlOptions));
		}
		((ControlDefinitionImpl) controlDefinition).setControlParams(controlParams);
	}

	private static void copyControlParam(List<ControlParam> controlParams, ModelControlParam controlParam,
			List<ControlOptionParam> controlOptions) {
		if (controlParam == null || containsControlParam(controlParams, controlParam.getId())) {
			return;
		}
		ControlOptionParam controlOption = controlOptions.stream()
				.filter(option -> option.getId().equalsIgnoreCase(controlParam.getId()))
				.findFirst()
				.orElseThrow(() -> new IllegalArgumentException("Missing control param options for parameter with id: " + controlParam.getId()));
		ControlParamImpl writeableControlParam = new ControlParamImpl();
		controlParams.add(writeableControlParam);
		writeableControlParam.setIdentifier(controlParam.getId());
		writeableControlParam.setName(controlOption.getName());
		writeableControlParam.setType(controlOption.getType());
		copyAttribute(controlParam, DefinitionModelAttributes.VALUE, writeableControlParam::setValue);
	}

	private static boolean containsControlParam(List<ControlParam> controlParams, String id) {
		return controlParams.stream()
				.anyMatch(controlParam -> controlParam.getIdentifier().equals(id));
	}

	private String getShortUri(Object uri) {
		return namespaceRegistryService.getShortUri(uri.toString());
	}

	private static PropertyDefinitionProxy create() {
		PropertyDefinitionProxy proxy = new PropertyDefinitionProxy();
		WritablePropertyDefinition fieldDefinition = new FieldDefinitionImpl();
		proxy.setTarget(fieldDefinition);
		return proxy;
	}

	private static String generateHeaderId(GenericDefinition definition, String headerKey) {
		return definition.getIdentifier() + "." + headerKey;
	}
}
