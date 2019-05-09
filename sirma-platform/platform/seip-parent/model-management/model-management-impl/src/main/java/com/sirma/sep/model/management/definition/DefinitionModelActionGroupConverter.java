package com.sirma.sep.model.management.definition;

import static com.sirma.sep.model.management.converter.ModelConverterUtilities.addLabels;
import static com.sirma.sep.model.management.converter.ModelConverterUtilities.addAttribute;

import java.util.LinkedHashMap;
import java.util.Map;

import javax.inject.Inject;

import com.sirma.itt.seip.collections.CollectionUtils;
import com.sirma.itt.seip.definition.TransitionGroupDefinition;
import com.sirma.itt.seip.domain.definition.GenericDefinition;
import com.sirma.sep.model.management.ModelActionGroup;
import com.sirma.sep.model.management.meta.ModelsMetaInfo;

/**
 * Converts {@link TransitionGroupDefinition} to {@link ModelActionGroup}.
 *
 * @author Boyan Tonchev.
 */
public class DefinitionModelActionGroupConverter {

	@Inject
	private LabelProvider labelProvider;

	/**
	 * Converts the {@link TransitionGroupDefinition}s from the provided {@link GenericDefinition} into {@link ModelActionGroup}s.
	 *
	 * @param definition the definition which will be processed
	 * @param modelsMetaInfo meta information mapping.
	 * @return list of converted {@link ModelActionGroup}. It may be empty if the provided definition has no {@link TransitionGroupDefinition}.
	 */
	public Map<String, ModelActionGroup> constructModelActionGroups(GenericDefinition definition, ModelsMetaInfo modelsMetaInfo) {
		return definition.getTransitionGroups()
				.stream()
				.map(actionGroup -> buildModelActionGroup(actionGroup, modelsMetaInfo))
				.collect(CollectionUtils.toIdentityMap(ModelActionGroup::getId, LinkedHashMap::new));
	}

	private ModelActionGroup buildModelActionGroup(TransitionGroupDefinition actionGroup, ModelsMetaInfo modelsMetaInfo) {
		ModelActionGroup actionGroupModel = new ModelActionGroup();
		actionGroupModel.setModelsMetaInfo(modelsMetaInfo);
		actionGroupModel.setId(actionGroup.getIdentifier());

		addAttribute(actionGroupModel, DefinitionModelAttributes.LABEL_ID, actionGroup.getLabelId());
		addLabels(actionGroup, actionGroupModel, labelProvider::getLabels);

		addAttribute(actionGroupModel, DefinitionModelAttributes.ID, actionGroup.getIdentifier());
		addAttribute(actionGroupModel, DefinitionModelAttributes.PARENT, actionGroup.getParent());
		addAttribute(actionGroupModel, DefinitionModelAttributes.ORDER, actionGroup.getOrder());
		addAttribute(actionGroupModel, DefinitionModelAttributes.TYPE, actionGroup.getType());

		actionGroupModel.setAsDeployed();
		return actionGroupModel;
	}
}
