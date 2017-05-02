package com.sirma.itt.seip.instance.draft.observers;

import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Matchers;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

import com.sirma.itt.seip.domain.instance.EmfInstance;
import com.sirma.itt.seip.instance.draft.DraftService;
import com.sirma.itt.seip.instance.event.AfterInstanceDeleteEvent;

/**
 * Tests for {@link InstanceDraftHandler}.
 *
 * @author A. Kunchev
 */
public class InstanceDraftHandlerTest {

	@InjectMocks
	private InstanceDraftHandler handler;

	@Mock
	private DraftService draftService;

	@Before
	public void setup() {
		handler = new InstanceDraftHandler();
		MockitoAnnotations.initMocks(this);
	}

	@Test
	public void onInstanceDeleted_nullEvent_deleteNotCalled() {
		handler.onInstanceDeleted(null);
		Mockito.verify(draftService, Mockito.never()).delete(Matchers.anyString());
	}

	@Test
	@SuppressWarnings({ "rawtypes", "unchecked" })
	public void onInstanceDeleted_nullInstance_deleteNotCalled() {
		handler.onInstanceDeleted(new AfterInstanceDeleteEvent(null));
		Mockito.verify(draftService, Mockito.never()).delete(Matchers.anyString());
	}

	@Test
	@SuppressWarnings({ "rawtypes", "unchecked" })
	public void onInstanceDeleted_notNullInstance_deleteCalled() {
		EmfInstance instance = new EmfInstance();
		instance.setId("instance-id");
		handler.onInstanceDeleted(new AfterInstanceDeleteEvent(instance));
		Mockito.verify(draftService).delete("instance-id");
	}

}
