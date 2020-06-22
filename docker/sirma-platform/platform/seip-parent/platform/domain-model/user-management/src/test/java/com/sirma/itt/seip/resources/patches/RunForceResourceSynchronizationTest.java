package com.sirma.itt.seip.resources.patches;

import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.MockitoAnnotations.initMocks;

import java.util.Arrays;
import java.util.Collections;

import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;

import com.sirma.itt.seip.resources.EmfUser;
import com.sirma.itt.seip.resources.ResourceService;
import com.sirma.itt.seip.synchronization.SyncRuntimeConfiguration;
import com.sirma.itt.seip.synchronization.SynchronizationRunner;

import liquibase.database.Database;

/**
 * Tests for {@link RunForceResourceSynchronization}.
 *
 * @author smustafov
 */
public class RunForceResourceSynchronizationTest {

	@Mock
	private ResourceService resourceService;
	@Mock
	private SynchronizationRunner synchronizationRunner;
	@Mock
	private Database database;

	@InjectMocks
	private RunForceResourceSynchronization runForceSynchronization;

	@Before
	public void init() {
		initMocks(this);
	}

	@Test
	public void should_NotExecutePatch_When_UsersListIsNull() throws Exception {
		when(resourceService.getAllUsers()).thenReturn(null);

		runForceSynchronization.execute(database);

		verify(synchronizationRunner, never()).runAll(any(SyncRuntimeConfiguration.class));
	}

	@Test
	public void should_NotExecutePatch_When_NoPersistedUsers() throws Exception {
		when(resourceService.getAllUsers()).thenReturn(Collections.emptyList());

		runForceSynchronization.execute(database);

		verify(synchronizationRunner, never()).runAll(any(SyncRuntimeConfiguration.class));
	}

	@Test
	public void should_ExecutePatch_When_ThereArePersistedUsers() throws Exception {
		when(resourceService.getAllUsers()).thenReturn(Arrays.asList(new EmfUser(), new EmfUser()));

		runForceSynchronization.execute(database);

		ArgumentCaptor<SyncRuntimeConfiguration> argCaptor = ArgumentCaptor.forClass(SyncRuntimeConfiguration.class);
		verify(synchronizationRunner).runAll(argCaptor.capture());

		SyncRuntimeConfiguration syncRuntimeConfiguration = argCaptor.getValue();
		assertEquals(true, syncRuntimeConfiguration.isForceSynchronizationEnabled());
		assertEquals(false, syncRuntimeConfiguration.isDeleteAllowed());
	}

}
