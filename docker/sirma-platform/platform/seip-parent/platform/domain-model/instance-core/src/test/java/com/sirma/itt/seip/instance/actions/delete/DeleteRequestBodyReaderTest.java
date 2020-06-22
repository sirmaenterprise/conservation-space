package com.sirma.itt.seip.instance.actions.delete;

import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.when;
import static org.testng.Assert.assertEquals;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.Arrays;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MultivaluedHashMap;
import javax.ws.rs.core.UriInfo;

import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import com.sirma.itt.seip.rest.utils.request.RequestInfo;

public class DeleteRequestBodyReaderTest {

	@Mock
	private RequestInfo requestInfo;

	@Mock
	private UriInfo uriInfo;

	@InjectMocks
	private DeleteRequestBodyReader reader;

	private final String ID = "testId";

	@Before
	public void init() {
		reader = new DeleteRequestBodyReader();
		MockitoAnnotations.initMocks(this);

		MultivaluedHashMap<String, String> pathParams = new MultivaluedHashMap<>();
		pathParams.put("id", Arrays.asList(ID));

		when(uriInfo.getPathParameters()).thenReturn(pathParams);
		when(requestInfo.getUriInfo()).thenReturn(uriInfo);
	}

	@Test
	public void should_ConstructDeleteRequest_UsingRequestParams() throws WebApplicationException, IOException {
		assertTrue(reader.isReadable(DeleteRequest.class, null, null, null));

		String json = "{ \"userOperation\": \"customDelete\" }";

		DeleteRequest result = reader.readFrom(null, null, null, null, null, new ByteArrayInputStream(json.getBytes()));

		assertEquals(result.getTargetId(), ID);
		assertEquals(result.getUserOperation(), "customDelete");
	}

	@Test
	public void should_DefaultUserOperationToDelete() throws WebApplicationException, IOException {
		String json = "{}";

		DeleteRequest result = reader.readFrom(null, null, null, null, null, new ByteArrayInputStream(json.getBytes()));

		assertEquals(result.getUserOperation(), DeleteRequest.DELETE_OPERATION);
	}

}
