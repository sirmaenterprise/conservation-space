package com.sirma.itt.emf.audit.observer;

import static com.sirma.itt.seip.util.EqualsHelper.getOrDefault;

import javax.inject.Inject;
import javax.inject.Singleton;

import com.sirma.itt.seip.definition.SemanticDefinitionService;
import com.sirma.itt.seip.domain.instance.PropertyInstance;

/**
 * Resolve action types of properties and relations using the model. The implementation relies on the semantic model to
 * provide the needed information.<br>
 * The service provides action types based on the relation was added, removed or changed
 *
 * @author BBonev
 */
@Singleton
public class PropertyActionResolver {

	@Inject
	private SemanticDefinitionService semanticDefinitionService;

	/**
	 * Checks if the given property is applicable for audit logging.
	 *
	 * @param propertyName
	 *            the property name to check for
	 * @return <code>true</code>, if is auditable property and <code>false</code> if it's not
	 */
	public boolean isAuditableProperty(String propertyName) {
		PropertyInstance relation = semanticDefinitionService.getRelation(propertyName);
		return isAuditable(relation);
	}

	private static boolean isAuditable(PropertyInstance relation) {
		return relation != null && relation.isAuditable();
	}

	/**
	 * Gets the adds the action for property.
	 *
	 * @param propertyName
	 *            the property name
	 * @return the adds the action for property
	 */
	public String getAddActionForProperty(String propertyName) {
		return toActionType(propertyName).getOnAdd();
	}

	/**
	 * Gets the removes the action for property.
	 *
	 * @param propertyName
	 *            the property name
	 * @return the removes the action for property
	 */
	public String getRemoveActionForProperty(String propertyName) {
		return toActionType(propertyName).getOnRemove();
	}

	/**
	 * Gets the change action for property.
	 *
	 * @param propertyName
	 *            the property name
	 * @return the change action for property
	 */
	public String getChangeActionForProperty(String propertyName) {
		return toActionType(propertyName).getOnChange();
	}

	private ActionType toActionType(String propertyName) {
		PropertyInstance relation = semanticDefinitionService.getRelation(propertyName);
		if (!isAuditable(relation)) {
			return ActionType.EMPTY;
		}
		return ActionType.parse(relation.getAuditEvent());
	}

	private static class ActionType {
		private static final ActionType EMPTY = new ActionType(null, null, null);

		private final String onAdd;
		private final String onRemove;
		private final String onChange;

		private ActionType(String onAdd, String onRemove, String onChange) {
			this.onAdd = onAdd;
			this.onRemove = onRemove;
			this.onChange = onChange;
		}

		/**
		 * Parses the given event type
		 *
		 * @param eventType
		 *            the event type
		 * @return the action type
		 */
		static ActionType parse(String eventType) {
			if (eventType == null) {
				return EMPTY;
			}

			String[] split = eventType.split("\\|");

			String onAdd = null;
			String onRemove = null;
			String onChange = null;

			for (int i = 0; i < split.length; i++) {
				String action = split[i];
				if (action.startsWith("+")) {
					onAdd = action.substring(1);
				} else if (action.startsWith("-")) {
					onRemove = action.substring(1);
				} else {
					onChange = action;
				}
			}

			// if no specific types are specified for add or remove use the one for change
			onAdd = getOrDefault(onAdd, onChange);
			onRemove = getOrDefault(onRemove, onChange);

			return new ActionType(onAdd, onRemove, onChange);
		}

		public String getOnAdd() {
			return onAdd;
		}

		public String getOnRemove() {
			return onRemove;
		}

		public String getOnChange() {
			return onChange;
		}
	}
}
