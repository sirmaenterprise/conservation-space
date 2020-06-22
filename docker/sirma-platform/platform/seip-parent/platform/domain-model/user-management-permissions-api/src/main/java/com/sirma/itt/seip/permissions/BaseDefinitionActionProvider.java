package com.sirma.itt.seip.permissions;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import javax.inject.Inject;

import com.sirma.itt.seip.definition.DefinitionService;
import com.sirma.itt.seip.definition.TransitionDefinition;
import com.sirma.itt.seip.definition.Transitional;
import com.sirma.itt.seip.definition.util.DefinitionUtil;
import com.sirma.itt.seip.domain.definition.DefinitionModel;
import com.sirma.itt.seip.domain.definition.label.LabelProvider;
import com.sirma.itt.seip.domain.security.Action;
import com.sirma.itt.seip.permissions.action.ActionProvider;
import com.sirma.itt.seip.permissions.action.EmfAction;

/**
 * Base implementation for {@link ActionProvider} that can handle definition operations.
 *
 * @author BBonev
 */
public abstract class BaseDefinitionActionProvider implements ActionProvider {

	@Inject
	protected LabelProvider labelProvider;

	@Inject
	protected DefinitionService definitionService;

	/**
	 * Initialize actions.
	 *
	 * @return the map
	 */
	protected Map<String, Action> initializeActions() {
		Map<String, Action> data = new LinkedHashMap<>();
		// collects all transitions from the project definitions
		for (DefinitionModel definition : getDefinitions()) {
			if (definition instanceof Transitional) {
				List<TransitionDefinition> transitions = DefinitionUtil.filterAction((Transitional) definition);
				for (TransitionDefinition transitionDefinition : transitions) {
					String key = transitionDefinition.getIdentifier();
					String purpose = transitionDefinition.getPurpose();
					if (!data.containsKey(key)) {
						addAction(key, purpose, transitionDefinition.getLabelId(), data);
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
	protected List<DefinitionModel> getDefinitions() {
		return definitionService.getAllDefinitions(getDefinitionClass());
	}

	/**
	 * Gets the definition class.
	 *
	 * @return the definition class
	 */
	protected abstract Class<? extends DefinitionModel> getDefinitionClass();

	@Override
	public Map<String, Action> provide() {
		return getData();
	}

	/**
	 * Gets the data.
	 *
	 * @return the data
	 */
	protected Map<String, Action> getData() {
		return initializeActions();
	}

	/**
	 * Adds the action.
	 *
	 * @param key
	 *            the key
	 * @param purpose
	 *            action purpose
	 * @param labelId
	 *            a label id to fetch the label
	 * @param map
	 *            the map
	 */
	protected void addAction(String key, String purpose, String labelId, Map<String, Action> map) {
		map.put(key, createAction(key, purpose, labelId, labelProvider));
	}

	/**
	 * Creates the action.
	 *
	 * @param actionId
	 *            the action id
	 * @param purpose
	 *            action purpose
	 * @param labelId
	 *            a label id to fetch the label
	 * @param labelProvider
	 *            from which to retrieve the label
	 * @return the action
	 */
	public static Action createAction(String actionId, String purpose, String labelId, LabelProvider labelProvider) {
		EmfAction action = new EmfAction(actionId, labelProvider);
		String value = labelId;
		if (value == null) {
			value = actionId;
		}
		action.setLabel(value);
		action.setPurpose(purpose);
		action.setConfirmationMessage(actionId + ".confirm");
		action.setDisabledReason(actionId + ".disabled.reason");
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
	private static String getIconImagePath(String imageName) {
		return "images:icon_" + imageName + ".png";
	}
}
