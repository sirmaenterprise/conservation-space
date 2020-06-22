package com.sirma.itt.seip.instance.lock;

import java.io.Serializable;
import java.lang.invoke.MethodHandles;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.transaction.Transactional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sirma.itt.seip.Pair;
import com.sirma.itt.seip.collections.CollectionUtils;
import com.sirma.itt.seip.db.DbDao;
import com.sirma.itt.seip.domain.instance.InstanceReference;
import com.sirma.itt.seip.event.EventService;
import com.sirma.itt.seip.instance.lock.action.LockRequest;
import com.sirma.itt.seip.instance.lock.action.UnlockRequest;
import com.sirma.itt.seip.instance.lock.exception.LockException;
import com.sirma.itt.seip.instance.lock.exception.UnlockException;
import com.sirma.itt.seip.model.LinkSourceId;
import com.sirma.itt.seip.permissions.action.AuthorityService;
import com.sirma.itt.seip.resources.ResourceService;
import com.sirma.itt.seip.security.context.SecurityContext;

/**
 * Default {@link LockService} implementation.
 *
 * @author BBonev
 */
@ApplicationScoped
class DefaultLockService implements LockService {

	private static final Logger LOGGER = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

	private static final Predicate<Serializable> NOT_LOCKED = user -> false;

	private static final LockInfo EMPTY_INFO = new LockInfo(null, null, null, null, NOT_LOCKED);
	private static final String INSTANCE_ID = "instanceId";

	@Inject
	private DbDao dbDao;

	@Inject
	private SecurityContext securityContext;

	@Inject
	private ResourceService resourceService;

	@Inject
	private EventService eventService;

	@Inject
	private AuthorityService authorityService;

	@Override
	public LockInfo lockStatus(InstanceReference reference) {
		if (reference == null) {
			return EMPTY_INFO;
		}
		List<LockEntity> entities = dbDao.fetchWithNamed(LockEntity.QUERY_LOCK_INFO_FOR_INSTANCE_KEY,
				Collections.singletonList(new Pair<>(INSTANCE_ID, reference.getId())));
		if (CollectionUtils.isEmpty(entities)) {
			return createNotLockedInfo(reference);
		}
		return createLockInfo(entities.get(0));
	}

	@Override
	public Map<InstanceReference, LockInfo> lockStatus(Collection<InstanceReference> references) {
		if (CollectionUtils.isEmpty(references)) {
			return Collections.emptyMap();
		}
		Collection<String> ids = references.stream().map(InstanceReference::getId).collect(
				CollectionUtils.toList(references.size()));

		List<LockEntity> entities = dbDao.fetchWithNamed(LockEntity.QUERY_LOCK_INFO_FOR_INSTANCES_KEY,
				Collections.singletonList(new Pair<>(INSTANCE_ID, ids)));

		Map<InstanceReference, LockInfo> status = entities.stream().map(this::createLockInfo).collect(
				Collectors.toMap(LockInfo::getLockedInstance, Function.identity()));

		for (InstanceReference reference : references) {
			status.computeIfAbsent(reference, DefaultLockService::createNotLockedInfo);
		}
		return status;
	}

	private LockInfo createLockInfo(LockEntity lockEntity) {
		return new LockInfo(lockEntity.getLockedInstance(), lockEntity.getLockedBy(), lockEntity.getLockedOn(),
				lockEntity.getLockType(), lockByMeTest());
	}

	private Predicate<Serializable> lockByMeTest() {
		return user -> {
			if (user == null || !securityContext.isActive()) {
				return false;
			}
			return resourceService.areEqual(user, securityContext.getAuthenticated());
		};
	}

	@Override
	@Transactional(dontRollbackOn = LockException.class)
	public LockInfo lock(InstanceReference reference) {
		return lockInternal(reference, true, "", throwLockException());
	}

	@Override
	@Transactional(dontRollbackOn = LockException.class)
	public LockInfo lock(InstanceReference reference, String type) {
		return lockInternal(reference, true, type, throwLockException());
	}

	@Override
	public LockInfo tryLock(InstanceReference reference, String type) {
		LockInfo lockStatus = lockStatus(reference);
		if (lockStatus.isLocked()) {
			// already locked, nothing to do more
			return lockStatus;
		}
		return persistLock(reference, type);
	}

	private LockInfo persistLock(InstanceReference reference, String type) {
		LockEntity savedEntity = dbDao.saveOrUpdate(createLockEntity(reference, type));
		LockInfo lockInfo = createLockInfo(savedEntity);

		eventService.fire(new AfterLockEvent(lockInfo));
		return lockInfo;
	}

