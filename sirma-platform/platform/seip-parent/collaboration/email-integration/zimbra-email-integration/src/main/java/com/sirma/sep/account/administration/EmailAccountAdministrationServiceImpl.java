package com.sirma.sep.account.administration;

import static com.sirma.email.ZimbraEmailIntegrationConstants.ADMIN_UI_COMPONENTS;
import static com.sirma.email.ZimbraEmailIntegrationConstants.DELEGATED_ADMIN_ACCOUNT;
import static com.sirma.email.ZimbraEmailIntegrationConstants.IS_ADMIN_ACCOUNT;
import static com.sirma.email.ZimbraEmailIntegrationConstants.MAX_MAIL_QUOTA;
import static com.sirma.sep.email.EmailIntegrationConstants.DISPLAY_NAME;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.xml.ws.soap.SOAPFaultException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sirma.email.ZimbraEmailIntegrationHelper;
import com.sirma.sep.email.configuration.EmailIntegrationConfiguration;
import com.sirma.sep.email.exception.EmailAccountCreationException;
import com.sirma.sep.email.exception.EmailAccountDeleteException;
import com.sirma.sep.email.exception.EmailIntegrationException;
import com.sirma.sep.email.model.account.EmailAccountInformation;
import com.sirma.sep.email.model.account.GenericAttribute;
import com.sirma.sep.email.service.EmailAccountAdministrationService;

import zimbra.AccountBy;
import zimbra.AccountSelector;
import zimbra.GranteeType;
import zimbra.OpValue;
import zimbra.TargetBy;
import zimbra.TargetType;
import zimbraaccount.GetWhiteBlackListRequest;
import zimbraaccount.GetWhiteBlackListResponse;
import zimbraaccount.ModifyWhiteBlackListRequest;
import zimbraadmin.AccountInfo;
import zimbraadmin.Attr;
import zimbraadmin.CreateAccountRequest;
import zimbraadmin.CreateAccountResponse;
import zimbraadmin.DeleteAccountRequest;
import zimbraadmin.EffectiveRightsTargetSelector;
import zimbraadmin.GetAccountRequest;
import zimbraadmin.GetAccountResponse;
import zimbraadmin.GetAllAccountsRequest;
import zimbraadmin.GetAllAccountsResponse;
import zimbraadmin.GrantRightRequest;
import zimbraadmin.GranteeBy;
import zimbraadmin.GranteeSelector;
import zimbraadmin.ModifyAccountRequest;
import zimbraadmin.ModifyAccountResponse;
import zimbraadmin.RenameAccountRequest;
import zimbraadmin.RenameAccountResponse;
import zimbraadmin.RevokeRightRequest;
import zimbraadmin.RightModifierInfo;

/**
 * Zimbra SOAP API administration service.
 *
 * @author g.tsankov
 */
@ApplicationScoped
public class EmailAccountAdministrationServiceImpl implements EmailAccountAdministrationService {

	private static final long serialVersionUID = -6083103213283341901L;
	private static final Logger LOGGER = LoggerFactory.getLogger(EmailAccountAdministrationServiceImpl.class);

	@Inject
	private AccountAuthenticationService authenticationService;

	@Inject
	private EmailIntegrationConfiguration emailIntegrationConfiguration;

	@Override
	public EmailAccountInformation createAccount(String accountName, String password, Map<String, String> attributes)
			throws EmailAccountCreationException {
		CreateAccountRequest createRequest = new CreateAccountRequest();
		createRequest.setName(accountName);
		createRequest.setPassword(password);

		CreateAccountResponse createResp;
		try {
			initializeAccountAttributes(createRequest, attributes);
			createResp = authenticationService.getTenantAdminPort().createAccountRequest(createRequest);
		} catch (SOAPFaultException | EmailIntegrationException e) {
			LOGGER.error("Account creation failed for account name:" + accountName, e);
			throw new EmailAccountCreationException("Account creation failed for account name:" + accountName, e);
		}
		AccountInfo accInfo = createResp.getAccount();
		LOGGER.info("Created account id is: {}", accInfo.getId());

		return createEmailAccountInformation(accInfo);
	}

