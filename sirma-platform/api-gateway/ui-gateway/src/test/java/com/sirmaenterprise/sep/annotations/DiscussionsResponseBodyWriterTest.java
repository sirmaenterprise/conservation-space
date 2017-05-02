package com.sirmaenterprise.sep.annotations;

import static org.mockito.Matchers.any;
import static org.mockito.Mockito.when;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import javax.ws.rs.WebApplicationException;

import org.apache.commons.io.IOUtils;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import com.sirma.itt.seip.annotations.rest.AnnotationWriter;
import com.sirma.itt.seip.collections.CollectionUtils;
import com.sirma.itt.seip.testutil.io.FileTestUtils;

import net.javacrumbs.jsonunit.JsonAssert;

/**
 * Tests the functionality of {@link DiscussionsResponseBodyWriter}.
 *
 * @author Vilizar Tsonev
 */
public class DiscussionsResponseBodyWriterTest {

	@InjectMocks
	private DiscussionsResponseBodyWriter writer;

	@Mock
	private AnnotationWriter annotationWriter;

	@Before
	public void beforeMethod() throws IOException {
		MockitoAnnotations.initMocks(this);
		Map<String, String> annotationsMap = CollectionUtils.createHashMap(1);
		annotationsMap.put("id", "annotation1");
		when(annotationWriter.convert(any(Collection.class))).thenReturn(annotationsMap);
	}

	/**
	 * Tests if the {@link DiscussionsResponse} is correctly serialized to JSON and all attributes are present.
	 */
	@Test
	public void testResponseIsProperlyBuilt() throws WebApplicationException, IOException {
		Map<String, String> headers = new HashMap<>(1);
		headers.put("annotation1", "sampleHeader");
		DiscussionsResponse response = new DiscussionsResponse().setTargetInstanceHeaders(headers);

		ByteArrayOutputStream out = new ByteArrayOutputStream();
		writer.writeTo(response, null, null, null, null, null, out);

		Object actual = new String(out.toByteArray(), StandardCharsets.UTF_8);
		JsonAssert.assertJsonEquals(loadExpectedJson(), actual);
	}

	private static Object loadExpectedJson() throws IOException {
		try (InputStream stream = FileTestUtils
				.getResourceAsStream("/com/sirmaenterprise/sep/annotations/discussionsResponse.json")) {
			return IOUtils.toString(stream, StandardCharsets.UTF_8);
		}
	}
}
