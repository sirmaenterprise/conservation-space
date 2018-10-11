package com.sirma.sep.account.administration;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import javax.inject.Inject;
import javax.xml.ws.soap.SOAPFaultException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sirma.sep.email.exception.EmailIntegrationException;
import com.sirma.sep.email.model.account.FolderInformation;
import com.sirma.sep.email.service.FolderAdministrationService;

import zimbramail.ActionGrantSelector;
import zimbramail.CreateFolderRequest;
import zimbramail.CreateMountpointRequest;
import zimbramail.Folder;
import zimbramail.FolderActionRequest;
import zimbramail.FolderActionSelector;
import zimbramail.GetFolderRequest;
import zimbramail.GetFolderResponse;
import zimbramail.NewFolderSpec;
import zimbramail.NewMountpointSpec;

/**
 * Zimbra SOAP folder administration service.
 *
 * @author g.tsankov
 */
public class FolderAdministrationServiceImpl implements FolderAdministrationService {

	private static final Logger LOGGER = LoggerFactory.getLogger(FolderAdministrationServiceImpl.class);

	@Inject
	private AccountAuthenticationService authenticationService;

	@Override
	public void createFolder(String ownerEmail, String folderName, String view) throws EmailIntegrationException {
		CreateFolderRequest req = new CreateFolderRequest();
		NewFolderSpec spec = new NewFolderSpec();
		spec.setView(view);
		spec.setName(folderName);
		req.setFolder(spec);

		try {
			authenticationService.getClientPort(ownerEmail).createFolderRequest(req);
		} catch (SOAPFaultException e) {
			throw new EmailIntegrationException("Mailbox Folder creation failed", e);
		}
	}

	@Override
	public FolderInformation getFolder(String folderOwnerEmail, String folderName) throws EmailIntegrationException {
		GetFolderResponse userFolders = null;

		try {
			userFolders = authenticationService.getClientPort(folderOwnerEmail)
					.getFolderRequest(new GetFolderRequest());
		} catch (SOAPFaultException e) {
			throw new EmailIntegrationException("Could not authenticate client:" + folderOwnerEmail, e);
		}
		Optional<Folder> folder = userFolders.getFolder().getFolderOrLinkOrSearch().stream()
				.filter(currentFolder -> folderName.equals(currentFolder.getName())).findFirst();

		if (folder.isPresent()) {
			return wrapFolderToFolderInformation(folder.get());
		}
		LOGGER.warn("Searched for folder {} in {} but found nothing.", folderName, folderOwnerEmail);
		return new FolderInformation();
	}

	@Override
	public void deleteFolder(String folderOwnerId, String folderId) throws EmailIntegrationException {
		FolderActionRequest req = new FolderActionRequest();
		FolderActionSelector sel = new FolderActionSelector();
		sel.setOp("delete");
		sel.setId(folderId);
		req.setAction(sel);

		try {
			authenticationService.getClientPort(folderOwnerId).folderActionRequest(req);
		} catch (SOAPFaultException e) {
			throw new EmailIntegrationException("Mailbox folder deletion failed", e);
		}
	}

	@Override
	public void giveShareRightsToFolder(String folderId, String grantee, String folderOwnerEmail)
			throws EmailIntegrationException {
		FolderActionRequest req = new FolderActionRequest();

		FolderActionSelector action = new FolderActionSelector();
		action.setOp("grant");
		action.setId(folderId);
		ActionGrantSelector grantSel = new ActionGrantSelector();
		grantSel.setGt("usr");
		grantSel.setD(grantee);
		grantSel.setPerm("r");
		action.setGrant(grantSel);
		req.setAction(action);

		try {
			authenticationService.getClientPort(folderOwnerEmail).folderActionRequest(req);
		} catch (SOAPFaultException e) {
			throw new EmailIntegrationException("Mailbox folder rights delegation failed for user: " + grantee, e);
		}
	}

	@Override
	public void createMountPointForFolder(String targetEmail, String folderOwnerId, String folderName, String view,
			String folderId) throws EmailIntegrationException {
		CreateMountpointRequest req = new CreateMountpointRequest();
		NewMountpointSpec spec = new NewMountpointSpec();
		spec.setL("1");
		spec.setName(folderName);
		spec.setView(view);
		spec.setZid(folderOwnerId);
		spec.setRid(Integer.parseInt(folderId));
		req.setLink(spec);

		try {
			authenticationService.getClientPort(targetEmail).createMountpointRequest(req);
		} catch (SOAPFaultException e) {
			throw new EmailIntegrationException("Folder mount point creation for mailbox: " + targetEmail, e);
		}
	}

	/**
	 * Creates a {@link FolderInformation} from zimbra {@link Folder}
	 *
	 * @param folder
	 *            zimbra {@link Folder}
	 * @return wrapped folderInformation
	 */
	private FolderInformation wrapFolderToFolderInformation(Folder folder) {
		FolderInformation folderInformation = new FolderInformation();
		folderInformation.setId(folder.getId());
		folderInformation.setName(folder.getName());
		folderInformation.setItemsCount(folder.getN());
		folderInformation.setInnerFolders(new ArrayList<>(folder.getFolderOrLinkOrSearch().size()));
		List<FolderInformation> innerFoldersArray = new ArrayList<>();

		if (folder.getFolderOrLinkOrSearch().isEmpty()) {
			folderInformation.setInnerFolders(innerFoldersArray);
		} else {
			for (Folder innerFolder : folder.getFolderOrLinkOrSearch()) {
				innerFoldersArray.add(wrapFolderToFolderInformation(innerFolder));
			}

			folderInformation.setInnerFolders(innerFoldersArray);
		}
		return folderInformation;

	}
}
