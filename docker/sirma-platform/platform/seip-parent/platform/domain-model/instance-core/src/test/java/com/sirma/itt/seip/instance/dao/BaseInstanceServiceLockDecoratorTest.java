/**
 *
 */
package com.sirma.itt.seip.instance.dao;

import static com.sirma.itt.seip.collections.CollectionUtils.emptyMap;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.Serializable;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import com.sirma.itt.seip.domain.definition.DefinitionModel;
import com.sirma.itt.seip.domain.instance.EmfInstance;
import com.sirma.itt.seip.domain.instance.Instance;
import com.sirma.itt.seip.domain.instance.InstanceReference;
import com.sirma.itt.seip.instance.lock.LockInfo;
import com.sirma.itt.seip.instance.lock.LockService;
import com.sirma.itt.seip.instance.lock.exception.LockException;
import com.sirma.itt.seip.instance.state.Operation;
import com.sirma.itt.seip.testutil.mocks.InstanceReferenceMock;
import com.sirma.itt.seip.util.ReflectionUtils;

/**
 * @author BBonev
 */
public class BaseInstanceServiceLockDecoratorTest {

	@InjectMocks
	BaseInstanceServiceLockDecoratorStub decoratorStub = new BaseInstanceServiceLockDecoratorStub();
	@Mock
	InstanceService service;
	@Mock
	LockService lockService;

	@BeforeMethod
	public void beforeMethod() {
		MockitoAnnotations.initMocks(this);
	}

	@Test
	public void test_saveNotLocked() {
		InstanceReference instance = InstanceReferenceMock.createGeneric("emf:instance");

		when(lockService.lockStatus(any(InstanceReference.class)))
				.thenReturn(new LockInfo(null, null, null, null, user -> false));
		when(lockService.isAllowedToModify(any(InstanceReference.class))).thenReturn(Boolean.TRUE);

		decoratorStub.save(instance.toInstance(), new Operation());

		verify(service).save(eq(instance.toInstance()), any());
	}

	@Test(expectedExceptions = LockException.class)
	public void test_saveLocked() {
		EmfInstance instance = new EmfInstance();
		instance.setId("emf:instance");
		ReflectionUtils.setFieldValue(instance, "reference", mock(InstanceReference.class));

		when(lockService.lockStatus(any(InstanceReference.class)))
				.thenReturn(new LockInfo(mock(InstanceReference.class), "admin", null, null, user -> false));
		when(lockService.isAllowedToModify(any(InstanceReference.class))).thenReturn(Boolean.FALSE);

		decoratorStub.save(instance, new Operation());
	}

	@Test
	public void test_saveLockedByMe() {
		EmfInstance instance = new EmfInstance();
		instance.setId("emf:instance");
		ReflectionUtils.setFieldValue(instance, "reference", mock(InstanceReference.class));

		when(lockService.lockStatus(any(InstanceReference.class)))
				.thenReturn(new LockInfo(mock(InstanceReference.class), "admin", null, null, user -> true));
		when(lockService.isAllowedToModify(any(InstanceReference.class))).thenReturn(Boolean.TRUE);

		decoratorStub.save(instance, new Operation());

		verify(service).save(eq(instance), any());
	}

	@Test
	public void test_publishNotLocked() {
		EmfInstance instance = new EmfInstance();
		instance.setId("emf:instance");
		ReflectionUtils.setFieldValue(instance, "reference", mock(InstanceReference.class));

		when(lockService.lockStatus(any(InstanceReference.class)))
				.thenReturn(new LockInfo(null, null, null, null, user -> false));
		when(lockService.isAllowedToModify(any(InstanceReference.class))).thenReturn(Boolean.TRUE);

		decoratorStub.publish(instance, new Operation());

		verify(service).publish(eq(instance), any());
	}

	@Test(expectedExceptions = LockException.class)
	public void test_publishLocked() {
		EmfInstance instance = new EmfInstance();
		instance.setId("emf:instance");
		ReflectionUtils.setFieldValue(instance, "reference", mock(InstanceReference.class));

		when(lockService.lockStatus(any(InstanceReference.class)))
				.thenReturn(new LockInfo(mock(InstanceReference.class), "admin", null, null, user -> false));
		when(lockService.isAllowedToModify(any(InstanceReference.class))).thenReturn(Boolean.FALSE);

		decoratorStub.publish(instance, new Operation());
	}

