package com.sirma.itt.seip.instance.actions.thumbnail;

import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Collections;
import java.util.Optional;
import java.util.Set;

import org.junit.Before;
import org.junit.Test;
import org.mockito.InOrder;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import com.sirma.itt.seip.Pair;
import com.sirma.itt.seip.collections.ContextualMap;
import com.sirma.itt.seip.content.rendition.ThumbnailService;
import com.sirma.itt.seip.domain.instance.Instance;
import com.sirma.itt.seip.exception.EmfRuntimeException;
import com.sirma.itt.seip.instance.InstanceTypeResolver;
import com.sirma.itt.seip.instance.relation.LinkConstants;
import com.sirma.itt.seip.instance.relation.LinkService;
import com.sirma.itt.seip.rest.exceptions.ResourceException;
import com.sirma.itt.seip.testutil.fakes.SecurityContextManagerFake;
import com.sirma.itt.seip.testutil.mocks.InstanceReferenceMock;

/**
 * Tests for {@link AddThumbnailAction}.
 *
 * @author A. Kunchev
 */
public class AddThumbnailActionTest {

	private static final Set<String> LINKS_TO_REMOVE = Collections.singleton(LinkConstants.HAS_THUMBNAIL);

	@InjectMocks
	private AddThumbnailAction action;

	@Mock
	private InstanceTypeResolver instanceTypeResolver;

	@Mock
	private LinkService linkService;

	@Mock
	private ThumbnailService thumbnailService;

	@Before
	public void setup() {
		action = new AddThumbnailAction();
		MockitoAnnotations.initMocks(this);
		LinkConstants.init(new SecurityContextManagerFake(), ContextualMap.create());
	}

	@Test
	public void getName() {
		assertEquals(AddThumbnailRequest.OPERATION_NAME, action.getName());
	}

	@Test(expected = EmfRuntimeException.class)
	public void perform_nullRequest() {
		action.perform(null);
	}

	@Test(expected = NullPointerException.class)
	public void perform_nullTargetId() {
		action.perform(buildRequest(null, "thumbnailId"));
	}

	@Test(expected = NullPointerException.class)
	public void perform_nullThumbnailObjectId() {
		action.perform(buildRequest("targetId", null));
	}

	@Test(expected = ResourceException.class)
	public void perform_noTargetInstanceFound() {
		when(instanceTypeResolver.resolveReference("targetId")).thenReturn(Optional.empty());
		action.perform(buildRequest("targetId", "thumbnailId"));
	}

	@Test(expected = ResourceException.class)
	public void perform_noThumbnailInstance() {
		when(instanceTypeResolver.resolveReference("targetId")).thenReturn(Optional.of(new InstanceReferenceMock()));
		when(instanceTypeResolver.resolveReference("thumbnailId")).thenReturn(Optional.empty());
		action.perform(buildRequest("targetId", "thumbnailId"));
	}

	@Test(expected = ResourceException.class)
	public void perform_noOldThumbnail_failToAddNewOne() {
		InstanceReferenceMock target = new InstanceReferenceMock();
		when(instanceTypeResolver.resolveReference("targetId")).thenReturn(Optional.of(target));
		InstanceReferenceMock thumbnail = new InstanceReferenceMock();
		when(instanceTypeResolver.resolveReference("thumbnailId")).thenReturn(Optional.of(thumbnail));
		when(linkService.link(target, thumbnail, LinkConstants.HAS_THUMBNAIL, LinkConstants.IS_THUMBNAIL_OF,
				LinkConstants.getDefaultSystemProperties())).thenReturn(Pair.nullPair());
		action.perform(buildRequest("targetId", "thumbnailId"));
		verify(linkService).removeLinksFor(target, LINKS_TO_REMOVE);
	}

	@Test
	public void perform_withOldThumbnail_addedNewOne() {
		InstanceReferenceMock target = new InstanceReferenceMock();
		when(instanceTypeResolver.resolveReference("targetId")).thenReturn(Optional.of(target));

		InstanceReferenceMock thumbnail = new InstanceReferenceMock();
		when(instanceTypeResolver.resolveReference("thumbnailId")).thenReturn(Optional.of(thumbnail));

		when(linkService.link(target, thumbnail, LinkConstants.HAS_THUMBNAIL, LinkConstants.IS_THUMBNAIL_OF,
				LinkConstants.getDefaultSystemProperties())).thenReturn(new Pair<>("first", "second"));

		action.perform(buildRequest("targetId", "thumbnailId"));
		verify(linkService).removeLinksFor(target, LINKS_TO_REMOVE);
		verify(thumbnailService).register(eq(target), any(Instance.class), eq(null));
	}

	@Test
	public void perform_shouldUnlinkRelations() {
		InOrder order = inOrder(linkService);

		InstanceReferenceMock target = new InstanceReferenceMock();
		when(instanceTypeResolver.resolveReference("targetId")).thenReturn(Optional.of(target));

		InstanceReferenceMock thumbnail = new InstanceReferenceMock();
		when(instanceTypeResolver.resolveReference("thumbnailId")).thenReturn(Optional.of(thumbnail));

		when(linkService.link(target, thumbnail, LinkConstants.HAS_THUMBNAIL, LinkConstants.IS_THUMBNAIL_OF,
				LinkConstants.getDefaultSystemProperties())).thenReturn(new Pair<>("first", "second"));

		action.perform(buildRequest("targetId", "thumbnailId"));
		// ensure the order of invocation of the methods
		order.verify(linkService).unlink(target, null, LinkConstants.HAS_THUMBNAIL, LinkConstants.IS_THUMBNAIL_OF);
		order.verify(linkService).removeLinksFor(target, LINKS_TO_REMOVE);
	}

	private static AddThumbnailRequest buildRequest(String targetId, String thumbnailObjectId) {
		AddThumbnailRequest request = new AddThumbnailRequest();
		request.setTargetId(targetId);
		request.setThumbnailObjectId(thumbnailObjectId);
		return request;
	}

}
