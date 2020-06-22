package com.sirma.itt.seip.instance.actions.evaluation;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyInt;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import javax.json.Json;
import javax.json.JsonArray;
import javax.json.JsonException;

import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

import com.sirma.itt.seip.domain.security.Action;
import com.sirma.itt.seip.permissions.action.EmfAction;

/**
 * Test for {@link ActionsMessageBodyWriter}.
 *
 * @author A. Kunchev
 */
public class ActionsMessageBodyWriterTest {

	@InjectMocks
	private ActionsMessageBodyWriter writer;

	@Before
	public void setup() {
		writer = new ActionsMessageBodyWriter();
		MockitoAnnotations.initMocks(this);
	}

	@Test
	public void getSize() {
		assertEquals(-1, writer.getSize(null, null, null, null, null));
	}

	@Test
	public void isWriteable() {
		ParameterizedType type = mock(ParameterizedType.class);
		when(type.getActualTypeArguments()).thenReturn(new Type[] { Action.class });
		assertTrue(writer.isWriteable(Collection.class, type, null, null));
	}

	@Test
	public void isWriteable_notCollection_false() {
		ParameterizedType type = mock(ParameterizedType.class);
		when(type.getActualTypeArguments()).thenReturn(new Type[] { Action.class });
		assertFalse(writer.isWriteable(String.class, type, null, null));
	}

	@Test
	public void isWriteable_notCollectionNotAction_false() {
		ParameterizedType type = mock(ParameterizedType.class);
		when(type.getActualTypeArguments()).thenReturn(new Type[] { Integer.class });
		assertFalse(writer.isWriteable(String.class, type, null, null));
	}

	@Test
	public void isWriteable_setOfActions_true() {
		ParameterizedType type = mock(ParameterizedType.class);
		when(type.getActualTypeArguments()).thenReturn(new Type[] { Action.class });
		assertTrue(writer.isWriteable(Set.class, type, null, null));
	}

	@Test
	public void writeTo_nullActions() throws IOException {
		try (OutputStream stream = mock(OutputStream.class)) {
			writer.writeTo(null, null, null, null, null, null, stream);
			verify(stream, never()).write(anyInt());
			verify(stream, never()).write(any(byte[].class));
			verify(stream, never()).write(any(byte[].class), anyInt(), anyInt());
		}
	}

	@Test
	public void writeTo_emptyActions() throws IOException {
		try (OutputStream stream = mock(OutputStream.class)) {
			writer.writeTo(new HashSet<>(), null, null, null, null, null, stream);
			verify(stream, never()).write(anyInt());
			verify(stream, never()).write(any(byte[].class));
			verify(stream, never()).write(any(byte[].class), anyInt(), anyInt());
		}
	}

	@Test
	public void writeTo_notEmptyActions() throws IOException {
		ByteArrayOutputStream stream = new ByteArrayOutputStream();
		EmfAction visibleAction = buildAction();
		EmfAction nonVisibleAction = buildAction();
		nonVisibleAction.setVisible(false);
		writer.writeTo(Arrays.asList(visibleAction, nonVisibleAction), null, null, null, null, null, stream);

		JsonArray array = Json.createReader(new ByteArrayInputStream(stream.toByteArray())).readArray();
		assertEquals(1, array.size());
		assertEquals("action", array.getJsonObject(0).getString("userOperation"));
		assertEquals("cus I'm Batman", array.getJsonObject(0).getString("disabledReason"));
	}

	@Test(expected = NullPointerException.class)
	public void writeTo_nullStream() throws IOException {
		EmfAction emfAction = buildAction();
		writer.writeTo(Arrays.asList(emfAction), null, null, null, null, null, null);
	}

	@Test(expected = JsonException.class)
	public void writeTo_errorWhileWriting() throws IOException {
		EmfAction emfAction = buildAction();
		try (OutputStream stream = Mockito.mock(OutputStream.class)) {
			doThrow(new IOException()).when(stream).write(any(byte[].class), anyInt(), anyInt());
			writer.writeTo(Arrays.asList(emfAction), null, null, null, null, null, stream);
		}
	}

	private static EmfAction buildAction() {
		EmfAction emfAction = new EmfAction("action");
		emfAction.setConfirmationMessage("message");
		emfAction.setDisabled(false);
		emfAction.setImmediate(true);
		emfAction.setLabel("label");
		emfAction.setDisabledReason("cus I'm Batman");
		return emfAction;
	}

}
