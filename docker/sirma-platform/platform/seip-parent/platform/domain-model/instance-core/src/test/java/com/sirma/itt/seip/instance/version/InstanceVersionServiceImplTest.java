package com.sirma.itt.seip.instance.version;

import static com.sirma.itt.seip.collections.CollectionUtils.emptySet;
import static com.sirma.itt.seip.domain.instance.DefaultProperties.CREATED_ON;
import static com.sirma.itt.seip.domain.instance.DefaultProperties.SEMANTIC_TYPE;
import static com.sirma.itt.seip.domain.instance.DefaultProperties.VERSION;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyCollection;
import static org.mockito.Matchers.anyCollectionOf;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.io.Serializable;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Matchers;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.Spy;

import com.sirma.itt.seip.configuration.ConfigurationProperty;
import com.sirma.itt.seip.convert.TypeConverter;
import com.sirma.itt.seip.convert.TypeConverterUtil;
import com.sirma.itt.seip.domain.instance.ArchivedInstance;
import com.sirma.itt.seip.domain.instance.ArchivedInstanceReference;
import com.sirma.itt.seip.domain.instance.EmfInstance;
import com.sirma.itt.seip.domain.instance.Instance;
import com.sirma.itt.seip.domain.instance.InstanceReference;
import com.sirma.itt.seip.domain.instance.InstanceType;
import com.sirma.itt.seip.exceptions.InstanceNotFoundException;
import com.sirma.itt.seip.instance.InstanceTypeResolver;
import com.sirma.itt.seip.instance.InstanceTypes;
import com.sirma.itt.seip.instance.archive.ArchivedInstanceDao;
import com.sirma.itt.seip.instance.lock.LockInfo;
import com.sirma.itt.seip.instance.lock.LockService;
import com.sirma.itt.seip.instance.lock.exception.LockException;
import com.sirma.itt.seip.instance.state.StateTransitionManager;
import com.sirma.itt.seip.instance.version.revert.RevertContext;
import com.sirma.itt.seip.instance.version.revert.RevertStep;
import com.sirma.itt.seip.mapping.ObjectMapper;
import com.sirma.itt.seip.plugin.Plugins;
import com.sirma.itt.seip.testutil.fakes.InstanceTypeFake;
import com.sirma.itt.seip.testutil.fakes.TransactionSupportFake;
import com.sirma.itt.seip.testutil.mocks.InstanceReferenceMock;
import com.sirma.itt.seip.tx.TransactionSupport;
import com.sirma.sep.content.Content;
import com.sirma.sep.content.ContentInfo;
import com.sirma.sep.content.InstanceContentService;

/**
 * Test for {@link InstanceVersionServiceImpl}.
 *
 * @author A. Kunchev
 */
public class InstanceVersionServiceImplTest {

	@InjectMocks
	private InstanceVersionService service;

	@Mock
	private InstanceTypeResolver instanceTypeResolver;

	@Mock
	private VersionStepsExecutor versionStepsExecutor;

	@Mock
	private ConfigurationProperty<String> initialVersion;

	@Mock
	private ConfigurationProperty<Date> contentHandlingEnabledDate;

	@Mock
	private ConfigurationProperty<Date> revertVersionOperationEnabled;

	@Mock
	private ArchivedInstanceDao archivedInstanceDao;

	@Mock
	private VersionDao versionDao;

	@Mock
	private ObjectMapper objectMapper;

	@Mock
	private InstanceTypes instanceTypes;

	@Mock
	private InstanceContentService instanceContentService;

	@Mock
	private TypeConverter typeConverter;

	@Mock
	private LockService lockService;

	@Mock
	private RevertStep firtsRevertStep;

	@Mock
	private RevertStep secondRevertStep;

	private List<RevertStep> revertStepList = new ArrayList<>();

	@Spy
	private Plugins<RevertStep> revertSteps = new Plugins<>("", revertStepList);

	@Mock
	private StateTransitionManager stateTransitionManager;

	@Spy
	private TransactionSupport transactionSupport = new TransactionSupportFake();

