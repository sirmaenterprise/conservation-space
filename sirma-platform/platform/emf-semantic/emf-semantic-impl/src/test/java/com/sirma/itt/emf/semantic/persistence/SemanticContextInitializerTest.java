package com.sirma.itt.emf.semantic.persistence;

import static org.mockito.Matchers.anyCollection;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.testng.Assert.fail;

import java.io.Serializable;
import java.rmi.RemoteException;
import java.util.Collection;
import java.util.stream.Collectors;

import javax.ejb.EJBException;

import org.testng.Assert;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import com.sirma.itt.commons.utils.reflection.ReflectionUtils;
import com.sirma.itt.emf.GeneralSemanticTest;
import com.sirma.itt.emf.mocks.NamespaceRegistryMock;
import com.sirma.itt.emf.mocks.TransactionalRepositoryConnectionMock;
import com.sirma.itt.seip.domain.definition.DataTypeDefinition;
import com.sirma.itt.seip.domain.instance.EmfInstance;
import com.sirma.itt.seip.domain.instance.Instance;
import com.sirma.itt.seip.domain.instance.InstanceReference;
import com.sirma.itt.seip.domain.instance.OwnedModel;
import com.sirma.itt.seip.instance.InstanceTypeResolver;
import com.sirma.itt.seip.instance.context.InstanceContextInitializer;
import com.sirma.itt.seip.monitor.Statistics;
import com.sirma.itt.seip.testutil.mocks.InstanceProxyMock;
import com.sirma.itt.seip.testutil.mocks.InstanceReferenceMock;
import com.sirma.itt.semantic.NamespaceRegistryService;
import com.sirma.itt.semantic.TransactionalRepositoryConnection;

/**
 * Test for {@link SemanticContextInitializer}
 *
 * @author BBonev
 */
@Test
public class SemanticContextInitializerTest extends GeneralSemanticTest<InstanceContextInitializer> {

	private static final String INSTANCE1 = "emf:instance-1";
	private static final String INSTANCE2 = "emf:instance-2";
	private static final String INSTANCE3 = "emf:instance-3";
	private static final String INSTANCE4 = "emf:instance-4";

	private final SemanticContextInitializer cut = new SemanticContextInitializer();

	private NamespaceRegistryService namespaceRegistryService;

	private TransactionalRepositoryConnection transactionalRepositoryConnection;

	/**
	 * Initialize class under test.
	 */
	@BeforeClass
	public void initializeClassUnderTest() {
		namespaceRegistryService = new NamespaceRegistryMock(context);
		ReflectionUtils.setField(cut, "namespaceRegistryService", namespaceRegistryService);
		ReflectionUtils.setField(cut, "statistics", Statistics.NO_OP);

		InstanceTypeResolver typeResolver = mock(InstanceTypeResolver.class);
		when(typeResolver.resolveInstances(anyCollection())).then(a -> {
			Collection<Serializable> argument = a.getArgumentAt(0, Collection.class);
			return argument
					.stream()
						.map(id -> namespaceRegistryService.getShortUri(id.toString()))
						.map(id -> createInstance(id))
						.collect(Collectors.toList());
		});
		when(typeResolver.resolveReferences(anyCollection())).then(a -> {
			Collection<Serializable> argument = a.getArgumentAt(0, Collection.class);
			return argument
					.stream()
						.map(id -> namespaceRegistryService.getShortUri(id.toString()))
						.map(id -> createReference(id))
						.collect(Collectors.toList());
		});
		ReflectionUtils.setField(cut, "typeResolver", typeResolver);
	}

	@Override
	@BeforeMethod
	public void beforeMethod() {
		super.beforeMethod();
		transactionalRepositoryConnection = new TransactionalRepositoryConnectionMock(context);

		ReflectionUtils.setField(cut, "connectionProvider", new InstanceProxyMock<>(transactionalRepositoryConnection));

		try {
			transactionalRepositoryConnection.afterBegin();
		} catch (EJBException | RemoteException e) {
			fail("", e);
		}
	}

