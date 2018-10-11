package com.sirma.sep.instance.batch;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.Serializable;
import java.util.Arrays;
import java.util.Collections;
import java.util.Optional;
import java.util.function.Supplier;
import java.util.stream.Stream;

import org.junit.Test;
import org.mockito.ArgumentCaptor;

import com.sirma.itt.seip.domain.instance.Instance;
import com.sirma.itt.seip.domain.search.SearchArguments;
import com.sirma.itt.seip.search.SearchService;

/**
 * @author <a href="mailto:borislav.bonev@sirma.bg">Borislav Bonev</a>
 * @since 13/07/2017
 */
public class BatchRequestBuilderTest {

	public static final String QUERY = "select ?s where {?s ?p ?o.}";

	@Test
	public void fromCollection() throws Exception {
		StreamBatchRequest request = BatchRequestBuilder.fromCollection("jobId", Arrays.asList("1", "2"));
		assertNotNull(request);
		assertEquals("jobId", request.getBatchName());
		assertEquals(2L, request.getStreamSupplier().get().count());
	}

	@Test
	public void fromSearch_shouldPassArguments() throws Exception {
		SearchService searchService = mock(SearchService.class);
		when(searchService.stream(any(), any())).then(a -> Stream.of("value1"));

		StreamBatchRequest request = BatchRequestBuilder.fromSearch("jobId", QUERY,
				Collections.singletonMap("key", "value"), "s", searchService);

		assertEquals("jobId", request.getBatchName());

		Supplier<Stream<Serializable>> supplier = request.getStreamSupplier();
		Optional<Serializable> first = supplier.get().findFirst();
		assertTrue(first.isPresent());
		assertEquals("value1", first.get());

		ArgumentCaptor<SearchArguments<Instance>> captor = ArgumentCaptor.forClass(SearchArguments.class);

		verify(searchService).stream(captor.capture(), any());

		SearchArguments<Instance> item = captor.getValue();
		assertFalse(((SearchArguments) item).getArguments().isEmpty());
		assertEquals(QUERY, item.getStringQuery());
		assertEquals(-1, item.getMaxSize());
	}

	@Test
	public void fromSearch() throws Exception {
		SearchService searchService = mock(SearchService.class);
		when(searchService.stream(any(), any())).then(a -> Stream.of("value1"));

		StreamBatchRequest request = BatchRequestBuilder.fromSearch("jobId", QUERY,
				Collections.emptyMap(), "s", searchService);

		assertEquals("jobId", request.getBatchName());

		Supplier<Stream<Serializable>> supplier = request.getStreamSupplier();
		Optional<Serializable> first = supplier.get().findFirst();
		assertTrue(first.isPresent());
		assertEquals("value1", first.get());

		ArgumentCaptor<SearchArguments<Instance>> captor = ArgumentCaptor.forClass(SearchArguments.class);

		verify(searchService).stream(captor.capture(), any());

		SearchArguments<Instance> item = captor.getValue();
		assertTrue(((SearchArguments) item).getArguments().isEmpty());
		assertEquals(QUERY, item.getStringQuery());
		assertEquals(-1, item.getMaxSize());
	}

}
