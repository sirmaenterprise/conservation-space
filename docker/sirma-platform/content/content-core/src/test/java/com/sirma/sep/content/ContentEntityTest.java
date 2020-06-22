/**
 *
 */
package com.sirma.sep.content;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.HashMap;
import java.util.Map;

import org.junit.Test;

import com.sirma.itt.seip.io.FileDescriptor;
import com.sirma.itt.seip.model.SerializableValue;
import com.sirma.sep.content.Content;
import com.sirma.sep.content.ContentEntity;
import com.sirma.sep.content.ContentImport;
import com.sirma.sep.content.StoreItemInfo;

/**
 * Tests for {@link ContentEntity}.
 *
 * @author BBonev
 */
@SuppressWarnings("static-method")
public class ContentEntityTest {

	@Test
	public void testEquals() throws Exception {
		ContentEntity entity1 = new ContentEntity();
		entity1.setId("1");
		entity1.setInstanceId("emf:instanceId");
		entity1.setPurpose("type");

		ContentEntity entity2 = new ContentEntity();
		entity2.setId("1");
		entity2.setInstanceId("emf:instanceId");
		entity2.setPurpose("type");
		assertTrue(entity1.equals(entity2));

		entity2.setId("2");
		assertFalse(entity1.equals(entity2));

		entity2.setInstanceId("emf:instanceId1");
		assertFalse(entity1.equals(entity2));

		entity2.setPurpose("type1");
		assertFalse(entity1.equals(entity2));

		assertTrue(entity1.equals(entity1));

		assertFalse(entity1.equals(null));

		assertFalse(entity1.equals(new StoreItemInfo()));
	}

	@Test
	public void testHashCode() throws Exception {
		ContentEntity entity1 = new ContentEntity();
		entity1.setId("1");
		entity1.setInstanceId("emf:instanceId");
		entity1.setPurpose("type");

		ContentEntity entity2 = new ContentEntity();
		entity2.setId("1");
		entity2.setInstanceId("emf:instanceId");
		entity2.setPurpose("type");
		assertTrue(entity1.hashCode() == entity2.hashCode());

		entity2.setId("2");
		assertFalse(entity1.hashCode() == entity2.hashCode());

		entity2.setInstanceId("emf:instanceId1");
		assertFalse(entity1.hashCode() == entity2.hashCode());

		entity2.setPurpose("type1");
		assertFalse(entity1.hashCode() == entity2.hashCode());

		entity2.setInstanceId(null);
		assertFalse(entity1.hashCode() == entity2.hashCode());

		entity2.setPurpose(null);
		assertFalse(entity1.hashCode() == entity2.hashCode());
	}

	@Test
	public void copyFrom_StoreItemInfo() throws Exception {
		ContentEntity entity = new ContentEntity();

		entity.copyFrom((StoreItemInfo) null);

		StoreItemInfo storeItemInfo = new StoreItemInfo()
				.setRemoteId("remoteId")
					.setProviderType("providerType")
					.setContentLength(1L)
					.setAdditionalData(new HashMap<>());
		entity.copyFrom(storeItemInfo);

		assertEquals("remoteId", entity.getRemoteId());
		assertEquals("providerType", entity.getRemoteSourceName());
		assertEquals(Long.valueOf(1L), entity.getContentSize());
		assertNotNull(entity.getAdditionalInfo());

		entity.copyFrom(storeItemInfo);
	}

