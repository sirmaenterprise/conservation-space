package com.sirma.itt.seip.adapters.iiif.rest;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import javax.json.Json;
import javax.json.JsonObject;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

import com.sirma.itt.seip.adapters.iiif.ImageManifestService;
import com.sirma.itt.seip.adapters.iiif.Manifest;
import com.srima.itt.seip.adapters.mock.ContentInfoMock;

/**
 * Tests the functionalities of the image rest service.
 *
 * @author Nikolay Ch
 */
public class ImageRestServiceTest {
	@InjectMocks
	private ImageRestService imageRestService = new ImageRestService();

	@Mock
	private ImageManifestService imageManifestService;

	@Before
	public void init() {
		MockitoAnnotations.initMocks(this);
	}

	/**
	 * Tests the method for getting and returning the manifest by given id.
	 *
	 * @throws Exception
	 *             when an error occurs
	 */
	@Test
	public void testManifestGetRequest() throws Exception {
		InputStream responseStream = new ByteArrayInputStream(
				Json.createObjectBuilder().add("asdf", 15).add("fdsa", 20).build().toString().getBytes("UTF-8"));
		ContentInfoMock contentInfo = new ContentInfoMock("manifestId");
		contentInfo.setInputStream(responseStream);
		Manifest manifest = new Manifest(contentInfo);
		Mockito.when(imageManifestService.getManifest(Mockito.any(String.class))).thenReturn(manifest);
		HttpServletResponse servletResponse = Mockito.mock(HttpServletResponse.class);
		Mockito.when(servletResponse.getOutputStream()).thenReturn(Mockito.mock(ServletOutputStream.class));
		Response response =  imageRestService.getManifest("manfiestId", servletResponse);

		assertNotNull(response);
		assertEquals(Status.OK.getStatusCode(), response.getStatus());
		Mockito.verify(servletResponse).setCharacterEncoding("UTF-8");
		Mockito.verify(servletResponse).setContentType("application/json");
	}

	/**
	 * Tests the extraction of the data and the creation of manifest.
	 */
	@Test
	public void testManifestCreationRequest() {
		JsonObject data = Json.createObjectBuilder().add("imageWidgetId", "idto").add("manifestId", "manifestId")
				.add("selectedImageIds", Json.createArrayBuilder().add("testId")).build();
		List<Serializable> testList = new ArrayList<Serializable>();
		testList.add("testId");
		Mockito.when(
				imageManifestService.processManifest(Mockito.anyString(), Mockito.anyString(), Mockito.any(List.class)))
				.thenReturn("manifestId");
		Response serviceResponse = imageRestService.updateManifest(data);
		assertEquals("manifestId", serviceResponse.getEntity());
		assertEquals(200, serviceResponse.getStatus());
	}
}
