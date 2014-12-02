/**
 * 
 */
package com.sirma.itt.cmf.integration;

/**
 * Service that will provide config information for access to DMS system from
 * CMF
 * 
 * REVIEW za iztrivane
 * 
 * @author bbanchev
 * 
 */
public interface SysAdminService {

	/**
	 * Gets DMS context - as /alfresco
	 * 
	 * @return DMS context
	 */
	public String getDMSContext();

	/**
	 * Gets DMS host. as locahlost
	 * 
	 * @return DMS host
	 */
	public String getDMSHost();

	/**
	 * Gets DMS port. as 8080
	 * 
	 * @return DMS port
	 */
	public int getDMSPort();

	/**
	 * Gets DMS protocole. as http
	 * 
	 * @return DMS protocole
	 */
	public String getDMSProtocol();

}