	@Before
	public void setup() {
		service = new InstanceVersionServiceImpl();
		MockitoAnnotations.initMocks(this);
		when(initialVersion.get()).thenReturn("1.0");

		TypeConverterUtil.setTypeConverter(typeConverter);
		when(typeConverter.convert(eq(ArchivedInstanceReference.class), any(Instance.class)))
				.then(a -> new InstanceReferenceMock(a.getArgumentAt(1, Instance.class)));

		// remove me when workarounds are removed
		when(instanceContentService.getContent(any(Serializable.class), eq(Content.PRIMARY_VIEW)))
				.thenReturn(ContentInfo.DO_NOT_EXIST);

		when(contentHandlingEnabledDate.isNotSet()).thenReturn(false);

		Calendar calendar = Calendar.getInstance();
		calendar.setTime(new Date());
		calendar.add(Calendar.DATE, 1);
		when(contentHandlingEnabledDate.get()).thenReturn(calendar.getTime());

		calendar.add(Calendar.DATE, -10);
		when(revertVersionOperationEnabled.get()).thenReturn(calendar.getTime());

		revertStepList.clear();
		revertStepList.add(firtsRevertStep);
		revertStepList.add(secondRevertStep);
	}

	@Test(expected = NullPointerException.class)
	public void createVersion_nullInstance() {
		service.saveVersion(VersionContext.create(null));
	}

	@Test(expected = NullPointerException.class)
	public void createVersion_nullDate() {
		service.saveVersion(VersionContext.create(new EmfInstance(), null));
	}

	@Test
	public void createVersion_withoutInstanceType() {
		Instance instance = new EmfInstance();
		VersionContext context = VersionContext.create(instance);
		service.saveVersion(context);

		verify(versionStepsExecutor, never()).execute(any(VersionContext.class));
	}

	@Test
	public void createVersion_notVersionableInstance() {
		Instance instance = new EmfInstance();
		stubInstanceType(instance, false);
		VersionContext context = VersionContext.create(instance);
		service.saveVersion(context);

		verify(versionStepsExecutor, never()).execute(any(VersionContext.class));
	}

	@Test
	public void createVersion_versionableNewInstance() {
		Instance instance = new EmfInstance();
		instance.setId("instance-id");
		stubInstanceType(instance, true);
		VersionContext context = VersionContext.create(instance);
		service.saveVersion(context);

		verify(versionStepsExecutor).execute(any(VersionContext.class));
	}

	private static void stubInstanceType(Instance instance, boolean versionable) {
		InstanceType type = mock(InstanceType.class);
		when(type.isVersionable()).thenReturn(versionable);
		instance.setType(type);
	}

	@Test
	public void setInitialVersion_nullInstance() {
		service.populateVersion(null);
		verify(initialVersion, never()).get();
	}

	@Test
	public void setInitialVersion_instanceNullVersion() {
		EmfInstance instance = new EmfInstance("emf:instanceId");
		instance.setType(new InstanceType.DefaultInstanceType("id"));
		instance.add(VERSION, null);
		service.populateVersion(instance);
		assertEquals("1.0", instance.getString(VERSION));
	}

	@Test
	public void setInitialVersion_instanceEmptyVersion() {
		EmfInstance instance = new EmfInstance("emf:instanceId");
		instance.setType(new InstanceType.DefaultInstanceType("id"));
		instance.add(VERSION, "");
		service.populateVersion(instance);
		assertEquals("1.0", instance.getString(VERSION));
	}

	@Test
	public void should_notSetVersion_whenNotVersionable() {
		EmfInstance instance = new EmfInstance("emf:instance-id");
		InstanceType type = mock(InstanceType.class);
		when(type.isVersionable()).thenReturn(false);

		instance.setType(type);
		instance.add(VERSION, "");
		boolean result = service.populateVersion(instance);
		assertFalse(result);
		assertEquals("", instance.getString(VERSION));
	}

