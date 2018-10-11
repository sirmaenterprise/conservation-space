/**
 *
 */
package com.sirma.sep.content;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;

import org.apache.commons.io.FileUtils;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import com.sirma.itt.seip.io.FileDescriptor;
import com.sirma.itt.seip.io.TempFileProvider;
import com.sirma.sep.content.Content;
import com.sirma.sep.content.StoreItemInfo;
import com.sirma.sep.content.TempContentStore;

/**
 * Tests for {@link TempContentStore}.
 *
 * @author BBonev
 */
public class TempContentStoreTest {

	@InjectMocks
	TempContentStore contentStore;

	private File location = new File("tempStore");

	@Mock
	private TempFileProvider tempFileProvider;

	@Before
	public void beforeMethod() {
		MockitoAnnotations.initMocks(this);
		location.mkdirs();
		when(tempFileProvider.createTempFile(anyString(), eq(null))).then(a -> {
			return File.createTempFile(a.getArgumentAt(0, String.class), a.getArgumentAt(1, String.class), location);
		});
		doAnswer(a -> {
			FileUtils.forceDelete(a.getArgumentAt(0, File.class));
			return null;
		}).when(tempFileProvider).deleteFile(any(File.class));

		when(tempFileProvider.getTempDir()).thenReturn(location);
	}

	@Test
	public void addToStore() throws Exception {
		Content descriptor = Content.createEmpty().setName("test.txt").setContent("test",
				StandardCharsets.UTF_8.name());

		StoreItemInfo info = contentStore.add(null, descriptor);
		assertNotNull(info);
		assertNotNull(info.getRemoteId());
		assertEquals(4L, info.getContentLength());
	}

	@Test
	public void addAndReadFromStore() throws Exception {
		Content descriptor = Content.createEmpty().setName("test.txt").setContent("test",
				StandardCharsets.UTF_8.name());

		StoreItemInfo info = contentStore.add(null, descriptor);
		assertNotNull(info);

		FileDescriptor channel = contentStore.getReadChannel(info);
		assertNotNull(channel);
		assertEquals("test", channel.asString());
	}

	@Test
	public void addAndDelete() throws Exception {
		Content descriptor = Content.createEmpty().setName("test.txt").setContent("test",
				StandardCharsets.UTF_8.name());

		StoreItemInfo info = contentStore.add(null, descriptor);
		assertNotNull(info);

		FileDescriptor channel = contentStore.getReadChannel(info);
		assertNotNull(channel);
		assertEquals("test", channel.asString());

		assertTrue(contentStore.delete(info));

		FileDescriptor fileDescriptor = contentStore.getReadChannel(info);
		assertNull(fileDescriptor);
	}

	@Test
	public void addAndUpdateContent() throws Exception {
		Content descriptor = Content.createEmpty().setName("test.txt").setContent("test",
				StandardCharsets.UTF_8.name());

		StoreItemInfo info = contentStore.add(null, descriptor);
		assertNotNull(info);

		FileDescriptor channel = contentStore.getReadChannel(info);
		assertNotNull(channel);
		assertEquals("test", channel.asString());

		Content newContent = Content.createEmpty().setName("test.txt").setContent("updated",
				StandardCharsets.UTF_8.name());

		StoreItemInfo updatedInfo = contentStore.update(null, newContent, info);
		assertNotNull(updatedInfo);
		assertEquals(info.getRemoteId(), updatedInfo.getRemoteId());
		assertEquals(7L, updatedInfo.getContentLength());

		FileDescriptor updatedChannel = contentStore.getReadChannel(updatedInfo);
		assertNotNull(updatedChannel);
		assertEquals("updated", updatedChannel.asString());
	}


	@Test(expected = ContentStoreMissMatchException.class)
	public void update_ShouldFailForNullPreviousInfo() {
		contentStore.update(null, null, null);
	}

	@Test(expected = ContentStoreMissMatchException.class)
	public void update_ShouldFailForMissMatchPreviousInfo() {
		contentStore.update(null, null, new StoreItemInfo().setProviderType("random"));
	}

	@Test(expected = ContentStoreMissMatchException.class)
	public void delete_ShouldFailForNullPreviousInfo() {
		contentStore.delete((StoreItemInfo) null);
	}

	@Test(expected = ContentStoreMissMatchException.class)
	public void delete_ShouldFailForMissMatchPreviousInfo() {
		contentStore.delete(new StoreItemInfo().setProviderType("random"));
	}

	@Test(expected = ContentStoreMissMatchException.class)
	public void getReadChannel_ShouldFailForNullPreviousInfo() {
		contentStore.getReadChannel(null);
	}

	@Test(expected = ContentStoreMissMatchException.class)
	public void getReadChannel_ShouldFailForMissMatchPreviousInfo() {
		contentStore.getReadChannel(new StoreItemInfo().setProviderType("random"));
	}

	@Test(expected = ContentStoreMissMatchException.class)
	public void prepareForDelete_ShouldFailForNullPreviousInfo() {
		contentStore.prepareForDelete(null);
	}

	@Test(expected = ContentStoreMissMatchException.class)
	public void prepareForDelete_ShouldFailForMissMatchPreviousInfo() {
		contentStore.prepareForDelete(new StoreItemInfo().setProviderType("random"));
	}

	@Test(expected = ContentStoreMissMatchException.class)
	public void deletePreparedData_ShouldFailForNullPreviousInfo() {
		contentStore.delete((DeleteContentData) null);
	}

	@Test(expected = ContentStoreMissMatchException.class)
	public void deletePreparedData_ShouldFailForMissMatchPreviousInfo() {
		contentStore.delete(new DeleteContentData().setStoreName("random"));
	}

	@After
	public void clean() throws IOException {
		FileUtils.deleteDirectory(location);
	}
}
