package com.sirmaenterprise.sep.models;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyByte;
import static org.mockito.Matchers.anyInt;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;

import javax.json.JsonException;

import org.junit.Test;

import com.sirma.itt.seip.io.ResourceLoadUtil;

import net.javacrumbs.jsonunit.JsonAssert;

/**
 * Test for {@link ModelsInfoBodyWriter}
 *
 * @author BBonev
 */
public class ModelsInfoBodyWriterTest {

	private ModelsInfoBodyWriter writer = new ModelsInfoBodyWriter();

	@Test
	public void writeEmpty() throws Exception {
		ByteArrayOutputStream stream = new ByteArrayOutputStream();
		writer.writeTo(new ModelsInfo(), null, null, null, null, null, stream);
		JsonAssert.assertJsonEquals("{\"models\":[]}", new String(stream.toByteArray(), StandardCharsets.UTF_8));
	}

	@Test
	public void writeFullInfo() throws Exception {
		ByteArrayOutputStream stream = new ByteArrayOutputStream();
		ModelsInfo info = new ModelsInfo();
		info.add(new ModelInfo("class1", "Class 1", "class"));
		info.add(new ModelInfo("class2", "Class 2", "class", "class1", true));
		info.add(new ModelInfo("defId1", "Definition 1", "definition", "class1", false));
		writer.writeTo(info, null, null, null, null, null, stream);
		String expected = ResourceLoadUtil.loadResource(getClass(), "full-model-info.json");
		String result = new String(stream.toByteArray(), StandardCharsets.UTF_8);
		JsonAssert.assertJsonEquals(expected, result);
	}

	@Test
	public void writeWithErrorMessage() throws Exception {
		ByteArrayOutputStream stream = new ByteArrayOutputStream();
		ModelsInfo info = new ModelsInfo();
		info.setErrorMessage("error");
		info.add(new ModelInfo("class1", "Class 1", "class"));
		info.add(new ModelInfo("class2", "Class 2", "class", "class1", true));
		info.add(new ModelInfo("defId1", "Definition 1", "definition", "class1", false));
		writer.writeTo(info, null, null, null, null, null, stream);
		String expected = ResourceLoadUtil.loadResource(getClass(), "full-model-info-with-error.json");
		String result = new String(stream.toByteArray(), StandardCharsets.UTF_8);
		JsonAssert.assertJsonEquals(expected, result);
	}

	@Test(expected = JsonException.class)
	public void failToWrite() throws Exception {
		OutputStream stream = mock(OutputStream.class);
		doThrow(IOException.class).when(stream).write(any(byte[].class));
		doThrow(IOException.class).when(stream).write(anyByte());
		doThrow(IOException.class).when(stream).write(any(byte[].class), anyInt(), anyInt());
		writer.writeTo(new ModelsInfo(), null, null, null, null, null, stream);
	}
}