	@Test
	public void should_populateVersion_when_VersionIsEmpty_instanceHasOlderVersions() {
		EmfInstance instance = new EmfInstance("emf:instanceId");
		instance.setType(new InstanceType.DefaultInstanceType("id"));
		instance.add(VERSION, "");

		ArchivedInstance versionInstance = new ArchivedInstance();
		versionInstance.setVersion("2.5");
		when(versionDao.findVersionsByTargetId(Matchers.any(Serializable.class), Matchers.anyInt(), Matchers.anyInt()))
				.thenReturn(Arrays.asList(versionInstance));
		boolean result = service.populateVersion(instance);
		assertFalse(result);
		assertEquals("2.5", instance.getString(VERSION));
	}

	@Test
	public void setInitialVersion_instanceWithVersion() {
		EmfInstance instance = new EmfInstance("emf:instanceId");
		instance.add(VERSION, "25.5");
		service.populateVersion(instance);
		assertEquals("25.5", instance.getString(VERSION));
	}

	@Test
	public void getInstanceVersions_nullId_emptyCollection() {
		VersionsResponse response = service.getInstanceVersions(null, 0, -1);
		assertTrue(response.isEmpty());
		assertEquals(Collections.emptyList(), response.getResults());
	}

	@Test
	public void getInstanceVersions_emptyId_emptyCollection() {
		VersionsResponse response = service.getInstanceVersions("", 0, 1);
		assertTrue(response.isEmpty());
		assertEquals(Collections.emptyList(), response.getResults());
	}

	@Test
	public void getInstanceVersions_noVersionFound() {
		when(versionDao.getVersionsCount("instance-id")).thenReturn(0);
		VersionsResponse versionsResponse = service.getInstanceVersions("instance-id", 0, -1);
		assertTrue(versionsResponse.isEmpty());
	}

	@Test
	public void getInstanceVersions_successful_internalServiceCalled() {
		ArchivedInstance archivedInstance1 = new ArchivedInstance();
		archivedInstance1.setTargetId("archived-instnace-1");
		archivedInstance1.add(SEMANTIC_TYPE, "testType");
		ArchivedInstance archivedInstance2 = new ArchivedInstance();
		archivedInstance2.setTargetId("archived-instnace-2");
		archivedInstance2.add(SEMANTIC_TYPE, "testType");
		ArchivedInstance archivedInstance3 = new ArchivedInstance();
		archivedInstance3.add(SEMANTIC_TYPE, "testType");
		List<ArchivedInstance> foundVersions = Arrays.asList(archivedInstance1, archivedInstance2,
				archivedInstance3);

		when(versionDao.getVersionsCount("instance-id")).thenReturn(foundVersions.size());
		when(versionDao.findVersionsByTargetId("instance-id", 0, -1)).thenReturn(foundVersions);
		when(objectMapper.map(any(ArchivedInstance.class), any())).thenReturn(new EmfInstance());
		when(instanceTypes.from(any(ArchivedInstance.class)))
				.thenReturn(Optional.of(InstanceTypeFake.build("emf:object", "objectinstance")));

		// resolve only 2 references in #convertToOriginalInstance
		when(typeConverter.convert(any(), any(Instance.class))).thenReturn(new InstanceReferenceMock(new EmfInstance()),
				new InstanceReferenceMock(new EmfInstance()), null);

		VersionsResponse versionsResponse = service.getInstanceVersions("instance-id", 0, -1);

		verify(archivedInstanceDao, times(3)).loadProperties(any(ArchivedInstance.class));
		assertFalse(versionsResponse.isEmpty());
		assertEquals(2, versionsResponse.getResults().size());
	}

	@Test
	public void loadVersionsById_nullInputCollection_emptyCollection() {
		assertEquals(Collections.emptyList(), service.loadVersionsById(null));
	}

	@Test
	public void loadVersionsById_emptyInputCollection_emptyCollection() {
		assertEquals(Collections.emptyList(), service.loadVersionsById(new ArrayList<>()));
	}

	@Test
	public void loadVersionsById_noInstanceFound() {
		when(versionDao.findVersionsById(anyCollectionOf(Serializable.class))).thenReturn(new ArrayList<>());
		Collection<Instance> results = service.loadVersionsById(Arrays.asList("version-id-1", "version-id-2"));
		assertEquals(Collections.emptyList(), results);
	}

