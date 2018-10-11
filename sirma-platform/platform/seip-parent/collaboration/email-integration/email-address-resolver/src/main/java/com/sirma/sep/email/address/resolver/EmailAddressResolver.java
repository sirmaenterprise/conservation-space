package com.sirma.sep.email.address.resolver;

import java.util.ArrayList;
import java.util.List;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import com.sirma.itt.seip.Pair;
import com.sirma.itt.seip.db.CoreDb;
import com.sirma.itt.seip.db.DbDao;

/**
 * Resolver for instance email address
 *
 * @author S.Djulgerova
 */
@ApplicationScoped
public class EmailAddressResolver {

	@Inject
	@CoreDb
	private DbDao dbDao;

	/**
	 * Select record from email address table by given email address
	 *
	 * @param emailAddress
	 *            email address
	 * @return email address information or null if email address don't exist
	 */
	public EmailAddress getEmailAddress(String emailAddress) {
		List<Pair<String, Object>> args = new ArrayList<>(1);
		args.add(new Pair<String, Object>("emailAddress", emailAddress));
		List<EmailAddress> result = dbDao.fetchWithNamed(EmailAddress.QUERY_EMAIL_ADDRESS_BY_EMAIL_KEY, args);
		if (result.isEmpty()) {
			return null;
		}
		return result.get(0);
	}

	/**
	 * Select record from email address table by given instance id and tenant id
	 *
	 * @param instanceId
	 *            instance id
	 * @param tenantId
	 *            tenant id
	 * @return email address information or null if email address don't exist
	 */
	public EmailAddress getEmailAddress(String instanceId, String tenantId) {
		List<Pair<String, Object>> args = new ArrayList<>(2);
		args.add(new Pair<String, Object>("instanceId", instanceId));
		args.add(new Pair<String, Object>("tenantId", tenantId));
		List<EmailAddress> result = dbDao.fetchWithNamed(EmailAddress.QUERY_EMAIL_ADDRESS_OF_INSTANCE_KEY, args);
		if (result.isEmpty()) {
			return null;
		}
		return result.get(0);
	}

	/**
	 * Retrieves all tenants that use the queried mail domain name.
	 * 
	 * @param mailDomain
	 *            mail domain name that will be checked how many tenants are using it.
	 * @return list of tenants that use the same domain name.
	 */
	public List<String> getAllTenantsInDomain(String mailDomain) {
		List<Pair<String, Object>> args = new ArrayList<>(1);
		args.add(new Pair<String, Object>("mailDomain", mailDomain));
		return dbDao.fetchWithNamed(EmailAddress.QUERY_TENANT_ID_BY_MAIL_DOMAIN_KEY, args);
	}

	/**
	 * Retrieves all email accounts created in tenant.
	 * 
	 * @param tenantId
	 *            queried tenantId
	 * @return a {@link EmailAddress} list of created accounts by the tenant.
	 */
	public List<EmailAddress> getAllEmailsByTenant(String tenantId) {
		List<Pair<String, Object>> args = new ArrayList<>(1);
		args.add(new Pair<String, Object>("tenantId", tenantId));
		return dbDao.fetchWithNamed(EmailAddress.QUERY_EMAIL_ADDRESS_BY_MAIL_TENANT_ID_KEY, args);
	}

	/**
	 * Insert new email address
	 *
	 * @param instanceId
	 *            instance id
	 * @param tenantId
	 *            tenant id
	 * @param emailAddress
	 *            generated email address
	 * @param mailDomain
	 *            domain where the email address belongs to
	 */
	public void insertEmailAddress(String instanceId, String tenantId, String emailAddress, String mailDomain) {
		EmailAddress entity = new EmailAddress(tenantId, instanceId, emailAddress, mailDomain);
		dbDao.saveOrUpdate(entity);
	}

	/**
	 * Delete existing email address
	 *
	 * @param emailAddress
	 *            email address
	 */
	public void deleteEmailAddress(String emailAddress) {
		List<Pair<String, Object>> args = new ArrayList<>(1);
		args.add(new Pair<String, Object>("emailAddress", emailAddress));
		dbDao.executeUpdate(EmailAddress.DELETE_EMAIL_ADDRESS_BY_EMAIL_KEY, args);
	}

}
