package com.sirma.sep.email;

import java.util.stream.Stream;

import javax.inject.Inject;
import javax.xml.ws.soap.SOAPFaultException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.apache.commons.lang3.StringUtils;
import com.sirma.sep.account.administration.AccountAuthenticationService;
import com.sirma.sep.email.EmailSenderService;
import com.sirma.sep.email.exception.EmailIntegrationException;

import zimbramail.EmailAddrInfo;
import zimbramail.MimePartInfo;
import zimbramail.MsgToSend;
import zimbramail.SendMsgRequest;

/**
 * {@link EmailSenderService} implementation to send mails to zimbra mail
 * servers.
 * 
 * @author georgi.ts
 *
 */
public class ZimbraMailSenderService implements EmailSenderService {
	private static final Logger LOGGER = LoggerFactory.getLogger(ZimbraMailSenderService.class);

	@Inject
	private AccountAuthenticationService accountAdministrationService;

	@Override
	public void sendMessage(String from, String to, String subject, String content, String senderPassword)
			throws EmailIntegrationException {
		Stream<Boolean> invalidArgFlags = Stream.of(StringUtils.isBlank(from), StringUtils.isBlank(to),
				StringUtils.isBlank(subject), StringUtils.isBlank(content),
				StringUtils.isBlank(senderPassword));
		boolean invalidArg = invalidArgFlags.anyMatch(e -> e);
		if (invalidArg) {
			throw new EmailIntegrationException("Required fields are empty or null. From: " + from + " ,To: " + to
					+ " ,Subject:" + subject + " ,Content:" + content + " or sent password might be invalid");
		}

		SendMsgRequest req = new SendMsgRequest();
		MsgToSend msg = new MsgToSend();
		MimePartInfo mpInfo = new MimePartInfo();
		mpInfo.setCt("text/plain");
		mpInfo.setContent(content);
		msg.setMp(mpInfo);

		EmailAddrInfo fromInfo = new EmailAddrInfo();
		fromInfo.setA(from);
		fromInfo.setT("f");
		fromInfo.setP("Tenant Admin");
		msg.getE().add(fromInfo);

		EmailAddrInfo toInfo = new EmailAddrInfo();
		toInfo.setT("t");
		toInfo.setA(to);
		msg.getE().add(toInfo);

		msg.setSu(subject);
		req.setM(msg);
		try {
			accountAdministrationService.getClientPort(from, senderPassword).sendMsgRequest(req);
		} catch (SOAPFaultException e) {
			LOGGER.error("Could not send mail message.", e);
			throw new EmailIntegrationException("Mail message could not be sent", e);
		}
	}

}