	@Test
	public void loadVersionsById_foundOneInstance() {
		ArchivedInstance archivedInstance = new ArchivedInstance();
		archivedInstance.add(SEMANTIC_TYPE, "testType");
		when(versionDao.findVersionsById(anyCollectionOf(Serializable.class)))
				.thenReturn(Arrays.asList(archivedInstance));
		stubArchivedInstanceLoadingAndConverting();

		Collection<Instance> results = service.loadVersionsById(Arrays.asList("version-id-1", "version-id-2"));
		assertEquals(1, results.size());
	}

	@Test
	public void loadVersionsById_ResolveTypeWithMissingSemanticType() {
		ArchivedInstance archivedInstance = new ArchivedInstance();
		archivedInstance.setTargetId("target-id");
		when(versionDao.findVersionsById(anyCollectionOf(Serializable.class)))
				.thenReturn(Collections.singletonList(archivedInstance));
		stubArchivedInstanceLoadingAndConverting();
		when(instanceTypes.from("target-id"))
				.thenReturn(Optional.of(InstanceTypeFake.build("emf:object", "objectinstance")));

		Collection<Instance> results = service.loadVersionsById(Arrays.asList("version-id-1", "version-id-2"));
		assertEquals(1, results.size());
	}

	@Test(expected = IllegalArgumentException.class)
	public void loadVersion_nullId() {
		service.loadVersion(null);
	}

	@Test(expected = IllegalArgumentException.class)
	public void loadVersion_emptyId() {
		service.loadVersion("");
	}

	@Test(expected = InstanceNotFoundException.class)
	public void loadVersion_versionInstanceNotFound() {
		when(instanceTypeResolver.resolveReference(anyString())).thenReturn(Optional.of(new InstanceReferenceMock()));
		when(versionDao.findVersionById("version-instance-id")).thenReturn(Optional.empty());
		service.loadVersion("version-instance-id");
	}

	@Test
	public void loadVersion_instanceFound_noQueriesContentId() {
		when(instanceTypeResolver.resolveReference(anyString())).thenReturn(Optional.of(new InstanceReferenceMock()));
		ArchivedInstance archivedInstance = new ArchivedInstance();
		archivedInstance.setCreatedOn(new Date());
		archivedInstance.add(SEMANTIC_TYPE, "testType");
		when(versionDao.findVersionById("version-instance-id")).thenReturn(Optional.of(archivedInstance));
		stubArchivedInstanceLoadingAndConverting();

		Instance instance = service.loadVersion("version-instance-id");
		assertNotNull(instance);
		assertNull(instance.get(VersionProperties.QUERIES_RESULTS));
	}

	@Test
	public void loadVersion_instanceFound_withQueriesContentId_noContent() {
		ArchivedInstance archivedInstance = new ArchivedInstance();
		archivedInstance.setCreatedOn(new Date());
		archivedInstance.add(SEMANTIC_TYPE, "testType");
		archivedInstance.add(VersionProperties.QUERIES_RESULT_CONTENT_ID, "queries-results-content-id");
		when(versionDao.findVersionById("version-instance-id")).thenReturn(Optional.of(archivedInstance));
		when(instanceTypeResolver.resolveReference(anyString())).thenReturn(Optional.of(new InstanceReferenceMock()));
		when(instanceContentService.getContent("queries-results-content-id", null))
				.thenReturn(ContentInfo.DO_NOT_EXIST);

		stubArchivedInstanceLoadingAndConverting();

		Instance instance = service.loadVersion("version-instance-id");
		assertNull(instance.get(VersionProperties.QUERIES_RESULTS));
	}

	@Test
	public void loadVersion_instanceFound_withQueriesContentId_withEmptyContent() {
		ArchivedInstance archivedInstance = new ArchivedInstance();
		archivedInstance.setCreatedOn(new Date());
		archivedInstance.add(SEMANTIC_TYPE, "testType");
		archivedInstance.add(VersionProperties.QUERIES_RESULT_CONTENT_ID, "queries-results-content-id");
		when(versionDao.findVersionById("version-instance-id")).thenReturn(Optional.of(archivedInstance));
		when(instanceTypeResolver.resolveReference(anyString())).thenReturn(Optional.of(new InstanceReferenceMock()));

		stubContentServiceResult(new ByteArrayInputStream("{}".getBytes(StandardCharsets.UTF_8)));
		stubArchivedInstanceLoadingAndConverting();

		Instance instance = service.loadVersion("version-instance-id");
		assertNull(instance.get(VersionProperties.QUERIES_RESULTS));
	}