	@Test
	public void copyFrom_Content() throws Exception {
		ContentEntity entity = new ContentEntity();

		entity.copyFrom((Content) null);

		Content content = Content
				.createEmpty()
					.setContentLength(1L)
					.setCharset("charset")
					.setMimeType("mimetype")
					.setName("name")
					.setPurpose("purpose")
					.setView(true);
		entity.copyFrom(content);

		assertEquals("charset", entity.getCharset());
		assertEquals("mimetype", entity.getMimeType());
		assertEquals("name", entity.getName());
		assertEquals("purpose", entity.getPurpose());
		assertEquals(Boolean.TRUE, entity.getView());
		assertEquals(Long.valueOf(1L), entity.getContentSize());

		FileDescriptor descriptor = mock(FileDescriptor.class);
		when(descriptor.getId()).thenReturn("descriptorName");
		content.setName(null).setContentLength(null).setContent(descriptor);

		entity = new ContentEntity();
		entity.copyFrom(content);
		assertEquals("descriptorName", entity.getName());
		assertEquals(Long.valueOf(-1L), entity.getContentSize());
	}

	@Test
	public void mergeFrom() throws Exception {

		new ContentEntity().merge(null);

		ContentEntity entity = new ContentEntity();
		ContentImport contentImport = ContentImport
				.createEmpty()
					.setCharset("charset")
					.setContentLength(10L)
					.setInstanceId("instanceId")
					.setRemoteId("remoteId")
					.setRemoteSourceName("remoteSource")
					.setView(true)
					.setMimeType("mimeType")
					.setPurpose("purpose")
					.setName("name");
		entity.merge(contentImport);

		assertEquals("charset", entity.getCharset());
		assertEquals("instanceId", entity.getInstanceId());
		assertEquals("remoteId", entity.getRemoteId());
		assertEquals("remoteSource", entity.getRemoteSourceName());
		assertEquals("mimeType", entity.getMimeType());
		assertEquals("purpose", entity.getPurpose());
		assertEquals("name", entity.getName());
		assertEquals(Long.valueOf(10L), entity.getContentSize());
	}

	@Test
	public void mergeFrom_doOverride() throws Exception {

		ContentEntity entity = new ContentEntity();
		entity.setCharset("utf-8");
		entity.setContentSize(5L);
		entity.setInstanceId("emf:instance");
		entity.setMimeType("mime");
		entity.setName("aName");
		entity.setRemoteId("remote");
		entity.setRemoteSourceName("remoteName");
		entity.setPurpose("aPurpose");

		ContentImport contentImport = ContentImport
				.createEmpty()
					.setCharset("charset")
					.setContentLength(10L)
					.setInstanceId("instanceId")
					.setRemoteId("newRemoteId")
					.setRemoteSourceName("newRemoteSource")
					.setView(true)
					.setMimeType("mimeType")
					.setPurpose("purpose")
					.setName("name");
		entity.merge(contentImport);

		assertEquals("charset", entity.getCharset());
		assertEquals("instanceId", entity.getInstanceId());
		assertEquals("newRemoteId", entity.getRemoteId());
		assertEquals("newRemoteSource", entity.getRemoteSourceName());
		assertEquals("mimeType", entity.getMimeType());
		assertEquals("purpose", entity.getPurpose());
		assertEquals("name", entity.getName());
		assertEquals(Long.valueOf(10L), entity.getContentSize());
	}

	@Test
	public void mergeFrom_doNotOverride() throws Exception {

		// the missing data not passed by the ContentImport object should not remove the one already present in the
		// entity

		ContentEntity entity = new ContentEntity();
		entity.setCharset("utf-8");
		entity.setContentSize(5L);
		entity.setInstanceId("emf:instance");
		entity.setMimeType("mime");
		entity.setName("aName");
		entity.setRemoteId("remote");
		entity.setRemoteSourceName("remoteName");
		entity.setPurpose("aPurpose");

		ContentImport contentImport = ContentImport
				.createEmpty()
					.setContentLength(10L)
					.setView(true)
					.setMimeType("mimeType")
					.setName("name");
		entity.merge(contentImport);

		assertEquals("utf-8", entity.getCharset());
		assertEquals("emf:instance", entity.getInstanceId());
		assertEquals("remote", entity.getRemoteId());
		assertEquals("remoteName", entity.getRemoteSourceName());
		assertEquals("mimeType", entity.getMimeType());
		assertEquals("aPurpose", entity.getPurpose());
		assertEquals("name", entity.getName());
		assertEquals(Long.valueOf(10L), entity.getContentSize());
	}

