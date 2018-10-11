package com.sirma.itt.seip.instance.actions.save;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

import javax.ws.rs.core.MultivaluedHashMap;
import javax.ws.rs.core.UriInfo;

import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import com.google.gson.JsonObject;
import com.sirma.itt.seip.convert.TypeConverter;
import com.sirma.itt.seip.convert.TypeConverterUtil;
import com.sirma.itt.seip.domain.instance.EmfInstance;
import com.sirma.itt.seip.domain.instance.Instance;
import com.sirma.itt.seip.rest.exceptions.BadRequestException;
import com.sirma.itt.seip.rest.resources.instances.InstanceResourceParser;
import com.sirma.itt.seip.rest.utils.JsonKeys;
import com.sirma.itt.seip.rest.utils.request.RequestInfo;
import com.sirma.sep.content.upload.UploadRequest;

/**
 * Test the {@link SaveRequestReader}
 *
 * @author nvelkov
 */
public class SaveRequestReaderTest {

	@Mock
	private RequestInfo request;

	@Mock
	private InstanceResourceParser instanceResourceParser;

	@InjectMocks
	private SaveRequestReader reader = new SaveRequestReader();

	@Before
	public void init() {
		MockitoAnnotations.initMocks(this);

		UriInfo pathInfo = mock(UriInfo.class);
		when(pathInfo.getPathParameters()).thenReturn(new MultivaluedHashMap<>());
		when(request.getUriInfo()).thenReturn(pathInfo);
	}

	@Test
	public void isReadable() {
		assertTrue(reader.isReadable(SaveRequest.class, null, null, null));
		assertFalse(reader.isReadable(UploadRequest.class, null, null, null));
	}

	@Test(expected = BadRequestException.class)
	public void readEmptyJson() throws IOException {
		String json = "{}";
		InputStream stream = new ByteArrayInputStream(json.getBytes(StandardCharsets.UTF_8));
		reader.readFrom(SaveRequest.class, null, null, null, null, stream);
	}

	@Test
	public void readJson() throws IOException {
		JsonObject json = new JsonObject();
		json.addProperty(JsonKeys.USER_OPERATION, "uploadNewVersion");
		json.add(JsonKeys.TARGET_INSTANCE, new JsonObject());

		mockInstanceResourceParser("emf:123");

		try (InputStream stream = new ByteArrayInputStream(json.toString().getBytes(StandardCharsets.UTF_8))) {
			SaveRequest saveRequest = reader.readFrom(SaveRequest.class, null, null, null, null, stream);
			assertEquals("emf:123", saveRequest.getTargetId());
			assertEquals("test", saveRequest.getTarget().get("title"));
		}
	}

	private void mockInstanceResourceParser(String id) {
		TypeConverterUtil.setTypeConverter(mock(TypeConverter.class));
		Instance instance = new EmfInstance(id);
		instance.add("title", "test");

		when(instanceResourceParser.toInstance(any(javax.json.JsonObject.class), any())).thenReturn(instance);
	}
}