/**
 *
 */
package com.sirma.itt.seip.resources.downloads;

import static com.sirma.itt.seip.domain.instance.DefaultProperties.MARKED_FOR_DOWNLOAD;
import static com.sirma.itt.seip.domain.instance.DefaultProperties.PRIMARY_CONTENT_ID;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyCollection;
import static org.mockito.Matchers.anySet;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.Serializable;
import java.lang.annotation.Annotation;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

import org.junit.Assert;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import com.sirma.itt.seip.collections.LinkIterable;
import com.sirma.itt.seip.domain.instance.DefaultProperties;
import com.sirma.itt.seip.domain.instance.EmfInstance;
import com.sirma.itt.seip.domain.instance.Instance;
import com.sirma.itt.seip.domain.instance.InstanceReference;
import com.sirma.itt.seip.event.EmfEvent;
import com.sirma.itt.seip.event.EventService;
import com.sirma.itt.seip.instance.relation.LinkConstants;
import com.sirma.itt.seip.instance.relation.LinkReference;
import com.sirma.itt.seip.instance.relation.LinkService;
import com.sirma.itt.seip.testutil.EmfTest;

/**
 * General test for DownloadsServiceImpl.
 *
 * @author A. Kunchev
 */
@Test
public class DownloadsServiceImplTest extends EmfTest {

	private static final String DMS_ID = "a1cd2002-d6c2-4300-b285-6899379fee74";

	private static final String TEST_DEFAULT_HEADER = "<span class=\"truncate-element\"><span class=\"glyphicons"
			+ " disk_save unmarked\" title=\"Add to downloads\" data-instanceId=\"${id.db}\"></span><span class=\"glyphicons"
			+ " dislikes favourites\" title=\"Add to favourites\" data-instanceId=\"emf:f5ea2f20-94e5-45c1-9ba0-"
			+ "0adef7ec3493\" ></span><a class=\"SUBMITTED emf:f5ea2f20-94e5-45c1-9ba0-0adef7ec3493 instance-link"
			+ " has-tooltip\" href=\"/emf/entity/open.jsf?type=projectinstance&instanceId=emf:f5ea2f20-94e5-45c1-9ba0"
			+ "-0adef7ec3493\" uid=\"282\"><b><span data-property=\"identifier\">282</span><span data-property=\"type\""
			+ "> (Project for testing)</span><span data-property=\"title\"> Alabala Starts everywhere</span><span "
			+ "data-property=\"status\"> (Submitted)</span></b></a></span><br /><span><label>Собственик: </label><span "
			+ "data-property=\"owner\"><a href=\"javascript:void(0)\">admin admin</a></span></span><span><label>, "
			+ "Създаден на: </label><span data-property=\"createdOn\">25.06.2015, 00:00</spann></span>";

	@InjectMocks
	private DownloadsServiceImpl service = new DownloadsServiceImpl();

	@Mock
	private LinkService chainingLinkService;

	@Mock
	private LinkService linkService;

	@Mock
	private EventService eventService;

	@Mock
	private DownloadsAdapterService downloadsAdapterService;

	/**
	 * Initialise test mocks and data.
	 */
	@BeforeClass
	public void setup() {
		MockitoAnnotations.initMocks(this);
		setUpSecurityContext();
		createTypeConverter();
		doNothing().when(eventService).fire(any(EmfEvent.class), any(Annotation.class));
	}

	// --------------------------------------------------------------------------
	// -------------------------------- ADD -------------------------------------
	// --------------------------------------------------------------------------

	/**
	 * <pre>
	 * Method: add(InstanceReference)
	 *
	 * InstanceReference: null
	 *
	 * Expected result  : false
	 * </pre>
	 */
	public void add_nullParam_resultFalse() {
		boolean result = service.add(null);
		assertEquals(false, result);
	}

