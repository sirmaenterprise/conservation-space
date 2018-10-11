package com.sirma.itt.seip.resources.synchronization;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Collections;
import java.util.Map;

import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import com.sirma.itt.seip.event.EmfEvent;
import com.sirma.itt.seip.event.EventService;
import com.sirma.itt.seip.synchronization.SynchronizationResult;
import com.sirma.itt.seip.synchronization.SynchronizationResultState;
import com.sirma.itt.seip.synchronization.SynchronizationRunner;

/**
 * Test for {@link ResourceSynchronization}
 *
 * @author BBonev
 */
public class ResourceSynchronizationTest {
	@InjectMocks
	private ResourceSynchronization synchronization;
	@Mock
	private SynchronizationRunner runner;
	@Mock
	private EventService eventService;

	@Before
	public void beforeMethod() {
		MockitoAnnotations.initMocks(this);
	}

	@Test
	public void synchronizeAll() {
		mockSynchronization(true);
		synchronization.synchronizeAll();
		verify(runner, times(3)).runSynchronization(anyString());
		verify(eventService, times(3)).fire(any(EmfEvent.class));
	}

	@Test
	public void synchronizeUsers() {
		mockSynchronization(true);
		synchronization.synchronizeUsers();
		verify(runner).runSynchronization(RemoteUserSyncProvider.NAME);
		verify(eventService).fire(any(EmfEvent.class));
	}

	@Test
	public void synchronizeUsers_noChanges() {
		mockSynchronization(false);
		synchronization.synchronizeUsers();
		verify(runner).runSynchronization(RemoteUserSyncProvider.NAME);
		verify(eventService, never()).fire(any(EmfEvent.class));
	}

	@Test
	public void synchronizeGroups() {
		mockSynchronization(true);
		synchronization.synchronizeGroups();
		verify(runner).runSynchronization(RemoteGroupSyncProvider.NAME);
	}

	@Test
	public void synchronizeGroupsMembers() {
		mockSynchronization(true);
		synchronization.synchronizeGroupsMembers();
		verify(runner).runSynchronization(RemoteGroupMembersSyncProvider.NAME);
		verify(eventService).fire(any(EmfEvent.class));
	}

	@Test
	public void synchronizeGroupsMembers_noChanges() {
		mockSynchronization(false);
		synchronization.synchronizeGroupsMembers();
		verify(runner).runSynchronization(RemoteGroupMembersSyncProvider.NAME);
		verify(eventService, never()).fire(any(EmfEvent.class));
	}

	private void mockSynchronization(boolean withChanges) {
		Map<String, String> changes = Collections.emptyMap();
		if (withChanges) {
			changes = Collections.singletonMap("key", "value");
		}
		Map<String, String> added = changes;
		SynchronizationResult<?, ?> result = new SynchronizationResult<>(added, Collections.emptyMap(),
				Collections.emptyMap());
		when(runner.runSynchronization(anyString()))
				.then(a -> new SynchronizationResultState(a.getArgumentAt(0, String.class), result, 0));
	}
}
