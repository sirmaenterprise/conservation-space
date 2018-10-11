package com.sirma.sep.content.rest;

import static org.mockito.Mockito.when;

import java.nio.charset.StandardCharsets;
import java.util.Base64;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import com.sirma.sep.content.rendition.RenditionService;

/**
 * Tests for {@link ThumbnailRestService}.
 * 
 * @author yasko
 */
public class ThumbnailRestServiceTest {

	private static final byte[] BYTES = "this is some test data".getBytes(StandardCharsets.UTF_8);
	private static final String BASE64_BYTES = Base64.getEncoder().encodeToString(ThumbnailRestServiceTest.BYTES);
	private static final String THUMBTAIL = RenditionService.BASE64_IMAGE_PREFIX
			+ ThumbnailRestServiceTest.BASE64_BYTES;

	@Mock
	private RenditionService renditionService;

	@InjectMocks
	private ThumbnailRestService service;

	@Before
	public void init() {
		MockitoAnnotations.initMocks(this);
		when(renditionService.getThumbnail("1")).thenReturn(THUMBTAIL);
		when(renditionService.getThumbnail("2")).thenReturn(BASE64_BYTES);
		when(renditionService.getThumbnail("3")).thenReturn(null);
	}

	@Test
	public void testRetrieveThumbnail() {
		Assert.assertEquals(200, service.load("1").getStatus());
		Assert.assertArrayEquals(BYTES, (byte[]) service.load("1").getEntity());
	}

	@Test
	public void testEncodedNoPrefix() {
		Assert.assertEquals(200, service.load("2").getStatus());
		Assert.assertArrayEquals(BYTES, (byte[]) service.load("2").getEntity());
	}

	@Test
	public void testResourceNotFound() {
		Assert.assertEquals(404, service.load("3").getStatus());
		Assert.assertNull(service.load("3").getEntity());
	}
}
