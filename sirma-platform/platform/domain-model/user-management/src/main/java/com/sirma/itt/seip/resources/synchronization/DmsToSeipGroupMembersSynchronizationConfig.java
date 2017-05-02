package com.sirma.itt.seip.resources.synchronization;

import static com.sirma.itt.seip.util.EqualsHelper.diffCollections;
import static com.sirma.itt.seip.util.EqualsHelper.nullSafeEquals;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import javax.enterprise.inject.Instance;
import javax.inject.Inject;

import com.sirma.itt.seip.Pair;
import com.sirma.itt.seip.plugin.Extension;
import com.sirma.itt.seip.resources.Group;
import com.sirma.itt.seip.resources.Resource;
import com.sirma.itt.seip.resources.ResourceService;
import com.sirma.itt.seip.resources.ResourceType;
import com.sirma.itt.seip.resources.adapter.CMFGroupService;
import com.sirma.itt.seip.resources.synchronization.DmsToSeipGroupMembersSynchronizationConfig.GroupInfo;
import com.sirma.itt.seip.synchronization.SyncRuntimeConfiguration;
import com.sirma.itt.seip.synchronization.SynchronizationConfiguration;
import com.sirma.itt.seip.synchronization.SynchronizationDataProvider;
import com.sirma.itt.seip.synchronization.SynchronizationException;
import com.sirma.itt.seip.synchronization.SynchronizationProvider;
import com.sirma.itt.seip.synchronization.SynchronizationResult;
import com.sirma.itt.seip.tx.TransactionSupport;

/**
 * Synchronization configuration for group members coming from Alfresco 4 installation
 *
 * @author BBonev
 */
@Extension(target = SynchronizationConfiguration.PLUGIN_NAME, order = 7)
public class DmsToSeipGroupMembersSynchronizationConfig implements SynchronizationConfiguration<String, GroupInfo> {

	public static final String NAME = "alfresco4ToSeipGroupMembers";

	@Inject
	private ResourceService resourceService;
	@Inject
	private TransactionSupport transactionSupport;
	@Inject
	private Instance<CMFGroupService> groupService;

	@Override
	public String getName() {
		return NAME;
	}

	@Override
	public SynchronizationDataProvider<String, GroupInfo> getDestination() {
		return SynchronizationDataProvider.create(loadLocalGroupMembers(), GroupInfo::getGroupName);
	}

	private SynchronizationProvider<Collection<? extends GroupInfo>> loadLocalGroupMembers() {
		ResourceService resources = resourceService;
		return () -> {
			List<Resource> groups = resources.getAllResources(ResourceType.GROUP, null);
			List<GroupInfo> groupInfos = new ArrayList<>(groups.size());
			for (Resource resource : groups) {
				GroupInfo info = new GroupInfo();
				info.systemGroupId = (String) resource.getId();
				info.groupName = resource.getName();

				Set<String> members = resources
						.getContainedResources(resource.getId())
							.stream()
							.map(Resource.class::cast)
							.map(Resource::getName)
							.collect(Collectors.toSet());
				info.memberNames.addAll(members);
				groupInfos.add(info);
			}
			return groupInfos;
		};
	}

	@Override
	public void save(SynchronizationResult<String, GroupInfo> result, SyncRuntimeConfiguration runtimeConfiguration) {
		transactionSupport.invokeConsumerInTx(this::saveChangesInTx, result);
	}

	void saveChangesInTx(SynchronizationResult<String, GroupInfo> result) {
		Collection<GroupInfo> modified = result.getModified().values();

		for (GroupInfo info : modified) {
			Pair<Set<String>, Set<String>> diff = diffCollections(info.newMembers, info.currentMembers);
			Set<String> newMembers = diff.getFirst();
			Set<String> membersToRemove = diff.getSecond();
			resourceService.modifyMembers(resourceService.findResource(info.systemGroupId), newMembers,
					membersToRemove);
		}
	}

	@Override
	public SynchronizationDataProvider<String, GroupInfo> getSource() {
		return SynchronizationDataProvider.create(loadExternalMapping(), GroupInfo::getGroupName);
	}

	private SynchronizationProvider<Collection<? extends GroupInfo>> loadExternalMapping() {
		ResourceService resources = resourceService;
		CMFGroupService service = groupService.get();
		return () -> {
			List<Resource> groups = resources.getAllResources(ResourceType.GROUP, null);
			List<GroupInfo> groupInfos = new ArrayList<>(groups.size());
			for (Resource resource : groups) {
				GroupInfo info = new GroupInfo();
				info.systemGroupId = (String) resource.getId();
				info.groupName = resource.getName();

				try {
					info.memberNames.addAll(service.getUsersInAuthority((Group) resource));
				} catch (Exception e) {
					throw new SynchronizationException(e);
				}
				groupInfos.add(info);
			}
			return groupInfos;
		};
	}

	@Override
	public boolean isMergeSupported() {
		return true;
	}

	@Override
	public GroupInfo merge(GroupInfo currentInfo, GroupInfo newInfo) {
		GroupInfo merged = new GroupInfo();
		merged.groupName = currentInfo.groupName;
		merged.systemGroupId = currentInfo.systemGroupId;
		merged.currentMembers.addAll(currentInfo.memberNames);
		merged.newMembers.addAll(newInfo.memberNames);
		return merged;
	}

	/**
	 * Represents a group information and it's members.
	 *
	 * @author BBonev
	 */
	public static class GroupInfo {
		String systemGroupId;
		String groupName;
		Set<String> memberNames = new HashSet<>();

		Set<String> currentMembers = new HashSet<>();
		Set<String> newMembers = new HashSet<>();

		/**
		 * Gets the group name.
		 *
		 * @return the group name
		 */
		String getGroupName() {
			return groupName;
		}

		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result + (groupName == null ? 0 : groupName.hashCode());
			result = prime * result + (memberNames == null ? 0 : memberNames.hashCode());
			return result;
		}

		@Override
		public boolean equals(Object obj) {
			if (this == obj) {
				return true;
			}
			if (!(obj instanceof GroupInfo)) {
				return false;
			}
			GroupInfo other = (GroupInfo) obj;
			return nullSafeEquals(groupName, other.groupName) && memberNames.equals(other.memberNames);
		}

	}
}
