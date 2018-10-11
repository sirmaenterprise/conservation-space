package com.sirma.itt.seip.instance.lock;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyList;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;

import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import com.sirma.itt.seip.db.DbDao;
import com.sirma.itt.seip.domain.instance.EmfInstance;
import com.sirma.itt.seip.domain.instance.Instance;
import com.sirma.itt.seip.domain.instance.InstanceReference;
import com.sirma.itt.seip.event.EventService;
import com.sirma.itt.seip.instance.lock.exception.LockException;
import com.sirma.itt.seip.instance.lock.exception.UnlockException;
import com.sirma.itt.seip.model.DataType;
import com.sirma.itt.seip.model.LinkSourceId;
import com.sirma.itt.seip.permissions.action.AuthorityService;
import com.sirma.itt.seip.resources.EmfUser;
import com.sirma.itt.seip.resources.Resource;
import com.sirma.itt.seip.resources.ResourceService;
import com.sirma.itt.seip.security.User;
import com.sirma.itt.seip.security.context.SecurityContext;
import com.sirma.itt.seip.util.EqualsHelper;

/**
 * Test for {@link DefaultLockService}
 *
 * @author BBonev
 * @author A. Kunchev
 */
@SuppressWarnings("unchecked")
public class DefaultLockServiceTest {

	@InjectMocks
	private DefaultLockService lockService;

	@Mock
	private DbDao dbDao;

	@Mock
	private ResourceService resourceService;

	@Mock
	private EventService eventService;

	@Mock
	private SecurityContext securityContext;

	@Mock
	private AuthorityService authorityService;

	@Before
	public void beforeMethod() {
		MockitoAnnotations.initMocks(this);
		when(dbDao.saveOrUpdate(any())).then(a -> a.getArgumentAt(0, LockEntity.class));

		when(securityContext.getAuthenticated()).thenReturn((User) buildUser("emf:admin"));
		when(securityContext.isActive()).thenReturn(Boolean.TRUE);

		when(resourceService.areEqual(any(), any())).then(a -> {
			String user1 = a.getArgumentAt(0, String.class);
			Resource user2 = a.getArgumentAt(1, Resource.class);
			return EqualsHelper.nullSafeEquals(user1, user2.getId());
		});
		when(dbDao.executeUpdate(anyString(), anyList())).thenReturn(1);
		when(resourceService.getDisplayName(any())).thenReturn("User Name");
		when(resourceService.findResource(any(Serializable.class)))
				.then(a -> buildUser(a.getArgumentAt(0, Serializable.class)));
	}

	@Test
	public void lockStatus() {
		when(dbDao.fetchWithNamed(eq(LockEntity.QUERY_LOCK_INFO_FOR_INSTANCE_KEY), anyList()))
				.thenReturn(buildLockEntitys(buildLocked("emf:instance", "emf:admin")));

		LockInfo status = lockService.lockStatus(buildInstanceRef("emf:instance"));
		assertNotNull(status);
		assertTrue(status.isLocked());
		assertNotNull(status.getLockedInstance());
		assertEquals("emf:instance", status.getLockedInstance().getId());
		assertTrue(status.isLockedByMe());
	}

	@Test
	public void lockStatus_nullReference() {
		InstanceReference reference = null;
		LockInfo status = lockService.lockStatus(reference);
		assertNull(status.getLockedBy());
		assertNull(status.getLockedInstance());
		assertNull(status.getLockedOn());
		assertNull(status.getLockInfo());
	}

