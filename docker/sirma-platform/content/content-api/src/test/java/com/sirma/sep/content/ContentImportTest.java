package com.sirma.sep.content;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.junit.Test;

import com.sirma.sep.content.ContentImport;
import com.sirma.sep.content.ContentInfo;

/**
 * Test for the default implementation of {@link ContentImport}
 *
 * @author BBonev
 */
@SuppressWarnings("static-method")
public class ContentImportTest {

	@Test(expected = NullPointerException.class)
	public void copyFromShouldForbidNullArg() throws Exception {
		ContentImport.copyFrom(null);
	}

	@Test(expected = IllegalArgumentException.class)
	public void copyFromShouldForbidNonExsitentContent() throws Exception {
		ContentImport.copyFrom(ContentInfo.DO_NOT_EXIST);
	}

	@Test
	public void copyFromShouldCopyAllPropertiesExceptInstanceId() throws Exception {
		ContentInfo info = mock(ContentInfo.class);
		when(info.exists()).thenReturn(Boolean.TRUE);
		when(info.getCharset()).thenReturn("UTF-8");
		when(info.getContentId()).thenReturn("contentId");
		when(info.getContentPurpose()).thenReturn("purpose");
		when(info.getInstanceId()).thenReturn("instanceId");
		when(info.getLength()).thenReturn(1L);
		when(info.getMimeType()).thenReturn("image/jpg");
		when(info.getName()).thenReturn("avatar.jpg");
		when(info.getRemoteId()).thenReturn("remoteId");
		when(info.getRemoteSourceName()).thenReturn("iiif");

		ContentImport copyFrom = ContentImport.copyFrom(info);

		assertNotNull(copyFrom);
		assertEquals("UTF-8", copyFrom.getCharset());
		assertEquals(Long.valueOf(1L), copyFrom.getContentLength());
		assertNull("Instance id should not be copied", copyFrom.getInstanceId());
		assertEquals("image/jpg", copyFrom.getMimeType());
		assertEquals("avatar.jpg", copyFrom.getName());
		assertEquals("purpose", copyFrom.getPurpose());
		assertEquals("remoteId", copyFrom.getRemoteId());
		assertEquals("iiif", copyFrom.getRemoteSourceName());
		assertFalse(copyFrom.shouldDetectedMimeTypeFromContent());
	}

	@Test(expected = UnsupportedOperationException.class)
	public void unsupportedMimeTypeDetecting() {
		ContentImport.createEmpty().setDetectedMimeTypeFromContent(true);
	}

}
