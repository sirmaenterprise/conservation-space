package com.sirma.itt.seip.instance.lock.rest;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Date;

import javax.json.Json;
import javax.json.JsonException;
import javax.json.JsonObject;
import javax.json.stream.JsonGenerator;

import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Matchers;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

import com.sirma.itt.seip.domain.instance.Instance;
import com.sirma.itt.seip.domain.instance.InstanceReference;
import com.sirma.itt.seip.instance.lock.LockInfo;
import com.sirma.itt.seip.resources.EmfUser;
import com.sirma.itt.seip.resources.ResourceService;
import com.sirma.itt.seip.rest.handlers.writers.InstanceToJsonSerializer;

/**
 * Test for {@link LockInfoWriter}.
 *
 * @author A. Kunchev
 */
public class LockInfoWriterTest {

	@InjectMocks
	private LockInfoWriter writer;

	@Mock
	private ResourceService resourceService;
	@Mock
	private InstanceToJsonSerializer instanceSerializer;

	@Before
	public void setup() {
		writer = new LockInfoWriter();
		MockitoAnnotations.initMocks(this);
		doAnswer(a -> {
			JsonGenerator generator = a.getArgumentAt(2, JsonGenerator.class);
			generator.writeStartObject(a.getArgumentAt(0, String.class));
			generator.writeEnd();
			return null;
		}).when(instanceSerializer).serialize(anyString(), any(Instance.class), any(JsonGenerator.class));
	}

	@Test
	public void isWriteable_wrongClass() {
		assertFalse(writer.isWriteable(String.class, null, null, null));
	}

	@Test
	public void isWriteable_correctClass() {
		assertTrue(writer.isWriteable(LockInfo.class, null, null, null));
	}

	@Test(expected = NullPointerException.class)
	public void writeTo_nullStream() throws IOException {
		writer.writeTo(new LockInfo(), null, null, null, null, null, null);
	}

	@Test(expected = NullPointerException.class)
	public void writeTo_nullLockInfo() throws IOException {
		writer.writeTo(null, null, null, null, null, null, null);
	}

	@Test(expected = JsonException.class)
	public void writeTo_IOWhileWriting() throws IOException {
		try (OutputStream stream = Mockito.mock(OutputStream.class)) {
			Mockito.doThrow(new IOException()).when(stream).write(Matchers.any(byte[].class), Matchers.anyInt(),
					Matchers.anyInt());
			LockInfo lockInfo = new LockInfo(mock(InstanceReference.class), "admin", new Date(), "info", f -> false);
			writer.writeTo(lockInfo, null, null, null, null, null, stream);
		}
	}

	@Test
	public void writeTo_locked() throws IOException {
		try (ByteArrayOutputStream stream = new ByteArrayOutputStream()) {
			LockInfo lockInfo = new LockInfo(mock(InstanceReference.class), "admin", new Date(), "info", f -> true);
			Mockito.when(resourceService.getResource("admin")).thenReturn(new EmfUser("admin"));
			writer.writeTo(lockInfo, null, null, null, null, null, stream);

			JsonObject actual = Json.createReader(new ByteArrayInputStream(stream.toByteArray())).readObject();
			assertEquals(true, actual.getBoolean("isLocked"));
			assertNotNull(actual.get("lockedBy"));
			assertEquals("info", actual.getString("lockInfo"));
			assertNotNull(actual.get("lockOn"));
		}
	}

	@Test
	public void writeTo_unlock() throws IOException {
		try (ByteArrayOutputStream stream = new ByteArrayOutputStream()) {
			LockInfo lockInfo = new LockInfo(mock(InstanceReference.class), null, null, null, f -> false);
			writer.writeTo(lockInfo, null, null, null, null, null, stream);

			JsonObject actual = Json.createReader(new ByteArrayInputStream(stream.toByteArray())).readObject();
			assertEquals(false, actual.getBoolean("isLocked"));
			assertNull(actual.get("lockedBy"));
			assertNull(actual.get("lockInfo"));
			assertNull(actual.get("lockOn"));
		}
	}

}
