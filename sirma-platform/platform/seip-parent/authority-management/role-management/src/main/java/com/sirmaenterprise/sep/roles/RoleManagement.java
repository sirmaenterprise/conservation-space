package com.sirmaenterprise.sep.roles;

import java.util.Collection;
import java.util.Optional;
import java.util.stream.Stream;

/**
 * API for management of {@link RoleDefinition}s, {@link ActionDefinition}s and their mapping.
 * <p>
 * The API provides means of loading and storing the changes to the defined permission roles, user actions and the which
 * actions are enabled in which role.
 *
 * @since 2017-03-27
 * @author <a href="mailto:borislav.bonev@sirma.bg">Borislav Bonev</a>
 */
public interface RoleManagement {

	/**
	 * Stream all role definitions
	 *
	 * @return role definitions stream
	 */
	Stream<RoleDefinition> getRoles();

	/**
	 * Try to find a role based on the given role identifier. Will try to load the role using exact match
	 *
	 * @param roleId
	 *            the to look for
	 * @return the found role or not
	 */
	Optional<RoleDefinition> getRole(String roleId);

	/**
	 * Stream all action definitions. The stream will contain all unique actions known to the system
	 *
	 * @return action definitions stream
	 */
	Stream<ActionDefinition> getActions();

	/**
	 * Try to find an action based on the given action identifier. Will try to load the action using exact match
	 *
	 * @param action
	 *            the action to look for
	 * @return the found action or not
	 */
	Optional<ActionDefinition> getAction(String action);

	/**
	 * Fetches the complete role/actions model. The model will contain information about all roles, actions and their
	 * mapping
	 *
	 * @return a collection of all action mappings
	 */
	RoleActionModel getRoleActionModel();

	/**
	 * Saves changes to roles described by the given role definitions. If such role does not exists it will be created.
	 * If the definitions describe an existing roles they will be updated with the provided data.
	 * <p>
	 * The correct flow of role update is to fetch the current role definitions to apply the changes to where needed and
	 * save the changes.
	 *
	 * <pre>
	 * <code>
	 * RoleManagement service = ...;
	 * # fetch a role instance
	 * RoleDefinition role = service.getRole(roleId).orElseGet(() -> new RoleDefinition().setId(roleId));
	 * # modify the role
	 * role.setEnabled(true);
	 * # save changes
	 * service.saveRoles(Collections.singleton(role));
	 * </code>
	 * </pre>
	 *
	 * @param definitions
	 *            a collection of modified roles to save
	 */
	void saveRoles(Collection<RoleDefinition> definitions);

	/**
	 * Saves changes to actions described by the given action definitions. If such action does not exists it will be
	 * created. If the definitions describe an existing actions they will be updated with the provided data.
	 * <p>
	 * The correct flow of action update is to fetch the current action definition to apply the changes to where needed
	 * and save the changes.
	 *
	 * <pre>
	 * <code>
	 * RoleManagement service = ...;
	 * # fetch a action instance
	 * ActionDefinition action = service.getRole(actionId).orElseGet(() -> new ActionDefinition().setId(actionId));
	 * # modify the action
	 * action.setEnabled(true);
	 * # save changes
	 * service.saveActions(Collections.singleton(action));
	 * </code>
	 * </pre>
	 *
	 * @param definitions
	 *            a collection of modified roles to save
	 */
	void saveActions(Collection<ActionDefinition> definitions);

	/**
	 * Save changes to the role action mappings. If such mapping does not exist it will be created.
	 *
	 * @param changes
	 *            the set of changes to apply to the role action mappings
	 */
	void updateRoleActionMappings(RoleActionChanges changes);

	/**
	 * Deletes all current role action mappings.
	 */
	void deleteRoleActionMappings();

}
