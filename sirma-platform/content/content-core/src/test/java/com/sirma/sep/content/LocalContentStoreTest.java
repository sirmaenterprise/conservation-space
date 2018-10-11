package com.sirma.sep.content;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.when;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Optional;


import org.apache.commons.io.FileUtils;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.Spy;
import org.testng.reporters.Files;

import com.sirma.itt.seip.concurrent.TaskExecutor;
import com.sirma.itt.seip.concurrent.locks.ContextualLock;
import com.sirma.itt.seip.io.FileDescriptor;
import com.sirma.itt.seip.security.context.ContextualExecutor;
import com.sirma.itt.seip.security.context.SecurityContextManager;
import com.sirma.itt.seip.testutil.fakes.TaskExecutorFake;
import com.sirma.itt.seip.testutil.mocks.ConfigurationPropertyMock;

/**
 * Tests for {@link LocalContentStore}.
 *
 * @author BBonev
 */
public class LocalContentStoreTest {

	@InjectMocks
	private LocalContentStore contentStore;

	private File location = new File("contentStore");

	@Spy
	private ConfigurationPropertyMock<File> storeLocation = new ConfigurationPropertyMock<>();
	@Spy
	private ConfigurationPropertyMock<File> tenantStoreRootLocation = new ConfigurationPropertyMock<>();
	@Mock
	private SecurityContextManager contextManager;
	@Spy
	private ContextualLock contextualLock = ContextualLock.create();

	@Before
	public void beforeMethod() {
		MockitoAnnotations.initMocks(this);
		contentStore.init();
		storeLocation.setValue(location);
		tenantStoreRootLocation.setValue(new File(location, "tenant.id"));
	}

	@Test
	public void addToStore() {
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

	@Test(expected = ContentNotFoundRuntimeException.class)
	public void aupdateContent_shouldFailOnMissingContent() throws Exception {
		Content newContent = Content.createEmpty().setName("test.txt").setContent("updated",
				StandardCharsets.UTF_8.name());

		contentStore.update(null, newContent, contentStore.createStoreInfo().setRemoteId("someRandomId"));
	}

	@Test
	public void invalidData() {
		assertNull(contentStore.add(null, null));
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

	@Test
	public void getReadChannel_ShouldNotFailForNullRemoteid() {
		assertNull(contentStore.getReadChannel(contentStore.createStoreInfo()));
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

	@Test
	public void twoPhaseDelete_shouldWork() {
		Content descriptor = Content.createEmpty().setName("test.txt").setContent("test",
				StandardCharsets.UTF_8.name());

		StoreItemInfo info = contentStore.add(null, descriptor);
		assertNotNull(info);

		Optional<DeleteContentData> deleteContentData = contentStore.prepareForDelete(info);
		assertTrue(deleteContentData.isPresent());

		contentStore.delete(deleteContentData.get());

		assertNull(contentStore.getReadChannel(info));
	}

	@Test
	public void twoPhaseDelete_shouldHandleStoreMove() {
		ContextualExecutor contextualExecutor = new ContextualExecutor.NoContextualExecutor();
		when(contextManager.executeAsTenant(any(String.class))).thenReturn(contextualExecutor);

		Content descriptor = Content.createEmpty().setName("test.txt").setContent("test",
				StandardCharsets.UTF_8.name());

		StoreItemInfo info = contentStore.add(null, descriptor);
		assertNotNull(info);

		Optional<DeleteContentData> deleteContentData = contentStore.prepareForDelete(info);
		assertTrue(deleteContentData.isPresent());

		DeleteContentData contentData = deleteContentData.get();
		String currentLocation = contentData.getString("location");
		currentLocation = currentLocation.replaceAll(location.getName(), "oldContentStore");
		contentData.addProperty("location", currentLocation);

		contentStore.delete(contentData);

		assertNull(contentStore.getReadChannel(info));
	}

	@Test
	public void twoPhaseDelete_shouldDoNothingIfFileDoesNotExists() {
		Content descriptor = Content.createEmpty().setName("test.txt").setContent("test",
				StandardCharsets.UTF_8.name());

		StoreItemInfo info = contentStore.add(null, descriptor);
		assertNotNull(info);

		contentStore.delete(info);

		Optional<DeleteContentData> deleteContentData = contentStore.prepareForDelete(info);
		assertFalse(deleteContentData.isPresent());
	}

	@Test
	public void readShouldFindTheFileInBaseLocationAndMoveItToTenantLocation() throws IOException {
		Files.writeFile("test data", new File(location, "someStorePath/test.txt"));
		StoreItemInfo storeInfo = contentStore.createStoreInfo().setRemoteId("someStorePath/test.txt");

		FileDescriptor readChannel = contentStore.getReadChannel(storeInfo);
		assertNotNull("The file should have been moved synchronously", readChannel);

		assertEquals("test data", readChannel.asString());
		assertFalse("The file should be moved to the new location", new File(location, "someStorePath/test.txt").exists());
		assertTrue("The file should be moved to the new location", new File(tenantStoreRootLocation.get(), "someStorePath/test.txt").exists());
	}

	@After
	public void clean() throws IOException {
		FileUtils.deleteDirectory(location);
	}
}
