package com.sirmaenterprise.sep.bpm.camunda.transitions.model.io;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.Map;

import javax.json.Json;
import javax.json.stream.JsonGenerator;

import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.mockito.Spy;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

import com.sirma.itt.seip.definition.rest.DefinitionModelObject;
import com.sirma.itt.seip.definition.rest.DefinitionModelToJsonSerializer;
import com.sirma.itt.seip.domain.definition.DefinitionModel;
import com.sirma.itt.seip.domain.instance.EmfInstance;
import com.sirma.itt.seip.domain.security.ActionTypeConstants;
import com.sirma.itt.seip.rest.exceptions.ResourceException;
import com.sirma.itt.seip.rest.handlers.writers.InstanceToJsonSerializer;
import com.sirmaenterprise.sep.bpm.camunda.transitions.model.BPMDefinitionModelObject;

public class BPMDefinitionModelMapBodyWriterTest {

	@Mock
	private DefinitionModelToJsonSerializer modelSerializer;

	@Mock
	private InstanceToJsonSerializer instanceSerializer;

	@InjectMocks
	@Spy
	private BPMDefinitionModelMapBodyWriter bpmDefinitionModelMapBodyWriter;

	@Before
	public void setUp() throws Exception {
		MockitoAnnotations.initMocks(this);
	}

	@Test
	public void testIsWriteableTrue() throws Exception {
		Map<String, BPMDefinitionModelObject> test = new HashMap<>();
		ParameterizedType type = mock(ParameterizedType.class);
		when(type.getActualTypeArguments()).thenReturn(new Type[] { String.class, BPMDefinitionModelObject.class });
		assertFalse(bpmDefinitionModelMapBodyWriter.isWriteable(Object.class, null, null, null));
		assertTrue(bpmDefinitionModelMapBodyWriter.isWriteable(test.getClass(), type, null, null));
	}

	@Test
	public void testWriteTo() throws Exception {
		Map<String, BPMDefinitionModelObject> objects = new HashMap<>();
		String key = "emf:id";

		DefinitionModel model = mock(DefinitionModel.class);
		EmfInstance instance = new EmfInstance();
		String operation = ActionTypeConstants.EDIT_DETAILS;

		DefinitionModelObject object = new DefinitionModelObject();
		object.setDefinitionModel(model);
		object.setOperation(operation);
		object.setInstance(instance);

		BPMDefinitionModelObject bpmObject = new BPMDefinitionModelObject(object);
		objects.put(key, bpmObject);
		try (ByteArrayOutputStream stream = new ByteArrayOutputStream()) {
			Mockito.doAnswer(new Answer() {
				@Override
				public Void answer(InvocationOnMock invocation) throws Throwable {
					JsonGenerator jsonGenerator = invocation.getArgumentAt(3, JsonGenerator.class);
					jsonGenerator.write("validationModel", Json.createObjectBuilder().build());
					jsonGenerator.write("viewModel", Json.createObjectBuilder().build());
					jsonGenerator.write("definitionId", "myDef");
					jsonGenerator.write("definitionLabel", "myDef label");
					jsonGenerator.write("instanceType", "object");
					return null;
				}
			}).when(modelSerializer).serialize(any(), any(), any(), any());
			Mockito.doAnswer(new Answer() {
				@Override
				public Void answer(InvocationOnMock invocation) throws Throwable {
					JsonGenerator jsonGenerator = invocation.getArgumentAt(1, JsonGenerator.class);
					String objectName = invocation.getArgumentAt(2, String.class);
					jsonGenerator.writeStartObject(objectName);
					jsonGenerator.write("id", "object");
					jsonGenerator.write("definitionId", "otherID");
					jsonGenerator.write("headers", Json.createObjectBuilder().build());
					jsonGenerator.write("instanceType", "myType");
					jsonGenerator.write("properties", Json.createObjectBuilder().build());
					jsonGenerator.writeEnd();
					return null;
				}
			}).when(instanceSerializer).serialize(any(), any(), eq("instance"));
			bpmDefinitionModelMapBodyWriter.writeTo(objects, null, null, null, null, null, stream);
			String result = new String(stream.toByteArray());
			String expected = "{\"emf:id\":{\"model\":{\"validationModel\":{},\"viewModel\":{},\"definitionId\":\"myDef\",\"definitionLabel\":\"myDef label\",\"instanceType\":\"object\"},"
					+ "\"instance\":{\"id\":\"object\",\"definitionId\":\"otherID\",\"headers\":{},\"instanceType\":\"myType\",\"properties\":{}}}}";
			assertEquals(expected, result);
		}
		verify(modelSerializer).serialize(eq(model), eq(instance), eq(operation), any(JsonGenerator.class));

	}

	@Test(expected = ResourceException.class)
	public void writeTo_nullMap() throws IOException {
		bpmDefinitionModelMapBodyWriter.writeTo(null, null, null, null, null, null, null);
	}

}
