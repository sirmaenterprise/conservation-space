package com.sirma.sep.account.administration;

import static com.sirma.email.ZimbraEmailIntegrationConstants.CLASS_OF_SERVICE_CONFIG;

import java.io.Serializable;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import javax.annotation.PostConstruct;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.xml.ws.soap.SOAPFaultException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sirma.email.ZimbraEmailIntegrationConstants;
import com.sirma.email.ZimbraEmailIntegrationHelper;
import com.sirma.itt.seip.configuration.ConfigurationProperty;
import com.sirma.itt.seip.runtime.boot.Startup;
import com.sirma.sep.email.PreAuthUtility;
import com.sirma.sep.email.configuration.EmailIntegrationConfiguration;
import com.sirma.sep.email.exception.EmailIntegrationException;
import com.sirma.sep.email.model.account.GenericAttribute;
import com.sirma.sep.email.model.domain.ClassOfServiceInformation;
import com.sirma.sep.email.model.domain.DomainInformation;
import com.sirma.sep.email.service.DomainAdministrationService;

import zimbraadmin.Attr;
import zimbraadmin.CosBy;
import zimbraadmin.CosInfo;
import zimbraadmin.CosSelector;
import zimbraadmin.CreateCosRequest;
import zimbraadmin.CreateCosResponse;
import zimbraadmin.CreateDomainRequest;
import zimbraadmin.DeleteCosRequest;
import zimbraadmin.DeleteDomainRequest;
import zimbraadmin.DomainBy;
import zimbraadmin.DomainInfo;
import zimbraadmin.DomainSelector;
import zimbraadmin.GetCosRequest;
import zimbraadmin.GetCosResponse;
import zimbraadmin.GetDomainInfoRequest;
import zimbraadmin.GetDomainRequest;
import zimbraadmin.GetDomainResponse;
import zimbraadmin.ModifyCosRequest;
import zimbraadmin.ModifyDomainRequest;

/**
 * Zimbra email Server Domain administration service. Used for common operations on domains.
 *
 * @author g.tsankov
 */
@Startup
@ApplicationScoped
public class DomainAdministrationServiceImpl implements DomainAdministrationService, Serializable {

	private static final long serialVersionUID = -5357362769928223728L;

	private static final Logger LOGGER = LoggerFactory.getLogger(DomainAdministrationServiceImpl.class);

	private static final String CONFIG_LISTENER_FAIL = "Could not modify class of service after update of configuration";

	private static final String DOMAIN_HASHING_FAILED = "Domain hashing failed for domain name:";

	// zimbra returns this id when a domain does not exist.
	private static final String NON_EXISTENT_DOMAIN_ID = "globalconfig-dummy-id";

	@Inject
	private EmailIntegrationConfiguration emailIntegrationConfiguration;

	@Inject
	private AccountAuthenticationService authenticationService;

	/**
	 * Configuration listeners for certain tenant configs need to be initialized here.
	 */
	@PostConstruct
	public void initialize() {
		initConfigurationListeners();
	}

	@Override
	public void createDomain(String domainName) throws EmailIntegrationException {
		CreateDomainRequest req = new CreateDomainRequest();
		req.setName(domainName);
		try {
			req.getA().add(ZimbraEmailIntegrationHelper.createZimbraAttribute("zimbraPreAuthKey",
					PreAuthUtility.generatePreauthHash(domainName)));
			req.getA().add(ZimbraEmailIntegrationHelper.createZimbraAttribute(
					ZimbraEmailIntegrationConstants.CLASS_OF_SERVICE_CONFIG, createCoS(domainName)));

			authenticationService.getAdminPort().createDomainRequest(req);

			LOGGER.debug("Email domain '{}' was successfuly created.", domainName);
		} catch (SOAPFaultException | EmailIntegrationException e) {
			throw new EmailIntegrationException("Domain creation failed for domain name:" + domainName, e);
		}
	}

