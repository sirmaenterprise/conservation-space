/**
 *
 */
package com.sirma.itt.emf.web.rest;

import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import org.mockito.InjectMocks;
import org.mockito.Matchers;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import com.sirma.itt.emf.adapter.DMSException;
import com.sirma.itt.emf.rest.EmfRestService;
import com.sirma.itt.emf.web.rest.util.RestServiceTestsUtil;
import com.sirma.itt.seip.convert.TypeConverter;
import com.sirma.itt.seip.domain.definition.label.LabelProvider;
import com.sirma.itt.seip.domain.instance.InstanceReference;
import com.sirma.itt.seip.domain.rest.RestServiceException;
import com.sirma.itt.seip.resources.EmfUser;
import com.sirma.itt.seip.resources.downloads.DownloadsService;
import com.sirma.itt.seip.security.context.SecurityContext;

/**
 * General test for DownloadsRestService.
 *
 * @author A. Kunchev
 */
@Test
public class DownloadsRestServiceTest {

	private static final String DMS_INSTANCE_ID = "3304abf1-08c2-47b5-a923-5893d41c22b8";

	private static final String JSON_DATA_NO_TYPE = "{\"instanceId\":\"123\",\"instanceType\":\"\"}";

	private static final String JSON_DATA = "{\"instanceId\":\"123\",\"instanceType\":\"instanceType\"}";

	private static final String WRONG_JSON_DATA = "{\"instanceId\123\",\"instanceType\":\"instanceType";

	@InjectMocks
	private DownloadsRestService downloadsRestService = new DownloadsRestService();

	@Mock
	private TypeConverter typeConverter;

	@Mock
	private DownloadsService downloadsService;

	@Mock
	private LabelProvider labelProvider;

	@Mock
	private EmfRestService emfRestService;
	@Mock
	private SecurityContext securityContext;

	/**
	 * Before method.
	 */
	@BeforeMethod
	public void beforeMethod() {
		MockitoAnnotations.initMocks(this);
		when(securityContext.getAuthenticated()).thenReturn(new EmfUser("admin"));
	}

	// --------------------------------------------------------------------------
	// -------------------------- ADD DOWNLOADS --------------------------------
	// --------------------------------------------------------------------------

	/**
	 * <pre>
	 * Method: addToDownloads
	 *
	 * Request data: empty
	 *
	 * Expected result: RestServiceException
	 * </pre>
	 */
	@Test(expectedExceptions = RestServiceException.class)
	public void addToDownloads_empty_data_badResponse() {
		downloadsRestService.addToDownloads("");
	}

	/**
	 * <pre>
	 * Method: addToDownloads
	 *
	 * Request data: null
	 *
	 * Expected result: RestServiceException
	 * </pre>
	 */
	@Test(expectedExceptions = RestServiceException.class)
	public void addToDownloads_null_data_badResponse() {
		downloadsRestService.addToDownloads(null);
	}

	/**
	 * <pre>
	 * Method: addToDownloads
	 *
	 * Request data: wrong JSON
	 *
	 * Expected result: RestServiceException
	 * </pre>
	 */
	@Test(expectedExceptions = RestServiceException.class)
	public void addToDownloads_nonexistent_instance_badResponse() {
		when(typeConverter.convert(Matchers.eq(InstanceReference.class), anyString())).thenReturn(null);
		downloadsRestService.addToDownloads(WRONG_JSON_DATA);
	}

	/**
	 * <pre>
	 * Method: addToDownloads
	 *
	 * Request data : correct
	 * Instance     : exists
	 * Link         : created
	 *
	 * Expected result: OK response
	 * </pre>
	 */
	public void addToDownloads_existent_instance_OKResponse_linkCreated() {
		InstanceReference instanceReference = RestServiceTestsUtil.prepareLinkInstance(typeConverter);
		when(downloadsService.add(instanceReference)).thenReturn(true);
		Response response = downloadsRestService.addToDownloads(JSON_DATA);
		assertEquals(Status.OK.getStatusCode(), response.getStatus());
	}

	/**
	 * <pre>
	 * Method: addToDownloads
	 *
	 * Request data : correct
	 * Instance     : exists
	 * Link         : not created
	 *
	 * Expected result: OK response
	 * </pre>
	 */
	public void addToDownloads_existent_instance_OKResponse_linkNotCreated() {
		InstanceReference instanceReference = RestServiceTestsUtil.prepareLinkInstance(typeConverter);
		when(downloadsService.add(instanceReference)).thenReturn(false);
		when(labelProvider.getValue(anyString())).thenReturn("");
		Response response = downloadsRestService.addToDownloads(JSON_DATA);
		assertEquals(Status.OK.getStatusCode(), response.getStatus());
	}

