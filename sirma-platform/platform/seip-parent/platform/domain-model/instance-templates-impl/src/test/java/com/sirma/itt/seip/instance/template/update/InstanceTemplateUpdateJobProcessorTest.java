package com.sirma.itt.seip.instance.template.update;

import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import javax.batch.runtime.context.JobContext;

import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import com.sirma.sep.instance.batch.BatchProperties;

public class InstanceTemplateUpdateJobProcessorTest {

	@InjectMocks
	private InstanceTemplateUpdateJobProcessor processor;

	@Mock
	private JobContext jobContext;

	@Mock
	private BatchProperties batchProperties;

	@Mock
	private InstanceTemplateUpdater instanceTemplateUpdater;

	private final static String TEMPLATE_ID = "template1";

	@Test
	public void should_CallProperSevicesOnItem() throws Exception {
		Object id = "template1";
		processor.init();
		processor.processItem(id);

		verify(instanceTemplateUpdater, times(1)).updateItem(TEMPLATE_ID, TEMPLATE_ID);
	}

	@Before
	public void init() {
		processor = new InstanceTemplateUpdateJobProcessor();
		MockitoAnnotations.initMocks(this);

		final long EXECUTION_ID = 1l;

		when(jobContext.getExecutionId()).thenReturn(EXECUTION_ID);

		when(batchProperties.getJobProperty(EXECUTION_ID, InstanceTemplateUpdateJobProperties.TEMPLATE_INSTANCE_ID))
		.thenReturn(TEMPLATE_ID);
	}

}
