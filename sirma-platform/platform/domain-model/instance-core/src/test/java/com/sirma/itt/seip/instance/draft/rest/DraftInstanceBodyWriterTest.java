package com.sirma.itt.seip.instance.draft.rest;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyInt;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import javax.json.Json;
import javax.json.JsonException;
import javax.json.JsonObject;

import org.junit.Before;
import org.junit.Test;
import org.mockito.MockitoAnnotations;

import com.sirma.itt.seip.instance.draft.DraftInstance;

/**
 * Tests for {@link DraftInstanceBodyWriter}.
 *
 * @author A. Kunchev
 */
public class DraftInstanceBodyWriterTest {

	private DraftInstanceBodyWriter writer;

	@Before
	public void setup() {
		writer = new DraftInstanceBodyWriter();
		MockitoAnnotations.initMocks(this);
	}

	@Test
	public void isWriteable_incorrcetClass_false() {
		assertFalse(writer.isWriteable(String.class, null, null, null));
	}

	@Test
	public void isWriteable_corrcetClass_false() {
		assertTrue(writer.isWriteable(DraftInstance.class, null, null, null));
	}

	@Test(expected = NullPointerException.class)
	public void writeTo_nullStream() throws IOException {
		writer.writeTo(new DraftInstance(), null, null, null, null, null, null);
	}

	@Test(expected = JsonException.class)
	public void writeTo_errorWhileWriting() throws IOException {
		try (OutputStream output = mock(OutputStream.class)) {
			doThrow(new IOException()).when(output).write(any(byte[].class), anyInt(), anyInt());
			writer.writeTo(buildDraftInstance(), null, null, null, null, null, output);
		}
	}

	@Test
	public void writeTo_nullDraftInstance_emptyJson() throws IOException {
		try (ByteArrayOutputStream out = new ByteArrayOutputStream()) {
			writer.writeTo(null, null, null, null, null, null, out);
			out.flush();

			JsonObject object = Json.createReader(new ByteArrayInputStream(out.toByteArray())).readObject();
			assertTrue(object.isEmpty());
		}
	}

	@Test
	public void writeTo_withInstance_notEmptyJson() throws IOException {
		try (ByteArrayOutputStream out = new ByteArrayOutputStream()) {
			DraftInstance draft = buildDraftInstance();
			writer.writeTo(draft, null, null, null, null, null, out);
			out.flush();

			JsonObject result = Json.createReader(new ByteArrayInputStream(out.toByteArray())).readObject();
			assertFalse(result.isEmpty());
			assertEquals("Batman", result.getString("draftCreator"));
			assertEquals("draft-content-id", result.getString("draftContentId"));
			assertEquals("instance-id", result.getString("draftInstanceId"));
			assertEquals("1976-02-19T00:00:00.000Z", result.getString("draftCreatedOn"));
		}
	}

	private static DraftInstance buildDraftInstance() {
		DraftInstance draft = new DraftInstance();
		draft.setCreator("Batman");
		draft.setDraftContentId("draft-content-id");
		draft.setInstanceId("instance-id");
		try {
			SimpleDateFormat sdf = new SimpleDateFormat("dd-M-yyyy hh:mm:ss");
			Date date = sdf.parse("19-02-1976 00:00:00");
			draft.setCreatedOn(date);
		} catch (ParseException e) {
			fail("Problem occurred while parsing date." + e.getMessage());
		}
		return draft;
	}

}
