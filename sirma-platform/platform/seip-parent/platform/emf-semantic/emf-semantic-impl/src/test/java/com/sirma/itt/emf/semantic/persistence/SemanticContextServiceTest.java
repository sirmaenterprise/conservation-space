package com.sirma.itt.emf.semantic.persistence;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyCollection;
import static org.mockito.Matchers.anyCollectionOf;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;

import java.io.Serializable;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Optional;
import java.util.stream.Collectors;

import org.eclipse.rdf4j.repository.RepositoryConnection;
import org.mockito.InjectMocks;
import org.mockito.Matchers;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.Spy;
import org.slf4j.Logger;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import com.sirma.itt.emf.GeneralSemanticTest;
import com.sirma.itt.emf.mocks.NamespaceRegistryMock;
import com.sirma.itt.emf.semantic.model.Rdf4JStringUriProxy;
import com.sirma.itt.seip.cache.lookup.EntityLookupCache;
import com.sirma.itt.seip.collections.ContextualMap;
import com.sirma.itt.seip.convert.TypeConverter;
import com.sirma.itt.seip.convert.TypeConverterUtil;
import com.sirma.itt.seip.convert.UriConverterProvider.StringUriProxy;
import com.sirma.itt.seip.definition.SemanticDefinitionService;
import com.sirma.itt.seip.domain.instance.ClassInstance;
import com.sirma.itt.seip.domain.instance.EmfInstance;
import com.sirma.itt.seip.domain.instance.Instance;
import com.sirma.itt.seip.domain.instance.InstancePropertyNameResolver;
import com.sirma.itt.seip.domain.instance.InstanceReference;
import com.sirma.itt.seip.exception.EmfRuntimeException;
import com.sirma.itt.seip.instance.InstanceTypeResolver;
import com.sirma.itt.seip.instance.context.InstanceContextService;
import com.sirma.itt.seip.instance.relation.LinkConstants;
import com.sirma.itt.seip.monitor.NoOpStatistics;
import com.sirma.itt.seip.monitor.Statistics;
import com.sirma.itt.seip.testutil.fakes.EntityLookupCacheContextFake;
import com.sirma.itt.seip.testutil.fakes.InstanceTypeFake;
import com.sirma.itt.seip.testutil.mocks.InstanceReferenceMock;
import com.sirma.itt.seip.util.ReflectionUtils;

/**
 * Test for {@link SemanticContextService}
 *
 * @author BBonev
 */
public class SemanticContextServiceTest extends GeneralSemanticTest<InstanceContextService> {

	private static final String INSTANCE1 = "emf:instance-1";
	private static final String INSTANCE2_FULL_URI = "http://ittruse.ittbg.com/ontology/enterpriseManagementFramework#instance-2";
	private static final String INSTANCE2 = "emf:instance-2";
	private static final String INSTANCE3 = "emf:instance-3";
	private static final String INSTANCE4 = "emf:instance-4";
	@SuppressWarnings("hiding")
	@InjectMocks
	private SemanticContextService contextService;
	@Mock
	private InstanceTypeResolver typeResolver;
	@Mock
	private SemanticDefinitionService semanticDefinitionService;
	@Spy
	private Statistics statistics = NoOpStatistics.INSTANCE;
	@Spy
	private InstancePropertyNameResolver fieldConverter = InstancePropertyNameResolver.NO_OP_INSTANCE;
	private RepositoryConnection repositoryConnection;
	private EntityLookupCacheContextFake cacheContext;
	private NamespaceRegistryMock namespaceRegistryService;
	private TypeConverter typeConverter;
	private EntityLookupCache<Serializable, String, Serializable> cache;

	@BeforeClass
	public void initializeClassUnderTest() throws Exception {
		namespaceRegistryService = spy(new NamespaceRegistryMock(context));
		typeConverter = spy(TypeConverterUtil.getConverter());
		LinkConstants.init(securityContextManager, ContextualMap.create());
	}

	@BeforeMethod
	@Override
	public void beforeMethod() {
		cacheContext = spy(EntityLookupCacheContextFake.createInMemory());
		repositoryConnection = spy(connectionFactory.produceManagedConnection());

		super.beforeMethod();
		contextService.initialize();
		cache = spy(cacheContext.getCache("INSTANCE_CONTEXT_STORE_CACHE"));
		when(typeResolver.resolveInstances(anyCollectionOf(Serializable.class))).thenAnswer(a -> {
			Collection<Serializable> argument = a.getArgumentAt(0, Collection.class);
			return argument
					.stream()
						.map(id -> namespaceRegistryService.getShortUri(id.toString()))
						.map(SemanticContextServiceTest::createInstance)
						.collect(Collectors.toList());
		});
		when(typeResolver.resolveReferences(anyCollectionOf(Serializable.class))).thenAnswer(a -> {
			Collection<Serializable> argument = a.getArgumentAt(0, Collection.class);
			return argument
					.stream()
						.map(id -> namespaceRegistryService.getShortUri(id.toString()))
						.map(this::createReference)
						.collect(Collectors.toList());
		});
		when(typeResolver.resolveReference(any(Serializable.class))).thenAnswer(a -> {
			Serializable argument = a.getArgumentAt(0, Serializable.class);
			return Optional.of(createReference(namespaceRegistryService.getShortUri(argument.toString())));
		});

		noTransaction();
	}

