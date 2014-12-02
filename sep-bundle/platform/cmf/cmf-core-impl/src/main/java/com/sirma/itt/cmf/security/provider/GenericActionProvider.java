package com.sirma.itt.cmf.security.provider;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import org.apache.log4j.Logger;

import com.sirma.itt.emf.definition.DefinitionUtil;
import com.sirma.itt.emf.definition.dao.AllowedChildrenTypeProvider;
import com.sirma.itt.emf.definition.model.GenericDefinition;
import com.sirma.itt.emf.definition.model.TransitionDefinition;
import com.sirma.itt.emf.definition.model.Transitional;
import com.sirma.itt.emf.domain.Pair;
import com.sirma.itt.emf.domain.model.DefinitionModel;
import com.sirma.itt.emf.plugin.Extension;
import com.sirma.itt.emf.security.ActionProvider;
import com.sirma.itt.emf.security.model.Action;
import com.sirma.itt.emf.security.provider.BaseDefinitionActionProvider;

/**
 * Action provider for generic definition. It will pass before other evaluators
 * 
 * @author BBonev
 */
@ApplicationScoped
@Extension(target = ActionProvider.TARGET_NAME, order = 5)
public class GenericActionProvider extends BaseDefinitionActionProvider {

	/** The logger. */
	@Inject
	private Logger logger;

	/** The type provider. */
	@Inject
	private AllowedChildrenTypeProvider typeProvider;

	/**
	 * Initialize actions.
	 * 
	 * @return the map
	 */
	@Override
	protected Map<Pair<Class<?>, String>, Action> initializeActions() {
		Map<Pair<Class<?>, String>, Action> data = new LinkedHashMap<Pair<Class<?>, String>, Action>();
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
	private void processDefinition(DefinitionModel definition,
			Map<Pair<Class<?>, String>, Action> data) {
		if (definition instanceof Transitional) {
			Class<?> instanceClass = getInstanceClass(definition);
			if (instanceClass == null) {
				// logger.warn("No Instance class found for definition of type "
				// + ((GenericDefinition) definition).getType());
				return;
			}
			Pair<Class<?>, String> key = new Pair<Class<?>, String>(null, null);
			key.setFirst(instanceClass);
			List<TransitionDefinition> transitions = DefinitionUtil.filterTransitionsByPurpose(
					(Transitional) definition, DefinitionUtil.TRANSITION_PERPOSE_ACTION);

			for (TransitionDefinition transitionDefinition : transitions) {
				key.setSecond(transitionDefinition.getIdentifier());
				if (!data.containsKey(key)) {
					addAction(key, transitionDefinition.getLabelId(), data);
				}
			}
		}
		// process all child definition also
		if (definition instanceof GenericDefinition) {
			for (GenericDefinition subDefinition : ((GenericDefinition) definition)
					.getSubDefinitions()) {
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

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected Class<?> getInstanceClass() {
		// there is no generic definition for now
		return null;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected Class<? extends DefinitionModel> getDefinitionClass() {
		return GenericDefinition.class;
	}

}
