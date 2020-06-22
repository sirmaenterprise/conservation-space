package com.sirma.itt.seip.instance.template.update;

import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.List;

import javax.batch.runtime.context.JobContext;

import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.Spy;

import com.sirma.itt.seip.domain.security.ActionTypeConstants;
import com.sirma.itt.seip.testutil.fakes.TransactionSupportFake;
import com.sirma.sep.instance.batch.BatchProperties;

public class InstanceTemplateUpdateJobWriterTest {

	@InjectMocks
	private InstanceTemplateUpdateJobWriter writer;

	@Mock
	private BatchProperties batchProperties;

	@Mock
	private JobContext jobContext;

	@Mock
	private InstanceTemplateUpdater instanceTemplateUpdater;

	@Spy
	private TransactionSupportFake transactionSupport;

	private final static String TEMPLATE_VERSION = "1.23";

	@Test
	public void should_CallSaveForEachItemProcessed() throws Exception {
		List<Object> items = new ArrayList<>();

		InstanceTemplateUpdateItem item1 = new InstanceTemplateUpdateItem("instance1",
				"<sections><section data-id=\"section1\"></section></sections>");
		items.add(item1);

		InstanceTemplateUpdateItem item2 = new InstanceTemplateUpdateItem("instance2",
				"<sections><section data-id=\"section2\"></section></sections>");
		items.add(item2);

		writer.init();
		writer.writeItems(items);

		verify(instanceTemplateUpdater, times(1)).saveItem((InstanceTemplateUpdateItem) items.get(0), TEMPLATE_VERSION,
				ActionTypeConstants.UPDATE_INSTANCE_TEMPLATE);

		verify(instanceTemplateUpdater, times(1)).saveItem((InstanceTemplateUpdateItem) items.get(1), TEMPLATE_VERSION,
				ActionTypeConstants.UPDATE_INSTANCE_TEMPLATE);
	}

	@Test
	public void should_ContinueInstanceSavingWhenExceptionOccurs() throws Exception {
		List<Object> items = new ArrayList<>();

		InstanceTemplateUpdateItem item1 = new InstanceTemplateUpdateItem("instance1",
				"<sections><section data-id=\"section1\"></section></sections>");
		items.add(item1);

		InstanceTemplateUpdateItem item2 = new InstanceTemplateUpdateItem("instance2",
				"<sections><section data-id=\"section2\"></section></sections>");
		items.add(item2);

		// Emulate exception when the first instance is saved
		when(instanceTemplateUpdater.saveItem(item1, TEMPLATE_VERSION, ActionTypeConstants.UPDATE_INSTANCE_TEMPLATE))
				.thenThrow(new IllegalStateException());

		writer.init();
		writer.writeItems(items);

		verify(instanceTemplateUpdater, times(1)).saveItem((InstanceTemplateUpdateItem) items.get(0), TEMPLATE_VERSION,
				ActionTypeConstants.UPDATE_INSTANCE_TEMPLATE);

		verify(instanceTemplateUpdater, times(1)).saveItem((InstanceTemplateUpdateItem) items.get(1), TEMPLATE_VERSION,
				ActionTypeConstants.UPDATE_INSTANCE_TEMPLATE);
	}

	@Before
	public void init() {
		writer = new InstanceTemplateUpdateJobWriter();
		MockitoAnnotations.initMocks(this);

		final long EXECUTION_ID = 1l;

		when(jobContext.getExecutionId()).thenReturn(EXECUTION_ID);

		when(batchProperties.getJobProperty(EXECUTION_ID, InstanceTemplateUpdateJobProperties.TEMPLATE_VERSION))
				.thenReturn(TEMPLATE_VERSION);
	}
}