	@Test
	public void cacheUsageOnRootLevel() {
		EmfInstance instance = createInstance(INSTANCE1);
		assertFalse(contextService.getContext(instance).isPresent());
		assertFalse(contextService.getContext(instance).isPresent());
		assertFalse(contextService.getRootContext(instance).isPresent());
		verify(repositoryConnection, times(1)).prepareTupleQuery(any(), any());
	}

	@Test
	public void should_ReturnFalse_When_ParentIsNotChanged() {
		EmfInstance instance = createInstance(INSTANCE3);
		instance.add(InstanceContextService.HAS_PARENT, INSTANCE2);
		assertFalse(contextService.isContextChanged(instance));
	}

	@Test
	public void should_ReturnTrue_When_ParentIsChanged() {
		EmfInstance instance = createInstance(INSTANCE3);
		instance.add(InstanceContextService.HAS_PARENT, INSTANCE1);
		assertTrue(contextService.isContextChanged(instance));
	}

	@Test
	public void should_ReturnFalse_When_ParentIsNotChangedAndParentFromInstanceIsSetWithFullUri() {
		EmfInstance instance = createInstance(INSTANCE3);
		instance.add(InstanceContextService.HAS_PARENT, INSTANCE2_FULL_URI);
		assertFalse(contextService.isContextChanged(instance));
	}

	@Test
	public void cacheUsageOnNonRootLevel() {
		// with single call of partOf relations in chain
		assertTrue(contextService.getContext(createInstance(INSTANCE3)).isPresent());
		assertTrue(contextService.getContext(createInstance(INSTANCE2)).isPresent());
		assertFalse(contextService.getContext(createInstance(INSTANCE1)).isPresent());
		assertTrue(contextService.getRootContext(createInstance(INSTANCE2)).isPresent());
		assertTrue(contextService.getRootContext(createInstance(INSTANCE3)).isPresent());
		// one retrieval and 1 related to validation retrieval
		verify(repositoryConnection, times(2)).prepareTupleQuery(any(), any());
		assertTrue(contextService.getRootContext(createInstance(INSTANCE4)).isPresent());
		verify(repositoryConnection, times(3)).prepareTupleQuery(any(), any());
	}

	@Test
	public void testContextRetrievalFromPropertiesData() {
		Mockito.reset(typeResolver);

		EmfInstance newInstance = createInstance("emf:new-instance");
		newInstance.add(InstanceContextService.PART_OF_URI, "emf:non-cache-parent");
		Optional<InstanceReference> resolvedNewRef = Optional.of(InstanceReferenceMock.createGeneric(newInstance));
		when(typeResolver.resolveReference(any(Serializable.class))).thenAnswer(a -> {
			Serializable argument = a.getArgumentAt(0, Serializable.class);
			if (argument.equals("emf:new-instance")) {
				return resolvedNewRef;
			}
			return Optional.of(createReference(namespaceRegistryService.getShortUri(argument.toString())));
		});

		assertTrue(contextService.getContext(newInstance).isPresent());
		assertEquals(contextService.getContext(newInstance).get(), createReference("emf:non-cache-parent"));
		assertTrue(contextService.getContext("emf:new-instance").isPresent());
		assertEquals(contextService.getContext("emf:new-instance").get(), createReference("emf:non-cache-parent"));
		// verify db request
		verify(repositoryConnection, times(1)).prepareTupleQuery(any(), any());
	}

	@Test
	public void testContextRetrievalFromPropertiesDataUsingInitialBind() {
		EmfInstance newInstance = createInstance("emf:new-instance");
		contextService.bindContext(newInstance, "emf:non-cache-parent");
		assertTrue(contextService.getContext(newInstance).isPresent());
		assertEquals(contextService.getContext(newInstance).get(), createReference("emf:non-cache-parent"));
		// verify db request
		verify(repositoryConnection, times(1)).prepareTupleQuery(any(), any());
	}

