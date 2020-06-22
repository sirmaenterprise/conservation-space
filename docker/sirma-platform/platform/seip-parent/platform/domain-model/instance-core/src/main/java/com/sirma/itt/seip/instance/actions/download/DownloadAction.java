package com.sirma.itt.seip.instance.actions.download;

import java.io.Serializable;

import javax.inject.Inject;
import javax.ws.rs.core.Response.Status;

import org.apache.commons.lang3.StringUtils;

import com.sirma.itt.seip.domain.event.AuditableEvent;
import com.sirma.itt.seip.domain.instance.InstanceReference;
import com.sirma.itt.seip.event.EventService;
import com.sirma.itt.seip.instance.InstanceTypeResolver;
import com.sirma.itt.seip.instance.actions.Action;
import com.sirma.itt.seip.plugin.Extension;
import com.sirma.itt.seip.rest.exceptions.ResourceException;
import com.sirma.itt.seip.rest.models.ErrorData;

/**
 * Executes the download action. The {@link #perform(DownloadRequest)} method builds link, with which the content of the
 * given instance, can be retrieved.
 *
 * @author A. Kunchev
 */
@Extension(target = Action.TARGET_NAME, enabled = true, order = 30)
public class DownloadAction implements Action<DownloadRequest> {

	@Inject
	private InstanceTypeResolver instanceTypeResolver;

	@Inject
	private EventService eventService;

	@Override
	public String getName() {
		return DownloadRequest.DOWNLOAD;
	}

	@Override
	public Object perform(DownloadRequest request) {
		Serializable targetId = request.getTargetId();
		InstanceReference reference = instanceTypeResolver
				.resolveReference(targetId)
					.orElseThrow(() -> new ResourceException(Status.NOT_FOUND,
							new ErrorData().setMessage("Could not find instance with id: " + targetId), null));

		StringBuilder link = new StringBuilder()
				.append("/instances/")
					.append(targetId)
					.append("/content?download=true");

		String purpose = request.getPurpose();
		if (StringUtils.isNotBlank(purpose)) {
			link.append("&purpose=").append(purpose);
		}

		eventService.fire(new AuditableEvent(reference.toInstance(), request.getUserOperation()));

		return link.toString();
	}

	@Override
	public boolean shouldLockInstanceBeforeAction(DownloadRequest request) {
		// read only operation does not require locking
		return false;
	}
}
