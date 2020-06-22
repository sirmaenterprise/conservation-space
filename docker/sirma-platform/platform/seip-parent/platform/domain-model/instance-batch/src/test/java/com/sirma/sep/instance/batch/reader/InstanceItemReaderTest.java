package com.sirma.sep.instance.batch.reader;

import static org.junit.Assert.*;
import static org.mockito.Matchers.anyList;
import static org.mockito.Matchers.anyLong;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.when;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import javax.batch.runtime.context.JobContext;
import javax.inject.Inject;

import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import com.sirma.itt.seip.domain.instance.EmfInstance;
import com.sirma.itt.seip.domain.instance.Instance;
import com.sirma.itt.seip.instance.dao.InstanceService;
import com.sirma.sep.instance.batch.BatchDataService;
import com.sirma.sep.instance.batch.BatchProperties;

/**
 * Test for {@link InstanceItemReader}
 *
 * @author <a href="mailto:borislav.bonev@sirma.bg">Borislav Bonev</a>
 * @since 01/12/2017
 */
public class InstanceItemReaderTest {
	@InjectMocks
	private InstanceItemReader reader;
	@Mock
	private InstanceService instanceService;
	@Mock
	private BatchDataService batchDataService;
	@Mock
	private JobContext jobContext;
	@Mock
	private BatchProperties batchProperties;

	@Before
	@SuppressWarnings("unchecked")
	public void setUp() throws Exception {
		MockitoAnnotations.initMocks(this);
		when(batchProperties.getChunkSize(anyLong())).thenReturn(Optional.of(5));
		when(batchDataService.getBatchData(anyLong(), eq(0), eq(5))).thenReturn(
				Arrays.asList("emf:instance1", "emf:instance2", "emf:instance3", "emf:instance4", "emf:instance5"));
		when(batchDataService.getBatchData(anyLong(), eq(5), eq(5))).thenReturn(Collections.emptyList());
		when(jobContext.getExecutionId()).thenReturn(1L);
		when(instanceService.loadByDbId(anyList())).then(a -> a.getArgumentAt(0, List.class)
						.stream()
						.map(id -> new EmfInstance(id.toString()))
						.collect(Collectors.toList())
		);
	}

	@Test
	public void loadBatchData() throws Exception {
		reader.open(null);
		verifyItem(reader.readItem(), "emf:instance1");
		verifyItem(reader.readItem(), "emf:instance2");
		verifyItem(reader.readItem(), "emf:instance3");
		verifyItem(reader.readItem(), "emf:instance4");
		verifyItem(reader.readItem(), "emf:instance5");
		assertNull(reader.readItem());
	}

	private void verifyItem(Object item, String expectedId) {
		assertTrue(item instanceof Instance);
		assertEquals(expectedId, ((Instance) item).getId().toString());
	}

}