	@Test
	public void testContextRetrievalFromPropertiesDataInconsistent() throws Exception {
		Field field = ReflectionUtils.getClassField(SemanticContextService.class, "LOGGER");
		Logger logger = Mockito.mock(Logger.class);
		setFinalStatic(field, logger);
		EmfInstance newInstance = createInstance("emf:new-instance");
		contextService.bindContext(newInstance, "emf:non-cache-parent");
		// reset properties
		newInstance.getProperties().clear();
		newInstance.add(InstanceContextService.PART_OF_URI, "emf:non-cache-parent2");
		Optional<InstanceReference> result = contextService.getContext(newInstance);
		assertTrue(result.isPresent());
		assertEquals(result.get(), createReference("emf:non-cache-parent"));
		// verify db request
		verify(repositoryConnection, times(1)).prepareTupleQuery(any(), any());
		verify(logger, times(1)).warn(
				eq("Detected inconsistent state for instance {}. Cached context value {}, value from provided data {}"),
				(Object[]) Matchers.anyVararg());
	}

	static void setFinalStatic(Field field, Object newValue) throws Exception {
		field.setAccessible(true);

		Field modifiersField = Field.class.getDeclaredField("modifiers");
		modifiersField.setAccessible(true);
		modifiersField.setInt(field, field.getModifiers() & ~Modifier.FINAL);

		field.set(null, newValue);
	}

	@Test
	public void parentLevel_1_noParent() {
		EmfInstance instance = createInstance(INSTANCE1);
		assertFalse(contextService.getContext(instance).isPresent());
	}

	@Test
	public void testDiffArgumentTypesSameResult() {
		InstanceReference contextRef = createReference(INSTANCE1);
		assertEquals(contextService.getContext(createInstance(INSTANCE2)).get(), contextRef);
		assertEquals(contextService.getContext(createReference(INSTANCE2)).get(), contextRef);
		assertEquals(contextService.getContext(INSTANCE2).get(), contextRef);
		assertEquals(contextService.getContext(new Rdf4JStringUriProxy(INSTANCE2)).get(), contextRef);
		assertEquals(contextService
				.getContext(TypeConverterUtil.getConverter().convert(StringUriProxy.class, INSTANCE2))
					.get(),
				contextRef);
	}

	@Test(expectedExceptions = EmfRuntimeException.class)
	public void testWrontTypeArgument() {
		EmfInstance instance = createInstance(null);
		contextService.getContext(instance).get();
	}

	@Test(expectedExceptions = EmfRuntimeException.class)
	public void testArgumentNotPersisted() {
		contextService.getContext(Long.valueOf(1)).get();
	}

	@Test
	public void parentLevel_2() {
		EmfInstance instance = createInstance(INSTANCE2);
		assertTrue(contextService.getContext(instance).isPresent());
		InstanceReference parent = contextService.getContext(instance).get();
		assertEquals(parent.getId(), INSTANCE1);
	}

	@Test
	public void testGetRootContext() throws Exception {
		InstanceReference ref1 = createReference(INSTANCE1);
		InstanceReference ref2 = createReference(INSTANCE2);
		InstanceReference ref3 = createReference(INSTANCE3);
		InstanceReference ref4 = createReference(INSTANCE4);
		assertEquals(contextService.getRootContext(ref4).get(), ref1);
		assertEquals(contextService.getRootContext(ref3).get(), ref1);
		assertEquals(contextService.getRootContext(ref2).get(), ref1);
		assertFalse(contextService.getRootContext(ref1).isPresent());
		assertFalse(contextService.getRootContext(createReference("emf:no-ref")).isPresent());
	}

	@Test
	public void testGetContextForNull() throws Exception {
		assertFalse(contextService.getContext(null).isPresent());
		assertFalse(contextService.getRootContext(null).isPresent());
		assertEquals(0, contextService.getContextPath(null).size());
	}

	@Test
	public void testGetContextPath() throws Exception {
		assertEquals(Arrays.asList(createReference(INSTANCE1), createReference(INSTANCE2), createReference(INSTANCE3)),
				contextService.getContextPath(createInstance(INSTANCE4)));
		assertEquals(Arrays.asList(createReference(INSTANCE1), createReference(INSTANCE2)),
				contextService.getContextPath(createInstance(INSTANCE3)));
		assertEquals(Arrays.asList(createReference(INSTANCE1)),
				contextService.getContextPath(createInstance(INSTANCE2)));
		assertEquals(Collections.emptyList(), contextService.getContextPath(createInstance(INSTANCE1)));
	}

	@Test
	public void testGetFullPath() throws Exception {
		assertEquals(Arrays.asList(createReference(INSTANCE1), createReference(INSTANCE2), createReference(INSTANCE3),
				createReference(INSTANCE4)), contextService.getFullPath(createInstance(INSTANCE4)));
		assertEquals(Arrays.asList(createReference(INSTANCE1)), contextService.getFullPath(createInstance(INSTANCE1)));

	}

