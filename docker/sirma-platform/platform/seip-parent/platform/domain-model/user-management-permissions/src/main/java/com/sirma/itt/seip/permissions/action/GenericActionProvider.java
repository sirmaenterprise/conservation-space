package com.sirma.itt.seip.permissions.action;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import com.sirma.itt.seip.definition.TransitionDefinition;
import com.sirma.itt.seip.definition.Transitional;
import com.sirma.itt.seip.definition.util.DefinitionUtil;
import com.sirma.itt.seip.domain.definition.DefinitionModel;
import com.sirma.itt.seip.domain.definition.GenericDefinition;
import com.sirma.itt.seip.domain.security.Action;
import com.sirma.itt.seip.permissions.BaseDefinitionActionProvider;
import com.sirma.itt.seip.permissions.action.ActionProvider;
import com.sirma.itt.seip.plugin.Extension;

/**
 * Action provider for generic definition. It will pass before other evaluators.
 *
 * @author BBonev
 */
@Extension(target = ActionProvider.TARGET_NAME, order = 5)
public class GenericActionProvider extends BaseDefinitionActionProvider {

	@Override
	protected Map<String, Action> initializeActions() {
		Map<String, Action> data = new LinkedHashMap<>();
		// collects all transitions from the project definitions
		List<? extends DefinitionModel> definitions = getDefinitions();
		for (DefinitionModel definition : definitions) {
			processDefinition(definition, data);
		}
		return data;
	}

	/**
	 * Process definition transitions and his sub definitions if any.
	 */
	private void processDefinition(DefinitionModel definition, Map<String, Action> data) {
		if (definition instanceof Transitional) {
			List<TransitionDefinition> transitions = DefinitionUtil.filterAction((Transitional) definition);

			for (TransitionDefinition transitionDefinition : transitions) {
				String key = transitionDefinition.getIdentifier();
				if (!data.containsKey(key)) {
					addAction(key, transitionDefinition.getPurpose(), transitionDefinition.getLabelId(), data);
				}
			}
		}
	}

	@Override
	protected Class<? extends DefinitionModel> getDefinitionClass() {
		return GenericDefinition.class;
	}
}