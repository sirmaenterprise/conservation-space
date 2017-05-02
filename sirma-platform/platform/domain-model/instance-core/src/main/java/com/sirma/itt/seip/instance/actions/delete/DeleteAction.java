package com.sirma.itt.seip.instance.actions.delete;

import javax.inject.Inject;
import javax.ws.rs.core.Response;

import org.apache.commons.lang3.StringUtils;

import com.sirma.itt.seip.instance.DomainInstanceService;
import com.sirma.itt.seip.instance.actions.Action;
import com.sirma.itt.seip.plugin.Extension;
import com.sirma.itt.seip.rest.exceptions.BadRequestException;

/**
 * Performs delete operation for collection of instances. The ids of the instances that should be deleted are passed as
 * {@link DeleteRequest}. The instances are loaded on batches and then passed for delete.
 *
 * @author A. Kunchev
 * @see DomainInstanceService#delete(String)
 */
@Extension(target = Action.TARGET_NAME, enabled = true, order = 85)
public class DeleteAction implements Action<DeleteRequest> {

	@Inject
	private DomainInstanceService domainInstanceService;

	@Override
	public String getName() {
		return DeleteRequest.DELETE_OPERATION;
	}

	@Override
	public Object perform(DeleteRequest request) {
		String id = (String) request.getTargetId();
		if (StringUtils.isBlank(id)) {
			throw new BadRequestException("The passed id is blank.");
		}

		domainInstanceService.delete(id);
		return Response.ok().build();
	}

}
