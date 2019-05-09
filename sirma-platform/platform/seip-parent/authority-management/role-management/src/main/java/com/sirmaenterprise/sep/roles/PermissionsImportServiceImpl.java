package com.sirmaenterprise.sep.roles;

import java.io.File;
import java.lang.invoke.MethodHandles;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.Set;
import java.util.function.Consumer;
import java.util.stream.Collectors;

import javax.enterprise.event.Observes;
import javax.inject.Inject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sirma.itt.seip.definition.event.DefinitionsChangedEvent;
import com.sirma.itt.seip.definition.label.LabelDefinition;
import com.sirma.itt.seip.definition.label.LabelService;
import com.sirma.itt.seip.definition.model.LabelImpl;
import com.sirma.itt.seip.domain.security.Action;
import com.sirma.itt.seip.permissions.Filterable;
import com.sirma.itt.seip.permissions.action.ActionProvider;
import com.sirma.itt.seip.permissions.action.RoleActionFilterService;
import com.sirma.itt.seip.permissions.role.Role;
import com.sirma.itt.seip.permissions.role.RoleIdentifier;
import com.sirma.itt.seip.permissions.role.RoleProviderExtension;
import com.sirma.itt.seip.plugin.ExtensionPoint;
import com.sirma.itt.seip.provider.MapProvider;
import com.sirma.itt.seip.time.TimeTracker;
import com.sirma.itt.seip.util.file.FileUtil;
import com.sirma.sep.xml.JAXBHelper;
import com.sirmaenterprise.sep.roles.jaxb.RoleDefinition;
import com.sirmaenterprise.sep.roles.jaxb.Roles;
import com.sirmaenterprise.sep.roles.validation.RolesValidator;

/**
 * Default implementation of {@link PermissionsImportService}. </br>
 * Validate functionality uses the API provided by {@link RolesValidator}</br>
 * Import functionality retrieves all actions from DB and all roles from the provided directory path. If the actions are
 * still not imported (not present in DB) it creates temporary, disabled records for each action. </br>
 * Provides an observer for definitions reload, and persists the actions when some change in the definitions has been
 * detected.
 *
 * @author Vilizar Tsonev
 */
public class PermissionsImportServiceImpl implements PermissionsImportService {

	private static final Logger LOGGER = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

	@Inject
	private RoleManagement roleManagement;

	@Inject
	private RolesValidator roleValidator;

	@Inject
	private ExternalRoleParser roleParser;

	@Inject
	private LabelService labelService;

	@Inject
	private DefaultRolesProvider defaultRolesProvider;

	@Inject
	private RoleActionFilterService filterService;

	@Inject
	@ExtensionPoint(ActionProvider.TARGET_NAME)
	private Iterable<ActionProvider> actionProviders;

	@Inject
	@ExtensionPoint(value = RoleProviderExtension.TARGET_NAME)
	private Iterable<RoleProviderExtension> roleProviders;

	@Override
	public List<String> validate(String directoryPath) {
		List<File> roleDefinitions = FileUtil.loadFromPath(directoryPath);
		LOGGER.info("Initiating validation of {} permissions definitions", roleDefinitions.size());
		return roleValidator.validate(roleDefinitions);
	}

	@Override
	public void importPermissions(String directoryPath) {
		Map<RoleIdentifier, Role> roleData = getRoles(directoryPath);

		// validate filters after generic definitions are uploaded
		validateActionFilters(roleData.values());

		// first delete current role action mappings
		roleManagement.deleteRoleActionMappings();

		LOGGER.info("Initiating import of {} permission definitions", roleData.size());
		TimeTracker tracker = TimeTracker.createAndStart();

		Map<String, Action> actionsData = getActions();

		List<com.sirmaenterprise.sep.roles.RoleDefinition> roles = new ArrayList<>(roleData.size());
		List<ActionDefinition> actions = new LinkedList<>();
		RoleActionChanges roleActionChanges = new RoleActionChanges();
		Collection<LabelDefinition> actionLabels = new HashSet<>();

		for (Role role : roleData.values()) {
			RoleIdentifier roleId = role.getRoleId();
			roles.add(toRoleDefinition(roleId));
			for (Action current : role.getAllAllowedActions()) {
				// get actions from registers and not from the statically defined in the roles and xmls
				Action action = actionsData.get(current.getActionId());
				if (action != null) {
					actions.add(toActionDefinition(action, !action.isDisabled()));
					roleActionChanges
							.enable(roleId.getIdentifier(), action.getActionId(), Filterable.getFilters(current));
				} else {
					LOGGER.warn(
							"Found action '{}' that is only defined in permission definitions. Going to mark the action as disabled",
							current.getActionId());

					action = current;
					actions.add(toActionDefinition(action, false));
				}
				copyLabelAndTooltip(action, actionLabels::add);
			}
		}

		roleManagement.saveActions(actions);
		roleManagement.saveRoles(roles);
		roleManagement.updateRoleActionMappings(roleActionChanges);
		saveNewActionLabels(actionLabels);
		LOGGER.info("Permission definitions imported for {} ms", tracker.stop());
	}

