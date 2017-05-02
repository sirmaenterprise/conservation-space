package com.sirma.itt.emf.io;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.argThat;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.assertNull;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.Serializable;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.apache.commons.io.IOUtils;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.mockito.Spy;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import com.sirma.itt.emf.adapter.DMSException;
import com.sirma.itt.seip.content.Content;
import com.sirma.itt.seip.content.ContentImport;
import com.sirma.itt.seip.content.ContentService;
import com.sirma.itt.seip.content.ContentSetter;
import com.sirma.itt.seip.content.ContentUploader;
import com.sirma.itt.seip.content.InstanceContentService;
import com.sirma.itt.seip.content.TextExtractor;
import com.sirma.itt.seip.domain.Purposable;
import com.sirma.itt.seip.domain.instance.DefaultProperties;
import com.sirma.itt.seip.domain.instance.EmfInstance;
import com.sirma.itt.seip.domain.instance.Instance;
import com.sirma.itt.seip.io.FileAndPropertiesDescriptor;
import com.sirma.itt.seip.io.FileDescriptor;
import com.sirma.itt.seip.testutil.CustomMatcher;

/**
 * Tests for {@link ContentService}.
 * @author BBonev
 * @author yasko
 */
@Test
public class ContentServiceImplTest {

	@InjectMocks
	private ContentServiceImpl contentService;

	@Mock
	private TextExtractor extractor;

	@Mock
	private ContentSetter setter;

	@Mock
	private ContentSetter inapplicable;

	@Spy
	private List<ContentSetter> contentSetters = new LinkedList<>();

	@Mock
	private ContentUploader contentUploaders;
	@Mock
	private InstanceContentService instanceContentService;

	@BeforeMethod
	public void beforeMethod() {
		MockitoAnnotations.initMocks(this);

		contentSetters.clear();
		contentSetters.add(setter);
		when(setter.isApplicable(any(Instance.class))).thenReturn(Boolean.TRUE);
		when(inapplicable.isApplicable(any(Instance.class))).thenReturn(Boolean.FALSE);
	}

	public void test_extractContent() throws IOException {
		when(extractor.extract(eq("text/plain"), any(FileDescriptor.class))).thenReturn(Optional.of("Content"));

		String content = contentService.extractContent("text/plain", mock(FileDescriptor.class));
		assertNotNull(content);
		assertEquals(content, "Content");
	}

	public void test_extractContent_error() throws IOException {
		when(extractor.extract(anyString(), any(FileDescriptor.class))).thenThrow(IOException.class);

		String content = contentService.extractContent("text/plain", mock(FileDescriptor.class));
		assertNull(content);
	}

	/**
	 * Test with no content setters available.
	 */
	public void testNoContentSetters() {
		contentSetters.clear();
		contentService.setContent(new EmfInstance(), "test");
	}

	/**
	 * Test with one applicable setter.
	 */
	public void testSetContent() {
		EmfInstance instance = new EmfInstance();
		contentService.setContent(instance, "test");

		Mockito.verify(setter).isApplicable(instance);
		Mockito.verify(setter).setContent(instance, "test");
	}

	/**
	 * Test with one applicable and one inapplicable setter.
	 */
	public void testSetContentWithOneInapplicable() {
		EmfInstance instance = new EmfInstance();
		contentSetters.add(0, inapplicable);
		contentService.setContent(instance, "test");

		Mockito.verify(inapplicable).isApplicable(instance);
		Mockito.verify(setter).isApplicable(instance);
		Mockito.verify(setter).setContent(instance, "test");
	}

	/**
	 * Test with null instance - it shouldn't blow up.
	 */
	public void testSetContentToNullInstance() {
		contentService.setContent(null, "test");
		Mockito.verify(setter, Mockito.never()).setContent(null, "test");
	}