	/**
	 * <pre>
	 * Method: add(InstanceReference)
	 *
	 * InstanceReference : not null
	 * Link              : created
	 *
	 * Expected result   : true
	 * </pre>
	 */
	public void add_notNullParam_linkCreated_resultTrue() {
		InstanceReference instanceReference = mock(InstanceReference.class);
		when(chainingLinkService.linkSimple(any(InstanceReference.class), any(InstanceReference.class), anyString()))
				.thenReturn(true);
		boolean result = service.add(instanceReference);
		assertEquals(true, result);
	}

	/**
	 * <pre>
	 * Method: add(InstanceReference)
	 *
	 * InstanceReference : not null
	 * Link              : not created
	 *
	 * Expected result   : false
	 * </pre>
	 */
	public void add_notNullParam_linkNotCreated_resultFalse() {
		InstanceReference instanceReference = mock(InstanceReference.class);
		when(chainingLinkService.linkSimple(any(InstanceReference.class), any(InstanceReference.class), anyString()))
				.thenReturn(false);
		boolean result = service.add(instanceReference);
		assertEquals(false, result);
	}

	/**
	 * <pre>
	 * Method: add(InstanceReference, UserInstanceReference)
	 *
	 * InstanceReference     : null
	 * UserInstanceReference : not null
	 *
	 * Expected result       : false
	 * </pre>
	 */
	public void addOverloaded_nullFirstParam_resultFalse() {
		InstanceReference instanceReference = mock(InstanceReference.class);
		boolean result = service.add(null, instanceReference);
		assertEquals(false, result);
	}

	/**
	 * <pre>
	 * Method: add(InstanceReference, UserInstanceReference)
	 *
	 * InstanceReference     : not null
	 * UserInstanceReference : null
	 *
	 * Expected result       : false
	 * </pre>
	 */
	public void addOverloaded_nullSecondParam_resultFalse() {
		InstanceReference instanceReference = mock(InstanceReference.class);
		boolean result = service.add(instanceReference, null);
		assertEquals(false, result);
	}

	/**
	 * <pre>
	 * Method: add(InstanceReference, UserInstanceReference)
	 *
	 * InstanceReference     : null
	 * UserInstanceReference : null
	 *
	 * Expected result       : false
	 * </pre>
	 */
	public void addOverloaded_nullBothParams_resultFalse() {
		boolean result = service.add(null, null);
		assertEquals(false, result);
	}

	// --------------------------------------------------------------------------
	// ----------------------------- REMOVE -------------------------------------
	// --------------------------------------------------------------------------

	/**
	 * <pre>
	 * Method: remove(InstanceReference)
	 *
	 * InstanceReference : null
	 *
	 * Expected result   : LinkService.unlinkSimple never called
	 * </pre>
	 */
	public void remove_nullParam() {
		service.remove(null);
		verify(chainingLinkService, never()).unlinkSimple(any(InstanceReference.class), anyString());
	}

	/**
	 * <pre>
	 * Method: remove(InstanceReference, UserInstanceReference)
	 *
	 * InstanceReference     : null
	 * UserInstanceReference : not null
	 *
	 * Expected result       : LinkService.unlinkSimple never called
	 * </pre>
	 */
	public void removeOverloaded_nullFirstParam() {
		InstanceReference user = mock(InstanceReference.class);
		service.remove(null, user);
		verify(chainingLinkService, never()).unlinkSimple(any(InstanceReference.class), any(InstanceReference.class),
				anyString());
	}

	/**
	 * <pre>
	 * Method: remove(InstanceReference, UserInstanceReference)
	 *
	 * InstanceReference     : not null
	 * UserInstanceReference : null
	 *
	 * Expected result       : LinkService.unlinkSimple never called
	 * </pre>
	 */
	public void removeOverloaded_nullSecondParam() {
		InstanceReference instance = mock(InstanceReference.class);
		service.remove(instance, null);
		verify(chainingLinkService, never()).unlinkSimple(any(InstanceReference.class), any(InstanceReference.class),
				anyString());
	}