	// --------------------------------------------------------------------------
	// -------------------------- REMOVE DOWNLOADS -----------------------------
	// --------------------------------------------------------------------------

	/**
	 * <pre>
	 * Method: removeFromDownloads
	 *
	 * Request data: empty
	 *
	 * Expected result: RestServiceException
	 * </pre>
	 */
	@Test(expectedExceptions = RestServiceException.class)
	public void removeFromDownloads_empty_data_badResponse() {
		downloadsRestService.removeFromDownloads("");
	}

	/**
	 * <pre>
	 * Method: removeFromDownloads
	 *
	 * Request data: null
	 *
	 * Expected result: RestServiceException
	 * </pre>
	 */
	@Test(expectedExceptions = RestServiceException.class)
	public void removeFromDownloads_null_data_badResponse() {
		downloadsRestService.removeFromDownloads(null);
	}

	/**
	 * <pre>
	 * Method: removeFromDownloads
	 *
	 * Request data: missing type in JSON
	 *
	 * Expected result: RestServiceException
	 * </pre>
	 */
	@Test(expectedExceptions = RestServiceException.class)
	public void removeFromDownloads_nonexistent_instance_badResponse() {
		when(typeConverter.convert(Matchers.eq(InstanceReference.class), anyString())).thenReturn(null);
		downloadsRestService.removeFromDownloads(JSON_DATA_NO_TYPE);
	}

	/**
	 * <pre>
	 * Method: removeFromDownloads
	 *
	 * Request data: correct
	 *
	 * Expected result: OK response
	 * </pre>
	 */
	public void removeFromDownloads_existent_instance_OKResponse() {
		RestServiceTestsUtil.prepareLinkInstance(typeConverter);
		Response response = downloadsRestService.removeFromDownloads(JSON_DATA);
		assertEquals(Status.OK.getStatusCode(), response.getStatus());
	}

	/**
	 * <pre>
	 * Method: removeAll
	 *
	 * Request data: correct
	 * Instances removed: false
	 *
	 * Expected result: OK response
	 * </pre>
	 */
	public void removeAll_noInstanceRemoved_OKResponse() {
		Response response = downloadsRestService.removeAll();
		assertEquals(Status.OK.getStatusCode(), response.getStatus());
	}

	/**
	 * <pre>
	 * Method: removeAll
	 *
	 * Request data: correct
	 * Instances removed: true
	 *
	 * Expected result: OK response
	 * </pre>
	 */
	public void removeAll_instanceRemoved_OKResponse() {
		Response response = downloadsRestService.removeAll();
		when(downloadsService.removeAll()).thenReturn(true);
		assertEquals(Status.OK.getStatusCode(), response.getStatus());
	}

	// --------------------------------------------------------------------------
	// -------------------------- GET DOWNLOADS ---------------------------------
	// --------------------------------------------------------------------------

	/**
	 * <pre>
	 * Method: getDownlaodsList
	 *
	 * Favourite list: null
	 *
	 * Expected result: OK response
	 * </pre>
	 */
	public void getDownlaodsList_nullDownloadsList_OKResponse() {
		RestServiceTestsUtil.prepareUser(downloadsRestService, labelProvider);
		when(downloadsService.getAll()).thenReturn(null);
		Response response = downloadsRestService.getDownlaodsList();
		assertEquals(Status.OK.getStatusCode(), response.getStatus());
	}

	/**
	 * <pre>
	 * Method: getDownlaodsList
	 *
	 * Favourite list: empty
	 *
	 * Expected result: OK response
	 * </pre>
	 */
	public void getDownlaodsList_emptyDownloadsList_OKResponse() {
		RestServiceTestsUtil.prepareUser(downloadsRestService, labelProvider);
		when(downloadsService.getAll()).thenReturn(Collections.emptyList());
		Response response = downloadsRestService.getDownlaodsList();
		assertEquals(Status.OK.getStatusCode(), response.getStatus());
	}

	/**
	 * <pre>
	 * Method: getDownlaodsList
	 *
	 * Favourite list: at least one favourite instance
	 *
	 * Expected result: OK response
	 * </pre>
	 */
	public void getDownlaodsList_notEmptyDownloadsList_OKResponse() {
		RestServiceTestsUtil.prepareUser(downloadsRestService, labelProvider);
		InstanceReference instanceReference = RestServiceTestsUtil.prepareLinkInstance(typeConverter);
		List<InstanceReference> instances = new ArrayList<>();
		instances.add(instanceReference);
		when(downloadsService.getAll()).thenReturn(instances);
		Response response = downloadsRestService.getDownlaodsList();
		assertEquals(Status.OK.getStatusCode(), response.getStatus());
	}