	@Override
	public EmailAccountInformation createTenantAdminAccount(String accountName, String password)
			throws EmailAccountCreationException {
		CreateAccountResponse createResp;
		try {
			CreateAccountRequest createRequest = new CreateAccountRequest();
			createRequest.setName(accountName);
			createRequest.setPassword(password);

			createRequest.getA().add(ZimbraEmailIntegrationHelper.createZimbraAttribute(IS_ADMIN_ACCOUNT, "TRUE"));
			createRequest.getA()
					.add(ZimbraEmailIntegrationHelper.createZimbraAttribute(DISPLAY_NAME, "System Account"));

			createResp = authenticationService.getAdminPort().createAccountRequest(createRequest);
		} catch (SOAPFaultException e) {
			LOGGER.error("Tenant admin account creation failed " + e.getMessage(), e);
			throw new EmailAccountCreationException("Tenant admin account creation failed " + e.getMessage(), e);
		}

		return createEmailAccountInformation(createResp.getAccount());
	}

	@Override
	public void deleteAccount(String accountId) throws EmailAccountDeleteException {
		DeleteAccountRequest deleteRequest = new DeleteAccountRequest();
		deleteRequest.setId(accountId);
		try {
			authenticationService.getAdminPort().deleteAccountRequest(deleteRequest);
		} catch (SOAPFaultException e) {
			LOGGER.error("Account deletion failed for account with id: " + accountId, e);
			throw new EmailAccountDeleteException("Account deletion failed for account with id: " + accountId, e);
		}
		LOGGER.info("Account with id: [{}] deleted successfuly.", accountId);
	}

	@Override
	public void renameAccount(String accountId, String newName) throws EmailIntegrationException {
		RenameAccountRequest renameRequest = new RenameAccountRequest();
		renameRequest.setId(accountId);
		renameRequest.setNewName(newName);

		RenameAccountResponse renameResponse;
		try {
			renameResponse = authenticationService.getTenantAdminPort().renameAccountRequest(renameRequest);
		} catch (SOAPFaultException e) {
			LOGGER.error("Account rename failed for account with id:" + accountId, e);
			throw new EmailIntegrationException("Account rename failed for account with id:" + accountId, e);
		}
		if (renameResponse != null) {
			AccountInfo renamedAcc = renameResponse.getAccount();
			LOGGER.info("Renamed successfully, account with Id: {} new name:", renamedAcc.getId(),
					renamedAcc.getName());
		}
	}

	@Override
	public void modifyDelegatePermission(String target, String grantee, boolean modifyFlag)
			throws EmailIntegrationException {
		if (modifyFlag) {
			grantDelegateRight(target, grantee);
		} else {
			removeDelegateRight(target, grantee);
		}
	}

	@Override
	public EmailAccountInformation modifyAccount(String accountId, List<GenericAttribute> attributes)
			throws EmailIntegrationException {
		ModifyAccountRequest modifyRequest = new ModifyAccountRequest();
		modifyRequest.setId(accountId);
		attributes.forEach(item -> modifyRequest.getA().add(ZimbraEmailIntegrationHelper.createZimbraAttribute(item)));
		ModifyAccountResponse modifyResponse = authenticationService.getTenantAdminPort()
				.modifyAccountRequest(modifyRequest);
		LOGGER.info("Account modification successful!");
		if (modifyResponse != null) {
			return createEmailAccountInformation(modifyResponse.getAccount());
		}
		return null;
	}

	@Override
	public void disableAccount(String accountId) throws EmailIntegrationException {
		try {
			modifyAccount(accountId, Collections.singletonList(new GenericAttribute("zimbraAccountStatus", "closed")));
		} catch (SOAPFaultException e) {
			LOGGER.error("Account disable failed for account:" + accountId, e);
			throw new EmailIntegrationException("Account disable failed for account:" + accountId, e);
		}
	}

	@Override
	public EmailAccountInformation getAccount(String accountName) throws EmailIntegrationException {
		return getAccount(accountName, null);
	}

	@Override
	public EmailAccountInformation getAccount(String accountName, List<String> accountAttributes)
			throws EmailIntegrationException {
		GetAccountRequest getAccount = new GetAccountRequest();
		AccountSelector accSelector = new AccountSelector();
		accSelector.setBy(AccountBy.NAME);
		accSelector.setValue(accountName);
		getAccount.setAccount(accSelector);
		GetAccountResponse accInfoResponse;
		if (accountAttributes != null && !accountAttributes.isEmpty()) {
			StringBuilder queriedAttributesBuilder = new StringBuilder();
			for (String attribute : accountAttributes) {
				queriedAttributesBuilder.append(attribute).append(",");
			}
			// remove the last ","
			String queriedAttributes = queriedAttributesBuilder.substring(0, queriedAttributesBuilder.length() - 1);
			getAccount.setAttrs(queriedAttributes);
		}

		try {
			accInfoResponse = authenticationService.getAdminPort().getAccountRequest(getAccount);
		} catch (SOAPFaultException e) {
			LOGGER.error("Account retrieval failed for account:" + accountName, e);
			throw new EmailIntegrationException("Account retrieval failed for account:" + accountName, e);
		}

		if (accInfoResponse != null) {
			return createEmailAccountInformation(accInfoResponse.getAccount());
		}
		return null;
	}

