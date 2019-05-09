package com.sirma.itt.seip.instance.actions.delete;

import java.util.Collection;

import javax.inject.Inject;

import org.apache.commons.lang3.StringUtils;

import com.sirma.itt.seip.domain.event.AuditableEvent;
import com.sirma.itt.seip.domain.instance.Instance;
import com.sirma.itt.seip.domain.instance.InstanceReference;
import com.sirma.itt.seip.event.EventService;
import com.sirma.itt.seip.exceptions.InstanceNotFoundException;
import com.sirma.itt.seip.instance.DomainInstanceService;
import com.sirma.itt.seip.instance.InstanceTypeResolver;
import com.sirma.itt.seip.instance.actions.Action;
import com.sirma.itt.seip.plugin.Extension;
import com.sirma.itt.seip.rest.exceptions.BadRequestException;

/**
 * Performs delete operation for instance. The id of the target instance that should be deleted is passed as part of
 * {@link DeleteRequest}. The action will return as result the ids of the deleted instances, which includes the children
 * of the target instance that is deleted.
 *
 * @author A. Kunchev
 * @see DomainInstanceService#delete(String)
 */
@Extension(target = Action.TARGET_NAME, enabled = true, order = 85)
public class DeleteAction implements Action<DeleteRequest> {

	@Inject
	private InstanceTypeResolver instanceTypeResolver;

	@Inject
	private DomainInstanceService domainInstanceService;

	@Inject
	private EventService eventService;

	@Override
	public String getName() {
		return DeleteRequest.DELETE_OPERATION;
	}

	@Override
	public Collection<String> perform(DeleteRequest request) {
		String id = (String) request.getTargetId();
		if (StringUtils.isBlank(id)) {
			throw new BadRequestException("The passed id is blank.");
		}

		Instance instance = instanceTypeResolver.resolveReference(id).map(InstanceReference::toInstance).orElseThrow(
				() -> new InstanceNotFoundException(id));
		Collection<String> deleted = domainInstanceService.delete(id);
		eventService.fire(new AuditableEvent(instance, request.getUserOperation()));
		return deleted;
	}

	@Override
	public boolean shouldLockInstanceBeforeAction(DeleteRequest request) {
		return false;
	}
}