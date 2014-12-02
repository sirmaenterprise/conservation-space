package com.sirma.itt.emf.security.provider;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import javax.inject.Inject;

import com.sirma.itt.emf.definition.DefinitionUtil;
import com.sirma.itt.emf.definition.DictionaryService;
import com.sirma.itt.emf.definition.model.TransitionDefinition;
import com.sirma.itt.emf.definition.model.Transitional;
import com.sirma.itt.emf.domain.Pair;
import com.sirma.itt.emf.domain.model.DefinitionModel;
import com.sirma.itt.emf.label.LabelProvider;
import com.sirma.itt.emf.security.ActionProvider;
import com.sirma.itt.emf.security.model.Action;
import com.sirma.itt.emf.security.model.EmfAction;

/**
 * Base implementation for {@link ActionProvider} that can handle definition operations.
 *
 * @author BBonev
 */
public abstract class BaseDefinitionActionProvider implements ActionProvider {

	/** The label provider. */
	@Inject
	protected LabelProvider labelProvider;
	/** The dictionary service. */
	@Inject
	protected DictionaryService dictionaryService;

	/**
	 * Initialize actions.
	 * 
	 * @return the map
	 */
	protected Map<Pair<Class<?>, String>, Action> initializeActions() {
		Map<Pair<Class<?>, String>, Action> data = new LinkedHashMap<Pair<Class<?>, String>, Action>();
		Pair<Class<?>, String> key = new Pair<Class<?>, String>(getInstanceClass(), null);
		// collects all transitions from the project definitions
		List<? extends DefinitionModel> definitions = getDefinitions();
		for (DefinitionModel definition : definitions) {
			if (definition instanceof Transitional) {
				List<TransitionDefinition> transitions = DefinitionUtil.filterTransitionsByPurpose(
						(Transitional) definition, DefinitionUtil.TRANSITION_PERPOSE_ACTION);

				for (TransitionDefinition transitionDefinition : transitions) {
					key.setSecond(transitionDefinition.getIdentifier());
					if (!data.containsKey(key)) {
						addAction(key, transitionDefinition.getLabelId(), data);
					}
				}
			}
		}
		return data;
	}

	/**
	 * Gets the definitions.
	 *
	 * @return the definitions
	 */
	protected List<? extends DefinitionModel> getDefinitions() {
		return dictionaryService.getAllDefinitions(getDefinitionClass());
	}

	/**
	 * Gets the actions preffix.
	 *
	 * @return the actions preffix
	 */
	protected abstract Class<?> getInstanceClass();

	/**
	 * Gets the definition class.
	 *
	 * @return the definition class
	 */
	protected abstract Class<? extends DefinitionModel> getDefinitionClass();

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Map<Pair<Class<?>, String>, Action> provide() {
		return getData();
	}

	/**
	 * Gets the data.
	 *
	 * @return the data
	 */
	protected Map<Pair<Class<?>, String>, Action> getData() {
			initializeActions();
		return initializeActions();
	}

	/**
	 * Adds the action.
	 *
	 * @param key
	 *            the key
	 * @param labelId
	 *            a label id to fetch the label
	 * @param map
	 *            the map
	 */
	protected void addAction(Pair<Class<?>, String> key, String labelId, Map<Pair<Class<?>, String>, Action> map) {
		map.put(key.clone(), createAction(key.getSecond(), labelId));
	}

	/**
	 * Creates the action.
	 *
	 * @param actionId
	 *            the action id
	 * @param labelId a label id to fetch the label
	 * @return the action
	 */
	protected Action createAction(String actionId, String labelId) {
		EmfAction action = new EmfAction(actionId, labelProvider);
		String value = labelId;
		if (value == null) {
			value = actionId;
		}
		action.setLabel(value);
		action.setConfirmationMessage(actionId + ".confirm");
		action.setDisabledReason(actionId + ".disabled.reason");
		// for now this cannot be called because the disabled reason will cause an exception to be
		// thrown because there is no session for the label provider
		// if (StringUtils.isNotNullOrEmpty(action.getDisabledReason())) {
		// action.setDisabled(true);
		// }
		action.setIconImagePath(getIconImagePath(actionId));
		return action;
	}

	/**
	 * Gets the icon image path.
	 *
	 * @param imageName
	 *            the image name
	 * @return the icon image path
	 */
	protected String getIconImagePath(String imageName) {
		return "images:icon_" + imageName + ".png";
	}

}