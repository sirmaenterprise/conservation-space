package com.sirma.itt.seip.resources;

import static com.sirma.itt.seip.collections.CollectionUtils.isEmpty;

import java.io.Serializable;
import java.lang.invoke.MethodHandles;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.function.Consumer;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import javax.inject.Inject;
import javax.inject.Singleton;

import org.apache.commons.httpclient.URIException;
import org.apache.commons.httpclient.util.URIUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sirma.itt.seip.Pair;
import com.sirma.itt.seip.db.DbDao;

/**
 * DAO class for {@link ResourceEntity} and {@link GroupMembershipEntity}.
 *
 * @author BBonev
 */
@Singleton
class ResourceEntityDao {
	private static final Logger LOGGER = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());
	private static final String GROUP_ID = "groupId";
	private static final String MEMBER_ID = "memberId";
	private static final String IDENTIFIER = "identifier";
	private static final String IDS = "ids";
	private static final String TYPE = "type";

	@Inject
	private DbDao dbDao;

	/**
	 * Find entities for the given system ids
	 *
	 * @param ids
	 *            the ids
	 * @return the list
	 */
	List<ResourceEntity> findResourceEntities(Set<Serializable> ids) {
		return dbDao.fetchWithNamed(ResourceEntity.QUERY_RESOURCES_BY_IDS_KEY,
				Arrays.asList(new Pair<String, Object>(IDS, ids)));
	}

	/**
	 * Gets the all resources from the database
	 *
	 * @return the all resources
	 */
	List<ResourceEntity> getAllResources() {
		return dbDao.fetchWithNamed(ResourceEntity.QUERY_ALL_RESOURCES_KEY,
				Collections.<Pair<String, Object>>emptyList());
	}

	/**
	 * Gets the all resources from the database filtered by {@link ResourceEntity#getType()} .
	 *
	 * @param resourceType
	 *            the resource type
	 * @return the all resources
	 */
	List<ResourceEntity> getAllResourcesByType(ResourceType resourceType) {
		return dbDao.fetchWithNamed(ResourceEntity.QUERY_ALL_RESOURCES_BY_TYPE_KEY,
				Arrays.asList(new Pair<String, Object>(TYPE, resourceType.getType())));
	}

	/**
	 * Gets all system resource ids for resources that match the given {@link ResourceEntity#getType()} .
	 *
	 * @param resourceType
	 *            the resource type
	 * @return the all resources identifiers
	 */
	List<String> getAllResourceIdsByType(ResourceType resourceType) {
		return dbDao.fetchWithNamed(ResourceEntity.QUERY_ALL_RESOURCE_IDS_BY_TYPE_KEY,
				Arrays.asList(new Pair<String, Object>(TYPE, resourceType.getType())));
	}

	/**
	 * Find resources that have the given identifiers (names) and type
	 *
	 * @param ids
	 *            the ids
	 * @param type
	 *            the type
	 * @return the list
	 */
	@SuppressWarnings("boxing")
	List<ResourceEntity> findResourcesByNameAndType(Collection<String> ids, ResourceType type) {
		List<String> idsLowerCase = ids.stream()
				.map(String::toLowerCase)
				.collect(Collectors.toList());

		List<Pair<String, Object>> args = new ArrayList<>(2);
		args.add(new Pair<String, Object>(IDENTIFIER, idsLowerCase));
		args.add(new Pair<String, Object>(TYPE, type.getType()));
		return dbDao.fetchWithNamed(ResourceEntity.QUERY_RESOURCES_BY_NAMES_AND_TYPE_KEY, args);
	}

	/**
	 * Persist resource entity
	 *
	 * @param entity
	 *            the entity
	 * @return the updated resource entity
	 */
	ResourceEntity persist(ResourceEntity entity) {
		return dbDao.saveOrUpdate(entity);
	}

	/**
	 * Find resource entity by system id.
	 *
	 * @param systemId the database identifier to look for
	 * @return the updated resource entity
	 */
	ResourceEntity find(String systemId) {
		return dbDao.find(ResourceEntity.class, systemId);
	}

	/**
	 * Checks if resource exists with the given system id or name
	 *
	 * @param id
	 *            the id to check for
	 * @return true, if exists and <code>false</code> if not.
	 */
	boolean resourceExists(Serializable id) {
		List<Long> list = dbDao.fetchWithNamed(ResourceEntity.CHECK_IF_RESOURCE_EXISTS_KEY,
				Arrays.asList(new Pair<String, Object>(IDENTIFIER, id)));
		if (isEmpty(list)) {
			return false;
		}
		return list.get(0).intValue() > 0;
	}

	/**
	 * Gets the system ids of the members for the given group system id.
	 *
	 * @param groupId
	 *            the group id
	 * @return the member ids that are part of the given group
	 */
	List<String> getMemberIdsOf(String groupId) {
		return dbDao.fetchWithNamed(GroupMembershipEntity.GET_ALL_MEMBERS_IDS_KEY,
				Arrays.asList(new Pair<>(GROUP_ID, groupId)));
	}

	/**
	 * Gets the member resource entities that are part of the given group
	 *
	 * @param groupId
	 *            the group id
	 * @return the members of a group identified by the given id
	 */
	List<ResourceEntity> getMembersOf(String groupId) {
		return dbDao.fetchWithNamed(GroupMembershipEntity.GET_ALL_MEMBERS_KEY,
				Arrays.asList(new Pair<>(GROUP_ID, groupId)));
	}

	/**
	 * Query all group ids that are associated with the given {@code memberId}
	 *
	 * @param resourceId
	 *            the resource id
	 * @return the containing group ids.
	 */
	List<String> getContainingGroups(String resourceId) {
		return dbDao.fetchWithNamed(GroupMembershipEntity.GET_CONTAINING_GROUP_IDS_KEY,
				Arrays.asList(new Pair<>(MEMBER_ID, resourceId)));
	}

	/**
	 * Set given resources as members to the given resource identified by the given system id.
	 *
	 * @param groupId
	 *            the group id
	 * @param newMembers
	 *            the new members to add. The collection must contain resource systemIds, {@link ResourceEntity} or
	 *            {@link Resource}
	 * @param onNewMember
	 *            consumer called with the Id of the actual added member
	 * @return true if some new members are added and <code>false</code> if no new members are added
	 */
	boolean addMembers(String groupId, Set<? extends Serializable> newMembers, Consumer<? super Serializable> onNewMember) {
		if (groupId == null || isEmpty(newMembers)) {
			return false;
		}
		Set<String> current = new HashSet<>(getMemberIdsOf(groupId));

		Set<GroupMembershipEntity> toAdd = newMembers
				.stream()
					.map(ResourceEntityDao::toSystemId)
					.filter(Objects::nonNull)
					.filter(id -> !current.contains(id))
					.map(memberId -> new GroupMembershipEntity(groupId, memberId))
					.collect(Collectors.toSet());

		if (toAdd.isEmpty()) {
			return false;
		}

		for (GroupMembershipEntity entity : toAdd) {
			onNewMember.accept(entity.getMemberId());
			dbDao.saveOrUpdate(entity);
		}
		return true;
	}

	/**
	 * Remove given resources as members from the given resource identified by the given system id.
	 *
	 * @param groupId
	 *            the group id
	 * @param members
	 *            the new members to remove. The collection must contain resource systemIds, {@link ResourceEntity} or
	 *            {@link Resource}
	 * @param onRemovedMember
	 *            consumer called with the Id of the actual removed member
	 * @return true if some members were removed and <code>false</code> if no changes were made
	 */
	boolean removeMembers(String groupId, Set<? extends Serializable> members, Consumer<? super Serializable> onRemovedMember) {
		if (groupId == null || isEmpty(members)) {
			return false;
		}
		Set<String> toRemove = members
				.stream()
					.map(ResourceEntityDao::toSystemId)
					.filter(Objects::nonNull)
					.peek(onRemovedMember)
					.collect(Collectors.toSet());

		if (toRemove.isEmpty()) {
			return false;
		}
		int removed = dbDao.executeUpdate(GroupMembershipEntity.REMOVE_MEMBERS_KEY,
				Arrays.asList(new Pair<>(GROUP_ID, groupId), new Pair<>("members", toRemove)));
		return removed > 0;
	}

	/**
	 * Remove all members for the given resource identified by the given system id. This may be called when the main
	 * resource is deleted and no longer valid
	 *
	 * @param groupId
	 *            the group id {@link Resource}
	 * @return true if some members were removed and <code>false</code> if no changes were made
	 */
	boolean removeAllMembers(String groupId) {
		if (groupId == null) {
			return false;
		}
		int removed = dbDao.executeUpdate(GroupMembershipEntity.REMOVE_GROUP_MEMBERS_KEY,
				Arrays.asList(new Pair<>(GROUP_ID, groupId)));
		return removed > 0;
	}

	/**
	 * Removes the participation of all groups where the given resource is member of. This may be called during resource
	 * deletion
	 *
	 * @param member
	 *            the member to remove from all groups
	 * @return true any changes are made
	 */
	boolean removeParticipation(Serializable member) {
		String memberId = toSystemId(member);
		if (memberId == null) {
			return false;
		}

		int removed = dbDao.executeUpdate(GroupMembershipEntity.REMOVE_MEMPER_PARTICIPATION_KEY,
				Arrays.asList(new Pair<>(MEMBER_ID, memberId)));
		return removed > 0;
	}

	static String toSystemId(Serializable serializable) {
		if (serializable instanceof String) {
			return (String) serializable;
		} else if (serializable instanceof ResourceEntity) {
			return ((ResourceEntity) serializable).getId();
		} else if (serializable instanceof Resource) {
			return (String) ((Resource) serializable).getId();
		}
		return null;
	}

	/**
	 * Generate resource db id.
	 *
	 * @param resourceName the resource name to use as base for generating a valid identifier
	 * @param alternativeId a supplier to provide a alternative user identifier if the current cannot be used as identifier
	 * @return the string
	 */
	static String generateResourceDbId(String resourceName, Supplier<String> alternativeId) {
		try {
			String updated = resourceName.replace('@', '-');
			return "emf:" + URIUtil.encodeWithinPath(updated, "UTF-8");
		} catch (URIException e) {
			LOGGER.warn("Detected invalid resourceName [{}] for semantic URI.", resourceName);
			LOGGER.trace("Detected invalid resourceName [{}] for semantic URI.", resourceName, e);
			return alternativeId.get();
		}
	}
}