	@Override
	public void modifyDomain(String domainName, String attributeName, String attributeValue)
			throws EmailIntegrationException {
		try {
			ModifyDomainRequest req = new ModifyDomainRequest();
			Optional<DomainInformation> domainInfo = getDomain(domainName);
			if (domainInfo.isPresent()) {
				req.setId(domainInfo.get().getDomainId());
				req.getA().add(ZimbraEmailIntegrationHelper.createZimbraAttribute(attributeName, attributeValue));
				authenticationService.getAdminPort().modifyDomainRequest(req);
			} else {
				LOGGER.error("Modification of non existing domain has been requested.");
			}
		} catch (SOAPFaultException e) {
			throw new EmailIntegrationException("Domain modification failed for domain:" + domainName, e);
		}
	}

	@Override
	public Optional<DomainInformation> getDomain(String domainName) throws EmailIntegrationException {
		GetDomainRequest req = new GetDomainRequest();
		DomainSelector selector = new DomainSelector();
		selector.setBy(DomainBy.NAME);
		selector.setValue(domainName);
		req.setDomain(selector);
		GetDomainResponse response;

		if (NON_EXISTENT_DOMAIN_ID.equals(getDomainId(selector))) {
			LOGGER.warn("A request for retrieval of non existent domain was made for domain name: {}", domainName);
			return Optional.empty();
		}

		try {
			response = authenticationService.getAdminPort().getDomainRequest(req);
		} catch (SOAPFaultException e) {
			throw new EmailIntegrationException("Domain information retrieval failed for domain name:" + domainName, e);
		}

		DomainInfo domainInfo = response.getDomain();
		return Optional.of(new DomainInformation(domainInfo.getId(), domainInfo.getName(),
				ZimbraEmailIntegrationHelper.toGenericAttributeList(domainInfo.getA())));
	}

	private String getDomainId(DomainSelector selector) throws EmailIntegrationException {
		GetDomainInfoRequest req = new GetDomainInfoRequest();
		req.setDomain(selector);
		try {
			return authenticationService.getAdminPort().getDomainInfoRequest(req).getDomain().getId();
		} catch (SOAPFaultException e) {
			throw new EmailIntegrationException(
					"Domain information retrieval failed for domain name:" + selector.getValue(), e);
		}
	}

	@Override
	public void deleteDomain(DomainInformation domainInfo) throws EmailIntegrationException {
		String domainId = domainInfo.getDomainId();
		DeleteDomainRequest req = new DeleteDomainRequest();
		req.setId(domainId);
		try {
			deleteCos(domainInfo.getDomainName());
			authenticationService.getAdminPort().deleteDomainRequest(req);
		} catch (SOAPFaultException e) {
			throw new EmailIntegrationException("Domain deletion failed for domain id:" + domainId, e);
		}
	}

	@Override
	public String createCoS(String cosName) throws EmailIntegrationException {
		CreateCosRequest request = new CreateCosRequest();
		request.setName(cosName);
		addSystemProperties(request.getA());
		try {
			CreateCosResponse response = authenticationService.getAdminPort().createCosRequest(request);
			return response.getCos().getId();
		} catch (SOAPFaultException e) {
			throw new EmailIntegrationException("Class of service creation faild for named service:" + cosName, e);
		}
	}

	@Override
	public ClassOfServiceInformation getCosByName(String cosName) throws EmailIntegrationException {
		GetCosRequest request = prepareCosRequest(CosBy.NAME, cosName);
		return getCos(request);
	}

	@Override
	public ClassOfServiceInformation getCosById(String cosId) throws EmailIntegrationException {
		GetCosRequest request = prepareCosRequest(CosBy.ID, cosId);
		return getCos(request);
	}

	@Override
	public String extractCosFromDomainAddress(Optional<DomainInformation> domainInfo) throws EmailIntegrationException {
		String cosName = "";
		List<GenericAttribute> domainAttributes = domainInfo.map(DomainInformation::getAttributes)
				.orElse(Collections.emptyList());
		for (GenericAttribute domainAttribute : domainAttributes) {
			if (CLASS_OF_SERVICE_CONFIG.equals(domainAttribute.getAttributeName())) {
				cosName = getCosById(domainAttribute.getValue()).getCosName();
				break;
			}
		}
		return cosName;
	}

