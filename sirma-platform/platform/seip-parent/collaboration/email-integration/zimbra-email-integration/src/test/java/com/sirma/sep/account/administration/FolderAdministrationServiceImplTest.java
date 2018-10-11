package com.sirma.sep.account.administration;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.fail;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.doCallRealMethod;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

import com.sirma.sep.email.exception.EmailIntegrationException;
import com.sirma.sep.email.model.account.FolderInformation;
import com.sirma.sep.email.service.FolderAdministrationService;
import com.zimbra.wsdl.zimbraservice.ZcsPortType;

import zimbramail.CreateFolderRequest;
import zimbramail.CreateMountpointRequest;
import zimbramail.Folder;
import zimbramail.FolderActionRequest;
import zimbramail.FolderActionSelector;
import zimbramail.GetFolderRequest;
import zimbramail.GetFolderResponse;
import zimbramail.NewMountpointSpec;

public class FolderAdministrationServiceImplTest {

	private static final String TEST_TARGET_EMAIL = "test-target-mail@mail.com";
	private static final String TEST_FOLDER_NAME = "test folder";
	private static final String TEST_FOLDER_ID = "test-folder-id";
	private static final String TEST_GRANTEE_ID = "test-grantee-id";

	@InjectMocks
	private FolderAdministrationService service;

	@Mock
	private AccountAuthenticationService authenticationService;

	private ZcsPortType clientPortMock;

	@Before
	public void before() throws EmailIntegrationException {
		clientPortMock = Mockito.mock(ZcsPortType.class);

		service = mock(FolderAdministrationServiceImpl.class);
		MockitoAnnotations.initMocks(this);
		when(authenticationService.getClientPort(TEST_TARGET_EMAIL)).thenReturn(clientPortMock);
	}

	@Test
	public void createFolderTest() throws EmailIntegrationException {
		String view = "test view";
		doCallRealMethod().when(service).createFolder(TEST_TARGET_EMAIL, TEST_FOLDER_NAME, view);
		ArgumentCaptor<CreateFolderRequest> requestCaptor = ArgumentCaptor.forClass(CreateFolderRequest.class);
		service.createFolder(TEST_TARGET_EMAIL, TEST_FOLDER_NAME, "test view");
		// verify the target is called to create the specified folder in the specified views
		verify(authenticationService, times(1)).getClientPort(TEST_TARGET_EMAIL);
		verify(clientPortMock, times(1)).createFolderRequest(requestCaptor.capture());
		CreateFolderRequest req = requestCaptor.getValue();
		assertEquals(view, req.getFolder().getView());
		assertEquals(TEST_FOLDER_NAME, req.getFolder().getName());
	}

	@Test(expected = EmailIntegrationException.class)
	public void createFolderFailTest() throws EmailIntegrationException {
		doCallRealMethod().when(service).createFolder(anyString(), anyString(), anyString());
		when(clientPortMock.createFolderRequest(any(CreateFolderRequest.class)))
				.thenThrow(EmailIntegrationException.class);
		service.createFolder(TEST_TARGET_EMAIL, TEST_FOLDER_NAME, "test view");
		fail("Shouldn't be reached");
	}

	@Test
	public void getFolderTest() throws EmailIntegrationException {
		GetFolderResponse resp = new GetFolderResponse();
		Folder outerFolderResult = new Folder();
		Folder innerFolder = new Folder();
		innerFolder.setName("wrong name");
		Folder actualInnerFolder = new Folder();
		actualInnerFolder.setName(TEST_FOLDER_NAME);
		actualInnerFolder.getFolderOrLinkOrSearch().add(innerFolder);
		outerFolderResult.getFolderOrLinkOrSearch().add(actualInnerFolder);
		resp.setFolder(outerFolderResult);

		when(service.getFolder(TEST_TARGET_EMAIL, TEST_FOLDER_NAME)).thenCallRealMethod();
		when(clientPortMock.getFolderRequest(any(GetFolderRequest.class))).thenReturn(resp);

		FolderInformation recieved = service.getFolder(TEST_TARGET_EMAIL, TEST_FOLDER_NAME);
		verify(clientPortMock, times(1)).getFolderRequest(any(GetFolderRequest.class));
		assertEquals(recieved.getName(), actualInnerFolder.getName());
	}