	/**
	 * <pre>
	 * Method: remove(InstanceReference, UserInstanceReference)
	 *
	 * InstanceReference     : null
	 * UserInstanceReference : null
	 *
	 * Expected result       : LinkService.unlinkSimple never called
	 * </pre>
	 */
	public void removeOverloaded_nullBothParams() {
		service.remove(null, null);
		verify(chainingLinkService, never()).unlinkSimple(any(InstanceReference.class), any(InstanceReference.class),
				anyString());
	}

	/**
	 * <pre>
	 * Method: remove(InstanceReference, UserInstanceReference)
	 *
	 * InstanceReference     : not null
	 * UserInstanceReference : not null
	 *
	 * Expected result       : LinkService.unlinkSimple called at least once
	 * </pre>
	 */
	public void removeOverloaded_NotNullParams() {
		InstanceReference reference = mock(InstanceReference.class);
		InstanceReference user = mock(InstanceReference.class);
		service.remove(reference, user);
		verify(chainingLinkService, atLeastOnce()).unlinkSimple(user, reference, LinkConstants.MARKED_FOR_DOWNLOAD);
	}

	/**
	 * <pre>
	 * Method: removeAll(UserInstanceReference)
	 *
	 * UserInstanceReference : null
	 *
	 * Expected result       : ChainingLinkService.removeLinksFor never called
	 * Operation result      : false
	 * </pre>
	 */
	public void removeAll_nullUser_false() {
		boolean actual = service.removeAll(null);
		verify(chainingLinkService, never()).removeLinksFor(any(InstanceReference.class), anySet());
		assertEquals(false, actual);
	}

	/**
	 * <pre>
	 * Method: removeAll(UserInstanceReference)
	 *
	 * UserInstanceReference : not null
	 * Instance in downloads : none
	 *
	 * Expected result       : ChainingLinkService.removeLinksFor at least once
	 * Operation result      : false
	 * </pre>
	 */
	@Test(enabled = false)
	public void removeAll_notNullUser_false() {
		boolean actual = service.removeAll();
		verify(chainingLinkService, atLeastOnce()).removeLinksFor(any(InstanceReference.class), anySet());
		assertEquals(false, actual);
	}

	/**
	 * Temp test.
	 */
	public void removeAll_notNullUser_trueNoLinks_temp() {
		boolean actual = service.removeAll();
		verify(chainingLinkService, never()).unlinkSimple(any(InstanceReference.class), any(InstanceReference.class),
				anyString());
		assertEquals(true, actual);
	}

	/**
	 * <pre>
	 * Method: removeAll(UserInstanceReference)
	 *
	 * UserInstanceReference : not null
	 * Instance in downloads : at least one
	 *
	 * Expected result       : ChainingLinkService.removeLinksFor at least once
	 * Operation result      : true
	 * </pre>
	 */
	@Test(enabled = false)
	public void removeAll_notNullUser_true() {
		when(chainingLinkService.removeLinksFor(any(InstanceReference.class), anySet())).thenReturn(true);
		boolean actual = service.removeAll();
		verify(chainingLinkService, atLeastOnce()).removeLinksFor(any(InstanceReference.class), anySet());
		assertEquals(true, actual);
	}

	// --------------------------------------------------------------------------
	// ----------------------------- GET ----------------------------------------
	// --------------------------------------------------------------------------

	/**
	 * <pre>
	 * Method: getAll
	 *
	 * Download instances : no
	 *
	 * Expected result     : empty collection
	 * </pre>
	 */
	public void getAllForCurrentUser_noInstances_resultEmptyList() {
		Collection<InstanceReference> instances = service.getAll();
		verify(linkService, atLeastOnce()).getSimpleLinks(any(InstanceReference.class), anyString());
		assertEquals(new LinkIterable<>(Collections.emptySet(), false).size(), instances.size());
	}

