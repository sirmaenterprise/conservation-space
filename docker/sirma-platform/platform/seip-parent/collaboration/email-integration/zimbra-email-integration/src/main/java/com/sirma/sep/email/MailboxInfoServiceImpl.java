package com.sirma.sep.email;

import java.util.List;

import javax.inject.Inject;

import com.sirma.sep.account.administration.AccountAuthenticationService;
import com.sirma.sep.email.exception.EmailIntegrationException;
import com.sirma.sep.email.service.MailboxInfoService;

import zimbramail.Folder;
import zimbramail.GetFolderRequest;
import zimbramail.GetFolderResponse;

/**
 * {@link MailboxInfoService} implementation used to retrieve specific info for given mailbox
 * 
 * @author S.Djulgerova
 */
public class MailboxInfoServiceImpl implements MailboxInfoService {

	@Inject
	private AccountAuthenticationService authenticationService;

	@Override
	public int getUnreadMessagesCount(String accountName) throws EmailIntegrationException {
		GetFolderResponse folderResponse = authenticationService.getClientPort(accountName)
				.getFolderRequest(new GetFolderRequest());
		return getUnread(folderResponse.getFolder().getFolderOrLinkOrSearch(), 0);
	}

	private int getUnread(List<Folder> folders, int totalCount) {
		for (Folder folder : folders) {
			if (!folder.getFolderOrLinkOrSearch().isEmpty()) {
				totalCount = getUnread(folder.getFolderOrLinkOrSearch(), totalCount);
			}
			if (folder.getU() != null) {
				totalCount += folder.getU();
			}
		}
		return totalCount;
	}

}
