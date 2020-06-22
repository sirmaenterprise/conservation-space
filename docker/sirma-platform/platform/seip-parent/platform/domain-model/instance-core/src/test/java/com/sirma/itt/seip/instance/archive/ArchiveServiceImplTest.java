package com.sirma.itt.seip.instance.archive;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyCollection;
import static org.mockito.Matchers.anyCollectionOf;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.atLeast;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.assertTrue;

import java.io.Serializable;
import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import com.sirma.itt.seip.configuration.ConfigurationProperty;
import com.sirma.itt.seip.convert.TypeConverter;
import com.sirma.itt.seip.domain.instance.ArchivedInstance;
import com.sirma.itt.seip.domain.instance.EmfInstance;
import com.sirma.itt.seip.domain.instance.Instance;
import com.sirma.itt.seip.domain.instance.InstanceReference;
import com.sirma.itt.seip.event.EventService;
import com.sirma.itt.seip.instance.InstanceTypeResolver;
import com.sirma.itt.seip.instance.actions.InstanceOperations;
import com.sirma.itt.seip.instance.dao.InstanceDao;
import com.sirma.itt.seip.instance.dao.ServiceRegistry;
import com.sirma.itt.seip.instance.lock.LockInfo;
import com.sirma.itt.seip.instance.lock.LockService;
import com.sirma.itt.seip.instance.lock.PredefinedLockTypes;
import com.sirma.itt.seip.instance.lock.exception.LockException;
import com.sirma.itt.seip.instance.state.Operation;
import com.sirma.itt.seip.mapping.ObjectMapper;
import com.sirma.itt.seip.search.SearchService;
import com.sirma.itt.seip.tasks.DefaultSchedulerConfiguration;
import com.sirma.itt.seip.tasks.SchedulerConfiguration;
import com.sirma.itt.seip.tasks.SchedulerContext;
import com.sirma.itt.seip.tasks.SchedulerEntryType;
import com.sirma.itt.seip.tasks.SchedulerService;
import com.sirma.itt.seip.tasks.TransactionMode;
import com.sirma.itt.seip.testutil.EmfTest;
import com.sirma.itt.seip.testutil.fakes.DbIdGeneratorFake;
import com.sirma.itt.seip.testutil.mocks.ConfigurationPropertyMock;
import com.sirma.itt.seip.testutil.mocks.InstanceReferenceMock;

/**
 * Test for ArchiveServiceImpl
 *
 * @author BBonev
 */
@Test
public class ArchiveServiceImplTest extends EmfTest {

	private static final Operation DELETE = new Operation("delete");
	@Mock
	private SchedulerService schedulerService;
	@Spy
	private TransactionIdHolder idHolder = new TransactionIdHolder(new DbIdGeneratorFake());
	@Mock
	private ObjectMapper mapper;
	@Mock
	private InstanceOperations operationInvoker;
	@Mock
	private InstanceDao archivedInstanceDao;
	@Mock
	private ServiceRegistry serviceRegistry;
	@Mock
	private EventService eventService;
	@Mock
	private InstanceDao dao;
	@Mock
	private TypeConverter typeConverter;
	@Mock
	private SearchService searchService;
	@Mock
	private InstanceTypeResolver instanceTypeResolver;
	@Mock
	private LockService lockService;

	@Spy
	private ConfigurationProperty<Boolean> syncDelete = new ConfigurationPropertyMock<>(Boolean.FALSE);
	@Spy
	private ConfigurationProperty<Integer> deleteRetries = new ConfigurationPropertyMock<>(5);
	@Spy
	private ConfigurationProperty<Long> retriesTimeout = new ConfigurationPropertyMock<>(120L);

	@InjectMocks
	private ArchiveServiceImpl service;
	DefaultSchedulerConfiguration configuration = new DefaultSchedulerConfiguration();

	@Override
	@BeforeMethod
	public void beforeMethod() {
		super.beforeMethod();
		createTypeConverter();
		when(schedulerService.buildEmptyConfiguration(SchedulerEntryType.TIMED)).thenReturn(configuration);
		when(serviceRegistry.getInstanceDao(any(Instance.class))).thenReturn(dao);
		when(instanceTypeResolver.resolveReferences(anyCollection())).thenAnswer(a -> a
				.getArgumentAt(0, Collection.class)
					.stream()
					.map(id -> InstanceReferenceMock.createGeneric((String) id))
					.collect(Collectors.toList()));
	}

	@AfterMethod
	public void clean() {
		idHolder.clearCurrentId();
	}

