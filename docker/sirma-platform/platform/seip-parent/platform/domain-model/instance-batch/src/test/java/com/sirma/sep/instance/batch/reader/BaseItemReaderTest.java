package com.sirma.sep.instance.batch.reader;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyInt;
import static org.mockito.Matchers.anyLong;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.BiConsumer;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import javax.batch.runtime.context.JobContext;

import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import com.sirma.itt.seip.util.ReflectionUtils;
import com.sirma.sep.instance.batch.BatchDataService;
import com.sirma.sep.instance.batch.BatchProperties;

public class BaseItemReaderTest {

	@InjectMocks
	private BaseItemReader<String> baseItemReader;

	@Mock
	private BatchDataService batchDataService;

	@Mock
	private JobContext jobContext;

	@Mock
	private BatchProperties batchProperties;

	private final static String JOB_ID = "job-uuid-id";
	private final static long JOB_EXECUTION_ID = 1L;
	private final static int CHUNK_SIZE = 29;

	private List<String> data = null;

	private BiConsumer<String, BiConsumer<String, String>> dataLoader;

	@Before
	public void init() {
		baseItemReader = new DefaultItemReader() {

			@Override
			protected int getChunkSize() {
				return CHUNK_SIZE;
			}

			@Override
			protected void loadBatchData(Collection<String> instanceIds, BiConsumer<String, String> consumer) {
				if (dataLoader == null) {
					super.loadBatchData(instanceIds, consumer);
				} else {
					instanceIds.forEach(id -> dataLoader.accept(id, consumer));
				}
			}
		};


		MockitoAnnotations.initMocks(this);

		when(batchDataService.getBatchData(eq(JOB_EXECUTION_ID), anyInt(), eq(CHUNK_SIZE))).then(a -> {
			int offset = a.getArgumentAt(1, Integer.class);
			int requestedCount = a.getArgumentAt(2, Integer.class);
			if (requestedCount + offset > data.size()) {
				requestedCount = data.size();
			}
			return new ArrayList<>(data.subList(offset, requestedCount));
		});

		doAnswer(a -> {
			@SuppressWarnings("unchecked")
			List<String> processedIds = (List<String>) a.getArgumentAt(1, List.class);
			data.replaceAll(s -> processedIds.contains(s) ? null : s);
			return null;
		}).when(batchDataService).markJobDataAsProcessed(eq(JOB_EXECUTION_ID), any());

		when(jobContext.getExecutionId()).thenReturn(JOB_EXECUTION_ID);
		when(batchProperties.getJobId(JOB_EXECUTION_ID)).thenReturn(JOB_ID);
		when(batchDataService.getJobProgress(JOB_ID)).thenReturn(0);
	}

	private void withDataOfSize(int size) throws Exception {
		data = IntStream.range(1, size + 1).boxed().map(Object::toString).collect(Collectors.toList());

		baseItemReader.open(null);
	}

	@Test
	public void should_ReadAChunkOfItems() throws Exception {
		withDataOfSize(CHUNK_SIZE);

		for (int i = 1; i <= CHUNK_SIZE; i++) {
			String item = (String) baseItemReader.readItem();
			assertEquals(Integer.toString(i), item);
		}
	}

	@Test
	public void should_LoadPartialChunk() throws Exception {
		withDataOfSize(5);

		for (int i = 1; i <= 5; i++) {
			String item = (String) baseItemReader.readItem();
			assertEquals(Integer.toString(i), item);
		}

		// in the test case the end of the data has been reached and a null should to be returned
		String outOfChunkItem = (String) baseItemReader.readItem();
		assertNull(outOfChunkItem);
	}

	@Test
	public void should_MarkChunkAsRead() throws Exception {
		withDataOfSize(100);

		for (int i = 1; i <= CHUNK_SIZE; i++) {
			baseItemReader.readItem();
		}

		baseItemReader.checkpointInfo();

		assertEquals(71, data.stream().filter(Objects::nonNull).count());
	}

	@Test
	public void should_MarkPartialChunkAsRead() throws Exception {
		withDataOfSize(100);

		for (int i = 1; i <= 5; i++) {
			baseItemReader.readItem();
		}

		baseItemReader.checkpointInfo();

		assertEquals(95, data.stream().filter(Objects::nonNull).count());
	}

	@Test
	public void should_LoadMoreDataWhenChunkEndIsReached() throws Exception {
		withDataOfSize(20);

		for (int i = 1; i <= CHUNK_SIZE; i++) {
			baseItemReader.readItem();
		}

		baseItemReader.checkpointInfo();

		String outOfChunkItem = (String) baseItemReader.readItem();
		assertNull(outOfChunkItem);
	}

	@Test
	public void should_ClearTemporaryData() throws Exception {
		withDataOfSize(10);

		baseItemReader.close();

		Field loadedField = BaseItemReader.class.getDeclaredField("loaded");
		loadedField.setAccessible(true);

		assertNull(ReflectionUtils.getFieldValue(loadedField, baseItemReader));

		Field iteratorField = BaseItemReader.class.getDeclaredField("processed");
		iteratorField.setAccessible(true);

		assertNull(ReflectionUtils.getFieldValue(iteratorField, baseItemReader));
	}

	@Test
	public void should_detectPartiallyLoadedData() throws Exception {

		AtomicInteger count = new AtomicInteger(0);
		// skip 1 of 5 items
		dataLoader = (id, consumer) -> {
			if (count.incrementAndGet() % 5 != 0) {
				consumer.accept(id, id);
			}
		};

		withDataOfSize((int) (CHUNK_SIZE * 1.5));

		Object item = null;
		do {
			for (int i = 1; i <= CHUNK_SIZE; i++) {
				item = baseItemReader.readItem();
				if (item == null) {
					break;
				}
			}

			baseItemReader.checkpointInfo();
		} while (item != null);

		ArgumentCaptor<List<String>> captor = ArgumentCaptor.forClass(List.class);
		verify(batchDataService, times(2)).markJobDataAsProcessed(anyLong(), captor.capture());
		long processed = captor.getAllValues().stream().mapToLong(List::size).sum();
		assertEquals("should have skipped", (int) (CHUNK_SIZE * 1.5), processed);

		verify(batchDataService, times(3)).getBatchData(anyLong(), anyInt(), anyInt());
	}

}
