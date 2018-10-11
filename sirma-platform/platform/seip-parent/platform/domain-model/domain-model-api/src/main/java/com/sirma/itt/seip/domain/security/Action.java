package com.sirma.itt.seip.domain.security;

import static com.sirma.itt.seip.json.JsonUtil.addToJson;

import java.io.Serializable;
import java.util.Collection;

import javax.json.Json;
import javax.json.JsonObject;
import javax.json.JsonObjectBuilder;

import org.json.JSONArray;
import org.json.JSONObject;

import com.sirma.itt.seip.Sealable;
import com.sirma.itt.seip.domain.Identity;
import com.sirma.itt.seip.domain.Ordinal;
import com.sirma.itt.seip.domain.Purposable;
import com.sirma.itt.seip.json.JsonUtil;

/**
 * Interface that represents a concrete user action or operation.
 *
 * @author BBonev
 */
public interface Action extends Sealable, Identity, Serializable, Purposable, Ordinal {

	static final String PURPOSE_KEY = "purpose";
	static final String IMMEDIATE_KEY = "immediate";
	static final String DISABLED_KEY = "disabled";
	static final String ON_CLICK_KEY = "onClick";
	static final String DISABLED_REASON_KEY = "disabledReason";
	static final String CONFIRMATION_MESSAGE_KEY = "confirmationMessage";
	static final String LABEL_KEY = "label";
	static final String ACTION_KEY = "action";
	static final String USER_OPERATION = "userOperation";
	static final String SERVER_OPERAION = "serverOperation";
	static final String TRANSITION = "transition";
	static final String CONFIGURATION = "configuration";
	static final String TOOLTIP = "tooltip";
	static final String ACTION_PATH = "actionPath";
	/**
	 * Gets the action id that uniquely identifies the action.
	 *
	 * @return the action id
	 */
	String getActionId();

	/**
	 * Gets the evaluated label.
	 *
	 * @return the evaluated label
	 */
	String getLabel();

	/**
	 * Gets the label id.
	 *
	 * @return the label id
	 */
	String getLabelId();

	/**
	 * Checks if is disabled.
	 *
	 * @return true, if is disabled
	 */
	boolean isDisabled();

	/**
	 * Gets tooltip
	 *
	 * @return the tooltip
	 */
	String getTooltip();

	/**
	 * Gets the disabled reason.
	 *
	 * @return the disabled reason
	 */
	String getDisabledReason();

	/**
	 * Gets the confirmation message.
	 *
	 * @return the confirmation message
	 */
	String getConfirmationMessage();

	/**
	 * Gets the icon image path.
	 *
	 * @return the icon image path
	 */
	String getIconImagePath();

	/**
	 * Gets the javascript onclick attribute value.
	 *
	 * @return the onclick
	 */
	String getOnclick();

	/**
	 * Get the actionPath of the action.
	 *
	 * @return the action path
	 */
	String getActionPath();

	/**
	 * Checks if current operation is immediate.
	 *
	 * @return true, if is immediate
	 */
	boolean isImmediateAction();

	/**
	 * Checks if is local.
	 *
	 * @return true, if is local
	 */
	boolean isLocal();

	/**
	 * Provides the group to which the action is added.
	 *
	 * @return the group identifier or null if the action is not added to any group.
	 */
	String getGroup();

	/**
	 * Checks if the action should be visible for the user or not. This is intended so that the model could define
	 * actions to be included in security calculations but also to be hidden from the user. <br>
	 * By default the all actions are visible.
	 *
	 * @return true, if is visible
	 */
	default boolean isVisible() {
		return true;
	}

	/**
	 * Gets described configuration for the action, if any. The configuration should be in JSON format. It is extracted
	 * as {@link String} and then converted to {@link JsonObject}. This configuration is used as additional data to the
	 * action for specific operation and functionalities. <br>
	 * <b>Action format example:</b>
	 *
	 * <pre>
	 * {@code
	 *	<transition id="action_id" label="action_label" eventId="event_id" purpose="action" >
	 *		<fields>
	 *			<field name="field_name" type="an10">
	 *				<value>
	 *					CONFIGURATION AS JSON
	 *				</value>
	 *				<control id="CONFIGURATION">
	 *			</field>
	 *		</fields>
	 *	</transition>
	 * }
	 * </pre>
	 *
	 * @return json described for the given action or empty json if there is no configuration
	 */
	JsonObject getConfigurationAsJson();

	/**
	 * Converts the given actions collection to JSON array
	 *
	 * @deprecated use {@link #convertAction(Action)} instead
	 * @param actions
	 *            the actions
	 * @return the jSON array
	 */
	// TODO this method is for backward compatibility with version 1. To be removed when UI 2 migration is done
	@Deprecated
	static JSONArray convertActions(Collection<Action> actions) {
		JSONArray array = new JSONArray();
		for (Action action : actions) {
			JSONObject jsonObject = new JSONObject();
			addToJson(jsonObject, ACTION_KEY, action.getActionId());
			addToJson(jsonObject, LABEL_KEY, action.getLabel());
			addToJson(jsonObject, CONFIRMATION_MESSAGE_KEY, action.getConfirmationMessage());
			addToJson(jsonObject, DISABLED_REASON_KEY, action.getDisabledReason());
			addToJson(jsonObject, ON_CLICK_KEY, action.getOnclick());
			addToJson(jsonObject, DISABLED_KEY, action.isDisabled());
			addToJson(jsonObject, IMMEDIATE_KEY, action.isImmediateAction());
			addToJson(jsonObject, PURPOSE_KEY, action.getPurpose());
			array.put(jsonObject);
		}
		return array;
	}

	/**
	 * Converts the given action to {@link JsonObject}.
	 *
	 * @param action
	 *            the action that will be converted
	 * @return {@link JsonObject} with the action information
	 */
	static JsonObject convertAction(Action action) {
		JsonObjectBuilder builder = Json.createObjectBuilder();
		addNotNullValue(builder, SERVER_OPERAION, action.getPurpose());
		addNotNullValue(builder, USER_OPERATION, action.getActionId());
		addNotNullValue(builder, LABEL_KEY, action.getLabel());
		addNotNullValue(builder, CONFIRMATION_MESSAGE_KEY, action.getConfirmationMessage());
		addNotNullValue(builder, DISABLED_REASON_KEY, action.getDisabledReason());
		addNotNullValue(builder, TOOLTIP, action.getTooltip());
		addNotNullValue(builder, ACTION_PATH, action.getActionPath());
		JsonObject configuration = action.getConfigurationAsJson();
		if (configuration != null) {
			builder.add(CONFIGURATION, configuration);
		}
		builder.add(DISABLED_KEY, action.isDisabled());
		return builder.build();
	}

	/**
	 * Adds not null values to the passed {@link JsonObjectBuilder}.
	 *
	 * @param builder
	 *            the builder to which values will be added
	 * @param key
	 *            the key to which the value will be assigned
	 * @param value
	 *            the value to be added
	 */
	static void addNotNullValue(JsonObjectBuilder builder, String key, String value) {
		if (value == null) {
			return;
		}

		builder.add(key, value);
	}

	/**
	 * Adds the given actions to the target JSON object.
	 *
	 * @param object
	 *            the object
	 * @param actions
	 *            the actions
	 */
	static void addActions(JSONObject object, Collection<Action> actions) {
		JsonUtil.addToJson(object, "actions", convertActions(actions));
	}

}
