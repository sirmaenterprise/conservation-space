package com.sirma.itt.seip.resources.synchronization;

import static java.util.stream.Collectors.toMap;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Properties;
import java.util.function.Function;
import java.util.stream.Collectors;

import javax.annotation.PostConstruct;
import javax.inject.Inject;

import com.sirma.itt.seip.io.ResourceLoadUtil;
import com.sirma.itt.seip.plugin.Extension;
import com.sirma.itt.seip.resources.EmfGroup;
import com.sirma.itt.seip.resources.EmfResource;
import com.sirma.itt.seip.resources.Group;
import com.sirma.itt.seip.resources.RemoteUserStoreAdapter;
import com.sirma.itt.seip.resources.Resource;
import com.sirma.itt.seip.resources.User;
import com.sirma.itt.seip.resources.adapter.CMFGroupService;
import com.sirma.itt.seip.resources.adapter.CMFUserService;

/**
 * Class used to perform the remote user and group access
 *
 * @author <a href="mailto:borislav.bonev@sirma.bg">Borislav Bonev</a>
 * @since 01/08/2017
 */
@Extension(target = RemoteUserStoreAdapter.NAME, order = 100)
public class AlfrescoRemoteUserStoreAdapter implements RemoteUserStoreAdapter {

	@Inject
	private CMFUserService userService;
	@Inject
	private CMFGroupService groupService;

	private Properties claimMappings;

	@PostConstruct
	void init() {
		claimMappings = ResourceLoadUtil.loadProperties("claimMapping.properties", getClass());
	}

	@Override
	public boolean isExistingUser(String userId) {
		return userService.findUser(userId) != null;
	}

	@Override
	public boolean isExistingGroup(String groupId) {
		return groupService.findGroup(groupId) != null;
	}

	@Override
	public Optional<User> getUserData(String userId) {
		return Optional.ofNullable(userService.findUser(userId));
	}

	@Override
	public Collection<User> getAllUsers() {
		return userService.getAllUsers();
	}

	@Override
	public Collection<Group> getAllGroups() {
		return groupService.getAllGroups();
	}

	@Override
	public Collection<String> getUsersInGroup(String groupId) {
		Group group = new EmfGroup(groupId, null);
		return groupService.getUsersInAuthority(group);
	}

	@Override
	public Collection<String> getGroupsOfUser(String userId) {
		Resource resource = new EmfResource();
		resource.setName(userId);
		return groupService.getAuthorities(resource).stream().map(Group::getName).collect(Collectors.toList());
	}

	@Override
	public String getUserClaim(String userId, String claimUri) {
		return getUserData(userId)
				.map(user -> user.getAsString(claimMappings.getProperty(claimUri, claimUri)))
					.orElse(null);
	}

	@Override
	public Map<String, String> getUserClaims(String userId, String... claimUris) {
		if (userId == null || claimUris == null || claimUris.length == 0) {
			return Collections.emptyMap();
		}
		Map<String, String> propertiesToClaim = Arrays.stream(claimUris).filter(claimMappings::containsKey).collect(
				Collectors.toMap(claimMappings::getProperty, Function.identity()));
		return getUserData(userId)
				.map(user -> user
						.getProperties()
							.entrySet()
							.stream()
							.filter(entry -> entry.getValue() != null)
							.filter(entry -> propertiesToClaim.containsKey(entry.getKey()))
							.collect(toMap(e -> propertiesToClaim.get(e.getKey()), e -> e.getValue().toString())))
					.orElse(Collections.emptyMap());
	}

	@Override
	public void createUser(User user) {
		throw new UnsupportedOperationException();
	}

	@Override
	public void updateUser(User user) {
		throw new UnsupportedOperationException();
	}

	@Override
	public void deleteUser(String userId) {
		throw new UnsupportedOperationException();
	}

	@Override
	public void createGroup(Group group) {
		throw new UnsupportedOperationException();
	}

	@Override
	public void deleteGroup(String groupId) {
		throw new UnsupportedOperationException();
	}

	@Override
	public void updateGroupsOfUser(String userId, List<String> groupsToRemove, List<String> groupsToAdd) {
		throw new UnsupportedOperationException();
	}

	@Override
	public void updateUsersInGroup(String groupId, List<String> usersToRemove, List<String> usersToAdd) {
		throw new UnsupportedOperationException();
	}

	@Override
	public boolean isReadOnly() {
		return true;
	}

	@Override
	public boolean isGroupInGroupSupported() {
		return true;
	}

	@Override
	public String getName() {
		return "alfresco";
	}
}
