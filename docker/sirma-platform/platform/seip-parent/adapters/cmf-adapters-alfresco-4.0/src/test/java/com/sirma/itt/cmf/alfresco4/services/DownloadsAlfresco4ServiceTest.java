/**
 *
 */
package com.sirma.itt.cmf.alfresco4.services;

import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyBoolean;
import static org.mockito.Matchers.anyList;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.UnsupportedEncodingException;
import java.util.Arrays;
import java.util.Collections;

import org.apache.commons.httpclient.HttpMethod;
import org.apache.commons.httpclient.URIException;
import org.apache.commons.httpclient.methods.DeleteMethod;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.commons.httpclient.methods.PostMethod;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import com.sirma.itt.seip.adapters.remote.DMSClientException;
import com.sirma.itt.seip.adapters.remote.RESTClient;
import com.sirma.itt.seip.domain.instance.InstanceReference;
import com.sirma.itt.seip.exception.RollbackedRuntimeException;
import com.sirma.itt.seip.resources.EmfUser;
import com.sirma.itt.seip.security.context.SecurityContext;
import com.sirma.sep.content.ContentInfo;
import com.sirma.sep.content.InstanceContentService;

/**
 * General test for DownloadsAlfresco4Service.
 *
 * @author A. Kunchev
 */
public class DownloadsAlfresco4ServiceTest {

	private static final String DMS_ID = "3304abf1-08c2-47b5-b285-6899379fee74";

	@Mock
	private RESTClient restClient;

	@Mock
	private InstanceContentService instanceContentService;

	@Mock
	private SecurityContext securityContext;

	@InjectMocks
	private DownloadsAlfresco4Service service = new DownloadsAlfresco4Service();

	@Before
	public void setup() {
		MockitoAnnotations.initMocks(this);
	}

	// --------------------------------------------------------------------------
	// -------------------- CREATE ARCHIVE --------------------------------------
	// --------------------------------------------------------------------------

	/**
	 * <pre>
	 * Method: createArchive()
	 *
	 * Parameter       : empty
	 * Expected result : DMSClientException
	 * </pre>
	 */
	@Test(expected = IllegalArgumentException.class)
	public void createArchive_emptyCollection_DMSException() throws DMSClientException {
		service.createArchive(Collections.emptyList());
	}

	/**
	 * <pre>
	 * Method: createArchive()
	 *
	 * Parameter       : null
	 * Expected result : DMSClientException
	 * </pre>
	 */
	@Test(expected = IllegalArgumentException.class)
	public void createArchive_nullCollection_DMSException() throws DMSClientException {
		service.createArchive(null);
	}

	/**
	 * <pre>
	 * Method: createArchive()
	 *
	 * Parameter 	   : not null
	 * JSON            : empty
	 * Expected result : DMSClientException
	 * </pre>
	 */
	@Test(expected = IllegalArgumentException.class)
	public void createArchive_emptyJSON_DMSException() throws DMSClientException {
		when(instanceContentService.getContent(anyList(), anyString())).thenReturn(Collections.emptyList());
		service.createArchive(Arrays.asList("ID"));
	}

	/**
	 * <pre>
	 * Method: createArchive()
	 *
	 * Parameter 	   : not null
	 * Http method     : failed
	 * Expected result : DMSClientException
	 * </pre>
	 */
	@Test(expected = RollbackedRuntimeException.class)
	public void createArchive_failToCreateHttpMethod_DMSException()
			throws DMSClientException, UnsupportedEncodingException {
		ContentInfo contentInfo = mock(ContentInfo.class);
		when(contentInfo.exists()).thenReturn(true);
		when(contentInfo.getRemoteSourceName()).thenReturn("alfresco");
		when(contentInfo.getRemoteId()).thenReturn("xxxx");
		when(instanceContentService.getContent(anyList(), anyString())).thenReturn(Arrays.asList(contentInfo));
		when(restClient.createMethod(any(HttpMethod.class), anyString(), anyBoolean()))
				.thenThrow(new UnsupportedEncodingException());
		service.createArchive(Arrays.asList("XXXX"));
	}