	private ClassOfServiceInformation getCos(GetCosRequest request) throws EmailIntegrationException {
		try {
			GetCosResponse response = authenticationService.getAdminPort().getCosRequest(request);
			CosInfo cosInfo = response.getCos();
			return new ClassOfServiceInformation(cosInfo.getId(), cosInfo.getName(),
					ZimbraEmailIntegrationHelper.toGenericAttributeList(cosInfo.getA()));
		} catch (SOAPFaultException e) {
			if (e.getMessage().contains("no such cos")) {
				return new ClassOfServiceInformation(null, null, null);
			}
			throw new EmailIntegrationException("Getting Class of Service information failed" + e.getMessage(), e);
		}
	}

	private static GetCosRequest prepareCosRequest(CosBy by, String value) {
		GetCosRequest request = new GetCosRequest();
		CosSelector selector = new CosSelector();
		selector.setBy(by);
		selector.setValue(value);
		request.setCos(selector);
		return request;
	}

	@Override
	public void modifyCos(String cosId, String attributeName, String attributeValue) throws EmailIntegrationException {
		ModifyCosRequest req = new ModifyCosRequest();
		req.setId(cosId);
		req.getA().add(ZimbraEmailIntegrationHelper.createZimbraAttribute(attributeName, attributeValue));
		try {
			authenticationService.getAdminPort().modifyCosRequest(req);
		} catch (SOAPFaultException e) {
			throw new EmailIntegrationException("Class of service modification failed for CoS with id:" + cosId, e);
		}

	}

	@Override
	public void deleteCos(String cosName) throws EmailIntegrationException {
		DeleteCosRequest request = new DeleteCosRequest();
		try {
			String cosId = getCosByName(cosName).getCosId();
			request.setId(cosId);
			authenticationService.getAdminPort().deleteCosRequest(request);
		} catch (SOAPFaultException e) {
			throw new EmailIntegrationException("Class of service deletion failed for CoS:" + cosName, e);
		}
	}

	/**
	 * Configurations needed for domain or class of service administration need configuration listeners so that the
	 * class of service or domain is modified appropriately.
	 */
	private void initConfigurationListeners() {
		emailIntegrationConfiguration.getTenantClassOfService().addConfigurationChangeListener(clf -> {
			try {
				String cosId = getCosByName(clf.get()).getCosId();
				if (cosId == null) {
					cosId = createCoS(clf.get());
				}
				modifyDomain(emailIntegrationConfiguration.getTenantDomainAddress().get(),
						ZimbraEmailIntegrationConstants.CLASS_OF_SERVICE_CONFIG, cosId);
			} catch (EmailIntegrationException e) {
				LOGGER.error("New Class of Service could not be applied to:"
						+ emailIntegrationConfiguration.getTenantDomainAddress().get(), e);
			}
		});

		assignCoSConfigListener(emailIntegrationConfiguration.getCalendarEnabled(),
				ZimbraEmailIntegrationConstants.CALENDAR_ENABLED);
		assignCoSConfigListener(emailIntegrationConfiguration.getContactsEnabled(),
				ZimbraEmailIntegrationConstants.CONTACTS_ENABLED);
		assignCoSConfigListener(emailIntegrationConfiguration.getFeatureOptionsEnabled(),
				ZimbraEmailIntegrationConstants.OPTIONS_ENABLED);
		assignCoSConfigListener(emailIntegrationConfiguration.getFeatureTaskEnabled(),
				ZimbraEmailIntegrationConstants.TASKS_ENABLED);
		assignCoSConfigListener(emailIntegrationConfiguration.getGroupCalendarEnabled(),
				ZimbraEmailIntegrationConstants.GROUP_CALENDAR_ENABLED);
		assignCoSConfigListener(emailIntegrationConfiguration.getCalendarEnabled(),
				ZimbraEmailIntegrationConstants.CALENDAR_ENABLED);
		assignCoSConfigListener(emailIntegrationConfiguration.getMailViewPreference(),
				ZimbraEmailIntegrationConstants.MAIL_VIEW_PREFERENCE);
		assignCoSConfigListener(emailIntegrationConfiguration.getBriefcasesEnabled(),
				ZimbraEmailIntegrationConstants.BRIEFCASE_ENABLED);
		assignCoSConfigListener(emailIntegrationConfiguration.getTaggingEnabled(),
				ZimbraEmailIntegrationConstants.TAGGING_ENABLED);
		assignCoSConfigListener(emailIntegrationConfiguration.getSavedSearchesEnabled(),
				ZimbraEmailIntegrationConstants.SAVED_SEARCHES_ENABLED);
		assignCoSConfigListener(emailIntegrationConfiguration.getSkin(), ZimbraEmailIntegrationConstants.PREF_SKIN);
	}

