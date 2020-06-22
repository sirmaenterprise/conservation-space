package com.sirma.itt.seip.instance.location;

import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import com.sirma.itt.seip.domain.instance.EmfInstance;
import com.sirma.itt.seip.domain.instance.Instance;
import com.sirma.itt.seip.domain.instance.InstanceReference;
import com.sirma.itt.seip.domain.search.SearchArguments;
import com.sirma.itt.seip.instance.location.InstanceDefaultLocationService;
import com.sirma.itt.seip.instance.location.InstanceDefaultLocationServiceImpl;
import com.sirma.itt.seip.instance.relation.LinkReference;
import com.sirma.itt.seip.instance.relation.LinkService;
import com.sirma.itt.seip.search.SearchService;

/**
 * Test for InstanceDefaultLocationServiceImpl.
 *
 * @author A. Kunchev
 */
public class InstanceDefaultLocationServiceImplTest {

	@InjectMocks
	private InstanceDefaultLocationService service = new InstanceDefaultLocationServiceImpl();

	@Mock
	private LinkService linkService;

	@Mock
	private SearchService searchService;

	@Before
	public void setup() {
		MockitoAnnotations.initMocks(this);
	}

	// -------------------------------- addDefaultLocations ---------------------------------------

	@Test
	public void addDefaultLocations_nullParam_linkServiceNotCalled() {
		addDefaultLocationsInternal_linkServiceNotCalled(null);
	}

	@Test
	public void addDefaultLocations_emptyParam_linkServiceNotCalled() {
		addDefaultLocationsInternal_linkServiceNotCalled(Collections.emptyMap());
	}

	private void addDefaultLocationsInternal_linkServiceNotCalled(Map<InstanceReference, InstanceReference> map) {
		service.addDefaultLocations(map);
		verify(linkService, never()).linkSimple(any(InstanceReference.class), any(InstanceReference.class),
				anyString());
	}

	@Test
	public void addDefaultLocations_twoEntries_linkServiceCalledTwice() {
		Map<InstanceReference, InstanceReference> locations = new HashMap<>();
		locations.put(mock(InstanceReference.class), mock(InstanceReference.class));
		locations.put(mock(InstanceReference.class), mock(InstanceReference.class));
		service.addDefaultLocations(locations);
		verify(linkService, times(2)).linkSimple(any(InstanceReference.class), any(InstanceReference.class),
				anyString());
	}

	// -------------------------------- retrieveDefaultLocations ----------------------------------

	@Test
	public void retrieveDefaultLocations_searchServiceCalled() {
		retrieveDefaultLocationsInternal(new SearchArguments<>());
	}

	@Test
	public void retrieveDefaultLocations_withResults_searchServiceCalled() {
		SearchArguments<Instance> arguments = new SearchArguments<>();
		arguments.setResult(Arrays.asList(new EmfInstance()));
		retrieveDefaultLocationsInternal(arguments);
	}

	private void retrieveDefaultLocationsInternal(SearchArguments<Instance> arguments) {
		when(searchService.getFilter(anyString(), any(), any())).thenReturn(arguments);
		service.retrieveLocations("definitionId");
		verify(searchService).getFilter(anyString(), any(), any());
		verify(searchService).searchAndLoad(any(), any());
	}

	// -------------------------------- retrieveOnlyDefaultLocations -------------------------------

	@Test
	public void retrieveOnlyDefaultLocations_nullReference_emptyCollection() {
		Collection<InstanceReference> locations = service.retrieveOnlyDefaultLocations(null);
		assertEquals(Collections.emptyList(), locations);
	}

	@Test
	public void retrieveOnlyDefaultLocations_oneLocation_oneElementInTheCollection() {
		LinkReference linkReference = new LinkReference();
		linkReference.setFrom(mock(InstanceReference.class));
		when(linkService.getSimpleLinksTo(any(InstanceReference.class), anyString()))
				.thenReturn(Arrays.asList(linkReference));
		Collection<InstanceReference> locations = service.retrieveOnlyDefaultLocations(mock(InstanceReference.class));
		assertEquals(1, locations.size());
	}

	// -------------------------------- updateDefaultLocations -------------------------------------

	@Test
	public void updateDefaultLocations_nullParam_linkServiceNotCalled() {
		updateDefaultLocationsInternal_linkServiceNotCalled(null);
	}

	@Test
	public void updateDefaultLocations_emptyParam_linkServiceNotCalled() {
		updateDefaultLocationsInternal_linkServiceNotCalled(Collections.emptyMap());
	}

	private void updateDefaultLocationsInternal_linkServiceNotCalled(Map<InstanceReference, InstanceReference> map) {
		service.updateDefaultLocations(map);
		verify(linkService, never()).unlinkSimple(any(InstanceReference.class), any(InstanceReference.class),
				anyString());
		verify(linkService, never()).linkSimple(any(InstanceReference.class), any(InstanceReference.class),
				anyString());
	}

	@Test
	public void updateDefaultLocations_oneEntry_linkServiceCalledOnce() {
		Map<InstanceReference, InstanceReference> map = new HashMap<>();
		map.put(mock(InstanceReference.class), mock(InstanceReference.class));
		service.updateDefaultLocations(map);
		verify(linkService).unlinkSimple(any(InstanceReference.class), any(InstanceReference.class), anyString());
		verify(linkService).linkSimple(any(InstanceReference.class), any(InstanceReference.class), anyString());
	}

	// -------------------------------- removeDefaultLocations -------------------------------------

	@Test
	public void removeDefaultLocations_nullCollection_linkServiceNotCalled() {
		removeDefaultLocationsInternal_linkServiceNotCalled(null);
	}

	@Test
	public void removeDefaultLocations_emptyCollection_linkServiceNotCalled() {
		removeDefaultLocationsInternal_linkServiceNotCalled(Collections.emptyList());
	}

	private void removeDefaultLocationsInternal_linkServiceNotCalled(Collection<InstanceReference> collection) {
		service.removeDefaultLocations(collection);
		verify(linkService, never()).getLinks(any(InstanceReference.class), anyString());
		verify(linkService, never()).unlinkSimple(any(InstanceReference.class), any(InstanceReference.class),
				anyString());
	}

	@Test
	public void removeDefaultLocations_oneInstance_linkServiceCalledOnce() {
		when(linkService.getSimpleLinksTo(any(InstanceReference.class), anyString()))
				.thenReturn(Arrays.asList(new LinkReference()));
		service.removeDefaultLocations(Arrays.asList(mock(InstanceReference.class)));
		verify(linkService).getSimpleLinksTo(any(InstanceReference.class), anyString());
		verify(linkService).unlinkSimple(any(InstanceReference.class), any(InstanceReference.class),
				anyString());
	}

}
