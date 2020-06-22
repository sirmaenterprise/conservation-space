package com.sirma.sep.model.management.definition;

import static com.sirma.sep.model.management.converter.ModelConverterUtilities.addAttribute;

import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import javax.inject.Inject;

import com.sirma.itt.seip.collections.CollectionUtils;
import com.sirma.itt.seip.definition.model.ControlDefinitionImpl;
import com.sirma.itt.seip.domain.definition.ControlDefinition;
import com.sirma.itt.seip.domain.definition.ControlParam;
import com.sirma.itt.seip.domain.definition.PropertyDefinition;
import com.sirma.sep.model.management.ModelControl;
import com.sirma.sep.model.management.ModelControlParam;
import com.sirma.sep.model.management.meta.ControlOption;
import com.sirma.sep.model.management.meta.ControlOptionParam;
import com.sirma.sep.model.management.meta.ModelControlMetaInfo;
import com.sirma.sep.model.management.meta.ModelsMetaInfo;

/**
 * Converts {@link ControlDefinition} to {@link ModelControl}.
 *
 * @author Stella Djulgerova.
 */
public class DefinitionModelControlConverter {

	private final DefinitionModelControlParamConverter definitionModelControlParamConverter;

	@Inject
	public DefinitionModelControlConverter(DefinitionModelControlParamConverter definitionModelControlParamConverter) {
		this.definitionModelControlParamConverter = definitionModelControlParamConverter;
	}

	public Map<String, ModelControl> constructModelControls(PropertyDefinition propertyDefinition,
			ModelsMetaInfo modelsMetaInfo) {
		List<ControlDefinition> controls = new LinkedList<>();
		ControlDefinitionImpl existingControl = (ControlDefinitionImpl) propertyDefinition.getControlDefinition();
		if (existingControl != null) {
			Map<Boolean, List<ControlParam>> controlParamsMap = propertyDefinition.getControlDefinition()
					.getControlParams()
					.stream()
					.collect(Collectors.groupingBy(controlParam -> DefinitionModelControlParams.DEFAULT_VALUE_PATTERN
							.equalsIgnoreCase(controlParam.getType())));

			List<ControlParam> defaultValuePattern = controlParamsMap.get(true);
			List<ControlParam> allOthers = controlParamsMap.get(false);

			// For a field with control in it there are two possible cases:
			// - The control can have only it's own control params
			// - The control can have it's own control params plus DEFAULT_VALUE_PATTERN param
			// If there's not mixed control params we remain the control unchanged.
			// If DEFAULT_VALUE_PATTERN is present we'll have two collections with control params and we have to
			// split the control in two different controls in the if below.
			if (CollectionUtils.isNotEmpty(defaultValuePattern) && !existingControl.getIdentifier()
					.equalsIgnoreCase(DefinitionModelControlParams.DEFAULT_VALUE_PATTERN)) {
				ControlDefinitionImpl newControl = new ControlDefinitionImpl();
				newControl.setIdentifier(DefinitionModelControlParams.DEFAULT_VALUE_PATTERN);
				newControl.setControlParams(defaultValuePattern);
				controls.add(newControl);
				if (CollectionUtils.isNotEmpty(allOthers)) {
					existingControl.setControlParams(allOthers);
				}
			}
			controls.add(existingControl);
		}
		return controls.stream()
				.map(control -> buildModelControl(control, modelsMetaInfo))
				.collect(CollectionUtils.toIdentityMap(ModelControl::getId, LinkedHashMap::new));
	}

	private ModelControl buildModelControl(ControlDefinition control, ModelsMetaInfo modelsMetaInfo) {
		ModelControl modelControl = new ModelControl();
		modelControl.setModelsMetaInfo(modelsMetaInfo);
		modelControl.setId(control.getIdentifier());

		addAttribute(modelControl, DefinitionModelAttributes.ID, control.getIdentifier());

		Map<String, ModelControlParam> modelControlParams = definitionModelControlParamConverter
				.constructModelControlParams(control, modelsMetaInfo);
		buildMissingControlParams(modelControlParams, control, modelsMetaInfo);
		modelControl.setControlParams(modelControlParams);
		modelControl.setAsDeployed();
		return modelControl;
	}

	private void buildMissingControlParams(Map<String, ModelControlParam> modelControlParams, ControlDefinition control,
			ModelsMetaInfo modelsMetaInfo) {

		List<ControlOption> controlOptions = ((ModelControlMetaInfo) modelsMetaInfo.getControlsMapping()
				.get(DefinitionModelAttributes.ID))
				.getControlOptions()
				.stream()
				.filter(op -> op.getId().equalsIgnoreCase(control.getIdentifier()))
				.collect(Collectors.toList());

		if (!controlOptions.isEmpty()) {
			List<String> requiredControlParams = controlOptions.get(0)
					.getParams()
					.stream()
					.map(ControlOptionParam::getId)
					.collect(Collectors.toList());

			control.getControlParams()
					.forEach(controlParam -> requiredControlParams.remove(controlParam.getIdentifier()));

			requiredControlParams.forEach(paramName -> {
				ModelControlParam modelControlParam = definitionModelControlParamConverter
						.buildModelControlParam(paramName, paramName, paramName, "", modelsMetaInfo);
				modelControlParams.put(paramName, modelControlParam);
			});
		}

	}
}