	/**
	 * <pre>
	 * Method: getAll
	 *
	 * Download instances : one instance
	 *
	 * Expected result     : collection with one instance
	 * </pre>
	 */
	public void getAll_notEmptyDownloadInstances_resultAtLeastOne() {
		InstanceReference instanceReference = mock(InstanceReference.class);
		LinkReference linkReference = new LinkReference();
		linkReference.setTo(instanceReference);
		when(linkService.getSimpleLinks(any(InstanceReference.class), anyString()))
				.thenReturn(Arrays.asList(linkReference));
		Collection<InstanceReference> instances = service.getAll();
		assertEquals(new LinkIterable<>(Arrays.asList(linkReference)).size(), instances.size());
	}

	/**
	 * <pre>
	 * Method: getAll(UserInstanceReference)
	 *
	 * UserInstanceReference : null
	 *
	 * Expected result       : empty collection
	 * </pre>
	 */
	public void getAll_noUser_resultEmptyList() {
		Collection<InstanceReference> instances = service.getAll(null);
		assertEquals(Collections.emptyList(), instances);
	}

	// --------------------------------------------------------------------------
	// -------------------- UPDATE DOWNLOADS STATE ------------------------------
	// --------------------------------------------------------------------------

	/**
	 * <pre>
	 * Method: updateDownlaodStateForInstance(Instance)
	 *
	 * Instance        : null
	 *
	 * Expected result : null
	 * </pre>
	 */
	public void updateDownlaodStateForInstance_nullInstance_resultNull() {
		Instance instance = service.updateDownloadStateForInstance(null);
		assertNull(instance);
	}

	/**
	 * <pre>
	 * Method: updateDownlaodStateForInstance(Instance)
	 *
	 * Instance        : not null
	 * Instance type   : other
	 * In downloads    : false
	 *
	 * Expected result : unchanged instance
	 * </pre>
	 */
	public void updateDownloadStateForInstance_notNull_notDocument_sameInstance() {
		EmfInstance emfInstance = new EmfInstance();
		emfInstance.setIdentifier("123");
		when(linkService.getSimpleLinks(any(InstanceReference.class), anyString())).thenReturn(Collections.emptyList());
		EmfInstance sameInstance = service.updateDownloadStateForInstance(emfInstance);
		verify(linkService, never()).getSimpleLinks(any(InstanceReference.class), anyString());
		assertEquals(emfInstance, sameInstance);
	}

	/**
	 * <pre>
	 * Method: updateDownlaodStateForInstance(Instance)
	 *
	 * Instance        : not null
	 * Instance type   : document
	 * In downloads    : false
	 *
	 * Expected result : unchanged instance
	 * </pre>
	 */
	public void updateDownlaodStateForInstance_notInDownload_sameInstance() {
		EmfInstance emfInstance = new EmfInstance();
		emfInstance.setIdentifier("123");
		// for for download
		emfInstance.add(PRIMARY_CONTENT_ID, "some content id");
		when(linkService.getSimpleLinks(any(InstanceReference.class), anyString())).thenReturn(Collections.emptyList());
		EmfInstance sameInstance = service.updateDownloadStateForInstance(emfInstance);
		verify(linkService, atLeastOnce()).getSimpleLinks(any(InstanceReference.class), anyString());
		assertEquals(emfInstance, sameInstance);
	}

	/**
	 * <pre>
	 * Method: updateDownlaodStateForInstance(Instance)
	 *
	 * Instance        : not null
	 * Instance type   : document
	 * In downloads    : true
	 *
	 * Expected result : updated instance
	 * </pre>
	 */
	public void updateDownlaodStateForInstance_notNullParams_updateInstance() {
		EmfInstance instance = prepareInstance();
		// for for download
		instance.add(PRIMARY_CONTENT_ID, "some content id");

		List<Instance> list = Arrays.asList(instance);
		service.updateDownloadStateForInstances(list);
		Instance updatedInstance = service.updateDownloadStateForInstance(instance);
		verify(linkService, atLeastOnce()).getSimpleLinks(any(InstanceReference.class), anyString());
		Assert.assertNotNull(updatedInstance);
		assertEquals(instance.get(MARKED_FOR_DOWNLOAD), Boolean.TRUE);
	}