	private void validateActionFilters(Collection<Role> roles) {
		Set<String> availableFilters = filterService.getFilters();

		for (Role role : roles) {
			for (Action action : role.getAllAllowedActions()) {
				if (action instanceof Filterable) {
					validateActionFilter(availableFilters, ((Filterable) action).getFilters());
				}
			}
		}
	}

	private static void validateActionFilter(Set<String> availableFilters, List<String> filtersFromDefinitions) {
		if (filtersFromDefinitions != null) {
			for (String filter : filtersFromDefinitions) {
				if (!availableFilters.contains(filter)) {
					throw new IllegalArgumentException(
							"Action filter defined in permissions definitions not found. Check your action filter definitions for: "
									+ filter);
				}
			}
		}
	}

	/**
	 * Observes the {@link DefinitionsChangedEvent} event and imports all actions from them.
	 */
	void onDefinitionChange(@Observes DefinitionsChangedEvent event) {
		LOGGER.info("Definitions change detected. Initiating actions import.");
		TimeTracker tracker = TimeTracker.createAndStart();

		List<ActionDefinition> actions = new LinkedList<>();
		Map<String, Action> actionsData = getActions();
		Collection<LabelDefinition> actionLabels = new HashSet<>();

		for (Entry<String, Action> actionEntry : actionsData.entrySet()) {
			Action action = actionEntry.getValue();
			copyLabelAndTooltip(action, actionLabels::add);
			actions.add(toActionDefinition(action, !action.isDisabled()));
		}

		roleManagement.saveActions(actions);
		saveNewActionLabels(actionLabels);
		LOGGER.info("{} Actions imported for {} ms", actions.size(), tracker.stop());
	}

	private void saveNewActionLabels(Collection<LabelDefinition> actionLabels) {
		labelService.saveLabels(new ArrayList<>(actionLabels));
	}

	private static ActionDefinition toActionDefinition(Action action, boolean enabled) {
		return new ActionDefinition()
					.setId(action.getActionId())
					.setActionType(action.getPurpose())
					.setEnabled(enabled)
					.setUserDefined(false);
	}

	private static com.sirmaenterprise.sep.roles.RoleDefinition toRoleDefinition(RoleIdentifier roleId) {
		return new com.sirmaenterprise.sep.roles.RoleDefinition()
					.setId(roleId.getIdentifier())
					.setOrder(roleId.getGlobalPriority())
					.setCanRead(roleId.canRead())
					.setCanWrite(roleId.canWrite())
					.setInternal(roleId.isInternal())
					.setEnabled(true)
					.setUserDefined(false);
	}

	private static String transformLabelId(String oldId, String postfix) {
		return oldId + "." + postfix;
	}

	private void copyLabelAndTooltip(Action action, Consumer<LabelDefinition> labelConsumer) {
		createLabelDef(action.getLabelId(), action.getActionId(), Action.LABEL_KEY).ifPresent(labelConsumer);
		createLabelDef(action.getTooltip(), action.getActionId(), Action.TOOLTIP).ifPresent(labelConsumer);
	}

	private Optional<LabelDefinition> createLabelDef(String originalLabelId, String actionId, String labelSuffix) {
		if (originalLabelId != null) {
			LabelDefinition label = labelService.getLabel(originalLabelId);
			if (label != null) {
				LabelImpl labelCopy = new LabelImpl();
				labelCopy.setIdentifier(transformLabelId(actionId, labelSuffix));
				labelCopy.setLabels(label.getLabels());
				return Optional.of(labelCopy);
			}
		}
		return Optional.empty();
	}

	private Map<RoleIdentifier, Role> getRoles(String path) {
		// step 1 - get the default, system roles to start with
		Map<RoleIdentifier, Role> roleMapping = defaultRolesProvider.getDefaultRoles();
		LOGGER.debug("{} default system roles were auto-created. Proceeding with import...", roleMapping.size());

		// step 2 - load the actual role definitions from the file system and enrich the mapping with them
		List<File> files = FileUtil.loadFromPath(path);
		LOGGER.debug("{} role definitions loaded from file system", files.size());
		List<RoleDefinition> roleDefinitions = files.stream()
					.map(file -> JAXBHelper.load(file, Roles.class))
					.map(Roles::getRole)
					.flatMap(List::stream)
					.collect(Collectors.toList());
		roleParser.addToInternalModel(roleMapping, roleDefinitions);

		// step 3 - post-process - add activity roles to the mapping and finalize it
		roleProviders.forEach(provider -> provider.getModel(roleMapping));
		LOGGER.debug("{} roles will be imported", roleMapping.size());
		return roleMapping;
	}

	private Map<String, Action> getActions() {
		Map<String, Action> result = new HashMap<>();
		for (MapProvider<String, Action> provider : actionProviders) {
			Map<String, Action> data = provider.provide();
			if (data != null) {
				result.putAll(data);
			}
		}
		return result;
	}

}
