package com.sirma.itt.seip.instance.actions.publish;

import javax.inject.Inject;

import com.sirma.itt.seip.domain.instance.Instance;
import com.sirma.itt.seip.instance.actions.Action;
import com.sirma.itt.seip.instance.revision.PublishInstanceRequest;
import com.sirma.itt.seip.instance.revision.RevisionService;
import com.sirma.itt.seip.plugin.Extension;

/**
 * Action that executes the publish operation
 *
 * @author BBonev
 */
@Extension(target = Action.TARGET_NAME, order = 343)
public class PublishAction implements Action<PublishActionRequest> {

	@Inject
	private RevisionService revisionService;

	@Override
	public String getName() {
		return PublishActionRequest.OPERATION_NAME;
	}

	@Override
	public Object perform(PublishActionRequest request) {
		Instance instance = request.getTargetReference().toInstance();
		PublishInstanceRequest publishRequest = new PublishInstanceRequest(instance, request.toOperation(),
				request.getRelationType(), request.getRelatedInstances());

		return revisionService.publish(publishRequest);
	}

}
