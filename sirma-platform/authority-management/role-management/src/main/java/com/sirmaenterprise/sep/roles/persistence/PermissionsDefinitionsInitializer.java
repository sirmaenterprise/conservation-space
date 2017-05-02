package com.sirmaenterprise.sep.roles.persistence;

import static com.sirma.itt.seip.permissions.SecurityModel.BaseRoles.NO_PERMISSION;

import java.lang.invoke.MethodHandles;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.event.Observes;
import javax.inject.Inject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sirma.itt.seip.definition.event.AllDefinitionsLoaded;
import com.sirma.itt.seip.definition.label.LabelDefinition;
import com.sirma.itt.seip.definition.label.LabelService;
import com.sirma.itt.seip.definition.model.LabelImpl;
import com.sirma.itt.seip.domain.security.Action;
import com.sirma.itt.seip.permissions.Filterable;
import com.sirma.itt.seip.permissions.action.ActionProvider;
import com.sirma.itt.seip.permissions.role.Role;
import com.sirma.itt.seip.permissions.role.RoleIdentifier;
import com.sirma.itt.seip.permissions.role.RoleProviderExtension;
import com.sirma.itt.seip.plugin.ExtensionPoint;
import com.sirma.itt.seip.provider.MapProvider;
import com.sirmaenterprise.sep.roles.ActionDefinition;
import com.sirmaenterprise.sep.roles.RoleActionChanges;
import com.sirmaenterprise.sep.roles.RoleDefinition;
import com.sirmaenterprise.sep.roles.RoleManagement;

/**
 * Listen for permission definitions change and triggers the initial load of the definitions. After the initial load no
 * changes could be applied from the external sources to the internal model. They should be done via the REST API and
 * the UI.
 *
 * @since 2017-03-23
 * @author <a href="mailto:borislav.bonev@sirma.bg">Borislav Bonev</a>
 */
@ApplicationScoped
public class PermissionsDefinitionsInitializer {

	private static final Logger LOGGER = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

	@Inject
	private RoleManagement roleManagement;

	@Inject
	@ExtensionPoint(ActionProvider.TARGET_NAME)
	private Iterable<ActionProvider> actionProviders;

	@Inject
	@ExtensionPoint(value = RoleProviderExtension.TARGET_NAME)
	private Iterable<RoleProviderExtension> roleProviders;

	@Inject
	private LabelService labelService;

	/**
	 * Listens for definitions changes and tries to initialize the default role actions definitions and the mapping
	 * between them if they are not already initialized. Otherwise only saves the actions.
	 *
	 * @param event
	 *            the trigger event
	 */
	public void onDefinitionChange(@Observes AllDefinitionsLoaded event) {
		if (roleManagement.getRoles().findAny().isPresent()) {
			// we have already persisted model for roles so save only the actions
			saveActions();
		} else {
			// collect the external and internal model data
			populateModel();
			LOGGER.info("Initialized the roles and actions. Any futher changes to them should happen via the UI!");
		}
	}

	private void saveActions() {
		List<ActionDefinition> actions = new LinkedList<>();
		Map<String, Action> actionsData = getActions();

		for (Entry<String, Action> actionEntry : actionsData.entrySet()) {
			copyLabelAndTooltip(actionEntry.getValue());
			actions.add(buildActionDefinition(actionEntry.getValue()));
		}

		roleManagement.saveActions(actions);
	}

	private void populateModel() {
		Map<RoleIdentifier, Role> roleData = getRolesData();
		Map<String, Action> actionsData = getActions();

		List<RoleDefinition> roles = new ArrayList<>(roleData.size());
		List<ActionDefinition> actions = new LinkedList<>();
		RoleActionChanges roleActionChanges = new RoleActionChanges();

		for (Role role : roleData.values()) {
			RoleIdentifier roleId = role.getRoleId();
			roles.add(buildRoleDefinition(roleId));
			for (Action current : role.getAllAllowedActions()) {
				// get actions from registers and not from the statically defined in the roles and xmls
				Action action = actionsData.getOrDefault(current.getActionId(), current);
				copyLabelAndTooltip(action);
				actions.add(buildActionDefinition(action));
				roleActionChanges.enable(roleId.getIdentifier(), action.getActionId(), Filterable.getFilters(current));
			}
		}

		roleManagement.saveActions(actions);
		roleManagement.saveRoles(roles);
		roleManagement.updateRoleActionMappings(roleActionChanges);
	}

	/**
	 * Copies the label and tooltip for the given action, with changed label id and tooltip to format -
	 * actionId.label/tooltip, so we can know how to get the label or the tooltip for role actions management.
	 *
	 * @param action
	 *            the action for which a copy of the its label and tooltip will be made
	 */
	private void copyLabelAndTooltip(Action action) {
		if (action.getLabelId() != null) {
			LabelDefinition labelDefinition = labelService.getLabel(action.getLabelId());
			if (labelDefinition != null) {
				LabelImpl labelCopy = new LabelImpl();
				labelCopy.setIdentifier(transformLabelId(action.getActionId(), Action.LABEL_KEY));
				labelCopy.setLabels(labelDefinition.getLabels());
				labelService.saveLabel(labelCopy);
			}
		}
		if (action.getTooltip() != null) {
			LabelDefinition tooltipDefinition = labelService.getLabel(action.getTooltip());
			if (tooltipDefinition != null) {
				LabelImpl tooltipCopy = new LabelImpl();
				tooltipCopy.setIdentifier(transformLabelId(action.getActionId(), Action.TOOLTIP));
				tooltipCopy.setLabels(tooltipDefinition.getLabels());
				labelService.saveLabel(tooltipCopy);
			}
		}
	}

	private static String transformLabelId(String oldLabelId, String labelPostfix) {
		StringBuilder builder = new StringBuilder(oldLabelId.length() + labelPostfix.length() + 2);
		builder.append(oldLabelId);
		builder.append(".");
		builder.append(labelPostfix);
		return builder.toString();
	}

	private static ActionDefinition buildActionDefinition(Action action) {
		return new ActionDefinition()
				.setId(action.getActionId())
					.setActionType(action.getPurpose())
					.setEnabled(!action.isDisabled())
					.setUserDefined(false);
	}

	private static RoleDefinition buildRoleDefinition(RoleIdentifier roleId) {
		return new RoleDefinition()
				.setId(roleId.getIdentifier())
					.setOrder(roleId.getGlobalPriority())
					.setCanRead(roleId.canRead())
					.setCanWrite(roleId.canWrite())
					.setInternal(roleId.isInternal())
					.setEnabled(true)
					.setUserDefined(false);
	}

	protected Map<RoleIdentifier, Role> getRolesData() {
		Map<RoleIdentifier, Role> lastState = new HashMap<>();
		for (RoleProviderExtension roleProvider : roleProviders) {
			// the providers should only enrich the mapping
			roleProvider.getModel(lastState);
		}
		setNoPermissionRole(lastState);
		return lastState;
	}

	protected Map<String, Action> getActions() {
		Map<String, Action> result = new HashMap<>();
		for (MapProvider<String, Action> provider : actionProviders) {
			Map<String, Action> data = provider.provide();
			if (data != null) {
				result.putAll(data);
			}
		}
		return result;
	}

	private static void setNoPermissionRole(Map<RoleIdentifier, Role> rolesMap) {
		rolesMap.putIfAbsent(NO_PERMISSION, new Role(NO_PERMISSION));
	}

}
