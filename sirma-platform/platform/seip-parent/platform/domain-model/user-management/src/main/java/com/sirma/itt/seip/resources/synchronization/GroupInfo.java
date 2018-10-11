package com.sirma.itt.seip.resources.synchronization;

import static com.sirma.itt.seip.collections.CollectionUtils.addNonNullValue;
import static com.sirma.itt.seip.util.EqualsHelper.nullSafeEquals;

import java.util.HashSet;
import java.util.Set;

/**
 * Data object to carry information about a group during group member synchronization
 *
 * @author <a href="mailto:borislav.bonev@sirma.bg">Borislav Bonev</a>
 * @since 01/08/2017
 */
public class GroupInfo {
	private String systemGroupId;
	private String groupName;
	private Set<String> memberNames = new HashSet<>();

	private Set<String> currentMembers = new HashSet<>();
	private Set<String> newMembers = new HashSet<>();

	public String getSystemGroupId() {
		return systemGroupId;
	}

	public Set<String> getMemberNames() {
		return memberNames;
	}

	/**
	 * Add a member to the list of all members for this group
	 *
	 * @param memberName the member to add
	 */
	public void addMember(String memberName) {
		addNonNullValue(memberNames, memberName);
	}

	public Set<String> getCurrentMembers() {
		return currentMembers;
	}

	public Set<String> getNewMembers() {
		return newMembers;
	}

	/**
	 * Gets the group name.
	 *
	 * @return the group name
	 */
	public String getGroupName() {
		return groupName;
	}

	public void setSystemGroupId(String systemGroupId) {
		this.systemGroupId = systemGroupId;
	}

	public void setGroupName(String groupName) {
		this.groupName = groupName;
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