	@Test
	public void lockStatus_Collection() {
		when(dbDao.fetchWithNamed(eq(LockEntity.QUERY_LOCK_INFO_FOR_INSTANCES_KEY), anyList()))
				.thenReturn(buildLockEntitys(buildLocked("emf:instance1", "emf:admin"),
						buildLocked("emf:instance2", "emf:admin"), buildLocked("emf:instance3", "emf:otherUser")));

		InstanceReference ref1 = buildInstanceRef("emf:instance1");
		InstanceReference ref2 = buildInstanceRef("emf:instance2");
		InstanceReference ref3 = buildInstanceRef("emf:instance3");
		InstanceReference ref4 = buildInstanceRef("emf:instance4");
		Map<InstanceReference, LockInfo> lockStatus = lockService.lockStatus(Arrays.asList(ref1, ref2, ref3, ref4));

		assertNotNull(lockStatus);
		assertFalse(lockStatus.isEmpty());
		assertEquals(4, lockStatus.size());
		assertTrue(lockStatus.containsKey(ref1));
		assertTrue(lockStatus.containsKey(ref2));
		assertTrue(lockStatus.containsKey(ref3));
		assertTrue(lockStatus.containsKey(ref4));

		// locked by me
		LockInfo lockInfo = lockStatus.get(ref1);
		assertNotNull(lockInfo);
		assertTrue(lockInfo.isLocked());
		assertNotNull(lockInfo.getLockedInstance());
		assertEquals("emf:instance1", lockInfo.getLockedInstance().getId());
		assertTrue(lockInfo.isLockedByMe());

		// locked by other
		lockInfo = lockStatus.get(ref3);
		assertNotNull(lockInfo);
		assertTrue(lockInfo.isLocked());
		assertNotNull(lockInfo.getLockedInstance());
		assertEquals("emf:instance3", lockInfo.getLockedInstance().getId());
		assertFalse(lockInfo.isLockedByMe());

		// not locked
		lockInfo = lockStatus.get(ref4);
		assertNotNull(lockInfo);
		assertFalse(lockInfo.isLocked());
		assertNotNull(lockInfo.getLockedInstance());
		assertEquals("emf:instance4", lockInfo.getLockedInstance().getId());
		assertFalse(lockInfo.isLockedByMe());
	}

	@Test
	public void lockStatus_Collection_emptyInput() {
		Map<InstanceReference, LockInfo> result = lockService.lockStatus(new ArrayList<>());
		assertEquals(Collections.emptyMap(), result);
	}

	@Test
	public void lockStatus_Collection_nullInput() {
		Collection<InstanceReference> collection = null;
		Map<InstanceReference, LockInfo> result = lockService.lockStatus(collection);
		assertEquals(Collections.emptyMap(), result);
	}

	@Test
	public void lock_notLocked() {
		LockInfo lockInfo = lockService.lock(buildInstanceRef("emf:instance"));
		assertNotNull(lockInfo);
		assertTrue(lockInfo.isLocked());
		assertNotNull(lockInfo.getLockedBy());
		assertNotNull(lockInfo.getLockedInstance());
		assertNotNull(lockInfo.getLockedOn());
		assertTrue(lockInfo.isLockedByMe());

		verify(eventService).fire(any(AfterLockEvent.class));
	}

	@Test
	public void lock_lockedByMy() {
		String type = "for edit";
		when(dbDao.fetchWithNamed(eq(LockEntity.QUERY_LOCK_INFO_FOR_INSTANCE_KEY), anyList()))
				.thenReturn(buildLockEntitys(buildLockedWithType("emf:instance", "emf:admin", type)));

		LockInfo lockInfo = lockService.lock(buildInstanceRef("emf:instance"), type);
		assertNotNull(lockInfo);
		assertTrue(lockInfo.isLocked());
		assertNotNull(lockInfo.getLockedBy());
		assertNotNull(lockInfo.getLockedInstance());
		assertNotNull(lockInfo.getLockedOn());
		assertTrue(lockInfo.isLockedByMe());
		assertEquals(type, lockInfo.getLockInfo());
	}

	@Test(expected = LockException.class)
	public void lock_lockedByOther() {
		when(dbDao.fetchWithNamed(eq(LockEntity.QUERY_LOCK_INFO_FOR_INSTANCE_KEY), anyList()))
				.thenReturn(buildLockEntitys(buildLocked("emf:instance", "emf:otherUser")));

		lockService.lock(buildInstanceRef("instanceId"));
	}

	@Test(expected = LockException.class)
	public void lock_lockedByOther_noPermissions() {
		when(dbDao.fetchWithNamed(eq(LockEntity.QUERY_LOCK_INFO_FOR_INSTANCE_KEY), anyList()))
				.thenReturn(buildLockEntitys(buildLocked("emf:instance", "emf:otherUser")));
		when(authorityService.isActionAllowed(any(Instance.class), anyString(), eq(null))).thenReturn(false);
		lockService.lock(buildInstanceRef("emf:instance"));
	}

