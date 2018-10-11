package com.sirma.itt.seip.instance.version.rest;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyCollectionOf;
import static org.mockito.Matchers.anyInt;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import java.io.IOException;
import java.io.OutputStream;
import java.util.Arrays;
import java.util.List;

import javax.json.JsonException;
import javax.json.stream.JsonGenerator;

import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

import com.sirma.itt.seip.domain.instance.EmfInstance;
import com.sirma.itt.seip.domain.instance.Instance;
import com.sirma.itt.seip.instance.dao.InstanceLoadDecorator;
import com.sirma.itt.seip.instance.version.VersionsResponse;
import com.sirma.itt.seip.rest.handlers.writers.PropertiesFilterBuilder;
import com.sirma.itt.seip.rest.handlers.writers.InstanceToJsonSerializer;

/**
 * Test for {@link VersionsResponseBodyWriter}.
 *
 * @author A. Kunchev
 */
public class VersionsResponseBodyWriterTest {

	@InjectMocks
	private VersionsResponseBodyWriter writer;

	@Mock
	private InstanceToJsonSerializer instanceToJsonSerializer;

	@Mock
	private InstanceLoadDecorator instanceLoadDecorator;

	@Before
	public void setUp() {
		writer = new VersionsResponseBodyWriter();
		MockitoAnnotations.initMocks(this);
	}

	@Test
	public void isWriteable_incorrectClass() {
		assertFalse(writer.isWriteable(String.class, null, null, null));
	}

	@Test
	public void isWriteable_correctClass() {
		assertTrue(writer.isWriteable(VersionsResponse.class, null, null, null));
	}

	@Test(expected = NullPointerException.class)
	public void writeTo_nullStream() throws IOException {
		writer.writeTo(new VersionsResponse(), null, null, null, null, null, null);
	}

	@Test(expected = JsonException.class)
	public void writeTo_errorWhileWriting() throws IOException {
		try (OutputStream stream = Mockito.mock(OutputStream.class)) {
			doThrow(new IOException()).when(stream).write(any(byte[].class), anyInt(), anyInt());
			writer.writeTo(VersionsResponse.emptyResponse(), null, null, null, null, null, stream);
		}
	}

	@Test
	public void writeTo_withTwoResults() throws IOException {
		List<Instance> instances = Arrays.asList(new EmfInstance(), new EmfInstance());
		VersionsResponse response = new VersionsResponse();
		response.setTotalCount(instances.size());
		response.setResults(instances);
		try (OutputStream stream = mock(OutputStream.class)) {
			writer.writeTo(response, null, null, null, null, null, stream);
			verify(stream).write(any(byte[].class), anyInt(), anyInt());
			verify(instanceLoadDecorator).decorateResult(anyCollectionOf(Instance.class));
			verify(instanceToJsonSerializer).serialize(eq(instances), any(PropertiesFilterBuilder.class),
					any(JsonGenerator.class));
		}
	}

}
