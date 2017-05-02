package com.sirma.itt.seip.instance.content;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.verify;

import java.io.Serializable;
import java.util.Optional;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Matchers;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

import com.sirma.itt.seip.content.Content;
import com.sirma.itt.seip.content.ContentInfo;
import com.sirma.itt.seip.content.InstanceContentService;
import com.sirma.itt.seip.content.upload.ContentUploader;
import com.sirma.itt.seip.domain.definition.label.LabelProvider;
import com.sirma.itt.seip.domain.instance.Instance;
import com.sirma.itt.seip.domain.instance.InstanceReference;
import com.sirma.itt.seip.domain.rest.EmfApplicationException;
import com.sirma.itt.seip.event.EventService;
import com.sirma.itt.seip.instance.DomainInstanceService;
import com.sirma.itt.seip.instance.InstanceTypeResolver;
import com.sirma.itt.seip.instance.lock.LockInfo;
import com.sirma.itt.seip.instance.lock.LockService;
import com.sirma.itt.seip.permissions.action.AuthorityService;

/**
 * Unit tests for the {@link CheckOutCheckInServiceImpl}.
 *
 * @author nvelkov
 * @author Vilizar Tsonev
 */
public class CheckOutCheckInServiceTest {

	@Mock
	private InstanceContentService instanceContentService;

	@Mock
	private LockService lockService;

	@Mock
	private InstanceTypeResolver instanceTypeResolver;

	@Mock
	private EventService eventService;

	@Mock
	private AuthorityService authorityService;

	@Mock
	private ContentUploader contentUploader;

	@Mock
	private LabelProvider labelProvider;

	@InjectMocks
	private CheckOutCheckInService checkOutCheckInService = new CheckOutCheckInServiceImpl();

	@Mock
	private DomainInstanceService instanceService;

	/**
	 * Initializes objects annotated with Mockito annotations.
	 */
	@Before
	public void init() {
		MockitoAnnotations.initMocks(this);
	}

	/**
	 * Tests {@link CheckOutCheckInService#checkIn(Content, Serializable)} with valid input data and permissions.
	 * Verifies that the program logic involved in the check-in is invoked with the right parameters
	 */
	@Test
	public void testCheckInValidContent() {
		String contentId = "contentId";
		ContentInfo info = createContentInfo("instanceId", contentId, "mimeType", "word.doc", true);
		mockInstanceContentService(info);
		mockInstanceTypeResolver(false);
		mockAuthorityService(true);
		mockLockService(true);
		Content content = Mockito.mock(Content.class);
		checkOutCheckInService.checkIn(content, contentId);
		verify(contentUploader).updateContent(any(Instance.class), eq(content), eq(contentId));
		verify(instanceContentService).getContent(eq(contentId), eq(""));
	}

	/**
	 * Tests {@link CheckOutCheckInService#checkIn(Content, Serializable)} when the action is not allowed for the user.
	 * Verifies that {@link EmfApplicationException} is thrown.
	 */
	@Test(expected = EmfApplicationException.class)
	public void testCheckInNoPermission() {
		String contentId = "contentId";
		ContentInfo info = createContentInfo("instanceId", contentId, "mimeType", "word.doc", true);
		mockInstanceContentService(info);
		mockInstanceTypeResolver(false);
		mockLabelProvider();
		mockLockService(true);
		mockAuthorityService(false);
		Content content = Mockito.mock(Content.class);
		checkOutCheckInService.checkIn(content, contentId);
	}

	/**
	 * Tests {@link CheckOutCheckInService#checkIn(Content, Serializable)} when content with the requested ID does not
	 * exist in the RDB. Verifies that {@link EmfApplicationException} is thrown.
	 */
	@Test(expected = EmfApplicationException.class)
	public void testCheckInNonExistingContent() {
		String contentId = "contentId";
		ContentInfo info = createContentInfo("instanceId", contentId, "mimeType", "word.doc", false);
		mockInstanceContentService(info);
		mockInstanceTypeResolver(false);
		mockLabelProvider();
		mockAuthorityService(true);
		Content content = Mockito.mock(Content.class);
		checkOutCheckInService.checkIn(content, contentId);
	}

	/**
	 * Tests {@link CheckOutCheckInService#checkIn(Content, Serializable)} when the target instance has been deleted.
	 * Verifies that {@link EmfApplicationException} is thrown.
	 */
	@Test(expected = EmfApplicationException.class)
	public void testCheckInDeletedInstance() {

		String contentId = "contentId";
		ContentInfo info = createContentInfo("instanceId", contentId, "mimeType", "word.doc", true);
		mockInstanceContentService(info);
		mockInstanceTypeResolver(true);
		mockLabelProvider();
		mockAuthorityService(true);
		Content content = Mockito.mock(Content.class);
		checkOutCheckInService.checkIn(content, contentId);
	}

