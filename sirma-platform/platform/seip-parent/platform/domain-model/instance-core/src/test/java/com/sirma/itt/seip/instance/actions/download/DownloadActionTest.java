package com.sirma.itt.seip.instance.actions.download;

import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Optional;

import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import com.sirma.itt.seip.domain.event.AuditableEvent;
import com.sirma.itt.seip.domain.instance.EmfInstance;
import com.sirma.itt.seip.domain.instance.Instance;
import com.sirma.itt.seip.event.EventService;
import com.sirma.itt.seip.instance.InstanceTypeResolver;
import com.sirma.itt.seip.rest.exceptions.ResourceException;
import com.sirma.itt.seip.testutil.mocks.DataTypeDefinitionMock;
import com.sirma.itt.seip.testutil.mocks.InstanceReferenceMock;

/**
 * Test for {@link DownloadAction}.
 *
 * @author A. Kunchev
 */
public class DownloadActionTest {

	private static final String TARGET_ID = "targetId";

	@InjectMocks
	private DownloadAction action;

	@Mock
	private InstanceTypeResolver instanceTypeResolver;

	@Mock
	private EventService eventService;

	@Before
	public void setup() {
		action = new DownloadAction();
		MockitoAnnotations.initMocks(this);
	}

	@Test
	public void getName() {
		assertEquals("download", action.getName());
	}

	@Test(expected = ResourceException.class)
	public void perform_nullInstance() {
		when(instanceTypeResolver.resolveReference(TARGET_ID)).thenReturn(Optional.empty());
		action.perform(buildDownloadRequest("purpose"));
	}

	@Test
	public void perform_nullPurpose() {
		String result = perform_purposeVariations(null);
		assertEquals("/instances/targetId/content?download=true", result);
	}

	@Test
	public void perform_emptyPurpose() {
		String result = perform_purposeVariations("");
		assertEquals("/instances/targetId/content?download=true", result);
	}

	@Test
	public void perform_withPurpose() {
		String result = perform_purposeVariations("primaryContent");
		assertEquals("/instances/targetId/content?download=true&purpose=primaryContent", result);
		verify(eventService).fire(new AuditableEvent(any(Instance.class), "download"));
	}

	private String perform_purposeVariations(String purpose) {
		EmfInstance instance = new EmfInstance();
		InstanceReferenceMock instanceReference = new InstanceReferenceMock(TARGET_ID,
				new DataTypeDefinitionMock(instance), instance);
		when(instanceTypeResolver.resolveReference(TARGET_ID)).thenReturn(Optional.of(instanceReference));
		return action.perform(buildDownloadRequest(purpose)).toString();
	}

	private static DownloadRequest buildDownloadRequest(String purpose) {
		DownloadRequest downloadRequest = new DownloadRequest();
		downloadRequest.setTargetId(TARGET_ID);
		downloadRequest.setPurpose(purpose);
		downloadRequest.setUserOperation("download");
		return downloadRequest;
	}

}
