package com.sirma.itt.seip.instance.actions.thumbnail;

import java.io.Serializable;
import java.util.Collections;
import java.util.Objects;
import java.util.Set;

import javax.inject.Inject;
import javax.ws.rs.core.Response.Status;

import com.sirma.itt.seip.Pair;
import com.sirma.itt.seip.content.rendition.RenditionService;
import com.sirma.itt.seip.content.rendition.ThumbnailService;
import com.sirma.itt.seip.domain.instance.Instance;
import com.sirma.itt.seip.domain.instance.InstanceReference;
import com.sirma.itt.seip.exception.EmfRuntimeException;
import com.sirma.itt.seip.instance.InstanceTypeResolver;
import com.sirma.itt.seip.instance.actions.Action;
import com.sirma.itt.seip.instance.relation.LinkConstants;
import com.sirma.itt.seip.instance.relation.LinkService;
import com.sirma.itt.seip.plugin.Extension;
import com.sirma.itt.seip.rest.exceptions.ResourceException;
import com.sirma.itt.seip.rest.models.ErrorData;

/**
 * Executes 'add thumbnail' operation. The {@link #perform(AddThumbnailRequest)} method retrieves the instances needed
 * to perform the operation. If the target instance already has thumbnail, it is removed and then added the new one.
 *
 * @author A. Kunchev
 */
@Extension(target = Action.TARGET_NAME, enabled = true, order = 50)
public class AddThumbnailAction implements Action<AddThumbnailRequest> {

	private static final Set<String> LINKS_TO_REMOVE = Collections.singleton(LinkConstants.HAS_THUMBNAIL);

	@Inject
	private InstanceTypeResolver instanceTypeResolver;

	@Inject
	private LinkService linkService;

	@Inject
	private ThumbnailService thumbnailService;

	@Override
	public String getName() {
		return AddThumbnailRequest.OPERATION_NAME;
	}

	@Override
	public Instance perform(AddThumbnailRequest request) {
		if (request == null) {
			throw new EmfRuntimeException("The request object is null.");
		}

		Serializable targetId = request.getTargetId();
		Serializable thumbnailObjectId = request.getThumbnailObjectId();
		Objects.requireNonNull(targetId, "Missing target id in the request!");
		Objects.requireNonNull(thumbnailObjectId, "Missing thumbnail object id in the request!");

		InstanceReference targetReference = resolveReference(targetId);
		InstanceReference thumbnailReference = resolveReference(thumbnailObjectId);

		// searches for the given relations that exist for the target reference and removes them
		linkService.unlink(targetReference, null, LinkConstants.HAS_THUMBNAIL, LinkConstants.IS_THUMBNAIL_OF);
		// marks the relations as isActive: false
		linkService.removeLinksFor(targetReference, LINKS_TO_REMOVE);
		Pair<Serializable, Serializable> link = linkService.link(targetReference, thumbnailReference,
				LinkConstants.HAS_THUMBNAIL, LinkConstants.IS_THUMBNAIL_OF, LinkConstants.getDefaultSystemProperties());

		if (link.getFirst() == null || link.getSecond() == null) {
			throw new ResourceException(Status.INTERNAL_SERVER_ERROR,
					new ErrorData().setMessage("Could not add thumbnail for instance with id: " + targetId), null);
		}

		// we remove the old thumbnail, if there is any, before register another
		thumbnailService.removeThumbnail(targetId, RenditionService.DEFAULT_PURPOSE);
		thumbnailService.register(targetReference, thumbnailReference.toInstance(), null);
		return targetReference.toInstance();
	}

	private InstanceReference resolveReference(Serializable id) {
		return instanceTypeResolver.resolveReference(id).orElseThrow(() -> new ResourceException(Status.NOT_FOUND,
				new ErrorData().setMessage("Could not find instance with id: " + id), null));
	}

}
