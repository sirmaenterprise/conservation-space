/**
 *
 */
package com.sirma.seip.semantic.management;

import java.net.URI;

/**
 * Contains fields that are needed for creation of semantic repository
 *
 * @author kirq4e
 */
public class RepositoryConfiguration {

	private RepositoryInfo info = new RepositoryInfo();
	private String label;
	@SuppressWarnings("boxing")
	private Long entityIndexSize = 2000000L;
	private String cacheMemory = "512m";
	private String tupleIndexMemory = "512m";

	/**
	 * @return the label
	 */
	public String getLabel() {
		return label;
	}

	/**
	 * @param label
	 *            the label to set
	 */
	public void setLabel(String label) {
		this.label = label;
	}

	/**
	 * Gets the reposioty name.
	 *
	 * @return the reposioty name
	 */
	public String getRepositoryName() {
		return info.getRepositoryName();
	}

	/**
	 * Sets the reposioty name.
	 *
	 * @param name
	 *            the new reposioty name
	 */
	public void setRepositoryName(String name) {
		info.setRepositoryName(name);
	}

	/**
	 * @return the userName
	 */
	public String getUserName() {
		return info.getUserName();
	}

	/**
	 * @param userName
	 *            the userName to set
	 */
	public void setUserName(String userName) {
		info.setUserName(userName);
	}

	/**
	 * @return the password
	 */
	public String getPassword() {
		return info.getPassword();
	}

	/**
	 * @param password
	 *            the password to set
	 */
	public void setPassword(String password) {
		info.setPassword(password);
	}

	/**
	 * @return the address
	 */
	public URI getAddress() {
		return info.getAddress();
	}

	/**
	 * @param address
	 *            the address to set
	 */
	public void setAddress(URI address) {
		info.setAddress(address);
	}

	/**
	 * @return the entityIndexSize
	 */
	public Long getEntityIndexSize() {
		return entityIndexSize;
	}

	/**
	 * @param entityIndexSize
	 *            the entityIndexSize to set
	 */
	public void setEntityIndexSize(Long entityIndexSize) {
		this.entityIndexSize = entityIndexSize;
	}

	/**
	 * @return the cacheMemory
	 */
	public String getCacheMemory() {
		return cacheMemory;
	}

	/**
	 * @param cacheMemory
	 *            the cacheMemory to set
	 */
	public void setCacheMemory(String cacheMemory) {
		this.cacheMemory = cacheMemory;
	}

	/**
	 * @return the tupleIndexMemory
	 */
	public String getTupleIndexMemory() {
		return tupleIndexMemory;
	}

	/**
	 * @param tupleIndexMemory
	 *            the tupleIndexMemory to set
	 */
	public void setTupleIndexMemory(String tupleIndexMemory) {
		this.tupleIndexMemory = tupleIndexMemory;
	}

	/**
	 * Gets the info.
	 *
	 * @return the info
	 */
	public RepositoryInfo getInfo() {
		return info;
	}

}