	/**
	 * <pre>
	 * Method: updateDownlaodStateForInstances(Collection)
	 *
	 * Collection      : empty
	 *
	 * Expected result : LinkService.getSimplelinks never called
	 * </pre>
	 */
	public void updateDownlaodStateForInstances_emptyCollection() {
		service.updateDownloadStateForInstances(Collections.emptyList());
		verify(linkService, never()).getSimpleLinks(any(InstanceReference.class), anyString());
	}

	/**
	 * <pre>
	 * Method: updateDownlaodStateForInstances(Collection)
	 *
	 * Collection      : null
	 *
	 * Expected result : LinkService.getSimplelinks never called
	 * </pre>
	 */
	public void updateDownlaodStateForInstances_nullCollection() {
		service.updateDownloadStateForInstances(null);
		verify(linkService, never()).getSimpleLinks(any(InstanceReference.class), anyString());
	}

	/**
	 * <pre>
	 * Method: updateDownlaodState(Collection, UserInstanceReference)
	 *
	 * Collection            : with one instance
	 * UserInstanceReference : null
	 *
	 * Expected result       : LinkService.getSimplelinks never called
	 * </pre>
	 */
	public void updateDownloadsStateForInstancesOverloaded_nullUser() {
		List<Instance> list = Arrays.asList(new EmfInstance());
		service.updateDownloadStateForInstances(list, null);
		verify(linkService, never()).getSimpleLinks(any(InstanceReference.class), anyString());
	}

	/**
	 * <pre>
	 * Method: updateDownlaodStateForInstances(Collection, UserInstanceReference)
	 *
	 * Collection            : with one instance
	 * UserInstanceReference : not null
	 *
	 * Expected result       : LinkService.getSimplelinks called at least once
	 * </pre>
	 */
	public void updateDownlaodStateForInstancesOverloaded_notNullBothParams() {
		EmfInstance instance = prepareInstance();
		// for for download
		instance.add(PRIMARY_CONTENT_ID, "some content id");
		List<Instance> list = Arrays.asList(instance);
		service.updateDownloadStateForInstances(list);
		verify(linkService, atLeastOnce()).getSimpleLinks(any(InstanceReference.class), anyString());
	}

	/**
	 * <pre>
	 * Method: updateDownlaodStateForInstances(Collection, UserInstanceReference)
	 *
	 * Collection            : with one instance
	 * UserInstanceReference : not null
	 *
	 * Expected result       : LinkService.getSimplelinks called at least once
	 * </pre>
	 */
	public void updateDownlaodStateForInstancesOverloaded_notNullBothParams_noDocumentPassed() {
		List<Instance> list = Arrays.asList(new EmfInstance());
		service.updateDownloadStateForInstances(list);
		verify(linkService, never()).getSimpleLinks(any(InstanceReference.class), anyString());
	}

	// --------------------------------------------------------------------------
	// -------------------- CREATE ARCHIVE --------------------------------------
	// --------------------------------------------------------------------------

	/**
	 * <pre>
	 * Method: createArchive()
	 * Marked documents  : none
	 * Expected result   : null
	 * </pre>
	 */
	public void createArchive_noMarkedDocuments_null() {
		String result = service.createArchive();
		assertNull(result);
	}