	/**
	 * <pre>
	 * Method: createArchive()
	 *
	 * Parameter 	   : not null
	 * Request         : OK
	 * Expected result : OK + JSON
	 * </pre>
	 */
	@SuppressWarnings("unchecked")
	@Test
	public void createArchive_okRequest_JSONObject() throws DMSClientException, UnsupportedEncodingException {
		prepareUser(securityContext);
		ContentInfo contentInfo = mock(ContentInfo.class);
		when(contentInfo.exists()).thenReturn(true);
		when(contentInfo.getRemoteSourceName()).thenReturn("alfresco");
		when(contentInfo.getRemoteId()).thenReturn("xxxx");
		when(instanceContentService.getContent(anyList(), anyString())).thenReturn(Arrays.asList(contentInfo));
		doReturn(new PostMethod()).when(restClient).createMethod(any(HttpMethod.class), anyString(), anyBoolean());
		when(restClient.request(anyString(), any(HttpMethod.class))).thenReturn("{\"status\":\"success\"}");
		service.createArchive(Arrays.asList("XXXX"));
		verify(restClient, atLeastOnce()).request(anyString(), any(HttpMethod.class));
	}

	// --------------------------------------------------------------------------
	// -------------------- GET ARCHIVE STATUS ----------------------------------
	// --------------------------------------------------------------------------

	/**
	 * <pre>
	 * Method: getArchiveStatus()
	 *
	 * Parameter       : empty
	 * Expected result : DMSClientException
	 * </pre>
	 */
	@Test(expected = IllegalArgumentException.class)
	public void getArchiveStatus_emptyParam_DMSException() throws DMSClientException {
		service.getArchiveStatus("");
	}

	/**
	 * <pre>
	 * Method: getArchiveStatus()
	 *
	 * Parameter       : null
	 * Expected result : DMSClientException
	 * </pre>
	 */
	@Test(expected = IllegalArgumentException.class)
	public void getArchiveStatus_nullParam_DMSException() throws DMSClientException {
		service.getArchiveStatus(null);
	}

	/**
	 * <pre>
	 * Method: getArchiveStatus()
	 *
	 * Parameter 	   : not null
	 * Http method     : failed
	 * Expected result : DMSClientException
	 * </pre>
	 */
	@Test(expected = RollbackedRuntimeException.class)
	public void getArchiveStatus_failToCreateHttpMethod_DMSException()
			throws DMSClientException, UnsupportedEncodingException {
		doThrow(new UnsupportedEncodingException()).when(restClient).createMethod(any(HttpMethod.class), anyString(),
				anyBoolean());
		service.getArchiveStatus(DMS_ID);
	}

	/**
	 * <pre>
	 * Method: getArchiveStatus()
	 *
	 * Parameter 	   : not null
	 * Request         : OK
	 * Expected result : OK + JSON
	 * </pre>
	 */
	@Test
	public void getArchiveStatus_okRequest_JSONObject() throws DMSClientException, UnsupportedEncodingException {
		prepareUser(securityContext);
		doReturn(new GetMethod()).when(restClient).createMethod(any(HttpMethod.class), anyString(), anyBoolean());
		when(restClient.request(anyString(), any(HttpMethod.class))).thenReturn("{\"status\":\"DONE\"}");
		service.getArchiveStatus(DMS_ID);
		verify(restClient, atLeastOnce()).request(anyString(), any(HttpMethod.class));
	}

	// --------------------------------------------------------------------------
	// -------------------- REMOVE ARCHIVE --------------------------------------
	// --------------------------------------------------------------------------

