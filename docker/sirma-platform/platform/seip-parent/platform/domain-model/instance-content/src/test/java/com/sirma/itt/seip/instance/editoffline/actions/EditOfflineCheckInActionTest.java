package com.sirma.itt.seip.instance.editoffline.actions;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Optional;

import javax.ws.rs.BadRequestException;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.runners.MockitoJUnitRunner;

import com.sirma.itt.seip.domain.definition.label.LabelProvider;
import com.sirma.itt.seip.domain.instance.DefaultProperties;
import com.sirma.itt.seip.domain.instance.Instance;
import com.sirma.itt.seip.domain.instance.InstanceReference;
import com.sirma.itt.seip.exceptions.InstanceNotFoundException;
import com.sirma.itt.seip.instance.InstanceTypeResolver;
import com.sirma.itt.seip.instance.content.CheckOutCheckInService;
import com.sirma.itt.seip.instance.lock.LockInfo;
import com.sirma.itt.seip.instance.lock.LockService;
import com.sirma.itt.seip.instance.lock.exception.LockException;
import com.sirma.itt.seip.permissions.InstanceAccessEvaluator;
import com.sirma.itt.seip.security.exception.NoPermissionsException;
import com.sirma.sep.content.upload.UploadRequest;

/**
 * Test for {@link EditOfflineCheckInAction}.
 *
 * @author Boyan Tonchev.
 */
@RunWith(MockitoJUnitRunner.class)
public class EditOfflineCheckInActionTest {

	@Mock
	private InstanceAccessEvaluator accessEvaluator;

	@Mock
	private InstanceTypeResolver instanceTypeResolver;

	@Mock
	private LabelProvider labelProvider;

	@Mock
	private LockService lockService;

	@Mock
	private CheckOutCheckInService checkOutCheckInService;

	@InjectMocks
	private EditOfflineCheckInAction editOfflineCheckInAction;

	@Test
	public void should_CheckInInstance() {
		String instanceId = "methodPerformShouldPass";
		String version = "1.2";
		InstanceReference instanceReference = mock(InstanceReference.class);
		Instance instance = mock(Instance.class);
		when(instance.getAsString(DefaultProperties.VERSION)).thenReturn(version);
		when(instanceReference.toInstance()).thenReturn(instance);
		when(accessEvaluator.canWrite(instanceId)).thenReturn(Boolean.TRUE);
		UploadRequest request = mock(UploadRequest.class);
		when(request.getTargetId()).thenReturn(instanceId);
		when(request.getInstanceVersion()).thenReturn(version);
		when(instanceTypeResolver.resolveReference(instanceId)).thenReturn(Optional.of(instanceReference));
		LockInfo lockStatus = mock(LockInfo.class);
		when(lockService.lockStatus(instanceReference)).thenReturn(lockStatus);
		when(lockStatus.isLocked()).thenReturn(Boolean.TRUE);
		when(lockStatus.isLockedByMe()).thenReturn(Boolean.TRUE);

		editOfflineCheckInAction.perform(request);

		verify(checkOutCheckInService).checkIn(request, instanceId);
	}

	@Test
	public void validate_shouldNotThrowAnyExceptions() {
		String instanceId = "validateMethodShouldReturnTrue";
		String version = "1.2";
		InstanceReference instanceReference = mock(InstanceReference.class);
		Instance instance = mock(Instance.class);
		when(instance.getAsString(DefaultProperties.VERSION)).thenReturn(version);
		when(instanceReference.toInstance()).thenReturn(instance);
		when(accessEvaluator.canWrite(instanceId)).thenReturn(Boolean.TRUE);
		UploadRequest request = mock(UploadRequest.class);
		when(request.getTargetId()).thenReturn(instanceId);
		when(request.getInstanceVersion()).thenReturn(version);
		when(instanceTypeResolver.resolveReference(instanceId)).thenReturn(Optional.of(instanceReference));
		LockInfo lockStatus = mock(LockInfo.class);
		when(lockService.lockStatus(instanceReference)).thenReturn(lockStatus);
		when(lockStatus.isLocked()).thenReturn(Boolean.TRUE);
		when(lockStatus.isLockedByMe()).thenReturn(Boolean.TRUE);

		editOfflineCheckInAction.validate(request);
	}

