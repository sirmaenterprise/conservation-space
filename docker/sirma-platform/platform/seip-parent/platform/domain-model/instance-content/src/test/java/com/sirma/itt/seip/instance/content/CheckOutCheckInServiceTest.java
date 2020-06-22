package com.sirma.itt.seip.instance.content;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.verify;

import java.io.Serializable;
import java.util.Arrays;
import java.util.Collections;
import java.util.Optional;

import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Matchers;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

import com.sirma.itt.seip.domain.definition.label.LabelProvider;
import com.sirma.itt.seip.domain.instance.DefaultProperties;
import com.sirma.itt.seip.domain.instance.Instance;
import com.sirma.itt.seip.domain.instance.InstanceReference;
import com.sirma.itt.seip.domain.rest.EmfApplicationException;
import com.sirma.itt.seip.event.EventService;
import com.sirma.itt.seip.exceptions.InstanceNotFoundException;
import com.sirma.itt.seip.instance.DomainInstanceService;
import com.sirma.itt.seip.instance.InstanceSaveContext;
import com.sirma.itt.seip.instance.InstanceTypeResolver;
import com.sirma.itt.seip.instance.content.event.CheckOutEvent;
import com.sirma.itt.seip.instance.editoffline.exception.FileNotSupportedException;
import com.sirma.itt.seip.instance.editoffline.updaters.CustomPropertyUpdater;
import com.sirma.itt.seip.instance.lock.LockInfo;
import com.sirma.itt.seip.instance.lock.LockService;
import com.sirma.itt.seip.permissions.action.AuthorityService;
import com.sirma.itt.seip.plugin.Plugins;
import com.sirma.sep.content.Content;
import com.sirma.sep.content.ContentInfo;
import com.sirma.sep.content.InstanceContentService;
import com.sirma.sep.content.upload.ContentUploader;
import com.sirma.sep.content.upload.UploadRequest;

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

	@Mock
	private Plugins<CustomPropertyUpdater> customPropertyUpdaters;

	/**
	 * Initializes objects annotated with Mockito annotations.
	 */
	@Before
	public void init() {
		MockitoAnnotations.initMocks(this);
	}

	@Test
	public void checkInMethodShouldPass() {
		String contentId = "checkInMethodShouldPass";
		String newContentId = "newContentId:checkInMethodShouldPass";
		String instanceId = "emf:instanceId";
		ContentInfo info = Mockito.mock(ContentInfo.class);
		Mockito.when(info.exists()).thenReturn(Boolean.TRUE);
		Mockito.when(info.getInstanceId()).thenReturn(contentId);
		Mockito.when(instanceContentService.getContent(contentId, Content.PRIMARY_CONTENT)).thenReturn(info);
		Instance instance = Mockito.mock(Instance.class);
		InstanceReference instanceReference = Mockito.mock(InstanceReference.class);
		Mockito.when(instanceReference.toInstance()).thenReturn(instance);
		Mockito.when(instanceReference.getId()).thenReturn(instanceId);
		Mockito.when(instanceService.loadInstance(instanceId)).thenReturn(instance);
		Mockito.when(instanceTypeResolver.resolveReference(contentId)).thenReturn(Optional.of(instanceReference));
		LockInfo lockInfo = Mockito.mock(LockInfo.class);
		Mockito.when(lockService.lockStatus(instanceReference)).thenReturn(lockInfo);
		Mockito.when(lockInfo.isLocked()).thenReturn(Boolean.TRUE);
		UploadRequest uploadRequest = Mockito.mock(UploadRequest.class);
		ContentInfo updatedContentInfo = Mockito.mock(ContentInfo.class);
		Mockito.when(updatedContentInfo.getContentId()).thenReturn(newContentId);
		Mockito.when(contentUploader.uploadForInstance(uploadRequest, contentId,
													   Content.PRIMARY_CONTENT, Boolean.TRUE)).thenReturn(updatedContentInfo);

		checkOutCheckInService.checkIn(uploadRequest, contentId);

		Mockito.verify(lockService).unlock(instanceReference);
		Mockito.verify(instance).remove(DefaultProperties.LOCKED_BY);
		Mockito.verify(instance).add(DefaultProperties.PRIMARY_CONTENT_ID, newContentId);
		Mockito.verify(instanceService).save(Matchers.any(InstanceSaveContext.class));
	}

	@Test(expected = InstanceNotFoundException.class)
	public void shouldTrowInstanceNotFoundExceptionIfInstanceDidontLoad() {
		String contentId = "voidshouldTrowInstanceNotFoundExceptionIfInstanceDidontLoad";
		ContentInfo info = Mockito.mock(ContentInfo.class);
		Mockito.when(info.exists()).thenReturn(Boolean.TRUE);
		Mockito.when(info.getInstanceId()).thenReturn(contentId);
		Mockito.when(instanceContentService.getContent(contentId, Content.PRIMARY_CONTENT)).thenReturn(info);
		Mockito.when(instanceTypeResolver.resolveReference(contentId)).thenReturn(Optional.empty());
		UploadRequest uploadRequest = Mockito.mock(UploadRequest.class);
		checkOutCheckInService.checkIn(uploadRequest, contentId);
	}

	@Test(expected = InstanceNotFoundException.class)
	public void shouldTrowInstanceNotFoundExceptionIfInstaneNotExist() {
		String contentId = "shouldTrowInstanceNotFoundExceptionIfInstaneNotExist";
		ContentInfo info = Mockito.mock(ContentInfo.class);
		Mockito.when(info.exists()).thenReturn(Boolean.FALSE);
		Mockito.when(instanceContentService.getContent(contentId, Content.PRIMARY_CONTENT)).thenReturn(info);
		UploadRequest uploadRequest = Mockito.mock(UploadRequest.class);
		checkOutCheckInService.checkIn(uploadRequest, contentId);
	}

	@Test
	public void checkOutMethodShouldPass() {
		String contentId = "checkOutMethodShouldPass";
		String mimeType = "mimetype";
		ContentInfo info = Mockito.mock(ContentInfo.class);
		Mockito.when(info.getMimeType()).thenReturn(mimeType);
		Mockito.when(info.exists()).thenReturn(Boolean.TRUE);
		Mockito.when(info.getInstanceId()).thenReturn(contentId);
		Mockito.when(instanceContentService.getContent(contentId, Content.PRIMARY_CONTENT)).thenReturn(info);
		Instance instance = Mockito.mock(Instance.class);
		InstanceReference instanceReference = Mockito.mock(InstanceReference.class);
		Mockito.when(instanceReference.toInstance()).thenReturn(instance);
		Mockito.when(instanceTypeResolver.resolveReference(contentId)).thenReturn(Optional.of(instanceReference));
		CustomPropertyUpdater unSupportedCustomPropertyUpdater = Mockito.mock(CustomPropertyUpdater.class);
		Mockito.when(unSupportedCustomPropertyUpdater.canUpdate(mimeType)).thenReturn(Boolean.FALSE);
		CustomPropertyUpdater supportedCustomPropertyUpdater = Mockito.mock(CustomPropertyUpdater.class);
		Mockito.when(supportedCustomPropertyUpdater.canUpdate(mimeType)).thenReturn(Boolean.TRUE);
		Mockito.when(customPropertyUpdaters.iterator()).thenReturn(Arrays.asList(unSupportedCustomPropertyUpdater, supportedCustomPropertyUpdater).iterator());

		checkOutCheckInService.checkOut(contentId);

		Mockito.verify(eventService).fire(Matchers.any(CheckOutEvent.class));
	}

	@Test(expected = FileNotSupportedException.class)
	public void shouldTrowFileNotSupportedExceptionIfMimeTypeNotSupported() {
		String contentId = "shouldTrowFileNotSupportedExceptionIfMimeTypeNotSupported";
		ContentInfo info = Mockito.mock(ContentInfo.class);
		Mockito.when(info.exists()).thenReturn(Boolean.TRUE);
		Mockito.when(info.getInstanceId()).thenReturn(contentId);
		Mockito.when(instanceContentService.getContent(contentId, Content.PRIMARY_CONTENT)).thenReturn(info);
		Instance instance = Mockito.mock(Instance.class);
		InstanceReference instanceReference = Mockito.mock(InstanceReference.class);
		Mockito.when(instanceReference.toInstance()).thenReturn(instance);
		Mockito.when(instanceTypeResolver.resolveReference(contentId)).thenReturn(Optional.of(instanceReference));
		Mockito.when(customPropertyUpdaters.iterator()).thenReturn(Collections.emptyIterator());

		checkOutCheckInService.checkOut(contentId);
	}

	@Test(expected = EmfApplicationException.class)
	public void shouldTrowEmfApplicationExceptionIfInstanceNotExist() {
		String contentId = "checkOutMethodShouldPass";
		ContentInfo info = Mockito.mock(ContentInfo.class);
		Mockito.when(info.exists()).thenReturn(Boolean.TRUE);
		Mockito.when(info.getInstanceId()).thenReturn(contentId);
		Mockito.when(instanceContentService.getContent(contentId, Content.PRIMARY_CONTENT)).thenReturn(info);
		Mockito.when(instanceTypeResolver.resolveReference(contentId)).thenReturn(Optional.empty());

		checkOutCheckInService.checkOut(contentId);
	}

	@Test(expected = FileNotSupportedException.class)
	public void shouldTrowFileNotSupportedExceptionIfContentInfodidnotContainsInstanceId() {
		String contentId = "shouldTrowFileNotSupportedExceptionIfContentInfodidnotContainsInstanceId";
		ContentInfo info = Mockito.mock(ContentInfo.class);
		Mockito.when(info.exists()).thenReturn(Boolean.TRUE);
		Mockito.when(instanceContentService.getContent(contentId, Content.PRIMARY_CONTENT)).thenReturn(info);

		checkOutCheckInService.checkOut(contentId);
	}

	@Test(expected = FileNotSupportedException.class)
	public void shouldTrowFileNotSupportedExceptionIfContentNotExist() {
		String contentId = "shouldTrowFileNotSupportedExceptionIfContentNotExist";
		ContentInfo info = Mockito.mock(ContentInfo.class);
		Mockito.when(info.exists()).thenReturn(Boolean.FALSE);
		Mockito.when(instanceContentService.getContent(contentId, Content.PRIMARY_CONTENT)).thenReturn(info);

		checkOutCheckInService.checkOut(contentId);
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