	@Test
	public void getEmptyFolderTest() throws EmailIntegrationException {
		GetFolderResponse resp = new GetFolderResponse();
		Folder outerFolderResult = new Folder();
		Folder innerFolder = new Folder();
		innerFolder.setName("wrong name");

		resp.setFolder(outerFolderResult);

		when(service.getFolder(TEST_TARGET_EMAIL, TEST_FOLDER_NAME)).thenCallRealMethod();
		when(clientPortMock.getFolderRequest(any(GetFolderRequest.class))).thenReturn(resp);
		FolderInformation recieved = service.getFolder(TEST_TARGET_EMAIL, TEST_FOLDER_NAME);
		verify(clientPortMock, times(1)).getFolderRequest(any(GetFolderRequest.class));
		assertNull(recieved.getName());
	}

	@Test(expected = EmailIntegrationException.class)
	public void getFolderFailTest() throws EmailIntegrationException {
		when(service.getFolder(TEST_TARGET_EMAIL, TEST_FOLDER_NAME)).thenCallRealMethod();
		when(clientPortMock.getFolderRequest(any(GetFolderRequest.class))).thenThrow(EmailIntegrationException.class);
		service.getFolder(TEST_TARGET_EMAIL, TEST_FOLDER_NAME);
	}

	@Test
	public void deleteFolderTest() throws EmailIntegrationException {
		doCallRealMethod().when(service).deleteFolder(TEST_TARGET_EMAIL, TEST_FOLDER_ID);
		ArgumentCaptor<FolderActionRequest> captor = ArgumentCaptor.forClass(FolderActionRequest.class);
		service.deleteFolder(TEST_TARGET_EMAIL, TEST_FOLDER_ID);
		verify(clientPortMock).folderActionRequest(captor.capture());
		FolderActionRequest req = captor.getValue();
		assertEquals("delete", req.getAction().getOp());
		assertEquals(TEST_FOLDER_ID, req.getAction().getId());
	}

	@Test(expected = EmailIntegrationException.class)
	public void deleteFolderFailTest() throws EmailIntegrationException {
		doCallRealMethod().when(service).deleteFolder(TEST_TARGET_EMAIL, TEST_FOLDER_ID);
		when(clientPortMock.folderActionRequest(any(FolderActionRequest.class)))
				.thenThrow(EmailIntegrationException.class);
		service.deleteFolder(TEST_TARGET_EMAIL, TEST_FOLDER_ID);
		fail("shouldn't be reached");
	}

	@Test
	public void grantRightTest() throws EmailIntegrationException {
		doCallRealMethod().when(service).giveShareRightsToFolder(TEST_FOLDER_ID, TEST_GRANTEE_ID, TEST_TARGET_EMAIL);
		ArgumentCaptor<FolderActionRequest> captor = ArgumentCaptor.forClass(FolderActionRequest.class);
		service.giveShareRightsToFolder(TEST_FOLDER_ID, TEST_GRANTEE_ID, TEST_TARGET_EMAIL);
		verify(clientPortMock).folderActionRequest(captor.capture());
		FolderActionRequest request = captor.getValue();
		FolderActionSelector action = request.getAction();
		assertEquals("grant", action.getOp());
		assertEquals(TEST_FOLDER_ID, action.getId());
		assertEquals("usr", action.getGrant().getGt());
		assertEquals(TEST_GRANTEE_ID, action.getGrant().getD());
		assertEquals("r", action.getGrant().getPerm());
	}

	@Test
	public void createMountPointTest() throws EmailIntegrationException {
		String testView = "test-view";
		String testTargetId = "test-target-id";
		String rid = "123456";
		doCallRealMethod().when(service).createMountPointForFolder(TEST_TARGET_EMAIL, testTargetId, TEST_FOLDER_NAME,
				testView, rid);
		ArgumentCaptor<CreateMountpointRequest> captor = ArgumentCaptor.forClass(CreateMountpointRequest.class);
		service.createMountPointForFolder(TEST_TARGET_EMAIL, testTargetId, TEST_FOLDER_NAME, testView, rid);
		verify(clientPortMock, times(1)).createMountpointRequest(captor.capture());
		CreateMountpointRequest req = captor.getValue();
		NewMountpointSpec spec = req.getLink();
		assertEquals("1", spec.getL());
		assertEquals(TEST_FOLDER_NAME, spec.getName());
		assertEquals(testView, spec.getView());
		assertEquals(testTargetId, spec.getZid());
		assertEquals(rid, spec.getRid().toString());
	}
}
