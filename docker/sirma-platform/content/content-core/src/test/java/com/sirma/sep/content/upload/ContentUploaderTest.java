package com.sirma.sep.content.upload;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.argThat;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.Serializable;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Collections;
import java.util.Map;

import org.apache.commons.fileupload.FileItem;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Matchers;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import com.sirma.itt.seip.domain.instance.EmfInstance;
import com.sirma.itt.seip.domain.instance.Instance;
import com.sirma.itt.seip.rest.exceptions.BadRequestException;
import com.sirma.itt.seip.rest.exceptions.ResourceException;
import com.sirma.itt.seip.testutil.CustomMatcher;
import com.sirma.sep.content.Content;
import com.sirma.sep.content.ContentInfo;
import com.sirma.sep.content.InstanceContentService;
import com.sirma.sep.content.upload.ContentUploader;
import com.sirma.sep.content.upload.RepositoryFileItemFactory;
import com.sirma.sep.content.upload.UploadRequest;

/**
 * Test for {@link ContentUploader}.
 *
 * @author BBonev
 * @author Vilizar Tsonev
 */
public class ContentUploaderTest {

	@InjectMocks
	private ContentUploader contentUploader;
	@Mock
	private InstanceContentService instanceContentService;
	@Mock
	private RepositoryFileItemFactory factory;

	@Before
	public void beforeMethod() {
		MockitoAnnotations.initMocks(this);
	}

	@Test
	public void testUploadWithoutInstance() throws IOException {
		FileItem fileItem = mock(FileItem.class);
		when(fileItem.isFormField()).thenReturn(Boolean.FALSE);
		when(fileItem.getContentType()).thenReturn("text/plain");
		when(fileItem.getName()).thenReturn("test.txt");
		when(fileItem.getSize()).thenReturn(4L);
		when(fileItem.getInputStream()).then(a -> new ByteArrayInputStream("test".getBytes(StandardCharsets.UTF_8)));

		UploadRequest request = new UploadRequest(Arrays.asList(fileItem), factory);
		when(instanceContentService.saveContent(any(Instance.class), any(Content.class)))
				.thenReturn(mock(ContentInfo.class));

		ContentInfo contentInfo = contentUploader.uploadWithoutInstance(request, Content.PRIMARY_CONTENT);
		assertNotNull(contentInfo);
		verify(instanceContentService).saveContent(any(Instance.class), argThat(CustomMatcher.of((Content content) -> {
			assertEquals("text/plain", content.getMimeType());
			assertEquals(Long.valueOf(4), content.getContentLength());
			assertEquals("test.txt", content.getName());
			assertNotNull(content.getContent());
			assertEquals("test.txt", content.getContent().getId());
			try {
				assertEquals("test", content.getContent().asString());
			} catch (IOException e) {
				fail(e.getMessage());
			}
			return true;
		})));
		verify(factory).resetRepository();
	}

	@Test(expected = BadRequestException.class)
	public void testUploadWithoutInstance_noItems() throws IOException {
		UploadRequest request = new UploadRequest(Collections.emptyList(), factory);
		try {
			contentUploader.uploadWithoutInstance(request, Content.PRIMARY_CONTENT);
		} finally {
			verify(factory).resetRepository();
		}
	}

	@Test(expected = BadRequestException.class)
	public void testUploadWithoutInstance_noFileItem() throws IOException {

		FileItem fileItem = mock(FileItem.class);
		when(fileItem.isFormField()).thenReturn(Boolean.TRUE);

		UploadRequest request = new UploadRequest(Arrays.asList(fileItem), factory);
		try {
			contentUploader.uploadWithoutInstance(request, Content.PRIMARY_CONTENT);
		} finally {
			verify(factory).resetRepository();
		}
	}

	/**
	 * Tests {@link ContentUploader#updateContent(Content, String)}.
	 */
	@Test
	public void testUpdateContent() {
		String contentId = "sampleContentId";
		String purpose = "somePurpose";
		String instanceID = "instanceID";
		String mimeType = "text/plain";

		Content content = mock(Content.class);
		content.setPurpose(purpose);

		ContentInfo contentInfo = mock(ContentInfo.class);
		when(contentInfo.getContentPurpose()).thenReturn(purpose);
		when(contentInfo.getInstanceId()).thenReturn(instanceID);
		when(contentInfo.getMimeType()).thenReturn(mimeType);
		when(instanceContentService.getContent(eq(contentId), eq(""))).thenReturn(contentInfo);

		contentUploader.updateContent(new EmfInstance(), content, contentId);
		verify(instanceContentService).saveContent(Matchers.any(Instance.class), eq(content));
	}

