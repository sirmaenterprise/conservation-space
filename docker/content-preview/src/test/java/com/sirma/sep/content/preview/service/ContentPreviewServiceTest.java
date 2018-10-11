package com.sirma.sep.content.preview.service;

import com.sirma.sep.content.preview.TestFileUtils;
import com.sirma.sep.content.preview.mimetype.MimeType;
import com.sirma.sep.content.preview.mimetype.MimeTypeSupport;
import com.sirma.sep.content.preview.mimetype.MimeTypesResolver;
import com.sirma.sep.content.preview.model.ContentPreviewRequest;
import com.sirma.sep.content.preview.model.ContentPreviewResponse;
import com.sirma.sep.content.preview.generator.ContentPreviewGenerator;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Matchers;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.springframework.http.MediaType;
import org.springframework.util.Base64Utils;

import java.io.File;
import java.nio.file.Files;
import java.util.Optional;

/**
 * Tests the service logic in {@link ContentPreviewService} of receiving {@link ContentPreviewRequest} and producing
 * {@link ContentPreviewResponse}.
 *
 * @author Mihail Radkov
 */
public class ContentPreviewServiceTest {

	@Mock
	private MimeTypesResolver mimeTypesService;
	@Mock
	private ContentPreviewGenerator contentPreviewGenerator;
	@InjectMocks
	private ContentPreviewService contentPreviewService;

	@Before
	public void beforeEach() {
		MockitoAnnotations.initMocks(this);
		mockMimetypeResolver(MediaType.TEXT_PLAIN_VALUE, MimeTypeSupport.YES, MimeTypeSupport.YES);
		mockMimetypeResolver(MediaType.APPLICATION_PDF_VALUE, MimeTypeSupport.SELF, MimeTypeSupport.YES);
		mockMimetypeResolver(MediaType.IMAGE_PNG_VALUE, MimeTypeSupport.NO, MimeTypeSupport.YES);
		mockMimetypeResolver(MediaType.APPLICATION_OCTET_STREAM_VALUE, MimeTypeSupport.NO, MimeTypeSupport.NO);
	}

	@Test
	public void shouldSupportPdf() {
		Assert.assertTrue(contentPreviewService.isContentSupported(MediaType.TEXT_PLAIN_VALUE));
	}

	@Test
	public void shouldNotSupportOctetStream() {
		Assert.assertFalse(contentPreviewService.isContentSupported(MediaType.APPLICATION_OCTET_STREAM_VALUE));
	}

	@Test(expected = IllegalArgumentException.class)
	public void processingNullFile_shouldFail() {
		ContentPreviewRequest request = new ContentPreviewRequest(null, "", "instance-id");
		contentPreviewService.processRequest(request);
	}

	@Test(expected = IllegalArgumentException.class)
	public void processingMissingFile_shouldFail() {
		ContentPreviewRequest request = new ContentPreviewRequest(new File(""), "", "instance-id");
		contentPreviewService.processRequest(request);
	}

	@Test(expected = IllegalArgumentException.class)
	public void processingMissingMimetype_shouldFail() throws Exception {
		File content = TestFileUtils.getTempFile();
		ContentPreviewRequest request = new ContentPreviewRequest(content, MediaType.TEXT_XML_VALUE, "instance-id");
		Mockito.when(mimeTypesService.resolve(Matchers.eq(MediaType.TEXT_XML_VALUE))).thenReturn(Optional.empty());
		contentPreviewService.processRequest(request);
	}

	@Test
	public void processing_shouldGenerateResponse() throws Exception {
		File content = TestFileUtils.getTempFile();
		ContentPreviewRequest request = new ContentPreviewRequest(content, MediaType.TEXT_PLAIN_VALUE, "instance-id");

		File preview = TestFileUtils.getTempFile();
		File thumbnail = TestFileUtils.getTempFile();
		Files.write(thumbnail.toPath(), "testing".getBytes());
		Mockito.when(contentPreviewGenerator.generatePreview(Matchers.eq(content), Matchers.eq(1))).thenReturn(preview);
		Mockito.when(contentPreviewGenerator.generateThumbnail(Matchers.eq(preview))).thenReturn(thumbnail);

		ContentPreviewResponse contentPreviewResponse = contentPreviewService.processRequest(request);
		Mockito.verify(contentPreviewGenerator).generatePreview(Matchers.eq(content), Matchers.eq(1));
		Mockito.verify(contentPreviewGenerator).generateThumbnail(Matchers.eq(preview));

		Assert.assertNotNull(contentPreviewResponse);
		Assert.assertEquals(preview, contentPreviewResponse.getPreview());
		String expectedThumbnail = Base64Utils.encodeToString("testing".getBytes());
		Assert.assertEquals(expectedThumbnail, contentPreviewResponse.getThumbnail());
	}

