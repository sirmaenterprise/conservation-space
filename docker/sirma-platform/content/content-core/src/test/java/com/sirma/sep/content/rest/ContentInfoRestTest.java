package com.sirma.sep.content.rest;

import static org.mockito.Mockito.when;

import java.io.InputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

import com.sirma.sep.content.ContentInfo;
import com.sirma.sep.content.ContentMetadata;
import com.sirma.sep.content.InstanceContentService;
import com.sirma.sep.content.rest.ContentRestService;
/**
 * Test class for {@link ContentInfoRest}. 
 * 
 * @author Nikolay Ch
 */
public class ContentInfoRestTest {

	@InjectMocks
	private ContentRestService service;

	@Mock
	private InstanceContentService instanceContentService;

	@Before
	public void beforeMethod() {
		MockitoAnnotations.initMocks(this);
		when(instanceContentService.getContent(Mockito.anyString(), (String) Mockito.isNull()))
				.thenReturn(new ContentInfoMock());
	}

	@Test
	public void testidContentMapping() {
		List<String> contentIds = new ArrayList<>();
		contentIds.add("emf:1");
		contentIds.add("emf:2");
		Map<String, ContentInfo> contentIdMapper = service.fetchContentInfo(contentIds);

		Assert.assertEquals(contentIdMapper.size(), 2);
		Assert.assertNotNull(contentIdMapper.get("emf:2"));
		Mockito.verify(instanceContentService, Mockito.times(2)).getContent(Mockito.anyString(),
				(String) Mockito.isNull());
	}

	private class ContentInfoMock implements ContentInfo {

		private static final long serialVersionUID = 1L;

		@Override
		public String getId() {
			return null;
		}

		@Override
		public String getContainerId() {
			return null;
		}

		@Override
		public InputStream getInputStream() {
			return null;
		}

		@Override
		public void close() {

		}

		@Override
		public String getName() {
			return null;
		}

		@Override
		public String getContentId() {
			return null;
		}

		@Override
		public Serializable getInstanceId() {
			return null;
		}

		@Override
		public String getContentPurpose() {
			return null;
		}

		@Override
		public boolean exists() {
			return true;
		}

		@Override
		public String getMimeType() {
			return null;
		}

		@Override
		public long getLength() {
			return 0;
		}

		@Override
		public boolean isView() {
			return false;
		}

		@Override
		public String getCharset() {
			return null;
		}

		@Override
		public String getRemoteId() {
			return null;
		}

		@Override
		public String getRemoteSourceName() {
			return null;
		}

		@Override
		public String getChecksum() {
			return null;
		}

		@Override
		public ContentMetadata getMetadata() {
			return null;
		}

		@Override
		public boolean isIndexable() {
			return false;
		}

		@Override
		public boolean isReuseable() {
			return false;
		}

	}
}
