package com.sirma.itt.seip.instance.editoffline.actions;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.File;
import java.util.Optional;

import javax.servlet.http.HttpServletResponse;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import com.sirma.itt.seip.domain.definition.label.LabelProvider;
import com.sirma.itt.seip.domain.instance.InstanceReference;
import com.sirma.itt.seip.exceptions.InstanceNotFoundException;
import com.sirma.itt.seip.instance.InstanceTypeResolver;
import com.sirma.itt.seip.instance.content.CheckOutCheckInService;
import com.sirma.itt.seip.instance.lock.LockService;
import com.sirma.itt.seip.instance.lock.exception.LockException;
import com.sirma.itt.seip.permissions.InstanceAccessEvaluator;
import com.sirma.itt.seip.rest.Range;
import com.sirma.itt.seip.security.exception.NoPermissionsException;
import com.sirma.sep.content.rest.ContentDownloadService;

/**
 * Test for {@link EditOfflineCheckOutAction}.
 *
 * @author Boyan Tonchev.
 */
@RunWith(MockitoJUnitRunner.class)
public class EditOfflineCheckOutActionTest {

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

	@Mock
	private ContentDownloadService downloadService;

	@InjectMocks
	private EditOfflineCheckOutAction editOfflineCheckOutAction;

	@Test
	public void should_CheckOutInstance() {
		String instanceId = "methodPerformShouldPass";
		InstanceReference instanceReference = mock(InstanceReference.class);
		when(accessEvaluator.canWrite(instanceId)).thenReturn(Boolean.TRUE);
		EditOfflineCheckOutRequest request = mock(EditOfflineCheckOutRequest.class);
		when(request.getTargetId()).thenReturn(instanceId);
		Range range = mock(Range.class);
		when(request.getRange()).thenReturn(range);
		when(request.getForDownload()).thenReturn(Boolean.TRUE);
		HttpServletResponse response = mock(HttpServletResponse.class);
		when(request.getResponse()).thenReturn(response);

		when(instanceTypeResolver.resolveReference(instanceId)).thenReturn(Optional.of(instanceReference));
		when(lockService.isAllowedToModify(instanceReference)).thenReturn(Boolean.TRUE);
		File file = mock(File.class);
		when(checkOutCheckInService.checkOut(instanceId)).thenReturn(file);

		editOfflineCheckOutAction.perform(request);

		verify(downloadService).sendFile(file, range, Boolean.TRUE, response, null, null);

	}

	@Test
	public void validate_shouldNotThrowAnyExceptions() {
		String instanceId = "shouldValidateReturnTrue";
		InstanceReference instanceReference = mock(InstanceReference.class);
		when(accessEvaluator.canWrite(instanceId)).thenReturn(Boolean.TRUE);
		EditOfflineCheckOutRequest request = mock(EditOfflineCheckOutRequest.class);
		when(request.getTargetId()).thenReturn(instanceId);
		when(instanceTypeResolver.resolveReference(instanceId)).thenReturn(Optional.of(instanceReference));
		when(lockService.isAllowedToModify(instanceReference)).thenReturn(Boolean.TRUE);

		editOfflineCheckOutAction.validate(request);
	}

	@Test(expected = LockException.class)
	public void should_ThrowLockException() {
		String instanceId = "shouldThrowLockException";
		InstanceReference instanceReference = mock(InstanceReference.class);
		when(accessEvaluator.canWrite(instanceId)).thenReturn(Boolean.TRUE);
		EditOfflineCheckOutRequest request = mock(EditOfflineCheckOutRequest.class);
		when(request.getTargetId()).thenReturn(instanceId);
		when(instanceTypeResolver.resolveReference(instanceId)).thenReturn(Optional.of(instanceReference));
		when(lockService.isAllowedToModify(instanceReference)).thenReturn(Boolean.FALSE);

		editOfflineCheckOutAction.validate(request);
	}

	@Test(expected = InstanceNotFoundException.class)
	public void should_ThrowInstanceNotFoundException() {
		String instanceId = "shouldThrowInstanceNotFoundException";
		when(accessEvaluator.canWrite(instanceId)).thenReturn(Boolean.TRUE);
		EditOfflineCheckOutRequest request = mock(EditOfflineCheckOutRequest.class);
		when(request.getTargetId()).thenReturn(instanceId);
		when(instanceTypeResolver.resolveReference(instanceId)).thenReturn(Optional.empty());

		editOfflineCheckOutAction.validate(request);

		verify(labelProvider).getValue("document.checkin.deleted.instance");
	}

	@Test(expected = NoPermissionsException.class)
	public void should_ThrowNoPermissionsException() {
		String instanceId = "shouldThrowNoPermissionsException";
		when(accessEvaluator.canWrite(instanceId)).thenReturn(Boolean.FALSE);
		EditOfflineCheckOutRequest request = mock(EditOfflineCheckOutRequest.class);
		when(request.getTargetId()).thenReturn(instanceId);

		editOfflineCheckOutAction.validate(request);
	}

	@Test
	public void should_ReturnCorrectName() {
		assertEquals(EditOfflineCheckOutRequest.EDIT_OFFLINE_CHECK_OUT, editOfflineCheckOutAction.getName());
	}
}