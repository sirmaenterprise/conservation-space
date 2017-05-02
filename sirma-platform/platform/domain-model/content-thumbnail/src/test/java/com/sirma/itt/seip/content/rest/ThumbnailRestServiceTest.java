package com.sirma.itt.seip.content.rest;

import static org.mockito.Mockito.when;

import java.nio.charset.StandardCharsets;
import java.util.Base64;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import com.sirma.itt.seip.content.rendition.RenditionService;
import com.sirma.itt.seip.rest.exceptions.ResourceNotFoundException;

/**
 * Tests for {@link ThumbnailRestService}.
 * 
 * @author yasko
 */
public class ThumbnailRestServiceTest {
	
	private static final byte[] BYTES = "this is some test data".getBytes(StandardCharsets.UTF_8);
	private static final String BASE64_BYTES = Base64.getEncoder().encodeToString(ThumbnailRestServiceTest.BYTES);
	private static final String THUMBTAIL = RenditionService.BASE64_IMAGE_PREFIX + ThumbnailRestServiceTest.BASE64_BYTES;

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
		Assert.assertArrayEquals(BYTES, service.load("1"));
	}
	
	@Test
	public void testEncodedNoPrefix() {
		Assert.assertArrayEquals(BYTES, service.load("2"));
	}
	
	@Test(expected = ResourceNotFoundException.class)
	public void testResourceNotFound() {
		service.load("3");
	}
}