	@Test
	public void loadVersion_instanceFound_withQueriesContentId_withContent() {
		ArchivedInstance archivedInstance = new ArchivedInstance();
		archivedInstance.setCreatedOn(new Date());
		archivedInstance.add(SEMANTIC_TYPE, "testType");
		archivedInstance.add(VersionProperties.QUERIES_RESULT_CONTENT_ID, "queries-results-content-id");
		when(versionDao.findVersionById("version-instance-id")).thenReturn(Optional.of(archivedInstance));
		when(instanceTypeResolver.resolveReference(anyString())).thenReturn(Optional.of(new InstanceReferenceMock()));

		Map<Serializable, Serializable> versionMap = new HashMap<>(2);
		versionMap.put("emf:e051c8aa-2041-4d5f-811b-0fd1e0f43275", "emf:e051c8aa-2041-4d5f-811b-0fd1e0f43275-v1.3");
		versionMap.put("emf:b1830899-67cf-4f8f-bb34-436fa114e45a", "emf:b1830899-67cf-4f8f-bb34-436fa114e45a-v1.8");
		when(versionDao.findVersionIdsByTargetIdAndDate(anyCollection(), any(Date.class))).thenReturn(versionMap);

		stubContentServiceResult(
				InstanceVersionServiceImplTest.class.getResourceAsStream("/queries-results-content-test.json"));
		stubArchivedInstanceLoadingAndConverting();

		Instance instance = service.loadVersion("version-instance-id");
		assertNotNull(instance.get(VersionProperties.QUERIES_RESULTS));
	}

	private void stubContentServiceResult(InputStream streamToReturn) {
		ContentInfo contentInfo = mock(ContentInfo.class);
		when(contentInfo.exists()).thenReturn(true);
		when(contentInfo.getInputStream()).thenReturn(streamToReturn);
		when(instanceContentService.getContent("queries-results-content-id", null)).thenReturn(contentInfo);
	}

	private void stubArchivedInstanceLoadingAndConverting() {
		when(objectMapper.map(any(ArchivedInstance.class), any())).then(a -> {
			Map<String, Serializable> properties = a.getArgumentAt(0, Instance.class).getProperties();
			Instance instance = new EmfInstance();
			instance.addAllProperties(properties);
			return instance;
		});
		when(instanceTypes.from(any(ArchivedInstance.class)))
				.thenReturn(Optional.of(InstanceTypeFake.build("emf:object", "objectinstance")));
		when(typeConverter.convert(any(), any(Instance.class)))
				.thenReturn(new InstanceReferenceMock(new EmfInstance()));
	}

	@Test(expected = IllegalArgumentException.class)
	public void delete_nullId() {
		service.deleteVersion(null);
	}

	@Test(expected = IllegalArgumentException.class)
	public void delete_emptyId() {
		service.deleteVersion("");
	}

	@Test
	public void delete_noVersionInstance_internalDeleteNotCalled() {
		when(versionDao.findVersionById("version-id")).thenReturn(Optional.empty());
		service.deleteVersion("version-id");
		verify(archivedInstanceDao, never()).delete(any(ArchivedInstance.class));
	}

	@Test
	public void delete_successful() {
		ArchivedInstance version = new ArchivedInstance();
		when(versionDao.findVersionById("version-id")).thenReturn(Optional.of(version));
		service.deleteVersion("version-id");
		verify(archivedInstanceDao).delete(version);
	}

	@Test(expected = NullPointerException.class)
	public void revertVersion_nullContext_NPE() {
		service.revertVersion(null);
	}

	@Test(expected = InstanceNotFoundException.class)
	public void revertVersion_currentInstanceNotFound_instanceNotFoundException() {
		when(instanceTypeResolver.resolveReference("instance-id")).thenReturn(Optional.empty());
		service.revertVersion(RevertContext.create("instance-id-v1.6"));
	}

