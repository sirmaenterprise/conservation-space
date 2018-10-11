package com.sirma.itt.seip.instance.revision.steps;

import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Collections;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

import com.sirma.itt.seip.domain.instance.EmfInstance;
import com.sirma.itt.seip.domain.instance.Instance;
import com.sirma.itt.seip.domain.rest.EmfApplicationException;
import com.sirma.itt.seip.exception.EmfException;
import com.sirma.itt.seip.exception.RollbackedRuntimeException;
import com.sirma.itt.seip.instance.revision.PublishInstanceRequest;
import com.sirma.itt.seip.instance.state.Operation;
import com.sirma.sep.content.idoc.Idoc;
import com.sirma.sep.content.idoc.SectionNode;
import com.sirma.sep.content.idoc.Sections;
import com.sirma.sep.export.ContentExportException;
import com.sirma.sep.export.ExportURIBuilder;

/**
 * Test for {@link ExportTabsToPdfPublishStep}
 *
 * @author BBonev
 */
public class ExportTabsToPdfPublishStepTest {

	@InjectMocks
	private ExportTabsToPdfPublishStep toPdfPublishStep;

	@Mock
	private ExportToPdfPublishService publisher;
	@Mock
	private ExportURIBuilder uriBuilder;

	@Before
	public void beforeMethod() throws ContentExportException {
		MockitoAnnotations.initMocks(this);
		when(uriBuilder.getCurrentJwtToken()).thenReturn("jwtToken");
	}

	@Test
	public void shouldExportTheMarkedTabs() throws Exception {
		Instance revision = new EmfInstance();
		revision.setId("emf:instance-r1.0");
		Instance instanceToPublish = new EmfInstance();
		instanceToPublish.setId("emf:instance");
		PublishInstanceRequest request = new PublishInstanceRequest(instanceToPublish, new Operation(), null, null);
		PublishContext context = new PublishContext(request, revision);
		context.setView(createView());

		toPdfPublishStep.execute(context);
		verify(publisher).publishInstance(any(), any(), any(), any());
	}

	@Test(expected = RollbackedRuntimeException.class)
	public void shouldFailOnExportTimeout() throws Exception {
		PublishInstanceRequest request = new PublishInstanceRequest(mock(EmfInstance.class), mock(Operation.class),
				null, null);
		PublishContext context = new PublishContext(request, mock(EmfInstance.class));
		context.setView(createView());
		Mockito.doThrow(new RollbackedRuntimeException()).when(publisher).publishInstance(any(), any(), any(), any());

		toPdfPublishStep.execute(context);
	}

	@Test(expected = EmfApplicationException.class)
	public void shouldFailWhenThereAreNoConfiguredTabs() throws EmfException {
		Instance revision = new EmfInstance();
		revision.setId("emf:instance-r1.0");
		Instance instanceToPublish = new EmfInstance();
		instanceToPublish.setId("emf:instance");
		PublishInstanceRequest request = new PublishInstanceRequest(instanceToPublish, new Operation(), null, null);
		PublishContext context = new PublishContext(request, revision);
		context.setView(createView());
		Mockito.doThrow(new EmfException()).when(publisher).publishInstance(any(), any(), any(), any());

		toPdfPublishStep.execute(context);
	}

	private Idoc createView() {
		Idoc idoc = mock(Idoc.class);
		Sections sections = mock(Sections.class);
		List<SectionNode> emptyList = Collections.emptyList();
		when(sections.stream()).thenReturn(emptyList.stream());
		when(idoc.getSections()).thenReturn(sections);
		return idoc;
	}
}
