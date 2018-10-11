package com.sirma.itt.seip.resources.synchronization;

import static com.sirma.itt.seip.util.EqualsHelper.diffCollections;
import static java.util.stream.Collectors.toList;

import java.io.Serializable;
import java.util.Collection;
import java.util.Set;
import java.util.function.Consumer;
import java.util.stream.Collectors;

import javax.inject.Inject;

import com.sirma.itt.seip.Pair;
import com.sirma.itt.seip.exception.RollbackedRuntimeException;
import com.sirma.itt.seip.plugin.Extension;
import com.sirma.itt.seip.resources.RemoteStoreException;
import com.sirma.itt.seip.resources.RemoteUserStoreAdapter;
import com.sirma.itt.seip.resources.Resource;
import com.sirma.itt.seip.resources.ResourceService;
import com.sirma.itt.seip.resources.ResourceType;
import com.sirma.itt.seip.synchronization.SyncRuntimeConfiguration;
import com.sirma.itt.seip.synchronization.SynchronizationConfiguration;
import com.sirma.itt.seip.synchronization.SynchronizationDataProvider;
import com.sirma.itt.seip.synchronization.SynchronizationProvider;
import com.sirma.itt.seip.synchronization.SynchronizationResult;
import com.sirma.itt.seip.tx.TransactionSupport;

/**
 * Synchronization configuration for group members coming from the configured remote provider. The implementation will
 * use the current configured implementation of the {@link RemoteUserStoreAdapter} to perform the synchronization.
 *
 * @author <a href="mailto:borislav.bonev@sirma.bg">Borislav Bonev</a>
 * @since 31/07/2017
 */
@Extension(target = SynchronizationConfiguration.PLUGIN_NAME, order = 6)
public class RemoteGroupMembersSyncProvider implements SynchronizationConfiguration<String, GroupInfo> {

	public static final String NAME = "remoteGroupMembersSyncProvider";

	@Inject
	protected ResourceService resourceService;
	@Inject
	private TransactionSupport transactionSupport;
	@Inject
	private RemoteUserStoreAdapter remoteUserStore;

	@Override
	public String getName() {
		return NAME;
	}

	@Override
	public SynchronizationDataProvider<String, GroupInfo> getDestination() {
		return SynchronizationDataProvider.create(loadLocalGroupMembers(), GroupInfo::getGroupName);
	}

	private SynchronizationProvider<Collection<GroupInfo>> loadLocalGroupMembers() {
		return () -> resourceService.getAllGroups()
				.stream()
				.map(resource -> {
					GroupInfo info = new GroupInfo();
					info.setSystemGroupId((String) resource.getId());
					info.setGroupName(resource.getName());
					getGroupMembers(resource.getId(), info::addMember);
					return info;
				}).collect(Collectors.toList());
	}

	private void getGroupMembers(Serializable resourceId, Consumer<String> memberConsumer) {
		boolean groupInGroupSupported = remoteUserStore.isGroupInGroupSupported();
		resourceService
				.getContainedResources(resourceId)
				.stream()
				.map(Resource.class::cast)
				.filter(resource -> groupInGroupSupported || resource.getType() == ResourceType.USER)
				.map(Resource::getName)
				.forEach(memberConsumer);
	}

	@Override
	public void save(SynchronizationResult<String, GroupInfo> result, SyncRuntimeConfiguration runtimeConfiguration) {
		transactionSupport.invokeConsumerInTx(this::saveChangesInTx, result);
	}

	/**
	 * Perform the actual saving of the found group members changes. The method will be called in its own transaction
	 *
	 * @param result synchronization result to write
	 */
	protected void saveChangesInTx(SynchronizationResult<String, GroupInfo> result) {
		for (GroupInfo info : result.getModified().values()) {
			Pair<Set<String>, Set<String>> diff = diffCollections(info.getNewMembers(), info.getCurrentMembers());
			Set<String> newMembers = diff.getFirst();
			Set<String> membersToRemove = diff.getSecond();
			resourceService.modifyMembers(resourceService.findResource(info.getSystemGroupId()), newMembers,
					membersToRemove);
		}
	}

	@Override
	public SynchronizationDataProvider<String, GroupInfo> getSource() {
		return SynchronizationDataProvider.create(loadExternalMapping(), GroupInfo::getGroupName);
	}

	@Override
	public boolean isMergeSupported() {
		return true;
	}

	@Override
	public GroupInfo merge(GroupInfo currentInfo, GroupInfo newInfo) {
		GroupInfo merged = new GroupInfo();
		merged.setGroupName(currentInfo.getGroupName());
		merged.setSystemGroupId(currentInfo.getSystemGroupId());
		merged.getCurrentMembers().addAll(currentInfo.getMemberNames());
		merged.getNewMembers().addAll(newInfo.getMemberNames());
		return merged;
	}

	/**
	 * Build a provider that will do the actual loading of the external resources for compare.
	 *
	 * @return provider to do the actual loading
	 */
	protected SynchronizationProvider<Collection<GroupInfo>> loadExternalMapping() {
		return () -> resourceService.getAllGroups()
				.stream()
				.map(group -> {
					GroupInfo info = new GroupInfo();
					info.setGroupName(group.getName());
					info.setSystemGroupId((String) group.getId());
					loadGroupMembers(group.getName(), info::addMember);
					return info;
				}).collect(toList());
	}

	/**
	 * Load the group member identifiers and pass them to the given consumer
	 *
	 * @param groupName to query
	 * @param groupMemberConsumer the consumer to call to accept each found member
	 */
	protected void loadGroupMembers(String groupName, Consumer<String> groupMemberConsumer) {
		try {
			remoteUserStore.getUsersInGroup(groupName).forEach(groupMemberConsumer);
		} catch (RemoteStoreException e) {
			throw new RollbackedRuntimeException(e);
		}
	}

}