	@Test(expected = LockException.class)
	public void revertVersion_currentInstanceLocked_lockException() {
		InstanceReferenceMock instanceReference = InstanceReferenceMock.createGeneric("instance-id");
		when(instanceTypeResolver.resolveReference("instance-id")).thenReturn(Optional.of(instanceReference));
		LockInfo lockInfo = mock(LockInfo.class);
		when(lockInfo.isLocked()).thenReturn(true);
		when(lockService.lockStatus(instanceReference)).thenReturn(lockInfo);
		service.revertVersion(RevertContext.create("instance-id-v1.6"));
	}

	@Test
	public void revertVersion_failWhileReverting_runtimeExceptionPlusRollback() {
		try {
			InstanceReferenceMock instanceReference = InstanceReferenceMock.createGeneric("instance-id");
			when(instanceTypeResolver.resolveReference("instance-id")).thenReturn(Optional.of(instanceReference));
			LockInfo lockInfo = mock(LockInfo.class);
			when(lockInfo.isLocked()).thenReturn(false);
			when(lockService.lockStatus(instanceReference)).thenReturn(lockInfo);

			doThrow(new RuntimeException()).when(secondRevertStep).invoke(any(RevertContext.class));
			service.revertVersion(RevertContext.create("instance-id-v1.6"));
		} catch (Exception e) {
			assertTrue(e instanceof RuntimeException);
		} finally {
			verify(transactionSupport).invokeOnFailedTransactionInTx(any()); // verifies rollback registration
			verify(lockService).tryUnlock(any(InstanceReference.class));
		}
	}

	@Test
	public void revertVersion_successful() {
		InstanceReferenceMock instanceReference = InstanceReferenceMock.createGeneric("instance-id");
		when(instanceTypeResolver.resolveReference("instance-id")).thenReturn(Optional.of(instanceReference));
		LockInfo lockInfo = mock(LockInfo.class);
		when(lockInfo.isLocked()).thenReturn(false);
		when(lockService.lockStatus(instanceReference)).thenReturn(lockInfo);

		service.revertVersion(RevertContext.create("instance-id-v1.6"));

		verify(firtsRevertStep, never()).rollback(any(RevertContext.class));
		verify(lockService).tryUnlock(any(InstanceReference.class));
	}

	@Test(expected = NullPointerException.class)
	public void isRevertOperationAllowed_nullInstance() {
		service.isRevertOperationAllowed(null);
	}

	@Test(expected = IllegalArgumentException.class)
	public void isRevertOperationAllowed_nullId() {
		Instance target = new EmfInstance();
		target.setId(null);
		service.isRevertOperationAllowed(target);
	}

	@Test(expected = IllegalArgumentException.class)
	public void isRevertOperationAllowed_emptyId() {
		Instance target = new EmfInstance();
		target.setId("");
		service.isRevertOperationAllowed(target);
	}

	@Test
	public void isRevertOperationAllowed_notVersionInstance_returnFalse() {
		Instance target = new EmfInstance();
		target.setId("instance-id");
		assertFalse(service.isRevertOperationAllowed(target));
	}

	@Test
	public void isRevertOperationAllowed_loadedVersionInstanceBeforeConfigDate_returnFalse() {
		Calendar versionCalendar = Calendar.getInstance();
		versionCalendar.setTime(new Date());
		versionCalendar.add(Calendar.YEAR, -1);
		ArchivedInstance version = new ArchivedInstance();
		version.setId("instance-id-v1.6");
		version.setCreatedOn(versionCalendar.getTime());
		when(versionDao.findVersionById("instance-id-v1.6")).thenReturn(Optional.of(version));

		Instance target = new EmfInstance();
		target.setId("instance-id-v1.6");
		assertFalse(service.isRevertOperationAllowed(target));
	}

