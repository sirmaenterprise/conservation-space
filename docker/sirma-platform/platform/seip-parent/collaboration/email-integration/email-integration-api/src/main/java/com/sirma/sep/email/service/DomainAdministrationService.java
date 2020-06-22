package com.sirma.sep.email.service;

import java.util.Optional;

import com.sirma.sep.email.exception.EmailIntegrationException;
import com.sirma.sep.email.model.domain.ClassOfServiceInformation;
import com.sirma.sep.email.model.domain.DomainInformation;

/**
 * Service used for email server domain administration like creation/deletion/information for domain and class of
 * service requests.
 *
 * @author g.tsankov
 */
public interface DomainAdministrationService {
	/**
	 * Creates a domain in the mail server.
	 *
	 * @param domainName
	 *            new name that will be added.
	 * @throws EmailIntegrationException
	 *             throw if a problem occurs with communication with the mail server
	 */
	public void createDomain(String domainName) throws EmailIntegrationException;

	/**
	 * Gets information about domain. It will return an {@link Optional} {@link DomainInformation} with populated
	 * id,name and attributes list if the domain is valid or none if the domain is not existent in the mail server.
	 *
	 * @param domainName
	 *            domain name that will be retrieved.
	 * @throws EmailIntegrationException
	 *             throw if a problem occurs with communication with the mail server
	 * @returns {@link Optional}<{@link DomainInformation}> about the requested domain.
	 */
	public Optional<DomainInformation> getDomain(String domainName) throws EmailIntegrationException;

	/**
	 * Modifies mail domain attribute.
	 *
	 * @param domainName
	 *            name of the domain
	 * @param attributeName
	 *            name of the attribute to be modified
	 * @param attributeValue
	 *            new value
	 * @throws EmailIntegrationException
	 *             throw if a problem occurs with communication with the mail server
	 */
	void modifyDomain(String domainName, String attributeName, String attributeValue) throws EmailIntegrationException;

	/**
	 * Deletes mail server domain specified by ID.
	 *
	 * @param domainInfo
	 *            id of the domain.
	 * @throws EmailIntegrationException
	 *             throw if a problem occurs with communication with the mail server
	 */
	public void deleteDomain(DomainInformation domainInfo) throws EmailIntegrationException;

	/**
	 * Creates a domain specific class of service.
	 *
	 * @param cosName
	 *            name of the class of service
	 * @return created class of service id.
	 * @throws EmailIntegrationException
	 *             throw if a problem occurs with communication with the mail server
	 */
	public String createCoS(String cosName) throws EmailIntegrationException;

	/**
	 * Gets information about class of service.
	 *
	 * @param cosName
	 *            name of the class of service.
	 * @return {@link ClassOfServiceInformation} wrapped CosInfo.
	 * @throws EmailIntegrationException
	 *             throw if a problem occurs with communication with the mail server. thrown if a non existent CoS is
	 *             queried from the server. This exception is catched and an empty {@link ClassOfServiceInformation} is
	 *             returned.
	 */
	public ClassOfServiceInformation getCosByName(String cosName) throws EmailIntegrationException;

	/**
	 * Gets information about class of service.
	 *
	 * @param cosId
	 *            id of the class of service.
	 * @return {@link ClassOfServiceInformation} wrapped CosInfo.
	 * @throws EmailIntegrationException
	 *             throw if a problem occurs with communication with the mail server or if a non existent CoS is queried
	 *             from the server.
	 */
	public ClassOfServiceInformation getCosById(String cosId) throws EmailIntegrationException;

	/**
	 * Extract default cos name for given domain
	 * 
	 * @param domainInfo
	 *            domain information or empty optional is domain don't exist
	 * @return default cos name
	 * @throws EmailIntegrationException
	 *             throw if cos can not be extracted
	 */
	public String extractCosFromDomainAddress(Optional<DomainInformation> domainInfo) throws EmailIntegrationException;

	/**
	 * Modifies class of service
	 *
	 * @param cosId
	 *            id of the class of service to be modified
	 * @param attributeName
	 *            name of the attribute
	 * @param attributeValue
	 *            new value
	 * @throws EmailIntegrationException
	 *             throw if a problem occurs with communication with the mail server.
	 */
	public void modifyCos(String cosId, String attributeName, String attributeValue) throws EmailIntegrationException;

	/**
	 * Deletes a class of service from the mail server.
	 *
	 * @param cosId
	 *            id of the cos to be removed.
	 * @throws EmailIntegrationException
	 *             throw if a problem occurs with communication with the mail server.
	 */
	public void deleteCos(String cosId) throws EmailIntegrationException;
}