	/**
	 * <pre>
	 * Method: createArchive()
	 * Marked documents  : at least one
	 * Expected result   : internal service called at least once
	 * </pre>
	 */
	// find why -> java.lang.ClassCastException: com.sirma.itt.emf.entity.LinkSourceId cannot be cast to
	// com.sirma.itt.emf.domain.Link
	@Test(enabled = false)
	public void createArchive_atLeastOneMarkedDocument_internalServiceCalled() {
		Collection<InstanceReference> collection = Arrays.asList(prepareInstance().toReference());
		when(service.getAll()).thenReturn(collection);
		when(downloadsAdapterService.createArchive(anyCollection())).thenReturn("");
		service.createArchive();
		verify(downloadsAdapterService, atLeastOnce()).createArchive(anyCollection());
	}

	/**
	 * <pre>
	 * Method: createArchive()
	 *
	 * Marked documents          : at least one
	 * Internal service response : DMSClientException
	 * Expected result           : DMSException
	 * </pre>
	 */
	// find why -> java.lang.ClassCastException: com.sirma.itt.emf.entity.LinkSourceId cannot be cast to
	// com.sirma.itt.emf.domain.Link
	@Test(expectedExceptions = RuntimeException.class, enabled = false)
	public void createArchive_atLeastOneMarkedDocument_internalServiceException() {
		Collection<InstanceReference> collection = Arrays.asList(prepareInstance().toReference());
		when(service.getAll()).thenReturn(collection);
		when(downloadsAdapterService.createArchive(anyCollection())).thenThrow(RuntimeException.class);
		service.createArchive();
		verify(downloadsAdapterService, atLeastOnce()).createArchive(anyCollection());
	}

	// --------------------------------------------------------------------------
	// ------------------------ GET ARCHIVE STATUS ------------------------------
	// --------------------------------------------------------------------------

	/**
	 * <pre>
	 * Method: getArchiveStatus(String)
	 *
	 * Parameter       : empty
	 * Expected result : null
	 * </pre>
	 */
	public void getArchiveStatus_emptyParam_null() {
		String result = service.getArchiveStatus("");
		assertNull(result);
	}

	/**
	 * <pre>
	 * Method: getArchiveStatus(String)
	 *
	 * Parameter         : null
	 * Expected result   : null
	 * </pre>
	 */
	public void getArchiveStatus_nullParam_null() {
		String result = service.getArchiveStatus(null);
		assertNull(result);
	}

	/**
	 * <pre>
	 * Method: getArchiveStatus(String)
	 *
	 * Parameter       : not null
	 * Expected result : internal service called at least once
	 * </pre>
	 */
	public void getArchiveStatus_notNullParam_internalServiceCalled() {
		when(downloadsAdapterService.getArchiveStatus(anyString())).thenReturn("");
		service.getArchiveStatus(DMS_ID);
		verify(downloadsAdapterService, atLeastOnce()).getArchiveStatus(anyString());
	}

	/**
	 * <pre>
	 * Method: getArchiveStatus(String)
	 *
	 * Parameter         : not null
	 * Internal response : DMSClientException
	 * Expected result   : DMSException
	 * </pre>
	 */
	@Test(expectedExceptions = RuntimeException.class)
	public void getArchiveStatus_atLeastOneMarkedDocument_internalServiceException() {
		when(downloadsAdapterService.getArchiveStatus(anyString())).thenThrow(RuntimeException.class);
		service.getArchiveStatus(DMS_ID);
		verify(downloadsAdapterService, atLeastOnce()).getArchiveStatus(anyString());
	}

	// --------------------------------------------------------------------------
	// ------------------------ REMOVE ARCHIVE ----------------------------------
	// --------------------------------------------------------------------------

	/**
	 * <pre>
	 * Method: removeArchive(String)
	 *
	 * Parameter       : empty
	 * Expected result : null
	 * </pre>
	 */
	public void removeArchive_emptyParam_null() {
		String result = service.removeArchive("");
		assertNull(result);
	}

	/**
	 * <pre>
	 * Method: removeArchive(String)
	 *
	 * Parameter       : null
	 * Expected result : null
	 * </pre>
	 */
	public void removeArchive_nullParam_null() {
		String result = service.removeArchive(null);
		assertNull(result);
	}

