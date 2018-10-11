package com.sirmaenterprise.sep.roles;

import static com.sirma.itt.seip.util.EqualsHelper.getOrDefault;
import static com.sirma.itt.seip.util.EqualsHelper.nullSafeEquals;

import java.io.Serializable;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import javax.inject.Inject;
import javax.inject.Singleton;

import org.apache.commons.lang3.StringUtils;

import com.sirma.itt.seip.collections.CollectionUtils;
import com.sirma.itt.seip.definition.jaxb.Definition;
import com.sirma.itt.seip.domain.definition.label.LabelProvider;
import com.sirma.itt.seip.domain.exceptions.DefinitionValidationException;
import com.sirma.itt.seip.domain.security.Action;
import com.sirma.itt.seip.mapping.ObjectMapper;
import com.sirma.itt.seip.permissions.BaseDefinitionActionProvider;
import com.sirma.itt.seip.permissions.action.EmfAction;
import com.sirma.itt.seip.permissions.action.external.Actions;
import com.sirma.itt.seip.permissions.model.RoleInstance;
import com.sirma.itt.seip.permissions.role.Role;
import com.sirma.itt.seip.permissions.role.RoleIdentifier;
import com.sirma.itt.seip.permissions.role.RoleProviderExtension;
import com.sirmaenterprise.sep.roles.jaxb.RoleDefinition;


/**
 * Parser class that can read external role definitions, to convert them to internal intermediate model and to add them
 * to the internal model. It can also work directly with the intermediate model so it could be used in REST services to
 * validate the model changes.
 *
 * @author BBonev
 */
@Singleton
public class ExternalRoleParser {
	private static final Comparator<RoleInstance> ROLE_COMPARATOR = new RoleComparator();
	@Inject
	private ObjectMapper mapper;
	@Inject
	private LabelProvider labelProvider;

	/**
	 * Process external roles by first converting them to intermediate model. After conversion the method
	 * {@link #validateAndAddToInternalModel(Map, List)} is called.
	 *
	 * @param chainedRoles
	 *            the chained roles
	 * @param roleDefinitions
	 *            the role definitions
	 * @return the map
	 */
	public Map<RoleIdentifier, Role> processExternalRoles(Map<RoleIdentifier, Role> chainedRoles,
			List<RoleDefinition> roleDefinitions) {
		List<RoleInstance> converted = convertToInternalModel(roleDefinitions);

		return validateAndAddToInternalModel(chainedRoles, converted);
	}

	/**
	 * Validate the given list of role instances and add them the to the provided model list.
	 *
	 * @param chainedRoles
	 *            the chained roles
	 * @param converted
	 *            the converted
	 * @return the map
	 */
	public Map<RoleIdentifier, Role> validateAndAddToInternalModel(Map<RoleIdentifier, Role> chainedRoles,
			List<RoleInstance> converted) {
		validateModel(converted);

		performIncludes(converted);

		addRoleData(chainedRoles, converted);

		return chainedRoles;
	}

	/**
	 * Converts the provided jaxb definitions list to internal model - {@link Role} and enriches the provided mapping
	 * with it.
	 * 
	 * @param rolesMap is the roles mapping where to add the converted {@link Role}
	 * @param definitions the list of input {@link Definition}
	 * @return the mapping
	 */
	public Map<RoleIdentifier, Role> addToInternalModel(Map<RoleIdentifier, Role> rolesMap,
			List<RoleDefinition> definitions) {
		List<RoleInstance> converted = convertToInternalModel(definitions);
		performIncludes(converted);
		addRoleData(rolesMap, converted);

		return rolesMap;
	}

	/**
	 * Include roles in corresponding instances
	 *
	 * @param converted
	 *            the converted external model
	 */
	private void performIncludes(List<RoleInstance> converted) {
		int size = converted.size();
		Map<String, RoleInstance> mappedExternal = CollectionUtils.createLinkedHashMap(size);
		Map<String, RoleInstance> mappedInternal = CollectionUtils.createLinkedHashMap(size);

		for (RoleInstance instance : converted) {
			if (instance.isExternal()) {
				mappedExternal.put(instance.getRoleId().getIdentifier(), instance);
			} else {
				mappedInternal.put(instance.getRoleId().getIdentifier(), instance);
			}
		}

		for (RoleInstance roleInstance : converted) {
			performIncludes(roleInstance, mappedExternal, mappedInternal);
		}
	}