	public void uploadContent_idoc() throws DMSException, IOException {
		WithPurpose instance = new WithPurpose();
		instance.setId("emf:instance");
		instance.setPurpose("iDoc");

		FileDescriptor descriptor = FileDescriptor
				.create(() -> new ByteArrayInputStream("test".getBytes(StandardCharsets.UTF_8)), 4);

		when(contentUploaders.uploadContent(eq(instance), any(FileDescriptor.class))).then(a -> {
			IOUtils.toString(a.getArgumentAt(1, FileDescriptor.class).getInputStream());
			FileAndPropertiesDescriptor result = mock(FileAndPropertiesDescriptor.class);
			when(result.getId()).thenReturn("workspace://alcresco/remoteId");
			Map<String, Serializable> map = new HashMap<>();
			map.put(DefaultProperties.NAME, "filename");
			map.put(DefaultProperties.MIMETYPE, "text/html");
			when(result.getProperties()).thenReturn(map);
			return result;
		});

		FileAndPropertiesDescriptor uploadContent = contentService.uploadContent(instance, descriptor);
		assertNotNull(uploadContent);
		verify(instanceContentService).importContent(argThat(CustomMatcher.of((ContentImport contentImport) -> {
					assertEquals(contentImport.getCharset(), StandardCharsets.UTF_8.name());
					assertEquals(contentImport.getRemoteId(), "workspace://alcresco/remoteId");
					assertEquals(contentImport.getRemoteSourceName(), "alfresco4");
					assertEquals(contentImport.getInstanceId(), "emf:instance");
					assertEquals(contentImport.getName(), "filename");
					assertEquals(contentImport.getMimeType(), "text/html");
					assertEquals(contentImport.getContentLength(), Long.valueOf(4));
					assertEquals(contentImport.getPurpose(), Content.PRIMARY_CONTENT);
					assertEquals(contentImport.isView(), true);
					return true;
		} , "expected argument does not match")));
	}

	public void uploadContent_image() throws DMSException, IOException {
		EmfInstance instance = new EmfInstance();
		instance.setId("emf:instance");

		FileDescriptor descriptor = FileDescriptor
				.create(() -> new ByteArrayInputStream("test".getBytes(StandardCharsets.UTF_8)), 4);

		when(contentUploaders.uploadContent(eq(instance), any(FileDescriptor.class))).then(a -> {
			IOUtils.toString(a.getArgumentAt(1, FileDescriptor.class).getInputStream());
			FileAndPropertiesDescriptor result = mock(FileAndPropertiesDescriptor.class);
			when(result.getId()).thenReturn("remoteImageId");
			Map<String, Serializable> map = new HashMap<>();
			map.put(DefaultProperties.NAME, "filename");
			map.put(DefaultProperties.MIMETYPE, "image/tiff");
			when(result.getProperties()).thenReturn(map);
			return result;
		});

		FileAndPropertiesDescriptor uploadContent = contentService.uploadContent(instance, descriptor);
		assertNotNull(uploadContent);
		verify(instanceContentService).importContent(argThat(CustomMatcher.of((ContentImport contentImport) -> {
					assertEquals(contentImport.getCharset(), StandardCharsets.UTF_8.name());
					assertEquals(contentImport.getRemoteId(), "remoteImageId");
					assertEquals(contentImport.getRemoteSourceName(), "iiif");
					assertEquals(contentImport.getInstanceId(), "emf:instance");
					assertEquals(contentImport.getName(), "filename");
					assertEquals(contentImport.getMimeType(), "image/tiff");
					assertEquals(contentImport.getContentLength(), Long.valueOf(4));
					assertEquals(contentImport.getPurpose(), Content.PRIMARY_CONTENT);
					assertEquals(contentImport.isView(), false);
					return true;
		} , "expected argument does not match")));
	}

	@Test
	public void uploadContent_invalidData() throws DMSException {
		assertNull(contentService.uploadContent(null, null));
		assertNull(contentService.uploadContent(new EmfInstance(), null));
		assertNull(contentService.uploadContent(null, mock(FileDescriptor.class)));
		// no service to handle it
		assertNull(contentService.uploadContent(new EmfInstance(), mock(FileDescriptor.class)));

		when(contentUploaders.uploadContent(any(Instance.class), any(FileDescriptor.class)))
				.thenThrow(DMSException.class);
		assertNull(contentService.uploadContent(new EmfInstance(), mock(FileDescriptor.class)));
	}

	private static class WithPurpose extends EmfInstance implements Purposable {

		@Override
		public String getPurpose() {
			return getString("purpose");
		}

		@Override
		public void setPurpose(String purpose) {
			add("purpose", purpose);
		}

	}
}
