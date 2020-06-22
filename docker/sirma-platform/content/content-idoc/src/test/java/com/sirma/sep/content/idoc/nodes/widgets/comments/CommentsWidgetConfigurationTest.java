package com.sirma.sep.content.idoc.nodes.widgets.comments;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.util.List;

import org.junit.Before;
import org.junit.Test;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import com.sirma.sep.content.idoc.nodes.widgets.comments.CommentsWidgetConfiguration;

/**
 * {@link CommentsWidgetConfiguration}.
 *
 * @author A. Kunchev
 */
public class CommentsWidgetConfigurationTest {

	private CommentsWidgetConfiguration configuration;

	@Before
	public void setUp() {
		configuration = new CommentsWidgetConfiguration(null, new JsonObject());
	}

	@Test
	public void isCurrentObjectIncluded_noProperty() {
		boolean included = configuration.isCurrentObjectIncluded();
		assertFalse(included);
	}

	@Test
	public void isCurrentObjectIncluded_withProperty() {
		configuration.addNotNullProperty("selectCurrentObject", new JsonPrimitive(true));
		boolean included = configuration.isCurrentObjectIncluded();
		assertTrue(included);
	}

	@Test
	public void getSelectedUsers_noProperty() {
		List<String> selectedUsers = configuration.getSelectedUsers();
		assertTrue(selectedUsers.isEmpty());
	}

	@Test
	public void getSelectedUsers_withProperty() {
		JsonArray users = new JsonArray();
		users.add(new JsonPrimitive("user"));
		configuration.addNotNullProperty("selectedUsers", users);
		List<String> selectedUsers = configuration.getSelectedUsers();
		assertFalse(selectedUsers.isEmpty());
	}

	@Test
	public void getFilterProperties_noProperty() {
		JsonObject filterProperties = configuration.getFilterProperties();
		assertNull(filterProperties);
	}

	@Test
	public void getFilterProperties_withProperty() {
		configuration.addNotNullProperty("filterProperties", new JsonObject());
		JsonObject filterProperties = configuration.getFilterProperties();
		assertNotNull(filterProperties);
	}

	@Test
	public void getFilterCriteria_noProperty() {
		JsonObject filterCriteria = configuration.getFilterCriteria();
		assertNull(filterCriteria);
	}

	@Test
	public void getFilterCriteria_withProperty() {
		configuration.setFilterCriteria(new JsonObject());
		JsonObject filterCriteria = configuration.getFilterCriteria();
		assertNotNull(filterCriteria);
	}

	@Test
	public void getLimit_noProperty_defaultValue() {
		int limit = configuration.getLimit();
		assertEquals(-1, limit);
	}

	@Test
	public void getLimit_withProperty() {
		configuration.addNotNullProperty("limit", new JsonPrimitive(10));
		int limit = configuration.getLimit();
		assertEquals(10, limit);
	}

	@Test
	public void getStatus_noProperty() {
		String status = configuration.getStatus();
		assertNull(status);
	}

	@Test
	public void getStatus_withProperty() {
		configuration.addNotNullProperty("status", new JsonPrimitive("some-status"));
		String status = configuration.getStatus();
		assertEquals("some-status", status);
	}

	@Test
	public void getText_noProperty() {
		String text = configuration.getText();
		assertNull(text);
	}

	@Test
	public void getText_withProperty() {
		configuration.addNotNullProperty("text", new JsonPrimitive("some-text"));
		String text = configuration.getText();
		assertEquals("some-text", text);
	}

}