	@Test
	public void parentLevel_3() {
		assertFalse(contextService.getContext(createInstance(INSTANCE1)).isPresent());
		assertTrue(contextService.getContext(createInstance(INSTANCE2)).isPresent());
		assertTrue(contextService.getContext(createInstance(INSTANCE3)).isPresent());
	}


	@Test
	public void parentLevel_4() {
		assertFalse(contextService.getContext(createInstance(INSTANCE1)).isPresent());
		assertTrue(contextService.getContext(createInstance(INSTANCE2)).isPresent());
		assertTrue(contextService.getContext(createInstance(INSTANCE3)).isPresent());
		assertTrue(contextService.getContext(createInstance(INSTANCE4)).isPresent());
	}

	@Test
	public void versionInstance() {
		InstanceReference version = createReference("version-instance-id-v1.4");
		Assert.assertFalse(contextService.getContext(version).isPresent());
	}

	@Test
	public void classInstanceRoot() {
		ClassInstance clazz = createClassInstance("emf:subClass");
		clazz.setSuperClasses(Collections.emptyList());
		InstanceReference classInstance = InstanceReferenceMock.createGeneric(clazz);
		assertFalse(contextService.getContext(classInstance).isPresent());
		assertFalse(contextService.getContext(classInstance).isPresent());
		verify(repositoryConnection, times(1)).prepareTupleQuery(any(), any());
	}

	@Test
	public void classInstanceWithSingleSuperClass() {
		ClassInstance clazz = createClassInstance("emf:subClass");
		clazz.setSuperClasses(Arrays.asList(createClassInstance("emf:superClassClass")));
		InstanceReference clazzRef = InstanceReferenceMock.createGeneric(clazz);
		clazzRef.setType(InstanceTypeFake.buildForCategory("classinstance"));
		Optional<InstanceReference> result = contextService.getContext(clazzRef);
		assertTrue(result.isPresent());
		assertEquals(result.get(), createReference("emf:superClassClass"));
		verify(repositoryConnection, times(0)).prepareTupleQuery(any(), any());
		verify(semanticDefinitionService, times(0)).getMostConcreteClass(anyCollection());
	}

	@Test
	public void classInstanceWithSuperClasses() {
		ClassInstance clazz = createClassInstance("emf:subClass");
		clazz.setSuperClasses(Arrays.asList(createClassInstance("emf:superClass1"),
				createClassInstance("emf:superClass2"), createClassInstance("emf:superClassClass")));
		InstanceReference clazzRef = InstanceReferenceMock.createGeneric(clazz);
		clazzRef.setType(InstanceTypeFake.buildForCategory("classinstance"));
		when(semanticDefinitionService.getMostConcreteClass(anyCollection())).thenReturn("emf:superClassClass");
		Optional<InstanceReference> result = contextService.getContext(clazzRef);
		assertTrue(result.isPresent());
		assertEquals(result.get(), createReference("emf:superClassClass"));
		verify(repositoryConnection, times(0)).prepareTupleQuery(any(), any());
		verify(semanticDefinitionService, times(1)).getMostConcreteClass(anyCollection());
	}

	@Test(expectedExceptions = EmfRuntimeException.class)
	public void testBindContextCycle() {
		Instance instance1 = createInstance(INSTANCE1);
		Instance instance3 = createInstance(INSTANCE3);
		// cycle dependency
		contextService.bindContext(instance1, instance3);

	}

	@Test(expectedExceptions = EmfRuntimeException.class)
	public void testSetSameContext() {
		Instance instance1 = createInstance(INSTANCE1);
		contextService.bindContext(instance1, INSTANCE1);
	}

	@Test
	public void testPersistNullContext() {
		Instance instance2 = createInstance(INSTANCE2);
		assertEquals(contextService.getContext(instance2).get(), createReference(INSTANCE1));
		contextService.bindContext(instance2, null);
		assertFalse(contextService.getContext(instance2).isPresent());
	}

	@Test(expectedExceptions = EmfRuntimeException.class)
	public void testPersistCycleContext() {
		Instance instance3 = createInstance(INSTANCE3);
		contextService.bindContext(instance3, createInstance(INSTANCE4));
	}

	private static ClassInstance createClassInstance(String id) {
		ClassInstance clazz = new ClassInstance();
		clazz.setId(id);
		return clazz;
	}

	private static EmfInstance createInstance(Serializable id) {
		return new EmfInstance(id);
	}

	private InstanceReference createReference(String id) {
		return InstanceReferenceMock.createGeneric(createInstance(id));
	}

	@Override
	protected String getTestDataFile() {
		return "SemanticContextServiceTest.ttl";
	}
}