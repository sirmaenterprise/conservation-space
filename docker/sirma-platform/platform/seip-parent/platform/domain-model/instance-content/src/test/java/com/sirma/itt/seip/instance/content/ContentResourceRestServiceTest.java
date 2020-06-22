package com.sirma.itt.seip.instance.content;

import static org.junit.Assert.assertEquals;

import java.io.Serializable;
import java.util.Base64;

import javax.ws.rs.core.Response;

import org.apache.commons.io.IOUtils;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Matchers;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

import com.sirma.itt.seip.content.ContentResourceManagerService;
import com.sirma.sep.content.ContentInfo;

/**
 * Test for the {@link ContentResourceRestService} .
 *
 * @author Nikolay Ch
 */
public class ContentResourceRestServiceTest {

	@Mock
	private ContentResourceManagerService contentManager;

	@InjectMocks
	private ContentResourceRestService contentResourceRestService;

	@Before
	public void init() {
		MockitoAnnotations.initMocks(this);
	}

	/**
	 * Tests the get content method of the service when given wrong data.
	 *
	 * @param <T>
	 */
	@Test
	public void testGetResourceWithWrongData() {
		ContentInfo contentInfo = Mockito.mock(ContentInfo.class);
		Mockito.when(contentInfo.exists()).thenReturn(false);
		Mockito.when(contentManager.getContent(Matchers.any(Serializable.class), Matchers.any(String.class)))
				.thenReturn(contentInfo);
		Response response = contentResourceRestService.getResource("contentId");
		assertEquals(response.getStatus(), 404);
		Mockito.verify(contentManager, Mockito.times(1)).getContent(Matchers.any(Serializable.class),
				Matchers.any(String.class));
	}

	/**
	 * Tests the get content method of the service when given correct data.
	 */
	@Test
	public void testGetResource() {
		String base64 = Base64.getEncoder().encodeToString(new String("image").getBytes());
		ContentInfo contentInfo = Mockito.mock(ContentInfo.class);
		Mockito.when(contentInfo.exists()).thenReturn(true);
		Mockito.when(contentInfo.getInputStream()).thenReturn(IOUtils.toInputStream("prefix," + base64));
		Mockito.when(contentManager.getContent(Matchers.any(Serializable.class), Matchers.any(String.class)))
				.thenReturn(contentInfo);
		Response response = contentResourceRestService.getResource("contentId");
		assertEquals(response.getStatus(), 200);
		Mockito.verify(contentManager, Mockito.times(1)).getContent(Matchers.any(Serializable.class),
				Matchers.any(String.class));
	}
}
