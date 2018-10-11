package com.sirma.itt.seip.instance.editoffline.actions;

import com.sirma.itt.seip.instance.actions.Actions;
import com.sirma.itt.seip.instance.editoffline.exception.FileCustomPropertiesUpdateException;
import com.sirma.itt.seip.instance.editoffline.exception.FileNotSupportedException;
import com.sirma.sep.content.upload.UploadRequest;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Matchers;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.runners.MockitoJUnitRunner;

import javax.ws.rs.BadRequestException;

/**
 * Tests for EditOfflineRestService.
 *
 * @author Boyan Tonchev.
 */
@RunWith(MockitoJUnitRunner.class)
public class EditOfflineRestServiceTest {

	@Mock
	private Actions actions;

	@InjectMocks
	private EditOfflineRestService editOfflineRestService;

	@Test
	public void should_UpdateContent() {
		UploadRequest request = Mockito.mock(UploadRequest.class);
		String version = "2.1";
		String targetId = "emf:id";

		editOfflineRestService.updateContent(request, targetId, version);

		Mockito.verify(request).setInstanceVersion(version);
		Mockito.verify(request).setTargetId(targetId);
		Mockito.verify(actions).callAction(request);
	}

	@Test(expected = BadRequestException.class)
	public void should_ThrowBadRequestException_When_FileCannotBeFound() {
		EditOfflineCheckOutRequest request = Mockito.mock(EditOfflineCheckOutRequest.class);
		Mockito.when(actions.callAction(Matchers.eq(request))).thenThrow(FileNotSupportedException.class);
		editOfflineRestService.streamContent(request);

		Mockito.verify(actions).callAction(request);
	}

	@Test(expected = BadRequestException.class)
	public void should_ThrowBadRequestException_When_HaveExceptionDuringUpdateOfCustomProperties() {
		EditOfflineCheckOutRequest request = Mockito.mock(EditOfflineCheckOutRequest.class);
		Mockito.when(actions.callAction(request)).thenThrow(FileCustomPropertiesUpdateException.class);
		editOfflineRestService.streamContent(request);

		Mockito.verify(actions).callAction(request);
	}

	@Test
	public void should_actionBeCalled() {
		EditOfflineCheckOutRequest request = Mockito.mock(EditOfflineCheckOutRequest.class);
		editOfflineRestService.streamContent(request);

		Mockito.verify(actions).callAction(request);
	}
}
