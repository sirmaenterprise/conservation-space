package com.sirma.itt.seip.instance.actions.evaluation;

import static com.sirma.itt.seip.instance.actions.evaluation.ActionsListRequest.ACTIONS_LIST;

import java.util.Collections;
import java.util.Set;

import javax.inject.Inject;

import com.sirma.itt.commons.utils.string.StringUtils;
import com.sirma.itt.seip.collections.CollectionUtils;
import com.sirma.itt.seip.domain.definition.label.LabelProvider;
import com.sirma.itt.seip.domain.instance.Instance;
import com.sirma.itt.seip.domain.instance.InstanceReference;
import com.sirma.itt.seip.domain.security.Action;
import com.sirma.itt.seip.domain.security.ActionTypeConstants;
import com.sirma.itt.seip.exceptions.InstanceNotFoundException;
import com.sirma.itt.seip.instance.InstanceTypeResolver;
import com.sirma.itt.seip.instance.context.InstanceContextInitializer;
import com.sirma.itt.seip.permissions.action.AuthorityService;
import com.sirma.itt.seip.permissions.action.EmfAction;
import com.sirma.itt.seip.plugin.Extension;
import com.sirma.itt.seip.rest.exceptions.BadRequestException;

/**
 * Used for instances actions evaluation. Uses {@link AuthorityService} to extract the actions for the user and the
 * instance. Also when the actions are evaluated the context is restored.
 *
 * @author A. Kunchev
 */
@Extension(target = com.sirma.itt.seip.instance.actions.Action.TARGET_NAME, enabled = true, order = 20)
public class InstanceActionsList implements com.sirma.itt.seip.instance.actions.Action<ActionsListRequest> {

	@Inject
	private AuthorityService authorityService;

	@Inject
	private InstanceTypeResolver instanceTypeResolver;

	@Inject
	private LabelProvider labelProvider;

	@Inject
	private InstanceContextInitializer instanceContextInitializer;

	@Override
	public String getName() {
		return ACTIONS_LIST;
	}

	@Override
	public Object perform(ActionsListRequest request) {
		return evaluateActions(request);
	}

	/**
	 * Uses {@link AuthorityService} to evaluate the actions for instance. If the service returns empty collection, then
	 * NO_PERMISSIONS action is set and returned. This method also resolves the context for the current instance.
	 *
	 * @param request
	 *            the {@link ActionsListRequest} which should contain all of the information needed to evaluate the
	 *            instance actions
	 * @return set of evaluated actions for the instance, if there are any or NO_PERMISSIONS if the internal service
	 *         returns empty collection
	 *         <p>
	 * @throws BadRequestException
	 *             when: <br />
	 *             the request object is null <br />
	 *             the id or the placeholder are null or empty <br />
	 *             cannot find instance for the passed id <br />
	 */
	private Set<Action> evaluateActions(ActionsListRequest request) {
		if (request == null) {
			throw new BadRequestException("Invalid request. The requst object is null.");
		}

		String targetId = (String) request.getTargetId();
		if (StringUtils.isNullOrEmpty(targetId) || request.getPlaceholder() == null) {
			throw new BadRequestException("Invalid request. The id of the instance or the placeholder are missing!");
		}

		Instance instance = instanceTypeResolver
				.resolveReference(targetId)
					.map(InstanceReference::toInstance)
					.orElseThrow(() -> new InstanceNotFoundException(targetId));

		instanceContextInitializer.restoreHierarchy(instance);
		Set<Action> allowedActions = authorityService.getAllowedActions(instance, request.getPlaceholder());
		if (CollectionUtils.isEmpty(allowedActions)) {
			EmfAction noPermission = new EmfAction(ActionTypeConstants.NO_PERMISSIONS);
			noPermission.setLabel(labelProvider.getValue("cmf.btn.actions.no_permissions"));
			noPermission.setDisabled(true);
			return Collections.singleton(noPermission);
		}

		return allowedActions;
	}

}
