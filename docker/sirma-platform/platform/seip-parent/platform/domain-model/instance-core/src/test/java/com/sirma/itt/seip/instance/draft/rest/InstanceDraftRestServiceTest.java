package com.sirma.itt.seip.instance.draft.rest;

import static org.junit.Assert.assertNotNull;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import com.sirma.itt.seip.domain.rest.InternalServerErrorException;
import com.sirma.itt.seip.instance.draft.DraftInstance;
import com.sirma.itt.seip.instance.draft.DraftService;
import com.sirma.itt.seip.rest.exceptions.BadRequestException;

/**
 * Tests for {@link InstanceDraftRestService}.
 *
 * @author A. Kunchev
 */
public class InstanceDraftRestServiceTest {

	@InjectMocks
	private InstanceDraftRestService service;

	@Mock
	private DraftService draftService;

	@Before
	public void setup() {
		service = new InstanceDraftRestService();
		MockitoAnnotations.initMocks(this);
	}

	@Test(expected = BadRequestException.class)
	public void createDraft_nullInstance_excption() {
		service.createDraft("instance-id", null);
	}

	@Test(expected = InternalServerErrorException.class)
	public void createDraft_failedToCreateDraft_excption() {
		when(draftService.create(eq("instance-id"), eq(null), eq("instance-content"))).thenReturn(null);
		service.createDraft("instance-id", "instance-content");
	}

	@Test
	public void createDraft_successful_notNullResult() {
		when(draftService.create(eq("instance-id"), eq(null), eq("content"))).thenReturn(new DraftInstance());
		assertNotNull(service.createDraft("instance-id", "content"));
	}

	@Test
	public void getDrafts_internalDraftExtractCalled() {
		service.getDraft("instance-id");
		verify(draftService).getDraft(eq("instance-id"), eq(null));
	}

	@Test
	public void deleteDrafts_internalDraftDeleteCalled() {
		service.deleteDraft("instance-id");
		verify(draftService).delete(eq("instance-id"), eq(null));
	}
}
