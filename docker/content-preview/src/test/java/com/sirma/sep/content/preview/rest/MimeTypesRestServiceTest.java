package com.sirma.sep.content.preview.rest;

import com.sirma.sep.content.preview.mimetype.MimeType;
import com.sirma.sep.content.preview.mimetype.MimeTypesResolver;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Matchers;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.Optional;

/**
 * Tests the mime type filtering in {@link MimeTypesRestService}.
 *
 * @author Mihail Radkov
 */
public class MimeTypesRestServiceTest {

	@Mock
	private MimeTypesResolver mimeTypesService;

	@InjectMocks
	private MimeTypesRestService mimeTypesRestService;

	@Before
	public void init() {
		MockitoAnnotations.initMocks(this);
	}

	@Test
	public void filterMimetype_badRequest() {
		ResponseEntity response = mimeTypesRestService.filterMimetype(null);
		Assert.assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
		response = mimeTypesRestService.filterMimetype("");
		Assert.assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
	}

	@Test
	public void filterMimetype_notFound() {
		Mockito.when(mimeTypesService.resolve(Matchers.eq("mime/type"))).thenReturn(Optional.empty());
		ResponseEntity response = mimeTypesRestService.filterMimetype("mime/type");
		Assert.assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
	}

	@Test
	public void filterMimetype_found() {
		MimeType mimeType = new MimeType();
		Mockito.when(mimeTypesService.resolve(Matchers.eq("mime/type"))).thenReturn(Optional.of(mimeType));
		ResponseEntity response = mimeTypesRestService.filterMimetype("mime/type");
		Assert.assertEquals(HttpStatus.OK, response.getStatusCode());
		Assert.assertEquals(mimeType, response.getBody());
	}
}