	/**
	 * <pre>
	 * Method: removeArchive()
	 *
	 * Parameter       : empty
	 * Expected result : DMSClientException
	 * </pre>
	 */
	@Test(expected = IllegalArgumentException.class)
	public void removeArchive_emptyParam_DMSException() throws DMSClientException {
		service.removeArchive("");
	}

	/**
	 * <pre>
	 * Method: removeArchive()
	 *
	 * Parameter       : null
	 * Expected result : DMSClientException
	 * </pre>
	 */
	@Test(expected = IllegalArgumentException.class)
	public void removeArchive_nullParam_DMSException() throws DMSClientException {
		service.removeArchive(null);
	}

	/**
	 * <pre>
	 * Method: removeArchive()
	 *
	 * Parameter 	   : not null
	 * Http method     : failed
	 * Expected result : DMSClientException
	 * </pre>
	 */
	@Test(expected = RollbackedRuntimeException.class)
	public void removeArchive_failToCreateHttpMethod_DMSException()
			throws DMSClientException, UnsupportedEncodingException {
		doThrow(new UnsupportedEncodingException()).when(restClient).createMethod(any(HttpMethod.class), anyString(),
				anyBoolean());
		service.removeArchive(DMS_ID);
	}

	/**
	 * <pre>
	 * Method: removeArchive()
	 *
	 * Parameter 	   : not null
	 * Request         : OK
	 * Expected result : OK + JSON
	 * </pre>
	 */
	@Test
	public void removeArchive_okRequest_JSONObject() throws DMSClientException, UnsupportedEncodingException {
		prepareUser(securityContext);
		doReturn(new DeleteMethod()).when(restClient).createMethod(any(HttpMethod.class), anyString(), anyBoolean());
		when(restClient.request(anyString(), any(HttpMethod.class))).thenReturn("{\"status\":\"CANCELLED\"}");
		service.removeArchive(DMS_ID);
		verify(restClient, atLeastOnce()).request(anyString(), any(HttpMethod.class));
	}

	// --------------------------------------------------------------------------
	// -------------------- GET ARCHIVR URL -------------------------------------
	// --------------------------------------------------------------------------

	/**
	 * <pre>
	 * Method: getArchiveURL()
	 *
	 * Parameter       : empty
	 * Expected result : DMSClientException
	 * </pre>
	 */
	@Test(expected = IllegalArgumentException.class)
	public void getArchiveURL_emptyParam_DMSException() throws DMSClientException {
		service.getArchiveURL("");
	}

	/**
	 * <pre>
	 * Method: getArchiveURL()
	 *
	 * Parameter       : null
	 * Expected result : DMSClientException
	 * </pre>
	 */
	@Test(expected = IllegalArgumentException.class)
	public void getArchiveURL_nullParam_DMSException() throws DMSClientException {
		service.getArchiveURL(null);
	}

	/**
	 * <pre>
	 * Method: getArchiveURL()
	 *
	 * Parameter 	   : not null
	 * Request         : OK
	 * Expected result : OK + JSON
	 * </pre>
	 *
	 * @throws URIException
	 */
	@Test
	public void getArchiveURL_okRequest_JSONObject() throws DMSClientException, URIException {
		prepareUser(securityContext);
		String archiveURL = service.getArchiveURL(DMS_ID);
		assertTrue(archiveURL.contains(DMS_ID));
	}

	/**
	 * Prepares mock for the user.
	 *
	 * @param securityContext
	 *            the security context
	 * @param authenticationService
	 *            the AuthenticationService, which methods will be mocked
	 */
	private static void prepareUser(SecurityContext securityContext) {
		InstanceReference userRef = mock(InstanceReference.class);
		EmfUser user = mock(EmfUser.class);
		when(securityContext.getAuthenticated()).thenReturn(user);
		when(user.toReference()).thenReturn(userRef);
		when(user.getTicket()).thenReturn("ticket");
		when(userRef.getId()).thenReturn("emf:user");
		when(user.getSystemId()).thenReturn("emf:user");
	}
}