	@Override
	public LockInfo forceLock(InstanceReference reference, String type) {
		return lockInternal(reference, false, type, Function.identity());
	}

	private LockInfo lockInternal(InstanceReference reference, boolean checkPermissions, String type,
			Function<LockInfo, LockInfo> onLocked) {
		LockInfo lockStatus = lockStatus(reference);
		if (lockStatus.isLocked()) {
			if (lockStatus.isLockedByMe()) {
				return lockStatus;
			}

			if (checkPermissions && !hasPermissionsToLock(lockStatus.getLockedInstance())) {
				return onLocked.apply(lockStatus);
			}
			// remove the current lock so it could be locked again
			forceUnlock(reference);
		}
		return persistLock(reference, type);
	}

	private Function<LockInfo, LockInfo> throwLockException() {
		return info -> {
			throw new LockException(info,
					"Instance already locked by " + resourceService.getDisplayName(info.getLockedBy()));
		};
	}

	@Override
	@Transactional(dontRollbackOn = UnlockException.class)
	public LockInfo unlock(InstanceReference reference) {
		return unlockInternal(reference, true, throwUnlockException());
	}

	@Override
	public LockInfo tryUnlock(InstanceReference reference) {
		LockInfo lockStatus = lockStatus(reference);
		if (!lockStatus.isLocked() || !lockStatus.isLockedByMe()) {
			return lockStatus;
		}

		return doUnlock(reference, lockStatus);
	}

	private LockInfo doUnlock(InstanceReference reference, LockInfo lockStatus) {
		eventService.fire(new BeforeUnlockEvent(lockStatus));

		int unlocked = dbDao.executeUpdate(LockEntity.UNLOCK_INSTANCE_KEY, Arrays
				.asList(new Pair<>(INSTANCE_ID, reference.getId()), new Pair<>("lockedBy", lockStatus.getLockedBy())));
		if (unlocked == 0) {
			LOGGER.warn("Tried to unlock already unlocked instance: {}", reference.getId());
		}
		return createNotLockedInfo(reference);
	}

	@Override
	public LockInfo forceUnlock(InstanceReference reference) {
		return unlockInternal(reference, false, Function.identity());
	}

	@Override
	public boolean isAllowedToModify(InstanceReference reference) {
		LockInfo lockStatus = lockStatus(reference);
		return !lockStatus.isLocked() || lockStatus.isLockedByMe() || authorityService.isAdminOrSystemUser();
	}

	@Override
	public boolean hasPermissionsToLock(InstanceReference reference) {
		return hasPermissionsToExecuteOperation(reference, LockRequest.LOCK);
	}

	@Override
	public boolean hasPermissionsToUnlock(InstanceReference reference) {
		return hasPermissionsToExecuteOperation(reference, UnlockRequest.UNLOCK);
	}

	private boolean hasPermissionsToExecuteOperation(InstanceReference reference, String operationId) {
		if (reference == null) {
			LOGGER.debug("Can not calculate permissions for null instance.");
			return false;
		}

		return authorityService.isActionAllowed(reference.toInstance(), operationId, null);
	}

	private LockInfo unlockInternal(InstanceReference reference, boolean checkPermissions,
			Function<LockInfo, LockInfo> onNotLockedByMe) {
		LockInfo lockStatus = lockStatus(reference);
		if (!lockStatus.isLocked()) {
			return lockStatus;
		}

		if (!lockStatus.isLockedByMe() && checkPermissions && !hasPermissionsToUnlock(lockStatus.getLockedInstance())) {
			return onNotLockedByMe.apply(lockStatus);
		}

		return doUnlock(reference, lockStatus);
	}

	private static LockInfo createNotLockedInfo(InstanceReference reference) {
		return new LockInfo(reference, null, null, null, NOT_LOCKED);
	}

	private Function<LockInfo, LockInfo> throwUnlockException() {
		return info -> {
			throw new UnlockException(info,
					"Instance already locked by " + resourceService.getDisplayName(info.getLockedBy()));
		};
	}

	private LockEntity createLockEntity(InstanceReference reference, String type) {
		LockEntity entity = new LockEntity();
		entity.setLockedBy(String.valueOf(securityContext.getAuthenticated().getSystemId()));
		entity.setLockedOn(new Date());
		entity.setLockType(type);
		entity.setLockedInstance(new LinkSourceId(reference));
		return entity;
	}

}
