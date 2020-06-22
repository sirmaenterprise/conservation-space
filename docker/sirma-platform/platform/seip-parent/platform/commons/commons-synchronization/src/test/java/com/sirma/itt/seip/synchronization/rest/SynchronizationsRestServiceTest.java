package com.sirma.itt.seip.synchronization.rest;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.when;

import java.util.Arrays;

import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import com.sirma.itt.seip.synchronization.SyncRuntimeConfiguration;
import com.sirma.itt.seip.synchronization.SynchronizationException;
import com.sirma.itt.seip.synchronization.SynchronizationResult;
import com.sirma.itt.seip.synchronization.SynchronizationResultState;
import com.sirma.itt.seip.synchronization.SynchronizationRunner;

import net.javacrumbs.jsonunit.JsonAssert;

/**
 * Test for {@link SynchronizationsRestService}
 *
 * @author BBonev
 */
public class SynchronizationsRestServiceTest {
	@InjectMocks
	private SynchronizationsRestService service;
	@Mock
	private SynchronizationRunner runner;

	@Before
	public void beforeMethod() {
		MockitoAnnotations.initMocks(this);
	}

	@Test
	public void getAvailableSynchronizations() throws Exception {
		when(runner.getAvailable()).thenReturn(Arrays.asList("1", "2"));
		JsonAssert.assertJsonEquals("[\"1\", \"2\"]", service.getAvailableSynchronizations());
	}

	@Test
	public void runSynchronization() throws Exception {
		when(runner.runSynchronization(eq("sync"), any(SyncRuntimeConfiguration.class)))
				.thenReturn(new SynchronizationResultState("sync", SynchronizationResult.EMPTY, 2));

		String synchronization = service.runSynchronization("sync", new SyncRuntimeConfiguration());
		JsonAssert.assertJsonEquals(
				"{\"name\":\"sync\", \"added\":[], \"removed\":[], \"modified\":[], \"duration\":2}",
				synchronization);
	}

	@Test
	public void runSynchronization_withEror() throws Exception {
		when(runner.runSynchronization(eq("sync"), any(SyncRuntimeConfiguration.class)))
				.thenReturn(new SynchronizationResultState("sync", new SynchronizationException("test")));

		String synchronization = service.runSynchronization("sync", new SyncRuntimeConfiguration());
		JsonAssert.assertJsonEquals("{\"name\":\"sync\", \"exception\":\"test\"}", synchronization);
	}

	@Test
	public void runAll() throws Exception {
		when(runner.runAll(any(SyncRuntimeConfiguration.class)))
				.thenReturn(Arrays.asList(new SynchronizationResultState("sync1", SynchronizationResult.EMPTY, 2),
						new SynchronizationResultState("sync2", new SynchronizationException("test"))));

		String synchronization = service.runAll(new SyncRuntimeConfiguration());
		JsonAssert.assertJsonEquals(
				"[{\"name\":\"sync1\", \"added\":[], \"removed\":[], \"modified\":[], \"duration\":2}, "
						+ "{\"name\":\"sync2\", \"exception\":\"test\"}]",
				synchronization);
	}
}
