package com.sirma.itt.seip.resources;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import com.sirma.itt.seip.Named;
import com.sirma.itt.seip.plugin.Plugin;

/**
 * Adapter for the underlying user store. It could be LDAP, Identity Server, Active Directory, etc.
 * <p>
 * <p>
 * Contains operations about users and groups, like fetching, creating, deleting, etc.
 * <p>
 * <p>
 * Important aspect is that the store can be read only then modification should not be allowed.
 *
 * @author smustafov
 */
public interface RemoteUserStoreAdapter extends Plugin, Named {

	String NAME = "remoteUserStoreAdapter";

	/**
	 * Checks if user with the given identifier exists in the current remote store
	 *
	 * @param userId the user name to check
	 * @return true if user with the given name exists
	 * @throws RemoteStoreException if cannot check if the user exists
	 */
	boolean isExistingUser(String userId) throws RemoteStoreException;

	/**
	 * Checks if group with the given identifier exists in the current remote store
	 *
	 * @param groupId name to be checked in the remote store
	 * @return true if the group exists
	 * @throws RemoteStoreException if cannot check if the group exists
	 */
	boolean isExistingGroup(String groupId) throws RemoteStoreException;

	/**
	 * Fetch all known data for the given user identified by the given user name
	 *
	 * @param userId the user name that identifies the needed information
	 * @return Optional user instance containing all properties. If the user does not exists an empty optional should
	 * be returned.
	 * @throws RemoteStoreException if there is a problem while extracting user data
	 */
	Optional<User> getUserData(String userId) throws RemoteStoreException;

	/**
	 * Get all users with their information
	 *
	 * @return get all users and their data
	 * @throws RemoteStoreException if cannot read all users or any of their data
	 */
	Collection<User> getAllUsers() throws RemoteStoreException;

	/**
	 * Get all groups with their information
	 *
	 * @return get all groups and their data
	 * @throws RemoteStoreException if cannot read all groups or any of their data
	 */
	Collection<Group> getAllGroups() throws RemoteStoreException;

	/**
	 * Returns a list of the members for the given group. Note that if {@link #isGroupInGroupSupported()} returns true
	 * the method response may contains groups and users. If {@link #isGroupInGroupSupported()} the response should
	 * contains only user names.
	 *
	 * @param groupId the group to fetch it's members
	 * @return the groups members
	 * @throws RemoteStoreException if cannot access the remote store to fetch the group members
	 */
	Collection<String> getUsersInGroup(String groupId) throws RemoteStoreException;

	/**
	 * Get the groups that have thr given user as their member
	 *
	 * @param userId the user to check
	 * @return the group identifiers that contain the given user or empty collection if it's not part of any groups
	 * @throws RemoteStoreException if cannot access the remote store to fetch the containing groups
	 */
	Collection<String> getGroupsOfUser(String userId) throws RemoteStoreException;

	/**
	 * Get single user property.
	 *
	 * @param userId the user name to get for
	 * @param claimUri the claim name to fetch
	 * @return returns the user property or null
	 * @throws RemoteStoreException if cannot access the remote store or the requested claim id is not valid
	 */
	String getUserClaim(String userId, String claimUri) throws RemoteStoreException;

	/**
	 * Get multiple claim properties for given user
	 *
	 * @param userId the requested user name
	 * @param claimUris the list of claim names to fetch
	 * @return the found claim values. The map keys is the requested claim URI and their corresponding values. If a
	 * claim is missing a value it may not be present in the result map.
	 * @throws RemoteStoreException if cannot fetch the user claims or there is invalid claim URI
	 */
	Map<String, String> getUserClaims(String userId, String... claimUris) throws RemoteStoreException;

	/**
	 * Creates the given user in the remote store if the store is not {@link #isReadOnly()}.
	 *
	 * @param user the user data to set for the new user
	 * @throws RemoteStoreException if failed to create
	 * @throws ReadOnlyStoreException if called on read only store
	 */
	void createUser(User user) throws RemoteStoreException;

	/**
	 * Updates the given user in the remote store if the store is not {@link #isReadOnly()}.
	 *
	 * @param user
	 *            the user to update
	 * @throws RemoteStoreException
	 *             if failed to update user
	 * @throws ReadOnlyStoreException
	 *             if called on read only store
	 */
	void updateUser(User user) throws RemoteStoreException;

	/**
	 * Delete user from a remote store. User and it's details should be removed and no longer accessible. it should also
	 * close any running user sessions.
	 *
	 * @param userId the user name of the client that should be deleted
	 * @throws RemoteStoreException if cannot delete the given user
	 * @throws ReadOnlyStoreException if called on read only store
	 */
	void deleteUser(String userId) throws RemoteStoreException;

	/**
	 * Create new group in the current remote store.
	 *
	 * @param group the group to create
	 * @throws RemoteStoreException if cannot create the given group
	 * @throws ReadOnlyStoreException if called on read only store
	 */
	void createGroup(Group group) throws RemoteStoreException;

	/**
	 * Deletes the specified group identified by the given name
	 *
	 * @param groupId the group name to delete
	 * @throws RemoteStoreException if cannot delete the group
	 * @throws ReadOnlyStoreException if called on read only store
	 */
	void deleteGroup(String groupId) throws RemoteStoreException;

	/**
	 * Update a user membership to groups. The operation is additive, so if the method is called with empty lists the will be no
	 * affect on the remote store
	 *
	 * @param userId the affected user name
	 * @param groupsToRemove the list of groups where the user should be removed from
	 * @param groupsToAdd the list of groups where the user should be added to
	 * @throws RemoteStoreException if failed to communicate with the remote store
	 * @throws ReadOnlyStoreException if called on read only store
	 */
	void updateGroupsOfUser(String userId, List<String> groupsToRemove, List<String> groupsToAdd) throws
			RemoteStoreException;

	/**
	 * Update group members. The operation is additive, so if the method is called with empty lists the will be no
	 * affect on the remote store
	 *
	 * @param groupId the affected group
	 * @param usersToRemove list of group members to add to a group
	 * @param usersToAdd list of group members to remove from a group
	 * @throws RemoteStoreException if cannot update the users' membership
	 * @throws ReadOnlyStoreException if called on read only store
	 */
	void updateUsersInGroup(String groupId, List<String> usersToRemove, List<String> usersToAdd) throws
			RemoteStoreException;

	/**
	 * Returns whether the underlying user store is configured with read only rights.
	 *
	 * @return true if the underlying user store has read only rights
	 */
	boolean isReadOnly() throws RemoteStoreException;

	/**
	 * Returns if current store supports groups to be members of other groups
	 *
	 * @return true if a group could be a member of other group
	 */
	boolean isGroupInGroupSupported();

}