	@Test
	public void tryLock_lockedByMe() {
		when(dbDao.fetchWithNamed(eq(LockEntity.QUERY_LOCK_INFO_FOR_INSTANCE_KEY), anyList()))
				.thenReturn(buildLockEntitys(buildLocked("emf:instance", "emf:admin")));

		LockInfo lockInfo = lockService.tryLock(buildInstanceRef("emf:instance"));
		assertNotNull(lockInfo);
		assertTrue(lockInfo.isLocked());
		assertEquals("emf:admin", lockInfo.getLockedBy());
		assertNotNull(lockInfo.getLockedInstance());
		assertNotNull(lockInfo.getLockedOn());
		assertTrue(lockInfo.isLockedByMe());
	}

	@Test
	public void tryLock_notLocked() {
		LockInfo lockInfo = lockService.tryLock(buildInstanceRef("emf:instance"));
		assertNotNull(lockInfo);
		assertTrue(lockInfo.isLocked());
		assertEquals("emf:admin", lockInfo.getLockedBy());
		assertNotNull(lockInfo.getLockedInstance());
		assertNotNull(lockInfo.getLockedOn());
		assertTrue(lockInfo.isLockedByMe());
		assertEquals("", lockInfo.getLockInfo());
	}

	@Test
	public void tryLock_lockedByOther() {
		when(dbDao.fetchWithNamed(eq(LockEntity.QUERY_LOCK_INFO_FOR_INSTANCE_KEY), anyList()))
				.thenReturn(buildLockEntitys(buildLocked("emf:instance", "emf:otherUser")));
		when(authorityService.isAdminOrSystemUser(any(Resource.class))).thenReturn(false);
		when(authorityService.isActionAllowed(any(Instance.class), anyString(), eq(null))).thenReturn(false);

		LockInfo lockInfo = lockService.tryLock(buildInstanceRef("emf:instance"));
		assertNotNull(lockInfo);
		assertTrue(lockInfo.isLocked());
		assertEquals("emf:otherUser", lockInfo.getLockedBy());
		assertNotNull(lockInfo.getLockedInstance());
		assertNotNull(lockInfo.getLockedOn());
		assertFalse(lockInfo.isLockedByMe());
	}

	@Test
	public void unlock_notLocked() {
		LockInfo lockInfo = lockService.unlock(buildInstanceRef("emf:instance"));
		assertNotNull(lockInfo);
		assertFalse(lockInfo.isLocked());
		assertNull(lockInfo.getLockedBy());
		assertNotNull(lockInfo.getLockedInstance());
		assertNull(lockInfo.getLockedOn());
		assertFalse(lockInfo.isLockedByMe());
	}

	@Test
	public void unlock_lockedByMe() {
		when(dbDao.fetchWithNamed(eq(LockEntity.QUERY_LOCK_INFO_FOR_INSTANCE_KEY), anyList()))
				.thenReturn(buildLockEntitys(buildLocked("emf:instance", "emf:admin")));

		when(authorityService.isActionAllowed(any(Instance.class), eq("unlock"), eq(null))).thenReturn(true);

		LockInfo lockInfo = lockService.unlock(buildInstanceRef("emf:instance"));
		assertNotNull(lockInfo);
		assertFalse(lockInfo.isLocked());
		assertNull(lockInfo.getLockedBy());
		assertNotNull(lockInfo.getLockedInstance());
		assertNull(lockInfo.getLockedOn());
		assertFalse(lockInfo.isLockedByMe());

		verify(eventService).fire(any(BeforeUnlockEvent.class));
	}

	@Test(expected = UnlockException.class)
	public void unlock_lockedByOther() {
		when(dbDao.fetchWithNamed(eq(LockEntity.QUERY_LOCK_INFO_FOR_INSTANCE_KEY), anyList()))
				.thenReturn(buildLockEntitys(buildLocked("emf:instance", "emf:otherUser")));

		lockService.unlock(buildInstanceRef("emf:instance"));
	}