	@Test
	public void parentLevel_1_noParent() {
		EmfInstance instance = createInstance(INSTANCE1);
		cut.restoreHierarchy(instance);
		Assert.assertNull(instance.getOwningInstance());
	}

	@Test
	public void parentLevel_2() {
		EmfInstance instance = createInstance(INSTANCE2);
		cut.restoreHierarchy(instance);
		Assert.assertNotNull(instance.getOwningInstance());
		Instance parent = instance.getOwningInstance();
		Assert.assertEquals(parent.getId(), INSTANCE1);
	}

	@Test
	public void parentLevel_3() {
		EmfInstance instance = createInstance(INSTANCE3);
		cut.restoreHierarchy(instance);
		Assert.assertNotNull(instance.getOwningInstance());
		Instance parent = instance.getOwningInstance();
		Assert.assertEquals(parent.getId(), INSTANCE2);

		parent = ((OwnedModel) parent).getOwningInstance();
		Assert.assertNotNull(parent);
		Assert.assertEquals(parent.getId(), INSTANCE1);
	}

	@Test
	public void parentLevel_4() {
		EmfInstance instance = createInstance(INSTANCE4);
		cut.restoreHierarchy(instance);
		Assert.assertNotNull(instance.getOwningInstance());
		Instance parent = instance.getOwningInstance();
		Assert.assertEquals(parent.getId(), INSTANCE3);

		parent = ((OwnedModel) parent).getOwningInstance();
		Assert.assertNotNull(parent);
		Assert.assertEquals(parent.getId(), INSTANCE2);

		parent = ((OwnedModel) parent).getOwningInstance();
		Assert.assertNotNull(parent);
		Assert.assertEquals(parent.getId(), INSTANCE1);
	}

	@Test
	public void parentLevel_4_fromReference() {
		InstanceReference instance = createReference(INSTANCE4);
		cut.restoreHierarchy(instance);
		Assert.assertNotNull(instance.getParent());
		InstanceReference parent = instance.getParent();
		Assert.assertEquals(parent.getIdentifier(), INSTANCE3);
		Assert.assertFalse(parent.isRoot());

		parent = parent.getParent();
		Assert.assertNotNull(parent);
		Assert.assertEquals(parent.getIdentifier(), INSTANCE2);
		Assert.assertFalse(parent.isRoot());

		parent = parent.getParent();
		Assert.assertNotNull(parent);
		Assert.assertEquals(parent.getIdentifier(), INSTANCE1);
		Assert.assertFalse(parent.isRoot());

		parent = parent.getParent();
		Assert.assertNotNull(parent);
		Assert.assertEquals(parent, InstanceReference.ROOT_REFERENCE);
		Assert.assertTrue(parent.isRoot());
	}

	@Test
	public void versionInstance() {
		EmfInstance version = createInstance("version-instance-id-v1.4");
		version.setReference(new InstanceReferenceMock());
		cut.restoreHierarchy(version);
		InstanceReference parent = version.toReference().getParent();
		Assert.assertEquals(parent, InstanceReference.ROOT_REFERENCE);
	}

	@Test
	public void versionReference() {
		InstanceReference version = createReference("version-instance-id-v1.4");
		cut.restoreHierarchy(version);
		InstanceReference parent = version.getParent();
		Assert.assertEquals(parent, InstanceReference.ROOT_REFERENCE);
	}

	private static EmfInstance createInstance(Serializable id) {
		EmfInstance instance = new EmfInstance();
		instance.setId(id);
		return instance;
	}

	private static InstanceReference createReference(String id) {
		return new InstanceReferenceMock(id, mock(DataTypeDefinition.class));
	}

	@AfterMethod
	public void commitTransaction() {
		try {
			transactionalRepositoryConnection.beforeCompletion();
			transactionalRepositoryConnection.afterCompletion(true);
		} catch (EJBException | RemoteException e) {
			fail("transaction commit failed", e);
		}
	}

	@Override
	protected String getTestDataFile() {
		return "SemanticContextInitializerTest.ttl";
	}

}