	/**
	 * Perform includes on a single instance.
	 *
	 * @param roleInstance
	 *            the role instance
	 * @param mappedExternal
	 *            the mapped external
	 * @param mappedInternal
	 *            the mapped internal
	 */
	private void performIncludes(RoleInstance roleInstance, Map<String, RoleInstance> mappedExternal,
			Map<String, RoleInstance> mappedInternal) {
		List<String> includes = roleInstance.getInclude();
		if (includes == null) {
			return;
		}
		for (String include : includes) {
			String[] split = include.split("/");
			String roleId = split[0];
			String permissionId = null;
			if (split.length == 2) {
				permissionId = split[1];
			}
			procesInclude(roleInstance, mappedExternal, mappedInternal, roleId, permissionId);
		}
	}

	private void procesInclude(RoleInstance roleInstance, Map<String, RoleInstance> mappedExternal,
			Map<String, RoleInstance> mappedInternal, String roleId, String permissionId) {
		RoleInstance depedency = mappedInternal.get(roleId);
		if (depedency == null && roleInstance.isExternal()) {
			depedency = mappedExternal.get(roleId);
		}
		if (depedency == null) {
			// this should not happen because of the validation
			throw new DefinitionValidationException("Could not find dependecy " + roleId);
		}
		// go the the last instance that does not have an include
		performIncludes(depedency, mappedExternal, mappedInternal);

		List<com.sirma.itt.seip.permissions.model.Permission> addedPermissions = new ArrayList<>(
				depedency.getPermissions());
		if (permissionId != null) {
			List<com.sirma.itt.seip.permissions.model.Permission> list = depedency.getPermissions()
					.stream()
					.filter(permission -> nullSafeEquals(permissionId, permission.getPermissionId(), true))
					.collect(Collectors.toList());
			if (!list.isEmpty()) {
				addedPermissions = list;
			}
		}

		addPermissions(roleInstance, addedPermissions);
	}

	/**
	 * Adds the permissions.
	 *
	 * @param roleInstance
	 *            the role instance
	 * @param addedPermissions
	 *            the added permissions
	 */
	private static void addPermissions(RoleInstance roleInstance,
			List<com.sirma.itt.seip.permissions.model.Permission> addedPermissions) {

		List<com.sirma.itt.seip.permissions.model.Permission> updated = new LinkedList<>(roleInstance.getPermissions());
		for (com.sirma.itt.seip.permissions.model.Permission permission : addedPermissions) {
			boolean found = false;
			for (com.sirma.itt.seip.permissions.model.Permission currentPermission : roleInstance.getPermissions()) {
				if (currentPermission.equals(permission)) {
					currentPermission.setActions(mergeActions(currentPermission.getActions(), permission.getActions()));
					found = true;
					break;
				}
			}
			if (!found) {
				updated.add(permission.createCopy());
			}
		}
		roleInstance.setPermissions(updated);

	}

	/**
	 * The method merges the 2 lists into one and updates any matching actions
	 *
	 * @param actions
	 *            the current list of actions to update
	 * @param actionsToAdd
	 *            the actions to add
	 * @return new updated list
	 */
	private static List<Actions> mergeActions(List<Actions> actions, List<Actions> actionsToAdd) {
		List<Actions> result = new LinkedList<>(actions);
		result.addAll(actionsToAdd);
		return result;
	}

	/**
	 * Validate the provided model. The model is check for missing dependencies and for circular dependencies
	 *
	 * @param converted
	 *            the converted
	 */
	private void validateModel(List<RoleInstance> converted) {
		Map<String, RoleInstance> mappedExternal = CollectionUtils.createLinkedHashMap(converted.size());
		Map<String, RoleInstance> mappedInternal = CollectionUtils.createLinkedHashMap(converted.size());

		for (RoleInstance instance : converted) {
			if (instance.isExternal()) {
				addToMappedModel(mappedExternal, instance);
			} else {
				addToMappedModel(mappedInternal, instance);
			}
		}

		Collections.sort(converted, ROLE_COMPARATOR);

		validateForCycles(converted, mappedExternal, mappedInternal);
	}

