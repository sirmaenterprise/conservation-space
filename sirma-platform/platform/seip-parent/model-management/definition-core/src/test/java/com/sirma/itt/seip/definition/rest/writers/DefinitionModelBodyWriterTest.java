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
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
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
import com.sirma.itt.seip.domain.security.ActionTypeConstants;
import com.sirma.itt.seip.rest.exceptions.ResourceException;

/**
 * Test for {@link DefinitionModelBodyWriter}.
 *
 * @author A. Kunchev
 */
public class DefinitionModelBodyWriterTest {

	@InjectMocks
	private DefinitionModelBodyWriter writer;

	@Mock
	private DefinitionModelToJsonSerializer serializer;

	@Before
	public void setup() {
		MockitoAnnotations.initMocks(this);
	}

	@Test
	public void isWriteable_wrongClass() {
		assertFalse(writer.isWriteable(String.class, null, null, null));
	}

	@Test(expected = NullPointerException.class)
	public void isWriteable_nullClass() {
		assertFalse(writer.isWriteable(null, null, null, null));
	}

	@Test
	public void isWriteable_DefinitionModelObjectClass() {
		assertTrue(writer.isWriteable(DefinitionModelObject.class, null, null, null));
	}

	@Test(expected = ResourceException.class)
	public void writeTo_nullObject() throws IOException {
		writer.writeTo(null, null, null, null, null, null, null);
	}

	@Test(expected = ResourceException.class)
	public void writeTo_nullModel() throws IOException {
		writer.writeTo(new DefinitionModelObject(), null, null, null, null, null, null);
	}

	@Test(expected = NullPointerException.class)
	public void writeTo_nullStream() throws IOException {
		DefinitionModelObject object = new DefinitionModelObject();
		object.setDefinitionModel(mock(DefinitionModel.class));
		writer.writeTo(object, null, null, null, null, null, null);
	}

	@Test(expected = JsonException.class)
	public void writeTo_IOExceptionWhileWriting() throws IOException {
		try (OutputStream stream = mock(OutputStream.class)) {
			doThrow(new IOException()).when(stream).write(any(byte[].class), anyInt(), anyInt());
			DefinitionModelObject object = new DefinitionModelObject();
			object.setDefinitionModel(mock(DefinitionModel.class));
			writer.writeTo(object, null, null, null, null, null, stream);
		}
	}

	@Test
	public void writeTo_serializerCalled() throws IOException {
		DefinitionModel model = mock(DefinitionModel.class);
		EmfInstance instance = new EmfInstance();
		String operation = ActionTypeConstants.EDIT_DETAILS;

		DefinitionModelObject object = new DefinitionModelObject();
		object.setDefinitionModel(model);
		object.setOperation(operation);
		object.setInstance(instance);

		try (OutputStream stream = mock(OutputStream.class)) {
			writer.writeTo(object, null, null, null, null, null, stream);
		}

		verify(serializer).serialize(eq(model), eq(instance), eq(operation), any(JsonGenerator.class));
	}

	@Test
	public void writeTo_serializerCalled_filteredProperties() throws IOException {
		DefinitionModel model = mock(DefinitionModel.class);
		PropertyDefinition property = mock(PropertyDefinition.class);
		when(property.getName()).thenReturn("property");
		when(model.getFieldsAndDependencies(anyCollection())).thenReturn(Stream.of(property));

		EmfInstance instance = new EmfInstance();
		String operation = ActionTypeConstants.EDIT_DETAILS;

		DefinitionModelObject object = new DefinitionModelObject()
				.setDefinitionModel(model)
					.setOperation(operation)
					.setInstance(instance)
					.setRequestedFields(Collections.singleton("property"));

		try (OutputStream stream = mock(OutputStream.class)) {
			writer.writeTo(object, null, null, null, null, null, stream);
		}

		verify(serializer).serialize(eq(model), eq(instance), eq(operation),
				eq(new HashSet<>(Arrays.asList("property"))), any(JsonGenerator.class));
	}
}