	/**
	 * Tests {@link CheckOutCheckInService#checkIn(Content, Serializable)} when the target instance is unlocked.
	 * Verifies that {@link EmfApplicationException} is thrown.
	 */
	@Test(expected = EmfApplicationException.class)
	public void testCheckInUnlockedInstance() {
		String contentId = "contentId";
		ContentInfo info = createContentInfo("instanceId", contentId, "mimeType", "word.doc", true);
		mockInstanceContentService(info);
		mockInstanceTypeResolver(false);
		mockAuthorityService(true);
		mockLabelProvider();
		mockLockService(false);
		Content content = Mockito.mock(Content.class);
		checkOutCheckInService.checkIn(content, contentId);
	}

	/**
	 * Test the checkout with a ms office document. The content id should be appended to the file name.
	 */
	@Test
	public void testCheckOut() {
		ContentInfo info = createContentInfo("instanceId", "contentId", "application/msword", "name.doc", true);
		mockInstanceContentService(info);
		mockInstanceTypeResolver(false);
		String downloadUrl = checkOutCheckInService.checkOut("something").get();
		Assert.assertEquals(downloadUrl, "/api/content/instanceId?download=true&fileName=name.contentId.doc");
	}

	/**
	 * Test the checkout with a null content (The content can't be found).
	 */
	@Test
	public void testCheckOutNullContent() {
		mockInstanceContentService(ContentInfo.DO_NOT_EXIST);
		mockInstanceTypeResolver(false);
		Optional<String> downloadUrl = checkOutCheckInService.checkOut("something");
		Assert.assertFalse(downloadUrl.isPresent());
	}

	/**
	 * Test the checkout with a file that is not a ms office document. The default value should be returned.
	 */
	@Test
	public void testCheckOutNotADoc() {
		ContentInfo info = createContentInfo("instanceId", "contentId", "application/txt", "name.txt", true);
		mockInstanceContentService(info);
		mockInstanceTypeResolver(false);
		String downloadUrl = checkOutCheckInService.checkOut("something").get();
		Assert.assertEquals(downloadUrl, "/api/content/instanceId?download=true");
	}

	private void mockAuthorityService(boolean actionAllowed) {
		Mockito
				.when(authorityService.isActionAllowed(Mockito.any(Instance.class), Mockito.anyString(), Mockito.any()))
					.thenReturn(actionAllowed);
	}

	private void mockInstanceContentService(ContentInfo contentInfo) {
		Mockito
				.when(instanceContentService.getContent(Matchers.any(Serializable.class), Matchers.anyString()))
					.thenReturn(contentInfo);
	}

	private void mockLabelProvider() {
		Mockito.when(labelProvider.getValue(Matchers.anyString())).thenReturn("someLabel");
	}

	/**
	 * Mocks the behaviour of the {@link LockService}.
	 *
	 * @param isInstanceLocked
	 *            indicates if the target instance has to be locked
	 */
	private void mockLockService(boolean isInstanceLocked) {
		LockInfo lockInfo = Mockito.mock(LockInfo.class);
		Mockito.when(lockInfo.isLocked()).thenReturn(isInstanceLocked);
		Mockito.when(lockService.lockStatus(Matchers.any(InstanceReference.class))).thenReturn(lockInfo);
	}

	/**
	 * Mocks the behaviour of the {@link InstanceTypeResolver}.
	 *
	 * @param isInstanceDeleted
	 *            indicates if the returned instance has to be null (deleted).
	 */
	private void mockInstanceTypeResolver(boolean isInstanceDeleted) {
		if (isInstanceDeleted) {
			Mockito.when(instanceTypeResolver.resolveReference(Matchers.any(Serializable.class))).thenReturn(
					Optional.empty());
			return;
		}
		InstanceReference instanceReference = Mockito.mock(InstanceReference.class);
		Instance instance = Mockito.mock(Instance.class);
		Mockito.when(instanceReference.toInstance()).thenReturn(instance);
		Mockito.when(instanceTypeResolver.resolveReference(Matchers.any(Serializable.class))).thenReturn(
				Optional.of(instanceReference));
	}

	private static ContentInfo createContentInfo(String instanceId, String contentId, String mimeType, String name,
			boolean exists) {
		ContentInfo contentInfo = Mockito.mock(ContentInfo.class);

		Mockito.when(contentInfo.getInstanceId()).thenReturn(instanceId);
		Mockito.when(contentInfo.getMimeType()).thenReturn(mimeType);
		Mockito.when(contentInfo.getContentId()).thenReturn(contentId);
		Mockito.when(contentInfo.getName()).thenReturn(name);
		Mockito.when(contentInfo.exists()).thenReturn(exists);

		return contentInfo;
	}
}