	/**
	 * Validate for cycles.
	 *
	 * @param converted
	 *            the converted
	 * @param mappedExternal
	 *            the mapped external
	 * @param mappedInternal
	 *            the mapped internal
	 */
	private void validateForCycles(List<RoleInstance> converted, Map<String, RoleInstance> mappedExternal,
			Map<String, RoleInstance> mappedInternal) {

		for (RoleInstance roleInstance : converted) {
			Collection<String> dependecyChain = new LinkedList<>();
			dependecyChain.add(roleInstance.getRoleId().getIdentifier());
			checkRoleDependencies(roleInstance, mappedExternal, mappedInternal, dependecyChain);
		}
	}

	/**
	 * Check role dependencies.
	 *
	 * @param roleInstance
	 *            the role instance
	 * @param mappedExternal
	 *            the mapped external
	 * @param mappedInternal
	 *            the mapped internal
	 * @param dependencyChain
	 *            the dependency chain
	 */
	private void checkRoleDependencies(RoleInstance roleInstance, Map<String, RoleInstance> mappedExternal,
			Map<String, RoleInstance> mappedInternal, Collection<String> dependencyChain) {
		for (String include : roleInstance.getInclude()) {
			// create new sub chain to test each dependency branch
			Collection<String> localChain = new LinkedList<>(dependencyChain);

			String[] split = include.split("/");
			String roleId = split[0];
			if (roleInstance.getRoleId().getIdentifier().equalsIgnoreCase(roleId)
					&& checkForMatchingRole(roleId, roleInstance.isExternal(), mappedExternal, mappedInternal)) {
				throw new DefinitionValidationException(
						"Found internal role " + roleInstance.getRoleId() + " to depend on itself");
			}
			if (localChain.contains(roleId)) {
				checkForCircularDependencies(roleInstance, roleId, mappedExternal, mappedInternal, localChain);
			}
			localChain.add(roleId);

			RoleInstance dependecy;
			if (roleInstance.isExternal()) {
				dependecy = mappedInternal.get(roleId);
				if (dependecy == null) {
					dependecy = mappedExternal.get(roleId);
				}
			} else {
				dependecy = mappedInternal.get(roleId);
			}

			if (dependecy == null) {
				throw new DefinitionValidationException("Found role " + roleInstance.getRoleId()
						+ " to depend on missing role " + roleId + ". Dependency chain is: " + localChain);
			}

			checkRoleDependencies(dependecy, mappedExternal, mappedInternal, localChain);
		}
	}

	/**
	 * Check for circular dependencies.
	 *
	 * @param roleInstance
	 *            the role instance
	 * @param roleId
	 *            the role id
	 * @param mappedExternal
	 *            the mapped external
	 * @param mappedInternal
	 *            the mapped internal
	 * @param localChain
	 *            the local chain
	 */
	private static void checkForCircularDependencies(RoleInstance roleInstance, String roleId,
			Map<String, RoleInstance> mappedExternal, Map<String, RoleInstance> mappedInternal,
			Collection<String> localChain) {
		if (roleInstance.isExternal()) {
			// external role could depend only on internal roles
			if (mappedExternal.containsKey(roleId) && !mappedInternal.containsKey(roleId)) {
				localChain.add(roleId);
				throw new DefinitionValidationException(MessageFormat.format(
						"Found external role {0} to depend on a role with same id causing circular dependecies in dependecy chain {1}",
						roleInstance.getRoleId(),
						localChain));
			}
		} else {
			if (mappedExternal.containsKey(roleId) && !mappedInternal.containsKey(roleId)) {
				// internal roles could not depend on external roles
				localChain.add(roleId);
				throw new DefinitionValidationException(MessageFormat.format(
						"Found internal role {0} to depend on external role {1} causing circular dependecies in dependecy chain {2}",
						roleInstance.getRoleId(),
						roleId,
						localChain));
			} else if (mappedInternal.containsKey(roleId)) {
				localChain.add(roleId);
				throw new DefinitionValidationException(MessageFormat.format(
						"Found internal role {0} to depend on other internal role {1} causing circular dependecies in dependecy chain {2}",
						roleInstance.getRoleId(),
						roleId,
						localChain));
			}
		}
	}

	/**
	 * Check for matching role depending on the boolean value. If <code>true</code> then the key will be matched using
	 * the first map and <code>false</code> against the second one.
	 *
	 * @param key
	 *            the key to check
	 * @param external
	 *            the external
	 * @param mappedExternal
	 *            the mapped external
	 * @param mappedInternal
	 *            the mapped internal
	 * @return true, if successful
	 */
	private static boolean checkForMatchingRole(String key, boolean external, Map<String, RoleInstance> mappedExternal,
			Map<String, RoleInstance> mappedInternal) {
		return external ? mappedExternal.containsKey(key) : mappedInternal.containsKey(key);
	}

