package com.sirma.itt.seip.instance.editoffline.actions;

import com.sirma.itt.seip.rest.Range;
import com.sirma.itt.seip.rest.utils.request.RequestInfo;
import com.sirma.itt.seip.rest.utils.request.params.RequestParams;
import com.sirma.sep.content.Content;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.mockito.runners.MockitoJUnitRunner;

import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.BeanParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.UriInfo;
import javax.ws.rs.ext.MessageBodyReader;
import java.io.IOException;
import java.util.Arrays;

import static org.junit.Assert.*;

/**
 * @author Boyan Tonchev.
 */
public class EditOfflineCheckOutBodyReaderTest {

	private static final String INSTANCE_ID = "emf:id";

	@Mock
	private HttpServletResponse response;

	@Mock
	private UriInfo uriInfo;

	@Mock
	private MultivaluedMap pathParameters;

	@Mock
	private RequestInfo request;

	@InjectMocks
	private EditOfflineCheckOutBodyReader editOfflineCheckOutBodyReader;

	@Before
	public void setUp() {
		MockitoAnnotations.initMocks(this);
		Mockito.when(request.getUriInfo()).thenReturn(uriInfo);
		Mockito.when(uriInfo.getPathParameters()).thenReturn(pathParameters);
		Mockito.when(pathParameters.get("id")).thenReturn(Arrays.asList(INSTANCE_ID));
	}

	@Test
	public void should_ReturnCorrectEditOfflineCheckOutRequest() throws IOException {
		EditOfflineCheckOutRequest editOfflineCheckOutRequest = editOfflineCheckOutBodyReader.readFrom(
				EditOfflineCheckOutRequest.class, null, null, null, null, null);

		Assert.assertEquals(INSTANCE_ID, editOfflineCheckOutRequest.getTargetId());
		Assert.assertEquals(EditOfflineCheckOutRequest.EDIT_OFFLINE_CHECK_OUT,
							editOfflineCheckOutRequest.getUserOperation());
		Assert.assertTrue(editOfflineCheckOutRequest.getForDownload());
		Assert.assertEquals(Content.PRIMARY_CONTENT, editOfflineCheckOutRequest.getPurpose());
		Assert.assertEquals(Range.fromString(Range.DEFAULT_RANGE), editOfflineCheckOutRequest.getRange());
		Assert.assertEquals(response, editOfflineCheckOutRequest.getResponse());
	}

	@Test
	public void should_ReturnTrue_When_ClassIsSameAsEditOfflineCheckOutRequest() throws Exception {
		Assert.assertTrue(editOfflineCheckOutBodyReader.isReadable(EditOfflineCheckOutRequest.class, null, null, null));
	}

	@Test
	public void should_ReturnFalse_When_ClassIsDiferentThanEditOfflineCheckOutRequest() throws Exception {
		Assert.assertFalse(
				editOfflineCheckOutBodyReader.isReadable(EditOfflineCheckOutBodyReaderTest.class, null, null, null));
	}
}