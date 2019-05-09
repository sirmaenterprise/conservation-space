package com.sirma.sep.model.management.definition;

import static com.sirma.sep.model.management.converter.ModelConverterUtilities.addAttribute;

import java.lang.invoke.MethodHandles;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.function.BinaryOperator;
import java.util.function.Function;
import java.util.stream.Collector;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sirma.itt.seip.domain.definition.ControlDefinition;
import com.sirma.itt.seip.domain.definition.ControlParam;
import com.sirma.sep.model.management.ModelControlParam;
import com.sirma.sep.model.management.meta.ModelsMetaInfo;

/**
 * Converts {@link ControlParam} to {@link ModelControlParam}.
 *
 * @author Stella Djulgerova.
 */
public class DefinitionModelControlParamConverter {

	private static final Logger LOGGER = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

	public Map<String, ModelControlParam> constructModelControlParams(ControlDefinition controlDefinition, ModelsMetaInfo modelsMetaInfo) {

		return controlDefinition.getControlParams().stream()
				.map(controlParam -> buildModelControlParam(controlParam, modelsMetaInfo))
				.collect(toControlParamMap(controlDefinition));
	}

	private ModelControlParam buildModelControlParam(ControlParam controlParam, ModelsMetaInfo modelsMetaInfo) {
		return buildModelControlParam(controlParam.getIdentifier(), controlParam.getName(), controlParam.getType(),
				controlParam.getValue(), modelsMetaInfo);
	}

	public ModelControlParam buildModelControlParam(String id, String name, String type, String value,
			ModelsMetaInfo modelsMetaInfo) {
		ModelControlParam modelControlParam = new ModelControlParam();
		modelControlParam.setModelsMetaInfo(modelsMetaInfo);
		modelControlParam.setId(id);

		addAttribute(modelControlParam, DefinitionModelAttributes.ID, id);
		addAttribute(modelControlParam, DefinitionModelAttributes.NAME, name);
		addAttribute(modelControlParam, DefinitionModelAttributes.TYPE, type);
		addAttribute(modelControlParam, DefinitionModelAttributes.VALUE, value);

		modelControlParam.setAsDeployed();
		return modelControlParam;
	}

	private static Collector<ModelControlParam, ?, Map<String, ModelControlParam>> toControlParamMap(ControlDefinition controlDefinition) {
		return Collectors.toMap(ModelControlParam::getId, Function.identity(), duplicateParamMerger(controlDefinition), LinkedHashMap::new);
	}

	private static BinaryOperator<ModelControlParam> duplicateParamMerger(ControlDefinition controlDefinition) {
		return (p1, p2) -> {
			LOGGER.warn("Duplicate control param {} in control {}", p1.getId(), controlDefinition.getIdentifier());
			return p2;
		};
	}

}
