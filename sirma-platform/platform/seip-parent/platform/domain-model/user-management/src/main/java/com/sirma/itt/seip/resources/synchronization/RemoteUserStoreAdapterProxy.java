package com.sirma.itt.seip.resources.synchronization;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import javax.annotation.PostConstruct;
import javax.inject.Inject;
import javax.inject.Singleton;

import com.sirma.itt.seip.configuration.ConfigurationProperty;
import com.sirma.itt.seip.configuration.annotation.Configuration;
import com.sirma.itt.seip.configuration.annotation.ConfigurationPropertyDefinition;
import com.sirma.itt.seip.context.Contextual;
import com.sirma.itt.seip.exception.EmfRuntimeException;
import com.sirma.itt.seip.plugin.ExtensionPoint;
import com.sirma.itt.seip.plugin.Plugins;
import com.sirma.itt.seip.resources.Group;
import com.sirma.itt.seip.resources.RemoteStoreException;
import com.sirma.itt.seip.resources.RemoteUserStoreAdapter;
import com.sirma.itt.seip.resources.User;

/**
 * Proxy implementation for {@link RemoteUserStoreAdapter} that uses a configuration that specify what actual
 * implementation to use.
 *
 * @author <a href="mailto:borislav.bonev@sirma.bg">Borislav Bonev</a>
 * @since 31/07/2017
 */
@Singleton
public class RemoteUserStoreAdapterProxy implements RemoteUserStoreAdapter {

	@Inject
	@Configuration
	@ConfigurationPropertyDefinition(name = "synchronization.resource.provider", defaultValue = "wso2Idp", sensitive =
			true, subSystem = "resources",
			label = "The provider name for the user and group source. One of the possible values are: wso2Idp (default), alfresco")
	private ConfigurationProperty<String> synchronizationProviderName;

	@Inject
	private Contextual<RemoteUserStoreAdapter> delegate;
	@Inject
	@ExtensionPoint(RemoteUserStoreAdapter.NAME)
	private Plugins<RemoteUserStoreAdapter> userStoreAdapters;

	@PostConstruct
	private void initialize() {
		delegate.initializeWith(this::resolveUserStore);
		synchronizationProviderName.addConfigurationChangeListener(c -> delegate.reset());
	}

	private RemoteUserStoreAdapter resolveUserStore() {
		return userStoreAdapters.get(synchronizationProviderName.get()).orElseThrow(() -> new EmfRuntimeException("No "
				+ RemoteUserStoreAdapter.class + " implementation with name "
				+ synchronizationProviderName.get() + " could be found"));
	}

	@Override
	public boolean isExistingUser(String userId)  throws RemoteStoreException {
		return getDelegate().isExistingUser(userId);
	}

	@Override
	public boolean isExistingGroup(String groupId) throws RemoteStoreException {
		return getDelegate().isExistingGroup(groupId);
	}

	@Override
	public Optional<User> getUserData(String userId) throws RemoteStoreException {
		return getDelegate().getUserData(userId);
	}

	@Override
	public Collection<User> getAllUsers() throws RemoteStoreException {
		return getDelegate().getAllUsers();
	}

	@Override
	public Collection<Group> getAllGroups() throws RemoteStoreException {
		return getDelegate().getAllGroups();
	}

	@Override
	public Collection<String> getUsersInGroup(String groupId) throws RemoteStoreException {
		return getDelegate().getUsersInGroup(groupId);
	}

	@Override
	public Collection<String> getGroupsOfUser(String userId) throws RemoteStoreException {
		return getDelegate().getUsersInGroup(userId);
	}

	@Override
	public String getUserClaim(String userId, String claimUri) throws RemoteStoreException {
		return getDelegate().getUserClaim(userId, claimUri);
	}

	@Override
	public Map<String, String> getUserClaims(String userId, String... claimUris) throws RemoteStoreException {
		return getDelegate().getUserClaims(userId, claimUris);
	}

	@Override
	public void createUser(User user) throws RemoteStoreException {
		getDelegate().createUser(user);
	}

	@Override
	public void updateUser(User user) throws RemoteStoreException {
		getDelegate().updateUser(user);
	}

	@Override
	public void deleteUser(String userId) throws RemoteStoreException {
		getDelegate().deleteUser(userId);
	}

	@Override
	public void createGroup(Group group) throws RemoteStoreException {
		getDelegate().createGroup(group);
	}

	@Override
	public void deleteGroup(String groupId) throws RemoteStoreException {
		getDelegate().deleteGroup(groupId);
	}

	@Override
	public void updateGroupsOfUser(String userId, List<String> groupsToRemove, List<String> groupsToAdd) throws RemoteStoreException {
		getDelegate().updateGroupsOfUser(userId, groupsToRemove, groupsToAdd);
	}

	@Override
	public void updateUsersInGroup(String groupId, List<String> usersToRemove, List<String> usersToAdd) throws RemoteStoreException {
		getDelegate().updateUsersInGroup(groupId, usersToRemove, usersToAdd);
	}

	@Override
	public boolean isReadOnly() throws RemoteStoreException {
		return getDelegate().isReadOnly();
	}

	@Override
	public boolean isGroupInGroupSupported() {
		return getDelegate().isGroupInGroupSupported();
	}

	@Override
	public String getName() {
		return getDelegate().getName();
	}

	private RemoteUserStoreAdapter getDelegate() {
		return delegate.getContextValue();
	}
}
