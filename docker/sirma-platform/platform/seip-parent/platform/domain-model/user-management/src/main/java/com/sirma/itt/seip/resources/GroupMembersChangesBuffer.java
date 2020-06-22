package com.sirma.itt.seip.resources;

import java.io.Serializable;

import javax.inject.Inject;
import javax.transaction.TransactionScoped;

import com.sirma.itt.seip.tx.TransactionSupport;
import com.sirma.itt.seip.tx.util.AbstractTransactionalChangesBuffer;

/**
 * Transaction buffer for group membership changes. The buffer will be automatically flushed at the end of transaction.
 *
 * @author <a href="mailto:borislav.bonev@sirma.bg">Borislav Bonev</a>
 * @since 09/11/2017
 */
@TransactionScoped
class GroupMembersChangesBuffer extends AbstractTransactionalChangesBuffer<GroupChange> implements Serializable{
	GroupMembersChangesBuffer() {
		super();
	}

	@Inject
	GroupMembersChangesBuffer(TransactionSupport transactionSupport) {
		super(transactionSupport);
	}

	/**
	 * Add addition member change. The given member will be assigned to the given group
	 *
	 * @param groupId the target group id
	 * @param memberId the member id
	 */
	void addMember(Serializable groupId, Serializable memberId) {
		add(new GroupChange(groupId, memberId, ChangeType.ADD));
	}

	/**
	 * Add removal member change. The given member will be unassigned to the given group
	 *
	 * @param groupId the target group id
	 * @param memberId the member id
	 */
	void removeMember(Serializable groupId, Serializable memberId) {
		add(new GroupChange(groupId, memberId, ChangeType.REMOVE));
	}
}

/**
 * Represents a single change to a member addition or removal
 *
 * @author BBonev
 */
class GroupChange implements Serializable {
	private final Serializable groupId;
	private final Serializable memberId;
	private final ChangeType changeType;

	GroupChange(Serializable groupId, Serializable memberId, ChangeType changeType) {
		this.groupId = groupId;
		this.memberId = memberId;
		this.changeType = changeType;
	}

	Serializable getGroupId() {
		return groupId;
	}

	Serializable getMemberId() {
		return memberId;
	}

	ChangeType getChangeType() {
		return changeType;
	}
}

/**
 * Member change type: Add or Remove
 *
 * @author BBonev
 */
enum ChangeType {
	ADD,
	REMOVE;
}
