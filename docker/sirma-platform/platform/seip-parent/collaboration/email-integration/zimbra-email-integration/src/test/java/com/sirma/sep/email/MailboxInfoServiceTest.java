package com.sirma.sep.email;

import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.anyObject;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.when;

import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import com.sirma.sep.account.administration.AccountAuthenticationService;
import com.sirma.sep.email.exception.EmailIntegrationException;
import com.sirma.sep.email.service.MailboxInfoService;
import com.zimbra.wsdl.zimbraservice.ZcsPortType;

import zimbramail.Folder;
import zimbramail.GetFolderResponse;

/**
 * Test for the {@link MailboxInfoService}
 * 
 * @author S.Djulgerova
 */
public class MailboxInfoServiceTest {

	@InjectMocks
	private MailboxInfoServiceImpl mailboxInfoServiceImpl;

	@Mock
	private AccountAuthenticationService authenticationService;

	@Mock
	ZcsPortType clientPort;

	@Before
	public void setup() throws EmailIntegrationException {
		MockitoAnnotations.initMocks(this);
		when(authenticationService.getClientPort(anyString())).thenReturn(clientPort);
	}

	@Test
	public void getUnreadMessagesCountTest() throws EmailIntegrationException {
		Folder folder = new Folder();

		Folder subFolderOne = new Folder();
		subFolderOne.setName("subFolderOne");
		subFolderOne.setU(2);

		Folder subFolderTwo = new Folder();
		subFolderTwo.setName("subFolderTwo");
		subFolderTwo.setU(null);

		Folder subFolderThree = new Folder();
		subFolderThree.setName("subFolderThree");
		subFolderThree.setU(1);
		subFolderTwo.getFolderOrLinkOrSearch().add(subFolderThree);

		folder.getFolderOrLinkOrSearch().add(subFolderOne);
		folder.getFolderOrLinkOrSearch().add(subFolderTwo);

		GetFolderResponse responseStub = new GetFolderResponse();
		responseStub.setFolder(folder);

		when(clientPort.getFolderRequest(anyObject())).thenReturn(responseStub);
		assertEquals(3, mailboxInfoServiceImpl.getUnreadMessagesCount("user@sirma.bg"));
	}

}
