package com.sirma.sep.email;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import com.sirma.sep.account.administration.AccountAuthenticationService;
import com.sirma.sep.email.exception.EmailIntegrationException;
import com.zimbra.wsdl.zimbraservice.ZcsPortType;

import zimbramail.MimePartInfo;
import zimbramail.MsgToSend;
import zimbramail.SendMsgRequest;

/**
 * Test suite for the {@link ZimbraMailSenderService}
 * 
 * @author georgi.ts
 */
public class ZimbraMailSenderServiceTest {

	@InjectMocks
	private ZimbraMailSenderService mailSender;

	@Mock
	private AccountAuthenticationService authenticationService;

	@Mock
	private ZcsPortType clientPort;

	private String from = "testFrom@domain.com";
	private String to = "testTo@domain.com";
	private String subject = "Test Subject";
	private String content = "Test Mail Content";
	private String password = "testMailPassword";

	@Before
	public void setup() throws Exception {
		MockitoAnnotations.initMocks(this);
		when(authenticationService.getClientPort(anyString(), anyString())).thenReturn(clientPort);
	}

	@Test
	public void sendMailTest() throws EmailIntegrationException {
		ArgumentCaptor<SendMsgRequest> requestCaptor = ArgumentCaptor.forClass(SendMsgRequest.class);
		mailSender.sendMessage(from, to, subject, content, password);
		verify(clientPort).sendMsgRequest(requestCaptor.capture());
		SendMsgRequest sentRequest = requestCaptor.getValue();
		MsgToSend sentMsg = sentRequest.getM();

		sentMsg.getE().forEach(emailAddrInfo -> {
			if (emailAddrInfo.getT().equals("f")) {
				assertEquals(emailAddrInfo.getA(), from);
			} else if (emailAddrInfo.getT().equals("t")) {
				assertEquals(emailAddrInfo.getA(), to);
			}
		});
		MimePartInfo sentMPInfo = sentMsg.getMp();
		assertEquals(sentMPInfo.getContent(), content);
		assertEquals("text/plain", sentMPInfo.getCt());
		assertEquals(sentMsg.getSu(), subject);
	}

	@Test(expected = EmailIntegrationException.class)
	public void failSendTest() throws EmailIntegrationException {
		mailSender.sendMessage(from, null, subject, content, password);
		fail("Should not be reached");
	}

}