	private void assignCoSConfigListener(ConfigurationProperty<String> cp, String configName) {
		cp.addConfigurationChangeListener(clf -> {
			try {
				modifyCos(getCosByName(emailIntegrationConfiguration.getTenantClassOfService().get()).getCosId(),
						configName, clf.get());
			} catch (EmailIntegrationException e) {
				LOGGER.error(CONFIG_LISTENER_FAIL + cp.getName(), e);
			}
		});
	}

	private void addSystemProperties(List<Attr> list) {
		list.add(ZimbraEmailIntegrationHelper.createZimbraAttribute(ZimbraEmailIntegrationConstants.CALENDAR_ENABLED,
				emailIntegrationConfiguration.getCalendarEnabled().get()));
		list.add(ZimbraEmailIntegrationHelper.createZimbraAttribute(ZimbraEmailIntegrationConstants.CONTACTS_ENABLED,
				emailIntegrationConfiguration.getContactsEnabled().get()));
		list.add(ZimbraEmailIntegrationHelper.createZimbraAttribute(ZimbraEmailIntegrationConstants.OPTIONS_ENABLED,
				emailIntegrationConfiguration.getFeatureOptionsEnabled().get()));
		list.add(ZimbraEmailIntegrationHelper.createZimbraAttribute(ZimbraEmailIntegrationConstants.TASKS_ENABLED,
				emailIntegrationConfiguration.getFeatureTaskEnabled().get()));
		list.add(ZimbraEmailIntegrationHelper.createZimbraAttribute(
				ZimbraEmailIntegrationConstants.GROUP_CALENDAR_ENABLED,
				emailIntegrationConfiguration.getGroupCalendarEnabled().get()));
		list.add(ZimbraEmailIntegrationHelper.createZimbraAttribute(ZimbraEmailIntegrationConstants.BRIEFCASE_ENABLED,
				emailIntegrationConfiguration.getBriefcasesEnabled().get()));
		list.add(ZimbraEmailIntegrationHelper.createZimbraAttribute(ZimbraEmailIntegrationConstants.TAGGING_ENABLED,
				emailIntegrationConfiguration.getTaggingEnabled().get()));
		list.add(ZimbraEmailIntegrationHelper.createZimbraAttribute(
				ZimbraEmailIntegrationConstants.SAVED_SEARCHES_ENABLED,
				emailIntegrationConfiguration.getCalendarEnabled().get()));
		list.add(ZimbraEmailIntegrationHelper
				.createZimbraAttribute(ZimbraEmailIntegrationConstants.COMPOSE_IN_NEW_WINDOW_ENABLED, "FALSE"));
		list.add(
				ZimbraEmailIntegrationHelper.createZimbraAttribute(ZimbraEmailIntegrationConstants.MAIL_VIEW_PREFERENCE,
						emailIntegrationConfiguration.getMailViewPreference().get()));
		list.add(ZimbraEmailIntegrationHelper.createZimbraAttribute(ZimbraEmailIntegrationConstants.PREF_SKIN,
				emailIntegrationConfiguration.getSkin().get()));
		list.add(ZimbraEmailIntegrationHelper.createZimbraAttribute(ZimbraEmailIntegrationConstants.ALLOW_FROM_ADDRESS,
				"TRUE"));
	}
}
