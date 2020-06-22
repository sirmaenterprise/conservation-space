package com.sirma.itt.seip.instance.actions.evaluation;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;

import javax.json.Json;
import javax.json.JsonArray;

import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

import com.sirma.itt.seip.definition.TransitionGroupDefinition;
import com.sirma.itt.seip.permissions.action.EmfAction;
import com.sirma.sep.instance.actions.group.ActionItem;
import com.sirma.sep.instance.actions.group.ActionMenu;
import com.sirma.sep.instance.actions.group.GroupItem;


/**
 * Test for {@link ActionsGroupMessageBodyWriter}.
 *
 * @author T. Dossev
 */
public class ActionsGroupMessageBodyWriterTest {

	@InjectMocks
	private ActionsGroupMessageBodyWriter writer;

	@Before
	public void setup() {
		writer = new ActionsGroupMessageBodyWriter();
		MockitoAnnotations.initMocks(this);
	}

	@Test
	public void getSize() {
		assertEquals(-1, writer.getSize(null, null, null, null, null));
	}

	@Test
	public void isWriteable() {
		assertTrue(writer.isWriteable(ActionMenu.class, null, null, null));
	}

	@Test
	public void writeTo_nullActions() throws IOException {
		try (OutputStream stream = mock(OutputStream.class)) {
			writer.writeTo(null, null, null, null, null, null, stream);
			verify(stream, never()).write(any(byte[].class));
		}
	}

	@Test
	public void writeTo_emptyMenu() throws IOException {
		ByteArrayOutputStream stream = new ByteArrayOutputStream();
		writer.writeTo(menu(), null, null, null, null, null, stream);

		JsonArray array = Json.createReader(new ByteArrayInputStream(stream.toByteArray())).readArray();
		assertEquals(2, array.size());
	}

	@Test(expected = NullPointerException.class)
	public void writeTo_nullStream() throws IOException {
		writer.writeTo(emptyMenu(), null, null, null, null, null, null);
	}

	private static ActionMenu emptyMenu() {
		ActionMenu menu = Mockito.mock(ActionMenu.class);
		return menu;
	}

	@SuppressWarnings("boxing")
	private static ActionMenu menu() {
		ActionMenu menu = new ActionMenu();
		ActionMenu m1 = menu.addMenuMember(new GroupItem(buildGroup("g1", null, 2)));
		menu.addMenuMember(new ActionItem(buildAction("a1", null, 1)));
		m1.addMenuMember(new ActionItem(buildAction("a2", "g1", 1)));
		return menu;
	}

	private static TransitionGroupDefinition buildGroup(String identifier, String parent, Integer order) {
		TransitionGroupDefinition group = Mockito.mock(TransitionGroupDefinition.class);
		Mockito.when(group.getIdentifier()).thenReturn(identifier);
		Mockito.when(group.getLabel()).thenReturn("label_" + identifier);
		Mockito.when(group.getParent()).thenReturn(parent);
		Mockito.when(group.getType()).thenReturn("menu");
		Mockito.when(group.getOrder()).thenReturn(order);
		return group;
	}

	private static EmfAction buildAction(String identifier, String group, Integer order) {
		EmfAction emfAction = Mockito.spy(new EmfAction(identifier));
		emfAction.setConfirmationMessage("message");
		emfAction.setDisabled(false);
		emfAction.setImmediate(true);
		emfAction.setLabel("label");
		emfAction.setGroup(group);
		Mockito.when(emfAction.getOrder()).thenReturn(order);
		return emfAction;
	}
}
