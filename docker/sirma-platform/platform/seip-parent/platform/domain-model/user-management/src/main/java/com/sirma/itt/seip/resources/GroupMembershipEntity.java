package com.sirma.itt.seip.resources;

import static com.sirma.itt.seip.util.EqualsHelper.nullSafeEquals;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Index;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;

import com.sirma.itt.seip.db.PersistenceUnits;
import com.sirma.itt.seip.db.discovery.PersistenceUnitBinding;
import com.sirma.itt.seip.model.BaseEntity;

/**
 * Entity that represents a membership of a resource to other resource. This is used to represent the group member
 * relations between groups and their members (group and/or users). The mapped groupId and memberId are the system id of
 * the corresponding resource from {@link ResourceEntity}. <br>
 * Note that entity prevents as single member to be assigned more than once to a particular group.
 *
 * @author BBonev
 */
@PersistenceUnitBinding(PersistenceUnits.PRIMARY)
@Entity
@Table(name = "seip_groupmembership", uniqueConstraints = {
		@UniqueConstraint(name = "gme_uc_uniquemember", columnNames = { "group_id", "member_id" }) }, indexes = {
				@Index(name = "gme_idx_groupid", columnList = "group_id"),
				@Index(name = "gme_idx_memberid", columnList = "member_id") })
@NamedQueries({
		@NamedQuery(name = GroupMembershipEntity.GET_ALL_MEMBERS_IDS_KEY, query = GroupMembershipEntity.GET_ALL_MEMBERS_IDS),
		@NamedQuery(name = GroupMembershipEntity.GET_CONTAINING_GROUP_IDS_KEY, query = GroupMembershipEntity.GET_CONTAINING_GROUP_IDS),
		@NamedQuery(name = GroupMembershipEntity.GET_ALL_MEMBERS_KEY, query = GroupMembershipEntity.GET_ALL_MEMBERS),
		@NamedQuery(name = GroupMembershipEntity.REMOVE_MEMBERS_KEY, query = GroupMembershipEntity.REMOVE_MEMBERS),
		@NamedQuery(name = GroupMembershipEntity.REMOVE_GROUP_MEMBERS_KEY, query = GroupMembershipEntity.REMOVE_GROUP_MEMBERS),
		@NamedQuery(name = GroupMembershipEntity.REMOVE_MEMPER_PARTICIPATION_KEY, query = GroupMembershipEntity.REMOVE_MEMPER_PARTICIPATION) })
public class GroupMembershipEntity extends BaseEntity {

	private static final long serialVersionUID = -8270547293592029575L;

	/**
	 * Query all member ids for the given {@code groupId}
	 */
	public static final String GET_ALL_MEMBERS_IDS_KEY = "GET_ALL_MEMBERS_IDS";
	static final String GET_ALL_MEMBERS_IDS = "select memberId from GroupMembershipEntity where groupId = :groupId";

	/**
	 * Query all group ids that are associated with the given {@code memberId}
	 */
	public static final String GET_CONTAINING_GROUP_IDS_KEY = "GET_CONTAINING_GROUP_IDS";
	static final String GET_CONTAINING_GROUP_IDS = "select groupId from GroupMembershipEntity where memberId = :memberId";

	/**
	 * Query all members as {@link ResourceEntity}s for the given {@code groupId}
	 */
	public static final String GET_ALL_MEMBERS_KEY = "GET_ALL_MEMBERS";
	static final String GET_ALL_MEMBERS = "select r from GroupMembershipEntity g, ResourceEntity r where g.groupId = :groupId and r.id=g.memberId";

	/** Remove {@code members} from the group identified by the given {@code groupId} */
	public static final String REMOVE_MEMBERS_KEY = "REMOVE_MEMBERS";
	static final String REMOVE_MEMBERS = "delete from GroupMembershipEntity where groupId = :groupId and memberId in (:members)";

	/** Remove all members for a group identified by the given {@code groupId} */
	public static final String REMOVE_GROUP_MEMBERS_KEY = "REMOVE_GROUP_MEMBERS";
	static final String REMOVE_GROUP_MEMBERS = "delete from GroupMembershipEntity where groupId = :groupId";

	/** Remove a member from all groups where it's a member */
	public static final String REMOVE_MEMPER_PARTICIPATION_KEY = "REMOVE_MEMPER_PARTICIPATION";
	static final String REMOVE_MEMPER_PARTICIPATION = "delete from GroupMembershipEntity where memberId = :memberId";

	@Column(name = "group_id", length = 100, nullable = false)
	private String groupId;

	@Column(name = "member_id", length = 100, nullable = false)
	private String memberId;

	/**
	 * Instantiates a new group membership entity.
	 */
	public GroupMembershipEntity() {
		// just default constructor
	}

	/**
	 * Instantiates a new group membership entity.
	 *
	 * @param groupId
	 *            the group id
	 * @param memberId
	 *            the member id
	 */
	public GroupMembershipEntity(String groupId, String memberId) {
		this.groupId = groupId;
		this.memberId = memberId;
	}

	/**
	 * The system id of the resource object that identifies the group container
	 *
	 * @return the groupId
	 */
	public String getGroupId() {
		return groupId;
	}

	/**
	 * Sets the system id of the resource that identifies the group container
	 *
	 * @param groupId
	 *            the groupId to set
	 */
	public void setGroupId(String groupId) {
		this.groupId = groupId;
	}

	/**
	 * Gets the system id of the resource that is part of the group identified by {@link #getGroupId()}
	 *
	 * @return the memberId
	 */
	public String getMemberId() {
		return memberId;
	}

	/**
	 * Sets the system id of the resource that should be part of the group identified by the {@link #getGroupId()}
	 *
	 * @param memberId
	 *            the memberId to set
	 */
	public void setMemberId(String memberId) {
		this.memberId = memberId;
	}

	@Override
	public int hashCode() {
		final int PRIME = 31;
		int result = super.hashCode();
		result = PRIME * result + (groupId == null ? 0 : groupId.hashCode());
		result = PRIME * result + (memberId == null ? 0 : memberId.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (!super.equals(obj)) {
			return false;
		}
		if (!(obj instanceof GroupMembershipEntity)) {
			return false;
		}
		GroupMembershipEntity other = (GroupMembershipEntity) obj;
		return nullSafeEquals(groupId, other.groupId) && nullSafeEquals(memberId, other.memberId);
	}

}