	@Test
	public void testUploadForInstance() throws Exception {
		FileItem fileItem = mock(FileItem.class);
		when(fileItem.isFormField()).thenReturn(Boolean.FALSE);
		when(fileItem.getContentType()).thenReturn("text/plain");
		when(fileItem.getName()).thenReturn("test.txt");
		when(fileItem.getSize()).thenReturn(4L);
		when(fileItem.getInputStream()).then(a -> new ByteArrayInputStream("test".getBytes(StandardCharsets.UTF_8)));

		FileItem metadata = mock(FileItem.class);
		when(metadata.isFormField()).thenReturn(Boolean.TRUE);
		when(metadata.getContentType()).thenReturn("application/json");
		when(metadata.getFieldName()).thenReturn("metadata");
		when(metadata.getInputStream())
				.then(a -> new ByteArrayInputStream("{\"property\":\"value\"}".getBytes(StandardCharsets.UTF_8)));

		UploadRequest request = new UploadRequest(Arrays.asList(fileItem, metadata), factory);
		when(instanceContentService.saveContent(any(Instance.class), any(Content.class)))
				.thenReturn(mock(ContentInfo.class));

		ContentInfo contentInfo = contentUploader.uploadForInstance(request, "emf:instance", Content.PRIMARY_CONTENT,
				true);
		assertNotNull(contentInfo);
		verify(instanceContentService).saveContent(argThat(CustomMatcher.ofPredicate((Instance instance) -> {
			assertEquals("emf:instance", instance.getId());
			assertTrue(instance.isPropertyPresent("property"));
			return true;
		})), argThat(CustomMatcher.ofPredicate((Content content) -> {
			assertEquals("text/plain", content.getMimeType());
			assertEquals(Long.valueOf(4), content.getContentLength());
			assertEquals("test.txt", content.getName());
			assertNotNull(content.getContent());
			assertEquals("test.txt", content.getContent().getId());
			try {
				assertEquals("test", content.getContent().asString());
			} catch (IOException e) {
				fail(e.getMessage());
			}
			return true;
		})));
		verify(factory).resetRepository();
	}

	@Test
	public void testRelatedInstanceMetadata() throws Exception {
		FileItem fileItem = mock(FileItem.class);
		when(fileItem.isFormField()).thenReturn(Boolean.TRUE);
		when(fileItem.getContentType()).thenReturn("application/json");
		when(fileItem.getFieldName()).thenReturn("metadata");
		when(fileItem.getInputStream())
				.then(a -> new ByteArrayInputStream("{\"property\":\"value\"}".getBytes(StandardCharsets.UTF_8)));

		UploadRequest request = new UploadRequest(Arrays.asList(fileItem), factory);

		Map<String, Serializable> metadata = contentUploader.getRelatedInstanceMetadata(request);
		assertNotNull(metadata);
		assertFalse(metadata.isEmpty());
		assertEquals("value", metadata.get("property"));
	}

	@Test
	public void testRelatedInstanceMetadata_noItems() throws Exception {
		UploadRequest request = new UploadRequest(Collections.emptyList(), factory);

		Map<String, Serializable> metadata = contentUploader.getRelatedInstanceMetadata(request);
		assertNotNull(metadata);
		assertTrue(metadata.isEmpty());
	}

	@Test
	public void testUpdateWithoutInstance() throws IOException {
		FileItem fileItem = mock(FileItem.class);
		when(fileItem.isFormField()).thenReturn(Boolean.FALSE);
		when(fileItem.getContentType()).thenReturn("text/plain");
		when(fileItem.getName()).thenReturn("test.txt");
		when(fileItem.getSize()).thenReturn(4L);
		when(fileItem.getInputStream()).then(a -> new ByteArrayInputStream("test".getBytes(StandardCharsets.UTF_8)));

		ContentInfo oldContent = mock(ContentInfo.class);
		when(oldContent.exists()).thenReturn(Boolean.TRUE);
		when(oldContent.getContentPurpose()).thenReturn(Content.PRIMARY_CONTENT);
		when(oldContent.getInstanceId()).thenReturn("emf:instanceId");
		when(instanceContentService.getContent("contentId", "")).thenReturn(oldContent);

		UploadRequest request = new UploadRequest(Arrays.asList(fileItem), factory);
		when(instanceContentService.saveContent(any(Instance.class), any(Content.class)))
				.thenReturn(mock(ContentInfo.class));

		ContentInfo contentInfo = contentUploader.updateWithoutInstance("contentId", request);
		assertNotNull(contentInfo);
		verify(instanceContentService).saveContent(any(Instance.class), argThat(CustomMatcher.of((Content content) -> {
			assertEquals("text/plain", content.getMimeType());
			assertEquals(Long.valueOf(4), content.getContentLength());
			assertEquals("test.txt", content.getName());
			assertNotNull(content.getContent());
			assertEquals("test.txt", content.getContent().getId());
			try {
				assertEquals("test", content.getContent().asString());
			} catch (IOException e) {
				fail(e.getMessage());
			}
			return true;
		})));
		verify(factory).resetRepository();
	}

	@Test(expected = ResourceException.class)
	public void testUpdateWithoutInstance_nonExisting() throws IOException {

		when(instanceContentService.getContent("contentId", "")).thenReturn(ContentInfo.DO_NOT_EXIST);

		UploadRequest request = mock(UploadRequest.class);
		when(request.getFileItemFactory()).thenReturn(mock(RepositoryFileItemFactory.class));
		when(instanceContentService.saveContent(any(Instance.class), any(Content.class)))
				.thenReturn(mock(ContentInfo.class));

		contentUploader.updateWithoutInstance("contentId", request);
	}

}
