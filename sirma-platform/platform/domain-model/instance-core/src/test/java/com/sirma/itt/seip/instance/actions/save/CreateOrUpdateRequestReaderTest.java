package com.sirma.itt.seip.instance.actions.save;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

import javax.ws.rs.core.MultivaluedHashMap;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.UriInfo;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Matchers;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

import com.google.gson.JsonObject;
import com.sirma.itt.seip.content.upload.UploadRequest;
import com.sirma.itt.seip.convert.TypeConverter;
import com.sirma.itt.seip.convert.TypeConverterUtil;
import com.sirma.itt.seip.domain.instance.EmfInstance;
import com.sirma.itt.seip.domain.instance.Instance;
import com.sirma.itt.seip.rest.exceptions.BadRequestException;
import com.sirma.itt.seip.rest.resources.instances.InstanceResourceParser;
import com.sirma.itt.seip.rest.utils.JsonKeys;
import com.sirma.itt.seip.rest.utils.request.RequestInfo;

/**
 * Test the {@link CreateOrUpdateRequestReader}
 *
 * @author nvelkov
 */
public class CreateOrUpdateRequestReaderTest {

	@Mock
	private RequestInfo request;

	@Mock
	private InstanceResourceParser instanceResourceParser;

	@InjectMocks
	private CreateOrUpdateRequestReader reader = new CreateOrUpdateRequestReader();

	@Before
	public void init() {
		MockitoAnnotations.initMocks(this);
	}

	@Test
	public void testIsReadable() {
		Assert.assertTrue(reader.isReadable(CreateOrUpdateRequest.class, null, null, null));
		Assert.assertFalse(reader.isReadable(UploadRequest.class, null, null, null));
	}

	@Test(expected = BadRequestException.class)
	public void testReadEmptyJson() throws IOException {
		String json = "{}";
		InputStream stream = new ByteArrayInputStream(json.getBytes(StandardCharsets.UTF_8));
		reader.readFrom(CreateOrUpdateRequest.class, null, null, null, null, stream);
	}

	@Test
	public void testReadJson() throws IOException {
		JsonObject json = new JsonObject();
		json.addProperty(JsonKeys.USER_OPERATION, "uploadNewVersion");
		json.add(JsonKeys.TARGET_INSTANCE, new JsonObject());

		MultivaluedMap<String, String> pathParams = getRequestParams();
		pathParams.add("id", "emf:123");

		mockInstanceResourceParser();
		InputStream stream = new ByteArrayInputStream(json.toString().getBytes(StandardCharsets.UTF_8));
		CreateOrUpdateRequest createOrUpdateRequest = reader.readFrom(CreateOrUpdateRequest.class, null, null, null,
				null, stream);

		Assert.assertEquals(createOrUpdateRequest.getTargetId(), "emf:123");
		Assert.assertEquals("test", createOrUpdateRequest.getTarget().get("title"));
	}

	private MultivaluedMap<String, String> getRequestParams() {
		UriInfo pathInfo = Mockito.mock(UriInfo.class);
		MultivaluedMap<String, String> pathParams = new MultivaluedHashMap<>();
		Mockito.when(pathInfo.getPathParameters()).thenReturn(pathParams);
		Mockito.when(request.getUriInfo()).thenReturn(pathInfo);

		return pathParams;
	}

	private void mockInstanceResourceParser() {
		TypeConverterUtil.setTypeConverter(Mockito.mock(TypeConverter.class));
		Instance instance = new EmfInstance();
		instance.add("title", "test");
		Mockito.when(instanceResourceParser.toInstance(Matchers.any(javax.json.JsonObject.class), Matchers.anyString()))
				.thenReturn(instance);

	}
}