	@Test
	public void processingPdf_shouldGenerateOnlyThumbnail() throws Exception {
		File content = TestFileUtils.getTempFile();
		ContentPreviewRequest request = new ContentPreviewRequest(content, MediaType.APPLICATION_PDF_VALUE,
																  "instance-id");

		assertOnlyThumbnail(content, request);
	}

	@Test
	public void processingImage_shouldGenerateOnlyThumbnail() throws Exception {
		File content = TestFileUtils.getTempFile();
		ContentPreviewRequest request = new ContentPreviewRequest(content, MediaType.IMAGE_PNG_VALUE, "instance-id");

		assertOnlyThumbnail(content, request);
	}

	private void assertOnlyThumbnail(File content, ContentPreviewRequest request) throws Exception {
		File thumbnail = TestFileUtils.getTempFile();
		Files.write(thumbnail.toPath(), "testing".getBytes());
		Mockito.when(contentPreviewGenerator.generateThumbnail(Matchers.eq(content))).thenReturn(thumbnail);

		ContentPreviewResponse contentPreviewResponse = contentPreviewService.processRequest(request);
		Mockito.verify(contentPreviewGenerator, Mockito.times(0)).generatePreview(Matchers.any(), Matchers.eq(1));
		Mockito.verify(contentPreviewGenerator).generateThumbnail(Matchers.eq(content));

		Assert.assertNotNull(contentPreviewResponse);
		Assert.assertNull(contentPreviewResponse.getPreview());
		String expectedThumbnail = Base64Utils.encodeToString("testing".getBytes());
		Assert.assertEquals(expectedThumbnail, contentPreviewResponse.getThumbnail());
	}

	@Test
	public void processingUnsupported_shouldNotGenerateAnything() throws Exception {
		ContentPreviewRequest request = new ContentPreviewRequest(TestFileUtils.getTempFile(), MediaType.APPLICATION_OCTET_STREAM_VALUE, "instance-id");
		assertNoGeneration(request);
	}

	@Test
	public void notGeneratingPreview_shouldSkipThumbnailGeneration() throws Exception {
		ContentPreviewRequest request = new ContentPreviewRequest(TestFileUtils.getTempFile(),
																  MediaType.TEXT_PLAIN_VALUE, "instance-id");
		File missingPreview = new File("");
		Mockito.when(contentPreviewGenerator.generatePreview(Matchers.any(File.class), Matchers.eq(1))).thenReturn(missingPreview);
		ContentPreviewResponse contentPreviewResponse = contentPreviewService.processRequest(request);
		Mockito.verify(contentPreviewGenerator, Mockito.times(0)).generateThumbnail(Matchers.any());
		Assert.assertNull(contentPreviewResponse.getPreview());
		Assert.assertNull(contentPreviewResponse.getThumbnail());
	}

	private void assertNoGeneration(ContentPreviewRequest request) {
		ContentPreviewResponse contentPreviewResponse = contentPreviewService.processRequest(request);
		Mockito.verify(contentPreviewGenerator, Mockito.times(0)).generatePreview(Matchers.any(), Matchers.eq(1));
		Mockito.verify(contentPreviewGenerator, Mockito.times(0)).generateThumbnail(Matchers.any());
		Assert.assertNull(contentPreviewResponse.getPreview());
		Assert.assertNull(contentPreviewResponse.getThumbnail());
	}

	@Test(expected = IllegalStateException.class)
	public void generatingMissingThumbnail_shouldFail() throws Exception {
		ContentPreviewRequest request = new ContentPreviewRequest(TestFileUtils.getTempFile(),
																  MediaType.APPLICATION_PDF_VALUE, "instance-id");
		File missingThumbnail = new File("");
		Mockito.when(contentPreviewGenerator.generateThumbnail(Matchers.any(File.class))).thenReturn(missingThumbnail);
		contentPreviewService.processRequest(request);
	}

	private void mockMimetypeResolver(String providedMimetype, MimeTypeSupport preview, MimeTypeSupport thumbnail) {
		MimeType mimeType = new MimeType();
		mimeType.setName(providedMimetype);
		mimeType.setPreview(preview);
		mimeType.setThumbnail(thumbnail);
		Mockito.when(mimeTypesService.resolve(Matchers.eq(providedMimetype))).thenReturn(Optional.of(mimeType));
	}

}
