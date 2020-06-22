package com.sirma.itt.seip.definition.rest.writers;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyCollection;
import static org.mockito.Matchers.anyInt;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.io.OutputStream;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.stream.Stream;

import javax.json.JsonException;
import javax.json.stream.JsonGenerator;

import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import com.sirma.itt.seip.definition.rest.DefinitionModelObject;
import com.sirma.itt.seip.definition.rest.DefinitionModelToJsonSerializer;
import com.sirma.itt.seip.domain.definition.DefinitionModel;
import com.sirma.itt.seip.domain.definition.PropertyDefinition;
import com.sirma.itt.seip.domain.instance.EmfInstance;
import com.sirma.itt.seip.domain.instance.Instance;
import com.sirma.itt.seip.domain.security.ActionTypeConstants;
import com.sirma.itt.seip.rest.exceptions.ResourceException;

/**
 * Test for {@link DefinitionModelMapBodyWriter}.
 *
 * @author A. Kunchev
 */
public class DefinitionModelMapBodyWriterTest {

	@InjectMocks
	private DefinitionModelMapBodyWriter writer;

	@Mock
	private DefinitionModelToJsonSerializer serializer;

	@Before
	public void setup() {
		writer = new DefinitionModelMapBodyWriter();
		MockitoAnnotations.initMocks(this);
	}

	@Test
	public void isWriteable_notAMap() {
		assertFalse(writer.isWriteable(Collection.class, null, null, null));
	}

	@Test
	public void isWriteable_map_nullType() {
		assertFalse(writer.isWriteable(Map.class, null, null, null));
	}

	@Test
	public void isWriteable_map_wrongKeyType() {
		ParameterizedType type = mock(ParameterizedType.class);
		when(type.getActualTypeArguments()).thenReturn(new Type[] { Object.class, DefinitionModelObject.class });
		assertFalse(writer.isWriteable(Map.class, type, null, null));
	}

	@Test
	public void isWriteable_map_wrongValueType() {
		ParameterizedType type = mock(ParameterizedType.class);
		when(type.getActualTypeArguments()).thenReturn(new Type[] { String.class, Object.class });
		assertFalse(writer.isWriteable(Map.class, type, null, null));
	}

	@Test
	public void isWriteable_map_wrongValueType_wrongKeyType() {
		ParameterizedType type = mock(ParameterizedType.class);
		when(type.getActualTypeArguments()).thenReturn(new Type[] { Object.class, Object.class });
		assertFalse(writer.isWriteable(Map.class, type, null, null));
	}

	@Test
	public void isWriteable_map_withValidTypes() {
		ParameterizedType type = mock(ParameterizedType.class);
		when(type.getActualTypeArguments()).thenReturn(new Type[] { String.class, DefinitionModelObject.class });
		assertTrue(writer.isWriteable(Map.class, type, null, null));
	}

	@Test(expected = ResourceException.class)
	public void writeTo_nullMap() throws IOException {
		writer.writeTo(null, null, null, null, null, null, null);
	}

	@Test(expected = ResourceException.class)
	public void writeTo_emptyMap() throws IOException {
		writer.writeTo(new HashMap<>(), null, null, null, null, null, null);
	}

	@Test(expected = NullPointerException.class)
	public void writeTo_nullStream() throws IOException {
		HashMap<String, DefinitionModelObject> objects = new HashMap<>();
		objects.put("id", new DefinitionModelObject());
		writer.writeTo(objects, null, null, null, null, null, null);
	}

	@Test(expected = JsonException.class)
	public void writeTo_IOExceptionWhileWriting() throws IOException {
		DefinitionModelObject object = new DefinitionModelObject();
		object.setDefinitionModel(mock(DefinitionModel.class));
		HashMap<String, DefinitionModelObject> objects = new HashMap<>();
		objects.put("id", object);

		try (OutputStream stream = mock(OutputStream.class)) {
			doThrow(new IOException()).when(stream).write(any(byte[].class), anyInt(), anyInt());
			writer.writeTo(objects, null, null, null, null, null, stream);
		}
	}

	@Test
	public void writeTo_serializerCalled() throws IOException {
		DefinitionModel model = mock(DefinitionModel.class);
		EmfInstance instance = new EmfInstance();
		String operation = ActionTypeConstants.EDIT_DETAILS;

		DefinitionModelObject object = new DefinitionModelObject()
				.setDefinitionModel(model)
					.setOperation(operation)
					.setInstance(instance);

		HashMap<String, DefinitionModelObject> objects = new HashMap<>();
		objects.put("id", object);
		objects.put("noModel", new DefinitionModelObject());
		try (OutputStream stream = mock(OutputStream.class)) {
			writer.writeTo(objects, null, null, null, null, null, stream);
		}

		verify(serializer).serialize(eq(model), eq(instance), eq(operation), any(JsonGenerator.class));
	}

	@Test
	public void writeTo_serializerCalled_withPropertiesFilter() throws IOException {
		DefinitionModel model = mock(DefinitionModel.class);
		PropertyDefinition property = mock(PropertyDefinition.class);
		when(property.getName()).thenReturn("property");
		when(model.getFieldsAndDependencies(anyCollection())).thenReturn(Stream.of(property));

		Instance instance = new EmfInstance();
		String operation = ActionTypeConstants.EDIT_DETAILS;

		DefinitionModelObject object = new DefinitionModelObject()
				.setDefinitionModel(model)
					.setOperation(operation)
					.setInstance(instance)
					.setRequestedFields(Collections.singleton("property"));

		HashMap<String, DefinitionModelObject> objects = new HashMap<>();
		objects.put("id", object);
		objects.put("noModel", new DefinitionModelObject());
		try (OutputStream stream = mock(OutputStream.class)) {
			writer.writeTo(objects, null, null, null, null, null, stream);
		}

		verify(serializer).serialize(eq(model), eq(instance), eq(operation),
				eq(new HashSet<>(Collections.singleton("property"))), any(JsonGenerator.class));
	}
}