	/**
	 * Adds the to mapped model.
	 *
	 * @param mapped
	 *            the mapping to store the given role instance
	 * @param instance
	 *            the instance
	 */
	private static void addToMappedModel(Map<String, RoleInstance> mapped, RoleInstance instance) {
		String identifier = instance.getRoleId().getIdentifier();
		RoleInstance roleInstance = mapped.get(identifier);
		if (roleInstance != null) {
			throw new DefinitionValidationException(
					"Found duplicate role definitions with same declaration and scope id=" + roleInstance.getRoleId()
							+ " external=" + roleInstance.isExternal());
		}
		mapped.put(identifier, instance);
	}

	/**
	 * Convert to internal model
	 *
	 * @param roleDefinitions
	 *            the role definitions
	 * @return the list
	 */
	private List<RoleInstance> convertToInternalModel(List<RoleDefinition> roleDefinitions) {
		List<RoleInstance> converted = new ArrayList<>(roleDefinitions.size());
		for (RoleDefinition roleDefinition : roleDefinitions) {
			converted.add(mapper.map(roleDefinition, com.sirma.itt.seip.permissions.model.RoleInstance.class));
		}

		return converted;
	}

	/**
	 * Adds the role data.
	 *
	 * @param chainedRoles
	 *            the chained roles
	 * @param converted
	 *            the role list
	 */
	private void addRoleData(Map<RoleIdentifier, Role> chainedRoles, List<RoleInstance> converted) {
		for (RoleInstance roleInstance : converted) {
			if (!roleInstance.isExternal()) {
				// ignore all internal instance. only external one are considered.
				continue;
			}
			RoleIdentifier roleId = roleInstance.getRoleId();
			Role role = RoleProviderExtension.getOrCreateRole(chainedRoles, roleId);
			for (com.sirma.itt.seip.permissions.model.Permission permissionType : roleInstance.getPermissions()) {
				List<Actions> actions = permissionType.getActions();
				readActions(role, actions);
			}
		}
	}

	/**
	 * Read actions.
	 *
	 * @param role
	 *            the permissions
	 * @param actions
	 *            the actions
	 */
	private void readActions(Role role, List<Actions> actions) {
		for (Actions actionsType : actions) {
			Set<Action> actionsSet = Role.newActionModel();
			actionsSet.addAll(convertExternalActionToPermissionModel(actionsType.getActions()));
			role.addActions(actionsSet);
		}
	}

	private List<Action> convertExternalActionToPermissionModel(
			List<com.sirma.itt.seip.permissions.action.external.Action> actions) {
		List<Action> result = new ArrayList<>(actions.size());
		for (com.sirma.itt.seip.permissions.action.external.Action action : actions) {
			String actionId = action.getBind();
			if (StringUtils.isEmpty(actionId)) {
				actionId = action.getName();
			}
			if (StringUtils.isEmpty(actionId)) {
				continue;
			}
			Action newAction = BaseDefinitionActionProvider.createAction(actionId,
					getOrDefault(StringUtils.trimToNull(action.getBind()), actionId), actionId, labelProvider);
			if (newAction instanceof EmfAction) {
				EmfAction securityAction = (EmfAction) newAction;
				securityAction.setFilters(action.getFilters());
				securityAction.setIdentifier(action.getName());
				securityAction.setLocal(action.isLocal());
			}
			result.add(newAction);
		}
		return result;
	}

	/**
	 * {@link RoleInstance} comparator that keeps the order except if the role ids are equal then checks the external
	 * field and the external roles are before the internal
	 *
	 * @author BBonev
	 */
	private static final class RoleComparator implements Comparator<RoleInstance>, Serializable {
		private static final long serialVersionUID = 3183039559542661858L;

		RoleComparator() {
		}

		@Override
		public int compare(RoleInstance o1, RoleInstance o2) {
			if (o1.getRoleId().equals(o2.getRoleId())) {
				if (o1.isExternal()) {
					return o2.isExternal() ? 0 : 1;
				}
				return o2.isExternal() ? -1 : 0;
			}
			return 0;
		}
	}

}