	@Override
	public List<EmailAccountInformation> getAllAccounts() throws EmailIntegrationException {
		List<EmailAccountInformation> retrievedAccounts = new ArrayList<>();

		GetAllAccountsResponse getAllResponse;
		try {
			getAllResponse = authenticationService.getTenantAdminPort()
					.getAllAccountsRequest(new GetAllAccountsRequest());
		} catch (SOAPFaultException e) {
			LOGGER.error("Accounts retrieval failed", e);
			throw new EmailIntegrationException("Accounts retrieval failed", e);
		}
		if (getAllResponse != null) {
			getAllResponse.getAccount()
					.forEach(zimbraAccount -> retrievedAccounts.add(createEmailAccountInformation(zimbraAccount)));
		}
		return Collections.emptyList();
	}

	@Override
	public List<String> getWhiteList(String emailName, String emailPassword) throws EmailIntegrationException {
		GetWhiteBlackListRequest request = new GetWhiteBlackListRequest();
		GetWhiteBlackListResponse response;
		try {
			response = authenticationService.getClientPort(emailName, emailPassword).getWhiteBlackListRequest(request);
		} catch (SOAPFaultException e) {
			LOGGER.error("White list retrieval failed for account:" + emailName, e);
			throw new EmailIntegrationException("White list retrieval failed for account:" + emailName, e);
		}
		if (response != null) {
			return response.getWhiteList().getAddr();
		}
		// return empty list if the list is empty.
		return new ArrayList<>();
	}

	@Override
	public void modifyWhiteList(String emailName, String password, List<String> emailAdresses, String operation)
			throws EmailIntegrationException {
		ModifyWhiteBlackListRequest modifyRequest = new ModifyWhiteBlackListRequest();
		ModifyWhiteBlackListRequest.WhiteList addReq = new ModifyWhiteBlackListRequest.WhiteList();
		emailAdresses.forEach(item -> addReq.getAddr().add(createOperationValue(operation, item)));

		modifyRequest.setWhiteList(addReq);
		try {
			authenticationService.getClientPort(emailName, password).modifyWhiteBlackListRequest(modifyRequest);
		} catch (SOAPFaultException e) {
			LOGGER.error("Error occured while modifying account whitelist for account:" + emailName, e);
			throw new EmailIntegrationException(
					"Error occured while modifying account whitelist for account:" + emailName, e);
		}
	}

	private void removeDelegateRight(String target, String grantee) throws EmailIntegrationException {
		RevokeRightRequest req = new RevokeRightRequest();
		req.setGrantee(createGrantee(grantee));
		req.setTarget(createTarget(target, TargetType.ACCOUNT));

		RightModifierInfo right = new RightModifierInfo();
		right.setValue("sendAs");
		req.setRight(right);
		try {
			authenticationService.getTenantAdminPort().revokeRightRequest(req);
		} catch (SOAPFaultException e) {
			LOGGER.error("Removal of delegation rights failed for account:" + target, e);
			throw new EmailIntegrationException("Removal of delegation rights failed for account:" + target, e);
		}
	}

	private void grantDelegateRight(String target, String grantee) throws EmailIntegrationException {
		GrantRightRequest req = new GrantRightRequest();
		req.setGrantee(createGrantee(grantee));
		req.setTarget(createTarget(target, TargetType.ACCOUNT));

		RightModifierInfo right = new RightModifierInfo();
		right.setValue("sendAs");
		req.setRight(right);
		try {
			authenticationService.getTenantAdminPort().grantRightRequest(req);
		} catch (SOAPFaultException e) {
			LOGGER.error("Addition of delegation rights failed for account:" + target, e);
			throw new EmailIntegrationException("Addition of delegation rights failed for account:" + target, e);
		}
	}

	@Override
	public void modifyAdminAccount(String target) throws EmailIntegrationException {
		List<GenericAttribute> attributes = new LinkedList<>();
		attributes.add(new GenericAttribute(DELEGATED_ADMIN_ACCOUNT, "TRUE"));
		attributes.add(new GenericAttribute(ADMIN_UI_COMPONENTS, "cartBlancheUI"));
		attributes.add(new GenericAttribute(ADMIN_UI_COMPONENTS, "domainListView"));
		attributes.add(new GenericAttribute(ADMIN_UI_COMPONENTS, "accountListView"));
		attributes.add(new GenericAttribute(ADMIN_UI_COMPONENTS, "DLListView"));
		attributes.add(new GenericAttribute(MAX_MAIL_QUOTA, "0"));
		try {
			modifyAccount(getAccount(target).getAccountId(), attributes);
		} catch (SOAPFaultException e) {
			throw new EmailIntegrationException("Faild to modify account:" + target, e);
		}
	}

