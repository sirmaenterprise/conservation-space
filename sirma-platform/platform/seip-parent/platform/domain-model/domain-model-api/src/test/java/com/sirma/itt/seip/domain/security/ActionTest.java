package com.sirma.itt.seip.domain.security;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import javax.json.Json;
import javax.json.JsonObject;
import javax.json.JsonObjectBuilder;

import org.junit.Test;
import org.mockito.Mockito;

/**
 * Tests for default {@link Action} methods.
 *
 * @author A. Kunchev
 */
@SuppressWarnings("static-method")
public class ActionTest {

	@Test
	public void convertAction_withoutConfiguration() {
		Action action = prepareActionMock("actionId", null);
		JsonObject actionAsJson = Action.convertAction(action).build();
		assertNotNull(actionAsJson);
		assertNull(actionAsJson.getJsonObject(Action.CONFIGURATION));
		assertNotNull(actionAsJson.getString(Action.USER_OPERATION));
		assertNotNull(actionAsJson.getString(Action.TOOLTIP));
		assertNotNull(actionAsJson.getString(Action.SERVER_OPERAION));
	}

	@Test
	public void convertAction_withConfiguration() {
		JsonObject configuration = Json.createObjectBuilder().add("config1", "config").build();
		Action action = prepareActionMock("actionId", configuration);
		JsonObject actionAsJson = Action.convertAction(action).build();
		assertNotNull(actionAsJson);
		assertNotNull(actionAsJson.getJsonObject(Action.CONFIGURATION));
		assertNotNull(actionAsJson.getString(Action.USER_OPERATION));
		assertNotNull(actionAsJson.getString(Action.TOOLTIP));
		assertNotNull(actionAsJson.getString(Action.SERVER_OPERAION));
	}

	@Test
	public void addNotNullValue_nullValue() {
		JsonObjectBuilder builder = Json.createObjectBuilder();
		Action.addNotNullValue(builder, "key", null);
		assertEquals("{}", builder.build().toString());
	}

	@Test
	public void convertAction_notNullValue() {
		JsonObjectBuilder builder = Json.createObjectBuilder();
		Action.addNotNullValue(builder, "key", "value");
		assertEquals("{\"key\":\"value\"}", builder.build().toString());
	}

	private static Action prepareActionMock(String id, JsonObject configuration) {
		Action action = Mockito.mock(Action.class);
		Mockito.when(action.getActionId()).thenReturn(id);
		Mockito.when(action.getLabel()).thenReturn("actionLabel");
		Mockito.when(action.getPurpose()).thenReturn("purpose");
		Mockito.when(action.getConfirmationMessage()).thenReturn("confMsg");
		Mockito.when(action.getDisabledReason()).thenReturn("I'm Batman.");
		Mockito.when(action.getTooltip()).thenReturn("tooltip");
		Mockito.when(action.getConfigurationAsJson()).thenReturn(configuration);
		Mockito.when(action.isDisabled()).thenReturn(false);
		return action;
	}

}