	@Test
	public void isRevertOperationAllowed_afterRevertConfigOriginalInstanceNotFound_returnFalse() {
		ArchivedInstance version = new ArchivedInstance();
		version.setId("instance-id-v1.6");
		version.setCreatedOn(new Date());
		when(versionDao.findVersionById("instance-id-v1.6")).thenReturn(Optional.of(version));
		when(instanceTypeResolver.resolveReference("instance-id")).thenReturn(Optional.empty());

		Instance target = new EmfInstance();
		target.setId("instance-id-v1.6");
		assertFalse(service.isRevertOperationAllowed(target));
	}

	@Test
	public void isRevertOperationAllowed_afterRevertConfigOperationNotAllowed_returnFalse() {
		ArchivedInstance version = new ArchivedInstance();
		version.setId("instance-id-v1.6");
		version.setCreatedOn(new Date());
		when(versionDao.findVersionById("instance-id-v1.6")).thenReturn(Optional.of(version));
		when(instanceTypeResolver.resolveReference("instance-id"))
				.thenReturn(Optional.of(InstanceReferenceMock.createGeneric("instance-id")));
		when(stateTransitionManager.getAllowedOperations(any(Instance.class), anyString())).thenReturn(emptySet());

		Instance target = new EmfInstance();
		target.setId("instance-id-v1.6");
		assertFalse(service.isRevertOperationAllowed(target));
	}

	@Test
	public void isRevertOperationAllowed_afterRevertConfigOperationAllowed_returnTrue() {
		ArchivedInstance version = new ArchivedInstance();
		version.setId("instance-id-v1.6");
		version.setCreatedOn(new Date());
		when(versionDao.findVersionById("instance-id-v1.6")).thenReturn(Optional.of(version));
		InstanceReferenceMock referenceMock = InstanceReferenceMock.createGeneric("instance-id");
		referenceMock.toInstance().add("status", "DRAFT");
		when(instanceTypeResolver.resolveReference("instance-id")).thenReturn(Optional.of(referenceMock));
		when(stateTransitionManager.getAllowedOperations(any(Instance.class), eq("DRAFT")))
				.thenReturn(Collections.singleton("revertVersion"));

		Instance target = new EmfInstance();
		target.setId("instance-id-v1.6");
		assertTrue(service.isRevertOperationAllowed(target));
	}

	// tests corner case for old versions. Its a test for workaround that will be removed.
	@Test
	public void isRevertOperationAllowed_missingCreatedOnDate_afterRevertConfigOperationAllowed_returnTrue() {
		ArchivedInstance version = new ArchivedInstance();
		version.setId("instance-id-v1.6");
		version.add(CREATED_ON, new Date());
		when(versionDao.findVersionById("instance-id-v1.6")).thenReturn(Optional.of(version));
		InstanceReferenceMock referenceMock = InstanceReferenceMock.createGeneric("instance-id");
		referenceMock.toInstance().add("status", "DRAFT");
		when(instanceTypeResolver.resolveReference("instance-id")).thenReturn(Optional.of(referenceMock));
		when(stateTransitionManager.getAllowedOperations(any(Instance.class), eq("DRAFT")))
				.thenReturn(Collections.singleton("revertVersion"));

		Instance target = new EmfInstance();
		target.setId("instance-id-v1.6");
		assertTrue(service.isRevertOperationAllowed(target));
	}

	@Test
	public void hasInitialVersion_nullInstance() {
		assertFalse(service.hasInitialVersion(null));
	}

	@Test
	public void hasInitialVersion_noVersionProperty() {
		assertFalse(service.hasInitialVersion(new EmfInstance()));
	}

	@Test
	public void hasInitialVersion_withNullValueForVersionProperty() {
		EmfInstance target = new EmfInstance();
		target.add(VERSION, null);
		assertFalse(service.hasInitialVersion(target));
	}

	@Test
	public void hasInitialVersion_withVersionProperty_notAsInitial() {
		EmfInstance target = new EmfInstance();
		target.add(VERSION, "2.13");
		assertFalse(service.hasInitialVersion(target));
	}

	@Test
	public void hasInitialVersion_withVersionProperty_asInitial() {
		EmfInstance target = new EmfInstance();
		target.add(VERSION, "1.0");
		assertTrue(service.hasInitialVersion(target));
	}
}