	@SuppressWarnings("unchecked")
	public void testScheduleDelete_noTransaction() {
		EmfInstance instance = new EmfInstance();
		instance.setId("emf:testId");
		instance.setProperties(new HashMap<String, Serializable>());

		ArchivedInstance archived = new ArchivedInstance();
		archived.setId(instance.getId());

		when(mapper.map(instance, ArchivedInstance.class)).thenReturn(archived);

		when(searchService.stream(any(), any())).thenReturn(Stream.of("emf:id-1", "emf:id-2"), Stream.of("emf:id-1", "emf:id-3", "emf:id-4"));
		// no transaction should be active before the call
		assertFalse(idHolder.isTransactionActive());

		ArchiveService.InstanceDeleteResult deleteResult = service.scheduleDelete(instance, DELETE);
		assertTrue(deleteResult.getDeletedInstances().containsAll(Arrays.asList("emf:id-1", "emf:id-2")));
		assertTrue(deleteResult.getRelatedInstances().containsAll(Arrays.asList("emf:id-3", "emf:id-4")), "The related instances should not include a deleted instance");
		assertFalse(deleteResult.getRelatedInstances().containsAll(Arrays.asList("emf:id-1", "emf:id-2")), "The related instances should not include a deleted instance");

		verify(schedulerService).schedule(eq(DeleteInstanceSchedulerExecutor.BEAN_ID),
				any(SchedulerConfiguration.class), any(SchedulerContext.class));

		assertTrue(configuration.isRemoveOnSuccess());
		assertTrue(configuration.isPersistent());
		assertFalse(configuration.isSynchronous());
		assertEquals(configuration.getTransactionMode(), TransactionMode.NOT_SUPPORTED);
		assertNotNull(configuration.getScheduleTime());
		assertTrue(configuration.getMaxRetryCount() > 0);

		verify(eventService).fire(any(ArchivedInstanceAddedEvent.class));
		verify(searchService, times(2)).getFilter(any(), any(), any());
		verify(searchService, times(2)).stream(any(), any());
		// no transaction should be active after the call
		assertFalse(idHolder.isTransactionActive());
	}

	public void testScheduleDelete_withTransaction() {
		EmfInstance instance = new EmfInstance();
		instance.setId("emf:testId");
		instance.setProperties(new HashMap<String, Serializable>());

		ArchivedInstance archived = new ArchivedInstance();
		archived.setId(instance.getId());

		when(typeConverter.convert(ArchivedInstance.class, instance)).thenReturn(archived);

		idHolder.setTransactionId("emf:transactionId");

		service.scheduleDelete(instance, DELETE);

		// transaction should be active after the call - the method should not change the state
		assertTrue(idHolder.isTransactionActive());

		verify(operationInvoker).invokeDelete(eq(instance), eq(DELETE));
		verify(archivedInstanceDao).persistChanges(any(ArchivedInstance.class));
	}

	@Test(expectedExceptions = LockException.class)
	public void testScheduleDelete_withSystemLockedInstance() {
		EmfInstance instance = new EmfInstance("emf:testId");
		instance.setProperties(new HashMap<String, Serializable>());

		ArchivedInstance archived = new ArchivedInstance();
		archived.setId(instance.getId());

		when(typeConverter.convert(ArchivedInstance.class, instance)).thenReturn(archived);
		when(searchService.stream(any(), any())).thenReturn(Stream.of("emf:testId"));
		when(lockService.lockStatus(anyCollectionOf(InstanceReference.class)))
				.then(a -> a.getArgumentAt(0, Collection.class).stream().collect(
						Collectors.toMap(Function.identity(), ir -> new LockInfo((InstanceReference) ir, "user",
								new Date(), PredefinedLockTypes.SYSTEM.getType(), user -> true))));

		service.scheduleDelete(instance, DELETE);
	}

	public void testDelete() {
		EmfInstance instance = new EmfInstance();
		instance.setId("emf:testId");
		instance.setProperties(new HashMap<String, Serializable>());

		ArchivedInstance archived = new ArchivedInstance();
		archived.setId(instance.getId());
		archived.setDeletedOn(new Date());

		when(typeConverter.convert(ArchivedInstance.class, instance)).thenReturn(archived);

		service.delete(instance, DELETE);

		verify(operationInvoker).invokeDelete(eq(instance), eq(DELETE));
		verify(archivedInstanceDao).persistChanges(any(ArchivedInstance.class));
		verify(dao).delete(eq(instance));

		assertNotNull(archived.getDeletedOn());

		// execute without archive
		service.delete(instance, DELETE, false);

		verify(operationInvoker, atLeast(2)).invokeDelete(eq(instance), eq(DELETE));
		verify(archivedInstanceDao).persistChanges(any(ArchivedInstance.class));
		verify(dao, atLeast(2)).delete(eq(instance));
	}

	public void loadByDbId() {
		List<String> ids = Arrays.asList("id");
		service.loadByDbId(ids);
		verify(archivedInstanceDao).loadInstancesByDbKey(ids);
	}

	public void loadByDbIdOverloaded() {
		List<String> ids = Arrays.asList("id");
		service.loadByDbId(ids, true);
		verify(archivedInstanceDao).loadInstancesByDbKey(ids, true);
	}

}