	@Test(expected = UnlockException.class)
	public void unlock_lockedByOther_noPermissions() {
		when(dbDao.fetchWithNamed(eq(LockEntity.QUERY_LOCK_INFO_FOR_INSTANCE_KEY), anyList()))
				.thenReturn(buildLockEntitys(buildLocked("emf:instance", "emf:otherUser")));
		when(authorityService.isActionAllowed(any(Instance.class), anyString(), eq(null))).thenReturn(false);
		lockService.unlock(buildInstanceRef("emf:instance"));
	}

	@Test
	public void tryUnlock_lockedByMe() {
		when(dbDao.fetchWithNamed(eq(LockEntity.QUERY_LOCK_INFO_FOR_INSTANCE_KEY), anyList()))
				.thenReturn(buildLockEntitys(buildLocked("emf:instance", "emf:admin")));

		when(authorityService.isActionAllowed(any(Instance.class), eq("unlock"), eq(null))).thenReturn(true);

		LockInfo lockInfo = lockService.tryUnlock(buildInstanceRef("emf:instance"));
		assertNotNull(lockInfo);
		assertFalse(lockInfo.isLocked());
		assertNull(lockInfo.getLockedBy());
		assertNotNull(lockInfo.getLockedInstance());
		assertNull(lockInfo.getLockedOn());
		assertFalse(lockInfo.isLockedByMe());
	}

	@Test
	public void tryUnlock_lockedByOtherUser() {
		when(dbDao.fetchWithNamed(eq(LockEntity.QUERY_LOCK_INFO_FOR_INSTANCE_KEY), anyList()))
				.thenReturn(buildLockEntitys(buildLocked("emf:instance", "emf:otherUser")));
		when(authorityService.isAdminOrSystemUser(any(Resource.class))).thenReturn(false);
		when(authorityService.isActionAllowed(any(Instance.class), anyString(), eq(null))).thenReturn(false);

		LockInfo lockInfo = lockService.tryUnlock(buildInstanceRef("emf:instance"));
		assertNotNull(lockInfo);
		assertTrue(lockInfo.isLocked());
		assertNotNull(lockInfo.getLockedBy());
		assertEquals("emf:otherUser", lockInfo.getLockedBy());
		assertNotNull(lockInfo.getLockedInstance());
		assertNotNull(lockInfo.getLockedOn());
		assertFalse(lockInfo.isLockedByMe());
	}

	@Test
	public void isAllowedToModify_notLocked() {
		assertTrue(lockService.isAllowedToModify(buildInstanceRef("emf:instance")));
	}

	@Test
	public void isAllowedToModify_LockedByMe() {
		when(dbDao.fetchWithNamed(eq(LockEntity.QUERY_LOCK_INFO_FOR_INSTANCE_KEY), anyList()))
				.thenReturn(buildLockEntitys(buildLocked("emf:instance", "emf:admin")));

		assertTrue(lockService.isAllowedToModify(buildInstanceRef("emf:instance")));
	}

	@Test
	public void isAllowedToModify_LockedByOtherUser() {
		when(dbDao.fetchWithNamed(eq(LockEntity.QUERY_LOCK_INFO_FOR_INSTANCE_KEY), anyList()))
				.thenReturn(buildLockEntitys(buildLocked("emf:instance", "emf:otherUser")));

		assertFalse(lockService.isAllowedToModify(buildInstanceRef("emf:instance")));
	}

	@Test
	public void isAllowedToModify_LockedByOtherUser_currentlyLoggedAdmin() {
		when(authorityService.isAdminOrSystemUser()).thenReturn(true);
		when(dbDao.fetchWithNamed(eq(LockEntity.QUERY_LOCK_INFO_FOR_INSTANCE_KEY), anyList()))
				.thenReturn(buildLockEntitys(buildLocked("emf:instance", "emf:otherUser")));

		assertTrue(lockService.isAllowedToModify(buildInstanceRef("emf:instance")));
	}

	@Test
	public void forceUnlock_lockedByOther_havePermissions() {
		when(dbDao.fetchWithNamed(eq(LockEntity.QUERY_LOCK_INFO_FOR_INSTANCE_KEY), anyList()))
				.thenReturn(buildLockEntitys(buildLocked("emf:instance", "emf:otherUser")));

		LockInfo lockInfo = lockService.forceUnlock(buildInstanceRef("emf:instance"));
		assertNotNull(lockInfo);
		assertFalse(lockInfo.isLocked());
		assertNull(lockInfo.getLockedBy());
		assertNotNull(lockInfo.getLockedInstance());
		assertNull(lockInfo.getLockedOn());
		assertFalse(lockInfo.isLockedByMe());
	}

