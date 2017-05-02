package com.sirma.itt.cmf.security.provider;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import com.sirma.itt.seip.definition.TypeMappingProvider;
import com.sirma.itt.seip.definition.util.DefinitionUtil;
import com.sirma.itt.seip.domain.definition.DefinitionModel;
import com.sirma.itt.seip.domain.definition.GenericDefinition;
import com.sirma.itt.seip.domain.definition.TransitionDefinition;
import com.sirma.itt.seip.domain.definition.Transitional;
import com.sirma.itt.seip.domain.security.Action;
import com.sirma.itt.seip.permissions.BaseDefinitionActionProvider;
import com.sirma.itt.seip.permissions.action.ActionProvider;
import com.sirma.itt.seip.plugin.Extension;

/**
 * Action provider for generic definition. It will pass before other evaluators
 *
 * @author BBonev
 */
@ApplicationScoped
@Extension(target = ActionProvider.TARGET_NAME, order = 5)
public class GenericActionProvider extends BaseDefinitionActionProvider {

	@Inject
	private TypeMappingProvider typeProvider;

	/**
	 * Initialize actions.
	 *
	 * @return the map
	 */
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
	 *
	 * @param definition
	 *            the definition
	 * @param data
	 *            the data
	 */
	private void processDefinition(DefinitionModel definition, Map<String, Action> data) {
		if (definition instanceof Transitional) {
			Class<?> instanceClass = getInstanceClass(definition);
			if (instanceClass == null) {
				return;
			}
			List<TransitionDefinition> transitions = DefinitionUtil.filterAction((Transitional) definition);

			for (TransitionDefinition transitionDefinition : transitions) {
				String key = transitionDefinition.getIdentifier();
				if (!data.containsKey(key)) {
					addAction(key, transitionDefinition.getPurpose(), transitionDefinition.getLabelId(), data);
				}
			}
		}
		// process all child definition also
		if (definition instanceof GenericDefinition) {
			for (GenericDefinition subDefinition : ((GenericDefinition) definition).getSubDefinitions()) {
				processDefinition(subDefinition, data);
			}
		}
	}

	/**
	 * Gets the instance class for the given definition.
	 *
	 * @param definition
	 *            the definition
	 * @return the instance class
	 */
	protected Class<?> getInstanceClass(DefinitionModel definition) {
		if (definition instanceof GenericDefinition) {
			return typeProvider.getInstanceClass(((GenericDefinition) definition).getType());
		}
		return getInstanceClass();
	}

	@Override
	protected Class<?> getInstanceClass() {
		// there is no generic definition for now
		return null;
	}

	@Override
	protected Class<? extends DefinitionModel> getDefinitionClass() {
		return GenericDefinition.class;
	}

}