	@Test(expected = BadRequestException.class)
	public void should_ThrowBadRequestException() {
		String instanceId = "shouldThrowBadRequestException";
		String version = "1.2";
		InstanceReference instanceReference = mock(InstanceReference.class);
		Instance instance = mock(Instance.class);
		when(instance.getAsString(DefaultProperties.VERSION)).thenReturn(version);
		when(instanceReference.toInstance()).thenReturn(instance);
		when(accessEvaluator.canWrite(instanceId)).thenReturn(Boolean.TRUE);
		UploadRequest request = mock(UploadRequest.class);
		when(request.getTargetId()).thenReturn(instanceId);
		when(instanceTypeResolver.resolveReference(instanceId)).thenReturn(Optional.of(instanceReference));
		LockInfo lockStatus = mock(LockInfo.class);
		when(lockService.lockStatus(instanceReference)).thenReturn(lockStatus);
		when(lockStatus.isLocked()).thenReturn(Boolean.TRUE);
		when(lockStatus.isLockedByMe()).thenReturn(Boolean.TRUE);

		editOfflineCheckInAction.validate(request);
	}

	@Test(expected = LockException.class)
	public void should_ThrowLockException_When_InstanceIsNotLockedByMe() {
		String instanceId = "shouldThrowBadRequestException";
		InstanceReference instanceReference = mock(InstanceReference.class);
		when(accessEvaluator.canWrite(instanceId)).thenReturn(Boolean.TRUE);
		UploadRequest request = mock(UploadRequest.class);
		when(request.getTargetId()).thenReturn(instanceId);
		when(instanceTypeResolver.resolveReference(instanceId)).thenReturn(Optional.of(instanceReference));
		LockInfo lockStatus = mock(LockInfo.class);
		when(lockService.lockStatus(instanceReference)).thenReturn(lockStatus);
		when(lockStatus.isLocked()).thenReturn(Boolean.TRUE);
		when(lockStatus.isLockedByMe()).thenReturn(Boolean.FALSE);

		editOfflineCheckInAction.validate(request);
	}

	@Test(expected = LockException.class)
	public void should_ThrowLockException_When_InstanceIsLocked() {
		String instanceId = "shouldThrowLockException";
		InstanceReference instanceReference = mock(InstanceReference.class);
		Mockito.when(accessEvaluator.canWrite(instanceId)).thenReturn(Boolean.TRUE);
		UploadRequest request = mock(UploadRequest.class);
		when(request.getTargetId()).thenReturn(instanceId);
		when(instanceTypeResolver.resolveReference(instanceId)).thenReturn(Optional.of(instanceReference));
		LockInfo lockStatus = mock(LockInfo.class);
		when(lockService.lockStatus(instanceReference)).thenReturn(lockStatus);
		when(lockStatus.isLocked()).thenReturn(Boolean.FALSE);

		editOfflineCheckInAction.validate(request);
	}

	@Test(expected = InstanceNotFoundException.class)
	public void should_ThrowInstanceNotFoundException() {
		String instanceId = "shouldThrowInstanceNotFoundException";
		when(accessEvaluator.canWrite(instanceId)).thenReturn(Boolean.TRUE);
		UploadRequest request = mock(UploadRequest.class);
		when(request.getTargetId()).thenReturn(instanceId);
		when(instanceTypeResolver.resolveReference(instanceId)).thenReturn(Optional.empty());

		editOfflineCheckInAction.validate(request);

		verify(labelProvider).getValue("document.checkin.deleted.instance");
	}

	@Test(expected = NoPermissionsException.class)
	public void should_ThrowNoPermissionsException() {
		String instanceId = "shouldThrowNoPermissionsException";
		when(accessEvaluator.canWrite(instanceId)).thenReturn(Boolean.FALSE);
		UploadRequest request = mock(UploadRequest.class);
		when(request.getTargetId()).thenReturn(instanceId);

		editOfflineCheckInAction.validate(request);
	}

	@Test
	public void should_ReturnCorrectName() {
		assertEquals(UploadRequest.UPLOAD, editOfflineCheckInAction.getName());
	}
}