	@Test
	public void forceLock_lockedByOther_havePermissions() {
		when(dbDao.fetchWithNamed(eq(LockEntity.QUERY_LOCK_INFO_FOR_INSTANCE_KEY), anyList()))
				.thenReturn(buildLockEntitys(buildLocked("emf:instance", "emf:otherUser")));

		LockInfo lockInfo = lockService.forceLock(buildInstanceRef("emf:instance"));
		assertNotNull(lockInfo);
		assertTrue(lockInfo.isLocked());
		assertNotNull(lockInfo.getLockedBy());
		assertEquals("emf:admin", lockInfo.getLockedBy());
		assertNotNull(lockInfo.getLockedInstance());
		assertNotNull(lockInfo.getLockedOn());
		assertTrue(lockInfo.isLockedByMe());
	}

	static Supplier<LockEntity> buildLocked(String instanceId, String user) {
		return () -> {
			LockEntity entity = new LockEntity();
			entity.setLockedBy(user);
			entity.setLockedInstance(buildInstanceRef(instanceId));
			entity.setLockedOn(new Date());
			return entity;
		};
	}

	static Supplier<LockEntity> buildLockedWithType(String instanceId, String user, String type) {
		return () -> {
			LockEntity entity = buildLocked(instanceId, user).get();
			entity.setLockType(type);
			return entity;
		};
	}

	private static Resource buildUser(Serializable id) {
		if (id instanceof Resource) {
			return (Resource) id;
		}
		EmfUser result = new EmfUser();
		result.setName(id.toString());
		result.setId(id);
		return result;
	}

	private static LinkSourceId buildInstanceRef(String instanceId) {
		DataType type = new DataType();
		type.setName("instance");
		EmfInstance instance = new EmfInstance();
		instance.setId(instanceId);
		return new LinkSourceId(instanceId, type, instance);
	}

	@SafeVarargs
	private static List<LockEntity> buildLockEntitys(Supplier<LockEntity>... suppliers) {
		List<LockEntity> entities = new ArrayList<>(suppliers.length);
		for (Supplier<LockEntity> supplier : suppliers) {
			entities.add(supplier.get());
		}
		return entities;
	}

	@Test
	public void hasPermissionsToLock_nullReference() {
		boolean result = lockService.hasPermissionsToLock(null);
		verify(authorityService, never()).isActionAllowed(any(Instance.class), eq("lock"), eq(null));
		assertFalse(result);
	}

	@Test
	public void hasPermissionsToLock_noPermissions() {
		when(authorityService.isActionAllowed(any(Instance.class), eq("lock"), eq(null))).thenReturn(false);
		boolean result = lockService.hasPermissionsToLock(mock(InstanceReference.class));
		assertFalse(result);
	}

	@Test
	public void hasPermissionsToLock_withPermissions() {
		when(authorityService.isActionAllowed(any(Instance.class), eq("lock"), eq(null))).thenReturn(true);
		boolean result = lockService.hasPermissionsToLock(mock(InstanceReference.class));
		assertTrue(result);
	}

	@Test
	public void hasPermissionsToUnlock_nullReference() {
		boolean result = lockService.hasPermissionsToUnlock(null);
		verify(authorityService, never()).isActionAllowed(any(Instance.class), eq("unlock"), eq(null));
		assertFalse(result);
	}

	@Test
	public void hasPermissionsToUnlock_noPermissions() {
		when(authorityService.isActionAllowed(any(Instance.class), eq("unlock"), eq(null))).thenReturn(false);
		boolean result = lockService.hasPermissionsToUnlock(mock(InstanceReference.class));
		assertFalse(result);
	}

	@Test
	public void hasPermissionsToUnlock_withPermissions() {
		when(authorityService.isActionAllowed(any(Instance.class), eq("unlock"), eq(null))).thenReturn(true);
		boolean result = lockService.hasPermissionsToUnlock(mock(InstanceReference.class));
		assertTrue(result);
	}

}