	@Test
	public void mergeFromForImport() throws Exception {
		ContentEntity entity = new ContentEntity();
		Content contentImport = Content
				.createEmpty()
					.setCharset("charset")
					.setContentLength(10L)
					.setView(true)
					.setMimeType("mimeType")
					.setPurpose("purpose")
					.setName("name");
		entity.merge(contentImport);

		assertEquals("charset", entity.getCharset());
		assertEquals("mimeType", entity.getMimeType());
		assertEquals("purpose", entity.getPurpose());
		assertEquals("name", entity.getName());
		assertEquals(Long.valueOf(10L), entity.getContentSize());
	}

	@Test
	public void mergeFrom_noName() throws Exception {
		FileDescriptor descriptor = mock(FileDescriptor.class);
		when(descriptor.getId()).thenReturn("id");
		ContentEntity entity = new ContentEntity();
		ContentImport contentImport = ContentImport
				.createEmpty()
					.setCharset("charset")
					.setContentLength(10L)
					.setInstanceId("instanceId")
					.setRemoteId("remoteId")
					.setRemoteSourceName("remoteSource")
					.setView(true)
					.setMimeType("mimeType")
					.setPurpose("purpose")
					.setContent(descriptor);
		entity.merge(contentImport);

		assertEquals("charset", entity.getCharset());
		assertEquals("instanceId", entity.getInstanceId());
		assertEquals("remoteId", entity.getRemoteId());
		assertEquals("remoteSource", entity.getRemoteSourceName());
		assertEquals("mimeType", entity.getMimeType());
		assertEquals("purpose", entity.getPurpose());
		assertEquals("id", entity.getName());
		assertEquals(Long.valueOf(10L), entity.getContentSize());
	}

	@Test
	public void mergeFrom_noContentLenght() throws Exception {
		ContentEntity entity = new ContentEntity();
		ContentImport contentImport = ContentImport
				.createEmpty()
					.setCharset("charset")
					.setInstanceId("instanceId")
					.setRemoteId("remoteId")
					.setRemoteSourceName("remoteSource")
					.setView(true)
					.setMimeType("mimeType")
					.setPurpose("purpose")
					.setName("name");
		entity.merge(contentImport);

		assertEquals("charset", entity.getCharset());
		assertEquals("instanceId", entity.getInstanceId());
		assertEquals("remoteId", entity.getRemoteId());
		assertEquals("remoteSource", entity.getRemoteSourceName());
		assertEquals("mimeType", entity.getMimeType());
		assertEquals("purpose", entity.getPurpose());
		assertEquals("name", entity.getName());
		assertEquals(Long.valueOf(-1L), entity.getContentSize());
	}

	@Test
	public void toStoreItemInfo() throws Exception {
		ContentEntity entity = new ContentEntity();
		entity.setId("1");
		entity.setMimeType("mimetype");
		entity.setRemoteSourceName("remoteSource");
		entity.setRemoteId("remoteId");
		entity.setName("name");
		entity.setPurpose("contentType");
		entity.setContentSize(1L);
		entity.setAdditionalInfo(new SerializableValue(new HashMap<>()));

		StoreItemInfo storeInfo = entity.toStoreInfo();
		assertNotNull(storeInfo);
		assertEquals("remoteSource", storeInfo.getProviderType());
		assertEquals("remoteId", storeInfo.getRemoteId());
		assertEquals(1L, storeInfo.getContentLength());
		assertNotNull(storeInfo.getAdditionalData());
		assertTrue(storeInfo.getAdditionalData() instanceof Map);
	}

	@Test
	public void isNew() throws Exception {
		ContentEntity entity = new ContentEntity();
		assertTrue(entity.isNew());
		entity.setId("1");

		assertTrue(entity.isNew());

		entity.setRemoteId("remoteId");

		assertFalse(entity.isNew());
	}
}
