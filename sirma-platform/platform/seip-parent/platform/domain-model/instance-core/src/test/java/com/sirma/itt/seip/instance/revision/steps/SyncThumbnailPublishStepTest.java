package com.sirma.itt.seip.instance.revision.steps;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Collections;
import java.util.Optional;

import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import com.sirma.itt.seip.collections.ContextualMap;
import com.sirma.itt.seip.domain.instance.Instance;
import com.sirma.itt.seip.instance.InstanceTypeResolver;
import com.sirma.itt.seip.instance.relation.LinkConstants;
import com.sirma.itt.seip.instance.relation.LinkReference;
import com.sirma.itt.seip.instance.relation.LinkService;
import com.sirma.itt.seip.instance.revision.PublishInstanceRequest;
import com.sirma.itt.seip.instance.state.Operation;
import com.sirma.itt.seip.security.context.SecurityContextManager;
import com.sirma.itt.seip.tasks.DefaultSchedulerConfiguration;
import com.sirma.itt.seip.tasks.SchedulerContext;
import com.sirma.itt.seip.tasks.SchedulerEntryType;
import com.sirma.itt.seip.tasks.SchedulerRetryException;
import com.sirma.itt.seip.tasks.SchedulerService;
import com.sirma.itt.seip.testutil.mocks.InstanceReferenceMock;
import com.sirma.sep.content.rendition.RenditionService;
import com.sirma.sep.content.rendition.ThumbnailService;

/**
 * Test for {@link SyncThumbnailPublishStep}
 *
 * @author BBonev
 */
public class SyncThumbnailPublishStepTest {

	@InjectMocks
	private SyncThumbnailPublishStep step;

	@Mock
	private LinkService linkService;
	@Mock
	private ThumbnailService thumbnailService;
	@Mock
	private RenditionService renditionService;
	@Mock
	private SchedulerService schedulerService;
	@Mock
	private InstanceTypeResolver instanceTypeResolver;
	@Mock
	private SecurityContextManager securityContextManager;

	@Before
	public void beforeMethod() {
		MockitoAnnotations.initMocks(this);
		LinkConstants.init(securityContextManager, ContextualMap.create());
		when(schedulerService.schedule(anyString(), any(), any())).then(a -> {
			step.execute(a.getArgumentAt(2, SchedulerContext.class));
			return null;
		});
		when(schedulerService.buildEmptyConfiguration(SchedulerEntryType.TIMED))
				.thenReturn(new DefaultSchedulerConfiguration());
	}

	@Test
	public void shouldDoNothingIfNoThumbnailIsFound() throws Exception {
		step.execute(buildContext());
		verify(renditionService, never()).getThumbnail(any());
	}

	@Test
	public void shouldAssignThumbnailToRevision() throws Exception {
		PublishContext context = buildContext();
		LinkReference response = new LinkReference();
		response.setTo(InstanceReferenceMock.createGeneric("emf:thumbnailSource"));
		when(linkService.getLinks(context.getRequest().getInstanceToPublish().toReference(),
				LinkConstants.HAS_THUMBNAIL)).thenReturn(Collections.singletonList(response));

		when(renditionService.getThumbnail("emf:thumbnailSource")).thenReturn("someThumbnail");

		step.execute(context);

		verify(thumbnailService).addThumbnail(context.getRevision().toReference(), "someThumbnail");
	}

	@Test(expected = SchedulerRetryException.class)
	public void shouldFailAssignThumbnailNotPresent() throws Exception {
		PublishContext context = buildContext();
		LinkReference response = new LinkReference();
		response.setTo(InstanceReferenceMock.createGeneric("emf:thumbnailSource"));
		when(linkService.getLinks(context.getRequest().getInstanceToPublish().toReference(),
				LinkConstants.HAS_THUMBNAIL)).thenReturn(Collections.singletonList(response));

		step.execute(context);
	}

	@Test(expected = SchedulerRetryException.class)
	public void shouldFailAssignThumbnailRevisionNotFound() throws Exception {
		PublishContext context = buildContext();
		LinkReference response = new LinkReference();
		response.setTo(InstanceReferenceMock.createGeneric("emf:thumbnailSource"));
		when(linkService.getLinks(context.getRequest().getInstanceToPublish().toReference(),
				LinkConstants.HAS_THUMBNAIL)).thenReturn(Collections.singletonList(response));

		when(instanceTypeResolver.resolveReference(any())).thenReturn(Optional.empty());

		when(renditionService.getThumbnail("emf:thumbnailSource")).thenReturn(null, "someThumbnail");

		step.execute(context);
	}

	@Test
	public void shouldAssignThumbnailDalayed() throws Exception {
		PublishContext context = buildContext();
		LinkReference response = new LinkReference();
		response.setTo(InstanceReferenceMock.createGeneric("emf:thumbnailSource"));
		when(linkService.getLinks(context.getRequest().getInstanceToPublish().toReference(),
				LinkConstants.HAS_THUMBNAIL)).thenReturn(Collections.singletonList(response));

		when(instanceTypeResolver.resolveReference(any()))
				.then(a -> Optional.of(InstanceReferenceMock.createGeneric(a.getArgumentAt(0, String.class))));

		when(renditionService.getThumbnail("emf:thumbnailSource")).thenReturn(null, "someThumbnail");

		step.execute(context);

		verify(thumbnailService).addThumbnail(context.getRevision().toReference(), "someThumbnail");
	}

	private static PublishContext buildContext() {
		Instance instance = InstanceReferenceMock.createGeneric("emf:sourceInstance").toInstance();
		Instance revision = InstanceReferenceMock.createGeneric("emf:sourceInstance-r1.0").toInstance();
		return new PublishContext(new PublishInstanceRequest(instance, new Operation(), null, null), revision);
	}
}
