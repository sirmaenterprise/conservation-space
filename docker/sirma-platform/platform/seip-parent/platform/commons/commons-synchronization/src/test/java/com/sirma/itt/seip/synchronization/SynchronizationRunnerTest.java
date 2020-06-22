package com.sirma.itt.seip.synchronization;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.Map.Entry;
import java.util.function.Function;

import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.Spy;

import com.sirma.itt.seip.plugin.Plugins;
import com.sirma.itt.seip.util.EqualsHelper;

/**
 * Test for {@link SynchronizationRunner}
 *
 * @author BBonev
 */
public class SynchronizationRunnerTest {

	@InjectMocks
	private SynchronizationRunner runner;

	@Mock
	private SynchronizationConfiguration<Object, Object> configuration;

	private Collection<SynchronizationConfiguration<Object, Object>> plugins = new LinkedList<>();
	@Spy
	private Plugins<SynchronizationConfiguration<Object, Object>> configurations = new Plugins<>("", plugins);

	@Before
	public void beforeMethod() {
		MockitoAnnotations.initMocks(this);
		plugins.clear();
		plugins.add(configuration);

		when(configuration.getName()).thenReturn("syncConfig");
		when(configuration.getComparator()).thenReturn(EqualsHelper::nullSafeEquals);
	}

	@Test
	public void testSyncByName_noData() throws Exception {
		when(configuration.getSource())
				.thenReturn(SynchronizationDataProvider.create(LinkedList::new, Function.identity()));
		when(configuration.getDestination())
				.thenReturn(SynchronizationDataProvider.create(LinkedList::new, Function.identity()));

		SynchronizationResultState resultState = runner.runSynchronization("syncConfig");
		assertNotNull(resultState);
		assertNotNull(resultState.getResult());
		assertFalse(resultState.getResult().hasChanges());
	}

	@Test
	public void testSyncByName_failToRetrieveData() throws Exception {
		when(configuration.getSource())
				.thenReturn(SynchronizationDataProvider.create(LinkedList::new, Function.identity()));
		when(configuration.getDestination()).thenThrow(SynchronizationException.class);

		SynchronizationResultState resultState = runner.runSynchronization("syncConfig");
		assertNotNull(resultState);
		assertNull(resultState.getResult());
		assertNotNull(resultState);
	}

	@Test
	public void testSyncByName_noConfiguration() throws Exception {
		SynchronizationResultState resultState = runner.runSynchronization("noSyncConfig");
		assertNotNull(resultState);
		assertNull(resultState.getResult());
		assertNotNull(resultState);
	}

	@Test
	public void getAvailable() throws Exception {
		Collection<String> available = runner.getAvailable();
		assertNotNull(available);
		assertFalse(available.isEmpty());
		assertEquals(1, available.size());
		assertEquals("syncConfig", available.iterator().next());
	}

	@Test
	public void runAll() throws Exception {
		when(configuration.getSource())
				.thenReturn(SynchronizationDataProvider.create(LinkedList::new, Function.identity()));
		when(configuration.getDestination())
				.thenReturn(SynchronizationDataProvider.create(LinkedList::new, Function.identity()));

		Collection<SynchronizationResultState> collection = runner.runAll();
		assertNotNull(collection);
		assertEquals(1, collection.size());
	}

	@Test
	public void testSyncByName_noMerge() throws Exception {
		when(configuration.getSource()).thenReturn(
				SynchronizationDataProvider.create(() -> Arrays.asList("1", "2", "3"), Function.identity()));
		when(configuration.getDestination()).thenReturn(
				SynchronizationDataProvider.create(() -> Arrays.asList("1", "4", "5"), Function.identity()));

		SynchronizationResultState resultState = runner.runSynchronization("syncConfig");
		assertNotNull(resultState);
		assertNotNull(resultState.getResult());
		assertTrue(resultState.getResult().hasChanges());
		verify(configuration).save(eq(resultState.getResult()), any());
		assertArrayEquals(Arrays.asList("2", "3").toArray(), resultState.getResult().getToAdd().keySet().toArray());
		assertArrayEquals(Arrays.asList("4", "5").toArray(), resultState.getResult().getToRemove().keySet().toArray());
	}

	@Test
	public void testSyncByName_withMerge() throws Exception {
		Map<Object, Object> source = new HashMap<>();
		source.put("1", "1");
		source.put("2", "3");
		source.put("3", "4");
		Map<Object, Object> destination = new HashMap<>();
		destination.put("1", "1");
		destination.put("2", "4");
		destination.put("4", "4");
		Function identity = (Object entry) -> ((Entry) entry).getKey();
		SynchronizationProvider sourceProvider = () -> new ArrayList<>(source.entrySet());
		SynchronizationProvider destinationProvider = () -> new ArrayList<>(destination.entrySet());
		when(configuration.getSource()).thenReturn(SynchronizationDataProvider.create(sourceProvider, identity));
		when(configuration.getDestination())
				.thenReturn(SynchronizationDataProvider.create(destinationProvider, identity));
		when(configuration.isMergeSupported()).thenReturn(Boolean.TRUE);
		when(configuration.merge(any(), any())).then(a -> a.getArgumentAt(1, Object.class));

		SynchronizationResultState resultState = runner.runSynchronization("syncConfig");
		assertNotNull(resultState);
		assertNotNull(resultState.getResult());
		assertTrue(resultState.getResult().hasChanges());
		verify(configuration).save(eq(resultState.getResult()), any());
		// add entry 3=4 for key 3
		assertEquals("{3=3=4}", resultState.getResult().getToAdd().toString());
		// remove entry 4=4 for key 4
		assertEquals("{4=4=4}", resultState.getResult().getToRemove().toString());
		// modified key 2 for entry 2=3 and removed 2=4
		assertEquals("{2=2=3}", resultState.getResult().getModified().toString());
	}

	@Test
	public void testSyncByName_problemSave() throws Exception {
		when(configuration.getSource()).thenReturn(
				SynchronizationDataProvider.create(() -> Arrays.asList("1", "2", "3"), Function.identity()));
		when(configuration.getDestination()).thenReturn(
				SynchronizationDataProvider.create(() -> Arrays.asList("1", "4", "5"), Function.identity()));
		doThrow(new RuntimeException()).when(configuration).save(any(), any());

		SynchronizationResultState resultState = runner.runSynchronization("syncConfig");
		assertNotNull(resultState);
		assertNull(resultState.getResult());
		assertNotNull(resultState.getException());
	}

}