	/**
	 * <pre>
	 * Method: removeArchive(String)
	 *
	 * Parameter       : not null
	 * Expected result : internal service called at least once
	 * </pre>
	 */
	public void removeArchive_notNullParam_internalServiceCalled() {
		when(downloadsAdapterService.removeArchive(anyString())).thenReturn("");
		service.removeArchive(DMS_ID);
		verify(downloadsAdapterService, atLeastOnce()).removeArchive(anyString());
	}

	/**
	 * <pre>
	 * Method: removeArchive(String)
	 *
	 * Parameter         : not null
	 * Internal response : DMSClientException
	 * Expected result   : DMSException
	 * </pre>
	 */
	@Test(expectedExceptions = RuntimeException.class)
	public void removeArchive_atLeastOneMarkedDocument_internalServiceException() {
		when(downloadsAdapterService.removeArchive(anyString())).thenThrow(RuntimeException.class);
		service.removeArchive(DMS_ID);
		verify(downloadsAdapterService, atLeastOnce()).removeArchive(anyString());
	}

	// --------------------------------------------------------------------------
	// ------------------------ GET ARCHIVE LINK --------------------------------
	// --------------------------------------------------------------------------

	/**
	 * <pre>
	 * Method: getArchiveLink(String)
	 *
	 * Parameter       : empty
	 * Expected result : null
	 * </pre>
	 */
	public void getArchiveLink_emptyParam_null() {
		String result = service.getArchiveLink("");
		assertNull(result);
	}

	/**
	 * <pre>
	 * Method: getArchiveLink(String)
	 *
	 * Parameter       : null
	 * Expected result : null
	 * </pre>
	 */
	public void getArchiveLink_nullParam_null() {
		String result = service.getArchiveLink(null);
		assertNull(result);
	}

	/**
	 * <pre>
	 * Method: getArchiveLink(String)
	 *
	 * Parameter       : not null
	 * Expected result : internal service called at least once
	 * </pre>
	 */
	public void getArchiveLink_notNullParam_internalServiceCalled() {
		when(downloadsAdapterService.getArchiveURL(anyString())).thenReturn("");
		service.getArchiveLink(DMS_ID);
		verify(downloadsAdapterService, atLeastOnce()).getArchiveURL(anyString());
	}

	/**
	 * <pre>
	 * Method: getArchiveLink(String)
	 *
	 * Parameter         : not null
	 * Internal response : DMSClientException
	 * Expected result   : DMSException
	 * </pre>
	 */
	@SuppressWarnings("unchecked")
	@Test(expectedExceptions = RuntimeException.class)
	public void getArchiveLink_atLeastOneMarkedDocument_internalServiceException() {
		when(downloadsAdapterService.getArchiveURL(anyString())).thenThrow(RuntimeException.class);
		service.getArchiveLink(DMS_ID);
		verify(downloadsAdapterService, atLeastOnce()).getArchiveURL(anyString());
	}

	// --------------------------------------------------------------------------
	// -------------------------- REUSABLE METHODS ------------------------------
	// --------------------------------------------------------------------------

	/**
	 * Prepares test instance and mocks some services.
	 *
	 * @return mocked instance
	 */
	private EmfInstance prepareInstance() {
		EmfInstance instance = new EmfInstance();
		instance.setId("123");
		HashMap<String, Serializable> properties = new HashMap<>(1);
		properties.put(DefaultProperties.HEADER_DEFAULT, TEST_DEFAULT_HEADER);
		instance.setProperties(properties);
		setReferenceField(instance);
		LinkReference linkReference = new LinkReference();
		linkReference.setTo(instance.toReference());
		when(linkService.getSimpleLinks(any(InstanceReference.class), anyString()))
				.thenReturn(Arrays.asList(linkReference));
		return instance;
	}
}
