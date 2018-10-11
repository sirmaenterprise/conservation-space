package com.sirma.sep.email.service;

import static com.sirma.email.ZimbraEmailIntegrationConstants.MAIL_DELIVERY_ADDRESS;
import static com.sirma.email.ZimbraEmailIntegrationConstants.PREF_FROM_ADDRESS;
import static com.sirma.email.ZimbraEmailIntegrationConstants.PREF_FROM_DISPLAY;
import static com.sirma.sep.email.EmailIntegrationConstants.DISPLAY_NAME;
import static com.sirma.sep.email.EmailIntegrationConstants.EMAIL_ADDRESS;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import javax.inject.Inject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sirma.itt.seip.domain.instance.Instance;
import com.sirma.itt.seip.domain.instance.InstanceReference;
import com.sirma.itt.seip.instance.DomainInstanceService;
import com.sirma.itt.seip.instance.InstanceTypeResolver;
import com.sirma.itt.seip.permissions.InstanceAccessEvaluator;
import com.sirma.sep.email.exception.EmailIntegrationException;
import com.sirma.sep.email.model.account.GenericAttribute;

/**
 * {@link EmailAccountDelegationService} service implementation
 * 
 * @author g.tsankov
 */
public class EmailAccountDelegationServiceImpl implements EmailAccountDelegationService {

	private static final Logger LOGGER = LoggerFactory.getLogger(EmailAccountDelegationServiceImpl.class);

	@Inject
	private EmailAccountAdministrationService administrationService;

	@Inject
	private InstanceAccessEvaluator accessEvaluator;

	@Inject
	private DomainInstanceService domainInstanceService;

	@Inject
	private InstanceTypeResolver instanceTypeResolver;

	@Override
	public void modifyAccountDelegationPermission(String target, String granteeId, boolean shouldAdd)
			throws EmailIntegrationException {
		Optional<InstanceReference> reference = instanceTypeResolver.resolveReference(granteeId);
		Instance instance = domainInstanceService.loadInstance(granteeId);
		if (!reference.isPresent() || instance == null) {
			LOGGER.warn("Instance not found or reference is not present.");
			throw new EmailIntegrationException("Instance not found or reference is not present.");
		}

		String granteeEmailAddress = null;
		if (accessEvaluator.canRead(reference.get(), instance)) {
			granteeEmailAddress = (String) instance.getProperties().get(EMAIL_ADDRESS);
		}

		if (granteeEmailAddress == null) {
			LOGGER.warn("No Email Address present for instance: {}", granteeId);
			throw new EmailIntegrationException("No Email Address present for instance:" + granteeId);
		}

		administrationService.modifyDelegatePermission(target, granteeEmailAddress, shouldAdd);
	}

	@Override
	public Map<String, String> getEmailAccountAttributes(String target) throws EmailIntegrationException {
		Map<String, String> attributes = new HashMap<>();
		String prefEmailAddress = null;
		String prefDisplayName = null;
		String emailAddress = null;
		String displayName = null;
		try {
			for (GenericAttribute attribute : administrationService.getAccount(target).getAttributes()) {
				if (PREF_FROM_ADDRESS.equals(attribute.getAttributeName())) {
					prefEmailAddress = attribute.getValue();
				}
				if (PREF_FROM_DISPLAY.equals(attribute.getAttributeName())) {
					prefDisplayName = attribute.getValue();
				}
				if (MAIL_DELIVERY_ADDRESS.equals(attribute.getAttributeName())) {
					emailAddress = attribute.getValue();
				}
				if (DISPLAY_NAME.equals(attribute.getAttributeName())) {
					displayName = attribute.getValue();
				}
			}
			if (prefEmailAddress == null) {
				attributes.put(EMAIL_ADDRESS, emailAddress);
				attributes.put(DISPLAY_NAME, displayName);
			} else {
				attributes.put(EMAIL_ADDRESS, prefEmailAddress);
				attributes.put(DISPLAY_NAME, prefDisplayName);
			}

		} catch (EmailIntegrationException e) {
			throw new EmailIntegrationException("Can not extract account information for account:" + target, e);
		}
		return attributes;
	}
}