	@Override
	public void grantAdminDomainRights(String grantee) throws EmailIntegrationException {
		List<String> rightsList = Arrays.asList("createAccount", "createAlias", "createCalendarResource",
				"createDistributionList", "deleteAlias", "listDomain", "listAccount", "configureQuota");
		grantRights(emailIntegrationConfiguration.getTenantDomainAddress().get(), grantee, TargetType.DOMAIN,
				rightsList);
	}

	@Override
	public void grantAdminAccountRights(String target) throws EmailIntegrationException {
		List<String> rightsList = Arrays.asList("getAccountInfo", "getAccountMembership", "getMailboxInfo",
				"listAccount", "removeAccountAlias", "renameAccount", "setAccountPassword", "viewAccountAdminUI",
				"configureQuota");
		grantRights(target, target, TargetType.ACCOUNT, rightsList);
	}

	private void grantRights(String target, String grantee, TargetType type, List<String> rightsList)
			throws EmailIntegrationException {
		GrantRightRequest rightRequest = new GrantRightRequest();
		rightRequest.setGrantee(createGrantee(grantee));
		rightRequest.setTarget(createTarget(target, type));
		RightModifierInfo right = new RightModifierInfo();

		try {
			for (String rightId : rightsList) {
				right.setValue(rightId);
				rightRequest.setRight(right);
				authenticationService.getTenantAdminPort().grantRightRequest(rightRequest);
			}
		} catch (SOAPFaultException e) {
			throw new EmailIntegrationException("Faild to grant admin rights for account:" + target, e);
		}
	}

	/**
	 * Creates a generic {@link EmailAccountInformation} from a zimbra {@link AccountInfo}
	 *
	 * @param accInfo
	 *            zimbra {@link AccountInfo} object.
	 * @return generic {@link EmailAccountInformation}
	 */
	private static EmailAccountInformation createEmailAccountInformation(AccountInfo accInfo) {
		EmailAccountInformation accInformation = new EmailAccountInformation();
		accInformation.setAccountId(accInfo.getId());
		accInformation.setAccountName(accInfo.getName());
		accInformation.setAttributes(ZimbraEmailIntegrationHelper.toGenericAttributeList(accInfo.getA()));

		return accInformation;
	}

	/**
	 * Creates a zimbra {@link OpValue} used in white/black list modifications.
	 *
	 * @param op
	 *            operation
	 * @param value
	 *            value
	 * @return created operation value
	 */
	private static OpValue createOperationValue(String op, String value) {
		OpValue operation = new OpValue();
		operation.setOp(op);
		operation.setValue(value);
		return operation;
	}

	/**
	 * Apply sep theme and disable unneeded features which intervene with the theme look and feel.
	 */
	private static void initializeAccountAttributes(CreateAccountRequest createRequest,
			Map<String, String> attributes) {
		if (attributes != null && !attributes.isEmpty()) {
			List<Attr> createRequestAttrs = createRequest.getA();

			attributes.forEach((key, value) -> createRequestAttrs
					.add(ZimbraEmailIntegrationHelper.createZimbraAttribute(key, value)));
		}
	}

	/**
	 * Creates a {@link EffectiveRightsTargetSelector} needed for email account modification. Uses account name as an
	 * identificator.
	 *
	 * @param target
	 *            target email address.
	 */
	private static EffectiveRightsTargetSelector createTarget(String target, TargetType type) {
		EffectiveRightsTargetSelector targetSel = new EffectiveRightsTargetSelector();
		targetSel.setType(type);
		targetSel.setBy(TargetBy.NAME);
		targetSel.setValue(target);
		return targetSel;
	}

	/**
	 * Creates a {@link GranteeSelector} needed for email account modification.Uses account name as an identificator.
	 *
	 * @param grantee
	 *            grantee email address.
	 */
	private static GranteeSelector createGrantee(String grantee) {
		GranteeSelector granteeSel = new GranteeSelector();
		granteeSel.setType(GranteeType.USR);
		granteeSel.setBy(GranteeBy.NAME);
		granteeSel.setValue(grantee);
		return granteeSel;
	}
}
