package com.sirma.itt.seip.instance.revision.steps;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.Spy;

import com.sirma.itt.seip.collections.ContextualMap;
import com.sirma.itt.seip.domain.instance.Instance;
import com.sirma.itt.seip.domain.instance.InstancePropertyNameResolver;
import com.sirma.itt.seip.instance.relation.LinkConstants;
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

	@Spy
	private InstancePropertyNameResolver nameResolver = InstancePropertyNameResolver.NO_OP_INSTANCE;
	@Mock
	private ThumbnailService thumbnailService;
	@Mock
	private RenditionService renditionService;
	@Mock
	private SchedulerService schedulerService;
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
	public void shouldAssignCustomThumbnailToRevision() throws Exception {
		PublishContext context = buildContext();
		context.getRequest().getInstanceToPublish().add(LinkConstants.HAS_THUMBNAIL, "emf:thumbnailSource");

		when(renditionService.getThumbnail("emf:thumbnailSource")).thenReturn("someThumbnail");

		step.execute(context);

		verify(thumbnailService).addAssignedThumbnail(context.getRevision().getId(), "someThumbnail");
	}

	@Test
	public void shouldAssignThumbnailOfTheSourceInstance() throws Exception {
		PublishContext context = buildContext();

		when(renditionService.getThumbnail("emf:sourceInstance")).thenReturn("source instance thumbnail");

		step.execute(context);

		verify(thumbnailService).addAssignedThumbnail(context.getRevision().getId(), "source instance thumbnail");
	}

	@Test(expected = SchedulerRetryException.class)
	public void shouldFailAssignThumbnailNotPresent() throws Exception {
		PublishContext context = buildContext();

		step.execute(context);
	}

	@Test
	public void shouldAssignThumbnailDelayed() throws Exception {
		PublishContext context = buildContext();
		context.getRequest().getInstanceToPublish().add(LinkConstants.HAS_THUMBNAIL, "emf:thumbnailSource");

		when(renditionService.getThumbnail("emf:thumbnailSource")).thenReturn(null, "someThumbnail");

		step.execute(context);

		verify(thumbnailService).addAssignedThumbnail(context.getRevision().getId(), "someThumbnail");
	}

	private static PublishContext buildContext() {
		Instance instance = InstanceReferenceMock.createGeneric("emf:sourceInstance").toInstance();
		Instance revision = InstanceReferenceMock.createGeneric("emf:sourceInstance-r1.0").toInstance();
		return new PublishContext(new PublishInstanceRequest(instance, new Operation(), null, null), revision);
	}
}
