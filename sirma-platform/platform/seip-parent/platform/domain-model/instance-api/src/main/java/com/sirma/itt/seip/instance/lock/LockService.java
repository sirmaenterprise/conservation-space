package com.sirma.itt.seip.instance.lock;

import java.util.Collection;
import java.util.Map;

import com.sirma.itt.seip.domain.instance.InstanceReference;
import com.sirma.itt.seip.instance.lock.exception.LockException;
import com.sirma.itt.seip.instance.lock.exception.UnlockException;

/**
 * Service that manages instance locking.
 *
 * @author BBonev
 */
public interface LockService {

	/**
	 * Returns the lock status for the given instance.
	 *
	 * @param reference
	 *            the instance reference
	 * @return the lock info for the instance. Never <code>null</code>.
	 */
	LockInfo lockStatus(InstanceReference reference);

	/**
	 * Returns the lock status for the given instances.
	 *
	 * @param reference
	 *            the instance reference
	 * @return the lock info for each of the given instance reference. Never <code>null</code>.
	 */
	Map<InstanceReference, LockInfo> lockStatus(Collection<InstanceReference> reference);

	/**
	 * Forces the lock on the given instance for the current user. This operation will succeed only if the current user
	 * has higher permissions than the user currently holding the lock. If the current user does not have the proper
	 * permissions or has less permissions that the one currently holding the lock then {@link LockException} will be
	 * thrown.
	 *
	 * @param reference
	 *            the instance reference
	 * @return the updated lock info after the operation if successful.
	 */
	default LockInfo forceLock(InstanceReference reference) {
		return forceLock(reference, "");
	}

	/**
	 * Forces the lock on the given instance for the current user. This operation will succeed only if the current user
	 * has higher permissions than the user currently holding the lock. If the current user does not have the proper
	 * permissions or has less permissions that the one currently holding the lock then {@link LockException} will be
	 * thrown.
	 *
	 * @param reference
	 *            the instance reference
	 * @return the updated lock info after the operation if successful.
	 */
	LockInfo forceLock(InstanceReference reference, String type);

	/**
	 * Locks the given instance if not already locked. If locked by the same user the method does not do anything. If
	 * already locked by someone else a {@link LockException} is thrown.
	 *
	 * @param reference
	 *            the instance reference
	 * @return the lock info containing the lock status.
	 */
	LockInfo lock(InstanceReference reference);

	/**
	 * Locks the given instance if not already locked. If locked by the same user the method does not do anything. If
	 * already locked by someone else a {@link LockException} is thrown.
	 *
	 * @param reference
	 *            the instance reference
	 * @param type
	 *            the type of the lock, primary used for information about the lock reason
	 * @return the lock info containing the lock status.
	 */
	LockInfo lock(InstanceReference reference, String type);

	/**
	 * Tries to lock the given instance by the current user. If the instance is not locked then the instance is locked.
	 * If the instance is already locked the method does nothing. This is in contrast of the method
	 * {@link #lock(InstanceReference)} where a {@link LockException} is thrown in this case. <br>
	 * In order to check if the method succeeded call {@link LockInfo#isLockedByMe()} to identify if the call
	 * successfully locked the instance for the current user.
	 *
	 * @param reference
	 *            the instance reference
	 * @return the lock info representing the current status.
	 */
	default LockInfo tryLock(InstanceReference reference){
		return tryLock(reference, "");
	}

	/**
	 * Tries to lock the given instance by the current user. If the instance is not locked then the instance is locked.
	 * If the instance is already locked the method does nothing. This is in contrast of the method
	 * {@link #lock(InstanceReference)} where a {@link LockException} is thrown in this case. <br>
	 * In order to check if the method succeeded call {@link LockInfo#isLockedByMe()} to identify if the call
	 * successfully locked the instance for the current user.
	 *
	 * @param reference
	 *            the instance reference
	 * @param type the lock type to try to acquire
	 * @return the lock info representing the current status.
	 */
	LockInfo tryLock(InstanceReference reference, String type);

	/**
	 * Unlocks the given instance. If the instance is not locked the method does nothing. If the instance is locked by
	 * the current user then it's the instance is unlocked at the end of the method call. If the instance is locked by
	 * other user then {@link UnlockException} is thrown.
	 *
	 * @param reference
	 *            the instance reference
	 * @return the lock info representing the instance lock status.
	 */
	LockInfo unlock(InstanceReference reference);

	/**
	 * Tries to unlock the given instance. If the instance is not locked the method does nothing. If locked by the
	 * current user then the instance is unlocked. If locked by other user the method does nothing. <br>
	 * In order to check if the method succeeded call {@link LockInfo#isLocked()} to identify if the call successfully
	 * unlocked the instance.
	 *
	 * @param reference
	 *            the instance reference
	 * @return the lock info representing the instance lock status.
	 */
	LockInfo tryUnlock(InstanceReference reference);

	/**
	 * Force unlock of the given instance. If the instance is not locked the method does nothing. If locked by the
	 * current user then the method unlocks the instance. If locked by other user and the current user has higher role
	 * then the instance is unlocked. If the calling user does not have enough permissions to unlock the instance an
	 * {@link UnlockException} is thrown.
	 *
	 * @param reference
	 *            the instance reference
	 * @return the lock info representing the instance lock status.
	 */
	LockInfo forceUnlock(InstanceReference reference);

	/**
	 * Checks if the current user is allowed to modify the instance represented by the given instance reference
	 * according the the lock service. Modification is allowed if the instance is not locked or is locked by the current
	 * user or the current user is administrator.
	 *
	 * @param reference
	 *            the reference to check.
	 * @return <code>true</code>, if current user is allowed to modify the instance.
	 */
	boolean isAllowedToModify(InstanceReference reference);

	/**
	 * Checks if the currently logged user has permissions to lock specific instance.
	 *
	 * @param InstanceReference
	 *            the reference of the instance which will be checked
	 * @return <b>TRUE</b> if the user has permissions lock the instance, <b>FALSE</b> otherwise
	 */
	boolean hasPermissionsToLock(InstanceReference InstanceReference);

	/**
	 * Checks if the currently logged user has permissions to unlock specific instance.
	 *
	 * @param InstanceReference
	 *            the reference of the instance which will be checked
	 * @return <b>TRUE</b> if the user has permissions unlock the instance, <b>FALSE</b> otherwise
	 */
	boolean hasPermissionsToUnlock(InstanceReference InstanceReference);

}
