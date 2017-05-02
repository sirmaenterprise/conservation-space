package com.sirma.itt.seip.content.actions.icons;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;

import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.UriInfo;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

import com.sirma.itt.seip.rest.utils.request.RequestInfo;

/**
 * Test for the add icons body reader.
 *
 * @author Nikolay Ch
 */
public class AddIconsBodyReaderTest {

	@Mock
	private RequestInfo request;

	@Mock
	private MultivaluedMap<String, String> paramsMap;

	@Mock
	private UriInfo uriInfo;

	@InjectMocks
	private AddIconsBodyReader addIconsBodyReader = new AddIconsBodyReader();

	/**
	 * Setup the mocks.
	 */
	@Before
	public void setup() {
		MockitoAnnotations.initMocks(this);
		Mockito.when(paramsMap.get("id")).thenReturn(Arrays.asList("instanceId"));
		Mockito.when(uriInfo.getPathParameters()).thenReturn(paramsMap);
		Mockito.when(request.getUriInfo()).thenReturn(uriInfo);
	}

	/**
	 * Test if the MessageBodyReader can produce an instance of a AddIconsActionRequest type. It should be.
	 */
	@Test
	public void testIsReadableTrue() {
		Assert.assertTrue(addIconsBodyReader.isReadable(AddIconsRequest.class, null, null, null));
	}

	/**
	 * Test the readFrom method with valid data.
	 *
	 * @throws IOException
	 *             the io exception
	 */
	@Test
	public void testReadFrom() throws IOException {
		try (InputStream stream = AddIconsBodyReaderTest.class
				.getResourceAsStream("/add-icons-action-reader-test.json")) {
			AddIconsRequest actionRequest = addIconsBodyReader.readFrom(null, null, null, null, null, stream);
			assertNotNull(actionRequest);
			assertEquals("addIcons", actionRequest.getOperation());
			assertEquals(2, actionRequest.getPurposeIconMapping().size());
		}
	}

}
