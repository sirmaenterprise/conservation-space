package com.sirma.itt.seip.instance.lock.rest;

import static com.sirma.itt.seip.domain.instance.DefaultProperties.LOCKED_BY;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Date;
import java.util.function.Predicate;

import javax.json.Json;
import javax.json.JsonObject;
import javax.json.stream.JsonGenerator;

import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.mockito.Spy;

import com.sirma.itt.seip.convert.TypeConverter;
import com.sirma.itt.seip.convert.TypeConverterImpl;
import com.sirma.itt.seip.domain.instance.Instance;
import com.sirma.itt.seip.domain.instance.InstanceReference;
import com.sirma.itt.seip.instance.lock.LockInfo;
import com.sirma.itt.seip.instance.lock.LockInfoConverterProvider;
import com.sirma.itt.seip.resources.EmfUser;
import com.sirma.itt.seip.resources.ResourceService;
import com.sirma.itt.seip.rest.handlers.writers.InstanceToJsonSerializer;
import com.sirma.itt.seip.rest.handlers.writers.PropertiesFilterBuilder;
import com.sirma.itt.seip.testutil.mocks.DefinitionMock;
import com.sirma.itt.seip.testutil.mocks.PropertyDefinitionMock;

/**
 * Test for {@link LockInfoWriter}.
 *
 * @author A. Kunchev
 */
public class LockInfoWriterTest {

	@InjectMocks
	private LockInfoWriter writer;
	@InjectMocks
	private LockInfoConverterProvider converterProvider;
	@Mock
	private ResourceService resourceService;
	@Mock
	private InstanceToJsonSerializer instanceSerializer;

	@Spy
	private TypeConverter typeConverter = new TypeConverterImpl();

	@Before
	public void setup() {
		writer = new LockInfoWriter();
		MockitoAnnotations.initMocks(this);

		converterProvider.register(typeConverter);

		doAnswer(a -> {
			Instance instance = a.getArgumentAt(1, Instance.class);
			DefinitionMock mock = buildDefinition();
			Predicate<String> filter = a.getArgumentAt(2, PropertiesFilterBuilder.class).buildFilter(mock);
			JsonGenerator generator = a.getArgumentAt(3, JsonGenerator.class);
			generator.writeStartObject(a.getArgumentAt(0, String.class)).writeStartObject("properties");
			instance.getOrCreateProperties().entrySet().stream().filter(entry -> filter.test(entry.getKey())).forEach(
					entry -> generator.write(entry.getKey(), entry.getValue().toString()));
			generator.writeEnd().writeEnd();
			return null;
		}).when(instanceSerializer).serialize(eq(LOCKED_BY), any(Instance.class), any(PropertiesFilterBuilder.class),
				any(JsonGenerator.class));
	}

	private DefinitionMock buildDefinition() {
		DefinitionMock mock = new DefinitionMock();
		PropertyDefinitionMock prop = new PropertyDefinitionMock();
		prop.setName("userId");
		mock.getFields().add(prop);
		return mock;
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

	@Test
	public void writeTo_locked() throws IOException {
		try (ByteArrayOutputStream stream = new ByteArrayOutputStream()) {
			LockInfo lockInfo = new LockInfo(mock(InstanceReference.class), "admin", new Date(), "info", f -> true);
			Mockito.when(resourceService.findResource("admin")).thenReturn(new EmfUser("admin"));
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
