package com.sirma.sep.content.batch;

import static org.junit.Assert.*;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Arrays;

import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import com.sirma.sep.content.ContentEntityDao;
import com.sirma.sep.instance.batch.BatchService;
import com.sirma.sep.instance.batch.StreamBatchRequest;

/**
 * Test for {@link BatchContentProcessingImpl}
 *
 * @author <a href="mailto:borislav.bonev@sirma.bg">Borislav Bonev</a>
 * @since 09/01/2019
 */
public class BatchContentProcessingImplTest {

	@InjectMocks
	private BatchContentProcessingImpl contentProcessing;

	@Mock
	private BatchService batchService;

	@Mock
	private ContentEntityDao contentDao;

	@Before
	public void setUp() throws Exception {
		MockitoAnnotations.initMocks(this);
		when(contentDao.getContentIdBy(any())).thenReturn(Arrays.asList("emf:content1", "emf:content2"));
	}

	@Test
	public void processContent() throws Exception {
		ContentProcessingRequest request = new ContentProcessingRequest();
		request.setContentSelector(new ContentInfoMatcher());
		request.setContentProcessorName("someProcessorName");
		request.setContentWriter("someWriterName");
		request.getProperties().setProperty("somePropertyKey", "somePropertyValue");
		int processContent = contentProcessing.processContent(request);

		assertEquals(2, processContent);
		ArgumentCaptor<StreamBatchRequest> captor = ArgumentCaptor.forClass(
				StreamBatchRequest.class);
		verify(batchService).execute(captor.capture());
		StreamBatchRequest value = captor.getValue();
		assertNotNull(value);
		assertEquals("somePropertyValue", value.getProperties().getProperty("somePropertyKey"));
	}

}