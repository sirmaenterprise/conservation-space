package com.sirma.itt.seip.instance.actions.compare;

import static com.sirma.itt.seip.instance.version.compare.VersionCompareContext.create;

import javax.inject.Inject;
import javax.ws.rs.core.Response.Status;

import com.sirma.itt.seip.instance.actions.Action;
import com.sirma.itt.seip.instance.version.compare.VersionCompareContext;
import com.sirma.itt.seip.instance.version.compare.VersionCompareException;
import com.sirma.itt.seip.instance.version.compare.VersionCompareService;
import com.sirma.itt.seip.plugin.Extension;
import com.sirma.itt.seip.rest.exceptions.BadRequestException;
import com.sirma.itt.seip.rest.exceptions.ResourceException;

/**
 * Executes the compare versions action. The {@link #perform(VersionCompareRequest)} method builds link with which the
 * result content from the operation execution could be download.
 *
 * @author A. Kunchev
 */
@Extension(target = Action.TARGET_NAME, order = 180)
public class VersionCompareAction implements Action<VersionCompareRequest> {

	@Inject
	private VersionCompareService compareVersionsService;

	@Override
	public String getName() {
		return VersionCompareRequest.COMPARE_VERSIONS;
	}

	@Override
	public String perform(VersionCompareRequest request) {
		VersionCompareContext context = create(request.getFirstSourceId(), request.getSecondSourceId(),
				request.getAuthenticationHeaders()).setOriginalInstanceId(request.getTargetId());

		try {
			return compareVersionsService.compareVersionsContent(context);
		} catch (IllegalArgumentException iae) {
			throw new BadRequestException(iae.getMessage(), iae);
		} catch (VersionCompareException vce) {
			throw new ResourceException(Status.INTERNAL_SERVER_ERROR, vce.getMessage(), vce);
		}
	}

	@Override
	public boolean shouldLockInstanceBeforeAction(VersionCompareRequest request) {
		// read only operation
		return false;
	}
}
