package com.sirma.itt.seip.instance.revision.steps;

import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Collections;
import java.util.List;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import com.sirma.itt.seip.domain.instance.Instance;
import com.sirma.itt.seip.instance.DomainInstanceService;
import com.sirma.itt.seip.instance.revision.PublishInstanceRequest;
import com.sirma.itt.seip.instance.revision.steps.PublishStep.Steps;
import com.sirma.itt.seip.instance.state.Operation;
import com.sirma.itt.seip.tasks.DefaultSchedulerConfiguration;
import com.sirma.itt.seip.tasks.SchedulerConfiguration;
import com.sirma.itt.seip.tasks.SchedulerContext;
import com.sirma.itt.seip.tasks.SchedulerEntryType;
import com.sirma.itt.seip.tasks.SchedulerService;
import com.sirma.sep.content.idoc.Idoc;
import com.sirma.sep.content.idoc.SectionNode;
import com.sirma.sep.content.idoc.Sections;
import com.sirma.sep.export.ExportURIBuilder;

/**
 * Test for {@link AsyncExportTabsToPdfPublishStepTest}
 *
 * @author BBonev
 * @author bbanchev
 */
@RunWith(MockitoJUnitRunner.class)
public class AsyncExportTabsToPdfPublishStepTest {

	@InjectMocks
	private AsyncExportTabsToPdfPublishStep toPdfPublishStep;

	@Mock
	private ExportToPdfPublishService publisher;
	@Mock
	private SchedulerService schedulerService;
	@Mock
	private DomainInstanceService domainInstanceService;
	@Mock
	private ExportURIBuilder uriBuilder;

	@Test
	public void testGetName() throws Exception {
		assertEquals(Steps.EXPORT_TABS_AS_PDF.getName() + "_async", toPdfPublishStep.getName());
	}

	@Test
	public void testExecutePublishContext() throws Exception {
		when(uriBuilder.getCurrentJwtToken()).thenReturn("jwt");
		PublishInstanceRequest request = new PublishInstanceRequest(mockInstance("instanceId"), mock(Operation.class),
				null, null);
		PublishContext context = new PublishContext(request, mockInstance("revisionId"));
		context.setView(createView());

		ArgumentCaptor<SchedulerContext> schedulerContextCaptor = ArgumentCaptor.forClass(SchedulerContext.class);
		SchedulerConfiguration schedulerConfiguration = new DefaultSchedulerConfiguration();
		when(schedulerService.buildEmptyConfiguration(eq(SchedulerEntryType.TIMED))).thenReturn(schedulerConfiguration);
		toPdfPublishStep.execute(context);
		verify(schedulerService).schedule(eq(AsyncExportTabsToPdfPublishStep.NAME), eq(schedulerConfiguration),
				schedulerContextCaptor.capture());
		SchedulerContext schedulerContext = schedulerContextCaptor.getValue();
		assertEquals("jwt", schedulerContext.get("token"));
		assertEquals("revisionId", schedulerContext.get("revisionId"));
		assertEquals("instanceId", schedulerContext.get("sourceInstance"));

		toPdfPublishStep.execute(schedulerContext);
		verify(domainInstanceService).loadInstance(eq("revisionId"));
		verify(publisher).publishInstance(any(), any(), any(), any());
	}

	private Instance mockInstance(String id) {
		Instance instance = mock(Instance.class);
		when(instance.getId()).thenReturn(id);
		return instance;
	}

	protected Idoc createView() {
		Idoc idoc = mock(Idoc.class);
		Sections sections = mock(Sections.class);
		List<SectionNode> emptyList = Collections.emptyList();
		when(sections.stream()).thenReturn(emptyList.stream());
		when(idoc.getSections()).thenReturn(sections);
		return idoc;
	}

}
