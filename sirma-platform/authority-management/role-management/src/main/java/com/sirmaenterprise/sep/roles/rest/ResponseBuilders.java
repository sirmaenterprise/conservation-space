package com.sirmaenterprise.sep.roles.rest;

import static com.sirma.itt.seip.collections.CollectionUtils.isNotEmpty;

import java.util.function.Function;

import com.sirma.itt.seip.domain.definition.label.LabelProvider;
import com.sirmaenterprise.sep.roles.ActionDefinition;
import com.sirmaenterprise.sep.roles.RoleActionModel.RoleActionMapping;
import com.sirmaenterprise.sep.roles.RoleDefinition;

/**
 * Builder for the role management response objects
 *
 * @author BBonev
 */
public class ResponseBuilders {

	private ResponseBuilders() {
		// utility class
	}

	/**
	 * Build {@link ActionResponse} that represents the information from the given action
	 *
	 * @param labelProvider
	 *            label provider to use when resolving labels
	 * @return the build response object
	 */
	public static Function<ActionDefinition, ActionResponse> buildAction(LabelProvider labelProvider) {
		return def -> new ActionResponse()
				.setId(def.getId())
					.setLabel(labelProvider.getLabel(def.getId() + ".label"))
					.setTooltip(labelProvider.getLabel(def.getId() + ".tooltip"))
					.setEnabled(def.isEnabled())
					.setActionType(def.getActionType())
					.setVisible(def.isVisible())
					.setImagePath(def.getImagePath())
					.setImmediate(def.isImmediate());
	}

	/**
	 * Build {@link RoleReponse} that represents the information from the given role
	 *
	 * @param labelProvider
	 *            label provider to use when resolving labels
	 * @return the build response object
	 */
	public static Function<RoleDefinition, RoleResponse> buildRole(LabelProvider labelProvider) {
		return role -> new RoleResponse()
				.setId(role.getIdentifier())
					.setOrder(role.getGlobalPriority())
					.setLabel(labelProvider.getLabel(role.getIdentifier().toLowerCase() + ".label"))
					.setCanRead(role.canRead())
					.setCanWrite(role.canWrite())
					.setUserDefined(role.isUserDefined());
	}

	/**
	 * Build {@link RoleAction} using the information about the given role and action
	 *
	 * @param entry
	 *            the source entry to use
	 * @return object combining information about the role and the action
	 */
	public static RoleAction buildRoleActions(RoleActionMapping entry) {
		RoleAction roleActionsResponse = new RoleAction()
				.setAction(entry.getAction().getId())
					.setEnabled(entry.isEnabled())
					.setRole(entry.getRole().getId());
		if (isNotEmpty(entry.getFilters())) {
			roleActionsResponse.getFilters().addAll(entry.getFilters());
		}
		return roleActionsResponse;
	}
}