	// --------------------------------------------------------------------------
	// -------------------------- CREATE ARCHIVE --------------------------------
	// --------------------------------------------------------------------------

	/**
	 * <pre>
	 * Method            : createArchive
	 * Internal response : OK
	 * Expected result   : OK + JSONObject
	 * </pre>
	 */
	public void createArchive_okDMSRequest_OKResponse() throws DMSException {
		doReturn("{\"status\":\"success\"}").when(downloadsService).createArchive();
		Response response = downloadsRestService.createArchive();
		assertEquals(Status.OK.getStatusCode(), response.getStatus());
	}

	// --------------------------------------------------------------------------
	// -------------------------- GET ARCHIVE STATUS ----------------------------
	// --------------------------------------------------------------------------

	/**
	 * <pre>
	 * Method           : getArchiveStatus
	 * Request parameter: empty
	 * Expected result  : RestServiceException
	 * </pre>
	 */
	@Test(expectedExceptions = RestServiceException.class)
	public void getArchiveStatus_emptyParam_RestServiceException() {
		downloadsRestService.getArchiveStatus("");
	}

	/**
	 * <pre>
	 * Method           : getArchiveStatus
	 * Request parameter: null
	 * Expected result  : RestServiceException
	 * </pre>
	 */
	@Test(expectedExceptions = RestServiceException.class)
	public void getArchiveStatus_nullParam_RestServiceException() {
		downloadsRestService.getArchiveStatus(null);
	}

	/**
	 * <pre>
	 * Method            : getArchiveStatus
	 * Internal response : OK
	 * Expected result   : OK + JSONObject with archivation status
	 * </pre>
	 */
	public void getArchiveStatus_okResponse_JSONObject() throws DMSException {
		doReturn("{\"status\":\"IN_PROGRESS\"}").when(downloadsService).getArchiveStatus(anyString());
		Response response = downloadsRestService.getArchiveStatus(DMS_INSTANCE_ID);
		assertEquals(Status.OK.getStatusCode(), response.getStatus());
	}

	// --------------------------------------------------------------------------
	// -------------------------- REMOVE ARCHIVE --------------------------------
	// --------------------------------------------------------------------------

	/**
	 * <pre>
	 * Method           : removeArchive
	 * Request parameter: empty
	 * Expected result  : RestServiceException
	 * </pre>
	 */
	@Test(expectedExceptions = RestServiceException.class)
	public void removeArchive_emptyParam_RestServiceException() {
		downloadsRestService.removeArchive("");
	}

	/**
	 * <pre>
	 * Method           : removeArchive
	 * Request parameter: null
	 * Expected result  : RestServiceException
	 * </pre>
	 */
	@Test(expectedExceptions = RestServiceException.class)
	public void removeArchive_nullParam_RestServiceException() {
		downloadsRestService.removeArchive(null);
	}

	/**
	 * <pre>
	 * Method            : removeArchive
	 * Internal response : ОК
	 * Expected result   : ОК + JSONObject
	 * </pre>
	 */
	public void removeArchive_okResponse_JSONObject() throws DMSException {
		doReturn("{\"status\":\"CANCELLED\"}").when(downloadsService).removeArchive(anyString());
		Response response = downloadsRestService.removeArchive(DMS_INSTANCE_ID);
		assertEquals(Status.OK.getStatusCode(), response.getStatus());
	}

	// --------------------------------------------------------------------------
	// -------------------------- GET ARCHIVE -----------------------------------
	// --------------------------------------------------------------------------

	/**
	 * <pre>
	 * Method           : getArchive
	 * Request parameter: empty
	 * Expected result  : RestServiceException
	 * </pre>
	 */
	@Test(expectedExceptions = RestServiceException.class)
	public void getArchive_emptyParam_RestServiceException() {
		downloadsRestService.getArchive("");
	}

	/**
	 * <pre>
	 * Method           : getArchive
	 * Request parameter: null
	 * Expected result  : RestServiceException
	 * </pre>
	 */
	@Test(expectedExceptions = RestServiceException.class)
	public void getArchive_nullParam_RestServiceException() {
		downloadsRestService.getArchive(null);
	}

	/**
	 * <pre>
	 * Method            : removeArchive
	 * Internal response : ОК
	 * Expected result   : ОК + JSONObject with the download link for the archive
	 * </pre>
	 */
	public void getArchive_okResponse_JSONObject() throws DMSException {
		doReturn("{\"link\":\"XXXXXXXXXXX\"}").when(downloadsService).getArchiveLink(anyString());
		Response response = downloadsRestService.getArchive(DMS_INSTANCE_ID);
		assertEquals(Status.OK.getStatusCode(), response.getStatus());
	}

}
