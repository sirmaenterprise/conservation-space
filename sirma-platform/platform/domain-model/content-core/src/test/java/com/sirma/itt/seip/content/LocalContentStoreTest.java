package com.sirma.itt.seip.content;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;

import org.apache.commons.io.FileUtils;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.MockitoAnnotations;
import org.mockito.Spy;

import com.sirma.itt.seip.io.FileDescriptor;
import com.sirma.itt.seip.testutil.mocks.ConfigurationPropertyMock;

/**
 * Tests for {@link LocalContentStore}.
 *
 * @author BBonev
 */
public class LocalContentStoreTest {

	@InjectMocks
	LocalContentStore contentStore;

	File location = new File("contentStore");

	@Spy
	ConfigurationPropertyMock<File> storeLocation = new ConfigurationPropertyMock<>();

	@Before
	public void beforeMethod() {
		MockitoAnnotations.initMocks(this);
		contentStore.init();
		storeLocation.setValue(location);
	}

	@Test
	public void addToStore() throws Exception {
		Content descriptor = Content.createEmpty().setName("test.txt").setContent("test",
				StandardCharsets.UTF_8.name());

		StoreItemInfo info = contentStore.add(null, descriptor);
		assertNotNull(info);
		assertNotNull(info.getRemoteId());
		assertFalse("The path should not contain the store folder name", info.getRemoteId().contains("contentStore"));
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

	@Test
	public void invalidData() throws Exception {
		assertNull(contentStore.add(null, null));
		assertNull(contentStore.update(null, null, null));
		assertNull(contentStore.update(null, mock(Content.class), null));
		contentStore.delete(null);
		assertNull(contentStore.getReadChannel(null));

		assertNull(contentStore.update(null, mock(Content.class), new StoreItemInfo()));
		contentStore.delete(new StoreItemInfo());
		assertNull(contentStore.getReadChannel(new StoreItemInfo()));

		assertNull(contentStore.getReadChannel(new StoreItemInfo().setRemoteId("INVALID PATH")));

	}

	@After
	public void clean() throws IOException {
		FileUtils.deleteDirectory(location);
	}
}