	@Test
	public void test_publishLockedByMe() {
		EmfInstance instance = new EmfInstance();
		instance.setId("emf:instance");
		ReflectionUtils.setFieldValue(instance, "reference", mock(InstanceReference.class));

		when(lockService.lockStatus(any(InstanceReference.class)))
				.thenReturn(new LockInfo(mock(InstanceReference.class), "admin", null, null, user -> true));
		when(lockService.isAllowedToModify(any(InstanceReference.class))).thenReturn(Boolean.TRUE);

		decoratorStub.publish(instance, new Operation());

		verify(service).publish(eq(instance), any());
	}

	@Test
	public void test_deleteNotLocked() {
		EmfInstance instance = new EmfInstance();
		instance.setId("emf:instance");
		ReflectionUtils.setFieldValue(instance, "reference", mock(InstanceReference.class));

		when(lockService.lockStatus(any(InstanceReference.class)))
				.thenReturn(new LockInfo(null, null, null, null, user -> false));
		when(lockService.isAllowedToModify(any(InstanceReference.class))).thenReturn(Boolean.TRUE);

		decoratorStub.delete(instance, new Operation(), false);

		verify(service).delete(eq(instance), any(), eq(false));
	}

	@Test(expectedExceptions = LockException.class)
	public void test_deleteLocked() {
		EmfInstance instance = new EmfInstance();
		instance.setId("emf:instance");
		ReflectionUtils.setFieldValue(instance, "reference", mock(InstanceReference.class));

		when(lockService.lockStatus(any(InstanceReference.class)))
				.thenReturn(new LockInfo(mock(InstanceReference.class), "admin", null, null, user -> false));
		when(lockService.isAllowedToModify(any(InstanceReference.class))).thenReturn(Boolean.FALSE);

		decoratorStub.delete(instance, new Operation(), false);
	}

	@Test
	public void test_deleteLockedByMe() {
		EmfInstance instance = new EmfInstance();
		instance.setId("emf:instance");
		ReflectionUtils.setFieldValue(instance, "reference", mock(InstanceReference.class));

		when(lockService.lockStatus(any(InstanceReference.class)))
				.thenReturn(new LockInfo(mock(InstanceReference.class), "admin", null, null, user -> true));
		when(lockService.isAllowedToModify(any(InstanceReference.class))).thenReturn(Boolean.TRUE);

		decoratorStub.delete(instance, new Operation(), false);

		verify(service).delete(eq(instance), any(), eq(false));
	}

	private static class BaseInstanceServiceLockDecoratorStub extends BaseInstanceServiceLockDecorator {

		private InstanceService delegate;

		@Override
		protected InstanceService getDelegate() {
			return delegate;
		}

		@Override
		public Instance createInstance(DefinitionModel definition, Instance parent) {
			return null;
		}

		@Override
		public Instance createInstance(DefinitionModel definition, Instance parent, Operation operation) {
			return null;
		}

		@Override
		public Instance cancel(Instance instance) {
			return null;
		}

		@Override
		public void refresh(Instance instance) {
			// nothing
		}

		@Override
		public List<Instance> loadInstances(Instance owner) {
			return null;
		}

		@Override
		public Instance loadByDbId(Serializable id) {
			return null;
		}

		@Override
		public Instance load(Serializable instanceId) {
			return null;
		}

		@Override
		public <S extends Serializable> List<Instance> load(List<S> ids) {
			return null;
		}

		@Override
		public <S extends Serializable> List<Instance> loadByDbId(List<S> ids) {
			return null;
		}

		@Override
		public <S extends Serializable> List<Instance> load(List<S> ids, boolean allProperties) {
			return null;
		}

		@Override
		public <S extends Serializable> List<Instance> loadByDbId(List<S> ids, boolean allProperties) {
			return null;
		}

		@Override
		public boolean isChildAllowed(Instance owner, String type, String definitionId) {
			return false;
		}

		@Override
		public Map<String, List<DefinitionModel>> getAllowedChildren(Instance owner) {
			return null;
		}

		@Override
		public List<DefinitionModel> getAllowedChildren(Instance owner, String type) {
			return null;
		}

		@Override
		public boolean isChildAllowed(Instance owner, String type) {
			return false;
		}

		@Override
		public Instance clone(Instance instance, Operation operation) {
			return null;
		}

		@Override
		public Instance deepClone(Instance instanceToClone, Operation operation) {
			return null;
		}

		@Override
		public void attach(Instance targetInstance, Operation operation, Instance... children) {
			// nothing
		}

		@Override
		public void detach(Instance sourceInstance, Operation operation, Instance... instances) {
			// nothing
		}

		@Override
		public Optional<Instance> loadDeleted(Serializable id) {
			return delegate.loadDeleted(id);
		}

		@Override
		public <S extends Serializable> InstanceExistResult<S> exist(Collection<S> identifiers,
				boolean includeDeleted) {
			return new InstanceExistResult<>(emptyMap());
		}
	}
}
